/*
 * GeoGrid.java
 *
 * Created on August 14, 2005, 3:54 PM
 *
 */
package ika.geo;

import java.awt.geom.*;
import java.text.DecimalFormat;

/**
 * A georeferenced raster grid.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoGrid extends AbstractRaster {

    private int cols;
    private int rows;
    private float[][] grid;

    public class GeoGridStatistics {

        public float min;
        public float max;
        public float mean;
        public int voidCount;

        public GeoGridStatistics(GeoGrid geoGrid) {
            float[][] grid = geoGrid.getGrid();
            min = Float.MAX_VALUE;
            max = -Float.MAX_VALUE;
            double tot = 0;
            voidCount = 0;
            for (int r = 0; r < rows; ++r) {
                float row[] = grid[r];
                for (int c = 0; c < cols; ++c) {
                    float v = row[c];
                    if (Float.isInfinite(v) || Float.isNaN(v)) {
                        ++voidCount;
                    } else {
                        tot += v;
                        if (v < min) {
                            min = v;
                        }
                        if (v > max) {
                            max = v;
                        }
                    }
                }
            }
            mean = (float) (tot / (cols * rows));

            // test for grid with only void values
            if (cols * rows == voidCount) {
                min = max = mean = Float.NaN;
            }
        }

        @Override
        public String toString() {
            return this.toString("\n");
        }

        public String toString(String newLine) {
            return this.toString(true, newLine);
        }

        public String toString(boolean includeNumberOfVoidPoints, String newLine) {
            DecimalFormat format = new DecimalFormat(max - min > 1 ? "#,##0.0" : "#,##0.0#####");
            StringBuilder sb = new StringBuilder();
            sb.append("Minimum: ");
            sb.append(format.format(min));
            sb.append(newLine);
            sb.append("Maximum: ");
            sb.append(format.format(max));
            sb.append(newLine);
            sb.append("Mean: ");
            sb.append(format.format(mean));
            if (includeNumberOfVoidPoints) {
                sb.append(newLine);
                sb.append("Number of void points: ");
                sb.append(new DecimalFormat("#,###").format(voidCount));
                if (voidCount > 0) {
                    sb.append(" (");
                    sb.append(new DecimalFormat("0.#").format(voidCount * 100 / (cols * rows)));
                    sb.append("%)");
                }
            }
            return sb.toString();
        }

    }


    /** Creates a new instance of GeoGrid */
    public GeoGrid(int cols, int rows, double cellSize) {
        this.initGrid(cols, rows, cellSize);
    }

    public GeoGrid(int cols, int rows, double cellSize, float initialValue) {
        this.initGrid(cols, rows, cellSize);
        for (int r = 0; r < rows; ++r) {
            java.util.Arrays.fill(this.grid[r], initialValue);
        }
    }

    public GeoGrid (float[][] grid, double cellSize) {
        if (grid == null
                || grid.length < 2
                || grid[0] == null
                || grid[0].length < 2
                || cellSize < 0) {
            throw new IllegalArgumentException();
        }

        this.grid = grid;
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.cellSize = cellSize;
    }

    @Override
    public GeoGrid clone() {
        GeoGrid copy = (GeoGrid) super.clone();

        // deep copy of grid
        copy.grid = new float[this.grid.length][];
        for (int row = 0; row < rows; row++) {
            copy.grid[row] = new float[cols];
            System.arraycopy(grid[row], 0, copy.grid[row], 0, cols);
        }
        return copy;

    }

    private void initGrid(int cols, int rows, double cellSize) {
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.grid = new float[rows][cols];
    }

    public boolean hasSameExtensionAndResolution(GeoGrid grid) {

        if (grid == null) {
            return false;
        }
        return getCols() == grid.getCols() 
                && getRows() == grid.getRows()
                && getWest() == grid.getWest()
                && getNorth() == grid.getNorth()
                && getCellSize() == grid.getCellSize();

    }

    public void drawNormalState(RenderParams rp) {
        GeoPath.newRect(getBounds2D(rp.scale)).drawNormalState(rp);
    }

    public void drawSelectedState(RenderParams rp) {
        if (!this.isSelected()) {
            return;
        }
        GeoPath.newRect(getBounds2D(rp.scale)).drawSelectedState(rp);
    }

    public Rectangle2D getBounds2D(double scale) {
        final double width = cellSize * (cols - 1);
        final double height = cellSize * (rows - 1);
        final double x = west;
        final double y = north - height;
        return new Rectangle2D.Double(x, y, width, height);
    }

    public boolean isIntersectedByRectangle(java.awt.geom.Rectangle2D rect, double scale) {
        // Test if if the passed rectangle and the bounding box of this object
        // intersect.
        // Use GeometryUtils.rectanglesIntersect and not Rectangle2D.intersects!
        final Rectangle2D bounds = this.getBounds2D(scale);
        return ika.utils.GeometryUtils.rectanglesIntersect(rect, bounds);
    }

    public boolean isPointOnSymbol(java.awt.geom.Point2D point, double tolDist, double scale) {
        Rectangle2D bounds = this.getBounds2D(scale);
        ika.utils.GeometryUtils.enlargeRectangle(bounds, tolDist);
        return bounds.contains(point);
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
    }

    public void transform(AffineTransform affineTransform) {
    }

    public final float getValue(int col, int row) {
        return grid[row][col];
    }

    public final float getNearestNeighbor(double x, double y) {
        // round to nearest neighbor
        int col = (int) ((x - this.west) / this.cellSize + 0.5);
        int row = (int) ((this.north - y) / this.cellSize + 0.5);
        if (col < 0 || col >= this.cols || row < 0 || row >= this.rows) {
            return Float.NaN;
        }
        return grid[row][col];
    }

    /**
     * Bilinear interpolation.
     * See http://www.geovista.psu.edu/sites/geocomp99/Gc99/082/gc_082.htm
     * "What's the point? Interpolation and extrapolation with a regular grid DEM"
     */
    public final float getBilinearInterpol(double x, double y) {

         float h1, h2, h3, h4;
        // column and row of the top left corner
        int col = (int)((x - this.west) / this.cellSize);
        int row = (int)((this.north - y) / this.cellSize);

        if (col < 0 || col >= this.cols || row < 0 || row >= this.rows) {
            return Float.NaN;        // relative coordinates in the square formed by the four points, scaled to 0..1.
        // The origin is in the lower left corner.
        }
        double relX = (x - this.west) / this.cellSize - col;
        final double south = this.getSouth();
        double relY = (y - south) / this.cellSize - this.rows + row + 2;

        if (row + 1 < this.rows) {
            // value at bottom left corner
            h1 = this.getValue(col, row + 1);
            // value at bottom right corner
            h2 = col + 1 < this.cols ? this.getValue(col + 1, row + 1) : Float.NaN;
        } else {
            h1 = Float.NaN;
            h2 = Float.NaN;
        }

        // value at top left corner
        h3 = this.getValue(col, row);

        // value at top right corner
        h4 = col + 1 < this.cols ? this.getValue(col + 1, row) : Float.NaN;

        // start with the optimistic case: all values are valid
        return GeoGrid.bilinearInterpolation(h1, h2, h3, h4, relX, relY);
    }

    /**
     * compute a bilinear interpolation.
     * @param h1 value bottom left
     * @param h2 value bottom right
     * @param h3 value top left
     * @param h4 value top right
     * @param relX relative horizontal coordinate (0 .. 1) counted from left to right
     * @param relY relative vertical coordinate (0 .. 1) counted from bottom to top
     * @return The interpolated value
     */
    private static float bilinearInterpolation(float h1, float h2, float h3, float h4, double relX, double relY) {
        return (float) (h1 + (h2 - h1) * relX + (h3 - h1) * relY + (h1 - h2 - h3 + h4) * relX * relY);
    }

    /**
     * compute a bicubic spline interpolation.
     * http://ozviz.wasp.uwa.edu.au/~pbourke/texture_colour/imageprocess/
     */
    /*
    public final float getBicubicInterpol(double x, double y) {

    final double left = x - this.west;
    final double top = this.north - y;
    final int i = (int)(left / this.cellSize);
    final int j = (int)(top / this.cellSize);
    final double dx = left - i * this.cellSize;
    final double dy = top - j * this.cellSize;

    if (i == 0 || i >= this.cols - 2
    || j == 0 || j >= this.rows - 2)
    return 0f;

    float v = 0f;
    for (int m = -1; m <= 2; m++) {
    final double rx = R(m-dx);
    for (int n = -1; n <= 2; n++) {
    v += getValue(i+m, j+n) * rx * R(dy-n);
    }
    }
    return v;
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
    public final float getBicubicInterpol(double x, double y) {
        // column and row of the top left corner
        int col1 = (int) ((x - this.west) / this.cellSize);
        int col0 = col1 - 1;
        int col2 = col1 + 1;
        int col3 = col1 + 2;
        // mirror values along the edges
        if (col1 == 0) {
            col0 = col2;
        } else if (col1 == this.cols - 1) {
            col2 = col0;
            col3 = col0 - 1;
        }

        int row1 = (int) ((this.north - y) / this.cellSize);
        int row0 = row1 - 1;
        int row2 = row1 + 1;
        int row3 = row1 + 2;
        // mirror values along the edges
        if (row1 == 0) {
            row0 = row2;
        } else if (row1 == this.rows - 1) {
            row2 = row0;
            row3 = row0 - 1;
        }

        double u = ((x - this.west) - col1 * this.cellSize) / cellSize;
        double v = ((this.north - y) - row1 * this.cellSize) / cellSize;

        double c00 = this.getValue(col0, row0);
        double c01 = this.getValue(col1, row0);
        double c02 = this.getValue(col2, row0);
        double c03 = this.getValue(col3, row0);

        double c10 = this.getValue(col0, row1);
        double c11 = this.getValue(col1, row1);
        double c12 = this.getValue(col2, row1);
        double c13 = this.getValue(col3, row1);

        double c20 = this.getValue(col0, row2);
        double c21 = this.getValue(col1, row2);
        double c22 = this.getValue(col2, row2);
        double c23 = this.getValue(col3, row2);

        double c30 = this.getValue(col0, row3);
        double c31 = this.getValue(col1, row3);
        double c32 = this.getValue(col2, row3);
        double c33 = this.getValue(col3, row3);

        return (float) interp_bicubic(
                u, v,
                c00, c01, c02, c03,
                c10, c11, c12, c13,
                c20, c21, c22, c23,
                c30, c31, c32, c33);
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

    /**
     * Computes aspect from north in clockwise direction.
     * @param x
     * @param y
     * @return Aspect from north in clockwise direction in radians.
     */
    public double getAspect(double x, double y) {

        final float w = this.getBilinearInterpol(x - this.cellSize, y);
        final float e = this.getBilinearInterpol(x + this.cellSize, y);
        final float s = this.getBilinearInterpol(x, y - this.cellSize);
        final float n = this.getBilinearInterpol(x, y + this.cellSize);
        return Math.atan2(n - s, e - w);
    }
    
    /**
     * Returns the slope for a point in radians. Not tested, probably not correct !!! ???
     * @param x
     * @param y
     * @return
     */
    public double getSlope(double x, double y) {
        final double D = this.cellSize / 5.;
        final float w = this.getBilinearInterpol(x - D, y);
        final float e = this.getBilinearInterpol(x + D, y);
        final float s = this.getBilinearInterpol(x, y - D);
        final float n = this.getBilinearInterpol(x, y + D);
        return Math.atan(Math.hypot((e - w), (n - s)) / (2. * D));
    }

    /**
     * Returns the slope for a cell. Slope is computed with four neighbors. Not tested.
     * @param col
     * @param row
     * @return Slope in radians
     */
    public double getSlope(int col, int row) {

        if (row < 1 || row >= this.grid.length - 1 || col < 1 || col >= this.grid[0].length - 1) {
            return Double.NaN;
        }
        final float w = this.grid[row][col - 1];
        final float e = this.grid[row][col + 1];
        final float s = this.grid[row + 1][col];
        final float n = this.grid[row - 1][col];
        return Math.atan(Math.hypot(e - w, n - s) / (2 * this.cellSize));

    }

    /**
     * Change a value in the grid.
     * <B>Important: This will not generate a MapChange event!</B>
     * @param value The new value
     * @param col The column of the value to change.
     * @param row The row of the value to change
     */
    public void setValue(float value, int col, int row) {
        grid[row][col] = value;
    }

    /**
     * Returns the minimum and maximum value of the grid. This can potentially
     * be expensive as the whole grid is parsed.
     */
    public float[] getMinMax() {
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (grid[r][c] < min) {
                    min = grid[r][c];
                }
                if (grid[r][c] > max) {
                    max = grid[r][c];
                }
            }
        }
        return new float[]{min, max};
    }

    public GeoGridStatistics getStatistics() {
        return new GeoGridStatistics(this);
    }

    public void cut(Rectangle2D extension) {
        final double w = extension.getMinX();
        final double e = extension.getMaxX();
        final double s = extension.getMinY();
        final double n = extension.getMaxY();
        final int firstRow = (int) ((this.north - n) / this.cellSize);
        final int nbrRows = this.getRows() - firstRow -
                (int) ((s - this.getSouth()) / this.cellSize);
        final int firstCol = (int) ((w - this.west) / this.cellSize);
        final int nbrCols = this.getCols() - firstCol -
                (int) ((this.getEast() - e) / this.cellSize);

        this.cut(firstRow, firstCol, nbrRows, nbrCols);
    }

    public void cut(int firstRow, int firstCol, int newRows, int newCols) {
        float[][] newGrid = new float[newRows][newCols];

        // copy section of grid
        for (int i = 0; i < newRows; i++) {
            System.arraycopy(this.grid[i + firstRow], firstCol, newGrid[i], 0, newCols);
        }

        this.cols = newCols;
        this.rows = newRows;
        this.west += firstCol * this.cellSize;
        this.north -= firstRow * this.cellSize;
        this.grid = newGrid;

        MapEventTrigger.inform(this);
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public double getSouth() {
        return this.north - (this.rows - 1) * this.cellSize;
    }

    public double getEast() {
        return this.west + (this.cols - 1) * this.cellSize;
    }

    public float[][] getGrid() {
        return grid;
    }

    /**
     * Converts an horizontal x coordinate to the next column id to the left of x.
     * @return The column (starting with 0) or -1 if x is not on the grid.
     */
    public int xToColumn(double x) {
        if (x < this.west || x > this.getEast()) {
            return -1;
        }
        return (int) ((x - this.west) / this.cellSize);
    }

    /**
     * Converts an vertical y coordinate to the next row id above y.
     * @return The row (starting with 0) or -1 if y is not on the grid.
     */
    public int yToRow(double y) {
        if (y > this.north || y < this.getSouth()) {
            return -1;
        }
        return (int) ((this.north - y) / this.cellSize);
    }

    @Override
    public String toString() {
        return this.toString("\n");
    }
	
    public String toString(String newLine) {
        DecimalFormat f = new DecimalFormat(cellSize < 1 ? "#,##0.0#####" : "#,##0.0");
        DecimalFormat intFormat = new DecimalFormat("#,###");
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(newLine);
        sb.append("Dimension: ");
        sb.append(intFormat.format(this.getCols()));
        sb.append("\u2006\u00D7\u2006"); // multiplication sign surrounded by small spaces
        sb.append(intFormat.format(this.getRows()));
        sb.append(newLine);
        sb.append("Cell size: ");
        sb.append(f.format(cellSize));
        sb.append(newLine);
        sb.append("West: ");
        sb.append(f.format(this.getWest()));
        sb.append(newLine);
        sb.append("North: ");
        sb.append(f.format(this.getNorth()));
        return sb.toString();
    }

    public String toStringWithStatistics() {
        return this.toStringWithStatistics("\n");
    }

    public String toStringWithStatistics(String newLine) {
        StringBuilder sb = new StringBuilder(this.toString(newLine));
        GeoGridStatistics stats = this.getStatistics();
        sb.append(newLine);
        sb.append(stats.toString(newLine));
        return sb.toString();
    }
    
}
