package factorio.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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

	public final Recipe product;

	private final JLabel label;
	private final JTextField text;
	private final JComboBox<String> options;
	private final JButton configure;

	private AssemblerSettings assemblerSettings;

	public ProductListRow(Recipe product) {
		super(new BorderLayout());

		this.product = product;

		this.label = new JLabel(Data.nameFor(product));
		this.label.setIcon(product.getIcon());

		this.add(this.label, BorderLayout.LINE_START);

		JPanel right = new JPanel(new GridBagLayout());

		GridBagConstraints r = new GridBagConstraints();

		text = new JTextField(8);
		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				try {
					if (!text.getText().isEmpty()) Evaluator.evaluate(text.getText());
					text.setBackground(Color.WHITE);
				} catch (IllegalArgumentException exception) {
					text.setBackground(new Color(255, 192, 192));
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
		r.weightx = 1;
		r.weighty = 1;
		right.add(text, r);

		this.options = new JComboBox<>(new String[] {"items per second", "max capacity assembers"});
		options.addItemListener(new ItemListener() {
			
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() != ItemEvent.SELECTED) return;
				
				configure.setEnabled(options.getSelectedIndex() == 1);
			}
		});
		options.setFont(this.options.getFont().deriveFont(Font.PLAIN));

		r.gridx = 1;
		r.weightx = 0;
		right.add(options, r);

		this.configure = new JButton("Configure...");
		this.configure.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: Add action listener
			}
		});
		this.configure.setFont(this.configure.getFont().deriveFont(Font.PLAIN));
		this.configure.setEnabled(false);
		
		r.gridx = 2;
		right.add(configure, r);

		right.setOpaque(false);
		
		this.add(right, BorderLayout.LINE_END);

		this.add(Box.createHorizontalGlue());
	}

}
