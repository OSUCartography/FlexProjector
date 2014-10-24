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
public class GridUpperQuartileOperator extends GridPercentileFilterOperator {

    public GridUpperQuartileOperator() {
    }

    public GridUpperQuartileOperator(int filterSize) {
        super(filterSize);
    }

    public String getName() {
        return "Upper Quartile";
    }

    @Override
    protected final float percentile(float[] values) {
        return ika.utils.Median.upperQuartile(values);
    }
}
