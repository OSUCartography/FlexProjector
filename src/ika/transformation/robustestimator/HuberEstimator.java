/*
 * HuberEstimator.java
 *
 * Created on April 5, 2005, 8:40 AM
 */

package ika.transformation.robustestimator;

/**
 * Robust estimator according to Huber
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class HuberEstimator extends RobustEstimator implements java.io.Serializable {
    
    private static final long serialVersionUID = 7601561936358246963L;
    
    /**
     * Tuning constant k. Estimates the qualtiy of the dataset.<br>
     * As with the value of the standard deviation:<br>
     * k = 1: 66% of all points are compensated with least squares estimation, <br>
     * the error on the other 34% has a lesser wheight.
     * k = 3: 99.5%
     */
    private double k = 1.5;
    
    private double minK = 0.0;
    
    private double maxK = 4.0;
    
    /** Creates a new instance of HuberEstimator */
    public HuberEstimator() {
    }
    
    /**
     * Returns a description of the estimator.
     * The description does not contain any numerical values.
     * @return The description.
     */
    public String getDescription() {
        String str = "Robust Huber-Estimator\n";
        str += "Split Point k\n";
        return str;
    }
    
    /**
     * Returns the name of this robust estimator.
     * @return Returns a String
     */
    public String getName(){
        String str = "Huber Estimator";
        return str;
    }
    
    /**
     * Returns a description of the estimator.
     * The description does contain numerical values.
     * @return The description.
     */
    public String getDescriptionOfValues() {
        return "Split Point k:\t" + this.k + "\n";
    }
    
    /**
     * weight function of the estimator
     * @param u residual scaled by s.
     * @return The weight for u.
     */
    public double w(double u) {
        final double u_abs = Math.abs(u);
        return u_abs < this.k ? 1. : this.k / u_abs;
    }

    /**
     * Returns the tuning constant k.
     * @return The tuning constant k.
     */
    public double getK() {
        return k;
    }

    /**
     * Sets the tuning constant k.
     * @param k The new value of the tuning constant k.
     */
    public void setK(double k) {
        if (k <= 0.)
            throw new IllegalArgumentException();
        this.k = k;
    }
    
       public String getShortReport(){
           String str = "Huber Estimator\n";
           str += this.getK();
           return str;
       }

    public double getMinK() {
        return minK;
    }

    public double getMaxK() {
        return maxK;
    }
}
