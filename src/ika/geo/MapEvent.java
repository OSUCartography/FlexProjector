/*
 * MapEvent.java
 *
 * Created on October 4, 2006, 5:05 PM
 *
 */

package ika.geo;

/**
 * MapEvent is sent to all registered MapEventListener when a GeoObject in the
 * tree of GeoObjects changes.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MapEvent {
    
    /**
     * True if the structure of the tree of GeoObjects changed. E.g. an object
     * was added or removed.
     */
    private boolean structureChanged;
    
    /**
     * True if the selection state of a GeoObject in the tree changed.
     */
    private boolean selectionChanged;
    
    /**
     * True if the visibility of a GeoObject in the tree changed.
     */
    private boolean visibilityChanged;
    
    /**
     * The root of the tree of GeoObjects that changed.
     */
    private GeoSet rootGeoSet;
    
    /**
     * Creates a new instance of MapEvent
     */
    public MapEvent() {
        this.structureChanged = false;
        this.selectionChanged = false;
        this.visibilityChanged = false;
        this.rootGeoSet = null;
    }
    
    /**
     * Creates a new instance of MapEvent.
     */
    public MapEvent (boolean structureChanged, 
            boolean selectionChanged,
            boolean visibilityChanged) {
        this.structureChanged = structureChanged;
        this.selectionChanged = selectionChanged;
        this.visibilityChanged = visibilityChanged;
        this.rootGeoSet = null;
    }
    
    public static MapEvent structureChange() {
        return new MapEvent (true, false, false);
    }
    
    public static MapEvent selectionChange() {
        return new MapEvent (false, true, false);
    }
    
    public static MapEvent visibilityChange() {
        return new MapEvent (false, false, true);
    }
    
    /**
     * Returns whether this event indicates a change in the structure of the
     * tree of objects.
     */
    public boolean isStructureChanged() {
        return structureChanged;
    }
    
    /**
     * Returns whether the selection state of an object changed.
     */
    public boolean isSelectionChanged() {
        return selectionChanged;
    }
    
    /**
     * Returns whether the visibility of an object changed.
     */
    public boolean isVisibilityChanged() {
        return visibilityChanged;
    }

    public GeoSet getRootGeoSet() {
        return rootGeoSet;
    }

    protected void setRootGeoSet(GeoSet rootGeoSet) {
        this.rootGeoSet = rootGeoSet;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MapEvent");
        sb.append(" root="+this.rootGeoSet);
        sb.append(" selectionChanged="+this.selectionChanged);
        sb.append(" structureChanged="+this.structureChanged);
        sb.append(" visibilityChanged="+this.visibilityChanged);
        return sb.toString();
    }
    
}
