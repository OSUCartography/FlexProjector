/*
 * TransformationHelmert.java
 *
 * Created on March 23, 2005, 12:10 PM
 */

package ika.transformation;

import java.text.*;
import java.io.*;
import ika.utils.*;

/**
 * Planar (2D) Helmert Transformation with four parameters:
 * translation in horizontal and vertical direction, rotation
 * and scale factor.
 *
 * X = cx + a1 * x + a2 * y
 * Y = cy - a2 * x + a1 * y
 *
 * @author Bernhard Jenny
 * Institute of Cartography
 * ETH Zurich
 */
public class TransformationHelmert extends Transformation implements Serializable {
    
    private static final long serialVersionUID = -8855315822705021323L;
    
    /**
     * Element 3 of the solution matrix x.
     * a1 = m * cos(alpha)
     * where m is the scale factor and alpha the rotation angle.
     */
    protected double a1;
    
    /**
     * Element 4 of the solution matrix x.
     * a2 = m * sin(alpha)
     * where m is the scale factor and alpha the rotation angle.
     */
    protected double a2;
    
    /**
     * Sigma 0 is the error per unit.
     */
    protected double sigma0;
    
    /**
     * Sigma of the translation parameters. Horizontally and vertically equal.
     */
    protected double transSigma;
    
    /**
     * The sigma of the scale parameter.
     */
    protected double scaleSigma;
    
    /**
     * The sigma of the rotation parameter.
     */
    protected double rotSigma;
    
    /**
     * x coordinate of center of gravity of the destination point set.
     */
    protected double cxDst;
    
    /**
     * y coordinate of center of gravity of the destination point set.
     */
    protected double cyDst;
    
    /**
     * x coordinate of center of gravity of the source point set.
     */
    protected double cxSrc;
    
    /**
     * y coordinate of center of gravity of the source point set.
     */
    protected double cySrc;
    
