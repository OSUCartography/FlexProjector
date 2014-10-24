/*
 * TransformationAffine6
 *
 * Created on March 23, 2005, 12:10 PM
 */

package ika.transformation;

import Jama.*;
import java.text.*;
import java.io.*;
import ika.utils.*;

/**
 * Affine Transformation with 6 parameters:
 * translation in horizontal and vertical direction, two rotation angles
 * and two scale factors.
 * Transformation:
 * Xi = X0 + a1*xi + a2*yi
 * Yi = Y0 + b1*xi + b2*yi
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */

public class TransformationAffine6 extends Transformation implements Serializable {
    
    private static final long serialVersionUID = -7509256281043772488L;
    
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
     * a1 = mx*cos(alpha)
     */
    protected double a1;
    
    /**
     * a2 = -my*sin(beta)
     */
    protected double a2;
    
    /**
     * a3 = mx*sin(alpha)
     */
    protected double a3;
    
    /**
     * a4 = my*cos(beta)
     */
    protected double a4;
    
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
     * The sigma of the x rotation parameter.
     */
    private double rotXSigma;
    
    /**
     * The sigma of the y rotation parameter.
     */
    private double rotYSigma;
    
    /**
     * Returns the name of the transformation.
     * @return The name.
     */
    public String getName() {
        return "Affine (6 Parameters)";
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
        str.append("6 Parameters:\n");
        
        str.append("X = x0 + mx*cos(alpha)*x - my*sin(beta)*y\n");
        str.append("Y = y0 + mx*sin(alpha)*x + my*cos(beta)*y\n");
        str.append("a1 = mx*cos(alpha)\n");
        str.append("a2 = -my*sin(beta)\n");
        str.append("a3 = mx*sin(alpha)\n");
        str.append("a4 = my*cos(beta)\n");
   
        str.append("x0:    Horizontal Translation\n");
        str.append("y0:    Vertical Translation\n");
        str.append("mx:    Horizontal Scale Factor\n");
        str.append("my:    Vertical Scale Factor\n");
        str.append("alpha: Rotation in Counter-Clockwise Direction for Horizontal Axis.\n");
        str.append("beta:  Rotation in Counter-Clockwise Direction for Vertical Axis.\n");
        return str.toString();
    }
    
