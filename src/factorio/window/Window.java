package factorio.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import factorio.calculator.Calculation;
import factorio.data.Recipe;
import factorio.window.treecell.CellRenderer;
import factorio.window.treecell.TotalHeader;

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
		full.setRootVisible(false);
		full.setCellRenderer(new CellRenderer());

		total = new JTree(new DefaultMutableTreeNode());
		total.setRootVisible(false);
		total.setCellRenderer(new CellRenderer());
		JScrollPane totalScroll = new JScrollPane(total);
		totalScroll.setColumnHeaderView(new TotalHeader("Totals", 3).getTreeCellRendererComponent(false, false));
		full_total = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(full), totalScroll);
		full_total.setDividerSize(7);
		full_total.setDividerLocation(Toolkit.getDefaultToolkit().getScreenSize().height / 2);

		in_out = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, full_total);
		in_out.setDividerSize(7);
		in_out.setDividerLocation(492);
		this.add(in_out);
		
		this.setJMenuBar(new MenuBar());
	}

}
