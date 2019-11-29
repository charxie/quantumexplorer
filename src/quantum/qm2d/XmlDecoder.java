package quantum.qm2d;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import quantum.qm2d.model.ElectricField2D;
import quantum.qm2d.model.MagneticField2D;
import quantum.qm2d.view.TextBox;
import quantum.qmshared.AbsorbingBoundary;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Charles Xie
 * 
 */
class XmlDecoder extends DefaultHandler {

	private QuantumBox box;
	private String str;
	private int nx = 200, ny = 200;
	private float xmin = -10, xmax = 10, ymin = -10, ymax = 10;
	private boolean itp;
	private boolean ruler;
	private boolean grid;
	private boolean contour;
	private float contourScale = 5;
	private boolean probOnly;
	private boolean dotMode;
	private float dotCellSize;
	private float intensityScale = 1;
	private boolean drawEnergy;
	private boolean drawCurrent;
	private float energyScale = 5;
	private boolean drawExpectation;
	private boolean frank = true;
	private float timeStep = 2;
	private int bgColor = 0x000000;
	private int probColor = 0x0099ff;

	XmlDecoder(QuantumBox box) {
		this.box = box;
	}

	private void resetGlobalVariables() {
		itp = false;
		frank = true;
		xmin = -10;
		xmax = 10;
		ymin = -10;
		ymax = 10;
		nx = 200;
		ny = 200;
		timeStep = 2;
		ruler = false;
		grid = false;
		contour = false;
		contourScale = 5;
		probOnly = false;
		dotMode = false;
		dotCellSize = 0;
		intensityScale = 1;
		drawEnergy = false;
		drawCurrent = false;
		energyScale = 5;
		drawExpectation = false;
		bgColor = 0x000000;
		probColor = 0x0099ff;
	}

	public void startDocument() {
	}

