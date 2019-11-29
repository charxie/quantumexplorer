package quantum.qm2d;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import quantum.qmevent.IOEvent;
import quantum.qmevent.IOListener;
import quantum.qmevent.ManipulationEvent;
import quantum.qmevent.ManipulationListener;
import quantum.qmutil.MiscUtil;
import quantum.qm2d.view.View2D;

/**
 * @author Charles Xie
 * 
 */
class ToolBar extends JToolBar implements IOListener, ManipulationListener {

	private static final long serialVersionUID = 1L;
	private JToggleButton gridButton;
	private JToggleButton selectButton;

	private QuantumBox box;

	ToolBar(QuantumBox qb) {

		super(HORIZONTAL);
		setFloatable(false);

		box = qb;

		ButtonGroup bg = new ButtonGroup();

		selectButton = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/select.png")));
		selectButton.setToolTipText("Select and move an object");
		selectButton.setSelected(true);
		selectButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.getView().setActionMode(View2D.SELECT_MODE);
			}
		});
		add(selectButton);
		bg.add(selectButton);

		JToggleButton x = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/rectangle.png")));
		x.setToolTipText("Draw a rectangle");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.getView().setActionMode(View2D.RECTANGLE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		x = new JToggleButton(new ImageIcon(ToolBar.class.getResource("resources/ellipse.png")));
		x.setToolTipText("Draw an ellipse");
		x.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				box.getView().setActionMode(View2D.ELLIPSE_MODE);
			}
		});
		x.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// MiscUtil.setSelectedSilently(graphButton, false);
			}
		});
		add(x);
		bg.add(x);

		gridButton = new JToggleButton(new ImageIcon(ToolBar.class
				.getResource("resources/grid.png")));
		gridButton.setToolTipText("Show or hide grid lines");
		gridButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				box.getView().setGridOn(src.isSelected());
				box.getView().repaint();
				box.getView().notifyManipulationListeners(null, ManipulationEvent.PROPERTY_CHANGE);
			}
		});
		add(gridButton);

	}

	public void manipulationOccured(ManipulationEvent e) {
		switch (e.getType()) {
		case ManipulationEvent.OBJECT_ADDED:
			selectButton.doClick();
			break;
		default:
			MiscUtil.setSelectedSilently(gridButton, box.getView().isGridOn());
		}
	}

	public void ioOccured(IOEvent e) {
		switch (e.getType()) {
		case IOEvent.FILE_INPUT:
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					selectButton.doClick();
					selectButton.requestFocusInWindow();
					MiscUtil.setSelectedSilently(gridButton, box.getView().isGridOn());
				}
			});
			break;
		case IOEvent.FILE_OUTPUT:
			break;
		}
	}

}
