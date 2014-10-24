/*
 * GridSlopeLineOperator.java
 *
 * Created on May 25, 2006, 2:23 PM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;

/**
 * Generates a new grid with the relative position in a slope of the source grid.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridSlopeLineOperator implements GridOperator {
    
    private float minVerDiff = 0;
    private double minSlope = 0;
    
    /** Creates a new instance of GridSlopeLineOperator */
    public GridSlopeLineOperator() {
    }
    
    public String getName() {
        return "Slope Line";
    }
    
    public GeoObject operate(GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        final int nrows = geoGrid.getRows();
        final int ncols = geoGrid.getCols();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, geoGrid.getCellSize());
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();

        for (int row = 1; row < nrows - 1; ++row) {
            /*
            if (row % 5 == 0)
            System.out.println ("row " + row);
             */
            for (int col = 1; col < ncols - 1; ++col) {
                nextDown(geoGrid, newGrid, col, row);
            }
        }

        return newGrid;
    }

    /*
    private int nextUp(GeoGrid elevationGrid, GeoGrid counterGrid,
            int currentCol, int currentRow) {
        
        long elevRows = elevationGrid.getRows();
        long elevCols = elevationGrid.getCols();
        
        float maxDiff = 0f;
        float hCenter = elevationGrid.getValue(currentCol, currentRow);
        
        float h, diff;
        int nextRow = currentRow;
        int nextCol = currentCol;
        final float sqrt2 = (float)Math.sqrt(2);
        
        h = elevationGrid.getValue(currentCol + 1, currentRow);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol - 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow - 1;
        }
        
        // test if a neighbor was found with a difference in elevation that is big enough.
        if (maxDiff == 0) {
            counterGrid.setValue(0, currentCol, currentRow);
            return 0;
        }
        
        // make sure we don't leave the grid
        if (nextRow == 0 || nextRow == elevRows - 1
                || nextCol == 0 || nextCol == elevCols - 1) {
            counterGrid.setValue(0, currentCol, currentRow);
            return 0;
        }

        float nextCount = counterGrid.getValue(nextCol, nextRow);
        if (nextCount > 0) {
            counterGrid.setValue(nextCount + 1, currentCol, currentRow);
            return (int)nextCount + 1;
        }
        
        int stepsUp = nextUp(elevationGrid, counterGrid, nextCol, nextRow) + 1;
        counterGrid.setValue(stepsUp, currentCol, currentRow);
        return stepsUp;
    }
    */

    private int nextDown(GeoGrid elevationGrid, GeoGrid counterGrid,
            int currentCol, int currentRow) {
        
        final float sqrt2 = (float)Math.sqrt(2);
        
        long elevRows = elevationGrid.getRows();
        long elevCols = elevationGrid.getCols();
        
        float maxDiff = 0f;
        float hCenter = elevationGrid.getValue(currentCol, currentRow);
        
        // find biggest elevation difference to all 8 neighbors
        float h, diff;
        int nextRow = currentRow;
        int nextCol = currentCol;
        
        h = elevationGrid.getValue(currentCol + 1, currentRow);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol - 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow - 1;
        }
       
        // test if a neighbor was found with a difference in elevation that is big enough.
        if (maxDiff == 0) {
            counterGrid.setValue(0, currentCol, currentRow);
            return 0;
        }

        // test for minimum slope
        final double slope = elevationGrid.getSlope(currentCol, currentRow);
        if (slope < minSlope) {
            counterGrid.setValue(0, currentCol, currentRow);
            return 0;
        }
       
        // make sure we don't leave the grid
        if (nextRow == 0 || nextRow == elevRows - 1
                || nextCol == 0 || nextCol == elevCols - 1) {
            counterGrid.setValue(0, currentCol, currentRow);
            return 0;
        }
        
        float nextCount = counterGrid.getValue(nextCol, nextRow);
        if (nextCount > 0) {
            counterGrid.setValue(nextCount + 1, currentCol, currentRow);
            return (int)nextCount + 1;
        }
        
        final int stepsDown = nextDown(elevationGrid, counterGrid, nextCol, nextRow) + 1;
        counterGrid.setValue(stepsDown, currentCol, currentRow);
        return stepsDown;
    }
    
    public float getMinVerDiff() {
        return minVerDiff;
    }

    public void setMinVerDiff(float minVerDiff) {
        this.minVerDiff = minVerDiff;
    }

    /**
     * @return the minSlope
     */
    public double getMinSlope() {
        return minSlope;
    }

    /**
     * @param minSlope the minSlope to set
     */
    public void setMinSlope(double minSlope) {
        this.minSlope = minSlope;
    }
}
