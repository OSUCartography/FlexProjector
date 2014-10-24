/*
 * VEstimator.java
 *
 * Created on March 26, 2005, 5:15 PM
 */

package ika.transformation.robustestimator;

/**
 * V-estimator as developed by D. Beineke in:<br>
 * Beineke, D. 2001. Verfahren zur Genauigkeitsanalyse fuer Altkarten. Universitaet
 * der Bundeswehr, Muenchen.<br>
 * 
 * The V-estimator has been developed for the analysis of old maps. It uses two
 * tuning contants: k and e.<br>
 * k is similar to k of the Huber estimator.<br>
 * e is the degree of contamination in 0 .. 1<br>
 * e in 0.0 .. 0.3: low contamination<br>
 * e in 0.3 .. 0.7: medium contamination<br>
 * e in 0.7 .. 1.0: high contamination.<br>
 * The contamination constant c is derived from k and e:<br>
 * c = k * e<br>
 * with c in 0 .. k<br>
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class VEstimator extends RobustEstimator implements java.io.Serializable {
    
    static final long serialVersionUID = 7940786881681576077L;
        
    /**
     * The degree of contamination e in 0 .. 1
     */
    private double e = 0.6;
    
    private double minE = 0.0;
    
    private double maxE = 1.0;
    
    /**
     * The tuning constant k.
     */
    private double k = 1.5;
    
    private double minK = 0.0;
    
    private double maxK = 4.0;
    
    /** Creates a new instance of VEstimator */
    public VEstimator(){
    }
    
    /**
     * Weight function of the estimator
     * @param u residual scaled by s.
     * @return The weight for u.
     */
    public final double w(double u) {
        final double u_abs = Math.abs(u);
        if (u_abs < k)
            return 1;
        else {
            final double c = e * k;
            return (k - c) / (u_abs - c);
        }
    }
    
    /**
     * Returns the degree of contamination e.
     * @return The degree of contamination e.
     */
    public double getE() {
        return e;
    }
    
    /**
     * Sets the degree of contamination.
     * @param e The degree of contamination.
     */
    public void setE(double e) {
        this.e = e;
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
     * @param k The tuning constant k.
     */
    
    public void setK(double k) {
        this.k = k;
    }
    
    /**
     * Returns a description of the estimator.
     * The description does not contain any numerical values.
     * @return The description.
     */
    public String getDescription() {
        String str = "Robust V-Estimator\n";
        str += "Split Point k\n";
        str += "Contamination: 0..1\n";
        return str;
    }
    
    /**
     * Returns the name of this robust estimator.
     * @return Returns a String
     */
    public String getName(){
        String str = "V Estimator";
        return str;
    }
    
     /**
     * Returns a description of the estimator.
     * The description does contain numerical values.
     * @return The description.
     */
    public String getDescriptionOfValues() {
        String str = ("Split Point k:\t");
        str += this.k + "\n";
        str += ("Degree of contamination e:\t");
        str += this.e + "\n";
        return str;
    }

    public double getMinE() {
        return minE;
    }

    public double getMaxE() {
        return maxE;
    }

    public double getMinK() {
        return minK;
    }

    public double getMaxK() {
        return maxK;
    }
}
