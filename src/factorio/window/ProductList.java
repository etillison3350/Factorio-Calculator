package factorio.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;

import factorio.calculator.EditDistance;
import factorio.data.Data;
import factorio.data.Recipe;

/**
 * A panel containing a {@link ProductListRow} for each {@link Recipe} in {@link Data#getRecipes()}
 * @author ricky3350
 */
public class ProductList extends JPanel {

	private static final long serialVersionUID = -2326666987175732004L;

	/**
	 * The string that is in the earch field.
	 */
	private String searchKey;

	/**
	 * The container for the rows, to make the rows stay at the top of the panel instead of spreading over the entire thing.
	 */
	private final JPanel container;

	/**
	 * An array of a {@link ProductListRow} for each {@link Recipe} in {@link Data#getRecipes()}
	 */
	private final ProductListRow[] listRows;

	public ProductList(final Collection<ProductListRow> listRows) {
		super(new BorderLayout());

		this.container = new JPanel(new GridLayout(0, 1));

		this.listRows = listRows.toArray(new ProductListRow[listRows.size()]);

		this.setSearchKey("");

		this.add(this.container, BorderLayout.PAGE_START);
	}

	public ProductList(final ProductListRow... listRows) {
		super(new BorderLayout());

		this.container = new JPanel(new GridLayout(0, 1));

		this.listRows = new ProductListRow[listRows.length];
		System.arraycopy(listRows, 0, this.listRows, 0, listRows.length);

		this.setSearchKey("");

		this.add(this.container, BorderLayout.PAGE_START);
	}

	/**
	 * <ul>
	 * <b><i>getSearchKey</i></b><br>
	 * <pre>public {@link String} getSearchKey()</pre>
	 * @return the current search key
	 *         </ul>
	 */
	public String getSearchKey() {
		return this.searchKey;
	}

	/**
	 * <ul>
	 * <b><i>setSearchKey</i></b><br>
	 * <pre> void setSearchKey()</pre> Sets the current search key, and reorders the {@link ProductListRow}s accordingly.
	 * @param searchKey - the new search key
	 * @see {@link EditDistance#distance(String, String)}
	 *      </ul>
	 */
	public void setSearchKey(final String searchKey) {
		this.searchKey = searchKey == null ? "" : searchKey;

		this.container.removeAll();

		if (this.searchKey.isEmpty()) {
			boolean even = true;
			for (final ProductListRow row : this.listRows) {
				this.container.add(row);
				if ((even = !even))
					row.setBackground(Color.WHITE);
				else
					row.setBackground(new Color(238, 238, 238));
			}
			this.container.revalidate();
			this.container.repaint();
		} else {
			final Set<ProductListRow> rows = new TreeSet<>((o1, o2) -> {
				int d = Double.compare(EditDistance.distance(ProductList.this.searchKey, Data.nameFor(o1.recipe)), EditDistance.distance(ProductList.this.searchKey, Data.nameFor(o2.recipe)));
				if (d != 0) return d;
				d = Data.nameFor(o1.recipe).compareToIgnoreCase(Data.nameFor(o2.recipe));
				if (d != 0) return d;
				return o1.recipe.name.compareTo(o2.recipe.name);
			});
			for (final ProductListRow plr : this.listRows) {
				final double ed = EditDistance.distance(searchKey, Data.nameFor(plr.recipe));
				if (ed < Double.POSITIVE_INFINITY) { // TODO set value
					rows.add(plr);
				}
			}

			boolean even = true;
			for (final ProductListRow row : rows) {
				this.container.add(row);
				if ((even = !even))
					row.setBackground(Color.WHITE);
				else
					row.setBackground(new Color(238, 238, 238));
			}
			this.container.revalidate();
			this.container.repaint();
		}
	}

	/**
	 * <ul>
	 * <b><i>getRates</i></b><br>
	 * <pre>public {@link Map}&lt;{@link Recipe}, {@link Number}&gt; getRates()</pre>
	 * @return a map mapping the recipes that are to be calculated for to the rates (in cycles per second) at which they are to
	 *         be produced.
	 *         </ul>
	 */
	public Map<Recipe, Number> getRates() {
		final Map<Recipe, Number> ret = new HashMap<>();
		for (final ProductListRow row : this.listRows) {
			final double rate = row.getRate();
			if (rate > 0) ret.put(row.recipe, rate);
		}

		return ret;
	}
}
