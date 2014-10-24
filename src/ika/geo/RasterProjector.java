/*
 * RasterProjector.java
 *
 * Created on June 5, 2007, 9:13 AM
 *
 */

package ika.geo;

import com.jhlabs.map.proj.Projection;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class RasterProjector {
    
    /** Creates a new instance of RasterProjector */
    public RasterProjector() {
    }
    
    /**
     * Find the extension of the projected grid or image by projecting the border
     * of the longitude / latitude graticule. This assumes that the projection
     * does not fold or otherwise distorts space in an unusual way.
     * @param projection The projection to transform the raster.
     * @param raster The raster to project in degrees, not radians.
     */
    public static Rectangle2D.Double findProjectedExtension(Projection projection, 
            AbstractRaster raster) {
        
        double projWest = Double.MAX_VALUE;
        double projEast = -Double.MAX_VALUE;
        double projSouth = Double.MAX_VALUE;
        double projNorth = -Double.MAX_VALUE;
        
        
        final double lon0 = projection.getProjectionLongitude();
        projection.setProjectionLongitude(0);
        projection.initialize();
        
        final double gridWest, gridEast, gridSouth, gridNorth, cellSize;
        final int gridCols, gridRows;
        if (raster == null) {
            gridWest = -180;
            gridEast = 180;
            gridSouth = -90;
            gridNorth = 90;
            cellSize = 1. / 60; // 1 minute
            gridCols = 360 * 60;
            gridRows = 180 * 60;
        } else {
            gridWest = Math.max(-180, raster.getWest());
            gridEast = Math.min(180, raster.getEast());
            gridSouth = Math.max(-90, raster.getSouth());
            gridNorth = Math.min(90, raster.getNorth());
            cellSize = raster.getCellSize();
            gridCols = raster.getCols();
            gridRows = raster.getRows();
        }
        java.awt.geom.Point2D.Double pt = new java.awt.geom.Point2D.Double();
        
        // top row
        for (int c = 0; c < gridCols; c++) {
            final double lon = gridWest + c * cellSize;
            projection.transform(lon, gridNorth, pt);
            if (pt.x < projWest) {
                projWest = pt.x;
            }
            if (pt.x > projEast) {
                projEast = pt.x;
            }
            if (pt.y < projSouth) {
                projSouth = pt.y;
            }
            if (pt.y > projNorth) {
                projNorth = pt.y;
            }
        }
        
        // bottom row
        for (int c = 0; c < gridCols; c++) {
            final double lon = gridWest + c * cellSize;
            projection.transform(lon, gridSouth, pt);
            if (pt.x < projWest) {
                projWest = pt.x;
            }
            if (pt.x > projEast) {
                projEast = pt.x;
            }
            if (pt.y < projSouth) {
                projSouth = pt.y;
            }
            if (pt.y > projNorth) {
                projNorth = pt.y;
            }
        }
        
        // left column
        for (int r = 0; r < gridRows; r++) {
            final double lat = gridSouth + r * cellSize;
            projection.transform(gridWest, lat, pt);
            if (pt.x < projWest) {
                projWest = pt.x;
            }
            if (pt.x > projEast) {
                projEast = pt.x;
            }
            if (pt.y < projSouth) {
                projSouth = pt.y;
            }
            if (pt.y > projNorth) {
                projNorth = pt.y;
            }
        }
        
        // right column
        for (int r = 0; r < gridRows; r++) {
            final double lat = gridSouth + r * cellSize;
            projection.transform(gridEast, lat, pt);
            if (pt.x < projWest) {
                projWest = pt.x;
            }
            if (pt.x > projEast) {
                projEast = pt.x;
            }
            if (pt.y < projSouth) {
                projSouth = pt.y;
            }
            if (pt.y > projNorth) {
                projNorth = pt.y;
            }
        }
       
        projection.setProjectionLongitude(lon0);
        projection.initialize();
        
        return new Rectangle2D.Double(projWest, projSouth, 
                projEast - projWest, projNorth - projSouth);
    }
}
