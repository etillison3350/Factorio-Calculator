package factorio.window.treecell;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import factorio.data.Recipe;

public interface TreeCell {

	public static final Icon ICON_BLANK = new ImageIcon(new BufferedImage(1, Recipe.SMALL_ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE));

	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus);

	public String getRawString();

	/**
	 * <ul>
	 * <b><i>addBorders</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void addBorders(Component c, boolean sel)</code><br>
	 * <br>
	 * Configures the given <code>Component</code>'s borders and background based on whether or not it's selected
	 * @param c - The <code>Component</code> to configure
	 * @param sel - Whether or not the component is selected
	 * @param hasFocus - Whether or not the component has focus
	 *        </ul>
	 */
	public static void addBorders(JComponent c, boolean sel, boolean hasFocus) {
		c.setOpaque(true);
		c.setBackground(UIManager.getColor(sel ? "Tree.selectionBackground" : "Tree.background"));
		c.setBorder(hasFocus ? BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor")) : BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}

}
