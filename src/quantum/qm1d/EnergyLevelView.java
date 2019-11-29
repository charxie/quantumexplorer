package quantum.qm1d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import quantum.math.MathUtil;

/**
 * @author Charles Xie
 * 
 */
class EnergyLevelView extends JPanel {

	private static final long serialVersionUID = 1L;
	private double[] energyLevel;
	private double[] pot;
	private int selectedIndex;
	private int hoverIndex = selectedIndex;
	private int energyLevelDiagramOffSet = 10;
	private GeneralPath path;
	private Stroke nonselectionStroke = new BasicStroke(1);
	private Stroke selectionStroke = new BasicStroke(3);
	private Stroke potentialStroke = new BasicStroke(5);
	private double vmin = -10, vmax = 10;
	private List<EnergyLevelSelectionListener> selectionListeners;

	EnergyLevelView() {
		super();
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				processMousePressed(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				processMouseMoved(e);
			}
		});
	}

	void addEnergyLevelSelectionListener(EnergyLevelSelectionListener listener) {
		if (selectionListeners == null)
			selectionListeners = new ArrayList<EnergyLevelSelectionListener>();
		if (!selectionListeners.contains(listener))
			selectionListeners.add(listener);
	}

	void removeEnergyLevelSelectionListener(EnergyLevelSelectionListener listener) {
		if (selectionListeners != null)
			selectionListeners.remove(listener);
	}

	private void notifyEnergyLevelSelection(EnergyLevelSelectionEvent e) {
		if (selectionListeners == null)
			return;
		for (EnergyLevelSelectionListener x : selectionListeners) {
			x.energyLevelSelected(e);
		}
	}

	int getSelectedIndex() {
		return selectedIndex;
	}

	void setPotential(double[] pot) {
		this.pot = pot;
		vmin = MathUtil.getMin(pot);
		vmax = MathUtil.getMax(pot);
		if (vmax > 10)
			vmax = 10;
		if (vmin < -10)
			vmin = -10;
	}

	void setEigenEnergies(double[] eigenEnergy) {
		energyLevel = eigenEnergy;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	@Override
	public void update(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		drawAxes(g);
		drawPotentialFunction(g2);
		drawEnergyLevels(g2);
	}

	private void drawAxes(Graphics g) {
		g.setColor(Color.lightGray);
		int halfWidth = getWidth() / 2;
		g.drawLine(halfWidth, 0, halfWidth, getHeight());
	}

	private void drawPotentialFunction(Graphics2D g2) {
		if (pot == null)
			return;
		if (path == null)
			path = new GeneralPath();
		else
			path.reset();
		double dy = getHeight() / (vmax - vmin);
		double dx = (double) getWidth() / (double) pot.length;
		path.moveTo(0, (float) (getHeight() - (pot[0] - vmin) * dy));
		for (int i = 1; i < pot.length; i++) {
			path.lineTo((float) (i * dx), (float) (getHeight() - (pot[i] - vmin) * dy));
		}
		g2.setColor(Color.gray);
		g2.setStroke(potentialStroke);
		g2.draw(path);

	}

	private void drawEnergyLevels(Graphics2D g2) {
		if (energyLevel == null)
			return;
		int n = energyLevel.length;
		double max = energyLevel[n - 1] - energyLevel[0];
		float delta = (float) (getHeight() - energyLevelDiagramOffSet * 2) / (float) max;
		int h = 0;
		for (int i = 0; i < n; i++) {
			h = (int) (getHeight() - energyLevelDiagramOffSet - delta * (energyLevel[i] - energyLevel[0]));
			if (i == hoverIndex) {
				g2.setColor(Color.yellow);
				g2.fillRoundRect(8, h - 18, 20, 20, 5, 5);
				g2.setColor(getForeground());
				g2.drawString(i + "", 10, h);
				g2.setStroke(selectionStroke);
				g2.setColor(Color.red);
			} else {
				g2.setStroke(nonselectionStroke);
				g2.setColor(getForeground());
			}
			g2.drawLine(0, h, getWidth(), h);
		}
	}

	private int whichEnergyLevel(int y) {
		if (energyLevel == null)
			return -1;
		int n = energyLevel.length;
		double max = energyLevel[n - 1] - energyLevel[0];
		float delta = (float) (getHeight() - energyLevelDiagramOffSet * 2) / (float) max;
		int h = 0;
		for (int i = 0; i < n; i++) {
			h = (int) (getHeight() - energyLevelDiagramOffSet - delta * (energyLevel[i] - energyLevel[0]));
			if (Math.abs(h - y) < 5)
				return i;
		}
		return -1;
	}

	private void processMousePressed(MouseEvent e) {

	}

	private void processMouseMoved(MouseEvent e) {
		hoverIndex = whichEnergyLevel(e.getY());
		if (hoverIndex != -1) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			notifyEnergyLevelSelection(new EnergyLevelSelectionEvent(this, hoverIndex));
		} else {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		repaint();
	}

}