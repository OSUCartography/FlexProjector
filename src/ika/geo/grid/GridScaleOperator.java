package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Multiply a grid by a constant factor.
 * @author jenny
 */
public class GridScaleOperator extends ThreadedGridOperator {
    
    private float scale;

    public GridScaleOperator() {
        this.scale = 1;
    }
    
    public GridScaleOperator(float scale) {
        this.scale = scale;
    }
    
    public String getName() {
        return "Scale";
    }

    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int nCols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow = srcGrid[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < nCols; ++col) {
                dstRow[col] = srcRow[col] * scale;
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
