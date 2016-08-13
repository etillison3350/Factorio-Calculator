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

/**
 * The {@code AssemblerSettings} class specifies the properties of an assembler, and contains the assembler type, what modules
 * it would have, and the fuel that would be used. This information is used to calculate its final speed, productivity, and
 * energy consumption. It also has static methods for storing and retieving default {@code AssemblerSettings} for given
 * {@link Recipe}s
 * @author ricky3350
 */
public class AssemblerSettings implements Comparable<AssemblerSettings> {

	/**
	 * Maps recipe category names to the default {@code AssemblerSettings} to use for that category
	 */ // TODO change based on user input
	private static Map<String, AssemblerSettings> defaultSettings = new HashMap<>();

	/**
	 * The name fo the default fuel item for burner assemblers
	 */
	private static String defaultFuel /* TODO */ = "coal";

	private static final Comparator<Assembler> ASSEMBLER_COMPARE = (o1, o2) -> {
		int d = -Integer.compare(o1.ingredients, o2.ingredients);
		if (d != 0) return d;
		d = Boolean.compare(o1.burnerPowered, o2.burnerPowered);
		if (d != 0) return d;
		d = -Double.compare(o1.speed, o2.speed);
		if (d != 0) return d;
		return o1.name.compareTo(o2.name);
	};

	/**
	 * The shutdown hook to write the settings to file
	 */
	private static final Thread WRITE_SETTINGS = new Thread(() -> {
		final Path settings = Paths.get("config/defaults.cfg");

		final String settingsString = defaultSettings.keySet().stream().map(str -> str + '=' + defaultSettings.get(str)).collect(Collectors.joining("\n"));

		try {
			Files.createDirectory(Paths.get("config"));
			Files.write(settings, settingsString.getBytes());
			Files.createFile(settings);
		} catch (final IOException e) {}
	});

	/**
	 * The {@link Path} of the saved assembler settings
	 */
	private static final Path SETTINGS_PATH = Paths.get("config/defaults.cfg");

	/**
	 * The assembler for this {@code AssemblerSettings}
	 */
	private final Assembler assembler;

	/**
	 * An array of {@link Module}s that {@link #assembler} would have in it
	 */
	private final Module[] modules;

	/**
	 * The name of the item used as fuel for {@link #assembler} if it is {@linkplain Assembler#burnerPowered burner powered},
	 * <code>null</code> otherwise
	 */
	private final String fuel;

	/**
	 * Creates a new {@code AssemblerSettings} with the given assembler and modules, and a fuel of <code>null</code>.
	 * @param assembler - The assembler
	 * @param modules - A list of modules for the assembler
	 * @throws IllegalArgumentException If {@code assembler} is <code>null</code> or {@linkplain Assembler#burnerPowered burner
	 *         powered}
	 * @see {@link #AssemblerSettings(Assembler, String, Module...)}
	 */
	public AssemblerSettings(final Assembler assembler, final Module... modules) {
		this(assembler, null, modules);
	}

	/**
	 * Creates a new {@code AssemblerSettings} with the given assembler, modules, and fuel
	 * @param assembler - The assembler
	 * @param fuel - The fuel for the assembler. Can be <code>null</code> if it is not burner powered
	 * @param modules - A list of modules for the assembler
	 * @throws IllegalArgumentException If
	 *         <ul>
	 *         <li>{@code assembler} is <code>null</code></li>
	 *         <li>{@code assembler} is {@linkplain Assembler#burnerPowered burner powered}, and <code>fuel</code> is
	 *         <code>null</code>, or {@link Data#getFuelValue(String)} is non-positive for <code>fuel</code></li>
	 *         </ul>
	 * @see {@link #AssemblerSettings(Assembler, Module...)}
	 */
	public AssemblerSettings(final Assembler assembler, final String fuel, final Module... modules) {
		if (assembler == null) throw new IllegalArgumentException("assembler cannot be null");
		if (assembler.burnerPowered && (fuel == null || Data.getFuelValue(fuel) <= 0)) throw new IllegalArgumentException(String.format("Illegal fuel \"%s\" for burner assembler \"%s\"", fuel, assembler));

		this.assembler = assembler;
		this.modules = new Module[modules.length];
		System.arraycopy(modules, 0, this.modules, 0, modules.length);
		this.fuel = assembler.burnerPowered ? fuel : null;
	}

	/**
	 * Creates a new {@code AssemblerSettings} from a {@link String}, parsed as the reverse operation of {@link #toString()}
	 * @param str - The {@code String} representation of an {@code AssemblerSettings}
	 */
	private AssemblerSettings(final String str) {
		final String[] parts = str.split("\\|", 2);
		final String[] aparts = parts[0].split("\\&");
		Assembler assembler = null;
		for (final Assembler a : Data.getAssemblers())
			if (a.name.equals(aparts[0])) {
				assembler = a;
				break;
			}
		if (assembler == null) throw new IllegalArgumentException("Could not find assembler " + parts[0]);
		this.assembler = assembler;

		if (this.assembler.burnerPowered) {
			if (aparts.length > 1 && Data.getFuelValue(aparts[1]) >= 0)
				this.fuel = aparts[1];
			else
				this.fuel = defaultFuel;
		} else
			this.fuel = null;

		final List<Module> modules = new ArrayList<>();
		if (parts.length > 1) for (final String m : parts[1].split("\\+"))
			for (final Module module : Data.getModules())
				if (module.name.equals(m)) {
					modules.add(module);
					break;
				}
		this.modules = modules.toArray(new Module[modules.size()]);
	}

