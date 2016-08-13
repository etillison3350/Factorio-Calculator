package factorio.window;

import factorio.calculator.AssemblerSettings;
import factorio.data.Technology;

public class TechnologyProductListRow extends ProductListRow {

	private static final long serialVersionUID = 4792177846559773999L;

	public TechnologyProductListRow(final Technology tech) {
		super(tech);
	}

	@Override
	protected String[] getOptions() {
		return new String[] {"seconds to finish", "max cap. labs"};
	}

	@Override
	public double getRate() {
		if (Double.isNaN(this.getValue())) return 0;

		switch (this.getSelectedOption()) {
			case "max cap. labs":
				return 1 / (this.getValue() * this.getAssemblerSettings().getSpeed() * this.recipe.time);
			default:
				return 1 / this.getValue();
		}
	}

	@Override
	public AssemblerSettings configure() {
		// TODO
		return null;
	}

}
