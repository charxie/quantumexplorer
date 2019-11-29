package quantum.qm2d.view;

import javax.swing.JDialog;

import quantum.qm2d.QuantumBox;
import quantum.qm2d.model.AnnularPotential;
import quantum.qm2d.model.EllipticalPotential;
import quantum.qm2d.model.IonicPotential;
import quantum.qm2d.model.RectangularPotential;

/**
 * @author Charles Xie
 * 
 */
final class DialogFactory {

	private QuantumBox box;
	private boolean modal = true;

	DialogFactory(QuantumBox box) {
		this.box = box;
	}

	void setModal(boolean modal) {
		this.modal = modal;
	}

	JDialog createDialog(Object o) {
		if (o instanceof RectangularPotential)
			return new RectangularPotentialDialog(box.getView(), (RectangularPotential) o, modal);
		if (o instanceof EllipticalPotential)
			return new EllipticalPotentialDialog(box.getView(), (EllipticalPotential) o, modal);
		if (o instanceof AnnularPotential)
			return new AnnularPotentialDialog(box.getView(), (AnnularPotential) o, modal);
		if (o instanceof IonicPotential)
			return new IonicPotentialDialog(box.getView(), (IonicPotential) o, modal);
		if (o instanceof QuantumBox)
			return new ModelPropertiesDialog(box, modal);
		return null;
	}

}
