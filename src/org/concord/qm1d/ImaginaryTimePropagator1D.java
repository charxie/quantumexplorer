package org.concord.qm1d;

import static org.concord.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static org.concord.qmshared.Constants.MASS_UNIT_CONVERTER;

import org.concord.qmshared.Particle;

/**
 * 
 * @author Charles Xie
 * 
 */
class ImaginaryTimePropagator1D extends TimePropagator1D {

	double[][] h;
	double[] psi, psi2;

	ImaginaryTimePropagator1D(Particle particle, int dimension) {
		n = dimension;
		h = new double[n][n];
		psi = new double[n];
		psi2 = new double[n];
		amplitude = new double[n];
		coordinate = new double[n];
		for (int i = 0; i < n; i++)
			coordinate[i] = i;
		this.particle = particle;
	}

	/* Discretize the Hamiltonian into a matrix using the finite-difference method. */
	void generateHamiltonianMatrix() {
		double p = potential.xmax - potential.xmin;
		delta = p / n;
		double a = 0.5 / (delta * delta * particle.getMass() * MASS_UNIT_CONVERTER);
		double ef = eField != null ? particle.getCharge() * eField.getValue(timeStep * iStep) : 0;
		for (int i = 0; i < n; i++) {
			p = 2 * a + clampPotential(potential.pot[i]);
			if (ef != 0) {
				p += ef * (i - n / 2) * ENERGY_UNIT_CONVERTER;
			}
			h[i][i] = p;
			if (iStep < 1) {
				for (int j = i + 1; j < n; j++) {
					h[i][j] = h[j][i] = j == i + 1 ? -a : 0;
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
			psi[i] = savedEigenvector[i];
		}
		initPsi();
	}

	void initPsi() {
		if (initState < 0) { // if no initial state is set, use a Gaussian
			double k;
			for (int i = 0; i < n; i++) {
				k = potential.xmin + i * delta;
				psi[i] = Math.exp(-(k - mu) * (k - mu) / (sigma * sigma));
			}
			normalizePsi();
		} else {
			if (savedEigenvector != null) {
				for (int i = 0; i < n; i++) {
					psi[i] = savedEigenvector[i];
				}
			}
		}
		for (int i = 0; i < n; i++) {
			amplitude[i] = psi[i] * psi[i];
		}
	}

	void normalizePsi() {
		sum = 0;
		for (int i = 0; i < n; i++) {
			sum += psi[i] * psi[i];
		}
		sum = 1.0 / Math.sqrt(sum);
		for (int i = 0; i < n; i++) {
			psi[i] *= sum;
		}
	}

	double[] getAmplitude() {
		return amplitude;
	}

	void nextStep() {

		generateHamiltonianMatrix();

		for (int i = 0; i < n; i++) {
			psi2[i] = 0;
			for (int j = 0; j < n; j++) {
				psi2[i] += timeStep * h[i][j] * psi[j];
			}
		}

		for (int i = 0; i < n; i++) {
			psi[i] -= psi2[i];
		}

		normalizePsi();

		iStep++;

		if (iStep % OUTPUT_INTERVAL == 0) {
			outputProperties();
		}

	}

	void calculateMomentum() {
		momentum = 0;
		double delta2 = delta * 2;
		// must use central difference, or we will have a non-zero imaginary component for momentum
		double z;
		for (int i = 1; i < n - 1; i++) {
			z = (psi[i + 1] - psi[i - 1]) / delta2;
			momentum += psi[i] * z;
		}
	}

	void calculateKineticEnergy() {
		kinE = 0;
		double z;
		for (int i = 1; i < n - 1; i++) {
			z = psi[i + 1] - 2 * psi[i] + psi[i - 1];
			kinE += psi[i] * z;
		}
		double delta2 = -2 * particle.getMass() * delta * delta;
		kinE /= (delta2 * MASS_UNIT_CONVERTER * ENERGY_UNIT_CONVERTER);
	}

	double calculateExpectation(double[] prop) {
		double result = 0;
		for (int i = 0; i < n; i++) {
			result += psi[i] * prop[i] * psi[i];
		}
		return result;
	}

	void outputProperties() {
		for (int i = 0; i < n; i++) {
			amplitude[i] = psi[i] * psi[i];
		}
		position = calculateExpectation(coordinate);
		calculateMomentum();
		calculateKineticEnergy();
		potE = calculateExpectation(getPotential());
		totE = kinE + potE;
		requestVisualization();
		if (MainWindow.logLevel == 1) {
			if (iStep % 1000 == 0) {
				System.out.printf(">>> %5.0f = %10.5f, %10.5f, %10.5f, %10.5f\n", iStep * timeStep,
						sum, totE, potE, kinE);
			}
		}
	}

}
