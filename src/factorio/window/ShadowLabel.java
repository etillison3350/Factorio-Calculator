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

public class ShadowLabel extends JLabel {

	private static final long serialVersionUID = -4284802313613195318L;

	private String text;

	public ShadowLabel(String text, Icon image) {
		super(image);
		
		this.text = text;
		this.setForeground(Color.WHITE);
		this.setBackground(Color.BLACK);
		
		this.setOpaque(false);
	}

	public void setShadowText(String text) {
		this.text = text;
		repaint();
	}

	public String getShadowText() {
		return this.text;
	}

	@Override
	protected void paintComponent(Graphics gg) {
		super.paintComponent(gg);
		
		if (!(gg instanceof Graphics2D)) return;
		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(this.getFont());

		FontMetrics fm = g.getFontMetrics();
		int strWidth = fm.stringWidth(text);

//		g.drawString(text, this.getWidth() - strWidth - 2, this.getHeight() - 2);
//		g.drawString(text, this.getWidth() - strWidth, this.getHeight() - 2);
//		g.drawString(text, this.getWidth() - strWidth - 2, this.getHeight());
//		g.drawString(text, this.getWidth() - strWidth, this.getHeight());
		
//		g.setTransform(new AffineTransform(0, 0, 0, 0, this.getWidth() - strWidth - 1, this.getHeight() - 1));
		g.translate(this.getWidth() - strWidth - 2, this.getHeight() - 2);
		
		GlyphVector vector = this.getFont().createGlyphVector(g.getFontRenderContext(), text);
//		for (int i = 0; i < text.length(); i++) {
//			
//		}
		
		Shape ch = vector.getOutline();
		g.setStroke(new BasicStroke(2));
		g.setColor(this.getBackground());
		g.draw(ch);
		g.setColor(this.getForeground());
		g.fill(ch);

//		g.setColor(this.getForeground());
//		g.drawString(text, this.getWidth() - strWidth - 1, this.getHeight() - 1);
	}
}
