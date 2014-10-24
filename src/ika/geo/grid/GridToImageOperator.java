package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoImage;
import java.awt.image.*;
import java.util.Arrays;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridToImageOperator implements GridOperator {
    
    /** Creates a new instance of GridToImageOperator */
    public GridToImageOperator() {
    }
    
    public String getName() {
        return "Grid To Image";
    }
    
    public GeoImage operate(GeoGrid grid) {
        
        if (grid == null) {
            throw new IllegalArgumentException();
        }
        
        final int nrows = grid.getRows();
        final int ncols = grid.getCols();
        if (nrows == 0 || ncols == 0) {
            return null;
        }
        
        final float[] minMax = grid.getMinMax();
        BufferedImage image = operate(grid, minMax[0], minMax[1]);
        final double cellSize = grid.getCellSize();
        final double gridWest = grid.getWest() - cellSize / 2.;
        final double gridNorth = grid.getNorth() - cellSize / 2.;
        return new GeoImage(image, gridWest, gridNorth, cellSize);
    }
    
    public BufferedImage operate(GeoGrid grid, float min, float max) {
        
        if (grid == null) {
            throw new IllegalArgumentException();
        }
        
        final int nrows = grid.getRows();
        final int ncols = grid.getCols();
        if (nrows == 0 || ncols == 0) {
            return null;
        }
        
        final float oldRange = max - min;
        
        float[][] srcGrid = grid.getGrid();
        byte[] pixels = new byte [nrows * ncols];
        
        int px = 0;
        if (oldRange != 0) {
            for (int row = 0; row < nrows; ++row) {
                float[] srcRow = srcGrid[row];
                for (int col = 0; col < ncols; ++col) {
                    pixels[px++] = (byte)((srcRow[col] - min) / oldRange * 255.f);
                }
            }
        } else {
            // make white image
            Arrays.fill(pixels, (byte)255);
        }
        
        // Create a BufferedImage of the gray values in bytes.
        BufferedImage bufferedImage = new BufferedImage(ncols, nrows,
                BufferedImage.TYPE_BYTE_GRAY);
        
        // Get the writable raster so that data can be changed.
        WritableRaster wr = bufferedImage.getRaster();
        
        // write the byte data to the raster
        wr.setDataElements(0, 0, ncols, nrows, pixels);
        
        return bufferedImage;
    }
}