package factorio.calculator;

import java.util.Arrays;

import factorio.data.Assembler;
import factorio.data.Data;
import factorio.data.Module;

public class AssemblerSettings {

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
	 * @return the efficiency multiplier of this {@code AssemblerSettings} or 0.2 (the minimum energy consumption), whichever is higher.
	 *         </ul>
	 */
	public float getEfficiency() {
		return 1 + (float) Math.max(0.2, Arrays.stream(modules).mapToDouble(module -> module.getEffectValue("consumption")).sum());// .reduce(1, (a, b) -> a + b));
	}

	public static AssemblerSettings getDefaultSettings(String recipeType) {
		// TODO
		for (Assembler a : Data.getAssemblers()) {
			if (a.canCraftCategory(recipeType)) return new AssemblerSettings(a, Data.getModules().iterator().next());
		}
		return null;
	}
}
