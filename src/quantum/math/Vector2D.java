package quantum.math;

/**
 * This is an implementation of 2D vector.
 * 
 * @author Charles Xie
 */

public class Vector2D {

	public float x, y;

	/**
	 * By default, construct a unit vector pointing in the abscissa direction.
	 */
	public Vector2D() {
		x = 1;
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float length() {
		return (float) Math.hypot(x, y);
	}

	public boolean equals(Object o) {
		if (!(o instanceof Vector2D))
			return false;
		Vector2D v = (Vector2D) o;
		return Float.floatToIntBits(x) == Float.floatToIntBits(v.x) && Float.floatToIntBits(y) == Float.floatToIntBits(v.y);
	}

	public int hashCode() {
		return new Float(x).hashCode() ^ new Float(y).hashCode();
	}

	public Vector2D unit() {
		float invlen = 1.0f / length();
		return new Vector2D(x * invlen, y * invlen);
	}

	public float dot(Vector2D v) {
		return x * v.x + y * v.y;
	}

	public float angle() {
		return (float) Math.atan2(y, x);
	}

	/**
	 * substract the passed vector <code>v</code> from the current vector, and return the result.
	 */
	public Vector2D substract(Vector2D v) {
		return new Vector2D(x - v.x, y - v.y);
	}

}
