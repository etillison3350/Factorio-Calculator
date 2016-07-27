package factorio.calculator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import factorio.Util;
import factorio.data.Assembler;
import factorio.data.Data;
import factorio.data.Module;
import factorio.data.Recipe;

public class AssemblerSettings implements Comparable<AssemblerSettings> {

	private static Map<String, AssemblerSettings> defaultSettings = new HashMap<>(); // TODO implement

	private Assembler assembler;
	private Module[] modules;

	public AssemblerSettings(Assembler assembler, Module... modules) {
		this.assembler = assembler;
		this.modules = modules;
	}

	public Assembler getAssembler() {
		return assembler;
	}

	/**
	 * <ul>
	 * <b><i>setAssembler</i></b><br>
	 * <br>
	 * <code>&nbsp;void setAssembler()</code><br>
	 * <br>
	 * Sets this {@code AssemblerSettings}' assembler.
	 * @param assembler - The assembler to set
	 * @throws NullPointerException if <code>assembler</code> is <code>null</code>
	 *         </ul>
	 */
	public void setAssembler(Assembler assembler) {
		if (assembler == null) throw new NullPointerException();
		this.assembler = assembler;
	}

	public Module[] getModules() {
		return modules;
	}

	public void setModules(Module... modules) {
		this.modules = modules;
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

	public static AssemblerSettings getDefaultSettings(Recipe recipe) {
		return getDefaultSettings(recipe.type, recipe.getIngredients().size());
	}

	public static AssemblerSettings getDefaultSettings(String recipeType, int ingredients) {
		// TODO
		for (Assembler a : Data.getAssemblers()) {
			if (a.canCraftCategory(recipeType) && a.ingredients >= ingredients && Math.random() < 0.5) return new AssemblerSettings(a, Data.getModules().stream().limit((long) Math.floor(a.modules * Math.random())).toArray(size -> new Module[size]));
		}
		return getDefaultSettings(recipeType, ingredients);
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
			if (e) bonus += (s || p ? ", " : "") + String.format(format, "#4C8818", Util.MODULE_FORMAT.format(efficiency), "efficiency");

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

	@Override
	public int compareTo(AssemblerSettings o) {
		int d = this.assembler.compareCategoriesTo(o.assembler);
		if (d != 0) return d;
		return Double.compare(this.assembler.speed * this.getSpeed(), o.assembler.speed * o.getSpeed());
	}
}
