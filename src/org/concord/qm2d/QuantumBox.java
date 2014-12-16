package org.concord.qm2d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.concord.modeler.MwService;
import org.concord.qm2d.model.AnnularPotential;
import org.concord.qm2d.model.EllipticalPotential;
import org.concord.qm2d.model.ImaginaryTimePropagator2D;
import org.concord.qm2d.model.PlaneWaveSource;
import org.concord.qm2d.model.PointSource;
import org.concord.qm2d.model.Potential2D;
import org.concord.qm2d.model.PotentialFactory;
import org.concord.qm2d.model.RealTimePropagator2D;
import org.concord.qm2d.model.RectangularPotential;
import org.concord.qm2d.model.TimePropagator2D;
import org.concord.qm2d.model.WavePacketFactory;
import org.concord.qm2d.view.View2D;
import org.concord.qmevent.IOEvent;
import org.concord.qmevent.IOListener;
import org.concord.qmevent.VisualizationEvent;
import org.concord.qmevent.VisualizationListener;
import org.concord.qmshared.Particle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Units: Time: 10^-15 second; Space: 10^-9 meter; Mass: 10^-3 atomic unit; Potential: eV (1.6x10^-19 Joule); Charge: 1 e.
 * 
 * @author Charles Xie
 * 
 */
public class QuantumBox extends JApplet implements MwService, VisualizationListener {

	private static final long serialVersionUID = 1L;

	final static String BRAND_NAME = "Quantum Workbench";

	static byte logLevel = 0;

	Particle particle;
	TimePropagator2D propagator;
	private int nx = 200;
	private int ny = 200;
	float xmin = -10;
	float xmax = 10;
	float ymin = -10;
	float ymax = 10;
	boolean editable;
	private boolean requestRebuildingWaveFunction;
	private final Lock lock = new ReentrantLock();

	private ExecutorService threadService;
	View2D view2D;
	private Scripter2D scripter;
	PotentialFactory potentialFactory;
	WavePacketFactory wavepacketFactory;

	Runnable clickRun, clickStop, clickReset, clickReload;
	private JButton buttonRun, buttonStop, buttonReset, buttonReload;
	private List<IOListener> ioListeners;
	private static Preferences preferences;

	private SAXParser saxParser;
	private DefaultHandler saxHandler;
	private XmlEncoder encoder;
	private File currentFile;
	private URL currentURL;
	private String currentModel;
	private boolean saved = true;

	private JFrame owner;

	public QuantumBox() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		particle = new Particle();
		potentialFactory = new PotentialFactory(nx, ny, xmin, xmax, ymin, ymax, particle);
		wavepacketFactory = new WavePacketFactory(nx, ny, xmin, xmax, ymin, ymax);
		encoder = new XmlEncoder(this);
		saxHandler = new XmlDecoder(this);
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		setPropagator(false);

		scripter = new Scripter2D(this);
		view2D = new View2D(this);
		view2D.setBackground(Color.black);
		view2D.setPotential(propagator.getPotentials());
		add(view2D, BorderLayout.CENTER);

