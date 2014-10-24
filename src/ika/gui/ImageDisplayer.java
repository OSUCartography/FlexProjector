/*
 * ImageDisplayer.java
 *
 * Created on December 3, 2007, 2:41 PM
 *
 */

package ika.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ImageDisplayer {
    
    private javax.swing.JFrame frame;
    private javax.swing.JLabel label;
    
    /** Creates a new instance of ImageDisplayer */
    public ImageDisplayer(BufferedImage image) {
        this.displayImageInWindow(image);
    }
    
    public void displayImageInWindow(BufferedImage image) {
        // Use a JLabel in a JFrame to display the image
        frame = new javax.swing.JFrame();
        label = new javax.swing.JLabel(new javax.swing.ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
    public void updateImage(BufferedImage image) {
        int h = label.getIcon().getIconHeight();
        int w = label.getIcon().getIconWidth();
        label.setIcon(new javax.swing.ImageIcon(image));
        if (h != image.getHeight() || w != image.getWidth())
            frame.pack();
    }
    
    
}
