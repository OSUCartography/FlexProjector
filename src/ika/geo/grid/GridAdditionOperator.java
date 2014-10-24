package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Add a constant to a grid.
 * @author jenny
 */
public class GridAdditionOperator implements GridOperator{
    
    private float add;

    public GridAdditionOperator() {
    }
    
    public GridAdditionOperator(float add) {
        this.add = add;
    }
    
    public String getName() {
        return "Addition";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
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
                dstRow[col] = srcRow[col] + add;
            }
        }
        return newGrid;
    }

}
