package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class MorseWell extends Potential1D {

	private double d = 4;
	private double alpha = 1;
	private double depth = -1;

	MorseWell(int n, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double center = (xmax + xmin) / 2;
		double x, y;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			y = (1 - Math.exp(-alpha * (x - center)));
			pot[i] = d * y * y + depth;
		}
	}

	@Override
	public String getName() {
		return "Morse Well";
	}

}
