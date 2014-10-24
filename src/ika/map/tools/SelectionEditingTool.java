/*
 * SelectionEditingTool.java
 *
 * Created on March 26, 2007, 12:07 AM
 *
 */

package ika.map.tools;

import ika.geo.*;
import ika.gui.MapComponent;
import ika.gui.SelectionBox;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * An abstract base class for tools that interactively scale, rotate or move 
 * selected objects. Manages an AffineTransform that is used to draw selected
 * objects while dragging. With this approach, the selected objects don't need
 * to be copied and their geometry be transformed during an interactive dragging
 * operation.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
abstract public class SelectionEditingTool extends DoubleBufferedTool {
    
    /**
     * The box surounding all selected objects before dragging starts.
     */
    protected Rectangle2D initialSelectionBox = null;
        
    /**
     * Remember the start location of the drag. This must be initialized by
     * derived classes in the startDrag method with:
     * this.startPoint = (Point2D.Double)point.clone();
     */
    protected Point2D.Double startPoint;
   
    /** Creates a new instance of SelectionEditingTool */
    public SelectionEditingTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    public boolean isDragging() {
        return initialSelectionBox != null;
    }
    
    /**
     * Returns whether the passed point differs from the point where the 
     * user clicked to start the dragging.
     */
    protected boolean differentFromStartPoint(Point2D point) {
        if (this.startPoint == null)
            return false;
        return point.distanceSq(this.startPoint) > 0;
    }
    
    /**
     * Returns an affine transformation that is applied on selected objects 
     * before drawing them during a dragging process.
     */
    abstract protected AffineTransform computeTransform(Point2D.Double point, 
            MouseEvent evt);
       
    /**
     * The mouse location changed during a drag, while this MapTool was the active one.
     * @param pointThe location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void updateDrag(Point2D.Double point, MouseEvent evt) {
        if (!this.isDragging())
            return;
        
        // store the current affine transformation for the selected objects
        // in the MapComponent. This transformation will be used to draw
        // selected GeoObjects
        AffineTransform trans = this.computeTransform(point, evt);
        this.mapComponent.setTransformForSelectedObjects(trans);
        
        // Force a redraw of the map.
        this.mapComponent.repaint();
    }
    
    /**
     * Release all allocated resources and reset variables. Derived classes must
     * call this method in their endDrag method.
     */
    protected void release() {
        this.initialSelectionBox = null;
        this.startPoint = null;
        this.mapComponent.setTransformForSelectedObjects(null);
        this.releaseBackground();
        this.setDefaultCursor();
    }
    
    /**
     * Draws the selected GeoObjects
     */
    public void draw(RenderParams rp) {
        if (!this.isDragging())
            return;
        
        AffineTransform originalTrans = rp.g2d.getTransform();
        try {
            // draw all selected GeoObjects of the map, but not the unselected ones.
            this.mapComponent.shiftGraphics2DByBorderWidth(rp.g2d);
            this.mapComponent.paintMap(rp.g2d, true);
            
            // paint a box around all selected objects
            SelectionBox.paintSelectionBox(initialSelectionBox, rp, false);
        } finally {
            rp.g2d.setTransform(originalTrans);
        }
    }
    
    /**
     * Treat escape key events.
     * The event can be consumed (return true) or be delegated to other
     * listeners (return false).
     * @param keyEvent The new key event.
     * @return True if the key event has been consumed, false otherwise.
     */
    public boolean keyEvent(KeyEvent keyEvent) {
        
        if (keyEvent.getKeyCode() != KeyEvent.VK_ESCAPE)
            return false;
        
        if (this.isDragging() == false)
            return false;
        
        this.release();
        return true;
    }
    
    protected String getCursorName() {
        return "arrow";
    }

}
