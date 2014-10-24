/*
 * GridInvertOperator.java
 *
 * Created on February 6, 2006, 9:56 AM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author jenny
 */
public class GridInvertOperator implements GridOperator {
    
    /** Creates a new instance of GridInvertOperator */
    public GridInvertOperator() {
    }

    public String getName() {
        return "Invert";
    }

    public ika.geo.GeoGrid operate(ika.geo.GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        float[] minMax = geoGrid.getMinMax();
        final float minPlusMax = minMax[0] + minMax[1];
        
        final int ncols = geoGrid.getCols();
        final int nrows = geoGrid.getRows();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, geoGrid.getCellSize());
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        
        for (int row = 0; row < nrows; row++) {
            float[] srcRow = srcGrid[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < ncols; ++col) {
                dstRow[col] = -srcRow[col] + minPlusMax;
            }
        }
        return newGrid;
    }
    
}
