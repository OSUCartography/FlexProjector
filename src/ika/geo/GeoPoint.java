package ika.geo;

import java.awt.geom.*;

/**
 * GeoPoint - A point that is drawn with a graphic symbol.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoPoint extends GeoObject implements java.io.Serializable {
    
    private static final long serialVersionUID = -6822508477473599463L;
    /**
     * The x coordinate of the point.
     */
    private double x = 0;
    
    /**
     * The y coordinate of the point.
     */
    private double y = 0;
    
    /**
     * The PointSymbol handles the graphic attributes of this GeoPoint.
     */
    private PointSymbol pointSymbol = new PointSymbol();

    
    /**
     * Create a new instance.
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param r The radius of the circle used to represent the point.
     */
    public GeoPoint(double x, double y, double r) {
        this.x = x;
        this.y = y;
        pointSymbol.setRadius(r);
    }
    
    /**
     * Create a new instance.
     * @param point The location of the point.
     * @param r The radius of the circle used to represent the point.
     * @param scaleInvariant Whether the point symbol should grow with the map
     *  scale or not.
     */
    public GeoPoint(Point2D point, double r, boolean scaleInvariant) {
        this.x = point.getX();
        this.y = point.getY();
        pointSymbol.setRadius(r);
        pointSymbol.setScaleInvariant(scaleInvariant);
    }
    
    /**
     * Create a new scale invariant instance.
     */
    public GeoPoint(double x, double y) {
        this.x = x;
        this.y = y;
        pointSymbol.setScaleInvariant(true);
    }
    
    /**
     * Create a new instance.
     * @param point The location of the point.
     * @param scaleInvariant Whether the point symbol should grow with the map
     *  scale or not.
     */
    public GeoPoint(Point2D point, boolean scaleInvariant) {
        this.x = point.getX();
        this.y = point.getY();
        pointSymbol.setScaleInvariant(scaleInvariant);
    }
    
    /**
     * Set the x coordinate of this point.
     * @param x The horizontal coordinate.
     */
    public void setX(double x) {
        this.x = x;
        MapEventTrigger.inform(this);
    }
    
    /**
     * Returns the x coordinate of this point.
     * @return The horizontal coordinate.
     */
    public double getX() {
        return this.x;
    }
    
    @Override
    public double getCenterX(double scale) {
        return this.x;
    }
    
    @Override
    public double getCenterY(double scale) {
        return this.y;
    }
    
    /**
     * Set the y coordinate of this point.
     * @param y The vertical coordinate.
     */
    public void setY(double y) {
        this.y = y;
        MapEventTrigger.inform(this);
    }
    
    /**
     * Returns the y coordinate of this point.
     * @return The vertical coordinate.
     */
    public double getY() {
        return this.y;
    }
    
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
        MapEventTrigger.inform(this);
    }
    
    /**
     * Returns a default VectorSymbol.
     * @return VectorSymbol.
     */
    public VectorSymbol getVectorSymbol() {
        return new VectorSymbol();
    }
        
    public void drawNormalState(RenderParams rp) {
        pointSymbol.drawPointSymbol(rp, isSelected(), x, y);
    }
    
    public void drawSelectedState(RenderParams rp) {
    }
    
    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist,
            double scale) {
        return pointSymbol.isPointOnSymbol(point, tolDist, scale, x, y);
    }
    
    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {
        GeoPath geoPath = pointSymbol.getPointSymbol(scale, x, y);
        return geoPath.isIntersectedByRectangle(rect, scale);
    }
    
    public java.awt.geom.Rectangle2D getBounds2D(double scale) {
        return new java.awt.geom.Rectangle2D.Double (x, y, 0, 0);
    }
    
    @Override
    public void move(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        MapEventTrigger.inform(this);
    }
    
    public void scale(double scale) {
        this.x *= scale;
        this.y *= scale;
        MapEventTrigger.inform(this);
    }
    
    public void scale(double hScale, double vScale) {
        this.x *= hScale;
        this.y *= vScale;
        MapEventTrigger.inform(this);
    }
    
    public void transform(AffineTransform affineTransform) {
        double[] pt = new double[] {this.x, this.y};
        affineTransform.transform(pt, 0, pt, 0, 1);
        this.x = pt[0];
        this.y = pt[1];
        MapEventTrigger.inform(this);
    }
    
    public PointSymbol getPointSymbol() {
        return pointSymbol;
    }

    public void setPointSymbol(PointSymbol pointSymbol) {
        this.pointSymbol = pointSymbol;
        MapEventTrigger.inform(this);
    }
    
    public final boolean isPointClose (GeoPoint geoPoint, double tolerance) {
        return (Math.abs(this.x - geoPoint.x) < tolerance
                && Math.abs(this.y - geoPoint.y) < tolerance);
    }
    
    public final boolean isPointClose (GeoPoint geoPoint) {
        return this.isPointClose(geoPoint, 1e-10);
    }
    
    public String toString(){
       return super.toString() + "\nCoordinates\t: " + this.x + " / " + this.y + "\n";
    }
}
