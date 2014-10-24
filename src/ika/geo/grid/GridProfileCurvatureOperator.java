package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * From: Wilson, J. P. and Gallant, J. C. (2000). Terrain Analysis - Principles
 * and Applications. Wiley. Pages 52-57.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridProfileCurvatureOperator implements GridOperator {

    private int filterSize = 3;
    
    public static float getProfileCurvature(GeoGrid grid, int col, int row, int filterSize) {

        if (filterSize % 2 == 1) {
            throw new IllegalArgumentException("even filter size");
        }
        
        int halfFilterSize = filterSize / 2;
        int rowAbove = Math.max(0, row - halfFilterSize);
        int rowBelow = Math.min(grid.getRows() - 1, row + halfFilterSize);
        int colLeft = Math.max(0, col - halfFilterSize);
        int colRight = Math.min(grid.getCols() - 1, col + halfFilterSize);

        final double cellSize = grid.getCellSize();
        final float inverseDoubleMeshSize = (float) (1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float) (1 / (cellSize * cellSize));
        final float z1 = grid.getValue(rowAbove, colRight); // top right
        final float z2 = grid.getValue(row, colRight); // right
        final float z3 = grid.getValue(rowBelow, colRight); // bottom right
        final float z4 = grid.getValue(rowBelow, col); // bottom
        final float z5 = grid.getValue(rowBelow, colLeft); // bottom left
        final float z6 = grid.getValue(row, colLeft); // left
        final float z7 = grid.getValue(rowAbove, colLeft); // top left
        final float z8 = grid.getValue(rowAbove, col); // top
        final float z9 = grid.getValue(row, col); // center

        final float zx = (z2 - z6) * inverseDoubleMeshSize;
        final float zy = (z8 - z4) * inverseDoubleMeshSize;
        final float zxx = (z2 - 2 * z9 + z6) * inverseSquareMeshSize;
        final float zyy = (z8 - 2 * z9 + z4) * inverseSquareMeshSize;
        final float zxy = (-z7 + z1 + z5 - z3) * 0.25f * inverseSquareMeshSize;
        final float p = zx * zx + zy * zy;
        final float q = p + 1;

        final float divider = (float) (p * q * Math.sqrt(q));
        if (divider != 0) {
            return (zxx * zx * zx + 2 * zxy * zx * zy + zyy * zy * zy) / divider * 100;
        }
        return 0;
        
    }
/*
    public static void main(String[] args) {
        try {
            
            String path = ika.utils.FileUtils.askFile(null, "Select ESRI ASCII Grid File", true);
            GeoGrid geoGrid = ika.geoimport.ESRIASCIIGridReader.read(path);
            ika.geoexport.ESRIASCIIGridExporter exporter = new ika.geoexport.ESRIASCIIGridExporter();
            {
                GridProfileCurvatureOperator op = new GridProfileCurvatureOperator();
                GeoGrid out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/profilecurv3x3.asc");
                
                op.setFilterSize(5);
                out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/profilecurv5x5.asc");
                
            }
            
            {
                GridPlanCurvatureOperator op = new GridPlanCurvatureOperator();
                GeoGrid out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/plancurv.asc");
            }
            
            {
                GridMaximumCurvatureOperator op = new GridMaximumCurvatureOperator();
                GeoGrid out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/maxcurv.asc");
            }
            
            {
                GridMinimumCurvatureOperator op = new GridMinimumCurvatureOperator();
                GeoGrid out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/mincurv.asc");
            }
            
            {
                GridShadeOperator op = new GridShadeOperator();
                GeoGrid out = op.operate(geoGrid);
                ika.geo.GeoImage image = new GridToImageOperator().operate(out);
                javax.imageio.ImageIO.write(image.getBufferedImage(), "png", new java.io.File("/Users/jenny/Desktop/shading.png"));
            }
            
            System.exit(0);
        } catch (Exception ex) {
            System.err.print(ex);
        }
        
    }
*/   
    public String getName() {
        return "Profile Curvature";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int halfFilterSize = filterSize / 2;
        
        final int cols = geoGrid.getCols();
        final int rows = geoGrid.getRows();
        final double cellSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(cols, rows, cellSize);
        newGrid.setName(this.getName());
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());
        
        final float inverseDoubleMeshSize = (float)(1d / (2d * cellSize));
        final float inverseSquareMeshSize = (float)(1d / (cellSize * cellSize));
  
        // top rows
        for (int row = 0; row < halfFilterSize; row++) {
            for (int col = 0; col < cols; col++) {
                this.operateBorder(geoGrid, newGrid, col, row, cellSize);
            }
        }
        // bottom rows
        for (int row = rows - halfFilterSize; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                this.operateBorder(geoGrid, newGrid, col, row, cellSize);
            }
        }
        // left columns
        for (int col = 0; col < halfFilterSize; col++) {
            for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
                this.operateBorder(geoGrid, newGrid, col, row, cellSize);
            }
        }
        // right columns
        for (int col = cols - halfFilterSize; col < cols; col++) {
            for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
                this.operateBorder(geoGrid, newGrid, col, row, cellSize);
            }
        }
        // interior of grid
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
            for (int col = halfFilterSize; col < cols - halfFilterSize; col++) {
                
                final float z1 = srcGrid[row-halfFilterSize][col+halfFilterSize]; // top right
                final float z2 = srcGrid[row][col+halfFilterSize]; // right
                final float z3 = srcGrid[row+halfFilterSize][col+halfFilterSize]; // bottom right
                final float z4 = srcGrid[row+halfFilterSize][col]; // bottom
                final float z5 = srcGrid[row+halfFilterSize][col-halfFilterSize]; // bottom left
                final float z6 = srcGrid[row][col-halfFilterSize]; // left
                final float z7 = srcGrid[row-halfFilterSize][col-halfFilterSize]; // top left
                final float z8 = srcGrid[row-halfFilterSize][col]; // top
                final float z9 = srcGrid[row][col]; // center
                
                final float zx = (z2 - z6) * inverseDoubleMeshSize;
                final float zy = (z8 - z4) * inverseDoubleMeshSize;
                final float zxx = (z2  - 2 * z9 + z6) * inverseSquareMeshSize;
                final float zyy = (z8 - 2 * z9 + z4) * inverseSquareMeshSize;
                final float zxy = (-z7 + z1 + z5 - z3) * 0.25f * inverseSquareMeshSize;
                final float p = zx * zx + zy *zy;
                final float q = p + 1;
                
                final float divider = (float)(p * q * Math.sqrt(q));
                if (divider != 0) {
                    dstGrid[row][col] = (zxx * zx * zx + 2 * zxy * zx * zy + zyy * zy * zy) / divider * 100;
                }
                
            }
        }
        
        return newGrid;
    }
    
    private void operateBorder(GeoGrid src, GeoGrid dst, int col, int row, double cellSize) {

        final int halfFilterSize = filterSize / 2;
        
        final float inverseDoubleMeshSize = (float) (1 / (2 * cellSize));
        final float inverseSquareMeshSize = (float) (1 / (cellSize * cellSize));

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        
        final int cols = src.getCols();
        final int rows = src.getRows();

        final int rm = row - halfFilterSize < 0 ? 0 : row - halfFilterSize;
        final int rp = row + halfFilterSize >= rows ? rows - halfFilterSize : row + halfFilterSize; 
        final int cm = col - halfFilterSize < 0 ? 0 : col - halfFilterSize;
        final int cp = col + halfFilterSize >= cols ? cols - halfFilterSize : col + halfFilterSize;
        
        final float z1 = srcGrid[rm][cp]; // top right
        final float z2 = srcGrid[row][cp]; // right
        final float z3 = srcGrid[rp][cp]; // bottom right
        final float z4 = srcGrid[rp][col]; // bottom
        final float z5 = srcGrid[rp][cm]; // bottom left
        final float z6 = srcGrid[row][cm]; // left
        final float z7 = srcGrid[rm][cm]; // top left
        final float z8 = srcGrid[rm][col]; // top
        final float z9 = srcGrid[row][col]; // center

        final float zx = (z2 - z6) * inverseDoubleMeshSize;
        final float zy = (z8 - z4) * inverseDoubleMeshSize;
        final float zxx = (z2 - 2 * z9 + z6) * inverseSquareMeshSize;
        final float zyy = (z8 - 2 * z9 + z4) * inverseSquareMeshSize;
        final float zxy = (-z7 + z1 + z5 - z3) * 0.25f * inverseSquareMeshSize;
        final float p = zx * zx + zy * zy;
        final float q = p + 1;

        final float divider = (float) (p * q * Math.sqrt(q));
        if (divider != 0) {
            dstGrid[row][col] = (zxx * zx * zx + 2 * zxy * zx * zy + zyy * zy * zy) / divider * 100;
        }
    }

    public int getFilterSize() {
        return filterSize;
    }

    public void setFilterSize(int filterSize) {
        if (filterSize % 2 == 0 || filterSize < 3) {
            throw new IllegalArgumentException("illegal filter size");
        }
        this.filterSize = filterSize;
    }

}
