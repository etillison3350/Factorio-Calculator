package factorio.data;

import java.awt.Image;
import java.util.HashMap;

public class OffshoreRecipe extends Recipe {

	public OffshoreRecipe(String name, String pumpName, String fluid, Image icon) {
		super(name, "pump-" + pumpName, 0.016666666F, new HashMap<>(), fluid, icon);
	}

}
