package org.concord.qm2d.model;

import static org.concord.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static org.concord.qmshared.Constants.MASS_UNIT_CONVERTER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.concord.math.FloatComplex;
import org.concord.math.Vector2D;
import org.concord.qm2d.QuantumBox;
import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Particle;

/**
 * The Crank-Nicolson algorithm (or Cayley's form as preferred by physicists) used in this finite-difference time-domain (FDTD) solver preserves the norm and conserves the energy if there is no potential. However, if there is a potential, the total energy (the kinetic energy plus the potential energy) is not strictly conserved any more (which is caused by the split of the operators). This is, however, not a serious problem because the calculation is stable and the total energy fluctuates around some value. This may be a reasonable cost for the high speed of this algorithm.
 * 
 * The time evolution of a particle in a box is found to be identical to the result obtained using the spectral method of Paul Falstad, which needs to find the eigenvalues and eigenvectors.
 * 
 * @author Charles Xie
 * 
 */
public class RealTimePropagator2D extends TimePropagator2D {

	private FloatComplex[] hxSubDiagonal, hxDiagonal, hxSupDiagonal;
	private FloatComplex[] hySubDiagonal, hyDiagonal, hySupDiagonal;
	private FloatComplex[][] psi;
	private FloatComplex[] phix, phiy;
	private CayleySolver2D csx, csy;
	private int currentSteps = 4;
	private List<Source> sources;

	/*
	 * use Suzuki's exponential product or not: psi(r, t+dt)=exp(-idtV/2) exp[idt(Dxx+Dyy)/2] exp(-idtV/2)
	 */
	private boolean suzukiFlag;

	public RealTimePropagator2D(Particle particle, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(particle, nx, ny, xmin, xmax, ymin, ymax);
		hxDiagonal = new FloatComplex[nx];
		hyDiagonal = new FloatComplex[ny];
		hxSubDiagonal = new FloatComplex[nx];
		hySubDiagonal = new FloatComplex[ny];
		hxSupDiagonal = new FloatComplex[nx];
		hySupDiagonal = new FloatComplex[ny];
		psi = new FloatComplex[nx][ny];
		phix = new FloatComplex[nx];
		phiy = new FloatComplex[ny];
		csx = new CayleySolver2D(nx);
		csy = new CayleySolver2D(ny);
		sources = Collections.synchronizedList(new ArrayList<Source>());
	}

	public void addSource(Source s) {
		sources.add(s);
	}

	public void removeSource(Source s) {
		sources.remove(s);
	}

	public Source getSource(int index) {
		if (index < 0 || index > sources.size())
			return null;
		return sources.get(index);
	}

	public int getSourceCount() {
		return sources.size();
	}

	void removeAllSources() {
		sources.clear();
	}

	private void initCurrentArray() {
		if (current != null)
			return;
		int mx = Math.round((float) nx / (float) currentSteps);
		int my = Math.round((float) ny / (float) currentSteps);
		current = new Vector2D[mx][my];
		for (int i = 0; i < mx; i++) {
			for (int j = 0; j < my; j++) {
				current[i][j] = new Vector2D();
			}
		}
	}

	public void destroy() {
		lock.lock();
		try {
			super.destroy();
			sources.clear();
			phase = null;
			hxSubDiagonal = null;
			hxDiagonal = null;
			hxSupDiagonal = null;
			hySubDiagonal = null;
			hyDiagonal = null;
			hySupDiagonal = null;
			psi = null;
			phix = null;
			phiy = null;
			current = null;
		} finally {
			lock.unlock();
		}
	}

