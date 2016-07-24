package factorio.window.treecell;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import factorio.data.Data;

public class TotalHeader implements TreeCell {

	public final String text;
	public final int headerLevel;

	public TotalHeader(String text, int headerLevel) {
		this.text = text;
		this.headerLevel = headerLevel;
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected) {
		JLabel label = new JLabel(text, Data.ICON_BLANK, SwingConstants.LEADING);
		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12 + 2 * headerLevel));
		return null;
	}

}
