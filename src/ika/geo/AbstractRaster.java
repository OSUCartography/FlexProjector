/*
 * AbstractRaster.java
 *
 * Created on June 5, 2007, 9:19 AM
 *
 */
package ika.geo;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class AbstractRaster extends GeoObject {

    /**
     * Size of a pixel.
     */
    protected double cellSize;
    /**
     * Vertical coordinate of top left corner of this image.
     */
    protected double north;
    /**
     * Horizontal coordinate of top left corner of this image.
     */
    protected double west;
    
    public AbstractRaster() {
        resetGeoreference();
    }
    
    /**
     * Returns true if the grid has non-zero dimensions and position.
     * @return 
     */
    public boolean isWellFormed() {
        return getCols() > 0
                && getRows() > 0
                && getCellSize() > 0
                && !Double.isNaN(getWest())
                && !Double.isNaN(getNorth());
    }

    public void resetGeoreference() {
        this.west = 0;
        this.north = getRows();
        this.cellSize = 1;
        MapEventTrigger.inform(this);
    }

    public double getCellSize() {
        return cellSize;
    }

    public void setCellSize(double cellSize) {
        if (cellSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.cellSize = cellSize;
        MapEventTrigger.inform(this);
    }

    /**
     * Returns vertical coordinate of top left corner.
     */
    public double getNorth() {
        return north;
    }

    /**
     * Set vertical coordinate of top left corner.
     */
    public void setNorth(double north) {
        this.north = north;
        MapEventTrigger.inform(this);
    }

    /**
     * Returns horizontal coordinate of top left corner.
     */
    public double getWest() {
        return west;
    }

    /**
     * Set horizontal coordinate of top left corner.
     */
    public void setWest(double west) {
        this.west = west;
        MapEventTrigger.inform(this);
    }

    public abstract double getSouth();

    public abstract double getEast();

    public abstract int getCols();

    public abstract int getRows();
}
