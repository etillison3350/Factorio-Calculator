package factorio.data;

import java.util.HashMap;

public class MiningRecipe extends Recipe {
	
	public final float hardness;
	
	public MiningRecipe(String name, String type, float time, float hardness, String result) {
		super(name, "mining-" + type, time, new HashMap<>(), result);
		
		this.hardness = hardness;
	}
	
	@Override
	public float timeIn(Assembler assembler, float speedMultiplier) {
		if (!(assembler instanceof MiningDrill)) return super.timeIn(assembler, speedMultiplier);
		
		MiningDrill drill = (MiningDrill) assembler;
		
		return this.time / ((drill.power - this.hardness) * (drill.speed * speedMultiplier));
	}

}
