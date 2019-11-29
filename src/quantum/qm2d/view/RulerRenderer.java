package quantum.qm2d.view;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */
class RulerRenderer {

	private final static DecimalFormat NANOMETER_FORMAT = new DecimalFormat("##.##");
	private Stroke stroke = new BasicStroke(1);
	private Font smallFont = new Font(null, Font.PLAIN, 9);
	private int nx = 100, ny = 100;
	private float dx, dy;
	private float xmin, ymin;

	RulerRenderer() {
	}

	void setSize(float xmin, float xmax, float ymin, float ymax) {
		float ratio = (ymax - ymin) / (xmax - xmin);
		ny = (int) (nx * ratio);
		dx = (xmax - xmin) / nx;
		dy = (ymax - ymin) / ny;
		this.xmin = xmin;
		this.ymin = ymin;
	}

	private static void centerString(String s, Graphics2D g, int x, int y) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		g.drawString(s, x - stringWidth / 2, y);
	}

	void render(JComponent c, Graphics2D g) {

		if (!c.isVisible())
			return;

		int w = c.getWidth();
		int h = c.getHeight();

		g.setStroke(stroke);
		g.setFont(smallFont);
		int k;
		for (int i = 1; i < nx; i++) {
			k = Math.round(i * w / nx);
			if (i % 10 == 0) {
				g.drawLine(k, h, k, h - 10);
				centerString(NANOMETER_FORMAT.format(xmin + i * dx), g, k, h - 15);
			} else {
				g.drawLine(k, h, k, h - 5);
			}
		}
		centerString("nm", g, w - 10, h - 15);
		for (int i = 1; i < ny; i++) {
			k = Math.round(i * h / ny);
			if (i % 10 == 0) {
				g.drawLine(0, k, 10, k);
				centerString(NANOMETER_FORMAT.format(ymin + i * dy), g, 25, k + 3);
			} else {
				g.drawLine(0, k, 5, k);
			}
		}
		centerString("nm", g, 25, 10);

	}

}
