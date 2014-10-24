package ika.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CatmullRomSpline implements Cloneable {

    // parameters for Catmull-Rom splines
    private final static float m00 = -0.5f;
    private final static float m01 = 1.5f;
    private final static float m02 = -1.5f;
    private final static float m03 = 0.5f;
    private final static float m10 = 1.0f;
    private final static float m11 = -2.5f;
    private final static float m12 = 2.0f;
    private final static float m13 = -0.5f;
    private final static float m20 = -0.5f;
    // private final static float m21 = 0.0f;
    private final static float m22 = 0.5f;
    // private final static float m23 = 0.0f;
    // private final static float m30 = 0.0f;
    private final static float m31 = 1.0f;
    // private final static float m32 = 0.0f;
    // private final static float m33 = 0.0f;
    
    public float[] x;
    public float[] y;

    private double newtonX = 0.5;

    final static double TOL = 1.0E-5F;

    public CatmullRomSpline() {
        super();
        reset();
    }

    public CatmullRomSpline(String str) {
        super();
        x = new float[0];
        y = new float[0];
        this.fromString(str);
    }

    public CatmullRomSpline(CatmullRomSpline curve) {
        super();
        x = (float[]) curve.x.clone();
        y = (float[]) curve.y.clone();
    }

    @Override
    public CatmullRomSpline clone() {
        return new CatmullRomSpline(this);
    }

    public void reset() {
        x = new float[]{0, 1};
        y = new float[]{0, 1};
        newtonX = 0.5;
    }

    public int addKnot(float kx, float ky) {
        int pos = findKnot(kx, ky);
        if (pos >= 0) {
            return pos;
        }
        int numKnots = x.length;
        float[] nx = new float[numKnots + 1];
        float[] ny = new float[numKnots + 1];
        int j = 0;
        for (int i = 0; i < numKnots; i++) {
            if (pos == -1 && x[i] > kx) {
                pos = j;
                nx[j] = kx;
                ny[j] = ky;
                j++;
            }
            nx[j] = x[i];
            ny[j] = y[i];
            j++;
        }
        if (pos == -1) {
            pos = j;
            nx[j] = kx;
            ny[j] = ky;
        }
        x = nx;
        y = ny;
        return pos;
    }

    public void removeKnot(int n) {
        int numKnots = x.length;
        if (numKnots <= 2) {
            return;
        }
        float[] nx = new float[numKnots - 1];
        float[] ny = new float[numKnots - 1];
        int j = 0;
        for (int i = 0; i < numKnots - 1; i++) {
            if (i == n) {
                j++;
            }
            nx[i] = x[j];
            ny[i] = y[j];
            j++;
        }
        x = nx;
        y = ny;
    }

    private void sortKnots() {
        int numKnots = x.length;
        for (int i = 1; i < numKnots - 1; i++) {
            for (int j = 1; j < i; j++) {
                if (x[i] < x[j]) {
                    float t = x[i];
                    x[i] = x[j];
                    x[j] = t;
                    t = y[i];
                    y[i] = y[j];
                    y[j] = t;
                }
            }
        }
    }

    private int findKnot(float kx, float ky) {
        for (int i = 0; i < this.x.length; i++) {
            if (kx == x[i] && ky == y[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a table with 256 integer values in the range [0..255]
     * @return
     */
    public int[] makeTable() {
        int[] table = new int[256];
        for (int px = 0; px < 256; px++) {
            table[px] = (int)Math.round(255 * evaluate(px / 255F));
        }
        return table;
    }

    /**
     * Clamp a value to an interval.
     * @param a the lower clamp threshold
     * @param b the upper clamp threshold
     * @param x the input parameter
     * @return the clamped value
     */
    public static float clamp(float x, float a, float b) {
        return (x < a) ? a : (x > b) ? b : x;
    }

    /**
     * Evaluate Cathmull-Rom spline defined by a set of knots at x
     * @param x Position to evaluate the spline. Must be between 0 and 1
     * @param knots The first and last knot are implicetly duplicated.
     * @return The spline value at x.
     */
    private static float spline(float x, float[] knots) {

        final int numKnots = knots.length;
        final int numSpans = numKnots - 1;
        if (numSpans < 1) {
            throw new IllegalArgumentException("Too few knots in spline");
        }

        x = clamp(x, 0f, 1f) * numSpans;
        int span = (int) x;
        if (span > numKnots - 2) {
            span = numKnots - 2;
        }
        x -= span;
       
        final float k0 = knots[Math.max(0, span - 1)];
        final float k1 = knots[span];
        final float k2 = knots[span + 1];
        final float k3 = knots[Math.min(numKnots - 1, span + 2)];
        
        final float c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
        final float c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
        final float c1 = m20 * k0 + m22 * k2;
        final float c0 = m31 * k1;

        return ((c3 * x + c2) * x + c1) * x + c0;
    }

    /**
     * Compute the first derivative of the spline at x.
     * @param x
     * @param knots
     * @return
     */
    private static float splineDerivative(float x, float[] knots) {

        final int numKnots = knots.length;
        final int numSpans = numKnots - 1;
        if (numSpans < 1) {
            throw new IllegalArgumentException("Too few knots in spline");
        }

        x = clamp(x, 0f, 1f) * numSpans;
        int span = (int) x;
        if (span > numKnots - 2) {
            span = numKnots - 2;
        }
        x -= span;

        final float k0 = knots[Math.max(0, span - 1)];
        final float k1 = knots[span];
        final float k2 = knots[span + 1];
        final float k3 = knots[Math.min(numKnots - 1, span + 2)];

        final float c3 = m00 * k0 + m01 * k1 + m02 * k2 + m03 * k3;
        final float c2 = m10 * k0 + m11 * k1 + m12 * k2 + m13 * k3;
        final float c1 = m20 * k0 + m22 * k2;

        return ((3 * c3 * x + 2 * c2) * x + c1) * numSpans;
    }

    /**
     * Newton-Raphson method to find the spline value for t [0..1] of the
     * x knots.
     * The x value is stored in newtonX and reused as initial value for the next
     * computation.
     * @param t spline parameter [0..1]
     */
    private void newtonX(double t) {
        final double MIN_DER = 0.001;
        final int MAX_ITER = 20;
        double diff; // difference between the previous and the new newtonY
        int iter;
        for (iter = 1; iter <= MAX_ITER; ++iter) {
            final double fy = spline((float) newtonX, x) - t;
            final double fyder = splineDerivative((float) newtonX, x);

            // if the slope is close to 0 the behaviour of the newton method
            // becomes erratic and is not define it the slope is 0.
            if (Math.abs(fyder) < MIN_DER) {
                newtonX = bisection(t, x);
                break;
            }
            newtonX -= diff = fy / fyder;
            if (Math.abs(diff) < TOL) {
                break;
            }
        }
        if (iter == MAX_ITER) {
            newtonX = bisection(t, x);
        }
        if (newtonX > 1) {
            newtonX = 1;
        } else if (newtonX < 0) {
            newtonX = 0;
        }
    }

    /**
     * bisection method to find the spline value for t [0..1]
     * @param t spline parameter [0..1]
     * @param knots The knots defining the spline
     * @return
     */
    private static double bisection(double t, float[] knots) {
        
        double f1 = 0;
        double f2 = 1;
        double y = (f1 + f2) / 2.0F;
        double diff; // difference between the previous and the new value
        do {
            final double fy = spline((float)y, knots);
            if (fy > t) {
                f2 = y;
            } else {
                f1 = y;
            }
            final double f_ = (f1 + f2) / 2;
            diff = Math.abs(y - f_);
            y = f_;
        } while (diff > TOL);
        return y;
    }

    /**
     * Returns the interpolated value at position t [0..1]. This method is
     * optimized for a series of t parameter that are close to each other. If
     * the current t parameter is different from previous calls, evaluate(t)
     * should be preferred. Attention: this method cashes intermediate results
     * in the variable newtonX. A single instance of this object should therefore
     * not be used concurrently by two different threads.
     * Calls to evaluate and evaluate() should not be mixed, as
     * evaluate() resets the cashed value.
     * @param t The position to evaluate the curve between 0 and 1
     * @return The value at t between 0 and 1.
     */
    public float evaluateSimilarToPrevious(double t) {
        newtonX(t);
        float py = spline((float) newtonX, y);
        py = clamp(py, 0f, 1f);
        return py;
    }

    public float evaluate(double t) {
        newtonX = 0.5;
        return evaluateSimilarToPrevious(t);
    }

    public void fromString(String str) {
        ArrayList<Float> xp = new ArrayList<Float>();
        ArrayList<Float> yp = new ArrayList<Float>();
        StringTokenizer tokenizer = new StringTokenizer(str, " ");
        while (tokenizer.hasMoreElements()) {
            xp.add(Float.parseFloat(tokenizer.nextToken()));
            yp.add(Float.parseFloat(tokenizer.nextToken()));
        }
        if (xp.size() >= 2) {
            this.x = new float[0];
            this.y = new float[0];
            for (int i = 0; i < xp.size(); i++) {
                this.addKnot(xp.get(i), yp.get(i));
            }
        }
    }

    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("##0.####");
        DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dfs);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.x.length; i++) {
            sb.append(formatter.format(x[i]));
            sb.append(" ");
            sb.append(formatter.format(y[i]));
            sb.append(" ");
        }
        return sb.toString();
    }
}
