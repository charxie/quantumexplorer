package quantum.qmshared;

/**
 * @author Charles Xie
 * 
 */
public final class Constants {

	private Constants() {
	}

	/**
	 * this converter is used because we set the Planck constant/2pi to be 1 in our differential equation.
	 */
	public final static float MASS_UNIT_CONVERTER = 16.6f / 1.0545726f;

	/**
	 * this converter is used because we set the Planck constant/2pi to be 1 in our differential equation.
	 */
	public final static float ENERGY_UNIT_CONVERTER = 1.6f / 1.0545726f;

}
