package org.concord.qm2d.model;

import static org.concord.qm2d.view.View2D.BOTTOM;
import static org.concord.qm2d.view.View2D.LEFT;
import static org.concord.qm2d.view.View2D.LOWER_LEFT;
import static org.concord.qm2d.view.View2D.LOWER_RIGHT;
import static org.concord.qm2d.view.View2D.RIGHT;
import static org.concord.qm2d.view.View2D.TOP;
import static org.concord.qm2d.view.View2D.UPPER_LEFT;
import static org.concord.qm2d.view.View2D.UPPER_RIGHT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.concord.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
public class EllipticalPotential extends AreaPotential {

	private float rx, ry;

	public EllipticalPotential(boolean imaginary, float energy, float xcenter, float ycenter, float rx, float ry, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, energy, nx, ny, xmin, xmax, ymin, ymax);
		this.xcenter = xcenter;
		this.ycenter = ycenter;
		this.rx = rx;
		this.ry = ry;
		float dx = (xmax - xmin) / nx;
		float dy = (ymax - ymin) / ny;
		float x, y;
		float ax, ay;
		for (int i = 0; i < nx; i++) {
			x = xmin + i * dx;
			ax = (x - xcenter) / rx;
			ax *= ax;
			for (int j = 0; j < ny; j++) {
				y = ymin + j * dy;
				ay = (y - ycenter) / ry;
				if (ax + ay * ay < 1) {
					pot[i][j] = energy;
				}
			}
		}
	}

	public void setRects(Rectangle[] rect, Polygon[] handle, Dimension d, Boundary bx, Boundary by) {
		super.setBoundaries(bx, by);
		int h = rect[0].width / 2;
		rect[UPPER_LEFT].x = convertPointX(xcenter - rx, d.width) - h;
		rect[UPPER_LEFT].y = convertPointY(ycenter - ry, d.height) - h;
		rect[LOWER_LEFT].x = convertPointX(xcenter - rx, d.width) - h;
		rect[LOWER_LEFT].y = convertPointY(ycenter + ry, d.height) - h;
		rect[UPPER_RIGHT].x = convertPointX(xcenter + rx, d.width) - h;
		rect[UPPER_RIGHT].y = convertPointY(ycenter - ry, d.height) - h;
		rect[LOWER_RIGHT].x = convertPointX(xcenter + rx, d.width) - h;
		rect[LOWER_RIGHT].y = convertPointY(ycenter + ry, d.height) - h;
		rect[TOP].x = convertPointX(xcenter, d.width) - h;
		rect[TOP].y = convertPointY(ycenter - ry, d.height) - h;
		rect[BOTTOM].x = convertPointX(xcenter, d.width) - h;
		rect[BOTTOM].y = convertPointY(ycenter + ry, d.height) - h;
		rect[LEFT].x = convertPointX(xcenter - rx, d.width) - h;
		rect[LEFT].y = convertPointY(ycenter, d.height) - h;
		rect[RIGHT].x = convertPointX(xcenter + rx, d.width) - h;
		rect[RIGHT].y = convertPointY(ycenter, d.height) - h;
		for (int i = 0; i < handle.length; i++)
			handle[i].reset();
	}

	public boolean contains(float x, float y) {
		float a = (x - xcenter) / rx;
		float b = (y - ycenter) / ry;
		return a * a + b * b <= 1;
	}

	public void setRx(float rx) {
		this.rx = rx;
	}

	public float getRx() {
		return rx;
	}

	public void setRy(float ry) {
		this.ry = ry;
	}

	public float getRy() {
		return ry;
	}

	public String toXml() {
		String xml = "<elliptical energy=\"" + energy;
		if (imaginary)
			xml += "\" imaginary=\"" + imaginary;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
		xml += "\" rx=\"" + rx;
		xml += "\" ry=\"" + ry;
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
