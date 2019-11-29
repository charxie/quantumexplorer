package quantum.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public class ElectricField2D {

	private float intensity = .1f;

	private float frequency;

	private float phase;

	private float angle;

	public ElectricField2D() {
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

	/**
	 * Electric potential = - electric field intensity * distance
	 * 
	 * @return the potential of the specified coordinate (x, y), relative to (0, 0), for a positive unit charge.
	 */
	public float getPotential(float x, float y, float time) {
		float e = getValue(time);
		if (angle == 0)
			return -e * x;
		if (angle == 90)
			return -e * y;
		if (angle == 180)
			return e * x;
		if (angle == 270)
			return e * y;
		double a = Math.toRadians(angle);
		return -(float) (e * (x * Math.cos(a) + y * Math.sin(a)));
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getAngle() {
		return angle;
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
		String s = "<efield";
		s += " intensity=\"" + intensity + "\"";
		s += " frequency=\"" + frequency + "\"";
		if (phase != 0)
			s += " phase=\"" + phase + "\"";
		if (angle != 0)
			s += " angle=\"" + angle + "\"";
		return s + "/>";
	}

}
