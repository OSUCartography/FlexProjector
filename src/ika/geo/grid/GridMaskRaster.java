/*
 * 
 * 
 */
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMaskRaster extends GaussianPyramid implements GridMask {

    private String path;
    
    public GridMaskRaster(GeoGrid geoGrid, int levelsCount, String path) {
        super(geoGrid, levelsCount);
        this.path = path;
    }

    public final float getWeight(int col, int row, int pyramidLevel) {
        return this.getValue(col, row, pyramidLevel);
    }

    public String getPath() {
        return path;
    }
}