	private void rotatePhase() {
		float angle;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				angle = staticRealPotential[i][j] * 0.5f * timeStep;
				psi[i][j] = psi[i][j].multiply(new FloatComplex((float) Math.cos(angle), -(float) Math.sin(angle)));
			}
		}
	}

	/*
	 * generate the tridiagonal matrix of the second order differential operator d^2/dx^2 for psi[.][j] (dimension=nx). The caller will iterate from 0 to ny-1 and obtain psi[.][.] (nx by ny).
	 */
	private void solveXTridiagMatrix(int j) {
		float p = timeStep * ax;
		if (iStep < 1 && bField == null) {
			FloatComplex c = new FloatComplex(0, p);
			Arrays.fill(hxSubDiagonal, c);
			Arrays.fill(hxSupDiagonal, c);
		} else {
			if (bField != null) {
				float k = 0.25f * bField.getValue(timeStep * iStep) * particle.getCharge() * (ymin + j * deltaY) / (deltaX * particle.getMass());
				k *= timeStep;
				Arrays.fill(hxSubDiagonal, new FloatComplex(k, p));
				Arrays.fill(hxSupDiagonal, new FloatComplex(-k, p));
			}
		}
		if (suzukiFlag) {
			// the advantage of using Suzuki exponential is that we do not have to construct
			// hxDiagonal and hxOffDiagonal at each step.
			if (iStep < 1) {
				Arrays.fill(hxDiagonal, new FloatComplex(0, -2 * p));
			}
			for (int i = 0; i < nx; i++) {
				phix[i] = psi[i][j];
			}
		} else {
			// when we split the d^2/dx^2 operator, we also split the potential energy operator
			// into half to go with it. The reason that it is half is because x and y direction
			// should be completely equivalent--we should not create a biased division for the
			// potential.
			float p1;
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
				p1 = -staticImaginaryPotential[i][j];
				p2 = -staticRealPotential[i][j];
				if (eField != null) {
					xi = xmin + i * deltaX;
					p2 -= particle.getCharge() * eField.getPotential(xi, yj, time);
				}
				if (bField != null) {
					xi = xmin + i * deltaX;
					p2 -= bterm * xi * xi * ENERGY_UNIT_CONVERTER;
				}
				p1 *= 0.5f * timeStep;
				p2 *= 0.5f * timeStep;
				p2 -= 2 * p;
				if (xBoundary == null) {
					hxDiagonal[i] = new FloatComplex(p1, p2);
				} else if (xBoundary instanceof AbsorbingBoundary) {
					AbsorbingBoundary ab = (AbsorbingBoundary) xBoundary;
					int lg = (int) (ab.getLengthPercentage() * nx);
					if (i < lg) {
						hxDiagonal[i] = new FloatComplex(p1 - ab.getAbsorption() * (lg - i), p2);
					} else if (i > nx - lg) {
						hxDiagonal[i] = new FloatComplex(p1 - ab.getAbsorption() * (lg - nx + i), p2);
					} else {
						hxDiagonal[i] = new FloatComplex(p1, p2);
					}
				}
				phix[i] = psi[i][j];
			}
		}
		phix = csx.nextStep(phix, hxSubDiagonal, hxDiagonal, hxSupDiagonal);
		for (int i = 0; i < nx; i++) {
			psi[i][j] = phix[i];
		}
	}

	/*
	 * generate the tridiagonal matrix of the second order differential operator d^2/dy^2 for psi[i][.] (dimension=ny). The caller will iterate from 0 to nx-1 and obtain psi[.][.] (nx by ny).
	 */
	private void solveYTridiagMatrix(int i) {
		float p = timeStep * ay;
		if (iStep < 1 && bField == null) {
			FloatComplex c = new FloatComplex(0, p);
			Arrays.fill(hySubDiagonal, c);
			Arrays.fill(hySupDiagonal, c);
		} else {
			if (bField != null) {
				float k = 0.25f * bField.getValue(timeStep * iStep) * particle.getCharge() * (xmin + i * deltaX) / (deltaY * particle.getMass());
				k *= timeStep;
				Arrays.fill(hySubDiagonal, new FloatComplex(-k, p));
				Arrays.fill(hySupDiagonal, new FloatComplex(k, p));
			}
		}
		if (suzukiFlag) {
			if (iStep < 1) {
				Arrays.fill(hyDiagonal, new FloatComplex(0, -2 * p));
			}
			for (int j = 0; j < ny; j++) {
				phiy[j] = psi[i][j];
			}
		} else {
			// when we split the d^2/dy^2 operator, we also split the potential energy operator
			// into half to go with it. The reason that it is half is because x and y direction
			// should be completely equivalent--we should not create a biased division for the
			// potential.
			float p1;
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
				p1 = -staticImaginaryPotential[i][j];
				p2 = -staticRealPotential[i][j];
				if (eField != null) {
					yj = ymin + j * deltaY;
					p2 -= particle.getCharge() * eField.getPotential(xi, yj, time);
				}
				if (bField != null) {
					yj = ymin + j * deltaY;
					p2 -= bterm * yj * yj * ENERGY_UNIT_CONVERTER;
				}
				p1 *= 0.5f * timeStep;
				p2 *= 0.5f * timeStep;
				p2 -= 2 * p;
				if (yBoundary == null) {
					hyDiagonal[j] = new FloatComplex(p1, p2);
				} else if (yBoundary instanceof AbsorbingBoundary) {
					AbsorbingBoundary ab = (AbsorbingBoundary) yBoundary;
					int lg = (int) (ab.getLengthPercentage() * ny);
					if (j < lg) {
						hyDiagonal[j] = new FloatComplex(p1 - ab.getAbsorption() * (lg - j), p2);
					} else if (j > ny - lg) {
						hyDiagonal[j] = new FloatComplex(p1 - ab.getAbsorption() * (lg - ny + j), p2);
					} else {
						hyDiagonal[j] = new FloatComplex(p1, p2);
					}
				}
				phiy[j] = psi[i][j];
			}
		}
		phiy = csy.nextStep(phiy, hySubDiagonal, hyDiagonal, hySupDiagonal);
		for (int j = 0; j < ny; j++) {
			psi[i][j] = phiy[j];
		}
	}

	void clearWaveFunction() {
		FloatComplex zero = new FloatComplex();
		for (int i = 0; i < nx; i++) {
			Arrays.fill(psi[i], zero);
		}
	}

	void initPsi() {
		if (psi == null || psi[0][0] == null)
			return;
		normalizePsi();
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				amplitude[i][j] = psi[i][j].absSquare();
				phase[i][j] = psi[i][j].arg();
			}
		}
		requestVisualization();
	}

	void normalizePsi() {
		sum = 0;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				sum += psi[i][j].absSquare();
			}
		}
		sum = (float) (1.0 / Math.sqrt(sum));
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				psi[i][j] = new FloatComplex(psi[i][j].real() * sum, psi[i][j].imag() * sum);
			}
		}
	}

	void addWaveFunction(FloatComplex[][] wf) {
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				if (psi[i][j] == null) {
					psi[i][j] = new FloatComplex(wf[i][j]);
				} else {
					psi[i][j] = psi[i][j].add(wf[i][j]);
				}
			}
		}
	}

	void nextStep() {
		lock.lock();
		try {
			applySource();
			if (suzukiFlag)
				rotatePhase();
			for (int j = 0; j < ny; j++) {
				solveXTridiagMatrix(j);
			}
			for (int i = 0; i < nx; i++) {
				solveYTridiagMatrix(i);
			}
			if (suzukiFlag)
				rotatePhase();
			if (iStep % OUTPUT_INTERVAL == 0) {
				outputProperties();
			}
			iStep++;
		} finally {
			lock.unlock();
		}
	}

	private void applySource() {
		if (iStep < 1)
			return;
		if (sources.isEmpty())
			return;
		float dx, dy;
		float g = 0;
		float a, b;
		for (Source s : sources) {
			if (iStep % Math.round(s.getPeriod() / timeStep) == 0) {
				float px = s.getPx();
				float py = s.getPy();
				float k;
				if (s instanceof PointSource) {
					PointSource ps = (PointSource) s;
					a = 0.5f / (ps.getSigma() * ps.getSigma());
					b = s.getAmplitude() / ((float) Math.sqrt(2 * Math.PI) * ps.getSigma());
					for (int i = 0; i < nx; i++) {
						dx = xmin + deltaX * i - s.getXcenter();
						dx *= dx;
						for (int j = 0; j < ny; j++) {
							dy = ymin + deltaY * j - s.getYcenter();
							g = (float) Math.exp(-(dx + dy * dy) * a) * b;
							if (px == 0 && py == 0) {
								psi[i][j] = psi[i][j].add(new FloatComplex(g, 0));
							} else {
								k = px * (xmin + i * deltaX) + py * (ymin + j * deltaY);
								psi[i][j] = psi[i][j].add(new FloatComplex((float) (g * Math.cos(k)), (float) (g * Math.sin(k))));
							}
						}
					}
				} else if (s instanceof PlaneWaveSource) {
					PlaneWaveSource pws = (PlaneWaveSource) s;
					g = pws.getAmplitude();
					for (int i = 0; i < nx; i++) {
						dx = xmin + deltaX * i;
						for (int j = 0; j < ny; j++) {
							dy = ymin + deltaY * j;
							if (pws.contains(dx, dy)) {
								if (px == 0 && py == 0) {
									psi[i][j] = psi[i][j].add(new FloatComplex(g, 0));
								} else {
									k = px * dx + py * dy;
									psi[i][j] = psi[i][j].add(new FloatComplex((float) (g * Math.cos(k)), (float) (g * Math.sin(k))));
								}
							}
						}
					}
				}
			}
		}
	}

	void outputProperties() {
		sum = 0;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				amplitude[i][j] = psi[i][j].absSquare();
				phase[i][j] = psi[i][j].arg();
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
		if (computeCurrent)
			calculateCurrent();
		requestVisualization();
		if (QuantumBox.getLogLevel() == 1) {
			if (iStep % 20 == 0) {
				System.out.printf(">>> %5.0f = %10.5f, %10.5f, %10.5f, %10.5f\n", iStep * timeStep, sum, totE, kinE, potE);
			}
		}
	}

	void calculatePosition() {
		position.x = 0;
		position.y = 0;
		FloatComplex c, z;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				c = psi[i][j].conjugate();
				z = c.multiply(new FloatComplex(i * psi[i][j].real(), i * psi[i][j].imag()));
				position.x += z.real();
				z = c.multiply(new FloatComplex(j * psi[i][j].real(), j * psi[i][j].imag()));
				position.y += z.real();
			}
		}
		position.x = xmin + deltaX * position.x;
		position.y = ymin + deltaY * position.y;
	}

	// must use central difference, or we will have a non-zero imaginary component for derivative
	void calculateMomentum() {
		FloatComplex m = new FloatComplex();
		FloatComplex psi1, psi2;
		for (int j = 0; j < ny; j++) {
			for (int i = 1; i < nx - 1; i++) {
				psi2 = psi[i + 1][j];
				psi1 = psi[i - 1][j];
				m = m.add(psi[i][j].conjugate().multiply(psi2.subtract(psi1)));
			}
		}
		momentum.x = m.imag() / (2 * deltaX);
		m = new FloatComplex();
		for (int i = 0; i < nx; i++) {
			for (int j = 1; j < ny - 1; j++) {
				psi2 = psi[i][j + 1];
				psi1 = psi[i][j - 1];
				m = m.add(psi[i][j].conjugate().multiply(psi2.subtract(psi1)));
			}
		}
		momentum.y = m.imag() / (2 * deltaY);
	}

	void resetCurrent() {
		if (current == null || current[0][0] == null)
			return;
		for (int i = 0; i < current.length; i++) {
			for (int j = 0; j < current[0].length; j++) {
				current[i][j].x = 0;
				current[i][j].y = 0;
			}
		}
	}

	// must use central difference, or we will have a non-zero imaginary component for derivative
	void calculateCurrent() {
		initCurrentArray();
		float cx = 0.5f / (deltaX * particle.getMass());
		float cy = 0.5f / (deltaY * particle.getMass());
		FloatComplex z;
		int i2, j2;
		FloatComplex psi1, psi2;
		float invCurrentSteps = 1f / currentSteps;
		for (int j = 0; j < ny; j += currentSteps) {
			j2 = Math.round(j * invCurrentSteps);
			for (int i = 1; i < nx - 1; i += currentSteps) {
				psi2 = psi[i + 1][j];
				psi1 = psi[i - 1][j];
				z = psi[i][j].conjugate().multiply(psi2.subtract(psi1));
				i2 = Math.round(i * invCurrentSteps);
				current[i2][j2].x = z.imag() * cx;
			}
		}
		for (int i = 0; i < nx; i += currentSteps) {
			i2 = Math.round(i * invCurrentSteps);
			for (int j = 1; j < ny - 1; j += currentSteps) {
				psi2 = psi[i][j + 1];
				psi1 = psi[i][j - 1];
				z = psi[i][j].conjugate().multiply(psi2.subtract(psi1));
				j2 = Math.round(j * invCurrentSteps);
				current[i2][j2].y = z.imag() * cy;
			}
		}
	}

	void calculatePotentialEnergy() {
		potE = 0;
		FloatComplex z;
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
				z = psi[i][j].conjugate().multiply(new FloatComplex(p * psi[i][j].real(), p * psi[i][j].imag()));
				potE += z.real();
			}
		}
	}

	void calculateKineticEnergy() {
		kinE = 0;
		FloatComplex kex = new FloatComplex();
		FloatComplex z;
		for (int j = 0; j < ny; j++) {
			for (int i = 1; i < nx - 1; i++) {
				z = new FloatComplex((psi[i + 1][j].real() - 2 * psi[i][j].real() + psi[i - 1][j].real()), (psi[i + 1][j].imag() - 2 * psi[i][j].imag() + psi[i - 1][j].imag()));
				kex = kex.add(psi[i][j].conjugate().multiply(z));
			}
		}
		FloatComplex key = new FloatComplex();
		for (int i = 0; i < nx; i++) {
			for (int j = 1; j < ny - 1; j++) {
				z = new FloatComplex((psi[i][j + 1].real() - 2 * psi[i][j].real() + psi[i][j - 1].real()), (psi[i][j + 1].imag() - 2 * psi[i][j].imag() + psi[i][j - 1].imag()));
				key = key.add(psi[i][j].conjugate().multiply(z));
			}
		}
		float k = -2 * particle.getMass() * MASS_UNIT_CONVERTER * ENERGY_UNIT_CONVERTER;
		kinE = kex.real() / (deltaX * deltaX) + key.real() / (deltaY * deltaY);
		kinE /= k;
	}

}
