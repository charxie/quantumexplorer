package quantum.qm2d;

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
		String s = "<html><h2>Quantum Leap</h2>";
		s += "<h4><i>An interactive quantum simulation and design environment</i></h4>";
		s += "<hr>";
		s += "<h4>Credit:</h4>This program is being developed by Dr. Charles Xie based on prior work funded<br>by the U.S. National Science Foundation.<br>";
		s += "</html>";
		JOptionPane.showMessageDialog(frame, new JLabel(s));
	}

}
