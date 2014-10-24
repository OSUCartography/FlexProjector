/*
 * ESRIASCIIGridExporter.java
 *
 * Created on August 14, 2005, 4:17 PM
 *
 */
package ika.geoexport;

import ika.geo.*;
import java.io.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ESRIASCIIGridExporter {

    private ESRIASCIIGridExporter() {
    }

    public static void export(GeoGrid geoGrid, String filePath) throws IOException {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
            String voidValueStr = Float.toString(findVoidValue(geoGrid));
            String lineSeparator = System.getProperty("line.separator");
            writer.write("ncols " + geoGrid.getCols() + lineSeparator);
            writer.write("nrows " + geoGrid.getRows() + lineSeparator);
            writer.write("xllcorner " + geoGrid.getWest() + lineSeparator);
            writer.write("yllcorner " + geoGrid.getSouth() + lineSeparator);
            writer.write("cellsize " + geoGrid.getCellSize() + lineSeparator);
            writer.write("nodata_value " + voidValueStr + lineSeparator);
            float[][] grid = geoGrid.getGrid();
            for (int r = 0; r < grid.length; ++r) {
                for (int c = 0; c < grid[0].length; ++c) {
                    float v = grid[r][c];
                    if (Float.isNaN(v)) {
                        writer.write(voidValueStr);
                    } else {
                        writer.write(Float.toString(v));
                    }
                    writer.write(" ");
                }
                writer.write(lineSeparator);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    private static float findVoidValue(GeoGrid grid) {
        float min = grid.getStatistics().min;
        String voidValue = "-9999";
        while (Float.parseFloat(voidValue) >= min) {
            voidValue += "9";
        }
        return Float.parseFloat(voidValue);
    }
}
