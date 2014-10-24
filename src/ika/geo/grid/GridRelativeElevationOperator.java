/*
 * GridMeanOperator.java
 *
 * Created on February 3, 2006, 10:06 AM
 *
 */
package ika.geo.grid;

import ika.geo.*;
import java.io.IOException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridRelativeElevationOperator implements GridOperator {
    
    public static void main(String[] args) {
        try {
            String path = "/Users/jenny/Documents/GeoTestData/DEM Data/1024x1024.asc";
            // path = ika.utils.FileUtils.askFile(null, "Select ESRI ASCII Grid File", true);
            GeoGrid geoGrid = ika.geoimport.EsriASCIIGridReader.read(path);
            {
                GridRelativeElevationOperator op = new GridRelativeElevationOperator();
                op.setFilterSize(3);
                GeoGrid out = op.operate(geoGrid);
                ika.geoexport.ESRIASCIIGridExporter.export(out, "/Users/jenny/Desktop/test" + op.filterSize + "x" + op.filterSize +".asc");

                GridSobelOperator sobel = new GridSobelOperator();
                out = sobel.operate(geoGrid);
                ika.geoexport.ESRIASCIIGridExporter.export(out, "/Users/jenny/Desktop/test sobel.asc");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
     
    
    private int filterSize = 5;
    private float percentage = 0.6f;

    /** Creates a new instance of GridMeanOperator */
    public GridRelativeElevationOperator() {
    }

    public GridRelativeElevationOperator(int filterSize) {
        setFilterSize(filterSize);
    }
    
    public String getName() {
        return "Mean";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();        // make sure filterSize is odd number
        }
        if (filterSize % 2 != 1) {
            return null;
        }
        final int halfFilterSize = this.filterSize / 2;

        // compute the size of the new GeoGrid and create it.
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
                float center = srcGrid[row][col];
                float tot = 0;
                for (int r = row - halfFilterSize; r <= row + halfFilterSize; r++) {
                    float[] srcRow = srcGrid[r];
                    for (int c = col - halfFilterSize; c <= col + halfFilterSize; c++) {
                        if (center > srcRow[c]) {
                            ++tot;
                        }
                    }
                }
                dstRow[col] = tot > npts * percentage ? 1 : 0;
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

        dst.getGrid()[row][col] = 0;

    }
}
