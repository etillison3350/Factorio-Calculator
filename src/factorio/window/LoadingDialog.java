package factorio.window;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadingDialog extends JFrame {

	private static final long serialVersionUID = 1042158646178608832L;

	private static Icon splash;

	private final JProgressBar progress;

	public LoadingDialog() {
		if (splash == null) {
			try {
				splash = new ImageIcon(Paths.get("resources/splash-screen-image.png").toUri().toURL());
			} catch (Exception e) {
				splash = new ImageIcon(new BufferedImage(307, 51, BufferedImage.TYPE_INT_ARGB_PRE));
			}
		}

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.setSize(384, 160);
		this.setLocationRelativeTo(null);
		this.setResizable(false);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

		content.add(new JLabel(splash));

		this.progress = new JProgressBar();
		this.progress.setIndeterminate(true);
		this.progress.setStringPainted(true);
		content.add(progress, BorderLayout.PAGE_END);

		this.add(content);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
				try {
					Thread.sleep(50);
				} catch (InterruptedException exception) {}
				System.exit(0);
			}
		});
	}

	public void setText(String text) {
		this.progress.setString(text);
	}

	public void setDeterminate(int maxValue) {
		this.progress.setIndeterminate(false);
		this.progress.setMaximum(maxValue);
	}

	public void incrementProgress() {
		this.progress.setValue(this.progress.getValue() + 1);
	}
}
