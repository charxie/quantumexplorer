package org.concord.qmutil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * @author Charles Xie
 * 
 */
public final class MiscUtil {

	private final static String FILE_SEPARATOR = System.getProperty("file.separator");

	/** return the file name of this path */
	public static String getFileName(String path) {
		if (path == null)
			return null;
		int i = path.lastIndexOf("/");
		if (i == -1)
			i = path.lastIndexOf("\\");
		if (i == -1)
			i = path.lastIndexOf(FILE_SEPARATOR);
		if (i == -1)
			return path;
		return path.substring(i + 1, path.length());
	}

	public static void setSelectedSilently(AbstractButton x, boolean b) {
		ActionListener[] al = x.getActionListeners();
		if (al != null && al.length > 0) {
			for (ActionListener a : al)
				x.removeActionListener(a);
		}
		ItemListener[] il = x.getItemListeners();
		if (il != null && il.length > 0) {
			for (ItemListener a : il)
				x.removeItemListener(a);
		}
		x.setSelected(b);
		if (al != null && al.length > 0) {
			for (ActionListener a : al)
				x.addActionListener(a);
		}
		if (il != null && il.length > 0) {
			for (ItemListener a : il)
				x.addItemListener(a);
		}
	}

	/** copy two-dimension arrays */
	public static void copy(float[][] dst, float[][] src) {
		for (int i = 0; i < src.length; i++)
			System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
	}

	public static void drawFrank(Graphics g, int x, int y) {
		g.setFont(new Font("Arial", Font.BOLD, 12));
		g.setColor(Color.gray);
		String s = "Quantum Workbench";
		g.drawString(s, x + 1, y - 1);
		g.drawString(s, x + 1, y + 1);
		g.drawString(s, x - 1, y - 1);
		g.drawString(s, x - 1, y + 1);
		g.setColor(Color.white);
		g.drawString(s, x, y);
	}

	/**
	 * platform-independent check for Windows' equivalent of right click of mouse button. This can be used as an alternative as MouseEvent.isPopupTrigger(), which requires checking within both mousePressed() and mouseReleased() methods.
	 */
	public static boolean isRightClick(MouseEvent e) {
		if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0)
			return true;
		if (System.getProperty("os.name").startsWith("Mac") && e.isControlDown())
			return true;
		return false;
	}

	public static Color getContrastColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}

	public static Color parseRGBColor(String str) {
		if (str.startsWith("0x")) {
			try {
				return new Color(Integer.valueOf(str.substring(2), 16));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if (str.startsWith("#")) {
			try {
				return new Color(Integer.valueOf(str.substring(1), 16));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Aligns the first <code>rows</code> <code>cols</code> components of <code>parent</code> in a grid. Each component in a column is as wide as the maximum preferred width of the components in that column; height is similarly determined for each row. The parent is made just big enough to fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);

	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int r, int c, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component component = parent.getComponent(r * cols + c);
		return layout.getConstraints(component);
	}

}
