package ika.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.JComponent;

/*
 * CoordinatesInfoPanel.java
 *
 * Created on January 7, 2008, 4:46 PM
 *
 */
/**
 * A component that displays the coordinates in degrees, plain values and a scale.
 * @beaninfo
 *      attribute: isContainer false
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class CoordinatesInfoPanel extends JComponent {

    private double lon;
    private double lat;
    private double x;
    private double y;
    private double scale;
    private double areaScaleFactor;
    private double angularDist;
    private static final int FONT_SIZE = 10;
    private static final int ROW1_V_OFFSET = 11;
    private static final int ROW2_V_OFFSET = 23;
    private static final int LEFT_BORDER_OFFSET = 10;
    private static final int VALUE_OFFSET = 3;
    private static final int XY_H_OFFSET = 85 + LEFT_BORDER_OFFSET;
    private static final int SCALE_H_OFFSET = 190 + LEFT_BORDER_OFFSET;
    private static final int DIST_H_OFFSET = 280 + LEFT_BORDER_OFFSET;
    private static final int MIN_DEG_WIDTH = 30;
    private static final int MIN_XY_WIDTH = 65;
    private static final int PANEL_WIDTH = 400 + LEFT_BORDER_OFFSET;
    private static final int PANEL_HEIGHT = 26;
    private static final DecimalFormat degreeFormat = initFormat("##0.0");
    private static final DecimalFormat xyFormat = initFormat("#,##0.00");
    private static final DecimalFormat scaleFormat = initFormat("1:#,###");
    private static final DecimalFormat areaPercentageFormat = initFormat("##0.0%");

    private static DecimalFormat initFormat(String formatStr) {
        DecimalFormat format = new DecimalFormat(formatStr);
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setNaN("-");
        dfs.setInfinity("-");
        format.setDecimalFormatSymbols(dfs);
        return format;
    }

    /** Creates a new instance of CoordinatesInfoPanel */
    public CoordinatesInfoPanel() {
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setMaximumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setDoubleBuffered(true);
        this.setOpaque(false);
    }

    public void setCoordinates(double lon, double lat, double x, double y) {
        this.lon = lon;
        this.lat = lat;
        this.x = x;
        this.y = y;
        this.repaint();
    }

    void setDistortion(double areaScaleFactor, double angularDist) {
        this.areaScaleFactor = areaScaleFactor;
        this.angularDist = angularDist;
        this.repaint();
    }

    public void setScale(double scale) {
        this.scale = scale;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create(); //copy g. Recomended by Sun tutorial.
        if (isOpaque()) {
            g2d.setPaint(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        Font font = new Font("SansSerif", Font.PLAIN, FONT_SIZE);
        g2d.setFont(font);

        // Lon: and Lat:
        int h = LEFT_BORDER_OFFSET;
        h = drawTextPair("Lon:", "Lat:", h, g2d, font, true, 0);

        // lon and lat coordinates
        h += VALUE_OFFSET;
        String lonStr = degreeFormat.format(Math.abs(lon));
        String latStr = degreeFormat.format(Math.abs(lat));
        h = drawTextPair(lonStr, latStr, h, g2d, font, true, MIN_DEG_WIDTH);

        // degree sign and E/W, S/N
        if (Double.isInfinite(lon) || Double.isNaN(lon)) {
            lonStr = "";
        } else {
            lonStr = "\u00B0" + (lon < 0 ? "W" : "E");
        }
        if (Double.isInfinite(lat) || Double.isNaN(lat)) {
            latStr = "";
        } else {
            latStr = "\u00B0" + (lat < 0 ? "S" : "N");
        }
        h = drawTextPair(lonStr, latStr, h, g2d, font, false, 0);

        // X: and Y:
        h = drawTextPair("X:", "Y:", XY_H_OFFSET, g2d, font, true, 0);

        // x and y coordinates
        String xStr = xyFormat.format(x);
        String yStr = xyFormat.format(y);
        h = drawTextPair(xStr, yStr, h, g2d, font, true, MIN_XY_WIDTH);

        // scale
        String scaleStr = scaleFormat.format(this.scale);
        h = drawTextPair("Scale", scaleStr, SCALE_H_OFFSET, g2d, font, false, 0);

        // distortion
        h = drawTextPair("Area Dist.:", "Angular Dist.:", DIST_H_OFFSET, g2d, font, true, 0);
        final String angularDistStr;
        final String arealDistStr;
        if (Double.isInfinite(angularDist) || Double.isNaN(angularDist)) {
            angularDistStr = "";
        } else {
            angularDistStr = degreeFormat.format(angularDist) + "\u00B0";
        }
        if (Double.isInfinite(areaScaleFactor) || Double.isNaN(areaScaleFactor)) {
            arealDistStr = "";
        } else {
            arealDistStr = areaPercentageFormat.format(areaScaleFactor);
        }

        h = drawTextPair(arealDistStr, angularDistStr, h + VALUE_OFFSET, g2d, font, false, 0);

        g2d.dispose(); //release the copy's resources. Recomended by Sun tutorial.

    }

    private int drawTextPair(String str1, String str2, int x,
            Graphics2D g2d, Font font, boolean righAdjusted, int minWidth) {

        int w1 = this.getStringWidth(str1, g2d, font);
        int w2 = this.getStringWidth(str2, g2d, font);
        int w = Math.max(Math.max(w1, w2), minWidth);
        g2d.drawString(str1, righAdjusted ? x + w - w1 : x, ROW1_V_OFFSET);
        g2d.drawString(str2, righAdjusted ? x + w - w2 : x, ROW2_V_OFFSET);

        return x + w;

    }

    private int getStringWidth(String str, Graphics2D g2d, Font font) {

        final FontRenderContext frc = g2d.getFontRenderContext();
        final LineMetrics lineMetrics = font.getLineMetrics(str, frc);
        final GlyphVector gv = font.createGlyphVector(frc, str);
        final Rectangle2D visualBounds = gv.getVisualBounds();
        return (int) Math.ceil(visualBounds.getWidth());

    }
    /*
    public static void main (String[] args) {
    javax.swing.JFrame frame = new javax.swing.JFrame("Test");
    CoordinatesInfoPanel infoPanel = new CoordinatesInfoPanel();
    frame.add(infoPanel);
    frame.pack();
    frame.setVisible(true);
    }
     */
}
