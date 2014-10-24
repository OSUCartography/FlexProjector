/*
 * GeoSetBroadcaster.java
 *
 * Created on January 15, 2007, 6:08 PM
 *
 */
package ika.geo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

/**
 * Extends the GeoSet class with event handling. A MapEventListener can be 
 * registered for change events in the tree.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoSetBroadcaster extends GeoSet {

    private static final long serialVersionUID = -632850350013391825L;
    /**
     * A set of registered MapEventListeners.
     */
    transient private Vector<MapEventListener> mapEventListeners;
    /**
     * Remember whether sending events is temporarily suspended.
     */
    transient private boolean eventsSuspended;
    /**
     * Remember whether informMapEventListeners() is currently informing all
     * listeners. This makes sure that an event handler is not called again if 
     * it changes any GeoObject in the tree.
     */
    transient private boolean informingListeners;

    /**
     * Creates a new instance of GeoSetBroadcaster
     */
    public GeoSetBroadcaster() {
        this.init();
    }

    /**
     * Transient attributes need customized deserialization.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        // read this object
        stream.defaultReadObject();

        this.init();
    }

    /**
     * Initialize the transient attributes.
     */
    private void init() {
        this.mapEventListeners = new Vector<MapEventListener>();
        this.eventsSuspended = false;
        this.informingListeners = false;
    }

    /**
     * Register a MapEventListener.
     * 
     * @param listener The MapEventListener to register.
     */
    public final synchronized void addMapEventListener(MapEventListener listener) {
        if (listener != null) {
            this.mapEventListeners.add(listener);
        }
    }

    /**
     * Static method to register a MapEventListener.
     * 
     * @param listener The MapEventListener to register.
     * @param geoObject Any GeoObject in the tree. The toplevel parent must be 
     * a GeoSetBroadcaster.
     */
    static public void addMapEventListener(MapEventListener listener,
            GeoObject geoObject) {
        GeoSetBroadcaster root = geoObject.getRoot();
        if (root == null) {
            throw new IllegalArgumentException("GeoObject is not in tree with GeoTreeRoot root.");
        }
        root.addMapEventListener(listener);
    }

    /**
     * Unregister a MapEventListener.
     */
    public final synchronized void removeMapEventListener(MapEventListener listener) {
        this.mapEventListeners.remove(listener);
    }

    /**
     * Remove every registered MapEventListener.
     */
    public final synchronized void removeAllMapEventListeners() {
        this.mapEventListeners.clear();
    }

    /**
     * Temporarily suspend event distribution to MapEventListener.
     */
    public final synchronized void suspendMapEventListeners() {
        this.eventsSuspended = true;
    }

    /**
     * Activate distribution of events again.
     */
    public final synchronized void activateMapEventListeners() {
        this.eventsSuspended = false;
    }

    /**
     * Returns whether distribution of events is currently suspended.
     */
    public final synchronized boolean mapEventListenersSuspended() {
        return this.eventsSuspended;
    }

    /**
     * Inform each registered MapEventListener about a change.
     */
    public final void informMapEventListeners(final MapEvent evt) {
        synchronized (this) {
            if (this.eventsSuspended || this.informingListeners) {
                return;
            }
            evt.setRootGeoSet(this);
        }
/*
        ika.utils.SwingThreadUtils.invokeAndWait(new Runnable() {

            public void run() {
  */              try {
                    informingListeners = true;

                    for (int i = mapEventListeners.size() - 1; i >= 0; i--) {
                        mapEventListeners.get(i).mapEvent(evt);
                    }

                } finally {
                    informingListeners = false;
                }
            }
 //       });
  //  }
}
