package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.grid.ThreadedGridOperator;

/**
 * Fills the destination grid with NaN values where the source grid has NaN
 * values. Other cells are set to 0. 
 * @author jenny
 */
public class GridExtractMaskOperator extends ThreadedGridOperator{

    @Override
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {
        final int ncols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow = src.getGrid()[row];
            float[] dstRow = dst.getGrid()[row];
            for (int col = 0; col < ncols; ++col) {
                dstRow[col] = Float.isNaN(srcRow[col]) ? Float.NaN : 0;
            }
        }
    }

    @Override
    public String getName() {
        return "Extract NaN Mask";
    }
    
}
