package org.concord.qm1d;

import static java.util.regex.Pattern.*;

import java.awt.EventQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Scripter;

/**
 * @author Charles Xie
 * 
 */
class Scripter1D extends Scripter {

	final static String INFINITE_SQUARE_WELL = "INFINITE_SQUARE_WELL";
	final static String SQUARE_WELL = "SQUARE_WELL";
	final static String SQUARE_QUANTUM_WELL = "SQUARE_QUANTUM_WELL";
	final static String QUARTIC_DOUBLE_WELL = "QUARTIC_DOUBLE_WELL";
	final static String ASYMMETRIC_QUARTIC_DOUBLE_WELL = "ASYMMETRIC_QUARTIC_DOUBLE_WELL";
	final static String SQUARE_BARRIER = "SQUARE_BARRIER";
	final static String BELL_BARRIER = "BELL_BARRIER";
	final static String HARMONIC_OSCILLATOR = "HARMONIC_OSCILLATOR";
	final static String ANHARMONIC_OSCILLATOR = "ANHARMONIC_OSCILLATOR";
	final static String MORSE_WELL = "MORSE_WELL";
	final static String DIRAC_COMB = "DIRAC_COMB";
	final static String COULOMB_WELL = "COULOMB_WELL";
	final static String DIATOMIC_MOLECULE = "DIATOMIC_MOLECULE";
	final static String CRYSTAL_LATTICE = "CRYSTAL_LATTICE";
	final static String CRYSTAL_LATTICE_IN_FIELD = "CRYSTAL_LATTICE_IN_FIELD";
	final static String CRYSTAL_LATTICE_VACANCY = "CRYSTAL_LATTICE_VACANCY";
	final static String CRYSTAL_LATTICE_INTERSTITIAL = "CRYSTAL_LATTICE_INTERSTITIAL";
	final static String BINARY_LATTICE = "BINARY_LATTICE";

	private final static Pattern LOAD = compile("(^(?i)load\\b){1}");
	private final static Pattern HEAT = compile("(^(?i)heat\\b){1}");
	private final static Pattern COOL = compile("(^(?i)cool\\b){1}");

	private MainWindow mainWindow;
	private TimePropagator1D propagator;

