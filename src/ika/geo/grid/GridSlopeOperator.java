/*
 * GridSlopeOperator.java
 *
 * Created on January 28, 2006, 2:11 PM
 *
 */
package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author jenny
 */
public class GridSlopeOperator extends ThreadedGridOperator {

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    // load and init data
                    String inputGridPath = ika.utils.FileUtils.askFile(null, "Select an ESRI ASCII Grid", true);
                    if (inputGridPath == null) {
                        System.exit(0);
                    }
                    GeoGrid grid = ika.geoimport.EsriASCIIGridReader.read(inputGridPath);

                    GridSlopeOperator op = new GridSlopeOperator();
                    grid = op.operate(grid);

                    String path = ika.utils.FileUtils.askFile(null, "Export ESRI ASCII Grid",
                            "slope.asc", false, "asc");
                    if (path == null) {
                        return;
                    }

                    ika.geoexport.ESRIASCIIGridExporter.export(grid, path);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                System.exit(0);
            }
        });
    }

    /** Creates a new instance of GridSlopeOperator */
    public GridSlopeOperator() {
    }

    public String getName() {
        return "Grid Slope";
    }

    @Override
    public boolean isOverwrittingSupported() {
        return false;
    }
    
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        // inverse double mesh size
        final double f = 1. / (2. * src.getCellSize());

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int nCols = src.getCols();
        final int nRows = src.getRows();
        final int firstInteriorRow = Math.max(1, startRow);
        final int lastInteriorRow = Math.min(nRows - 1, endRow);
        
        if (startRow == 0) {
            for (int col = 1; col < nCols - 1; col++) {
                final float w = srcGrid[0][col - 1];
                final float e = srcGrid[0][col + 1];
                final float s = srcGrid[1][col];
                final float c = srcGrid[0][col];
                final double dH = (e - w);
                final double dV = (c - s) * 2;
                final float slope = (float) (Math.atan(Math.hypot(dH, dV) * f));
                dstGrid[0][col - 1] = slope;
            }
            // top left corner
            {
                final float c = srcGrid[0][0];
                final float e = srcGrid[0][1];
                final float s = srcGrid[1][0];
                final double dH = (e - c) * 2;
                final double dV = (c - s) * 2;
                final float slope = (float) (Math.atan(
                        Math.hypot(dH, dV) * f));
                dstGrid[0][0] = slope;
            }

            // top right corner
            {
                final float c = srcGrid[0][nCols - 1];
                final float w = srcGrid[0][nCols - 2];
                final float s = srcGrid[1][nCols - 1];
                final double dH = (c - w) * 2;
                final double dV = (c - s) * 2;
                final float slope = (float) (Math.atan(
                        Math.hypot(dH, dV) * f));
                dstGrid[0][nCols - 1] = slope;
            }
        }

        if (endRow == nRows) {
            // bottom row
            for (int col = 1; col < nCols - 1; col++) {
                final float w = srcGrid[nRows - 1][col - 1];
                final float e = srcGrid[nRows - 1][col + 1];
                final float c = srcGrid[nRows - 1][col];
                final float n = srcGrid[nRows - 2][col];
                final double dH = (e - w);
                final double dV = (n - c) * 2;
                final float slope = (float) (Math.atan(Math.hypot(dH, dV) * f));
                dstGrid[nRows - 1][col - 1] = slope;
            }

            // bottom left corner
            {
                final float c = srcGrid[nRows - 1][0];
                final float e = srcGrid[nRows - 1][1];
                final float n = srcGrid[nRows - 2][0];
                final double dH = (e - c) * 2;
                final double dV = (n - c) * 2;
                final float slope = (float) (Math.atan(
                        Math.hypot(dH, dV) * f));
                dstGrid[nRows - 1][0] = slope;
            }

            // bottom right corner
            {
                final float c = srcGrid[nRows - 1][nCols - 1];
                final float w = srcGrid[nRows - 1][nCols - 2];
                final float n = srcGrid[nRows - 2][nCols - 1];
                final double dH = (c - w) * 2;
                final double dV = (n - c) * 2;
                final float slope = (float) (Math.atan(
                        Math.hypot(dH, dV) * f));
                dstGrid[nRows - 1][nCols - 1] = slope;
            }
        }

        // left column
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            final float c = srcGrid[row][0];
            final float e = srcGrid[row][1];
            final float s = srcGrid[row + 1][0];
            final float n = srcGrid[row - 1][0];
            final double dH = (e - c) * 2;
            final double dV = (n - s);
            final float slope = (float) (Math.atan(
                    Math.hypot(dH, dV) * f));
            dstGrid[row][0] = slope;
        }


        // right column
        for (int row = firstInteriorRow; row < lastInteriorRow; row++) {
            final float w = srcGrid[row][nCols - 2];
            final float c = srcGrid[row][nCols - 1];
            final float s = srcGrid[row + 1][nCols - 1];
            final float n = srcGrid[row - 1][nCols - 1];
            final double dH = (c - w) * 2;
            final double dV = (n - s);
            final float slope = (float) (Math.atan(
                    Math.hypot(dH, dV) * f));
            dstGrid[row][nCols - 1] = slope;
        }

        for (int row = firstInteriorRow; row < lastInteriorRow; ++row) {
            float[] dstRow = dstGrid[row];
            for (int col = 1; col < nCols - 1; ++col) {
                final float w = srcGrid[row][col - 1];
                final float e = srcGrid[row][col + 1];
                final float s = srcGrid[row + 1][col];
                final float n = srcGrid[row - 1][col];
                final double dH = (e - w);
                final double dV = (n - s);
                dstRow[col] = (float) (Math.atan(Math.hypot(dH, dV) * f));
            }
        }

    }

}
