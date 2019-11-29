package quantum.qm1d;

/**
 * @author Charles Xie
 * 
 */
class SquareWell extends Potential1D {

	private double xcenter = 0;
	private double width = 10;

	SquareWell(int n, double depth, double height, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double x;
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			if (x < xcenter - 0.5 * width) {
				pot[i] = height;
			} else if (x > xcenter + 0.5 * width) {
				pot[i] = height;
			} else {
				pot[i] = depth;
			}
		}
	}

	@Override
	public String getName() {
		return "Square Well";
	}

}
