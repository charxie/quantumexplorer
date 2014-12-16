package org.concord.qm2d.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JComponent;

import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
class WaveFunctionRenderer {

	private static final float INTENSITY_UNIT = 1000000f / 255f;

	private BufferedImage image;
	private int[] pixels;
	private int w, h;
	private float scale = 1;
	private boolean probOnly;
	private boolean dotMode;
	private float[] rgb = new float[3];
	private Boundary xBoundary, yBoundary;
	private Color probColor = new Color(0x0099ff);
	private float inversePi;
	private int dotCellSize = 4;

	WaveFunctionRenderer() {
		inversePi = 1.0f / (float) Math.PI;
	}

	void setProbColor(Color c) {
		probColor = c;
	}

	Color getProbColor() {
		return probColor;
	}

	void setProbOnly(boolean b) {
		probOnly = b;
	}

	boolean isProbOnly() {
		return probOnly;
	}

	void setDotCellSize(int dotCellSize) {
		if (dotCellSize > 0)
			this.dotCellSize = dotCellSize;
	}

	int getDotCellSize() {
		return dotCellSize;
	}

	void setDotMode(boolean b) {
		dotMode = b;
	}

	boolean getDotMode() {
		return dotMode;
	}

	void setIntensityScale(float scale) {
		this.scale = scale;
	}

	float getIntensityScale() {
		return scale;
	}

	void setBoundary(char direction, Boundary boundary) {
		if (direction == 'x' || direction == 'X')
			xBoundary = boundary;
		if (direction == 'y' || direction == 'Y')
			yBoundary = boundary;
	}

	void renderProbility(float[][] prob, float[][] phase, JComponent c, Graphics2D g) {

		if (!c.isVisible())
			return;

		w = c.getWidth();
		h = c.getHeight();
		createImage(w, h, c);

		Color bgColor = c.getBackground();
		int rBg = bgColor.getRed();
		int gBg = bgColor.getGreen();
		int bBg = bgColor.getBlue();

		int m = prob.length;
		int m0 = 0;
		if (xBoundary instanceof AbsorbingBoundary) {
			m0 = (int) (((AbsorbingBoundary) xBoundary).getLengthPercentage() * m);
			m -= 2 * m0;
		}
		int n = prob[0].length;
		int n0 = 0;
		if (yBoundary instanceof AbsorbingBoundary) {
			n0 = (int) (((AbsorbingBoundary) yBoundary).getLengthPercentage() * n);
			n -= 2 * n0;
		}

		float dx = (float) m / (float) w;
		float dy = (float) n / (float) h;

		int r2 = 0, g2 = 0, b2 = 0;
		int ix, iy;
		float v;
		if (probOnly) {
			rgb[0] = probColor.getRed();
			rgb[1] = probColor.getGreen();
			rgb[2] = probColor.getBlue();
		}
		float scale2 = scale * INTENSITY_UNIT;
		if (dotMode) {
			Arrays.fill(pixels, 0);
			int i2, j2;
			for (int i = 0; i < w; i += dotCellSize) {
				ix = (int) (i * dx + m0);
				for (int j = 0; j < h; j += dotCellSize) {
					iy = (int) (j * dy + n0);
					if (!probOnly)
						calculatePhaseColor(phase[ix][iy]);
					v = prob[ix][iy] * scale2;
					if (v > 1)
						v = 1;
					r2 = (int) (rBg + (rgb[0] - rBg) * v);
					g2 = (int) (gBg + (rgb[1] - gBg) * v);
					b2 = (int) (bBg + (rgb[2] - bBg) * v);
					i2 = (int) (i + dotCellSize * (Math.random() - 0.5));
					j2 = (int) (j + dotCellSize * (Math.random() - 0.5));
					if (i2 < 0)
						i2 = i;
					else if (i2 >= w)
						i2 = w - 1;
					if (j2 < 0)
						j2 = j;
					else if (j2 >= h)
						j2 = h - 1;
					pixels[i2 + j2 * w] = (((int) (255 * v)) << 24) | (r2 << 16) | (g2 << 8) | b2;
				}
			}
		} else {
			for (int i = 0; i < w; i++) {
				ix = (int) (i * dx + m0);
				for (int j = 0; j < h; j++) {
					iy = (int) (j * dy + n0);
					if (!probOnly)
						calculatePhaseColor(phase[ix][iy]);
					v = prob[ix][iy] * scale2;
					if (v > 1)
						v = 1;
					r2 = (int) (rBg + (rgb[0] - rBg) * v);
					g2 = (int) (gBg + (rgb[1] - gBg) * v);
					b2 = (int) (bBg + (rgb[2] - bBg) * v);
					pixels[i + j * w] = (((int) (255 * v)) << 24) | (r2 << 16) | (g2 << 8) | b2;
				}
			}
		}

		image.setRGB(0, 0, w, h, pixels, 0, w);
		g.drawImage(image, 0, 0, c);

	}

	private void calculatePhaseColor(float angle) {
		float p = (angle * inversePi + 1) * 3;
		float a2 = p % 1;
		float a3 = 1 - a2;
		switch ((int) p) {
		case 6:
		case 0:
			rgb[0] = 255;
			rgb[1] = a2 * 255;
			rgb[2] = 0;
			break;
		case 1:
			rgb[0] = a3 * 255;
			rgb[1] = 255;
			rgb[2] = 0;
			break;
		case 2:
			rgb[0] = 0;
			rgb[1] = 255;
			rgb[2] = a2 * 255;
			break;
		case 3:
			rgb[0] = 0;
			rgb[1] = a3 * 255;
			rgb[2] = 255;
			break;
		case 4:
			rgb[0] = a2 * 255;
			rgb[1] = 0;
			rgb[2] = 255;
			break;
		case 5:
			rgb[0] = 255;
			rgb[1] = 0;
			rgb[2] = a3 * 255;
			break;
		}
	}

	private void createImage(int w, int h, JComponent c) {
		if (image != null) {
			if (w != image.getWidth(c) || h != image.getHeight(c)) {
				image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				pixels = new int[w * h];
			}
		} else {
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			pixels = new int[w * h];
		}
	}

}
