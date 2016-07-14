package factorio.window;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import factorio.data.Data;
import factorio.data.Recipe;

public class RecipeListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -1000369904153517159L;

	private String searchKey = "";

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof Recipe && c instanceof JLabel) {
			JPanel panel = new JPanel(new BorderLayout());
			
			JLabel label = (JLabel) c;
			Recipe recipe = (Recipe) value;

			label.setIcon(recipe.getIcon());

			String name = Data.nameFor(recipe), pre = "", search = "", post = "";
			if (!name.toLowerCase().contains(searchKey)) {
				pre = name;
			} else {
				int ix = name.toLowerCase().indexOf(searchKey);
				pre = name.substring(0, ix);
				search = name.substring(ix, ix + searchKey.length());
				post = name.substring(ix + searchKey.length());
			}

			label.setText(String.format("<html>%s<font color=\"#8b0000\">%s</font>%s</html>", pre, search, post));
			panel.add(label, BorderLayout.LINE_START);
			
			panel.add(new JTextField(10), BorderLayout.LINE_END);
			
			panel.add(Box.createHorizontalGlue());
			
			return panel;
		}

		return c;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey == null ? "" : searchKey.toLowerCase();
	}

}
