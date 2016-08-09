package factorio.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import factorio.calculator.Calculation;
import factorio.data.Recipe;
import factorio.window.treecell.CellRenderer;
import factorio.window.treecell.TotalHeader;
import factorio.window.treecell.TreeCell;

/**
 * The main {@link JFrame}
 * @author ricky3350
 */
public class Window extends JFrame {

	private static final long serialVersionUID = -377970844785993226L;

	/**
	 * A {@link JSplitPane} dividing the input and ouput sides of the window
	 */
	private final JSplitPane in_out;

	/**
	 * A {@link JSplitPane} dividing the {@link #full} panel from the
	 * {@link #total} panel
	 */
	private final JSplitPane full_total;

	/**
	 * The input panel
	 */
	private final JPanel inputPanel;

	/**
	 * The tree that displays each ingredient separately
	 * @see {@link Calculation#getAsTreeNode()}
	 */
	private final JTree full;

	/**
	 * The tree that displays the sums of the calculation
	 * @see {@link Calculation#getTotalTreeNode()}
	 */
	private final JTree total;

	/**
	 * A search bar
	 */
	private final JTextField search;

	/**
	 * The product list
	 */
	private final ProductList inputList;

	/**
	 * The button that executes the {@link Calculation}
	 */
	private final JButton calculate;

	public Window() {
		super("Factorio Calculator");
		this.setSize(1024, 768);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.inputPanel = new JPanel(new BorderLayout());

		this.search = new JTextField();
		this.search.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				this.update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				this.update();
			}

