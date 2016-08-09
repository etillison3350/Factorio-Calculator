package factorio.window;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * The menu bar at the top of the main window.
 * @author ricky3350
 */
public class MenuBar extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = 6631989072879466832L;

	/**
	 * The delegate ot the menu bar.
	 */
	private final MenuBarDelegate delegate;

	/**
	 * The file menu
	 */
	private final JMenu file;

	/**
	 * The reset menu item
	 */
	private final JMenuItem reset;

	/**
	 * The open menu item
	 */
	private final JMenuItem open;

	/**
	 * The save menu item
	 */
	private final JMenuItem save;

	/**
	 * The exit menu item
	 */
	private final JMenuItem exit;

	/**
	 * The settings menu
	 */
	private final JMenu settings;

	/**
	 * The mods menu item
	 */
	private final JMenuItem mods;

	/**
	 * The defaults menu item
	 */
	private final JMenuItem defaults;

	/**
	 * Creates a new menu bar with no delegate
	 */
	public MenuBar() {
		this(null);
	}

	public MenuBar(MenuBarDelegate delegate) {
		this.delegate = delegate;

		final int control = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		this.file = new JMenu("File");
		this.file.setMnemonic(KeyEvent.VK_F);
		this.add(this.file);

		this.reset = new JMenuItem("Reset");
		this.reset.setMnemonic(KeyEvent.VK_R);
		this.reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, control));
		this.reset.addActionListener(this);
		this.file.add(this.reset);

		this.open = new JMenuItem("Open");
		this.open.setMnemonic(KeyEvent.VK_O);
		this.open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, control));
		this.open.addActionListener(this);
		this.file.add(this.open);

		this.save = new JMenuItem("Save");
		this.save.setMnemonic(KeyEvent.VK_S);
		this.save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, control));
		this.save.addActionListener(this);
		this.file.add(this.save);

		this.exit = new JMenuItem("Exit");
		if (!System.getProperty("os.name").contains("Mac")) {
			this.file.addSeparator();

			this.exit.setMnemonic(KeyEvent.VK_E);
			this.exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
			this.exit.addActionListener(this);
			this.file.add(this.exit);
		}

		this.settings = new JMenu("Settings");
		this.settings.setMnemonic(KeyEvent.VK_S);
		this.add(this.settings);

		this.mods = new JMenuItem("Mods");
		this.mods.setMnemonic(KeyEvent.VK_M);
		this.mods.addActionListener(this);
		this.settings.add(this.mods);

		this.defaults = new JMenuItem("Defaults");
		this.defaults.setMnemonic(KeyEvent.VK_D);
		this.defaults.addActionListener(this);
		this.settings.add(this.defaults);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.reset) {
			if (this.delegate != null) this.delegate.reset();
		} else if (e.getSource() == this.save) {
			if (this.delegate != null) this.delegate.save();
		} else if (e.getSource() == this.open) {
			if (this.delegate != null) this.delegate.open();
		} else if (e.getSource() == this.exit) {
			if (this.delegate != null) this.delegate.exit();
		} else if (e.getSource() == this.mods) {
			if (this.delegate != null) this.delegate.changeMods();
		} else if (e.getSource() == this.defaults) {
			if (this.delegate != null) this.delegate.changeDefaults();
		}
	}

}
