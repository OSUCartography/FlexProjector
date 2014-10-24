/*
 * GridAspectSobelOperator.java
 *
 * Created on February 6, 2006, 11:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ika.geo.grid;

import ika.geo.*;
import ika.utils.GeometryUtils;

/**
 *
 * @author jenny
 */
public class GridAspectSobelOperator implements GridOperator {
    
    /** Creates a new instance of GridAspectSobelOperator */
    public GridAspectSobelOperator() {
    }
  
    public String getName() {
        return "Aspect Sobel";
    }

    public GeoObject operate(GeoGrid geoGrid) {
    if (geoGrid == null)
            throw new IllegalArgumentException();
        
        final int newCols = geoGrid.getCols() - 2;
        final int newRows = geoGrid.getRows() - 2;
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(newCols, newRows, meshSize);
        newGrid.setWest(geoGrid.getWest() + meshSize);
        newGrid.setNorth(geoGrid.getNorth() + meshSize);
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();
        
        for (int row = 1; row < srcRows - 1; row++) {
            float[] dstRow = dstGrid[row-1];
            for (int col = 1; col < srcCols - 1; col++) {
                /*
                 * |A B C|
                 * |D 0 E|
                 * |F G H|
                 */
                final float a = srcGrid[row-1][col-1];
                final float b = srcGrid[row-1][col];
                final float c = srcGrid[row-1][col+1];
                final float d = srcGrid[row][col-1];
                final float e = srcGrid[row][col+1];
                final float f = srcGrid[row+1][col-1];
                final float g = srcGrid[row+1][col];
                final float h = srcGrid[row+1][col+1];
                
                
                //final float val = (Math.abs(-a-2*b-c+f+2*g+h) + Math.abs(-c-2*e-h+a+2*d+f)) / 8;
                
                float vhor, vver;
                vhor = GeometryUtils.angleDif(-a, 2*b);
                vhor = GeometryUtils.angleDif(vhor, c);
                vhor = GeometryUtils.angleSum(vhor, f);
                vhor = GeometryUtils.angleSum(vhor, 2*g);
                vhor = GeometryUtils.angleSum(vhor, h);
                
                vver = GeometryUtils.angleDif(-c, 2*e);
                vver = GeometryUtils.angleDif(vver, h);
                vver = GeometryUtils.angleSum(vver, a);
                vver = GeometryUtils.angleSum(vver, 2*d);
                vver = GeometryUtils.angleSum(vver, f);
                
                dstRow[col-1] = (Math.abs(vhor) + Math.abs(vver)) / 8;
            }
        }
   
        return newGrid;
    }

}

