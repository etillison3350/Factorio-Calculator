package factorio.calculator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import factorio.Util;
import factorio.data.Assembler;
import factorio.data.Data;
import factorio.data.Module;
import factorio.data.Recipe;

public class AssemblerSettings implements Comparable<AssemblerSettings> {

	private static Map<String, AssemblerSettings> defaultSettings = new HashMap<>(); // TODO finish

	private static final Comparator<Assembler> ASSEMBLER_COMPARE = new Comparator<Assembler>() {

		@Override
		public int compare(Assembler o1, Assembler o2) {
			int d = -Integer.compare(o1.ingredients, o2.ingredients);
			if (d != 0) return d;
			d = Boolean.compare(o1.burnerPowered, o2.burnerPowered);
			if (d != 0) return d;
			d = -Float.compare(o1.speed, o2.speed);
			if (d != 0) return d;
			return o1.name.compareTo(o2.name);
		}

	};

	/**
	 * The shutdown hook to write the settings to file
	 */
	private static final Thread WRITE_SETTINGS = new Thread(new Runnable() {

		@Override
		public void run() {
			Path settings = Paths.get("config/defaults.cfg");

			String settingsString = defaultSettings.keySet().stream().map(str -> str + '=' + defaultSettings.get(str)).collect(Collectors.joining("\n"));

			try {
				Files.createDirectory(Paths.get("config"));
				Files.write(settings, settingsString.getBytes());
				Files.createFile(settings);
			} catch (IOException e) {}
		}
	});

	public static void readSettings() {
		Path settings = Paths.get("config/defaults.cfg");
		if (Files.exists(settings)) {
			try {
				Files.readAllLines(settings).forEach(str -> {
					String[] parts = str.split("=", 2);
					if (parts.length > 1) defaultSettings.put(parts[0], new AssemblerSettings(parts[1]));
				});
			} catch (IOException e) {}
		}

		try {
			Runtime.getRuntime().addShutdownHook(WRITE_SETTINGS);
		} catch (Exception e) {}
	}

	private final Assembler assembler;
	private final Module[] modules;

	public AssemblerSettings(Assembler assembler, Module... modules) {
		this.assembler = assembler;
		this.modules = modules;
	}

	private AssemblerSettings(String str) {
		String[] parts = str.split("\\|", 2);
		Assembler assembler = null;
		for (Assembler a : Data.getAssemblers()) {
			if (a.name.equals(parts[0])) {
				assembler = a;
				break;
			}
		}
		if (assembler == null) throw new IllegalArgumentException("Could not find assembler " + parts[0]);
		this.assembler = assembler;

		List<Module> modules = new ArrayList<>();
		if (parts.length > 1) {
			for (String m : parts[1].split("\\+")) {
				for (Module module : Data.getModules()) {
					if (module.name.equals(m)) {
						modules.add(module);
						break;
					}
				}
			}
		}
		this.modules = modules.toArray(new Module[modules.size()]);
	}

	public Assembler getAssembler() {
		return assembler;
	}

	public Module[] getModules() {
		return modules;
	}

