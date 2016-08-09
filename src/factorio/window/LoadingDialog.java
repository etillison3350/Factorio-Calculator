package factorio.window;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * A dialog to inform the user that the program is loading before the main
 * window opens.
 * @author ricky3350
 */
public class LoadingDialog extends JFrame {

	private static final long serialVersionUID = 1042158646178608832L;

	/**
	 * The {@link Icon} that serves as the background image for the dialog
	 */
	private static final Icon SPLASH;

	static {
		ImageIcon splash;
		try {
			splash = new ImageIcon(Paths.get("resources/splash-screen-image.png").toUri().toURL());
		} catch (final Exception e) {
			splash = new ImageIcon(new BufferedImage(307, 51, BufferedImage.TYPE_INT_ARGB_PRE));
		}
		SPLASH = splash;
	}

	/**
	 * Shows the loading progress
	 */
	private final JProgressBar progress;

	public LoadingDialog() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setSize(384, 160);
		this.setLocationRelativeTo(null);
		this.setResizable(false);

		final JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

		content.add(new JLabel(SPLASH));

		this.progress = new JProgressBar();
		this.progress.setIndeterminate(true);
		this.progress.setStringPainted(true);
		content.add(this.progress, BorderLayout.PAGE_END);

		this.add(content);

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				LoadingDialog.this.dispose();
				try {
					Thread.sleep(50);
				} catch (final InterruptedException exception) {}
				System.exit(0);
			}
		});
	}

	/**
	 * <ul>
	 * <b><i>setText</i></b><br>
	 * <pre>public void setText({@link String} text)</pre> Sets the text of the
	 * progress bar
	 * @param text - The text to set
	 *        </ul>
	 */
	public void setText(String text) {
		this.progress.setString(text);
	}

	/**
	 * <ul>
	 * <b><i>setDeterminate</i></b><br>
	 * <pre>public void setDeterminate(int maxValue)</pre> Sets whether or not
	 * the progress bar is determinate, and also sets the bar's maximum value
	 * @param maxValue - The maximum value of the progress bar to set.
	 * @see {@link JProgressBar#setIndeterminate(boolean)}
	 *      </ul>
	 */
	public void setDeterminate(int maxValue) {
		this.progress.setIndeterminate(false);
		this.progress.setMaximum(maxValue);
	}

	/**
	 * <ul>
	 * <b><i>incrementProgress</i></b><br>
	 * <pre>public void incrementProgress()</pre> Increments the progress of the
	 * progress bar.
	 * </ul>
	 */
	public void incrementProgress() {
		this.progress.setValue(this.progress.getValue() + 1);
	}
}
