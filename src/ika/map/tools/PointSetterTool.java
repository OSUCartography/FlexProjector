/*
 * PointTool.java
 *
 * Created on April 8, 2005, 7:59 AM
 */

package ika.map.tools;

import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import ika.geo.*;
import ika.gui.MapComponent;

/**
 * PointSetterTool - a tool that adds GeoPoints to a map by simple clicks.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class PointSetterTool extends MapTool {
    
    private PointSymbol pointSymbol;
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public PointSetterTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     * @param pointSymbol The PointSymbol used to draw newly created points.
     */
    public PointSetterTool(MapComponent mapComponent,
            PointSymbol pointSymbol) {
        super(mapComponent);
        this.pointSymbol = pointSymbol;
    }
    
    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {                       
        // add a new point
        GeoPoint geoPoint = new GeoControlPoint(point);
        if (this.pointSymbol != null)
            geoPoint.setPointSymbol(this.pointSymbol);
        geoPoint.setSelected(true);
        
        // deselect all current GeoObjects in destination GeoSet
        this.destinationGeoSet.setSelected(false);
        
        // add point
        this.destinationGeoSet.add(geoPoint);
    }
    
    public PointSymbol getPointSymbol() {
        return pointSymbol;
    }

    public void setPointSymbol(PointSymbol pointSymbol) {
        this.pointSymbol = pointSymbol;
    }
    
    @Override
    protected String getCursorName() {
        return "setpointarrow";
    }
}
