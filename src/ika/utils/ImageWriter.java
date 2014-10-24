package ika.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract base class for image writers for various file formats. Derived
 * image writers must be able to handle very large images that don't fit into
 * the available memory, i.e. passed pixel values must immediately be written
 * to a stream.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class ImageWriter {

    /**
     * The stream to write to.
     */
    protected OutputStream out;
    /**
     * The number of columns in the image.
     */
    protected int cols;
    /**
     * The number of rows in the image.
     */
    protected int rows;
    /**
     * Counts the number of pixels written.
     */
    private int pixelCounter = 0;

    /** 
     * Creates a new instance of ImageWriter and writes the header.
     * 
     */
    public ImageWriter(OutputStream out,
            int cols, int rows) throws java.io.IOException {
        this.out = out;
        this.cols = cols;
        this.rows = rows;

        this.writeHeader();
    }

    /**
     * Writes a color value to the image file. Does nothing if all possible
     * values have already been written. Automatically writes the "end of file
     * information" after the last pixel is written.
     * @param r Red in the range [0..255]
     * @param g Green  in the range [0..255]
     * @param b Blue  in the range [0..255]
     * @throws java.io.IOException
     */
    final public void write(int r, int g, int b) throws IOException {

        final int maxPixels = this.cols * this.rows;

        if (this.pixelCounter < maxPixels)
            this.writeRGB(r, g, b);

        if (++this.pixelCounter == maxPixels)
            this.writeFooter();

    }
    
    /**
     * Writes a color value to the image file. The r, g, and b values must be
     * premultiplied by the a value. Does nothing if all possible
     * values have already been written. Automatically writes the "end of file
     * information" after the last pixel is written.
     * @param r Red in the range [0..255]
     * @param g Green  in the range [0..255]
     * @param b Blue  in the range [0..255]
     * @param a Alpha  in the range [0..255]
     * @throws java.io.IOException
     */
    final public void write(int r, int g, int b, int a) throws IOException {

        final int maxPixels = this.cols * this.rows;

        if (this.pixelCounter < maxPixels)
            this.writeRGB(r, g, b, a);

        if (++this.pixelCounter == maxPixels)
            this.writeFooter();

    }

    /**
     * Write an argb value to the file. This default implementation unpacks the
     * channels and calls write(r, g, b, a). The r, g, and b values must be
     * premultiplied by the a value.
     * @param color An rgba value packed in an integer
     * @throws java.io.IOException
     */
    public void write(int argb) throws java.io.IOException {
        
        final int a = (argb >> 24) & 0xff;
        final int r = (argb >> 16) & 0xff;
        final int g = (argb >> 8) & 0xff;
        final int b = 0xff & argb;
        this.write(r, g, b, a);
        
    }
    
    /**
     * Writes the file header. This is called by the constructor.
     * @throws java.io.IOException
     */
    protected abstract void writeHeader() throws java.io.IOException;

    /**
     * Writes the file footer at the end of the file. The standard implementation
     * does not write anything.
     * @throws java.io.IOException
     */
    protected void writeFooter() throws java.io.IOException {
    }

    /**
     * Write an rgb value to the file.
     * @param r Red in the range [0..255]
     * @param g Green in the range [0..255]
     * @param b Blue in the range [0..255]
     * @throws java.io.IOException
     */
    protected abstract void writeRGB(int r, int g, int b) throws java.io.IOException;
    
    /**
     * Write an rgba value to the file. The r, g, and b values must be
     * premultiplied by the a value. This default implementation ignores
     * the alpha channel. 
     * @param r Red in the range [0..255]
     * @param g Green in the range [0..255]
     * @param b Blue in the range [0..255]
     * @param a Alpha in the range [0..255]
     * @throws java.io.IOException
     */
    protected void writeRGB(int r, int g, int b, int a) throws java.io.IOException {
        this.write (r, g, b);
    }
    
}
