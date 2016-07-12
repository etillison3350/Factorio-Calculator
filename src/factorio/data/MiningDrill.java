package factorio.data;

import java.util.List;

public class MiningDrill extends Assembler {

	public final float power;
	
	public MiningDrill(String name, float speed, float power, long energy, int modules, boolean coal, List<String> categories, List<String> effects) {
		super(name, 0, speed, energy, modules, coal, categories, effects);
		
		this.power = power;
	}
	
}
