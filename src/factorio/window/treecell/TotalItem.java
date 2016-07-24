package factorio.window.treecell;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import factorio.calculator.AssemblerSettings;
import factorio.data.Data;
import factorio.data.Recipe;

public class TotalItem implements TreeCell {

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

		recipe: if (this.recipe == null) {
			for (TotalItem ti : children) {
				if (ti.recipe == recipe) {
					ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
					break recipe;
				}
			}
			TotalItem b = new TotalItem(null, recipe, assembler);
			b.recipeRate = recipeRate;
			b.assemblerCount = assemblerCount;
			this.children.add(b);
		} else if (this.recipe == recipe) {
			this.recipeRate += recipeRate;

			assembler: if (this.assembler == null) {
				for (TotalItem ti : children) {
					if (ti.recipe == recipe) {
						ti.add(itemRate, recipeRate, recipe, assemblerCount, assembler);
						break assembler;
					}
				}
				TotalItem b = new TotalItem(null, null, assembler);
				b.assemblerCount = assemblerCount;
				this.children.add(b);
			} else if (this.assembler == assembler) {
				this.assemblerCount += assemblerCount;
			} else {
				TotalItem a = new TotalItem(null, null, this.assembler);
				a.assemblerCount = this.assemblerCount;
				TotalItem b = new TotalItem(null, null, assembler);
				b.assemblerCount = assemblerCount;

				this.children.add(a);
				this.children.add(b);

				this.assembler = null;
			}
		} else {
			TotalItem a = new TotalItem(null, this.recipe, this.assembler);
			a.recipeRate = this.recipeRate;
			a.assemblerCount = this.assemblerCount;
			TotalItem b = new TotalItem(null, recipe, assembler);
			b.recipeRate = recipeRate;
			b.assemblerCount = assemblerCount;

			this.children.add(a);
			this.children.add(b);

			this.recipe = null;
			this.assembler = null;
		}
	}

	@Override
	public Component getTreeCellRendererComponent(boolean selected) {
		JPanel ret = new JPanel(new FlowLayout(FlowLayout.LEADING, 1, 1));
		TreeCell.addBorders(ret, selected);

		// TODO modify a little bit to exclude item when it is null
		
		String prod = String.format("<html><b>%s</b> at <b>%s</b> items/s", Data.nameFor(this.item), Data.NUMBER_FORMAT.format(this.itemRate));
		if (Data.hasMultipleRecipes(this.item)) {
			prod += " (via ";
			ret.add(new JLabel(String.format("<html><b>%s</b> at <b>%s</b> cycles/s)</html>", Data.nameFor(this.getRecipe()), Data.NUMBER_FORMAT.format(this.recipeRate)), this.getRecipe().getSmallIcon(), SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}
		ret.add(new JLabel(prod + "</html>", Data.getItemIcon(this.item), SwingConstants.LEADING), 0).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		if (this.getAssembler() != null) {
			String assemblerStr = String.format("<html>requires <b>%s</b> %s", Data.NUMBER_FORMAT.format(this.assemblerCount), Data.nameFor(this.assembler.getAssembler().name));

			assemblerStr += this.getAssembler().getBonusString();

			ret.add(new JLabel(assemblerStr + " </html>", Data.ICON_BLANK, SwingConstants.LEADING)).setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		}

		return ret;
	}

}
