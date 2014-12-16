package org.concord.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public class PlaneWaveSource extends Source {

	private float width, height;

	public PlaneWaveSource(float xcenter, float ycenter, float width, float height, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(nx, ny, xmin, xmax, ymin, ymax);
		setLocation(xcenter, ycenter);
		setWidth(width);
		setHeight(height);
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getWidth() {
		return width;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getHeight() {
		return height;
	}

	public boolean contains(float x, float y) {
		float xmin = xcenter - 0.5f * width;
		float xmax = xcenter + 0.5f * width;
		float ymin = ycenter - 0.5f * height;
		float ymax = ycenter + 0.5f * height;
		return x > xmin && x < xmax && y > ymin && y < ymax;
	}

	@Override
	public String toXml() {
		String xml = "<planewave period=\"" + period;
		xml += "\" amplitude=\"" + amplitude;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
		xml += "\" width=\"" + width;
		xml += "\" height=\"" + height;
		xml += "\" px=\"" + px;
		xml += "\" py=\"" + py;
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
