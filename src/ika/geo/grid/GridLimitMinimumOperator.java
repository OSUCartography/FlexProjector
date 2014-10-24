/*
 * GridLimitMinimumOperator.java
 *
 * Created on February 3, 2006, 11:58 AM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Replaces grid values that are smaller than a limit by another value.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridLimitMinimumOperator implements GridOperator{
    
    private float limit = 0.f;
    private float replacementValue = 0.f;
    
    /** Creates a new instance of GridLimitMinimumOperator */
    public GridLimitMinimumOperator() {
    }
    
    public String getName() {
        return "Limit Minimum";
    }
    
    public void operate(GeoGrid geoGrid, GeoGrid newGrid) {
        if (geoGrid == null || newGrid == null)
            throw new IllegalArgumentException();
        
        final int nrows = geoGrid.getRows();
        final int ncols = geoGrid.getCols();
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();

        for (int row = 0; row < nrows; ++row) {
            final float[] srcRow = srcGrid[row];
            final float[] dstRow = dstGrid[row];
            for (int col = 0; col < ncols; ++col) {
                final float v = srcRow[col];
                dstRow[col] = srcRow[col] < limit ? replacementValue : v;
            }
        }
    }
    
    public GeoGrid operate(GeoGrid geoGrid) {
        final int nrows = geoGrid.getRows();
        final int ncols = geoGrid.getCols();
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, meshSize);
        this.operate(geoGrid, newGrid);
        return newGrid;
    }

    public GeoGrid operate(GeoGrid geoGrid, float limit, float replacementValue) {
        setLimit(limit);
        setReplacementValue(replacementValue);
        return this.operate(geoGrid);
    }

    public float getReplacementValue() {
        return replacementValue;
    }

    public void setReplacementValue(float replacementValue) {
        this.replacementValue = replacementValue;
    }

    public float getLimit() {
        return limit;
    }

    public void setLimit(float limit) {
        this.limit = limit;
    }
}
