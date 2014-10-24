/*
 * PointSymbol.java
 *
 * Created on May 13, 2005, 12:08 PM
 */
package ika.geo;

import java.awt.*;

/**
 *
 * @author jenny
 */
public class PointSymbol extends VectorSymbol implements java.io.Serializable {

    private static final long serialVersionUID = -755322865498970894L;
    /**
     * The radius used to draw a circle around the point. We mix here geometry
     * and graphic attributes, which is not how it should be done.
     */
    private double radius = 3;
    /**
     * The length of the radial lines used to draw this PointSymbol.
     */
    private double lineLength = 6;

    public PointSymbol() {
        this.filled = false;
        this.stroked = true;
        this.strokeColor = Color.BLACK;
        this.strokeWidth = 2;
        this.scaleInvariant = true;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getLineLength() {
        return lineLength;
    }

    public void setLineLength(double lineLength) {
        this.lineLength = lineLength;
    }

    /**
     * Returns a path that can be used to draw this point.
     * @param scale The current scale of the map.
     * @return The path that can be used to draw this point.
     */
    public GeoPath getPointSymbol(double scale, double x, double y) {

        // geometry
        final double l = scaleInvariant ? getLineLength() / scale : getLineLength();
        final double r = scaleInvariant ? getRadius() / scale : getRadius();
        GeoPath path = GeoPath.newCircle(x, y, r);
        if (l > 0) {
            path.moveTo(r + x, y);
            path.lineTo(r + l + x, y);
            path.moveTo(-r + x, y);
            path.lineTo(-r - l + x, y);
            path.moveTo(x, r + y);
            path.lineTo(x, r + l + y);
            path.moveTo(x, -r + y);
            path.lineTo(x, -r - l + y);
        }

        // symbolization
        path.setVectorSymbol(this);

        return path;

    }

    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist,
            double scale, double x, double y) {

        final double px = point.getX();
        final double py = point.getY();
        final double r = scaleInvariant ? getRadius() / scale : getRadius();
        final double halfStrokeWidth = getScaledStrokeWidth(scale) / 2;
        final double l = scaleInvariant ? getLineLength() / scale : getLineLength();

        // test if point is in bounding box (including the radial lines).
        if (px < x - r - l
                || px > x + r + l
                || py < y - r - l
                || py > y + r + l) {
            return false;
        }

        // test if point is inside central circle
        final double dx = point.getX() - x;
        final double dy = point.getY() - y;
        final double dsquare = dx * dx + dy * dy;
        if (dsquare <= (r + halfStrokeWidth) * (r + halfStrokeWidth)) {
            return true;
        }

        // test if point is on one of the straight lines
        // right
        if (px >= x + r
                && px <= x + r + l
                && py >= y - halfStrokeWidth
                && py <= y + halfStrokeWidth) {
            return true;
        }
        // left
        if (px >= x - r - l
                && px <= x - r
                && py >= y - halfStrokeWidth
                && py <= y + halfStrokeWidth) {
            return true;
        }
        // bottom
        if (px >= x - halfStrokeWidth
                && px <= x + halfStrokeWidth
                && py >= y - r - l
                && py <= y - r) {
            return true;
        }
        // top
        if (px >= x - halfStrokeWidth
                && px <= x + halfStrokeWidth
                && py >= y + r
                && py <= y + r + l) {
            return true;
        }

        return false;
    }

   

    protected void drawPointSymbol(RenderParams rp, boolean isSelected, double x, double y) {
        final GeoPath pointSymbol = getPointSymbol(rp.scale, x, y);
        pointSymbol.drawNormalState(rp);
    }
}
