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
 * A {@link TreeCell} for the total number of assemblers for a give item type required in a {@link Calculation}. Splits into
 * children if multiple recipes/assemblers are used for a single item.
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
	 * The total rate at which the item is being produced as fuel
	 */
	private double fuelItemRate;

	/**
	 * The total rate at which the recipe is being assembled
	 */
	private double recipeRate;

	/**
	 * The total rate at which the recipe is being assembled for fuel
	 */
	private double fuelRecipeRate;

	/**
	 * The total number of assemblers to assemble {@link #recipe} at {@link #recipeRate}
	 */
	private double assemblerCount;

	/**
	 * The total number of assemblers to assemble {@link #recipe} at {@link #fuelRecipeRate}
	 */
	private double fuelAssemblerCount;

	/**
	 * All of the {@code TotalItem}s that are this {@code TotalItem}'s children, i.e. share an item, and sometimes also a recipe
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
	 * <pre>public void add(double itemRate, double recipeRate, {@link Recipe} recipe, double assemblerCount, {@link AssemblerSettings} assembler, boolean asFuel)</pre>
	 * Add the given rates/assembler counts to this item. If either the recipe or the assembler settings do not match, this
	 * {@code TotalItem} is split and gains children
	 * @param itemRate - the rate, in items/s, to add
	 * @param recipeRate - the rate, in cycles/s, to add
	 * @param recipe - the recipe to add to
	 * @param assemblerCount - the number of assemblers to add
	 * @param assembler - the assembler to add to
	 * @param asFuel - whether or not the given information is for producing a fuel
	 *        </ul>
	 */
	public void add(final double itemRate, final double recipeRate, final Recipe recipe, final double assemblerCount, final AssemblerSettings assembler, final boolean asFuel) {
		if (asFuel)
			this.fuelItemRate += itemRate;
		else
			this.itemRate += itemRate;

		if (this.recipe == null && this.item != null) {
			for (final TotalItem ti : this.children) {
				if (ti.recipe == recipe) {
					ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler, asFuel);
					return;
				}
			}
			if (recipe != null && assembler != null) {
				final TotalItem b = new TotalItem(null, recipe, assembler);
				if (asFuel) {
					b.fuelRecipeRate = recipeRate;
					b.fuelAssemblerCount = assemblerCount;
				} else {
					b.recipeRate = recipeRate;
					b.assemblerCount = assemblerCount;
				}
				this.children.add(b);
			}
		} else if (this.recipe == recipe || this.recipe == null) {
			if (this.recipe != null) {
				if (asFuel)
					this.fuelRecipeRate += recipeRate;
				else
					this.recipeRate += recipeRate;
			}

			if (this.assembler == null) {
				for (final TotalItem ti : this.children) {
					if (ti.assembler.equals(assembler)) {
						ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler, asFuel);
						return;
					}
				}
				if (assembler != null) {
					final TotalItem b = new TotalItem(null, null, assembler);
					if (asFuel)
						b.fuelAssemblerCount = assemblerCount;
					else
						b.assemblerCount = assemblerCount;
					this.children.add(b);
				}
			} else if (this.assembler.equals(assembler)) {
				if (asFuel)
					this.fuelAssemblerCount += assemblerCount;
				else
					this.assemblerCount += assemblerCount;
			} else {
				final TotalItem a = new TotalItem(null, null, this.assembler);
				if (asFuel)
					a.fuelAssemblerCount = this.assemblerCount;
				else
					a.assemblerCount = this.assemblerCount;
				this.children.add(a);

				if (assembler != null) {
					final TotalItem b = new TotalItem(null, null, assembler);
					if (asFuel)
						b.fuelAssemblerCount = assemblerCount;
					else
						b.assemblerCount = assemblerCount;
					this.children.add(b);
				}

				this.assembler = null;
			}
		} else {
			final TotalItem a = new TotalItem(null, this.recipe, this.assembler);
			if (asFuel) {
				a.fuelRecipeRate = this.recipeRate;
				a.fuelAssemblerCount = this.assemblerCount;
			} else {
				a.recipeRate = this.recipeRate;
				a.assemblerCount = this.assemblerCount;
			}
			this.children.add(a);

			if (recipe != null && assembler != null) {
				final TotalItem b = new TotalItem(null, recipe, assembler);
				if (asFuel) {
					b.fuelRecipeRate = recipeRate;
					b.fuelAssemblerCount = assemblerCount;
				} else {
					b.recipeRate = recipeRate;
					b.assemblerCount = assemblerCount;
				}
				this.children.add(b);
			}

			this.recipe = null;
			this.assembler = null;
		}
	}

	@Override
	public int compareTo(final TotalItem o) {
		int d = 0;
		if (this.item != null && o.item != null)
			d = Data.nameFor(this.item).compareTo(Data.nameFor(o.item));
		else if (this.recipe != null && o.recipe != null)
			d = Data.nameFor(this.recipe).compareTo(Data.nameFor(o.recipe));
		else if (this.assembler != null && o.assembler != null) d = this.assembler.compareTo(o.assembler);

		if (d != 0) return d;
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	/**
	 * <ul>
	 * <b><i>formatFuel</i></b><br>
	 * <pre>private {@link String} formatFuel(double notFuel, double fuel, boolean html, String suffix)</pre> Formats the given
	 * numbers.<br>
	 * Format: <i>NUMBERS SUFFIX</i><br>
	 * <i>NUMBERS</i>: <i>NOTFUEL FUEL</i><br>
	 * <i>NOTFUEL</i>: if {@code notFuel} is greater than zero, or both {@code notFuel} and {@code fuel} are zero, then this is
	 * the result of {@link Util.NUMBER_FORMAT#format(double) Util.NUMBER_FORMAT.format(notFuel)} in boldface, empty otherwise.
	 * <br>
	 * <i>FUEL</i>: if {@code fuel} is greater than zero, this is the result of {@link Util.NUMBER_FORMAT#format(double)
	 * Util.NUMBER_FORMAT.format(fuel)}, in dark red ({@code #800000}), with a leading plus sign if <i>NOTFUEL</i> is not empty.
	 * Empty if {@code fuel} is zero.<br>
	 * <i>SUFFIX</i>: {@code suffix}, followed by an <code>'s'</code> only if {@code suffix} is not <code>null</code> and not
	 * empty, and one of {@code notFuel} and {@code fuel} is one, and the other is zero.
	 * @param notFuel - the number for formatting <i>NOTFUEL</i>
	 * @param fuel - the number for formatting <i>FUEL</i>
	 * @param html - whether or not to use the html formats specified above for <i>NOTFUEL</i> (boldface) and <i>FUEL</i> (red)
	 * @param suffix - the suffix to use. <code>null</code> or empty for no suffix
	 * @return the given numbers formatted using the above rules.
	 *         </ul>
	 */
	private static String formatFuel(final double notFuel, final double fuel, final boolean html, final String suffix) {
		String ret = "";

		if (notFuel > 0.00001 || fuel <= 0.00001) ret += (html ? "<b>" : "") + Util.NUMBER_FORMAT.format(notFuel) + (html ? "</b>" : "");
		if (fuel > 0.00001) ret += (html ? "<font color=\"#800000\">" : "") + (notFuel > 0.00001 ? " +" : "") + Util.NUMBER_FORMAT.format(fuel) + (html ? "</font>" : "(fuel)");
		if (suffix != null && !suffix.isEmpty()) ret += " " + suffix + ((Math.abs(fuel - 1) < 0.00001 && Math.abs(notFuel) < 0.00001) || (Math.abs(fuel) < 0.00001 && Math.abs(notFuel - 1) < 0.00001) ? "" : "s");

		return ret;
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
			String ret = String.format("%s at %s/s", Data.nameFor(this.item), TotalItem.formatFuel(this.itemRate, this.fuelItemRate, false, "item"));

			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) {
				ret += String.format(" (using %s at %s/s)", Data.nameFor(this.recipe), TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, false, "cycle"));
			} else if (this.recipeRate > 0 && this.fuelRecipeRate > 0 && (this.itemRate != this.recipeRate || this.fuelItemRate != this.fuelRecipeRate)) {
				ret += String.format(" (at %s/s)", TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, false, "cycle"));
			}

			if (this.assembler != null) ret += String.format(" requires %s %s%s", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, false, null), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));

			return ret;
		} else if (this.recipe != null) {
			String ret = String.format("%s%s at %s/s", Double.isNaN(this.itemRate) ? "" : "using ", Data.nameFor(this.recipe), TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, false, "cycle"));

			if (this.assembler != null) ret += String.format(" requires %s %s%s", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, false, null), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));

			return ret;
		} else if (this.assembler != null) {
			return String.format("%s %s%s", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, false, null), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));
		}

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
			final String asm = this.assembler != null ? String.format(" requires %s %s%s</html>", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, true, null), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : "";

			String prod = String.format("<html><b>%s</b> at %s/s", Data.nameFor(this.item), TotalItem.formatFuel(this.itemRate, this.fuelItemRate, true, "item"));
			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) {
				prod += " (using ";
				ret.add(new JLabel(String.format("<html><b>%s</b> at %s/s)%s</html>", Data.nameFor(this.getRecipe()), TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, true, "cycle"), asm), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			} else {
				if (this.recipeRate > 0 && this.fuelRecipeRate > 0 && (this.itemRate != this.recipeRate || this.fuelItemRate != this.fuelRecipeRate)) prod += String.format(" (at %s/s)", TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, true, "cycle"));
				prod += asm;
			}
			ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.item, false), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else if (this.recipe != null) {
			if (!Double.isNaN(this.itemRate)) ret.add(new JLabel("using ", TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			ret.add(new JLabel(String.format("<html><b>%s</b> at %s/s%s</html>", Data.nameFor(this.getRecipe()), TotalItem.formatFuel(this.recipeRate, this.fuelRecipeRate, true, "cycle"), this.assembler != null ? String.format(" requires %s %s %s", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, true, null), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : ""), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else if (this.assembler != null) {
			ret.add(new JLabel(String.format("<html>%s %s%s</html>", TotalItem.formatFuel(this.assemblerCount, this.fuelAssemblerCount, true, null), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)), TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}
}
