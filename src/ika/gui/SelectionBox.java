/*
 * SelectionBox.java
 *
 * Created on May 31, 2006, 5:06 PM
 *
 */
package ika.gui;

import ika.geo.GeoPath;
import ika.geo.RenderParams;
import ika.utils.ColorUtils;
import java.awt.geom.*;
import java.awt.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class SelectionBox {

    public static final int SELECTION_HANDLE_NONE = -1;
    // counter clock-wise direction starting with lower left handle
    public static final int SELECTION_HANDLE_LOWER_LEFT = 0;
    public static final int SELECTION_HANDLE_LOWER_RIGHT = 1;
    public static final int SELECTION_HANDLE_UPPER_RIGHT = 2;
    public static final int SELECTION_HANDLE_UPPER_LEFT = 3;
    public static final int SELECTION_HANDLE_UPPER_CENTER = 4;
    public static final int SELECTION_HANDLE_LOWER_CENTER = 5;
    public static final int SELECTION_HANDLE_LEFT_CENTER = 6;
    public static final int SELECTION_HANDLE_RIGHT_CENTER = 7;
    public static final double HANDLE_BOX_SIZE = 6;
    public static final double SELECTED_STROKE_WIDTH = 0.5;

    public static Rectangle2D[] getSelectionHandles(Rectangle2D selectionBox,
            double mapScale) {

        if (selectionBox == null) {
            return null;
        }

        Rectangle2D[] handles = new Rectangle2D[8];
        final double minX = selectionBox.getMinX();
        final double minY = selectionBox.getMinY();
        final double maxX = selectionBox.getMaxX();
        final double maxY = selectionBox.getMaxY();
        final double centerX = (minX + maxX) / 2;
        final double centerY = (minY + maxY) / 2;

        final double handleDim = HANDLE_BOX_SIZE / mapScale;
        final double halfHandleDim = handleDim / 2.;
        handles[SELECTION_HANDLE_LOWER_LEFT] = new Rectangle2D.Double(
                minX - halfHandleDim, minY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_LOWER_RIGHT] = new Rectangle2D.Double(
                maxX - halfHandleDim, minY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_UPPER_RIGHT] = new Rectangle2D.Double(
                maxX - halfHandleDim, maxY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_UPPER_LEFT] = new Rectangle2D.Double(
                minX - halfHandleDim, maxY - halfHandleDim, handleDim, handleDim);

        handles[SELECTION_HANDLE_UPPER_CENTER] = new Rectangle2D.Double(
                centerX - halfHandleDim, maxY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_LOWER_CENTER] = new Rectangle2D.Double(
                centerX - halfHandleDim, minY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_LEFT_CENTER] = new Rectangle2D.Double(
                minX - halfHandleDim, centerY - halfHandleDim, handleDim, handleDim);
        handles[SELECTION_HANDLE_RIGHT_CENTER] = new Rectangle2D.Double(
                maxX - halfHandleDim, centerY - halfHandleDim, handleDim, handleDim);

        return handles;
    }

    public static void paintSelectionBox(Rectangle2D selectionBox,
            RenderParams rp, boolean drawHandles) {
        drawRectangle(selectionBox, rp, drawHandles, 0);
    }

    /**
     * Draw a rectangle
     * @param selectionBox The rectangle to draw.
     * @param rp The render parameters for the map.
     * @param drawHandles If true, handles are drawn at the corner of the rectangle.
     * @param dashLength If larger than 0, a dashed line is used.
     */
    public static void drawRectangle(Rectangle2D box,
            RenderParams rp, boolean drawHandles, int dashLength) {

        // don't test for selectionBox.isEmpty() which returns true if
        // the width or height are negative!

        if (box == null || rp.g2d == null) {
            return;
        }

        rp.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        rp.g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);

        // make sure the rectangle has positive width and height for drawing
        double w = box.getWidth();
        double h = box.getHeight();
        if (w < 0 || h < 0) {
            final double x = box.getX();
            final double y = box.getY();
            final Rectangle2D rect;
            if (w < 0 && h < 0) {
                w = Math.abs(w);
                h = Math.abs(h);
                box = new Rectangle2D.Double(x - w, y - h, w, h);
            } else if (w < 0) {
                w = Math.abs(w);
                box = new Rectangle2D.Double(x - w, y, w, h);
            } else {
                h = Math.abs(h);
                box = new Rectangle2D.Double(x, y - h, w, h);
            }
        }

        GeoPath rectPath = GeoPath.newRect(box);
        initSymbol(rectPath, ColorUtils.getHighlightColor(), null, dashLength);
        rectPath.drawNormalState(rp);

        // draw handles
        if (drawHandles) {
            Rectangle2D[] handles = SelectionBox.getSelectionHandles(box, rp.scale);
            if (handles != null) {
                for (int i = 0; i < handles.length; i++) {
                    GeoPath p = GeoPath.newRect(handles[i]);
                    initSymbol(p, ColorUtils.getHighlightColor(), Color.WHITE, 0);
                    p.drawNormalState(rp);
                }
            }
        }

        rp.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        rp.g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    /**
     * Draws a rectangle defined by two points using a dashed line. Takes care of
     * avoiding the "marching ants" effect.
     * @param p1 Start point of the rectangle. This point is supposed to stay put
     * between multiple calls.
     * @param p2 End point of the rectangle. This point is supposed to move 
     * between multiple calls.
     * @param rp Rendering parameters.
     * @param dashLength The length of dashes in pixels.
     */
    public static void drawDashedRectangle(Point2D p1, Point2D p2,
            RenderParams rp, int dashLength) {

        if (p1 == null || p2 == null || rp.g2d == null) {
            return;
        }

        RenderingHints initialRenderingHints = rp.g2d.getRenderingHints();
        rp.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        rp.g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);

        drawRectangleLines(p1, p2, rp, dashLength);
        
        rp.g2d.setRenderingHints(initialRenderingHints);
    }

    private static void initSymbol(GeoPath p,
            Color strokeColor,
            Color fillColor,
            int dashLength) {

        p.getVectorSymbol().setStrokeColor(strokeColor);
        // setup stroke. Use a line width of 0 for a thin line. This is ok, see
        // java bug #4114921 and #4093921.
        p.getVectorSymbol().setStrokeWidth(0);
        if (dashLength > 0) {
            p.getVectorSymbol().setDashLength(dashLength);
        }

        if (fillColor != null) {
            p.getVectorSymbol().setFilled(true);
            p.getVectorSymbol().setFillColor(fillColor);
        }
    }

    /**
     * draw 4 lines forming a rectangle. Useful to reduce the marching ants effect.
     * @param rect
     * @param rp
     * @param dashLength 
     */
    private static void drawRectangleAsLines(Rectangle2D rect, RenderParams rp, int dashLength) {
        final double west = rect.getMinX();
        final double east = rect.getMaxX();
        final double south = rect.getMinY();
        final double north = rect.getMaxY();
        GeoPath p = new GeoPath();
        initSymbol(p, ColorUtils.getHighlightColor(), null, dashLength);
        p.moveTo(west, south);
        p.lineTo(east, south);
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(east, north);
        p.lineTo(east, south);
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(west, north);
        p.lineTo(east, north);
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(west, north);
        p.lineTo(west, south);
        p.drawNormalState(rp);
    }
    
    /**
     * Individually draws each line of a rectangle. This is useful for reducing
     * the "marching ants" effect.
     * @param p1 Start point of the rectangle. This point is supposed to stay put
     * between multiple calls.
     * @param p2 End point of the rectangle. This point is supposed to move 
     * between multiple calls.
     * @param rp Rendering parameters.
     * @param dashLength The length of dashes in pixels.
     */
    private static void drawRectangleLines(Point2D p1, Point2D p2, RenderParams rp, int dashLength) {
        GeoPath p = new GeoPath();
        initSymbol(p, ColorUtils.getHighlightColor(), null, dashLength);
        p.moveTo(p1.getX(), p1.getY());
        p.lineTo(p2.getX(), p1.getY());
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(p1.getX(), p1.getY());
        p.lineTo(p1.getX(), p2.getY());
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(p1.getX(), p2.getY());
        p.lineTo(p2.getX(), p2.getY());
        p.drawNormalState(rp);
        p.reset();
        p.moveTo(p2.getX(), p1.getY());
        p.lineTo(p2.getX(), p2.getY());
        p.drawNormalState(rp);
    }

    /**
     * Returns a handle ID for a passed point.
     * @param point The point for which to search the handle ID.
     * @selectionBox The bounding box.
     * @mapScale The current map scale.
     * @return The handle ID between 0 and 7, or SELECTION_HANDLE_NONE if
     * the passed point is not on a handle.
     */
    public static int findBoxHandle(Point2D point,
            Rectangle2D selectionBox, double mapScale) {

        Rectangle2D[] handles = SelectionBox.getSelectionHandles(selectionBox, mapScale);
        if (handles != null) {
            for (int i = 0; i < handles.length; i++) {
                if (handles[i].contains(point)) {
                    return i;
                }
            }
        }
        return SELECTION_HANDLE_NONE;
    }

    public static void adjustSelectionBox(Rectangle2D selectionBox,
            int draggedHandle,
            Point2D.Double point,
            boolean uniformScaling,
            double initialRatio) {

        final double x = selectionBox.getMinX();
        final double y = selectionBox.getMinY();
        final double w = selectionBox.getWidth();
        final double h = selectionBox.getHeight();
        final double px = point.getX();
        final double py = point.getY();
        double newX = x;
        double newY = y;
        double newW = w;
        double newH = h;
        switch (draggedHandle) {
            // corner handles
            case SelectionBox.SELECTION_HANDLE_LOWER_LEFT:
                newX = px;
                newY = py;
                newW = w + x - px;
                newH = h + y - py;
                break;
            case SelectionBox.SELECTION_HANDLE_LOWER_RIGHT:
                newY = py;
                newW = px - x;
                newH = h + y - py;
                break;
            case SelectionBox.SELECTION_HANDLE_UPPER_RIGHT:
                newW = px - x;
                newH = py - y;
                break;
            case SelectionBox.SELECTION_HANDLE_UPPER_LEFT:
                newX = px;
                newW = w + x - px;
                newH = py - y;
                break;

            // central handles
            case SelectionBox.SELECTION_HANDLE_UPPER_CENTER:
                newH = py - y;
                break;
            case SelectionBox.SELECTION_HANDLE_LOWER_CENTER:
                newY = py;
                newH = h + y - py;
                break;
            case SelectionBox.SELECTION_HANDLE_LEFT_CENTER:
                newX = px;
                newW = w + x - px;
                break;
            case SelectionBox.SELECTION_HANDLE_RIGHT_CENTER:
                newW = px - x;
                break;
        }

        if (uniformScaling) {
            final double wRatio = newW / w;
            final double hRatio = newH / h;
            if (wRatio < hRatio) {
                newH = newW / initialRatio;
            } else {
                newW = initialRatio * newH;
            }
        }

        selectionBox.setFrame(newX, newY, newW, newH);
    }
}
