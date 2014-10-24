
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author jenny
 */
public class HalveGrid {

    public static GeoGrid halve(GeoGrid grid) {
        
        int cols = grid.getCols();
        int rows = grid.getRows();
        double cellsize = grid.getCellSize();
        GeoGrid halfGrid = new GeoGrid(cols / 2, rows / 2, cellsize * 2);
        halfGrid.setWest(grid.getWest());
        halfGrid.setNorth(grid.getNorth());
        
        for (int r = 0; r < rows; r += 2) {
            for (int c = 0; c < cols; c += 2) {
                final float v = grid.getValue(c, r);
                halfGrid.setValue(v, c / 2, r / 2);
            }
        }
        
        return halfGrid;
    }
}
