package org.concord.qm1d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.concord.modeler.MwService;
import org.concord.qmevent.VisualizationEvent;
import org.concord.qmevent.VisualizationListener;
import org.concord.qmshared.Particle;

/**
 * @author Charles Xie
 * 
 */
public class MainWindow extends JApplet implements MwService, VisualizationListener {

	private static final long serialVersionUID = 1L;
	private WaveFunctionView waveFunView;
	private EnergyLevelView energyLevelView;
	private DynamicsView dynamicsView;
	private Scripter1D scripter;
	private int n = 200;
	private double xmin = -10, xmax = 10;
	private boolean stationary = true;
	boolean editable = true;

	private ExecutorService threadService;

	static byte logLevel = 0;
	Particle particle;
	ElectricField1D eField;
	Potential1D potential;
	TimePropagator1D propagator;
	StationaryStateSolver stationaryStateSolver;

	Runnable clickRun, clickStop, clickReset;

	public MainWindow() {
		particle = new Particle();
	}

	public void setEditable(boolean b) {
		editable = b;
	}

	public boolean needExecutorService() {
		return true;
	}

	public void setExecutorService(ExecutorService service) {
		threadService = service;
	}

	@Override
	public void destroy() {
		stop();
		if (threadService != null && !threadService.isShutdown()) {
			threadService.shutdownNow();
		}
	}

	public void stop() {
		if (propagator != null) {
			propagator.stop();
		}
	}

	void setFrank(boolean b) {
		if (waveFunView != null)
			waveFunView.setFrank(b);
		if (dynamicsView != null)
			dynamicsView.setFrank(b);
	}

