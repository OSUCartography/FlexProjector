/*
 * GeoControlPoint.java
 *
 * Created on May 13, 2005, 6:15 PM
 */

package ika.geo;

import java.awt.geom.*;

/**
 *
 */
public class GeoControlPoint extends GeoPoint{
    
    private double destX = Double.NaN;
    private double destY = Double.NaN;
    
    /**
     * Create a new instance.
     * @param point The location of the point.
     */
    public GeoControlPoint(Point2D point) {
        super (point, true);
    }
    
    public GeoControlPoint(double x, double y) {
        super (new Point2D.Double (x, y), true);
    }

    public double getDestX() {
        return destX;
    }

    public void setDestX(double destX) {
        this.destX = destX;
        MapEventTrigger.inform(this);
    }

    public double getDestY() {
        return destY;
    }

    public void setDestY(double destY) {
        this.destY = destY;
        MapEventTrigger.inform(this);
    }
    
    
}
