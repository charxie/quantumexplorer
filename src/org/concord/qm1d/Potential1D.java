package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
public abstract class Potential1D {

	protected double xmin = -10;
	protected double xmax = 10;
	protected double[] pot;

	public Potential1D(int n, double xmin, double xmax) {
		pot = new double[n];
		this.xmin = xmin;
		this.xmax = xmax;
	}

	public double[] getPotential() {
		return pot;
	}

	public abstract String getName();

	public double getLowerBound() {
		return xmin;
	}

	public double getUpperBound() {
		return xmax;
	}

}
