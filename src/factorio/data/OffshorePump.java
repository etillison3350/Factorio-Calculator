package factorio.data;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A special type of assembler that creates one specific type of fluid.
 * @author ricky3350
 * @see {@link OffshoreRecipe}
 */
public class OffshorePump extends Assembler {

	public OffshorePump(String name, double speed) {
		super(name, 0, speed, 0, 0, false, 1, Arrays.asList("pump-" + name), new ArrayList<>());
	}

}
