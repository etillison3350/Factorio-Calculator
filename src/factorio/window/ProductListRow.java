package factorio.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;

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

/**
 * A row in a {@link ProductList} corresponding to a single recipe
 * @author ricky3350
 */
public class ProductListRow extends JPanel {

	private static final long serialVersionUID = -5835567095011127426L;

	/**
	 * The recipe coresponding to this row
	 */
	public final Recipe recipe;

	/**
	 * The label of the recipe, containing the name and the icon
	 */
	private final JLabel label;

	/**
	 * The text field for the rate
	 */
	private final JTextField text;

	/**
	 * The different options for what the number in {@link #text} means
	 */
	private final JComboBox<String> options;

	/**
	 * A button to open a dialog to configure the assembler if the max cap. assembler option in {@link #options} is selected.
	 */
	private final JButton configure;

	/**
	 * The {@link AssemblerSettings} that are configured with {@link #configure}
	 */
	private AssemblerSettings assemblerSettings;

	/**
	 * The current numerical value of {@link #text}
	 */
	private double value = Double.NaN;

	public ProductListRow(final Recipe recipe) {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 0));

		this.recipe = recipe;
		this.assemblerSettings = AssemblerSettings.getDefaultSettings(recipe);

		this.label = new JLabel(Data.nameFor(recipe));
		this.label.setIcon(recipe.getIcon());

		this.add(this.label);

		RecipePopupManager.registerComponent(this.label, recipe);

		final JPanel right = new JPanel(new GridBagLayout());

		final GridBagConstraints r = new GridBagConstraints();

		this.text = new JTextField(8);
		this.text.setHorizontalAlignment(SwingConstants.TRAILING);
		this.text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(final FocusEvent e) {
				try {
					if (!ProductListRow.this.text.getText().isEmpty()) {
						ProductListRow.this.value = Evaluator.evaluate(ProductListRow.this.text.getText());
					} else {
						ProductListRow.this.value = Double.NaN;
					}
					ProductListRow.this.text.setBackground(Color.WHITE);
				} catch (final IllegalArgumentException exception) {
					ProductListRow.this.text.setBackground(new Color(255, 192, 192));
					ProductListRow.this.value = Double.NaN;
				}
			}

		});
		final PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter() {

			@Override
			public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr) throws BadLocationException {
				fb.insertString(offset, string.replaceAll("[^\\d\\.\\-\\+\\*\\/\\(\\)]+", ""), attr);
			}

			@Override
			public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException {
				fb.replace(offset, length, text.replaceAll("[^\\d\\.\\-\\+\\*\\/\\(\\)]+", ""), attrs);
			}

		});
		this.text.setDocument(doc);

		r.fill = GridBagConstraints.HORIZONTAL;
		r.gridx = 0;
		r.gridy = 0;
		r.insets = new Insets(0, 1, 0, 0);
		r.weightx = 1;
		r.weighty = 1;
		right.add(this.text, r);

		this.options = new JComboBox<>(this.getOptions());
		this.options.addItemListener(e -> {
			if (e.getStateChange() != ItemEvent.SELECTED) return;

			ProductListRow.this.configure.setEnabled(ProductListRow.this.options.getSelectedIndex() == this.getOptions().length);
		});
		this.options.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		r.gridx = 1;
		r.weightx = 0;
		right.add(this.options, r);

		this.configure = new JButton(new ImageIcon("resources\\gear.png"));
		this.configure.setMargin(new Insets(1, 1, 1, 1));
		this.configure.addActionListener(e -> {
			if (e.getSource() == this.configure) {
				final AssemblerSettings s = this.configure();
				if (s != null) this.assemblerSettings = s;
			}
		});
		this.configure.setEnabled(false);

		r.gridx = 2;
		right.add(this.configure, r);

		right.setOpaque(false);

		this.add(right, BorderLayout.LINE_END);
	}

	/**
	 * <ul>
	 * <b><i>getOptions</i></b><br>
	 * <pre>private {@link String}[] getOptions({@link Recipe} recipe)</pre>
	 * @return the options for the options combo box.
	 *         </ul>
	 */
	protected String[] getOptions() {
		if (this.recipe.getResults().size() == 1 && Math.abs(this.recipe.getResults().values().iterator().next() - 1) < 0.0001) {
			return new String[] {"items per second", "max cap. assembers"};
		} else {
			return new String[] {"items per second", "cycles per second", "max cap. assembers"};
		}
	}

	/**
	 * <ul>
	 * <b><i>getSelectedOption</i></b><br>
	 * <pre>public final {@link String} getSelectedOption()</pre>
	 * @return the text of the selected option in the combo box.
	 *         </ul>
	 */
	public final String getSelectedOption() {
		return this.options.getSelectedItem().toString();
	}

	/**
	 * <ul>
	 * <b><i>getRate</i></b><br>
	 * <pre>public double getRate()</pre>
	 * @return The number of recipe cycles per second the user has specified in this <code>ProductListRow</code>'s text field.
	 *         </ul>
	 */
	public double getRate() {
		if (Double.isNaN(this.value)) return 0;

		switch (this.getSelectedOption()) {
			case "cycles per second":
				return this.value;
			case "max cap. assembers":
				return 1 / (this.value * this.assemblerSettings.getSpeed() * this.recipe.time);
			default:
				return this.value / this.recipe.getResults().values().stream().mapToDouble(f -> (double) f).sum();
		}
	}

	protected final AssemblerSettings getAssemblerSettings() {
		return this.assemblerSettings;
	}

	protected final double getValue() {
		return this.value;
	}

	/**
	 * <ul>
	 * <b><i>configure</i></b><br>
	 * <pre>public {@link AssemblerSettings} configure()</pre> Called when the current assembler needs to be configured
	 * @return the new settings from the configuration, or <code>null</code> of it was cancelled.
	 *         </ul>
	 */
	public AssemblerSettings configure() {
		// TODO
		return null;
	}

}
