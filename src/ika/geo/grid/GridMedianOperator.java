/*
 * GridMedianOperator.java
 *
 * Created on February 14, 2006, 9:05 PM
 *
 */

package ika.geo.grid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMedianOperator extends GridPercentileFilterOperator{
   
    /** Creates a new instance of GridMedianOperator */
    public GridMedianOperator() {
    }
    
    /** Creates a new instance of GridMedianOperator */
    public GridMedianOperator(int filterSize) {
        super(filterSize);
    }

    public String getName() {
        return "Median";
    }

    @Override
    protected final float percentile(float[] values) {
        return ika.utils.Median.median(values, false);
    }
    
}
