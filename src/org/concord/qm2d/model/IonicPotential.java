package org.concord.qm2d.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;

import org.concord.qmshared.Boundary;

/**
 * To avoid the singularity of the Coulombic potential, we use the following modified form:
 * 
 * v(r)=q*r/(r^2+a)
 * 
 * This function approaches 1/r when r approaches infinity. It becomes zero when r becomes zero. a can also be regarded as a parameter that represents electronegativity.
 * 
 * @author Charles Xie
 * 
 */
public class IonicPotential extends PointPotential {

	private final static float COULOMB_CONSTANT_OVER_EVNM = 2.30708f / 1.6f;
	private float charge, offset;

	public IonicPotential(boolean imaginary, float e, float charge, float offset, float xcenter, float ycenter, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, nx, ny, xmin, xmax, ymin, ymax);
		this.xcenter = xcenter;
		this.ycenter = ycenter;
		this.charge = charge;
		this.offset = offset;
		float dx = (xmax - xmin) / nx;
		float dy = (ymax - ymin) / ny;
		float dx2, dy2;
		float rSq;
		if (charge != 0) {
			float k = COULOMB_CONSTANT_OVER_EVNM * e * charge;
			for (int i = 0; i < nx; i++) {
				dx2 = xmin + i * dx - xcenter;
				dx2 *= dx2;
				for (int j = 0; j < ny; j++) {
					dy2 = ymin + j * dy - ycenter;
					rSq = dx2 + dy2 * dy2;
					pot[i][j] = (float) (k * Math.sqrt(rSq) / (rSq + offset));
				}
			}
		} else {
			for (int i = 0; i < nx; i++) {
				dx2 = xmin + i * dx - xcenter;
				dx2 *= dx2;
				for (int j = 0; j < ny; j++) {
					dy2 = ymin + j * dy - ycenter;
					rSq = dx2 + dy2 * dy2;
					pot[i][j] = rSq > offset * offset ? 0 : 1;
				}
			}
		}
	}

	public void setHalo(Ellipse2D.Float halo, Dimension d, Boundary bx, Boundary by) {
		super.setBoundaries(bx, by);
		halo.width = convertLengthX(offset, d.width) + 20;
		halo.height = convertLengthY(offset, d.height) + 20;
		halo.x = convertPointX(xcenter, d.width) - halo.width / 2;
		halo.y = convertPointY(ycenter, d.height) - halo.height / 2;
	}

	public boolean contains(float x, float y) {
		float size = 2 * Math.max(offset, 0.01f * Math.max(xmax - xmin, ymax - ymin));
		float a = (x - xcenter) / size;
		float b = (y - ycenter) / size;
		return a * a + b * b <= 1;
	}

	public void setCharge(float charge) {
		this.charge = charge;
	}

	public float getCharge() {
		return charge;
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}

	public float getOffset() {
		return offset;
	}

	public String toXml() {
		String xml = "<ionic charge=\"" + charge;
		if (imaginary)
			xml += "\" imaginary=\"" + imaginary;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
		xml += "\" offset=\"" + offset;
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
