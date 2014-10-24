/*
 * PenTool.java
 *
 * Created on April 21, 2005, 6:10 PM
 */
package ika.map.tools;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;
import ika.geo.*;
import ika.gui.MapComponent;
import ika.utils.ColorUtils;

/**
 * MeasureTool - a tool to measure distances between two points.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MeasureTool extends DoubleBufferedTool {

    /**
     * The location to start measuring.
     */
    private Point2D.Double dragStartPos;
    /**
     * The current position to measur to.
     */
    private Point2D.Double dragCurrentPos;
    /**
     * A set of MeasurToolListeners that will be informed when a new distance has
     * been computed.
     */
    private Set listeners = new HashSet();

    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public MeasureTool(MapComponent mapComponent) {
        super(mapComponent);
    }

    @Override
    public void deactivate() {
        reportClearDistance();
        mapComponent.repaint();
    }

    /**
     * Adds a MeasureToolListener.
     * @param listener The MeasureToolListener to add.
     */
    public void addMeasureToolListener(MeasureToolListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.listeners.add(listener);
    }

    /**
     * Removes a MeasureToolListener.
     * @param listener The MeasureToolListener to remove.
     */
    public void removeMeasureToolListener(MeasureToolListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Inform all registered MeasureToolListeners of a new distance.
     */
    private void reportDistance(boolean finalDistance) {
        if (dragStartPos == null || dragCurrentPos == null) {
            return;
        }

        final double dx = dragCurrentPos.x - dragStartPos.x;
        final double dy = dragCurrentPos.y - dragStartPos.y;
        final double d = Math.sqrt(dx * dx + dy * dy);
        final double angle = Math.atan2(dy, dx);

        Iterator iterator = this.listeners.iterator();
        while (iterator.hasNext()) {
            MeasureToolListener listener = (MeasureToolListener) iterator.next();
            if (finalDistance) {
                listener.newDistance(d, angle, this.mapComponent);
            } else {
                listener.distanceChanged(d, angle, this.mapComponent);
            }
        }
    }

    /**
     * Inform all registered MeasureToolListeners that the distance is not valid anymore.
     */
    private void reportClearDistance() {
        Iterator iterator = this.listeners.iterator();
        while (iterator.hasNext()) {
            MeasureToolListener listener = (MeasureToolListener) iterator.next();
            listener.clearDistance();
        }
    }

    /**
     * The mouse was pressed down, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseDown(Point2D.Double point, MouseEvent evt) {
        setMeasureCursor();
        captureBackground();
    }

    /**
     * The mouse starts a drag, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        setMeasureCursor();
        this.dragStartPos = (Point2D.Double) point.clone();
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
            setMeasureCursor();
            return;
        }

        // if this is the first time mouseDragged is called, capture the screen.
        if (dragCurrentPos == null) {
            captureBackground();
        }

        dragCurrentPos = (Point2D.Double) point.clone();
        mapComponent.repaint();

        reportDistance(false);
    }

    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        /*
        dragStartPos = null;
        dragCurrentPos = null;
         */
        reportDistance(true);
        releaseBackground();
        mapComponent.repaint();
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
            dragStartPos = null;
            dragCurrentPos = null;
            releaseBackground();
            mapComponent.repaint();
            setDefaultCursor();
            reportClearDistance();
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

        rp.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        rp.g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        GeoPath p = new GeoPath();
        p.getVectorSymbol().setStrokeColor(ColorUtils.getHighlightColor());
        p.getVectorSymbol().setScaleInvariant(true);
        p.getVectorSymbol().setStrokeWidth(1);
        p.moveTo(dragStartPos.getX(), dragStartPos.getY());
        p.lineTo(dragCurrentPos.getX(), dragCurrentPos.getY());
        p.drawNormalState(rp);
    }

    /**
     * Utility method to change the cursor to a cross-hair cursor.
     */
    private void setMeasureCursor() {
        mapComponent.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }
    
    @Override
    protected String getCursorName() {
        return "crosshair";
    }
}
