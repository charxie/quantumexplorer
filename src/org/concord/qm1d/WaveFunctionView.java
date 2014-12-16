package org.concord.qm1d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import javax.swing.JPanel;

import org.concord.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class WaveFunctionView extends JPanel implements EnergyLevelSelectionListener {

	private static final long serialVersionUID = 1L;
	private static Color waveColor = new Color(127, 127, 127, 127);

	private double[][] waveFun;
	private GeneralPath path;
	private int selectedIndex = 0;
	private float magnitudeRatio = 2;
	private Font displayNameFont = new Font(null, Font.BOLD, 11);
	private final static Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 2f }, 0.0f);
	private final static Stroke normal = new BasicStroke(1);
	private boolean frank = true;

	WaveFunctionView() {
		super();
	}

	void setSelectedIndex(int i) {
		selectedIndex = i;
	}

	void setEigenVectors(double[][] eigenVector) {
		waveFun = eigenVector;
	}

	void setFrank(boolean b) {
		frank = b;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	@Override
	public void update(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		drawAxes(g);
		drawDisplayName(g);
		g2.setColor(Color.gray);
		g2.setStroke(dashed);
		drawSelectedWaveFunction(g2, false);
		g2.setColor(getForeground());
		g2.setStroke(normal);
		drawSelectedWaveFunction(g2, true);
		if (frank)
			MiscUtil.drawFrank(g2, getWidth() - 125, 15);
	}

	private void drawDisplayName(Graphics g) {
		g.setColor(Color.lightGray);
		g.setFont(displayNameFont);
		g.drawString("Wave Function", 10, 30);
		g.drawString("Amplitude", 10, getHeight() / 2 + 40);
	}

	private void drawAxes(Graphics g) {
		g.setColor(Color.lightGray);
		int halfHeight = getHeight() / 2;
		int halfWidth = getWidth() / 2;
		g.drawLine(0, halfHeight, getWidth(), halfHeight);
		g.drawLine(halfWidth, 0, halfWidth, getHeight());
	}

	private void drawSelectedWaveFunction(Graphics2D g2, boolean drawAmplitude) {
		if (waveFun == null)
			return;
		int halfHeight = getHeight() / 2;
		if (path == null)
			path = new GeneralPath();
		else
			path.reset();
		double x = waveFun[selectedIndex][0];
		if (drawAmplitude) {
			path.moveTo(0, (float) (getHeight() - halfHeight * x * x * magnitudeRatio * 5));
		} else {
			path.moveTo(0, (float) (halfHeight * (1 + x * magnitudeRatio)));
		}
		float delta = (float) getWidth() / (float) waveFun[selectedIndex].length;
		for (int i = 1; i < waveFun[selectedIndex].length; i++) {
			x = waveFun[selectedIndex][i];
			if (drawAmplitude) {
				path.lineTo((float) (i * delta), (float) (getHeight() - x * x * magnitudeRatio * 5 * halfHeight));
			} else {
				path.lineTo((float) (i * delta), (float) ((1 + x * magnitudeRatio) * halfHeight));
			}
		}
		if (drawAmplitude) {
			path.closePath();
			g2.setColor(waveColor);
			g2.fill(path);
			g2.setColor(Color.gray);
		} else {
			g2.setColor(Color.black);
		}
		g2.draw(path);
	}

	public void energyLevelSelected(EnergyLevelSelectionEvent e) {
		int i = e.getSelectedIndex();
		if (i >= 0 && i < waveFun.length) {
			selectedIndex = i;
			repaint();
		}
	}

}
