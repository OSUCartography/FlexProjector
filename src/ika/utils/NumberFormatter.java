/*
 * NumberFormatter.java
 *
 * Created on March 24, 2005, 3:35 PM
 */

package ika.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting double values.
 * @author Bernhard Jenny
 * Institute of Cartography
 * ETH Zuerich
 */
public class NumberFormatter {
    
    private static DecimalFormat formatter;
    static { // static initialization block for formatter
        formatter = new DecimalFormat();
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        formatter.setMinimumIntegerDigits(1);
        formatter.setGroupingUsed(false);
    }
    
    /**
     * Convert a double value to a string with a specified width, number of digits after
     * the decimal point, and preceeding white spaces.
     * @param value The value to convert.
     * @param width The total width of the string (= the number of characters).
     * @param decimals The number of digits after the decimal point.
     * @return The value converted to a string.
     */
    public static String format(double value, int width, int decimals) {
        return NumberFormatter.format(value, width, decimals, 0);
    }
    
    /**
     * Convert a double value to a string with a specified width, number of digits after
     * the decimal point, and preceeding white spaces.
     * @param value The value to convert.
     * @param width The total width of the string (= the number of characters).
     * @param decimals The number of digits after the decimal point.
     * @param leadingSpaces The number of leading spaces. Usually 0.
     * @return The value converted to a string.
     */
    public static String format(double value, int width, int decimals, int leadingSpaces) {
        StringBuffer str = new StringBuffer(width);
        width+= 1+leadingSpaces;
        NumberFormatter.formatter.setMaximumFractionDigits(decimals);
        NumberFormatter.formatter.setMinimumFractionDigits(decimals);
        String s = NumberFormatter.formatter.format(value); // format the number
        int padding = Math.max(leadingSpaces,width-s.length()); // insert leadingSpaces
        for (int k = 0; k < padding; k++)
            str.append(' ');
        str.append(s);
        return str.toString();
    }
    
    public static String formatScale(String prefix, double scale) {
        return NumberFormatter.formatScale(prefix, scale, false);
    }
    
    public static String formatScale(String prefix, double scale, boolean precise) {
        if (scale < 0)
            throw new IllegalArgumentException("negative scale");
        
        if (precise) {
            if (scale > 10000)
                scale = Math.round(scale / 100) * 100;
            else if (scale > 1000)
                scale = Math.round(scale / 10) * 10;
            else if (scale > 100)
                scale = Math.round(scale);
        } else {
            if (scale > 10000)
                scale = Math.round(scale / 1000) * 1000;
            else if (scale > 1000)
                scale = Math.round(scale / 100) * 100;
            else if (scale > 100)
                scale = Math.round(scale / 10) * 10;
        }
        
        java.text.DecimalFormat scaleFormatter;
        if (scale > 10)
            scaleFormatter = new java.text.DecimalFormat("###,###");
        else if (scale < 1)
            scaleFormatter = new java.text.DecimalFormat("###,###.###");
        else
            scaleFormatter = new java.text.DecimalFormat("###,###.#");
        StringBuffer str = new StringBuffer();
        if (prefix != null) {
            str.append(prefix);
            str.append(":\t");
        }
        str.append("1:");
        str.append(scaleFormatter.format(scale));
        
        return str.toString();
    }
}
