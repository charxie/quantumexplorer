package quantum.math;

/**
 * A complex number implementation.
 * 
 * @author Charles Xie
 * 
 */
public class FloatComplex {

	private final float u;
	private final float v;

	public FloatComplex() {
		this(0, 0);
	}

	public FloatComplex(float x, float y) {
		u = x;
		v = y;
	}

	public FloatComplex(FloatComplex c) {
		u = c.u;
		v = c.v;
	}

	/** @return the real part */
	public float real() {
		return u;
	}

	/** @return the imaginary part */
	public float imag() {
		return v;
	}

	/** @return the negative */
	public FloatComplex negative() {
		return new FloatComplex(-u, -v);
	}

	/** @return the conjugate */
	public FloatComplex conjugate() {
		return new FloatComplex(u, -v);
	}

	/** @return the absolute value (aka modulus or magnitude) */
	public float abs() {
		return (float) Math.hypot(u, v);
	}

	/** @return the square of the absolute value */
	public float absSquare() {
		return u * u + v * v;
	}

	/**
	 * Compute the argument by computing an arc tangent of v/u in the range of -pi to pi.
	 * 
	 * @return the argument (aka the angle).
	 */
	public float arg() {
		if (u == 0 && v == 0)
			return 0; // convention
		return (float) Math.atan2(v, u);
	}

	/** @return this+z */
	public FloatComplex add(FloatComplex z) {
		return new FloatComplex(u + z.u, v + z.v);
	}

	/** @return this-z */
	public FloatComplex subtract(FloatComplex z) {
		return new FloatComplex(u - z.u, v - z.v);
	}

	/** @return this*z */
	public FloatComplex multiply(FloatComplex z) {
		return new FloatComplex(u * z.u - v * z.v, u * z.v + v * z.u);
	}

	/** @return this/z */
	public FloatComplex divide(FloatComplex z) {
		float invZSq = 1.0f / z.absSquare();
		return new FloatComplex((u * z.u + v * z.v) * invZSq, (v * z.u - u * z.v) * invZSq);
	}

	/** @return the multiplicative inverse */
	public FloatComplex inverse() {
		float invSq = 1.0f / absSquare();
		return new FloatComplex(u * invSq, -v * invSq);
	}

	/**
	 * The two square roots of a+bi are (x+yi) and -(x+yi) with y = sqrt((r - a)/2) and x = b/(2.y).
	 * This method returns the first one.
	 * 
	 * @return a square root of this complex number.
	 */
	public FloatComplex sqrt1() {
		float r = (float) Math.hypot(u, v);
		float y = (float) Math.sqrt((r - u) * 0.5);
		return new FloatComplex(v * 0.5f / y, y);
	}

	/**
	 * The two square roots of a+bi are (x+yi) and -(x+yi) with y = sqrt((r - a)/2) and x = b/(2.y).
	 * This method returns the second one.
	 ** 
	 * @return a square root of this complex number.
	 */
	public FloatComplex sqrt2() {
		float r = (float) Math.hypot(u, v);
		float y = -(float) Math.sqrt((r - u) * 0.5);
		return new FloatComplex(v * 0.5f / y, y);
	}

	public boolean equals(Object o) {
		if (o instanceof FloatComplex) {
			FloatComplex c = (FloatComplex) o;
			return c.u == u && c.v == v;
		}
		return super.equals(o);
	}

	public int hashCode() {
		int bits = Float.floatToIntBits(u);
		bits ^= Float.floatToIntBits(v) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	public String toString() {
		return v > 0 ? u + " + i" + v : u + " - i" + (-v);
	}

}
