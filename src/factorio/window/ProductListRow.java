package factorio.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import factorio.calculator.AssemblerSettings;
import factorio.calculator.Evaluator;
import factorio.data.Data;
import factorio.data.Recipe;

public class ProductListRow extends JPanel {

	private static final long serialVersionUID = -5835567095011127426L;

	public final Recipe recipe;

	private final JLabel label;
	private final JTextField text;
	private final JComboBox<String> options;
	private final JButton configure;

	private AssemblerSettings assemblerSettings;

	private float value = 0;

	public ProductListRow(Recipe recipe) {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 0));

		this.recipe = recipe;
		this.assemblerSettings = AssemblerSettings.getDefaultSettings(recipe);

		this.label = new JLabel(Data.nameFor(recipe));
		this.label.setIcon(recipe.getIcon());

		this.add(this.label);
		
		RecipePopupManager.registerComponent(this.label, recipe);

		JPanel right = new JPanel(new GridBagLayout());

		GridBagConstraints r = new GridBagConstraints();

		text = new JTextField(8);
		text.setHorizontalAlignment(SwingConstants.TRAILING);
		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				try {
					if (!text.getText().isEmpty()) {
						value = (float) Evaluator.evaluate(text.getText());
					} else {
						value = 0;
					}
					text.setBackground(Color.WHITE);
				} catch (IllegalArgumentException exception) {
					text.setBackground(new Color(255, 192, 192));
					value = 0;
				}
			}

		});
		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {

			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				fb.insertString(offset, string.replaceAll("[^\\d\\.\\-\\+\\*\\/]+", ""), attr);
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				fb.replace(offset, length, text.replaceAll("[^\\d\\.\\-\\+\\*\\/]+", ""), attrs);
			}

		});
		text.setDocument(doc);

		r.fill = GridBagConstraints.HORIZONTAL;
		r.gridx = 0;
		r.gridy = 0;
		r.insets = new Insets(0, 1, 0, 0);
		r.weightx = 1;
		r.weighty = 1;
		right.add(text, r);

		String[] optionArray = recipe.getResults().size() == 1 && Math.abs(recipe.getResults().values().iterator().next() - 1) < 0.0001 ? new String[] {"items per second", "max cap. assembers"} : new String[] {"items per second", "cycles per second", "max cap. assembers"};
		this.options = new JComboBox<>(optionArray);
		options.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) return;

				configure.setEnabled(options.getSelectedIndex() == optionArray.length);
			}
		});
		options.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		r.gridx = 1;
		r.weightx = 0;
		right.add(options, r);

		this.configure = new JButton(new ImageIcon("resources\\gear.png"));
		this.configure.setMargin(new Insets(1, 1, 1, 1));
		this.configure.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: Add action listener
			}
		});
		this.configure.setEnabled(false);

		r.gridx = 2;
		right.add(configure, r);

		right.setOpaque(false);

		this.add(right, BorderLayout.LINE_END);
	}

	/**
	 * <ul>
	 * <b><i>getRate</i></b><br>
	 * <br>
	 * <code>&nbsp;public float getRate()</code><br>
	 * <br>
	 * @return The number of recipe cycles per second the user has specified in this <code>ProductListRow</code>'s text field.
	 * </ul>
	 */
	public double getRate() {
		switch (options.getSelectedItem().toString()) {
			case "cycles per second":
				return value;
			case "max cap. assembers":
				return 1 / (value * assemblerSettings.getSpeed() * recipe.time);
			default:
				return value / recipe.getResults().values().stream().mapToDouble(f -> (double) f).sum();
		}
	}

}
