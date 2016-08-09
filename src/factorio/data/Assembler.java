package factorio.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The {@code Assembler} represents any in-game entity that outputs items other than the items put into it, excluding players.
 * @author ricky3350
 */
public class Assembler {

	/**
	 * Maps all of the crafting categories of all created assemblers to all of the assemblers that can produce recipes in that
	 * category.
	 */
	private static final Map<String, Set<Assembler>> groupedAssemblers = new TreeMap<>();

	/**
	 * The categories that this {@code Assembler} can produce
	 */
	private final Set<String> categories = new HashSet<>();

	/**
	 * The module effects that are allowed in this assembler; can be empty if all are allowed
	 */
	private final Set<String> allowedEffects = new HashSet<>();

	/**
	 * The internal name of this assembler
	 */
	public final String name;

	/**
	 * The maximum number of ingredients that a recipe can have to be produced in this assembler
	 */
	public final int ingredients;

	/**
	 * Whether or not this assembler requires fuel
	 */
	public final boolean burnerPowered;

	/**
	 * The percentage of energy from fuel that is actually consumed by the assembler
	 */
	public final double fuelEffectivity;

	/**
	 * The maximum number of modules that can fit into the assembler
	 */
	public final int modules;

	/**
	 * The amount of energy, in watts, that this assembler consumes
	 */
	public final long energy;

	/**
	 * The speed multiplier of this assembler
	 */
	public final double speed;

	protected Assembler(String name, int ingredients, double speed, long energy, int modules, boolean burner, double effectivity, Collection<String> categories, Collection<String> effects) {
		for (final String cat : categories) {
			if (!groupedAssemblers.containsKey(cat)) {
				groupedAssemblers.put(cat, new HashSet<>());
			}
			groupedAssemblers.get(cat).add(this);
		}

		this.name = name;
		this.ingredients = ingredients;
		this.speed = speed;
		this.energy = energy;
		this.modules = modules;
		this.burnerPowered = burner;
		this.fuelEffectivity = effectivity;
		this.categories.addAll(categories);
		this.allowedEffects.addAll(effects);
	}

	/**
	 * <ul>
	 * <b><i>canCraftCategory</i></b><br>
	 * <pre> public boolean canCraftCategory({@link String} category)</pre>
	 * @param category - The category to test
	 * @return whether or not this {@code Assembler} can craft recipes of the given category
	 *         </ul>
	 */
	public boolean canCraftCategory(String category) {
		return this.categories.contains(category);
	}

	/**
	 * <ul>
	 * <b><i>getPrimaryCategory</i></b><br>
	 * <pre> private String getPrimaryCategory()</pre>
	 * @return the primary category of this assembler. This is the category that the most assemblers share with this assembler.
	 *         </ul>
	 */
	private String getPrimaryCategory() {
		int max = 0;
		String best = "";
		for (final String cat : this.categories) {
			final int size = groupedAssemblers.get(cat).size();
			if (max < size || (max == size && cat.length() > best.length())) {
				max = size;
				best = cat;
			}
		}
		return best;
	}

	/**
	 * <ul>
	 * <b><i>compareCategoriesTo</i></b><br>
	 * <pre> public int compareCategoriesTo(Assembler other)</pre>
	 * @param other - The assembler to compare against
	 * @return the result of {@link String#compareTo(String)} for this assembler's primary category, and the given assembler's
	 *         promary category (this is the category that the most assemblers share with this assembler).
	 *         </ul>
	 */
	public int compareCategoriesTo(Assembler other) {
		return this.getPrimaryCategory().compareTo(other.getPrimaryCategory());
	}
}
