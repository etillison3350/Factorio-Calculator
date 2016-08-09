package factorio.window.treecell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import factorio.Util;
import factorio.calculator.AssemblerSettings;
import factorio.calculator.Calculation;
import factorio.data.Data;
import factorio.data.Recipe;

/**
 * A {@link TreeCell} for the total number of assemblers for a give item type
 * required in a {@link Calculation}. Splits into children if multiple
 * recipes/assemblers are used for a single item.
 * @author ricky3350
 */
public class TotalItem implements TreeCell, Comparable<TotalItem> {

	/**
	 * The item that is being added to
	 */
	private final String item;

	/**
	 * The recipe that is being used to produce the item
	 */
	private Recipe recipe;

	/**
	 * The assembler being used to assemble the {@link #recipe}
	 */
	private AssemblerSettings assembler;

	/**
	 * The total rate at which the item is being produced
	 */
	private double itemRate;

	/**
	 * The total rate at which the recipe is being assembled
	 */
	private double recipeRate;

	/**
	 * The total number of assemblers to assemble {@link #recipe} at
	 * {@link #recipeRate}
	 */
	private double assemblerCount;

	/**
	 * All of the {@code TotalItem}s that are this {@code TotalItem}'s children,
	 * i.e. share an item, and sometimes also a recipe
	 */
	private final Set<TotalItem> children = new HashSet<>();

	public TotalItem(final String item, final Recipe recipe, final AssemblerSettings assembler) {
		this.item = item;
		this.recipe = recipe;
		this.assembler = assembler;
	}

	/**
	 * <ul>
	 * <b><i>add</i></b><br>
	 * <pre>public void add(double itemRate, double recipeRate, {@link Recipe} recipe, double assemblerCount, {@link AssemblerSettings} assembler)</pre>
	 * Add the given rates/assembler counts to this item. If either the recipe
	 * or the assembler settings do not match, this {@code TotalItem} is split
	 * and gains children
	 * @param itemRate - the rate, in items/s, to add
	 * @param recipeRate - the rate, in cycles/s, to add
	 * @param recipe - the recipe to add to
	 * @param assemblerCount - the number of assemblers to add
	 * @param assembler - the assembler to add to
	 *        </ul>
	 */
	public void add(final double itemRate, final double recipeRate, final Recipe recipe, final double assemblerCount, final AssemblerSettings assembler) {
		this.itemRate += itemRate;

		if (this.recipe == null && this.item != null) {
			for (final TotalItem ti : this.children)
				if (ti.recipe == recipe) {
					ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
					return;
				}
			if (recipe != null && assembler != null) {
				final TotalItem b = new TotalItem(null, recipe, assembler);
				b.recipeRate = recipeRate;
				b.assemblerCount = assemblerCount;
				this.children.add(b);
			}
		} else if (this.recipe == recipe || this.recipe == null) {
			if (this.recipe != null) this.recipeRate += recipeRate;

			if (this.assembler == null) {
				for (final TotalItem ti : this.children)
					if (ti.assembler.equals(assembler)) {
						ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
						return;
					}
				if (assembler != null) {
					final TotalItem b = new TotalItem(null, null, assembler);
					b.assemblerCount = assemblerCount;
					this.children.add(b);
				}
			} else if (this.assembler.equals(assembler))
				this.assemblerCount += assemblerCount;
			else {
				final TotalItem a = new TotalItem(null, null, this.assembler);
				a.assemblerCount = this.assemblerCount;
				this.children.add(a);

				if (assembler != null) {
					final TotalItem b = new TotalItem(null, null, assembler);
					b.assemblerCount = assemblerCount;
					this.children.add(b);
				}

				this.assembler = null;
			}
		} else {
			final TotalItem a = new TotalItem(null, this.recipe, this.assembler);
			a.recipeRate = this.recipeRate;
			a.assemblerCount = this.assemblerCount;
			this.children.add(a);

			if (recipe != null && assembler != null) {
				final TotalItem b = new TotalItem(null, recipe, assembler);
				b.recipeRate = recipeRate;
				b.assemblerCount = assemblerCount;
				this.children.add(b);
			}

			this.recipe = null;
			this.assembler = null;
		}
	}

	@Override
	public int compareTo(final TotalItem o) {
		int d = 0;
		if (this.item != null)
			d = Data.nameFor(this.item).compareTo(Data.nameFor(o.item));
		else if (this.recipe != null)
			d = Data.nameFor(this.recipe).compareTo(Data.nameFor(o.recipe));
		else if (this.assembler != null) d = this.assembler.compareTo(o.assembler);

		if (d != 0) return d;
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	/**
	 * <ul>
	 * <b><i>getAssembler</i></b><br>
	 * <pre>public {@link AssemblerSettings} getAssembler()</pre>
	 * @return the assembler used to assemble the recipe
	 *         </ul>
	 */
	public AssemblerSettings getAssembler() {
		return this.assembler;
	}

	/**
	 * <ul>
	 * <b><i>getChildren</i></b><br>
	 * <pre>public {@link SortedSet}&lt;TotalItem&gt; getChildren()</pre>
	 * @return the children of this {@code TotalItem}
	 *         </ul>
	 */
	public SortedSet<TotalItem> getChildren() {
		return new TreeSet<>(this.children);
	}

	/**
	 * <ul>
	 * <b><i>getItem</i></b><br>
	 * <pre>public {@link String} getItem()</pre>
	 * @return the item that is being added to
	 *         </ul>
	 */
	public String getItem() {
		return this.item;
	}

	@Override
	public String getRawString() {
		if (this.item != null) {
			String ret = String.format("%s at %s/s", Data.nameFor(this.item), Util.formatPlural(this.itemRate, "item"));

			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) ret += String.format(" (using %s at %s/s)", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle"));

			if (this.assembler != null) ret += String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));

			return ret;
		} else if (this.recipe != null) {
			String ret = String.format("using %s at %s/s", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle"));

			if (this.assembler != null) ret += String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));

			return ret;
		} else if (this.assembler != null) return String.format("%s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));

		return "";
	}

	/**
	 * <ul>
	 * <b><i>getRecipe</i></b><br>
	 * <pre>public {@link Recipe} getRecipe()</pre>
	 * @return the recipe used to produce the item
	 *         </ul>
	 */
	public Recipe getRecipe() {
		return this.recipe;
	}

	@Override
	public Component getTreeCellRendererComponent(final boolean selected, final boolean hasFocus) {
		final JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected, hasFocus);

		if (this.item != null) {
			final String asm = this.assembler != null ? String.format(" requires <b>%s</b> %s%s</html>", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : "";

			String prod = String.format("<html><b>%s</b> at <b>%s/s", Data.nameFor(this.item), Util.formatPlural(this.itemRate, "</b> item"));
			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) {
				prod += " (using ";
				ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s/s)%s</html>", Data.nameFor(this.getRecipe()), Util.formatPlural(this.recipeRate, "</b> cycle"), asm), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			} else
				prod += asm;
			ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.item, false), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else if (this.recipe != null) {
			ret.add(new JLabel("using ", TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s</b> cycles/s%s</html>", Data.nameFor(this.getRecipe()), Util.formatPlural(this.recipeRate, "</b> cycle"), this.assembler != null ? String.format(" requires <b>%s</b> %s %s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : ""), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else if (this.assembler != null) ret.add(new JLabel(String.format("<html><b>%s</b> %s%s</html>", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)), TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		return ret;
	}
}
