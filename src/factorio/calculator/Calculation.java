package factorio.calculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import factorio.data.Recipe;
import factorio.window.treecell.CalculatedRecipe;
import factorio.window.treecell.TotalAssemblerCount;
import factorio.window.treecell.TotalHeader;
import factorio.window.treecell.TotalItem;

/**
 * A {@code Calculation} takes a map of recipes and the rates that they should be produced at, and produces the
 * {@link CalculatedRecipe} for each. It also has methods for creating {@link TreeNode}s for both the full and total tree.
 * @author ricky3350
 */
public class Calculation {

	/**
	 * Maps the recipes to be produced to the rate at which they should be produced at
	 */
	private final Map<Recipe, Number> productRates = new HashMap<>();

	/**
	 * Creates and calculates a new calculation with the given rates.
	 * @param productRates - A map mapping recipes to be produced to the rate at which they should be produced at
	 */
	public Calculation(final Map<Recipe, ? extends Number> productRates) {
		this.productRates.putAll(productRates);

		for (final Recipe recipe : productRates.keySet()) {
			this.result.add(new CalculatedRecipe(recipe, productRates.get(recipe).doubleValue()));
		}
	}

	/**
	 * The {@link CalculatedRecipe}s that serve as the result of the {@code Calculation}
	 */
	private final Set<CalculatedRecipe> result = new TreeSet<>();

	/**
	 * <ul>
	 * <b><i>getAsTreeNode</i></b><br>
	 * <pre> public {@link TreeNode} getAsTreeNode()</pre>
	 * @return a {@code TreeNode} with children containing a {@link CalculatedRecipe} for each {@link Recipe} in the product
	 *         rates.
	 * @see {@link #getTotalTreeNode()}
	 *      </ul>
	 */
	public TreeNode getAsTreeNode() {
		final DefaultMutableTreeNode ret = new DefaultMutableTreeNode();

		for (final CalculatedRecipe recipe : this.result) {
			addRecipeToParent(recipe, ret);
		}

		return ret;
	}

	/**
	 * <ul>
	 * <b><i>addRecipeToParent</i></b><br>
	 * <pre> private static void addRecipeToParent({@link CalculatedRecipe} recipe, {@link DefaultMutableTreeNode} parent)</pre>
	 * Adds a new {@link DefaultMutableTreeNode} to the specified node as a parent. Is recursively called for each ingredient in
	 * the given {@code CalculatedRecipe}
	 * @param recipe - The contents of the added tree node
	 * @param parent - The node to add the new node to
	 *        </ul>
	 */
	private static void addRecipeToParent(final CalculatedRecipe recipe, final DefaultMutableTreeNode parent) {
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(recipe);
		parent.add(node);

		for (final CalculatedRecipe r : recipe.getIngredients()) {
			addRecipeToParent(r, node);
		}
	}

	/**
	 * <ul>
	 * <b><i>addTotalToParent</i></b><br>
	 * <pre> private static void addTotalToParent({@link TotalItem} total, {@link DefaultMutableTreeNode} parent)</pre> Adds a
	 * new {@link DefaultMutableTreeNode} to the specified node as a parent. Is recursively called for each child in the given
	 * {@code TotalItem}
	 * @param total - The contents of the added tree node
	 * @param parent - The node to add the new node to
	 *        </ul>
	 */
	private static void addTotalToParent(final TotalItem total, final DefaultMutableTreeNode parent) {
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(total);
		parent.add(node);

		for (final TotalItem ti : total.getChildren()) {
			addTotalToParent(ti, node);
		}
	}

	/**
	 * <ul>
	 * <b><i>getTotalTreeNode</i></b><br>
	 * <pre> public {@link TreeNode} getTotalTreeNode()</pre>
	 * @return a {@code TreeNode} with the total assembler requirement by item and by assembler.
	 * @see {@link #getAsTreeNode()}
	 *      </ul>
	 */
	public TreeNode getTotalTreeNode() {
		final DefaultMutableTreeNode ret = new DefaultMutableTreeNode();
		final DefaultMutableTreeNode byItem = new DefaultMutableTreeNode(new TotalHeader("By Item", 1));
		final DefaultMutableTreeNode byAssembler = new DefaultMutableTreeNode(new TotalHeader("By Assembler", 1));

		ret.add(byItem);
		ret.add(byAssembler);

		final Set<TotalItem> totalItems = new TreeSet<>();
		final Set<TotalAssemblerCount> totalAssemblers = new TreeSet<>();

		for (final CalculatedRecipe r : this.result) {
			addRecipeToTotals(r, totalItems, totalAssemblers);
		}

		totalItems.forEach(ti -> addTotalToParent(ti, byItem));
		totalAssemblers.forEach(ta -> byAssembler.add(new DefaultMutableTreeNode(ta)));
		return ret;
	}

	/**
	 * <ul>
	 * <b><i>addRecipeToTotals</i></b><br>
	 * <pre> private static void addRecipeToTotals({@link CalculatedRecipe} recipe, {@link Collection}&lt;{@link TotalItem}&gt; totalItems, {@code Collection}&lt;{@link TotalAssemblerCount}&gt; totalAssemblers)</pre>
	 * Adds the given {@code CalculatedRecipe} and all of its children to the given collections, using
	 * {@link TotalItem#add(double, double, Recipe, double, AssemblerSettings, boolean)},
	 * {@link TotalAssemblerCount#add(double)}, or {@linkplain Collection#add(Object) adding} a new total into the appropriate
	 * {@code Collection}
	 * @param recipe - The recipe to add
	 * @param totalItems - A {@code Collection} of {@code TotalItem}s to add to
	 * @param totalAssemblers - A {@code Collection} of {@code TotalAssemblerCount}s to add to
	 *        </ul>
	 */
	private static void addRecipeToTotals(final CalculatedRecipe recipe, final Collection<TotalItem> totalItems, final Collection<TotalAssemblerCount> totalAssemblers) {
		findItem: {
			for (final TotalItem ti : totalItems) {
				if (recipe.product == ti.getItem() || (recipe.product != null && recipe.product.equals(ti.getItem()))) {
					ti.add(recipe.getRate(), recipe.getRecipeRate(), recipe.getRecipe(), recipe.getAssemblers(), recipe.getAssembler(), recipe.fuel != null);
					break findItem;
				}
			}
			final TotalItem add = new TotalItem(recipe.product, recipe.getRecipe(), recipe.getAssembler());
			add.add(recipe.getRate(), recipe.getRecipeRate(), recipe.getRecipe(), recipe.getAssemblers(), recipe.getAssembler(), recipe.fuel != null);
			totalItems.add(add);
		}

		findAssembler: if (recipe.getAssembler() != null) {
			for (final TotalAssemblerCount tac : totalAssemblers) {
				if (tac.getAssembler().equals(recipe.getAssembler())) {
					tac.add(recipe.getAssemblers());
					break findAssembler;
				}
			}
			final TotalAssemblerCount ntac = new TotalAssemblerCount(recipe.getAssembler());
			ntac.add(recipe.getAssemblers());
			totalAssemblers.add(ntac);
		}

		for (final CalculatedRecipe r : recipe.getIngredients())
			addRecipeToTotals(r, totalItems, totalAssemblers);
	}
}
