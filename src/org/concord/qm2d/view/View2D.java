package org.concord.qm2d.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.concord.math.Vector2D;
import org.concord.qm2d.QuantumBox;
import org.concord.qm2d.model.AnnularPotential;
import org.concord.qm2d.model.AreaPotential;
import org.concord.qm2d.model.ElectricField2D;
import org.concord.qm2d.model.EllipticalPotential;
import org.concord.qm2d.model.IonicPotential;
import org.concord.qm2d.model.MagneticField2D;
import org.concord.qm2d.model.PointPotential;
import org.concord.qm2d.model.PointSource;
import org.concord.qm2d.model.Potential2D;
import org.concord.qm2d.model.RealTimePropagator2D;
import org.concord.qm2d.model.RectangularPotential;
import org.concord.qm2d.model.Source;
import org.concord.qm2d.model.TimePropagator2D;
import org.concord.qmevent.ManipulationEvent;
import org.concord.qmevent.ManipulationListener;
import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Boundary;
import org.concord.qmutil.ContourPlot;
import org.concord.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
public class View2D extends JPanel {

	private static final long serialVersionUID = 1L;
	public final static byte SELECT_MODE = 0;
	public final static byte RECTANGLE_MODE = 1;
	public final static byte ELLIPSE_MODE = 2;

	public final static byte UPPER_LEFT = 0;
	public final static byte LOWER_LEFT = 1;
	public final static byte UPPER_RIGHT = 2;
	public final static byte LOWER_RIGHT = 3;
	public final static byte TOP = 4;
	public final static byte BOTTOM = 5;
	public final static byte LEFT = 6;
	public final static byte RIGHT = 7;
	final static byte HANDLE1 = 8;
	final static byte HANDLE2 = 9;

	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");
	private final static int MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL = 20;

	private final static double COS45 = Math.cos(Math.toRadians(45.0));
	private final static double SIN45 = Math.sin(Math.toRadians(45.0));

