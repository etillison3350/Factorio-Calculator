package factorio.data;

import java.util.HashMap;
import java.util.Map;

public class Recipe {

	public final String name, type;
	private final Map<String, Float> results = new HashMap<>();
	private final Map<String, Float> ingredients = new HashMap<>();
	public final float time;

	public Recipe(String name, float time, Map<String, Float> ingredients, String result) {
		this(name, time, ingredients, result, 1);
	}

	public Recipe(String name, String type, float time, Map<String, Float> ingredients, String result) {
		this(name, type, time, ingredients, result, 1);
	}

	public Recipe(String name, float time, Map<String, Float> ingredients, String result, int resultCount) {
		this(name, "crafting", time, ingredients, result, resultCount);
	}

	public Recipe(String name, String type, float time, Map<String, Float> ingredients, String result, float resultCount) {
		this.name = name;
		this.type = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.put(result, resultCount);
	}

	public Recipe(String name, float time, Map<String, Float> ingredients, Map<String, Float> results) {
		this(name, "crafting", time, ingredients, results);
	}

	public Recipe(String name, String type, float time, Map<String, Float> ingredients, Map<String, Float> results) {
		this.name = name;
		this.type = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.putAll(results);
	}

	public Map<String, Float> getIngredients() {
		return new HashMap<>(ingredients);
	}

	public Map<String, Float> getResults() {
		return new HashMap<>(results);
	}
	
	public float timeIn(Assembler assembler, float speedMultiplier) {
		return this.time / (assembler.speed * speedMultiplier);
	}

	@Override
	public String toString() {
		String ingString = "";
		for (String item : ingredients.keySet())
			ingString += ", " + ingredients.get(item) + " " + item;
		if (ingString.isEmpty()) ingString = "  ";
		
		String resString = "";
		for (String item : results.keySet())
			resString += ", " + results.get(item) + " " + item;
		if (resString.isEmpty()) resString = "  ";
		
		String name = Data.nameFor(this.name);
		if (name == null) name = "\"" + this.name + "\"";
		
		return this.type.toUpperCase().charAt(0) + this.type.substring(1).replace("-", " ") + " recipe " + name + ": " + ingString.substring(2) + " -> " + resString.substring(2) + " in " + time + "s";
	}

}
