package factorio.window;

import javax.swing.JFrame;

public class Window extends JFrame {

	private static final long serialVersionUID = -377970844785993226L;

	public Window() {
		super("Factorio Calculator");
		this.setSize(1024, 768);
		this.setExtendedState(Window.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
