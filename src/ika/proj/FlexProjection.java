package ika.proj;

import com.jhlabs.map.MapMath;
import java.awt.geom.Point2D;

/**
 * A projection that uses cubic spline interpolation through a series of control
 * points.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class FlexProjection extends DesignProjection implements SerializableProjection {

    public static final String FORMAT_IDENTIFIER = "Flex Projector Format 2.0 - Flex";
    public static final String LEGACY_FORMAT_IDENTIFIER = "Flex Projector Format 1.0";
    private static final int NODES = 18;
    private static final double EPS = 1e-8;
    private static final double RAD15 = Math.toRadians(15);
    private FlexProjectionModel model = null;

    /**
     * Creates a new instance of FlexProjection
     */
    public FlexProjection() {
        this.model = new FlexProjectionModel();
    }

    @Override
    public FlexProjection clone() {
        FlexProjection copy = (FlexProjection) super.clone();
        copy.model = (FlexProjectionModel) this.model.clone();
        return copy;
    }

    @Override
    public boolean parallelsAreParallel() {

        return !model.isAdjustingBending();
    }

    /**
     * Returns true if this projection has an inverse
     */
    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public Point2D.Double project(double x, double y, Point2D.Double dst) {
        /*
         if (x > Math.PI || x < -Math.PI || y > MapMath.HALFPI || y < -MapMath.HALFPI) {
         dst.x = dst.y = Double.NaN;
         return dst;
         }
         */
        final double meridianShift = Math.signum(x) * model.getXDistFactor(x) * RAD15;
        final double scale = model.getScale();

        final double longitudeScaleFactor = model.getLongitudeScaleFactor(y);
        dst.x = scale * longitudeScaleFactor * (x + meridianShift);

        dst.y = scale * model.getScaleY() * model.getLatitudeScaleFactor(y) * Math.PI;
        if (y < 0.0) {
            dst.y = -dst.y;
        }

        // bending
        double bend = model.getBendFactor(y);
        if (bend != 0. && bend != -0.) {
            // Make sure bending is not enlarging the extension of the graticule.
            switch (model.getCurveShape()) {
                case FlexProjectionModel.CUBIC_CURVE: {
                    // cubic curve bending
                    final double xn = Math.abs(x) / Math.PI;
                    if (bend < 0) {
                        dst.y *= 1 + bend * (1 - xn * xn * xn);
                    } else {
                        dst.y *= 1 - bend * xn * xn * xn;
                    }
                    break;
                }
                case FlexProjectionModel.QUADRATIC_CURVE: {
                    // quadratic curve bending
                    final double xn = x / Math.PI;
                    if (bend < 0) {
                        dst.y *= 1 + bend * (1 - xn * xn);
                    } else {
                        dst.y *= 1 - bend * (xn * xn);
                    }
                    break;
                }
                case FlexProjectionModel.COSINE_CURVE: {
                    // sine curve bending
                    if (bend < 0) {
                        dst.y *= 1 + bend * Math.cos(Math.abs(x * 0.5));
                    } else {
                        dst.y *= 1 - bend * Math.abs(Math.cos(x * 0.5));
                    }
                    break;
                }
            }
        }

        return dst;
    }

    /**
     * Inverse projection from X/Y to longitude/latitude.
     * Warning: This inverse projection is extremely slow if the projection is
     * not normalized, that is, if the maximum parallel length and parallel 
     * distance is not equal to 1. If this is the case, this projection is
     * cloned before the computations. Call normalize() to bring the values to
     * 1 before using projectInverse to project an entire data set with many points..
     * @param x
     * @param y
     * @param lp
     * @return 
     */
    @Override
    public Point2D.Double projectInverse(double x, double y, Point2D.Double lp) {

        FlexProjection fp = this;
        if (!model.isNormalized()) {
            fp = this.clone();
            fp.model.normalize();
        }
        
        // first approximation
        fp.projectInverseRobinson(x, y, lp);

        // binary search approximation
        fp.binarySearchInverse(x, y, lp.x, lp.y, lp);

        /* 
         // Test with multiquadratic interpolation. Does not work!
         int ptDist = 5;
         double[][] srcPoints = new double[(180 / ptDist + 1)  * (90 / ptDist + 1)][2];
         double[][] dstPoints = new double[(180 / ptDist + 1)  * (90 / ptDist + 1)][2];
         Point2D.Double pt = new Point2D.Double();
         int ptID = 0;
        
         for (int lon = 0; lon <= 180; lon+=ptDist) {
         for (int lat = 0; lat <= 90; lat+=ptDist) {
         this.project(Math.toRadians(lon), Math.toRadians(lat), pt);
         dstPoints[ptID][0] = Math.toRadians(lon);
         dstPoints[ptID][1] = Math.toRadians(lat);
         srcPoints[ptID][0] = pt.x;
         srcPoints[ptID][1] = pt.y;
         ptID++;
         }
         }
         MultiquadraticInterpolation inter = new MultiquadraticInterpolation();
         inter.solveCoefficients(dstPoints, srcPoints, 1);
         double[] p = new double[] {x, y};
         inter.transform(p, 1);
        
         System.out.println("Multiquadratic interpolation: " + Math.toDegrees(p[0]) + " " + Math.toDegrees(p[1]));
        
         this.inverseNewtonRaphson(x, y, p[0], p[1], lp);
         */
        return lp;
    }

    /**
     * Inverse Newton-Raphson does not work, since the forward projection is not
     * continuous, depending on what curves the user selects, i.e. the first
     * derivative is not continuous, which is a requirement for this method.
     * private void inverseNewtonRaphson(double x, double y, double seedLon,
     * double seedLat, Point2D.Double lp) {
     *
     * int counter = 0; final double h = 1e-10; final double h_2 = h / 2;
     *
     * double lon = seedLon; double lat = seedLat;
     *
     * // derivatives of forward projection Point2D.Double pt1 = new
     * Point2D.Double(); Point2D.Double pt2 = new Point2D.Double(); for (;;) {
     *
     * double lon1 = lon + h_2; double lon2 = lon - h_2; double lat1 = lat +
     * h_2; double lat2 = lat - h_2; double dv = h; double dh = h;
     *
     * if (lat1 > MapMath.HALFPI) { lat1 = MapMath.HALFPI; dv = h_2; } else if
     * (lat2 < -MapMath.HALFPI) { lat2 = -MapMath.HALFPI; dv = h_2; } if (lon1 >
     * Math.PI) { lon1 = Math.PI; dh = h_2; } else if (lon2 < -Math.PI) { lon2 =
     * -Math.PI; dh = h_2; }
     *
     * this.project(lon1, lat, pt1); this.project(lon2, lat, pt2); double
     * xderLon = (pt1.x - pt2.x) / dh; double xderLat = (pt1.y - pt2.y) / dv;
     *
     * this.project(lon, lat1, pt1); this.project(lon, lat2, pt2); double
     * yderLon = (pt1.x - pt2.x) / dh; double yderLat = (pt1.y - pt2.y) / dv;
     *
     * double det = xderLon * yderLat - xderLat * yderLon;
     *
     *
     * // Newton: // |lon'| |lon| |x(lon, lat) - x| // | | = | | - inv(D) * | |
     * // |lat'| |lat| |y(lon, lat) - y| // // |xderLon xderLat| // D = | | //
     * |yderLon yderLat| // // inv(D) = 1/det(D)*trans(D)
     *
     * this.project(lon, lat, pt1); double fx = (pt1.x - x); double fy = (pt1.y
     * - y);
     *
     * double lonDif = (xderLon * fx + yderLon * fy) / det; double latDif =
     * (xderLat * fx + yderLat * fy) / det; lon -= lonDif; lat -= latDif; if
     * (Math.abs(lonDif) < EPS && Math.abs(latDif) < EPS) { break; }
     *
     * counter++; //System.out.println("#" + (counter) + "\t " + lonDif + "\t "
     * + latDif); //System.out.println("\t"+ Math.toDegrees(lon) + "\t " +
     * Math.toDegrees(lat));
     *
     * if (counter > 500) { lon = Double.NaN; lat = Double.NaN; break; } }
     *
     * lp.x = lon; lp.y = lat;
     *
     * }
     */
    // inversion without bending or meridians distribution
    public Point2D.Double projectInverseRobinson(double x, double y, Point2D.Double lp) {

        assert this.model.getY(NODES) == 1;

        final double globalScale = this.model.getScale();
        lp.x = x / globalScale;
        lp.y = Math.abs(y / this.model.getScaleY() / globalScale / Math.PI);
        if (lp.y >= 1.0) { // simple pathologic cases
            if (lp.y > 1.000001) {
                lp.x = Double.NaN;
                lp.y = Double.NaN;
                return lp;
//                throw new ProjectionException();
            } else {
                lp.y = y < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
                lp.x /= this.model.getX(NODES);
            }
        } else { // general problem
            // in Y space, reduce to table interval

            int i;
            for (i = (int) (lp.y * NODES);;) {
                if (this.model.getY(i) > lp.y) {
                    i--;
                } else if (this.model.getY(i + 1) <= lp.y) {
                    i++;
                } else {
                    break;
                }
            }

            final double[] splineCoeffs = this.model.getDistSplineCoeffs(i);
            double Tc0 = splineCoeffs[0];
            final double Tc1 = splineCoeffs[1];
            final double Tc2 = splineCoeffs[2];
            final double Tc3 = splineCoeffs[3];

            // first guess, linear interpolation
            final double Yi1 = this.model.getY(i + 1);
            double t1, t = (lp.y - Tc0) / (Yi1 - Tc0);

            // make into root: find x for y = 0 of f(x)=spline(x)-Tc0
            Tc0 -= lp.y;
            for (;;) { // Newton-Raphson
                final double f = Tc0 + t * (Tc1 + t * (Tc2 + t * Tc3));
                final double fder = Tc1 + t * (Tc2 + Tc2 + t * 3. * Tc3);
                t -= t1 = f / fder;
                if (Math.abs(t1) < EPS) {
                    break;
                }
            }
            lp.y = Math.toRadians(5 * (i + t)); // could be more efficient
            if (y < 0.) {
                lp.y = -lp.y;
            }

            lp.x /= model.getLongitudeScaleFactor(t, i);
        }
        return lp;
    }

    public FlexProjectionModel getModel() {
        return model;
    }

    public void setModel(FlexProjectionModel parameters) {
        this.model = parameters;
    }

    @Override
    public String toString() {
        return "Flex Sliders";
    }

    @Override
    public void setScale(double scale) {
        model.setScale(scale);
    }

    @Override
    public double getScale() {
        return model.getScale();
    }

    @Override
    public double getVerticalScale() {
        return model.getScaleY();
    }

    @Override
    public void setVerticalScale(double vScale) {
        model.setScaleY(vScale);
    }

    @Override
    public String serializeToString() {
        return model.serializeToString();
    }

    @Override
    public void deserializeFromString(String str) {
        model.deserializeFromString(str);
    }
}
