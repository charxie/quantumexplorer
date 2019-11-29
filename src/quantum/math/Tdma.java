package quantum.math;

/**
 * The tridiagonal matrix algorithm (TDMA), also known as the Thomas algorithm, is a simplified form of Gaussian elimination that can be used to solve tridiagonal systems of equations. A tridiagonal system for n unknowns may be written as
 * 
 * a[i]*x[i-1]+b[i]*x[i]+c[i]*x[i+1]=d[i], where a[0]=0 and c[n-1]=0.
 * 
 * @author Charles Xie
 * 
 */
public class Tdma {

	private Tdma() {
	}

	/**
	 * Real number version. c and d are modified during the operation. All input arrays must be of the same size.
	 * 
	 * @param a
	 *            the subdiagonal elements
	 * @param b
	 *            the diagonal elements
	 * @param c
	 *            the superdiagonal elements
	 * @param d
	 *            the right-hand-side vector
	 */
	public static double[] solve(double[] a, double[] b, double[] c, double[] d) {
		int n = d.length;
		double temp;
		c[0] /= b[0];
		d[0] /= b[0];
		for (int i = 1; i < n; i++) {
			temp = 1.0 / (b[i] - c[i - 1] * a[i]);
			c[i] *= temp; // redundant at the last step as c[n-1]=0.
			d[i] = (d[i] - d[i - 1] * a[i]) * temp;
		}
		double[] x = new double[n];
		x[n - 1] = d[n - 1];
		for (int i = n - 2; i >= 0; i--) {
			x[i] = d[i] - c[i] * x[i + 1];
		}
		return x;
	}

	/**
	 * Complex number version. c and d are modified during the operation. All input arrays must be of the same size.
	 * 
	 * @param a
	 *            the subdiagonal complex elements
	 * @param b
	 *            the diagonal complex elements
	 * @param c
	 *            the superdiagonal complex elements
	 * @param d
	 *            the right-hand-side complex vector.
	 * @return the solution
	 */
	public static FloatComplex[] solve(FloatComplex[] a, FloatComplex[] b, FloatComplex[] c, FloatComplex[] d) {
		int n = d.length;
		FloatComplex temp;
		c[0] = c[0].divide(b[0]);
		d[0] = d[0].divide(b[0]);
		for (int i = 1; i < n; i++) {
			temp = b[i].subtract(c[i - 1].multiply(a[i])).inverse();
			c[i] = c[i].multiply(temp); // redundant at the last step as c[n-1]=0.
			d[i] = d[i].subtract(d[i - 1].multiply(a[i])).multiply(temp);
		}
		FloatComplex[] x = new FloatComplex[n];
		x[n - 1] = d[n - 1];
		for (int i = n - 2; i >= 0; i--) {
			x[i] = d[i].subtract(c[i].multiply(x[i + 1]));
		}
		return x;
	}

	/**
	 * Complex number version. c and d are modified during the operation. All input arrays must be of the same size.
	 * 
	 * @param a
	 *            the subdiagonal complex elements
	 * @param b
	 *            the diagonal complex elements
	 * @param c
	 *            the superdiagonal complex elements
	 * @param d
	 *            the right-hand-side complex vector.
	 * @return the solution
	 */
	public static DoubleComplex[] solve(DoubleComplex[] a, DoubleComplex[] b, DoubleComplex[] c, DoubleComplex[] d) {
		int n = d.length;
		DoubleComplex temp;
		c[0] = c[0].divide(b[0]);
		d[0] = d[0].divide(b[0]);
		for (int i = 1; i < n; i++) {
			temp = b[i].subtract(c[i - 1].multiply(a[i])).inverse();
			c[i] = c[i].multiply(temp); // redundant at the last step as c[n-1]=0.
			d[i] = d[i].subtract(d[i - 1].multiply(a[i])).multiply(temp);
		}
		DoubleComplex[] x = new DoubleComplex[n];
		x[n - 1] = d[n - 1];
		for (int i = n - 2; i >= 0; i--) {
			x[i] = d[i].subtract(c[i].multiply(x[i + 1]));
		}
		return x;
	}

}