	/**
	 * <ul>
	 * <b><i>getDefaultDefaults</i></b><br>
	 * <pre> public static AssemblerSettings getDefaultDefaults({@link String} recipeType)</pre> Calculates and returns the
	 * default {@code AssemblerSettings} for the given recipe crafting category. This has an {@link Assembler} that can craft
	 * the given crafting category and has the higenst maximum ingredient count of all such {@code Assembler}s in
	 * {@link Data#getAssemblers()}, the defualt fuel, and no {@link Module}s. For use when no defualt assembler has been set
	 * for the given category.
	 * @param recipeType - The crafting category to find a default {@code AssemblerSettings} for
	 * @return The default {@code AssemblerSettings}, as described above
	 * @see {@link #getDefaultSettings(Recipe)}
	 *      </ul>
	 */
	public static AssemblerSettings getDefaultDefaults(final String recipeType) {
		return new AssemblerSettings(Data.getAssemblers().stream().filter(a -> a.canCraftCategory(recipeType)).sorted(ASSEMBLER_COMPARE).findFirst().get(), defaultFuel);
	}

	/**
	 * <ul>
	 * <b><i>getDefaultSettings</i></b><br>
	 * <pre> public static AssemblerSettings getDefaultSettings({@link Recipe} recipe)</pre> Gets the default
	 * {@code AssemblerSettings} for the given recipe.<br>
	 * <br>
	 * If an {@code AssemblerSettings} has been stored for the recipe's category (from {@link #readSettings()}, past invocations
	 * of this method, or TODO), the stored settings is returned if it is valid for the given recipe. If it is invalid:
	 * <ul>
	 * <li>If the stored settings' assembler cannot craft the given recipe because it requires too many ingredients, the
	 * assembler worst assembler than can is used instead; i.e. the assembler that can craft recipes with at most <i>n</i>
	 * ingredients is used, where <i>n</i> has the minimum value among all {@link Assembler}s in {@link Data#getAssemblers()},
	 * but is greater than or equal to both the maximum number of ingredients of the stored assembler and the number of
	 * ingredients required for the given recipe.
	 * <li>Modules that cannot be used for the given recipe in the given assembler (see {@link Module#canCraft(String)}) are
	 * removed.
	 * </ul>
	 * Otherwise, the default assembler is calculated using {@link #getDefaultDefaults(String)}, stored, and returned.
	 * @param recipe - The recipe to find {@code AssemblerSettings} for.
	 * @return The default {@code AssemblerSettings} for the given recipe, by the rules above.
	 * @throws IllegalArgumentException If no assembler could be found that can craft the given recipe.
	 *         </ul>
	 */
	public static AssemblerSettings getDefaultSettings(final Recipe recipe) {
		if (defaultSettings.containsKey(recipe.category)) {
			AssemblerSettings ret = defaultSettings.get(recipe.category);
			if (ret.assembler.ingredients < recipe.getIngredients().size()) {
				final NavigableSet<Assembler> assemblers = new TreeSet<>(ASSEMBLER_COMPARE);
				assemblers.addAll(Data.getAssemblers());
				assemblers.removeIf(a -> !a.canCraftCategory(recipe.category));
				do {
					ret = new AssemblerSettings(assemblers.higher(ret.assembler), ret.fuel, ret.modules);
					if (ret.assembler == null) throw new IllegalArgumentException("Too many ingredients");
				} while (ret.assembler.ingredients < recipe.getIngredients().size());
			}

			final List<Module> modules = new ArrayList<>(Arrays.asList(ret.modules));
			modules.removeIf(m -> !m.canCraft(recipe.name));
			ret = new AssemblerSettings(ret.assembler, ret.fuel, modules.toArray(new Module[modules.size()]));
			return ret;
		}
		final AssemblerSettings ret = getDefaultDefaults(recipe.category);
		if (ret.assembler.ingredients < recipe.getIngredients().size()) throw new IllegalArgumentException("Too many ingredients");
		defaultSettings.put(recipe.category, ret);
		return ret;
	}

	/**
	 * <ul>
	 * <b><i>readSettings</i></b><br>
	 * <pre> public static void readSettings()</pre> Reads the default assemblers from {@link #SETTINGS_PATH}
	 * </ul>
	 */
	public static void readSettings() {
		if (Files.exists(SETTINGS_PATH)) try {
			Files.readAllLines(SETTINGS_PATH).forEach(str -> {
				final String[] parts = str.split("=", 2);
				if (parts.length > 1) defaultSettings.put(parts[0], new AssemblerSettings(parts[1]));
			});
		} catch (final IOException e) {}

		try {
			Runtime.getRuntime().addShutdownHook(WRITE_SETTINGS);
		} catch (final Exception e) {}
	}

