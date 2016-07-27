package factorio.data;

import java.util.ArrayList;
import java.util.Arrays;

public class OffshorePump extends Assembler {

	public OffshorePump(String name, float speed) {
		super(name, 0, speed, 0, 0, false, 1, Arrays.asList("pump-" + name), new ArrayList<>());
	}

}
