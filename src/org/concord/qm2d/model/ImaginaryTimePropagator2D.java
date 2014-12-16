package org.concord.qm2d.model;

import static org.concord.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static org.concord.qmshared.Constants.MASS_UNIT_CONVERTER;

import java.util.Arrays;

import org.concord.math.FloatComplex;
import org.concord.qm2d.QuantumBox;
import org.concord.qmshared.Particle;

/**
 * This is an implementation of the imaginary time propagation method for finding the ground state of a system. The method converts the time-dependent Schrodinger equation from the real time space into the imaginary time space. Since the excited states have higher energy than the ground state, they die off more quickly than the ground state. Therefore, the wave function converges to the ground state after some time of propagation.
 * 
 * This method is unable to distinguish degenerate ground states. For example, the ground state of a square barrier is degenerate: one is symmetric and the other antisymmetric. This wave function this method will converge to depends on whether the initial expectation value of position is located. If a Gaussian wave packet is placed to the left of the barrier, the converged wave function will be the left part of the ground state wave function---the right part will vanish. If a Gaussian wave packet is placed to the right of the barrier, the converged wave function will be the right part of the ground state wave function---the left part will vanish. Only when a Gaussian wave packet is placed at exactly the center of the barrier will the converged wave function has an equal left and right part.
 * 
 * Degeneracy is mostly caused by structural symmetry. For non-symmetric systems, the method tends to be able to find the ground state rapidly.
 * 
 * @author Charles Xie
 * 
 */
public class ImaginaryTimePropagator2D extends TimePropagator2D {

	private float[] hxSubDiagonal, hxDiagonal, hxSupDiagonal;
	private float[] hySubDiagonal, hyDiagonal, hySupDiagonal;
	private float[][] psi;
	private float[] phix, phiy;

	public ImaginaryTimePropagator2D(Particle particle, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(particle, nx, ny, xmin, xmax, ymin, ymax);
		hxSubDiagonal = new float[nx];
		hxDiagonal = new float[nx];
		hxSupDiagonal = new float[nx];
		hySubDiagonal = new float[ny];
		hyDiagonal = new float[ny];
		hySupDiagonal = new float[ny];
		psi = new float[nx][ny];
		phix = new float[nx];
		phiy = new float[ny];
	}

	public void destroy() {
		lock.lock();
		try {
			super.destroy();
			phase = null;
			psi = null;
			hxSubDiagonal = null;
			hySubDiagonal = null;
			hxDiagonal = null;
			hyDiagonal = null;
			hxSupDiagonal = null;
			hySupDiagonal = null;
		} finally {
			lock.unlock();
		}
	}

	/*
	 * generate the tridiagonal matrix of the second order differential operator d^2/dx^2 for psi[.][j] (dimension=nx). The caller will iterate from 0 to ny-1 and obtain psi[.][.] (nx by ny).
	 */
	private void solveXTridiagMatrix(int j) {
		float p = timeStep * ax;
		if (iStep < 1) {
			Arrays.fill(hxSubDiagonal, -p);
			Arrays.fill(hxSupDiagonal, -p);
		}
		// when we split the d^2/dx^2 operator, we also split the potential energy operator
		// into half to go with it. The reason that it is half is because x and y direction
		// should be completely equivalent--we should not create a biased division for the
		// potential.
		float p2;
		float xi;
		float yj = ymin + j * deltaY;
		float time = timeStep * iStep;
		float bterm = 0;
		if (bField != null) {
			bterm = bField.getValue(time) * particle.getCharge();
			bterm *= bterm;
			bterm *= 0.125f / particle.getMass();
		}
		for (int i = 0; i < nx; i++) {
			p2 = staticRealPotential[i][j];
			if (eField != null) {
				xi = xmin + i * deltaX;
				p2 += particle.getCharge() * eField.getPotential(xi, yj, time);
			}
			if (bField != null) {
				xi = xmin + i * deltaX;
				p2 += bterm * xi * xi * ENERGY_UNIT_CONVERTER;
			}
			p2 *= 0.5f * timeStep; // 0.5 is due to potential splitting cited above
			p2 -= 2 * p;
			hxDiagonal[i] = p2;
			phix[i] = psi[i][j];
		}
		phix = nextStep(phix, hxSubDiagonal, hxDiagonal, hxSupDiagonal);
		for (int i = 0; i < nx; i++) {
			psi[i][j] += phix[i];
		}
	}

	private static float[] nextStep(float[] phi, float[] sub, float[] d, float[] sup) {
		int n = phi.length;
		float[] x = new float[n];
		x[0] = d[0] * phi[0] + sup[0] * phi[1];
		for (int k = 1; k < n - 1; k++) {
			x[k] = sub[k] * phi[k - 1] + d[k] * phi[k] + sup[k] * phi[k + 1];
		}
		x[n - 1] = sub[n - 1] * phi[n - 2] + d[n - 1] * phi[n - 1];
		return x;
	}

