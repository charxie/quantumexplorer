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
public class AnnularPotential extends AreaPotential {

	private float innerRx, innerRy, outerRx, outerRy;

	public AnnularPotential(boolean imaginary, float energy, float xcenter, float ycenter, float outerRx, float outerRy, float innerRx, float innerRy, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, energy, nx, ny, xmin, xmax, ymin, ymax);
		this.xcenter = xcenter;
		this.ycenter = ycenter;
		this.outerRx = outerRx;
		this.outerRy = outerRy;
		this.innerRx = innerRx;
		this.innerRy = innerRy;
		float dx = (xmax - xmin) / nx;
		float dy = (ymax - ymin) / ny;
		float x, y;
		float outerAx, outerAy, innerAx, innerAy;
		for (int i = 0; i < nx; i++) {
			x = xmin + i * dx;
			outerAx = (x - xcenter) / outerRx;
			outerAx *= outerAx;
			innerAx = (x - xcenter) / innerRx;
			innerAx *= innerAx;
			for (int j = 0; j < ny; j++) {
				y = ymin + j * dy;
				outerAy = (y - ycenter) / outerRy;
				innerAy = (y - ycenter) / innerRy;
				if (outerAx + outerAy * outerAy < 1 && innerAx + innerAy * innerAy > 1) {
					pot[i][j] = energy;
				}
			}
		}
	}

	public void setRects(Rectangle[] rect, Polygon[] handle, Dimension d, Boundary bx, Boundary by) {
		super.setBoundaries(bx, by);
		int h = rect[0].width / 2;
		rect[UPPER_LEFT].x = convertPointX(xcenter - outerRx, d.width) - h;
		rect[UPPER_LEFT].y = convertPointY(ycenter - outerRy, d.height) - h;
		rect[LOWER_LEFT].x = convertPointX(xcenter - outerRx, d.width) - h;
		rect[LOWER_LEFT].y = convertPointY(ycenter + outerRy, d.height) - h;
		rect[UPPER_RIGHT].x = convertPointX(xcenter + outerRx, d.width) - h;
		rect[UPPER_RIGHT].y = convertPointY(ycenter - outerRy, d.height) - h;
		rect[LOWER_RIGHT].x = convertPointX(xcenter + outerRx, d.width) - h;
		rect[LOWER_RIGHT].y = convertPointY(ycenter + outerRy, d.height) - h;
		rect[TOP].x = convertPointX(xcenter, d.width) - h;
		rect[TOP].y = convertPointY(ycenter - outerRy, d.height) - h;
		rect[BOTTOM].x = convertPointX(xcenter, d.width) - h;
		rect[BOTTOM].y = convertPointY(ycenter + outerRy, d.height) - h;
		rect[LEFT].x = convertPointX(xcenter - outerRx, d.width) - h;
		rect[LEFT].y = convertPointY(ycenter, d.height) - h;
		rect[RIGHT].x = convertPointX(xcenter + outerRx, d.width) - h;
		rect[RIGHT].y = convertPointY(ycenter, d.height) - h;
		for (int i = 0; i < handle.length; i++)
			handle[i].reset();
		int x = convertPointX(xcenter - innerRx, d.width);
		int y = convertPointY(ycenter, d.height);
		handle[0].addPoint(x - 4, y);
		handle[0].addPoint(x, y + 4);
		handle[0].addPoint(x + 4, y);
		handle[0].addPoint(x, y - 4);
		x = convertPointX(xcenter, d.width);
		y = convertPointY(ycenter - innerRy, d.height);
		handle[1].addPoint(x - 4, y);
		handle[1].addPoint(x, y + 4);
		handle[1].addPoint(x + 4, y);
		handle[1].addPoint(x, y - 4);
	}

	public boolean contains(float x, float y) {
		float a = (x - xcenter) / innerRx;
		float b = (y - ycenter) / innerRy;
		float c = (x - xcenter) / outerRx;
		float d = (y - ycenter) / outerRy;
		return a * a + b * b >= 1 && c * c + d * d <= 1;
	}

	public void setOuterRx(float outerRx) {
		this.outerRx = outerRx;
	}

	public float getOuterRx() {
		return outerRx;
	}

	public void setOuterRy(float outerRy) {
		this.outerRy = outerRy;
	}

	public float getOuterRy() {
		return outerRy;
	}

	public void setInnerRx(float innerRx) {
		this.innerRx = innerRx;
	}

	public float getInnerRx() {
		return innerRx;
	}

	public void setInnerRy(float innerRy) {
		this.innerRy = innerRy;
	}

	public float getInnerRy() {
		return innerRy;
	}

	public String toXml() {
		String xml = "<annular energy=\"" + energy;
		if (imaginary)
			xml += "\" imaginary=\"" + imaginary;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
		xml += "\" innerrx=\"" + innerRx;
		xml += "\" innerry=\"" + innerRy;
		xml += "\" outerrx=\"" + outerRx;
		xml += "\" outerry=\"" + outerRy;
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
