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

import quantum.qm2d.model.AnnularPotential;
import quantum.qmutil.ColoredLabel;
import quantum.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class AnnularPotentialDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField energyField, xField, yField, outerRxField, outerRyField, innerRxField, innerRyField;
	private ColoredLabel coloredLabel;
	private JCheckBox visibleCheckBox, draggableCheckBox;
	private Window owner;
	private ActionListener okListener;

	AnnularPotentialDialog(final View2D view, final AnnularPotential potential, boolean modal) {

		super(JOptionPane.getFrameForComponent(view), "Annular Potential (#" + view.quantumBox.indexOfPotential(potential) + ")", modal);
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
				float outerRx = parse(outerRxField.getText());
				if (Float.isNaN(outerRx))
					return;
				float outerRy = parse(outerRyField.getText());
				if (Float.isNaN(outerRy))
					return;
				float innerRx = parse(innerRxField.getText());
				if (Float.isNaN(innerRx))
					return;
				float innerRy = parse(innerRyField.getText());
				if (Float.isNaN(innerRy))
					return;
				int index = view.quantumBox.removePotential(potential);

				potential.setEnergy(energy);
				potential.setXcenter(xcenter);
				potential.setYcenter(ycenter);
				potential.setOuterRx(outerRx);
				potential.setOuterRy(outerRy);
				potential.setInnerRx(innerRx);
				potential.setInnerRy(innerRy);
				potential.setColor(coloredLabel.getBackground());
				potential.setVisible(visibleCheckBox.isSelected());
				potential.setDraggable(draggableCheckBox.isSelected());
				view.setSelectedPotential(view.quantumBox.addPotential(index, potential));
				view.repaint();

				AnnularPotentialDialog.this.dispose();

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
				AnnularPotentialDialog.this.dispose();
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
		label = new JLabel("Outer half width");
		main.add(label);
		outerRxField = new JTextField(potential.getOuterRx() + "");
		outerRxField.addActionListener(okListener);
		main.add(outerRxField);
		label = new JLabel("nm");
		main.add(label);

		// row 5
		label = new JLabel("Outer half height");
		main.add(label);
		outerRyField = new JTextField(potential.getOuterRy() + "");
		outerRyField.addActionListener(okListener);
		main.add(outerRyField);
		label = new JLabel("nm");
		main.add(label);

		// row 6
		label = new JLabel("Inner half width");
		main.add(label);
		innerRxField = new JTextField(potential.getInnerRx() + "");
		innerRxField.addActionListener(okListener);
		main.add(innerRxField);
		label = new JLabel("nm");
		main.add(label);

		// row 7
		label = new JLabel("Inner half height");
		main.add(label);
		innerRyField = new JTextField(potential.getInnerRy() + "");
		innerRyField.addActionListener(okListener);
		main.add(innerRyField);
		label = new JLabel("nm");
		main.add(label);

		// row 8
		label = new JLabel("Color");
		main.add(label);
		coloredLabel = new ColoredLabel(potential.getColor());
		main.add(coloredLabel);
		label = new JLabel();
		main.add(label);

		MiscUtil.makeCompactGrid(main, 8, 3, 5, 5, 10, 2);

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
