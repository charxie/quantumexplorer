package quantum.qmshared;

/**
 * @author Charles Xie
 * 
 */
public class Particle {

	/*
	 * the mass of the particle in the unit of 1/1000 a. u.. By default, it is the mass of the electron.
	 */
	private float mass = 0.910938188f / 1.66f;

	/* the charge of the particle, By default, it is the charge of the electron. */
	private float charge = -1;

	public void setCharge(float charge) {
		this.charge = charge;
	}

	public float getCharge() {
		return charge;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public float getMass() {
		return mass;
	}

	public String toXml() {
		String s = "<particle";
		s += " mass=\"" + mass + "\"";
		if (charge != -1)
			s += " charge=\"" + charge + "\"";
		return s + "/>";
	}

}
