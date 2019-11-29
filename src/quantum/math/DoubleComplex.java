package quantum.math;

/**
 * A complex number implementation.
 * 
 * @author Charles Xie
 * 
 */
public class DoubleComplex {

	private final double u;
	private final double v;

	public DoubleComplex() {
		this(0, 0);
	}

	public DoubleComplex(double x, double y) {
		u = x;
		v = y;
	}

	public DoubleComplex(DoubleComplex c) {
		u = c.u;
		v = c.v;
	}

	/** @return the real part */
	public double real() {
		return u;
	}

	/** @return the imaginary part */
	public double imag() {
		return v;
	}

	/** @return the negative */
	public DoubleComplex negative() {
		return new DoubleComplex(-u, -v);
	}

	/** @return the conjugate */
	public DoubleComplex conjugate() {
		return new DoubleComplex(u, -v);
	}

	/** @return the absolute value (aka modulus or magnitude) */
	public double abs() {
		return Math.hypot(u, v);
	}

	/** @return the square of the absolute value */
	public double absSquare() {
		return u * u + v * v;
	}

	/**
	 * Compute the argument by computing an arc tangent of v/u in the range of -pi to pi.
	 * 
	 * @return the argument (aka the angle).
	 */
	public double arg() {
		if (u == 0 && v == 0)
			return 0; // convention
		return Math.atan2(v, u);
	}

	/** @return this+z */
	public DoubleComplex add(DoubleComplex z) {
		return new DoubleComplex(u + z.u, v + z.v);
	}

	/** @return this-z */
	public DoubleComplex subtract(DoubleComplex z) {
		return new DoubleComplex(u - z.u, v - z.v);
	}

	/** @return this*z */
	public DoubleComplex multiply(DoubleComplex z) {
		return new DoubleComplex(u * z.u - v * z.v, u * z.v + v * z.u);
	}

	/** @return this/z */
	public DoubleComplex divide(DoubleComplex z) {
		double invZSq = 1.0 / z.absSquare();
		return new DoubleComplex((u * z.u + v * z.v) * invZSq, (v * z.u - u * z.v) * invZSq);
	}

	/** @return the multiplicative inverse */
	public DoubleComplex inverse() {
		double invSq = 1.0 / absSquare();
		return new DoubleComplex(u * invSq, -v * invSq);
	}

	/**
	 * The two square roots of a+bi are (x+yi) and -(x+yi) with y = sqrt((r - a)/2) and x = b/(2.y).
	 * This method returns the first one.
	 * 
	 * @return a square root of this complex number.
	 */
	public DoubleComplex sqrt1() {
		double r = Math.hypot(u, v);
		double y = Math.sqrt((r - u) * 0.5);
		return new DoubleComplex(v * 0.5 / y, y);
	}

	/**
	 * The two square roots of a+bi are (x+yi) and -(x+yi) with y = sqrt((r - a)/2) and x = b/(2.y).
	 * This method returns the second one.
	 ** 
	 * @return a square root of this complex number.
	 */
	public DoubleComplex sqrt2() {
		double r = Math.hypot(u, v);
		double y = -Math.sqrt((r - u) * 0.5);
		return new DoubleComplex(v * 0.5 / y, y);
	}

	public boolean equals(Object o) {
		if (o instanceof DoubleComplex) {
			DoubleComplex c = (DoubleComplex) o;
			return c.u == u && c.v == v;
		}
		return super.equals(o);
	}

	public int hashCode() {
		long bits = Double.doubleToLongBits(u);
		bits ^= Double.doubleToLongBits(v) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	public String toString() {
		return v > 0 ? u + " + i" + v : u + " - i" + (-v);
	}

}
