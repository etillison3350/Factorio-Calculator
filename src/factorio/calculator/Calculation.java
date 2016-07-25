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

public class Calculation {

	private final Map<Recipe, Number> productRates = new HashMap<>();

	public Calculation(Map<Recipe, ? extends Number> productRates) {
		this.productRates.putAll(productRates);

		for (Recipe recipe : productRates.keySet()) {
			result.add(new CalculatedRecipe(recipe, productRates.get(recipe).floatValue(), false));
		}
	}

	private Set<CalculatedRecipe> result = new TreeSet<>();

	public TreeNode getAsTreeNode() {
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode();

		for (CalculatedRecipe recipe : result) {
			addRecipeToParent(recipe, ret);
		}

		return ret;
	}

	private static void addRecipeToParent(CalculatedRecipe recipe, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(recipe);
		parent.add(node);

		for (CalculatedRecipe r : recipe.getIngredients()) {
			addRecipeToParent(r, node);
		}
	}

	private static void addTotalToParent(TotalItem total, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(total);
		parent.add(node);

		for (TotalItem ti : total.getChildren()) {
			addTotalToParent(ti, node);
		}
	}

	public TreeNode getTotalTreeNode() {
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode();
		DefaultMutableTreeNode byItem = new DefaultMutableTreeNode(new TotalHeader("By Item", 1));
		DefaultMutableTreeNode byAssembler = new DefaultMutableTreeNode(new TotalHeader("By Assembler", 1));

		ret.add(byItem);
		ret.add(byAssembler);

		Set<TotalItem> totalItems = new TreeSet<>();
		Set<TotalAssemblerCount> totalAssemblers = new TreeSet<>();

		for (CalculatedRecipe r : result) {
			addRecipeToTotals(r, totalItems, totalAssemblers);
		}

		totalItems.forEach(ti -> addTotalToParent(ti, byItem));
		totalAssemblers.forEach(ta -> byAssembler.add(new DefaultMutableTreeNode(ta)));
		return ret;
	}

	private static void addRecipeToTotals(CalculatedRecipe recipe, Collection<TotalItem> totalItems, Collection<TotalAssemblerCount> totalAssemblers) {
		findItem: {
			for (TotalItem ti : totalItems) {
				if (recipe.product.equals(ti.getItem())) {
					ti.add(recipe.getRate(), recipe.getRecipeRate(), recipe.getRecipe(), recipe.getAssemblers(), recipe.getSettings());
					break findItem;
				}
			}
			TotalItem add = new TotalItem(recipe.product, recipe.getRecipe(), recipe.getSettings());
			add.add(recipe.getRate(), recipe.getRecipeRate(), recipe.getRecipe(), recipe.getAssemblers(), recipe.getSettings());
			totalItems.add(add);
		}

		findAssembler: if (recipe.getSettings() != null) {
			for (TotalAssemblerCount tac : totalAssemblers) {
				if (tac.getAssembler().equals(recipe.getSettings())) {
					tac.add(recipe.getAssemblers());
					break findAssembler;
				}
			}
			TotalAssemblerCount ntac = new TotalAssemblerCount(recipe.getSettings());
			ntac.add(recipe.getAssemblers());
			totalAssemblers.add(ntac);
		}

		for (CalculatedRecipe r : recipe.getIngredients())
			addRecipeToTotals(r, totalItems, totalAssemblers);
	}
}
