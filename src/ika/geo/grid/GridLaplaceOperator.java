/*
 * GridLaplaceOperator.java
 *
 * Created on February 19, 2006, 12:05 AM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author jenny
 */
public class GridLaplaceOperator implements GridOperator{
    
    /** Creates a new instance of GridLaplaceOperator */
    public GridLaplaceOperator() {
    }
    
    public String getName() {
        return "Laplace";
    }
    
    public GeoObject operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int newCols = geoGrid.getCols() - 2;
        final int newRows = geoGrid.getRows() - 2;
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(newCols, newRows, meshSize);
        newGrid.setWest(geoGrid.getWest() + meshSize);
        newGrid.setNorth(geoGrid.getNorth() + meshSize);
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();
        
        for (int row = 1; row < srcRows - 1; row++) {
            for (int col = 1; col < srcCols - 1; col++) {
                /*
                | 0  1  0 |
                | 1 -4  1 |
                | 0  1  0 |
                 */
                final float center = srcGrid[row][col];
                final float top = srcGrid[row-1][col];
                final float left = srcGrid[row][col-1];
                final float right = srcGrid[row][col+1];
                final float bottom = srcGrid[row+1][col];
                
                final float val = top + left - 4f * center + right + bottom;
                dstGrid[row-1][col-1] = val;
            }
        }
        
        return newGrid;
    }
}
