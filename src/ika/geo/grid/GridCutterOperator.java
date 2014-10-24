/*
 * GridCutterOperator.java
 *
 * Created on February 3, 2006, 8:56 AM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author jenny
 */
public class GridCutterOperator implements GridOperator {
    
    private int left = 0;
    private int right = 0;
    private int bottom = 0;
    private int top = 0;
    
    /** Creates a new instance of GridCutterOperator */
    public GridCutterOperator() {
    }

    public String getName() {
        return "Cutter";
    }
    
    public void operate (GeoGrid srcGeoGrid, GeoGrid dstGeoGrid) {
        final int oldRows = srcGeoGrid.getRows();
        final int oldCols = srcGeoGrid.getCols();
        final int newRows = oldRows - top - bottom;
        final int newCols = oldCols - left - right;
        
        if (newRows < 0)
            return;
        if (newCols < 0)
            return;
        
        float[][] srcGrid = srcGeoGrid.getGrid();
        float[][] dstGrid = dstGeoGrid.getGrid();
        
        for (int row = 0; row < newRows; row++){
            float[] srcRow = srcGrid[row + top];
            float[] dstRow = dstGrid[row];
            System.arraycopy(srcRow, left, dstRow, 0, newCols);
        }
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        final int oldRows = geoGrid.getRows();
        final int oldCols = geoGrid.getCols();
        final int newRows = oldRows - top - bottom;
        final int newCols = oldCols - left - right;
        
        if (newRows < 0)
            return null;
        if (newCols < 0)
            return null;
        
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(newCols, newRows, meshSize);
        newGrid.setWest(geoGrid.getWest() + left * meshSize);
        newGrid.setNorth(geoGrid.getNorth() + top * meshSize);
        
        this.operate(geoGrid, newGrid);
        
        return newGrid;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void setTop(int top) {
        this.top = top;
    }
    
    public void setBorder(int b) {
        this.left = this.right = this.top = this.bottom = b;
    }
    
}
