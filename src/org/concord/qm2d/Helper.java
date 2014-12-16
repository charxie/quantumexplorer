package org.concord.qm2d;

import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * @author Charles Xie
 * 
 */
public class Helper {

	private static ScriptDialog scriptDialog;

	private Helper() {
	}

	public final static void showScriptDialog(QuantumBox box) {
		if (scriptDialog != null && scriptDialog.isShowing()) {
			scriptDialog.toFront();
		} else {
			scriptDialog = new ScriptDialog(box);
			scriptDialog.pack();
			scriptDialog.setLocationRelativeTo(box.getView());
			scriptDialog.setVisible(true);
		}
	}

	public final static void showKeyboardShortcuts(Frame frame) {
		String s = "<html><h2>Keyboard Shortcuts</h2><hr>";
		s += "<br><font face=Courier>'R'</font> &mdash; Run or pause the simulation.";
		s += "<br><font face=Courier>'T'</font> &mdash; Reset the simulation.";
		s += "<br><font face=Courier>'L'</font> &mdash; Reload the initial configurations.";
		s += "<br><font face=Courier>'G'</font> &mdash; Show or hide the graph.";
		s += "</html>";
		JOptionPane.showMessageDialog(frame, new JLabel(s));
	}

	public final static void showAbout(Frame frame) {
		String s = "<html><h2>Quantum Workbench</h2>";
		s += "<h4><i>Interactive quantum mechanics simulation</i></h4>";
		s += "http://www.concord.org/~qxie/quantum<br>The Concord Consortium, 2011-2014";
		s += "<hr>";
		s += "<h4>Credit:</h4>This program is created by Dr. Charles Xie (charxie@gmail.com).<br>";
		s += "Funding of this project is provided by the National Science Foundation<br>";
		s += "under grant #0802532.";
		s += "<h4>License:</h4>GNU General Public License V3.0";
		s += "</html>";
		JOptionPane.showMessageDialog(frame, new JLabel(s));
	}

}
