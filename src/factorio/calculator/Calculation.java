package factorio.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import factorio.data.Recipe;

public class Calculation {

	private final Map<Recipe, Number> productRates = new HashMap<>();
	
	public Calculation(Map<Recipe, ? extends Number> productRates) {
		this.productRates.putAll(productRates);
		
		for (Recipe recipe : productRates.keySet()) {
			result.add(new CalculatedRecipe(recipe, productRates.get(recipe).floatValue()));
		}
	}
	
	private List<CalculatedRecipe> result = new ArrayList<>();
	
	public TreeNode getAsTreeNode() {
		DefaultMutableTreeNode ret = new DefaultMutableTreeNode();
		
		for (CalculatedRecipe recipe : result) {
			addRecipeToParent(recipe, ret);
		}
		
		return ret;
	}
	
	private void addRecipeToParent(CalculatedRecipe recipe, DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(recipe);
		parent.add(node);
		
		for (CalculatedRecipe r : recipe.getIngredients()) {
			addRecipeToParent(r, node);
		}
	}

}
