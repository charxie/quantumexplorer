package org.concord.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public class PointSource extends Source {

	private float sigma = 1;

	public PointSource(float xcenter, float ycenter, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(nx, ny, xmin, xmax, ymin, ymax);
		setLocation(xcenter, ycenter);
	}

	public void setSigma(float sigma) {
		this.sigma = sigma;
	}

	public float getSigma() {
		return sigma;
	}

	public boolean contains(float x, float y) {
		float size = 0.02f * Math.max(xmax - xmin, ymax - ymin);
		float a = (x - xcenter) / size;
		float b = (y - ycenter) / size;
		return a * a + b * b <= 1;
	}

	@Override
	public String toXml() {
		String xml = "<point period=\"" + period;
		xml += "\" amplitude=\"" + amplitude;
		xml += "\" sigma=\"" + sigma;
		xml += "\" xcenter=\"" + xcenter;
		xml += "\" ycenter=\"" + ycenter;
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
