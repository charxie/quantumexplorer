package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class CoulombWell extends Potential1D {

	private double k = 1;
	private double offset = -1;

	CoulombWell(int n, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double center = (xmax + xmin) / 2;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			pot[i] = -k / Math.abs(x - center);
			if (pot[i] < offset)
				pot[i] = offset;
		}
	}

	@Override
	public String getName() {
		return "Coulomb Well";
	}

}
