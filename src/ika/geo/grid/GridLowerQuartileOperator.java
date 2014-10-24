/*
 * GridMedianOperator.java
 *
 * Created on August 12, 2008
 *
 */

package ika.geo.grid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridLowerQuartileOperator extends GridPercentileFilterOperator {
    
    public GridLowerQuartileOperator() {
    }
    
    public GridLowerQuartileOperator(int filterSize) {
        super(filterSize);
    }

    public String getName() {
        return "Lower Quartile";
    }

    @Override
    protected final float percentile(float[] values) {
        return ika.utils.Median.lowerQuartile(values);
    }

}
