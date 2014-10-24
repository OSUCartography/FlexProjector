/*
 * SelectionTool.java
 *
 * Created on April 7, 2005, 7:21 PM
 */

package ika.map.tools;

import java.awt.geom.*;
import java.awt.event.*;
import ika.utils.*;
import ika.gui.MapComponent;

/**
 * PanTool - a tool to pan a map with the mouse.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class PanTool extends MapTool{
    
    /**
     * Store the start position of the panning.
     */
    private Point2D.Double dragStartPos;
    
    /**
     * Store the area the was visible before dragging.
     */
    private Rectangle2D initiallyVisibleRect;
    
    /** Creates a new instance of SelectionTool
      * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public PanTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * Utility method to set the cursor icon to a closed hand.
     */
    private void setClosedHandCursor() {
        final String iconName = "panclicked";
        CursorUtils.setCursor(iconName, mapComponent);
    }
    
    /**
     * The mouse was pressed down, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseDown(Point2D.Double point, MouseEvent evt) {
        // change cursor to closed hand
        setClosedHandCursor();
    }
    
    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        setDefaultCursor();
    }
    
    /**
     * The mouse starts a drag, while this MapTool is the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        // store the start location of the drag.
        dragStartPos = (Point2D.Double)point.clone();
        initiallyVisibleRect = mapComponent.getVisibleArea();
    }
    
    /**
     * The mouse location changed during a drag, while this MapTool was the 
     * active tool.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void updateDrag(Point2D.Double point, MouseEvent evt) {
        // just in case we didn't get a mousePressed-Event
        if (dragStartPos == null) {
            dragStartPos = (Point2D.Double)point.clone();
            this.initiallyVisibleRect = this.mapComponent.getVisibleArea();
            return;
        }
        
        // do the panning
        final double dx = dragStartPos.getX() - point.getX();
        final double dy = dragStartPos.getY() - point.getY();
        mapComponent.offsetVisibleArea(dx, dy);
    }
    
    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        this.dragStartPos = null;
        
        // change cursor to open hand
        setDefaultCursor();
    }
    
    /**
     * Treat escape key events. Stop drawing the rectangle and revert to the
     * previous state, without doing anything.
     * The event can be consumed (return true) or be delegated to other
     * listeners (return false).
     * @param keyEvent The new key event.
     * @return True if the key event has been consumed, false otherwise.
     */
    @Override
    public boolean keyEvent(KeyEvent keyEvent) {
        final boolean keyReleased = keyEvent.getID() == KeyEvent.KEY_RELEASED;
        final boolean isEscapeKey = keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE;
        
        if (keyReleased && isEscapeKey) {
            // abort dragging and reset the visible area to the initial state.
            this.dragStartPos = null;
            this.mapComponent.zoomOnRectangle(this.initiallyVisibleRect);
            mapComponent.repaint();
            
            // change cursor to open hand
            setDefaultCursor();
        }
        
        return false;
    }
    
    @Override
    protected String getCursorName() {
        return "pan";
    }
}
