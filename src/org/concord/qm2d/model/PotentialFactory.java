package org.concord.qm2d.model;

import java.awt.Color;

import org.concord.qmshared.Particle;

/**
 * @author Charles Xie
 * 
 */
public class PotentialFactory {

	private int nx, ny;
	private float xmin, xmax, ymin, ymax;
	private Particle particle;

	public PotentialFactory(int nx, int ny, float xmin, float xmax, float ymin, float ymax, Particle particle) {
		set(nx, ny, xmin, xmax, ymin, ymax);
		this.particle = particle;
	}

	public void set(int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		this.nx = nx;
		this.ny = ny;
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public Potential2D copy(Potential2D p) {
		if (p instanceof IonicPotential) {
			IonicPotential q = (IonicPotential) p;
			return createIonicPotential(q.isImaginary(), particle.getCharge(), q.getCharge(), q.getOffset(), q.getXcenter(), q.getYcenter(), q.getColor(), q.isVisible(), q.isDraggable());
		} else if (p instanceof EllipticalPotential) {
			EllipticalPotential e = (EllipticalPotential) p;
			return createEllipticalPotentialArea(e.isImaginary(), e.getEnergy(), e.getXcenter(), e.getYcenter(), e.getRx(), e.getRy(), e.getColor(), e.isVisible(), e.isDraggable());
		} else if (p instanceof RectangularPotential) {
			RectangularPotential r = (RectangularPotential) p;
			return createRectangularPotentialArea(r.isImaginary(), r.getEnergy(), r.getXcenter(), r.getYcenter(), r.getLx(), r.getLy(), r.getCornerRadius(), r.getColor(), r.isVisible(), r.isDraggable());
		} else if (p instanceof AnnularPotential) {
			AnnularPotential a = (AnnularPotential) p;
			return createAnnularPotentialArea(a.isImaginary(), a.getEnergy(), a.getXcenter(), a.getYcenter(), a.getOuterRx(), a.getOuterRy(), a.getInnerRx(), a.getInnerRy(), a.getColor(), a.isVisible(), a.isDraggable());
		}
		return p;
	}

	public IonicPotential createIonicPotential(boolean imaginary, float e, float charge, float offset, float xcenter, float ycenter, Color color, boolean visible, boolean draggable) {
		IonicPotential p = new IonicPotential(imaginary, e, charge, offset, xcenter, ycenter, nx, ny, xmin, xmax, ymin, ymax);
		if (color != null)
			p.setColor(color);
		p.setVisible(visible);
		p.setDraggable(draggable);
		return p;
	}

	public RectangularPotential createRectangularPotentialArea(boolean imaginary, float energy, float xcenter, float ycenter, float xlength, float ylength, float cornerRadius, Color color, boolean visible, boolean draggable) {
		RectangularPotential p = new RectangularPotential(imaginary, energy, xcenter, ycenter, xlength, ylength, cornerRadius, nx, ny, xmin, xmax, ymin, ymax);
		if (color != null)
			p.setColor(color);
		p.setVisible(visible);
		p.setDraggable(draggable);
		return p;
	}

	public EllipticalPotential createEllipticalPotentialArea(boolean imaginary, float energy, float xcenter, float ycenter, float rx, float ry, Color color, boolean visible, boolean draggable) {
		EllipticalPotential p = new EllipticalPotential(imaginary, energy, xcenter, ycenter, rx, ry, nx, ny, xmin, xmax, ymin, ymax);
		if (color != null)
			p.setColor(color);
		p.setVisible(visible);
		p.setDraggable(draggable);
		return p;
	}

	public AnnularPotential createAnnularPotentialArea(boolean imaginary, float energy, float xcenter, float ycenter, float outerRx, float outerRy, float innerRx, float innerRy, Color color, boolean visible, boolean draggable) {
		AnnularPotential p = new AnnularPotential(imaginary, energy, xcenter, ycenter, outerRx, outerRy, innerRx, innerRy, nx, ny, xmin, xmax, ymin, ymax);
		if (color != null)
			p.setColor(color);
		p.setVisible(visible);
		p.setDraggable(draggable);
		return p;
	}

}
