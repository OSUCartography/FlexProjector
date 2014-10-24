/*
 * TransformationWeightedHelmert.java
 *
 * Created on March 23, 2005, 12:10 PM
 */

package ika.transformation;

import java.io.*;

/**
 * Planar (2D) Weighted Helmert Transformation with four parameters:<br>
 * translation in horizontal and vertical direction, rotation
 * and scale factor. Important: This is not a general purpose implementation, but
 * has been optimized for speed for use by ika.mapanalyst.Isolines
 * This implementation does not retain the horizontal and vertical offsets.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class TransformationWeightedHelmert extends Transformation
        implements Serializable {
    
    private static final long serialVersionUID = 7956296477523422791L;
    
    double a1, a2;
    
    /**
     * Returns the name of the transformation.
     * @return The name.
     */
    public String getName() {
        return "Weighted Helmert";
    }
    
    /**
     * Returns a short description of this transformation.
     * getShortDescription can be called before the transformation is
     * initialized using initWithPoints
     * @return The description.
     */
    public String getShortDescription() {
        String str = "\nAn individual weight can be asigned to each point.";
        return str;
    }

    private double [][] ATP(double[][] srcPts, int nbrPts) {
        
        double[][] res = new double [4][nbrPts * 2];
        for (int c = 0, c_x_2 = 0; c < nbrPts; c++, c_x_2+=2) {
            final double w = srcPts[c][2];
            final double xw = srcPts[c][0] * w;
            final double yw = srcPts[c][1] * w;
            res[0][c_x_2] = res[1][c_x_2+1] = w;
            res[2][c_x_2] = xw;
            res[2][c_x_2+1] = yw;
            res[3][c_x_2] = -yw;
            res[3][c_x_2+1] = xw;
        }
        return res;
    }
    
    private double[][] ATP_x_A(double[][] ATP, double[][] srcPts, int nbrPts) {
        
        // ATPA is 4 x 4 big.
        double[][] res = new double[4][4];
        
        final int m = ATP[0].length;
        
        for (int r = 0; r < 4; r++) {
            double[] res_row = res[r];
            double[] ATP_row = ATP[r];
            
            for (int k = 0; k < m; k+=2) {
                res_row[0] += ATP_row[k];
                res_row[1] += ATP_row[k+1];
            }
            
            for (int k = 0; k < nbrPts; k++) {
                double[] srcPts_row = srcPts[k];
                double ptX = srcPts_row[0];
                double ptY = srcPts_row[1];
                res_row[2] += ATP_row[k*2] * ptX + ATP_row[k*2+1] * ptY;
                res_row[3] += - ATP_row[k*2] * ptY + ATP_row[k*2+1] * ptX;
            }
        }
        return res;
    }
    
    
    /* compute a1 and a2 of the transformation matrix.
     * Not the complete transformation matrix!
     */
    private double[] QATP_x_l_a1_a2(double[][] QATP, double[][] points) {
        final int n_half = QATP[0].length / 2;
        double[] res = new double[2];
        for (int row = 2; row < 4; ++row) {
            final double[] QATP_row = QATP[row];
            final int r = row - 2;
            for (int i = 0; i < n_half; ++i) {
                res[r] += QATP_row[i*2] * points[i][0]
                        + QATP_row[i*2+1] * points[i][1];
            }
        }
        return res;
    }
    
    /**
     * Initialize the transformation with two sets of control points.
     * The control points are not copied, nor is a reference to them
     * retained. Instead, the transformation parameters are
     * immmediately computed by initWithPoints.
     * @param destSet A two-dimensional array (nx2) containing the x and y
     * coordinates of the points of the destination set.
     * The destSet must be of exactly the same size as sourceSet.
     * @param sourceSet A two-dimensional array (nx3) containing the x and y
     * coordinates of the points of the source set, and the weight of each point.
     * The sourceSet must be of exactly the same size as destSet.
     */
    public final void initWithPoints(double[][] destSet, double[][] sourceSet, 
            int nbrPts, float[] scaleRot){
        
        // Compute transformation parameters
        final double[][] ATP = this.ATP(sourceSet, nbrPts);
        final double[][] N = this.ATP_x_A (ATP, sourceSet, nbrPts);
        final double[][] Q = ika.utils.MatrixUtils.invertMatrix4x4 (N);
        final double[][] QATP = ika.utils.MatrixUtils.matrix_x_matrix(Q, ATP);
        final double[] x = this.QATP_x_l_a1_a2(QATP, destSet);
        
        final float scale = (float)Math.sqrt(x[0]*x[0]+x[1]*x[1]);
        scaleRot[0] = scale;
       
        final float rotDegree = (float)Math.toDegrees(Math.atan2(x[1], x[0]));
        // Math.atan2 returns values in the range -pi..+pi
        scaleRot[1] = (rotDegree < 0.f) ? rotDegree + 360.f : rotDegree;
        
        // copy paramters to object variables
        this.a1 = x[0];
        this.a2 = x[1];
    }
    
    protected final void initWithPoints(double[][] destSet, double[][] sourceSet){
        float[] dummyScaleRot = new float[2];
        this.initWithPoints(destSet, sourceSet,  destSet.length, dummyScaleRot);
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
        return null;
    }
    
    public String getReport(boolean invert) {
        return "";
    }
    
    public final double getRotation() {
        double rot = Math.atan2(a2, a1);
        if (rot < 0.)
            rot += Math.PI * 2.;
        return rot;
    }
    
    public final double getScale() {
        return Math.sqrt(a1*a1+a2*a2);
    }
    
    public String getShortReport(boolean invert) {
        return "";
    }
    
    public double getSigma0() {
        return 0;
    }
    
    public final double[] transform(double[] point) {
        return new double[] {a1*point[0]-a2*point[1], a2*point[0]+a1*point[1]};
    }
}