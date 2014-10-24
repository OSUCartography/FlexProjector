/*
 * PageSize.java
 *
 * Created on April 20, 2007, 9:10 PM
 *
 */
package ika.gui;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * PageFormat specifies the map scale, the map size and the map position.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class PageFormat implements Cloneable, Serializable {

    /**
     * A factor to convert from millimeter to pixels. Assumes 72 pixels per
     * inch.
     */
    public static final double MM2PX = 72. / 2.54 / 10.;
    /**
     * Position of left border of page in world coordinates.
     */
    private double pageLeft = 0;
    /**
     * Position of lower border of page in world coordinates.
     */
    private double pageBottom = 0;
    /**
     * Width of page in pixels or millimeters.
     */
    private double pageWidth = PageFormatPanel.A4WIDTH * MM2PX;
    /**
     * Height of page in pixels or millimeters.
     */
    private double pageHeight = PageFormatPanel.A4HEIGHT * MM2PX;
    /**
     * Scale number of the page, which scales world coordinates to page
     * coordinates in world units (not pixels or mm).
     */
    private double pageScale = 100000.;
    /**
     * A vector of PageFormatChangeListener that are informed whenever this
     * PageFormat changes.
     */
    transient private Vector eventListeners = new Vector(1);
    /**
     * If true, the page width and height are in pixels, otherwise in
     * millimeter.
     */
    private boolean unitPixels = true;
    /**
     * If true, the page format is automatically adjusted to include the
     * complete map. This is only a flag that indicates that an external object
     * has to update.
     */
    private boolean automatic = true;
    /**
     * If true, the outline of the page should be shown in the map.
     */
    private boolean visible = true;

    /**
     * Creates a new instance of PageSize
     */
    public PageFormat() {
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Custom deserialization to initialize the vector of
     * PageFormatChangeListeners.
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.eventListeners = new Vector(1);
    }

    /**
     * Register a PageFormatChangeListener.
     */
    public final synchronized void addPageFormatChangeListener(
            PageFormatChangeListener listener) {
        if (listener != null && !this.eventListeners.contains(listener)) {
            this.eventListeners.add(listener);
        }
    }

    /**
     * Unregister a PageFormatChangeListener.
     */
    public final synchronized void removePageFormatChangeListener(
            PageFormatChangeListener listener) {
        this.eventListeners.remove(listener);
    }

    /**
     * Inform each registered MapEventListener about a change.
     */
    protected final synchronized void informListeners() {
        // Inform listeners in inverse order. This allows listeners to remove
        // themselves in the called method.
        for (int i = eventListeners.size() - 1; i >= 0; i--) {
            PageFormatChangeListener listener =
                    (PageFormatChangeListener) eventListeners.get(i);
            listener.pageFormatChanged(this);
        }
    }

    /**
     * Returns the left border of the page in world coordinates.
     */
    public double getPageLeft() {
        return pageLeft;
    }

    /**
     * Sets the left border of the page in world coordinates.
     */
    public void setPageLeft(double pageLeft) {
        this.pageLeft = pageLeft;
        this.informListeners();
    }

    /**
     * Returns the bottom border of the page in world coordinates.
     */
    public double getPageBottom() {
        return pageBottom;
    }

    /**
     * Sets the bottom border of the page in world coordinates.
     */
    public void setPageBottom(double pageBottom) {
        this.pageBottom = pageBottom;
        this.informListeners();
    }

    /**
     * Returns the top border of the page in world coordinates.
     */
    public double getPageTop() {
        return pageBottom + this.getPageHeightWorldCoordinates();
    }

    /**
     * Returns the right border of the page in world coordinates.
     */
    public double getPageRight() {
        return pageLeft + this.getPageWidthWorldCoordinates();
    }

    /**
     * Returns the page width in pixels or millimeter.
     */
    public double getPageWidth() {
        return pageWidth;
    }

    /**
     * Set the page width in pixels or millimeter.
     */
    public void setPageWidth(double pageWidth) {
        this.pageWidth = pageWidth;
        this.informListeners();
    }

    /**
     * Set the page width in world coordinates. Uses the current pageScale to
     * compute a height in pixels or mm.
     */
    public void setPageWidthWorldCoordinates(double pageWidth) {
        // convert from world coordinates to millimeter
        double w = pageWidth / this.pageScale * 1000.;
        if (this.unitPixels) {
            w *= MM2PX;
        }
        this.pageWidth = w;
        this.informListeners();
    }

    /**
     * Returns the page width in world coordinates.
     */
    public double getPageWidthWorldCoordinates() {
        // convert from millimeters to world coordinates.
        double w = this.pageScale * this.pageWidth / 1000.;
        if (this.unitPixels) {
            w /= MM2PX;
        }
        return w;
    }

    /**
     * Returns the page height in pixels or millimeter.
     */
    public double getPageHeight() {
        return pageHeight;
    }

    /**
     * Set the page height in pixels or millimeter.
     */
    public void setPageHeight(double pageHeight) {
        this.pageHeight = pageHeight;
        this.informListeners();
    }

    /**
     * Set the page height in world coordinates. Uses the current pageScale to
     * compute a height in pixels or mm.
     */
    public void setPageHeightWorldCoordinates(double pageHeight) {
        // convert from world coordinates to millimeter
        double h = pageHeight / this.pageScale * 1000.;
        if (this.unitPixels) {
            h *= MM2PX;
        }
        this.pageHeight = h;
        this.informListeners();
    }

    /**
     * Returns the page height in world coordinates.
     */
    public double getPageHeightWorldCoordinates() {
        // convert from millimeters to world coordinates.
        double h = this.pageScale * this.pageHeight / 1000.;
        if (this.unitPixels) {
            h /= MM2PX;
        }
        return h;
    }

    /**
     * Returns the page scale. If the user enters "1:100,000", the page scale is
     * 100,000.
     */
    public double getPageScale() {
        return pageScale;
    }

    /**
     * Sets the page scale. If the user enters "1:100,000", the page scale is
     * 100,000
     */
    public void setPageScale(double pageScale) {
        this.pageScale = pageScale;
        this.informListeners();
    }

    /**
     * Returns the extension of the page in world coordinates.
     */
    public Rectangle2D getPageSizeWorldCoordinates() {

        final double w = getPageWidthWorldCoordinates();
        final double h = getPageHeightWorldCoordinates();
        return new Rectangle2D.Double(pageLeft, pageBottom, w, h);
    }

    /**
     * Returns true if the page size is defined in pixels, false if it is
     * defined in millimeter.
     */
    public boolean isUnitPixels() {
        return unitPixels;
    }

    /**
     * Returns true if the page size is defined in millimeter, false if it is
     * defined in pixels.
     */
    public boolean isUnitMillimeter() {
        return !unitPixels;
    }

    /**
     * Set the unit of page dimension to pixels or millimeter.
     *
     * @param unitPixels If true, unit is pixels, if false it is in millimeter.
     */
    public void setUnitPixels(boolean unitPixels) {
        if (this.unitPixels == unitPixels) {
            return;
        }
        if (unitPixels == true) {
            this.pageWidth *= MM2PX;
            this.pageHeight *= MM2PX;
        } else {
            this.pageWidth /= MM2PX;
            this.pageHeight /= MM2PX;
        }
        this.unitPixels = unitPixels;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
        this.informListeners();
    }

    /**
     * Adjust the position and dimension of the page to include the passed
     * rectangle. Uses the current pageScale to compute a height and width in
     * pixels or mm.
     *
     * @param bounds
     */
    public void setPageWorldCoordinates(Rectangle2D bounds) {
        if (bounds == null) {
            return;
        }

        this.pageLeft = bounds.getMinX();
        this.pageBottom = bounds.getMinY();
        this.setPageHeightWorldCoordinates(bounds.getHeight());
        this.setPageWidthWorldCoordinates(bounds.getWidth());
    }

    /**
     * Adjusts the size of the page to cover the passed bounding box in world
     * coordinates. Adjust the pageScale such that the page has the passed
     * height and width in pixels.
     *
     * @param boundsWCS The bounding box in world coordinates.
     * @param width Width of the page in pixels.
     * @param height Height of the page in pixels.
     */
    public void adjustToRectangle(Rectangle2D boundsWC,
            double width, double height) {

        if (boundsWC == null || width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        this.unitPixels = true;
        double vScale = boundsWC.getHeight() / (height / 1000 / MM2PX);
        double hScale = boundsWC.getWidth() / (width / 1000 / MM2PX);
        if (vScale > hScale) {
            this.pageScale = vScale;
            this.pageLeft = boundsWC.getMinX()
                    - (width / 1000 / MM2PX * vScale - boundsWC.getWidth()) / 2;
            this.pageBottom = boundsWC.getMinY();
        } else {
            this.pageScale = hScale;
            this.pageLeft = boundsWC.getMinX();
            this.pageBottom = boundsWC.getMinY()
                    - (height / 1000 / MM2PX * hScale - boundsWC.getHeight()) / 2;
        }
        this.pageWidth = width;
        this.pageHeight = height;

        this.informListeners();

    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.informListeners();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PageFormat");
        sb.append(" Left: ").append(this.pageLeft);
        sb.append(" Bottom: ").append(this.pageBottom);
        sb.append(" Scale: ").append(this.pageScale);
        sb.append(" Width: ").append(this.pageWidth);
        sb.append(" Height: ").append(this.pageHeight);
        sb.append(" Automatic: ").append(this.automatic);
        sb.append(" Show: ").append(this.visible);
        return sb.toString();
    }
}
