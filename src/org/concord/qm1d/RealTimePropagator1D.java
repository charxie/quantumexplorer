package org.concord.qm1d;

import static org.concord.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static org.concord.qmshared.Constants.MASS_UNIT_CONVERTER;

import org.concord.math.DoubleComplex;
import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Particle;

/**
 * @author Charles Xie
 * 
 */
abstract class RealTimePropagator1D extends TimePropagator1D {

	DoubleComplex[][] h;
	DoubleComplex[] psi;

	private double p0;

	// Schrodinger-Langevin-Kostin dissipation
	private double slkFriction;
	private double[] phase, foldedPhase;

	RealTimePropagator1D(Particle particle, int dimension) {
		n = dimension;
		h = new DoubleComplex[n][n];
		amplitude = new double[n];
		psi = new DoubleComplex[n];
		coordinate = new double[n];
		for (int i = 0; i < n; i++)
			coordinate[i] = i;
		this.particle = particle;
	}

	void setSlkFriction(double slkFriction) {
		this.slkFriction = slkFriction;
		if (Math.abs(slkFriction) > 0) {
			if (phase == null)
				phase = new double[n];
			if (foldedPhase == null)
				foldedPhase = new double[n];
		}
	}

	void heat(double ratio) {
		double k;
		for (int i = 0; i < n; i++) {
			k = ratio * momentum * (potential.xmin + i * delta);
			psi[i] = psi[i].multiply(new DoubleComplex(Math.cos(k), Math.sin(k)));
		}
	}

	/*
	 * Ensure that the phase changes continuous, instead of being folded to [-pi, pi) by the atan(y, x) function.
	 */
	private void calculatePhase(int i) {
		double newPhase = psi[i].arg();
		double phaseChange = newPhase - foldedPhase[i];
		if (phaseChange > Math.PI) {
			phaseChange -= 2 * Math.PI;
		} else if (phaseChange < -Math.PI) {
			phaseChange += 2 * Math.PI;
		}
		phase[i] += phaseChange;
		foldedPhase[i] = newPhase;
	}

	/* Discretize the Hamiltonian into a matrix using the finite-difference method. */
	void generateHamiltonianMatrix() {
		double p = potential.xmax - potential.xmin;
		delta = p / n;
		double a = 0.5 / (delta * delta * particle.getMass() * MASS_UNIT_CONVERTER);
		double ef = eField != null ? particle.getCharge() * eField.getValue(timeStep * iStep) : 0;
		boolean slk = Math.abs(slkFriction) > 0 && psi[0] != null;
		double ft = 0;
		if (slk)
			ft = calculateExpectation(phase);
		for (int i = 0; i < n; i++) {
			p = 2 * a + clampPotential(potential.pot[i]);
			if (ef != 0) {
				p += ef * (i - n / 2) * ENERGY_UNIT_CONVERTER;
			}
			if (slk) {
				calculatePhase(i);
				p += slkFriction * (phase[i] - ft) / (particle.getMass() * MASS_UNIT_CONVERTER);
			}
			if (boundary == null) {
				h[i][i] = new DoubleComplex(0, -timeStep * p);
			} else if (boundary instanceof AbsorbingBoundary) {
				AbsorbingBoundary ab = (AbsorbingBoundary) boundary;
				int lg = (int) (ab.getLengthPercentage() * n);
				if (i < lg) {
					h[i][i] = new DoubleComplex(-ab.getAbsorption() * (lg - i), -timeStep);
				} else if (i > n - lg) {
					h[i][i] = new DoubleComplex(-ab.getAbsorption() * (lg - n + i), -timeStep * p);
				} else {
					h[i][i] = new DoubleComplex(0, -timeStep * p);
				}
			}
			if (iStep < 1) {
				for (int j = i + 1; j < n; j++) {
					h[i][j] = h[j][i] = new DoubleComplex(0, j == i + 1 ? timeStep * a : 0);
				}
			}
		}
	}

