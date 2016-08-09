package factorio.window;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A {@link JLabel} with the {@link Icon} behind the text, and a border around the text. The border color is specified by
 * {@link #setBackground(Color)}
 * @author ricky3350
 */
public class BorderLabel extends JLabel {

	private static final long serialVersionUID = -4284802313613195318L;

	private String shadowText;

	public BorderLabel(String text, Icon image) {
		super(image);

		this.shadowText = "" + text;

		this.setForeground(Color.WHITE);
		this.setBackground(Color.BLACK);

		super.setOpaque(false);
	}

	public String getShadowText() {
		return this.shadowText;
	}

	public void setShadowText(String shadowText) {
		this.shadowText = shadowText;
	}

	/**
	 * <ul>
	 * <b><i>setOpaque</i></b><br>
	 * <pre>public void setOpaque(boolean isOpaque)</pre> {@code BorderLabel}s cannot be opaque
	 * </ul>
	 */
	@Override
	public void setOpaque(boolean isOpaque) {}

	@Override
	protected void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		if (!(gg instanceof Graphics2D)) return;
		final Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(this.getFont());

		final FontMetrics fm = g.getFontMetrics();
		final int strWidth = fm.stringWidth(this.shadowText);

		g.translate(this.getWidth() - strWidth - 2, this.getHeight() - 2);

		final GlyphVector vector = this.getFont().createGlyphVector(g.getFontRenderContext(), this.shadowText);

		final Shape ch = vector.getOutline();
		g.setStroke(new BasicStroke(2));
		g.setColor(this.getBackground());
		g.draw(ch);
		g.setColor(this.getForeground());
		g.fill(ch);
	}
}
