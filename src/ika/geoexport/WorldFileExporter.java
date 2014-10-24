/*
 * WorldFileExporter.java
 *
 * Created on June 6, 2007, 9:24 PM
 *
 */

package ika.geoexport;

import ika.geo.GeoImage;
import ika.utils.FileUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Write world file for a georeferenced image.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class WorldFileExporter {
    
    private WorldFileExporter() {
    }
    
    /**
     * Construct a world file path for for a passed image file path.
     * @param imageFilePath The path to the image file.
     * @return The path to the world file.
     */
    public static String constructPath(String imageFilePath) {
        String ext = FileUtils.getFileExtension(imageFilePath);
        switch (ext.length()) {
            case 0:
                ext = "w";
                break;
            case 1:
            case 2:
                ext = ext + "w";
                break;
            default:
                ext = ext.substring(0, 1)
                + ext.substring(ext.length()-1, ext.length())
                + "w";
                break;
        }
        return FileUtils.replaceExtension(imageFilePath, ext);
    }
    
    public static void writeWorldFile(String worldFilePath, GeoImage geoImage)
    throws IOException{
        
        WorldFileExporter.writeWorldFile(worldFilePath,
                geoImage.getCellSize(),
                geoImage.getWest(),
                geoImage.getNorth());
    }
    
    public static void writeWorldFile(String worldFilePath, double cellSize,
            double west, double north)
            throws IOException{
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(worldFilePath)));
            writer.println(cellSize);
            writer.println(0);
            writer.println(0);
            writer.println(-cellSize);
            writer.println(west);
            writer.println(north);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
