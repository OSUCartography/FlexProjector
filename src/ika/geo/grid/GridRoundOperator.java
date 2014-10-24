/*
 * GridRoundOperator.java
 *
 * Created on February 2, 2006, 8:48 PM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 * Rounds all values in a grid to their nearest entire integer value.
 * @author jenny
 */
public class GridRoundOperator implements GridOperator {
    
    /** Creates a new instance of GridRoundOperator */
    public GridRoundOperator() {
    }

    public String getName() {
        return "Round";
    }

    public ika.geo.GeoObject operate(ika.geo.GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
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
                dstRow[col] = (float)Math.round(srcRow[col]);
            }
        }
        return newGrid;
    }

    
}
