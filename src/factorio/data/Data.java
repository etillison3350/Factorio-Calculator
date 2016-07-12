package factorio.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class Data {

	private Data() {}

	private static final Path package_path_lua = Paths.get("lua/package_path.lua");

	private static final Set<Recipe> recipes = new HashSet<>();
	private static final Set<Assembler> assemblers = new HashSet<>();
	private static final Set<Module> modules = new HashSet<>();

	private static final Map<String, Long> fuels = new HashMap<>();

	private static final Map<String, String> names = new HashMap<>();

	private static final Pattern LOCALE_HEADER = Pattern.compile("^\\[(.+?)\\]$");
	private static final Pattern LOCALE_ENTRY = Pattern.compile("^(.+?)=(.+)$");

	public static void load(Path factorioDir, Path... mods) throws IOException {
		LuaTable global = JsePlatform.standardGlobals();

		Path core = factorioDir.resolve("data/core");

		Files.write(package_path_lua, ("package.path = '" + core.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua'").getBytes());
		global.get("dofile").call(LuaValue.valueOf(package_path_lua.toFile().getAbsolutePath()));

		Files.walk(core.resolve("lualib")).forEach(path -> {
			if (Files.isDirectory(path)) return;

			try {
				global.get("dofile").call(LuaValue.valueOf(path.toFile().getAbsolutePath()));
			} catch (LuaError e) {}
		});

		global.get("dofile").call(LuaValue.valueOf(core.resolve("data.lua").toFile().getAbsolutePath()));

		for (int m = -1; m < mods.length; m++) {
			Path mod = m == -1 ? factorioDir.resolve("data/base") : mods[m];

			Files.write(package_path_lua, ("package.path = '" + mod.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua'").getBytes());
			global.get("dofile").call(LuaValue.valueOf(package_path_lua.toFile().getAbsolutePath()));

			global.get("dofile").call(LuaValue.valueOf(mod.resolve("data.lua").toFile().getAbsolutePath()));

			global.get("dofile").call(LuaValue.valueOf(Paths.get("lua/gather.lua").toFile().getAbsolutePath()));

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

						Data.recipes.add(new Recipe(name, type, time, ingredients, result, resultCount));
					} catch (LuaError err) {
						Map<String, Float> results = new HashMap<>();
						LuaValue standardizedResults = global.get("getIngredients").call(recipe.get("results"));
						for (int n = 1; n <= standardizedResults.length(); n++) {
							LuaValue ing = standardizedResults.get(n);
							results.put(ing.get("name").checkjstring(), ing.get("amount").tofloat());
						}

						Data.recipes.add(new Recipe(name, type, time, ingredients, results));
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

					try {
						LuaValue luaResult = resource.get("minable").get("result");
						if (luaResult == LuaValue.NIL) throw new LuaError("");
						String result = luaResult.checkjstring();

						Data.recipes.add(new MiningRecipe(name, type, time, hardness, result));
					} catch (LuaError err) {
						String result = resource.get("results").get(0).get("name").checkjstring();

						Data.recipes.add(new MiningRecipe(name, type, time, hardness, result));
					}
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
						reading = head.group(1).equals("item-name") || head.group(1).equals("recipe-name") || head.group(1).equals("entity-name") || head.group(1).equals("fluid-name");
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
	}

	public static String nameFor(String id) {
		return names.get(id);
	}

}
