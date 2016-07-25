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

	public Component getTreeCellRendererComponent(boolean selected);
	
	/**
	 * <ul>
	 * <b><i>addBorders</i></b><br>
	 * <br>
	 * <code>&nbsp;public static void addBorders(Component c, boolean sel)</code><br>
	 * <br>
	 * Configures the given <code>Component</code>'s borders and background based on whether or not it's selected
	 * @param c - The <code>Component</code> to configure
	 * @param sel - Whether or not the component is selected
	 * </ul>
	 */
	public static void addBorders(JComponent c, boolean sel) {
		c.setOpaque(true);
		
		if (sel) {
			c.setBackground(UIManager.getColor("Tree.selectionBackground"));
			c.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor")));
		} else {
			c.setBackground(UIManager.getColor("Tree.background"));
			c.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}
	}
	
}
