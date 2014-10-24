package ika.geo.grid;

import ika.geo.GeoGrid;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A base class for multi-threaded grid operators. Allocates as many threads
 * for operating on the grid as CPU cores are available.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public abstract class ThreadedGridOperator implements GridOperator {

    /**
     * Operate row-wise on the passed source grid and store the result in the passed 
     * destination grid. The source and the destination can be the same object
     * if isOverwrittingSupported() returns true.
     * @param src The source grid.
     * @param dst The destination grid.
     * @param startRow The index of the first row to operate on.
     * @param endRow The index of the last row. Derived classes should not
     * operate on this row. It may be equal to src.getRows().
     */
    protected abstract void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow);

    /**
     * Returns whether the source and destination grids can be the same object.
     * Defaults to true, that is, the source grid can be overwritten. Needs to
     * be overridden by derived classes that access multiple cells for computing
     * a single new cell value. In this case, false needs to be returned.
     * @return True if the source and destination can be identical, false otherwise.
     */
    public boolean isOverwrittingSupported() {
        return true;
    }
    
    /**
     * Creates a new grid that will store the results of the operator. This
     * method creates a new grid of the same size as the source grid. It must be
     * overridden if the derived operator generates a grid that has a different
     * dimension or position than the source grid.
     * @param src The source grid.
     * @return The new grid of the same size and position as the source grid.
     */
    protected GeoGrid initDestinationGrid(GeoGrid src) {
        if (src == null || !src.isWellFormed()) {
            throw new IllegalArgumentException(getName() + ": invalid source grid");
        }
        
        int nrows = src.getRows();
        int ncols = src.getCols();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, src.getCellSize());
        newGrid.setWest(src.getWest());
        newGrid.setNorth(src.getNorth());
        newGrid.setName(src.getName());
        return newGrid;
    }

    /**
     * Apply the filter and store the result in a new grid that is returned.
     * @param src The source grid.
     * @return The new grid storing the result.
     */
    public GeoGrid operate(GeoGrid src) {
        GeoGrid dst = initDestinationGrid(src);
        return operate(src, dst);
    }

    /**
     * Apply the filter and store the result in the passed destination grid.
     * @param src The source grid.
     * @param dst The destination grid. May not be null.
     * @return The passed dst grid is returned.
     */
    public GeoGrid operate(GeoGrid src, GeoGrid dst) {
        
        if (src == null || !src.isWellFormed()) {
            throw new IllegalArgumentException(getName() + ": invalid source grid");
        }
        if (dst == null || !dst.isWellFormed()) {
            throw new IllegalArgumentException(getName() + ": invalid destination grid");
        }
        if (!isOverwrittingSupported() && src.getGrid() == dst.getGrid()) {
            throw new IllegalArgumentException(getName() + ": overwriting source grid is not possible");
        }
        
        int nRows = src.getRows();
        int nThreads = Runtime.getRuntime().availableProcessors();
        ArrayList<GridOperatorThread> threads = new ArrayList(nThreads);
        int rowChunk = (nRows / nThreads) + 1;
        for (int i = 0; i < nThreads; i++) {
            int startRow = i * rowChunk;
            int endRow = Math.min(nRows, startRow + rowChunk);
            GridOperatorThread t = new GridOperatorThread(src, dst, startRow, endRow);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadedGridOperator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return dst;
    }

    /**
     * A private utility class for wrapping a thread.
     */
    private class GridOperatorThread extends Thread {

        final GeoGrid srcGrid;
        final GeoGrid dstGrid;
        final int startRow;
        final int endRow;

        public GridOperatorThread(GeoGrid srcGrid,
                GeoGrid dstGrid,
                int startRow,
                int endRow) {

            this.srcGrid = srcGrid;
            this.dstGrid = dstGrid;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public void run() {
            operate(srcGrid, dstGrid, startRow, endRow);
        }
    }
}
