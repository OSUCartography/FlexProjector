package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Changes all void values of a grid to a specific value.
 * @author jenny
 */
public class GridChangeVoidOperator extends ThreadedGridOperator{
    
    private float v;

    public GridChangeVoidOperator() {
        this.v = 0;
    }
    
    public GridChangeVoidOperator(float v) {
        this.v = v;
    }
    
    public String getName() {
        return "Change Void";
    }

    @Override
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {
        final int ncols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow = src.getGrid()[row];
            float[] dstRow = dst.getGrid()[row];
            for (int col = 0; col < ncols; ++col) {
                dstRow[col] = Float.isNaN(srcRow[col]) ? v : srcRow[col];
            }
        }
    }
    
}
