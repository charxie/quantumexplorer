package org.concord.qm2d.model;

import java.awt.Color;

import org.concord.qmshared.AbsorbingBoundary;
import org.concord.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
public abstract class Shape2D {

	protected float xmin, xmax, ymin, ymax;
	protected float xcenter, ycenter;

	private Color color = Color.gray;
	private boolean visible = true;
	private boolean draggable = true;
	private boolean selected;
	private String label;
	private String uid;
	private Boundary xBoundary, yBoundary;

	public Shape2D(float xmin, float xmax, float ymin, float ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
	}

	public boolean isDraggable() {
		return draggable;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setXcenter(float xcenter) {
		this.xcenter = xcenter;
	}

	public float getXcenter() {
		return xcenter;
	}

	public void setYcenter(float ycenter) {
		this.ycenter = ycenter;
	}

	public float getYcenter() {
		return ycenter;
	}

	public abstract boolean contains(float x, float y);

	public void setSelected(boolean b) {
		selected = b;
	}

	public boolean isSelected() {
		return selected;
	}

	void setBoundaries(Boundary xBoundary, Boundary yBoundary) {
		this.xBoundary = xBoundary;
		this.yBoundary = yBoundary;
	}

	float convertPointX(int x, int w) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return xmin2 + (xmax2 - xmin2) * (float) x / (float) w;
		}
		return xmin + (xmax - xmin) * (float) x / (float) w;
	}

	float convertPointY(int y, int h) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return ymin2 + (ymax2 - ymin2) * (float) y / (float) h;
		}
		return ymin + (ymax - ymin) * (float) y / (float) h;
	}

	int convertPointX(float x, float w) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return (int) ((x - xmin2) / (xmax2 - xmin2) * w);
		}
		return (int) ((x - xmin) / (xmax - xmin) * w);
	}

	int convertPointY(float y, float h) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return (int) ((y - ymin2) / (ymax2 - ymin2) * h);
		}
		return (int) ((y - ymin) / (ymax - ymin) * h);
	}

	int convertLengthX(float l, float w) {
		if (xBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) xBoundary;
			float xmin2 = xmin + a.getLengthPercentage() * (xmax - xmin);
			float xmax2 = xmax - a.getLengthPercentage() * (xmax - xmin);
			return (int) (l / (xmax2 - xmin2) * w);
		}
		return (int) (l / (xmax - xmin) * w);
	}

	int convertLengthY(float l, float h) {
		if (yBoundary instanceof AbsorbingBoundary) {
			AbsorbingBoundary a = (AbsorbingBoundary) yBoundary;
			float ymin2 = ymin + a.getLengthPercentage() * (ymax - ymin);
			float ymax2 = ymax - a.getLengthPercentage() * (ymax - ymin);
			return (int) (l / (ymax2 - ymin2) * h);
		}
		return (int) (l / (ymax - ymin) * h);
	}

	public abstract String toXml();

}
