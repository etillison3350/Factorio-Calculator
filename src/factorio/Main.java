package factorio;

import java.io.IOException;
import java.nio.file.Paths;

import factorio.data.Data;
import factorio.window.Window;

public class Main {

	public static void main(String args[]) {
		try {
			Data.load(Paths.get("C:/Program Files/Factorio"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Window window = new Window();
		window.setVisible(true);
	}

}
