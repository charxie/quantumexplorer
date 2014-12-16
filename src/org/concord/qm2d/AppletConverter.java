package org.concord.qm2d;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.concord.qmutil.MiscUtil;

/**
 * @author Charles Xie
 * 
 */
class AppletConverter {

	private final static String LINE_SEPARATOR = System.getProperty("line.separator");

	private QuantumBox box;

	AppletConverter(QuantumBox box) {
		this.box = box;
	}

	void write(final File file) {

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (out == null)
			return;

		String s = "<html>";
		s += LINE_SEPARATOR;

		s += "  <head>";
		s += LINE_SEPARATOR;
		s += "    <title>" + QuantumBox.BRAND_NAME + "</title>";
		s += LINE_SEPARATOR;
		s += "  </head>";
		s += LINE_SEPARATOR;

		s += "  <body>";
		s += LINE_SEPARATOR;

		s += "    <p><font color=\"red\">If nothing shows up below, try the following: ";
		s += "1) Download <a href=\"http://www.concord.org/~qxie/qw/qw.jar\">qw.jar</a> ";
		s += "to where this HTML file is located; ";
		s += "2); Make sure " + box.getCurrentFile() + " is copied or moved to where this HTML file is located; ";
		s += "3) <b>Restart the browser</b> and reload this page. ";
		s += "This line of message should be removed if the applet works.</font></p>";
		s += LINE_SEPARATOR;

		s += "    <center>";
		s += LINE_SEPARATOR;

		s += "      <applet code=\"org.concord.qm2d.QuantumBox\" archive=\"qw.jar\" width=\"500\" height=\"500\">";
		s += LINE_SEPARATOR;
		s += "        <param name=\"script\" value=\"load " + MiscUtil.getFileName(box.getCurrentFile().toString()) + "\"/>";
		s += LINE_SEPARATOR;
		s += "      </applet>";
		s += LINE_SEPARATOR;

		s += "      <br><br>";
		s += LINE_SEPARATOR;

		s += "      <p><b>System requirements:</b> You must have Java Version 5 or higher. <a href=\"http://java.com\">Download Java now</a>.";
		s += LINE_SEPARATOR;

		s += "    </center>";
		s += LINE_SEPARATOR;

		s += "  </body>";
		s += LINE_SEPARATOR;

		s += "</html>";

		try {
			out.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}

	}

}
