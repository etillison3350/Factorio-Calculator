package factorio.data;

import java.awt.Image;
import java.util.HashMap;

/**
 * A recipe for use in {@link OffshorePump}s
 * @author ricky3350
 */
public class OffshoreRecipe extends Recipe {

	public OffshoreRecipe(String name, String pumpName, String fluid, Image icon) {
		super(name, "pump-" + pumpName, 0.016666666F, new HashMap<>(), fluid, icon);
	}

}
