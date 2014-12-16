package org.concord.qm2d.model;

import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;

import org.concord.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
public abstract class AreaPotential extends Potential2D {

	protected float energy;

	public AreaPotential(boolean imaginary, float energy, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(imaginary, nx, ny, xmin, xmax, ymin, ymax);
		this.energy = energy;
	}

	public abstract void setRects(Rectangle[] rect, Polygon[] handle, Dimension dim, Boundary xBoundary, Boundary yBoundary);

	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public float getEnergy() {
		return energy;
	}

}
