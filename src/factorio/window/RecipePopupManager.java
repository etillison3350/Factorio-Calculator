package factorio.window;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import factorio.Util;
import factorio.data.Data;
import factorio.data.Recipe;

public class RecipePopupManager extends MouseAdapter {

	private static final RecipePopupManager listener = new RecipePopupManager();

	private RecipePopupManager() {}

	private static final Map<Component, Recipe> registeredComponents = new HashMap<>();

	private static final Timer timer = new Timer(50, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			popup = PopupFactory.getSharedInstance().getPopup(currentComponent, createRecipePanel(registeredComponents.get(currentComponent)), x, y);
			popup.show();
		}
	});
	private static Popup popup;

	private static Component currentComponent;
	private static int x, y;

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
		c.insets = new Insets(5, 10, 5, 10);
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

		for (String ingredient : r.getIngredients().keySet()) {
			c.gridy++;
			JLabel label = new JLabel(Util.NUMBER_FORMAT.format(r.getIngredients().get(ingredient)) + " \u00D7 " + Data.nameFor(ingredient), Data.getItemIcon(ingredient, true), SwingConstants.LEADING);
			label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			panel.add(label, c);
		}

		panel.setBackground(new Color(216, 216, 216));
		
		return panel;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!(e.getSource() instanceof Component) || !registeredComponents.containsKey(e.getSource())) return;
		currentComponent = e.getComponent();
		timer.setRepeats(false);

		timer.restart();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (popup == null) timer.restart();
		x = e.getXOnScreen() + 1;
		y = e.getYOnScreen() + 1;
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
		currentComponent = null;
		timer.stop();
	}

}
