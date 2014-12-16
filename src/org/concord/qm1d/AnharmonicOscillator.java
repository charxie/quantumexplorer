package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class AnharmonicOscillator extends Potential1D {

	private double k = .01;
	private double offset = -1;

	AnharmonicOscillator(int n, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double center = (xmax + xmin) / 2;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			pot[i] = (x < center ? 10 * k : k) * (x - center) * (x - center) + offset;
		}
	}

	@Override
	public String getName() {
		return "Anharmonic Oscillator";
	}

}
