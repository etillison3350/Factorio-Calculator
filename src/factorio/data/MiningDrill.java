package factorio.data;

import java.util.List;

/**
 * The {@code MiningDrill} class represents a special class of assembler which has a mining power as well as a speed
 * @author ricky3350
 * @see {@link MiningRecipe}
 */
public class MiningDrill extends Assembler {

	/**
	 * The mining power of the drill
	 */
	public final double power;

	protected MiningDrill(String name, double speed, double power, long energy, int modules, boolean burner, double effectivity, List<String> categories, List<String> effects) {
		super(name, 0, speed, energy, modules, burner, effectivity, categories, effects);

		this.power = power;
	}

}
