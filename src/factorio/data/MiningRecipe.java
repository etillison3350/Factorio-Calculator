package factorio.data;

import java.awt.Image;
import java.util.HashMap;

public class MiningRecipe extends Recipe {
	
	public final float hardness;
	
	protected MiningRecipe(String name, String type, float time, float hardness, String result, Image icon) {
		super(name, "mining-" + type, time, new HashMap<>(), result, icon);
		
		this.hardness = hardness;
	}
	
	@Override
	public double timeIn(Assembler assembler, double speedMultiplier) {
		if (!(assembler instanceof MiningDrill)) return super.timeIn(assembler, speedMultiplier);
		
		MiningDrill drill = (MiningDrill) assembler;
		
		return this.time / ((drill.power - this.hardness) * (drill.speed * speedMultiplier));
	}

}
