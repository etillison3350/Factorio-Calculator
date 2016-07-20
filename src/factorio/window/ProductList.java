package factorio.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

import factorio.calculator.EditDistance;
import factorio.data.Data;
import factorio.data.Recipe;

public class ProductList extends JPanel {

	private static final long serialVersionUID = -2326666987175732004L;

	private String searchKey;

	private JPanel container;

	private ProductListRow[] listRows;

	public ProductList() {
		super(new BorderLayout());

		this.container = new JPanel(new GridLayout(0, 1));

		Set<Recipe> recipes = Data.getRecipesSorted();

		this.listRows = new ProductListRow[recipes.size()];

		Iterator<Recipe> iter = recipes.iterator();
		for (int i = 0; i < listRows.length; i++) {
			listRows[i] = new ProductListRow(iter.next());
		}

		this.setSearchKey("");

		this.add(container, BorderLayout.PAGE_START);
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey == null ? "" : searchKey;

		container.removeAll();

		if (this.searchKey.isEmpty()) {
			boolean even = true;
			for (ProductListRow row : listRows) {
				container.add(row);
				if ((even = !even))
					row.setBackground(Color.WHITE);
				else
					row.setBackground(new Color(238, 238, 238));
			}
//			Arrays.stream(listRows).forEach(container::add);
			container.revalidate();
			container.repaint();
		} else {
			Set<ProductListRow> rows = new TreeSet<>(new Comparator<ProductListRow>() {

				@Override
				public int compare(ProductListRow o1, ProductListRow o2) {
					int d = Double.compare(EditDistance.distance(ProductList.this.searchKey, Data.nameFor(o1.product)), EditDistance.distance(ProductList.this.searchKey, Data.nameFor(o2.product)));
					if (d != 0) return d;
					d = Data.nameFor(o1.product).compareToIgnoreCase(Data.nameFor(o2.product));
					if (d != 0) return d;
					return o1.product.name.compareTo(o2.product.name);
				}
			});
			for (ProductListRow plr : listRows) {
				double ed = EditDistance.distance(searchKey, Data.nameFor(plr.product));
				if (ed < Double.POSITIVE_INFINITY) { // TODO set value
					rows.add(plr);
				}
			}

			boolean even = true;
			for (ProductListRow row : rows) {
				container.add(row);
				if ((even = !even))
					row.setBackground(Color.WHITE);
				else
					row.setBackground(new Color(238, 238, 238));
			}
//			rows.forEach(container::add);
			container.revalidate();
			container.repaint();
		}
	}
}
