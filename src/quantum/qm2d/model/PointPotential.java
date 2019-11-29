package quantum.qm2d.model;

import java.awt.Dimension;
import java.awt.geom.Ellipse2D;

import quantum.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
public abstract class PointPotential extends Potential2D {

	public PointPotential(boolean imaginary, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, nx, ny, xmin, xmax, ymin, ymax);
	}

	public abstract void setHalo(Ellipse2D.Float halo, Dimension d, Boundary bx, Boundary by);

}
