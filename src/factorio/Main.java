package factorio;

import java.io.IOException;
import java.nio.file.Paths;

import factorio.calculator.AssemblerSettings;
import factorio.data.Data;
import factorio.window.LoadingDialog;
import factorio.window.Window;

public class Main {

	public static final LoadingDialog loadingDialog = new LoadingDialog();

	public static void main(String args[]) {
		loadingDialog.setVisible(true);
		try {
			Data.load(Paths.get("C:/Program Files/Factorio"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadingDialog.setText("Reading assembler settings...");
		AssemblerSettings.readSettings();
		loadingDialog.dispose();

		System.out.println();
		
		Window window = new Window();
		window.setVisible(true);
	}

}
