package quantum.qm2d.model;

import quantum.math.FloatComplex;

/**
 * @author Charles Xie
 * 
 */
class UniformRectangle2D extends WavePacket2D {

	private float x, y, w, h;

	public UniformRectangle2D(float magnitude, float x, float y, float w, float h, float px, float py, int nx, int ny, float xmin, float xmax, float ymin, float ymax) {
		super(magnitude, nx, ny);
		this.magnitude = magnitude;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.px = px;
		this.py = py;
		float deltaX = (xmax - xmin) / nx;
		float deltaY = (ymax - ymin) / ny;
		float kx, ky;
		float k;
		boolean withinX;
		FloatComplex zero = new FloatComplex();
		FloatComplex magn = new FloatComplex(magnitude, 0);
		for (int i = 0; i < nx; i++) {
			kx = xmin + i * deltaX;
			withinX = kx > x && kx < x + w;
			for (int j = 0; j < ny; j++) {
				ky = ymin + j * deltaY;
				if (withinX && ky > y && ky < y + h) {
					if (px == 0 && py == 0) {
						psi[i][j] = magn;
					} else {
						k = px * kx + py * ky;
						psi[i][j] = new FloatComplex((float) (magnitude * Math.cos(k)), (float) (magnitude * Math.sin(k)));
					}
				} else {
					psi[i][j] = zero;
				}
			}
		}
	}

	void setX(float x) {
		this.x = x;
	}

	float getX() {
		return x;
	}

	void setY(float y) {
		this.y = y;
	}

	float getY() {
		return y;
	}

	void setW(float w) {
		this.w = w;
	}

	float getW() {
		return w;
	}

	void setH(float h) {
		this.h = h;
	}

	float getH() {
		return h;
	}

	@Override
	public String toXml() {
		String xml = "<uniform_rectangle magnitude=\"" + magnitude;
		xml += "\" x=\"" + x;
		xml += "\" y=\"" + y;
		xml += "\" width=\"" + w;
		xml += "\" height=\"" + h;
		xml += "\" px=\"" + px;
		xml += "\" py=\"" + py;
		xml += "\"/>\n";
		return xml;
	}

	@Override
	public String toString() {
		return "Uniform rectangle [" + magnitude + ", " + x + ", " + y + ", " + w + ", " + h + ", " + px + ", " + py + "]";
	}
}
