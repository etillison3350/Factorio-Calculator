package factorio.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The {@code MiningDrill} class represents a special class of assembler which does research
 * @author ricky3350
 * @see {@link Technology}
 */
public class Lab extends Assembler {

	private final Collection<String> ingredients = new ArrayList<>();

	protected Lab(final String name, final Collection<String> ingredients, final double speed, final long energy, final int modules, final boolean burner, final double effectivity, final Collection<String> effects) {
		super(name, ingredients.size(), speed, energy, modules, burner, effectivity, Arrays.asList("lab-research"), effects);

		this.ingredients.addAll(ingredients);
	}

}
