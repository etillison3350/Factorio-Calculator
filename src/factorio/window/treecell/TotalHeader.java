package factorio.window.treecell;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A header for the totals section of the output
 * @author ricky3350
 */
public class TotalHeader implements TreeCell {

	/**
	 * The text to display
	 */
	public final String text;

	/**
	 * The impoerance of the header. Higher values result in larger font sizes
	 */
	public final int headerLevel;

	public TotalHeader(String text, int headerLevel) {
		this.text = text;
		this.headerLevel = headerLevel;
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		final JLabel label = new JLabel(this.text, TreeCell.ICON_BLANK, SwingConstants.LEADING);
		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12 + 2 * this.headerLevel));

		TreeCell.addBorders(label, selected, hasFocus);

		return label;
	}

	@Override
	public String getRawString() {
		return this.text;
	}

}
