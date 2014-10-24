/*
 * MapChangeListener.java
 *
 * Created on March 5, 2005, 3:28 PM
 */

package ika.geo;

/**
 * MapEventListener - a listener for change events.
 * Whenever a GeoObject changes mapEvent is informed.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface MapEventListener {
    
    /**
     * mapEvent() is called whenever a GeoObject changes.
     * @param evt Information about the type of change.
     */
    public void mapEvent (MapEvent evt);
}
