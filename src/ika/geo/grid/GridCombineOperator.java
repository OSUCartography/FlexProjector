package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;
import java.awt.geom.Rectangle2D;

/**
 * Combines two grid. An optional grid with weights for linear weighting per 
 * cell can be used. An optional mask with NaN values can be used..
 * @author jenny
 */
public class GridCombineOperator extends ThreadedGridOperator {

    private GeoGrid src2;
    private GeoGrid weightGrid;
    private GeoGrid mask;

    public String getName() {
        return "Combination";
    }

    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {
        if (weightGrid == null) {
            if (mask == null) {
                combine(startRow, endRow, src, dst);
            } else {
                combineMasked(startRow, endRow, src, dst);
            }
        } else {
            if (mask == null) {
                combineWeighted(startRow, endRow, src, dst);
            } else {
                combineWeightedMasked(startRow, endRow, src, dst);
            }
        }
    }

    private void combineWeightedMasked(int startRow, int endRow, GeoGrid src, GeoGrid dst) {
        final int nCols = src.getCols();
        float[][] wGrid = weightGrid.getGrid();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow1 = src.getGrid()[row];
            float[] srcRow2 = src2.getGrid()[row];
            float[] wRow = wGrid[row];
            float[] dstRow = dst.getGrid()[row];
            float[] maskRow = mask.getGrid()[row];
            for (int col = 0; col < nCols; ++col) {
                if (Float.isNaN(maskRow[col])) {
                    dstRow[col] = Float.NaN;
                } else {
                    final float w = wRow[col];
                    dstRow[col] = srcRow1[col] * w + srcRow2[col] * (1f - w);
                }
            }
        }
    }

    private void combineWeighted(int startRow, int endRow, GeoGrid src, GeoGrid dst) {
        final int nCols = src.getCols();
        float[][] wGrid = weightGrid.getGrid();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow1 = src.getGrid()[row];
            float[] srcRow2 = src2.getGrid()[row];
            float[] wRow = wGrid[row];
            float[] dstRow = dst.getGrid()[row];
            for (int col = 0; col < nCols; ++col) {
                final float w = wRow[col];
                dstRow[col] = srcRow1[col] * w + srcRow2[col] * (1f - w);
            }
        }
    }

    private void combineMasked(int startRow, int endRow, GeoGrid src, GeoGrid dst) {
        final int nCols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow1 = src.getGrid()[row];
            float[] srcRow2 = src2.getGrid()[row];
            float[] dstRow = dst.getGrid()[row];
            float[] maskRow = mask.getGrid()[row];
            for (int col = 0; col < nCols; ++col) {
                if (Float.isNaN(maskRow[col])) {
                    dstRow[col] = Float.NaN;
                } else {
                    dstRow[col] = srcRow1[col] + srcRow2[col];
                }
            }
        }
    }

    private void combine(int startRow, int endRow, GeoGrid src, GeoGrid dst) {
        final int nCols = src.getCols();
        for (int row = startRow; row < endRow; ++row) {
            float[] srcRow1 = src.getGrid()[row];
            float[] srcRow2 = src2.getGrid()[row];
            float[] dstRow = dst.getGrid()[row];
            for (int col = 0; col < nCols; ++col) {
                dstRow[col] = srcRow1[col] + srcRow2[col];
            }
        }
    }

    /**
     * Sums the values of two grids. The new grid has the size of grid1, grid2
     * is resampled using bicubic interpolation.
     * @param grid1
     * @param grid2
     * @return
     */
    public GeoGrid operateWithResampling(GeoGrid grid1, GeoGrid grid2) {

        if (grid1 == null || grid2 == null) {
            throw new IllegalArgumentException();
        }

        final int nrows = grid1.getRows();
        final int ncols = grid1.getCols();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, grid1.getCellSize());
        newGrid.setWest(grid1.getWest());
        newGrid.setNorth(grid1.getNorth());

        float[][] src1Grid = grid1.getGrid();
        float[][] dst = newGrid.getGrid();
        Rectangle2D boundsGrid2 = grid2.getBounds2D(GeoObject.UNDEFINED_SCALE);

        for (int row = 0; row < nrows; ++row) {
            for (int col = 0; col < ncols; ++col) {
                double x = grid1.getWest() + col * grid1.getCellSize();
                double y = grid1.getNorth() - row * grid1.getCellSize();
                float v1 = src1Grid[row][col];

                float v2 = 0;
                try {
                    v2 = boundsGrid2.contains(x, y) ? grid2.getBilinearInterpol(x, y) : 0;
                } catch (Throwable e) {
                }

                dst[row][col] = v1 < 0 ? 0 : v1 + v2;
            }
        }
        return newGrid;

    }

    /**
     * @param grid2 the grid2 to set
     */
    public void setSrc2(GeoGrid src2) {
        this.src2 = src2;
    }

    /**
     * @param weightGrid the weightGrid to set
     */
    public void setWeightGrid(GeoGrid weightGrid) {
        this.weightGrid = weightGrid;
    }

    /**
     * @param mask the mask to set
     */
    public void setMask(GeoGrid mask) {
        this.mask = mask;
    }
}
