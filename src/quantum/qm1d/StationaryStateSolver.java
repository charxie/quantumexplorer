package quantum.qm1d;

import static quantum.qmshared.Constants.ENERGY_UNIT_CONVERTER;
import static quantum.qmshared.Constants.MASS_UNIT_CONVERTER;

import java.util.Arrays;

import quantum.qmshared.Particle;
import org.netlib.lapack.Dstedc;
import org.netlib.util.intW;

/**
 * Solving the time-independent one-dimensional Schroedinger equation using the finite difference method and LAPACK.
 * 
 * @author Charles Xie
 * 
 */
class StationaryStateSolver {

	/* The dimension of the symmetric tridiagonal matrix. */
	private int n;

	/*
	 * (input/output) DOUBLE PRECISION array, dimension (N). On entry, the diagonal elements of the tridiagonal matrix. On exit, if INFO = 0, the eigenvalues in ascending order.
	 */
	private double[] d;

	/*
	 * (input/output) DOUBLE PRECISION array, dimension (N-1). On entry, the subdiagonal elements of the tridiagonal matrix. On exit, E has been destroyed.F
	 */
	private double[] e;

	/*
	 * (input/output) DOUBLE PRECISION array, dimension (LDZ,N). On entry, if COMPZ = 'V', then Z contains the orthogonal matrix used in the reduction to tridiagonal form. On exit, if INFO = 0, then if COMPZ = 'V', Z contains the orthonormal eigenvectors of the original symmetric matrix, and if COMPZ = 'I', Z contains the orthonormal eigenvectors of the symmetric tridiagonal matrix. If COMPZ = 'N', then Z is not referenced.
	 */
	private double[] z;

	/*
	 * (workspace/output) DOUBLE PRECISION array, dimension (LWORK) On exit, if INFO = 0, WORK(1) returns the optimal LWORK. If COMPZ = 'N' or N <= 1 then LWORK must be at least 1. If COMPZ = 'V' and N > 1 then LWORK must be at least ( 1 + 3N + 2Nlg N + 3N^2 ), where lg( N ) = smallest integer k such that 2k >= N. If COMPZ = 'I' and N > 1 then LWORK must be at least ( 1 + 4N + N^2 ). Note that for COMPZ = 'I' or 'V', then if N is less than or equal to the minimum divide size, usually 25, then LWORK need only be max(1,2(N-1)).
	 * 
	 * If LWORK = -1, then a workspace query is assumed; the routine only calculates the optimal size of the WORK array, returns this value as the first entry of the WORK array, and no error message related to LWORK is issued by XERBLA.
	 */
	private double[] work;

	/*
	 * (workspace/output) INTEGER array, dimension (MAX(1,LIWORK)) On exit, if INFO = 0, IWORK(1) returns the optimal LIWORK. If COMPZ = 'N' or N <= 1 then LIWORK must be at least 1. If COMPZ = 'V' and N > 1 then LIWORK must be at least ( 6 + 6N + 5Nlg N ). If COMPZ = 'I' and N > 1 then LIWORK must be at least ( 3 + 5N ). Note that for COMPZ = 'I' or 'V', then if N is less than or equal to the minimum divide size, usually 25, then LIWORK need only be 1.
	 * 
	 * If LIWORK = -1, then a workspace query is assumed; the routine only calculates the optimal size of the IWORK array,
	 * 
	 * returns this value as the first entry of the IWORK array, and no error message related to LIWORK is issued by XERBLA.
	 */
	private int[] iwork;

	/*
	 * (output) INTEGER = 0: successful exit. < 0: if INFO = -i, the i-th argument had an illegal value. > 0: The algorithm failed to compute an eigenvalue while working on the submatrix lying in rows and columns INFO/(N+1) through mod(INFO,N+1).
	 */
	private intW info;

	/* the potential energy as a function of coordinate */
	private double[] v;

	/* the eigen energies */
	private double[] eigenEnergy;

	/* highest number of eigen energies we will show */
	private int maxState = 10;

	/* eigen vectors we will show */
	private double[][] eigenVector;

	private Particle particle;

	StationaryStateSolver() {
		this(100);
	}

	StationaryStateSolver(int n) {
		setN(n);
	}

	void setParticle(Particle particle) {
		this.particle = particle;
	}

	void setMaxState(int maxState) {
		if (maxState < 0 || maxState >= n)
			throw new IllegalArgumentException("cannot return " + maxState + " eigen states.");
		this.maxState = maxState;
	}

	int getN() {
		return n;
	}

	void setN(int n) {
		this.n = n;
		v = new double[n];
		d = new double[n];
		e = new double[n - 1];
		z = new double[n * n];
		work = new double[1 + 4 * n + n * n];
		iwork = new int[3 + 5 * n];
		info = new intW(0);
		eigenEnergy = new double[n];
	}

	void setPotential(double[] v) {
		this.v = v;
	}

	double[][] getEigenVectors() {
		return eigenVector;
	}

	double[] getEigenEnergies() {
		double[] ee = new double[maxState];
		System.arraycopy(eigenEnergy, 0, ee, 0, maxState);
		return ee;
	}

	/*
	 * COMPZ (input) CHARACTER1 = 'N': Compute eigenvalues only. = 'I': Compute eigenvectors of tridiagonal matrix also.
	 */
	void solve(String compZ, double length) {

		double delta = n / length;
		double a = 0.5 * delta * delta / (particle.getMass() * MASS_UNIT_CONVERTER);

		// The Schroedinger equation is approximated as a tridiagonal matrix equation using the
		// finite difference method. The second-order derivative is approximated using the
		// central difference approximation: f''=[f(x+a)-2f(x)+f(x-a)]/a^2
		for (int i = 0; i < n; i++) {
			if (i < n - 1)
				e[i] = -a;
			d[i] = 2 * a + v[i] * ENERGY_UNIT_CONVERTER;
			int in = i * n;
			for (int j = 0; j < n; j++)
				z[in + j] = 0;
			z[in + i] = 1;
		}

		// LAPACK to calculate eigenvalues and eigenvectors
		Dstedc.dstedc(compZ, n, d, 0, e, 0, z, 0, n, work, 0, work.length, iwork, 0, iwork.length, info);
		for (int i = 0; i < n; i++)
			eigenEnergy[i] = d[i];
		Arrays.sort(eigenEnergy);

		// System.out.println(">>> 1st = "+(eigenEnergy[1]-eigenEnergy[0]));
		// System.out.println(">>> 2nd = "+(eigenEnergy[2]-eigenEnergy[1]));

		if ("I".equalsIgnoreCase(compZ)) {
			eigenVector = new double[maxState][n];
			for (int i = 0; i < maxState; i++) {
				int j = 0;
				for (; j < n; j++)
					if (eigenEnergy[i] == d[j])
						break;
				// the eigen vectors are already normalized
				for (int k = 0; k < n; k++) {
					eigenVector[i][k] = z[j * n + k];
				}
			}
		}

	}

}
