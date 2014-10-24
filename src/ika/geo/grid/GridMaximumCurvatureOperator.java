package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * http://www.soi.city.ac.uk/~jwo/phd/04param.php
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMaximumCurvatureOperator extends ThreadedGridOperator {

    public String getName() {
        return "Maximum Curvature";
    }
    
    @Override
    public boolean isOverwrittingSupported() {
        return false;
    }

    private void operateBorder(GeoGrid src, GeoGrid dst, int col, int row, double cellSize) {
        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int cols = src.getCols();
        final int rows = src.getRows();

        final int rm = row - 1 < 0 ? 0 : row - 1;
        final int rp = row + 1 >= rows ? rows - 1 : row + 1;
        final int cm = col - 1 < 0 ? 0 : col - 1;
        final int cp = col + 1 >= cols ? cols - 1 : col + 1;

        final float z1 = srcGrid[rm][cm]; // top left
        final float z2 = srcGrid[rm][col]; // top
        final float z3 = srcGrid[rm][cp]; // top right
        final float z4 = srcGrid[row][cm]; // left
        final float z5 = srcGrid[row][col]; // center
        final float z6 = srcGrid[row][cp]; // right
        final float z7 = srcGrid[rp][cm]; // bottom left
        final float z8 = srcGrid[rp][col]; // bottom
        final float z9 = srcGrid[rp][cp]; // bottom right

        final double gg = cellSize * cellSize;
        final double a = gg * ((z1 + z3 + z4 + z6 + z7 + z9) / 6 - (z2 + z5 + z8) / 3);
        final double b = gg * ((z1 + z2 + z3 + z7 + z8 + z9) / 6 - (z4 + z5 + z6) / 3);
        final double a_b = a - b;
        final double c = (z3 + z7 - z1 - z9) / 4 * gg;

        dstGrid[row][col] = (float) (-a - b + Math.sqrt(a_b * a_b + c * c));
    }

    @Override
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        final int cols = src.getCols();
        final int rows = src.getRows();
        final double cellSize = src.getCellSize();
        final double gg = cellSize * cellSize;
        final int firstInteriorRow = Math.max(1, startRow);
        final int lastInteriorRow = Math.min(rows - 1, endRow);

        // top row
        if (startRow == 0) {
            for (int col = 0; col < cols; col++) {
                operateBorder(src, dst, col, 0, cellSize);
            }
        }

        // bottom row
        if (endRow == rows - 1) {
            for (int col = 0; col < cols; col++) {
                operateBorder(src, dst, col, rows - 1, cellSize);
            }
        }

        // left column
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            operateBorder(src, dst, 0, row, cellSize);
        }

        // right column
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            operateBorder(src, dst, cols - 1, row, cellSize);
        }

        // interior of grid
        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            for (int col = 1; col < cols - 1; col++) {

                final float z1 = srcGrid[row - 1][col - 1]; // top left
                final float z2 = srcGrid[row - 1][col]; // top
                final float z3 = srcGrid[row - 1][col + 1]; // top right
                final float z4 = srcGrid[row][col - 1]; // left
                final float z5 = srcGrid[row][col]; // center
                final float z6 = srcGrid[row][col + 1]; // right
                final float z7 = srcGrid[row + 1][col - 1]; // bottom left
                final float z8 = srcGrid[row + 1][col]; // bottom
                final float z9 = srcGrid[row + 1][col + 1]; // bottom right

                final double a = gg * ((z1 + z3 + z4 + z6 + z7 + z9) / 6 - (z2 + z5 + z8) / 3);
                final double b = gg * ((z1 + z2 + z3 + z7 + z8 + z9) / 6 - (z4 + z5 + z6) / 3);
                final double a_b = a - b;
                final double c = (z3 + z7 - z1 - z9) / 4 * gg;

                dstGrid[row][col] = (float) (-a - b + Math.sqrt(a_b * a_b + c * c));
            }
        }
    }
}
