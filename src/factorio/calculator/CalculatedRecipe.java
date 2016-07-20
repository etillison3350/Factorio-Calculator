package factorio.calculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import factorio.data.Data;
import factorio.data.Recipe;

public class CalculatedRecipe {

	public final String product;
	private Recipe recipe;

	/**
	 * The rate, in items per second, to calculate for.
	 */
	private float rate;

	/**
	 * The rate, in recipes completed per second, to calculate for.
	 */
	private float recipeRate;

	private float assemblers;
	private AssemblerSettings settings;
	private Map<String, CalculatedRecipe> ingredients = new HashMap<>();

	protected CalculatedRecipe(String product, float rate) {
		this(product, rate, new ArrayList<>());
	}

	private CalculatedRecipe(String product, float rate, Collection<String> banned) {
		this.product = product;
		this.rate = rate;

		for (Recipe r : Data.getRecipes()) {
			if (r.getResults().containsKey(product) && !Data.isBlacklisted(r.name) && !banned.contains(r.name)) {
				this.recipe = r;

				this.recipeRate = this.rate / this.recipe.getResults().get(product);

				Collection<String> newBanned = new ArrayList<>(banned);
				newBanned.add(r.name);
				for (String ingredient : this.recipe.getIngredients().keySet()) {
					ingredients.put(ingredient, new CalculatedRecipe(ingredient, this.rate * this.recipe.getIngredients().get(ingredient), newBanned));
				}

				break;
			}
		}
		if (this.recipe != null) this.settings = AssemblerSettings.getDefaultSettings(this.recipe.type);

		calculateAssemblers();
	}

	protected CalculatedRecipe(Recipe recipe, float rate) {
		if (recipe == null) throw new NullPointerException();

		this.recipe = recipe;
		this.product = this.recipe.getResults().keySet().iterator().next();
		this.recipeRate = rate;
		this.rate = this.recipeRate * this.recipe.getResults().get(this.product);
		this.settings = AssemblerSettings.getDefaultSettings(this.recipe.type);

		for (String ingredient : this.recipe.getIngredients().keySet()) {
			ingredients.put(ingredient, new CalculatedRecipe(ingredient, this.rate * this.recipe.getIngredients().get(ingredient)));
		}

		calculateAssemblers();
	}

	public void setRate(float rate) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(product).floatValue();

		calculateAssemblers();
		updateIngredients();
	}

	public void setSettings(AssemblerSettings settings) {
		this.settings = settings;

		calculateAssemblers();
		// updateIngredients();
	}

	public void setRateAndSettings(float rate, AssemblerSettings settings) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(product).floatValue();
		this.settings = settings;

		calculateAssemblers();
		updateIngredients();
	}

	private void calculateAssemblers() {
		if (this.recipe != null && this.settings != null) {
			// The number of items produced per second by one assembler
			float ips = recipe.getResults().get(this.product) * settings.getProductivity() / recipe.timeIn(settings.getAssembler(), settings.getSpeed());

			this.assemblers = rate / ips;
		}
	}

	private void updateIngredients() {
		if (this.recipe != null) {
			for (String ingredient : ingredients.keySet()) {
				ingredients.get(ingredient).setRate(this.rate * this.recipe.getIngredients().get(ingredient));
			}
		}
	}

	public float getAssemblers() {
		return assemblers;
	}

//	@Override
//	public String toString() {
//		if (this.recipe == null) return String.format("%s at %.6g items/s", Data.nameFor(this.product), this.rate);
//		return String.format("%s at %.6g cycles/s requires %.4f assemblers", Data.nameFor(recipe), recipeRate, assemblers);
//	}

	public Set<CalculatedRecipe> getIngredients() {
		return new HashSet<>(ingredients.values());
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public float getRate() {
		return rate;
	}

	public float getRecipeRate() {
		return recipeRate;
	}

	public AssemblerSettings getSettings() {
		return settings;
	}

}