	private void addDynamicView() {
		dynamicsView = new DynamicsView();
		dynamicsView.setBackground(Color.white);
		dynamicsView.setForeground(Color.black);
		add(dynamicsView, BorderLayout.CENTER);
		dynamicsView.setPreferredSize(new Dimension(600, 400));
		JPanel p = new JPanel();
		add(p, BorderLayout.SOUTH);
		final JButton buttonRun = new JButton("Run");
		final JButton buttonStop = new JButton("Stop");
		final JButton buttonReset = new JButton("Reset");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runDynamics();
				buttonRun.setEnabled(false);
				buttonStop.setEnabled(true);
			}
		});
		p.add(buttonRun);
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propagator.stop();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonStop);
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propagator.reset();
				buttonRun.setEnabled(true);
				buttonStop.setEnabled(false);
			}
		});
		p.add(buttonReset);
		clickRun = new Runnable() {
			public void run() {
				buttonRun.doClick();
			}
		};
		clickStop = new Runnable() {
			public void run() {
				buttonStop.doClick();
			}
		};
		clickReset = new Runnable() {
			public void run() {
				buttonReset.doClick();
			}
		};
	}

	private void addStationaryView() {
		waveFunView = new WaveFunctionView();
		waveFunView.setPreferredSize(new Dimension(600, 150));
		waveFunView.setSelectedIndex(0);
		waveFunView.setBackground(Color.white);
		waveFunView.setForeground(Color.black);
		add(waveFunView, BorderLayout.NORTH);
		energyLevelView = new EnergyLevelView();
		energyLevelView.setPreferredSize(new Dimension(600, 200));
		energyLevelView.setForeground(Color.black);
		energyLevelView.addEnergyLevelSelectionListener(waveFunView);
		add(energyLevelView, BorderLayout.CENTER);
	}

	boolean isStationary() {
		return stationary;
	}

	@Override
	public void init() {
		try {
			xmin = Double.parseDouble(getParameter("xmin"));
			xmax = Double.parseDouble(getParameter("xmax"));
		} catch (Exception e) {
		}
		String s = null;
		try {
			s = getParameter("stationary");
		} catch (Exception e) {
			s = "false";
		}
		if ("false".equalsIgnoreCase(s)) {
			stationary = false;
			addDynamicView();
			propagator = new RungeKuttaSolver(particle, n);
			try {
				s = getParameter("boundary");
			} catch (Exception e) {
				s = null;
			}
			propagator.setBoundaryCondition(s);
			double timestep = propagator.getTimeStep();
			try {
				timestep = Double.parseDouble(getParameter("timestep"));
			} catch (Exception e) {
			}
			propagator.setTimeStep(timestep);
			try {
				s = getParameter("potential");
			} catch (Exception e) {
				s = null;
			}
			setPotential(s == null ? Scripter1D.SQUARE_WELL : s);
			try {
				s = getParameter("init_state");
			} catch (Exception e) {
				s = null;
			}
			if (s != null) {
				int initState = 0;
				try {
					initState = Integer.parseInt(s);
				} catch (Exception e) {
				}
				initializeStationaryStateSolver();
				stationaryStateSolver.setPotential(potential.getPotential());
				stationaryStateSolver.solve("I", potential.xmax - potential.xmin);
				propagator.setInitialState(initState, stationaryStateSolver.getEigenVectors());
			} else {
				double mu = -4;
				double sigma = 2;
				try {
					mu = Double.parseDouble(getParameter("mu"));
					sigma = Double.parseDouble(getParameter("sigma"));
				} catch (Exception e) {
				}
				propagator.setGaussianParameters(mu, sigma);
			}
			s = null;
			try {
				s = getParameter("efield");
			} catch (Exception e) {
				s = "false";
			}
			if ("true".equalsIgnoreCase(s)) {
				eField = new ElectricField1D();
				if (stationaryStateSolver != null) {
					double[] x = stationaryStateSolver.getEigenEnergies();
					eField.setFrequency(Math.abs(x[1] - x[0]) > 0.0001 ? x[1] - x[0] : x[2] - x[0]);
				}
				propagator.setElectricField(eField);
			}
			propagator.addVisualizationListener(this);
			dynamicsView.setPotential(potential, true);
			dynamicsView.setBoundaryLayer(propagator.getBoundary());
			dynamicsView.setTimeStep(propagator.getTimeStep());
			propagator.init();
		} else {
			stationary = true;
			addStationaryView();
			initializeStationaryStateSolver();
			s = null;
			try {
				s = getParameter("max_state");
			} catch (Exception e) {
				s = null;
			}
			if (s != null) {
				int maxState = -1;
				try {
					maxState = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					maxState = -1;
				}
				if (maxState > 0) {
					stationaryStateSolver.setMaxState(maxState);
				}
			}
			try {
				s = getParameter("potential");
			} catch (Exception e) {
				s = null;
			}
			if (s == null) {
				setPotential(Scripter1D.SQUARE_WELL);
			} else {
				setPotential(s);
			}
		}
		try {
			s = getParameter("script");
		} catch (Exception e) {
			s = null;
		}
		if (s != null) {
			runNativeScript(s);
		}
	}

	private void initializeStationaryStateSolver() {
		if (stationaryStateSolver == null) {
			stationaryStateSolver = new StationaryStateSolver(n);
			stationaryStateSolver.setParticle(particle);
			stationaryStateSolver.setMaxState(20);
		}
	}

	private void runDynamics() {
		if (threadService == null)
			threadService = Executors.newFixedThreadPool(1);
		threadService.execute(new Runnable() {
			public void run() {
				propagator.run();
			}
		});
	}

	public void visualizationRequested(VisualizationEvent e) {
		if (dynamicsView != null) {
			dynamicsView.setPotential(potential, false);
			if (eField != null)
				dynamicsView.setElectricField(eField, propagator.getTime());
			dynamicsView.setProbability(propagator.getAmplitude());
			dynamicsView.setPosition(propagator.getPosition());
			dynamicsView.setVelocity(propagator.getVelocity());
			dynamicsView.setKineticEnergy(propagator.getKineticEnergy());
			dynamicsView.setPotentialEnergy(propagator.getPotentialEnergy());
			dynamicsView.repaint();
		}
	}

	private void loadPotential(String name) {
		if (name == null)
			return;
		if (Scripter1D.INFINITE_SQUARE_WELL.equalsIgnoreCase(name)) {
			potential = new SquareWell(n, 0, 0, xmin, xmax);
		} else if (Scripter1D.SQUARE_WELL.equalsIgnoreCase(name)) {
			potential = new SquareWell(n, -1, 1, xmin, xmax);
		} else if (Scripter1D.SQUARE_QUANTUM_WELL.equalsIgnoreCase(name)) {
			potential = new SquareQuantumWell(n, 0.0, 2, 1, 0.2, 0, xmin, xmax);
		} else if (Scripter1D.SQUARE_BARRIER.equalsIgnoreCase(name)) {
			potential = new SquareBarrier(n, xmin, xmax);
		} else if (Scripter1D.BELL_BARRIER.equalsIgnoreCase(name)) {
			potential = new BellBarrier(n, xmin, xmax);
		} else if (Scripter1D.HARMONIC_OSCILLATOR.equalsIgnoreCase(name)) {
			potential = new HarmonicOscillator(n, xmin, xmax);
		} else if (Scripter1D.ANHARMONIC_OSCILLATOR.equalsIgnoreCase(name)) {
			potential = new AnharmonicOscillator(n, xmin, xmax);
		} else if (Scripter1D.MORSE_WELL.equalsIgnoreCase(name)) {
			potential = new MorseWell(n, xmin, xmax);
		} else if (Scripter1D.QUARTIC_DOUBLE_WELL.equalsIgnoreCase(name)) {
			potential = new QuarticDoubleWell(n, 0, xmin, xmax);
		} else if (Scripter1D.ASYMMETRIC_QUARTIC_DOUBLE_WELL.equalsIgnoreCase(name)) {
			potential = new QuarticDoubleWell(n, 0.05, xmin, xmax);
		} else if (Scripter1D.COULOMB_WELL.equalsIgnoreCase(name)) {
			potential = new CoulombWell(n, xmin, xmax);
		} else if (Scripter1D.DIRAC_COMB.equalsIgnoreCase(name)) {
			potential = new DiracComb(n, 10, 1, xmin, xmax);
		} else if (Scripter1D.DIATOMIC_MOLECULE.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 2, 1, 0, CoulombWellArray.DEFAULT, xmin, xmax);
		} else if (Scripter1D.CRYSTAL_LATTICE.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 13, 1.4, 0, CoulombWellArray.DEFAULT, xmin, xmax);
		} else if (Scripter1D.CRYSTAL_LATTICE_IN_FIELD.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 11, 1.5, 0.002, CoulombWellArray.DEFAULT, xmin, xmax);
		} else if (Scripter1D.CRYSTAL_LATTICE_VACANCY.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 11, 1.5, 0, CoulombWellArray.VACANCY, xmin, xmax);
		} else if (Scripter1D.CRYSTAL_LATTICE_INTERSTITIAL.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 11, 1.5, 0, CoulombWellArray.INTERSTITIAL, xmin, xmax);
		} else if (Scripter1D.BINARY_LATTICE.equalsIgnoreCase(name)) {
			potential = new CoulombWellArray(n, 11, 1.5, 0, CoulombWellArray.BINARY_LATTICE, xmin, xmax);
		}
	}

	void setPotential(String name) {
		loadPotential(name);
		if (potential == null || potential.getPotential() == null)
			return;
		if (stationary) {
			energyLevelView.setPotential(potential.getPotential());
			stationaryStateSolver.setPotential(potential.getPotential());
			stationaryStateSolver.solve("I", potential.xmax - potential.xmin);
			waveFunView.setEigenVectors(stationaryStateSolver.getEigenVectors());
			energyLevelView.setEigenEnergies(stationaryStateSolver.getEigenEnergies());
			waveFunView.repaint();
			energyLevelView.repaint();
		} else {
			dynamicsView.setPotential(potential, true);
			propagator.setPotential(potential);
			propagator.reset();
			dynamicsView.repaint();
		}
	}

	public JPopupMenu getPopupMenu() {
		return null;
	}

	public Component getSnapshotComponent() {
		return stationary ? this : dynamicsView;
	}

	public void loadState(InputStream is) throws IOException {
	}

	public String runNativeScript(String script) {
		if (script == null)
			return null;
		if (scripter == null)
			scripter = new Scripter1D(this);
		scripter.executeScript(script);
		return null;
	}

	public void saveState(OutputStream os) throws IOException {
	}

	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MainWindow mw = new MainWindow();
		logLevel = 1;
		mw.init();
		if (mw.propagator != null)
			mw.propagator.setInitialMomentum(2);
		frame.setContentPane(mw.getContentPane());
		frame.setLocation(100, 100);
		frame.pack();
		frame.setVisible(true);
	}

}
