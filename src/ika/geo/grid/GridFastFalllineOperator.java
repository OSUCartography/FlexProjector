/*
 * GridSlopeLineOperator.java
 *
 * Created on May 25, 2006, 2:23 PM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;
import ika.geo.GeoPath;
import ika.geo.VectorSymbol;
import java.awt.geom.Rectangle2D;

/**
 * Generates a GeoPath following the line of maximum slope. The line jumps
 * from one cell to the next, i.e. the line will have corners with an angle
 * that is a mulitple of 45 degrees.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridFastFalllineOperator implements GridOperator {
    
    public enum Direction {DIRUP, DIRDOWN, DIRBILATERAL};
    
    private Direction direction = Direction.DIRBILATERAL;
    
    private float minVerDiff;
    
    private double startX;
    
    private double startY;
    
    /** Creates a new instance of GridSlopeLineOperator */
    public GridFastFalllineOperator() {
    }
    
    public String getName() {
        return "Slope Line";
    }
    
    public GeoObject operate(GeoGrid geoGrid) {
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        // make sure start point is on grid
        Rectangle2D bounds = geoGrid.getBounds2D(GeoObject.UNDEFINED_SCALE);
        if (bounds.contains(getStartX(), getStartY()) == false)
            return null;
        
        int col = (int)((getStartX() - geoGrid.getWest()) / geoGrid.getCellSize());
        int row = (int)((geoGrid.getNorth() - getStartY()) / geoGrid.getCellSize());
        
        GeoPath geoPath = new GeoPath();
        VectorSymbol vs = new VectorSymbol();
        vs.setScaleInvariant(true);
        geoPath.setVectorSymbol(vs);
        
        switch (getDirection()) {
            case DIRUP:
                this.nextUp(geoGrid, geoPath, col, row);
                break;
            case DIRDOWN:
                this.nextDown(geoGrid, geoPath, col, row);
                break;
            case DIRBILATERAL:
                this.nextUp(geoGrid, geoPath, col, row);
                geoPath.invertDirection();
                this.nextDown(geoGrid, geoPath, col, row);
                break;
        }
        

        return geoPath;
    }
    
    private void nextUp(GeoGrid elevationGrid, GeoPath path, 
            int currentCol, int currentRow) {
        
        long elevRows = elevationGrid.getRows();
        long elevCols = elevationGrid.getCols();
        
        float maxDiff = 0f;
        float hCenter = elevationGrid.getValue(currentCol, currentRow);
        
        float h, diff;
        int nextRow = currentRow;
        int nextCol = currentCol;
        final float sqrt2 = (float)Math.sqrt(2);
        
        // compute difference to 8 neighbors
        h = elevationGrid.getValue(currentCol + 1, currentRow);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol - 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff && diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow + 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow - 1);
        diff = h - hCenter;
        if (diff > maxDiff * sqrt2 && diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow - 1;
        }
        
        // test if a neighbor was found with a difference in elevation that is big enough.
        if (maxDiff == 0) {
            return;
        }
        
        // make sure we don't leave the grid
        if (nextRow == 0 || nextRow == elevRows - 1
                || nextCol == 0 || nextCol == elevCols - 1) {
            return;
        }
        
        double cellSize = elevationGrid.getCellSize();
        double x = elevationGrid.getWest() + currentCol * cellSize;
        double y = elevationGrid.getNorth() - currentRow * cellSize;
        path.moveOrLineTo(x, y);
        
        nextUp(elevationGrid, path, nextCol, nextRow);
    }
    
    private void nextDown(GeoGrid elevationGrid, GeoPath path, 
            int currentCol, int currentRow) {
        
        final float sqrt2 = (float)Math.sqrt(2);
        
        long elevRows = elevationGrid.getRows();
        long elevCols = elevationGrid.getCols();
        
        float maxDiff = 0f;
        float hCenter = elevationGrid.getValue(currentCol, currentRow);
        
        // find biggest elevation difference to all 8 neighbors
        float h, diff;
        int nextRow = currentRow;
        int nextCol = currentCol;
        
        h = elevationGrid.getValue(currentCol + 1, currentRow);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextCol = currentCol - 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff && -diff >= this.minVerDiff) {
            maxDiff = diff;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol + 1, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol + 1;
            nextRow = currentRow - 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow + 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow + 1;
        }
        h = elevationGrid.getValue(currentCol - 1, currentRow - 1);
        diff = h - hCenter;
        if (diff < maxDiff * sqrt2 && -diff >= this.minVerDiff * sqrt2) {
            maxDiff = diff;
            nextCol = currentCol - 1;
            nextRow = currentRow - 1;
        }
       
        
        // test if a neighbor was found with a difference in elevation that is big enough.
        if (maxDiff == 0) {
           return;
        }
       
        // make sure we don't leave the grid
        if (nextRow == 0 || nextRow == elevRows - 1
                || nextCol == 0 || nextCol == elevCols - 1) {
            return;
        }
        
        double cellSize = elevationGrid.getCellSize();
        double x = elevationGrid.getWest() + currentCol * cellSize;
        double y = elevationGrid.getNorth() - currentRow * cellSize;
        path.moveOrLineTo(x, y);
        
        nextDown(elevationGrid, path, nextCol, nextRow);
    }
    
    public float getMinVerDiff() {
        return minVerDiff;
    }

    public void setMinVerDiff(float minVerDiff) {
        this.minVerDiff = minVerDiff;
    }

    public double getStartX() {
        return startX;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public double getStartY() {
        return startY;
    }

    public void setStartY(double startY) {
        this.startY = startY;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
