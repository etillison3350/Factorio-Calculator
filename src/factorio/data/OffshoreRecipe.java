package factorio.data;

import java.awt.Image;
import java.util.HashMap;

/**
 * A recipe for use in {@link OffshorePump}s
 * @author ricky3350
 */
public class OffshoreRecipe extends Recipe {

	public OffshoreRecipe(final String name, final String pumpName, final String fluid, final Image icon) {
		super(name, "pump-" + pumpName, 1 / 60.0, new HashMap<>(), fluid, icon);
	}

}
