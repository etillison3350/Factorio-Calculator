package factorio.window.treecell;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import factorio.Util;
import factorio.calculator.AssemblerSettings;
import factorio.data.Data;

public class TotalAssemblerCount implements TreeCell, Comparable<TotalAssemblerCount> {

	private final AssemblerSettings assembler;
	private double assemblerCount;

	public TotalAssemblerCount(AssemblerSettings assembler) {
		this.assembler = assembler;
	}

	public void add(double assemblerCount) {
		this.assemblerCount += assemblerCount;
	}

	public AssemblerSettings getAssembler() {
		return assembler;
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		String power = assembler.getAssembler().burnerPowered || assembler.getAssembler().energy < 0.0001 ? "" : " requires <b>" + Util.formatEnergy((double) assemblerCount * assembler.getAssembler().energy) + "</b>";

		JLabel ret = new JLabel(String.format("<html><b>%s</b> %s %s%s</html>", Util.NUMBER_FORMAT.format(assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true), power), TreeCell.ICON_BLANK, SwingConstants.LEADING);
		ret.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		TreeCell.addBorders(ret, selected, hasFocus);

		return ret;
	}
	
	@Override
	public String getRawString() {
		String power = assembler.getAssembler().burnerPowered || assembler.getAssembler().energy < 0.0001 ? "" : " requires " + Util.formatEnergy((double) assemblerCount * assembler.getAssembler().energy);

		return String.format("%s %s%s%s", Util.NUMBER_FORMAT.format(assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(false), power);
	}

	@Override
	public int compareTo(TotalAssemblerCount o) {
		return this.assembler.compareTo(o.assembler);
	}

}
