package quantum.qm1d;

/**
 * @author Charles Xie
 * 
 */
class BellBarrier extends Potential1D {

	private double xcenter = 0;
	private double width = 2;
	private double depth = 0.01;

	BellBarrier(int n, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta - xcenter;
			pot[i] = Math.exp(-x * x / (width * width));
			pot[i] = depth * (pot[i] - 1);
		}
	}

	@Override
	public String getName() {
		return "Bell Barrier";
	}

}
