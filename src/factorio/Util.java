package factorio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import factorio.data.Data;
import factorio.data.Recipe;

public class Util {

	public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.####");
	public static final NumberFormat MODULE_FORMAT = new DecimalFormat("+0.##%;-0.##%");
	public static final NumberFormat ENERGY_FORMAT = new DecimalFormat("##0.###E0W");
	
	private static Collection<String> blacklist;
	
	private static Map<String, Boolean> multRecipe = new HashMap<>();

	private Util() {}

	public static String formatEnergy(double watts) {
		ENERGY_FORMAT.setMaximumFractionDigits(2);
		String ret = ENERGY_FORMAT.format(watts);
		
		ret = ret.replace("E0", "");
		ret = ret.replace("E3", "k");
		ret = ret.replace("E6", "M");
		ret = ret.replace("E9", "G");
		
		return ret;
	}

	public static boolean isBlacklisted(String recipeName) {
		if (blacklist == null) {
			Path blacklist = Paths.get("resources/recipe-blacklist.cfg");;
			if (!Files.exists(blacklist)) {
				try {
					Files.createDirectories(Paths.get("resources"));
					Files.createFile(blacklist);
				} catch (IOException e) {}
				Util.blacklist = new HashSet<>();
				return false;
			} else {
				try {
					Util.blacklist = new HashSet<>(Files.readAllLines(blacklist));
				} catch (IOException e) {
					Util.blacklist = new HashSet<>();
					return false;
				}
			}
		}
	
		return blacklist.contains(recipeName);
	}

	public static boolean hasMultipleRecipes(String product) {
		Boolean ret = multRecipe.get(product);
	
		if (ret == null) {
			int found = 0;
			for (Recipe r : Data.getRecipes()) {
				if (!isBlacklisted(r.name) && r.getResults().containsKey(product)) {
					if (found++ == 1)
						break;
				}
			}
			multRecipe.put(product, found >= 2);
			return found >= 2;
		}
		return ret;
	}
	
	public static String formatPlural(double number, String suffix) {
		return String.format("%s %s%s", NUMBER_FORMAT.format(number), suffix, Math.abs(number - 1) < 0.00001 ? "" : "s");
	}

}