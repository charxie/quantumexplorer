package quantum.qm2d.model;

import quantum.math.FloatComplex;

/**
 * @author Charles Xie
 * 
 */
public class Gaussian2D extends WavePacket2D {

	private float sigma = 2;
	private float muX;
	private float muY;

	public Gaussian2D(float magnitude, float sigma, float muX, float muY, float px, float py, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(magnitude, nx, ny);
		this.sigma = sigma;
		this.muX = muX;
		this.muY = muY;
		this.px = px;
		this.py = py;
		float deltaX = (xmax - xmin) / nx;
		float deltaY = (ymax - ymin) / ny;
		float g;
		float kx, ky;
		float k;
		for (int i = 0; i < nx; i++) {
			kx = (xmin + i * deltaX - muX) / sigma;
			kx *= kx;
			for (int j = 0; j < ny; j++) {
				ky = (ymin + j * deltaY - muY) / sigma;
				ky *= ky;
				g = (float) (magnitude * Math.exp(-(kx + ky)));
				if (px == 0 && py == 0) {
					psi[i][j] = new FloatComplex(g, 0);
				} else {
					k = px * (xmin + i * deltaX) + py * (ymin + j * deltaY);
					psi[i][j] = new FloatComplex((float) (g * Math.cos(k)), (float) (g * Math.sin(k)));
				}
			}
		}
	}

	public void setSigma(float sigma) {
		this.sigma = sigma;
	}

	public float getSigma() {
		return sigma;
	}

	public void setMuX(float muX) {
		this.muX = muX;
	}

	public float getMuX() {
		return muX;
	}

	public void setMuY(float muY) {
		this.muY = muY;
	}

	public float getMuY() {
		return muY;
	}

	@Override
	public String toXml() {
		String xml = "<gaussian magnitude=\"" + magnitude;
		xml += "\" sigma=\"" + sigma;
		xml += "\" xcenter=\"" + muX;
		xml += "\" ycenter=\"" + muY;
		xml += "\" px=\"" + px;
		xml += "\" py=\"" + py;
		xml += "\"/>\n";
		return xml;
	}

	@Override
	public String toString() {
		return "Gaussian [" + magnitude + ", " + sigma + ", " + muX + ", " + muY + ", " + px + ", " + py + "]";
	}

}
