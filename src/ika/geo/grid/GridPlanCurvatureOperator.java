/*
 * GridPlanCurvatureOperator.java
 *
 * Created on February 14, 2006, 8:43 PM
 */

package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * http://www.soi.city.ac.uk/~jwo/phd/04param.php
 * @author jenny
 */
public class GridPlanCurvatureOperator extends ThreadedGridOperator{
    
    public static float planCurv(GeoGrid geoGrid, int col, int row) {
        float[][] srcGrid = geoGrid.getGrid();
        final double cellSize = geoGrid.getCellSize();
        final float inverseDoubleMeshSize = (float) (1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float) (1 / (cellSize * cellSize));

        final int rabove = Math.max(0, row - 1);
        final int rbelow = Math.min(geoGrid.getRows() - 1, row + 1);
        final int cleft = Math.max(0, col - 1);
        final int cright = Math.min(geoGrid.getCols() - 1, col + 1);
        final float e0 = srcGrid[row][col]; // center
        final float e1 = srcGrid[rabove][cleft]; // north-west
        final float e2 = srcGrid[rabove][col]; // north
        final float e3 = srcGrid[rabove][cright]; //north-east
        final float e4 = srcGrid[row][cleft]; // west
        final float e5 = srcGrid[row][cright]; // east
        final float e6 = srcGrid[rbelow][cleft]; // south-west
        final float e7 = srcGrid[rbelow][col]; // south
        final float e8 = srcGrid[rbelow][cright]; // south-east

        final float D = ((e4 + e5) / 2 - e0) * inverseSquareMeshSize;
        final float E = ((e2 + e7) / 2 - e0) * inverseSquareMeshSize;
        final float F = (-e1 + e3 + e6 - e8) / 4 * inverseSquareMeshSize;
        final float G = (-e4 + e5) * inverseDoubleMeshSize;
        final float H = (e2 - e7) * inverseDoubleMeshSize;
        final float divider = G * G + H * H;
        return divider == 0 ? 0 : 2 * ((D * H * H + E * G * G - F * G * H) / divider);
    }

    /** Creates a new instance of GridPlanCurvatureOperator */
    public GridPlanCurvatureOperator() {
    }
    
    public String getName() {
        return "Plan Curvature";
    }

    @Override
    public boolean isOverwrittingSupported() {
        return false;
    }
    
