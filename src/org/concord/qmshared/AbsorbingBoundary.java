package org.concord.qmshared;

/**
 * @author Charles Xie
 * 
 */
public class AbsorbingBoundary implements Boundary {

	private float lengthPercentage = 0.1f;
	private float absorption = 0.001f;
	private char direction = 'x';

	public AbsorbingBoundary() {
	}

	public void setDirection(char direction) {
		this.direction = direction;
	}

	public char getDirection() {
		return direction;
	}

	public void setLengthPercentage(float lengthPercentage) {
		this.lengthPercentage = lengthPercentage;
	}

	public float getLengthPercentage() {
		return lengthPercentage;
	}

	public void setAbsorption(float absorption) {
		this.absorption = absorption;
	}

	public float getAbsorption() {
		return absorption;
	}

	public String toXml() {
		String xml = "<absorbing_boundary direction=\"" + direction + "\" length_percentage=\"" + lengthPercentage;
		xml += "\" absorption=\"" + absorption;
		xml += "\"/>\n";
		return xml;
	}

}
