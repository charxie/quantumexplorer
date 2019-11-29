package quantum.qm2d;

import java.awt.Color;

import quantum.qm2d.model.ImaginaryTimePropagator2D;
import quantum.qm2d.model.RealTimePropagator2D;
import quantum.qmshared.Boundary;

/**
 * @author Charles Xie
 * 
 */
class XmlEncoder {

	private QuantumBox box;

	XmlEncoder(QuantumBox box) {
		this.box = box;
	}

	String encode() {
		StringBuffer sb = new StringBuffer(1000);
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<model>\n");
		sb.append("<engine>\n");
		sb.append("<itp>" + (box.propagator instanceof ImaginaryTimePropagator2D) + "</itp>\n");
		sb.append("<nx>" + box.getNx() + "</nx>\n");
		sb.append("<ny>" + box.getNy() + "</ny>\n");
		sb.append("<timestep>" + box.propagator.getTimeStep() + "</timestep>\n");
		sb.append("</engine>\n");
		sb.append("<state>\n");
		sb.append("<xmin>" + box.xmin + "</xmin>\n");
		sb.append("<xmax>" + box.xmax + "</xmax>\n");
		sb.append("<ymin>" + box.ymin + "</ymin>\n");
		sb.append("<ymax>" + box.ymax + "</ymax>\n");
		Boundary b = box.propagator.getBoundary('x');
		if (b != null)
			sb.append(b.toXml());
		b = box.propagator.getBoundary('y');
		if (b != null)
			sb.append(b.toXml());
		sb.append("<frank>" + box.view2D.isFrank() + "</frank>\n");
		sb.append("<ruler>" + box.view2D.isRulerOn() + "</ruler>\n");
		sb.append("<grid>" + box.view2D.isGridOn() + "</grid>\n");
		sb.append("<contour>" + box.view2D.isContourShown() + "</contour>\n");
		sb.append("<probonly>" + box.view2D.isProbOnly() + "</probonly>\n");
		sb.append("<dotmode>" + box.view2D.getDotMode() + "</dotmode>\n");
		sb.append("<dotcellsize>" + box.view2D.getDotCellSize() + "</dotcellsize>\n");
		sb.append("<drawenergy>" + box.view2D.getDrawEnergy() + "</drawenergy>\n");
		sb.append("<drawcurrent>" + box.view2D.getDrawCurrent() + "</drawcurrent>\n");
		sb.append("<energyscale>" + box.view2D.getEnergyScale() + "</energyscale>\n");
		sb.append("<intensityscale>" + box.view2D.getIntensityScale() + "</intensityscale>\n");
		sb.append("<drawexpectation>" + box.view2D.getDrawExpectation() + "</drawexpectation>\n");
		if (!box.view2D.getBackground().equals(Color.black))
			sb.append("<bgcolor>" + Integer.toHexString(0x00ffffff & box.view2D.getBackground().getRGB()) + "</bgcolor>\n");
		sb.append("<probcolor>" + Integer.toHexString(0x00ffffff & box.view2D.getProbColor().getRGB()) + "</probcolor>\n");
		sb.append(box.propagator.getParticle().toXml());
		sb.append("\n");
		if (box.propagator.getElectricField() != null) {
			sb.append(box.propagator.getElectricField().toXml());
			sb.append("\n");
		}
		int textCount = box.view2D.getTextBoxCount();
		if (textCount > 0) {
			sb.append("<textboxes>\n");
			for (int i = 0; i < textCount; i++) {
				sb.append(box.view2D.getTextBox(i).toXml());
			}
			sb.append("</textboxes>\n");
		}
		int pCount = box.propagator.getPotentialCount();
		if (pCount > 0) {
			sb.append("<potentials>\n");
			for (int i = 0; i < pCount; i++) {
				sb.append(box.propagator.getPotential(i).toXml());
			}
			sb.append("</potentials>\n");
		}
		int waveCount = box.propagator.getWavePacketCount();
		if (waveCount > 0) {
			sb.append("<wavepackets>\n");
			for (int i = 0; i < waveCount; i++) {
				sb.append(box.propagator.getWavePacket(i).toXml());
			}
			sb.append("</wavepackets>\n");
		}
		if (box.propagator instanceof RealTimePropagator2D) {
			RealTimePropagator2D rtp = (RealTimePropagator2D) box.propagator;
			int sCount = rtp.getSourceCount();
			if (sCount > 0) {
				sb.append("<sources>\n");
				for (int i = 0; i < sCount; i++) {
					sb.append(rtp.getSource(i).toXml());
				}
				sb.append("</sources>\n");
			}
		}
		sb.append("</state>");
		sb.append("</model>");
		return sb.toString();
	}

}
