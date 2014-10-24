package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Computes the absolute values in a grid.
 * @author jenny
 */
public class GridAbsoluteOperator extends ThreadedGridOperator {

    public String getName() {
        return "Absolute";
    }

    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int nCols = src.getCols();

        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow = srcGrid[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < nCols; ++col) {
                dstRow[col] = Math.abs(srcRow[col]);
            }
        }
    }
}
