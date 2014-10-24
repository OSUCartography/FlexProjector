package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class Convolution5x5 {

    private float wa = 0.4f;
    private float wb = 0.25f;
    private float wc = 0.05f;

    public Convolution5x5() {
    }

    public GeoGrid convolveToHalfSize(GeoGrid geoGrid) {

        final int rows = geoGrid.getRows();
        final int cols = geoGrid.getCols();
        if (cols < 4 || rows < 4) {
            return null;
        }

        final int newCols = cols / 2 + cols % 2;
        final int newRows = rows / 2 + rows % 2;
        GeoGrid convGrid = new GeoGrid(newCols, newRows, geoGrid.getCellSize() * 2);
        convGrid.setWest(geoGrid.getWest());
        convGrid.setNorth(geoGrid.getNorth());

        // top and bottom rows
        for (int c = 0; c < cols; c += 2) {
            float g = this.convolveBorder(geoGrid, c, 0);
            convGrid.setValue(g, c / 2, 0);
            g = this.convolveBorder(geoGrid, c, rows - 1);
            convGrid.setValue(g, c / 2, rows / 2 - 1 + rows % 2);
        }

        // left and right columns
        for (int r = 0; r < rows; r += 2) {
            float g = this.convolveBorder(geoGrid, 0, r);
            convGrid.setValue(g, 0, r / 2);
            g = this.convolveBorder(geoGrid, cols - 1, r);
            convGrid.setValue(g, cols / 2 - 1 + cols % 2, r / 2);
        }

        // interior of grid
        for (int r = 2; r < rows - 2; r += 2) {
            for (int c = 2; c < cols - 2; c += 2) {
                final float g = this.convolve(geoGrid, c, r);
                convGrid.setValue(g, c / 2, r / 2);
            }
        }

        return convGrid;
    }

    public GeoGrid convolve(GeoGrid geoGrid) {

        final int rows = geoGrid.getRows();
        final int cols = geoGrid.getCols();
        GeoGrid convoluted = new GeoGrid(cols, rows, geoGrid.getCellSize());
        convoluted.setWest(geoGrid.getWest());
        convoluted.setNorth(geoGrid.getNorth());

        // top and bottom rows
        for (int c = 0; c < cols; c++) {
            convoluted.setValue(this.convolveBorder(geoGrid, c, 0), c, 0);
            convoluted.setValue(this.convolveBorder(geoGrid, c, 1), c, 1);
            convoluted.setValue(this.convolveBorder(geoGrid, c, rows - 1), c, rows - 1);
            convoluted.setValue(this.convolveBorder(geoGrid, c, rows - 2), c, rows - 2);
        }

        // left and right columns
        for (int r = 0; r < rows; r++) {
            convoluted.setValue(this.convolveBorder(geoGrid, 0, r), 0, r);
            convoluted.setValue(this.convolveBorder(geoGrid, 1, r), 1, r);
            convoluted.setValue(this.convolveBorder(geoGrid, cols - 1, r), cols - 1, r);
            convoluted.setValue(this.convolveBorder(geoGrid, cols - 2, r), cols - 2, r);
        }

        // interior of grid
        for (int r = 2; r < rows - 2; r++) {
            for (int c = 2; c < cols - 2; c++) {
                convoluted.setValue(this.convolve(geoGrid, c, r), c, r);
            }
        }

        return convoluted;

    }

    private float convolveBorder(GeoGrid geoGrid, int col, int row) {

        final int rows = geoGrid.getRows();

        // mirror values
        /*
        final int r0 = row - 2 < 0 ? row + 2 : row - 2;
        final int r1 = row - 1 < 0 ? row + 1 : row - 1;
        final int r3 = row + 1 >= rows ? row - 1 : row + 1;
        final int r4 = row + 2 >= rows ? row - 2 : row + 2;
         */

        // use border values. First derivative is zero.
        final int r0 = row - 2 < 0 ? 0 : row - 2;
        final int r1 = row - 1 < 0 ? 0 : row - 1;
        final int r3 = row + 1 >= rows ? rows - 1 : row + 1;
        final int r4 = row + 2 >= rows ? rows - 1 : row + 2;

        float v0 = convolveBorderRow(geoGrid, col, r0);
        float v1 = convolveBorderRow(geoGrid, col, r1);
        float v2 = convolveBorderRow(geoGrid, col, row);
        float v3 = convolveBorderRow(geoGrid, col, r3);
        float v4 = convolveBorderRow(geoGrid, col, r4);
        final float res = wc * (v0 + v4) + wb * (v1 + v3) + wa * v2;
        return Float.isNaN(res) ? convolveWithVoid(geoGrid, col, row) : res;
    }

    private float convolveBorderRow(GeoGrid geoGrid, int col, int row) {

        final int cols = geoGrid.getCols();

        // mirror
        /*
        final int c0 = col - 2 < 0 ? col + 2 : col - 2;
        final int c1 = col - 1 < 0 ? col + 1 : col - 1;
        final int c3 = col + 1 >= cols ? col - 1 : col + 1;
        final int c4 = col + 2 >= cols ? col - 2 : col + 2;
         */

        final int c0 = col - 2 < 0 ? 0 : col - 2;
        final int c1 = col - 1 < 0 ? 0 : col - 1;
        final int c3 = col + 1 >= cols ? cols - 1 : col + 1;
        final int c4 = col + 2 >= cols ? cols - 1 : col + 2;

        final float v0 = geoGrid.getValue(c0, row);
        final float v1 = geoGrid.getValue(c1, row);
        final float v2 = geoGrid.getValue(col, row);
        final float v3 = geoGrid.getValue(c3, row);
        final float v4 = geoGrid.getValue(c4, row);
        return wc * (v0 + v4) + wb * (v1 + v3) + wa * v2;

    }

    private float convolve(GeoGrid geoGrid, int col, int row) {
        
        final float v0 = convolveRow(geoGrid, col, row - 2);
        final float v1 = convolveRow(geoGrid, col, row - 1);
        final float v2 = convolveRow(geoGrid, col, row);
        final float v3 = convolveRow(geoGrid, col, row + 1);
        final float v4 = convolveRow(geoGrid, col, row + 2);
        final float res = wc * (v0 + v4) + wb * (v1 + v3) + wa * v2;
        return Float.isNaN(res) ? convolveWithVoid(geoGrid, col, row) : res;
        
    }

    private float convolveRow(GeoGrid geoGrid, int col, int row) {
        
        final float v0 = geoGrid.getValue(col - 2, row);
        final float v1 = geoGrid.getValue(col - 1, row);
        final float v2 = geoGrid.getValue(col, row);
        final float v3 = geoGrid.getValue(col + 1, row);
        final float v4 = geoGrid.getValue(col + 2, row);
        return wc * (v0 + v4) + wb * (v1 + v3) + wa * v2;
        
    }

    private float convolveWithVoid(float v0, float v1, float v2, float v3, float v4) {

        float totW = 0f;
        float res = 0f;
        if (!Float.isNaN(v0)) {
            res = wc * v0;
            totW = wc;
        }
        if (!Float.isNaN(v1)) {
            res += wb * v1;
            totW += wb;
        }
        if (!Float.isNaN(v2)) {
            res += wa * v2;
            totW += wa;
        }
        if (!Float.isNaN(v3)) {
            res += wb * v3;
            totW += wb;
        }
        if (!Float.isNaN(v4)) {
            res += wc * v4;
            totW += wc;
        }
        if (totW == 0) {
            return Float.NaN;
        }
        final float scale = (2 * (wc + wb) + wa) / totW;
        return res * scale;

    }

    private float convolveWithVoid(GeoGrid geoGrid, int col, int row) {

        final int rows = geoGrid.getRows();
        final float v0 = convolveRowWithVoid(geoGrid, col, Math.max(0, row - 2));
        final float v1 = convolveRowWithVoid(geoGrid, col, Math.max(0, row - 1));
        final float v2 = convolveRowWithVoid(geoGrid, col, row);
        final float v3 = convolveRowWithVoid(geoGrid, col, Math.min(rows - 1, row + 1));
        final float v4 = convolveRowWithVoid(geoGrid, col, Math.min(rows - 1, row + 2));
        return convolveWithVoid(v0, v1, v2, v3, v4);

    }

    private float convolveRowWithVoid(GeoGrid geoGrid, int col, int row) {

        final int cols = geoGrid.getCols();
        final float v0 = geoGrid.getValue(Math.max(0, col - 2), row);
        final float v1 = geoGrid.getValue(Math.max(0, col - 1), row);
        final float v2 = geoGrid.getValue(col, row);
        final float v3 = geoGrid.getValue(Math.min(cols - 1, col + 1), row);
        final float v4 = geoGrid.getValue(Math.min(cols - 1, col + 2), row);
        return convolveWithVoid(v0, v1, v2, v3, v4);

    }
}
