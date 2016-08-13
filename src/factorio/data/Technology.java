package factorio.data;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code MiningRecipe} class is a special type of recipe completed in a lab, with a count field indicating how many sets of
 * ingredients are required to complete the research, and a number field, for sucessive upgrades. Note that the count field is
 * purely for display purposes; the ingredients are multiplied by the count field on construction.
 * @author ricky3350
 */
public class Technology extends Recipe {

	public final int count;
	public final int number;

	protected Technology(final String name, final double time, final Map<String, ? extends Number> ingredients, final int count, final Image icon) {
		super(name.matches(".*?\\d$") ? name.substring(0, name.lastIndexOf('-')) : name, "lab-research", time * count, multiply(ingredients, count), new HashMap<>(), icon);

		this.count = count;
		this.number = name.matches(".*?\\d$") ? Integer.parseInt(name.substring(name.lastIndexOf('-') + 1)) : 0;
	}

	private static Map<String, Double> multiply(final Map<String, ? extends Number> ingredients, final int count) {
		final HashMap<String, Double> ret = new HashMap<>(ingredients.size());
		ingredients.forEach((i, n) -> ret.put(i, n.doubleValue() * count));
		return ret;
	}

}
