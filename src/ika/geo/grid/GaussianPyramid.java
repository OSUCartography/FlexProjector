package ika.geo.grid;

import ika.geo.GeoGrid;
import java.util.ArrayList;

/**
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GaussianPyramid {

    private static int MIN_SIDE_LENGTH = 2;
    private GeoGrid[] pyramid;
    
    public static GeoGrid[] createPyramid(GeoGrid geoGrid, int maxLevelsCount) {
        return GaussianPyramid.createPyramid(geoGrid, maxLevelsCount, 
                MIN_SIDE_LENGTH * MIN_SIDE_LENGTH);
    }
    
    public static GeoGrid[] createPyramid(GeoGrid geoGrid,
            int maxLevelsCount,
            int minCellCount) {

        ArrayList<GeoGrid> pyramid = new ArrayList<GeoGrid>();
        pyramid.add(geoGrid);
        Convolution5x5 conv = new Convolution5x5();
        for (;;) {
            int newCols = geoGrid.getCols() / 2;
            int newRows = geoGrid.getRows() / 2;
            if (geoGrid == null 
                    || newCols <= MIN_SIDE_LENGTH
                    || newRows <= MIN_SIDE_LENGTH
                    || newCols * newRows < minCellCount
                    || pyramid.size() == maxLevelsCount) {
                break;
            }
            geoGrid = conv.convolveToHalfSize(geoGrid);
            //ESRIASCIIGridExporter.quickExport(geoGrid, "/Users/jenny/Desktop/DEM" + pyramid.size()+".asc");
            pyramid.add(geoGrid);
        }

        return pyramid.toArray(new GeoGrid[pyramid.size()]);

    }

    public GaussianPyramid(GeoGrid geoGrid) {
        this.pyramid = GaussianPyramid.createPyramid(geoGrid, 9999);
    }

    public GaussianPyramid(GeoGrid geoGrid, int maxLevelsCount) {
        this.pyramid = GaussianPyramid.createPyramid(geoGrid, maxLevelsCount);
    }

    public GaussianPyramid(GeoGrid geoGrid, int maxLevelsCount, int minCellCount) {
        this.pyramid = GaussianPyramid.createPyramid(geoGrid, maxLevelsCount, minCellCount);
    }

    public GeoGrid[] getPyramid() {
        return this.pyramid;
    }

    public GeoGrid getFullResolutionLevel() {
        return this.pyramid[0];
    }

    /**
     * Returns a grid at a specified pyramid level. The full resolution grid
     * has level 0, the lowest resolution grid has level getLevelsCount() - 1
     * @param level
     * @return GeoGrid at requested level
     */
    public GeoGrid getLevel (int level) {
        return this.pyramid[level];
    }

    public int getLevelsCount() {
        return this.pyramid.length;
    }

    public float getValue(int col, int row, int pyramidLevel) {
        return this.pyramid[pyramidLevel].getValue(col, row);
    }
}
