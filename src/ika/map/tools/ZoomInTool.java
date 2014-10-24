/*
 * ZoomInTool.java
 *
 * Created on April 8, 2005, 12:16 PM
 */
package ika.map.tools;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import ika.utils.*;
import ika.gui.MapComponent;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ZoomInTool - a tool to zoom in a map.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ZoomInTool extends RectangleTool {

    /**
     * If the user presses the right mouse button, zoom out and show the 
     * zoom-out cursor for this long. Unit: milliseconds.
     */
    private static final long ZOOM_OUT_CURSOR_VISIBLE_TIME = 500;

    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public ZoomInTool(MapComponent mapComponent) {
        super(mapComponent);
    }

    /**
     * The mouse was clicked, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        super.mouseClicked(point, evt);
        // make behaviour dependent on mousebutton (thanks to Geert Kloosterman)
        if (evt.getButton() == MouseEvent.BUTTON1) {
            mapComponent.zoomIn(point);
        } else if (evt.getButton() != MouseEvent.NOBUTTON) {
            this.showTemporaryZoomOutCursorIcon();
            mapComponent.zoomOut(point);
        }
    }

    /**
     * A drag ends, while this MapTool was the active one.
     * @param point The location of the mouse in world coordinates.
     * @param evt The original event.
     */
    @Override
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        Rectangle2D.Double rect = getRectangle();
        boolean rectangleIsLargeEnough = isRectangleLargeEnough();
        super.endDrag(point, evt);

        // Geert Kloosterman: make behaviour dependent on mousebutton
        if (rectangleIsLargeEnough) {
            if (evt.getButton() == MouseEvent.BUTTON1) {
                mapComponent.zoomOnRectangle(rect);
            } else if (evt.getButton() != MouseEvent.NOBUTTON) {
                this.showTemporaryZoomOutCursorIcon();
                mapComponent.zoomOut(point);
            }
        }
    }

    private void showTemporaryZoomOutCursorIcon() {
        // temporarily change the mouse-cursor to the zoom-out icon
        CursorUtils.setCursor("zoomout", mapComponent);
        final String zoomoutCursorName = mapComponent.getCursor().getName();
        new Timer().schedule(new TimerTask() {

            public void run() {
                Cursor currentCursor = mapComponent.getCursor();
                if (zoomoutCursorName.equals(currentCursor.getName())) {
                    CursorUtils.setCursor("zoomin", mapComponent);
                }
            }
        }, ZOOM_OUT_CURSOR_VISIBLE_TIME);
    }

    @Override
    protected String getCursorName() {
        return "zoomin";
    }
}
