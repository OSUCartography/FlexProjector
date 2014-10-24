/*
 * PPMImageWriter.java
 *
 * Created on June 5, 2007, 10:16 AM
 *
 */
package ika.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes PPM rgb images. Can handle large images that do not fit into the
 * availble memory. PPM is an simple file format that can be opened
 * by Photoshop and other applications.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class PPMImageWriter extends ImageWriter {

    /** 
     * Creates a new instance of PBMImageWriter and writes the header
    of the file.
     */
    public PPMImageWriter(OutputStream out,
            int cols, int rows) throws java.io.IOException {
        super(out, cols, rows);
    }

    protected void writeHeader() throws java.io.IOException {
        String newline = System.getProperty("line.separator");
        this.out.write(("P6" + newline).getBytes("ASCII"));
        this.out.write(Integer.toString(cols).getBytes("ASCII"));
        this.out.write(newline.getBytes());
        this.out.write(Integer.toString(rows).getBytes("ASCII"));
        this.out.write(newline.getBytes());
        this.out.write(("255" + newline).getBytes("ASCII"));
    }

    /**
     * Writes a color value to the image file.
     * @param r Red in the range [0..255]
     * @param g Green  in the range [0..255]
     * @param b Blue  in the range [0..255]
     */
    public void writeRGB(int r, int g, int b) throws IOException {
        this.out.write((byte) r);
        this.out.write((byte) g);
        this.out.write((byte) b);
    }
}