	Scripter1D(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	protected void evalCommand(String ci) {

		propagator = mainWindow.propagator;

		// load
		Matcher matcher = LOAD.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			mainWindow.setPotential(s);
			return;
		}

		matcher = RESET.matcher(ci);
		if (matcher.find()) {
			if (mainWindow.clickReset != null)
				EventQueue.invokeLater(mainWindow.clickReset);
			return;
		}

		matcher = RUN.matcher(ci);
		if (matcher.find()) {
			if (mainWindow.clickRun != null)
				EventQueue.invokeLater(mainWindow.clickRun);
			return;
		}

		matcher = STOP.matcher(ci);
		if (matcher.find()) {
			if (mainWindow.clickStop != null)
				EventQueue.invokeLater(mainWindow.clickStop);
			return;
		}

		matcher = HEAT.matcher(ci);
		if (matcher.find()) {
			if (propagator != null)
				propagator.heat(0.1);
			return;
		}

		matcher = COOL.matcher(ci);
		if (matcher.find()) {
			if (propagator != null)
				propagator.heat(-0.1);
			return;
		}

		matcher = SET.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			String[] t = s.split(REGEX_WHITESPACE);
			if (t.length == 2) {
				if (t[0].equalsIgnoreCase("init_state")) {
					int initState = 0;
					try {
						initState = Integer.parseInt(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null && mainWindow.stationaryStateSolver != null) {
						int previousInitState = propagator.getInitialState();
						propagator.setInitialState(initState, mainWindow.stationaryStateSolver.getEigenVectors());
						mainWindow.visualizationRequested(null);
						if (mainWindow.eField != null && previousInitState >= 0) {
							double[] ee = mainWindow.stationaryStateSolver.getEigenEnergies();
							double ediff = ee[initState] - ee[previousInitState];
							mainWindow.eField.setFrequency((mainWindow.eField.getFrequency() - ediff));
						}
					}
				} else if (t[0].equalsIgnoreCase("final_state")) {
					float finalState = 1;
					try {
						finalState = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						if (mainWindow.stationaryStateSolver != null && mainWindow.eField != null) {
							double[] ee = mainWindow.stationaryStateSolver.getEigenEnergies();
							int i0 = (int) finalState;
							double de = ee[i0] + (ee[i0 + 1] - ee[i0]) * (finalState - i0) - ee[propagator.getInitialState()];
							mainWindow.eField.setFrequency(de);
						}
					}
				} else if (t[0].equalsIgnoreCase("boundary")) {
					if (propagator != null) {
						propagator.setBoundaryCondition(t[1]);
					}
				} else if (t[0].equalsIgnoreCase("boundarylayerlength")) {
					if (propagator != null) {
						if (propagator.getBoundary() == null)
							propagator.setBoundaryCondition("ABC");
						float layerLength = 0;
						try {
							layerLength = Float.parseFloat(t[1]);
						} catch (NumberFormatException e) {
							return;
						}
						((AbsorbingBoundary) propagator.getBoundary()).setLengthPercentage(layerLength);
					}
				} else if (t[0].equalsIgnoreCase("boundarylayerabsorption")) {
					if (propagator != null) {
						if (propagator.getBoundary() == null)
							propagator.setBoundaryCondition("ABC");
						float absorption = 0;
						try {
							absorption = Float.parseFloat(t[1]);
						} catch (NumberFormatException e) {
							return;
						}
						((AbsorbingBoundary) propagator.getBoundary()).setAbsorption(absorption);
					}
				} else if (t[0].equalsIgnoreCase("p0")) {
					double p0 = 0;
					try {
						p0 = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						propagator.setInitialMomentum(p0);
						propagator.calculateMomentum();
						mainWindow.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("mu")) {
					double mu = 0;
					try {
						mu = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						propagator.setGaussianMu(mu);
						mainWindow.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("sigma")) {
					double sigma = 0;
					try {
						sigma = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						propagator.setGaussianSigma(sigma);
						mainWindow.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("mass")) {
					float mass = 0;
					try {
						mass = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					mainWindow.particle.setMass(mass);
				} else if (t[0].equalsIgnoreCase("timestep")) {
					double timestep = 0;
					try {
						timestep = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						propagator.setTimeStep(timestep);
					}
				} else if (t[0].equalsIgnoreCase("efield_intensity")) {
					double eFieldIntensity = 0;
					try {
						eFieldIntensity = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						if (mainWindow.eField == null) {
							mainWindow.eField = new ElectricField1D();
							propagator.setElectricField(mainWindow.eField);
						}
						mainWindow.eField.setIntensity(eFieldIntensity);
					}
				} else if (t[0].equalsIgnoreCase("efield_frequency")) {
					double eFieldFrequency = 0;
					try {
						eFieldFrequency = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator != null) {
						if (mainWindow.eField == null) {
							mainWindow.eField = new ElectricField1D();
							propagator.setElectricField(mainWindow.eField);
						}
						mainWindow.eField.setFrequency(eFieldFrequency);
					}
				} else if (t[0].equalsIgnoreCase("slk_friction")) {
					double slkFriction = 0;
					try {
						slkFriction = Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator instanceof RealTimePropagator1D) {
						((RealTimePropagator1D) propagator).setSlkFriction(slkFriction);
					}
				} else if (t[0].equalsIgnoreCase("max_state")) {
					int maxState = 10;
					try {
						maxState = (int) Double.parseDouble(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (mainWindow.stationaryStateSolver != null) {
						mainWindow.stationaryStateSolver.setMaxState(maxState);
					}
				} else if (t[0].equalsIgnoreCase("frank")) {
					String t1 = t[1].trim();
					mainWindow.setFrank(t1.equalsIgnoreCase("true"));
				}
			}
			return;
		}

	}
}
