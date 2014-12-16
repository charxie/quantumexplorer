package org.concord.qm2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingAnnulus implements MovingShape {

	private Ellipse2D.Float outer, inner;

	MovingAnnulus(Ellipse2D.Float outer, Ellipse2D.Float inner) {
		super();
		this.inner = inner;
		this.outer = outer;
	}

	void setOuter(Ellipse2D.Float outer) {
		this.outer = outer;
	}

	void setInner(Ellipse2D.Float inner) {
		this.inner = inner;
	}

	public Shape[] getShapes() {
		return new Shape[] { outer, inner };
	}

	public void render(Graphics2D g) {
		g.draw(inner);
		g.draw(outer);
	}

	@Override
	public String toString() {
		return outer.getBounds() + ", " + inner.getBounds();
	}

}