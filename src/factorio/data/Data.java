package factorio.data;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import factorio.Main;

/**
 * A non-instantiable class for loading and storing the prototypes from lua in all of the mods
 * @author ricky3350
 */
public class Data {

	/**
	 * All of the {@link Recipe}s that have been loaded.
	 */
	private static final Set<Recipe> recipes = new HashSet<>();

	/**
	 * All of the {@link Assembler}s that have been loaded.
	 */
	private static final Set<Assembler> assemblers = new HashSet<>();

	/**
	 * All of the {@link Module}s that have been loaded.
	 */
	private static final Set<Module> modules = new HashSet<>();

	/**
	 * Maps the names of fuels to their energy value, in joules.
	 */
	private static final Map<String, Long> fuels = new HashMap<>();

	/**
	 * Maps internal names to in-game names.
	 */
	private static final Map<String, String> names = new HashMap<>();

	/**
	 * A {@link Pattern} to match headers in locale files.
	 */
	private static final Pattern LOCALE_HEADER = Pattern.compile("^\\[(.+?)\\]$");

	/**
	 * A {@link Pattern} to match enries in locale files.
	 */
	private static final Pattern LOCALE_ENTRY = Pattern.compile("^(.+?)=(.+)$");

	/**
	 * A {@link Pattern} to match the mod path in icon paths.
	 */
	private static final Pattern MOD_PATH = Pattern.compile("__(.+?)__");

	/**
	 * Maps item names to the locations of their icons.
	 */
	private static Map<String, Path> itemIconPaths;

	/**
	 * Maps item names to their icons, if they have been stored.
	 */
	private static Map<String, Icon> storedIcons = new HashMap<>();

	private Data() {}

	public static Set<Assembler> getAssemblers() {
		return new HashSet<>(assemblers);
	}

	public static long getFuelValue(final String fuel) {
		return fuels.getOrDefault(fuel, 0L);
	}

