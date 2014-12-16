package org.concord.qmutil;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * @author Charles Xie
 * 
 */
public class ColoredLabel extends JLabel {

	private static final long serialVersionUID = 1L;
	private static JColorChooser colorChooser;

	public ColoredLabel(Color color) {

		super();

		setOpaque(true);
		setText("Double-click to change");
		setHorizontalAlignment(CENTER);
		setBackground(color);
		setBorder(BorderFactory.createEtchedBorder());

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() > 1) {
					if (colorChooser == null) {
						colorChooser = new JColorChooser(getBackground());
					} else {
						colorChooser.setColor(getBackground());
					}
					JDialog d = JColorChooser.createDialog(ColoredLabel.this, "Color", true, colorChooser, new ActionListener() {
						public void actionPerformed(ActionEvent e) { // ok
							ColoredLabel.this.setBackground(colorChooser.getColor());
							ColoredLabel.this.setForeground(MiscUtil.getContrastColor(colorChooser.getColor()));
							ColoredLabel.this.repaint();
						}
					}, new ActionListener() {// cancel
								public void actionPerformed(ActionEvent e) {

								}
							});
					d.pack();
					d.setVisible(true);
				}
			}
		});

	}

}
