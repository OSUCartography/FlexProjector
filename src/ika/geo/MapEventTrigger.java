/*
 * MapEventTrigger.java
 *
 * Created on January 16, 2007, 5:44 PM
 *
 */

package ika.geo;

/**
 * Sends MapEvent to registered MapEventListener.
 * Remembers whether delivery of events to listeners is suspended.
 * Here is how to use it:
 * <CODE>
 *    // The constructor of MapEventTrigger remembers whether events are 
 *    // initially suspended, and suspends sending of events.
 *    MapEventTrigger trigger = new MapEventTrigger(this);
 *    final boolean informListeners;
 *    try {
 *        informListeners = doSomethingThatTriggersMultipleMapEvents();
 *    } finally {
 *        // activate sending of events again if it has not been suspended
 *        // initially and send an event by calling inform(), or call abort()
 *        // when no event should be generated.
 *        if (informListeners)
 *          trigger.inform();
 *        else
 *          trigger.abort();
 *    }
 * </CODE>
 * <B>It is important to make sure inform() or abort() is always called after the constructor
 * of MapEventTrigger has been called. Use the finally block for this.</B>
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MapEventTrigger {
    
    /**
     * Remembers whether sending events was suspended when the constructor 
     * was called.
     */
    private transient boolean eventsSuspendedInitially = false;
    
    /**
     * Remembers the root of the tree of GeoObjects. This is needed when
     * triggering an event.
     */
    private transient GeoSetBroadcaster root;
    
    /**
     * Creates a new instance of MapEventTrigger
     * @param geoObject Any object in the tree of geo objects.
     */
    public MapEventTrigger(GeoObject geoObject) {
        if (geoObject == null)
            throw new IllegalArgumentException();
        this.root = geoObject.getRoot();
        if (this.root != null) {
            this.eventsSuspendedInitially = this.root.mapEventListenersSuspended();
            this.root.suspendMapEventListeners();
        }
    }
    
    /**
     * abort() must be called when a MapEventTrigger has been constructed, but
     * inform() should not be called. If abort() is not called, further sending
     * of MapEvents is broken.
     */
    public void abort() {
        if (this.root != null && this.eventsSuspendedInitially == false) {
            this.root.activateMapEventListeners();
        }
    }
    
    /**
     * Activate sending of events if this was not suspended when the constructor
     * was called, and send an event.
     * A standard event is created and sent.
     */
    public void inform() {
        this.inform(new MapEvent());
    }
    
    /**
     * Activate sending of events if this was not suspended when the constructor
     * was called, and send the passed event.
     * @param evt The MapEvent that is sent to all registered listeners.
     */
    public void inform(MapEvent evt) {
        if (this.root != null && this.eventsSuspendedInitially == false) {
            this.root.activateMapEventListeners();
            this.root.informMapEventListeners(evt);
        }
    }
    
    /**
     * Helper method to inform registered listeners of a change.
     * @param geoObject Any GeoObject in the tree that changed. It can be 
     * the GeoObject that changed, but this is not required.
     */
    static public void inform(GeoObject geoObject) {
        MapEventTrigger.inform(new MapEvent(), geoObject);
    }
    
    /**
     * Helper method to inform registered listeners of a change.
     * @param evt The MapEvent discribing the type of change that occured.
     * @param geoObject Any GeoObject in the tree that changed. It can be 
     * the GeoObject that changed, but this is not required.
     */
    static public void inform(MapEvent evt, GeoObject geoObject) {
        final GeoSetBroadcaster root = geoObject.getRoot();
        if (root != null)
            root.informMapEventListeners(evt);
    }
}
