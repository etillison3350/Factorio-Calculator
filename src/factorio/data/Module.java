package factorio.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Module {

	private final Map<String, Float> effects = new HashMap<>();
	private final Set<String> allowedRecipes = new HashSet<>();
	public final String name;
	
	protected Module(String name, Map<String, Float> effects, String... allowedRecipes) {
		this.name = name;
		this.effects.putAll(effects);
		Arrays.stream(allowedRecipes).forEach(this.allowedRecipes::add);
	}
	
	public float getEffectValue(String effect) {
		Float value = effects.get(effect);
		if (value == null) {
			return 1;
		}
		return value + 1;
	}

}
