package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Applies a power function to the values in a grid.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridExponentiationOperator implements GridOperator {

    private double exponent;
    private float negativeVal = 0f;

    public GridExponentiationOperator() {
    }

    public GridExponentiationOperator(double exponent, float negativeVal) {
        this.exponent = exponent;
        this.negativeVal = negativeVal;
    }

    public String getName() {
        return "Exponentiation";
    }

    public void operate(GeoGrid geoGrid, GeoGrid newGrid) {
        if (geoGrid == null || newGrid == null) {
            throw new IllegalArgumentException();
        }

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
                dstRow[col] = v > 0 ? (float) Math.pow(v, exponent) : negativeVal;
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

}
