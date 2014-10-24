/*
 * Scale.java
 *
 * Created on May 31, 2006, 11:15 AM
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
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ScaleTool extends SelectionEditingTool implements CombinableTool {
    
    private int draggedHandle = SelectionBox.SELECTION_HANDLE_NONE;
    
    /** Creates a new instance of SelectionMoveScaleTool */
    public ScaleTool(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    /**
     * The mouse starts a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        final double mapScale = this.mapComponent.getScaleFactor();
        
        // test if there are any selected GeoObjects and find bounding box of them
        this.initialSelectionBox = this.mapComponent.getBoundingBoxOfSelectedGeoObjects();
        
        // test if click was on a handle of the selection box
        this.draggedHandle = SelectionBox.findBoxHandle(point,
                initialSelectionBox, mapScale);
        if (this.draggedHandle == SelectionBox.SELECTION_HANDLE_NONE
                || this.initialSelectionBox == null
                || this.initialSelectionBox.isEmpty()) {
            // there are no selected GeoObjects
            this.initialSelectionBox = null;
            this.draggedHandle = SelectionBox.SELECTION_HANDLE_NONE;
            return;
        }
        
        this.captureBackground();
        
        // remember the start position of the drag
        this.startPoint = (Point2D.Double)point.clone();
    }
    
    protected AffineTransform computeTransform(Point2D.Double point, MouseEvent evt) {
        Rectangle2D scaledSelectionBox = (Rectangle2D)this.initialSelectionBox.clone();
        final double selectionBoxRatio = this.initialSelectionBox.getWidth()
        / this.initialSelectionBox.getHeight();
        final boolean uniformScaling = evt.isShiftDown();
        SelectionBox.adjustSelectionBox(scaledSelectionBox,
                this.draggedHandle, point, uniformScaling, selectionBoxRatio);
        
        final double hScale = scaledSelectionBox.getWidth() / this.initialSelectionBox.getWidth();
        final double vScale = scaledSelectionBox.getHeight() / this.initialSelectionBox.getHeight();
        
        AffineTransform t = new AffineTransform();
        t.translate(+scaledSelectionBox.getMinX(), +scaledSelectionBox.getMinY());
        t.scale(hScale, vScale);
        t.translate(-this.initialSelectionBox.getMinX(), -this.initialSelectionBox.getMinY());
        
        return t;
    }
    
    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        try {
            // Test whether the objects were altered during the dragging.
            if (!this.isDragging() || !this.differentFromStartPoint(point))
                return;
            
            // apply the resulting transformation on the selected GeoObjects
            AffineTransform trans = this.computeTransform(point, evt);
            if (trans == null)
                return;
            this.mapComponent.transformSelectedGeoObjects(trans);
            
            this.mapComponent.addUndo("Scale");
        } finally {
            this.draggedHandle = SelectionBox.SELECTION_HANDLE_NONE;
            this.release();
        }
    }
    
    /**
     * The mouse moved, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    public boolean adjustCursor(Point2D.Double point) {
        
        Rectangle2D selectionBox =
                this.mapComponent.getBoundingBoxOfSelectedGeoObjects();
        final double mapScale = this.mapComponent.getScaleFactor();
        int handleID = SelectionBox.findBoxHandle(point, selectionBox, mapScale);
        switch (handleID) {
            // ascending arrow
            case SelectionBox.SELECTION_HANDLE_LOWER_LEFT:
            case SelectionBox.SELECTION_HANDLE_UPPER_RIGHT:
                ika.utils.CursorUtils.setCursor("scaleasc", this.mapComponent);
                return true;
                
                // descending arrow
            case SelectionBox.SELECTION_HANDLE_LOWER_RIGHT:
            case SelectionBox.SELECTION_HANDLE_UPPER_LEFT:
                ika.utils.CursorUtils.setCursor("scaledes", this.mapComponent);
                return true;
                
                // vertical arrow
            case SelectionBox.SELECTION_HANDLE_UPPER_CENTER:
            case SelectionBox.SELECTION_HANDLE_LOWER_CENTER:
                ika.utils.CursorUtils.setCursor("scalev", this.mapComponent);
                return true;
                
                // horizontal arrow
            case SelectionBox.SELECTION_HANDLE_LEFT_CENTER:
            case SelectionBox.SELECTION_HANDLE_RIGHT_CENTER:
                ika.utils.CursorUtils.setCursor("scaleh", this.mapComponent);
                return true;
            default:
                return false;
        }
    }
    
}