	public void endDocument() {
		box.visualizationRequested(null);
		box.view2D.setFrank(frank);
		box.setNx(nx);
		box.setNy(ny);
		box.setArea(xmin, xmax, ymin, ymax);
		box.propagator.setTimeStep(timeStep);
		box.view2D.setRulerOn(ruler);
		box.view2D.setGridOn(grid);
		box.view2D.setContourShown(contour);
		box.view2D.setContourResolution(contourScale);
		box.view2D.setProbOnly(probOnly);
		box.view2D.setDotMode(dotMode);
		if (dotCellSize > 0)
			box.view2D.setDotCellSize(dotCellSize);
		box.view2D.setDrawEnergy(drawEnergy);
		box.view2D.setDrawCurrent(drawCurrent);
		box.view2D.setEnergyScale(energyScale);
		box.view2D.setDrawExpectation(drawExpectation);
		box.view2D.setProbColor(new Color(probColor));
		box.view2D.setIntensityScale(intensityScale);
		if (bgColor != 0x000000) {
			final int bgColor2 = bgColor;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					box.view2D.setBackground(new Color(bgColor2));
				}
			});
		}
		box.propagator.reset();
		// box.propagator.buildWaveFunction();
		box.view2D.repaint();
		resetGlobalVariables();
	}

	public void startElement(String uri, String localName, String qName, Attributes attrib) {

		String attribName, attribValue;

		if (qName == "rectangular") {
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float width = 1;
				float height = 1;
				float corner = 0;
				float energy = 0;
				boolean visible = true;
				boolean draggable = true;
				boolean imaginary = false;
				Color color = null;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "energy") {
						energy = Float.parseFloat(attribValue);
					} else if (attribName == "imaginary") {
						imaginary = Boolean.parseBoolean(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						width = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						height = Float.parseFloat(attribValue);
					} else if (attribName == "corner") {
						corner = Float.parseFloat(attribValue);
					} else if (attribName == "visible") {
						visible = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				box.addRectangularPotential(imaginary, energy, xcenter, ycenter, width, height, corner, color, visible, draggable);
			}
		} else if (qName == "elliptical") {
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float rx = 1;
				float ry = 1;
				float energy = 0;
				Color color = null;
				boolean visible = true;
				boolean draggable = true;
				boolean imaginary = false;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "energy") {
						energy = Float.parseFloat(attribValue);
					} else if (attribName == "imaginary") {
						imaginary = Boolean.parseBoolean(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "rx") {
						rx = Float.parseFloat(attribValue);
					} else if (attribName == "ry") {
						ry = Float.parseFloat(attribValue);
					} else if (attribName == "visible") {
						visible = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				box.addEllipticalPotential(imaginary, energy, xcenter, ycenter, rx, ry, color, visible, draggable);
			}
		} else if (qName == "annular") {
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float innerRx = 1;
				float innerRy = 1;
				float outerRx = 1;
				float outerRy = 1;
				float energy = 0;
				Color color = null;
				boolean visible = true;
				boolean draggable = true;
				boolean imaginary = false;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "energy") {
						energy = Float.parseFloat(attribValue);
					} else if (attribName == "imaginary") {
						imaginary = Boolean.parseBoolean(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "innerrx") {
						innerRx = Float.parseFloat(attribValue);
					} else if (attribName == "innerry") {
						innerRy = Float.parseFloat(attribValue);
					} else if (attribName == "outerrx") {
						outerRx = Float.parseFloat(attribValue);
					} else if (attribName == "outerry") {
						outerRy = Float.parseFloat(attribValue);
					} else if (attribName == "visible") {
						visible = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				box.addAnnularPotential(imaginary, energy, xcenter, ycenter, outerRx, outerRy, innerRx, innerRy, color, visible, draggable);
			}
		} else if (qName == "ionic") {
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float offset = 1;
				float charge = 0;
				Color color = null;
				boolean visible = true;
				boolean draggable = true;
				boolean imaginary = false;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "charge") {
						charge = Float.parseFloat(attribValue);
					} else if (attribName == "imaginary") {
						imaginary = Boolean.parseBoolean(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "offset") {
						offset = Float.parseFloat(attribValue);
					} else if (attribName == "visible") {
						visible = Boolean.parseBoolean(attribValue);
					} else if (attribName == "draggable") {
						draggable = Boolean.parseBoolean(attribValue);
					} else if (attribName == "color") {
						color = new Color(Integer.parseInt(attribValue, 16));
					}
				}
				box.addIonicPotential(imaginary, charge, offset, xcenter, ycenter, color, visible, draggable);
			}
		} else if (qName == "point") {// point source
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float period = 1;
				float amplitude = 0;
				float px = 0;
				float py = 0;
				float sigma = 1;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "period") {
						period = Float.parseFloat(attribValue);
					} else if (attribName == "amplitude") {
						amplitude = Float.parseFloat(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "px") {
						px = Float.parseFloat(attribValue);
					} else if (attribName == "py") {
						py = Float.parseFloat(attribValue);
					} else if (attribName == "sigma") {
						sigma = Float.parseFloat(attribValue);
					}
				}
				box.addGaussianSource(period, amplitude, sigma, xcenter, ycenter, px, py);
			}
		} else if (qName == "planewave") {// plane wave source
			if (attrib != null) {
				float xcenter = 0;
				float ycenter = 0;
				float period = 1;
				float amplitude = 0;
				float px = 0;
				float py = 0;
				float width = 0;
				float height = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "period") {
						period = Float.parseFloat(attribValue);
					} else if (attribName == "amplitude") {
						amplitude = Float.parseFloat(attribValue);
					} else if (attribName == "xcenter") {
						xcenter = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ycenter = Float.parseFloat(attribValue);
					} else if (attribName == "px") {
						px = Float.parseFloat(attribValue);
					} else if (attribName == "py") {
						py = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						width = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						height = Float.parseFloat(attribValue);
					}
				}
				box.addPlaneWaveSource(period, amplitude, xcenter, ycenter, width, height, px, py);
			}
		} else if (qName == "gaussian") {// gaussian wave packet
			if (attrib != null) {
				float magnitude = 0;
				float sigma = 0;
				float rx = 0;
				float ry = 0;
				float px = 0;
				float py = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "magnitude") {
						magnitude = Float.parseFloat(attribValue);
					} else if (attribName == "sigma") {
						sigma = Float.parseFloat(attribValue);
					} else if (attribName == "xcenter") {
						rx = Float.parseFloat(attribValue);
					} else if (attribName == "ycenter") {
						ry = Float.parseFloat(attribValue);
					} else if (attribName == "px") {
						px = Float.parseFloat(attribValue);
					} else if (attribName == "py") {
						py = Float.parseFloat(attribValue);
					}
				}
				box.addGaussianWavePacket(magnitude, sigma, rx, ry, px, py);
			}
		} else if (qName == "uniform_rectangle") {// uniform rectangular wave packet
			if (attrib != null) {
				float magnitude = 0;
				float w = 0;
				float h = 0;
				float x = 0;
				float y = 0;
				float px = 0;
				float py = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "magnitude") {
						magnitude = Float.parseFloat(attribValue);
					} else if (attribName == "width") {
						w = Float.parseFloat(attribValue);
					} else if (attribName == "height") {
						h = Float.parseFloat(attribValue);
					} else if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "px") {
						px = Float.parseFloat(attribValue);
					} else if (attribName == "py") {
						py = Float.parseFloat(attribValue);
					}
				}
				box.addPlaneWavePacket(magnitude, x, y, w, h, px, py);
			}
		} else if (qName == "efield") {
			if (attrib != null) {
				float intensity = 0.01f;
				float frequency = 1;
				float phase = 0;
				float angle = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "intensity") {
						intensity = Float.parseFloat(attribValue);
					} else if (attribName == "frequency") {
						frequency = Float.parseFloat(attribValue);
					} else if (attribName == "phase") {
						phase = Float.parseFloat(attribValue);
					} else if (attribName == "angle") {
						angle = Float.parseFloat(attribValue);
					}
				}
				ElectricField2D eField = box.propagator.getElectricField();
				if (eField == null) {
					eField = new ElectricField2D();
					box.propagator.setElectricField(eField);
				}
				eField.setAngle(angle);
				eField.setFrequency(frequency);
				eField.setIntensity(intensity);
				eField.setPhase(phase);
			}
		} else if (qName == "bfield") {
			if (attrib != null) {
				float intensity = 0.01f;
				float frequency = 1;
				float phase = 0;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "intensity") {
						intensity = Float.parseFloat(attribValue);
					} else if (attribName == "frequency") {
						frequency = Float.parseFloat(attribValue);
					} else if (attribName == "phase") {
						phase = Float.parseFloat(attribValue);
					}
				}
				MagneticField2D bField = box.propagator.getMagneticField();
				if (bField == null) {
					bField = new MagneticField2D();
					box.propagator.setMagneticField(bField);
				}
				bField.setFrequency(frequency);
				bField.setIntensity(intensity);
				bField.setPhase(phase);
			}
		} else if (qName == "particle") {
			if (attrib != null) {
				float mass = 1f;
				float charge = -1;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "mass") {
						mass = Float.parseFloat(attribValue);
					} else if (attribName == "charge") {
						charge = Float.parseFloat(attribValue);
					}
				}
				box.particle.setCharge(charge);
				box.setMass(mass);
			}
		} else if (qName == "absorbing_boundary") {
			if (attrib != null) {
				float lengthPercentage = 0.1f;
				float absorption = 0.001f;
				char direction = 'x';
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "direction") {
						direction = attribValue.charAt(0);
					} else if (attribName == "length_percentage") {
						lengthPercentage = Float.parseFloat(attribValue);
					} else if (attribName == "absorption") {
						absorption = Float.parseFloat(attribValue);
					}
				}
				box.propagator.setBoundaryCondition(direction, "ABC");
				AbsorbingBoundary b = (AbsorbingBoundary) box.propagator.getBoundary(direction);
				b.setLengthPercentage(lengthPercentage);
				b.setAbsorption(absorption);
				box.view2D.setBoundary(direction, b);
			}
		} else if (qName == "textbox") {// text box
			if (attrib != null) {
				float x = 0;
				float y = 0;
				int rgb = Color.black.getRGB();
				int size = 12;
				String text = null;
				String name = "Arial";
				int style = Font.PLAIN | Font.BOLD;
				for (int i = 0, n = attrib.getLength(); i < n; i++) {
					attribName = attrib.getQName(i).intern();
					attribValue = attrib.getValue(i);
					if (attribName == "text") {
						text = attribValue;
					} else if (attribName == "name") {
						name = attribValue;
					} else if (attribName == "x") {
						x = Float.parseFloat(attribValue);
					} else if (attribName == "y") {
						y = Float.parseFloat(attribValue);
					} else if (attribName == "size") {
						size = Integer.parseInt(attribValue);
					} else if (attribName == "style") {
						style = Integer.parseInt(attribValue);
					} else if (attribName == "color") {
						rgb = Integer.parseInt(attribValue, 16);
					}
				}
				box.view2D.addText(text, x, y);
				TextBox tb = box.view2D.getTextBox(box.view2D.getTextBoxCount() - 1);
				tb.setStyle(style);
				tb.setSize(size);
				tb.setColor(new Color(rgb));
				tb.setName(name);
				box.view2D.repaint();
			}
		}

	}

	public void endElement(String uri, String localName, String qName) {

		if (qName == "itp") {
			itp = Boolean.parseBoolean(str);
			box.setPropagator(itp);
		} else if (qName == "xmin") {
			xmin = Float.parseFloat(str);
			box.xmin = xmin;
		} else if (qName == "xmax") {
			xmax = Float.parseFloat(str);
			box.xmax = xmax;
		} else if (qName == "ymin") {
			ymin = Float.parseFloat(str);
			box.ymin = ymin;
		} else if (qName == "ymax") {
			ymax = Float.parseFloat(str);
			box.ymax = ymax;
		} else if (qName == "nx") {
			nx = Integer.parseInt(str);
		} else if (qName == "ny") {
			ny = Integer.parseInt(str);
		} else if (qName == "timestep") {
			timeStep = Float.parseFloat(str);
		} else if (qName == "ruler") {
			ruler = Boolean.parseBoolean(str);
		} else if (qName == "contour") {
			contour = Boolean.parseBoolean(str);
		} else if (qName == "grid") {
			grid = Boolean.parseBoolean(str);
		} else if (qName == "probonly") {
			probOnly = Boolean.parseBoolean(str);
		} else if (qName == "dotmode") {
			dotMode = Boolean.parseBoolean(str);
		} else if (qName == "dotcellsize") {
			dotCellSize = Float.parseFloat(str);
		} else if (qName == "drawenergy") {
			drawEnergy = Boolean.parseBoolean(str);
		} else if (qName == "drawcurrent") {
			drawCurrent = Boolean.parseBoolean(str);
		} else if (qName == "energyscale") {
			energyScale = Float.parseFloat(str);
		} else if (qName == "intensityscale") {
			intensityScale = Float.parseFloat(str);
		} else if (qName == "drawexpectation") {
			drawExpectation = Boolean.parseBoolean(str);
		} else if (qName == "frank") {
			frank = Boolean.parseBoolean(str);
		} else if (qName == "probcolor") {
			probColor = Integer.parseInt(str, 16);
		} else if (qName == "bgcolor") {
			bgColor = Integer.parseInt(str, 16);
		}

	}

	public void characters(char[] ch, int start, int length) {
		str = new String(ch, start, length);
	}

	public void warning(SAXParseException e) {
		e.printStackTrace();
	}

	public void error(SAXParseException e) {
		e.printStackTrace();
	}

	public void fatalError(SAXParseException e) {
		e.printStackTrace();
	}

}
