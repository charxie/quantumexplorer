package quantum.qm2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;

import quantum.qm2d.model.AnnularPotential;
import quantum.qm2d.model.EllipticalPotential;
import quantum.qm2d.model.IonicPotential;
import quantum.qm2d.model.Potential2D;
import quantum.qm2d.model.RectangularPotential;
import quantum.qmshared.AbsorbingBoundary;
import quantum.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
class PotentialRenderer {

	private Stroke borderStroke = new BasicStroke(2);
	private Boundary xBoundary, yBoundary;
	private float xmin, xmax, ymin, ymax;
	private float x0, x1, y0, y1, dx, dy;

	PotentialRenderer() {
	}

	void setArea(float xmin, float xmax, float ymin, float ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	void setBoundary(char direction, Boundary boundary) {
		if (direction == 'x' || direction == 'X')
			xBoundary = boundary;
		if (direction == 'y' || direction == 'Y')
			yBoundary = boundary;
	}

	void render(Potential2D p, JComponent c, Graphics2D g) {

		if (!c.isVisible() || !p.isVisible())
			return;

		if (p instanceof IonicPotential) {
			renderPotential((IonicPotential) p, c, g);
		} else if (p instanceof RectangularPotential) {
			renderPotential((RectangularPotential) p, c, g);
		} else if (p instanceof EllipticalPotential) {
			renderPotential((EllipticalPotential) p, c, g);
		} else if (p instanceof AnnularPotential) {
			renderPotential((AnnularPotential) p, c, g);
		}

	}

	private void renderPotential(IonicPotential ion, JComponent c, Graphics2D g) {

		calculateWindow(c.getSize());
		g.setStroke(borderStroke);
		int x = (int) ((ion.getXcenter() - ion.getOffset() - x0) * dx);
		int y = (int) ((ion.getYcenter() - ion.getOffset() - y0) * dy);
		int w = (int) (2 * ion.getOffset() * dx);
		int h = (int) (2 * ion.getOffset() * dy);
		if (w == 0)
			w = 1;
		if (h == 0)
			h = 1;
		g.setColor(ion.getColor());
		g.fillOval(x, y, w, h);
		if (ion.getCharge() < 0)
			paintEllipticalBorder(false, c, g, x, y, w, h);
		else if (ion.getCharge() > 0)
			paintEllipticalBorder(true, c, g, x, y, w, h);
		if (ion.getCharge() != 0) {
			g.setColor(ion.getCharge() > 0 ? Color.red : Color.blue);
			g.drawLine(x + w / 2 - 5, y + h / 2, x + w / 2 + 5, y + h / 2);
			if (ion.getCharge() > 0)
				g.drawLine(x + w / 2, y + h / 2 - 5, x + w / 2, y + h / 2 + 5);
		}

	}

	private void renderPotential(AnnularPotential area, JComponent c, Graphics2D g) {
		calculateWindow(c.getSize());
		float x = (area.getXcenter() - area.getOuterRx() - x0) * dx;
		float y = (area.getYcenter() - area.getOuterRy() - y0) * dy;
		float w = 2 * area.getOuterRx() * dx;
		float h = 2 * area.getOuterRy() * dy;
		g.setStroke(borderStroke);
		paintEllipticalBorder(area.getEnergy() > 0, c, g, (int) (x - 1), (int) (y - 1), (int) (w + 2), (int) (h + 2));
		Area annulus = new Area(new Ellipse2D.Float(x, y, w, h));
		x = (area.getXcenter() - area.getInnerRx() - x0) * dx;
		y = (area.getYcenter() - area.getInnerRy() - y0) * dy;
		w = 2 * area.getInnerRx() * dx;
		h = 2 * area.getInnerRy() * dy;
		annulus.subtract(new Area(new Ellipse2D.Float(x, y, w, h)));
		paintEllipticalBorder(area.getEnergy() > 0, c, g, (int) x, (int) y, (int) w, (int) h);
		g.setColor(area.getColor());
		g.fill(annulus);
	}

	private void renderPotential(EllipticalPotential area, JComponent c, Graphics2D g) {
		calculateWindow(c.getSize());
		int x = (int) ((area.getXcenter() - area.getRx() - x0) * dx);
		int y = (int) ((area.getYcenter() - area.getRy() - y0) * dy);
		int w = (int) (2 * area.getRx() * dx);
		int h = (int) (2 * area.getRy() * dy);
		g.setColor(area.getColor());
		g.fillOval(x, y, w, h);
		g.setStroke(borderStroke);
		paintEllipticalBorder(area.getEnergy() > 0, c, g, x, y, w, h);
	}

	private void renderPotential(RectangularPotential area, JComponent c, Graphics2D g) {
		calculateWindow(c.getSize());
		int x = (int) ((area.getXcenter() - area.getLx() * 0.5 - x0) * dx);
		int y = (int) ((area.getYcenter() - area.getLy() * 0.5 - y0) * dy);
		int w = (int) (area.getLx() * dx);
		int h = (int) (area.getLy() * dy);
		int a = (int) (area.getCornerRadius() * dx);
		int b = (int) (area.getCornerRadius() * dy);
		g.setColor(area.getColor());
		g.fillRoundRect(x, y, w, h, a * 2, b * 2);
		g.setStroke(borderStroke);
		paintRoundRectangularBorder(area.getEnergy() > 0, c, g, x, y, w, h, a, b);
	}

	public static void paintRoundRectangularBorder(boolean lowered, JComponent c, Graphics2D g, int x, int y, int w, int h, int a, int b) {
		g.translate(x, y);
		Color oldColor = g.getColor();
		Color bg = c.getBackground();
		if (bg.equals(Color.black) || bg.equals(Color.white)) // special cases
			bg = Color.gray;
		if (a == 0 && b == 0) {
			g.setColor(lowered ? bg.darker() : bg.brighter());
			g.drawRect(0, 0, w, h);
			g.setColor(lowered ? bg.brighter() : bg.darker());
			g.drawRect(1, 1, w - 2, h - 2);
		} else {
			g.setColor(lowered ? bg.darker() : bg.brighter());
			g.drawRoundRect(0, 0, w, h, 2 * a, 2 * b);
			g.setColor(lowered ? bg.brighter() : bg.darker());
			g.drawRoundRect(1, 1, w - 2, h - 2, 2 * a - 2, 2 * b - 2);
		}
		g.setColor(oldColor);
		g.translate(-x, -y);
	}

	private static void paintEllipticalBorder(boolean lowered, JComponent c, Graphics2D g, int x, int y, int w, int h) {
		g.translate(x, y);
		Color oldColor = g.getColor();
		Color bg = c.getBackground();
		if (bg.equals(Color.black) || bg.equals(Color.white)) // special cases
			bg = Color.gray;
		g.setColor(lowered ? bg.darker() : bg.brighter());
		g.drawOval(0, 0, w, h);
		g.setColor(lowered ? bg.brighter() : bg.darker());
		g.drawOval(1, 1, w - 2, h - 2);
		g.setColor(oldColor);
		g.translate(-x, -y);
	}

	private void calculateWindow(Dimension size) {
		x0 = xmin;
		x1 = xmax;
		y0 = ymin;
		y1 = ymax;
		if (xBoundary instanceof AbsorbingBoundary) {
			float layer = (xmax - xmin) * ((AbsorbingBoundary) xBoundary).getLengthPercentage();
			x0 = xmin + layer;
			x1 = xmax - layer;
		}
		if (yBoundary instanceof AbsorbingBoundary) {
			float layer = (ymax - ymin) * ((AbsorbingBoundary) yBoundary).getLengthPercentage();
			y0 = ymin + layer;
			y1 = ymax - layer;
		}
		dx = size.width / (x1 - x0);
		dy = size.height / (y1 - y0);
	}

}
