package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class QuarticDoubleWell extends Potential1D {

	private double a1 = -2;
	private double a2 = 2;
	private double v0 = 0.5;

	QuarticDoubleWell(int n, double ansymmetry, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double center = (xmax + xmin) / 2;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta - center;
			if (x < center) {
				pot[i] = v0 * (x * x - a1 * a1) * (x * x - a1 * a1) / (a1 * a1 * a1 * a1);
			} else {
				pot[i] = v0 * (x * x - a2 * a2) * (x * x - a2 * a2) / (a2 * a2 * a2 * a2);
			}
			pot[i] += ansymmetry * x;
		}
	}

	@Override
	public String getName() {
		return "Quartic Double Well";
	}

}
