/*
 * ImageImporter.java
 *
 * Created on August 15, 2006, 8:29 PM
 *
 */
package ika.geoimport;

import ika.geo.*;
import ika.gui.ProgressIndicator;
import java.io.*;
import java.awt.image.*;
import ika.utils.*;
import java.awt.Dimension;
import java.net.*;
import javax.imageio.*;
import javax.imageio.stream.*;

/**
 * An importer for raster images that can be georeferenced by a world file.
 * Uses java.io to read various formats (GIF, JPEG). If JAI is installed,
 * additional files can be read (TIFF, Targa and others).
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ImageImporter extends GeoImporter {

    /**
     * Use the DPI information optionally stored in the image file header to 
     * determine the size of the image if there is no world file associated with
     * the image file.
     * The default setting is false, i.e. the DPI information is not per default
     * extracted from the file header to avoid parsing the image if not required. 
     */
    private boolean readDPI = false;
    /**
     * If true, the read image is optimized for fast display on the current
     * main monitor. If this is not wanted, use setOptimizeForDisplay(false)
     * before the image is imported.
     */
    private boolean optimizeForDisplay = true;
    /**
     * True if the imported image is georeferenced, i.e. an associated world
     * file has been found. use isGeoreferenced() to access this flag.
     */
    private boolean georeferenced = false;

    /** Creates a new instance of ImageImporter */
    public ImageImporter() {
    }

    /**
     * Returns the width and height of an image without reading the image pixels.
     * @param url The image
     * @return The dimensions of the image
     * @throws IOException
     */
    public static Dimension getDimensions(java.net.URL url) throws IOException {

        ImageReader reader = null;
        ImageInputStream iis = null;

        try {

            reader = ika.utils.ImageUtils.findImageReader(url);
            if (reader == null) {
                throw new java.io.IOException("The image is not readable.");
            }
            iis = ImageIO.createImageInputStream(url.openStream());
            reader.setInput(iis);
            return new Dimension(reader.getWidth(0), reader.getHeight(0));
        } finally {
            if (reader != null) {
                reader.dispose();
                try {
                    // reader.dispose() does not close the ImageInputStream!
                    // so do this here.
                    if (iis != null) {
                        iis.close();
                    }
                } catch (Exception exc) {
                    // do not throw an exception if an error occurs on closing.
                }
            }
        }
    }

    protected GeoImage importData(java.net.URL url) throws IOException {

        // read the image into a BufferedImage
        MetaBufferedImage metaBufferedImage = this.readImage(url);
        if (metaBufferedImage == null) // if the user cancels, null is returned
        {
            return null;
        }

        // the following cannot be cancelled, so disable the cancel button.
        if (this.getProgressIndicator() != null) {
            this.getProgressIndicator().disableCancel();
        }

        // optimize the image for fast display
        BufferedImage bufferedImage;
        if (optimizeForDisplay) {
            bufferedImage =
                    ImageUtils.optimizeForGraphicsHardware(metaBufferedImage.image);
        } else {
            bufferedImage = metaBufferedImage.image;
        }

        // create the GeoImage
        GeoImage geoImage = new GeoImage/*Pyramid*/(bufferedImage, url);
        geoImage.setName(ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath()));

        // search and read an associated world file containing georeferencing
        // information. If there is a World file we are done.
        URL worldFileURL = WorldFileImporter.searchWorldFile(url);
        this.georeferenced = worldFileURL != null;
        if (worldFileURL != null) {
            WorldFileImporter.readWorldFile(geoImage, worldFileURL);
        } else if (this.readDPI) {
            // No world file found. Use the possibly available dpi information.
            float[] pixelSizeMM =
                    ika.utils.ImageUtils.getPixelSizeMillimeterFromMetadata(
                    metaBufferedImage.meta);
            if (pixelSizeMM != null) {
                geoImage.setCellSize(pixelSizeMM[0] / 1000.);
                geoImage.setNorth(bufferedImage.getHeight() * pixelSizeMM[1] / 1000.);
            }
        }

        // Enable the cancel button again. Maybe the progress indicator will be
        // reused.
        if (this.getProgressIndicator() != null) {
            this.getProgressIndicator().enableCancel();
        }

        return geoImage;
    }

    public String getImporterName() {
        return "Image Importer";
    }

    /**
     * Reads an image from a URL and returns the image.
     * Updates the ProgressIndicator.
     */
    private MetaBufferedImage readImage(java.net.URL url) throws IOException {
        ImageReader reader = null;
        ImageInputStream iis = null;

        try {
            reader = ika.utils.ImageUtils.findImageReader(url);
            if (reader == null) {
                throw new java.io.IOException("The image is not readable.");
            }

            // setup input stream
            iis = ImageIO.createImageInputStream(url.openStream());
            reader.setInput(iis);

            // setup progress indicator
            ProgressIndicator progIndicator = this.getProgressIndicator();
            if (progIndicator != null) {
                ImageImporterProgressAdaptor progressAdaptor =
                        new ImageImporterProgressAdaptor(progIndicator);
                reader.addIIOReadProgressListener(progressAdaptor);

                try {
                    if (url.getProtocol().equals("file")) {
                        if (new java.io.File(url.toURI()).length() > 1024 * 1024 * 2) {
                            progressAdaptor.showDialog();
                        }
                    }
                } catch (Exception exc) {
                }
            }

            // find the image to read and read it
            final int imageIndex = reader.getMinIndex();
            BufferedImage bufferedImage = reader.read(imageIndex);

            // if the user cancels the import, the image is not null,
            // but contains black areas.
            if (this.getProgressIndicator() != null && getProgressIndicator().isAborted()) {
                return null;
            }

            // read the metadata
            javax.imageio.metadata.IIOMetadata meta = null;
            try {
                if (this.readDPI) {
                    meta = reader.getImageMetadata(imageIndex);
                }
            } catch (Exception exc) {
            }

            return new MetaBufferedImage(bufferedImage, meta);
        } finally {
            if (reader != null) {
                reader.dispose();
                try {
                    // reader.dispose() does not close the ImageInputStream!
                    // so do this here.
                    if (iis != null) {
                        iis.close();
                    }
                } catch (Exception exc) {
                    // do not throw an exception if an error occurs on closing.
                }
            }
        }
    }

    /**
     * Searches a URL referencing a data source for an image. This verifies 
     * whether the passed URL contains an image that can be read.
     * @param url The URL to verify.
     * @return The passed URL or null if it does not contain a readable image.
     */
    protected java.net.URL findDataURL(java.net.URL url) {
        if (url == null) {
            return null;
        }

        if (ika.utils.ImageUtils.findImageReader(url) != null) {
            return url;
        }

        return null;
    }

    /**
     * MetaBufferedImage is a helper class that groups a BufferedImage with
     * its associated metadata.
     */
    private class MetaBufferedImage {

        public BufferedImage image;
        public javax.imageio.metadata.IIOMetadata meta;

        public MetaBufferedImage(BufferedImage image,
                javax.imageio.metadata.IIOMetadata meta) {
            this.image = image;
            this.meta = meta;
        }
    }

    /**
     * Returns true if the DPI information should be read if there is no world
     * file associated with this image file.
     */
    public boolean isReadDPI() {
        return readDPI;
    }

    /**
     * Set whether the DPI information should be read if there is no world file
     * associated with this image file.
     */
    public void setReadDPI(boolean readDPI) {
        this.readDPI = readDPI;
    }

    public boolean isOptimizeForDisplay() {
        return optimizeForDisplay;
    }

    public void setOptimizeForDisplay(boolean optimizeForDisplay) {
        this.optimizeForDisplay = optimizeForDisplay;
    }

    public boolean isGeoreferenced() {
        return georeferenced;
    }
}