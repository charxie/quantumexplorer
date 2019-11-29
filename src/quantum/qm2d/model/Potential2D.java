package quantum.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public abstract class Potential2D extends Shape2D {

	protected float[][] pot;
	protected boolean imaginary;

	public Potential2D(boolean imaginary, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(xmin, xmax, ymin, ymax);
		pot = new float[nx][ny];
		this.imaginary = imaginary;
	}

	public float[][] getPotential() {
		return pot;
	}

	public void setImaginary(boolean imaginary) {
		this.imaginary = imaginary;
	}

	public boolean isImaginary() {
		return imaginary;
	}

	void destroy() {
		pot = null;
	}

}
