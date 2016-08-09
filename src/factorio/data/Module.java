package factorio.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@code Module} class represents an in-game module, an item which improves the performance of an assembler.
 * @author ricky3350
 */
public class Module {

	/**
	 * Maps effect names to the bonus they give.
	 */
	private final Map<String, Double> effects = new HashMap<>();

	/**
	 * A list of recipe names that this module can be used for. Can be empty if all are allowed
	 */
	private final Set<String> allowedRecipes = new HashSet<>();

	/**
	 * The internal name of the module.
	 */
	public final String name;

	protected Module(String name, Map<String, Double> effects, String... allowedRecipes) {
		this.name = name;
		this.effects.putAll(effects);
		Arrays.stream(allowedRecipes).forEach(this.allowedRecipes::add);
	}

	/**
	 * <ul>
	 * <b><i>getEffectValue</i></b><br>
	 * <pre>public double getEffectValue({@link String} effect)</pre>
	 * @param effect - the name of the effect
	 * @return the bonus for the given effect.
	 *         </ul>
	 */
	public double getEffectValue(String effect) {
		final Double value = this.effects.get(effect);
		if (value == null) return 0;
		return value;
	}

	/**
	 * <ul>
	 * <b><i>canCraft</i></b><br>
	 * <pre> boolean canCraft()</pre>
	 * @param recipeName - The name of the recipe to test
	 * @return whether or not this {@code Module} is able to be placed into an assembler crafting the recipe with the given
	 *         name.
	 *         </ul>
	 */
	public boolean canCraft(String recipeName) {
		return this.allowedRecipes.isEmpty() || this.allowedRecipes.contains(recipeName);
	}

}
