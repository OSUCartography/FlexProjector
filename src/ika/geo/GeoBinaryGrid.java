/*
 * GeoGrid.java
 *
 */
package ika.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/**
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoBinaryGrid extends AbstractRaster {

    public class BinaryImage {

        public BufferedImage image;
        public Graphics2D g2d;

        public BinaryImage(BufferedImage image, Graphics2D g2d) {
            this.image = image;
            this.g2d = g2d;
        }
    }
    /**
     * grid to accumulate rasterized GeoPaths
     */
    private BinaryImage grid;
    /**
     * temporarily used grid to rasterize a single GeoPath and to test
     * overlaps with the accumulated paths in grid
     */
    private BinaryImage rasterizerGrid;

    /** Creates a new instance of GeoBinaryGrid */
    public GeoBinaryGrid(int cols, int rows, double west, double north, double cellSize) {
        this.cellSize = cellSize;
        this.west = west;
        this.north = north;
        this.grid = initGrid(cols, rows);
    }

    private BinaryImage initGrid(int w, int h) {

        // create binary image with black as transparent background color
        // with a transparent color, paths rasterized to rasterizerGrid can 
        // be copied to grid to accumulate all paths without overwriting what
        // has been accumulated before.
        final byte ff = (byte) 0xff;
        final byte[] r = {0, ff};
        final byte[] g = {0, ff};
        final byte[] b = {0, ff};
        IndexColorModel cm = new IndexColorModel(1, 2, r, g, b, 0);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY, cm);

        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.setBackground(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // transform from Geo space to user space, last transformation first!
        /*
         * Transformation:
         * x_ = (x-west)*scale;
         * y_ = (north-y)*scale = (y-north)*(-scale);
         */
        final double scale = 1. / cellSize;
        g2d.scale(scale, -scale);
        g2d.translate(-west, -north);

        return new BinaryImage(bi, g2d);
    }

    public void rasterize(GeoPath geoPath) {
        rasterize(geoPath, this.grid.g2d);
    }

    private void rasterize(GeoPath geoPath, Graphics2D g2d) {

        VectorSymbol vectorSymbol = geoPath.getVectorSymbol();

        // render path
        GeneralPath path = new GeneralPath();
        path.append(geoPath.toPathIterator(null), false);
        if (vectorSymbol.isFilled()) {
            g2d.fill(path);
        }
        if (vectorSymbol.isStroked()) {
            g2d.setStroke(new BasicStroke(vectorSymbol.getStrokeWidth()));
            g2d.draw(path);
        }

    }

    public boolean isAddingCausingOverlay(GeoPath geoPath, boolean addIfNotOverlaying) {

        int w = this.grid.image.getWidth();
        int h = this.grid.image.getHeight();
        this.rasterizerGrid = initGrid(w, h);
        rasterize(geoPath, rasterizerGrid.g2d);

        Rectangle2D objBounds = geoPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        final double objWest = objBounds.getMinX();
        final double objEast = objBounds.getMaxX();
        final double objSouth = objBounds.getMinY();
        final double objNorth = objBounds.getMaxY();

        VectorSymbol vectorSymbol = geoPath.getVectorSymbol();
        final double lineWidth = vectorSymbol != null ? vectorSymbol.getStrokeWidth() * 2. : 0;

        int firstCol = (int) ((objWest - lineWidth - this.west) / cellSize);
        int firstRow = (int) ((objNorth - lineWidth - this.getNorth()) / cellSize);
        firstCol = Math.max(0, firstCol);
        firstRow = Math.max(0, firstRow);

        int lastCol = (int) Math.ceil((objEast + lineWidth - this.west) / cellSize);
        int lastRow = (int) Math.ceil((this.getNorth() - objSouth + lineWidth) / cellSize);
        lastCol = Math.min(grid.image.getWidth(), lastCol);
        lastRow = Math.min(grid.image.getHeight(), lastRow);

        Raster r1 = rasterizerGrid.image.getRaster();
        Raster r2 = grid.image.getRaster();

        for (int r = firstRow; r < lastRow; ++r) {
            for (int c = firstCol; c < lastCol; ++c) {
                if (r1.getSample(c, r, 0) == 1 && r2.getSample(c, r, 0) == 1) {
                    return true;
                }
            }
        }

        if (addIfNotOverlaying) {
            AffineTransform trans = this.grid.g2d.getTransform();
            this.grid.g2d.setTransform(new AffineTransform());
            this.grid.g2d.drawImage(rasterizerGrid.image, 0, 0, null);
            this.grid.g2d.setTransform(trans);
        }

        return false;

    }

    public final boolean contains(double x, double y) {
        int c = (int) Math.floor((x - west) / cellSize);
        int r = (int) Math.floor((north - y) / cellSize);
        int w = this.grid.image.getWidth();
        int h = this.grid.image.getHeight();
        return r >= 0 && c >= 0 && r < h && c < w && grid.image.getRaster().getSample(c, r, 0) == 1;
    }

    @Override
    public void drawNormalState(RenderParams rp) {
        GeoPath.newRect(getBounds2D(rp.scale)).drawNormalState(rp);
    }

    @Override
    public void drawSelectedState(RenderParams rp) {
        if (!this.isSelected()) {
            return;
        }
        GeoPath.newRect(getBounds2D(rp.scale)).drawNormalState(rp);
    }

    @Override
    public java.awt.geom.Rectangle2D getBounds2D(double scale) {
        int w = this.grid.image.getWidth();
        int h = this.grid.image.getHeight();
        final double width = this.cellSize * (w - 1);
        final double height = this.cellSize * (h - 1);
        final double x = this.west;
        final double y = this.north - height;
        return new Rectangle2D.Double(x, y, width, height);
    }

    @Override
    public int getCols() {
        if (grid == null || grid.image == null) {
            return 0;
        }
        return grid.image.getRaster().getWidth();
    }

    @Override
    public int getRows() {
        if (grid == null || grid.image == null) {
            return 0;
        }
        return this.grid.image.getRaster().getHeight();
    }

    @Override
    public double getSouth() {
        return this.north - (getRows() - 1) * this.cellSize;
    }

    @Override
    public double getEast() {
        return this.west + (getCols() - 1) * this.cellSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("\nDimension: ");
        sb.append(getCols()).append(" x ").append(getRows());
        sb.append("\nCell size: ");
        sb.append(getCellSize());
        sb.append("\nWest: ");
        sb.append(getWest());
        sb.append("\nNorth: ");
        sb.append(getNorth());
        return sb.toString();
    }

    @Override
    public boolean isPointOnSymbol(Point2D point, double tolDist, double scale) {
        return this.contains(point.getX(), point.getY());
    }

    @Override
    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {
        Rectangle2D bbox = getBounds2D(GeoObject.UNDEFINED_SCALE);
        return ika.utils.GeometryUtils.rectanglesIntersect(rect, bbox);
    }

    @Override
    public void transform(AffineTransform affineTransform) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
