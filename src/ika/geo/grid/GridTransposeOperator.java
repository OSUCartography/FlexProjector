package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author jenny
 */
public class GridTransposeOperator implements GridOperator {

    public String getName() {
        throw new UnsupportedOperationException("Transpose");
    }

    public GeoGrid operate(GeoGrid geoGrid) {

        int cols = geoGrid.getCols();
        int rows = geoGrid.getRows();
        GeoGrid dst = new GeoGrid(rows, cols, geoGrid.getCellSize());
        dst.setWest(geoGrid.getWest());
        dst.setNorth(geoGrid.getNorth());

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                dst.setValue(geoGrid.getValue(c, r), r, c);
            }
        }

        return dst;
    }
}