	void setInitialState(int initState, double[][] eigenVector) {
		this.initState = initState;
		if (savedEigenvector == null)
			savedEigenvector = new double[n];
		for (int i = 0; i < n; i++) {
			savedEigenvector[i] = eigenVector[initState][i];
			psi[i] = new DoubleComplex(savedEigenvector[i], 0);
		}
		initPsi();
	}

	int getInitialState() {
		return initState;
	}

	void setInitialMomentum(double p0) {
		this.p0 = p0;
		initPsi();
	}

	void initPsi() {
		if (initState < 0) { // if no initial state is set, use a Gaussian
			double g;
			double k;
			for (int i = 0; i < n; i++) {
				k = potential.xmin + i * delta;
				g = Math.exp(-(k - mu) * (k - mu) / (sigma * sigma));
				k = p0 * k;
				psi[i] = new DoubleComplex(g * Math.cos(k), g * Math.sin(k));
			}
			normalizePsi();
		} else {
			if (savedEigenvector != null) {
				double k, g;
				for (int i = 0; i < n; i++) {
					k = p0 * (potential.xmin + i * delta);
					g = savedEigenvector[i];
					psi[i] = new DoubleComplex(g * Math.cos(k), g * Math.sin(k));
				}
			}
		}
		for (int i = 0; i < n; i++) {
			amplitude[i] = psi[i].absSquare();
		}
		if (Math.abs(slkFriction) > 0) {
			for (int i = 0; i < n; i++) {
				phase[i] = psi[i].arg();
				foldedPhase[i] = phase[i];
			}
		}
	}

	void normalizePsi() {
		sum = 0;
		for (int i = 0; i < n; i++)
			sum += psi[i].absSquare();
		sum = 1.0 / Math.sqrt(sum);
		for (int i = 0; i < n; i++)
			psi[i] = new DoubleComplex(psi[i].real() * sum, psi[i].imag() * sum);
	}

	double[] getAmplitude() {
		return amplitude;

	}

	void nextStep() {
		generateHamiltonianMatrix();
	}

	void calculateMomentum() {
		DoubleComplex m = new DoubleComplex();
		double delta2 = delta * 2;
		// must use central difference, or we will have a non-zero imaginary component for momentum
		DoubleComplex z;
		for (int i = 1; i < n - 1; i++) {
			z = new DoubleComplex((psi[i + 1].real() - psi[i - 1].real()) / delta2, (psi[i + 1].imag() - psi[i - 1].imag()) / delta2);
			m = m.add(psi[i].conjugate().multiply(z));
		}
		momentum = m.imag();
	}

	void calculateKineticEnergy() {
		kinE = 0;
		DoubleComplex ke = new DoubleComplex();
		DoubleComplex z;
		for (int i = 1; i < n - 1; i++) {
			z = new DoubleComplex(psi[i + 1].real() - 2 * psi[i].real() + psi[i - 1].real(), psi[i + 1].imag() - 2 * psi[i].imag() + psi[i - 1].imag());
			ke = ke.add(psi[i].conjugate().multiply(z));
		}
		double delta2 = -2 * particle.getMass() * delta * delta;
		kinE = ke.real() / (delta2 * MASS_UNIT_CONVERTER * ENERGY_UNIT_CONVERTER);
	}

	double calculateExpectation(double[] prop) {
		double result = 0;
		DoubleComplex z;
		for (int i = 0; i < n; i++) {
			z = psi[i].conjugate().multiply(new DoubleComplex(prop[i] * psi[i].real(), prop[i] * psi[i].imag()));
			result += z.real();
		}
		return result;
	}

	void outputProperties() {
		sum = 0.0;
		for (int i = 0; i < n; i++) {
			amplitude[i] = psi[i].absSquare();
			sum += amplitude[i];
		}
		position = calculateExpectation(coordinate);
		calculateMomentum();
		calculateKineticEnergy();
		potE = calculateExpectation(getPotential());
		totE = kinE + potE;
		requestVisualization();
		if (MainWindow.logLevel == 1) {
			if (iStep % 1000 == 0) {
				System.out.printf(">>> %5.0f = %10.5f, %10.5f, %10.5f, %10.5f\n", iStep * timeStep, sum, totE, potE, kinE);
			}
		}
	}

}
