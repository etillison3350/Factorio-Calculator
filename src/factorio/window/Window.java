package factorio.window;

import java.awt.Dimension;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import factorio.data.Data;
import factorio.data.Recipe;

public class Window extends JFrame {

	private static final long serialVersionUID = -377970844785993226L;

	private JPanel inputPanel;
	private JTree all, total;

	private JTextField search;
	private JList<Recipe> possibilities;
	private RecipeListCellRenderer renderer;

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
				String text = search.getText().trim().toLowerCase().replace('-', ' ');
				renderer.setSearchKey(text);
				if (text.isEmpty()) {
					possibilities.setListData(new Vector<>(Data.getRecipes()));
				}

				Vector<Recipe> exact = new Vector<>();
				Vector<Recipe> matchName = new Vector<>();
				Vector<Recipe> matchId = new Vector<>();
				Vector<Recipe> containName = new Vector<>();
				Vector<Recipe> containId = new Vector<>();

				Set<Recipe> recipes = Data.getRecipes();
				for (Recipe recipe : recipes) {
					if (Data.nameFor(recipe).equalsIgnoreCase(text) || recipe.name.replace('-', ' ').equalsIgnoreCase(text)) {
						exact.addElement(recipe);
					} else if (Data.nameFor(recipe).toLowerCase().startsWith(text)) {
						matchName.addElement(recipe);
					} else if (recipe.name.replace('-', ' ').toLowerCase().startsWith(text)) {
						matchId.addElement(recipe);
					} else if (Data.nameFor(recipe).toLowerCase().contains(text)) {
						containName.addElement(recipe);
					} else if (recipe.name.replace('-', ' ').contains(text)) {
						containId.addElement(recipe);
					}
				}

				exact.addAll(matchName);
				exact.addAll(matchId);
				exact.addAll(containName);
				exact.addAll(containId);

				possibilities.setListData(exact);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
		});
		search.setMaximumSize(new Dimension(search.getMaximumSize().width, search.getPreferredSize().height));
		inputPanel.add(search);

		possibilities = new JList<>(new Vector<>(Data.getRecipes()));
		renderer = new RecipeListCellRenderer();
		possibilities.setCellRenderer(renderer);
		inputPanel.add(new JScrollPane(possibilities));

		inputPanel.add(Box.createVerticalGlue());

		this.add(inputPanel);
	}

}
