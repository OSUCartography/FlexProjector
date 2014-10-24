package ika.geo.grid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class Interpolator {

    private double frontBackAngle;
    private double sin, cos;

    public Interpolator() {
        this.init();
    }

    private void init() {

        double a = Math.toRadians(this.frontBackAngle);
        sin = Math.sin(a);
        cos = Math.cos(a);

    }

    /**
     * Returns a weight between 0 and 1 that varies along the direction of the
     * current <code>frontBackAngle</code>.
     * @param col The horizontal coordinate of the cell in the grid for which
     * the weight is computed.
     * @param row The vertical coordinate of the cell in the grid for which
     * the weight is computed.
     * @param cols The number of columns in the grid.
     * @param rows The number of rows in the grid.
     * @return A weight between 0 and 1.
     */
    public final double weight(int col, int row, int cols, int rows) {

        final double halfRows = rows / 2d;
        final double halfCols = cols / 2d;

        // point in grid coordinates relative to center of grid
        final double cx = col - halfCols;
        final double cy = halfRows - row;

        // rotate point in grid
        final double yrot = -cx * sin + cy * cos;

        // corner of grid relative to center of grid
        final double yCorner, xCorner;
        if (frontBackAngle < -90) {
            xCorner = -halfCols;
            yCorner = -halfRows;
        } else if (frontBackAngle < 0) {
            xCorner = -halfCols;
            yCorner = halfRows;
        } else if (frontBackAngle < 90) {
            xCorner = halfCols;
            yCorner = halfRows;
        } else {
            xCorner = halfCols;
            yCorner = -halfRows;
        }

        // rotate corner of grid
        final double ycornerRot = xCorner * sin + yCorner * cos;

        return (ycornerRot - yrot) / (ycornerRot * 2d);

    }

    public final float interpolateWeight(double w1, double w2, int c, int r, int cols, int rows) {
        double w = weight(c, r, cols, rows);
        return (float) (w1 * w + w2 * (1. - w));
    }

    public double getFrontBackAngle() {
        return frontBackAngle;
    }

    public void setFrontBackAngle(double frontBackAngle) {
        this.frontBackAngle = frontBackAngle;
        init();
    }
}
