/*
 * CenteredStringRenderer.java
 *
 * Created on August 15, 2006, 12:39 AM
 *
 */
package ika.utils;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * CenteredStringRenderer draws a string centered on a point.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class CenteredStringRenderer {

    public static final boolean NOFLIP = false;
    public static final boolean FLIPY = true;

    /**
     * Draws a string centered on its mid-point.
     * From http://forum.java.sun.com/thread.jspa?forumID=20&threadID=516778
     */
    public static void drawCentered(Graphics2D g2, String text,
            float centerX, float centerY, boolean flipY) {

        if (g2 == null || text == null || text.length() == 0) {
            return;
        }

        AffineTransform initialTrans = g2.getTransform();
        try {
            if (flipY == FLIPY) {
                g2.translate(0, centerY);
                g2.scale(1, -1);
                g2.translate(0, -centerY);
            }
            FontRenderContext frc = g2.getFontRenderContext();
            Font font = g2.getFont();
            Rectangle2D bounds = new TextLayout(text, font, frc).getBounds();
            float w = (float) bounds.getWidth();
            float h = (float) bounds.getHeight();
            float x = (float) (centerX - bounds.getX() - w / 2);
            float y = (float) (centerY - bounds.getY() - h / 2);
            g2.drawString(text, x, y);
        } finally {
            g2.setTransform(initialTrans);
        }
    }
}