	QuantumBox quantumBox;
	private BufferedImage bimg;
	private byte actionMode = SELECT_MODE;
	private Stroke thinStroke = new BasicStroke(1);
	private Stroke momentumStroke = new BasicStroke(2);
	private static Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2 }, 0);
	private static Color fieldColor = new Color(0, 0, 127, 127);
	private static Font font = new Font(null, Font.PLAIN, 11);
	private List<Potential2D> potentials;
	private PotentialRenderer potentialRenderer;
	private GridRenderer gridRenderer;
	private RulerRenderer rulerRenderer;
	private WaveFunctionRenderer waveFunctionRenderer;
	private CurrentRenderer currentRenderer;
	private float[][] prob, phase;
	private Vector2D position, momentum;
	private Boundary xBoundary, yBoundary;
	private float xmin, xmax, ymin, ymax;
	private boolean drawExpectation, drawEnergy, drawCurrent;
	private boolean frank = true;
	private ElectricField2D eField;
	private MagneticField2D bField;
	private float kinE, potE, totE;
	private float time, timeStep;
	private float energyScale = 5;
	private GeneralPath path;
	private Potential2D selectedPotential;
	private JPopupMenu popupMenu;
	private Rectangle[] rect = new Rectangle[8];
	private Polygon[] handle = new Polygon[2];
	private Ellipse2D.Float halo = new Ellipse2D.Float(0, 0, 20, 20);
	private boolean mouseBeingDragged;
	private MovingShape movingShape;
	private Point pressedPointRelative = new Point();
	private long mousePressedTime;
	private byte selectedSpot = -1;
	private Point anchorPoint = new Point();
	private ContourPlot contourPlot;
	private List<TextBox> textBoxes;
	private List<Picture> pictures;

	private DialogFactory dialogFactory;
	private JPopupMenu tipPopupMenu;

	private List<ManipulationListener> manipulationListeners;

	private Action copyAction;
	private Action cutAction;
	private Action pasteAction;

	public View2D(QuantumBox quantumBox) {
		super();
		for (int i = 0; i < rect.length; i++)
			rect[i] = new Rectangle(0, 0, 6, 6);
		for (int i = 0; i < handle.length; i++)
			handle[i] = new Polygon();
		this.quantumBox = quantumBox;
		potentialRenderer = new PotentialRenderer();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				processKeyPressed(e);
			}

			public void keyReleased(KeyEvent e) {
				processKeyReleased(e);
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				processMousePressed(e);
			}

			public void mouseReleased(MouseEvent e) {
				processMouseReleased(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				processMouseMoved(e);
			}

			public void mouseDragged(MouseEvent e) {
				processMouseDragged(e);
			}
		});
		createActions();
		createPopupMenu();
		dialogFactory = new DialogFactory(quantumBox);
		manipulationListeners = new ArrayList<ManipulationListener>();
	}

	@SuppressWarnings("serial")
	private void createActions() {

		cutAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		};
		KeyStroke ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK);
		cutAction.putValue(Action.NAME, "Cut");
		cutAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Cut");
		getActionMap().put("Cut", cutAction);

		copyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK);
		copyAction.putValue(Action.NAME, "Copy");
		copyAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Copy");
		getActionMap().put("Copy", copyAction);

		pasteAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		};
		ks = IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
		pasteAction.putValue(Action.NAME, "Paste");
		pasteAction.putValue(Action.ACCELERATOR_KEY, ks);
		getInputMap().put(ks, "Paste");
		getActionMap().put("Paste", pasteAction);

	}

	public void setActionMode(byte mode) {
		actionMode = mode;
		switch (mode) {
		case SELECT_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		case RECTANGLE_MODE:
		case ELLIPSE_MODE:
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			break;
		}
		repaint();
	}

	public byte getActionMode() {
		return actionMode;
	}

	public void clear() {
		potentials.clear();
		if (textBoxes != null)
			textBoxes.clear();
		if (pictures != null)
			pictures.clear();
		xBoundary = null;
		yBoundary = null;
		if (waveFunctionRenderer != null) {
			waveFunctionRenderer.setBoundary('x', null);
			waveFunctionRenderer.setBoundary('y', null);
		}
	}

	public void addText(String text, float x, float y) {
		if (textBoxes == null)
			textBoxes = new ArrayList<TextBox>();
		textBoxes.add(new TextBox(text, x, y));
	}

	public int getTextBoxCount() {
		if (textBoxes == null)
			return 0;
		return textBoxes.size();
	}

	public TextBox getTextBox(int i) {
		if (textBoxes == null)
			return null;
		if (i < 0 || i >= textBoxes.size())
			return null;
		return textBoxes.get(i);
	}

	public void addPicture(ImageIcon image, float x, float y) {
		if (pictures == null)
			pictures = new ArrayList<Picture>();
		pictures.add(new Picture(image, quantumBox.getView().convertPointToPixelX(x), quantumBox.getView().convertPointToPixelY(y)));
	}

	public int getPictureCount() {
		if (pictures == null)
			return 0;
		return pictures.size();
	}

	public Picture getPicture(int i) {
		if (pictures == null)
			return null;
		if (i < 0 || i >= pictures.size())
			return null;
		return pictures.get(i);
	}

	public void addManipulationListener(ManipulationListener l) {
		if (!manipulationListeners.contains(l))
			manipulationListeners.add(l);
	}

	public void removeManipulationListener(ManipulationListener l) {
		manipulationListeners.remove(l);
	}

	public void notifyManipulationListeners(Object m, byte type) {
		if (manipulationListeners.isEmpty())
			return;
		ManipulationEvent e = new ManipulationEvent(this, m, type);
		for (ManipulationListener l : manipulationListeners) {
			l.manipulationOccured(e);
		}
	}

	public void createDialog(Object o) {
		JDialog d = dialogFactory.createDialog(o);
		if (d != null)
			d.setVisible(true);
	}

	public JPopupMenu getPopupMenu() {
		return popupMenu;
	}

	private void cut() {
		if (selectedPotential != null) {
			quantumBox.removePotential(selectedPotential);
			setSelectedPotential(null);
			repaint();
		}
	}

	private void copy() {
		if (selectedPotential != null) {
		}
	}

	private void paste() {
		if (selectedPotential != null) {
			repaint();
		}
	}

	private void createPopupMenu() {

		if (popupMenu != null)
			return;

		popupMenu = new JPopupMenu();
		popupMenu.setInvoker(this);

		JMenuItem mi = new JMenuItem("Copy");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("Cut");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		});
		popupMenu.add(mi);

		mi = new JMenuItem("Paste");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		popupMenu.add(mi);
		popupMenu.addSeparator();

		mi = new JMenuItem("Properties");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedPotential != null) {
					JDialog dialog = dialogFactory.createDialog(selectedPotential);
					if (dialog != null) { // FIXME: should allow user to see the selected potential
						dialog.setLocationRelativeTo(View2D.this);
						dialog.setVisible(true);
					}
				} else {
					JDialog dialog = dialogFactory.createDialog(quantumBox);
					if (dialog != null) {
						dialog.setLocationRelativeTo(View2D.this);
						dialog.setVisible(true);
					}
				}
			}
		});

		popupMenu.add(mi);

	}

	public void setArea(float xmin, float xmax, float ymin, float ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		potentialRenderer.setArea(xmin, xmax, ymin, ymax);
	}

	public void setBoundary(char direction, Boundary boundary) {
		if (direction == 'x' || direction == 'X') {
			xBoundary = boundary;
			potentialRenderer.setBoundary('x', xBoundary);
		} else if (direction == 'y' || direction == 'Y') {
			yBoundary = boundary;
			potentialRenderer.setBoundary('y', yBoundary);
		}
	}

	public void setEnergies(float kinE, float potE, float totE) {
		this.kinE = kinE;
		this.potE = potE;
		this.totE = totE;
	}

	public void setExpectationPosition(Vector2D position) {
		this.position = position;
	}

	public void setExpectationMomentum(Vector2D momentum) {
		this.momentum = momentum;
	}

	public void setCurrent(Vector2D[][] current) {
		currentRenderer.setCurrent(current);
	}

	public void setFrank(boolean b) {
		frank = b;
	}

	public boolean isFrank() {
		return frank;
	}

	public void setContourShown(boolean b) {
		if (b) {
			if (contourPlot == null)
				contourPlot = new ContourPlot();
		} else {
			contourPlot = null;
		}
	}

	public boolean isContourShown() {
		return contourPlot != null;
	}

	public void setContourResolution(float resolution) {
		if (contourPlot != null)
			contourPlot.setResolutionScale(resolution);
	}

	public float getContourResolution() {
		if (contourPlot == null)
			return 5;
		return contourPlot.getResolutionScale();
	}

	public void setProbColor(Color c) {
		if (waveFunctionRenderer != null)
			waveFunctionRenderer.setProbColor(c);
	}

	public Color getProbColor() {
		if (waveFunctionRenderer == null)
			return null;
		return waveFunctionRenderer.getProbColor();
	}

	public void setProbOnly(boolean b) {
		if (waveFunctionRenderer != null)
			waveFunctionRenderer.setProbOnly(b);
	}

	public boolean isProbOnly() {
		if (waveFunctionRenderer == null)
			return false;
		return waveFunctionRenderer.isProbOnly();
	}

	public void setDotMode(boolean b) {
		if (waveFunctionRenderer != null)
			waveFunctionRenderer.setDotMode(b);
	}

	public boolean getDotMode() {
		if (waveFunctionRenderer == null)
			return false;
		return waveFunctionRenderer.getDotMode();
	}

	public void setDotCellSize(float dotCellSize) {
		if (waveFunctionRenderer != null) {
			waveFunctionRenderer.setDotCellSize(convertLengthToPixelX(dotCellSize));
		}
	}

	public float getDotCellSize() {
		if (waveFunctionRenderer == null)
			return 4;
		return convertPixelToLengthX(waveFunctionRenderer.getDotCellSize());
	}

	public void setDrawCurrent(boolean b) {
		drawCurrent = b;
		if (quantumBox != null) {
			quantumBox.getPropagator().setComputeCurrent(b);
		}
		if (b && currentRenderer == null)
			currentRenderer = new CurrentRenderer();
		if (currentRenderer != null)
			currentRenderer.setBoundary(xBoundary, yBoundary);
	}

	public boolean getDrawCurrent() {
		return drawCurrent;
	}

	public void setDrawExpectation(boolean b) {
		drawExpectation = b;
		if (quantumBox != null) {
			quantumBox.getPropagator().setComputeExpectation(b);
		}
	}

	public boolean getDrawExpectation() {
		return drawExpectation;
	}

	public void setDrawEnergy(boolean b) {
		drawEnergy = b;
		if (quantumBox != null) {
			quantumBox.getPropagator().setComputeEnergy(b);
		}
	}

	public boolean getDrawEnergy() {
		return drawEnergy;
	}

	public void setEnergyScale(float scale) {
		energyScale = scale;
	}

	public float getEnergyScale() {
		return energyScale;
	}

	public void setCurrentScale(float scale) {
		if (currentRenderer == null)
			currentRenderer = new CurrentRenderer();
		currentRenderer.setScale(scale);
	}

	public void setIntensityScale(float scale) {
		if (waveFunctionRenderer != null)
			waveFunctionRenderer.setIntensityScale(scale);
	}

	public float getIntensityScale() {
		if (waveFunctionRenderer == null)
			return 1;
		return waveFunctionRenderer.getIntensityScale();
	}

	public void setGridOn(boolean b) {
		gridRenderer = b ? new GridRenderer(quantumBox.getNx(), quantumBox.getNy()) : null;
	}

	public boolean isGridOn() {
		return gridRenderer != null;
	}

	public void setRulerOn(boolean b) {
		rulerRenderer = b ? new RulerRenderer() : null;
		if (b) {
			float xMargin = 0;
			float yMargin = 0;
			if (xBoundary instanceof AbsorbingBoundary) {
				AbsorbingBoundary ab = (AbsorbingBoundary) xBoundary;
				xMargin = ab.getLengthPercentage() * (xmax - xmin);
			}
			if (yBoundary instanceof AbsorbingBoundary) {
				AbsorbingBoundary ab = (AbsorbingBoundary) yBoundary;
				yMargin = ab.getLengthPercentage() * (ymax - ymin);
			}
			rulerRenderer.setSize(xmin + xMargin, xmax - xMargin, ymin + yMargin, ymax - yMargin);
		}
	}

	public boolean isRulerOn() {
		return rulerRenderer != null;
	}

	public void setPotential(List<Potential2D> potentials) {
		this.potentials = potentials;
	}

	public void setProbability(float[][] prob) {
		this.prob = prob;
		createWaveFunctionRenderer();
	}

	public void setPhase(float[][] phase) {
		this.phase = phase;
		createWaveFunctionRenderer();
	}

	public void setTimeInfo(float time, float timeStep) {
		this.time = time;
		this.timeStep = timeStep;
	}

	public void setElectricField(ElectricField2D eField) {
		this.eField = eField;
	}

	public void setMagneticField(MagneticField2D bField) {
		this.bField = bField;
	}

	private void createWaveFunctionRenderer() {
		if (waveFunctionRenderer == null)
			waveFunctionRenderer = new WaveFunctionRenderer();
		waveFunctionRenderer.setBoundary('x', xBoundary);
		waveFunctionRenderer.setBoundary('y', yBoundary);
	}

	private Graphics2D createGraphics2D() {
		int w = getWidth();
		int h = getHeight();
		Graphics2D g;
		if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
			bimg = (BufferedImage) createImage(w, h);
		}
		g = bimg.createGraphics();
		g.setBackground(getBackground());
		g.clearRect(0, 0, w, h);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return g;
	}

	/*
	 * Need to use this old double-buffering technique in order to avoid flickering when run as an applet on the Mac
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = createGraphics2D();
		draw(g2);
		g2.dispose();
		if (bimg != null)
			g.drawImage(bimg, 0, 0, this);
	}

	private void draw(Graphics2D g) {
		int w = getWidth();
		int h = getHeight();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(getBackground());
		g.fillRect(0, 0, w, h);
		Color contrast = MiscUtil.getContrastColor(getBackground());
		if (potentialRenderer != null && potentials != null) {
			synchronized (potentials) {
				for (Potential2D potential : potentials) {
					if (potential instanceof AreaPotential) {
						AreaPotential a = (AreaPotential) potential;
						if (a.getEnergy() <= 0)
							potentialRenderer.render(potential, this, g2);
					}
				}
			}
		}
		if (waveFunctionRenderer != null) {
			waveFunctionRenderer.renderProbility(prob, phase, this, g2);
		}
		if (potentialRenderer != null && potentials != null) {
			synchronized (potentials) {
				for (Potential2D potential : potentials) {
					if (potential instanceof PointPotential) {
						potentialRenderer.render(potential, this, g2);
					} else if (potential instanceof AreaPotential) {
						AreaPotential a = (AreaPotential) potential;
						if (a.getEnergy() > 0)
							potentialRenderer.render(potential, this, g2);
					}
				}
			}
		}
		if (gridRenderer != null)
			gridRenderer.render(this, g2);
		if (drawExpectation) {
			if (position != null && momentum != null) {
				int x = convertPointToPixelX(position.x);
				int y = convertPointToPixelY(position.y);
				g2.setColor(contrast);
				g2.drawLine(0, y, w, y);
				g2.drawLine(x, 0, x, h);
				if (Math.abs(momentum.x) > 0.01f || Math.abs(momentum.y) > 0.01f) {
					g2.setColor(Color.yellow);
					g2.setStroke(momentumStroke);
					drawVector(g2, x, y, momentum.x, momentum.y, 10);
				}
			}
		}
		g2.setStroke(thinStroke);
		float eFieldValue = eField == null ? 0 : eField.getValue(time);
		if (eFieldValue != 0) {
			drawEField(g2, eFieldValue, w);
		}
		float bFieldValue = bField == null ? 0 : bField.getValue(time);
		if (bFieldValue != 0) {
			drawBField(g2, bFieldValue, w);
		}
		if (drawEnergy && kinE > 0)
			drawEnergies(g2, contrast);
		if (selectedPotential instanceof AreaPotential) {
			for (Rectangle r : rect) {
				if (r.x != 0 || r.y != 0) {
					g2.setColor(Color.yellow);
					g2.fill(r);
					g2.setColor(contrast);
					g2.draw(r);
				}
			}
			for (Polygon r : handle) {
				if (r.npoints > 0) {
					g2.setColor(Color.pink);
					g2.fill(r);
					g2.setColor(contrast);
					g2.draw(r);
				}
			}
		} else if (selectedPotential instanceof PointPotential) {
			if (halo.x != 0 || halo.y != 0) {
				g2.setColor(contrast);
				g2.setStroke(dashed);
				g2.draw(halo);
			}
		}
		if (mouseBeingDragged) {
			if (movingShape != null) {
				g2.setColor(contrast);
				g2.setStroke(dashed);
				movingShape.render(g2);
			}
		}
		if (contourPlot != null) {
			g2.setStroke(thinStroke);
			int xMargin = 0;
			int yMargin = 0;
			if (xBoundary instanceof AbsorbingBoundary) {
				AbsorbingBoundary ab = (AbsorbingBoundary) xBoundary;
				xMargin = (int) (ab.getLengthPercentage() * prob.length);
			}
			if (yBoundary instanceof AbsorbingBoundary) {
				AbsorbingBoundary ab = (AbsorbingBoundary) yBoundary;
				yMargin = (int) (ab.getLengthPercentage() * prob[0].length);
			}
			contourPlot.setMargins(yMargin, yMargin, xMargin, xMargin);
			contourPlot.render(g, getSize(), prob);
		}
		if (currentRenderer != null) {
			currentRenderer.render(this, g2);
		}
		if (rulerRenderer != null) {
			g2.setColor(contrast);
			rulerRenderer.render(this, g2);
		}
		if (quantumBox.getPropagator() instanceof RealTimePropagator2D) {
			RealTimePropagator2D rtp = (RealTimePropagator2D) quantumBox.getPropagator();
			int sourceCount = rtp.getSourceCount();
			if (sourceCount > 0) {
				g2.setColor(contrast);
				g2.setStroke(thinStroke);
				for (int i = 0; i < sourceCount; i++) {
					Source source = rtp.getSource(i);
					int d = 0;
					int i1 = Math.round(rtp.getTime() / rtp.getTimeStep()) + 1;
					int i2 = Math.round(source.getPeriod() / rtp.getTimeStep());
					if (i2 > 0 && i1 % i2 < i2 * 0.1f) {
						d = 10;
						g2.setStroke(dashed);
					}
					if (source instanceof PointSource) {
						PointSource ps = (PointSource) source;
						int psx = convertPointToPixelX(ps.getXcenter());
						int psy = convertPointToPixelY(ps.getYcenter());
						g2.fillOval(psx - 4, psy - 4, 8, 8);
						if (d != 0)
							g2.drawOval(psx - d, psy - d, d * 2, d * 2);
					}
				}
			}
		}
		drawPictures(g2);
		drawTextBoxes(g2);
		if (frank) {
			int dy = rulerRenderer != null ? 32 : 12;
			MiscUtil.drawFrank(g2, getWidth() - 130, getHeight() - dy);
		}
	}

	private void drawTextBoxes(Graphics2D g) {
		if (textBoxes == null || textBoxes.isEmpty())
			return;
		Font oldFont = g.getFont();
		Color oldColor = g.getColor();
		FontMetrics fm = g.getFontMetrics();
		int stringWidth;
		for (TextBox x : textBoxes) {
			g.setFont(new Font(x.getName(), x.getStyle(), x.getSize()));
			g.setColor(x.getColor());
			stringWidth = fm.stringWidth(x.getText());
			g.drawString(x.getText(), convertPointToPixelX(x.getX()) - stringWidth / 2, convertPointToPixelY(x.getY()));
		}
		g.setFont(oldFont);
		g.setColor(oldColor);
	}

	private void drawPictures(Graphics2D g) {
		if (pictures == null || pictures.isEmpty())
			return;
		for (Picture x : pictures) {
			x.getImage().paintIcon(this, g, convertPointToPixelX(x.getX()) - x.getWidth() / 2, convertPointToPixelY(x.getY()) - x.getHeight() / 2);
		}
	}

	private void drawEnergies(Graphics2D g2, Color contrast) {
		g2.setColor(contrast);
		g2.drawString("T. E. " + (totE > 0 ? "\u002b" : "\u2212"), 5, 20);
		g2.drawString("P. E. " + (potE > 0 ? "\u002b" : "\u2212"), 5, 35);
		g2.drawString("K. E. \u002b", 5, 50);
		int ke = (int) Math.round(Math.abs(kinE) * energyScale);
		int pe = (int) Math.round(Math.abs(potE) * energyScale);
		int te = (int) Math.round(Math.abs(totE) * energyScale);
		if (te > 0) {
			g2.setColor(Color.magenta);
			g2.fillRect(45, 10, te, 10);
		}
		if (pe > 0) {
			g2.setColor(Color.blue);
			g2.fillRect(45, 25, pe, 10);
		}
		if (ke > 0) {
			g2.setColor(Color.red);
			g2.fillRect(45, 40, ke, 10);
		}
		g2.setColor(contrast);
		if (te > 0)
			g2.drawRect(45, 10, te, 10);
		if (pe > 0)
			g2.drawRect(45, 25, pe, 10);
		if (ke > 0)
			g2.drawRect(45, 40, ke, 10);
	}

	private void drawBField(Graphics2D g2, float bFieldValue, int w) {
		g2.setColor(Color.white);
		g2.setStroke(momentumStroke);
		g2.drawOval(w - 50, 30, 16, 16);
		if (bField.getIntensity() < 0) {
			g2.fillOval(w - 44, 36, 4, 4);
		} else if (bField.getIntensity() > 0) {
			g2.drawLine(w - 48, 32, w - 36, 44);
			g2.drawLine(w - 36, 32, w - 48, 44);
		}
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();
		String s = "Magnetic Field";
		g2.drawString(s, getWidth() - fm.stringWidth(s) - 10, 20);
		if (bField.getFrequency() != 0) {
			float x0 = w - 40;
			float y0 = 50;
			float dx = 40;
			float dy = 60;
			g2.setColor(Color.cyan);
			g2.fillRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
			g2.setColor(Color.black);
			g2.drawRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
			g2.drawLine((int) x0, (int) y0, (int) x0, (int) (y0 + dy));
			float x = (float) (bFieldValue / bField.getIntensity() * 0.5 * dx);
			if (path == null)
				path = new GeneralPath();
			else
				path.reset();
			path.moveTo(x0 - x, y0);
			g2.setColor(Color.red);
			g2.fillOval((int) (x0 - x - 2), (int) (y0 - 2), 4, 4);
			for (int i = 1; i < dy; i++) {
				x = (float) (bField.getValue(time - timeStep * i * TimePropagator2D.OUTPUT_INTERVAL) / bField.getIntensity() * 0.5 * dx);
				path.lineTo(x0 - x, y0 + i);
			}
			g2.setColor(fieldColor);
			g2.draw(path);
		}
	}

	private void drawEField(Graphics2D g2, float eFieldValue, int w) {
		g2.setColor(MiscUtil.getContrastColor(getBackground()));
		g2.setStroke(dashed);
		g2.drawOval(w - 60, 30, 40, 40);
		g2.setStroke(momentumStroke);
		double a = Math.toRadians(eField.getAngle());
		drawVector(g2, w - 40, 50, Math.cos(a), Math.sin(a), 20);
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();
		String s = "Electric Field";
		g2.drawString(s, getWidth() - fm.stringWidth(s) - 10, 20);
		if (eField.getFrequency() != 0) {
			float x0 = w - 40;
			float y0 = 50;
			float dx = 40;
			float dy = 60;
			g2.setColor(Color.cyan);
			g2.fillRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
			g2.setColor(Color.black);
			g2.drawRect((int) (x0 - dx / 2), (int) y0, (int) dx, (int) dy);
			g2.drawLine((int) x0, (int) y0, (int) x0, (int) (y0 + dy));
			float x = (float) (eFieldValue / eField.getIntensity() * 0.5 * dx);
			if (path == null)
				path = new GeneralPath();
			else
				path.reset();
			path.moveTo(x0 - x, y0);
			g2.setColor(Color.red);
			g2.fillOval((int) (x0 - x - 2), (int) (y0 - 2), 4, 4);
			for (int i = 1; i < dy; i++) {
				x = (float) (eField.getValue(time - timeStep * i * TimePropagator2D.OUTPUT_INTERVAL) / eField.getIntensity() * 0.5 * dx);
				path.lineTo(x0 - x, y0 + i);
			}
			g2.setColor(fieldColor);
			g2.draw(path);
		}
	}

	private static void drawVector(Graphics2D g, int x, int y, double vx, double vy, double scale) {
		double r = 1.0 / Math.hypot(vx, vy);
		double arrowx = vx * r;
		double arrowy = vy * r;
		int x1 = (int) (x + vx * scale);
		int y1 = (int) (y + vy * scale);
		g.drawLine(x, y, x1, y1);
		r = 5;
		double wingx = r * (arrowx * COS45 + arrowy * SIN45);
		double wingy = r * (arrowy * COS45 - arrowx * SIN45);
		g.drawLine(x1, y1, (int) (x1 - wingx), (int) (y1 - wingy));
		wingx = r * (arrowx * COS45 - arrowy * SIN45);
		wingy = r * (arrowy * COS45 + arrowx * SIN45);
		g.drawLine(x1, y1, (int) (x1 - wingx), (int) (y1 - wingy));
	}

	private void setAnchorPointForRectangularShape(byte i, float x, float y, float w, float h) {
		switch (i) {
		case UPPER_LEFT:
			anchorPoint.setLocation(x + w, y + h);
			break;
		case UPPER_RIGHT:
			anchorPoint.setLocation(x, y + h);
			break;
		case LOWER_RIGHT:
			anchorPoint.setLocation(x, y);
			break;
		case LOWER_LEFT:
			anchorPoint.setLocation(x + w, y);
			break;
		case TOP:
			anchorPoint.setLocation(x, y + h);
			break;
		case RIGHT:
			anchorPoint.setLocation(x, y);
			break;
		case BOTTOM:
			anchorPoint.setLocation(x, y);
			break;
		case LEFT:
			anchorPoint.setLocation(x + w, y);
			break;
		}
	}

	void setSelectedPotential(Potential2D p) {
		if (p == null) {
			if (selectedPotential != null)
				selectedPotential.setSelected(false);
			selectedPotential = null;
		} else {
			if (!potentials.contains(p)) {
				System.err.println("Potential error: " + p);
				return;
			}
			if (selectedPotential != null && selectedPotential != p)
				selectedPotential.setSelected(false);
			selectedPotential = p;
			selectedPotential.setSelected(true);
			if (selectedPotential instanceof PointPotential) {
				((PointPotential) selectedPotential).setHalo(halo, getSize(), xBoundary, yBoundary);
			} else if (selectedPotential instanceof AreaPotential) {
				((AreaPotential) selectedPotential).setRects(rect, handle, getSize(), xBoundary, yBoundary);
			}
		}
	}

	private void processKeyPressed(KeyEvent e) {
		if (selectedPotential != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				quantumBox.translatePotentialBy(selectedPotential, -.01f * (xmax - xmin), 0);
				break;
			case KeyEvent.VK_RIGHT:
				quantumBox.translatePotentialBy(selectedPotential, .01f * (xmax - xmin), 0);
				break;
			case KeyEvent.VK_DOWN:
				quantumBox.translatePotentialBy(selectedPotential, 0, .01f * (ymax - ymin));
				break;
			case KeyEvent.VK_UP:
				quantumBox.translatePotentialBy(selectedPotential, 0, -.01f * (ymax - ymin));
				break;
			}
			setSelectedPotential(selectedPotential);
		}
		repaint();
		e.consume();
	}

	private void processKeyReleased(KeyEvent e) {
		if (selectedPotential != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_DELETE:
			case KeyEvent.VK_BACK_SPACE:
				quantumBox.removePotential(selectedPotential);
				setSelectedPotential(null);
				break;
			}
		}
		repaint();
		e.consume();
	}

	private void processMousePressed(MouseEvent e) {
		mousePressedTime = System.currentTimeMillis();
		requestFocusInWindow();
		int x = e.getX();
		int y = e.getY();
		if (selectedPotential instanceof AreaPotential) {
			selectedSpot = -1;
			for (byte i = 0; i < rect.length; i++) {
				if (rect[i].x < -10 || rect[i].y < -10)
					continue;
				if (rect[i].contains(x, y)) {
					selectedSpot = i;
					break;
				}
			}
			for (byte i = 0; i < handle.length; i++) {
				if (handle[i].npoints > 0 && handle[i].contains(x, y)) {
					selectedSpot = (byte) (HANDLE1 + i);
					break;
				}
			}
			if (selectedSpot != -1) {
				setMovingShape(true, true);
				e.consume();
				return;
			}
		}
		selectPotential(x, y);
		if (selectedPotential != null) {
			pressedPointRelative.x = x - convertPointToPixelX(selectedPotential.getXcenter());
			pressedPointRelative.y = y - convertPointToPixelY(selectedPotential.getYcenter());
			setMovingShape(false, false);
		}
		repaint();
		e.consume();
	}

	private void selectPotential(int x, int y) {
		setSelectedPotential(null);
		float rx = convertPixelToPointX(x);
		float ry = convertPixelToPointY(y);
		synchronized (potentials) {
			for (Potential2D p : potentials) {
				if (p.contains(rx, ry)) {
					setSelectedPotential(p);
					break;
				}
			}
		}
	}

	private void setMovingShape(boolean areaPotentialOnly, boolean anchor) {
		if (selectedPotential instanceof EllipticalPotential) {
			EllipticalPotential p = (EllipticalPotential) selectedPotential;
			int a = convertPointToPixelX(p.getXcenter() - p.getRx());
			int b = convertPointToPixelY(p.getYcenter() - p.getRy());
			int c = convertLengthToPixelX(2 * p.getRx());
			int d = convertLengthToPixelY(2 * p.getRy());
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
			movingShape = new MovingEllipse(new Ellipse2D.Float(a, b, c, d));
		} else if (selectedPotential instanceof RectangularPotential) {
			RectangularPotential p = (RectangularPotential) selectedPotential;
			int a = convertPointToPixelX(p.getXcenter() - 0.5f * p.getLx());
			int b = convertPointToPixelY(p.getYcenter() - 0.5f * p.getLy());
			int c = convertLengthToPixelX(p.getLx());
			int d = convertLengthToPixelY(p.getLy());
			int r = 2 * convertLengthToPixelX(p.getCornerRadius());
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, a, b, c, d);
			movingShape = new MovingRoundRectangle(new RoundRectangle2D.Float(a, b, c, d, r, r));
		} else if (selectedPotential instanceof AnnularPotential) {
			AnnularPotential p = (AnnularPotential) selectedPotential;
			int a1 = convertPointToPixelX(p.getXcenter() - p.getOuterRx());
			int b1 = convertPointToPixelY(p.getYcenter() - p.getOuterRy());
			int c1 = convertLengthToPixelX(2 * p.getOuterRx());
			int d1 = convertLengthToPixelY(2 * p.getOuterRy());
			int a2 = convertPointToPixelX(p.getXcenter() - p.getInnerRx());
			int b2 = convertPointToPixelY(p.getYcenter() - p.getInnerRy());
			int c2 = convertLengthToPixelX(2 * p.getInnerRx());
			int d2 = convertLengthToPixelY(2 * p.getInnerRy());
			if (anchor)
				setAnchorPointForRectangularShape(selectedSpot, a1, b1, c1, d1);
			movingShape = new MovingAnnulus(new Ellipse2D.Float(a1, b1, c1, d1), new Ellipse2D.Float(a2, b2, c2, d2));
		}
		if (!areaPotentialOnly) {
			if (selectedPotential instanceof IonicPotential) {
				IonicPotential p = (IonicPotential) selectedPotential;
				int a = convertPointToPixelX(p.getXcenter());
				int b = convertPointToPixelY(p.getYcenter());
				movingShape = new MovingEllipse(new Ellipse2D.Float(a - 10, b - 10, 20, 20));
			}
		}
	}

	private void processMouseDragged(MouseEvent e) {
		mouseBeingDragged = true;
		if (MiscUtil.isRightClick(e))
			return;
		if (System.currentTimeMillis() - mousePressedTime < MINIMUM_MOUSE_DRAG_RESPONSE_INTERVAL)
			return;
		mousePressedTime = System.currentTimeMillis();
		int x = e.getX();
		int y = e.getY();
		if (movingShape != null && selectedPotential != null) {
			if (quantumBox.isEditable() || selectedPotential.isDraggable()) {
				Shape[] shape = movingShape.getShapes();
				if (shape[0] instanceof RectangularShape) {
					RectangularShape s = (RectangularShape) shape[0];
					double a = s.getX(), b = s.getY(), c = s.getWidth(), d = s.getHeight();
					if (selectedSpot == -1) {
						a = x - pressedPointRelative.x - c * 0.5;
						b = y - pressedPointRelative.y - d * 0.5;
						setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						switch (selectedSpot) {
						case LOWER_LEFT:
						case LOWER_RIGHT:
						case UPPER_LEFT:
						case UPPER_RIGHT:
							a = Math.min(x, anchorPoint.x);
							b = Math.min(y, anchorPoint.y);
							c = Math.abs(x - anchorPoint.x);
							d = Math.abs(y - anchorPoint.y);
							break;
						case TOP:
						case BOTTOM:
							b = Math.min(y, anchorPoint.y);
							d = Math.abs(y - anchorPoint.y);
							break;
						case LEFT:
						case RIGHT:
							a = Math.min(x, anchorPoint.x);
							c = Math.abs(x - anchorPoint.x);
							break;
						case HANDLE1:
							if (s instanceof RoundRectangle2D.Float) {
								float arc = (float) (x - a);
								if (arc < 0)
									arc = 0;
								else
									arc = (float) Math.min(arc, 0.5f * Math.min(c, d));
								RoundRectangle2D.Float rr = (RoundRectangle2D.Float) s;
								rr.arcwidth = rr.archeight = arc * 2;
							}
							break;
						}
						setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
					s.setFrame(a, b, c, d);
				}
				if (shape.length > 1) {
					if (selectedPotential instanceof AnnularPotential) {
						RectangularShape s2 = (RectangularShape) shape[1];
						if (selectedSpot == -1) {
							RectangularShape s1 = (RectangularShape) shape[0];
							double a = s1.getX() + 0.5 * (s1.getWidth() - s2.getWidth());
							double b = s1.getY() + 0.5 * (s1.getHeight() - s2.getHeight());
							s2.setFrame(a, b, s2.getWidth(), s2.getHeight());
						} else {
							switch (selectedSpot) {
							case HANDLE1:
								break;
							case HANDLE2:
								break;
							}
						}
					}
				}
			} else {
				showTip("<html><font color=red>The selected object is not draggable!</font></html>", x, y, 500);
			}
		}
		repaint();
		e.consume();
	}

	private void processMouseReleased(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			JDialog dialog = dialogFactory.createDialog(selectedPotential);
			if (dialog != null) {
				dialog.setLocationRelativeTo(this);
				dialog.setVisible(true);
			}
			return;
		}
		int x = e.getX();
		int y = e.getY();
		if (MiscUtil.isRightClick(e)) {
			createPopupMenu();
			popupMenu.show(this, x, y);
			return;
		}
		if (movingShape != null && mouseBeingDragged && selectedPotential != null) {
			if (quantumBox.isEditable() || selectedPotential.isDraggable()) {
				Shape[] shape = movingShape.getShapes();
				if (shape[0] instanceof RectangularShape) {
					if (selectedSpot == -1) {
						float x2 = convertPixelToPointX((int) (x - pressedPointRelative.x));
						float y2 = convertPixelToPointY((int) (y - pressedPointRelative.y));
						quantumBox.translatePotentialTo(selectedPotential, x2, y2);
						setSelectedPotential(selectedPotential);
					} else {
						if (selectedSpot == HANDLE1) {
							if (selectedPotential instanceof RectangularPotential) {
								RoundRectangle2D.Float rr = (RoundRectangle2D.Float) shape[0];
								float r = 0.5f * convertPixelToLengthX((int) rr.arcwidth);
								quantumBox.reshapeRectangularPotential((RectangularPotential) selectedPotential, r);
							}
						} else {
							RectangularShape r = (RectangularShape) shape[0];
							float x2 = convertPixelToPointX((int) r.getX());
							float y2 = convertPixelToPointY((int) r.getY());
							float w2 = convertPixelToLengthX((int) r.getWidth());
							float h2 = convertPixelToLengthY((int) r.getHeight());
							quantumBox.resizePotentialTo(selectedPotential, x2, y2, w2, h2);
						}
						setSelectedPotential(selectedPotential);
					}
				}
			}
		} else {
			selectPotential(x, y);
		}
		repaint();
		e.consume();
		movingShape = null;
		mouseBeingDragged = false;
	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int iSpot = -1;
		if (selectedPotential instanceof AreaPotential) {
			for (int i = 0; i < rect.length; i++) {
				if (rect[i].x < -10 || rect[i].y < -10)
					continue;
				if (rect[i].contains(x, y)) {
					iSpot = i;
					break;
				}
			}
			if (iSpot == -1) {
				for (int i = 0; i < handle.length; i++) {
					if (handle[i].npoints == 0)
						continue;
					if (handle[i].contains(x, y)) {
						iSpot = HANDLE1 + i;
						break;
					}
				}
			}
			if (iSpot >= 0) {
				switch (iSpot) {
				case UPPER_LEFT:
					setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
					break;
				case LOWER_LEFT:
					setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
					break;
				case UPPER_RIGHT:
					setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
					break;
				case LOWER_RIGHT:
					setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
					break;
				case TOP:
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
					break;
				case BOTTOM:
					setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
					break;
				case LEFT:
					setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
					break;
				case RIGHT:
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					break;
				case HANDLE1:
				case HANDLE2:
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					break;
				}
			}
		}
		if (iSpot == -1) {
			float rx = convertPixelToPointX(x);
			float ry = convertPixelToPointY(y);
			boolean contained = false;
			synchronized (potentials) {
				for (Potential2D p : potentials) {
					if (p.contains(rx, ry)) {
						contained = true;
						break;
					}
				}
			}
			setCursor(Cursor.getPredefinedCursor(contained ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
		}
		e.consume();
	}

	private float convertPixelToPointX(int x) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return xmin2 + (xmax2 - xmin2) * (float) x / (float) getWidth();
		}
		return xmin + (xmax - xmin) * (float) x / (float) getWidth();
	}

	private float convertPixelToPointY(int y) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return ymin2 + (ymax2 - ymin2) * (float) y / (float) getHeight();
		}
		return ymin + (ymax - ymin) * (float) y / (float) getHeight();
	}

	private float convertPixelToLengthX(int l) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return (xmax2 - xmin2) * (float) l / (float) getWidth();
		}
		return (xmax - xmin) * (float) l / (float) getWidth();
	}

	private float convertPixelToLengthY(int l) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return (ymax2 - ymin2) * (float) l / (float) getHeight();
		}
		return (ymax - ymin) * (float) l / (float) getHeight();
	}

	public int convertPointToPixelX(float x) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return Math.round((x - xmin2) / (xmax2 - xmin2) * getWidth());
		}
		return Math.round((x - xmin) / (xmax - xmin) * getWidth());
	}

	public int convertPointToPixelY(float y) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return Math.round((y - ymin2) / (ymax2 - ymin2) * getHeight());
		}
		return Math.round((y - ymin) / (ymax - ymin) * getHeight());
	}

	private int convertLengthToPixelX(float l) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return Math.round(l / (xmax2 - xmin2) * getWidth());
		}
		return Math.round(l / (xmax - xmin) * getWidth());
	}

	private int convertLengthToPixelY(float l) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return Math.round(l / (ymax2 - ymin2) * getHeight());
		}
		return Math.round(l / (ymax - ymin) * getHeight());
	}

	private void showTip(String msg, int x, int y, int time) {
		if (tipPopupMenu == null) {
			tipPopupMenu = new JPopupMenu("Tip");
			tipPopupMenu.setBorder(BorderFactory.createLineBorder(Color.black));
			tipPopupMenu.setBackground(SystemColor.info);
			JLabel l = new JLabel(msg);
			l.setFont(new Font(null, Font.PLAIN, 10));
			l.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			tipPopupMenu.add(l);
		} else {
			((JLabel) tipPopupMenu.getComponent(0)).setText(msg);
		}
		tipPopupMenu.show(this, x, y);
		if (time > 0) {
			Timer timer = new Timer(time, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tipPopupMenu.setVisible(false);
				}
			});
			timer.setRepeats(false);
			timer.setInitialDelay(time);
			timer.start();
		}
	}

}
