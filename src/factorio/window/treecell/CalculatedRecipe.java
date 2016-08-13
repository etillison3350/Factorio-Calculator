package factorio.window.treecell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
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
 * A {@link Recipe} in a {@link Calculation}. Calculates for its ingredients/children
 * @author ricky3350
 */
public class CalculatedRecipe implements TreeCell, Comparable<CalculatedRecipe> {

	/**
	 * Whether or not the component for this {@code CalculatedRecipe} will have a "Fuel" label
	 */
	public final boolean hasFuelLabel;

	/**
	 * If the product of this {@code CalculatedRecipe} is fuel or a component of a fuel, this is the name of that fuel.
	 * Otherwise, it is <code>null</code> .
	 */
	public final String fuel;

	/**
	 * The name of the item being produced
	 */
	public final String product;

	/**
	 * The recipe used to produce {@link #product}
	 */
	private Recipe recipe;

	/**
	 * The rate, in items per second, to calculate for.
	 */
	private double rate;

	/**
	 * The rate, in recipes completed per second, to calculate for.
	 */
	private double recipeRate;

	/**
	 * The amount of assemblers (type given by {@link #assembler}) required to produce {@link #product} at {@link #rate} items/s
	 */
	private double assemblerCount;

	/**
	 * The {@link AssemblerSettings} for the assembler that {@link #recipe} would be assembled in
	 */
	private AssemblerSettings assembler;

	/**
	 * Maps the names of the ingredients of {@link #recipe} to the {@code CalculatedRecipe}s representing them
	 */
	private final Map<String, CalculatedRecipe> ingredients = new TreeMap<>();

	public CalculatedRecipe(final Recipe recipe, final double rate) {
		if (recipe == null) throw new IllegalArgumentException("recipe cannot be null");

		this.hasFuelLabel = false;
		this.fuel = null;

		this.recipe = recipe;
		this.product = this.recipe.getResults().size() == 1 ? this.recipe.getResults().keySet().iterator().next() : null;
		this.recipeRate = rate;
		this.rate = this.product == null ? Double.NaN : this.recipeRate * this.recipe.getResults().get(this.product);
		this.assembler = AssemblerSettings.getDefaultSettings(this.recipe);

		this.calculateAssemblers();
		addIngredients(this.ingredients, this.recipe, this.assembler, this.recipeRate, null, this.assemblerCount, new ArrayList<>());
	}

	public CalculatedRecipe(final String product, final double rate) {
		this(product, rate, new ArrayList<>(), null);
	}

	private CalculatedRecipe(final String product, final double rate, final Collection<String> banned, final String fuel) {
		this.hasFuelLabel = false;
		this.fuel = fuel;

		this.product = product;
		this.rate = rate;

		for (final Recipe r : Data.getRecipes()) {
			if (r.getResults().containsKey(product) && !Util.isBlacklisted(r.name) && !banned.contains(r.name)) {
				this.recipe = r;
				this.assembler = AssemblerSettings.getDefaultSettings(this.recipe);

				this.recipeRate = this.rate / this.recipe.getResults().get(product);

				this.calculateAssemblers();
				addIngredients(this.ingredients, this.recipe, this.assembler, this.recipeRate, this.fuel, this.assemblerCount, banned);

				break;
			}
		}
	}

	private CalculatedRecipe(final String fuel, final double energy, final boolean inclIngredients) {
		this.hasFuelLabel = true;
		this.fuel = fuel;
		this.product = fuel;
		this.rate = energy / Data.getFuelValue(fuel);

		for (final Recipe r : Data.getRecipes()) {
			if (r.getResults().containsKey(this.product) && !Util.isBlacklisted(r.name)) {
				this.recipe = r;
				this.assembler = AssemblerSettings.getDefaultSettings(this.recipe);

				this.recipeRate = this.rate / this.recipe.getResults().get(this.product);

				this.calculateAssemblers();
				if (inclIngredients) {
					addIngredients(this.ingredients, this.recipe, this.assembler, this.recipeRate, this.fuel, this.assemblerCount, new ArrayList<>());
				}

				break;
			}
		}

		// The number of fuel items required to produce 1 fuel item per second.
		final double cost = totalFuelRequirements(this.ingredients.values(), fuel);

		this.setRate(this.rate / (1 - (cost / this.rate)));
	}

