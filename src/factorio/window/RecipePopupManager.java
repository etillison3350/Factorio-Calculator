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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class RecipePopupManager extends MouseAdapter {

	private static final ImageIcon TIME;

	private static final RecipePopupManager listener = new RecipePopupManager();

	private RecipePopupManager() {}

	private static final Map<Component, Recipe> registeredComponents = new HashMap<>();

	private static final Timer timer = new Timer(50, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			JPanel panel = createRecipePanel(registeredComponents.get(currentComponent));
			panel.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseMoved(MouseEvent e) {
					Point converted = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), currentComponent);
					if (currentComponent != null && !currentComponent.contains(converted)) {
						listener.mouseExited(new MouseEvent(currentComponent, MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(), converted.x, converted.y, e.getClickCount(), e.isPopupTrigger()));
					}
				}
			});
			popup = PopupFactory.getSharedInstance().getPopup(currentComponent, panel, x, y);
			popup.show();
		}
	});
	private static Popup popup;

	private static Component currentComponent;
	private static int x, y;

	static {
		Image img;
		try {
			img = Toolkit.getDefaultToolkit().getImage(Paths.get("resources/clock-icon.png").toUri().toURL()).getScaledInstance(Recipe.ICON_SIZE, Recipe.ICON_SIZE, Image.SCALE_SMOOTH);
		} catch (MalformedURLException e) {
			img = new BufferedImage(Recipe.ICON_SIZE, Recipe.ICON_SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
		}
		TIME = new ImageIcon(img);

		timer.setRepeats(false);
	}

	public static void registerComponent(Component c, Recipe r) {
		registeredComponents.put(c, r);
		c.addMouseListener(listener);
		c.addMouseMotionListener(listener);
	}

	public static void unregisterComponent(Component c) {
		registeredComponents.remove(c);
		c.removeMouseListener(listener);
		c.addMouseMotionListener(listener);
	}

	private static JPanel createRecipePanel(Recipe r) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		boolean products = r.getResults().size() > 1 || !Data.nameFor(r).equals(Data.nameFor(r.getResults().keySet().iterator().next()));
		float resultCount = r.getResults().values().iterator().next();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(8, 8, 0, 8);
		c.weightx = 1;
		c.weighty = 0.5;

		panel.add(new JLabel((!products && r.getResults().values().iterator().next() != 1 ? Util.NUMBER_FORMAT.format(resultCount) + " \u00D7 " : "") + Data.nameFor(r)), c);

		if (products) {
			c.gridy++;

			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
			p.setBackground(new Color(0, 0, 0, 0));
			p.add(new JLabel("Products:")).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			for (String result : r.getResults().keySet()) {
				p.add(new ShadowLabel(Util.NUMBER_FORMAT.format(r.getResults().get(result)), Data.getItemIcon(result, true)));
			}

			panel.add(p, c);
		}

		c.gridy++;
		JLabel time = new JLabel(Util.NUMBER_FORMAT.format(r.time), TIME, SwingConstants.LEADING);
		time.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		panel.add(time, c);

		for (String ingredient : r.getIngredients().keySet()) {
			c.gridy++;
			JLabel label = new JLabel(Util.NUMBER_FORMAT.format(r.getIngredients().get(ingredient)) + " \u00D7 " + Data.nameFor(ingredient), Data.getItemIcon(ingredient, true), SwingConstants.LEADING);
			label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			panel.add(label, c);
		}

		c.gridy++;
		panel.add(Box.createVerticalStrut(1), c);

		panel.setBackground(new Color(216, 216, 216));

		panel.setBorder(BorderFactory.createRaisedBevelBorder());

		return panel;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (popup == null) {
			if (!(e.getSource() instanceof Component) || !registeredComponents.containsKey(e.getSource())) return;
			currentComponent = e.getComponent();
			timer.restart();
		}
		x = e.getXOnScreen() + 1;
		y = e.getYOnScreen() + 1;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getComponent().contains(e.getPoint())) return;

		if (popup != null) {
			popup.hide();
			popup = null;
		}
		currentComponent = null;
		timer.stop();
	}

}
