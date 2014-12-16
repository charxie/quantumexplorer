package org.concord.qm2d.view;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * @author Charles Xie
 * 
 */
interface MovingShape {

	public Shape[] getShapes();

	public void render(Graphics2D g);

}
