package ika.geo;
/*
 * GeoImage.java
 *
 * Created on 5. Februar 2005, 14:52
 */

import ika.utils.GeometryUtils;
import ika.utils.ImageUtils;
import ika.utils.MathUtils;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.net.URL;

/**
 * A georeferenced image.
 * Important: An image and a grid with the same numbers of columns and rows are 
 * not of the same size. The image is larger by one cellsize, and the west and 
 * north values are shifted by cellsize / 2.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoImage extends AbstractRaster implements Serializable {

    private static final long serialVersionUID = -65438923204671769L;
    /**
     * The image to display. GeoImage does not offer any functionality to
     * edit this image.
     */
    transient protected BufferedImage image; // cannot be serialized!
    /**
     * URL of the image file that was read.
     */
    private final URL url;

    /**
     * Create a new instance of GeoImage.
     * @param image A reference to an image to display. The image is not copied,
     * instead a reference is retained.
     * @param west Top left corner of this image.
     * @param north Top left corner of this image.
     * @param cellSize Size of a pixel.
     */
    public GeoImage(BufferedImage image, double west, double north, double cellSize) {

        if (image == null || cellSize <= 0) {
            throw new IllegalArgumentException();
        }

        this.image = image;
        this.url = null;
        this.west = west;
        this.north = north;
        this.cellSize = cellSize;
    }

    /**
     * Create a new instance of GeoImage. The lower left corner of the image is
     * placed at 0/0, the size of a pixel in world coordinates equals 1.
     * @param image A reference to an image to display. The image is not copied,
     * instead a reference is retained.
     */
    public GeoImage(BufferedImage image, URL url) {
        this.image = image;
        this.url = url;
    }

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {

        // write the serializable part of this GeoImage
        stream.defaultWriteObject();

        // write the BufferedImage FIXME
        if (this.image != null); // javax.imageio.ImageIO.write(this.image, "png", stream); this is called for undoing! it takes too much time for large images !!! ???
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        // read the serializable part of this GeoImage.
        stream.defaultReadObject();

        // read the BufferedImage
        if (this.image == null) {
            this.image = javax.imageio.ImageIO.read(stream);
        } else {
            System.err.println("reading of GeoImage from file path not implemented yet"); // FIXME !!! ???
            this.image = null;
        }
        /* !!! ???
        ImageImporter importer = new ImageImporter();
        importer.setAskUserToGeoreferenceImage(false);
        GeoImage geoImage = importer.importGeoImageWithImageIOSync(url);
        this.image = geoImage.getBufferedImage();
        geoImage = null;
         */
    }

    public void optimizeForDisplay() {
        this.image = ImageUtils.optimizeForGraphicsHardware(this.image);
    }

    public void convertToGrayscale() {
        if (this.image.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            this.image = ImageUtils.convertToGrayscale(this.image);
        }
    }

    public void drawNormalState(RenderParams rp) {
        if (image != null) {
            drawImage(image, rp, 1);
        }
    }

    public void drawSelectedState(RenderParams rp) {
        if (!isSelected()) {
            return;
        }

        Rectangle2D bounds = getBounds2D(rp.scale);
        if (bounds != null) {
            rp.g2d.draw(bounds);
        }

    }

    protected void drawImage(BufferedImage image, RenderParams rp, double imageScale) {

        Rectangle2D bounds = getBounds2D(rp.scale);
        if (bounds == null) {
            return;
        }

        AffineTransform trans = new AffineTransform();
        trans.translate(rp.tX(getWest()),rp.tY(getNorth()));
        double pixelWidth = bounds.getWidth() * rp.scale * imageScale;
        double pixelHeight = bounds.getHeight() * rp.scale * imageScale;
        trans.scale(pixelWidth / image.getWidth(), pixelHeight / image.getHeight());
        rp.g2d.drawImage(image, trans, null);
        
        //GeoPath.newRect(bounds).drawNormalState(rp);
    }

    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist,
            double scale) {
        if (image == null) {
            return false;
        }
        Rectangle2D bounds = this.getBounds2D(scale);
        GeometryUtils.enlargeRectangle(bounds, tolDist);
        return bounds.contains(point);
    }

    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {
        // Test if if the passed rectangle and the bounding box of this object
        // intersect.
        // Use GeometryUtils.rectanglesIntersect and not Rectangle2D.intersects!
        final Rectangle2D bounds = this.getBounds2D(scale);
        return ika.utils.GeometryUtils.rectanglesIntersect(rect, bounds);
    }

    public Rectangle2D getBounds2D(double scale) {
        if (image == null) {
            return null;
        }

        final double h = cellSize * image.getHeight();
        final double w = cellSize * image.getWidth();
        return new Rectangle2D.Double(west, north - h, w, h);
    }

    /** Returns the number of rows in the image.
     */
    public int getRows() {
        return this.image == null ? 0 : this.image.getHeight();
    }

    /** Returns the number of columns in the image.
     */
    public int getCols() {
        return this.image == null ? 0 : this.image.getWidth();
    }

    /**
     *  Returns the southern border of the lowest row of pixels. This is not the
     *  center of the pixels, but the border.
     * @return The southern border of the lowest row of pixels.
     */
    public double getSouth() {
        return this.north - this.getRows() * this.cellSize;
    }

    /**
     *  Returns the eastern border of the right-most column of pixels. This is 
     * not the center of the pixels, but the border.
     * @return The eastern border of the right-most column of pixels.
     */
    public double getEast() {
        return this.west + this.getCols() * this.cellSize;
    }

    public final int getRGB(int col, int row) {
        return this.image.getRGB(col, row);
    }

    /**
     * Returns a gray value between 0 and 255
     * @param col
     * @param row
     * @return
     */
    public final int getGray(int col, int row) {
        // getRGB() returns a wrong value for grayscale images. The conversion
        // from linear rgb to gamma-corrected rgb seems to be applied twice.
        // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6467250
        if (this.image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return image.getRaster().getSample(col, row, 0);
        } else {
            final int rgb = this.image.getRGB(col, row);
            final int r = (rgb >> 16) & 255;
            final int g = (rgb >> 8) & 255;
            final int b = rgb & 255;
            // compute the weighted gray value from the red, green and blue values
            return (int) (0.299f * r + 0.587f * g + 0.114f * b);
        }
    }

    /**
     * Returns the argb color value that is closest to the passed position.
     * @param x Horizontal coordinate.
     * @param y Vertical coordinate.
     * @return The nearest argb value or transparent black if the point x/y is 
     * outside of the image.
     */
    public final int getNearestNeighbor(double x, double y) {
        // round to nearest neighbor
        int col = (int) ((x - this.west) / this.cellSize);
        int row = (int) ((this.north - y) / this.cellSize);

        final int cols = this.getCols();
        if (col == cols) {
            col = cols - 1;
        } else if (col < 0 || col > cols) {
            return 0;
        }

        final int rows = this.getRows();
        if (row == rows) {
            row = rows - 1;
        } else if (row < 0 || row > rows) {
            return 0;
        }

        return this.image.getRGB(col, row);
    }

    /**
     * Bilinear interpolation.
     * See http://www.geovista.psu.edu/sites/geocomp99/Gc99/082/gc_082.htm
     * "What's the point? Interpolation and extrapolation with a regular grid DEM"
     */
    public final int getBilinearInterpol(double x, double y) {
        final int h1, h2, h3, h4;
        final int rows = this.getRows();
        final int cols = this.getCols();
        final double left = x - this.west;
        final double top = this.north - y;

        // column and row of the top left corner
        final int col = (int) (left / this.cellSize);
        final int row = (int) (top / this.cellSize);
        if (col < 0 || col >= cols || row < 0 || row >= rows) {
            return 0;
        }

        // relative coordinates in the square formed by the four points, scaled to 0..1.
        // The origin is in the lower left corner.
        double relX = left / this.cellSize - col;
        double relY = 1. - (top / this.cellSize - row);
        if (relX < 0) {
            relX = 0;
        } else if (relX > 1) {
            relX = 1;
        }
        if (relY < 0) {
            relY = 0;
        } else if (relY > 1) {
            relY = 1;
        }

        if (row + 1 < rows) {
            // value at bottom left corner
            h1 = this.image.getRGB(col, row + 1);
            // value at bottom right corner
            h2 = col + 1 < cols ? this.image.getRGB(col + 1, row + 1) : 0;
        } else {
            h1 = 0;
            h2 = 0;
        }

        // value at top left corner
        h3 = this.image.getRGB(col, row);

        // value at top right corner
        h4 = col + 1 < cols ? this.image.getRGB(col + 1, row) : 0;

        return GeoImage.bilinearInterpolation(h1, h2, h3, h4, relX, relY);
    }

    /**
     * compute a bilinear interpolation.
     * @param c1 value bottom left
     * @param c2 value bottom right
     * @param c3 value top left
     * @param c4 value top right
     * @param relX relative horizontal coordinate (0 .. 1) counted from left to right
     * @param relY relative vertical coordinate (0 .. 1) counted from bottom to top
     * @return The interpolated value
     */
    private static int bilinearInterpolation(int c1, int c2, int c3, int c4,
            double relX, double relY) {

        double r1 = (0xff0000 & c1) >> 16;
        double g1 = (0xff00 & c1) >> 8;
        double b1 = 0xff & c1;

        double r2 = (0xff0000 & c2) >> 16;
        double g2 = (0xff00 & c2) >> 8;
        double b2 = 0xff & c2;

        double r3 = (0xff0000 & c3) >> 16;
        double g3 = (0xff00 & c3) >> 8;
        double b3 = 0xff & c3;

        double r4 = (0xff0000 & c4) >> 16;
        double g4 = (0xff00 & c4) >> 8;
        double b4 = 0xff & c4;

        int r = (int) (r1 + (r2 - r1) * relX + (r3 - r1) * relY + (r1 - r2 - r3 + r4) * relX * relY);
        int g = (int) (g1 + (g2 - g1) * relX + (g3 - g1) * relY + (g1 - g2 - g3 + g4) * relX * relY);
        int b = (int) (b1 + (b2 - b1) * relX + (b3 - b1) * relY + (b1 - b2 - b3 + b4) * relX * relY);
        if (r > 255) {
            r = 255;
        } else if (r < 0) {
            r = 0;
        }

        if (g > 255) {
            g = 255;
        } else if (g < 0) {
            g = 0;
        }

        if (b > 255) {
            b = 255;
        } else if (b < 0) {
            b = 0;
        }

        return (int) (r << 16 | g << 8 | b | 0xff000000);
    }

    /**
     * compute a bicubic spline interpolation.
     * http://ozviz.wasp.uwa.edu.au/~pbourke/texture_colour/imageprocess/
     * This results in a blurry image.
     */
    /*
    int getBicubicInterpol(double x, double y) {

    // http://www.all-in-one.ee/~dersch/interpolator/interpolator.html

    //final double A = 0.75;
    //final double w;
    //if (x < 1)
    //    w = (( A + 2.0 )*x - ( A + 3.0 ))*x*x +1.0; // 0<x<1
    //else
    //    w = (( A * x - 5.0 * A ) * x + 8.0 * A ) * x - 4.0 * A; // 1<x<2

    // weight = (( A + 2.0 )*x - ( A + 3.0 ))*x*x +1.0;        0<x<1
    // weight = (( A * x - 5.0 * A ) * x + 8.0 * A ) * x - 4.0 * A; 1<x<2

    final double left = x - this.west;
    final double top = this.north - y;
    final int i = (int)(left / this.cellSize);
    final int j = (int)(top / this.cellSize);
    final double dx = (left - i * this.cellSize) / this.cellSize;
    final double dy = (top - j * this.cellSize) / this.cellSize;

    final int rows = this.getRows();
    final int cols = this.getCols();

    if (i == 0 || i >= cols - 2
    || j == 0 || j >= rows - 2)
    return 0;

    double rt = 0;
    double gt = 0;
    double bt = 0;
    for (int m = -1; m <= 2; m++) {
    final double rx = R(m-dx);
    for (int n = -1; n <= 2; n++) {
    final int c = this.image.getRGB(i+m, j+n);
    final int r = ( 0xff0000 & c ) >> 16;
    final int g = ( 0xff00 & c ) >> 8;
    final int b = 0xff & c;
    final double ry = R(n-dy);
    final double f = rx * ry;
    if (f < 0)
    continue;
    rt += r * f;
    gt += g * f;
    bt += b * f;
    }
    }

    final int r, g, b;
    if (rt > 255)
    r = 255;
    else if (rt < 0)
    r = 0;
    else
    r = (int)rt;

    if (gt > 255)
    g = 255;
    else if (gt < 0)
    g = 0;
    else
    g = (int)gt;

    if (bt > 255)
    b = 255;
    else if (bt < 0)
    b = 0;
    else
    b = (int)bt;

    return (int)(r << 16 | g << 8 | b | 0xff000000);
    }
    
    private final double R(double x) {
    final double p_1 = x-1 > 0 ? x-1 : 0;
    final double p = x > 0 ? x : 0;
    final double p1 = x+1 > 0 ? x+1 : 0;
    final double p2 = x+2 > 0 ? x+2 : 0;

    return (p2*p2*p2 - 4 * p1*p1*p1 + 6 * p*p*p - 4 * p_1*p_1*p_1) / 6.;
    }
     */
    /** This has not been tested or verified !!! ???
     * From Grass: raster/r.resamp.interp and lib/gis/interp.c
     */
    public final int getBicubicInterpol(double x, double y) {
        // column and row of the top left corner
        final int col1 = (int) ((x - this.west) / this.cellSize);
        final int col0 = col1 - 1;
        final int col2 = col1 + 1;
        final int col3 = col1 + 2;

        final int row1 = (int) ((this.north - y) / this.cellSize);
        final int row0 = row1 - 1;
        final int row2 = row1 + 1;
        final int row3 = row1 + 2;

        if (col1 == 0 || col1 >= this.getCols() - 2
                || row1 == 0 || row1 >= this.getRows() - 2) {
            // not really a solution, but it works for now !!! ???
            return this.getNearestNeighbor(x, y);
        }

        final double u = ((x - this.west) - col1 * this.cellSize) / cellSize;
        final double v = ((this.north - y) - row1 * this.cellSize) / cellSize;

        final int c00 = this.image.getRGB(col0, row0);
        final int c01 = this.image.getRGB(col1, row0);
        final int c02 = this.image.getRGB(col2, row0);
        final int c03 = this.image.getRGB(col3, row0);

        final int c10 = this.image.getRGB(col0, row1);
        final int c11 = this.image.getRGB(col1, row1);
        final int c12 = this.image.getRGB(col2, row1);
        final int c13 = this.image.getRGB(col3, row1);

        final int c20 = this.image.getRGB(col0, row2);
        final int c21 = this.image.getRGB(col1, row2);
        final int c22 = this.image.getRGB(col2, row2);
        final int c23 = this.image.getRGB(col3, row2);

        final int c30 = this.image.getRGB(col0, row3);
        final int c31 = this.image.getRGB(col1, row3);
        final int c32 = this.image.getRGB(col2, row3);
        final int c33 = this.image.getRGB(col3, row3);

        final int r00 = (0xff0000 & c00) >> 16;
        final int g00 = (0xff00 & c00) >> 8;
        final int b00 = 0xff & c00;
        final int r01 = (0xff0000 & c01) >> 16;
        final int g01 = (0xff00 & c01) >> 8;
        final int b01 = 0xff & c01;
        final int r02 = (0xff0000 & c02) >> 16;
        final int g02 = (0xff00 & c02) >> 8;
        final int b02 = 0xff & c02;
        final int r03 = (0xff0000 & c03) >> 16;
        final int g03 = (0xff00 & c03) >> 8;
        final int b03 = 0xff & c03;

        final int r10 = (0xff0000 & c10) >> 16;
        final int g10 = (0xff00 & c10) >> 8;
        final int b10 = 0xff & c10;
        final int r11 = (0xff0000 & c11) >> 16;
        final int g11 = (0xff00 & c11) >> 8;
        final int b11 = 0xff & c11;
        final int r12 = (0xff0000 & c12) >> 16;
        final int g12 = (0xff00 & c12) >> 8;
        final int b12 = 0xff & c12;
        final int r13 = (0xff0000 & c13) >> 16;
        final int g13 = (0xff00 & c13) >> 8;
        final int b13 = 0xff & c13;

        final int r20 = (0xff0000 & c20) >> 16;
        final int g20 = (0xff00 & c20) >> 8;
        final int b20 = 0xff & c20;
        final int r21 = (0xff0000 & c21) >> 16;
        final int g21 = (0xff00 & c21) >> 8;
        final int b21 = 0xff & c21;
        final int r22 = (0xff0000 & c22) >> 16;
        final int g22 = (0xff00 & c22) >> 8;
        final int b22 = 0xff & c22;
        final int r23 = (0xff0000 & c23) >> 16;
        final int g23 = (0xff00 & c23) >> 8;
        final int b23 = 0xff & c23;

        final int r30 = (0xff0000 & c30) >> 16;
        final int g30 = (0xff00 & c30) >> 8;
        final int b30 = 0xff & c30;
        final int r31 = (0xff0000 & c31) >> 16;
        final int g31 = (0xff00 & c31) >> 8;
        final int b31 = 0xff & c31;
        final int r32 = (0xff0000 & c32) >> 16;
        final int g32 = (0xff00 & c32) >> 8;
        final int b32 = 0xff & c32;
        final int r33 = (0xff0000 & c33) >> 16;
        final int g33 = (0xff00 & c33) >> 8;
        final int b33 = 0xff & c33;

        final double rd = interp_bicubic(
                u, v,
                r00, r01, r02, r03,
                r10, r11, r12, r13,
                r20, r21, r22, r23,
                r30, r31, r32, r33);
        final double gd = interp_bicubic(
                u, v,
                g00, g01, g02, g03,
                g10, g11, g12, g13,
                g20, g21, g22, g23,
                g30, g31, g32, g33);
        final double bd = interp_bicubic(
                u, v,
                b00, b01, b02, b03,
                b10, b11, b12, b13,
                b20, b21, b22, b23,
                b30, b31, b32, b33);

        final int r, g, b;
        if (rd > 255) {
            r = 255;
        } else if (rd < 0) {
            r = 0;
        } else {
            r = (int) rd;
        }

        if (gd > 255) {
            g = 255;
        } else if (gd < 0) {
            g = 0;
        } else {
            g = (int) gd;
        }

        if (bd > 255) {
            b = 255;
        } else if (bd < 0) {
            b = 0;
        } else {
            b = (int) bd;
        }

        return (int) (r << 16 | g << 8 | b | 0xff000000);
    }

    private double interp_cubic(double u, double c0, double c1, double c2, double c3) {
        return (u * (u * (u * (c3 - 3 * c2 + 3 * c1 - c0) + (-c3 + 4 * c2 - 5 * c1 + 2 * c0)) + (c2 - c0)) + 2 * c1) / 2;
    }

    private double interp_bicubic(double u, double v,
            double c00, double c01, double c02, double c03,
            double c10, double c11, double c12, double c13,
            double c20, double c21, double c22, double c23,
            double c30, double c31, double c32, double c33) {
        double c0 = interp_cubic(u, c00, c01, c02, c03);
        double c1 = interp_cubic(u, c10, c11, c12, c13);
        double c2 = interp_cubic(u, c20, c21, c22, c23);
        double c3 = interp_cubic(u, c30, c31, c32, c33);

        return interp_cubic(v, c0, c1, c2, c3);
    }

    @Override
    public void move(double dx, double dy) {
        this.west += dx;
        this.north += dy;
        MapEventTrigger.inform(this);
    }

    @Override
    public void scale(double scale) {
        this.west *= scale;
        this.north *= scale;
        this.cellSize *= scale;
        MapEventTrigger.inform(this);
    }

    @Override
    public void scale(double hScale, double vScale) {
        if (hScale == vScale) {
            this.scale(hScale);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void rotateAroundCenter(double rotRad) {
        AffineTransform at = AffineTransform.getRotateInstance(-rotRad);
        Rectangle2D bounds = this.getBounds2D(GeoObject.UNDEFINED_SCALE);
        bounds = new Rectangle2D.Double(0, 0, bounds.getWidth(), bounds.getHeight());
        GeneralPath path = new GeneralPath(bounds);
        path.transform(at);
        Rectangle2D transformedBounds = path.getBounds2D();
        at.translate(-transformedBounds.getMinX(), -transformedBounds.getMinY());

        RenderingHints hints = new RenderingHints(null);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        AffineTransformOp op = new AffineTransformOp(at, hints);
        int w = (int) transformedBounds.getWidth();
        int h = (int) transformedBounds.getHeight();
        BufferedImage transformedImage = new BufferedImage(w, h, this.image.getType());
//        BufferedImage transformedImage = op.createCompatibleDestImage(this.image, null);
        this.image = op.filter(this.image, transformedImage);
    }

    public void transform(AffineTransform affineTransform) {

        final double m00 = affineTransform.getScaleX();
        final double m01 = affineTransform.getShearX();
        final double m02 = affineTransform.getTranslateX();
        final double m10 = affineTransform.getShearY();
        final double m11 = affineTransform.getScaleY();
        final double m12 = affineTransform.getTranslateY();

        if (MathUtils.numbersAreClose(0, m01) && MathUtils.numbersAreClose(0, m10)) {
            // the transformation only scales and moves this image
            this.scale(m00, m11);
            this.move(m02, m12);
        } else if (MathUtils.numbersAreClose(m01, -m10, 0.00001)) {
            // the transformation rotates this image
            final double rot = Math.asin(m10);
            this.rotateAroundCenter(rot);
        } else {
            throw new UnsupportedOperationException("Shearing of images not supported");
        }

    }

    public BufferedImage getBufferedImage() {
        return this.image;
    }

    public URL getURL() {
        return this.url;
    }

    public GeoImage getResampledCopy(double newCellSize, Object renderingHint, int imageType) {
        int newRows = (int) ((getNorth() - getSouth()) / newCellSize);
        int newCols = (int) ((getEast() - getWest()) / newCellSize);
        BufferedImage newImage = ImageUtils.getFasterScaledInstance(image,
                newCols, newRows, renderingHint, imageType, true);
        return new GeoImage(newImage, getWest(), getNorth(), newCellSize);
    }

    /**
     * Returns a histogram of the image in an array with 256 entries. The
     * histogram is scaled to a maximum of 255.
     * @return
     */
    public int[] getHistogram() {

        int cols = this.getCols();
        int rows = this.getRows();
        int[] histogram = new int[256];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ++histogram[getGray(c, r)];
            }
        }

        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            final int h = histogram[i];
            if (h > max) {
                max = h;
            }
        }
        if (max > 0) {
            for (int i = 0; i < histogram.length; i++) {
                histogram[i] = histogram[i] * 255 / max;
            }
        }
        return histogram;
    }
}
