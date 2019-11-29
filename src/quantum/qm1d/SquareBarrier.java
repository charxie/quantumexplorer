package quantum.qm1d;

/**
 * @author Charles Xie
 * 
 */
class SquareBarrier extends Potential1D {

	private double xcenter = 0;
	private double width = 1;
	private double depth = 0.1;

	SquareBarrier(int n, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			if (x < xcenter - 0.5 * width) {
				pot[i] = -depth;
			} else if (x > xcenter + 0.5 * width) {
				pot[i] = -depth;
			} else {
				pot[i] = 0;
			}
		}
	}

	@Override
	public String getName() {
		return "Square Barrier";
	}

}