    /**
     * Returns the name of the transformation.
     * @return The name.
     */
    public String getName() {
        return "Helmert (4 Parameters)";
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
        str.append("alpha: Rotation in Counter-Clockwise Direction\n");
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
        
        
        str.append("Parameters computed with ");
        str.append(this.getNumberOfPoints());
        str.append(" points.\n\n");
        
        str.append("x0 Translation Horizontal [m]:                     ");
        str.append(formatPrecise(this.getTranslationX()));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getTranslationXSigma()));
        str.append("\n");
        str.append("y0 Translation Vertical [m]:                       ");
        str.append(formatPrecise(this.getTranslationY()));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getTranslationYSigma()));
        str.append("\n");
        
        final double scale = this.getScale(invert);
        str.append("m Scale Factor:                                    ");
        str.append(formatPrecise(scale));
        str.append(" +/-");
        str.append(formatPreciseShort(this.getScaleSigma(invert)));
        str.append("\n");
        
        str.append("alpha Rotation: [deg ccw]                          ");
        str.append(formatPrecise(Math.toDegrees(this.getRotation())));
        str.append(" +/-");
        str.append(formatPreciseShort(Math.toDegrees(this.getRotationSigma())));
        str.append("\n\n");
        
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
        
        double scale = this.getScale(invert);
        str.append(NumberFormatter.formatScale("Scale", scale, true));
        str.append("\n");
        
        str.append(this.formatRotation("Rotation", this.getRotation(invert)));
        str.append("\n");
        
        if (!invert)
            scale = 1;
        str.append(this.formatSigma0(this.getSigma0() * scale));
        str.append("\n");
        str.append(this.formatStandardErrorOfPosition(this.getStandardErrorOfPosition() * scale));
        str.append("\n");
        
        return str.toString();
    }
    
    /**
     * Returns a1 = m * cos(alpha)
     * @return a1
     */
    protected double getA1() {
        return a1;
    }
    
    /**
     * Returns a2 = m * sin(alpha)
     * @return a2
     */
    protected double getA2() {
        return a2;
    }
    
    /**
     * Returns the rotation used by this transformation.
     * @return The rotation.
     */
    public double getRotation()  {
        double rot = Math.atan2(a2, a1);
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
        return Math.sqrt(a1*a1+a2*a2);
    }
    
    /**
     * Returns the precision of the scale factor.
     * @return The precision of the scale factor.
     */
    public double getScaleSigma(boolean invert) {
        if (invert) {
            final double scale = this.getScale();
            return this.scaleSigma / (scale * scale);
        }
        return this.scaleSigma;
    }
    
    /**
     * Returns the horizontal translation paramater
     * used by this transformation.
     * @return The horizontal translation.
     */
    public double getTranslationX() {
        return this.cxDst - this.a1*this.cxSrc + this.a2*this.cySrc;
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
        return this.cyDst - this.a2*this.cxSrc - this.a1*this.cySrc;
    }
    
    /**
     * Returns the precision of the vertical translation
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
     * Initialize the transformation with two sets of control points.
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
    protected void initWithPoints(double[][] destSet, double[][] sourceSet){
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
        
        // compute a1 and a2
        double sumX1_times_x2 = 0;
        double sumY1_times_y2 = 0;
        double sumx2_times_x2 = 0;
        double sumy2_times_y2 = 0;
        double sumY1_times_x2 = 0;
        double sumX1_times_y2 = 0;
        for (int i = 0; i < this.numberOfPoints; i++) {
            final double x2 = sourceSet[i][0] - this.cxSrc;
            final double y2 = sourceSet[i][1] - this.cySrc;
            sumX1_times_x2 += destSet[i][0] * x2;
            sumY1_times_y2 += destSet[i][1] * y2;
            sumx2_times_x2 += x2 * x2;
            sumy2_times_y2 += y2 * y2;
            sumY1_times_x2 += destSet[i][1] * x2;
            sumX1_times_y2 += destSet[i][0] * y2;
        }
        this.a1 = (sumX1_times_x2+sumY1_times_y2)/(sumx2_times_x2+sumy2_times_y2);
        this.a2 = (sumY1_times_x2-sumX1_times_y2)/(sumx2_times_x2+sumy2_times_y2);  
        
        // Compute residuals v and sigma 0
        double vTv = 0;
        for (int i = 0; i < numberOfPoints; i++) {
            final double x_red = sourceSet[i][0] - this.cxSrc;
            final double y_red = sourceSet[i][1] - this.cySrc;
            final double x_trans = a1 * x_red - a2 * y_red + this.cxDst;
            final double y_trans = a2 * x_red + a1 * y_red + this.cyDst;
            final double dx = x_trans - destSet[i][0];
            final double dy = y_trans - destSet[i][1];
            
            // copy residuals to this.v
            this.v[i][0] = dx;
            this.v[i][1] = dy;

            vTv += dx*dx + dy*dy;
        }
        this.sigma0 = Math.sqrt(vTv/(2.*this.numberOfPoints-4));
        this.scaleSigma = sigma0 * Math.sqrt(1./(sumx2_times_x2+sumy2_times_y2));
        // formula: P. Howald Theorie des Erreurs, EPFL, p. 9.11.
        this.transSigma = sigma0 * Math.sqrt(1./numberOfPoints + 
                (cxSrc*cxSrc+cySrc*cySrc) / (sumx2_times_x2+sumy2_times_y2));
        this.rotSigma = scaleSigma / this.getScale();
    }
    
    /**
     * Transform a point from the coordinate system of the source set
     * to the coordinate system of destination set.
     * @return The transformed coordinates (array of size 1x2).
     * @param point The point to be transformed (array of size 1x2).
     */
    public double[] transform(double[] point)  {
        double[] pointTransformed = new double[2];
        final double x_red = point[0] - this.cxSrc;
        final double y_red = point[1] - this.cySrc;
        pointTransformed[0] = a1*x_red-a2*y_red+this.cxDst;
        pointTransformed[1] = a2*x_red+a1*y_red+this.cyDst;
        return pointTransformed;
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
            points[i][xid] = this.a1*xSrc-this.a2*ySrc+this.cxDst;
            points[i][yid] = this.a2*xSrc+this.a1*ySrc+this.cyDst;
        }
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
        java.awt.geom.AffineTransform trans = new java.awt.geom.AffineTransform();
        trans.setTransform(a1, -a2, cxDst-cxSrc, a2, a1, cyDst-cySrc);
        return trans;
    }
}
