package quantum.qm1d;

import static quantum.qmshared.Boundary.ABSORBING_BOUNDARY_CONDITION;
import static quantum.qmshared.Boundary.DEFAULT_BOUNDARY_CONDITION;
import static quantum.qmshared.Constants.ENERGY_UNIT_CONVERTER;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import quantum.qmevent.VisualizationEvent;
import quantum.qmevent.VisualizationListener;
import quantum.qmshared.AbsorbingBoundary;
import quantum.qmshared.Boundary;
import quantum.qmshared.Particle;

/**
 * @author Charles Xie
 * 
 */
abstract class TimePropagator1D {

	final static double VMAX = 5;

	final static int OUTPUT_INTERVAL = 50;

	int n;
	double[] coordinate, savedEigenvector;
	double[] amplitude;
	int iStep;
	double timeStep = 0.01;
	Potential1D potential;
	Particle particle;
	ElectricField1D eField;
	Boundary boundary;
	double sum, totE, potE, kinE, position, momentum;
	volatile boolean running;
	volatile boolean notifyReset;
	double mu = 10, sigma = 10;
	double delta;
	int initState = -1;

	private List<VisualizationListener> listeners;

	void setBoundaryCondition(String name) {
		if ("ABC".equalsIgnoreCase(name)) {
			setBoundaryCondition(ABSORBING_BOUNDARY_CONDITION);
		} else {
			setBoundaryCondition(DEFAULT_BOUNDARY_CONDITION);
		}
	}

	void setBoundaryCondition(byte bc) {
		if (bc == ABSORBING_BOUNDARY_CONDITION) {
			boundary = new AbsorbingBoundary();
		} else if (bc == DEFAULT_BOUNDARY_CONDITION) {
			boundary = null;
		}
	}

	Boundary getBoundary() {
		return boundary;
	}

	void heat(double ratio) {
	}

	void setPotential(Potential1D potential) {
		this.potential = potential;
		generateHamiltonianMatrix();
	}

	double[] getPotential() {
		if (eField != null) {
			double ef = particle.getCharge() * eField.getValue(timeStep * iStep);
			double[] p = new double[n];
			for (int i = 0; i < n; i++) {
				p[i] = potential.pot[i] + ef * (i - n / 2);
			}
			return p;
		}
		return potential.pot;
	}

	/* Clamp the potential function to avoid numeric instability due to large potential value. */
	double clampPotential(double v) {
		if (v > VMAX)
			v = VMAX;
		else if (v < -VMAX)
			v = -VMAX;
		return v * ENERGY_UNIT_CONVERTER;
	}

	/* Discretize the Hamiltonian into a matrix using the finite-difference method. */
	abstract void generateHamiltonianMatrix();

	void setElectricField(ElectricField1D eField) {
		this.eField = eField;
	}

	void setTimeStep(double timestep) {
		this.timeStep = timestep;
	}

	double getTimeStep() {
		return timeStep;
	}

	double getTime() {
		return iStep * timeStep;
	}

	abstract void setInitialState(int initState, double[][] eigenVector);

	int getInitialState() {
		return initState;
	}

	void setInitialMomentum(double p0) {
	}

	void setGaussianMu(double mu) {
		initState = -1;
		this.mu = mu;
		initPsi();
	}

	void setGaussianSigma(double sigma) {
		initState = -1;
		this.sigma = sigma;
		initPsi();
	}

	void setGaussianParameters(double mu, double sigma) {
		initState = -1;
		this.mu = mu;
		this.sigma = sigma;
		initPsi();
	}

	abstract void initPsi();

	abstract void normalizePsi();

	abstract double[] getAmplitude();

	double getPosition() {
		return position;
	}

	double getVelocity() {
		return momentum;
	}

	double getKineticEnergy() {
		return kinE;
	}

	double getPotentialEnergy() {
		return potE;
	}

	void init() {
		initPsi();
		position = calculateExpectation(coordinate);
		calculateMomentum();
		calculateKineticEnergy();
		if (potential != null)
			potE = calculateExpectation(potential.pot);
		totE = kinE + potE;
		requestVisualization();
	}

	void stop() {
		running = false;
	}

	void run() {
		if (!running) {
			running = true;
			while (running)
				nextStep();
			if (notifyReset) {
				init();
				notifyReset = false;
			}
		}
	}

	void reset() {
		iStep = 0;
		if (running) {
			stop();
			notifyReset = true;
		} else {
			init();
		}
	}

	abstract void nextStep();

	abstract void calculateMomentum();

	abstract void calculateKineticEnergy();

	abstract double calculateExpectation(double[] prop);

	abstract void outputProperties();

	public void addVisualizationListener(VisualizationListener listener) {
		if (listeners == null)
			listeners = new ArrayList<VisualizationListener>();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeVisualizationListener(VisualizationListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	void requestVisualization() {
		if (listeners == null || listeners.isEmpty())
			return;
		final VisualizationEvent e = new VisualizationEvent(this);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				for (VisualizationListener x : listeners)
					x.visualizationRequested(e);
			}
		});
	}

}
