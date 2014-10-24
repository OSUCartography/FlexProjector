/*
 * TransformationRobustHelmert
 *
 * Created on March 23, 2005, 12:10 PM
 */

package ika.transformation;

import Jama.*;
import ika.transformation.robustestimator.*;
import ika.utils.Median;
import java.text.*;
import java.io.*;
import ika.utils.*;

/**
 * Robust Helmert Transformation - a Helmert transformation with a robust estimator.<br>
 * Different types of estimators can be used, e.g. Huber, Hampel, etc.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class TransformationRobustHelmert extends Transformation implements Serializable {
    
    private static final long serialVersionUID = -3585115773164408066L;
    
    /**
     * The robust estimator that is used to compute the parameters of this transformation.
     */
    private RobustEstimator robustEstimator;
    /**
     * Matrix of differences between the transformed source points and the destination
     * points. This matrix is needed to compute the P matrix.
     */
    private Matrix mat_v = null;
    /**
     * MAD: the median of all values of mat_v
     */
    private double s;
    /**
     * Iterative computation is stopped when changes of s are smaller than sTolerance.
     */
    private double sTolerance = 0.0000001;
    
    /**
     * The tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     */
    private double parameterTolerance = 0.0000001;
    
    /**
     * The parameters of this transformation.
     */
    private double [] params = new double [4];
    /**
     * Access to horizontal translation parameter in params.
     */
    private static final int TRANSX = 0;
    /**
     * Access to vertical translation parameter in params.
     */
    private static final int TRANSY = 1;
    /**
     * Access to A1 parameter in params.
     */
    private static final int A1 = 2;
    /**
     * Access to A2 parameter in params.
     */
    private static final int A2 = 3;
    
    /**
     * Element (0, 0) of Q (Q = inv(ATPA))
     */
    private double q11;
    
    /**
     * Element (1, 1) of Q (Q = inv(ATPA))
     */
    private double q22;
    
    /**
     * Element (2, 2) of Q (Q = inv(ATPA))
     */
    private double q33;
    
    /**
     * Element (3, 3) of Q (Q = inv(ATPA))
     */
    private double q44;
    /**
     * sigma 0
     */
    private double sigma0;
    
    /**
     * Stores the number of iterations that were necessary to compute the parameters.
     */
    private int numberOfIterations = 0;
    
    public TransformationRobustHelmert() {
        this.robustEstimator = new VEstimator();
    }
    
    public TransformationRobustHelmert(RobustEstimator robustEstimator) {
        this.robustEstimator = robustEstimator;
    }
    
    /**
     * Returns the name of the transformation.
     * @return The name.
     */
    public String getName() {
        return "Robust Helmert";
    }
    
    /**
     * Returns a short description of this transformation.
     * getShortDescription can be called before the transformation is
     * initialized using initWithPoints
     * @return The description.
     */
    public String getShortDescription() {
        StringBuffer str = new StringBuffer(1024);
        str.append(this.getName());
        str.append("\n");
        str.append("4 Parameters:\n");
        
        str.append("X = x0 + ax - by\n");
        str.append("Y = y0 + bx + ay\n");
        str.append("a = m * cos(alpha)\n");
        str.append("b = m * sin(alpha)\n");
        
        str.append("x0:    Horizontal Translation\n");
        str.append("y0:    Vertical Translation\n");
        str.append("m:     Scale Factor\n");
        str.append("alpha: Rotation in Counter-Clockwise Direction\n\n");
        
        if (this.robustEstimator != null)
            str.append(this.robustEstimator.getDescription());
        
        return str.toString();
    }
    
    /**
     * Returns a report containing the computed parameters of this
     * transformation.
     * @return The description.
     */
    public String getReport(boolean invert)  {
        StringBuffer str = new StringBuffer(1024);
        str.append(this.getShortDescription());
        str.append("\n");
        if (this.robustEstimator != null)
            str.append(this.robustEstimator.getDescriptionOfValues());
        str.append("\n\n");
        
        str.append("Parameters computed with ");
        str.append(this.getNumberOfPoints());
        str.append(" points.\n\n");
        
        str.append("x0 Translation Horizontal:                         ");
        str.append(formatPrecise(this.getTranslationX()));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getTranslationXSigma()));
        str.append("\n");
        str.append("y0 Translation Vertical:                           ");
        str.append(formatPrecise(this.getTranslationY()));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getTranslationYSigma()));
        str.append("\n");
        
        str.append("m Scale Factor:                                    ");
        str.append(formatPrecise(this.getScale(invert)));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getScaleSigma(invert)));
        str.append("\n");
        
        str.append("alpha Rotation: [deg ccw]                          ");
        str.append(formatPrecise(Math.toDegrees(this.getRotation())));
        str.append(" +/-");
        str.append(formatPreciseShort(Math.toDegrees(this.getRotationSigma())));
        str.append("\n\n");
        
        final double scale = this.getScale(invert);
        final double sigma0 = this.getSigma0();               
        str.append("Standard Deviation in Destination Map [m]:         ");
        str.append(formatPrecise(sigma0 * scale));
        str.append("\n");
        
        str.append("Standard Deviation in Source Map [m]:              ");
        str.append(formatPrecise(sigma0));
        str.append("\n");
        
        double sep = this.getStandardErrorOfPosition();
        str.append("Mean Position Error in Destination Map [m]:        ");
        str.append(formatPrecise(sep * scale));
        str.append("\n");
        
        str.append("Mean Position Error in Source Map [m]:             ");
        str.append(formatPrecise(sep));
        str.append("\n");
        return str.toString();
    }
    
    public String getShortReport(boolean invert) {
        StringBuffer str = new StringBuffer(1024);
        
        str.append(this.robustEstimator.getName());
        str.append("\n");
        str.append(NumberFormatter.formatScale("Scale", this.getScale(invert), true));
        str.append("\n");
        
        str.append (this.formatRotation("Rotation", this.getRotation(invert)));
        str.append("\n");
        
        double scale = this.getScale(invert);
        if (!invert)
            scale = 1;
        str.append(this.formatSigma0(this.getSigma0() * scale));
        str.append("\n");
        str.append(this.formatStandardErrorOfPosition(this.getStandardErrorOfPosition() * scale));
        str.append("\n");
        
        return str.toString();
    }
    
    /**
     * Returns the rotation used by this transformation.
     * @return The rotation.
     */
    public double getRotation()  {
        double rot = Math.atan2(params[A2], params[A1]);
        if (rot < 0.)
            rot += Math.PI * 2.;
        return rot;
    }
    
    /**
     * Returns the precision of the rotation.
     * @return The precision.
     */
    public double getRotationSigma()  {
        return this.getScaleSigma(false)/this.getScale();
    }
    
    /**
     * Returns the scale factor used by this transformation.
     * @return The scale factor.
     */
    public double getScale() {
        return Math.sqrt(params[A1]*params[A1]+params[A2]*params[A2]);
    }
    
    /**
     * Returns the precision of the scale factor.
     * @return The precision of the scale factor.
     */
    public double getScaleSigma(boolean invert) {
        if (invert) {
            final double scale = this.getScale();
            return this.sigma0 * this.q33 / (scale * scale);
        }
        return this.sigma0 * this.q33;
    }
    
    /**
     * Returns the horizontal translation paramater
     * used by this transformation.
     * @return The horizontal translation.
     */
    public double getTranslationX() {
        return params[TRANSX]; // this.cx1 - this.a1*this.cx2 + this.a2*this.cy2;
    }
    
    /**
     *  Returns the precision of the horizontal translation
     * paramater used by this transformation.
     * @return The precision of the horizontal translation.
     */
    public double getTranslationXSigma() {
        return this.sigma0 * q11;
    }
    
    /**
     *  Returns the vertical translation paramater
     * used by this transformation.
     * @return The vertical translation.
     */
    public double getTranslationY() {
        return params[TRANSY]; // this.cy1 - this.a2*this.cx2 - this.a1*this.cy2;
    }
    
    /**
     *   Returns the precision of the vertical translation
     * paramater used by this transformation.
     * @return The precision of the vertical translation.
     */
    public double getTranslationYSigma() {
        return this.sigma0 * q22;
    }
    
    /**
     * Returns sigma 0.
     * @return sigma 0
     */
    public double getSigma0() {
        return this.sigma0;
    }
    
    /**
     * Constructs the weight matrix P.
     * @return Wheight matrix P.
     */
    private Matrix constructP() {
        if (this.mat_v == null)
            return Matrix.identity(numberOfPoints*2, numberOfPoints*2);
        else {
            // VERSION Beineke 2001 S101
            // Berechnung mit Laengen der Restklaffungsvektoren
            // Liefert gleiche Resultate wie Beineke 2003 Seite 8, Kolonne HU-D.
            // jedoch Fehler(?) in Beineke 2001 (siehe unten)
            double[] d = new double [this.mat_v.getRowDimension()/2];
            
            for (int i = 0; i < d.length; i++) {
                final double vx = mat_v.get(i*2, 0);
                final double vy = mat_v.get(i*2+1, 0);
                d[i] = Math.sqrt(vx*vx+vy*vy);
            }
            
            final double dmed = Median.median(d, true);
            double[] d_minus_dmed = new double [this.mat_v.getRowDimension()/2];
            for (int i = 0; i < d_minus_dmed.length; i++) {
                d_minus_dmed[i] = Math.abs(d[i] - dmed);
            }
            this.s = Median.median(d_minus_dmed, false) / 0.4485;
            
            // compute P
            final int pSize = mat_v.getRowDimension();
            Matrix mat_P = new Matrix(pSize, pSize);
            for (int i = 0; i < d.length; i++) {
                // - dmed vermutlich falsch in Beineke 2001 S.101:
                // final double ui = (d[i] - dmed) / s;
                final double ui = d[i] / s;
                final double w = robustEstimator.w(ui);
                mat_P.set(i*2, i*2, w);
                mat_P.set(i*2+1, i*2+1, w);
            }
            return mat_P;
            
            
            /* -0.01619
             * -0.00770
             *  1.00987
             *358.98945 */
            /*
            // VERSION Beineke 2003 S5. Kolonne HU-V Seite 8
            // Resulate nicht identisch mit Beineke 2003
             
            double[] v = new double [this.mat_v.getRowDimension()];
            double[] vCopy = new double [this.mat_v.getRowDimension()];
            double[] abs_v_minus_medv = new double [this.mat_v.getRowDimension()];
            for (int i = 0; i < v.length; i++) {
                v[i] = vCopy[i] = mat_v.get(i, 0);
            }
            final double medv = Median.median(vCopy);
            for (int i = 0; i < abs_v_minus_medv.length; i++) {
                abs_v_minus_medv[i] = Math.abs(v[i] - medv);
            }
             
            this.s = Median.median(abs_v_minus_medv) / 0.6745;
             
            // compute P
            Matrix mat_P = new Matrix(v.length, v.length);
            for (int i = 0; i < v.length; i++) {
                final double w = robustEstimator.w(v[i] / this.s);
                mat_P.set(i, i, w);
            }
            return mat_P;
             */
            /*
            // VERSION vx und vy getrennt
            final int vHalfLength = this.mat_v.getRowDimension() / 2;
            double[] vx = new double [vHalfLength];
            double[] vxCopy = new double [vHalfLength];
            double[] vy = new double [vHalfLength];
            double[] vyCopy = new double [vHalfLength];
            double[] abs_vx_minus_medvx = new double [vHalfLength];
            double[] abs_vy_minus_medvy = new double [vHalfLength];
             
            for (int i = 0; i < vx.length; i++) {
                vx[i] = vxCopy[i] = mat_v.get(i*2, 0);
                vy[i] = vyCopy[i] = mat_v.get(i*2+1, 0);
            }
            final double medvx = Median.median(vxCopy);
            final double medvy = Median.median(vyCopy);
            for (int i = 0; i < abs_vx_minus_medvx.length; i++) {
                abs_vx_minus_medvx[i] = Math.abs(vx[i] - medvx);
                abs_vy_minus_medvy[i] = Math.abs(vy[i] - medvy);
            }
             
            double sx = Median.median(abs_vx_minus_medvx) / 0.6745;
            double sy = Median.median(abs_vy_minus_medvy) / 0.6745;
             
            // compute P
            Matrix mat_P = new Matrix(vHalfLength*2, vHalfLength*2);
            for (int i = 0; i < vHalfLength; i++) {
                final double wx = robustEstimator.w(vx[i] / sx);
                mat_P.set(i*2, i*2, wx);
                final double wy = robustEstimator.w(vy[i] / sy);
                mat_P.set(i*2+1, i*2+1, wy);
            }
            System.out.println(test_dtot);
            System.out.println(test_vtot);
            return mat_P;
             */
            /*
            // VERSION Beineke 2001 S99
            double[] v = new double [this.mat_v.getRowDimension()];
            double[] v_abs = new double [this.mat_v.getRowDimension()];
            for (int i = 0; i < v.length; i++) {
                v[i] = mat_v.get(i, 0);
                v_abs[i] = Math.abs(v[i]);
            }
             
            this.s = Median.median(v_abs); // s: median value of v
            //            System.out.println("Median: " + this.s);
             
            // compute P
            Matrix mat_P = new Matrix(v.length, v.length);
            for (int i = 0; i < v.length; i++) {
                final double w = robustEstimator.w(v[i] / this.s);
                mat_P.set(i, i, w);
            }
            return mat_P;
             */
        }
    }
    
    /**
     * Returns the number of iterations that were necessary to compute the parameters.
     * @return The number of iterations
     */
    public int getNumberOfIterations(){
        return this.numberOfIterations;
    }
    
    /**
     * Computes new parameters for this transformation.
     * @param mat_l Matrix l containing the coordinates of the destination point set.
     * @param mat_A Model matrix A.
     * @param result The new parameters are stored in the array referenced by result.
     * Required size of result: 4x1
     */
    private void solve(Matrix mat_l, Matrix mat_A, double[] result) {
        // Construct P matrix.
        Matrix mat_P = this.constructP();
        //System.out.println();
        //mat_P.print(15, 3);
        
        // Compute transformation parameters
        Matrix mat_Atrans = mat_A.transpose();
        Matrix mat_Q = mat_Atrans.times(mat_P).times(mat_A).inverse();
        Matrix mat_x = mat_Q.times(mat_Atrans.times(mat_P).times(mat_l));
        
        // Compute v: Residuals
        this.mat_v = mat_A.times(mat_x).minus(mat_l);
        
        this.q11 = mat_Q.get(0, 0);
        this.q22 = mat_Q.get(1, 1);
        this.q33 = mat_Q.get(2, 2);
        this.q44 = mat_Q.get(3, 3);
        
        // copy resulting parameters
        for (int i = 0; i < 4; i++)
            result[i] = mat_x.get(i,0);
    }
    
    /**
     * Internal utility method that tests whether the difference between the old and
     * the new parameters are sufficiently small to stop the iterative computation.
     */
    private boolean smallDiff(double[] x, double[] y) {
        return (   Math.abs(x[0]-y[0]) < this.parameterTolerance
                && Math.abs(x[1]-y[1]) < this.parameterTolerance
                && Math.abs(x[2]-y[2]) < this.parameterTolerance
                && Math.abs(x[3]-y[3]) < this.parameterTolerance);
    }
    
    /**
     * Initialize the transformation with two set of control points.
     * The control points are not copied, nor is a reference to them
     * retained. Instead, the transformation parameters are
     * immmediately computed by initWithPoints.
     * @param destSet A two-dimensional array (nx2) containing the x and y
     * coordinates of the points of the destination set.
     * The destSet must be of exactly the same size as sourceSet.
     * @param sourceSet A two-dimensional array (nx2) containing the x and y
     * coordinates of the points of the source set.
     * The sourceSet must be of exactly the same size as destSet.
     */
    protected void initWithPoints(double[][] destSet, double[][] sourceSet) {
        
        // Construct l matrix with points of set 1.
        double[][] lArray = new double [this.numberOfPoints*2][1];
        Matrix mat_l = new Matrix(lArray,this.numberOfPoints*2, 1);
        for (int i = 0; i < this.numberOfPoints; i++) {
            lArray[i*2][0] = destSet[i][0];
            lArray[i*2+1][0] = destSet[i][1];
        }
        
        // Construct A matrix.
        Matrix mat_A = new Matrix(this.numberOfPoints*2, 4, 0.);
        for (int i = 0; i < this.numberOfPoints; i++) {
            double [][]a ={
                {1., 0., sourceSet[i][0], -sourceSet[i][1]},
                {0., 1., sourceSet[i][1], sourceSet[i][0]}
            };
            mat_A.setMatrix(i*2, i*2+1, 0, 3, new Matrix(a, 2, 4));
        }
        
        // initialize values
        this.s = 0;
        this.numberOfIterations = 0;
        for (int i = 0; i < 4; i++)
            this.params[i] = 0;
        double[] params_new = new double [4];
        
        // recursevely compute improved params[]
        do {
            this.numberOfIterations++;
            final double s_old = this.s;
            this.solve(mat_l, mat_A, params_new);
            
            // print report for current iteration
            /*
            for (int i = 0; i < 4; i++)
                System.out.println(numberOfIterations + " " + i +": "+ params_new [i]);
            System.out.println(numberOfIterations + " s: " + this.s);
            System.out.println(numberOfIterations + " d0: "+Math.abs(params[0]-params_new[0]));
            System.out.println(numberOfIterations + " d1: "+Math.abs(params[1]-params_new[1]));
            System.out.println(numberOfIterations + " d2: "+Math.abs(params[2]-params_new[2]));
            System.out.println(numberOfIterations + " d3: "+Math.abs(params[3]-params_new[3]));
            System.out.println(numberOfIterations + " ds: "+Math.abs(s-s_old));
            System.out.println();
             */
            /* Stop the iterations when changes of the transformation parameters
             * and the change of s (s=median of residuals vi) are small enough.
             */
            if (this.smallDiff(params_new , this.params)
            && Math.abs(s_old - this.s) < this.sTolerance)
                break;
            for (int i = 0; i < 4; i++) {
                this.params[i] = params_new [i];
            }
        } while (true);
        
        double[] v = new double [this.mat_v.getRowDimension()];
        double[] v_abs = new double [this.mat_v.getRowDimension()];
        for (int i = 0, j = 0; i < v.length / 2; i++, j+= 2) {
            final double vx = this.mat_v.get(j, 0);
            final double vy = this.mat_v.get(j+1, 0);
            v[j] = vx;
            v[j+1] = vy;
            v_abs[j] = Math.abs(vx);
            v_abs[j+1] = Math.abs(vy);
            this.v[i][0] = vx;  // copy residuals to this.v
            this.v[i][1] = vy;
        }
        final double mad = Median.median(v_abs, false);
        this.sigma0 = this.robustEstimator.getSigma0(v, mad);
        
        /*
         final double[][] v_arr = mat_v.getArray();
        for (int i = 0; i < numberOfPoints; i++) {
            this.v[i][0] = v_arr[i*2][0];
            this.v[i][1] = v_arr[i*2+1][0];
        }
         */
    }
    
    /**
     * Return the tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     */
    public double getParameterTolerance() {
        return parameterTolerance;
    }
    
    /**
     * Sets the tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     */
    public void setParameterTolerance(double parameterTolerance) {
        this.parameterTolerance = parameterTolerance;
    }
    
    /**
     * Sets the robust estimator that is used to estimate the parameters of this
     * transformation.
     * @param robustEstimator The estimator
     */
    public void setRobustEstimator(RobustEstimator robustEstimator) {
        if (robustEstimator == null)
            throw new IllegalArgumentException();
        this.robustEstimator = robustEstimator;
    }
    
    /**
     * Returns a reference on the robust estimator that is used to estimate the
     * parameters of this transformation.
     * @return The estimator.
     */
    public RobustEstimator getRobustEstimator() {
        return this.robustEstimator;
    }
    
    /**
     * Transform a point from the coordinate system of the source set
     * to the coordinate system of destination set.
     * @return The transformed coordinates (array of size 1x2).
     * @param point The point to be transformed (array of size 1x2).
     */
    public double[] transform(double[] point)  {
        return new double[] {
            params[A1]*point[0]-params[A2]*point[1]+params[TRANSX],
                    params[A2]*point[0]+params[A1]*point[1]+params[TRANSY]};
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
       return null; 
    }
}

