package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Abstract base class for non-linear percentile filters, such as median, upper
 * and lower quartile.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich. 3 October 2008.
 */
public abstract class GridPercentileFilterOperator implements GridOperator {

    protected int filterSize = 3;

    public GridPercentileFilterOperator() {
    }

    public GridPercentileFilterOperator(int filterSize) {
        this.filterSize = filterSize;
    }

    protected abstract float percentile(float[] values);
    
    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();        
        }
        
        // make sure filterSize is odd number
        if (filterSize % 2 != 1) {
            return null;
        }
        final int halfFilterSize = this.filterSize / 2;

        // create the new grid
        final int nrows = geoGrid.getRows();
        final int ncols = geoGrid.getCols();
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, meshSize);
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());

        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();

        // filter interior of grid
        float[] values = new float[this.filterSize * this.filterSize];
        for (int row = halfFilterSize; row < nrows - halfFilterSize; row++) {
            final float[] dstRow = dstGrid[row];
            for (int col = halfFilterSize; col < ncols - halfFilterSize; col++) {
                for (int r = 0; r < filterSize; r++) {
                    System.arraycopy(srcGrid[r + row - halfFilterSize],
                            col - halfFilterSize,
                            values, r * filterSize, filterSize);
                }
                dstRow[col] = this.percentile(values);
            }
        }

        // filter border of grid
        this.operateBorder(geoGrid, newGrid);
        return newGrid;
    }

    private void operateBorder(GeoGrid src, GeoGrid dst) {

        final int halfFilterSize = this.filterSize / 2;
        final int cols = src.getCols();
        final int rows = src.getRows();

        // top rows
        for (int r = 0; r < halfFilterSize; r++) {
            for (int c = 0; c < cols; c++) {
                this.operateBorder(src, dst, c, r);
            }
        }

        // bottom rows
        for (int r = rows - halfFilterSize; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                this.operateBorder(src, dst, c, r);
            }
        }

        // left columns
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < halfFilterSize; c++) {
                this.operateBorder(src, dst, c, r);
            }
        }

        // right columns
        for (int r = 0; r < rows; r++) {
            for (int c = cols - halfFilterSize; c < cols; c++) {
                this.operateBorder(src, dst, c, r);
            }
        }
    }

    private void operateBorder(GeoGrid src, GeoGrid dst, int col, int row) {
        final int halfFilterSize = this.filterSize / 2;
        float[] values = new float[this.filterSize * this.filterSize];
        final int cols = src.getCols();
        final int rows = src.getRows();
        int counter = 0;
        for (int r = -halfFilterSize + row; r <= halfFilterSize + row; r++) {
            final int gridRow = r < 0 ? -r : (r >= rows ? 2 * rows - 2 - r : r);
            for (int c = -halfFilterSize + col; c <= halfFilterSize + col; c++) {
                final int gridCol = c < 0 ? -c : (c >= cols ? 2 * cols - 2 - c : c);
                values[counter++] = src.getValue(gridCol, gridRow);
            }
        }
        dst.setValue(this.percentile(values), col, row);
    }

    public int getFilterSize() {
        return filterSize;
    }

    public void setFilterSize(int filterSize) {
        this.filterSize = filterSize;
    }
    
}
