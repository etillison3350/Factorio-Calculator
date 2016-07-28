package factorio.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class Window extends JFrame {

	private static final long serialVersionUID = -377970844785993226L;

	private JSplitPane in_out, full_total;

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
		inputScroll.setBorder(BorderFactory.createEmptyBorder());
		inputPanel.add(inputScroll, BorderLayout.CENTER);

		calculate = new JButton("Calculate");
		calculate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<Recipe, Number> rates = inputList.getRates();

				Calculation calc = new Calculation(rates);

				((DefaultTreeModel) full.getModel()).setRoot(calc.getAsTreeNode());
				for (int i = 0; i < full.getRowCount(); i++) {
					full.expandRow(i);
				}

				((DefaultTreeModel) total.getModel()).setRoot(calc.getTotalTreeNode());
				for (int i = 0; i < total.getRowCount(); i++) {
					total.expandRow(i);
				}
			}
		});
		inputPanel.add(calculate, BorderLayout.SOUTH);

		full = new JTree(new DefaultMutableTreeNode());
		full.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
		full.getActionMap().put("copy", new AbstractAction() {

			private static final long serialVersionUID = 490257379525148970L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath[] selected = full.getSelectionPaths();
				if (selected == null || selected.length <= 0) return;

				Set<TreePath> min = Arrays.stream(selected).collect(HashSet::new, (set, path) -> {
					if (set.isEmpty()) {
						set.add(path);
					} else {
						if (path.getPathCount() < set.iterator().next().getPathCount()) set.clear();
						if (path.getPathCount() <= set.iterator().next().getPathCount()) set.add(path);
					}
				}, HashSet::addAll);

				boolean shouldIndent = true;

				outer: for (TreePath path : selected) {
					if (min.contains(path)) continue;
					for (TreePath p : min) {
						if (p.isDescendant(path)) continue outer;
					}

					shouldIndent = false;
					break;
				}

				String copy = "";
				for (TreePath path : selected) {
					if (!copy.isEmpty()) copy += "\n";
					if (shouldIndent) {
						for (int n = min.iterator().next().getPathCount(); n < path.getPathCount(); n++) {
							copy += "\t";
						}
					}
					if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
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
		full.setRootVisible(false);
		full.setShowsRootHandles(true);
		full.setCellRenderer(new CellRenderer());

		total = new JTree(new DefaultMutableTreeNode());
		total.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
		total.getActionMap().put("copy", new AbstractAction() {

			private static final long serialVersionUID = -3232571785198520875L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath[] selectedPaths = total.getSelectionPaths();
				if (selectedPaths == null || selectedPaths.length <= 0) return;

				Set<TreePath> selected = new TreeSet<>(Comparator.comparing(path -> total.getRowForPath(path)));
				Arrays.stream(selectedPaths).forEach(selected::add);
				Set<TreePath> min = new HashSet<>();

				DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) total.getModel()).getRoot();
				for (int c = 0; c < root.getChildCount(); c++) {
					TreePath newPath = new TreePath(new Object[] {root, root.getChildAt(c)});
					for (TreePath path : selectedPaths) {
						if (newPath.isDescendant(path)) {
							min.add(newPath);
							break;
						}
					}
				}
				
				boolean indentOne = min.size() > 1;
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

				outer: for (TreePath path : selected) {
					if (min.contains(path)) continue;
					for (TreePath p : min) {
						if (p.isDescendant(path)) continue outer;
					}

					shouldIndent = false;
					break;
				}
				
				String copy = "";
				for (TreePath path : selected) {
					if (!copy.isEmpty()) copy += "\n";
					
					String line = "";
					boolean isHeader = false;
					if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
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
		total.setRootVisible(false);
		total.setCellRenderer(new CellRenderer());
		
		JScrollPane fullScroll = new JScrollPane(full);
		full.setBorder(BorderFactory.createEmptyBorder());
		
		JScrollPane totalScroll = new JScrollPane(total);
		totalScroll.setColumnHeaderView(new TotalHeader("Totals", 3).getTreeCellRendererComponent(false, false));
		totalScroll.setBorder(BorderFactory.createEmptyBorder());
		
		full_total = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fullScroll, totalScroll);
		full_total.setDividerSize(7);
		full_total.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().height / 2);

		in_out = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, full_total);
		in_out.setDividerSize(7);
		in_out.setDividerLocation(492);
		this.add(in_out);

		this.setJMenuBar(new MenuBar());
	}
}
