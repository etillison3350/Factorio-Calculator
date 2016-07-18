package factorio.calculator;

import factorio.data.Recipe;

public class CalculatedRecipe {

	public final Recipe recipe;

	/**
	 * The rate, in items per second, to calculate for.
	 */
	private float rate;

	private float assemblers;
	private AssemblerSettings settings;
	private CalculatedRecipe[] ingredients;

	protected CalculatedRecipe(Recipe recipe, float rate, AssemblerSettings settings) {
		this.recipe = recipe;
	}

	public void setRate(float rate) {
		this.rate = rate;

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
		this.settings = settings;

		calculateAssemblers();
		updateIngredients();
	}

	private void calculateAssemblers() {
		// The number of items produced per second by one assembler
		float ips = settings.getProductivity() / recipe.timeIn(settings.getAssembler(), settings.getSpeed());

		this.assemblers = rate / ips;
	}

	private void updateIngredients() {
		for (CalculatedRecipe recipe : ingredients) {
			float max = 0;
			for (String result : recipe.recipe.getResults().keySet()) {
				max = Math.max(max, this.recipe.getIngredients().getOrDefault(result, 0.0F));
			}
			recipe.setRate(rate * max);
		}
	}

	public float getAssemblers() {
		return assemblers;
	}

}
