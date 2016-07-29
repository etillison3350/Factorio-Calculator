package factorio.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Assembler {

	private static final Map<String, Set<Assembler>> groupedAssemblers = new HashMap<>();

	private final Set<String> categories = new HashSet<>();
	private final Set<String> allowedEffects = new HashSet<>();
	public final String name;
	public final int ingredients;
	public final boolean burnerPowered;
	public final float fuelEffectivity;
	public final int modules;
	public final long energy;
	public final float speed;

	protected Assembler(String name, int ingredients, float speed, long energy, int modules, boolean burner, float effectivity, Collection<String> categories, Collection<String> effects) {
		for (String cat : categories) {
			if (!groupedAssemblers.containsKey(cat)) {
				groupedAssemblers.put(cat, new HashSet<>());
			}
			groupedAssemblers.get(cat).add(this);
		}

		this.name = name;
		this.ingredients = ingredients;
		this.speed = speed;
		this.energy = energy;
		this.modules = modules;
		this.burnerPowered = burner;
		this.fuelEffectivity = effectivity;
		this.categories.addAll(categories);
		this.allowedEffects.addAll(effects);
	}

	public boolean canCraftCategory(String category) {
		return categories.contains(category);
	}

	private String getPrimaryCategory() {
		int max = 0;
		String best = "";
		for (String cat : categories) {
			int size = groupedAssemblers.get(cat).size();
			if (max < size || (max == size && cat.length() > best.length())) {
				max = size;
				best = cat;
			}
		}
		return best;
	}
	
	public int compareCategoriesTo(Assembler other) {
		return this.getPrimaryCategory().compareTo(other.getPrimaryCategory());
	}
//
//	@Override
//	public int compareTo(Assembler o) {
//		int d = compareCategoriesTo(o);
//		if (d != 0) return d;
//		d = Integer.compare(this.ingredients, o.ingredients);
//		if (d != 0) return d;
//		d = -Boolean.compare(this.burnerPowered, o.burnerPowered);
//		if (d != 0) return d;
//		d = Float.compare(this.speed, o.speed);
//		if (d != 0) return d;
//		return this.name.compareTo(o.name);
//	}

}
