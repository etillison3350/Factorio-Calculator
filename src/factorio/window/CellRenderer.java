package factorio.window;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import factorio.calculator.CalculatedRecipe;
import factorio.data.Data;
import factorio.data.Recipe;

public class CellRenderer extends DefaultTreeCellRenderer {

	private static final Icon BLANK = new ImageIcon(new BufferedImage(1, Recipe.SMALL_ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE));
	
	private static final long serialVersionUID = 1970214071933272417L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		if (!(value instanceof DefaultMutableTreeNode) || !(((DefaultMutableTreeNode) value).getUserObject() instanceof CalculatedRecipe)) {
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}

		CalculatedRecipe recipe = (CalculatedRecipe) ((DefaultMutableTreeNode) value).getUserObject();

		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		if (sel) {
			ret.setBackground(UIManager.getColor("Tree.selectionBackground"));
			ret.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor")));
		} else {
			ret.setBackground(UIManager.getColor("Tree.background"));
			ret.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}

//		label.setOpaque(false);
//		label.setBackground(new Color(0, 0, 0, 0));
//		label.setBorder(BorderFactory.createEmptyBorder());

		// recipe.getRecipe() == null ? Data.getItemIcon(recipe.product) : recipe.getRecipe().getSmallIcon()

		String prod = "<html><b>" + Data.nameFor(recipe.product) + "</b> at <b>" + Data.NUMBER_FORMAT.format(recipe.getRate()) + "</b> items/s";
		if (Data.hasMultipleRecipes(recipe.product)) {
			prod += " (via ";
			ret.add(new JLabel("<html><b>" + Data.nameFor(recipe.getRecipe()) + "</b> at <b>" + Data.NUMBER_FORMAT.format(recipe.getRecipeRate()) + "</b> cycles/s)</html>", recipe.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}
		ret.add(new JLabel(prod + "</html>", Data.getItemIcon(recipe.product), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		if (recipe.getSettings() != null) {
			String assemblerStr = String.format("<html>requires <b>%s</b> %s", Data.NUMBER_FORMAT.format(recipe.getAssemblers()), Data.nameFor(recipe.getSettings().getAssembler().name));

			float speed = recipe.getSettings().getSpeed() - 1;
			float productivity = recipe.getSettings().getProductivity() - 1;
			float efficiency = recipe.getSettings().getEfficiency() - 1;

			boolean s = Math.abs(speed) > 0.0001;
			boolean p = Math.abs(productivity) > 0.0001;
			boolean e = Math.abs(efficiency) > 0.0001;

			if (s || p || e) {
				String bonus = " (";

				if (s) bonus += "<font color=\"#0457FF\">" + Data.MODULE_FORMAT.format(speed) + "</font>";
				if (p) bonus += (s ? ", " : "") + "<font color=\"#AD4ECC\">" + Data.MODULE_FORMAT.format(productivity) + "</font>";
				if (e) bonus += (s || p ? ", " : "") + "<font color=\"#4C8818\">" + Data.MODULE_FORMAT.format(efficiency) + "</font>";
				
				assemblerStr += bonus + ")";
			}

			ret.add(new JLabel(assemblerStr + " </html>", BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}

}
