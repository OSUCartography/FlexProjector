/*
 * FontSymbol.java
 *
 * Created on August 10, 2005, 11:48 AM
 *
 */

package ika.geo;

import ika.utils.ColorUtils;
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * Font symbol. Default is sans serif with 12 points, scale invariant, 
 * horizontally and vertically centered, black;
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class FontSymbol extends Symbol implements Cloneable{
    
    private enum HAlign { CENTER, LEFT, RIGHT };
    
    private HAlign hAlign = HAlign.CENTER;
    
    private Font font = new Font("SansSerif", Font.PLAIN, 12);
    
    private boolean scaleInvariant = true; // FIXME ignored for rendering
       
    private boolean centerVer = true;
    
    private Color color = Color.BLACK;
    
    /** Creates a new instance of FontSymbol */
    public FontSymbol() {
    }
    
    @Override
    public Object clone() {
        try {
            FontSymbol fontSymbol = (FontSymbol)super.clone();
            
            // make deep copy of FontSymbol
            fontSymbol.font = this.font.deriveFont(this.font.getSize());
            
            return fontSymbol;
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }
    
    public void drawFontSymbol(RenderParams rp,
            boolean drawSelected,
            double x, double y,
            double dxPx, double dyPx,
            String text,
            double rotation) {
        
        if (text == null) {
            return;
        }
        
        // position of the first character
        double tx = rp.tX(x);
        double ty = rp.tY(y);
        
        final Graphics2D g2d;
        if (rotation == 0) {
            g2d = rp.g2d;
        } else {
            // make a copy, as the affine transformation changes for rotated text
            g2d = (Graphics2D)(rp.g2d.create()); 
            g2d.translate(tx, ty);
            g2d.rotate(Math.toRadians(rotation));
            g2d.translate(-tx, -ty);
        }
        
        // add the scale-independent offset (after rotation)
        tx += dxPx;
        ty -= dyPx;
        
        // horizontal alignment
        Rectangle2D visualTextBounds = null;
        switch (hAlign) {
            case CENTER:
                visualTextBounds = visualTextBoundsPx(text);
                tx -= visualTextBounds.getWidth() / 2;
                break;
            case RIGHT:
                visualTextBounds = visualTextBoundsPx(text);
                tx -= visualTextBounds.getWidth();
                break;
        }
        
        // vertical alignment
        if (centerVer) {
            if (visualTextBounds == null) {
                visualTextBounds = visualTextBoundsPx(text);
            }
            ty += visualTextBounds.getHeight() / 2;
        }
        
        g2d.setColor(drawSelected ? ColorUtils.getSelectionColor() : color);
        g2d.setFont(font);
        g2d.drawString(text, (float)tx, (float)ty);
        
        // if a copy of Graphics2D was created, release it.
        if (g2d != rp.g2d) {
            g2d.dispose();
        }
    }
   
    private Rectangle2D visualTextBoundsPx(String str) {
        FontRenderContext frc = new FontRenderContext(null, true, false);
        GlyphVector gv = font.createGlyphVector(frc, str);
        return gv.getVisualBounds();
    }
    
    public Rectangle2D getBounds2D(String str, double x, double y, 
            double dxPx, double dyPx, double scale) {
        
        if (str == null) {
            return null;
        }
        
        final Rectangle2D visualBoundsPx  = visualTextBoundsPx(str);
        double tx = x + dxPx / scale;
        double ty = y + dyPx / scale;
        
        // horizontal alignment
        switch (hAlign) {
            case CENTER:
                tx -= visualBoundsPx.getWidth() / 2 / scale;
                break;
            case RIGHT:
                tx -= visualBoundsPx.getWidth() / scale;
                break;
        }
        
        // vertical alignment
        if (centerVer) {
            ty -= visualBoundsPx.getHeight() / 2 / scale;
        }
        
        double w = visualBoundsPx.getWidth() / scale;
        double h = visualBoundsPx.getHeight() / scale;
        
        // FIXME: rotation
        
        return new Rectangle2D.Double(tx, ty, w, h);
    }
    
    public Font getFont() {
        return font;
    }
    
    public void setFont(Font font) {
        this.font = font;
    }
    
    public boolean isScaleInvariant() {
        return scaleInvariant;
    }
    
    public void setScaleInvariant(boolean scaleInvariant) {
        this.scaleInvariant = scaleInvariant;
    }
    
    public boolean isCenterHor() {
        return this.hAlign == HAlign.CENTER;
    }
    
    public void setCenterHor(boolean centerHor) {
        this.hAlign = centerHor ? HAlign.CENTER : HAlign.LEFT;
    }
    
    public void setAlignLeft() {
        this.hAlign = HAlign.LEFT;
    }
    
    public boolean isAlignLeft() {
        return this.hAlign == HAlign.LEFT;
    }
    
    public void setAlignRight() {
        this.hAlign = HAlign.RIGHT;
    }
    
    public boolean isAlignRight() {
        return this.hAlign == HAlign.RIGHT;
    }
    
    public boolean isCenterVer() {
        return centerVer;
    }
    
    public void setCenterVer(boolean centerVer) {
        this.centerVer = centerVer;
    }
    
    public int getSize() {
        return font.getSize();
    }

    public void setSize (int size) {
       this.font = font.deriveFont((float)size);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
