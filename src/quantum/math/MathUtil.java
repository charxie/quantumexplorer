package quantum.math;

/**
 * @author Charles Xie
 * 
 */
public class MathUtil {

	/** @return true if x is between a and b. */
	public final static boolean between(float a, float b, float x) {
		return x < Math.max(a, b) && x > Math.min(a, b);
	}

	public static double getMax(double[] array) {
		double max = -Double.MAX_VALUE;
		for (double x : array) {
			if (x > max)
				max = x;
		}
		return max;
	}

	public static double getMin(double[] array) {
		double min = Double.MAX_VALUE;
		for (double x : array) {
			if (x < min)
				min = x;
		}
		return min;
	}

	public static double getMax(double[][] array) {
		double max = -Double.MAX_VALUE;
		for (double[] a : array) {
			for (double x : a) {
				if (x > max)
					max = x;
			}
		}
		return max;
	}

	public static double getMin(double[][] array) {
		double min = Double.MAX_VALUE;
		for (double[] a : array) {
			for (double x : a) {
				if (x < min)
					min = x;
			}
		}
		return min;
	}

}
