/*
 * PenTool.java
 *
 * Created on April 21, 2005, 6:10 PM
 */

package ika.map.tools;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import ika.geo.*;
import ika.gui.MapComponent;

/**
 * A map tool to draw paths consisting of straight lines.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
abstract public class PolygonToolBase extends DoubleBufferedTool {
    
    /**
     * Location of the last mouse click.
     */
    private Point2D.Double lastClickLoc;
    
    /**
     * Current position of the mouse cursor.
     */
    private Point2D.Double currentMouseLoc;
    
    /**
     * The GeoPath that is currerntly drawn.
     */
    protected GeoPath geoPath;
       
    /**
     * Time of the last mouse click in milliseconds.
     */
    private long eventTimeMilliSec = 0;
    
    /**
     * Maximum time difference between two consecutive mouse clicks to form a double click.
     */
    private static final long DOUBLE_CLICK_TIME_DIF = 500;
    
    /**
     * Create a new instance.
     * @param mapComponent The MapComponent for which this MapTool provides its services.
     */
    public PolygonToolBase(MapComponent mapComponent) {
        super(mapComponent);
    }
    
    @Override
    public void deactivate() {
        finishPath();
        releaseBackground();
    }
    
    @Override
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        if (geoPath == null) {
            geoPath = new GeoPath();
            geoPath.setSelected(true);
            
            // deselect all currently selected objects in the destinationGeoSet
            if (destinationGeoSet != null) {
                destinationGeoSet.setSelected(false);
            }
            
            VectorSymbol symbol = new VectorSymbol();
            symbol.setFilled(false);
            symbol.setStroked(true);
            symbol.setStrokeWidth(1);
            symbol.setStrokeColor(Color.green);
            symbol.setScaleInvariant(true);
            geoPath.setVectorSymbol(symbol);
            
            captureBackground();
        }
        
        // add point to path
        if (evt.getClickCount() == 2) {
            
            // A double click generates two calls of mouseClicked. So we have
            // to remove the last added point.
            // Additionally make sure that the last click happened within a short
            // period of time. This way we are still safe, hould another virtual 
            // machine generate only one call to mouseClicked after a double click.
            if (evt.getWhen() - eventTimeMilliSec < DOUBLE_CLICK_TIME_DIF) {
                geoPath.removeLastPoint();
            }
            
            // close the path and pass it to the map
            geoPath.closePath();
            finishPath();
        } else {
            // add a straight line
            geoPath.moveOrLineTo(point.x, point.y);
        }
        
        mapComponent.repaint();
        
        // remember where and when the last click happened.
        lastClickLoc = (Point2D.Double)point.clone();
        eventTimeMilliSec = evt.getWhen();
    }
    
    @Override
    public void mouseExited(Point2D.Double point, MouseEvent evt) {
        // don't draw a straight line from the last point to the current
        // mouse position if the mouse is not inside the map.
        this.currentMouseLoc = null;
        mapComponent.repaint();
    }
   
    @Override
    public void mouseMoved(Point2D.Double point, MouseEvent evt) {        
        // remember the current mouse position and repaint if their already is at
        // least one point.
        if (lastClickLoc != null) {
            this.currentMouseLoc = (Point2D.Double)point.clone();
            mapComponent.repaint();
        }
    }
    
    @Override
    public void draw(RenderParams rp) {
        if (geoPath == null) {
            return;
        }
        
        this.mapComponent.shiftGraphics2DByBorderWidth(rp.g2d);
        //this.mapComponent.setupGraphics2DForDrawingInWorldCoordinates(rp.g2d); FIXME
        
        final float scale = (float)this.mapComponent.getScaleFactor();
        rp.g2d.setColor(Color.black);
        final double strokeWidth = 1. / mapComponent.getScaleFactor();
        BasicStroke stroke = new BasicStroke((float)strokeWidth);
        rp.g2d.setStroke(stroke);
        
        // draw the GeoPath
        geoPath.drawNormalState(rp);
        
        // draw a line from last clicked point to current mouse position
        if (lastClickLoc != null && currentMouseLoc != null) {
            Line2D.Double line = new Line2D.Double(lastClickLoc, currentMouseLoc);
            rp.g2d.draw(line);
        }
    }
    
    /**
     * Utility method that is called when the user finishes drawing.
     * The captured background is released, and
     * variables reset, so that the next path can be drawn.
     */
    protected void finishPath() {
        releaseBackground();
        
        // init variables so that next drawing can start.
        geoPath = null;
        lastClickLoc = null;
        currentMouseLoc = null;
    }
}
