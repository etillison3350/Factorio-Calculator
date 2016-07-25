package factorio.window.treecell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import factorio.calculator.AssemblerSettings;
import factorio.data.Data;
import factorio.data.Recipe;

public class CalculatedRecipe implements TreeCell, Comparable<CalculatedRecipe> {

	public final boolean isFuel;
	
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
	private Map<String, CalculatedRecipe> ingredients = new TreeMap<>();

	public CalculatedRecipe(String product, float rate) {
		this(product, rate, false);
	}
	
	public CalculatedRecipe(String product, float rate, boolean isFuel) {
		this(product, rate, new ArrayList<>(), isFuel);
	}

	private CalculatedRecipe(String product, float rate, Collection<String> banned, boolean isFuel) {
		this.isFuel = isFuel;
		
		this.product = product;
		this.rate = rate;

		for (Recipe r : Data.getRecipes()) {
			if (r.getResults().containsKey(product) && !Data.isBlacklisted(r.name) && !banned.contains(r.name)) {
				this.recipe = r;

				this.recipeRate = this.rate / this.recipe.getResults().get(product);

				Collection<String> newBanned = new ArrayList<>(banned);
				newBanned.add(r.name);
				for (String ingredient : this.recipe.getIngredients().keySet()) {
					ingredients.put(ingredient, new CalculatedRecipe(ingredient, this.rate * this.recipe.getIngredients().get(ingredient), newBanned, false));
				}

				break;
			}
		}
		if (this.recipe != null) this.settings = AssemblerSettings.getDefaultSettings(this.recipe);

		calculateAssemblers();
	}
	
	public CalculatedRecipe(Recipe recipe, float rate, boolean isFuel) {
		if (recipe == null) throw new NullPointerException();

		this.isFuel = isFuel;
		
		this.recipe = recipe;
		this.product = this.recipe.getResults().keySet().iterator().next();
		this.recipeRate = rate;
		this.rate = this.recipeRate * this.recipe.getResults().get(this.product);
		this.settings = AssemblerSettings.getDefaultSettings(this.recipe);

		for (String ingredient : this.recipe.getIngredients().keySet()) {
			ingredients.put(ingredient, new CalculatedRecipe(ingredient, this.rate * this.recipe.getIngredients().get(ingredient)));
		}

		calculateAssemblers();
	}

	/**
	 * <ul>
	 * <b><i>setRate</i></b><br>
	 * <br>
	 * <code>&nbsp;public void setRate(float rate)</code><br>
	 * <br>
	 * Sets the rate of this <code>CalculatedRecipe</code>, and updates the assembler count and rate for all nested <code>CalculatedRecipe</code>s
	 * @param rate - The rate to set
	 * </ul>
	 */
	public void setRate(float rate) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(product).floatValue();

		calculateAssemblers();
		updateIngredients();
	}

	/**
	 * <ul>
	 * <b><i>setSettings</i></b><br>
	 * <br>
	 * <code>&nbsp;public void setSettings({@link AssemblerSettings} settings)</code><br>
	 * <br>
	 * Sets the {@code AssemblerSettings} for this {@code CalculatedRecipe}, and updates the assembler count for this {@code CalculatedRecipe} <b>only</b>.
	 * @param settings - The settings to set
	 * </ul>
	 */
	public void setSettings(AssemblerSettings settings) {
		this.settings = settings;

		calculateAssemblers();
		// updateIngredients();
	}

	/**
	 * <ul>
	 * <b><i>setRateAndSettings</i></b><br>
	 * <br>
	 * <code>&nbsp;public void setRateAndSettings(float rate, AssemblerSettings settings)</code><br>
	 * <br>
	 * Sets the settings and rate of the {@code CalculatedRecipe}s as specified in {@link #setSettings(AssemblerSettings)} and {@link #setRate(float)} respectively
	 * @param rate - The rate to set
	 * @param settings - The settings to set
	 * </ul>
	 */
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
		return new TreeSet<>(ingredients.values());
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

	@Override
	public int compareTo(CalculatedRecipe o) {
		int d = Data.nameFor(this.product).compareTo(Data.nameFor(o.product));
		if (d != 0) return d;
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected) {
		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected);

		String prod = String.format("<html><b>%s</b> at <b>%s</b> items/s", Data.nameFor(this.product), Data.NUMBER_FORMAT.format(this.rate));
		if (Data.hasMultipleRecipes(this.product)) {
			prod += " (via ";
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s</b> cycles/s)</html>", Data.nameFor(this.recipe), Data.NUMBER_FORMAT.format(this.recipeRate)), this.recipe.getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}
		ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.product), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		if (this.settings != null) {
			String assemblerStr = String.format("<html>requires <b>%s</b> %s", Data.NUMBER_FORMAT.format(this.assemblers), Data.nameFor(this.settings.getAssembler().name));

			assemblerStr += this.settings.getBonusString();

			ret.add(new JLabel(assemblerStr + " </html>", TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}

}
