/*
 * ColorButton.java
 *
 * Created on May 14, 2005, 5:13 PM
 */

package ika.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * A button to select a color. The currently selected color is
 * displayed by the button.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ColorButton extends JToggleButton implements ActionListener {
    
    /**
     * The current color.
     */
    private Color color;
    
    /**
     * The horizontal width of the icon that displays the color.
     */
    private int iconWidth;
    
    /**
     * The vertical width of the icon that displays the color.
     */
    private int iconHeight;
    
    /**
     * The title of the color chooser dialog.
     */
    private String colorChooserTitle;
    
    //The dialog that the button brings up.
    JColorChooser colorChooser;
    JDialog dialog;
    
    /**
     * Creates a new instance of ColorButton. Default color is black, default
     * size of the icon is 16 x 16 pixels.
     * This button is registered with itself for receiving action performed calls.
     */
    public ColorButton() {
        this.color = new Color(0, 0, 0);
        this.iconHeight = 16;
        this.iconWidth = 16;
        this.colorChooserTitle = "Choose a Color";
        
        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
            
        // replace the ugly and useless preview panel by an empty JPanel
        // colorChooser.setPreviewPanel(new JPanel());

        this.updateIcon();
    }
    
    /**
     * Internal utility method that creates a new icon. Called after the user
     * selects a new color, or when the size of the icon changes.
     */
    private void updateIcon() {
        BufferedImage image = new BufferedImage(iconWidth, iconHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        
        g2d.setColor(this.color);
        // turn antialiasing off
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        // we want fast rendering
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        
        g2d.fillRect(0, 0, iconWidth, iconHeight);
        ImageIcon imageIcon = new ImageIcon(image);
        this.setIcon(imageIcon);
        g2d.dispose();
    }
    
    /**
     * Action listener for the OK button in the color chooser.
     */
    public void actionPerformed(ActionEvent e) {
        // ok button in color chooser was pressed.
        this.setColor(colorChooser.getColor());
        super.fireActionPerformed(e);
    }
    
    @Override
    protected void fireActionPerformed(ActionEvent event) {
        try {
            this.setSelected(true);
            
            // create the dialog if this has not been done yet.
            if (this.dialog == null) {
                // Get the top level ancestor as parent for the new dialog.
                // This ensures that on Mac OS X the global menu bar remains 
                // visible while the dialog is open.
                final Container parent = this.getTopLevelAncestor();
                dialog = JColorChooser.createDialog(parent,
                        colorChooserTitle,
                        true,  //modal
                        colorChooser,
                        this,  //OK button handler
                        null); //no CANCEL button handler
                dialog.pack();
                dialog.setResizable(false);
            }
            
            // show the dialog
            colorChooser.setColor(color);
            dialog.setVisible(true);
            
            /* A simpler option that is displaying the ugly preview panel:
            Color newColor = JColorChooser.showDialog(this.getTopLevelAncestor(),
                    colorChooserTitle, color);
            if (newColor != null){
                // only set color and fire an event when user did not cancel.
                this.setColor(newColor);
                super.fireActionPerformed(event);
            }*/
        } catch (Exception e){
        } finally {
            this.setSelected(false);
        }
    }
    
    /**
     * Returns the currently selected Color.
     * @return The current color.
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Changes the current color.
     * @param color The new color.
     */
    public void setColor(Color color) {
        if (color != null) {
            this.setEnabled(true);
            this.color = color;
            this.updateIcon();
        } else {
            this.setEnabled(false);
        }
    }
    
    /**
     * Returns the width of the icon used to display the color.
     * @return The width of the icon.
     */
    public int getIconWidth() {
        return iconWidth;
    }
    
    /**
     * Sets the width of the icon used to display the color.
     * @param iconWidth The new width of the icon.
     */
    public void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
        this.updateIcon();
    }
    
    /**
     * Returns the height of the icon used to display the color.
     * @return The height of the icon.
     */
    public int getIconHeight() {
        return iconHeight;
    }
    
    /**
     * Sets the height of the icon used to display the color.
     * @param iconHeight The new height of the icon.
     */
    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
        this.updateIcon();
    }
    
    /**
     * Returns the title of the color chooser dialog.
     * @return The title.
     */
    public String getColorChooserTitle() {
        return colorChooserTitle;
    }
    
    /**
     * Sets the title of the color chooser dialog
     * @param colorChooserTitle The new title.
     */
    public void setColorChooserTitle(String colorChooserTitle) {
        this.colorChooserTitle = colorChooserTitle;
    }
}
