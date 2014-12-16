package org.concord.qm2d.view;

import javax.swing.ImageIcon;

import org.concord.qm2d.model.Shape2D;

/**
 * @author Charles Xie
 * 
 */
public class Picture {

	private ImageIcon image;
	private float x = 0, y = 0;
	private Shape2D host;

	public Picture(ImageIcon image, float x, float y) {
		setImage(image);
		setLocation(x, y);
	}

	public void setHost(Shape2D host) {
		this.host = host;
	}

	public Shape2D getHost() {
		return host;
	}

	public int getWidth() {
		return image.getIconWidth();
	}

	public int getHeight() {
		return image.getIconHeight();
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	public void setImage(ImageIcon image) {
		this.image = image;
	}

	public ImageIcon getImage() {
		return image;
	}

}
