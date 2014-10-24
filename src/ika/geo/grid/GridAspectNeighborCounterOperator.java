/*
 * GridNeighborCounterOperator.java
 *
 * Created on February 1, 2006, 11:50 PM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridAspectNeighborCounterOperator implements GridOperator{
    
    /**
     * Size of the filter.
     * filterSize must be odd number;
     */
    private int filterSize = 5;
    
    /**
     * Only values with an absolute difference that is smaller than maxDiff
     * are counted. maxDiff is an angle, units are the same as the angles in the
     * input grid.
     */
    private float maxDiff = 0.35f;
    
    /**
     * The filter scans each ring around its center, starting at the center
     * towards its borders. The scan stops when there is a certain amount of
     * small values.
     * For one ring: fract = nbrValuesLargerThanMaxDiff / totalNbrValues.
     * The scan stops if fract < minFraction
     */
    private float minFraction = 0.7f;
    
    /**
     * Creates a new instance of GridNeighborCounterOperator
     */
    public GridAspectNeighborCounterOperator() {
    }
    
    public String getName() {
        return "Aspect Count Filter";
    }
    
    public ika.geo.GeoObject operate(ika.geo.GeoGrid geoGrid) {
        
        /*
        % Size of the resulting matrices relative to input grid: The filter uses
        % aspect values that are not (well) defined for border rows and columns.
        % The size of the input grid to the filtering process is therefore nrows-2
        % times ncols-2. The filtering is again not (well) defined on the border
        % rows and columns of this reduced aspect grid. The size is reduced by
        % filterSize-1 if filterSize is odd. The number of rows of the resulting
        % grids therefore is nrows-2-(filterSize-1) = nrows-1-filterSize.
         */
        
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        // make sure filterSize is odd number
        if (filterSize % 2 != 1)
            return null;
        final int halfFilterSize = this.filterSize / 2;
        
        // compute the size of the new GeoGrid and create it.
        final int old_nrows = geoGrid.getRows();
        final int old_ncols = geoGrid.getCols();
        final int counter_nrows = old_nrows - this.filterSize + 1;
        final int counter_ncols = old_ncols - this.filterSize + 1;
        final double meshSize = geoGrid.getCellSize();
        GeoGrid counterGrid = new GeoGrid(counter_ncols, counter_nrows, meshSize);
        counterGrid.setWest(geoGrid.getWest() + meshSize * halfFilterSize);
        counterGrid.setNorth(geoGrid.getNorth() + meshSize * halfFilterSize);
        
        /* loop over each pixel */
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = counterGrid.getGrid();
        
        for (int row = halfFilterSize; row < old_nrows-halfFilterSize; row++) {
            float[] dstRow = dstGrid[row - halfFilterSize];
            for (int col = halfFilterSize; col < old_ncols-halfFilterSize; col++) {
                int nbrScannedPts = 0;
                int npts = 0;
                
                // loop over each ring around the central pixel
                for (int i = 1; i <= halfFilterSize; i++) {
                    // count the number of values in the ring that are similar
                    // to the central value
                    final int nbrFoundPts = scanRect(col, row, srcGrid, i);
                    
                    // keep track of the number of pixel that have been visited so far
                    // For one ring: npts = 4n-2, here half filter size.
                    nbrScannedPts += 8 * i - 2;
                    
                    // abort scanning when there are too few similar points in
                    // the current ring
                    if (((double)npts + nbrFoundPts) / nbrScannedPts < minFraction)
                        break;
                    
                    // accumulate the number of found points
                    npts += nbrFoundPts;
                }
                
                // store the number of found points in the grid
                dstRow[col-halfFilterSize] = npts;
            }
        }
        return counterGrid;
    }
    
    /**
     * Scans a rectangular ring around a central pixel and counts the number of
     * pixels that are similar to the central pixel.
     * @param col The column of the central pixel
     * @param row The row of the central pixel
     * @param grid The GeoGrid to scan.
     * @param currentHalfFilterSize The distance in pixel from the central pixel
     * to the ring to scan.
     * @return The number of pixels with a value similar to the central pixel.
     */
    private int scanRect(int col, int row, float[][] grid, int currentHalfFilterSize) {
        int npts = 0;
        final float centerVal = grid[row][col];
        
        // scan top and bottom row
        final float[] topRow = grid[row-currentHalfFilterSize];
        final float[] botRow = grid[row+currentHalfFilterSize];
        for (int c = col-currentHalfFilterSize; c <= col+currentHalfFilterSize; c++) {
            if (this.isAngleDiffLarge(centerVal, topRow[c])) {
                npts++;
            }
            if (this.isAngleDiffLarge(centerVal, botRow[c])){
                npts++;
            }
        }
        
        // scan left colum and right column. Don't scan corner points, which
        // have been scanned in the loop above.
        for (int r = row-currentHalfFilterSize+1; r < row+currentHalfFilterSize; r++) {
            final float[] srcRow = grid[r];
            if (this.isAngleDiffLarge(centerVal, srcRow[col-currentHalfFilterSize])) {
                npts++;
            }
            if (this.isAngleDiffLarge(centerVal, srcRow[col+currentHalfFilterSize])) {
                npts++;
            }
        }
        return npts;
    }
    
    private boolean isAngleDiffLarge(float a1, float a2) {
        
        // compute the difference between two angles. We are interested in the
        // absolute value only.
        double dif = Math.abs(a1 - a2);
        if (dif > Math.PI)
            dif = 2. * Math.PI - dif;
        return dif < this.maxDiff;
    }
    
    public int getFilterSize() {
        return filterSize;
    }
    
    public void setFilterSize(int filterSize) {
        this.filterSize = filterSize;
    }
    
    public float getMaxDiff() {
        return maxDiff;
    }
    
    public void setMaxDiff(float maxDiff) {
        this.maxDiff = maxDiff;
    }
    
    public float getMinFraction() {
        return minFraction;
    }
    
    public void setMinFraction(float minFraction) {
        this.minFraction = minFraction;
    }
}