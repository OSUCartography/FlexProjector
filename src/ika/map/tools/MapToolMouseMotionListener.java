/*
 * MapToolMouseMotionListener.java
 *
 * Created on May 15, 2005, 6:47 PM
 */

package ika.map.tools;

import java.awt.geom.*;
import ika.gui.MapComponent;

/**
 *
 * @author jenny
 */
public interface MapToolMouseMotionListener {
    public void mouseMoved(Point2D.Double point, MapComponent mapComponent);    
}
