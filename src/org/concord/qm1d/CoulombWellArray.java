package org.concord.qm1d;

/**
 * @author Charles Xie
 * 
 */
class CoulombWellArray extends Potential1D {

	final static byte DEFAULT = -1;
	final static byte VACANCY = 0;
	final static byte INTERSTITIAL = 1;
	final static byte BINARY_LATTICE = 2;

	private double k = 0.1;
	private double offset = -1;

	CoulombWellArray(int n, int ionCount, double latticeConstant, double field, byte type, double xmin, double xmax) {
		super(n, xmin, xmax);
		double delta = (xmax - xmin) / n;
		double center = (xmax + xmin) / 2;
		double[] location = new double[ionCount];
		for (int i = 0; i < ionCount; i++) {
			if (ionCount % 2 == 0) {
				location[i] = center + (i - ionCount / 2 + 0.5) * latticeConstant;
			} else {
				location[i] = center + (i - ionCount / 2) * latticeConstant;
			}
		}
		double x;
		int defectIndex = ionCount < 3 ? 0 : 1 + (int) (Math.random() * (ionCount - 2));
		for (int i = 0; i < n; i++) {
			x = xmin + i * delta;
			switch (type) {
			case DEFAULT:
				for (int j = 0; j < location.length; j++) {
					if (x != location[j]) {
						pot[i] += field * x - k / Math.abs(x - location[j]);
					} else {
						pot[i] -= 1000;
					}
				}
				break;
			case BINARY_LATTICE:
				for (int j = 0; j < location.length; j++) {
					if (x != location[j]) {
						pot[i] += field * x - (0.5 + (j % 2) * 2) * k / Math.abs(x - location[j]);
					} else {
						pot[i] -= 1000;
					}
				}
				break;
			case VACANCY:
				for (int j = 0; j < location.length; j++) {
					if (j == defectIndex)
						continue;
					if (x != location[j]) {
						pot[i] += field * x - k / Math.abs(x - location[j]);
					} else {
						pot[i] -= 1000;
					}
				}
				break;
			case INTERSTITIAL:
				for (int j = 0; j < location.length; j++) {
					if (x != location[j]) {
						pot[i] += field * x - k / Math.abs(x - location[j]);
					} else {
						pot[i] -= 1000;
					}
				}
				if (x != location[defectIndex] + latticeConstant * 0.5) {
					pot[i] += field * x - k / Math.abs(x - (location[defectIndex] + latticeConstant * 0.5));
				} else {
					pot[i] -= 1000;
				}
				break;
			}
			if (pot[i] < offset)
				pot[i] = offset;
		}
	}

	@Override
	public String getName() {
		return "Coulomb Well Array";
	}

}