	@Override
	public int compareTo(final AssemblerSettings o) {
		int d = this.assembler.compareCategoriesTo(o.assembler);
		if (d != 0) return d;
		d = Double.compare(this.assembler.speed * this.getSpeed(), o.assembler.speed * o.getSpeed());
		if (d != 0) return d;
		d = -Boolean.compare(this.assembler.burnerPowered, o.assembler.burnerPowered);
		if (d != 0) return d;
		return Integer.compare(this.assembler.ingredients, o.assembler.ingredients);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		final AssemblerSettings other = (AssemblerSettings) obj;
		if (this.assembler == null) {
			if (other.assembler != null) return false;
		} else if (!this.assembler.equals(other.assembler)) return false;

		final List<Module> m1 = Arrays.asList(this.modules);
		final List<Module> m2 = Arrays.asList(other.modules);
		Collections.sort(m1, Comparator.comparingInt(m -> m.hashCode()));
		Collections.sort(m2, Comparator.comparingInt(m -> m.hashCode()));

		if (!m1.equals(m2)) return false;
		return true;
	}

	/**
	 * <ul>
	 * <b><i>getAssembler</i></b><br>
	 * <pre> public {@link Assembler} getAssembler()</pre>
	 * @return The assembler
	 *         </ul>
	 */
	public Assembler getAssembler() {
		return this.assembler;
	}

	/**
	 * <ul>
	 * <b><i>getBonusString</i></b><br>
	 * <pre>public {@link String} getBonusString(boolean html)</pre> Gets the string for the bonuses given by this
	 * {@code AssemblerSettings}' {@link Module}s. The string includes the bonus value formatted by {@link Util#MODULE_FORMAT}
	 * for each nonzero module bonus value (the empty string is returned if there is no bonus for any). If <code>html</code> is
	 * <code>true</code>, then each number is formatted to the approporate color (using html). Otherwise, the names of each
	 * bonus (speed, productivity, or consumption) are used following the value, without html.
	 * @param html - Whether or not to format the string as html
	 * @return The bonus string
	 *         </ul>
	 */
	public String getBonusString(final boolean html) {
		final String format = html ? "<font color=\"%1$s\">%2$s</font>" : "%2$s %3$s";

		final double speed = this.getSpeed() - 1;
		final double productivity = this.getProductivity() - 1;
		final double efficiency = this.getEfficiency() - 1;

		final boolean s = Math.abs(speed) > 0.0001;
		final boolean p = Math.abs(productivity) > 0.0001;
		final boolean e = Math.abs(efficiency) > 0.0001;

		if (s || p || e) {
			String bonus = " (";

			if (s) bonus += String.format(format, "#0457FF", Util.MODULE_FORMAT.format(speed), "speed");
			if (p) bonus += (s ? ", " : "") + String.format(format, "#AD4ECC", Util.MODULE_FORMAT.format(productivity), "productivity");
			if (e) bonus += (s || p ? ", " : "") + String.format(format, "#4C8818", Util.MODULE_FORMAT.format(efficiency), "consumption");

			return bonus + ")";
		}
		return "";
	}

	/**
	 * <ul>
	 * <b><i>getEfficiency</i></b><br>
	 * <pre> public double getEfficiency()</pre>
	 * @return the greater of the efficiency multiplier of this {@code AssemblerSettings} and 0.2 (the minimum energy
	 *         consumption).
	 *         </ul>
	 */
	public double getEfficiency() {
		return Math.max(0.2, 1 + Arrays.stream(this.modules).mapToDouble(module -> module.getEffectValue("consumption")).sum());
	}

	public String getFuel() {
		return this.fuel;
	}

	/**
	 * <ul>
	 * <b><i>getModules</i></b><br>
	 * <pre> public {@link Module}[] getModules()</pre>
	 * @return The modules for this assembler
	 *         </ul>
	 */
	public Module[] getModules() {
		return this.modules;
	}

	/**
	 * <ul>
	 * <b><i>getProductivity</i></b><br>
	 * <pre> public double getProductivity()</pre>
	 * @return the productivity multiplier of this {@code AssemblerSettings}
	 *         </ul>
	 */
	public double getProductivity() {
		return 1 + Arrays.stream(this.modules).mapToDouble(module -> module.getEffectValue("productivity")).sum();
	}

	/**
	 * <ul>
	 * <b><i>getSpeed</i></b><br>
	 * <pre> public double getSpeed()</pre>
	 * @return the speed multiplier of this {@code AssemblerSettings}
	 *         </ul>
	 */
	public double getSpeed() {
		return 1 + Arrays.stream(this.modules).mapToDouble(module -> module.getEffectValue("speed")).sum();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.assembler == null ? 0 : this.assembler.hashCode());
		final List<Module> m1 = Arrays.asList(this.modules);
		Collections.sort(m1, Comparator.comparingInt(m -> m.hashCode()));
		result = prime * result + m1.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return this.assembler.name + (this.fuel == null ? "" : "&" + this.fuel) + "|" + Arrays.stream(this.modules).map(m -> m.name).collect(Collectors.joining("+"));
	}
}
