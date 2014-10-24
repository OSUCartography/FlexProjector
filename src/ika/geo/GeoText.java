/*
 * GeoText.java
 *
 * Created on August 10, 2005, 11:51 AM
 *
 */

package ika.geo;

import java.awt.geom.AffineTransform;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GeoText extends GeoObject {
    
    /** Horizontal position of the text. */
    protected double x = 0;
    
    /** Vertical position of the text. */
    protected double y = 0;
    
    /** Horizontal offset of the text position. This distance scales with the map scale if 
     * this text is scale-invariant. */
    protected double dx = 0;
    
    /** Vertical offset of the text position. This distance scales with the map scale if 
     * this text is scale-invariant. */
    protected double dy = 0;
    
    /** FontSymbol for rendering this GeoText. */
    protected FontSymbol fontSymbol = new FontSymbol();
    
    /** The text content. */
    protected String text = "";
    
    /** Rotation of the text label around x / y in degrees counter-clock wise. */
    private double rotation = 0;
    
    /** Creates a new instance of GeoText */
    public GeoText() {
    }
    
    /** Creates a new instance of GeoText */
    public GeoText (String text, double x, double y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }
    
    /** Creates a new instance of GeoText */
    public GeoText (String text, double x, double y, double dx, double dy) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }
    
    @Override
    public GeoText clone() {
        try {
            GeoText geoText = (GeoText)super.clone();
            
            // make deep copy of FontSymbol
            geoText.fontSymbol = (FontSymbol)this.fontSymbol.clone();
            
            return geoText;
        } catch (Exception exc) {
            return null;
        }
    }
    
    public void drawNormalState(RenderParams rp) {
        final boolean drawSelected = rp.drawSelectionState && isSelected();
        fontSymbol.drawFontSymbol(rp, drawSelected,
                x, y, dx, dy, text, rotation);
    }
    
    public void drawSelectedState(RenderParams rp) {
    }

    public java.awt.geom.Rectangle2D getBounds2D(double scale) {
        return this.fontSymbol.getBounds2D (this.text, x, y, dx, dy, scale);
    }

    public boolean isIntersectedByRectangle(java.awt.geom.Rectangle2D rect, double scale) {
        // Test if if the passed rectangle and the bounding box of this object
        // intersect.
        // Use GeometryUtils.rectanglesIntersect and not Rectangle2D.intersects!
        final java.awt.geom.Rectangle2D bounds = this.getBounds2D(scale);
        return ika.utils.GeometryUtils.rectanglesIntersect(rect, bounds);
    }

    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist, double scale) {
        java.awt.geom.Rectangle2D bounds = this.getBounds2D(scale);
        ika.utils.GeometryUtils.enlargeRectangle(bounds, tolDist);
        return bounds.contains(point);
    }

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
    
    public void scale (double hScale, double vScale) {
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
     
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
        MapEventTrigger.inform(this);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        MapEventTrigger.inform(this);
    }
    
    /** Returns the horizontal position of the origin of this GeoText, including
     * a possible offset depending on dx and the current map scale. */
    public double getVisualX (double scale) {
        return this.x + scale * this.dx;
    }
    
    /** Returns the vertical position of the origin of this GeoText, including
     * a possible offset depending on dy and the current map scale. */
    public double getVisualY (double scale) {
        return this.y + scale * this.dy;
    }
    
    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
        MapEventTrigger.inform(this);
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
        MapEventTrigger.inform(this);
    }
        
    public FontSymbol getFontSymbol() {
        return fontSymbol;
    }

    public void setFontSymbol(FontSymbol fontSymbol) {
        this.fontSymbol = fontSymbol;
        MapEventTrigger.inform(this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        MapEventTrigger.inform(this);
    }
    
    public boolean isScaleInvariant() {
        return this.fontSymbol.isScaleInvariant();
    }
    
    public void setScaleInvariant(boolean scaleInvariant) {
        this.fontSymbol.setScaleInvariant(scaleInvariant);
        MapEventTrigger.inform(this);
    }
    
    public boolean isCenterHor() {
        return this.fontSymbol.isCenterHor();
    }

    public void setCenterHor(boolean centerHor) {
        this.fontSymbol.setCenterHor(centerHor);
        MapEventTrigger.inform(this);
    }

    public void setAlignLeft() {
        this.fontSymbol.setAlignLeft();
    }
    
    public boolean isAlignLeft() {
        return fontSymbol.isAlignLeft();
    }
    
    public void setAlignRight() {
        this.fontSymbol.setAlignRight();
    }
    
    public boolean isAlignRight() {
        return this.fontSymbol.isAlignRight();
    }
    
    public boolean isCenterVer() {
        return this.fontSymbol.isCenterVer();
    }

    public void setCenterVer(boolean centerVer) {
        this.fontSymbol.setCenterVer(centerVer);
        MapEventTrigger.inform(this);
    }
    
    public int getSize() {
        return this.fontSymbol.getSize();
    }

    public void setSize (int size) {
        this.fontSymbol.setSize (size);
        MapEventTrigger.inform(this);
    }   

    /**
     * Returns the rotation of the text in degrees counter-clock wise.
     * @return The rotation.
     */
    public double getRotation() {
        return rotation;
    }
    
    /**
     * Set the rotation of the text in degrees counter-clock wise.
     * @param rotation The rotation.
     **/
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
}