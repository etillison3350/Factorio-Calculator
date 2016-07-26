package factorio.window.treecell;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class CellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 7349604339432840092L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		return value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof TreeCell ? ((TreeCell) ((DefaultMutableTreeNode) value).getUserObject()).getTreeCellRendererComponent(sel, hasFocus) : super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	}

}
