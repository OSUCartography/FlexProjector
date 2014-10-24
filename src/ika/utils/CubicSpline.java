package ika.utils;

import Jama.Matrix;

/**
 * A piecewise regular cubic spline.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class CubicSpline implements Cloneable {

    /**
     * Knot vector: The values for which to compute an interpolating spline curve.
     * The values are assumed to be equally distant.
     */
    private double[] t;
    /**
     * Spline coefficients for each spline segment.
     */
    private double[][] abcd;
    /**
     * Slope of the interpolated curve at the start in radians.
     * If startSlope is NaN, the second derivative at the start of the curve is
     * set to 0.
     */
    private double startSlope = Double.NaN;
    /**
     * Slope of the interpolated curve at the end in radians.
     * If endSlope is NaN, the second derivative at the end of the curve is set
     * to 0.
     */
    private double endSlope = Double.NaN;

    /**
     * Create a new instance of CubicSpline.
     * @param t The knot vector: The values for which to compute an
     * interpolating cubic spline curve. The number of knots cannot be changed
     * later on.
     */
    public CubicSpline(double[] t) {
        if (t == null || t.length < 3) {
            throw new IllegalArgumentException();
        }
        this.t = t.clone();
        this.computeCubicSpline();
    }

    /**
     * Create a new instance of CubicSpline.
     * @param knotsCount The number of knots, i.e. the number of values for
     * which to compute an interpolating cubic spline curve. The number of knots
     * cannot be changed later on.
     */
    public CubicSpline(int knotsCount) {
        if (knotsCount < 3) {
            throw new IllegalArgumentException();
        }
        this.t = new double[knotsCount];
        this.computeCubicSpline();
    }

    /**
     * Creates a deep copy of a CubicSpline.
     * @return
     */
    @Override
    public CubicSpline clone() {
        try {
            CubicSpline copy = (CubicSpline) super.clone();
            copy.t = (double[]) this.t.clone();
            copy.abcd = ika.utils.ArrayUtils.clone2DArray(this.abcd);
            return copy;
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }

    /**
     * Returns the number of knots, i.e. the number of values that are
     * interpolated by this spline curve.
     * @return The number of knots.
     */
    public int getKnotsCount() {
        return this.t.length;
    }

    /**
     * Set the knot value at position i. An exception is thrown if i is smaller
     * than 0 or equal or larger than getKnotsCount().
     * @param i The position of the knot between 0 and getKnotsCount() - 1.
     * @param v The new value of the knot.
     */
    public void setKnot(int i, double v) {
        if (this.t[i] != v) {
            this.t[i] = v;
            this.computeCubicSpline();
        }
    }

    /**
     * Returns the knot value at position i. An exception is thrown if i is smaller
     * than 0 or equal or larger than getKnotsCount().
     * @param i The position of the knot between 0 and getKnotsCount() - 1.
     * @return The knot value.
     */
    public double getKnot(int i) {
        return this.t[i];
    }

    /**
     * Returns the maximum knot value.
     */
    public double getKnotMaximum() {
        double tMax = t[0];
        for (int i = 1; i < t.length; i++) {
            if (t[i] > tMax) {
                tMax = t[i];
            }
        }
        return tMax;
    }

    /**
     * Returns a copy of all knot values.
     * @return
     */
    public double[] getKnotsClone() {
        return this.t.clone();
    }

    /**
     * Returns true if all knot values are equal to ref.
     * @param ref The reference value to compare the knots to.
     * @return True if all knot values are equal to ref, false otherwise.
     */
    public boolean allKnotsEqual(double ref) {
        for (int i = 0; i < this.t.length; i++) {
            if (this.t[i] != ref) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the coefficients a, b, c, d for the spline segment i.
     * @param i The spline segment for which the coefficients are returned.
     * @return The coefficients for a, b, c, d for Yi(t)=ai+bi*t+ci*t*t+di*t*t*t
     */
    public double[] getCoefficientsClone(int i) {
        return this.abcd[i].clone();
    }
    
    /**
     * Returns the slope at the start of the first spline segment.
     * @return The slope at the first point in radians.
     */
    public double getStartSlope() {
        return startSlope;
    }

    /**
     * Sets the slope at the start of the first spline segment.
     * @param The slope at the first point in radians.
     */
    public void setStartSlope(double startSlope) {
        if (this.startSlope != startSlope) {
            this.startSlope = startSlope;
            this.computeCubicSpline();
        }
    }

    /**
     * Returns the slope at the end of the last spline segment.
     * @return The slope at the last point in radians
     */
    public double getEndSlope() {
        return endSlope;
    }

    /**
     * Sets the slope at the end of teh last spline segment.
     * @param endSlope The slope at the last point in radians.
     */
    public void setEndSlope(double endSlope) {
        if (this.endSlope != endSlope) {
            this.endSlope = endSlope;
            this.computeCubicSpline();
        }
    }

    /**
     * After setToNaturalSpline is called, this spline is a natural cubic
     * spline. The start and end slopes are set to NaN.
     */
    public void setToNaturalSpline() {
        if (this.startSlope != Double.NaN || this.endSlope != Double.NaN) {
            this.startSlope = Double.NaN;
            this.endSlope = Double.NaN;
            this.computeCubicSpline();
        }
    }

    /**
     * Compute the spline coefficients for a cubic spline. Call this after every
     * change to the knots vector t or changes to the start or end slopes.
     * The spline coefficients are computed with a system of linear
     * equations. See http://mathworld.wolfram.com/CubicSpline.html
     * The coefficients are a, b, c, d for Yi(t)=ai+bi*t+ci*t*t+di*t*t*t
     * and stored in this.abcd
     */
    private void computeCubicSpline() {

        // solve the system A * D = B
        // see http://mathworld.wolfram.com/CubicSpline.html

        final int n = t.length;
        final int n_1 = n - 1;

        // setup matrix A
        double[][] A = new double[n][n];
        if (Double.isNaN(startSlope)) {
            // the second derivative at the start is 0 if no start slope is provided
            A[0][0] = 2;
            A[0][1] = 1;
        } else {
            // use equation 7 of http://mathworld.wolfram.com/CubicSpline.html to fix
            // the start and end slopes
            // Y0'(0) = 0 -> 1 * D0 = b0 = 0
            A[0][0] = 1;
        }
        for (int i = 1; i < n_1; i++) {
            A[i][i - 1] = 1;
            A[i][i] = 4;
            A[i][i + 1] = 1;
        }
        if (Double.isNaN(endSlope)) {
            // the second derivative at the end is 0 if no end slope is provided
            A[n_1][n_1 - 1] = 1;
            A[n_1][n_1] = 2;
        } else {
            A[n_1][n_1] = 1;
        }
        Matrix mat_a = new Matrix(A);

        // setup matrix B, which is a vector
        double[] B = new double[n];
        if (Double.isNaN(startSlope)) {
            B[0] = 3 * (t[1] - t[0]); // second derivate is 0
        } else {
            B[0] = startSlope; // slope at start
        }
        for (int i = 1; i < n_1; i++) {
            B[i] = 3 * (t[i + 1] - t[i - 1]);
        }
        if (Double.isNaN(endSlope)) {
            // the second derivative at the end is 0 if no end slope is provided
            B[n_1] = 3 * (t[n_1] - t[n_1 - 1]);
        } else {
            B[n_1] = endSlope;
        }
        Matrix mat_b = new Matrix(B, n);
        
        // solve for D
        Matrix mat_d = mat_a.inverse().times(mat_b);
        // compute the spline coefficients a, b, c, d
        double[][] D = mat_d.getArray();

        // a = yi
        // b = Di
        // c = 3(yi+1 - yi) - 2dDi - Di+1
        // d = 2(yi - yi+1) + dDi + Di+1 = -2(yi+1 - yi) + dDi + Di+1
        final int coefCount = this.t.length - 1;
        this.abcd = new double[coefCount][4];
        for (int i = 0; i < coefCount; i++) {
            final double di = D[i][0];
            final double diplus1 = D[i + 1][0];
            final double[] abcd_row = this.abcd[i];
            final double valDif = this.t[i + 1] - this.t[i];
            abcd_row[0] = this.t[i];
            abcd_row[1] = di;
            abcd_row[2] = 3 * valDif - 2 * di - diplus1;
            abcd_row[3] = -2 * valDif + di + diplus1;
        }
    }

    /**
     * Evaluates the piecewise cubic spline Y = a+b*t+c*t*t+d*t*t*t at a given position.
     * @param i The segment for which to evaluate the spline, must be in
     * [0..getKnotsCount()-1]. An IndexOutOfBoundsException is thrown otherwise.
     * @t The spline parameter for segment i, must be in [0..1]. Otherwise the
     * result is not defined.
     * @return The value defined by the piecewise cubic spline at position i + t.
     */
    public final double eval(int i, double t) {
        final double[] abcd_row = abcd[i];
        return abcd_row[0] + t * (abcd_row[1] + t * (abcd_row[2] + t * abcd_row[3]));
    }

    /**
     * Evaluates a piecewise cubic spline at position x.
     * @param x The position where the spline is evaluated. Must be in
     * [0..getKnotsCount()-1]. An IndexOutOfBoundsException is thrown otherwise.
     * @return The value defined by the piecewise cubic spline at position x.
     */
    public final double eval(double x) {
        int i = (int) x;
        if (i >= this.abcd.length) {
            i = this.abcd.length - 1;
        }
        final double ti = x - i;
        return eval(i, ti);
    }

    /**
     * Compute the first derivative of a piecewise cubic spline at a given position.
     * @param i The segment for which to evaluate the spline, must be in
     * [0..getKnotsCount()-1]. An IndexOutOfBoundsException is thrown otherwise.
     * @t The spline parameter for segment i, must be in [0..1]. Otherwise the
     * result is not defined.
     * @return The first derivative of the piecwise cubic spline at position i + t.
     */
    public final double firstDerivative(int i, double t) {
        final double[] abcd_row = abcd[i];
        return abcd_row[1] + t * (2. * abcd_row[2] + t * 3. * abcd_row[3]);
    }

    /**
     * Computes the first derivative of a piecewise cubic spline at position x.
     * @param x The position where the spline is evaluated. Must be in
     * [0..getKnotsCount()-1]. An IndexOutOfBoundsException is thrown otherwise.
     * @return The first derivative of the piecewise cubic spline at position x.
     */
    public final double firstDerivative(double x) {
        int i = (int) x;
        if (i >= this.abcd.length) {
            i = this.abcd.length - 1;
        }
        final double ti = x - i;
        return firstDerivative(i, ti);
    }
}