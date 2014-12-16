package org.concord.qm2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * @author Charles Xie
 * 
 */
class MovingEllipse implements MovingShape {

	private Ellipse2D.Float ellipse;

	MovingEllipse(Ellipse2D.Float ellipse) {
		this.ellipse = ellipse;
	}

	public Shape[] getShapes() {
		return new Shape[] { ellipse };
	}

	public void render(Graphics2D g) {
		g.draw(ellipse);
	}

}
