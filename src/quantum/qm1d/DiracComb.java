package quantum.qm1d;

/**
 * @author Charles Xie
 * 
 */
class DiracComb extends Potential1D {

	private double depth = -1;

	DiracComb(int n, int count, double separation, double xmin, double xmax) {
		super(n, xmin, xmax);
		double length = xmax - xmin;
		double delta = length / n;
		double offset = 0.5 * (length - separation * count);
		int interval = (int) (separation / delta);
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			if (x < xmin + offset) {
				pot[i] = 0;
			} else if (x > xmax - offset) {
				pot[i] = 0;
			} else {
				pot[i] = i % interval == 0 ? depth : 0;
			}
		}
	}

	@Override
	public String getName() {
		return "Dirac Comb";
	}

}
