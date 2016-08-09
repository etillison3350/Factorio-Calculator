package factorio.window;

/**
 * A delegate interface to a {@link MenuBar}
 * @author ricky3350
 */
public interface MenuBarDelegate {

	/**
	 * <ul>
	 * <b><i>reset</i></b><br>
	 * <pre> public void reset()</pre> Handles a request from the {@link MenuBar} to reset the current state of the calculator
	 * to the default. This will be called when the {@code Reset} item's action is performed.
	 * </ul>
	 */
	public void reset();

	/**
	 * <ul>
	 * <b><i>save</i></b><br>
	 * <pre> public void save()</pre> Handles a request from the {@link MenuBar} to save the current input state of the
	 * calculator. This will be called when the {@code Save} item's action is performed.
	 * </ul>
	 */
	public void save();

	/**
	 * <ul>
	 * <b><i>open</i></b><br>
	 * <pre> public void open()</pre> Handles a request from the {@link MenuBar} to set the current input state (and output,
	 * accordingly) of the calculator to an external source. This will be called when the {@code Open} item's action is
	 * performed.
	 * </ul>
	 */
	public void open();

	/**
	 * <ul>
	 * <b><i>exit</i></b><br>
	 * <pre> public void exit()</pre> Handles a request from the {@link MenuBar} to terminate the program. This will be called
	 * when the {@code Exit} item's action is performed.
	 * </ul>
	 */
	public void exit();

	/**
	 * <ul>
	 * <b><i>changeMods</i></b><br>
	 * <pre> public void changeMods()</pre> Handles a request from the {@link MenuBar} to change the currently loaded mods, and
	 * core directory. This will be called when the {@code Mods} item's action is performed.
	 * </ul>
	 */
	public void changeMods();

	/**
	 * <ul>
	 * <b><i>changeDefaults</i></b><br>
	 * <pre> public void changeDefaults()</pre> Handles a request from the {@link MenuBar} to change the default production
	 * settings (e.g. assemblers, fuel, etc.). This will be called when the {@code Defaults} item's action is performed.
	 * </ul>
	 */
	public void changeDefaults();

}