	/**
	 * <ul>
	 * <b><i>addIngredients</i></b><br>
	 * <pre>private static void addIngredients({@link Map}&lt;{@link String}, CalculatedRecipe&gt; ingredients, {@link Recipe} recipe, {@link AssemblerSettings} assembler, double recipeRate, String fuel, {@link Collection}&lt;String&gt; banned)</pre>
	 * Adds new ingredients to the specified {@code Map}
	 * @param ingredients - the map to put the new ingredients in
	 * @param recipe - the recipe whose ingredients are to be added
	 * @param assembler - the assembler that the ingredients should calculate for
	 * @param recipeRate - the rate at which the recipe is being assemblerd (cycles/s)
	 * @param fuel - if the ingredients are part of a fuel, this is the name of that fuel. Otherwise, it is <code>null</code>.
	 * @param assemblerCount - the number of assemblers assembling {@code recipe}
	 * @param banned - a {@code Collection} of recipes that cannot be used in ingredients
	 *        </ul>
	 */
	private static void addIngredients(final Map<String, CalculatedRecipe> ingredients, final Recipe recipe, final AssemblerSettings assembler, final double recipeRate, final String fuel, final double assemblerCount, final Collection<String> banned) {
		final Collection<String> newBanned = new ArrayList<>(banned);
		newBanned.add(recipe.name);
		for (final String ingredient : recipe.getIngredients().keySet()) {
			ingredients.put(ingredient, new CalculatedRecipe(ingredient, recipeRate * recipe.getIngredients().get(ingredient) / assembler.getProductivity(), newBanned, fuel));
		}
		if (assembler.getAssembler().burnerPowered) {
			ingredients.put("__FUEL__" + assembler.getFuel(), new CalculatedRecipe(assembler.getFuel(), assemblerCount * assembler.getAssembler().energy * assembler.getEfficiency(), fuel == null));
		}
	}

	/**
	 * <ul>
	 * <b><i>totalFuelRequirements</i></b><br>
	 * <pre>private static double totalFuelRequirements({@link Collection}&lt;CalculatedRecipe&gt;, {@link String} fuel)</pre>
	 * Calculates the total rate (in items/s) of production of all {@code CalculatedRecipe}s in {@code ingredients} (including
	 * their children) that produce the given fuel as a fuel. <b>These are <i>removed</i> from the collection</b>.
	 * @param ingredients - a {@code Collection} of {@code CalculatedRecipe}s to total.
	 * @param fuel - the name of the fuel item to search for.
	 * @return the total rate, as described above.
	 *         </ul>
	 */
	private static double totalFuelRequirements(final Collection<CalculatedRecipe> ingredients, final String fuel) {
		double ret = 0;
		final Set<CalculatedRecipe> toRemove = new HashSet<>();
		for (final CalculatedRecipe r : ingredients) {
			if (r.hasFuelLabel && r.fuel.equals(fuel)) {
				toRemove.add(r);
			} else {
				ret += totalFuelRequirements(r.ingredients.values(), fuel);
			}
		}
		ingredients.removeAll(toRemove);

		return ret + toRemove.stream().mapToDouble(cr -> cr.rate).sum();
	}

	/**
	 * <ul>
	 * <b><i>calculateAssemblers</i></b><br>
	 * <pre>private void calculateAssemblers()</pre> Calculates the number of assemblers required to produce {@link #recipe} at
	 * {@link #recipeRate} cycles/s in {@link #assembler} (sets {@link #assemblerCount})
	 * </ul>
	 */
	private void calculateAssemblers() {
		if (this.recipe != null && this.assembler != null) {
			this.assemblerCount = this.recipe.timeIn(this.assembler.getAssembler(), this.assembler.getSpeed()) * this.recipeRate / this.assembler.getProductivity();
		}
	}