	/**
	 * <ul>
	 * <b><i>getSpeed</i></b><br>
	 * <br>
	 * <code>&nbsp;public double getSpeed()</code><br>
	 * <br>
	 * @return the speed multiplier of this {@code AssemblerSettings}
	 *         </ul>
	 */
	public double getSpeed() {
		return 1 + (double) Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("speed")).sum();// .reduce(1, (a, b) -> a + b);
	}

	/**
	 * <ul>
	 * <b><i>getProductivity</i></b><br>
	 * <br>
	 * <code>&nbsp;public double getProductivity()</code><br>
	 * <br>
	 * @return the productivity multiplier of this {@code AssemblerSettings}
	 *         </ul>
	 */
	public double getProductivity() {
		return 1 + (double) Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("productivity")).sum();// .reduce(1, (a, b) -> a + b);
	}

	/**
	 * <ul>
	 * <b><i>getEfficiency</i></b><br>
	 * <br>
	 * <code>&nbsp;public double getEfficiency()</code><br>
	 * <br>
	 * @return the greater of the efficiency multiplier of this {@code AssemblerSettings} and 0.2 (the minimum energy consumption).
	 *         </ul>
	 */
	public double getEfficiency() {
		return (double) Math.max(0.2, 1 + Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("consumption")).sum());// .reduce(1, (a, b) -> a + b));
	}

	public static AssemblerSettings getDefaultDefaults(String recipeType) {
		return new AssemblerSettings(Data.getAssemblers().stream().filter(a -> a.canCraftCategory(recipeType)).sorted(ASSEMBLER_COMPARE).findFirst().get());
	}

	public static AssemblerSettings getDefaultSettings(Recipe recipe) {
		if (defaultSettings.containsKey(recipe.type)) {
			AssemblerSettings ret = defaultSettings.get(recipe.type);
			if (ret.assembler.ingredients < recipe.getIngredients().size()) {
				NavigableSet<Assembler> assemblers = new TreeSet<>(ASSEMBLER_COMPARE);
				assemblers.addAll(Data.getAssemblers());
				assemblers.removeIf(a -> !a.canCraftCategory(recipe.type));
				do {
					ret = new AssemblerSettings(assemblers.higher(ret.assembler), ret.modules);
					if (ret.assembler == null) throw new IllegalArgumentException("Too many ingredients");
				} while (ret.assembler.ingredients < recipe.getIngredients().size());
			}

			List<Module> modules = new ArrayList<>(Arrays.asList(ret.modules));
			modules.removeIf(m -> !m.canCraft(recipe.name));
			ret = new AssemblerSettings(ret.assembler, modules.toArray(new Module[modules.size()]));
			return ret;
		}
		AssemblerSettings ret = getDefaultDefaults(recipe.type);
		defaultSettings.put(recipe.type, ret);
		return ret;
	}

	public String getBonusString(boolean html) {
		final String format = html ? "<font color=\"%1$s\">%2$s</font>" : "%2$s %3$s";

		double speed = this.getSpeed() - 1;
		double productivity = this.getProductivity() - 1;
		double efficiency = this.getEfficiency() - 1;

		boolean s = Math.abs(speed) > 0.0001;
		boolean p = Math.abs(productivity) > 0.0001;
		boolean e = Math.abs(efficiency) > 0.0001;

		if (s || p || e) {
			String bonus = " (";

			if (s) bonus += String.format(format, "#0457FF", Util.MODULE_FORMAT.format(speed), "speed");
			if (p) bonus += (s ? ", " : "") + String.format(format, "#AD4ECC", Util.MODULE_FORMAT.format(productivity), "productivity");
			if (e) bonus += (s || p ? ", " : "") + String.format(format, "#4C8818", Util.MODULE_FORMAT.format(efficiency), "consumption");

			return bonus + ")";
		}
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assembler == null) ? 0 : assembler.hashCode());
		List<Module> m1 = Arrays.asList(modules);
		Collections.sort(m1, Comparator.comparingInt(m -> m.hashCode()));
		result = prime * result + m1.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AssemblerSettings other = (AssemblerSettings) obj;
		if (assembler == null) {
			if (other.assembler != null) return false;
		} else if (!assembler.equals(other.assembler)) {
			return false;
		}

		List<Module> m1 = Arrays.asList(modules);
		List<Module> m2 = Arrays.asList(other.modules);
		Collections.sort(m1, Comparator.comparingInt(m -> m.hashCode()));
		Collections.sort(m2, Comparator.comparingInt(m -> m.hashCode()));

		if (!m1.equals(m2)) return false;
		return true;
	}

	public String toString() {
		return this.assembler.name + "|" + Arrays.stream(this.modules).map(m -> m.name).collect(Collectors.joining("+"));
	}

	@Override
	public int compareTo(AssemblerSettings o) {
		int d = this.assembler.compareCategoriesTo(o.assembler);
		if (d != 0) return d;
		d = Double.compare(this.assembler.speed * this.getSpeed(), o.assembler.speed * o.getSpeed());
		if (d != 0) return d;
		d = -Boolean.compare(this.assembler.burnerPowered, o.assembler.burnerPowered);
		if (d != 0) return d;
		return Integer.compare(this.assembler.ingredients, o.assembler.ingredients);
	}
}
