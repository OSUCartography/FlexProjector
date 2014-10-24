package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Copy a grid.
 * @author jenny
 */
public class GridCopyOperator extends ThreadedGridOperator {

    @Override
    protected void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {
        for (int row = startRow; row < endRow; ++row) {
            float[] srcArray = src.getGrid()[row];
            float[] dstArray = dst.getGrid()[row];
            System.arraycopy(srcArray, 0, dstArray, 0, srcArray.length);
        }
    }

    public String getName() {
        return "Copy";
    }
    
}
