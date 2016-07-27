package factorio.data;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class Recipe {

	public static final int ICON_SIZE = 22, SMALL_ICON_SIZE = 16;
	
	public final String name, type;
	private final Map<String, Float> results = new HashMap<>();
	private final Map<String, Float> ingredients = new HashMap<>();
	public final float time;
	private final ImageIcon icon, smallIcon;

	protected Recipe(String name, float time, Map<String, Float> ingredients, String result, Image icon) {
		this(name, time, ingredients, result, 1, icon);
	}

	protected Recipe(String name, String type, float time, Map<String, Float> ingredients, String result, Image icon) {
		this(name, type, time, ingredients, result, 1, icon);
	}

	protected Recipe(String name, float time, Map<String, Float> ingredients, String result, int resultCount, Image icon) {
		this(name, "crafting", time, ingredients, result, resultCount, icon);
	}

	protected Recipe(String name, String type, float time, Map<String, Float> ingredients, String result, float resultCount, Image icon) {
		this.name = name;
		this.type = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.put(result, resultCount);
		this.icon = new ImageIcon(icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));
		this.smallIcon = new ImageIcon(icon.getScaledInstance(SMALL_ICON_SIZE, SMALL_ICON_SIZE, Image.SCALE_SMOOTH));
	}

	protected Recipe(String name, float time, Map<String, Float> ingredients, Map<String, Float> results, Image icon) {
		this(name, "crafting", time, ingredients, results, icon);
	}

	protected Recipe(String name, String type, float time, Map<String, Float> ingredients, Map<String, Float> results, Image icon) {
		this.name = name;
		this.type = type.toLowerCase();
		this.time = time;
		this.ingredients.putAll(ingredients);
		this.results.putAll(results);
		this.icon = new ImageIcon(icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH));
		this.smallIcon = new ImageIcon(icon.getScaledInstance(SMALL_ICON_SIZE, SMALL_ICON_SIZE, Image.SCALE_SMOOTH));
	}

	public Map<String, Float> getIngredients() {
		return new HashMap<>(ingredients);
	}

	public Map<String, Float> getResults() {
		return new HashMap<>(results);
	}

	public double timeIn(Assembler assembler, double speedMultiplier) {
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

	public ImageIcon getIcon() {
		return icon;
	}
	
	public ImageIcon getSmallIcon() {
		return smallIcon;
	}

}
