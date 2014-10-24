/*
 * HampelEstimator.java
 *
 * Created on April 18, 2005, 6:40 PM
 */

package ika.transformation.robustestimator;



/**
 * Robust Hampel estimator
 * @author Bernhard Jenny<br>
 * Institute of Cartography<br>
 * ETH Zurich<br>
 */
public class HampelEstimator extends RobustEstimator implements java.io.Serializable {
    
    private static final long serialVersionUID = -4205700373276056184L;
    
    /**
     * Parameter a
     */
    private double a = 1;
    
    private double minA = 0.0;
    
    private double maxA = 4.0;
    
    /**
     * Parameter b
     */
    private double b = 2;
    
    private double minB = 1.0;
    
    private double maxB = 6.0;
        
    /**
     * Parameter c
     */
    private double c = 4;
    
    private double minC = 2.0;
    
    private double maxC = 12.0;
    
    /** Creates a new instance of HampelEstimator */
    public HampelEstimator() {
    }
    
    /**
     * Returns a description of the estimator.
     * The description does not contain any numerical values.
     * @return The description.
     */
    public String getDescription() {
        String str = "Hampel Estimator\n";
        str += "Parameters a, b, c";
        return str;
    }
    
    /**
     * Returns the name of this robust estimator.
     * @return Returns a String
     */
    public String getName(){
        String str = "Hampel Estimator";
        return str;
    }
    
    /**
     * Returns a description of the estimator.
     * The description does contain numerical values.
     * @return The description.
     */
    public String getDescriptionOfValues() {
        String str = "a:\t" + a;
        str += " b:\t" + b;
        str += " c:\t" + c;
        return str;
    }
    
    /**
     * weight function of the estimator
     * @param u residual scaled by s.
     * @return The weight for u.
     */
    public double w(double u) {
        final double uabs = Math.abs(u);
        if (uabs < a)
            return 1;
        if (uabs < b)
            return a / uabs;
        if (uabs < c)
            return a/uabs * c/(c-b)-a/(c-b);
        return 0;
    }
    
    /**
     * Get parameter a
     * @return Parameter a.
     */
    public double getA() {
        return a;
    }
    
    /**
     * Set parameter a
     * @param a Parameter a.
     */
    public void setA(double a) {
        this.a = a;
    }
    
    /**
     * Get parameter b
     * @return Parameter b.
     */
    public double getB() {
        return b;
    }
    
    /**
     * Set parameter b
     * @param b Parameter b.
     */
    public void setB(double b) {
        this.b = b;
    }
    
    /**
     * Get parameter c
     * @return Parameter c.
     */
    public double getC() {
        return c;
    }
    
    /**
     * Set parameter c
     * @param c Parameter c.
     */
    public void setC(double c) {
        this.c = c;
    }

    public double getMinA() {
        return minA;
    }

    public double getMaxA() {
        return maxA;
    }

    public double getMinB() {
        return minB;
    }

    public double getMaxB() {
        return maxB;
    }

    public double getMinC() {
        return minC;
    }

    public double getMaxC() {
        return maxC;
    }
    
}
