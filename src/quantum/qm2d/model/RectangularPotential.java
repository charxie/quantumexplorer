package quantum.qm2d.model;

import static quantum.qm2d.view.View2D.BOTTOM;
import static quantum.qm2d.view.View2D.LEFT;
import static quantum.qm2d.view.View2D.LOWER_LEFT;
import static quantum.qm2d.view.View2D.LOWER_RIGHT;
import static quantum.qm2d.view.View2D.RIGHT;
import static quantum.qm2d.view.View2D.TOP;
import static quantum.qm2d.view.View2D.UPPER_LEFT;
import static quantum.qm2d.view.View2D.UPPER_RIGHT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;

import quantum.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
public class RectangularPotential extends AreaPotential {

	private float lx, ly, cornerRadius;

	public RectangularPotential(boolean imaginary, float energy, float xcenter, float ycenter, float lx, float ly, float cornerRadius, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, energy, nx, ny, xmin, xmax, ymin, ymax);
		this.xcenter = xcenter;
		this.ycenter = ycenter;
		this.lx = lx;
		this.ly = ly;
		this.cornerRadius = cornerRadius;
		float dx = (xmax - xmin) / nx;
		float dy = (ymax - ymin) / ny;
		float x, y;
		if (cornerRadius <= 0) {
			boolean b;
			for (int i = 0; i < nx; i++) {
				x = xmin + i * dx;
				b = Math.abs(x - xcenter) < 0.5 * lx;
				for (int j = 0; j < ny; j++) {
					y = ymin + j * dy;
					if (b && Math.abs(y - ycenter) < 0.5 * ly) {
						pot[i][j] = energy;
					}
				}
			}
		} else {
			for (int i = 0; i < nx; i++) {
				x = xmin + i * dx;
				for (int j = 0; j < ny; j++) {
					y = ymin + j * dy;
					if (contains(x, y)) {
						pot[i][j] = energy;
					}
				}
			}
		}
	}

	public void setRects(Rectangle[] rect, Polygon[] handle, Dimension d, Boundary bx, Boundary by) {
		super.setBoundaries(bx, by);
		int h = rect[0].width / 2;
		rect[UPPER_LEFT].x = convertPointX(xcenter - lx * 0.5f, d.width) - h;
		rect[UPPER_LEFT].y = convertPointY(ycenter - ly * 0.5f, d.height) - h;
		rect[LOWER_LEFT].x = convertPointX(xcenter - lx * 0.5f, d.width) - h;
		rect[LOWER_LEFT].y = convertPointY(ycenter + ly * 0.5f, d.height) - h;
		rect[UPPER_RIGHT].x = convertPointX(xcenter + lx * 0.5f, d.width) - h;
		rect[UPPER_RIGHT].y = convertPointY(ycenter - ly * 0.5f, d.height) - h;
		rect[LOWER_RIGHT].x = convertPointX(xcenter + lx * 0.5f, d.width) - h;
		rect[LOWER_RIGHT].y = convertPointY(ycenter + ly * 0.5f, d.height) - h;
		rect[TOP].x = convertPointX(xcenter, d.width) - h;
		rect[TOP].y = convertPointY(ycenter - ly * 0.5f, d.height) - h;
		rect[BOTTOM].x = convertPointX(xcenter, d.width) - h;
		rect[BOTTOM].y = convertPointY(ycenter + ly * 0.5f, d.height) - h;
		rect[LEFT].x = convertPointX(xcenter - lx * 0.5f, d.width) - h;
		rect[LEFT].y = convertPointY(ycenter, d.height) - h;
		rect[RIGHT].x = convertPointX(xcenter + lx * 0.5f, d.width) - h;
		rect[RIGHT].y = convertPointY(ycenter, d.height) - h;
		for (int i = 0; i < handle.length; i++)
			handle[i].reset();
		float x0 = rect[UPPER_LEFT].x + convertLengthX(cornerRadius, d.width);
		float y0 = rect[UPPER_LEFT].y;
		handle[0].addPoint(Math.round(x0 - 4), Math.round(y0 - 14));
		handle[0].addPoint(Math.round(x0), Math.round(y0 - 18));
		handle[0].addPoint(Math.round(x0 + 4), Math.round(y0 - 14));
		handle[0].addPoint(Math.round(x0), Math.round(y0 - 10));
	}

	public boolean contains(float x, float y) {
		if (cornerRadius <= 0)
			return Math.abs(x - xcenter) < 0.5f * lx && Math.abs(y - ycenter) < 0.5f * ly;
		float xmin = xcenter - 0.5f * lx;
		float ymin = ycenter - 0.5f * ly;
		float xmax = xmin + lx;
		float ymax = ymin + ly;
		if (x < xmin || y < ymin || x >= xmax || y >= ymax)
			return false;
		float aw = Math.min(lx * 0.5f, cornerRadius);
		float ah = Math.min(ly * 0.5f, cornerRadius);
		if (x >= (xmin += aw) && x < (xmin = xmax - aw))
			return true;
		if (y >= (ymin += ah) && y < (ymin = ymax - ah))
			return true;
		x = (x - xmin) / aw;
		y = (y - ymin) / ah;
		return x * x + y * y <= 1;
	}

	public void setLx(float lx) {
		this.lx = lx;
	}

	public float getLx() {
		return lx;
	}

	public void setLy(float ly) {
		this.ly = ly;
	}

	public float getLy() {
		return ly;
	}

	public void setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
	}

	public float getCornerRadius() {
		return cornerRadius;
	}

	public String toXml() {
		String xml = "<rectangular energy=\"" + energy;
		if (imaginary)
			xml += "\" imaginary=\"" + imaginary;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
		xml += "\" width=\"" + lx;
		xml += "\" height=\"" + ly;
		if (cornerRadius > 0)
			xml += "\" corner=\"" + cornerRadius;
		if (!getColor().equals(Color.gray))
			xml += "\" color=\"" + Integer.toHexString(0x00ffffff & getColor().getRGB());
		if (!isVisible())
			xml += "\" visible=\"false";
		if (!isDraggable())
			xml += "\" draggable=\"false";
		xml += "\"/>\n";
		return xml;
	}

	@Override
	public String toString() {
		return toXml();
	}

}