    /*
    // obsolete single threaded version
    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int cols = geoGrid.getCols();
        final int rows = geoGrid.getRows();
        final double cellSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(cols, rows, cellSize);
        newGrid.setName(this.getName());
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        
        // top row
        for (int col = 0; col < cols; col++) {
            this.operateBorder(geoGrid, newGrid, col, 0, cellSize);
        }
        // bottom row
        for (int col = 0; col < cols; col++) {
            this.operateBorder(geoGrid, newGrid, col, rows - 1, cellSize);
        }
        // left column
        for (int row = 1; row < rows - 1; row++) {
            this.operateBorder(geoGrid, newGrid, 0, row, cellSize);
        }
        // right column
        for (int row = 1; row < rows - 1; row++) {
            this.operateBorder(geoGrid, newGrid, cols - 1, row, cellSize);
        }
        // interior of grid
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        final float inverseDoubleMeshSize = (float)(1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float)(1 / (cellSize * cellSize));
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col < cols - 1; col++) {
                
                final float e0 = srcGrid[row][col]; // center
                final float e1 = srcGrid[row-1][col-1]; // north-west
                final float e2 = srcGrid[row-1][col]; // north
                final float e3 = srcGrid[row-1][col+1]; //north-east
                final float e4 = srcGrid[row][col-1]; // west
                final float e5 = srcGrid[row][col+1]; // east
                final float e6 = srcGrid[row+1][col-1]; // south-west
                final float e7 = srcGrid[row+1][col]; // south
                final float e8 = srcGrid[row+1][col+1]; // south-east
                
                final float D = ((e4 + e5) / 2 - e0) * inverseSquareMeshSize;
                final float E = ((e2 + e7) / 2 - e0) * inverseSquareMeshSize;
                final float F = (-e1 + e3 + e6 - e8) / 4 * inverseSquareMeshSize;
                final float G = (-e4 + e5) * inverseDoubleMeshSize;
                final float H = (e2 - e7) * inverseDoubleMeshSize;
                final float divider = G*G+H*H;
                if (divider != 0) {
                    dstGrid[row][col] = 2*((D*H*H + E*G*G - F*G*H) / divider);
                }
            }
        }
        
        return newGrid;
    }
    */
    private void operateBorder(GeoGrid src, GeoGrid dst, int col, int row, double cellSize) {
        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final float inverseDoubleMeshSize = (float)(1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float)(1 / (cellSize * cellSize));
        final int cols = src.getCols();
        final int rows = src.getRows();
        
        final int rm = row - 1 < 0 ? 0 : row - 1;
        final int rp = row + 1 >= rows ? rows - 1 : row + 1; 
        final int cm = col - 1 < 0 ? 0 : col - 1;
        final int cp = col + 1 >= cols ? cols - 1 : col + 1;
        
        final float e0 = srcGrid[row][col]; // center
        final float e1 = srcGrid[rm][cm]; // north-west
        final float e2 = srcGrid[rm][col]; // north
        final float e3 = srcGrid[rm][cp]; //north-east
        final float e4 = srcGrid[row][cm]; // west
        final float e5 = srcGrid[row][cp]; // east
        final float e6 = srcGrid[rp][cm]; // south-west
        final float e7 = srcGrid[rp][col]; // south
        final float e8 = srcGrid[rp][cp]; // south-east

        final float D = ((e4 + e5) / 2 - e0) * inverseSquareMeshSize;
        final float E = ((e2 + e7) / 2 - e0) * inverseSquareMeshSize;
        final float F = (-e1 + e3 + e6 - e8) / 4 * inverseSquareMeshSize;
        final float G = (-e4 + e5) * inverseDoubleMeshSize;
        final float H = (e2 - e7) * inverseDoubleMeshSize;
        final float divider = G * G + H * H;
        if (divider != 0) {
            dstGrid[row][col] = 2 * ((D * H * H + E * G * G - F * G * H) / divider);
        } else {
            dstGrid[row][col] = 0;
        }
    }

    @Override
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        final int cols = src.getCols();
        final int rows = src.getRows();
        final double cellSize = src.getCellSize();
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
        final float inverseDoubleMeshSize = (float)(1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float)(1 / (cellSize * cellSize));
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            for (int col = 1; col < cols - 1; col++) {

                final float e0 = srcGrid[row][col]; // center
                final float e1 = srcGrid[row-1][col-1]; // north-west
                final float e2 = srcGrid[row-1][col]; // north
                final float e3 = srcGrid[row-1][col+1]; //north-east
                final float e4 = srcGrid[row][col-1]; // west
                final float e5 = srcGrid[row][col+1]; // east
                final float e6 = srcGrid[row+1][col-1]; // south-west
                final float e7 = srcGrid[row+1][col]; // south
                final float e8 = srcGrid[row+1][col+1]; // south-east

                final float D = ((e4 + e5) / 2 - e0) * inverseSquareMeshSize;
                final float E = ((e2 + e7) / 2 - e0) * inverseSquareMeshSize;
                final float F = (-e1 + e3 + e6 - e8) / 4 * inverseSquareMeshSize;
                final float G = (-e4 + e5) * inverseDoubleMeshSize;
                final float H = (e2 - e7) * inverseDoubleMeshSize;
                final float divider = G*G+H*H;
                if (divider != 0) {
                    dstGrid[row][col] = 2*((D*H*H + E*G*G - F*G*H) / divider);
                } else {
                    dstGrid[row][col] = 0;
                }
            }
        }
    }
    
}
