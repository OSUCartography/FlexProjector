/*
 * GridMeanOperator.java
 *
 * Created on February 3, 2006, 10:06 AM
 *
 */
package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMeanOperator implements GridOperator {
    /*
    public static void main(String[] args) {
        try {
            String path = ika.utils.FileUtils.askFile(null, "Select ESRI ASCII Grid File", true);
            GeoGrid geoGrid = ika.geoimport.ESRIASCIIGridReader.read(path);
            ika.geoexport.ESRIASCIIGridExporter exporter = new ika.geoexport.ESRIASCIIGridExporter();
            {
                GridMeanOperator op = new GridMeanOperator();
                GeoGrid out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/mean5x5.asc");

                op.setFilterSize(11);
                out = op.operate(geoGrid);
                exporter.export(out, "/Users/jenny/Desktop/mean11x11.asc");
            }
        } catch (IOException ex) {
        }
    }
     */
    
    private int filterSize = 5;

    /** Creates a new instance of GridMeanOperator */
    public GridMeanOperator() {
    }

    public GridMeanOperator(int filterSize) {
        setFilterSize(filterSize);
    }
    
    public String getName() {
        return "Mean";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        // make sure filterSize is odd number
        if (filterSize % 2 != 1) {
            return null;
        }
        final int halfFilterSize = this.filterSize / 2;

        // create the new GeoGrid
        final int rows = geoGrid.getRows();
        final int cols = geoGrid.getCols();
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(cols, rows, meshSize);
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());

        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();

        // top rows
        for (int row = 0; row < halfFilterSize; row++) {
            for (int col = 0; col < cols; col++) {
                this.operateBorder(geoGrid, newGrid, col, row);
            }
        }
        // bottom rows
        for (int row = rows - halfFilterSize; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                this.operateBorder(geoGrid, newGrid, col, row);
            }
        }
        // left columns
        for (int col = 0; col < halfFilterSize; col++) {
            for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
                this.operateBorder(geoGrid, newGrid, col, row);
            }
        }
        // right columns
        for (int col = cols - halfFilterSize; col < cols; col++) {
            for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
                this.operateBorder(geoGrid, newGrid, col, row);
            }
        }

        // interior of grid
        final float npts = this.filterSize * this.filterSize;
        for (int row = halfFilterSize; row < rows - halfFilterSize; row++) {
            float[] dstRow = dstGrid[row];
            for (int col = halfFilterSize; col < cols - halfFilterSize; col++) {
                float tot = 0;
                for (int r = row - halfFilterSize; r <= row + halfFilterSize; r++) {
                    float[] srcRow = srcGrid[r];
                    for (int c = col - halfFilterSize; c <= col + halfFilterSize; c++) {
                        tot += srcRow[c];
                    }
                }
                dstRow[col] = tot / npts;
            }
        }
        return newGrid;
    }

    public GeoGrid operate(GeoGrid geoGrid, int loops) {
        if (loops <= 0) {
            return geoGrid.clone();
        }
        for (int i = 0; i < loops; i++) {
            geoGrid = this.operate(geoGrid);
        }
        return geoGrid;
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

    private void operateBorder(GeoGrid src, GeoGrid dst, int col, int row) {

        final int halfFilterSize = filterSize / 2;

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();

        final int cols = src.getCols();
        final int rows = src.getRows();

        int npts = 0;
        float tot = 0;
        for (int r = row - halfFilterSize; r <= row + halfFilterSize; r++) {
            if (r > 0 && r < rows) {
                float[] srcRow = srcGrid[r];
                for (int c = col - halfFilterSize; c <= col + halfFilterSize; c++) {
                    if (c > 0 && c < cols) {
                        tot += srcRow[c];
                        ++npts;
                    }
                }
            }
        }
        dstGrid[row][col] = tot / npts;

    }
}
