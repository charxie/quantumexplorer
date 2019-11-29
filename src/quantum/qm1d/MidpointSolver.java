package quantum.qm1d;

import quantum.math.DoubleComplex;
import quantum.qmshared.Particle;

/**
 * 
 * @author Charles Xie
 * 
 */
class MidpointSolver extends RealTimePropagator1D {

	private final static DoubleComplex HALF = new DoubleComplex(0.5, 0.0);

	private DoubleComplex[] k1;
	private DoubleComplex z1, z2;

	MidpointSolver(Particle particle, int dimension) {
		super(particle, dimension);
		k1 = new DoubleComplex[n];
		setTimeStep(0.002);
	}

	void nextStep() {

		super.nextStep();

		for (int i = 0; i < n; i++) {
			k1[i] = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z1 = h[i][j];
				if (z1.absSquare() > 0) {
					z1 = z1.multiply(psi[j]);
					k1[i] = k1[i].add(z1);
				}
			}
			z1 = HALF.multiply(k1[i]);
			k1[i] = psi[i].add(z1);
		}

		for (int i = 0; i < n; i++) {
			z2 = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z1 = h[i][j];
				if (z1.absSquare() > 0) {
					z1 = z1.multiply(k1[j]);
					z2 = z2.add(z1);
				}
			}
			psi[i] = psi[i].add(z2);
		}

		iStep++;

		if (iStep % OUTPUT_INTERVAL == 0) {
			outputProperties();
		}

	}

}
