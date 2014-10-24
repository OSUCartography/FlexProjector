/*
 * GridAspectOperator.java
 *
 * Created on January 28, 2006, 2:43 PM
 *
 */
package ika.geo.grid;

import ika.geo.*;

/**
 * Extracts aspect angles from a grid. Aspect angles are in radians, counted
 * from the horizontal x axis towards north (standard geometric coordinate
 * system). Angles point upwards.
 *
 * @author jenny
 */
public class GridAspectOperator implements GridOperator {

    /**
     * Creates a new instance of GridAspectOperator
     */
    public GridAspectOperator() {
    }

    public String getName() {
        return "Grid Aspect";
    }

    public ika.geo.GeoGrid operate(ika.geo.GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }

        final int newCols = geoGrid.getCols() - 2;
        final int newRows = geoGrid.getRows() - 2;
        final double cellSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(newCols, newRows, cellSize);
        newGrid.setWest(geoGrid.getWest() + cellSize);
        newGrid.setNorth(geoGrid.getNorth() - cellSize);

        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();

        for (int row = 1; row < srcRows - 1; row++) {
            for (int col = 1; col < srcCols - 1; col++) {
                final float w = srcGrid[row][col - 1];
                final float e = srcGrid[row][col + 1];
                final float s = srcGrid[row + 1][col];
                final float n = srcGrid[row - 1][col];
                final float aspect = (float) Math.atan2(n - s, e - w);

                dstGrid[row - 1][col - 1] = aspect;
            }
        }

        return newGrid;
    }
}
