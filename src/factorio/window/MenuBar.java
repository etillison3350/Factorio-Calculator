package factorio.window;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MenuBar extends JMenuBar implements ActionListener {

	private static final long serialVersionUID = 6631989072879466832L;

	private final MenuBarDelegate delegate;

	private final JMenu file;
	private final JMenuItem reset;
	private final JMenuItem open;
	private final JMenuItem save;
	private final JMenuItem exit;
	private final JMenu settings;
	private final JMenuItem mods;
	private final JMenuItem defaults;

	public MenuBar() {
		this(null);
	}

	public MenuBar(MenuBarDelegate delegate) {
		this.delegate = delegate;

		int control = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		this.add(file);

		reset = new JMenuItem("Reset");
		reset.setMnemonic(KeyEvent.VK_R);
		reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, control));
		reset.addActionListener(this);
		file.add(reset);

		open = new JMenuItem("Open");
		open.setMnemonic(KeyEvent.VK_O);
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, control));
		open.addActionListener(this);
		file.add(open);

		save = new JMenuItem("Save");
		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, control));
		save.addActionListener(this);
		file.add(save);

		exit = new JMenuItem("Exit");
		if (!System.getProperty("os.name").contains("Mac")) {
			file.addSeparator();

			exit.setMnemonic(KeyEvent.VK_E);
			exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
			exit.addActionListener(this);
			file.add(exit);
		}

		settings = new JMenu("Settings");
		settings.setMnemonic(KeyEvent.VK_S);
		this.add(settings);

		mods = new JMenuItem("Mods");
		mods.setMnemonic(KeyEvent.VK_M);
		mods.addActionListener(this);
		settings.add(mods);

		defaults = new JMenuItem("Defaults");
		defaults.setMnemonic(KeyEvent.VK_D);
		defaults.addActionListener(this);
		settings.add(defaults);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reset) {
			if (this.delegate != null) delegate.reset();
		} else if (e.getSource() == save) {
			if (this.delegate != null) delegate.save();
		} else if (e.getSource() == open) {
			if (this.delegate != null) delegate.open();
		} else if (e.getSource() == exit) {
			if (this.delegate != null) delegate.exit();
		} else if (e.getSource() == mods) {
			if (this.delegate != null) delegate.changeMods();
		} else if (e.getSource() == defaults) {
			if (this.delegate != null) delegate.changeDefaults();
		}
	}

}
