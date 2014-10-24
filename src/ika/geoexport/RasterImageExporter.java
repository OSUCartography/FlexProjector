/*
 * RasterImageExporter.java
 *
 * Created on June 29, 2005, 12:01 PM
 */

package ika.geoexport;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import ika.geo.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class RasterImageExporter extends GeoSetExporter {

    private static final int MAX_IMAGE_DIM = 32000;
    
    private int imageWidth;
    private String format;
    
    /** Creates a new instance of RasterImageExporter */
    public RasterImageExporter(){
        imageWidth = 1000;
        format = "jpg";
    }
    
    public String getFileFormatName(){
        return format.toUpperCase();
    }
    
    public String getFileExtension() {
        return format;
    }
    
    protected void write(GeoSet geoSet, OutputStream outputStream) throws IOException {
        
        // Create an image to save
        Rectangle2D bounds = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
        final int imageHeight = 
                (int)Math.ceil(bounds.getHeight() / bounds.getWidth() * imageWidth);

        if (imageWidth > MAX_IMAGE_DIM || imageHeight > MAX_IMAGE_DIM) {
            throw new IOException("The raster image is too large to export.");
        }

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_RGB);
        
        // Create a graphics context on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // set white background
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        
        // enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // enable high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        // enable bicubic interpolation of images
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        final double scale = imageWidth / bounds.getWidth();

        // set default appearance of vector elements
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.black);
        
        // draw the map tree
        RenderParams rp = new RenderParams (g2d, scale, 
                bounds.getMinX(), bounds.getMinY(), 
                bounds.getWidth(), bounds.getHeight(),
                false, null);
        geoSet.drawNormalState(rp);
        
        // Graphics context no longer needed so dispose it
        g2d.dispose();
        
        // Write generated image to a raster image stream
        ImageIO.write(bufferedImage, format, outputStream);
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
