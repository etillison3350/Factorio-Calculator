package factorio;

import java.io.IOException;
import java.nio.file.Paths;

import factorio.calculator.AssemblerSettings;
import factorio.data.Data;
import factorio.window.LoadingDialog;
import factorio.window.Window;

/**
 * The main class, for starting the program. Houses the main method and a
 * loading dialog, only.
 * @author ricky3350
 */
public class Main {

	public static final LoadingDialog loadingDialog = new LoadingDialog();

	public static void main(final String args[]) {
		loadingDialog.setVisible(true);
		try {
			Data.load(Paths.get("C:/Program Files/Factorio"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		loadingDialog.setText("Reading assembler settings...");
		AssemblerSettings.readSettings();
		loadingDialog.dispose();

		final Window window = new Window();
		window.setVisible(true);
	}

}
