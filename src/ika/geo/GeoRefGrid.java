/*
 * GeoRefGrid.java
 *
 * Created on February 15, 205, 2:37 PM
 */

package ika.geo;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoRefGrid extends GeoObject {
    
    /* LinkedHashSet keeps the order in which the elements were added. */
    private LinkedHashSet<GeoObject>[] refs;
    private int cols;
    private int rows;
    private double west;
    private double south;
    private double cellSize;
    private BufferedImage rasterizerImage;
    private GeoImage visualizerImage;
    private Graphics2D rasterizerG2d;
    
    public final static int STATS_1_REF = 0;
    public final static int STATS_2_REF = 1;
    public final static int STATS_3_REF = 2;
    public final static int STATS_4_REF = 3;
    public final static int STATS_TOTAL = 4;
    public final static int STATS_OCCUPIED = 5;
    
    
    public static GeoRefGrid createGeoRefGrid(java.awt.geom.Rectangle2D bounds,
            double cellSize) {
        if (cellSize <= 0)
            throw new IllegalArgumentException("Cell size must be > 0");
        
        int cols = (int)Math.ceil(bounds.getWidth() / cellSize);
        int rows = (int)Math.ceil(bounds.getHeight() / cellSize);
        
        // make sure grid is at least 1x1 pixel large.
        cols = Math.max(cols, 1);
        rows = Math.max(rows, 1);
        
        return new GeoRefGrid(cols, rows, bounds.getX(), bounds.getY(), cellSize);
    }
    
    /** Creates a new instance of GeoRefGrid */
    public GeoRefGrid(int cols, int rows, double west, double south, double cellSize) {
        refs = new LinkedHashSet [cols*rows];
        this.cols = cols;
        this.rows = rows;
        this.west = west;
        this.south = south;
        this.cellSize = cellSize;
        
        // setup image to rasterize objects
        this.rasterizerImage = new BufferedImage(cols, rows,
                BufferedImage.TYPE_BYTE_GRAY);
        this.rasterizerG2d = rasterizerImage.createGraphics();
        this.rasterizerG2d.setColor(Color.white);
        this.rasterizerG2d.setBackground(Color.black);
        this.rasterizerG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON); // enable antialiasing
        
        // transform from Geo space to user space.
        // last transformation first!
        /*
         * Transformation:
         * x_ = (x-west)*scale;
         * y_ = (north-y)*scale = (y-north)*(-scale);
         */
        final double scale = 1./cellSize;
        rasterizerG2d.scale(scale, -scale);
        rasterizerG2d.translate(-west, -this.getNorth());
        
        this.visualizerImage = null;
    }
    
    public double getWidth() {
        return cellSize * cols;
    }
    
    public double getHeight() {
        return cellSize * rows;
    }
    
    public double getNorth(){
        return this.south + this.rows * this.cellSize;
    }
    
    private void rasterizeGeoSet(GeoSet geoSet){
        if (geoSet == null)
            return;
        
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            final int nbrChildren = geoSet.getNumberOfChildren();
            for (int i = 0; i < nbrChildren; i++) {
                GeoObject geoObject = geoSet.getGeoObject(i);
                if (geoObject instanceof GeoPath){
                    
                    GeoPath geoPath = (GeoPath)geoObject;
                    VectorSymbol symbol = geoPath.getVectorSymbol();
                    rasterizerG2d.setStroke(new BasicStroke((float)symbol.getStrokeWidth()));
                    this.rasterize(geoPath.toPathIterator(null), symbol);
                    
                } else if (geoObject instanceof GeoSet){
                    this.add((GeoSet)geoObject, false);
                } else if (geoObject instanceof GeoImage){
                } else if (geoObject instanceof GeoPoint){
                }
            }
        } finally {
            trigger.inform();
        }
    }
    
    public void add(GeoSet geoSet, boolean treatGeoSetAsOneObject) {
        if (geoSet == null)
            return;
        
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (treatGeoSetAsOneObject){
                clearRasterizer();
                rasterizeGeoSet(geoSet);
                copyRasterizer(geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE), geoSet, null);
            } else {
                final int nbrChildren = geoSet.getNumberOfChildren();
                for (int i = 0; i < nbrChildren; i++) {
                    GeoObject geoObject = geoSet.getGeoObject(i);
                    if (geoObject instanceof GeoPath)
                        this.add((GeoPath)geoObject);
                    else if (geoObject instanceof GeoSet)
                        this.add((GeoSet)geoObject, false);
                    else if (geoObject instanceof GeoImage)
                        this.add((GeoImage)geoObject);
                    else if (geoObject instanceof GeoPoint)
                        this.add((GeoPoint)geoObject);
                }
            }
        } finally {
            trigger.inform();
        }
    }
    
    public void add(GeoPoint geoPoint) {
        PointSymbol pointSymbol = geoPoint.getPointSymbol();
        rasterizerG2d.setStroke(new BasicStroke((float)pointSymbol.getStrokeWidth()));
        
        GeoPath geoPath = pointSymbol.getPointSymbol(1, geoPoint.getX(), geoPoint.getY());
        PathIterator pathIterator = geoPath.toPathIterator(null);
        java.awt.geom.Rectangle2D objBounds = geoPath.getBounds2D(GeoPath.UNDEFINED_SCALE);
        this.addPathIterator(pathIterator, pointSymbol, objBounds, geoPoint);
        MapEventTrigger.inform(this);
    }
    
    public void add(GeoPath geoPath) {
        // apply symbol of GeoPath
        VectorSymbol vectorSymbol = geoPath.getVectorSymbol();
        rasterizerG2d.setStroke(new BasicStroke((float)vectorSymbol.getStrokeWidth()));
        Rectangle2D objBounds = geoPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        this.addPathIterator(geoPath.toPathIterator(null), vectorSymbol, objBounds, geoPath);
        MapEventTrigger.inform(this);
    }
    
    public boolean isAddingCausingOverlay(GeoSet geoSet, boolean treatGeoSetAsOneObject) {
        if (treatGeoSetAsOneObject){
            clearRasterizer();
            rasterizeGeoSet(geoSet);
            Rectangle2D objBounds = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
            return isCopyingRasterizerCausingOverlay(objBounds, null);
        } else {
            final int nbrChildren = geoSet.getNumberOfChildren();
            for (int i = 0; i < nbrChildren; i++) {
                GeoObject geoObject = geoSet.getGeoObject(i);
                if (geoObject instanceof GeoPath)
                    return this.isAddingCausingOverlay((GeoPath)geoObject);
                else if (geoObject instanceof GeoSet)
                    return this.isAddingCausingOverlay((GeoSet)geoObject, false);
            }
        }
        return false;
    }
    
    public boolean isAddingCausingOverlay(GeoPath geoPath){
        // apply symbol of GeoPath
        VectorSymbol vectorSymbol = geoPath.getVectorSymbol();
        rasterizerG2d.setStroke(new BasicStroke((float)vectorSymbol.getStrokeWidth()));
        clearRasterizer();
        rasterize(geoPath.toPathIterator(null), vectorSymbol);
        Rectangle2D objBounds = geoPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        
        return isCopyingRasterizerCausingOverlay(objBounds, vectorSymbol); 
    }
    
    public int getNbrOverlayedCellsWhenAdding(GeoSet geoSet,
            boolean treatGeoSetAsOneObject, GeoObject[] geoObjectsToIgnore) {
        
        if (geoSet == null)
            return 0;
        
        if (treatGeoSetAsOneObject){
            clearRasterizer();
            rasterizeGeoSet(geoSet);
            Rectangle2D objBounds = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
            return getNbrOverlayedCellsWhenAdding(objBounds, null, 
                    geoObjectsToIgnore);
        } else {
            final int nbrChildren = geoSet.getNumberOfChildren();
            for (int i = 0; i < nbrChildren; i++) {
                GeoObject geoObject = geoSet.getGeoObject(i);
                if (geoObject instanceof GeoPath)
                    return this.getNbrOverlayedCellsWhenAdding(
                            (GeoPath)geoObject, geoObjectsToIgnore);
                else if (geoObject instanceof GeoSet)
                    return this.getNbrOverlayedCellsWhenAdding((GeoSet)geoObject, 
                            false, geoObjectsToIgnore);
            }
        }
        return 0;
    }
    
    public int getNbrOverlayedCellsWhenAdding(GeoPath geoPath, GeoObject[] geoObjectsToIgnore){
        // apply symbol of GeoPath
        VectorSymbol vectorSymbol = geoPath.getVectorSymbol();
        rasterizerG2d.setStroke(new BasicStroke((float)vectorSymbol.getStrokeWidth()));
        clearRasterizer();
        rasterize(geoPath.toPathIterator(null), vectorSymbol);
        Rectangle2D objBounds = geoPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        
        return getNbrOverlayedCellsWhenAdding(objBounds, vectorSymbol, geoObjectsToIgnore); 
    }
    
    public void add(GeoImage geoImage) {
        throw new IllegalArgumentException("GeoImage not supported yet");
    }
    
    private void addPathIterator(PathIterator pathIterator,
            VectorSymbol vectorSymbol,
            Rectangle2D objBounds,
            GeoObject geoObject) {
        clearRasterizer();
        rasterize(pathIterator, vectorSymbol);
        copyRasterizer(objBounds, geoObject, vectorSymbol);
    }
    
    /** Clears the rasterizer.
     */
    private void clearRasterizer(){
        this.visualizerImage = null;
        
        // clear the rasterizer image
        AffineTransform trans = rasterizerG2d.getTransform();
        rasterizerG2d.setTransform(new AffineTransform());
        rasterizerG2d.clearRect(0, 0, cols, rows);
        rasterizerG2d.setTransform(trans);
    }
    
    /**
     * Draws a PathIterator into the rasterizer
     */
    private void rasterize(PathIterator pathIterator,
            VectorSymbol vectorSymbol){
        GeneralPath path = new GeneralPath();
        path.append(pathIterator, false);
        if (vectorSymbol.isFilled())
            rasterizerG2d.fill(path);
        if (vectorSymbol.isStroked())
            rasterizerG2d.draw(path);
    }
    
    /** Extracts painted pixels from the rasterizer and stores
     * references to GeoObject.
     */
    private void copyRasterizer(Rectangle2D objBounds, GeoObject geoObject,
            VectorSymbol vectorSymbol) {
        // copy occupied cells from rasterizer image to this GeoRefGrid
        // compute section that has to be copied from the rasterized image
        
        final double lineWidth = vectorSymbol !=  null ?
            vectorSymbol.getStrokeWidth()*2. : 0;
        
        final double objWest = objBounds.getMinX();
        final double objEast = objBounds.getMaxX();
        final double objSouth = objBounds.getMinY();
        final double objNorth = objBounds.getMaxY();
        
        int firstCol = (int)((objWest - lineWidth - this.west)/cellSize);
        int firstRow = (int)((objNorth - lineWidth - this.getNorth())/cellSize);
        firstCol = Math.max(0, firstCol);
        firstRow = Math.max(0, firstRow);
        
        int lastCol = (int)Math.ceil((objEast + lineWidth - this.west)/cellSize);
        int lastRow = (int)Math.ceil((this.getNorth() - objSouth + lineWidth)/cellSize);
        lastCol = Math.min(rasterizerImage.getWidth(), lastCol);
        lastRow = Math.min(rasterizerImage.getHeight(), lastRow);
        int imgWidth = lastCol - firstCol;
        int imgHeight = lastRow - firstRow;
        int[] grayValues = null;
        
        final int w = Math.max(0, lastCol-firstCol);
        final int h = Math.max(0, lastRow-firstRow);
        grayValues = rasterizerImage.getRaster().getSamples(firstCol, firstRow,
                w, h, 0, grayValues);
        LinkedHashSet set;
        int refGridIndex;
        int imgIndex = 0;
        for (int r = 0; r < imgHeight; ++r) {
            refGridIndex = (firstRow+r) * cols + firstCol;
            for (int c = 0; c < imgWidth; ++c) {
                if (grayValues[imgIndex++] > 0) {
                    if (refs[refGridIndex] == null) {
                        set = new LinkedHashSet();
                        refs[refGridIndex] = set;
                    } else
                        set = refs[refGridIndex];
                    set.add(geoObject);
                }
                ++refGridIndex;
            }
        }
        
    }
    
    private boolean isCopyingRasterizerCausingOverlay(Rectangle2D objBounds,
            VectorSymbol vectorSymbol) {
        
        final double lineWidth = vectorSymbol !=  null ?
            vectorSymbol.getStrokeWidth()*2. : 0;
        
        final double objWest = objBounds.getMinX();
        final double objEast = objBounds.getMaxX();
        final double objSouth = objBounds.getMinY();
        final double objNorth = objBounds.getMaxY();
        
        int firstCol = (int)((objWest - lineWidth - this.west)/cellSize);
        int firstRow = (int)((objNorth - lineWidth - this.getNorth())/cellSize);
        firstCol = Math.max(0, firstCol);
        firstRow = Math.max(0, firstRow);
        
        int lastCol = (int)Math.ceil((objEast + lineWidth - this.west)/cellSize);
        int lastRow = (int)Math.ceil((this.getNorth() - objSouth + lineWidth)/cellSize);
        lastCol = Math.min(rasterizerImage.getWidth(), lastCol);
        lastRow = Math.min(rasterizerImage.getHeight(), lastRow);
        int imgWidth = lastCol - firstCol;
        int imgHeight = lastRow - firstRow;
        int[] grayValues = null;
        
        final int w = Math.max(0, lastCol-firstCol);
        final int h = Math.max(0, lastRow-firstRow);
        grayValues = rasterizerImage.getRaster().getSamples(firstCol, firstRow,
                w, h, 0, grayValues);

        int refGridIndex;
        int imgIndex = 0;
        for (int r = 0; r < imgHeight; ++r) {
            refGridIndex = (firstRow+r) * cols + firstCol;
            for (int c = 0; c < imgWidth; ++c) {
                if (grayValues[imgIndex++] > 0) {
                    // found a grid cell covered by the passed GeoObject 
                    // that contains a reference to another GeoObject
                    if (refs[refGridIndex] != null) {
                        return true;
                    }
                }
                ++refGridIndex;
            }
        }
        
        return false;
    }
    
    private int getNbrOverlayedCellsWhenAdding (Rectangle2D objBounds,
            VectorSymbol vectorSymbol, GeoObject[] geoObjectsToIgnore) {
        
        final double lineWidth = vectorSymbol !=  null ?
            vectorSymbol.getStrokeWidth()*2. : 0;
        
        final double objWest = objBounds.getMinX();
        final double objEast = objBounds.getMaxX();
        final double objSouth = objBounds.getMinY();
        final double objNorth = objBounds.getMaxY();
        
        int firstCol = (int)((objWest - lineWidth - this.west)/cellSize);
        int firstRow = (int)((objNorth - lineWidth - this.getNorth())/cellSize);
        firstCol = Math.max(0, firstCol);
        firstRow = Math.max(0, firstRow);
        
        int lastCol = (int)Math.ceil((objEast + lineWidth - this.west)/cellSize);
        int lastRow = (int)Math.ceil((this.getNorth() - objSouth + lineWidth)/cellSize);
        lastCol = Math.min(rasterizerImage.getWidth(), lastCol);
        lastRow = Math.min(rasterizerImage.getHeight(), lastRow);
        int imgWidth = lastCol - firstCol;
        int imgHeight = lastRow - firstRow;
        int[] grayValues = null;
        
        final int w = Math.max(0, lastCol-firstCol);
        final int h = Math.max(0, lastRow-firstRow);
        grayValues = rasterizerImage.getRaster().getSamples(firstCol, firstRow,
                w, h, 0, grayValues);
        LinkedHashSet set;
        int refGridIndex;
        int imgIndex = 0;
        int nbrOverlayedCells = 0;
        for (int r = 0; r < imgHeight; ++r) {
            refGridIndex = (firstRow+r) * cols + firstCol;
            for (int c = 0; c < imgWidth; ++c) {
                if (grayValues[imgIndex++] > 0) {
                    // found a grid cell covered by the passed GeoObject 
                    // that contains a reference to another GeoObject
                    if (refs[refGridIndex] != null) {
                        set = refs[refGridIndex];
                        int nbrOverlays = set.size();
                        for (int i = 0; i < geoObjectsToIgnore.length; ++i){
                            if (set.contains(geoObjectsToIgnore[i]))
                                --nbrOverlays;
                        }
                        if (nbrOverlays > 0)
                            ++nbrOverlayedCells;
                    }
                }
                ++refGridIndex;
            }
        }
        
        return nbrOverlayedCells;
    }
    
    public BufferedImage toBufferedImage() {
        GeoImage geoImage = this.toGeoImage();
        return geoImage.getBufferedImage();
    }
    
    public GeoImage toGeoImage() {
        
        if (visualizerImage != null)
            return visualizerImage;
        
        // index colors. First color will be replaced with complete transparency.
        // can be expanded to up to 16 indexed colors.
        byte ff = (byte)0xff;
        byte[] r = {ff, 0,  0,  ff};
        byte[] g = {ff, ff, 0,  0};
        byte[] b = {ff, 0,  ff, 0};
        
        // use 2 bits for currently 4 colors, index 0 transparent.
        final int nbrColors = 4;
        final int lastColorIndex = nbrColors-1;
        IndexColorModel cm = new IndexColorModel(2, nbrColors, r, g, b, 0);
        BufferedImage image = new BufferedImage(cols, rows,
                BufferedImage.TYPE_BYTE_BINARY, cm);
        
        // fill image
        int nbrCells = this.cols * this.rows;
        int [] grayValues = new int [nbrCells];
        for (int i = 0; i < nbrCells; ++i) {
            Set set = refs[i];
            grayValues[i] = (set != null)
            ? Math.min(lastColorIndex, set.size())
            : 0;
        }
        image.getRaster().setSamples(0, 0, image.getWidth(), image.getHeight(),
                0, grayValues);
        
        this.visualizerImage = new GeoImage(image, this.west, this.getNorth(), this.cellSize);
        return this.visualizerImage;
    }
    
    public void drawNormalState(RenderParams rp) {
        
        rp.g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        rp.g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
        GeoImage geoImage = toGeoImage();
        
        geoImage.drawNormalState(rp);
        GeoPath.newRect(getBounds2D(rp.scale)).drawNormalState(rp);
        
    }
    
    public void drawSelectedState(RenderParams rp) {
        if (!isSelected()) {
            return;
        }
         
        GeoPath.newRect(getBounds2D(rp.scale)).drawNormalState(rp);
    }
    
    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist,
            double scale) {
        Rectangle2D rect = new Rectangle2D.Double(west-tolDist, south-tolDist,
                cellSize * cols + 2 * tolDist,
                cellSize * rows + 2 * tolDist);
        return rect.contains(point);
    }
    
    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {
        return false;
    }
    
    public Rectangle2D getBounds2D(double scale) {
        return new Rectangle2D.Double(west, south, cellSize * cols, cellSize * rows);
    }
    
    /**
     * Returns the objects referenced at a specified position in the grid.
     * @param col Column of the cell from the left border.
     * @param row Row of the cell from the top border.
     * @return A shared instance of a set containing the referenced objects.
     */
    public Set<GeoObject> getObjectsAtColRow(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows)
            return null;
        int id = col + row * cols;
        return this.refs[id];
    }
    
    public Set getObjectsAtPosition(Point2D pt) {
        return this.getObjectsAtPosition(pt.getX(), pt.getY());
    }
    
    public Set getObjectsAtPosition(double x, double y) {
        // find cell at position west / south
        int col = (int)((x - west) / cellSize);
        int row = (int)((this.getNorth() - y) / cellSize);
        return this.getObjectsAtColRow(col, row);
    }
    
    public Set<GeoObject> getCloseObjects(double x, double y, int searchRadiusInCells) {
        int col = (int)((x - west) / cellSize);
        int row = (int)((this.getNorth() - y) / cellSize);
        LinkedHashSet<GeoObject> set = new LinkedHashSet<GeoObject>();
        for (int r = row - searchRadiusInCells; r <= row + searchRadiusInCells; r++) {
            for (int c = col - searchRadiusInCells; c <= col + searchRadiusInCells; c++) {
                Set<GeoObject> cellSet = getObjectsAtColRow(c, r);
                if (cellSet != null)
                    set.addAll(cellSet);
            }
        }
        return set;
    }
    
    public int[] getStatistics(){
        int[] stats = new int[6];
        stats[STATS_TOTAL] = this.cols * this.rows;
        
        if (this.refs == null)
            return stats;
        
        for (int i = 0; i < this.refs.length; i++){
            LinkedHashSet set = this.refs[i];
            if (set != null) {
                int nbrRefs = set.size();
                if (nbrRefs > 4)
                    nbrRefs = 4;
                ++stats[nbrRefs - 1];
                ++stats[STATS_OCCUPIED];
            }
        }
        
        return stats;
    }
    
    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(super.toString());
        str.append("\nCell Size\t: ");
        str.append(this.cellSize);
        str.append("\nColums\t: ");
        str.append(this.cols);
        str.append("\nRows\t: ");
        str.append(this.rows);
        
        int[] stats = this.getStatistics();
        str.append("\nTotal number of cells\t: ");
        str.append(stats[STATS_TOTAL]);
        str.append("\nNumber of occupied cells\t: ");
        str.append(stats[STATS_OCCUPIED]);
        for (int i = 0; i < 4; i++){
            str.append("\nNumber of cells with ");
            str.append(i + 1);
            str.append(" references\t: ");
            str.append(stats[i]);
        }
        
        return str.toString();
    }
    
    @Override
    public void move(double dx, double dy) {
        this.west += dx;
        this.south += dy;
        MapEventTrigger.inform(this);
    }
    
    @Override
    public void scale(double scale) {
        this.west *= scale;
        this.south *= scale;
        this.cellSize *= scale;
        MapEventTrigger.inform(this);
    }
    
    public void transform(AffineTransform affineTransform) {
        throw new IllegalArgumentException("not supported yet");
    }
}