	@Override
	public int compareTo(final CalculatedRecipe o) {
		final int d = (this.product == null ? Data.nameFor(this.recipe) : Data.nameFor(this.product)).compareTo((o.product == null ? Data.nameFor(o.recipe) : Data.nameFor(o.product)));
		if (d != 0) return d;
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	/**
	 * <ul>
	 * <b><i>getAssembler</i></b><br>
	 * <pre>public {@link AssemblerSettings} getAssembler()</pre>
	 * @return the {@link AssemblerSettings} that this {@code CalculatedRecipe} is calculating for
	 *         </ul>
	 */
	public AssemblerSettings getAssembler() {
		return this.assembler;
	}

	/**
	 * <ul>
	 * <b><i>getAssemblers</i></b><br>
	 * <pre>public double getAssemblers()</pre>
	 * @return the number of assemblers required to produce {@link #recipe} at {@link #recipeRate} cycles/s in
	 *         {@link #assembler}
	 *         </ul>
	 */
	public double getAssemblers() {
		return this.assemblerCount;
	}

	/**
	 * <ul>
	 * <b><i>getIngredients</i></b><br>
	 * <pre>public {@link SortedSet}&lt;CalculatedRecipe&gt; getIngredients()</pre>
	 * @return a {@code SortedSet} of the ingredients (children) of this {@code CalculatedRecipe}
	 *         </ul>
	 */
	public SortedSet<CalculatedRecipe> getIngredients() {
		return new TreeSet<>(this.ingredients.values());
	}

	/**
	 * <ul>
	 * <b><i>getRate</i></b><br>
	 * <pre>public double getRate()</pre>
	 * @return the rate, in items per second, that this {@code CalculatedRecipe} is calculating for, or {@link Double#NaN} if
	 *         this {@code CalculatedRecipe} is not calculating for a specific {@link #product} (i.e. {@code product} is
	 *         <code>null</code>)
	 *         </ul>
	 */
	public double getRate() {
		return this.rate;
	}

	@Override
	public String getRawString() {
		String ret;
		if (this.product == null)
			ret = String.format("%s at %s/s", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle"));
		else
			ret = String.format("%s%s at %s/s%s", (this.hasFuelLabel ? "Fuel: " : ""), Data.nameFor(this.product), Util.formatPlural(this.rate, "item"), Util.hasMultipleRecipes(this.product) ? String.format(" (using %s at %s/s)", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle")) : "");

		return ret + (this.assembler != null ? String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(false)) : "");
	}

	/**
	 * <ul>
	 * <b><i>getRecipe</i></b><br>
	 * <pre>public {@link Recipe} getRecipe()</pre>
	 * @return the {@code Recipe} that this {@code CalculatedRecipe} is calculating for
	 *         </ul>
	 */
	public Recipe getRecipe() {
		return this.recipe;
	}

	/**
	 * <ul>
	 * <b><i>getRecipeRate</i></b><br>
	 * <pre>public double getRecipeRate()</pre>
	 * @return the rate, in cycles per second, that this {@code CalculatedRecipe} is calculating for
	 *         </ul>
	 */
	public double getRecipeRate() {
		return this.recipeRate;
	}

	@Override
	public Component getTreeCellRendererComponent(final boolean selected, final boolean hasFocus) {
		final JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected, hasFocus);

		final String asm = this.assembler != null ? String.format(" requires <b>%s</b> %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : "";

		if (this.product == null) {
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s/s%s</html>", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "</b> cycle"), asm), this.recipe.getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else {
			String prod = String.format("<html><b>%s%s</b> at <b>%s/s", (this.hasFuelLabel ? "Fuel</b>: <b>" : ""), Data.nameFor(this.product), Util.formatPlural(this.rate, "</b> item"));
			if (Util.hasMultipleRecipes(this.product)) {
				prod += " (using ";
				ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s/s)%s</html>", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "</b> cycle"), asm), this.recipe.getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			} else {
				prod += asm;
			}
			ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.product, false), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}

	/**
	 * <ul>
	 * <b><i>setRate</i></b><br>
	 * <pre> public void setRate(double rate)</pre> Sets the rate of this <code>CalculatedRecipe</code>, and updates the
	 * assembler count and rate for all nested <code>CalculatedRecipe</code>s
	 * @param rate - The rate to set
	 *        </ul>
	 */
	public void setRate(final double rate) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(this.product).doubleValue();

		this.calculateAssemblers();
		this.updateIngredients();
	}

	/**
	 * <ul>
	 * <b><i>setRateAndSettings</i></b><br>
	 * <pre> public void setRateAndSettings(double rate, AssemblerSettings settings)</pre> Sets the settings and rate of the
	 * {@code CalculatedRecipe}s as specified in {@link #setSettings(AssemblerSettings)} and {@link #setRate(double)}
	 * respectively
	 * @param rate - The rate to set
	 * @param settings - The settings to set
	 *        </ul>
	 */
	public void setRateAndSettings(final double rate, final AssemblerSettings settings) {
		this.rate = rate;
		if (this.recipe != null) this.recipeRate = this.rate / this.recipe.getResults().get(this.product).doubleValue();
		this.assembler = settings;

		this.calculateAssemblers();
		this.updateIngredients();
	}

	/**
	 * <ul>
	 * <b><i>setSettings</i></b><br>
	 * <pre> public void setSettings({@link AssemblerSettings} settings)</pre> Sets the {@code AssemblerSettings} for this
	 * {@code CalculatedRecipe}, and updates the assembler count for this {@code CalculatedRecipe} <b>only</b> .
	 * @param settings - The settings to set
	 *        </ul>
	 */
	public void setSettings(final AssemblerSettings settings) {
		this.assembler = settings;

		this.calculateAssemblers();
		this.updateIngredients();
	}

	/**
	 * <ul>
	 * <b><i>updateIngredients</i></b><br>
	 * <pre>private void updateIngredients()</pre> Updates the children in {@link #ingredients}
	 * </ul>
	 */
	private void updateIngredients() {
		if (this.recipe != null && this.assembler != null) {
			for (final String ingredient : this.ingredients.keySet()) {
				this.ingredients.get(ingredient).setRate(ingredient.startsWith("__FUEL__") ? this.assembler.getAssembler().energy * this.assembler.getEfficiency() * this.assemblerCount / Data.getFuelValue(this.fuel) : this.recipeRate * this.recipe.getIngredients().get(ingredient) / this.assembler.getProductivity());
			}
		}
	}

}
