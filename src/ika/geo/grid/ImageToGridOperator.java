/*
 * ImageToGridOperator.java
 *
 * Created on October 25, 2007, 4:20 PM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoImage;
import java.awt.image.BufferedImage;

/**
 * Convert a GeoImage to a GeoGrid. RGB colors are converted to gray and stored
 * with a range of 0 to 255.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ImageToGridOperator {
    
    /** Creates a new instance of ImageToGridOperator */
    public ImageToGridOperator() {
    }
    
    /** Returns a descriptive name of this GridOperator
     * @return The name of this GridOperator.
     */
    public String getName() {
        return "Grid to Image";
    }
    
    public GeoGrid operate (GeoImage geoImage) {       
        int cols = geoImage.getCols();
        int rows = geoImage.getRows();
        double cellSize = geoImage.getCellSize();
        GeoGrid grid = new GeoGrid(cols, rows, cellSize);
        grid.setWest(geoImage.getWest() + 0.5 * cellSize);
        grid.setNorth(geoImage.getNorth() - 0.5 * cellSize);
        
        BufferedImage image = geoImage.getBufferedImage();
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final int rgb = image.getRGB(col, row);
                
                // extract r, g and b values from the combined integer value.
                final int r = (rgb >> 16) & 255;
                final int g = (rgb >> 8) & 255;
                final int b = rgb & 255;
                // compute the weighted gray value from the red, green and blue values
                // see e.g.: http://de.wikipedia.org/wiki/RGB-Farbraum
                final float gray = 0.299f * r + 0.587f * g + 0.114f * b;
                grid.setValue(gray, col, row);
            }
        }
        return grid;
    }
}
