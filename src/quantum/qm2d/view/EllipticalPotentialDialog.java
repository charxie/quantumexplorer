package quantum.qm2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import quantum.qm2d.model.EllipticalPotential;
import quantum.qmutil.ColoredLabel;
import quantum.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class EllipticalPotentialDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField energyField, xField, yField, aField, bField;
	private ColoredLabel coloredLabel;
	private JCheckBox visibleCheckBox, draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	EllipticalPotentialDialog(final View2D view, final EllipticalPotential potential, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Elliptical Potential (#" + view.quantumBox.indexOfPotential(potential) + ")", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float energy = parse(energyField.getText());
				if (Float.isNaN(energy))
					return;
				float xcenter = parse(xField.getText());
				if (Float.isNaN(xcenter))
					return;
				float ycenter = parse(yField.getText());
				if (Float.isNaN(ycenter))
					return;
				float width = parse(aField.getText());
				if (Float.isNaN(width))
					return;
				float height = parse(bField.getText());
				if (Float.isNaN(height))
					return;
				int index = view.quantumBox.removePotential(potential);

				potential.setEnergy(energy);
				potential.setXcenter(xcenter);
				potential.setYcenter(ycenter);
				potential.setRx(width);
				potential.setRy(height);
				potential.setColor(coloredLabel.getBackground());
				potential.setVisible(visibleCheckBox.isSelected());
				potential.setDraggable(draggableCheckBox.isSelected());
				view.setSelectedPotential(view.quantumBox.addPotential(index, potential));
				view.repaint();

				EllipticalPotentialDialog.this.dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		visibleCheckBox = new JCheckBox("Visible", potential.isVisible());
		buttonPanel.add(visibleCheckBox);

		draggableCheckBox = new JCheckBox("Draggable", potential.isDraggable());
		buttonPanel.add(draggableCheckBox);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EllipticalPotentialDialog.this.dispose();
			}
		});
		buttonPanel.add(button);

		JPanel main = new JPanel(new SpringLayout());
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(main);

		// row 1
		JLabel label = new JLabel("Energy");
		main.add(label);
		energyField = new JTextField(potential.getEnergy() + "", 16);
		energyField.addActionListener(okListener);
		main.add(energyField);
		label = new JLabel("eV");
		main.add(label);

		// row 2
		label = new JLabel("Center x");
		main.add(label);
		xField = new JTextField(potential.getXcenter() + "", 10);
		xField.addActionListener(okListener);
		main.add(xField);
		label = new JLabel("nm");
		main.add(label);

		// row 3
		label = new JLabel("Center y");
		main.add(label);
		yField = new JTextField(potential.getYcenter() + "");
		yField.addActionListener(okListener);
		main.add(yField);
		label = new JLabel("nm");
		main.add(label);

		// row 4
		label = new JLabel("a");
		main.add(label);
		aField = new JTextField(potential.getRx() + "");
		aField.addActionListener(okListener);
		main.add(aField);
		label = new JLabel("nm");
		main.add(label);

		// row 5
		label = new JLabel("b");
		main.add(label);
		bField = new JTextField(potential.getRy() + "");
		bField.addActionListener(okListener);
		main.add(bField);
		label = new JLabel("nm");
		main.add(label);

		// row 6
		label = new JLabel("Color");
		main.add(label);
		coloredLabel = new ColoredLabel(potential.getColor());
		main.add(coloredLabel);
		label = new JLabel();
		main.add(label);

		MiscUtil.makeCompactGrid(main, 6, 3, 5, 5, 10, 2);

		pack();

	}

	private float parse(String s) {
		float x = Float.NaN;
		try {
			x = Float.parseFloat(s);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(owner, "Cannot parse: " + s, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return x;
	}

}
