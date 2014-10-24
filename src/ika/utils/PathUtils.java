/*
 * PathUtils.java
 *
 * Created on February 17, 2006, 4:41 PM
 *
 */

package ika.utils;

import java.awt.geom.*;

/**
 *
 * @author jenny
 */
public class PathUtils {
    
    /** Returns true if this GeneralPath consists of more than one line or
     *  polygon, i.e. it has more moveto commands than the initial one.
     */
    static public boolean isCompound(GeneralPath generalPath) {
        PathIterator pi = generalPath.getPathIterator(null);
        return ika.utils.PathUtils.isCompound(pi);
    }
    
    /** Returns true if this PathIterator consists of more than one line or
     *  polygon, i.e. it has more moveto commands than the initial one.
     */
    static public boolean isCompound(PathIterator pi) {
        if (pi.isDone())
            return false;
        
        // overread initial moveto
        pi.next();
        
        // search for moveto
        double [] coords = new double [6];
        while (pi.isDone() == false) {
            // moveto starts a new line that is not connected to the previous one.
            if (pi.currentSegment(coords) == PathIterator.SEG_MOVETO) {
                return true;
            }
            pi.next();
        }
        return false;
    }
}
