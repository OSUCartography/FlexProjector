/*
 * ScaleLabel.java
 *
 * Created on February 27, 2007, 11:40 AM
 *
 */

package ika.gui;

import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JLabel;

/**
 * A text label that displays the current scale of a MapComponent
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ScaleLabel extends JLabel implements ScaleChangeHandler {
    
    /**
     * A formatting string used to format the scale before displaying.
     */
    private String scaleFormat = "#,###";
    
    /**
     * A prefix that preceds the scale number.
     */
    private String prefix = "1:";
    
    /** Creates a new instance of ScaleLabel */
    public ScaleLabel() {
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        this.setFont(font);
    }
    
    /**
     * This method is required by the ScaleChangeHandler interface. It is 
     * called whenever the scale of the map changes.
     * @param mapComponent The MapComponent that just changed its scale.
     * @param currentMapScaleFactor The scale factor to transform from screen 
     * pixels to coordinates as currently displayed.
     * @param currentMapScaleNumber The scale to transform from ground coordinates
     * to coordinates as currently displayed.
     */
    public void scaleChanged(MapComponent mapComponent,
            double currentMapScaleFactor, double currentMapScaleNumber) {
        
        String str;
        if (mapComponent == null || currentMapScaleNumber <= 0.) {
            str = "";
        } else {
            DecimalFormat formatter = new DecimalFormat(this.scaleFormat);
            str = this.prefix + formatter.format(currentMapScaleNumber);
        }
        this.setText(str);
    }
    
    /**
     * Register this ScaleLabel with the passed MapComponent, so that 
     * scaleChanged() is called whenever the map changes its scale.
     */
    public void registerWithMapComponent(MapComponent mapComponent) {
        mapComponent.addScaleChangeHandler(this);
    }
    
    public String getScaleFormat() {
        return this.scaleFormat;
    }

    public void setScaleFormat(String scaleFormat) {
        this.scaleFormat = scaleFormat;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
