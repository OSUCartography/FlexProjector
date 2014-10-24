/*
 * GridMedianOperator.java
 *
 * Created on August 12, 2008
 *
 */
package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geoimport.EsriASCIIGridReader;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class AccumulationFlowOperator implements GridOperator {

    public static void main(String[] args) {
        try {
            GeoGrid geoGrid = EsriASCIIGridReader.read("/Users/jenny/Desktop/1024x1024.asc");
            GeoGrid accflow = new AccumulationFlowOperator().operate(geoGrid);
            ika.geo.GeoImage image = new GridToImageOperator().operate(accflow);
            ika.utils.ImageUtils.displayImageInWindow(image.getBufferedImage());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private enum FlowDirection {

        NONE, TOPRIGHT, RIGHT, BOTTOMRIGHT, BOTTOM, BOTTOMLEFT, LEFT, TOPLEFT, TOP
    };

    public AccumulationFlowOperator() {
    }

    public String getName() {
        return "Accumulation Flow";
    }

    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }

        final int cols = geoGrid.getCols();
        final int rows = geoGrid.getRows();
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(cols, rows, meshSize);
        newGrid.setWest(geoGrid.getWest());
        newGrid.setNorth(geoGrid.getNorth());

        for (int row = 1; row < rows - 1; row++) {
            System.out.println(row);
            for (int col = 1; col < cols - 1; col++) {
                incrementFlow(geoGrid, newGrid, col, row);
            }
        }

        return newGrid;
    }

    private void incrementFlow(GeoGrid dem, GeoGrid accflow, int col, int row) {

        for (;;) {
            FlowDirection dir = FlowDirection.NONE;
            try {
                dir = this.getFlowDirection(dem, col, row);
            } catch (IndexOutOfBoundsException exc) {
                break;
            }
            switch (dir) {
                case NONE:
                    break;
                case TOPRIGHT:
                    col++;
                    row--;
                    break;
                case RIGHT:
                    col++;
                    break;
                case BOTTOMRIGHT:
                    col++;
                    row++;
                    break;
                case BOTTOM:
                    row++;
                    break;
                case BOTTOMLEFT:
                    col--;
                    row++;
                    break;
                case LEFT:
                    col--;
                    break;
                case TOPLEFT:
                    col--;
                    row--;
                    break;
                case TOP:
                    row--;
                    break;
            }
            try {
                float v = accflow.getValue(col, row);
                accflow.setValue(v + 1, col, row);
            } catch (IndexOutOfBoundsException exc) {
                break;
            }
        }
    }

    private FlowDirection getFlowDirection(GeoGrid geoGrid, int col, int row) {

        // search for direction of flow
        final float hc = geoGrid.getValue(col, row);
        FlowDirection dir = FlowDirection.NONE;
        float diff = 0;
        float d = geoGrid.getValue(col + 1, row - 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.TOPRIGHT;
        }
        d = geoGrid.getValue(col + 1, row) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.RIGHT;
        }
        d = geoGrid.getValue(col + 1, row + 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.BOTTOMRIGHT;
        }
        d = geoGrid.getValue(col, row + 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.BOTTOM;
        }
        d = geoGrid.getValue(col - 1, row + 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.BOTTOMLEFT;
        }
        d = geoGrid.getValue(col - 1, row) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.LEFT;
        }
        d = geoGrid.getValue(col - 1, row - 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.TOPLEFT;
        }
        d = geoGrid.getValue(col, row - 1) - hc;
        if (d < diff) {
            diff = d;
            dir = FlowDirection.TOP;
        }
        return dir;
    }
}
