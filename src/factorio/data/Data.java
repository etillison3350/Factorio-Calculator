package factorio.data;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

public class Data {

	public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0");
	public static final NumberFormat MODULE_FORMAT = new DecimalFormat("+0%;-0%");
	
	private Data() {}

	private static final Set<Recipe> recipes = new HashSet<>();
	private static final Set<Assembler> assemblers = new HashSet<>();
	private static final Set<Module> modules = new HashSet<>();

	private static final Map<String, Long> fuels = new HashMap<>();

	private static final Map<String, String> names = new HashMap<>();

	private static final Pattern LOCALE_HEADER = Pattern.compile("^\\[(.+?)\\]$");
	private static final Pattern LOCALE_ENTRY = Pattern.compile("^(.+?)=(.+)$");

	private static final Pattern MOD_PATH = Pattern.compile("__(.+?)__");

	public static void load(Path factorioDir, Path... mods) throws IOException {
		NUMBER_FORMAT.setMaximumFractionDigits(4);
		MODULE_FORMAT.setMaximumFractionDigits(2);
		
		LuaTable global = JsePlatform.standardGlobals();

		Path core = factorioDir.resolve("data/core");

		global.get("package").set("path", core.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua");

		Files.walk(core.resolve("lualib")).forEach(path -> {
			if (Files.isDirectory(path)) return;

			try {
				global.get("dofile").call(LuaValue.valueOf(path.toFile().getAbsolutePath()));
			} catch (LuaError e) {}
		});

		global.get("dofile").call(LuaValue.valueOf(core.resolve("data.lua").toFile().getAbsolutePath()));

		Map<String, Path> modPaths = new HashMap<>();

		for (int m = -1; m < mods.length; m++) {
			Path mod = m == -1 ? factorioDir.resolve("data/base") : mods[m];
			modPaths.put(m == -1 ? "base" : mods[m].getFileName().toString().replaceAll("_\\d+\\.\\d+\\.\\d+$", ""), mod);

			global.get("package").set("path", mod.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua");

			global.get("dofile").call(LuaValue.valueOf(mod.resolve("data.lua").toFile().getAbsolutePath()));

			Files.walk(mod.resolve("locale/en")).forEach(path -> {
				if (Files.isDirectory(path) || !path.getFileName().toString().endsWith(".cfg")) return;

				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (Exception e) {
					return;
				}

				boolean reading = false;
				for (String line : lines) {
					if (line.isEmpty()) continue;

					Matcher head = LOCALE_HEADER.matcher(line);
					if (head.find()) {
						reading = head.group(1).endsWith("-name") && !head.group(1).contains("category");
					} else {
						if (reading) {
							Matcher entry = LOCALE_ENTRY.matcher(line);
							if (!entry.find()) continue;
							names.put(entry.group(1), entry.group(2));
						}
					}
				}
			});
		}

		global.get("dofile").call(LuaValue.valueOf(Paths.get("resources/gather.lua").toFile().getAbsolutePath()));

		itemIconPaths = new HashMap<>();
		LuaValue k = LuaValue.NIL;
		while (true) {
			Varargs n = global.get("icons").next(k);
			if ((k = n.arg1()).isnil()) break;
			LuaValue v = n.arg(2);

			itemIconPaths.put(k.checkjstring(), Paths.get(resolve(v.checkjstring(), modPaths)));
		}

		LuaValue recipes = global.get("recipes");
		for (int i = 1; i <= recipes.length(); i++) {
			try {
				LuaValue recipe = recipes.get(i);

				String name = recipe.get("name").checkjstring();
				String type;
				try {
					LuaValue luaType = recipe.get("category");
					type = luaType == LuaValue.NIL ? "crafting" : luaType.checkjstring();
				} catch (LuaError err) {
					type = "crafting";
				}
				float time = (float) recipe.get("energy_required").optdouble(0.5);

				Map<String, Float> ingredients = new HashMap<>();
				LuaValue standardizedIngredients = global.get("getIngredients").call(recipe.get("ingredients"));
				for (int n = 1; n <= standardizedIngredients.length(); n++) {
					LuaValue ing = standardizedIngredients.get(n);
					ingredients.put(ing.get("name").checkjstring(), ing.get("amount").tofloat());
				}

				try {
					LuaValue luaResult = recipe.get("result");
					if (luaResult == LuaValue.NIL) throw new LuaError("");
					String result = luaResult.checkjstring();

					float resultCount = (float) recipe.get("result_count").optdouble(1.0);

					LuaValue luaIcon = recipe.get("icon");
					if (luaIcon == LuaValue.NIL) {
						luaIcon = global.get("icons").get(result);
					}

					Image icon;
					try {
						String iconPath = resolve(luaIcon.checkjstring(), modPaths);
						icon = ImageIO.read(Paths.get(iconPath).toFile());
					} catch (Exception e) {
						icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
					}

					Data.recipes.add(new Recipe(name, type, time, ingredients, result, resultCount, icon));
				} catch (LuaError err) {
					Map<String, Float> results = new HashMap<>();
					LuaValue standardizedResults = global.get("getIngredients").call(recipe.get("results"));
					for (int n = 1; n <= standardizedResults.length(); n++) {
						LuaValue ing = standardizedResults.get(n);
						results.put(ing.get("name").checkjstring(), ing.get("amount").tofloat());
					}

					LuaValue luaIcon = recipe.get("icon");
					if (luaIcon == LuaValue.NIL) {
						luaIcon = global.get("icons").get(results.keySet().iterator().next());
					}

					Image icon;
					try {
						String iconPath = resolve(luaIcon.checkjstring(), modPaths);
						icon = ImageIO.read(Paths.get(iconPath).toFile());
					} catch (Exception e) {
						icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
					}

					Data.recipes.add(new Recipe(name, type, time, ingredients, results, icon));
				}
			} catch (LuaError e) {
				e.printStackTrace(System.err);
			}
		}

		LuaValue resources = global.get("resources");
		for (int i = 1; i <= resources.length(); i++) {
			try {
				LuaValue resource = resources.get(i);

				LuaValue infinite = resource.get("infinite");
				if (infinite != LuaValue.NIL && infinite.checkboolean()) continue;

				String name = resource.get("name").checkjstring();
				String type;
				try {
					LuaValue luaType = resource.get("category");
					type = luaType == LuaValue.NIL ? "basic-solid" : luaType.checkjstring();
				} catch (LuaError err) {
					type = "basic-solid";
				}

				float time = resource.get("minable").get("mining_time").tofloat();

				float hardness = resource.get("minable").get("hardness").tofloat();

				String result;
				try {
					LuaValue luaResult = resource.get("minable").get("result");
					if (luaResult == LuaValue.NIL) throw new LuaError("");
					result = luaResult.checkjstring();
				} catch (LuaError err) {
					result = resource.get("results").get(0).get("name").checkjstring();
				}

				LuaValue luaIcon = resource.get("icon");
				if (luaIcon == LuaValue.NIL) {
					luaIcon = global.get("icons").get(result);
				}

				Image icon;
				try {
					String iconPath = resolve(luaIcon.checkjstring(), modPaths);
					icon = ImageIO.read(Paths.get(iconPath).toFile());
				} catch (Exception e) {
					icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
				}

				Data.recipes.add(new MiningRecipe(name, type, time, hardness, result, icon));
			} catch (LuaError e) {
				e.printStackTrace(System.err);
			}
		}

		LuaValue assemblers = global.get("assemblers");
		for (int i = 1; i <= assemblers.length(); i++) {
			try {
				LuaValue assembler = assemblers.get(i);

				String name = assembler.get("name").checkjstring();

				int ingredients = assembler.get("ingredient_count").optint(1);

				boolean coal = assembler.get("energy_source").get("type").checkjstring().contains("burner");

				float speed = (float) assembler.get("crafting_speed").optdouble(1);

				String nrg = assembler.get("energy_usage").checkjstring().replace("W", "").toLowerCase();
				long energy = Long.parseLong(nrg.replaceAll("\\D+", "")) * (nrg.endsWith("k") ? 1000 : (nrg.endsWith("m") ? 1000000 : (nrg.endsWith("g") ? 1000000000 : 1)));

				int modules;
				try {
					modules = assembler.get("module_specification").get("module_slots").optint(0);
				} catch (LuaError e) {
					modules = 0;
				}

				List<String> categories = new ArrayList<>();
				LuaValue luaCats = assembler.get("crafting_categories");
				for (int l = 1; l <= luaCats.length(); l++) {
					categories.add(luaCats.get(l).checkjstring());
				}

				List<String> effects = new ArrayList<>();
				LuaValue luaEff = assembler.get("allowed_effects");
				if (luaEff != LuaValue.NIL) {
					for (int l = 1; l < luaEff.length(); l++) {
						effects.add(luaEff.get(l).checkjstring());
					}
				} else {
					effects.add("all");
				}

				Data.assemblers.add(new Assembler(name, ingredients, speed, energy, modules, coal, categories, effects));
			} catch (LuaError e) {
				e.printStackTrace(System.err);
			}
		}

		LuaValue drills = global.get("drills");
		for (int i = 1; i <= drills.length(); i++) {
			try {
				LuaValue drill = drills.get(i);

				String name = drill.get("name").checkjstring();

				float power = drill.get("mining_power").tofloat();

				boolean coal = drill.get("energy_source").get("type").checkjstring().contains("burner");

				float speed = (float) drill.get("mining_speed").optdouble(1);

				String nrg = drill.get("energy_usage").checkjstring().replace("W", "").toLowerCase();
				long energy = Long.parseLong(nrg.replaceAll("\\D+", "")) * (nrg.endsWith("k") ? 1000 : (nrg.endsWith("m") ? 1000000 : (nrg.endsWith("g") ? 1000000000 : 1)));

				int modules;
				try {
					modules = drill.get("module_specification").get("module_slots").optint(0);
				} catch (LuaError e) {
					modules = 0;
				}

				List<String> categories = new ArrayList<>();
				LuaValue luaCats = drill.get("resource_categories");
				for (int l = 1; l <= luaCats.length(); l++) {
					categories.add("mining-" + luaCats.get(l).checkjstring());
				}

				List<String> effects = new ArrayList<>();
				LuaValue luaEff = drill.get("allowed_effects");
				if (luaEff != LuaValue.NIL) {
					for (int l = 1; l < luaEff.length(); l++) {
						effects.add(luaEff.get(l).checkjstring());
					}
				} else {
					effects.add("all");
				}

				Data.assemblers.add(new MiningDrill(name, speed, power, energy, modules, coal, categories, effects));
			} catch (LuaError e) {
				e.printStackTrace(System.err);
			}
		}

		LuaValue modules = global.get("modules");
		for (int i = 1; i <= modules.length(); i++) {
			try {
				LuaValue module = modules.get(i);

				String name = module.get("name").checkjstring();

				Map<String, Float> effects = new HashMap<>();
				LuaValue luaEffs = global.get("getEffects").call(module.get("effect"));
				for (int l = 1; l <= luaEffs.length(); l++) {
					LuaValue effect = luaEffs.get(l);
					effects.put(effect.get("effect").checkjstring(), effect.get("amount").tofloat());
				}

				List<String> limitation = new ArrayList<>();
				LuaValue luaLim = module.get("limitation");
				if (luaLim != LuaValue.NIL) {
					for (int l = 1; l < luaLim.length(); l++) {
						limitation.add(luaLim.get(l).checkjstring());
					}
				} else {
					limitation.add("all");
				}

				Data.modules.add(new Module(name, effects, limitation.toArray(new String[limitation.size()])));
			} catch (LuaError e) {
				e.printStackTrace(System.err);
				try {
					Thread.sleep(2);
				} catch (InterruptedException e1) {}
			}
		}

		LuaValue fuels = global.get("fuel");
		for (int i = 1; i <= fuels.length(); i++) {
			try {
				LuaValue fuel = fuels.get(i);

				String val = fuel.get("fuel_value").checkjstring().replace("J", "").toLowerCase();
				long value = Long.parseLong(val.replaceAll("\\D+", "")) * (val.endsWith("k") ? 1000 : (val.endsWith("m") ? 1000000 : (val.endsWith("g") ? 1000000000 : 1)));

				Data.fuels.put(fuel.get("name").checkjstring(), value);
			} catch (LuaError e) {
				e.printStackTrace(System.err);
			}
		}
	}

	private static String resolve(String path, Map<String, Path> mods) {
		Matcher m = MOD_PATH.matcher(path);

		if (m.find()) {
			return path.replace(m.group(), mods.get(m.group(1)).toFile().toString().replace('\\', '/'));
		} else {
			return path;
		}
	}

	private static Map<String, Path> itemIconPaths;
	private static Map<String, Icon> storedIcons = new HashMap<>();

	public static Icon getItemIcon(String icon) {
		Icon ret = storedIcons.get(icon);
		if (ret != null) return ret;

		if (itemIconPaths.containsKey(icon)) {
			try {
				ret = new ImageIcon(ImageIO.read(itemIconPaths.get(icon).toFile()).getScaledInstance(Recipe.SMALL_ICON_SIZE, Recipe.SMALL_ICON_SIZE, Image.SCALE_SMOOTH));
				storedIcons.put(icon, ret);
				return ret;
			} catch (Exception e) {}
		}
		return new ImageIcon(new BufferedImage(Recipe.SMALL_ICON_SIZE, Recipe.SMALL_ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE));
	}

	private static Collection<String> blacklist;

	public static boolean isBlacklisted(String recipeName) {
		if (blacklist == null) {
			Path blacklist = Paths.get("resources/recipe-blacklist.cfg");;
			if (!Files.exists(blacklist)) {
				try {
					Files.createDirectories(Paths.get("resources"));
					Files.createFile(blacklist);
				} catch (IOException e) {}
				Data.blacklist = new HashSet<>();
				return false;
			} else {
				try {
					Data.blacklist = new HashSet<>(Files.readAllLines(blacklist));
				} catch (IOException e) {
					Data.blacklist = new HashSet<>();
					return false;
				}
			}
		}

		return blacklist.contains(recipeName);
	}

	private static Map<String, Boolean> multRecipe = new HashMap<>();

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

	public static String nameFor(String id) {
		return names.get(id);
	}

	public static String nameFor(Recipe recipe) {
		String ret = nameFor(recipe.name);
		if (ret == null) return nameFor(recipe.getResults().keySet().iterator().next());
		return ret;
	}

	public static SortedSet<Recipe> getRecipesSorted() {
		SortedSet<Recipe> ret = new TreeSet<>(new Comparator<Recipe>() {

			@Override
			public int compare(Recipe o1, Recipe o2) {
				int ret = nameFor(o1).toLowerCase().compareTo(nameFor(o2).toLowerCase());
				if (ret == 0) return o1.name.compareTo(o2.name);
				return ret;
			}

		});
		ret.addAll(recipes);
		return ret;
	}

	public static Set<Recipe> getRecipes() {
		return new HashSet<>(recipes);
	}

	public static Set<Assembler> getAssemblers() {
		return new HashSet<>(assemblers);
	}

	public static Set<Module> getModules() {
		return new HashSet<>(modules);
	}

}
