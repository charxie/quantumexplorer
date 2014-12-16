package org.concord.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public abstract class Source extends Shape2D {

	protected float period = 5;
	protected float amplitude = .1f;
	protected float px, py;

	public Source(int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(xmin, xmax, ymin, ymax);
	}

	public void setLocation(float x, float y) {
		this.xcenter = x;
		this.ycenter = y;
	}

	public void setPx(float px) {
		this.px = px;
	}

	public float getPx() {
		return px;
	}

	public void setPy(float py) {
		this.py = py;
	}

	public float getPy() {
		return py;
	}

	public void setPeriod(float perioid) {
		this.period = perioid;
	}

	public float getPeriod() {
		return period;
	}

	public void setAmplitude(float amplitude) {
		this.amplitude = amplitude;
	}

	public float getAmplitude() {
		return amplitude;
	}

}