	/**
	 * <ul>
	 * <b><i>getItemIcon</i></b><br>
	 * <pre>public static {@link Icon} getItemIcon({@link String} item, boolean large)</pre> Gets the icon for the given item
	 * name, storing it if it has not already been created.
	 * @param item - The name of the item to find or create an icon for.
	 * @param large - Whether or not the icon should be large
	 * @return the icon for the given item
	 * @see {@link Recipe#LARGE_ICON_SIZE}, {@link Recipe#SMALL_ICON_SIZE}
	 *      </ul>
	 */
	public static Icon getItemIcon(final String item, final boolean large) {
		final int iconSize = large ? Recipe.LARGE_ICON_SIZE : Recipe.SMALL_ICON_SIZE;
		final String iconStr = item + (large ? "_*LARGE" : "");

		Icon ret = storedIcons.get(iconStr);
		if (ret != null) return ret;

		if (itemIconPaths.containsKey(item)) try {
			ret = new ImageIcon(Toolkit.getDefaultToolkit().getImage(itemIconPaths.get(item).toUri().toURL()).getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH));
			storedIcons.put(iconStr, ret);
			return ret;
		} catch (final Exception e) {}
		return new ImageIcon(new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB_PRE));
	}

	public static Set<Module> getModules() {
		return new HashSet<>(modules);
	}

	public static Set<Recipe> getRecipes() {
		return new HashSet<>(recipes);
	}

	public static SortedSet<Recipe> getRecipesSorted() {
		final SortedSet<Recipe> ret = new TreeSet<>((o1, o2) -> {
			final int ret1 = nameFor(o1).toLowerCase().compareTo(nameFor(o2).toLowerCase());
			if (ret1 == 0) return o1.name.compareTo(o2.name);
			return ret1;
		});
		ret.addAll(recipes);
		return ret;
	}

	/**
	 * <ul>
	 * <b><i>load</i></b><br>
	 * <pre>public static void load({@link Path} factorioDir, Path... mods) throws {@link IOException}</pre> Loads prototypes
	 * from the given directories.
	 * @param factorioDir - The
	 *        <a href= "https://wiki.factorio.com/index.php?title=Application_directory#Application_directory" >application
	 *        directory</a>
	 * @param mods - Mod directories, usually in the
	 *        <a href= "https://wiki.factorio.com/index.php?title=Application_directory#User_Data_directory" >user data
	 *        directory</a>. Zip files will be unzipped (TODO)
	 * @throws IOException if an {@code IOException} occurs while trying to load the prototypes.
	 *         </ul>
	 */
	public static void load(final Path factorioDir, final Path... mods) throws IOException {
		Main.loadingDialog.setText("Loading prototypes...");

		final LuaTable global = JsePlatform.standardGlobals();

		final Path core = factorioDir.resolve("data/core");

		global.get("package").set("path", core.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua");

		Files.walk(core.resolve("lualib")).forEach(path -> {
			if (Files.isDirectory(path)) return;

			try {
				global.get("dofile").call(LuaValue.valueOf(path.toFile().getAbsolutePath()));
			} catch (final LuaError e) {}
		});

		global.get("dofile").call(LuaValue.valueOf(core.resolve("data.lua").toFile().getAbsolutePath()));

		final Map<String, Path> modPaths = new HashMap<>();

		for (int m = -1; m < mods.length; m++) {
			final Path mod = m == -1 ? factorioDir.resolve("data/base") : mods[m];
			final String name = m == -1 ? "base" : mods[m].getFileName().toString().replaceAll("_\\d+\\.\\d+\\.\\d+$", "");
			Main.loadingDialog.setText("Loading prototypes for " + name + "...");
			modPaths.put(name, mod);

			global.get("package").set("path", mod.toFile().getAbsolutePath().replace("\\", "/") + "/?.lua;" + core.resolve("lualib").toFile().getAbsolutePath().replace("\\", "/") + "/?.lua");

			global.get("dofile").call(LuaValue.valueOf(mod.resolve("data.lua").toFile().getAbsolutePath()));

			Files.walk(mod.resolve("locale/en")).forEach(path -> {
				if (Files.isDirectory(path) || !path.getFileName().toString().endsWith(".cfg")) return;

				List<String> lines;
				try {
					lines = Files.readAllLines(path);
				} catch (final Exception e) {
					return;
				}

				boolean reading = false;
				for (final String line : lines) {
					if (line.isEmpty()) continue;

					final Matcher head = LOCALE_HEADER.matcher(line);
					if (head.find())
						reading = head.group(1).endsWith("-name") && !head.group(1).contains("category");
					else if (reading) {
						final Matcher entry = LOCALE_ENTRY.matcher(line);
						if (!entry.find()) continue;
						names.put(entry.group(1), entry.group(2));
					}
				}
			});
		}

		Main.loadingDialog.setText("Loading prototypes...");
		global.get("dofile").call(LuaValue.valueOf(Paths.get("resources/gather.lua").toFile().getAbsolutePath()));

		Main.loadingDialog.setText("Loading sprites...");
		itemIconPaths = new HashMap<>();
		LuaValue k = LuaValue.NIL;
		while (true) {
			final Varargs n = global.get("icons").next(k);
			if ((k = n.arg1()).isnil()) break;
			final LuaValue v = n.arg(2);

			itemIconPaths.put(k.checkjstring(), Paths.get(resolve(v.checkjstring(), modPaths)));
		}

		Main.loadingDialog.setDeterminate(global.get("totalLength").toint());

		final LuaValue recipes = global.get("recipes");
		int length = recipes.length();
		Main.loadingDialog.setText("Parsing recipes (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue recipe = recipes.get(i);

				final String name = recipe.get("name").checkjstring();
				String type;
				try {
					final LuaValue luaType = recipe.get("category");
					type = luaType == LuaValue.NIL ? "crafting" : luaType.checkjstring();
				} catch (final LuaError err) {
					type = "crafting";
				}
				final double time = recipe.get("energy_required").optdouble(0.5);

				final Map<String, Double> ingredients = new HashMap<>();
				final LuaValue standardizedIngredients = global.get("getIngredients").call(recipe.get("ingredients"));
				for (int n = 1; n <= standardizedIngredients.length(); n++) {
					final LuaValue ing = standardizedIngredients.get(n);
					ingredients.put(ing.get("name").checkjstring(), ing.get("amount").todouble());
				}

				try {
					final LuaValue luaResult = recipe.get("result");
					if (luaResult == LuaValue.NIL) throw new LuaError("");
					final String result = luaResult.checkjstring();

					final double resultCount = recipe.get("result_count").optdouble(1.0);

					LuaValue luaIcon = recipe.get("icon");
					if (luaIcon == LuaValue.NIL) luaIcon = global.get("icons").get(result);

					Image icon;
					try {
						final String iconPath = resolve(luaIcon.checkjstring(), modPaths);
						icon = Toolkit.getDefaultToolkit().getImage(Paths.get(iconPath).toUri().toURL());
					} catch (final Exception e) {
						icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
					}

					Data.recipes.add(new Recipe(name, type, time, ingredients, result, resultCount, icon));
				} catch (final LuaError err) {
					final Map<String, Double> results = new HashMap<>();
					final LuaValue standardizedResults = global.get("getIngredients").call(recipe.get("results"));
					for (int n = 1; n <= standardizedResults.length(); n++) {
						final LuaValue ing = standardizedResults.get(n);
						results.put(ing.get("name").checkjstring(), ing.get("amount").todouble());
					}

					LuaValue luaIcon = recipe.get("icon");
					if (luaIcon == LuaValue.NIL) luaIcon = global.get("icons").get(results.keySet().iterator().next());

					Image icon;
					try {
						final String iconPath = resolve(luaIcon.checkjstring(), modPaths);
						icon = Toolkit.getDefaultToolkit().getImage(Paths.get(iconPath).toUri().toURL());
					} catch (final Exception e) {
						icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
					}

					Data.recipes.add(new Recipe(name, type, time, ingredients, results, icon));
				}
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}
			Main.loadingDialog.setText("Parsing recipes (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue resources = global.get("resources");
		length = resources.length();
		Main.loadingDialog.setText("Parsing resources (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue resource = resources.get(i);

				final LuaValue infinite = resource.get("infinite");
				if (infinite != LuaValue.NIL && infinite.checkboolean()) continue;

				final String name = resource.get("name").checkjstring();
				String type;
				try {
					final LuaValue luaType = resource.get("category");
					type = luaType == LuaValue.NIL ? "basic-solid" : luaType.checkjstring();
				} catch (final LuaError err) {
					type = "basic-solid";
				}

				final double time = resource.get("minable").get("mining_time").todouble();

				final double hardness = resource.get("minable").get("hardness").todouble();

				String result;
				try {
					final LuaValue luaResult = resource.get("minable").get("result");
					if (luaResult == LuaValue.NIL) throw new LuaError("");
					result = luaResult.checkjstring();
				} catch (final LuaError err) {
					result = resource.get("results").get(0).get("name").checkjstring();
				}

				LuaValue luaIcon = resource.get("icon");
				if (luaIcon == LuaValue.NIL) luaIcon = global.get("icons").get(result);

				Image icon;
				try {
					final String iconPath = resolve(luaIcon.checkjstring(), modPaths);
					icon = Toolkit.getDefaultToolkit().getImage(Paths.get(iconPath).toUri().toURL());
				} catch (final Exception e) {
					icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
				}

				Data.recipes.add(new MiningRecipe(name, type, time, hardness, result, icon));
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing resources (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue assemblers = global.get("assemblers");
		length = assemblers.length();
		Main.loadingDialog.setText("Parsing assemblers (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue assembler = assemblers.get(i);

				final String name = assembler.get("name").checkjstring();

				final int ingredients = assembler.get("ingredient_count").optint(1);

				final boolean burner = assembler.get("energy_source").get("type").checkjstring().contains("burner");

				final double effectivity = assembler.get("energy_source").get("effectivity").optdouble(1);

				final double speed = assembler.get("crafting_speed").optdouble(1);

				final String nrg = assembler.get("energy_usage").checkjstring().replace("W", "").toLowerCase();
				final long energy = Long.parseLong(nrg.replaceAll("\\D+", "")) * (nrg.endsWith("k") ? 1000 : nrg.endsWith("m") ? 1000000 : nrg.endsWith("g") ? 1000000000 : 1);

				int modules;
				try {
					modules = assembler.get("module_specification").get("module_slots").optint(0);
				} catch (final LuaError e) {
					modules = 0;
				}

				final List<String> categories = new ArrayList<>();
				final LuaValue luaCats = assembler.get("crafting_categories");
				for (int l = 1; l <= luaCats.length(); l++)
					categories.add(luaCats.get(l).checkjstring());

				final List<String> effects = new ArrayList<>();
				final LuaValue luaEff = assembler.get("allowed_effects");
				if (luaEff != LuaValue.NIL) {
					for (int l = 1; l < luaEff.length(); l++)
						effects.add(luaEff.get(l).checkjstring());
				}

				Data.assemblers.add(new Assembler(name, ingredients, speed, energy, modules, burner, effectivity, categories, effects));
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing assemblers (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue drills = global.get("drills");
		length = drills.length();
		Main.loadingDialog.setText("Parsing mining drills (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue drill = drills.get(i);

				final String name = drill.get("name").checkjstring();

				final double power = drill.get("mining_power").todouble();

				final boolean burner = drill.get("energy_source").get("type").checkjstring().contains("burner");

				final double effectivity = drill.get("energy_source").get("effectivity").optdouble(1);

				final double speed = drill.get("mining_speed").optdouble(1);

				final String nrg = drill.get("energy_usage").checkjstring().replace("W", "").toLowerCase();
				final long energy = Long.parseLong(nrg.replaceAll("\\D+", "")) * (nrg.endsWith("k") ? 1000 : nrg.endsWith("m") ? 1000000 : nrg.endsWith("g") ? 1000000000 : 1);

				int modules;
				try {
					modules = drill.get("module_specification").get("module_slots").optint(0);
				} catch (final LuaError e) {
					modules = 0;
				}

				final List<String> categories = new ArrayList<>();
				final LuaValue luaCats = drill.get("resource_categories");
				for (int l = 1; l <= luaCats.length(); l++)
					categories.add("mining-" + luaCats.get(l).checkjstring());

				final List<String> effects = new ArrayList<>();
				final LuaValue luaEff = drill.get("allowed_effects");
				if (luaEff != LuaValue.NIL) {
					for (int l = 1; l < luaEff.length(); l++)
						effects.add(luaEff.get(l).checkjstring());
				}

				Data.assemblers.add(new MiningDrill(name, speed, power, energy, modules, burner, effectivity, categories, effects));
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing mining drills (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue pumps = global.get("pumps");
		length = pumps.length();
		Main.loadingDialog.setText("Parsing pumps (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue pump = pumps.get(i);

				final String name = pump.get("name").checkjstring();

				final double speed = pump.get("pumping_speed").todouble();

				Data.assemblers.add(new OffshorePump(name, speed));

				final String fluid = pump.get("fluid").checkjstring();

				Image icon;
				try {
					final String iconPath = resolve(global.get("icons").get(fluid).checkjstring(), modPaths);
					icon = Toolkit.getDefaultToolkit().getImage(Paths.get(iconPath).toUri().toURL());
				} catch (final Exception e) {
					icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
				}

				Data.recipes.add(new OffshoreRecipe(fluid, name, fluid, icon));
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing pumps (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue modules = global.get("modules");
		length = modules.length();
		Main.loadingDialog.setText("Parsing modules (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue module = modules.get(i);

				final String name = module.get("name").checkjstring();

				final Map<String, Double> effects = new HashMap<>();
				final LuaValue luaEffs = global.get("getEffects").call(module.get("effect"));
				for (int l = 1; l <= luaEffs.length(); l++) {
					final LuaValue effect = luaEffs.get(l);
					effects.put(effect.get("effect").checkjstring(), effect.get("amount").todouble());
				}

				final List<String> limitation = new ArrayList<>();
				final LuaValue luaLim = module.get("limitation");
				if (luaLim != LuaValue.NIL) {
					for (int l = 1; l < luaLim.length(); l++)
						limitation.add(luaLim.get(l).checkjstring());
				}

				Data.modules.add(new Module(name, effects, limitation.toArray(new String[limitation.size()])));
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing modules (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}

		final LuaValue fuels = global.get("fuel");
		length = fuels.length();
		Main.loadingDialog.setText("Parsing fuel (0/" + length + ")");
		for (int i = 1; i <= length; i++) {
			try {
				final LuaValue fuel = fuels.get(i);

				final String val = fuel.get("fuel_value").checkjstring().replace("J", "").toLowerCase();
				final long value = Long.parseLong(val.replaceAll("\\D+", "")) * (val.endsWith("k") ? 1000 : val.endsWith("m") ? 1000000 : val.endsWith("g") ? 1000000000 : 1);

				Data.fuels.put(fuel.get("name").checkjstring(), value);
			} catch (final LuaError e) {
				e.printStackTrace(System.err);
			}

			Main.loadingDialog.setText("Parsing fuel (" + i + "/" + length + ")");
			Main.loadingDialog.incrementProgress();
		}
	}

	public static String nameFor(final Recipe recipe) {
		final String ret = nameFor(recipe.name);
		if (ret == null) return nameFor(recipe.getResults().keySet().iterator().next());
		return ret;
	}

	/**
	 * <ul>
	 * <b><i>nameFor</i></b><br>
	 * <pre> String nameFor() </pre> description
	 * @param id
	 * @return
	 *         </ul>
	 */
	public static String nameFor(final String id) {
		return names.get(id);
	}

	/**
	 * <ul>
	 * <b><i>resolve</i></b><br>
	 * <pre>private static {@link String} resolve(String path, {@link Map}&lt;String, {@link Path}&gt; mods) </pre> Converts the
	 * given relative path to an absolute path, replacing mod path shortcuts (e.g. <code>__base__</code>) with the full path.
	 * @param path - The path as it is in the prototype definition in the lua
	 * @param mods - A {@code Map} mapping mod names to their paths.
	 * @return the resolved path
	 *         </ul>
	 */
	private static String resolve(final String path, final Map<String, Path> mods) {
		final Matcher m = MOD_PATH.matcher(path);

		if (m.find())
			return path.replace(m.group(), mods.get(m.group(1)).toFile().toString().replace('\\', '/'));
		else
			return path;
	}
}
