/*
 * ZoomOutTool.java
 *
 * Created on April 8, 2005, 12:20 PM
 */
package ika.map.tools;

import java.awt.geom.*;
import java.awt.event.*;
import ika.gui.MapComponent;

/**
 * ZoomOutTool - a MapTool to zoom out in a map.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ZoomOutTool extends MapTool {

    /**
     * Creates a new instance of ZoomOutTool
     * @param mapComponent The MapComponent for which to zoom out.
     */
    public ZoomOutTool(MapComponent mapComponent) {
        super(mapComponent);
    }

    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        mapComponent.zoomOut(point);
    }

    @Override
    protected String getCursorName() {
        return "zoomout";
    }
}
