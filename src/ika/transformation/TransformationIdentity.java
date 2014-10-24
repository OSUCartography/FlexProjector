/*
 * TransformationIdentity.java
 *
 * Created on November 27, 2005, 10:50 AM
 *
 */

package ika.transformation;

import java.text.*;
import java.io.*;
import ika.utils.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class TransformationIdentity extends Transformation implements Serializable{
    
    private static final long serialVersionUID = -1359315422767021373L;
    
    /**
     * Sigma 0 is the error per unit.
     */
    private double sigma0;
    
    /**
     * Creates a new instance of TransformationIdentity
     */
    public TransformationIdentity() {
    }
    
    public java.awt.geom.AffineTransform getAffineTransform() {
        // return identiy transform
        return new java.awt.geom.AffineTransform();
    }
    
    public String getName() {
        return "Identity";
    }
    
    public String getReport(boolean invert) {
        StringBuffer str = new StringBuffer(1024);
        str.append(this.getShortDescription());
        str.append("\n");
        return str.toString();
    }
    
    public double getRotation() {
        return 0;
    }
    
    public double getScale() {
        return 1;
    }
    
    public String getShortDescription() {
        StringBuffer str = new StringBuffer();
        str.append(this.getName());
        str.append("\n");
        str.append("The identity transformation is not changing coordinates\n");
        str.append("Use for special applications only when the old and the " +
                "new map share a common coordinate system.\n");
        return str.toString();
    }
    
    public String getShortReport(boolean invert) {
        StringBuffer str = new StringBuffer(1024);
        
        double scale = this.getScale(invert);
        str.append(NumberFormatter.formatScale("Scale", 1, true));
        str.append("\n");
        
        str.append(this.formatRotation("Rotation", 0));
        str.append("\n");
        
        str.append(this.formatSigma0(0));
        str.append("\n");
        str.append(this.formatStandardErrorOfPosition(0));
        str.append("\n");
        
        return str.toString();
    }
    
    public double getSigma0() {
        return this.sigma0;
    }
    
    protected void initWithPoints(double[][] destSet, double[][] sourceSet) {
        // Compute residuals v and sigma 0
        double vTv = 0;
        for (int i = 0; i < numberOfPoints; i++) {
            final double dx = sourceSet[i][0] - destSet[i][0];
            final double dy = sourceSet[i][1] - destSet[i][1];
            
            // copy residuals to this.v
            this.v[i][0] = dx;
            this.v[i][1] = dy;

            vTv += dx*dx + dy*dy;
        }
        this.sigma0 = Math.sqrt(vTv/(2.*this.numberOfPoints));
    }
    
    public double[] transform(double[] point) {
        return new double[]{point[0], point[1]};
    }  
}