			private void update() {
				Window.this.inputList.setSearchKey(Window.this.search.getText().trim().toLowerCase().replace('-', ' '));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
		});
		this.search.setMaximumSize(new Dimension(this.search.getMaximumSize().width, this.search.getPreferredSize().height));
		this.inputPanel.add(this.search, BorderLayout.NORTH);

		this.inputList = new ProductList();
		final JScrollPane inputScroll = new JScrollPane(this.inputList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		inputScroll.getVerticalScrollBar().setUnitIncrement(Recipe.LARGE_ICON_SIZE);
		inputScroll.setBorder(BorderFactory.createEmptyBorder());
		this.inputPanel.add(inputScroll, BorderLayout.CENTER);

		this.calculate = new JButton("Calculate");
		this.calculate.addActionListener(e -> {
			final Map<Recipe, Number> rates = Window.this.inputList.getRates();

			final Calculation calc = new Calculation(rates);

			((DefaultTreeModel) Window.this.full.getModel()).setRoot(calc.getAsTreeNode());
			for (int i1 = 0; i1 < Window.this.full.getRowCount(); i1++) {
				Window.this.full.expandRow(i1);
			}

			((DefaultTreeModel) Window.this.total.getModel()).setRoot(calc.getTotalTreeNode());
			for (int i2 = 0; i2 < Window.this.total.getRowCount(); i2++) {
				Window.this.total.expandRow(i2);
			}
		});
		this.inputPanel.add(this.calculate, BorderLayout.SOUTH);

		this.full = new JTree(new DefaultMutableTreeNode());
		this.full.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
		this.full.getActionMap().put("copy", new AbstractAction() {

			private static final long serialVersionUID = 490257379525148970L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final TreePath[] selected = Window.this.full.getSelectionPaths();
				if (selected == null || selected.length <= 0) return;

				final Set<TreePath> min = Arrays.stream(selected).collect(HashSet::new, (set, path) -> {
					if (set.isEmpty()) {
						set.add(path);
					} else {
						if (path.getPathCount() < set.iterator().next().getPathCount()) set.clear();
						if (path.getPathCount() <= set.iterator().next().getPathCount()) set.add(path);
					}
				}, HashSet::addAll);

				boolean shouldIndent = true;

				outer: for (final TreePath path : selected) {
					if (min.contains(path)) continue;
					for (final TreePath p : min) {
						if (p.isDescendant(path)) continue outer;
					}

					shouldIndent = false;
					break;
				}

				String copy = "";
				for (final TreePath path : selected) {
					if (!copy.isEmpty()) copy += "\n";
					if (shouldIndent) {
						for (int n = min.iterator().next().getPathCount(); n < path.getPathCount(); n++) {
							copy += "\t";
						}
					}
					if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (node.getUserObject() instanceof TreeCell) {
							copy += ((TreeCell) node.getUserObject()).getRawString();
						} else {
							copy += node.getUserObject().toString();
						}
					} else {
						copy += path.getLastPathComponent().toString();
					}
				}

				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copy), null);
			}
		});
		this.full.setRootVisible(false);
		this.full.setShowsRootHandles(true);
		this.full.setCellRenderer(new CellRenderer());

		this.total = new JTree(new DefaultMutableTreeNode());
		this.total.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
		this.total.getActionMap().put("copy", new AbstractAction() {

			private static final long serialVersionUID = -3232571785198520875L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final TreePath[] selectedPaths = Window.this.total.getSelectionPaths();
				if (selectedPaths == null || selectedPaths.length <= 0) return;

				final Set<TreePath> selected = new TreeSet<>(Comparator.comparing(path -> Window.this.total.getRowForPath(path)));
				Arrays.stream(selectedPaths).forEach(selected::add);
				final Set<TreePath> min = new HashSet<>();

				final DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) Window.this.total.getModel()).getRoot();
				for (int c = 0; c < root.getChildCount(); c++) {
					final TreePath newPath = new TreePath(new Object[] {root, root.getChildAt(c)});
					for (final TreePath path : selectedPaths) {
						if (newPath.isDescendant(path)) {
							min.add(newPath);
							break;
						}
					}
				}

				final boolean indentOne = min.size() > 1;
				if (indentOne) {
					selected.addAll(min);
				} else {
					selected.forEach(path -> {
						if (min.isEmpty()) {
							min.add(path);
						} else {
							if (path.getPathCount() < min.iterator().next().getPathCount()) min.clear();
							if (path.getPathCount() <= min.iterator().next().getPathCount()) min.add(path);
						}
					});
				}

				boolean shouldIndent = true;

				outer: for (final TreePath path : selected) {
					if (min.contains(path)) continue;
					for (final TreePath p : min) {
						if (p.isDescendant(path)) continue outer;
					}

					shouldIndent = false;
					break;
				}

				String copy = "";
				for (final TreePath path : selected) {
					if (!copy.isEmpty()) copy += "\n";

					String line = "";
					boolean isHeader = false;
					if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
						final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						if (node.getUserObject() instanceof TreeCell) {
							isHeader = node.getUserObject() instanceof TotalHeader;
							line += ((TreeCell) node.getUserObject()).getRawString();
						} else {
							line += node.getUserObject().toString();
						}
					} else {
						line += path.getLastPathComponent().toString();
					}

					if (shouldIndent) {
						for (int n = min.iterator().next().getPathCount(); n < path.getPathCount() + (indentOne && !isHeader ? 1 : 0); n++) {
							line = "\t" + line;
						}
					}

					copy += line;
				}

				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copy), null);
			}
		});
		this.total.setRootVisible(false);
		this.total.setCellRenderer(new CellRenderer());

		final JScrollPane fullScroll = new JScrollPane(this.full);
		this.full.setBorder(BorderFactory.createEmptyBorder());

		final JScrollPane totalScroll = new JScrollPane(this.total);
		totalScroll.setColumnHeaderView(new TotalHeader("Totals", 3).getTreeCellRendererComponent(false, false));
		totalScroll.setBorder(BorderFactory.createEmptyBorder());

		this.full_total = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fullScroll, totalScroll);
		this.full_total.setDividerSize(7);
		this.full_total.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().height / 2);

		this.in_out = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.inputPanel, this.full_total);
		this.in_out.setDividerSize(7);
		this.in_out.setDividerLocation(492);
		this.add(this.in_out);

		this.setJMenuBar(new MenuBar());
	}
}
