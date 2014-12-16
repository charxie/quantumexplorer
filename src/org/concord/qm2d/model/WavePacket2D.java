package org.concord.qm2d.model;

import org.concord.math.FloatComplex;

/**
 * @author Charles Xie
 * 
 */
public abstract class WavePacket2D {

	protected FloatComplex[][] psi;
	protected float magnitude = 1;
	protected float px;
	protected float py;

	public WavePacket2D(float magnitude, int nx, int ny) {
		this.magnitude = magnitude;
		psi = new FloatComplex[nx][ny];
	}

	public void setMagnitude(float magnitude) {
		this.magnitude = magnitude;
	}

	public float getMagnitude() {
		return magnitude;
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

	void destroy() {
		psi = null;
	}

	public abstract String toXml();

}
