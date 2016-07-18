package factorio.calculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import factorio.data.Recipe;

public class Calculation {

	private final Map<Recipe, Number> productRates = new HashMap<>();
	
	public Calculation(Map<Recipe, ? extends Number> productRates) {
		this.productRates.putAll(productRates);
	}
	
	private List<CalculatedRecipe> result;
	
	public void calculate() {
		for (Recipe recipe : productRates.keySet()) {
			result.add(new CalculatedRecipe(recipe, productRates.get(recipe).floatValue(), AssemblerSettings.getDefaultSettings(recipe.type)));
		}
	}
	
	public TreeNode getAsTree() {
		// TODO
		return null;
	}

}
