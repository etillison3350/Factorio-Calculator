package factorio.data;

import java.util.List;

public class MiningDrill extends Assembler {

	public final float power;

	protected MiningDrill(String name, float speed, float power, long energy, int modules, boolean burner, float effectivity, List<String> categories, List<String> effects) {
		super(name, 0, speed, energy, modules, burner, effectivity, categories, effects);

		this.power = power;
	}

}
