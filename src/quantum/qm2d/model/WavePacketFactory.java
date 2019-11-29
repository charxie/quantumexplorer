package quantum.qm2d.model;

/**
 * @author Charles Xie
 * 
 */
public class WavePacketFactory {

	private int nx, ny;
	private float xmin, xmax, ymin, ymax;

	public WavePacketFactory(int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		set(nx, ny, xmin, xmax, ymin, ymax);
	}

	public void set(int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		this.nx = nx;
		this.ny = ny;
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public UniformRectangle2D createPlaneWave(float magnitude, float x, float y, float w, float h, float px, float py) {
		return new UniformRectangle2D(magnitude, x, y, w, h, px, py, nx, ny, xmin, xmax, ymin, ymax);
	}

	public Gaussian2D createGaussian(float magnitude, float sigma, float muX, float muY, float px, float py) {
		return new Gaussian2D(magnitude, sigma, muX, muY, px, py, nx, ny, xmin, xmax, ymin, ymax);
	}

}
