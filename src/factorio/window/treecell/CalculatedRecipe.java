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

import factorio.Util;
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
	private double rate;

	/**
	 * The rate, in recipes completed per second, to calculate for.
	 */
	private double recipeRate;

	private double assemblers;
	private AssemblerSettings settings;
	private Map<String, CalculatedRecipe> ingredients = new TreeMap<>();

	public CalculatedRecipe(String product, double rate) {
		this(product, rate, false);
	}

	public CalculatedRecipe(String product, double rate, boolean isFuel) {
		this(product, rate, new ArrayList<>(), isFuel);
	}

	private CalculatedRecipe(String product, double rate, Collection<String> banned, boolean isFuel) {
		this.isFuel = isFuel;

		this.product = product;
		this.rate = rate;

		for (Recipe r : Data.getRecipes()) {
			if (r.getResults().containsKey(product) && !Util.isBlacklisted(r.name) && !banned.contains(r.name)) {
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

	public CalculatedRecipe(Recipe recipe, double rate, boolean isFuel) {
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
	 * <code>&nbsp;public void setRate(double rate)</code><br>
	 * <br>
	 * Sets the rate of this <code>CalculatedRecipe</code>, and updates the assembler count and rate for all nested <code>CalculatedRecipe</code>s
	 * @param rate - The rate to set
	 *        </ul>
	 */
	public void setRate(double rate) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(product).doubleValue();

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
	 *        </ul>
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
	 * <code>&nbsp;public void setRateAndSettings(double rate, AssemblerSettings settings)</code><br>
	 * <br>
	 * Sets the settings and rate of the {@code CalculatedRecipe}s as specified in {@link #setSettings(AssemblerSettings)} and {@link #setRate(double)} respectively
	 * @param rate - The rate to set
	 * @param settings - The settings to set
	 *        </ul>
	 */
	public void setRateAndSettings(double rate, AssemblerSettings settings) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(product).doubleValue();
		this.settings = settings;

		calculateAssemblers();
		updateIngredients();
	}

	private void calculateAssemblers() {
		if (this.recipe != null && this.settings != null) {
			// The number of items produced per second by one assembler
			double ips = recipe.getResults().get(this.product) * settings.getProductivity() / recipe.timeIn(settings.getAssembler(), settings.getSpeed());

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

	public double getAssemblers() {
		return assemblers;
	}

	public Set<CalculatedRecipe> getIngredients() {
		return new TreeSet<>(ingredients.values());
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public double getRate() {
		return rate;
	}

	public double getRecipeRate() {
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
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected, hasFocus);

		String prod = String.format("<html><b>%s</b> at <b>%s/s", Data.nameFor(this.product), Util.formatPlural(this.rate, "</b> item"));
		if (Util.hasMultipleRecipes(this.product)) {
			prod += " (using ";
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s/s)</html>", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "</b> cycle")), this.recipe.getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}
		ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.product, false), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		if (this.settings != null) ret.add(new JLabel(String.format("<html>requires <b>%s</b> %s%s</html>", Util.NUMBER_FORMAT.format(this.assemblers), Data.nameFor(this.settings.getAssembler().name), this.settings.getBonusString(true)), TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		return ret;
	}

	@Override
	public String getRawString() {
		String ret = String.format("%s at %s/s%s", Data.nameFor(this.product), Util.formatPlural(this.rate, "item"), Util.hasMultipleRecipes(this.product) ? String.format(" (using %s at %s/s)", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle")) : "");

		return ret + (this.settings != null ? String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblers), Data.nameFor(this.settings.getAssembler().name), this.settings.getBonusString(false)) : "");
	}

}
