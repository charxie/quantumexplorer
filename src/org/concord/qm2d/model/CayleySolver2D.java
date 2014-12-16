package org.concord.qm2d.model;

import org.concord.math.FloatComplex;
import org.concord.math.Tdma;

/**
 * This is a fast and stable (norm-preserving) solver based on Cayley's form.
 * 
 * @author Charles Xie
 * 
 */
class CayleySolver2D {

	private int n;
	private FloatComplex[] a, b, c, d;

	CayleySolver2D(int n) {
		this.n = n;
		a = new FloatComplex[n];
		b = new FloatComplex[n];
		c = new FloatComplex[n];
		d = new FloatComplex[n];
		a[0] = new FloatComplex();
		c[n - 1] = new FloatComplex();
	}

	FloatComplex[] nextStep(FloatComplex[] phi, FloatComplex[] subDiagonal, FloatComplex[] diagonal, FloatComplex[] supDiagonal) {

		for (int i = 0; i < n; i++) {

			// compute the rhs vector
			d[i] = phi[i].add(diagonal[i].multiply(phi[i]));

			if (i > 0) {
				// compute the subdiagonal elements
				a[i] = subDiagonal[i - 1].negative();
				d[i] = d[i].add(subDiagonal[i - 1].multiply(phi[i - 1]));
			}

			// compute the diagonal elements
			b[i] = new FloatComplex(1, 0).subtract(diagonal[i]);

			if (i < n - 1) {
				// compute the superdiagonal elements
				c[i] = supDiagonal[i + 1].negative();
				d[i] = d[i].add(supDiagonal[i + 1].multiply(phi[i + 1]));
			}

		}

		return Tdma.solve(a, b, c, d);

	}

}
