package factorio.window.treecell;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import factorio.data.Recipe;

/**
 * An interface representing a basic tree cell for use in {@link CellRenderer}
 * @author ricky3350
 */
public interface TreeCell {

	/**
	 * A transparent {@link Icon} with a width of 1 and a height given by
	 * {@link Recipe#SMALL_ICON_SIZE}
	 */
	public static final Icon ICON_BLANK = new ImageIcon(new BufferedImage(1, Recipe.SMALL_ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE));

	/**
	 * <ul>
	 * <b><i>getTreeCellRendererComponent</i></b><br>
	 * <pre>public {@link Component} getTreeCellRendererComponent(boolean selected, boolean hasFocus)</pre>
	 * Get a component to render this {@code TreeCell}. This method will be used
	 * by
	 * {@link CellRenderer#getTreeCellRendererComponent(javax.swing.JTree, Object, boolean, boolean, boolean, int, boolean)}
	 * @param selected - Whether or not the cell is selected
	 * @param hasFocus - Whether or not the cell has focus
	 * @return A {@code Component} for this {@code TreeCell}
	 *         </ul>
	 */
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus);

	/**
	 * <ul>
	 * <b><i>getRawString</i></b><br>
	 * <pre>public {@link String} getRawString()</pre>
	 * @return A textual representation of this {@code TreeCell}
	 *         </ul>
	 */
	public String getRawString();

	/**
	 * <ul>
	 * <b><i>addBorders</i></b><br>
	 * <pre> public static void addBorders(Component c, boolean sel)</pre>
	 * Configures the given <code>Component</code>'s borders and background
	 * based on whether or not it's selected/has focus. Also sets the component
	 * to be {@linkplain JComponent#setOpaque(boolean) opaque}.
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
