/*
 * RectangleTool.java
 *
 * Created on April 8, 2005, 2:47 PM
 */
package ika.map.tools;

import ika.geo.RenderParams;
import java.awt.geom.*;
import java.awt.event.*;
import ika.gui.MapComponent;
import ika.gui.SelectionBox;

/**
 * An abstract tool that draws a rectangle when dragging the mouse.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class RectangleTool extends DoubleBufferedTool {

    /**
     * The start position of the drag.
     */
    protected Point2D.Double dragStartPos;
    /**
     * The current position of the drag.
     */
    protected Point2D.Double dragCurrentPos;
    /**
     * The dash length used for drawing the rectangle with dashes.
     */
    protected static final int DASH_LENGTH = 3;
    /**
     * MIN_RECT_DIM_PX is used by isRectangleLargeEnough to test whether
     * the currently drawn rectangle is considered to be large enough.
     * E.g. with the magnifier tool, if the user only draws a rectangle of
     * 1x1 pixel, the view will not zoom to such a small area, which would
     * be confusing.
     * Unit: screen pixel
     */
    protected static final int MIN_RECT_DIM_PX = 3;

    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    protected RectangleTool(MapComponent mapComponent) {
        super(mapComponent);
    }

    /**
     * The mouse starts a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        dragStartPos = (Point2D.Double) point.clone();
    }

    /**
     * The mouse location changed during a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void updateDrag(Point2D.Double point, MouseEvent evt) {
        // just in case we didn't get a mousePressed-Event
        if (dragStartPos == null) {
            dragStartPos = (Point2D.Double) point.clone();
            return;
        }

        // if this is the first time mouseDragged is called, capture the screen.
        if (dragCurrentPos == null) {
            captureBackground();
        }

        dragCurrentPos = (Point2D.Double) point.clone();
        mapComponent.repaint();
    }

    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        // release the corner points of the drag rectangle
        this.dragStartPos = this.dragCurrentPos = null;

        releaseBackground();
        mapComponent.repaint();
    }

    /**
     * Returns whether the tool is currently dragging.
     */
    @Override
    public boolean isDragging() {
        return dragStartPos != null;
    }

    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        this.dragStartPos = this.dragCurrentPos = null;
        releaseBackground();
        mapComponent.repaint();
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
            // release the corner points of the drag rectangle
            this.dragStartPos = this.dragCurrentPos = null;

            // repaint the map
            releaseBackground();
            mapComponent.repaint();
        }

        return false;
    }

    /**
     * Draw the interface elements of this MapTool.
     * @param rp The destination to draw to.
     */
    @Override
    public void draw(RenderParams rp) {
        if (dragStartPos == null || dragCurrentPos == null) {
            return;
        }

        SelectionBox.drawDashedRectangle(dragStartPos, dragCurrentPos, rp, DASH_LENGTH);
    }

    /**
     * Returns the rectangle formed by the start location and the current drag location.
     * @return The rectangle.
     */
    protected Rectangle2D.Double getRectangle() {
        if (dragStartPos == null || dragCurrentPos == null) {
            return null;
        }
        double x = Math.min(dragStartPos.getX(), dragCurrentPos.getX());
        double y = Math.min(dragStartPos.getY(), dragCurrentPos.getY());
        double w = Math.abs(dragCurrentPos.getX() - dragStartPos.getX());
        double h = Math.abs(dragCurrentPos.getY() - dragStartPos.getY());
        return new Rectangle2D.Double(x, y, w, h);
    }

    /**
     * Returns whether the currently drawn rectangle is large enough.
     * E.g. with the magnifier tool, if the user only draws a rectangle of
     * 1 x 1 pixel, the view will not zoom to such a small area, as it would
     * be confusing.
     */
    protected boolean isRectangleLargeEnough() {
        final Rectangle2D.Double rect = this.getRectangle();
        if (rect == null) {
            return false;
        }
        final double minRectDim = MIN_RECT_DIM_PX / this.mapComponent.getScaleFactor();
        return (rect.width >= minRectDim && rect.height >= minRectDim);
    }
}
