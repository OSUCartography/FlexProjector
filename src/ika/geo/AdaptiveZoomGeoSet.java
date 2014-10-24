/*
 * AdaptiveZoomGeoSet.java
 *
 * Created on 30. Januar 2007, 12:10
 *
 */

package ika.geo;

import java.awt.GraphicsConfiguration;
import java.awt.geom.AffineTransform;

/**
 * A GeoSet that is only drawn within a certain map scale. The minimum and 
 * maximum scale are handled by this AdatpiveZoomGeoSet.
 *
 * @author Bernhard Jenny & Chris Lienert, Institute of Cartography, ETH Zurich
 */
public class AdaptiveZoomGeoSet extends GeoSet {
    
    private double minScale = 0;
    private double maxScale = Double.MAX_VALUE;
    
    /** Creates a new instance of AdaptiveZoomGeoSet */
    public AdaptiveZoomGeoSet() {
    }
    
    @Override
    public synchronized void drawNormalState(RenderParams rp) {
        
        if (!this.isVisible()) {
            return;
        }
        
        // determine the current map scale
        GraphicsConfiguration gc = rp.g2d.getDeviceConfiguration();
        AffineTransform trans = gc.getNormalizingTransform();
        final double dpiScale = trans.getScaleX();
        final double scale = (72 * dpiScale * 100 / 2.54) / rp.scale;
        if (scale >= minScale && scale < maxScale) {
            super.drawNormalState(rp);
        }
        
    }

    public double getMinScale() {
        return minScale;
    }

    public void setMinScale(double minScale) {
        this.minScale = minScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(double maxScale) {
        this.maxScale = maxScale;
    }
 
}
