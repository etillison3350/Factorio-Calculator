package factorio.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Assembler {

	private final SortedSet<String> categories = new TreeSet<>();
	private final Set<String> allowedEffects = new HashSet<>();
	public final String name;
	public final int ingredients;
	public final boolean coalPowered;
	public final int modules;
	public final long energy;
	public final float speed;

	protected Assembler(String name, int ingredients, float speed, long energy, int modules, boolean coal, Collection<String> categories, Collection<String> effects) {
		this.name = name;
		this.ingredients = ingredients;
		this.speed = speed;
		this.energy = energy;
		this.modules = modules;
		this.coalPowered = coal;
		this.categories.addAll(categories);
		this.allowedEffects.addAll(effects);
	}

	public boolean canCraftCategory(String category) {
		return categories.contains(category);
	}

	public int compareCategoriesTo(Assembler other) {
		Set<String> intersect = new HashSet<>(other.categories);
		intersect.retainAll(categories);
		if (intersect.size() != 0) {
			return 0;
		}
		return categories.first().compareTo(other.categories.first());
	}

}
