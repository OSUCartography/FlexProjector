/*
 * GeoTreeRoot.java
 *
 * Created on April 17, 2007, 3:44 PM
 *
 */

package ika.geo;

import ika.gui.PageFormat;
import ika.gui.PageFormatChangeListener;
import ika.utils.Serializer;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

/**
 * GeoTreeRoot has three GeoSets: background, main, and foreground. 
 * Usually the mainGeoSet is edited by the application, the foreground and the
 * background serve as container for supplemental data.
 * GeoTreeRoot also has a reference to a PageFormat that determines the size
 * and position of the map document.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoTreeRoot extends GeoSetBroadcaster
        implements PageFormatChangeListener, MapEventListener{
    
    /**
     * A GeoSet containing additional information that is drawn, but cannot
     * usually be manipulated by the user. The data contained by backgroundGeoSet
     * is not exported by GeoSetExporters. It can be used for a background
     * raster grid or other usually non-manipulatable graphics.
     */
    private GeoSet backgroundGeoSet = null;
    
    /**
     * The main GeoSet that is displayed by this MapComponent and exported by 
     * GeoExporters.
     */
    private GeoSet mainGeoSet = null;
    
    /**
     * A GeoSet containing additional GeoObjects that are displayed in front of
     * the backgroundGeoSet and the mainGeoSet, e.g. the page outline.
     * This GeoSet is not exported by GeoSetExporters.
     */
    private GeoSet foregroundGeoSet = null;
    
    /**
     * The size, position and scale of the page.
     */
    private PageFormat pageFormat = new PageFormat();
    
    /**
     * The outline of the page that will be added to the map.
     */
    transient private GeoPath pageFormatOutline = new GeoPath();
    
    /** Creates a new instance of GeoTreeRoot */
    public GeoTreeRoot() {
        this.setName("root");
        this.mainGeoSet = new GeoSet();
        this.mainGeoSet.setName("main");
        this.backgroundGeoSet = new GeoSet();
        this.backgroundGeoSet.setName("background");
        this.foregroundGeoSet = new GeoSet();
        this.foregroundGeoSet.setName("foreground");
        this.add(this.backgroundGeoSet);
        this.add(this.mainGeoSet);
        this.add(this.foregroundGeoSet);
        
        // setup the page outline
        VectorSymbol vs = new VectorSymbol(null, Color.GRAY, 0);
        vs.setScaleInvariant(true);
        this.pageFormatOutline.setVectorSymbol(vs);
        this.foregroundGeoSet.add(this.pageFormatOutline);
        this.pageFormatOutline.setName("page outline");
        this.updatePageFormatOutline();
        
        // register this as a MapEventListener that updates the page format
        // whenever the map content changes, if the page format is automatic.
        this.addMapEventListener(this);
        
        // register this as a listener for page format changes to update the
        // page outline represented in the map
        this.pageFormat.addPageFormatChangeListener(this);
    }
    
    public byte[][] serializeModel() throws java.io.IOException {
        // serialize the backgroundGeoSet, the mainGeoSet and the foregroundGeoSet.
        byte[] b = Serializer.serialize(this.backgroundGeoSet, true);
        byte[] m = Serializer.serialize(this.mainGeoSet, true);
        byte[] f = Serializer.serialize(this.foregroundGeoSet, true);
        byte[] pf = Serializer.serialize(this.pageFormat, true);
        return new byte[][] {b, m, f, pf};
    }

    public void deserializeModel(byte[][] data) 
    throws java.lang.ClassNotFoundException, java.io.IOException {

        Object b = Serializer.deserialize(data[0], true);
        Object m = Serializer.deserialize(data[1], true);
        Object f = Serializer.deserialize(data[2], true);
        Object pf = Serializer.deserialize(data[3], true);
        
        this.setBackgroundGeoSet((GeoSet)b);
        this.setMainGeoSet((GeoSet)m);
        this.setForegroundGeoSet((GeoSet)f);
        this.pageFormat = (PageFormat)pf;
        this.pageFormat.addPageFormatChangeListener(this);
        this.updatePageFormatOutline();
    }
    
    public GeoSet getMainGeoSet() {
        return this.mainGeoSet;
    }
    
    public void setMainGeoSet(GeoSet newMainGeoSet) {
        if (newMainGeoSet != null) {
            GeoSet oldMainGeoSet = this.mainGeoSet;
            // replaceGeoObject() will trigger a MapEvent. An event listener may
            // access this.mainGeoSet. Therefore replace this.mainGeoSet before
            // calling replaceGeoObject().
            this.mainGeoSet = newMainGeoSet;
            this.replaceGeoObject(newMainGeoSet, oldMainGeoSet);
        }
    }
    
    public GeoSet getBackgroundGeoSet() {
        return backgroundGeoSet;
    }
    
    public void setBackgroundGeoSet(GeoSet geoSet) {
        if (geoSet != null)
            this.backgroundGeoSet = geoSet;
    }
    
    public GeoSet getForegroundGeoSet() {
        return foregroundGeoSet;
    }
    
    public void setForegroundGeoSet(GeoSet geoSet) {
        if (geoSet != null) {
            this.foregroundGeoSet = geoSet;
        }
    }
    
    /**
     * Returns this GeoTreeRoot. Overwrites getRoot() of GeoObject which
     * traverses the tree of GeoObject in upwards direction. This call is the
     * last one in the chain.
     *
     * @return The GeoTreeRoot of the tree. This is the topmost Geoset.
     */
    @Override
    public GeoTreeRoot getRoot() {
        return this;
    }
    
    public PageFormat getPageFormat() {
        return pageFormat;
    }
    
    private void updatePageFormatOutline() {
        if (this.pageFormat == null) {
            this.pageFormatOutline.setVisible(false);
            return;
        }
        
        final boolean oldVisibility = this.pageFormatOutline.isVisible();
        final boolean newVisibility = this.pageFormat.isVisible();
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            Rectangle2D outline = pageFormat.getPageSizeWorldCoordinates();
            pageFormatOutline.rectangle(outline);
            pageFormatOutline.setVisible(newVisibility);
        } finally {
            if (oldVisibility != newVisibility) {
                trigger.inform();
            } else {
                trigger.abort();
            }
        }
    }
    
    /**
     * Implement the PageFormatChangeListener interface. Update the outline of
     * the page format when the page format changes.
     */
    public void pageFormatChanged(PageFormat pageFormat) {
        updatePageFormatOutline();
    }
    
    /**
     * Implement the MapEventListener interface.
     * Update the page format when the content of the map changes and if the 
     * page format is set to automatic updating.
     */
    public void mapEvent(MapEvent evt) {
        if (pageFormat == null || !pageFormat.isAutomatic()) {
            return;
        }
        Rectangle2D bounds = mainGeoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
        pageFormat.setPageWorldCoordinates(bounds);
    }
    
}
