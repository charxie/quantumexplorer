package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class ElectricField1D {

	private double intensity = 0.0001;

	private double frequency = 0.05;

	private double phase;

	public ElectricField1D() {
	}

	public double getValue(double time) {
		return intensity * Math.sin(frequency * time + phase);
	}

	public void setPhase(double phase) {
		this.phase = phase;
	}

	public double getPhase() {
		return phase;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public double getFrequency() {
		return frequency;
	}

}
