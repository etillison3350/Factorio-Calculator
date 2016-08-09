package factorio.data;

import java.awt.Image;
import java.util.HashMap;

/**
 * The {@code MiningRecipe} class is a special type of recipe with a hardness field in addition to the time field.
 * @author ricky3350
 * @see {@link MiningDrill}
 */
public class MiningRecipe extends Recipe {

	public final double hardness;

	protected MiningRecipe(String name, String type, double time, double hardness, String result, Image icon) {
		super(name, "mining-" + type, time, new HashMap<>(), result, icon);

		this.hardness = hardness;
	}

	@Override
	public double timeIn(Assembler assembler, double speedMultiplier) {
		if (!(assembler instanceof MiningDrill)) return super.timeIn(assembler, speedMultiplier);

		final MiningDrill drill = (MiningDrill) assembler;

		return this.time / ((drill.power - this.hardness) * (drill.speed * speedMultiplier));
	}

}
