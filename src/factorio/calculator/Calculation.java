package factorio.calculator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import factorio.data.Recipe;

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

	public TreeNode getTotalTreeNode() {
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode();
		
		Map<String, float[]> totals = new HashMap<>();
		
		for (CalculatedRecipe r : result) {
			addRecipeToTotals(r, totals);
		}
		
		// TODO create new class for totals
		return null;
	}
	
	private static void addRecipeToTotals(CalculatedRecipe recipe, Map<String, float[]> totals) {
		// TODO make better
		
		float[] total = totals.get(recipe.getRecipe().name);
		if (total == null) {
			totals.put(recipe.getRecipe().name, new float[] {recipe.getRecipeRate(), recipe.getAssemblers()});
		} else {
			total[0] += recipe.getRecipeRate();
			total[1] += recipe.getAssemblers();
		}
		
		for (CalculatedRecipe r : recipe.getIngredients())
			addRecipeToTotals(r, totals);
	}
}
