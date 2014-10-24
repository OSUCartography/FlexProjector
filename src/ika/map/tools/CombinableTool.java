/*
 * CombinableTool.java
 *
 * Created on March 29, 2007, 11:24 AM
 *
 */

package ika.map.tools;

import java.awt.geom.Point2D;

/**
 * A CombinableTool can be part of a CombinedTool.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface CombinableTool {
    /**
     * Adjust the cursor if necessary. Don't set the cursor to the default cursor.
     * @return True if the cursor has been changed.
     */
    boolean adjustCursor(Point2D.Double point);
}
