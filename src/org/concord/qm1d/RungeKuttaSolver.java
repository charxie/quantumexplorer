package org.concord.qm1d;

import org.concord.math.DoubleComplex;
import org.concord.qmshared.Particle;

/**
 * This is a solver of the time-dependence Schrodinger equation based on the Fourth-Order Runge-Kutta Method (not a fast method). It is a direct propogation method --- not based on solving the eigenvalues and eigenvectors.
 * 
 * @author Charles Xie
 * 
 */
class RungeKuttaSolver extends RealTimePropagator1D {

	private final static DoubleComplex HALF = new DoubleComplex(0.5, 0.0);
	private final static DoubleComplex TWO = new DoubleComplex(2.0, 0.0);
	private final static DoubleComplex ONE_SIXTH = new DoubleComplex(1.0 / 6.0, 0.0);

	private DoubleComplex[] f1, f2, f3, f4, temp;
	private DoubleComplex z, z2, z3;

	RungeKuttaSolver(Particle particle, int dimension) {
		super(particle, dimension);
		f1 = new DoubleComplex[n];
		f2 = new DoubleComplex[n];
		f3 = new DoubleComplex[n];
		f4 = new DoubleComplex[n];
		temp = new DoubleComplex[n];
		setTimeStep(0.01);
	}

	void nextStep() {

		super.nextStep();

		for (int i = 0; i < n; i++) {
			f1[i] = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z = h[i][j];
				if (z.absSquare() > 0) {
					z = z.multiply(psi[j]);
					f1[i] = f1[i].add(z);
				}
			}
		}
		for (int i = 0; i < n; i++) {
			z = HALF.multiply(f1[i]);
			temp[i] = psi[i].add(z);
		}

		for (int i = 0; i < n; i++) {
			f2[i] = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z = h[i][j];
				if (z.absSquare() > 0) {
					z = z.multiply(temp[j]);
					f2[i] = f2[i].add(z);
				}
			}
		}
		for (int i = 0; i < n; i++) {
			z = HALF.multiply(f2[i]);
			temp[i] = psi[i].add(z);
		}

		for (int i = 0; i < n; i++) {
			f3[i] = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z = h[i][j];
				if (z.absSquare() > 0) {
					z = z.multiply(temp[j]);
					f3[i] = f3[i].add(z);
				}
			}
		}
		for (int i = 0; i < n; i++) {
			temp[i] = psi[i].add(f3[i]);
		}

		for (int i = 0; i < n; i++) {
			f4[i] = new DoubleComplex();
			for (int j = 0; j < n; j++) {
				z = h[i][j];
				if (z.absSquare() > 0) {
					z = z.multiply(temp[j]);
					f4[i] = f4[i].add(z);
				}
			}
		}

		for (int i = 0; i < n; i++) {
			z2 = TWO.multiply(f2[i]);
			z3 = TWO.multiply(f3[i]);
			z = z2.add(z3);
			z2 = z.add(f1[i]);
			z3 = z2.add(f4[i]);
			z = ONE_SIXTH.multiply(z3);
			psi[i] = psi[i].add(z);
		}

		iStep++;

		if (iStep % OUTPUT_INTERVAL == 0) {
			outputProperties();
		}

	}

}
