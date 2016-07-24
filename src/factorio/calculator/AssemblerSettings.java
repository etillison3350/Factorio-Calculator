package factorio.calculator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import factorio.data.Assembler;
import factorio.data.Data;
import factorio.data.Module;

public class AssemblerSettings {

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
	public float getSpeed() {
		return 1 + (float) Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("speed")).sum();// .reduce(1, (a, b) -> a + b);
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
	public float getProductivity() {
		return 1 + (float) Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("productivity")).sum();// .reduce(1, (a, b) -> a + b);
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
	public float getEfficiency() {
		return (float) Math.max(0.2, 1 + Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("consumption")).sum());// .reduce(1, (a, b) -> a + b));
	}

	public static AssemblerSettings getDefaultSettings(String recipeType, int ingredients) {
		// TODO
		for (Assembler a : Data.getAssemblers()) {
			if (a.canCraftCategory(recipeType) && a.ingredients >= ingredients) return new AssemblerSettings(a, Data.getModules().iterator().next());
		}
		return null;
	}

	public String getBonusString() {
		float speed = this.getSpeed() - 1;
		float productivity = this.getProductivity() - 1;
		float efficiency = this.getEfficiency() - 1;

		boolean s = Math.abs(speed) > 0.0001;
		boolean p = Math.abs(productivity) > 0.0001;
		boolean e = Math.abs(efficiency) > 0.0001;

		if (s || p || e) {
			String bonus = " (";

			if (s) bonus += "<font color=\"#0457FF\">" + Data.MODULE_FORMAT.format(speed) + "</font>";
			if (p) bonus += (s ? ", " : "") + "<font color=\"#AD4ECC\">" + Data.MODULE_FORMAT.format(productivity) + "</font>";
			if (e) bonus += (s || p ? ", " : "") + "<font color=\"#4C8818\">" + Data.MODULE_FORMAT.format(efficiency) + "</font>";
			
			return bonus + ")";
		}
		return "";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assembler == null) ? 0 : assembler.hashCode());
		result = prime * result + Arrays.hashCode(modules);
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
		if (!Arrays.equals(modules, other.modules)) return false;
		return true;
	}
}
