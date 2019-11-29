package quantum.qm2d.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

import quantum.qm2d.QuantumBox;
import quantum.qmutil.ColoredLabel;
import quantum.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class ModelPropertiesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField xminField, xmaxField, yminField, ymaxField;
	private ColoredLabel bgColorLabel, probColorLabel;
	private JCheckBox contourCheckBox, gridCheckBox, rulerCheckBox, phaseColorCheckBox;
	private JCheckBox drawEnergyCheckBox, dotViewCheckBox;
	private JCheckBox frankCheckBox;
	private Window owner;
	private ActionListener okListener;

	ModelPropertiesDialog(final QuantumBox box, boolean modal) {

		super(JOptionPane.getFrameForComponent(box.getView()), "Model Properties", modal);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		owner = getOwner();

		okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				float xmin = parse(xminField.getText());
				if (Float.isNaN(xmin))
					return;
				float xmax = parse(xmaxField.getText());
				if (Float.isNaN(xmax))
					return;
				float ymin = parse(yminField.getText());
				if (Float.isNaN(ymin))
					return;
				float ymax = parse(ymaxField.getText());
				if (Float.isNaN(ymax))
					return;

				// box.xmin = xmin;
				// box.xmax = xmax;
				// box.ymin = ymin;
				// box.ymax = ymax;
				// box.setArea(xmin, xmax, ymin, ymax);

				box.getView().setFrank(frankCheckBox.isSelected());
				box.getView().setContourShown(contourCheckBox.isSelected());
				box.getView().setGridOn(gridCheckBox.isSelected());
				box.getView().setRulerOn(rulerCheckBox.isSelected());
				box.getView().setProbOnly(!phaseColorCheckBox.isSelected());
				box.getView().setDrawEnergy(drawEnergyCheckBox.isSelected());
				box.getView().setDotMode(dotViewCheckBox.isSelected());
				box.getView().setBackground(bgColorLabel.getBackground());
				box.getView().setProbColor(probColorLabel.getBackground());
				box.getView().repaint();

				ModelPropertiesDialog.this.dispose();

			}
		};

		JPanel panel = new JPanel(new BorderLayout());
		setContentPane(panel);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("OK");
		button.addActionListener(okListener);
		buttonPanel.add(button);

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelPropertiesDialog.this.dispose();
			}
		});
		buttonPanel.add(button);

		JPanel main = new JPanel(new BorderLayout(10, 10));
		main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(main);

		// Physics
		JPanel physicsPanel = new JPanel(new SpringLayout());
		physicsPanel.setBorder(BorderFactory.createTitledBorder("Physics options"));
		main.add(physicsPanel, BorderLayout.NORTH);

		// row 1
		JLabel label = new JLabel("<html>X<sub>min</sub></html>");
		physicsPanel.add(label);
		xminField = new JTextField(box.getMinX() + "", 16);
		xminField.setEditable(false);
		xminField.addActionListener(okListener);
		physicsPanel.add(xminField);
		label = new JLabel("nm");
		physicsPanel.add(label);

		// row 2
		label = new JLabel("<html>X<sub>max</sub></html>");
		physicsPanel.add(label);
		xmaxField = new JTextField(box.getMaxX() + "");
		xmaxField.setEditable(false);
		xmaxField.addActionListener(okListener);
		physicsPanel.add(xmaxField);
		label = new JLabel("nm");
		physicsPanel.add(label);

		// row 3
		label = new JLabel("<html>Y<sub>min</sub></html>");
		physicsPanel.add(label);
		yminField = new JTextField(box.getMinY() + "", 16);
		yminField.setEditable(false);
		yminField.addActionListener(okListener);
		physicsPanel.add(yminField);
		label = new JLabel("nm");
		physicsPanel.add(label);

		// row 4
		label = new JLabel("<html>Y<sub>max</sub></html>");
		physicsPanel.add(label);
		ymaxField = new JTextField(box.getMaxY() + "");
		ymaxField.setEditable(false);
		ymaxField.addActionListener(okListener);
		physicsPanel.add(ymaxField);
		label = new JLabel("nm");
		physicsPanel.add(label);

		MiscUtil.makeCompactGrid(physicsPanel, 4, 3, 5, 5, 10, 2);

		JPanel viewPanel = new JPanel(new GridLayout(6, 2, 5, 5));
		viewPanel.setBorder(BorderFactory.createTitledBorder("View options"));
		main.add(viewPanel, BorderLayout.CENTER);

		gridCheckBox = new JCheckBox("Grid lines", box.getView().isGridOn());
		viewPanel.add(gridCheckBox);
		contourCheckBox = new JCheckBox("Wave contour map", box.getView().isContourShown());
		viewPanel.add(contourCheckBox);
		rulerCheckBox = new JCheckBox("Ruler", box.getView().isRulerOn());
		viewPanel.add(rulerCheckBox);
		phaseColorCheckBox = new JCheckBox("Phase coloring", !box.getView().isProbOnly());
		viewPanel.add(phaseColorCheckBox);
		drawEnergyCheckBox = new JCheckBox("Draw energy", box.getView().getDrawEnergy());
		viewPanel.add(drawEnergyCheckBox);
		dotViewCheckBox = new JCheckBox("Dot view", box.getView().getDotMode());
		viewPanel.add(dotViewCheckBox);
		frankCheckBox = new JCheckBox("Brand", box.getView().isFrank());
		viewPanel.add(frankCheckBox);
		viewPanel.add(new JLabel());
		bgColorLabel = new ColoredLabel(box.getView().getBackground());
		bgColorLabel.setForeground(MiscUtil.getContrastColor(box.getView().getBackground()));
		bgColorLabel.setText("Background");
		viewPanel.add(bgColorLabel);
		probColorLabel = new ColoredLabel(box.getView().getProbColor());
		probColorLabel.setForeground(MiscUtil.getContrastColor(box.getView().getProbColor()));
		probColorLabel.setText("Amplitude");
		viewPanel.add(probColorLabel);

		pack();
		setLocationRelativeTo(box.getView());

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
