/*
 * GeoImagePyramid.java
 *
 * Created on December 4, 2006, 12:33 AM
 *
 */

package ika.geo;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Vector;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GeoImagePyramid extends GeoImage {
    
    /**
     * A vector holding the images of the pyramid. The first image at position 0
     * is the original unscaled image, images with decreasing size follow.
     */
    transient protected Vector images = new Vector(4); // FIXME
    
    /**
     * The smallest image will be a little smaller than 
     * MIN_IMAGE_SIZE x MIN_IMAGE_SIZE pixels.
     */
    private static final int MIN_IMAGE_SIZE = 256;
    
    /**
     * The next smaller image in the pyramid measures origSize x IMAGE_SCALE.
     */
    private static final double IMAGE_SCALE = 0.5;
    
    /**
     * Create a new instance of GeoImagePyramid.
     * @param image A reference to an image to display. The image is not copied,
     * instead a reference is retained.
     * @param x Top left corner of this image.
     * @param y Top left corner of this image.
     * @param pixelSize Size of a pixel.
     */
    public GeoImagePyramid(BufferedImage image, double x, double y, double pixelSize) {
        super(image, x, y, pixelSize);
        this.createPyramid();
    }
    
    /**
     * Create a new instance of GeoImagePyramid. The lower left corner of the image is
     * placed at 0/0, the size of a pixel in world coordinates equals 1.
     * @param image A reference to an image to display. The image is not copied,
     * instead a reference is retained.
     */
    public GeoImagePyramid(BufferedImage image, URL url) {
        super(image, url);
        this.createPyramid();
    }
    
    /**
     * Creates the image pyramid. Stores all images in this.images, including
     * the original unscaled image already stored in super.image.
     */
    private void createPyramid() {
        
        // clear any previous image pyramid
        this.images.clear();
        
        // add the unscaled image to the pyramid vector.
        this.images.add(this.image);
        
        // setup an affine transformation for downscaling
        AffineTransform tx = new AffineTransform();
        tx.scale(IMAGE_SCALE, IMAGE_SCALE);
        AffineTransformOp op = new AffineTransformOp(tx,
                AffineTransformOp.TYPE_BICUBIC);
        
        // repeatedly downscale the image and store the images in the pyramid.
        BufferedImage lastImage = this.image;
        while (lastImage.getHeight() > MIN_IMAGE_SIZE
                || lastImage.getWidth() > MIN_IMAGE_SIZE) {
            lastImage = op.filter(lastImage, null);
            this.images.add(lastImage);
        }
    }
    
    /**
     * Draw the image in a map.
     */
    @Override
    public void drawNormalState(RenderParams rp) {
        if (this.image == null) {
            return;
        }
        
        // find the image to display
        final int imageID = this.findImageToDraw(rp.scale);
        BufferedImage img = (BufferedImage)this.images.get(imageID);
        
        // compute the scale for the image.
        // This should be done in a better way, by deriving the scale from the bounding box. FIXME
        double imageScale = Math.pow(1/IMAGE_SCALE, imageID);
        
        this.drawImage(img, rp, imageScale);
    }
    
    @Override
    public void drawSelectedState(RenderParams rp) {
        if (!this.isSelected()) {
            return;
        }
         
        Rectangle2D.Double bounds = (Rectangle2D.Double)this.getBounds2D(rp.scale);
        if (bounds != null) {
            rp.g2d.draw(bounds);
        }
    }
    
    @Override
    public void transform(AffineTransform affineTransform) {
        super.transform(affineTransform);
        this.createPyramid();
    }
    /**
     * Returns the index of an image to draw for a certain map scale. The index 
     * points into this.images.
     * @param mapScale The current scale of the map.
     */
    private int findImageToDraw(double mapScale) {
        
        // compute the size of one image pixel in screen coordinates.
        double pixelSize = Math.min(this.getCellSize(), this.getCellSize()); // FIXME
        double pixelSizeOnScreen = pixelSize * mapScale;
        
        // if the original image has to be enlarged for display, draw the original.
        if (pixelSizeOnScreen >= 1) {
            return 0;
        }
        
        // search through the pyramid for the appropriate image.
        // not elegant, but it works.
        int imageID = -1;
        while (imageID < images.size()
        && pixelSizeOnScreen < 1) {
            pixelSizeOnScreen /= IMAGE_SCALE;
            imageID++;
        }
        return Math.min(imageID, images.size()-1);
    }
}
