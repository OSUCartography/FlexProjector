/*
 * SingleSelectionPointSetterTool.java
 *
 * Created on June 30, 2005, 9:48 AM
 *
 */

package ika.map.tools;

import ika.gui.MapComponent;
import ika.geo.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * SingleSelectionPointSetterTool extends PointSetterTool and allows for a new
 * point to be set only when the currently selected point can first be deselected.
 * @author jenny
 */
public class SingleSelectionPointSetterTool extends PointSetterTool {
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     * @param destinationGeoSet The GeoSet that will receive the newly created points.
     */
    public SingleSelectionPointSetterTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     * @param destinationGeoSet The GeoSet that will receive the newly created points.
     * @param pointSymbol The PointSymbol used to draw newly created points.
     */
    public SingleSelectionPointSetterTool(MapComponent mapComponent,
            PointSymbol pointSymbol) {
        super(mapComponent, pointSymbol);
    }
    
    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        if (this.getDestinationGeoSet() == null)
            return;
        
        // test if the currently selected point can be deselected.
        //if (this.getDestinationGeoSet().canChangeSelectionOfChildren())
            super.mouseClicked(point, evt);
    }
}
