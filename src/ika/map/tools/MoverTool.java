/*
 * MoverTool.java
 *
 * Created on April 20, 2005, 7:14 PM
 */

package ika.map.tools;

import ika.geo.*;
import ika.utils.FocusUtils;
import ika.gui.MapComponent;
import java.awt.geom.*;
import java.awt.event.*;

/**
 * MoverTool - a tool to move GeoObjects by dragging them with the mouse, or
 * moving them with an arrow key.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MoverTool extends SelectionEditingTool implements CombinableTool {
    
    /**
     * The number of pixels by which selected objects are moved when an
     * arrow key is pressed.
     */
    final double KEY_MOVE_DIST = 3;
    
    /**
     * The number of pixels by which selected objects are moved when an
     * arrow key is pressed and the shift key is down.
     */
    final double KEY_SHIFT_MOVE_DIST = 15;
    
    
    /**
     * if selectOnDragStart is true, GeoObjects that are not selected are
     * selected when a click on them is detected and if this click is followed
     * by a dragging action.
     */
    private boolean selectOnDragStart = false;
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public MoverTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * The mouse started a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        // search if mouse started drag by a mouse down event on a GeoObject
        GeoObject geoObject = mapComponent.getObjectAtPosition(point,
                SelectionTool.CLICK_PIXEL_TOLERANCE, true, true);
        
        if (geoObject == null) {
            // no object found under cursor
            this.startPoint = null;
            return;
        }
        
        if (this.selectOnDragStart
                && !geoObject.isSelected()
                && geoObject.isSelectable()) {
            // We received a click on an unselected item followed by dragging.
            mapComponent.selectByPoint(point, false, SelectionTool.CLICK_PIXEL_TOLERANCE);
        }
        
        // remember the start position of the drag
        this.startPoint = (Point2D.Double)point.clone();
        
        this.captureBackground();
        
    }
    
    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        if (this.startPoint == null)
            return;
        try {
            // Test whether the objects were altered during the dragging.
            if (!this.isDragging() || !this.differentFromStartPoint(point))
                return;
            
            // compute the moving distance
            final double dx = point.getX() - this.startPoint.getX();
            final double dy = point.getY() - this.startPoint.getY();
            final boolean moved = evt.isAltDown() ?
            mapComponent.cloneAndMoveSelectedGeoObjects(dx, dy)
            : mapComponent.moveSelectedGeoObjects(dx, dy);
            
            if (this.differentFromStartPoint(point))
                this.mapComponent.addUndo("Move");
        } finally {
            this.release();
        }
    }
    
    /**
     * Treat arrow key events.
     * The event can be consumed (return true) or be delegated to other
     * listeners (return false).
     * @param keyEvent The new key event.
     * @return True if the key event has been consumed, false otherwise.
     */
    public boolean keyEvent(KeyEvent keyEvent) {
        
        // give parent class a chance to treat escape key strokes
        if (super.keyEvent(keyEvent))
            return true;
        
        // don't test for KEY_RELEASED. A series of arrow key events should move
        // the selected objects continuosly.
        
        // before computing anything, find out if an arrow key was pressed
        double dx = 0;
        double dy = 0;
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_UP:
                dy = 1;
                break;
            case KeyEvent.VK_DOWN:
                dy = -1;
                break;
            case KeyEvent.VK_LEFT:
                dx = -1;
                break;
            case KeyEvent.VK_RIGHT:
                dx = 1;
                break;
        }
        
        if (dx == 0 && dy == 0)
            return false;
        
        // make sure the parent window of the mapComponent owns the focus.
        if (!FocusUtils.parentWindowHasFocus(this.mapComponent))
            return false;
        
        // make sure the component with the current focus does not react
        // on arrow key strokes.
        if (FocusUtils.currentFocusOwnerListensForKey(keyEvent.getKeyCode()))
            return false;
        
        // no other component is handling arrow key strokes,
        // it is save to move the currently selected objects.
        final double scale = this.mapComponent.getScaleFactor();
        final double d;
        if (keyEvent.isShiftDown())
            d = KEY_SHIFT_MOVE_DIST / scale;
        else
            d = KEY_MOVE_DIST / scale;
        dx *= d;
        dy *= d;
        
        final boolean moved = keyEvent.isAltDown() ?
            mapComponent.cloneAndMoveSelectedGeoObjects(dx, dy)
            : mapComponent.moveSelectedGeoObjects(dx, dy);
        
        this.mapComponent.addUndo("Move");
        
        // inform MapToolActionListeners about action
        if (moved)
            ;//this.informMapToolActionListeners("Move"); !!! ???
        
        return moved;
    }
    
    protected String getCursorName() {
        return "movearrow";
    }
    
    /**
     * Returns whether the tool is currently dragging an object.
     */
    public boolean isDragging() {
        return this.startPoint != null;
    }
    
    public boolean isSelectOnDragStart() {
        return selectOnDragStart;
    }
    
    public void setSelectOnDragStart(boolean selectOnDragStart) {
        this.selectOnDragStart = selectOnDragStart;
    }
    
    protected AffineTransform computeTransform(Point2D.Double point, MouseEvent evt) {
        final double dx = point.getX() - startPoint.getX();
        final double dy = point.getY() - startPoint.getY();
        return AffineTransform.getTranslateInstance(dx, dy);
    }

    public boolean adjustCursor(Point2D.Double point) {
        return false;
    }

}
