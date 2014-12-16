package org.concord.qm2d.view;

import java.awt.Color;
import java.awt.Font;

import org.concord.qm2d.model.Shape2D;

/**
 * @author Charles Xie
 * 
 */
public class TextBox {

	private String text;
	private String name = "Arial";
	private int style = Font.PLAIN | Font.BOLD;
	private int size = 12;
	private Color color = Color.black;
	private float x = 0, y = 0;
	private Shape2D host;

	public TextBox(String text, float x, float y) {
		setText(text);
		setLocation(x, y);
	}

	public void setHost(Shape2D host) {
		this.host = host;
	}

	public Shape2D getHost() {
		return host;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getX() {
		return x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getY() {
		return y;
	}

	public void setLocation(float x, float y) {
		setX(x);
		setY(y);
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public String toString() {
		return text + ", (" + x + ", " + y + "), " + color;
	}

	public String toXml() {
		String xml = "<textbox text=\"" + text;
		xml += "\" x=\"" + x;
		xml += "\" y=\"" + y;
		xml += "\" color=\"" + Integer.toHexString(0x00ffffff & color.getRGB());
		xml += "\" size=\"" + size;
		xml += "\" style=\"" + style;
		xml += "\" name=\"" + name;
		xml += "\"/>\n";
		return xml;
	}

}