	/*
	 * generate the tridiagonal matrix of the second order differential operator d^2/dy^2 for psi[i][.] (dimension=ny). The caller will iterate from 0 to nx-1 and obtain psi[.][.] (nx by ny).
	 */
	private void solveYTridiagMatrix(int i) {
		float p = timeStep * ay;
		if (iStep < 1) {
			Arrays.fill(hySubDiagonal, -p);
			Arrays.fill(hySupDiagonal, -p);
		}
		// when we split the d^2/dy^2 operator, we also split the potential energy operator
		// into half to go with it. The reason that it is half is because x and y direction
		// should be completely equivalent--we should not create a biased division for the
		// potential.
		float p2;
		float xi = xmin + i * deltaX;
		float yj;
		float time = timeStep * iStep;
		float bterm = 0;
		if (bField != null) {
			bterm = bField.getValue(time) * particle.getCharge();
			bterm *= bterm;
			bterm *= 0.125f / particle.getMass();
		}
		for (int j = 0; j < ny; j++) {
			p2 = staticRealPotential[i][j];
			if (eField != null) {
				yj = ymin + j * deltaY;
				p2 += particle.getCharge() * eField.getPotential(xi, yj, time);
			}
			if (bField != null) {
				yj = ymin + j * deltaY;
				p2 += bterm * yj * yj * ENERGY_UNIT_CONVERTER;
			}
			p2 *= 0.5f * timeStep; // 0.5 is due to potential splitting cited above
			p2 -= 2 * p;
			hyDiagonal[j] = p2;
			phiy[j] = psi[i][j];
		}
		phiy = nextStep(phiy, hySubDiagonal, hyDiagonal, hySupDiagonal);
		for (int j = 0; j < ny; j++) {
			psi[i][j] += phiy[j];
		}
	}

	void clearWaveFunction() {
		for (int i = 0; i < nx; i++) {
			Arrays.fill(psi[i], 0);
		}
	}

	void initPsi() {
		if (psi == null)
			return;
		normalizePsi();
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				amplitude[i][j] = psi[i][j] * psi[i][j];
			}
		}
		requestVisualization();
	}

	void normalizePsi() {
		sum = 0;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				sum += psi[i][j] * psi[i][j];
			}
		}
		sum = (float) (1.0 / Math.sqrt(sum));
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				psi[i][j] *= sum;
			}
		}
	}

	void addWaveFunction(FloatComplex[][] wf) {
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				psi[i][j] += wf[i][j].real();
			}
		}
	}

	void nextStep() {
		lock.lock();
		try {
			for (int j = 0; j < ny; j++) {
				solveXTridiagMatrix(j);
			}
			for (int i = 0; i < nx; i++) {
				solveYTridiagMatrix(i);
			}
			normalizePsi();
			if (iStep % OUTPUT_INTERVAL == 0) {
				outputProperties();
			}
			iStep++;
		} finally {
			lock.unlock();
		}
	}

	void outputProperties() {
		sum = 0;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				amplitude[i][j] = psi[i][j] * psi[i][j];
				sum += amplitude[i][j];
			}
		}
		if (xBoundary == null && yBoundary == null) {
			if (computeExpectation) {
				calculatePosition();
				calculateMomentum();
			}
			if (computeEnergy) {
				calculateKineticEnergy();
				calculatePotentialEnergy();
				totE = kinE + potE;
			}
		}
		requestVisualization();
		if (QuantumBox.getLogLevel() == 1) {
			if (iStep % 20 == 0) {
				System.out.printf(">>> %5.0f = %10.5f, %10.5f, %10.5f, %10.5f, %10.5f, %10.5f, %10.5f, %10.5f\n", iStep * timeStep, sum, totE, kinE, potE, position.x, position.y, momentum.x, momentum.y);
			}
		}
	}

	void calculatePosition() {
		position.x = position.y = 0;
		float c;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				c = psi[i][j];
				position.x += c * c * i;
				position.y += c * c * j;
			}
		}
		position.x = xmin + deltaX * position.x;
		position.y = ymin + deltaY * position.y;
	}

	void calculateMomentum() {
	}

	void resetCurrent() {

	}

	void calculateCurrent() {
	}

	void calculatePotentialEnergy() {
		potE = 0;
		float p;
		float time = timeStep * iStep;
		float x, y;
		for (int i = 0; i < nx; i++) {
			x = xmin + deltaX * i;
			for (int j = 0; j < ny; j++) {
				p = staticRealPotential[i][j];
				if (eField != null) {
					y = ymin + deltaY * j;
					p += particle.getCharge() * eField.getPotential(x, y, time);
				}
				if (bField != null) {
					y = ymin + deltaY * j;
					p += 0.125f * particle.getCharge() * particle.getCharge() * bField.getValue(time) * bField.getValue(time) * (x * x + y * y) / particle.getMass();
				}
				potE += psi[i][j] * p * psi[i][j];
			}
		}
	}

	void calculateKineticEnergy() {
		kinE = 0;
		float kex = 0;
		for (int j = 0; j < ny; j++) {
			for (int i = 1; i < nx - 1; i++) {
				kex += psi[i][j] * (psi[i + 1][j] - 2 * psi[i][j] + psi[i - 1][j]);
			}
		}
		float key = 0;
		for (int i = 0; i < nx; i++) {
			for (int j = 1; j < ny - 1; j++) {
				key += psi[i][j] * ((psi[i][j + 1] - 2 * psi[i][j] + psi[i][j - 1]));
			}
		}
		float k = -2 * particle.getMass() * MASS_UNIT_CONVERTER * ENERGY_UNIT_CONVERTER;
		kinE = kex / (deltaX * deltaX) + key / (deltaY * deltaY);
		kinE /= k;
	}

}
