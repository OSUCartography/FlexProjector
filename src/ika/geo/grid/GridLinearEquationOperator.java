package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Applies a linear equation on the grid values: y = m * x + c
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridLinearEquationOperator implements GridOperator{
    
    private float m = 0.f;
    private float c = 0.f;
    
    /** Creates a new instance of GridLimitMinimumOperator */
    public GridLinearEquationOperator() {
    }
    
    public String getName() {
        return "Linear Equation";
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
                final float x = srcRow[col];
                dstRow[col] = m * x + c;
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

    public GeoGrid operate(GeoGrid geoGrid, float m, float c) {
        setM(m);
        setC(c);
        return this.operate(geoGrid);
    }

    /**
     * @return the m
     */
    public float getM() {
        return m;
    }

    /**
     * @param m the m to set
     */
    public void setM(float m) {
        this.m = m;
    }

    /**
     * @return the c
     */
    public float getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public void setC(float c) {
        this.c = c;
    }

   
}
