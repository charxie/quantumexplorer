package quantum.qm2d.model;

/**
 * Symmetric gauge: Ax=-yB/2, Ay=xB/2.
 * 
 * @author Charles Xie
 * 
 */
public class MagneticField2D {

	private float intensity = 0.1f; // B

	private float frequency;

	private float phase;

	public MagneticField2D() {
	}

	public float getValue(float time) {
		if (intensity == 0)
			return 0;
		float e = intensity;
		if (frequency != 0) {
			e *= Math.sin(frequency * time + phase);
		}
		return e;
	}

	public void setPhase(float phase) {
		this.phase = phase;
	}

	public float getPhase() {
		return phase;
	}

	public void setIntensity(float intensity) {
		this.intensity = intensity;
	}

	public float getIntensity() {
		return intensity;
	}

	public void setFrequency(float frequency) {
		this.frequency = frequency;
	}

	public float getFrequency() {
		return frequency;
	}

	public String toXml() {
		String s = "<bfield";
		s += " intensity=\"" + intensity + "\"";
		s += " frequency=\"" + frequency + "\"";
		if (phase != 0)
			s += " phase=\"" + phase + "\"";
		return s + "/>";
	}

}
