/*
 * WorldFileImporter.java
 *
 * Created on November 11, 2006, 7:50 PM
 *
 */
package ika.geoimport;

import ika.geo.GeoImage;
import ika.utils.FileUtils;
import ika.utils.URLUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class WorldFileImporter {

    /**
     * Searches a World file containing georeferencing information for a raster image.
     * @param imageURL The URL of the raster image for which a World file has to be searched.
     * @return Returns a File object if found, null otherwise.
     */
    public static URL searchWorldFile(final URL imageURL) {

        String imageFilePath = imageURL.getPath();
        String imageFileExt = FileUtils.getFileExtension(imageFilePath);

        // construct name of the new world file
        // if the extension is shorter than 3 characters, just add a "w" or "W".
        if (imageFileExt.length() < 3) {
            // try with "w".
            String worldFileExt = imageFileExt + "w";
            URL worldFileURL = URLUtils.replaceFileExtension(imageURL, worldFileExt);
            if (URLUtils.resourceExists(worldFileURL)) {
                return worldFileURL;
            }

            // try with a "W"
            worldFileExt = imageFileExt + "W";
            worldFileURL = URLUtils.replaceFileExtension(imageURL, worldFileExt);
            if (URLUtils.resourceExists(worldFileURL)) {
                return worldFileURL;
            }
        }

        // take first and last character of the extension and append "w" or "W"
        // try with "xyw"
        String worldFileExt = "";
        worldFileExt += imageFileExt.charAt(0);
        worldFileExt += imageFileExt.charAt(imageFileExt.length() - 1);
        worldFileExt += 'w';
        URL worldFileURL = URLUtils.replaceFileExtension(imageURL, worldFileExt);
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }

        // try with "xyW"
        worldFileExt = "";
        worldFileExt += imageFileExt.charAt(0);
        worldFileExt += imageFileExt.charAt(imageFileExt.length() - 1);
        worldFileExt += 'W';
        worldFileURL = URLUtils.replaceFileExtension(imageURL, worldFileExt);
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }

        // directly append "w" or "W" to the image file name
        // try with "w"
        worldFileURL = URLUtils.replaceFile(imageURL, imageFilePath + "w");
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }

        // try with "W"
        worldFileURL = URLUtils.replaceFile(imageURL, imageFilePath + "W");
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }

        // use ".w" or ".W" as file extension
        // try with "w"
        worldFileURL = URLUtils.replaceFileExtension(imageURL, "w");
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }
        // try with "W"
        worldFileURL = URLUtils.replaceFileExtension(imageURL, "W");
        if (URLUtils.resourceExists(worldFileURL)) {
            return worldFileURL;
        }

        return null;
    }

    /**
     * Reads georeferencing information for a raster image from a World file and
     * configures a GeoImage accordingly.
     * @param geoImage The GeoImage that will be georeferenced.
     * @param worldFile The World file containing the georeferencing information.
     * @throws java.io.IOException Throws an IOException if any error related to the file occurs.
     */
    public static void readWorldFile(GeoImage geoImage, URL worldFile)
            throws java.io.IOException {

        InputStreamReader isr = new InputStreamReader(worldFile.openStream());
        BufferedReader in = new BufferedReader(isr);
        try {
            double pixelSizeHorizontal = Double.parseDouble(in.readLine());
            double rotX = Double.parseDouble(in.readLine());
            double rotY = Double.parseDouble(in.readLine());
            double pixelSizeVertical = Double.parseDouble(in.readLine());
            double west = Double.parseDouble(in.readLine());
            double north = Double.parseDouble(in.readLine());

            pixelSizeHorizontal = Math.abs(pixelSizeHorizontal);
            pixelSizeVertical = Math.abs(pixelSizeVertical);
            if (pixelSizeHorizontal != pixelSizeVertical) {
                throw new IOException("Horizontal and vertical pixel sizes are" +
                        " different in world file.");
            }
            if (rotX != 0 || rotY != 0) {
                throw new IOException("World file specifies unsupported" +
                        " image rotation.");
            }
            
            geoImage.setCellSize(pixelSizeHorizontal);

            geoImage.setWest(west);
            geoImage.setNorth(north);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception exc) {
                }
            }
        }
    }
}