    /**
     * Returns a report containing the computed parameters of this
     * transformation.9
     * @return The description.
     */
    public String getReport(boolean invert) {
        StringBuffer str = new StringBuffer(1024);
        str.append(this.getShortDescription());
        str.append("\n");
        
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
        str.append("alpha Horizontal Rotation [deg ccw]:               ");
        str.append(formatPrecise(Math.toDegrees(this.getRotationX(invert))));
        str.append(" +/-");
        str.append(formatPreciseShort(Math.toDegrees(this.getRotationXSigma())));
        str.append("\n");
        str.append("beta Vertical Rotation [deg ccw]:                  ");
        str.append(formatPrecise(Math.toDegrees(this.getRotationY(invert))));
        str.append(" +/-");
        str.append(formatPreciseShort(Math.toDegrees(this.getRotationYSigma())));
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
        
        str.append(NumberFormatter.formatScale("Scale Hor.", this.getScaleX(invert)));
        str.append("\n");
        
        str.append(NumberFormatter.formatScale("Scale Vert.", this.getScaleY(invert)));
        str.append("\n");
        
        str.append (this.formatRotation("Rotation X", this.getRotationX(invert)));
        str.append("\n");
        
        str.append (this.formatRotation("Rotation Y", this.getRotationY(invert)));
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
    public double getRotationX(boolean invert) {
        double rotX = Math.atan2(a3, a1);
        if (rotX < 0.)
            rotX += Math.PI * 2.;
        return invert ? -rotX : rotX;
    }
    
    /**
     * Returns the precision of the horizontal rotation.
     * @return The precision.
     */
    public double getRotationXSigma() {
        return this.rotXSigma;
    }
    
    /**
     * Returns the vertical rotation used by this transformation.
     * @return The rotation.
     */
    public double getRotationY(boolean invert) {
        double rotY = Math.atan2(-a2, a4);
        if (rotY < 0.)
            rotY += Math.PI * 2.;
        return invert ? -rotY : rotY;
    }
    
    public double getRotation() {
        double rot = (this.getRotationX(false) + this.getRotationY(false)) / 2.;
        if (rot < 0.)
            rot += Math.PI * 2.;
        if (rot > Math.PI * 2.)
            rot -= Math.PI * 2.;
        return rot;
    }
    
    /**
     * Returns the precision of the vertical rotation.
     * @return The precision.
     */
    public double getRotationYSigma() {
        return this.rotYSigma;
    }
    
    public double getScale() {
        return (this.getScaleX(false) + this.getScaleY(false)) / 2.;
    }
    
    /**
     * Returns the scale factor in horizontal direction used by this transformation.
     * @return The scale factor.
     */
    public double getScaleX(boolean invert) {
        final double scale = Math.sqrt(a1*a1+a3*a3);
        return invert ? 1. / scale : scale;
    }
    
    /**
     * Returns the precision of the scale factor in horizontal direction .
     * @return The precision of the scale factor.
     */
    public double getScaleXSigma(boolean invert) {
        if (invert) {
            final double scaleX = getScaleX(false);
            return this.scaleXSigma/(scaleX*scaleX);
        }
        return this.scaleXSigma;
    }
    
    /**
     * Returns the scale factor in vertical direction used by this transformation.
     * @return The scale factor.
     */
    public double getScaleY(boolean invert) {
        final double scale = Math.sqrt(a2*a2+a4*a4);
        return invert ? 1. / scale : scale;
    }
    
    /**
     * Returns the precision of the scale factor in vertical direction .
     * @return The precision of the scale factor.
     */
    public double getScaleYSigma(boolean invert) {
        if (invert) {
            final double scaleY = getScaleY(false);
            return this.scaleYSigma/(scaleY*scaleY);
        }
        return this.scaleYSigma;
    }
    
    /**
     * Returns the horizontal translation paramater
     * used by this transformation
     * @return The horizontal translation.
     */
    public double getTranslationX() {
        return this.cxDst - this.a1*this.cxSrc - this.a2*this.cySrc;
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
        return this.cyDst - this.a3*this.cxSrc - this.a4*this.cySrc;
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
     * @return sigma 0
     */
    public double getSigma0() {
        return this.sigma0;
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
                
        /* Transformation:
         * Xi = X0 + a1*xi + a2*yi
         * Yi = Y0 + b1*xi + b2*yi
         * Parameters X0, a1, a2 for X can be computed independently of
         * the parameters Y0, b1, b2 for Y. This reduces the sizes of the
         * matrices to invert and multiply, and thereby accelerates computation
         * (acceleration for 300 points approximately by factor 2).
         * Solve two equation systems.
         * x = Aa
         * y = Ab
         * where:
         *
         *     |X1|
         * x = |..|
         *     |Xn|
         *
         *     |Y1|
         * y = |..|
         *     |Yn|
         *
         *     |1 x1 y1|
         * A = |. .  . |
         *     |1 xn yn|
         *
         *     |X0|
         * a = |a1|
         *     |a2|
         *
         *     |Y0|
         * b = |b1|
         *     |b2|
         *
         * improvements u and w:
         * u = Aa-x
         * w = Aa-y
         *
         * solution for a and b:
         * a = (ATA)'ATx
         * b = (ATA)'ATy
         *
         * Estimation of precision:
         * sigma0 = sqrt((uTu+wTw)/(2n-6)) with n = number of points
         *
         * For more details see:
         * Beineke, D. (2001). Verfahren zur Genauigkeitsanalyse für Altkarten.
         */
        
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
        
        // allocate matrices x, y, and A.
        double[][] xArray = new double [numberOfPoints][1];
        Matrix mat_x = new Matrix(xArray, numberOfPoints, 1);
        double[][] yArray = new double [numberOfPoints][1];
        Matrix mat_y = new Matrix(yArray, numberOfPoints, 1);
        double[][] AArray = new double [numberOfPoints][3];
        Matrix mat_A = new Matrix(AArray, numberOfPoints, 3);
        
        // initialize matrices x, y, and A.
        for (int i = 0; i < numberOfPoints; i++) {
            xArray[i][0] = destSet[i][0] - cxDst;
            yArray[i][0] = destSet[i][1] - cyDst;
            AArray[i][0] = 1.;
            AArray[i][1] = sourceSet[i][0] - cxSrc;
            AArray[i][2] = sourceSet[i][1] - cySrc;
        }
        
        Matrix mat_Atrans = mat_A.transpose();
        Matrix mat_Q = mat_Atrans.times(mat_A).inverse();
        Matrix mat_a = mat_Q.times(mat_Atrans.times(mat_x));
        Matrix mat_b = mat_Q.times(mat_Atrans.times(mat_y));
        
        // Compute residuals u, w, vTv and Sigma aposteriori (sigma0).
        Matrix mat_u = mat_A.times(mat_a).minus(mat_x);
        Matrix mat_w = mat_A.times(mat_b).minus(mat_y);
        
        final double vTv = this.vTv(mat_u) + this.vTv(mat_w);
        final double sigma0square= vTv / (2. * numberOfPoints - 6);
        this.sigma0 = Math.sqrt(sigma0square);
        
        // copy residuals to this.v
        final double[][] v_x = mat_u.getArray();
        final double[][] v_y = mat_w.getArray();
        for (int i = 0; i < numberOfPoints; i++) {
            this.v[i][0] = v_x[i][0];
            this.v[i][1] = v_y[i][0];
        }
        
        // copy paramters to instance variables
        this.a1 = mat_a.get(1,0);
        this.a2 = mat_a.get(2,0);
        this.a3 = mat_b.get(1,0);
        this.a4 = mat_b.get(2,0);
        
        final double s1 = Math.sqrt(sigma0square * mat_Q.get(0,0));
        final double s2 = Math.sqrt(sigma0square * mat_Q.get(1,1));
        final double s3 = Math.sqrt(sigma0square * mat_Q.get(2,2));
        
        this.transSigma = s1;
        this.scaleXSigma = s2;
        this.scaleYSigma = s3;
        this.rotXSigma = s2 / this.getScaleX(false);
        this.rotYSigma = s3 / this.getScaleY(false);
        
        /* Computation with a single system of equations.
         * Matrices to transpose, multiply and inverse are bigger and
         * computation is therefore slower.
         */
        /*
        // Construct l matrix with points of destination set.
        double[][] lArray = new double [this.numberOfPoints*2][1];
        for (int i = 0; i < this.numberOfPoints; i++) {
            lArray[i*2][0] = destSet[i][0];
            lArray[i*2+1][0] = destSet[i][1];
        }
        Matrix matrix_l = new Matrix(lArray,this.numberOfPoints*2, 1);
         
        // Construct A matrix.
        Matrix matrix_A = new Matrix(this.numberOfPoints*2, 6, 0.);
        for (int i = 0; i < this.numberOfPoints; i++) {
            double [][]a ={
                {1., 0., sourceSet[i][0], -sourceSet[i][1], 0., 0.},
                {0., 1., 0., 0., sourceSet[i][0], sourceSet[i][1]}
            };
            matrix_A.setMatrix(i*2, i*2+1, 0, 5, new Matrix(a, 2, 6));
        }
         
        // Compute transformation parameters
        Matrix matrix_Atrans = matrix_A.transpose();
        Matrix matrix_Q = matrix_Atrans.times(matrix_A).inverse();
        Matrix matrix_x = matrix_Q.times(matrix_Atrans.times(matrix_l));
         
         
        // Compute v: Residuals
        Matrix v = matrix_A.times(matrix_x).minus(matrix_l);
         
        // Sigma aposteriori (sigma0)
        Matrix matrix_vTv = v.transpose().times(v);
        this.sigma0 = Math.sqrt(matrix_vTv .get(0,0)/(2.*this.numberOfPoints-6));
         
        // copy paramters to object variables
        this.X0 = matrix_x.get(0,0);
        this.Y0 = matrix_x.get(1,0);
        this.a1 = matrix_x.get(2,0);
        this.a2 = matrix_x.get(3,0);
        this.a3 = matrix_x.get(4,0);
        this.a4 = matrix_x.get(5,0);
         **/
    }
    
    /**
     * Transform a point from the coordinate system of the source set
     * to the coordinate system of destination set.
     * @return The transformed coordinates (array of size 1x2).
     * @param point The point to be transformed (array of size 1x2).
     */
    public double[] transform(double[] point) {
        final double xSrc = point[0] - this.cxSrc;
        final double ySrc = point[1] - this.cySrc;
        return new double[] {
            a1 * xSrc + a2 * ySrc + cxDst,
                    a3 * xSrc + a4 * ySrc + cyDst};
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
        for (int i = 0; i < points.length; i++) {
            final double xSrc = points[i][xid] - this.cxSrc;
            final double ySrc = points[i][yid] - this.cySrc;
            points[i][xid] = a1 * xSrc + a2 * ySrc + cxDst;
            points[i][yid] = a3 * xSrc + a4 * ySrc + cyDst;
        }
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
       return null; 
    }
}
