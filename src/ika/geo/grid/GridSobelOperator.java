/*
 * GridSobelOperator.java
 *
 * Created on February 6, 2006, 8:56 AM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 * Sobel edge detection filter.
 * @author jenny
 */
public class GridSobelOperator implements GridOperator{
    
    /** Creates a new instance of GridSobelOperator */
    public GridSobelOperator() {
    }
    
    public String getName() {
        return "Sobel";
    }
    
    public GeoGrid operate(GeoGrid geoGrid) {
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
                 * |A B C|
                 * |D 0 E|
                 * |F G H|
                 */
                final float a = srcGrid[row-1][col-1];
                final float b = srcGrid[row-1][col];
                final float c = srcGrid[row-1][col+1];
                final float d = srcGrid[row][col-1];
                final float e = srcGrid[row][col+1];
                final float f = srcGrid[row+1][col-1];
                final float g = srcGrid[row+1][col];
                final float h = srcGrid[row+1][col+1];
                
                final float val = (Math.abs(-a-2*b-c+f+2*g+h) +
                        Math.abs(-c-2*e-h+a+2*d+f)) / 8;
                dstGrid[row-1][col-1] = val;
            }
        }
        
        return newGrid;
    }
    
}
