/*
 * GridScaleNegativeOperator.java
 *
 * Created on February 2, 2006, 8:33 PM
 *
 */
package ika.geo.grid;

import ika.geo.*;

/**
 * Scale negative values. Positive values are set to 0.
 * @author jenny
 */
public class GridScaleNegativeOperator extends ThreadedGridOperator {
    
    private float scale;

    public GridScaleNegativeOperator() {
        this.scale = 1;
    }
    
    public GridScaleNegativeOperator(float scale) {
        this.scale = scale;
    }
    
    @Override
    public String getName() {
        return "Scale Negative Values";
    }

    @Override
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int nCols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow = srcGrid[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < nCols; ++col) {
                final float in = srcRow[col];
                dstRow[col] = in > 0f ? 0f : in * scale;
            }
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}