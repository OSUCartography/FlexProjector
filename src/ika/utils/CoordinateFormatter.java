/*
 * CoordinateFormatter.java
 *
 * Created on August 12, 2005, 5:54 AM
 *
 */

package ika.utils;

import java.text.DecimalFormat;

/**
 *
 * @author jenny
 */
public class CoordinateFormatter {
    
    /**
     * Coordinates are multiplied by scaleFactor before
     * being converted to a string. This scale factor can be used to transform
     * between the global mapping unit of the GeoObjects and the 
     * unit for displaying coorinates.
     * e.g. meter - centimeter -> scaleFactor = 100
     */
    private double scaleFactor = 1.d;
    
    private java.text.DecimalFormat decimalFormat;
    private java.text.DecimalFormat shortDecimalFormat;
    
    /** Creates a new instance of CoordinateFormatter */
    public CoordinateFormatter(String format, String shortFormat, double scaleFactor) {
        decimalFormat = new java.text.DecimalFormat (format);
        shortDecimalFormat = new java.text.DecimalFormat (shortFormat);
        this.scaleFactor = scaleFactor;
    }
    
    public String format (double number) {
        return decimalFormat.format(number * scaleFactor);
    }
    
    public String formatShort (double number) {
        return shortDecimalFormat.format(number * scaleFactor);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        if (scaleFactor == 0.)
            throw new IllegalArgumentException();
        this.scaleFactor = scaleFactor;
    }
}