		createActions();

	}

	@SuppressWarnings("serial")
	private void createActions() {

		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Helper.showScriptDialog(QuantumBox.this);
			}
		};
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, true);
		a.putValue(Action.NAME, "Script");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		view2D.getInputMap().put(ks, "Script");
		view2D.getActionMap().put("Script", a);

		a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				view2D.createDialog(QuantumBox.this);
			}
		};
		ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK);
		a.putValue(Action.NAME, "Property");
		a.putValue(Action.ACCELERATOR_KEY, ks);
		view2D.getInputMap().put(ks, "Property");
		view2D.getActionMap().put("Property", a);

	}

	public View2D getView() {
		return view2D;
	}

	public TimePropagator2D getPropagator() {
		return propagator;
	}

	public void setNx(int nx) {
		this.nx = nx;
	}

	public int getNx() {
		return nx;
	}

	public void setNy(int ny) {
		this.ny = ny;
	}

	public int getNy() {
		return ny;
	}

	public float getMinX() {
		return xmin;
	}

	public float getMaxX() {
		return xmax;
	}

	public float getMinY() {
		return ymin;
	}

	public float getMaxY() {
		return ymax;
	}

	public static byte getLogLevel() {
		return logLevel;
	}

	public void setEditable(boolean b) {
		editable = b;
	}

	public boolean isEditable() {
		return editable;
	}

	void clear() {
		propagator.clear();
		view2D.clear();
	}

	void setMass(float mass) {
		particle.setMass(mass);
		propagator.calculateMassRelatedThings();
	}

	public void translatePotentialBy(Potential2D p, float dx, float dy) {
		translatePotentialTo(p, p.getXcenter() + dx, p.getYcenter() + dy);
	}

	public void translatePotentialTo(Potential2D p, float x, float y) {
		Potential2D oldP = potentialFactory.copy(p);
		p.setXcenter(x);
		p.setYcenter(y);
		propagator.removePotentialFunction(oldP.isImaginary(), oldP.getPotential());
		Potential2D newP = potentialFactory.copy(p);
		propagator.addPotentialFunction(newP.isImaginary(), newP.getPotential());
	}

	public void resizePotentialTo(Potential2D p, float x, float y, float w, float h) {
		Potential2D oldP = potentialFactory.copy(p);
		p.setXcenter(x + 0.5f * w);
		p.setYcenter(y + 0.5f * h);
		if (p instanceof RectangularPotential) {
			RectangularPotential rp = (RectangularPotential) p;
			rp.setLx(w);
			rp.setLy(h);
		} else if (p instanceof EllipticalPotential) {
			EllipticalPotential ep = (EllipticalPotential) p;
			ep.setRx(w * 0.5f);
			ep.setRy(h * 0.5f);
		} else if (p instanceof AnnularPotential) {
			AnnularPotential ap = (AnnularPotential) p;
			ap.setOuterRx(w * 0.5f);
			ap.setOuterRy(h * 0.5f);
		}
		propagator.removePotentialFunction(oldP.isImaginary(), oldP.getPotential());
		Potential2D newP = potentialFactory.copy(p);
		propagator.addPotentialFunction(newP.isImaginary(), newP.getPotential());
	}

	public void reshapeRectangularPotential(RectangularPotential p, float r) {
		Potential2D oldP = potentialFactory.copy(p);
		p.setCornerRadius(r);
		propagator.removePotentialFunction(oldP.isImaginary(), oldP.getPotential());
		Potential2D newP = potentialFactory.copy(p);
		propagator.addPotentialFunction(newP.isImaginary(), newP.getPotential());
	}

	public Potential2D addPotential(int index, Potential2D p) {
		Potential2D p2 = potentialFactory.copy(p);
		propagator.addPotential(index, p2);
		return p2;
	}

	public int removePotential(Potential2D p) {
		return propagator.removePotential(p);
	}

	public int indexOfPotential(Potential2D p) {
		return propagator.indexOfPotential(p);
	}

	public void requestRebuildingWaveFunction(boolean b) {
		requestRebuildingWaveFunction = b;
	}

	void addPlaneWavePacket(float magnitude, float x, float y, float w, float h, float px, float py) {
		// make sure the wave packet is on the same page about the parameters
		wavepacketFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addWavePacket(wavepacketFactory.createPlaneWave(magnitude, x, y, w, h, px, py));
	}

	void addGaussianWavePacket(float magnitude, float sigma, float muX, float muY, float px, float py) {
		wavepacketFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addWavePacket(wavepacketFactory.createGaussian(magnitude, sigma, muX, muY, px, py));
	}

	void addPlaneWaveSource(float period, float magnitude, float x, float y, float w, float h, float px, float py) {
		if (propagator instanceof RealTimePropagator2D) {
			RealTimePropagator2D rtp = (RealTimePropagator2D) propagator;
			PlaneWaveSource s = new PlaneWaveSource(x, y, w, h, nx, ny, xmin, xmax, ymin, ymax);
			s.setAmplitude(magnitude);
			s.setPeriod(period);
			rtp.addSource(s);
		}
	}

	void addGaussianSource(float period, float magnitude, float sigma, float x, float y, float px, float py) {
		if (propagator instanceof RealTimePropagator2D) {
			RealTimePropagator2D rtp = (RealTimePropagator2D) propagator;
			PointSource s = new PointSource(x, y, nx, ny, xmin, xmax, ymin, ymax);
			s.setAmplitude(magnitude);
			s.setSigma(sigma);
			s.setPeriod(period);
			rtp.addSource(s);
		}
	}

	void addIonicPotential(boolean imaginary, float charge, float offset, float xcenter, float ycenter, Color color, boolean visible, boolean movable) {
		// make sure the potential is on the same page about the parameters
		potentialFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addPotential(potentialFactory.createIonicPotential(imaginary, particle.getCharge(), charge, offset, xcenter, ycenter, color, visible, movable));
		view2D.repaint();
	}

	void addRectangularPotential(boolean imaginary, float energy, float xcenter, float ycenter, float xlength, float ylength, float cornerRadius, Color color, boolean visible, boolean movable) {
		potentialFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addPotential(potentialFactory.createRectangularPotentialArea(imaginary, energy, xcenter, ycenter, xlength, ylength, cornerRadius, color, visible, movable));
		view2D.repaint();
	}

	void addEllipticalPotential(boolean imaginary, float energy, float xcenter, float ycenter, float rx, float ry, Color color, boolean visible, boolean movable) {
		potentialFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addPotential(potentialFactory.createEllipticalPotentialArea(imaginary, energy, xcenter, ycenter, rx, ry, color, visible, movable));
		view2D.repaint();
	}

	void addAnnularPotential(boolean imaginary, float energy, float xcenter, float ycenter, float outerRx, float outerRy, float innerRx, float innerRy, Color color, boolean visible, boolean movable) {
		potentialFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.addPotential(potentialFactory.createAnnularPotentialArea(imaginary, energy, xcenter, ycenter, outerRx, outerRy, innerRx, innerRy, color, visible, movable));
		view2D.repaint();
	}

	void setArea(float xmin, float xmax, float ymin, float ymax) {
		potentialFactory.set(nx, ny, xmin, xmax, ymin, ymax);
		propagator.setArea(xmin, xmax, ymin, ymax);
		view2D.setPreferredSize(new Dimension((int) (500 * (xmax - xmin) / (ymax - ymin)), 500));
		view2D.setArea(xmin, xmax, ymin, ymax);
	}

	void setPropagator(boolean itp) {
		boolean created = false;
		if (itp) {
			if (propagator == null || propagator instanceof RealTimePropagator2D) {
				propagator = new ImaginaryTimePropagator2D(particle, nx, ny, xmin, xmax, ymin, ymax);
				created = true;
			}
		} else {
			if (propagator == null || propagator instanceof ImaginaryTimePropagator2D) {
				propagator = new RealTimePropagator2D(particle, nx, ny, xmin, xmax, ymin, ymax);
				created = true;
			}
		}
		if (created) {
			propagator.setLock(lock);
			propagator.addVisualizationListener(this);
			propagator.setWavePacketFactory(wavepacketFactory);
			propagator.setPotentialFactory(potentialFactory);
			if (view2D != null)
				view2D.setPotential(propagator.getPotentials());
		}
	}

	@Override
	public void init() {
		String s = null;
		try {
			s = getParameter("script");
		} catch (Exception e) {
			s = null;
		}
		if (s != null) {
			final String s2 = s;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					runNativeScript(s2);
				}
			});
		}
		view2D.repaint();
	}

	void saveApplet(File file) {
		new AppletConverter(this).write(file);
	}

	public void stop() {
		propagator.stop();
		if (buttonRun != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					buttonRun.setEnabled(true);
					buttonStop.setEnabled(false);
				}
			});
		}
	}

	public void reset() {
		// should call this first, or reset() may have some residual effect
		stop();
		try {
			Thread.sleep(200); // wait 200 ms for the current cycle of calculation to finish
		} catch (InterruptedException e) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				propagator.reset();
				view2D.repaint();
			}
		});
	}

	public void reload() {
		stop();
		if (currentFile != null) {
			loadFile(currentFile);
			return;
		}
		if (currentModel != null) {
			loadModel(currentModel);
			return;
		}
		if (currentURL != null) {
			try {
				loadURL(currentURL);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	public void run() {
		if (threadService == null)
			threadService = Executors.newFixedThreadPool(1);
		threadService.execute(new Runnable() {
			public void run() {
				propagator.run();
			}
		});
		if (buttonRun != null) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					buttonRun.setEnabled(false);
					buttonStop.setEnabled(true);
				}
			});
		}
	}

	public void runSteps(final int n) {
		if (threadService == null)
			threadService = Executors.newFixedThreadPool(1);
		threadService.execute(new Runnable() {
			public void run() {
				propagator.runSteps(n);
			}
		});
	}

	private void createButtonPanel() {
		JPanel p = new JPanel();
		add(p, BorderLayout.SOUTH);
		buttonRun = new JButton("Run");
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		p.add(buttonRun);
		buttonStop = new JButton("Stop");
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		p.add(buttonStop);
		buttonReset = new JButton("Reset");
		buttonReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		p.add(buttonReset);
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(20, 10));
		p.add(spacer);
		buttonReload = new JButton("Reload");
		buttonReload.setEnabled(false);
		buttonReload.setToolTipText("Reload the initial configurations");
		buttonReload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reload();
			}
		});
		p.add(buttonReload);
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
		clickReload = new Runnable() {
			public void run() {
				buttonReload.doClick();
			}
		};
	}

	@Override
	public void destroy() {
		stop();
		propagator.destroy();
		if (threadService != null && !threadService.isShutdown()) {
			threadService.shutdownNow();
		}
	}

	public JPopupMenu getPopupMenu() {
		return view2D.getPopupMenu();
	}

	public Component getSnapshotComponent() {
		return view2D;
	}

	public String runNativeScript(final String script) {
		requestRebuildingWaveFunction = false;
		scripter.executeScript(script);
		if (requestRebuildingWaveFunction)
			propagator.rebuildWaveFunction();
		return null;
	}

	Scripter2D getScripter() {
		return scripter;
	}

	private void loadStateApp(InputStream is) throws IOException {
		stop();
		reset();
		clear();
		loadState(is);
	}

	public void loadState(InputStream is) throws IOException {
		saved = true;
		stop();
		if (is == null)
			return;
		try {
			saxParser.parse(new InputSource(is), saxHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}

	public void saveState(OutputStream os) throws IOException {
		stop();
		if (os == null)
			return;
		try {
			os.write(encoder.encode().getBytes());
		} finally {
			os.close();
		}
	}

	void loadFile(File file) {
		setReloadButtonEnabled(true);
		if (file == null)
			return;
		try {
			loadStateApp(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentFile = file;
		currentModel = null;
		currentURL = null;
		setFrameTitle();
	}

	void loadModel(String name) {
		setReloadButtonEnabled(true);
		if (name == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		try {
			loadStateApp(QuantumBox.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentModel = name;
		currentFile = null;
		currentURL = null;
		setFrameTitle();
	}

	void loadURL(URL url) throws IOException {
		setReloadButtonEnabled(true);
		if (url == null)
			return;
		if (!askSaveBeforeLoading())
			return;
		loadStateApp(url.openConnection().getInputStream());
		notifyIOListeners(new IOEvent(IOEvent.FILE_INPUT, this));
		currentURL = url;
		currentFile = null;
		currentModel = null;
		setFrameTitle();
	}

	private void setReloadButtonEnabled(final boolean b) {
		if (buttonReload == null)
			return;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				buttonReload.setEnabled(b);
			}
		});
	}

	int askSaveOption() {
		if (saved || owner == null || currentModel != null || currentURL != null)
			return JOptionPane.NO_OPTION;
		return JOptionPane.showConfirmDialog(owner, "Do you want to save the changes?", "Energy2D", JOptionPane.YES_NO_CANCEL_OPTION);
	}

	boolean askSaveBeforeLoading() {
		if (owner == null) // not an application
			return true;
		switch (askSaveOption()) {
		case JOptionPane.YES_OPTION:
			Action a = null;
			if (currentFile != null) {
				a = view2D.getActionMap().get("Save");
			} else {
				a = view2D.getActionMap().get("SaveAs");
			}
			if (a != null)
				a.actionPerformed(null);
			return true;
		case JOptionPane.NO_OPTION:
			return true;
		default:
			return false;
		}
	}

	void setCurrentModel(String name) {
		currentModel = name;
	}

	public String getCurrentModel() {
		return currentModel;
	}

	void setCurrentFile(File file) {
		currentFile = file;
		setFrameTitle();
	}

	File getCurrentFile() {
		return currentFile;
	}

	public boolean needExecutorService() {
		return true;
	}

	public void setExecutorService(ExecutorService service) {
		threadService = service;
	}

	public void visualizationRequested(VisualizationEvent e) {
		if (view2D == null)
			return;
		view2D.setTimeInfo(propagator.getTime(), propagator.getTimeStep());
		if (propagator.getElectricField() != null)
			view2D.setElectricField(propagator.getElectricField());
		if (propagator.getMagneticField() != null)
			view2D.setMagneticField(propagator.getMagneticField());
		view2D.setProbability(propagator.getAmplitude());
		view2D.setPhase(propagator.getPhase());
		if (view2D.getDrawEnergy()) {
			view2D.setEnergies(propagator.getKineticEnergy(), propagator.getPotentialEnergy(), propagator.getTotalEnergy());
		}
		if (view2D.getDrawExpectation()) {
			view2D.setExpectationPosition(propagator.getPosition());
			view2D.setExpectationMomentum(propagator.getMomentum());
		}
		if (view2D.getDrawCurrent()) {
			view2D.setCurrent(propagator.getCurrent());
		}
		view2D.repaint();
	}

	void addIOListener(IOListener l) {
		if (ioListeners == null)
			ioListeners = new ArrayList<IOListener>();
		if (!ioListeners.contains(l))
			ioListeners.add(l);
	}

	void removeIOListener(IOListener l) {
		if (ioListeners == null)
			return;
		ioListeners.remove(l);
	}

	private void notifyIOListeners(IOEvent e) {
		setFrameTitle();
		if (ioListeners == null)
			return;
		for (IOListener x : ioListeners)
			x.ioOccured(e);
	}

	private void setFrameTitle() {
		if (owner == null)
			return;
		if (currentFile != null) {
			owner.setTitle(BRAND_NAME + ": " + currentFile);
		} else if (currentModel != null) {
			owner.setTitle(BRAND_NAME + ": " + currentModel);
		} else if (currentURL != null) {
			owner.setTitle(BRAND_NAME + ": " + currentURL);
		} else {
			owner.setTitle(BRAND_NAME);
		}
	}

	static void savePreferences(QuantumBox box) {
		if (preferences == null || box.owner == null)
			return;
		MenuBar menuBar = (MenuBar) box.owner.getJMenuBar();
		preferences.put("Latest QWB Path", menuBar.getLatestPath("qwb"));
		preferences.put("Latest HTM Path", menuBar.getLatestPath("htm"));
		String[] recentFiles = menuBar.getRecentFiles();
		if (recentFiles != null) {
			int n = recentFiles.length;
			if (n > 0)
				for (int i = 0; i < n; i++)
					preferences.put("Recent File " + i, recentFiles[n - i - 1]);
		}
	}

	public static void main(String[] args) {

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", BRAND_NAME);
		}

		if (preferences == null)
			preferences = Preferences.userNodeForPackage(QuantumBox.class);

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		final QuantumBox box = new QuantumBox();
		logLevel = 1;
		box.owner = frame;
		box.setArea(box.xmin, box.xmax, box.ymin, box.ymax);
		box.addGaussianWavePacket(.1f, 1f, 0, 0, 0, 0);
		box.propagator.buildWaveFunction();
		box.propagator.setTimeStep(0.5f);
		box.createButtonPanel();
		box.view2D.setIntensityScale(5f);

		MenuBar menuBar = new MenuBar(box, frame);
		frame.setJMenuBar(menuBar);
		frame.setContentPane(box.getContentPane());
		ToolBar toolBar = new ToolBar(box);
		box.addIOListener(toolBar);
		box.getView().addManipulationListener(toolBar);
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		frame.setLocation(100, 100);
		frame.setTitle(BRAND_NAME);
		frame.pack();
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Action a = box.view2D.getActionMap().get("Quit");
				if (a != null)
					a.actionPerformed(null);
			}
		});

		menuBar.setLatestPath(preferences.get("Latest QWB Path", null), "qwb");
		menuBar.setLatestPath(preferences.get("Latest HTM Path", null), "htm");
		menuBar.addRecentFile(preferences.get("Recent File 0", null));
		menuBar.addRecentFile(preferences.get("Recent File 1", null));
		menuBar.addRecentFile(preferences.get("Recent File 2", null));
		menuBar.addRecentFile(preferences.get("Recent File 3", null));

		if (System.getProperty("os.name").startsWith("Mac")) {
			Application app = new Application();
			app.setEnabledPreferencesMenu(true);
			app.addApplicationListener(new ApplicationAdapter() {
				public void handleQuit(ApplicationEvent e) {
					Action a = box.view2D.getActionMap().get("Quit");
					if (a != null)
						a.actionPerformed(null);
					// e.setHandled(true); //DO NOT CALL THIS!!!
				}

				public void handlePreferences(ApplicationEvent e) {
					e.setHandled(true);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
						}
					});
				}

				public void handleAbout(ApplicationEvent e) {
					Helper.showAbout(frame);
					e.setHandled(true);
				}
			});
		}

	}

}
