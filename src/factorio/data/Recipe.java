package factorio.data;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;

/**
 * The java representation for a recipe that takes in a number of ingredients, and produces some items as a result.
 * @author ricky3350
 */
public class Recipe {

	/**
	 * The size of a large icon, in pixels
	 */
	public static final int LARGE_ICON_SIZE = 22;

	/**
	 * The size of a small icon, in pixels
	 */
	public static final int SMALL_ICON_SIZE = 16;

	/**
	 * The internal name of this {@code Recipe}
	 */
	public final String name;

	/**
	 * The crafting category of this {@code Recipe}
	 */
	public final String category;

	/**
	 * Maps the names of the results of this {@code Recipe} to the amount produced.
	 */
	private final Map<String, Double> results = new HashMap<>();

	/**
	 * Maps the names of the ingredients of this {@code Recipe} to the amount required.
	 */
	private final Map<String, Double> ingredients = new HashMap<>();

	/**
	 * The time, in seconds, required to produce this {@code Recipe}
	 */
	public final double time;

	/**
	 * The large icon for this {@code Recipe}
	 */
	private final ImageIcon icon;

	/**
	 * The small icon for this {@code Recipe}
	 */
	private final ImageIcon smallIcon;

	protected Recipe(String name, double time, Map<String, Double> ingredients, String result, Image icon) {
		this(name, time, ingredients, result, 1, icon);
	}

	protected Recipe(String name, String type, double time, Map<String, Double> ingredients, String result, Image icon) {
		this(name, type, time, ingredients, result, 1, icon);
	}

	protected Recipe(String name, double time, Map<String, Double> ingredients, String result, int resultCount, Image icon) {
		this(name, "crafting", time, ingredients, result, resultCount, icon);
	}

	protected Recipe(String name, String type, double time, Map<String, Double> ingredients, String result, double resultCount, Image icon) {
		this.name = name;
		this.category = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.put(result, resultCount);
		this.icon = new ImageIcon(icon.getScaledInstance(LARGE_ICON_SIZE, LARGE_ICON_SIZE, Image.SCALE_SMOOTH));
		this.smallIcon = new ImageIcon(icon.getScaledInstance(SMALL_ICON_SIZE, SMALL_ICON_SIZE, Image.SCALE_SMOOTH));
	}

	protected Recipe(String name, double time, Map<String, Double> ingredients, Map<String, Double> results, Image icon) {
		this(name, "crafting", time, ingredients, results, icon);
	}

	protected Recipe(String name, String type, double time, Map<String, Double> ingredients, Map<String, Double> results, Image icon) {
		this.name = name;
		this.category = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.putAll(results);
		this.icon = new ImageIcon(icon.getScaledInstance(LARGE_ICON_SIZE, LARGE_ICON_SIZE, Image.SCALE_SMOOTH));
		this.smallIcon = new ImageIcon(icon.getScaledInstance(SMALL_ICON_SIZE, SMALL_ICON_SIZE, Image.SCALE_SMOOTH));
	}

	/**
	 * <ul>
	 * <b><i>getIngredients</i></b><br>
	 * <pre> public {@link Map}&lt;{@link String}, {@link Double}&gt; getIngredients()</pre>
	 * @return A map mapping the names of the ingredients of this {@code Recipe} to the respective amounts required.
	 *         </ul>
	 */
	public Map<String, Double> getIngredients() {
		return new TreeMap<>(this.ingredients);
	}

	/**
	 * <ul>
	 * <b><i>getResults</i></b><br>
	 * <pre> public {@link Map}&lt;{@link String}, {@link Double}&gt; getResults()</pre>
	 * @return A map mapping the names of the results of this {@code Recipe} to the respective amounts produced
	 *         </ul>
	 */
	public Map<String, Double> getResults() {
		return new TreeMap<>(this.results);
	}

	/**
	 * <ul>
	 * <b><i>timeIn</i></b><br>
	 * <pre>public double timeIn({@link Assembler} assembler, double speedMultiplier)</pre>
	 * @param assembler - The {@code Assembler} to calculate for
	 * @param speedMultiplier - The speed multiplier of the assembler
	 * @return The amount of time it would take to produce this {@code Recipe} in the given {@code Assembler} with the given
	 *         speed multiplier.
	 *         </ul>
	 */
	public double timeIn(Assembler assembler, double speedMultiplier) {
		return this.time / (assembler.speed * speedMultiplier);
	}

	@Override
	public String toString() {
		String ingString = "";
		for (final String item : this.ingredients.keySet())
			ingString += ", " + this.ingredients.get(item) + " " + item;
		if (ingString.isEmpty()) ingString = "  ";

		String resString = "";
		for (final String item : this.results.keySet())
			resString += ", " + this.results.get(item) + " " + item;
		if (resString.isEmpty()) resString = "  ";

		String name = Data.nameFor(this.name);
		if (name == null) name = "\"" + this.name + "\"";

		return this.category.toUpperCase().charAt(0) + this.category.substring(1).replace("-", " ") + " recipe " + name + ": " + ingString.substring(2) + " -> " + resString.substring(2) + " in " + this.time + "s";
	}

	/**
	 * <ul>
	 * <b><i>getIcon</i></b><br>
	 * <pre>public {@link ImageIcon} getIcon()</pre>
	 * @return the large icon for this {@code Recipe}
	 *         </ul>
	 */
	public ImageIcon getIcon() {
		return this.icon;
	}

	/**
	 * <ul>
	 * <b><i>getSmallIcon</i></b><br>
	 * <pre>public {@link ImageIcon} getSmallIcon()</pre>
	 * @return the small icon for this {@code Recipe}
	 *         </ul>
	 */
	public ImageIcon getSmallIcon() {
		return this.smallIcon;
	}

}
