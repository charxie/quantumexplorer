package quantum.qm2d.model;

import static quantum.qmshared.Boundary.ABSORBING_BOUNDARY_CONDITION;
import static quantum.qmshared.Boundary.DEFAULT_BOUNDARY_CONDITION;
import static quantum.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static quantum.qmshared.Constants.MASS_UNIT_CONVERTER;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import quantum.math.FloatComplex;
import quantum.math.Vector2D;
import quantum.qmevent.VisualizationEvent;
import quantum.qmevent.VisualizationListener;
import quantum.qmshared.AbsorbingBoundary;
import quantum.qmshared.Boundary;
import quantum.qmshared.Particle;

/**
 * @author Charles Xie
 * 
 */
public abstract class TimePropagator2D {

	public final static int OUTPUT_INTERVAL = 2;

	Lock lock;
	int nx, ny;
	int iStep;
	float[][] amplitude, phase;
	ElectricField2D eField;
	MagneticField2D bField;

	float deltaX, deltaY; // spatial step
	float xmin, xmax, ymin, ymax;
	Particle particle;
	Boundary xBoundary, yBoundary;
	float[][] staticRealPotential;
	float[][] staticImaginaryPotential;

	float sum, totE, potE, kinE;
	Vector2D position, momentum;
	Vector2D[][] current;
	float timeStep = 2;
	private volatile boolean running;
	private volatile boolean notifyReset;
	private List<Potential2D> potentials;
	List<WavePacket2D> wavePackets, wavePacketHolder;
	float ax, ay;
	boolean computeExpectation, computeEnergy, computeCurrent;
	WavePacketFactory wavepacketFactory;
	private PotentialFactory potentialFactory;

	private List<VisualizationListener> listeners;

	public TimePropagator2D(Particle particle, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		this.particle = particle;
		this.nx = nx;
		this.ny = ny;
		setArea(xmin, xmax, ymin, ymax);
		amplitude = new float[nx][ny];
		phase = new float[nx][ny];
		staticRealPotential = new float[nx][ny];
		staticImaginaryPotential = new float[nx][ny];
		potentials = Collections.synchronizedList(new ArrayList<Potential2D>());
		wavePackets = Collections.synchronizedList(new ArrayList<WavePacket2D>());
		wavePacketHolder = Collections.synchronizedList(new ArrayList<WavePacket2D>());
		position = new Vector2D();
		momentum = new Vector2D();
	}

	public Particle getParticle() {
		return particle;
	}

	public void clear() {
		removeAllPotentials();
		removeAllWavePackets();
		removeAllSources();
		setBoundaryCondition('x', DEFAULT_BOUNDARY_CONDITION);
		setBoundaryCondition('y', DEFAULT_BOUNDARY_CONDITION);
	}

	public void setComputeExpectation(boolean b) {
		computeExpectation = b;
	}

	public void setComputeEnergy(boolean b) {
		computeEnergy = b;
	}

