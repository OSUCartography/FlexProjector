/*
 * CursorUtils.java
 *
 * Created on April 8, 2005, 1:53 PM
 */

package ika.utils;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * CursorUtils - a utility class to set the shape of the cursor.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class CursorUtils {
    
    /**
     * Set the cursor for a JComponent.
     * @param cursorName The name of the cursor. See loadCustomCursor for valid names.
     * @param jComponent The JComponent for which the cursor will be set.
     */
    public static void setCursor(String cursorName, JComponent jComponent) {
        Cursor cursor = loadCustomCursor(cursorName);
        // only set cursor if it is different from the current one to avoid
        // flickering.
        if (!cursor.getName().equals(jComponent.getCursor().getName())) {
            jComponent.setCursor(cursor);
        }
    }
    
    /**
     * Loads a custom cursor from a graphics file and configures the cursor.<br>
     * This is not how it should be done in a clean and portable program, but it works.
     * @param cursorName The name of the cursor. Have a look at the code for valid names.
     * @return The loaded cursor, or a default cursor if the specified cursor cannot be found.
     */
    public static Cursor loadCustomCursor(String cursorName) {
        
        /* If the system does not support custom cursor, getBestCursorSize
         *returns 0, 0. Return a default cursor in this case. */
        Dimension bestCursorSize = Toolkit.getDefaultToolkit().
                getBestCursorSize(32, 32);
        if (bestCursorSize.width == 0 || bestCursorSize.height == 0) {
            return new Cursor(Cursor.DEFAULT_CURSOR);
        }
        
        cursorName = cursorName.toLowerCase();
        
        String fileName = null;
        int backupCursorID = Cursor.DEFAULT_CURSOR;
        int x = 8;
        int y = 8;
        String accessibleCursorName = null;
        
        if ("arrow".equals(cursorName)) {
            return new Cursor(Cursor.DEFAULT_CURSOR);
        } else if ("crosshair".equals(cursorName)) {
            fileName = null;
            accessibleCursorName = null;
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("pan".equals(cursorName)) {
            fileName = "Hand32x32.gif";
            accessibleCursorName = "Pan";
            backupCursorID = Cursor.MOVE_CURSOR;
        } else if ("setpointarrow".equals(cursorName)) {
            x = 0;
            y = 0;
            fileName = "SetPoint32x32.gif";
            accessibleCursorName = "Set Point";
            backupCursorID = Cursor.DEFAULT_CURSOR;
        } else if ("selectionarrow".equals(cursorName)) {
            x = 0;
            y = 0;
            fileName = "SelectPoints32x32.gif";
            accessibleCursorName = "Select Points";
            backupCursorID = Cursor.DEFAULT_CURSOR;
        } else if ("movearrow".equals(cursorName)) {
            x = 0;
            y = 0;
            fileName = "MovePoint32x32.gif";
            accessibleCursorName = "Move Points";
            backupCursorID = Cursor.DEFAULT_CURSOR;
        } else if ("panclicked".equals(cursorName)) {
            fileName = "ClosedHand32x32.gif";
            accessibleCursorName = "Pan";
            backupCursorID = Cursor.MOVE_CURSOR;
        } else if ("polyselect".equals(cursorName)) {
            x = 0;
            y = 0;
            fileName = "PolySelect32x32.gif";
            accessibleCursorName = "Select by Polygon";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("pen".equals(cursorName)) {
            fileName = "Pen32x32.gif";
            x = 4;
            y = 1;
            accessibleCursorName = "Pen";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("zoomin".equals(cursorName)) {
            fileName = "ZoomIn32x32.gif";
            x = 6;
            y = 6;
            accessibleCursorName = "Zoom In";
            backupCursorID = Cursor.HAND_CURSOR;
        } else if ("zoomout".equals(cursorName)) {
            fileName = "ZoomOut32x32.gif";
            x = 6;
            y = 6;
            accessibleCursorName = "Zoom Out";
            backupCursorID = Cursor.HAND_CURSOR;
        } else if ("rotate".equals(cursorName)) {
            fileName = "Rotate32x32.png";
            x = 8;
            y = 8;
            accessibleCursorName = "Rotate";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("scaleh".equals(cursorName)) {
            fileName = "ScaleH32x32.png";
            x = 8;
            y = 8;
            accessibleCursorName = "Scale Horizontally";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("scalev".equals(cursorName)) {
            fileName = "ScaleV32x32.png";
            x = 8;
            y = 8;
            accessibleCursorName = "Scale Vertically";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("scaledes".equals(cursorName)) {
            fileName = "ScaleDes32x32.png";
            x = 8;
            y = 8;
            accessibleCursorName = "Scale Descending";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        } else if ("scaleasc".equals(cursorName)) {
            fileName = "ScaleAsc32x32.png";
            x = 8;
            y = 8;
            accessibleCursorName = "Scale Ascending";
            backupCursorID = Cursor.CROSSHAIR_CURSOR;
        }
        
        if (fileName == null) {
            return systemCursor(backupCursorID);
        }
        
        try {
            ImageIcon imageIcon = IconUtils.loadImageIcon(fileName, accessibleCursorName);
            Image image = imageIcon.getImage();
            // convert the Image to a BufferedImage with transparency
            BufferedImage bufferedImage = ImageUtils.makeBufferedImage(image,
                    BufferedImage.TYPE_INT_ARGB);
            return Toolkit.getDefaultToolkit().createCustomCursor(image,
                    (new Point(x, y)),accessibleCursorName);
        } catch (Exception exc) {
            return systemCursor(backupCursorID);
        }
    }
    
    private static Cursor systemCursor(int cursorID) {
        Cursor cursor = new Cursor(cursorID);
        if (cursor == null) {
            cursor = new Cursor(Cursor.DEFAULT_CURSOR);
        }
        return cursor;
    }
}