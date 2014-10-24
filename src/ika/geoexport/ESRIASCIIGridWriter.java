/*
 * ESRIASCIIGridExporter.java
 *
 * Created on August 14, 2005, 4:17 PM
 *
 */

package ika.geoexport;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * ESRIASCIIGridWriter writes a grid of float values to a ESRI ASCII grid file.
 * It does not write a GeoGrid or another object modeling a grid, but is used 
 * in "immediate mode", i.e. the user of this class directly calls methods to 
 * write grid values. The constructor writes the header of the file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public final class ESRIASCIIGridWriter {
    
    /**
     * Counts the number of values written to the file.
     */
    private int valueCounter = 0;
    
    /**
     * The number of columns of the grid.
     */
    private int cols;
    
    /**
     * The number of rows of the grid.
     */
    private int rows;
    
    /**
     * The value that is written if the corresponding value is not valid.
     */
    private String noDataString;
    
    /**
     * Write to this PrintWriter.
     */
    private PrintWriter writer;

    private DecimalFormat formatter;

    /**
     * A system dependent separator string, typically '\n' or '\r' or a 
     * combination of the two.
     */
    private static final String lineSeparator = System.getProperty("line.separator");

    /** Creates a new instance of ESRIASCIIGridExporter and writes the header
     of the file.
     */
    public ESRIASCIIGridWriter(PrintWriter writer, 
            int cols, int rows, 
            double west, double south, 
            double cellSize, float noDataValue) {


        if (cols <= 1 || rows <= 1 || cellSize <= 0) {
            throw new IllegalArgumentException();
        }

        this.setNumberFormat("##0.#");
        this.writer = writer;
        this.cols = cols;
        this.rows = rows;
        this.noDataString = Float.toString(noDataValue) + " ";  // append a space
        
        writer.write("ncols " + cols + lineSeparator);
        writer.write("nrows " + rows + lineSeparator);
        writer.write("xllcorner " + west + lineSeparator);
        writer.write("yllcorner " + south + lineSeparator);
        writer.write("cellsize " + cellSize + lineSeparator);
        writer.write("nodata_value " + noDataValue + lineSeparator);
    }

    public void setNumberFormat(String pattern) {
        formatter = new DecimalFormat(pattern);
        DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dfs);
    }
    /**
     * Writes a value to the grid file. Throws an exception if all possible 
     * values have already been written.
     * @param v The value to write to the file. Can be NaN or infinite. Should 
     * be different from the noDataValue parameter passed in the constructor.
     */
    public void write(float v) {
        this.assertGridNotFull();
        
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            writer.write(this.noDataString);
        } else {
            writer.write(formatter.format(v));
        }
        writer.write(' ');
        ++valueCounter;
    }
    
    /**
     * Write a noDataValue to the grid file.
     */
    public void writeNoData() {
        this.assertGridNotFull();
        
        writer.write(this.noDataString);
        ++valueCounter;
    }
    
    /**
     * Writes a system-dependent new-line character to the file. This should
     * be called at the end of each row.
     */
    public void newLine() {
        writer.write (lineSeparator);
    }
    
    /**
     * Make sure not all values have already been written to the grid file.
     */
    private void assertGridNotFull() {
        if (this.valueCounter >= this.cols * this.rows) {
            throw new IllegalStateException("ASCII grid is complete.");
        }
    }
}