	public void setComputeCurrent(boolean b) {
		computeCurrent = b;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public void destroy() {
		listeners.clear();
		potentials.clear();
		wavePackets.clear();
		amplitude = null;
		staticRealPotential = null;
		staticImaginaryPotential = null;
	}

	public void setWavePacketFactory(WavePacketFactory wavepacketFactory) {
		this.wavepacketFactory = wavepacketFactory;
	}

	public void setPotentialFactory(PotentialFactory potentialFactory) {
		this.potentialFactory = potentialFactory;
	}

	public void setArea(float xmin, float xmax, float ymin, float ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.deltaX = (this.xmax - this.xmin) / nx;
		this.deltaY = (this.ymax - this.ymin) / ny;
		calculateMassRelatedThings();
	}

	public void calculateMassRelatedThings() {
		ax = 0.5f / (deltaX * deltaX * particle.getMass() * MASS_UNIT_CONVERTER);
		ay = 0.5f / (deltaY * deltaY * particle.getMass() * MASS_UNIT_CONVERTER);
	}

	public void setBoundaryCondition(char direction, String name) {
		if ("ABC".equalsIgnoreCase(name)) {
			setBoundaryCondition(direction, ABSORBING_BOUNDARY_CONDITION);
		} else {
			setBoundaryCondition(direction, DEFAULT_BOUNDARY_CONDITION);
		}
	}

	public void setBoundaryCondition(char direction, byte bc) {
		if (direction == 'x' || direction == 'X') {
			if (bc == ABSORBING_BOUNDARY_CONDITION) {
				xBoundary = new AbsorbingBoundary();
				xBoundary.setDirection('x');
				((AbsorbingBoundary) xBoundary).setLengthPercentage(0.1f);
				((AbsorbingBoundary) xBoundary).setAbsorption(0.01f);
			} else if (bc == DEFAULT_BOUNDARY_CONDITION) {
				xBoundary = null;
			}
		} else if (direction == 'y' || direction == 'Y') {
			if (bc == ABSORBING_BOUNDARY_CONDITION) {
				yBoundary = new AbsorbingBoundary();
				yBoundary.setDirection('y');
				((AbsorbingBoundary) yBoundary).setLengthPercentage(0.1f);
				((AbsorbingBoundary) yBoundary).setAbsorption(0.01f);
			} else if (bc == DEFAULT_BOUNDARY_CONDITION) {
				yBoundary = null;
			}
		}
	}

	public Boundary getBoundary(char direction) {
		if (direction == 'x' || direction == 'X')
			return xBoundary;
		if (direction == 'y' || direction == 'Y')
			return yBoundary;
		return null;
	}

	public int getPotentialCount() {
		return potentials.size();
	}

	public Potential2D getPotential(int i) {
		return potentials.get(i);
	}

	public void addPotential(Potential2D potential) {
		potentials.add(potential);
		addPotentialFunction(potential.imaginary, potential.pot);
		potential.destroy();
	}

	public void addPotential(int index, Potential2D potential) {
		potentials.add(index, potential);
		addPotentialFunction(potential.imaginary, potential.pot);
		potential.destroy();
	}

	public void addPotentialFunction(boolean imaginary, float[][] pot) {
		if (imaginary) {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					staticImaginaryPotential[i][j] += pot[i][j] * ENERGY_UNIT_CONVERTER;
				}
			}
		} else {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					staticRealPotential[i][j] += pot[i][j] * ENERGY_UNIT_CONVERTER;
				}
			}
		}
	}

	public int removePotential(Potential2D potential) {
		int index = potentials.indexOf(potential);
		potentials.remove(potential);
		Potential2D p = potentialFactory.copy(potential);
		removePotentialFunction(p.imaginary, p.pot);
		p.destroy();
		potential.destroy();
		return index;
	}

	public int indexOfPotential(Potential2D p) {
		return potentials.indexOf(p);
	}

	public void removePotentialFunction(boolean imaginary, float[][] pot) {
		if (imaginary) {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					staticImaginaryPotential[i][j] -= pot[i][j] * ENERGY_UNIT_CONVERTER;
				}
			}
		} else {
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					staticRealPotential[i][j] -= pot[i][j] * ENERGY_UNIT_CONVERTER;
				}
			}
		}
	}

	public void removeAllPotentials() {
		potentials.clear();
		for (int i = 0; i < nx; i++) {
			Arrays.fill(staticRealPotential[i], 0);
			Arrays.fill(staticImaginaryPotential[i], 0);
		}
	}

	public void setElectricField(ElectricField2D eField) {
		this.eField = eField;
	}

	public ElectricField2D getElectricField() {
		return eField;
	}

	public void setMagneticField(MagneticField2D bField) {
		this.bField = bField;
	}

	public MagneticField2D getMagneticField() {
		return bField;
	}

	public List<Potential2D> getPotentials() {
		return potentials;
	}

	public void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}

	public float getTimeStep() {
		return timeStep;
	}

	public float getTime() {
		return iStep * timeStep;
	}

	public WavePacket2D getWavePacket(int i) {
		if (i >= wavePackets.size() || i < 0)
			return null;
		return wavePackets.get(i);
	}

	public int getWavePacketCount() {
		return wavePackets.size();
	}

	public void addWavePacket(WavePacket2D wave) {
		wavePackets.add(wave);
	}

	public void removeAllWavePackets() {
		wavePackets.clear();
		clearWaveFunction();
	}

	void removeAllSources() {
	}

	abstract void clearWaveFunction();

	abstract void initPsi();

	abstract void normalizePsi();

	public float[][] getAmplitude() {
		return amplitude;
	}

	public float[][] getPhase() {
		return phase;
	}

	public Vector2D getPosition() {
		return position;
	}

	public Vector2D getMomentum() {
		return momentum;
	}

	public Vector2D[][] getCurrent() {
		return current;
	}

	public float getKineticEnergy() {
		return kinE;
	}

	public float getPotentialEnergy() {
		return potE;
	}

	public float getTotalEnergy() {
		return totE;
	}

	public void stop() {
		running = false;
	}

	public void run() {
		if (!running) {
			running = true;
			while (running)
				nextStep();
			if (notifyReset) {
				rebuildWaveFunction();
				notifyReset = false;
			}
		}
	}

	public void runSteps(int n) {
		if (!running) {
			running = true;
			for (int i = 0; i < n; i++)
				nextStep();
			if (notifyReset) {
				rebuildWaveFunction();
				notifyReset = false;
			}
			running = false;
		}
	}

	public void reset() {
		iStep = 0;
		position.x = position.y = 0;
		if (running) {
			stop();
			clearWaveFunction();
			notifyReset = true;
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// IMPORTANT to be invoked later
					rebuildWaveFunction();
				}
			});
		}
		running = false; // reassure that it is not running
	}

	abstract void addWaveFunction(FloatComplex[][] wf);

	public void buildWaveFunction() {
		clearWaveFunction();
		synchronized (wavePackets) {
			for (WavePacket2D wave : wavePackets) {
				addWaveFunction(wave.psi);
				wave.destroy();
				wave = null;
			}
		}
		initPsi();
	}

	public void rebuildWaveFunction() {
		clearWaveFunction();
		wavePacketHolder.clear();
		wavepacketFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		synchronized (wavePackets) {
			for (WavePacket2D wave : wavePackets) {
				if (wave instanceof Gaussian2D) {
					Gaussian2D g = (Gaussian2D) wave;
					wavePacketHolder.add(wavepacketFactory.createGaussian(g.getMagnitude(), g.getSigma(), g.getMuX(), g.getMuY(), g.getPx(), g.getPy()));
				} else if (wave instanceof UniformRectangle2D) {
					UniformRectangle2D p = (UniformRectangle2D) wave;
					wavePacketHolder.add(wavepacketFactory.createPlaneWave(p.getMagnitude(), p.getX(), p.getY(), p.getW(), p.getH(), p.getPx(), p.getPy()));
				}
			}
		}
		wavePackets.clear();
		synchronized (wavePacketHolder) {
			for (WavePacket2D wave : wavePacketHolder) {
				addWavePacket(wave);
				addWaveFunction(wave.psi);
				wave.destroy();
			}
		}
		wavePacketHolder.clear();
		initPsi();
		if (computeCurrent)
			resetCurrent();
	}

	abstract void nextStep();

	abstract void outputProperties();

	abstract void calculatePosition();

	abstract void calculateMomentum();

	abstract void calculatePotentialEnergy();

	abstract void calculateKineticEnergy();

	abstract void resetCurrent();

	abstract void calculateCurrent();

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
