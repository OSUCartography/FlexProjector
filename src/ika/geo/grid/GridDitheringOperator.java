package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;
import ika.geo.GeoPoint;
import ika.geo.GeoSet;

/**
 * Floyd-Steinberg error diffusion dithering.
 *
 * @author Bernie Jenny, Oregon State University
 */
public class GridDitheringOperator implements GridOperator {

    // Floyd Steinberg dithering constants
    private static final float A = 7f / 16f;
    private static final float B = 3f / 16f;
    private static final float C = 5f / 16f;
    private static final float D = 1f / 16f;

    public String getName() {
        return "Floyd-Steinberg Error Diffusion Dithering";
    }

    public GeoObject operate(GeoGrid geoGrid) {
        
        geoGrid = new GridScaleToRangeOperator(0f, 255f).operate(geoGrid);
        
        GeoSet dots = new GeoSet();
        final double dist = geoGrid.getCellSize();
        int nRows = geoGrid.getRows();
        int nCols = geoGrid.getCols();

        double y = geoGrid.getNorth();
        for (int row = 0; row < nRows; row++) {
            double x = geoGrid.getWest();

            // traverse the image in zig-zag order. Even rows from left to right
            // and odd rows from right to left.
            int col = row % 2 == 0 ? 0 : nCols - 1;
            final int inc = row % 2 == 0 ? 1 : -1;
            for (; row % 2 == 0 ? col < nCols : col >= 0; col += inc) {

                final int shadeCol = (int) ((x - geoGrid.getWest()) / dist);
                final int shadeRow = (int) ((geoGrid.getNorth() - y) / dist);
                // FIXME
                if (shadeCol > 0 && shadeRow > 0 && shadeCol < geoGrid.getCols() - 1 && shadeRow < geoGrid.getRows() - 1) {

                    float shade = geoGrid.getValue(shadeCol, shadeRow);
                    final float dif;
                    if (shade < 128) {
                        dots.add(new GeoPoint(x, y));
                        dif = shade;
                    } else {
                        dif = shade - 255;
                    }

                    // Floyd Steinberg error diffusion dithering
                    // right
                    float v = geoGrid.getValue(shadeCol + inc, shadeRow);
                    geoGrid.setValue((short) (v + dif * A), shadeCol + inc, shadeRow);

                    // left bottom
                    v = geoGrid.getValue(shadeCol - inc, shadeRow + 1);
                    geoGrid.setValue((short) (v + dif * B), shadeCol - inc, shadeRow + 1);

                    // center bottom
                    v = geoGrid.getValue(shadeCol, shadeRow + 1);
                    geoGrid.setValue((short) (v + dif * C), shadeCol, shadeRow + 1);

                    // right bottom
                    v = geoGrid.getValue(shadeCol + inc, shadeRow + 1);
                    geoGrid.setValue((short) (v + dif * D), shadeCol + inc, shadeRow + 1);
                }

                x += dist;
            }
            y -= dist;
        }

        return dots;
    }
    
}
