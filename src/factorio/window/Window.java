package factorio.window;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import factorio.data.Recipe;

public class Window extends JFrame {

	private static final long serialVersionUID = -377970844785993226L;

	private JSplitPane in_full, full_total;

	private JPanel inputPanel;
	private JTree full, total;

	private JTextField search;
	private ProductList inputList;
	private JButton calculate;

	public Window() {
		super("Factorio Calculator");
		this.setSize(1024, 768);
		this.setExtendedState(Window.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

		search = new JTextField();
		search.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			private void update() {
				inputList.setSearchKey(search.getText().trim().toLowerCase().replace('-', ' '));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
		});
		search.setMaximumSize(new Dimension(search.getMaximumSize().width, search.getPreferredSize().height));
		inputPanel.add(search);

		inputList = new ProductList();
		((JScrollPane) inputPanel.add(new JScrollPane(inputList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER))).getVerticalScrollBar().setUnitIncrement(Recipe.ICON_SIZE);

		calculate = new JButton("Calculate");
		calculate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		inputPanel.add(calculate);

		inputPanel.add(Box.createVerticalGlue());

		full = new JTree();

		in_full = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, full);
		this.add(in_full);
	}

}
