package ika.utils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * A utility class for loading, converting and drawing images.
 */
public class ImageUtils {

    /**
     * Required by waitForImage.
     */
    private static final Component sComponent = new Component() {
    };
    /**
     * Required by waitForImage.
     */
    private static final MediaTracker sTracker = new MediaTracker(sComponent);
    /**
     * Required by waitForImage.
     */
    private static int sID = 0;

    /**
     * Waits for an image to load fully. Returns true if everything goes well,
     * false on error. This method should support multi-threading (not tested).<br>
     * From <link>http://examples.oreilly.com/java2d/examples/Utilities.java</link><br>
     * In: Knudsen, J. 1999. Java 2D Graphics. O'Reilly. Page 198.<br>
     * @param image The image to load.
     * @return Returns true if everything goes well, false on error.
     */
    public static boolean waitForImage(Image image) {
        int id;
        synchronized (sComponent) {
            id = sID++;
        }
        sTracker.addImage(image, id);
        try {
            sTracker.waitForID(id);
        } catch (InterruptedException ie) {
            return false;
        }
        if (sTracker.isErrorID(id)) {
            return false;
        }
        return true;
    }

    /**
     * Converts an Image to a rgb BufferedImage. The resulting image does not
     * contain transparent pixels.
     * @param image The image to convert.
     * @return Returns the BufferedImage
     */
    public static BufferedImage makeBufferedImage(Image image) {
        return makeBufferedImage(image, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Converts an Image to a BufferedImage.
     * @param image The image to convert.
     * @param imageType The type of the BufferedImage, e.g. BufferedImage.TYPE_INT_RGB
     * @return Returns the BufferedImage
     */
    public static BufferedImage makeBufferedImage(Image image, int imageType) {
        if (waitForImage(image) == false) {
            return null;
        }
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null), imageType);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, null, null);
        return bufferedImage;
    }

    /**
     * Converts an image to an optimized version for the default screen.<br>
     * The optimized image should draw faster.
     * @param image The image to optimize.
     * @return Returns the optimized image.
     */
    public static BufferedImage optimizeForGraphicsHardware(BufferedImage image) {
        try {
            // create an empty optimized BufferedImage
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            int w = image.getWidth();
            int h = image.getHeight();
            
            // strange things happen on Linux and Windows on some systems when
            // monitors are set to 16 bit.
            boolean bit16 = gc.getColorModel().getPixelSize() < 24;
            final int transp;
            if (bit16) {
                transp = Transparency.TRANSLUCENT;
            } else {
                transp = image.getColorModel().getTransparency();
            }
            BufferedImage optimized = gc.createCompatibleImage(w, h, transp);

            // draw the passed image into the optimized image
            optimized.getGraphics().drawImage(image, 0, 0, null);
            return optimized;
        } catch (Exception e) {
            // return the original image if an exception occured.
            return image;
        }
    }

    /**
     * Converts an image to an optimized version for the default screen.<br>
     * The optimized image should draw faster. Only creates a new image if
     * the passed image is not already optimized.
     * @param image The image to optimize.
     * @return Returns the optimized image.
     */
    public static BufferedImage optimizeForGraphicsHardwareIfRequired(BufferedImage image) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();

            // return passed image if it already is optimized
            if (image.getColorModel().equals(gc.getColorModel())) {
                return image;
            }
            // create an empty optimized BufferedImage
            int w = image.getWidth();
            int h = image.getHeight();

            // strange things happen on Linux and Windows on some systems when
            // monitors are set to 16 bit.
            boolean bit16 = gc.getColorModel().getPixelSize() < 24;
            final int transp;
            if (bit16) {
                transp = Transparency.TRANSLUCENT;
            } else {
                transp = image.getColorModel().getTransparency();
            }
            BufferedImage optimized = gc.createCompatibleImage(w, h, transp);

            // draw the passed image into the optimized image
            optimized.getGraphics().drawImage(image, 0, 0, null);
            return optimized;
        } catch (Exception e) {
            // return the original image if an exception occured.
            return image;
        }
    }

    /**
     * Converts an image to ARGB, i.e. adds a alpha channel if only RGB. Returns the
     * passed image without creating a new image if the passed image already has
     * a alpha channel.
     * @param image The image to convert.
     * @return A new image with an additional alpha channel, or the original
     * image if it already has an alpha channel.
     */
    public static BufferedImage toARGB(BufferedImage image) {
        try {
            if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
                return image;
            }
            int w = image.getWidth();
            int h = image.getHeight();
            BufferedImage d = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            d.getGraphics().drawImage(image, 0, 0, null);
            return d;
        } catch (Exception e) {
            // return the original image if an exception occured.
            return image;
        }
    }

    /**
     * From The Java Developers Almanac 1.4 <br>
     * e674. Creating and Drawing an Accelerated Image
     * This method draws a volatile image and returns it, or possibly a
     * newly created volatile image object. Subsequent calls to this method
     * should always use the returned volatile image.
     * If the contents of the image is lost, it is recreated using orig.
     * img may be null, in which case a new volatile image is created.
     * @param g The destination for drawing.
     * @param volatileImage The image that will be drawn. Might be null, in
     * which case a new volatile image is created.
     * @param x Horizontal position for drawing the image.
     * @param y Vertical position for drawing the image.
     * @return Returns a VolatileImage if one has been created, null otherwise.
     * Pass this VolatileImage to subsequent calls of drawVolatileImage.
     * @param orig The original image that will be converted to a VolatileImage,
     * if volatileImage is null, or if volatileImage is not valid anymore.
     */
    public static VolatileImage drawVolatileImage(Graphics2D g,
            VolatileImage volatileImage,
            int x, int y, Image orig) {

        final boolean VERBOSE = false;

        final int MAX_TRIES = 5;
        for (int i = 0; i < MAX_TRIES; i++) {

            if (VERBOSE) {
                System.out.println("try " + (i + 1));
            }
            if (volatileImage == null) {
                // Create the volatile image
                final int imageWidth = orig.getWidth(null);
                final int imageHeight = orig.getHeight(null);
                volatileImage = g.getDeviceConfiguration().createCompatibleVolatileImage(
                        imageWidth, imageHeight);
                // Copy the original image to accelerated image memory
                Graphics2D gc = (Graphics2D) volatileImage.createGraphics();
                gc.drawImage(orig, 0, 0, null);
                gc.dispose();
                if (VERBOSE) {
                    System.out.println("volatile image was not created before, created new volatile image of size: " + imageWidth + " x " + imageHeight);
                }
            }

            if (volatileImage != null) {
                // Draw the volatile image
                g.drawImage(volatileImage, x, y, null);

                // Check if it is still valid
                if (!volatileImage.contentsLost()) {
                    if (VERBOSE) {
                        System.out.println("drawn volatile image, volatile image still valid, exiting drawVolatileImage");
                    }
                    return volatileImage;
                }

                if (VERBOSE) {
                    System.out.println("drawn volatile image, image not valid");
                }
            }

            // Determine how to fix the volatile image
            switch (volatileImage.validate(g.getDeviceConfiguration())) {
                case VolatileImage.IMAGE_OK:
                    // This should not happen
                    if (VERBOSE) {
                        System.out.println("VolatileImage.validate returned IMAGE_OK");
                    }
                    break;
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    // Create a new volatile image object;
                    // this could happen if the component was moved to another device
                    volatileImage.flush();
                    volatileImage = g.getDeviceConfiguration().createCompatibleVolatileImage(
                            orig.getWidth(null), orig.getHeight(null));
                    if (VERBOSE) {
                        System.out.println("VolatileImage.validate returned IMAGE_INCOMPATIBLE");
                    }
                case VolatileImage.IMAGE_RESTORED:
                    // Copy the original image to accelerated image memory
                    Graphics2D gc = (Graphics2D) volatileImage.createGraphics();
                    gc.drawImage(orig, 0, 0, null);
                    gc.dispose();
                    if (VERBOSE) {
                        System.out.println("VolatileImage.validate returned IMAGE_RESTORED");
                    }
                    break;
            }
        }

        // The image failed to be drawn after MAX_TRIES;
        // draw with the non-accelerated image
        if (VERBOSE) {
            System.out.println("Could not draw volatile image. Will draw normal image.");
        }
        g.drawImage(orig, x, y, null);
        return volatileImage;
    }

    /**
     * Returns true if the specified file extension can be read
     */
    public static boolean canReadImageFile(String fileExt) {
        java.util.Iterator iter = ImageIO.getImageReadersBySuffix(fileExt);
        return iter.hasNext();
    }

    /**
     * Returns true if the specified file can be read
     */
    public static boolean canReadImage(String filePath) {
        String fileExt = FileUtils.getFileExtension(filePath);
        return canReadImageFile(fileExt);
    }

    /**
     * Returns true if the specified file extension can be written.
     */
    public static boolean canWriteImageFile(String fileExt) {
        java.util.Iterator iter = ImageIO.getImageWritersBySuffix(fileExt);
        return iter.hasNext();
    }

    /**
     * Searches an ImageReader for a URL.
     * @param url The URL for which to find an ImageReader.
     * @return An ImageReader or null if none can be found.
     */
    public static ImageReader findImageReader(java.net.URL url) {
        // The WBMPImageReader claims to be able to read many file types
        // that it actually can't read.
        // The work around recomended at
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6331418
        // does not work:
        /* com.sun.imageio.plugins.wbmp.WBMPImageReader wbmpReader
        = (com.sun.imageio.plugins.wbmp.WBMPImageReader)imageReader;
        if (wbmpReader.getOriginatingProvider().canDecodeInput(imin))
        return imageReader; */
        // The bug only seems to appear when using getImageReaders() with 
        // ImageInputStream, which is required when using URLs.
        // The bug can be avoided when the image importer is searched by the 
        // file extension.

        ImageIO.scanForPlugins();

        try {
            // search ImageReader by file extension if URL is a file.
            InputStream in = url.openStream();
            ImageInputStream imin = ImageIO.createImageInputStream(in);
            String ext = ika.utils.FileUtils.getFileExtension(url.getPath());
            java.util.Iterator<ImageReader> iterator = ImageIO.getImageReadersBySuffix(ext);
            if (iterator.hasNext()) {
                ImageReader imageReader = iterator.next();        // search ImageReader by opening and examining the image.
                if (imageReader.getOriginatingProvider().canDecodeInput(imin)) {
                    return imageReader;
                }
            }

            java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(imin);
            if (readers.hasNext()) {
                ImageReader imageReader = readers.next();

                // Don't import WBMP files. See bug description above.
                // The WBMPImageReader class is not always in the same package,
                // so only test for the class name, not the package.
                if (!imageReader.getClass().getSimpleName().equals("WBMPImageReader") && imageReader.getOriginatingProvider().canDecodeInput(imin)) {
                    return imageReader;
                }
            } else {
                return null;
            }

        } catch (Exception exc) {
        }

        return null;
    }

    /**
     * Returns the dpi of an image. If it cannot be determined, returns null.
     * Most graphics formats (including jpg and png - but not gif) have a
     * setting for the image dimension in the x and y direction. This is the size
     * of a pixel in millimeter. JPEGs don't always have a the dpi stored (e.g
     * when saved with Photoshop using the "Save for Web" command).
     *
     * From the DTD:
     * http://java.sun.com/j2se/1.4.2/docs/api/javax/imageio/
     * metadata/doc-files/standard_metadata.html
     *
     * <!ELEMENT "VerticalPixelSize" EMPTY>
     * <!-- The height of a pixel, in millimeters, as it should be
     * rendered on media -->
     * <!ATTLIST "VerticalPixelSize" "value" #CDATA #REQUIRED>
     * <!-- Data type: Float -->
     *
     * @param imageReader An ImageReader with an associated ImageInputStream.
     * @return The dimension of a pixel in millimeters. Returns null if dimension
     * cannot be identified.
     */
    public static float[] getPixelSizeMillimeterFromMetadata(javax.imageio.metadata.IIOMetadata meta) {

        if (meta == null) {
            return null;
        }
        float hps = Float.NaN;
        float vps = Float.NaN;

        try {
            org.w3c.dom.Node n = meta.getAsTree("javax_imageio_1.0");
            n = n.getFirstChild();
            while (n != null) {
                if (n.getNodeName().equals("Dimension")) {
                    org.w3c.dom.Node n2 = n.getFirstChild();
                    while (n2 != null) {
                        if (n2.getNodeName().equals("HorizontalPixelSize")) {
                            org.w3c.dom.NamedNodeMap nnm = n2.getAttributes();
                            org.w3c.dom.Node n3 = nnm.item(0);
                            hps = Float.parseFloat(n3.getNodeValue());
                        }
                        if (n2.getNodeName().equals("VerticalPixelSize")) {
                            org.w3c.dom.NamedNodeMap nnm = n2.getAttributes();
                            org.w3c.dom.Node n3 = nnm.item(0);
                            vps = Float.parseFloat(n3.getNodeValue());
                        }
                        n2 = n2.getNextSibling();
                    }
                }
                n = n.getNextSibling();
            }

        } catch (Exception e) {
            return null;
        }

        if (Float.isNaN(hps) 
                || Float.isNaN(vps)
                || Float.isInfinite(hps) 
                || Float.isInfinite(vps)) {
            return null;
        }
        
        return new float[]{hps, vps};
    }

    public static void printImageReadWriteCapabilites() {
        System.out.println("Read tif: " + canReadImageFile("tif"));
        System.out.println("Read jpg: " + canReadImageFile("jpg"));
        System.out.println("Read png: " + canReadImageFile("png"));
        System.out.println("Read gif: " + canReadImageFile("gif"));

        System.out.println("Write tif: " + canWriteImageFile("tif"));
        System.out.println("Write jpg: " + canWriteImageFile("jpg"));
        System.out.println("Write png: " + canWriteImageFile("png"));
        System.out.println("Write gif: " + canWriteImageFile("gif"));
    }

    public static void displayImageInWindow(BufferedImage image) {
        // Use a JLabel in a JFrame to display the image
        javax.swing.JFrame frame = new javax.swing.JFrame();
        javax.swing.JLabel label = new javax.swing.JLabel(
                new javax.swing.ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    public static BufferedImage convertToGrayscale(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage grayImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = (Graphics2D) (grayImage.getGraphics());
        g2d.drawImage(image, 0, 0, null);
        return grayImage;
    }

    public static BufferedImage createGrayscaleImage(int imageWidth,
            int imageHeight,
            byte[] data) {

        ComponentColorModel ccm = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new int[]{8},
                false, // hasAlpha
                false, // alpha premultiplied
                Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        ComponentSampleModel csm = new ComponentSampleModel(
                DataBuffer.TYPE_BYTE,
                imageWidth, imageHeight, 1, imageWidth, new int[]{0});
        DataBuffer dataBuf = new DataBufferByte(data, imageWidth);
        WritableRaster wr = Raster.createWritableRaster(csm, dataBuf, new Point(0, 0));
        return new BufferedImage(ccm, wr, true, null);
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided BufferedImage.
     *
     * From Filthy Rich Clients: http://filthyrichclients.org/
     * Modified by Bernhard Jenny.
     *
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    RenderingHints.KEY_INTERPOLATION (e.g.
     *    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
     *    RenderingHints.VALUE_INTERPOLATION_BILINEAR,
     *    RenderingHints.VALUE_INTERPOLATION_BICUBIC)
     * @param imageType The type of image to create, e.g. BufferedImage.TYPE_INT_ARGB
     * @param progressiveBilinear if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique. Only useful in down-scaling cases, where
     *    targetWidth or targetHeight is smaller than the original dimensions.
     *    progressiveBilinear is ignored when upsammpling.
     * @return a scaled version of the original BufferedImage
     */
    public static BufferedImage getFasterScaledInstance(BufferedImage img,
            int targetWidth, int targetHeight, Object hint, int imageType,
            boolean progressiveBilinear) {
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();

        // only use progressive resampling when downsampling
        progressiveBilinear &= targetWidth < prevW || targetHeight < prevH;
        boolean isTranslucent = img.getTransparency() != Transparency.OPAQUE;

        if (progressiveBilinear) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (progressiveBilinear && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (progressiveBilinear && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            if (scratchImage == null || isTranslucent) {
                // Use a single scratch buffer for all iterations
                // and then copy to the final, correctly-sized image
                // before returning
                scratchImage = new BufferedImage(w, h, imageType);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;

            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        // If we used a scratch buffer that is larger than our target size,
        // create an image of the right size and copy the results into it
        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, imageType);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return ret;
    }
}
