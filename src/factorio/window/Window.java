package factorio.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;

import factorio.calculator.Calculation;
import factorio.data.Data;
import factorio.data.Recipe;
import factorio.window.treecell.CellRenderer;

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

		inputPanel = new JPanel(new BorderLayout());
//		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

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
		inputPanel.add(search, BorderLayout.NORTH);

		inputList = new ProductList();
		JScrollPane inputScroll = new JScrollPane(inputList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		inputScroll.getVerticalScrollBar().setUnitIncrement(Recipe.ICON_SIZE);
		inputPanel.add(inputScroll, BorderLayout.CENTER);

		calculate = new JButton("Calculate");
		calculate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		inputPanel.add(calculate, BorderLayout.SOUTH);

//		inputPanel.add(Box.createVerticalGlue());

		full = new JTree();
		full.setRootVisible(false);
		full.setCellRenderer(new CellRenderer());
		Recipe[] recipes = Data.getRecipes().toArray(new Recipe[Data.getRecipes().size()]);
		Map<Recipe, Number> rates = new HashMap<>();
		for (int i = 0; i <= 16; i++) {
			rates.put(recipes[new Random().nextInt(recipes.length)], Math.random());
		}
		((DefaultTreeModel) full.getModel()).setRoot(new Calculation(rates).getAsTreeNode());
		for (int i = 0; i < full.getRowCount(); i++) {
			full.expandRow(i);
		}

		in_full = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, new JScrollPane(full));
		in_full.setDividerSize(7);
		in_full.setDividerLocation(485);
		this.add(in_full);
	}

}
