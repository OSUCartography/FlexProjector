/*
 * RobustEstimator.java
 *
 * Created on March 26, 2005, 5:14 PM
 */

package ika.transformation.robustestimator;

/**
 * Base class for robust estimators used for robust estimation.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public abstract class RobustEstimator implements java.io.Serializable {
    
    static final long serialVersionUID = -1792827173460595144L;
    
    /**
     * weight function of the estimator
     * @param u residual scaled by s.
     * s is the median of vi.
     * @return The weight for u.
     */
    public abstract double w (double u);
    /**
     * Returns a description of the estimator.
     * The description does not contain any numerical values.
     * @return The description.
     */
    public abstract String getDescription();
    /**
     * Returns a description of the estimator.
     * The description does contain numerical values.
     * @return The description.
     */
    public abstract String getDescriptionOfValues();
    
    public RobustEstimator(){
    }
    
    /**
     * Returns the name of the robust estimator.
     * @return Returns a String
     */
    public abstract String getName();
    
    /**
     * Computes sigma 0<br>
     * Formula: Beineke, D. (2001). Verfahren zur Genauigkeitsanalyse für Altkarten.
     * page 102.
     * @param v array of residuals
     * @param s s: MAD = median of the absolute deviations from the median
     * s = median(abs(vi))
     * @return sigma 0
     */
    public double getSigma0 (double[] v, double s) {
        if (v.length < 1)
            throw new IllegalArgumentException();
        
        double wtot = 0;
        double wu_square_tot = 0;
        for (int i = 0; i < v.length; i++) {
            final double u = v[i]/s;
            final double w = this.w(u);
            wu_square_tot += w*w*u*u;
            wtot += w;
        }
        
        // avoid division by 0
        if (wtot == 0.)
            return 0;
        
        return s*v.length/wtot*Math.sqrt(wu_square_tot/(v.length-1))
        /**Math.sqrt((v.length-1)/(v.length-4))*/;
    }
}
