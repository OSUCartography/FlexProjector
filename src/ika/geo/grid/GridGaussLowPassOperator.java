/*
 * GridGaussLowPassOperator.java
 *
 */
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Gaussian blur or low pass filter.
 * Uses the fact that a 2D Gaussian convolution can be replaced by a horizontal
 * and a vertical 1D convolution. A horizontal 1D convolution is applied, then
 * the grid is transposed, the horizontal 1D convolution applied again, and the
 * grid transposed again. This is much faster than a combination of a horizontal
 * and a vertical convolution, due to the fact that horizontal rows are stored
 * in contiguous locations. The horizontal 1D convolution and the transposition
 * operators are merged into one multi-threaded operator.
 * See http://en.wikipedia.org/wiki/Gaussian_blur
 * August 26, 2010, and April 14, 2011.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridGaussLowPassOperator implements GridOperator{

    /**
     * Standard deviation of the Gaussian distribution. Higher values produce
     * stronger smoothing.
     */
    private double std = 0.8;
    
    /**
     * A grid that is used during the filtering process. It is cached between
     * calls to operate(), as the allocation of this grid is relatively expensive.
     */
    private GeoGrid tempTransposedGrid;
    
    /**
     * The size of the kernel relative to the standard deviation. The kernel's 
     * dimension in pixels in one direction is: relativeFilterSize * std
     * A value of 6 is sufficient for most applications. If 
     * gradients are computed after filtering with a kernel size of 6, however,
     * artifacts (vertical and horizontal stripes) are likely to become visible.
     */
    private int relativeFilterSize = 8;

    /**
     * Applies horizontal Gaussian convolution and stores results in a
     * transposed grid.
     */
    private class HorizontalTransposedConvolution extends ThreadedGridOperator {

        /**
         * Create a transposed grid
         * @param srcGrid
         * @return
         */
        @Override
        protected GeoGrid initDestinationGrid(GeoGrid srcGrid) {

            if (!isTemporaryTransposedGridValid(srcGrid)) {
                final int nrows = srcGrid.getRows();
                final int ncols = srcGrid.getCols();
                tempTransposedGrid = new GeoGrid(nrows, ncols, srcGrid.getCellSize());
                tempTransposedGrid.setWest(srcGrid.getWest());
                tempTransposedGrid.setNorth(srcGrid.getNorth());
                tempTransposedGrid.setName(srcGrid.getName());
            }
            return tempTransposedGrid;
        }

        private boolean isTemporaryTransposedGridValid(GeoGrid srcGrid) {
            return tempTransposedGrid != null
                    && srcGrid.getCols() == tempTransposedGrid.getRows()
                    && srcGrid.getRows() == tempTransposedGrid.getCols();
        }

        @Override
        public GeoGrid operate(GeoGrid src, GeoGrid dst) {
            if (src.getCols() != dst.getRows() || src.getRows() != dst.getCols()) {
                throw new IllegalStateException("destination grid has wrong size");
            }
            return super.operate(src, dst);
        }

        @Override
        public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

            final int ncols = src.getCols();
            final int halfFilterSize = kernelSize() / 2;
            final float[] kernel = kernel();

            for (int row = startRow; row < endRow; row++) {
                final float[][] dstGrid = dst.getGrid();
                final float[] srcRow = src.getGrid()[row];

                // convolve left border area
                final int maxCol = Math.min(halfFilterSize, ncols);
                for (int col = 0; col < maxCol; col++) {
                    float sum = 0;
                    float coefSum = 0;
                    for (int f = -col; f <= halfFilterSize; f++) {
                        if (col + f < ncols) {
                            final float s = kernel[f + halfFilterSize];
                            sum += srcRow[col + f] * s;
                            coefSum += s;
                        }
                    }
                    dstGrid[col][row] = sum / coefSum; // transposed destination
                }

                // convolve center area
                for (int col = halfFilterSize; col < ncols - halfFilterSize; col++) {
                    float sum = 0;
                    for (int c = col - halfFilterSize, f = 0; c <= col + halfFilterSize; c++, f++) {
                        sum += srcRow[c] * kernel[f];
                    }
                    dstGrid[col][row] = sum; // transposed destination
                }

                // convolve right border area
                final int minCol = Math.max(0, ncols - halfFilterSize);
                for (int col = minCol; col < ncols; col++) {
                    float sum = 0;
                    float coefSum = 0;
                    for (int f = -halfFilterSize; f < ncols - col; f++) {
                        if (col + f >= 0) {
                            final float s = kernel[f + halfFilterSize];
                            sum += srcRow[col + f] * s;
                            coefSum += s;
                        }
                    }
                    dstGrid[col][row] = sum / coefSum; // transposed destination
                }
            }
        }

        public String getName() {
            return "Horizontal Transposed 1D Convolution";
        }
    }
    
    /** Creates a new instance of GridGaussLowPassOperator */
    public GridGaussLowPassOperator() {
    }

    /** Creates a new instance of GridGaussLowPassOperator */
    public GridGaussLowPassOperator(double std) {
        setStandardDeviation(std);
    }

    public String getName() {
        return "Gauss Low Pass";
    }

    /**
     * Evaluates the Gaussian function.
     * @param x Evaluate at distance x from 0.
     * @return The vertical distance at position x.
     */
    private double gaussian(double x) {
        final double stdSqr2 = 2 * std * std;
        return Math.exp(-x * x / stdSqr2) / Math.sqrt(stdSqr2 * Math.PI);
    }

    /**
     * Returns the size of the kernel.
     * @return
     */
    private int kernelSize() {
        // compute odd filter size and even half size
        // the size is relativeFilterSize * the standard deviation on both 
        // sides of the origin. Kernel values outside this range are neglected.
        int filterSize = (int) Math.ceil(relativeFilterSize * std);
        if (filterSize % 2 == 0) {
            filterSize += 1;
        }
        return filterSize;
    }
    
    /**
     * Computes the coefficients for the Gaussian kernel
     * @param filterSize
     * @return
     */
    private float[] kernel() {
        int size = kernelSize();
        float coef[] = new float[size];
        for (int i = 0; i <= size / 2; i++) {
            coef[i] = (float) gaussian(size / 2 - i);
            coef[size - i - 1] = coef[i];
        }

        // normalize sum of coefficients
        float coefSum = 0;
        for (int i = 0; i < size; i++) {
            coefSum += coef[i];
        }
        for (int i = 0; i < size; i++) {
            coef[i] /= coefSum;
        }
        return coef;
    }

    public GeoGrid operate(GeoGrid grid) {
        GeoGrid dst = new GeoGrid(grid.getCols(), grid.getRows(), grid.getCellSize());
        return operate(grid, dst);
    }

    public GeoGrid operate(GeoGrid src, GeoGrid dst, double std) {
        setStandardDeviation(std);
        return operate(src, dst);
    }
    
    public GeoGrid operate(GeoGrid src, GeoGrid dst, double std, int relativeFilterSize) {
        setStandardDeviation(std);
        setRelativeFilterSize(relativeFilterSize);
        return operate(src, dst);
    }

    public GeoGrid operate(GeoGrid src, GeoGrid dst) {
        if (std == 0) {
            if (src == dst) {
                return dst;
            } else {
                return new GridCopyOperator().operate(src, dst);
            }
        }
        
        HorizontalTransposedConvolution hop = new HorizontalTransposedConvolution();
        GeoGrid transposedGrid = hop.operate(src);
        return hop.operate(transposedGrid, dst);
    }

    /**
     * Get the standard deviation of the Gaussian distribution.
     * @return the standard deviation
     */
    public double getStandardDeviation() {
        return std;
    }

    /**
     * Set the standard deviation of the Gaussian distribution. If std is 0, no
     * filter is applied.
     * @param std the standard deviation to set
     */
    public final void setStandardDeviation(double std) {
        if (std < 0) {
            throw new IllegalArgumentException("negative standard deviation");
        }
        this.std = std;
    }
    
    /**
     * Get the size of the kernel, relative to the standard deviation. The 
     * kernel size in pixels in one dimension is relativeFilterSize * std
     * @return the relativeFilterSize
     */
    public int getRelativeFilterSize() {
        return relativeFilterSize;
    }

    /**
     * Set the size of the kernel, relative to the standard deviation. The 
     * kernel size in pixels in one dimension is relativeFilterSize * std
     * @param relativeFilterSize the relativeFilterSize to set
     */
    public void setRelativeFilterSize(int relativeFilterSize) {
        this.relativeFilterSize = relativeFilterSize;
    }
}
