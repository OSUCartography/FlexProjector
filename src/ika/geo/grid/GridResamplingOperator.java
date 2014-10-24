/*
 * GridMedianOperator.java
 *
 * Created on February 14, 2006, 9:05 PM
 *
 */
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author jenny
 */
public class GridResamplingOperator implements GridOperator {

    private double newCellSize = 1;
    private double alignX = Double.NaN;
    private double alignY = Double.NaN;

    /** Creates a new instance of GridMedianOperator */
    public GridResamplingOperator(double newCellSize, double alignX, double alignY) {
        this.newCellSize = newCellSize;
        this.alignX = alignX;
        this.alignY = alignY;
    }

    public GridResamplingOperator(double newCellSize) {
        this.newCellSize = newCellSize;
    }
    
    public String getName() {
        return "Resampling";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();

        if (Double.isNaN(this.alignX) || Double.isNaN(this.alignY)) {
            this.alignX = geoGrid.getWest();
            this.alignY = geoGrid.getNorth();
        }
        
        double newWest = geoGrid.getWest(); // alignX - (int)((alignX - geoGrid.getWest()) / newCellSize) * newCellSize; !!! ???
        double newNorth = geoGrid.getNorth(); // alignY + (int)((geoGrid.getNorth() - alignY) / newCellSize) * newCellSize;
        int newRows = (int)((newNorth - geoGrid.getSouth()) / newCellSize);
        int newCols = (int)((geoGrid.getEast() - newWest) / newCellSize);
        GeoGrid newGrid = new GeoGrid(newCols, newRows, newCellSize);
        newGrid.setWest(newWest);
        newGrid.setNorth(newNorth);
        
        for (int row = 0; row < newRows; row++) {
            double y = newNorth - row * newCellSize;
            for (int col = 0; col < newCols; col++) {
                double x = newWest + col * newCellSize;
                float v = geoGrid.getBicubicInterpol(x, y);
                newGrid.setValue(v, col, row);
            }
        }
        return newGrid;
    }
}
