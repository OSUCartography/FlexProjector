/*
 * PolygonTool.java
 *
 * Created on October 27, 2005, 12:02 PM
 *
 */

package ika.map.tools;

import ika.gui.MapComponent;

/**
 * Tool to draw GeoPaths
 * @author jenny
 */
public class PolygonTool extends PolygonToolBase {
    
    /** Creates a new instance of PolygonTool */
    public PolygonTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * Finish drawing. Add the GeoPath to the map.
     */
    protected void finishPath() {
        
        // add the GeoPath to the map.
        if (geoPath != null && geoPath.hasOneOrMorePoints()) {
            //geoPath.setSelected(false);
            if (this.destinationGeoSet != null)
                this.destinationGeoSet.add(geoPath);
            else
                this.mapComponent.addGeoObject(geoPath, true);
        }
        
        // this will reset the GeoPath. So call this at the end of this method.
        super.finishPath();
    }
    
    protected String getCursorName() {
        return "pen";
    }
}
