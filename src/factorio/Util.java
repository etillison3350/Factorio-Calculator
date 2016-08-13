package factorio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import factorio.data.Data;
import factorio.data.Recipe;

/**
 * A non-instantiatable class holding utility methods and objects for various purposes.
 * @author ricky3350
 */
public class Util {

	/**
	 * A general {@link NumberFormat} for numbers
	 */
	public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,##0.####");

	/**
	 * A {@link NumberFormat} for module bonuses, as a percent with a leading plus sign
	 */
	public static final NumberFormat MODULE_FORMAT = new DecimalFormat("+0.##%;-0.##%");

	/**
	 * A {@link NumberFormat} for energy, in engineering notation with a <code>'W'</code> suffix
	 */
	public static final NumberFormat ENERGY_FORMAT = new DecimalFormat("##0.##E0W");

	/**
	 * A {@link Pattern} matching the exponent of {@link #ENERGY_FORMAT}
	 */
	private static final Pattern ENERGY_PATTERN = Pattern.compile("E(\\d+)");

	/**
	 * The SI unit prefixes, to replace the exponent in {@link #ENERGY_FORMAT}
	 */
	private static final String[] PREFIXES = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

	static {
		ENERGY_FORMAT.setMaximumFractionDigits(2);
	}

	/**
	 * A {@link Collection} of recipe names that cannot be used without the user specifically using them as input
	 */
	private static Collection<String> blacklist;

	/**
	 * Maps an item name to whether or not it is a product of multiple {@link Recipe}s in {@link Data#getRecipes()}
	 */
	private static Map<String, Boolean> multRecipe = new HashMap<>();

	/**
	 * <ul>
	 * <b><i>formatEnergy</i></b><br>
	 * <br>
	 * <pre> public static {@link String} formatEnergy(double watts)</pre> <br>
	 * @param watts - The value to format
	 * @return <code>watts</code>, formatted in engineering notation with SI unit prefixes
	 *         </ul>
	 */
	public static String formatEnergy(final double watts) {
		final String ret = ENERGY_FORMAT.format(watts);

		final Matcher m = ENERGY_PATTERN.matcher(ret);
		m.find();

		return ret.replace(m.group(), PREFIXES[Integer.parseInt(m.group(1)) / 3]);
	}

	/**
	 * <ul>
	 * <b><i>formatPlural</i></b><br>
	 * <br>
	 * <pre> public static {@link String} formatPlural(double number, {@code String} suffix)</pre> <br>
	 * Formats the given number, then appends the given suffix. Adds an <code>s</code> if the number is not within
	 * <code>1e-4</code> of 0.
	 * @param number - The number to format
	 * @param suffix - The suffix to append
	 * @return <code>number</code>, formatted as decribed above
	 *         </ul>
	 */
	public static String formatPlural(final double number, final String suffix) {
		return String.format("%s %s%s", NUMBER_FORMAT.format(number), suffix, Math.abs(number - 1) < 1e-4 ? "" : "s");
	}

	/**
	 * <ul>
	 * <b><i>hasMultipleRecipes</i></b><br>
	 * <br>
	 * <pre> public static boolean hasMultipleRecipes({@link String} product)</pre> <br>
	 * @param product - The name of the item produced
	 * @return Whether or not multiple {@link Recipe}s in {@link Data#getRecipes()}
	 *         </ul>
	 */
	public static boolean hasMultipleRecipes(final String product) {
		final Boolean ret = multRecipe.get(product);

		if (ret == null) {
			int found = 0;
			for (final Recipe r : Data.getRecipes())
				if (!isBlacklisted(r.name) && r.getResults().containsKey(product)) if (found++ == 1) break;
			multRecipe.put(product, found >= 2);
			return found >= 2;
		}
		return ret;
	}

	/**
	 * <ul>
	 * <b><i>isBlacklisted</i></b><br>
	 * <br>
	 * <pre> public static boolean isBlacklisted({@link String} recipeName)</pre> <br>
	 * @param recipeName - The name of the recipe
	 * @return Whether or not recipes with the given name can be used without the user specifically using them as input
	 *         </ul>
	 */
	public static boolean isBlacklisted(final String recipeName) {
		if (blacklist == null) {
			final Path blacklist = Paths.get("resources/recipe-blacklist.cfg");;
			if (!Files.exists(blacklist)) {
				try {
					Files.createDirectories(Paths.get("resources"));
					Files.createFile(blacklist);
				} catch (final IOException e) {}
				Util.blacklist = new HashSet<>();
				return false;
			} else {
				try {
					Util.blacklist = new HashSet<>(Files.readAllLines(blacklist));
				} catch (final IOException e) {
					Util.blacklist = new HashSet<>();
					return false;
				}
			}
		}

		return blacklist.contains(recipeName);
	}

	// Util cannot be instantiated
	private Util() {}

}
