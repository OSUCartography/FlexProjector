/*
 * TransformationAffine5
 *
 * Created on March 23, 2005, 12:10 PM
 */

package ika.transformation;

import Jama.*;
import java.text.*;
import java.io.*;
import ika.utils.*;

/**
 * Affine Transformation with 5 parameters:
 * translation in horizontal and vertical direction, one rotation angle
 * and two scale factors.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class TransformationAffine5 extends Transformation implements Serializable {
    
    private static final long serialVersionUID = -7154302211383221986L;
    
    /**
     * The parameters of this transformation.
     * Order:
     * translation in x direction
     * translation in y direction
     * scale in x direction
     * scale in y direction
     * rotation
     * Use TRANSX, TRANSY, SCALEX, SCALEY and ROT to access the values of this array.
     */
    private double [] params = new double [5];
    
    /**
     * TRANSX, TRANSY, SCALEX, SCALEY and ROT are indexes to access values in the
     * array params.
     */
    private static final int TRANSX = 0;
    /**
     * TRANSX, TRANSY, SCALEX, SCALEY and ROT are indexes to access values in the
     * array params.
     */
    private static final int TRANSY = 1;
    /**
     * TRANSX, TRANSY, SCALEX, SCALEY and ROT are indexes to access values in the
     * array params.
     */
    private static final int SCALEX = 2;
    /**
     * TRANSX, TRANSY, SCALEX, SCALEY and ROT are indexes to access values in the
     * array params.
     */
    private static final int SCALEY = 3;
    /**
     * TRANSX, TRANSY, SCALEX, SCALEY and ROT are indexes to access values in the
     * array params.
     */
    private static final int ROT = 4;
    
    /**
     * Stores the number of iterations that were necessary to compute the parameters.
     */
    private int numberOfIterations = 0;
    
    /**
     * The tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     */
    private double parameterTolerance = 0.000001;
    
    /**
     * Sigma 0 is the error per unit.
     */
    private double sigma0;
    
    /**
     * Sigma of the translation parameters. Horizontally and vertically equal.
     */
    private double transSigma;
    /**
     * The sigma of the horizontal scale parameter.
     */
    private double scaleXSigma;
    /**
     * The sigma of the vertical scale parameter.
     */
    private double scaleYSigma;
    /**
     * The sigma of the rotation parameter.
     */
    private double rotSigma;
    
    /**
     * X coordinate of the center of gravity of the destination point set.
     */
    private double cxDst;
    
    /**
     * Y coordinate of the center of gravity of the destination point set.
     */
    private double cyDst;
    
    /**
     * X coordinate of the center of gravity of the source point set.
     */
    private double cxSrc;
    
    /**
     * Y coordinate of the center of gravity of the source point set.
     */
    private double cySrc;
    
    /**
     * Returns the name of the transformation.
     * @return The name.
     */
    public String getName() {
        return "Affine (5 Parameters)";
    }
    
    /**
     * Returns a short description of this transformation.
     * getShortDescription can be called before the transformation is
     * initialized using initWithPoints
     * @return The description.
     */
    public String getShortDescription() {
        StringBuffer str = new StringBuffer(512);
        str.append(this.getName());
        str.append("\n");
        str.append("5 Parameters:\n");
        str.append("X = x0 + mx*cos(alpha)*x - my*sin(alpha)*y\n");
        str.append("Y = y0 + mx*sin(alpha)*x + my*cos(alpha)*y\n");
        str.append("x0:    Horizontal Translation\n");
        str.append("y0:    Vertical Translation\n");
        str.append("mx:    Horizontal Scale Factor\n");
        str.append("my:    Vertical Scale Factor\n");
        str.append("alpha: Rotation in Counter-Clockwise Direction.");
        return str.toString();
    }
    
    /**
     * Returns a report containing the computed parameters of this
     * transformation.
     * @return The description.
     */
    public String getReport(boolean invert) {
        StringBuffer str = new StringBuffer(1024);
        str.append(this.getShortDescription());
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
        str.append("mx Horizontal Scale Factor:                        ");
        str.append(formatPrecise(this.getScaleX(invert)));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getScaleXSigma(invert)));
        str.append("\n");
        str.append("my Vertical Scale Factor:                          ");
        str.append(formatPrecise(this.getScaleY(invert)));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getScaleYSigma(invert)));
        str.append("\n");
        str.append("alpha Rotation [deg ccw]:                          ");
        str.append(formatPrecise(Math.toDegrees(this.getRotation())));
        str.append(" +/-");
        str.append(formatPreciseShort(Math.toDegrees(this.getRotationSigma())));
        str.append("\n\n");
        
        final double scale = this.getScale(invert);
        double sigma0 = this.getSigma0();
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
        
        str.append(NumberFormatter.formatScale("Scale Hor.", this.getScaleX(invert), true));
        str.append("\n");
        str.append(NumberFormatter.formatScale("Scale Vert.", this.getScaleY(invert), true));
        str.append("\n");
        
        str.append(this.formatRotation("Rotation", this.getRotation(invert)));
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
     * Returns the horizontal rotation used by this transformation.
     * @return The rotation.
     */
    public double getRotation() {
        return (params[ROT] < 0.) ? params[ROT] + Math.PI * 2. : params[ROT];
    }
    
    /**
     * Returns the precision of the horizontal rotation.
     * @return The precision.
     */
    public double getRotationSigma()  {
        return this.rotSigma;
    }
    
    
    public double getScale() {
        return (this.getScaleX(false) + this.getScaleY(false)) / 2.;
    }
    
    /**
     * Returns the scale factor in horizontal direction used by this transformation.
     * @return The scale factor.
     */
    public double getScaleX(boolean invert) {
        return invert ? 1. / params[SCALEX] : params[SCALEX];
    }
    
    /**
     * Returns the precision of the scale factor in horizontal direction .
     * @return The precision of the scale factor.
     */
    public double getScaleXSigma(boolean invert) {
        if (invert)
            return this.scaleXSigma / (params[SCALEX] * params[SCALEX]);
        else
            return this.scaleXSigma;
    }
    
    /**
     * Returns the scale factor in vertical direction used by this transformation.
     * @return The scale factor.
     */
    public double getScaleY(boolean invert){
        return invert ? 1. / params[SCALEY] : params[SCALEY];
    }
    
    /**
     * Returns the precision of the scale factor in vertical direction .
     * @return The precision of the scale factor.
     */
    public double getScaleYSigma(boolean invert){
        if (invert)
            return this.scaleYSigma / (params[SCALEY] * params[SCALEY]);
        else
            return this.scaleYSigma;
    }
    
    /**
     * Returns the horizontal translation paramater
     * used by this transformation.
     * @return The horizontal translation.
     */
    public double getTranslationX() {
        final double cosRot = Math.cos(this.params[ROT]);
        final double sinRot = Math.sin(this.params[ROT]);
        return this.cxDst
                - this.params[SCALEX]*cosRot*this.cxSrc
                + this.params[SCALEY]*sinRot*this.cySrc;
    }
    
    /**
     *  Returns the precision of the horizontal translation
     * paramater used by this transformation.
     * @return The precision of the horizontal translation.
     */
    public double getTranslationXSigma() {
        return this.transSigma;
    }
    
    /**
     *  Returns the vertical translation paramater
     * used by this transformation.
     * @return The vertical translation.
     */
    public double getTranslationY() {
        final double cosRot = Math.cos(this.params[ROT]);
        final double sinRot = Math.sin(this.params[ROT]);
        return this.cyDst
                - this.params[SCALEX]*sinRot*this.cxSrc
                - this.params[SCALEY]*cosRot*this.cySrc;
    }
    
    /**
     *   Returns the precision of the vertical translation
     * paramater used by this transformation.
     * @return The precision of the vertical translation.
     */
    public double getTranslationYSigma() {
        return this.transSigma;
    }
    
    /**
     * Returns sigma 0.
     * @return Sigma 0
     */
    public double getSigma0() {
        return this.sigma0;
    }
    
    /**
     * Returns the number of iterations that were necessary to compute the parameters.
     * @return The number of iterations.
     */
    public int getNumberOfIterations(){
        return this.numberOfIterations;
    }
    
    /**
     * Internal utility method that computes improvements for the parameters.
     * @param destSet The destination point set.
     * @param sourceSet The source point set.
     * @return An array with enhancement for the parameters.
     */
    private double[] solve(double[][] destSet, double[][] sourceSet) {
        
        double cosRot = Math.cos(params[ROT]);
        double sinRot = Math.sin(params[ROT]);
        
        // allocate matrix l and matrix A.
        double[][] lArray = new double [this.numberOfPoints * 2][1];
        Matrix mat_A = new Matrix(this.numberOfPoints * 2, 5);
        Matrix mat_l = new Matrix(lArray,this.numberOfPoints * 2, 1);
        
        // loop over all points to fill matrix l and matrix A.
        for (int i = 0; i < this.numberOfPoints; i++) {
            
            final double xCosRot = cosRot*(sourceSet[i][0]-cxSrc);
            final double xSinRot = sinRot*(sourceSet[i][0]-cxSrc);
            final double yCosRot = cosRot*(sourceSet[i][1]-cySrc);
            final double ySinRot = sinRot*(sourceSet[i][1]-cySrc);
            
            final double estimationX = this.params[TRANSX]
                    +this.params[SCALEX]*xCosRot
                    -this.params[SCALEY]*ySinRot;
            final double estimationY = this.params[TRANSY]
                    +this.params[SCALEX]*xSinRot
                    +this.params[SCALEY]*yCosRot;
            
            // fill matrix l
            lArray[i*2][0] = destSet[i][0] - cxDst - estimationX;
            lArray[i*2+1][0] = destSet[i][1] - cyDst - estimationY;
            
            // fill matrix A
            final double [][]a = {
                {1, 0, xCosRot, -ySinRot, -(destSet[i][1]-cyDst)+params[TRANSY]},
                {0, 1, xSinRot, yCosRot, (destSet[i][0]-cxDst)-params[TRANSX]}
            };
            mat_A.setMatrix(i*2, i*2+1, 0, 4, new Matrix(a, 2, 5));
        }
        
        // Compute transformation parameters
        Matrix mat_Atrans = mat_A.transpose();
        Matrix mat_Q = mat_Atrans.times(mat_A).inverse();
        Matrix mat_x = mat_Q.times(mat_Atrans.times(mat_l));
        
        // Compute v: Residuals
        Matrix mat_v = mat_A.times(mat_x).minus(mat_l);
        
        // Sigma aposteriori of planar vector (sigma0)
        final double vTv = this.vTv(mat_v);
        this.sigma0 = Math.sqrt(vTv/(2.*this.numberOfPoints-5));
        
        // copy residuals to this.v
        final double[][] v_arr = mat_v.getArray();
        for (int i = 0; i < numberOfPoints; i++) {
            this.v[i][0] = v_arr[i*2][0];
            this.v[i][1] = v_arr[i*2+1][0];
        }
        
        this.transSigma = this.sigma0 * Math.sqrt(mat_Q.get(0,0));
        this.scaleXSigma = this.sigma0 * Math.sqrt(mat_Q.get(2,2));
        this.scaleYSigma = this.sigma0 * Math.sqrt(mat_Q.get(3,3));
        this.rotSigma = this.sigma0 * Math.sqrt(mat_Q.get(4,4));
        
        /*
        System.out.println(Math.sqrt(matrix_Q.get(0,0)));
        System.out.println(Math.sqrt(matrix_Q.get(1,1)));
        System.out.println(Math.sqrt(matrix_Q.get(2,2)));
        System.out.println(Math.sqrt(matrix_Q.get(3,3)));
        System.out.println(Math.sqrt(matrix_Q.get(4,4)));
        System.out.println();
         */
        
        // copy results to array
        double[] x = new double[5];
        for (int i = 0; i < 5; i++)
            x[i] = mat_x.get(i,0);
        return x;
    }
    
    /**
     * Internal utility method that tests whether the improvements are sufficiently
     * small to stop the iterative computation.
     * @param x A vector of double values.
     * @param y A vector of double values.
     * @return true if differences between the two vectors are smaller than this.parameterTolerance
     */
    private boolean smallDiff(double[] x, double[] y) {
        return (   Math.abs(x[0]-y[0]) < this.parameterTolerance
                && Math.abs(x[1]-y[1]) < this.parameterTolerance
                && Math.abs(x[2]-y[2]) < this.parameterTolerance
                && Math.abs(x[3]-y[3]) < this.parameterTolerance
                && Math.abs(x[4]-y[4]) < this.parameterTolerance);
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
        
        // Compute centres of gravity of the two point sets.
        this.cxDst = this.cyDst = this.cxSrc = this.cySrc = 0.;
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.cxDst += destSet[i][0];
            this.cyDst += destSet[i][1];
            this.cxSrc += sourceSet[i][0];
            this.cySrc += sourceSet[i][1];
        }
        this.cxDst /= this.numberOfPoints;
        this.cyDst /= this.numberOfPoints;
        this.cxSrc /= this.numberOfPoints;
        this.cySrc /= this.numberOfPoints;
        
        // for a first estimation of the parameters compute a Helmert transformation
        TransformationHelmert helmert = new TransformationHelmert();
        helmert.init(destSet, sourceSet);
        this.params[TRANSX] = 0; // translation will be close to 0, since
        this.params[TRANSY] = 0; // both sets of points are centered around 0.
        this.params[SCALEX] = params[SCALEY] = helmert.getScale();
        this.params[ROT] = helmert.getRotation();
        
        double[] dx_previous = new double [5];
        this.numberOfIterations = 0;
        do {
            this.numberOfIterations++;
            double[] dx = this.solve(destSet, sourceSet);
            if (this.smallDiff(dx, dx_previous))
                break;
            for (int i = 0; i < 5; i++) {
                // System.out.println(this.params[i]);
                this.params[i] += dx[i];
                dx_previous[i] = dx[i];
            }
        } while (true);
    }
    
    /**
     * Transform a point from the coordinate system of the source set
     * to the coordinate system of destination set.
     * @return The transformed coordinates (array of size 1x2).
     * @param point The point to be transformed (array of size 1x2).
     */
    public double[] transform(double[] point)  {
        final double cosRot = Math.cos(this.params[ROT]);
        final double sinRot = Math.sin(this.params[ROT]);
        return new double[] {
            cxDst
                    + this.params[SCALEX] * cosRot * (point[0]-this.cxSrc)
                    - this.params[SCALEY] * sinRot * (point[1]-this.cySrc),
                    cyDst
                    + this.params[SCALEX] * sinRot * (point[0]-this.cxSrc)
                    + this.params[SCALEY] * cosRot * (point[1]-this.cySrc)};
    }
    
    /**
     * Transform an array of points from the coordinate system of the source set
     * to the coordinate system of destination set.
     * The transformed points overwrite the original values in the points[] array.
     * @param points The point to be transformed.
     * @param xid The column of points[] containing the x-coordinate.
     * @param yid The column of points[] containing the y-coordinate.
     */
    public void transform(double[][] points, int xid, int yid) {
        final double cosRot = Math.cos(this.params[ROT]);
        final double sinRot = Math.sin(this.params[ROT]);
        
        for (int i = 0; i < points.length; i++) {
            final double xSrc = points[i][xid] - this.cxSrc;
            final double ySrc = points[i][yid] - this.cySrc;
            points[i][xid] = cxDst
                    + this.params[SCALEX] * cosRot * xSrc
                    - this.params[SCALEY] * sinRot * ySrc;
            points[i][yid] = cyDst
                    + this.params[SCALEX] * sinRot * xSrc
                    + this.params[SCALEY] * cosRot * ySrc;
        }
    }
    
    /**
     * Return the tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     * @return The tolerance value.
     */
    public double getParameterTolerance() {
        return parameterTolerance;
    }
    
    /**
     * Sets the tolerance that is used to determine whether a new improvement for the
     * parameters is small enough to stop the computations.
     * @param parameterTolerance The tolerance value.
     */
    public void setParameterTolerance(double parameterTolerance) {
        this.parameterTolerance = parameterTolerance;
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
        return null;
    }
}
