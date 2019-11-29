package quantum.qm2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import javax.swing.JComponent;

import quantum.math.Vector2D;
import quantum.qmshared.AbsorbingBoundary;
import quantum.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
class CurrentRenderer {

	private static final float SCALE_UNIT = 1000;
	private final static float COS45 = (float) Math.cos(Math.toRadians(45));
	private final static float SIN45 = (float) Math.sin(Math.toRadians(45));

	private Stroke stroke = new BasicStroke(1);
	private Color color = new Color(225, 225, 128);
	private float scale = 10;
	private Vector2D[][] current;
	private Boundary xBoundary, yBoundary;

	CurrentRenderer() {
	}

	void setBoundary(Boundary xBoundary, Boundary yBoundary) {
		this.xBoundary = xBoundary;
		this.yBoundary = yBoundary;
	}

	void setScale(float scale) {
		this.scale = scale;
	}

	void setCurrent(Vector2D[][] current) {
		this.current = current;
	}

	void render(JComponent c, Graphics2D g) {

		if (!c.isVisible() || current == null)
			return;

		int w = c.getWidth();
		int h = c.getHeight();
		int mx = current.length;
		int my = current[0].length;
		int mx0 = 0;
		int my0 = 0;
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			mx0 = (int) (mx * a.getLengthPercentage());
			mx -= 2 * mx0;
		}
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			my0 = (int) (my * a.getLengthPercentage());
			my -= 2 * my0;
		}
		float dx = (float) w / (float) mx;
		float dy = (float) h / (float) my;

		g.setColor(color);
		g.setStroke(stroke);
		int ix, iy;
		float tx, ty;
		float s = scale * SCALE_UNIT;
		Vector2D v;
		for (int i = 0; i < mx; i++) {
			ix = Math.round((i + 0.5f) * dx);
			for (int j = 0; j < my; j++) {
				v = current[i + mx0][j + my0];
				tx = s * v.x;
				ty = s * v.y;
				if (tx >= 1 || ty >= 1) {
					iy = Math.round((j + 0.5f) * dy);
					drawVector(g, ix, iy, tx, ty);
				}
			}
		}

	}

	private void drawVector(Graphics2D g, int x, int y, float tx, float ty) {
		int x2 = (int) (x + tx);
		int y2 = (int) (y + ty);
		g.drawLine(x, y, x2, y2);
		float rinv = 1.0f / (float) Math.hypot(tx, ty);
		float arrowX = tx * rinv;
		float arrowY = ty * rinv;
		float arrowLength = 2;
		float wingX = arrowLength * (arrowX * COS45 + arrowY * SIN45);
		float wingY = arrowLength * (arrowY * COS45 - arrowX * SIN45);
		g.drawLine(x2, y2, (int) (x2 - wingX), (int) (y2 - wingY));
		wingX = arrowLength * (arrowX * COS45 - arrowY * SIN45);
		wingY = arrowLength * (arrowY * COS45 + arrowX * SIN45);
		g.drawLine(x2, y2, (int) (x2 - wingX), (int) (y2 - wingY));
	}

}
