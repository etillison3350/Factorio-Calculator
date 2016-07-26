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
	private float assemblerCount;

	public TotalAssemblerCount(AssemblerSettings assembler) {
		this.assembler = assembler;
	}

	public void add(float assemblerCount) {
		this.assemblerCount += assemblerCount;
	}

	public AssemblerSettings getAssembler() {
		return assembler;
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		String power = assembler.getAssembler().coalPowered ? "" : " requires <b>" + Util.formatEnergy((double) assemblerCount * assembler.getAssembler().energy) + "</b>";

		JLabel ret = new JLabel(String.format("<html><b>%s</b> %s %s%s</html>", Util.NUMBER_FORMAT.format(assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true), power), TreeCell.ICON_BLANK, SwingConstants.LEADING);
		ret.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		TreeCell.addBorders(ret, selected, hasFocus);

		return ret;
	}
	
	@Override
	public String getRawString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(TotalAssemblerCount o) {
		return this.assembler.compareTo(o.assembler);
	}

}
