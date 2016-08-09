package factorio.window.treecell;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import factorio.Util;
import factorio.calculator.AssemblerSettings;
import factorio.calculator.Calculation;
import factorio.data.Data;

/**
 * A {@link TreeCell} for the total number of assemblers of a given type required in a {@link Calculation}. Also displays energy
 * requirements.
 * @author ricky3350
 */
public class TotalAssemblerCount implements TreeCell, Comparable<TotalAssemblerCount> {

	private final AssemblerSettings assembler;
	private double assemblerCount;

	public TotalAssemblerCount(AssemblerSettings assembler) {
		this.assembler = assembler;
	}

	/**
	 * <ul>
	 * <b><i>add</i></b><br>
	 * <pre>public void add(double assemblerCount)</pre> Adds the given number of assemblers to the total
	 * @param assemblerCount - the number of assemblers to add
	 *        </ul>
	 */
	public void add(double assemblerCount) {
		this.assemblerCount += assemblerCount;
	}

	/**
	 * <ul>
	 * <b><i>getAssembler</i></b><br>
	 * <pre>public {@link AssemblerSettings} getAssembler()</pre>
	 * @return the {@code AssemblerSettings} that is being totaled
	 *         </ul>
	 */
	public AssemblerSettings getAssembler() {
		return this.assembler;
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		final String power = this.assembler.getAssembler().burnerPowered || this.assembler.getAssembler().energy < 0.0001 ? "" : " requires <b>" + Util.formatEnergy(this.assemblerCount * this.assembler.getAssembler().energy * this.assembler.getEfficiency()) + "</b>";

		final JLabel ret = new JLabel(String.format("<html><b>%s</b> %s %s%s</html>", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true), power), TreeCell.ICON_BLANK, SwingConstants.LEADING);
		ret.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		TreeCell.addBorders(ret, selected, hasFocus);

		return ret;
	}

	@Override
	public String getRawString() {
		final String power = this.assembler.getAssembler().burnerPowered || this.assembler.getAssembler().energy < 0.0001 ? "" : " requires " + Util.formatEnergy(this.assemblerCount * this.assembler.getAssembler().energy * this.assembler.getEfficiency());

		return String.format("%s %s%s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(false), power);
	}

	@Override
	public int compareTo(TotalAssemblerCount o) {
		return this.assembler.compareTo(o.assembler);
	}

}
