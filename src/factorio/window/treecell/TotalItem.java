package factorio.window.treecell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import factorio.Util;
import factorio.calculator.AssemblerSettings;
import factorio.data.Data;
import factorio.data.Recipe;

public class TotalItem implements TreeCell, Comparable<TotalItem> {

	private String item;
	private Recipe recipe;
	private AssemblerSettings assembler;

	private float itemRate, recipeRate, assemblerCount;

	private Set<TotalItem> children = new HashSet<>();

	public TotalItem(String item, Recipe recipe, AssemblerSettings assembler) {
		this.item = item;
		this.recipe = recipe;
		this.assembler = assembler;
	}

	public String getItem() {
		return item;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public AssemblerSettings getAssembler() {
		return assembler;
	}

	public void add(float itemRate, float recipeRate, Recipe recipe, float assemblerCount, AssemblerSettings assembler) {
		this.itemRate += itemRate;

		if (this.recipe == null && this.item != null) {
			for (TotalItem ti : children) {
				if (ti.recipe == recipe) {
					ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
					return;
				}
			}
			if (recipe != null && assembler != null) {
				TotalItem b = new TotalItem(null, recipe, assembler);
				b.recipeRate = recipeRate;
				b.assemblerCount = assemblerCount;
				this.children.add(b);
			}
		} else if (this.recipe == recipe || this.recipe == null) {
			if (this.recipe != null) this.recipeRate += recipeRate;

			if (this.assembler == null) {
				for (TotalItem ti : children) {
					if (ti.assembler.equals(assembler)) {
						ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
						return;
					}
				}
				if (assembler != null) {
					TotalItem b = new TotalItem(null, null, assembler);
					b.assemblerCount = assemblerCount;
					this.children.add(b);
				}
			} else if (this.assembler.equals(assembler)) {
				this.assemblerCount += assemblerCount;
			} else {
				TotalItem a = new TotalItem(null, null, this.assembler);
				a.assemblerCount = this.assemblerCount;
				this.children.add(a);

				if (assembler != null) {
					TotalItem b = new TotalItem(null, null, assembler);
					b.assemblerCount = assemblerCount;
					this.children.add(b);
				}

				this.assembler = null;
			}
		} else {
			TotalItem a = new TotalItem(null, this.recipe, this.assembler);
			a.recipeRate = this.recipeRate;
			a.assemblerCount = this.assemblerCount;
			this.children.add(a);

			if (recipe != null && assembler != null) {
				TotalItem b = new TotalItem(null, recipe, assembler);
				b.recipeRate = recipeRate;
				b.assemblerCount = assemblerCount;
				this.children.add(b);
			}

			this.recipe = null;
			this.assembler = null;
		}
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected, boolean hasFocus) {
		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected, hasFocus);

		if (this.item != null) {
			String prod = String.format("<html><b>%s</b> at <b>%s/s", Data.nameFor(this.item), Util.formatPlural(this.itemRate, "</b> item"));
			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) {
				prod += " (using ";
				ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s/s)</html>", Data.nameFor(this.getRecipe()), Util.formatPlural(this.recipeRate, "</b> cycle")), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			}
			ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.item), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

			if (this.assembler != null) {
				ret.add(new JLabel(String.format("<html>requires <b>%s</b> %s%s </html>", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(true)), TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			}
		} else if (this.recipe != null) {
			ret.add(new JLabel("using ", TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s</b> cycles/s%s</html>", Data.nameFor(this.getRecipe()), Util.formatPlural(this.recipeRate, "</b> cycle"), assembler != null ? String.format(" requires <b>%s</b> %s %s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)) : ""), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		} else if (this.assembler != null) {
			ret.add(new JLabel(String.format("<html><b>%s</b> %s%s</html>", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.assembler.getBonusString(true)), TreeCell.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}

	@Override
	public String getRawString() {
		if (this.item != null) {
			String ret = String.format("%s at %s/s", Data.nameFor(this.item), Util.formatPlural(this.itemRate, "item"));

			if (this.recipe != null && Util.hasMultipleRecipes(this.item)) {
				ret += String.format(" (using %s at %s/s)", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle"));
			}

			if (this.assembler != null) {
				ret += String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));
			}
			
			return ret;
		} else if (this.recipe != null) {
			String ret = String.format("using %s at %s/s", Data.nameFor(this.recipe), Util.formatPlural(this.recipeRate, "cycle"));
			
			if (this.assembler != null) {
				ret += String.format(" requires %s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));
			}
			
			return ret;
		} else if (this.assembler != null) {
			return String.format("%s %s%s", Util.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name), this.getAssembler().getBonusString(false));
		}

		return "";
	}

	@Override
	public int compareTo(TotalItem o) {
		int d = 0;
		if (this.item != null)
			d = Data.nameFor(this.item).compareTo(Data.nameFor(o.item));
		else if (this.recipe != null)
			d = Data.nameFor(recipe).compareTo(Data.nameFor(o.recipe));
		else if (this.assembler != null) d = assembler.compareTo(o.assembler);

		if (d != 0) return d;
		return Integer.compare(this.hashCode(), o.hashCode());
	}

	public SortedSet<TotalItem> getChildren() {
		return new TreeSet<>(this.children);
	}
}
