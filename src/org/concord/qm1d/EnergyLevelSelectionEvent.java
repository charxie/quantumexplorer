package org.concord.qm1d;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class EnergyLevelSelectionEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	private final int selectedIndex;

	public EnergyLevelSelectionEvent(Object source, int selectedIndex) {
		super(source);
		this.selectedIndex = selectedIndex;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

}
