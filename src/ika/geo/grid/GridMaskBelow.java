/*
 * 
 * 
 */

package ika.geo.grid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMaskBelow implements GridMask {

    private GaussianPyramid elevationPyramid;
    private float threshold;
    private float margin;
    
    public GridMaskBelow (GaussianPyramid elevationPyramid, float threshold, float margin) {
        this.elevationPyramid = elevationPyramid;
        this.threshold = threshold;
        this.margin = margin;
    }
    
    public final float getWeight(int col, int row, int pyramidLevel) {
        float v = this.elevationPyramid.getValue(col, row, pyramidLevel);
        if (v < threshold - margin) {
            return 1f;
        } else if (v > threshold + margin) {
            return 0f;
        } else if (margin > 0) {
            return (v - threshold + margin) * (-1.f / (2f * margin));
        }
        return 1f;
    }

    public float getThreshold() {
        return threshold;
    }

    public float getMargin() {
        return margin;
    }

}
