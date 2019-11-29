package quantum.qm1d;

import quantum.math.DoubleComplex;
import quantum.math.Tdma;
import quantum.qmshared.Particle;

/**
 * This is a fast and stable (norm-preserving) solver based on Cayley's form.
 * 
 * @author Charles Xie
 * 
 */
class CayleySolver1D extends RealTimePropagator1D {

	private DoubleComplex[] a, b, c, d;

	CayleySolver1D(Particle particle, int dimension) {
		super(particle, dimension);
		a = new DoubleComplex[n];
		b = new DoubleComplex[n];
		c = new DoubleComplex[n];
		d = new DoubleComplex[n];
		setTimeStep(.01);
		a[0] = new DoubleComplex();
		c[n - 1] = new DoubleComplex();
	}

	void rotatePhase() {
		double ef = eField != null ? particle.getCharge() * eField.getValue(timeStep * iStep) : 0;
		double angle;
		for (int i = 0; i < n; i++) {
			angle = clampPotential(potential.pot[i]);
			if (ef != 0)
				angle += ef * (i - n / 2);
			angle *= 0.5 * timeStep;
			psi[i] = psi[i].multiply(new DoubleComplex(Math.cos(angle), -Math.sin(angle)));
		}
	}

	void nextStep() {

		super.nextStep();

		// rotatePhase();

		for (int i = 0; i < n; i++) {

			// compute the rhs vector
			d[i] = psi[i].add(h[i][i].multiply(psi[i]));

			if (i > 0) {
				// compute the subdiagonal elements
				a[i] = h[i - 1][i].negative();
				d[i] = d[i].add(h[i - 1][i].multiply(psi[i - 1]));
			}

			// compute the diagonal elements
			b[i] = new DoubleComplex(1, 0).subtract(h[i][i]);

			if (i < n - 1) {
				// compute the superdiagonal elements
				c[i] = h[i + 1][i].negative();
				d[i] = d[i].add(h[i + 1][i].multiply(psi[i + 1]));
			}

		}

		psi = Tdma.solve(a, b, c, d);

		// rotatePhase();

		iStep++;

		if (iStep % OUTPUT_INTERVAL == 0) {
			outputProperties();
		}

	}

}
