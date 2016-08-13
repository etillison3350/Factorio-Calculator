package factorio.window;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import factorio.Util;
import factorio.data.Data;
import factorio.data.Recipe;
import factorio.data.Technology;

/**
 * A non-instanitable manager for {@link Popup}s when the recipe {@link JLabel}s in {@link ProductListRow}s are hovered over.
 * @author etillison
 */
public class RecipePopupManager extends MouseAdapter {

	/**
	 * The image for time
	 */
	private static final ImageIcon TIME;

	/**
	 * An instance of {@code RecipePopupManager} to serve as a mouse listener
	 */
	private static final RecipePopupManager listener = new RecipePopupManager();

	private RecipePopupManager() {}

	/**
	 * A {@link Map} mapping {@link Component}s that are registered by the {@code RecipePopupManager} to the {@link Recipe}s
	 * that they correspond to
	 */
	private static final Map<Component, Recipe> registeredComponents = new HashMap<>();

	/**
	 * The {@link Popup} that is shown
	 */
	private static Popup popup;

	/**
	 * The {@link Component} that is currently being hovered over
	 */
	private static Component currentComponent;

	/**
	 * The position of the mouse pointer
	 */
	private static int x, y;

	/**
	 * The {@link Timer} that controls the showing of the {@link #popup}
	 */
	private static final Timer timer = new Timer(50, e -> {
		final JPanel panel = createRecipePanel(registeredComponents.get(currentComponent));
		panel.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				final Point converted = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), currentComponent);
				if (currentComponent != null && !currentComponent.contains(converted)) {
					listener.mouseExited(new MouseEvent(currentComponent, MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(), converted.x, converted.y, e.getClickCount(), e.isPopupTrigger()));
				}
			}
		});
		popup = PopupFactory.getSharedInstance().getPopup(currentComponent, panel, x, y);
		popup.show();
	});

	static {
		Image img;
		try {
			img = Toolkit.getDefaultToolkit().getImage(Paths.get("resources/clock-icon.png").toUri().toURL()).getScaledInstance(Recipe.LARGE_ICON_SIZE, Recipe.LARGE_ICON_SIZE, Image.SCALE_SMOOTH);
		} catch (final MalformedURLException e) {
			img = new BufferedImage(Recipe.LARGE_ICON_SIZE, Recipe.LARGE_ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
		}
		TIME = new ImageIcon(img);

		timer.setRepeats(false);
	}

	/**
	 * <ul>
	 * <b><i>registerComponent</i></b><br>
	 * <pre>public static void registerComponent({@link Component} c, {@link Recipe} r)</pre> Registers the given
	 * {@code Component} with the {@code RecipePopupManager} , correspoinding the the given {@code Recipe}
	 * @param c - The {@code Component} to register
	 * @param r - The {@code Recipe} that the {@link Component} corresponds to
	 *        </ul>
	 */
	public static void registerComponent(final Component c, final Recipe r) {
		if (c == null) throw new IllegalArgumentException("The component cannot be null");
		if (r == null) throw new IllegalArgumentException("The recipe cannot be null");
		registeredComponents.put(c, r);
		c.addMouseListener(listener);
		c.addMouseMotionListener(listener);
	}

	/**
	 * <ul>
	 * <b><i>unregisterComponent</i></b><br>
	 * <pre>public static void unregisterComponent({@link Component} c)</pre> Unregisters the {@code Component} from the
	 * {@code RecipePopupManager}
	 * @param c - the {@code Component} to unregister
	 *        </ul>
	 */
	public static void unregisterComponent(final Component c) {
		if (c == null) throw new IllegalArgumentException("The component cannot be null");
		registeredComponents.remove(c);
		c.removeMouseListener(listener);
		c.removeMouseMotionListener(listener);
	}

	/**
	 * <ul>
	 * <b><i>createRecipePanel</i></b><br>
	 * <pre>private static {@link JPanel} createRecipePanel({@link Recipe} r)</pre>
	 * @param r - The recipe to create a panel for
	 * @return the contents of the {@link #popup} for the given {@code Recipe}
	 *         </ul>
	 */
	private static JPanel createRecipePanel(final Recipe r) {
		if (r instanceof Technology) return createTechPanel((Technology) r);

		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		final boolean products = r.getResults().size() > 1 || !Data.nameFor(r).equals(Data.nameFor(r.getResults().keySet().iterator().next()));
		final double resultCount = r.getResults().values().iterator().next();

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(8, 8, 0, 8);
		c.weightx = 1;
		c.weighty = 0.5;

		panel.add(new JLabel((!products && r.getResults().values().iterator().next() != 1 ? Util.NUMBER_FORMAT.format(resultCount) + " \u00D7 " : "") + Data.nameFor(r)), c);

		if (products) {
			c.gridy++;

			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
			p.setBackground(new Color(0, 0, 0, 0));
			p.add(new JLabel("Products:")).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			for (final String result : r.getResults().keySet()) {
				p.add(new BorderLabel(Util.NUMBER_FORMAT.format(r.getResults().get(result)), Data.getItemIcon(result, true)));
			}

			panel.add(p, c);
		}

		c.gridy++;
		final JLabel time = new JLabel(Util.NUMBER_FORMAT.format(r.time), TIME, SwingConstants.LEADING);
		time.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		panel.add(time, c);

		for (final String ingredient : r.getIngredients().keySet()) {
			c.gridy++;
			final JLabel label = new JLabel(Util.NUMBER_FORMAT.format(r.getIngredients().get(ingredient)) + " \u00D7 " + Data.nameFor(ingredient), Data.getItemIcon(ingredient, true), SwingConstants.LEADING);
			label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			panel.add(label, c);
		}

		c.gridy++;
		panel.add(Box.createVerticalStrut(1), c);

		panel.setBackground(new Color(216, 216, 216));

		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		return panel;
	}

	/**
	 * <ul>
	 * <b><i>createTechPanel</i></b><br>
	 * <pre>private static {@link JPanel} createTechPanel({@link Technology} tech)</pre> A {@code Technology}-specific version
	 * of {@link #createRecipePanel(Recipe)}
	 * @param tech - the technology to create a panel for
	 * @return the contents of the {@link #popup} for the given {@code Technology}
	 *         </ul>
	 */
	private static JPanel createTechPanel(final Technology tech) {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(8, 8, 0, 8);
		c.weightx = 1;
		c.weighty = 0.5;
		panel.add(new JLabel(Data.nameFor(tech)), c);

		c.gridy++;
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
		p.setBackground(new Color(0, 0, 0, 0));
		p.add(new JLabel("Cost:")).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		p.add(new BorderLabel(Util.NUMBER_FORMAT.format(tech.time / tech.count), TIME));
		for (final String ing : tech.getIngredients().keySet()) {
			p.add(new BorderLabel(Util.NUMBER_FORMAT.format(tech.getIngredients().get(ing) / tech.count), Data.getItemIcon(ing, true)));
		}
		p.add(new JLabel("\u00D7 " + tech.count)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		panel.add(p, c);

		c.gridy++;
		panel.add(Box.createVerticalStrut(1), c);

		panel.setBackground(new Color(216, 216, 216));

		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		return panel;
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		if (popup == null) {
			if (!(e.getSource() instanceof Component) || !registeredComponents.containsKey(e.getSource())) return;
			currentComponent = e.getComponent();
			timer.restart();
		}
		x = e.getXOnScreen() + 1;
		y = e.getYOnScreen() + 1;
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		if (e.getComponent().contains(e.getPoint())) return;

		if (popup != null) {
			popup.hide();
			popup = null;
		}
		currentComponent = null;
		timer.stop();
	}
}
