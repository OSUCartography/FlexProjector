package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Changes all cells with a certain value to void.
 */
public class GridInvalidateOperator implements GridOperator{
    
    private float v;

    public GridInvalidateOperator() {
    }
    
    public GridInvalidateOperator(float v) {
        this.v = v;
    }
    
    public String getName() {
        return "Invalidate";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int nrows = geoGrid.getRows();
        final int ncols = geoGrid.getCols();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, geoGrid.getCellSize());
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        newGrid.setName(geoGrid.getName());
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        
        for (int row = 0; row < nrows; ++row) {
            float[] srcRow = srcGrid[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < ncols; ++col) {
                dstRow[col] = (srcRow[col] == v) ? Float.NaN : srcRow[col];
            }
        }
        return newGrid;
    }

}
