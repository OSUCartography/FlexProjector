/*
 * ScaleChangeHandler.java
 *
 * Created on February 26, 2007, 8:32 PM
 *
 */

package ika.gui;

/**
 * An event handler that is called whenever the scale of MapComponent changes.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public interface ScaleChangeHandler {
    
    /**
     * This method is called whenever the scale of the map changes.
     * @param mapComponent The MapComponent that just changed its scale.
     * @param currentMapScaleFactor The scale factor to transform from screen 
     * pixels to coordinates as currently displayed.
     * @param currentMapScaleNumber The scale to transform from ground coordinates
     * to coordinates as currently displayed.
     */
    public void scaleChanged (MapComponent mapComponent, 
            double currentMapScaleFactor,
            double currentMapScaleNumber);
}
