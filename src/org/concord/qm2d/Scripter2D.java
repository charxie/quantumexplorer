package org.concord.qm2d;

import static java.util.regex.Pattern.compile;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.concord.qm2d.model.AnnularPotential;
import org.concord.qm2d.model.ElectricField2D;
import org.concord.qm2d.model.EllipticalPotential;
import org.concord.qm2d.model.Gaussian2D;
import org.concord.qm2d.model.IonicPotential;
import org.concord.qm2d.model.MagneticField2D;
import org.concord.qm2d.model.PointSource;
import org.concord.qm2d.model.Potential2D;
import org.concord.qm2d.model.RealTimePropagator2D;
import org.concord.qm2d.model.RectangularPotential;
import org.concord.qm2d.model.Source;
import org.concord.qm2d.model.TimePropagator2D;
import org.concord.qm2d.model.WavePacket2D;
import org.concord.qm2d.view.Picture;
import org.concord.qm2d.view.TextBox;
import org.concord.qmevent.ScriptEvent;
import org.concord.qmevent.ScriptListener;
import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Scripter;
import org.concord.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class Scripter2D extends Scripter {

	private final static Pattern RUNSTEPS = compile("(^(?i)runsteps\\b){1}");
	private final static Pattern ADD = compile("(^(?i)add\\b){1}");
	private final static Pattern ATTACH = compile("(^(?i)attach\\b){1}");
	private final static Pattern POTENTIAL = compile("(^(?i)potential\\b){1}");
	private final static Pattern WAVEPACKET = compile("(^(?i)wavepacket\\b){1}");
	private final static Pattern SOURCE = compile("(^(?i)source\\b){1}");
	private final static Pattern EFIELD = compile("(^(?i)efield\\b){1}");
	private final static Pattern BFIELD = compile("(^(?i)bfield\\b){1}");
	private final static Pattern WAVEPACKET_FIELD = compile("^%?((?i)wavepacket){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern POTENTIAL_FIELD = compile("^%?((?i)potential){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern SOURCE_FIELD = compile("^%?((?i)source){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern IMAGE_FIELD = compile("^%?((?i)image){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");
	private final static Pattern TEXT_FIELD = compile("^%?((?i)text){1}(\\[){1}" + REGEX_WHITESPACE + "*" + REGEX_NONNEGATIVE_DECIMAL + REGEX_WHITESPACE + "*(\\]){1}\\.");

	private QuantumBox box;
	private TimePropagator2D propagator;
	private List<ScriptListener> listenerList;

	Scripter2D(QuantumBox box) {
		this.box = box;
	}

	void addScriptListener(ScriptListener listener) {
		if (listenerList == null)
			listenerList = new CopyOnWriteArrayList<ScriptListener>();
		if (!listenerList.contains(listener))
			listenerList.add(listener);
	}

	void removeScriptListener(ScriptListener listener) {
		if (listenerList == null)
			return;
		listenerList.remove(listener);
	}

	void removeAllScriptListeners() {
		if (listenerList == null)
			return;
		listenerList.clear();
	}

	private void notifyScriptListener(ScriptEvent e) {
		if (listenerList == null)
			return;
		synchronized (listenerList) {
			for (ScriptListener l : listenerList) {
				l.outputScriptResult(e);
			}
		}
	}

	private void showException(String command, Exception e) {
		e.printStackTrace();
		out(ScriptEvent.FAILED, "Error in \'" + command + "\':" + e.getMessage());
	}

	private void showError(String command, String message) {
		out(ScriptEvent.FAILED, "Error in \'" + command + "\':" + message);
	}

	private void out(byte status, String description) {
		if (status == ScriptEvent.FAILED) {
			notifyScriptListener(new ScriptEvent(box, status, "Aborted: " + description));
		} else {
			notifyScriptListener(new ScriptEvent(box, status, description));
		}
	}

	protected void evalCommand(String ci) {

		propagator = box.propagator;

		Matcher matcher = RESET.matcher(ci);
		if (matcher.find()) {
			if (box.clickReset != null) {
				EventQueue.invokeLater(box.clickReset);
			} else {
				box.reset();
			}
			return;
		}

		matcher = RELOAD.matcher(ci);
		if (matcher.find()) {
			if (box.clickReload != null) {
				EventQueue.invokeLater(box.clickReload);
			} else {
				box.reload();
			}
			return;
		}

		matcher = RUN.matcher(ci);
		if (matcher.find()) {
			if (box.clickRun != null) {
				EventQueue.invokeLater(box.clickRun);
			} else {
				box.run();
			}
			return;
		}

		matcher = RUNSTEPS.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			int nsteps = 10;
			try {
				nsteps = (int) Float.parseFloat(s);
			} catch (Exception e) {
				showException(ci, e);
			}
			box.runSteps(nsteps);
			return;
		}

		matcher = STOP.matcher(ci);
		if (matcher.find()) {
			if (box.clickStop != null) {
				EventQueue.invokeLater(box.clickStop);
			} else {
				box.stop();
			}
			return;
		}

		matcher = LOAD.matcher(ci);
		if (matcher.find()) {
			URL codeBase = box.getCodeBase();
			if (codeBase != null) {
				String s = ci.substring(matcher.end()).trim();
				try {
					box.loadURL(new URL(codeBase, s));
				} catch (IOException e) {
					showException(ci, e);
				}
			}
			return;
		}

		matcher = DELAY.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			try {
				Thread.sleep((int) (Float.valueOf(s).floatValue() * 1000));
			} catch (Exception e) {
				showException(ci, e);
			}
			return;
		}

		matcher = ADD.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("text")) {
				s = s.substring(4).trim();
				int i = s.indexOf("(");
				final int j = s.indexOf(")");
				if (i != -1 && j != -1) {
					final float[] z = parseArray(2, s.substring(i + 1, j));
					if (z != null) {
						final String s2 = s;
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								box.view2D.addText(s2.substring(j + 1), z[0], z[1]);
								box.view2D.repaint();
							}
						});
					}
				}
			} else if (s.toLowerCase().startsWith("image")) {
				s = s.substring(5).trim();
				int i = s.indexOf("(");
				int j = s.indexOf(")");
				if (i != -1 && j != -1) {
					final float[] z = parseArray(2, s.substring(i + 1, j));
					if (z != null) {
						String filename = s.substring(j + 1);
						URL url = null;
						try {
							url = new URL(box.getCodeBase(), filename);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						if (url != null) {
							final ImageIcon image = new ImageIcon(url);
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									box.view2D.addPicture(image, z[0], z[1]);
								}
							});
						}
					}
				}
			} else {
				showError(ci, "Unrecognized command");
			}
			return;
		}

		matcher = ATTACH.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("text")) {
				s = s.substring(4).trim();
				String[] t = s.split(REGEX_WHITESPACE + "*");
				if (t.length == 4) {
					System.out.println(java.util.Arrays.asList(t));
				}
			} else if (s.toLowerCase().startsWith("image")) {
				s = s.substring(5).trim();
			}
			return;
		}

		matcher = EFIELD.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			propagator.setElectricField("on".equalsIgnoreCase(s) ? new ElectricField2D() : null);
		}

		matcher = BFIELD.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			propagator.setMagneticField("on".equalsIgnoreCase(s) ? new MagneticField2D() : null);
		}

		matcher = WAVEPACKET.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("gaussian")) {
				s = s.substring(8).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 6) {
					try {
						float magnitude = Float.parseFloat(t[0]);
						float sigma = Float.parseFloat(t[1]);
						float muX = Float.parseFloat(t[2]);
						float muY = Float.parseFloat(t[3]);
						float px = Float.parseFloat(t[4]);
						float py = Float.parseFloat(t[5]);
						box.addGaussianWavePacket(magnitude, sigma, muX, muY, px, py);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("planewave")) {
				s = s.substring(9).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 7) {
					try {
						float magnitude = Float.parseFloat(t[0]);
						float x = Float.parseFloat(t[1]);
						float y = Float.parseFloat(t[2]);
						float w = Float.parseFloat(t[3]);
						float h = Float.parseFloat(t[4]);
						float px = Float.parseFloat(t[5]);
						float py = Float.parseFloat(t[6]);
						box.addPlaneWavePacket(magnitude, x, y, w, h, px, py);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}

		matcher = SOURCE.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("gaussian")) {
				s = s.substring(8).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 7) {
					try {
						float period = Float.parseFloat(t[0]);
						float magnitude = Float.parseFloat(t[1]);
						float sigma = Float.parseFloat(t[2]);
						float muX = Float.parseFloat(t[3]);
						float muY = Float.parseFloat(t[4]);
						float px = Float.parseFloat(t[5]);
						float py = Float.parseFloat(t[6]);
						box.addGaussianSource(period, magnitude, sigma, muX, muY, px, py);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("planewave")) {
				s = s.substring(9).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 8) {
					try {
						float period = Float.parseFloat(t[0]);
						float magnitude = Float.parseFloat(t[1]);
						float x = Float.parseFloat(t[2]);
						float y = Float.parseFloat(t[3]);
						float w = Float.parseFloat(t[4]);
						float h = Float.parseFloat(t[5]);
						float px = Float.parseFloat(t[6]);
						float py = Float.parseFloat(t[7]);
						box.addPlaneWaveSource(period, magnitude, x, y, w, h, px, py);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}

		matcher = POTENTIAL.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			if (s.toLowerCase().startsWith("ionic")) {
				s = s.substring(5).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 4) {
					try {
						float charge = Float.parseFloat(t[0]);
						float offset = Float.parseFloat(t[1]);
						float xcenter = Float.parseFloat(t[2]);
						float ycenter = Float.parseFloat(t[3]);
						box.addIonicPotential(false, charge, offset, xcenter, ycenter, null, true, true);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("rectangle")) {
				s = s.substring(9).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 5) {
					try {
						float energy = Float.parseFloat(t[0]);
						float xcenter = Float.parseFloat(t[1]);
						float ycenter = Float.parseFloat(t[2]);
						float xlength = Float.parseFloat(t[3]);
						float ylength = Float.parseFloat(t[4]);
						box.addRectangularPotential(false, energy, xcenter, ycenter, xlength, ylength, 0, null, true, true);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				} else if (t.length == 6) {
					try {
						float energy = Float.parseFloat(t[0]);
						float xcenter = Float.parseFloat(t[1]);
						float ycenter = Float.parseFloat(t[2]);
						float xlength = Float.parseFloat(t[3]);
						float ylength = Float.parseFloat(t[4]);
						float cornerRadius = Float.parseFloat(t[5]);
						box.addRectangularPotential(false, energy, xcenter, ycenter, xlength, ylength, cornerRadius, null, true, true);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("ellipse")) {
				s = s.substring(7).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 5) {
					try {
						float en = Float.parseFloat(t[0]);
						float xc = Float.parseFloat(t[1]);
						float yc = Float.parseFloat(t[2]);
						float rx = Float.parseFloat(t[3]);
						float ry = Float.parseFloat(t[4]);
						box.addEllipticalPotential(false, en, xc, yc, rx, ry, null, true, true);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			} else if (s.toLowerCase().startsWith("annulus")) {
				s = s.substring(7).trim();
				s = s.substring(1, s.length() - 1);
				String[] t = s.split(REGEX_SEPARATOR + "+");
				if (t.length == 7) {
					try {
						float en = Float.parseFloat(t[0]);
						float xc = Float.parseFloat(t[1]);
						float yc = Float.parseFloat(t[2]);
						float outerRx = Float.parseFloat(t[3]);
						float outerRy = Float.parseFloat(t[4]);
						float innerRx = Float.parseFloat(t[5]);
						float innerRy = Float.parseFloat(t[6]);
						box.addAnnularPotential(false, en, xc, yc, outerRx, outerRy, innerRx, innerRy, null, true, true);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}

		matcher = SET.matcher(ci);
		if (matcher.find()) {
			String s = ci.substring(matcher.end()).trim();
			String[] t = s.split(REGEX_WHITESPACE);
			if (t.length == 2) {
				if (t[0].equalsIgnoreCase("onlyprobability")) {
					box.view2D.setProbOnly("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("frank")) {
					String t1 = t[1].trim();
					box.view2D.setFrank(t1.equalsIgnoreCase("true"));
				} else if (t[0].equalsIgnoreCase("dotmode")) {
					box.view2D.setDotMode("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("dotcellsize")) {
					float dotCellSize = 0;
					try {
						dotCellSize = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.view2D.setDotCellSize(dotCellSize);
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("ruler")) {
					box.view2D.setRulerOn("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("grid")) {
					box.view2D.setGridOn("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("contour")) {
					box.view2D.setContourShown("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("contourresolution")) {
					float resolution = 0;
					try {
						resolution = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.view2D.setContourResolution(resolution);
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("drawexpectation")) {
					box.view2D.setDrawExpectation("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("intensityscale")) {
					float scale = 0;
					try {
						scale = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.view2D.setIntensityScale(scale);
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("drawenergy")) {
					box.view2D.setDrawEnergy("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("drawcurrent")) {
					box.view2D.setDrawCurrent("true".equalsIgnoreCase(t[1]));
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("energyscale")) {
					float scale = 0;
					try {
						scale = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.view2D.setEnergyScale(scale);
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("currentscale")) {
					float scale = 0;
					try {
						scale = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.view2D.setCurrentScale(scale);
					box.view2D.repaint();
				} else if (t[0].equalsIgnoreCase("xboundary")) {
					propagator.setBoundaryCondition('x', t[1]);
				} else if (t[0].equalsIgnoreCase("xboundarylayerlength")) {
					if (propagator.getBoundary('x') == null)
						propagator.setBoundaryCondition('x', "ABC");
					float layerLength = 0;
					try {
						layerLength = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					((AbsorbingBoundary) propagator.getBoundary('x')).setLengthPercentage(layerLength);
				} else if (t[0].equalsIgnoreCase("xboundarylayerabsorption")) {
					if (propagator.getBoundary('x') == null)
						propagator.setBoundaryCondition('x', "ABC");
					float absorption = 0;
					try {
						absorption = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					((AbsorbingBoundary) propagator.getBoundary('x')).setAbsorption(absorption);
				} else if (t[0].equalsIgnoreCase("yboundary")) {
					propagator.setBoundaryCondition('y', t[1]);
				} else if (t[0].equalsIgnoreCase("yboundarylayerlength")) {
					if (propagator.getBoundary('y') == null)
						propagator.setBoundaryCondition('y', "ABC");
					float layerLength = 0;
					try {
						layerLength = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					((AbsorbingBoundary) propagator.getBoundary('y')).setLengthPercentage(layerLength);
				} else if (t[0].equalsIgnoreCase("yboundarylayerabsorption")) {
					if (propagator.getBoundary('y') == null)
						propagator.setBoundaryCondition('y', "ABC");
					float absorption = 0;
					try {
						absorption = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					((AbsorbingBoundary) propagator.getBoundary('y')).setAbsorption(absorption);
				} else if (t[0].equalsIgnoreCase("efield_intensity")) {
					float intensity = 0;
					try {
						intensity = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator.getElectricField() != null) {
						propagator.getElectricField().setIntensity(intensity);
						box.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("efield_frequency")) {
					float frequency = 0;
					try {
						frequency = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator.getElectricField() != null) {
						propagator.getElectricField().setFrequency(frequency);
						box.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("efield_angle")) {
					float angle = 0;
					try {
						angle = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator.getElectricField() != null) {
						propagator.getElectricField().setAngle(angle);
						box.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("bfield_intensity")) {
					float intensity = 0;
					try {
						intensity = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator.getMagneticField() != null) {
						propagator.getMagneticField().setIntensity(intensity);
						box.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("bfield_frequency")) {
					float frequency = 0;
					try {
						frequency = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					if (propagator.getMagneticField() != null) {
						propagator.getMagneticField().setFrequency(frequency);
						box.visualizationRequested(null);
					}
				} else if (t[0].equalsIgnoreCase("mass")) {
					float mass = 0;
					try {
						mass = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					box.setMass(mass);
				} else if (t[0].equalsIgnoreCase("timestep")) {
					float timestep = 0;
					try {
						timestep = Float.parseFloat(t[1]);
					} catch (NumberFormatException e) {
						return;
					}
					propagator.setTimeStep(timestep);
				} else if (t[0].equalsIgnoreCase("probcolor")) {
					final Color c = MiscUtil.parseRGBColor(t[1]);
					if (c != null) {
						box.view2D.setProbColor(c);
					}
				} else if (t[0].equalsIgnoreCase("bgcolor")) {
					final Color c = MiscUtil.parseRGBColor(t[1]);
					if (c != null) {
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								box.view2D.setBackground(c);
							}
						});
					}
				} else {
					// wave packet field
					matcher = WAVEPACKET_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setWavePacketField(s1, s2, s3);
						box.requestRebuildingWaveFunction(true);
						return;
					}
					// potential field
					matcher = POTENTIAL_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setPotentialField(s1, s2, s3);
						// quantumBox.requestRebuildingWaveFunction(true);
						return;
					}
					// source field
					matcher = SOURCE_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setSourceField(s1, s2, s3);
						return;
					}
					// text field
					matcher = TEXT_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setTextField(s1, s2, s3);
						box.view2D.repaint();
						return;
					}
					// image field
					matcher = IMAGE_FIELD.matcher(s);
					if (matcher.find()) {
						int end = matcher.end();
						String s1 = s.substring(end).trim();
						int i = s1.indexOf(" ");
						if (i < 0) {
							return;
						}
						String s2 = s1.substring(0, i).trim();
						String s3 = s1.substring(i + 1).trim();
						s1 = s.substring(0, end - 1);
						setImageField(s1, s2, s3);
						return;
					}
				}
			}
			return;
		}

	}

	private void setPotentialField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= propagator.getPotentialCount()) {
			return;
		}
		if (str3.startsWith("#")) {
			try {
				z = Integer.parseInt(str3.substring(1), 16);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
			try {
				z = Integer.parseInt(str3.substring(2), 16);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else {
			try {
				z = Float.parseFloat(str3);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		String s = str2.toLowerCase().intern();
		Potential2D potential = propagator.getPotential(i);
		Potential2D oldP = box.potentialFactory.copy(potential);
		if (potential instanceof RectangularPotential) {
			RectangularPotential p = (RectangularPotential) potential;
			if (s == "width") {
				p.setLx(z);
			} else if (s == "height") {
				p.setLy(z);
			} else if (s == "x") {
				p.setXcenter(z);
			} else if (s == "y") {
				p.setYcenter(z);
			} else if (s == "color") {
				p.setColor(new Color((int) z));
			} else if (s == "energy") {
				p.setEnergy(z);
			}
		} else if (potential instanceof EllipticalPotential) {
			EllipticalPotential p = (EllipticalPotential) potential;
			if (s == "rx") {
				p.setRx(z);
			} else if (s == "ry") {
				p.setRy(z);
			} else if (s == "x") {
				p.setXcenter(z);
			} else if (s == "y") {
				p.setYcenter(z);
			} else if (s == "color") {
				p.setColor(new Color((int) z));
			} else if (s == "energy") {
				p.setEnergy(z);
			}
		} else if (potential instanceof AnnularPotential) {
			AnnularPotential p = (AnnularPotential) potential;
			if (s == "outerrx") {
				p.setOuterRx(z);
			} else if (s == "outerry") {
				p.setOuterRy(z);
			} else if (s == "innerrx") {
				p.setInnerRx(z);
			} else if (s == "innerry") {
				p.setInnerRy(z);
			} else if (s == "x") {
				p.setXcenter(z);
			} else if (s == "y") {
				p.setYcenter(z);
			} else if (s == "color") {
				p.setColor(new Color((int) z));
			} else if (s == "energy") {
				p.setEnergy(z);
			}
		} else if (potential instanceof IonicPotential) {
			IonicPotential p = (IonicPotential) potential;
			if (s == "charge") {
				p.setCharge(z);
			} else if (s == "offset") {
				p.setOffset(z);
			} else if (s == "x") {
				p.setXcenter(z);
			} else if (s == "y") {
				p.setYcenter(z);
			} else if (s == "color") {
				p.setColor(new Color((int) z));
			}
		}
		box.propagator.removePotentialFunction(oldP.isImaginary(), oldP.getPotential());
		Potential2D newP = box.potentialFactory.copy(potential);
		box.propagator.addPotentialFunction(newP.isImaginary(), newP.getPotential());
		box.view2D.repaint();
	}

	private void setWavePacketField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= propagator.getWavePacketCount()) {
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String s = str2.toLowerCase().intern();
		WavePacket2D wave = propagator.getWavePacket(i);
		if (s == "magnitude")
			wave.setMagnitude(z);
		else if (s == "px")
			wave.setPx(z);
		else if (s == "py")
			wave.setPy(z);
		else if (s == "sigma") {
			if (wave instanceof Gaussian2D) {
				Gaussian2D g = (Gaussian2D) wave;
				g.setSigma(z);
			}
		} else if (s == "mux") {
			if (wave instanceof Gaussian2D) {
				Gaussian2D g = (Gaussian2D) wave;
				g.setMuX(z);
			}
		} else if (s == "muy") {
			if (wave instanceof Gaussian2D) {
				Gaussian2D g = (Gaussian2D) wave;
				g.setMuY(z);
			}
		}
	}

	private void setSourceField(String str1, String str2, String str3) {
		if (!(propagator instanceof RealTimePropagator2D))
			return;
		RealTimePropagator2D rtp = (RealTimePropagator2D) propagator;
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= rtp.getSourceCount()) {
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String s = str2.toLowerCase().intern();
		Source source = rtp.getSource(i);
		if (s == "magnitude")
			source.setAmplitude(z);
		else if (s == "period")
			source.setPeriod(z);
		else if (s == "px")
			source.setPx(z);
		else if (s == "py")
			source.setPy(z);
		else if (s == "sigma") {
			if (source instanceof PointSource) {
				PointSource ps = (PointSource) source;
				ps.setSigma(z);
			}
		} else if (s == "mux") {
			if (source instanceof PointSource) {
				PointSource ps = (PointSource) source;
				ps.setXcenter(z);
			}
		} else if (s == "muy") {
			if (source instanceof PointSource) {
				PointSource ps = (PointSource) source;
				ps.setYcenter(z);
			}
		}
	}

	private void setTextField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= box.view2D.getTextBoxCount()) {
			return;
		}
		final TextBox text = box.view2D.getTextBox(i);
		if (text == null)
			return;
		String s = str2.toLowerCase().intern();
		if (s == "name") {
			text.setName(str3);
		} else if (s == "text") {
			text.setText(str3);
		} else {
			if (str3.startsWith("#")) {
				try {
					z = Integer.parseInt(str3.substring(1), 16);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} else if (str3.startsWith("0X") || str3.startsWith("0x")) {
				try {
					z = Integer.parseInt(str3.substring(2), 16);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} else {
				try {
					z = Float.parseFloat(str3);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			if (s == "color") {
				text.setColor(new Color((int) z));
			} else if (s == "x") {
				final float z2 = z;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						text.setX(z2);
					}
				});
			} else if (s == "y") {
				final float z2 = z;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						text.setY(z2);
					}
				});
			} else if (s == "size") {
				text.setSize((int) z);
			} else if (s == "style") {
				text.setStyle((int) z);
			}
		}
	}

	private void setImageField(String str1, String str2, String str3) {
		int lb = str1.indexOf("[");
		int rb = str1.indexOf("]");
		float z = 0;
		try {
			z = Float.parseFloat(str1.substring(lb + 1, rb));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int i = (int) Math.round(z);
		if (i < 0 || i >= box.view2D.getPictureCount()) {
			return;
		}
		try {
			z = Float.parseFloat(str3);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String s = str2.toLowerCase().intern();
		final Picture picture = box.view2D.getPicture(i);
		if (picture == null)
			return;
		if (s == "x") {
			final float z2 = z;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					picture.setX(z2);
				}
			});
		} else if (s == "y") {
			final float z2 = z;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					picture.setY(z2);
				}
			});
		}
	}

}
