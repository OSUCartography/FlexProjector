/*
 * VectorSymbol.java
 *
 * Created on 5. Februar 2005, 15:23
 */

package ika.geo;
import java.awt.*;
import java.security.InvalidParameterException;

/**
 * VectorSymbol - graphic attributes for a vector path. Used to draw the vector.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class VectorSymbol extends Symbol implements java.io.Serializable, Cloneable {
    
    private static final long serialVersionUID = 1756423008209209816L;
    
    /**
     * The color used to draw the interior of a vector path.
     */
    protected Color fillColor = Color.white;
    
    /**
     * The color used to draw the outer border of a vector path.
     */
    protected Color strokeColor = Color.black;
    
    /**
     * The width used to draw the outer border of a vector path.
     */
    protected float strokeWidth = 1;
    
    /**
     * True: the interior of a vector is filled with the fillColor. False: no filling
     * is drawn.
     */
    protected boolean filled = false;
    
    /**
     * True: the outer border of a vector is drawn with the storkeColor. False: the
     * outer line is not drawn.
     */
    protected boolean stroked = true;
    
    /**
     * The length of a single dash. Set to 0 or smaller if line should not be dashed.
     * Unit: pixels.
     */
    protected float dashLength = 0;
    
    /**
     * If scaleInvariant is true, the symbol is independent of the
     * current scale of the map.
     */
    protected boolean scaleInvariant = false;
    
    /**
     * The end decoration of lines. Possible values: BasicStroke.CAP_BUTT,
     * BasicStroke.CAP_ROUND, and BasicStroke.CAP_SQUARE
     */
    private int cap = BasicStroke.CAP_SQUARE;
    
    /** Creates a new instance of VectorSymbol */
    public VectorSymbol() {
    }
    
    /**
     * Creates a new instance of VectorSymbol
     * @param fillColor The color used to fill the interior a vector. If null, 
     * the symbol is not filled.
     * @param strokeColor The color used to stroke the outter border of a vector.
     * If null, the symbol is not stroked.
     * @param strokeWidth The width of the stroke that is used to draw the 
     * border of a vector path. The smallest possible value is 0, which is the
     * thinnest line that can be drawn. If strokeWidth is 0, the new 
     * VectorSymbol is scale invariant.
     */
    public VectorSymbol(Color fillColor, Color strokeColor, float strokeWidth) {
        this.fillColor = fillColor == null ? Color.WHITE : fillColor;
        this.filled = fillColor != null;
        
        this.strokeColor = strokeColor == null ? Color.BLACK : strokeColor ;
        this.stroked = strokeColor != null;
        
        if (strokeWidth < 0.f) {
            strokeWidth = 0.f;
        }
        this.strokeWidth = strokeWidth;
        this.setScaleInvariant(strokeWidth == 0.f);
    }
    
    @Override
    public VectorSymbol clone() {
        VectorSymbol copy = new VectorSymbol();
        this.copyTo(copy);
        return copy;
    }
    
    public void copyTo(VectorSymbol dest) {
        dest.fillColor = this.fillColor;
        dest.strokeColor = this.strokeColor;
        dest.strokeWidth = this.strokeWidth;
        dest.filled = this.filled;
        dest.stroked = this.stroked;
        dest.dashLength = this.dashLength;
        dest.scaleInvariant = this.scaleInvariant;
        dest.cap = this.cap;
    }
    
    public boolean equals(VectorSymbol vectorSymbol) {
        
        if (this == vectorSymbol) {
            return true;
        }
        
        return vectorSymbol.fillColor.equals(this.fillColor)
        && vectorSymbol.strokeColor.equals(this.strokeColor)
        && vectorSymbol.strokeWidth == this.strokeWidth
                && vectorSymbol.filled == this.filled
                && vectorSymbol.stroked == this.stroked
                && vectorSymbol.dashLength == this.dashLength
                && vectorSymbol.scaleInvariant == this.scaleInvariant
                && vectorSymbol.cap == this.cap;
    }
    
    /**
     *
     */
    public Color getFillColor() {
        return this.fillColor;
    }
    
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }
    
    /**
     * alpha 0: completely transparent; 255: completely opaque.
     */
    public int getFillTransparency() {
        if (this.fillColor == null) {
            return 255;
        }
        return this.fillColor.getAlpha();
    }
    
    /**
     * Important: This method will generate a new internal Color object. Any 
     * reference obtained by getFillColor will no longer be valid!
     * If fillColor is null, the passed alpha value will not be retained.
     * @param alpha 0: completely transparent; 255: completely opaque.
    */
    public void setFillTransparancy(int alpha) {
        this.fillColor = 
                ika.utils.ColorUtils.transparentColor(this.fillColor, alpha);
    }
    
    /**
     * Returns true if the fill color is partially transparent.
     */
    public boolean isFillTransparent() {
        if (this.fillColor == null) {
            return false;
        }
        return this.fillColor.getAlpha() < 255;
    }
    
    public Color getStrokeColor() {
        return this.strokeColor;
    }
    
    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }
    
    public float getStrokeWidth() {
        return strokeWidth;
    }
    
    public final float getScaledStrokeWidth(double scale) {
        float w = scaleInvariant ? strokeWidth : (float)(strokeWidth * scale);
        return w < 0f ? 0f : w;
    }
    
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    public boolean isFilled() {
        return this.filled;
    }
    
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
    
    public boolean isStroked() {
        return stroked;
    }
    
    public void setStroked(boolean stroked) {
        this.stroked = stroked;
    }
    
    public boolean isDashed() {
        return this.getDashLength() > 0;
    }
    
    public float getDashLength() {
        return dashLength;
    }
    
    public final float getScaledDashLength(double scale) {
        return scaleInvariant ? (float)(dashLength * scale) : dashLength;
    }
    
    public void setDashLength(float dashLength) {
        this.dashLength = dashLength;
    }
    
    public boolean isScaleInvariant() {
        return scaleInvariant;
    }
    
    public void setScaleInvariant(boolean scaleInvariant) {
        this.scaleInvariant = scaleInvariant;
    }
    
    public int getCap() {
        return cap;
    }
    
    public void setCap(int cap) {
        if (cap == BasicStroke.CAP_BUTT
                || cap == BasicStroke.CAP_ROUND
                || cap == BasicStroke.CAP_SQUARE) {
            this.cap = cap;
        }
        else {
            throw new InvalidParameterException("invalid line cap");
        }
    }
    
    /**
     * Returns a Stroke for drawing in a Graphics2D using g2d.setStroke().
     * @param scale The scale of the map. Used for computing the stroke width and dash
     * length when this symbol is scale invariant.
     * @return A Stroke that can be used to draw into Graphics2d.
     */
    public final Stroke getStroke(double scale) {
        final float scaledStrokeWidth = getScaledStrokeWidth(scale);
        BasicStroke stroke;
        if (dashLength <= 0f) {
            stroke = new BasicStroke(scaledStrokeWidth,
                    this.cap, 
                    BasicStroke.JOIN_MITER);
        } else {
            float scaledDashLength = getScaledDashLength(scale);
            stroke = new BasicStroke(scaledStrokeWidth,
                    this.cap,
                    BasicStroke.JOIN_BEVEL,
                    0,
                    new float[] {scaledDashLength, scaledDashLength},
                    0);
        }
        
        return stroke;
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        String nl = System.getProperty("line.separator");
        str.append("Fill: ").append(this.filled).append(" color: ").append(this.fillColor);
        str.append(nl);
        
        str.append("Stroke: ").append(this.stroked).append(" color: ").append(this.strokeColor);
        str.append(" width: ").append(this.strokeWidth);
        str.append(" dash length: ").append(this.dashLength);
        str.append(nl);
        
        str.append("Scale invariant: ").append(this.scaleInvariant);
        
        return str.toString();
    }
}
