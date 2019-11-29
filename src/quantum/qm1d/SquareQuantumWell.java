package quantum.qm1d;

/**
 * @author Charles Xie
 * 
 */
class SquareQuantumWell extends Potential1D {

	private double xcenter = 0;

	SquareQuantumWell(int n, double field, double barrierWidth, double wellWidth, double barrierHeight, double wellDepth, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double x;
		double center = 0.5 * (xmin + xmax);
		double w2 = 0.5 * (barrierWidth + wellWidth);
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			if (x > xcenter - 0.5 * wellWidth && x < xcenter + 0.5 * wellWidth) {
				pot[i] = wellDepth + field * (x - center);
			} else if (x > xcenter - w2 && x < xcenter + w2) {
				pot[i] = barrierHeight + field * (x - center);
			} else {
				pot[i] = 0;
			}
		}
	}

	@Override
	public String getName() {
		return "Square Quantum Well";
	}

}
