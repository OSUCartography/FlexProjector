/*
 * MapTool.java
 *
 * Created on April 7, 2005, 7:17 PM
 */

package ika.map.tools;

import java.awt.geom.*;
import java.awt.event.*;
import java.awt.*;
import ika.utils.*;
import ika.gui.MapComponent;
import ika.geo.*;

/**
 * MapTool - an abstract base class for map tools. A MapTool offers some kind of
 * interactivity based on mouse events.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class MapTool {
    
    /**
     * The MapComponent for which this MapTool provides its services.
     */
    protected MapComponent mapComponent;
    
    protected GeoSet destinationGeoSet;
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public MapTool(MapComponent mapComponent) {
        this.mapComponent = mapComponent;
    }
    
    /**
     * This method is called when the MapTool is activated, i.e. made the current tool.
     */
    public void activate() {
    }
    
    /**
     * This method is called when the MapTool is deactivated, i.e. it is no longer the
     * current tool.
     */
    public void deactivate() {
        mapComponent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * pause is called when the MapTool is temporarily suspended, i.e. another
     * MapTool is activated for a certain time. pause will be balanced by a call
     * to resume.
     */
    public void pause(){
    }
    
    /**
     * resume is called when the MapTool was previously temporarily suspended,
     * and can resume again.
     */
    public void resume(){
    }
    
    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse was pressed down, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseDown  (Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse moved, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseMoved(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse entered the map, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseEntered(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse exited the map, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void mouseExited(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse starts a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void startDrag(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * The mouse location changed during a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void updateDrag(Point2D.Double point, MouseEvent evt) {
    }
    
    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        /** call mouseClicked
         * This makes sure, a derived tool receives a mouse-event if the mouse is
         * clicked, dragged to another position and released there. E.g. the
         * ZoomOutTool does not overwrite startDrag, updateDrag and endDrag and
         * would therefore not receive an event if the mouse is clicked, dragged
         * and released.
         */
        this.mouseClicked(point, evt);
    }
    
    /**
     * Returns whether the tool is currently dragging.
     */
    public boolean isDragging() {
        return false;
    }
    
    /**
     * Treat key events.
     * The event can be consumed (return true) or be delegated to other
     * listeners (return false).
     * @param keyEvent The new key event.
     * @return True if the key event has been consumed, false otherwise.
     */
    public boolean keyEvent(KeyEvent keyEvent) {
        // default: delegate key event to other components
        return false;
    }
    
    /**
     * Draw some background information.
     * @return True if the map has been completely drawn by this tool, false otherwise.
     */
    public boolean drawBackground(Graphics2D g2d) {
        // default: don't do anything
        return false;
    }
    
    /**
     * Draw the interface elements of this MapTool.
     * @param g2d The destination to draw to.
     */
    public void draw(RenderParams rp) {
    }
    
    /**
     * Returns the default cursor that is used while this MapTool is active.
     * @return The name of the cursor. See CursorUtils for possible names.
     */
    protected String getCursorName() {
        return "arrow";
    }
    
    /**
     * Sets the cursor icon to the default cursor specified by getCursorName.
     */
    public void setDefaultCursor() {
        String cursorName = getCursorName();
        CursorUtils.setCursor(cursorName, mapComponent);
    }

    public GeoSet getDestinationGeoSet() {
        return destinationGeoSet;
    }

    public void setDestinationGeoSet(GeoSet destinationGeoSet) {
        this.destinationGeoSet = destinationGeoSet;
    }
}
