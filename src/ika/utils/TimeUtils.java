/*
 * TimeUtils.java
 *
 * Created on January 9, 2006, 10:20 AM
 *
 */

package ika.utils;

/**
 * Also see NanoTimer class for measuring time with nanosecond resolution.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class TimeUtils {
    
    /**
     * Returns the current time and date in a formatted string.
     * @return The string containing time and date.
     */
    public static final String getCurrentTimeAndDate() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
    
}
