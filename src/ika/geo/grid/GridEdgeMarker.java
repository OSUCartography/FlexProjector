/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author jenny
 */
public class GridEdgeMarker {

    public static GeoGrid markEdges(GeoGrid grid) {

        int cols = grid.getCols();
        int rows = grid.getRows();
        GeoGrid markedGrid = new GeoGrid(cols, rows, grid.getCellSize());
        markedGrid.setWest(grid.getWest());
        markedGrid.setNorth(grid.getNorth());

        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {

                if (grid.getValue(c, r) == 1f) {
                    if (grid.getValue(c - 1, r) == 0 
                            || grid.getValue(c + 1, r) == 0 
                            || grid.getValue(c, r - 1) == 0 
                            || grid.getValue(c, r + 1) == 0) {
                        markedGrid.setValue(0.5f, c, r);
                    } else {
                        markedGrid.setValue(1f, c, r);
                    }
                }
            }
        }

        return markedGrid;

    }
}
