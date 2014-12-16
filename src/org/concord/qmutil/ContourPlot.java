package org.concord.qmutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import org.concord.math.MathUtil;

public class ContourPlot {

	private float resolutionUnit = 0.00001f;
	private float resolutionScale = 5;
	private float resolution = resolutionScale * resolutionUnit;
	private Color color = Color.gray;
	private float[][] func;
	private int nx, ny;
	private Dimension size;
	private Point pa, pb;
	private int step = 2;
	private int marginLeft, marginRight, marginTop, marginBottom;
	private int nx2, ny2;

	public ContourPlot() {
		pa = new Point();
		pb = new Point();
	}

	public void setMargins(int top, int bottom, int left, int right) {
		marginTop = top;
		marginBottom = bottom;
		marginLeft = left;
		marginRight = right;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setResolutionScale(float resolutionScale) {
		this.resolutionScale = resolutionScale;
		resolution = resolutionScale * resolutionUnit;
	}

	public float getResolutionScale() {
		return resolutionScale;
	}

	public void render(Graphics g, Dimension size, float[][] func) {
		this.func = func;
		this.nx = func.length;
		this.ny = func[0].length;
		this.size = size;
		g.setColor(color);
		nx2 = nx - marginLeft - marginRight;
		ny2 = ny - marginTop - marginBottom;
		for (int x = 0; x < nx2 - step; x += step)
			for (int y = 0; y < ny2 - step; y += step) {
				connect(g, x, y, x + step, y, x, y + step, x + step, y + step);
				connect(g, x, y, x + step, y, x, y, x, y + step);
				connect(g, x, y, x + step, y, x + step, y, x + step, y + step);
				connect(g, x, y, x, y + step, x + step, y, x + step, y + step);
				connect(g, x, y, x, y + step, x, y + step, x + step, y + step);
				connect(g, x + step, y, x + step, y + step, x, y + step, x + step, y + step);
			}
	}

	// draw a contour line between (x1, y1) - (x2, y2) and (x3, y3) - (x4, y4) if applicable
	private void connect(Graphics g, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		float f1 = func[x1 + marginLeft][y1 + marginTop];
		float f2 = func[x2 + marginLeft][y2 + marginTop];
		float f3 = func[x3 + marginLeft][y3 + marginTop];
		float f4 = func[x4 + marginLeft][y4 + marginTop];
		float fmin = Math.min(Math.min(f1, f2), Math.min(f3, f4));
		float fmax = Math.max(Math.max(f1, f2), Math.max(f3, f4));
		int imin = (int) (fmin / resolution);
		int imax = (int) (fmax / resolution);
		if (imin != imax) {
			float v;
			for (int i = imin; i <= imax; i++) {
				v = i * resolution;
				if (MathUtil.between(f1, f2, v) && MathUtil.between(f3, f4, v)) {
					interpolate(f1, f2, x1, y1, x2, y2, v, pa);
					interpolate(f3, f4, x3, y3, x4, y4, v, pb);
					g.drawLine(pa.x, pa.y, pb.x, pb.y);
				}
			}
		}
	}

	private void interpolate(float f1, float f2, int x1, int y1, int x2, int y2, float v, Point p) {
		float r2 = (v - f1) / (f2 - f1);
		float r1 = 1 - r2;
		float h = 0.5f * step;
		p.x = (int) (((x1 + h) * r1 + (x2 + h) * r2) * size.width / (float) nx2);
		p.y = (int) (((y1 + h) * r1 + (y2 + h) * r2) * size.height / (float) ny2);
	}

}
