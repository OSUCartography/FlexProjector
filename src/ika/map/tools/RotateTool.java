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
 * A tool that interactively rotates selected objects.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class RotateTool extends SelectionEditingTool implements CombinableTool {
    
    /**
     * Mouse must be closer to RADIUS from a corner point to start a rotation.
     */
    private static final double RADIUS = 10;

    /**
     * Minimum angle for transformation to take place.
     */
    private static final double MIN_ANGLE = Math.toRadians(0.1);
    
    /** Creates a new instance of SelectionMoveScaleTool */
    public RotateTool(MapComponent mapComponent) {
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
        if (this.initialSelectionBox == null)
            return;
        
        this.startPoint = this.searchStartPosition(point);
        if (this.startPoint == null) {
            this.initialSelectionBox = null;
            return;
        }
        
        this.captureBackground();
        
        // remember the start position of the drag
        this.startPoint = (Point2D.Double)point.clone();
        
        ika.utils.CursorUtils.setCursor("rotate", mapComponent);
    }
    
    /**
     * Returns the corner point of the selection box close to the passed point.
     * @param point Point from which to search the closest point of the selection box.
     * @param selectionBox The bounding box around the selected objects.
     * @return The closest corner point of the selection box. Null if the passed
     * point is too far.
     */
    private Point2D.Double searchStartPosition(Point2D point, Rectangle2D selectionBox) {

        if (selectionBox == null || point == null)
            return null;
        
        // don't accept points inside the selection rectangle
        if (selectionBox.contains(point))
            return null;
        
        // don't accept points inside the selection handles
        final double mapScale = this.mapComponent.getScaleFactor();
        int handleID = SelectionBox.findBoxHandle(point, selectionBox, mapScale);
        if (handleID != SelectionBox.SELECTION_HANDLE_NONE)
            return null;
        
        final double xMin = selectionBox.getMinX();
        final double xMax = selectionBox.getMaxX();
        final double yMin = selectionBox.getMinY();
        final double yMax = selectionBox.getMaxY();

        final double SQUARE_RADIUS = RADIUS * RADIUS / (mapScale * mapScale);
        
        final double dll = point.distanceSq(xMin, yMin);
        final double dlr = point.distanceSq(xMax, yMin);
        final double dur = point.distanceSq(xMax, yMax);
        final double dul = point.distanceSq(xMin, yMax);
        
        // find closest corner point of the selection box
        if (dll < SQUARE_RADIUS && dll < dlr && dll < dur && dll < dul)
            return new Point2D.Double(xMin, yMin); // lower left
        else if (dlr < SQUARE_RADIUS && dlr < dll && dlr < dur && dlr < dul)
            return new Point2D.Double(xMax, yMin); // lower right
        else if (dur < SQUARE_RADIUS && dur < dll && dur < dlr && dur < dul)
            return new Point2D.Double(xMax, yMax); // upper right
        else if (dul < SQUARE_RADIUS && dul < dll && dul < dlr && dul < dur)
            return new Point2D.Double(xMin, yMax); // upper left

        return null;
    }
    
    /**
     * Returns the corner point of the selection box close to the passed point.
     * @param point Point from which to search the closest point of the selection box.
     * @return The closest corner point of the selection box. Null if the passed
     * point is too far.
     */
    private Point2D.Double searchStartPosition(Point2D point) {
        if (this.initialSelectionBox == null)
            return null;
        return this.searchStartPosition(point, this.initialSelectionBox);
    }
    
    private double computeRotationAngle(Point2D.Double point, MouseEvent evt) {
        final double cx = this.initialSelectionBox.getCenterX();
        final double cy = this.initialSelectionBox.getCenterY();
        double angle = ika.utils.GeometryUtils.angle(
                point.getX(), point.getY(),
                cx, cy,
                this.startPoint.getX(), this.startPoint.getY());
        if (evt.isShiftDown()) {
            if (angle < 0)
                angle += 2 * Math.PI;
            angle += Math.PI / 8;
            final double PI_4 = Math.PI / 4;
            final int id = (int)(angle/ PI_4);
            angle = id * PI_4;
        }
        return angle;
    }
    
    protected AffineTransform computeTransform(Point2D.Double point, MouseEvent evt) {
        final double cx = initialSelectionBox.getCenterX();
        final double cy = initialSelectionBox.getCenterY();
        
        AffineTransform t = new AffineTransform();
        t.translate(cx, cy);
        final double angle = this.computeRotationAngle(point, evt);
        if (Math.abs(angle) < MIN_ANGLE)
            return null;
        t.rotate(angle);
        t.translate(-cx, -cy);
        
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
            
            this.mapComponent.addUndo("Rotate");
        } finally {
            this.release();
        }
    }
    
    /**
     * The mouse moved, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     */
    public boolean adjustCursor(Point2D.Double point) {
    
        Rectangle2D selectionBox = 
                this.mapComponent.getBoundingBoxOfSelectedGeoObjects();
        if(this.searchStartPosition(point, selectionBox) == null)
            return false;
        
        ika.utils.CursorUtils.setCursor("rotate", mapComponent);
        return true;
    }
}