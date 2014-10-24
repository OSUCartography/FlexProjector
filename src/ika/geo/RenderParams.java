/*
 * RenderParams.java
 *
 * Created on November 18, 2006, 5:24 PM
 *
 */

package ika.geo;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * RenderParams encapsulates parameters needed for drawing GeoObjects.
 * It is used by GeoObject.draw(RenderParams rp);
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class RenderParams {
    
    /**
     * The Graphic2D to draw to.
     */
    public final Graphics2D g2d;
    
    /**
     * The scale between the coordinate system of the GeoObjects and the coordinate 
     * system of the Graphic2D rendering destination.
     */
    public final double scale;
    
    /**
     * The left limit of the currently visible area that must be drawn.
     */
    public final double visLeft;
    
    /**
     * The top limit of the currently visible area that must be drawn.
     */
    public final double visTop;
        
    /**
     * The width of the currently visible area that must be drawn.
     */
    public final double visWidth;
    
    /**
     * The height of the currently visible area that must be drawn.
     */
    public final double visHeight;

    /**
     * True if the selection state of GeoObjects must be drawn.
     */
    public final boolean drawSelectionState;

    
    /**
     * Apply this transformation to all selected objects before drawing them.
     * This allows interactive tools to dynamically drag, scale and rotate
     * objects without changing their geometry. It may be null.
     */
    public AffineTransform selectedTransform;
    
    
    /**
     * Creates a new instance of RenderParams.
     * @param visLeft The left limit of the currently visible area that must be drawn.
     * @param visBottom The bottom limit of the currently visible area that must be drawn.
     * @param visWidth The width of the currently visible area that must be drawn.
     * @param visHeight The height of the currently visible area that must be drawn.
     * @param g2d The destination to draw to.
     * @param scale The scale factor transforming from this GeoObject's 
     * coordinate system to the coordinate system of the Graphics2D. scale can 
     * be used for scale-invariant drawing, e.g. a line width can be scaled so 
     * that its width is the same at any scale.
     * @param drawSelectionState Flag that indicates whether the selection state
     * of this GeoObject should be visualized, e.g. a selected path can be 
     * drawn with a special highlight color.
     * @param selectedTransform Apply this transformation if the object is 
     * selected before drawing it. Can be null if no transformation should be 
     * applied.
     */
    public RenderParams(Graphics2D g2d, 
            double scale, 
            double visLeft,
            double visBottom,
            double visWidth,
            double visHeight,
            boolean drawSelectionState,
            AffineTransform selectedTransform) {
        this.g2d = g2d;
        this.scale = scale; 
        this.visLeft = visLeft;
        this.visTop = visBottom + visHeight;
        this.visWidth = visWidth;
        this.visHeight = visHeight;
        this.drawSelectionState = drawSelectionState;
        this.selectedTransform = selectedTransform;
    }

    /**
     * Converts horizontal coordinate in world coordinate system to the coordinate
     * system of the map component.
     * @param x
     * @return 
     */
    public final double tX(double x) {
        return (x - visLeft) * scale;
    }
    
    /**
     * Converts vertical coordinate in world coordinate system to the coordinate
     * system of the map component.
     * @param y
     * @return 
     */
    public final double tY(double y) {
        return (visTop - y) * scale;
    }
}