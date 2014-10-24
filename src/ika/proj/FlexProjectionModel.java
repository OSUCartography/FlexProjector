/*
 * FlexProjectionModel.java
 *
 * Created on May 12, 2007, 12:27 PM
 *
 */
package ika.proj;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.utils.CubicSpline;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.StringTokenizer;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public final class FlexProjectionModel implements Cloneable {

    public static final double ROBINSON_SCALE = 0.8487;
    private static final double ROBINSON_SCALE_Y = 0.5072;
    private static final double[] ROBINSON_PARALLELS_LENGTH = {
        1,
        0.9986,
        0.9954,
        0.99,
        0.9822,
        0.973,
        0.96,
        0.9427,
        0.9216,
        0.8962,
        0.8679,
        0.835,
        0.7986,
        0.7597,
        0.7186,
        0.6732,
        0.6213,
        0.5722,
        0.5322
    };
    private static final double[] ROBINSON_PARALLELS_DISTANCE = {
        0,
        0.062,
        0.124,
        0.186,
        0.248,
        0.31,
        0.372,
        0.434,
        0.4958,
        0.5571,
        0.6176,
        0.6769,
        0.7346,
        0.7903,
        0.8435,
        0.8936,
        0.9394,
        0.9761,
        1
    };
    /**
     * The distance between parallels for which a reference value is defined.
     */
    private static final double LAT_INC_INV = 1. / Math.toRadians(90. / 18.);
    private static final double LON_INC_INV = 1. / Math.toRadians(180. / 12.);
    public static final int CUBIC_CURVE = 0;
    public static final int QUADRATIC_CURVE = 1;
    public static final int COSINE_CURVE = 2;
    public static final double MAX_BENDING = 1d;
    public static final double MIN_BENDING = -MAX_BENDING;
    public static final double MAX_MERIDIANS_DIST = 1d;
    public static final double MIN_MERIDIANS_DIST = -MAX_MERIDIANS_DIST;
    private int curveShape = CUBIC_CURVE;
    /**
     * Global scale factor applied to the graticule.
     */
    private double scale = ROBINSON_SCALE;

    /**
     * Vertical scale factor
     */
    private double scaleY = ROBINSON_SCALE_Y;
    /**
     * A cubic spline  for computing the length of parallels.
     */
    private CubicSpline lengthSpline = new CubicSpline((double[])ROBINSON_PARALLELS_LENGTH.clone());
    /**
     * A cubic spline  for computing the vertical distance of parallels
     * from the equator.
     */
    private CubicSpline distSpline = new CubicSpline((double[]) ROBINSON_PARALLELS_DISTANCE.clone());
    /**
     * A cubic spline  for computing the bending of parallels.
     */
    private CubicSpline bendSpline = new CubicSpline(19);
    /**
     * A cubic spline for computing the horizontal distribution of meridians.
     * A knot value every 15 degrees.
     */
    private CubicSpline xDistSpline = new CubicSpline(13);
    /**
     * True if the direction of meridians is adjusted at the poles.
     */
    private boolean adjustPoleDirection = false;
    /**
     * The angle to adjust the direction of meridians at poles.
     */
    private double meridiansPoleDirection = 80;
    /**
     * True if meridians are smooth at the equator.
     */
    private boolean meridiansSmoothAtEquator = true;

    /** Creates a new instance of FlexProjectionModel */
    public FlexProjectionModel() {
        this.updateSplineTables();
    }

    /**
     * Returns a copy of this object.
     * Important: FlexProjectionModel contains a reference to a FlexProjection.
     * This reference must be updated if a FlexProjectionModel is cloned when
     * a FlexProjection is cloned.
     * @return A copy.
     */
    @Override
    public FlexProjectionModel clone() {
        try {
            FlexProjectionModel copy = (FlexProjectionModel) super.clone();

            copy.lengthSpline = this.lengthSpline.clone();
            copy.distSpline = this.distSpline.clone();
            copy.bendSpline = this.bendSpline.clone();
            copy.xDistSpline = this.xDistSpline.clone();

            return copy;
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }

    /**
     * Adjust the proportions of the projection such that there is no shape
     * distortion at the origin, i.e. the Tissot indicatrix at the origin will
     * be a sphere.
     */
    public void eliminateShapeDistortionAtOrigin() {

        FlexProjection proj = new FlexProjection();
        proj.setModel(this.clone());
        proj.initialize();

        ProjectionFactors pf = new ProjectionFactors();
        pf.compute(proj, 0, 0, 1e-5);
        scaleY *= pf.k / pf.h;

    }

    /**
     * Reset this model to a reference projection.
     * @param proj 
     */
    public void reset(Projection proj) {

        // clone the projection since the ellipsoid will be changed
        proj = (Projection) proj.clone();

        java.awt.geom.Point2D.Double pt = new java.awt.geom.Point2D.Double();
        proj.setEllipsoid(new com.jhlabs.map.Ellipsoid(
                "unarysphere", 1, 1, 0.0, "Unary Sphere"));
        proj.initialize();

        // compute height at central meridian and at 180 degrees and take max
        final double height = Math.max(proj.project(0, MapMath.HALFPI, pt).y,
                proj.project(Math.PI, MapMath.HALFPI, pt).y);

        // compute width at equator and poles and take max
        final double width = Math.max(proj.project(Math.PI, 0, pt).x,
                proj.project(Math.PI, MapMath.HALFPI, pt).x);

        scaleY = height / width;

        // length and distance of parallels
        for (int i = 0; i <= 90; i += 5) {
            final double lat = Math.toRadians(i);
            proj.project(Math.PI, lat, pt);
            lengthSpline.setKnot(i / 5, pt.x / Math.PI);
            distSpline.setKnot(i / 5, pt.y / (scaleY * Math.PI));
        }

        adjustPoleDirection = false;

        scale = 1;

        // bending of parallels
        for (int i = 5; i <= 90; i += 5) {
            // test whether there is some bending
            final double lat = Math.toRadians(i);
            final double y0 = proj.project(0, lat, pt).y;
            final double y180 = proj.project(Math.PI, lat, pt).y;
            final double b_i = y0 / y180 - 1;
            final double bending = Math.abs(b_i) < 0.0001 ? 0 : b_i;
            bendSpline.setKnot(i / 5, bending);
        }
        bendSpline.setKnot(0, bendSpline.getKnot(1));

        // distribution of meridians
        final int hDistKnotsCount = xDistSpline.getKnotsCount();
        // first value is 0, i.e. the central meridian cannot be moved.
        xDistSpline.setKnot(0, 0d);
        // last value is 0, i.e. the bounding meridian cannot be moved.
        xDistSpline.setKnot(hDistKnotsCount - 1, 0d);
        final double meridDist = Math.PI / (hDistKnotsCount - 1);
        for (int i = 1; i < hDistKnotsCount - 1; i++) {
            final double lon = i * meridDist;
            final double xRef = proj.project(lon, 0, pt).x;
            final double xDist = (xRef - width / Math.PI * lon) / (width / Math.PI * meridDist);
            final double knot = (Math.abs(xDist) < 0.0001) ? 0 : xDist;
            xDistSpline.setKnot(i, knot);
        }

        normalize();

    }

    /**
     * Call this each time any variable changes that influences the spline curves.
     */
    private void updateSplineTables() {

        // curvature or slope of meridians at the equator
        final double startSlope;
        if (this.meridiansSmoothAtEquator) {
            // first derivative at the equator is 0, i.e. the length of parallels
            // around the equator does not change.
            startSlope = 0d;
        } else {
            // first derivative at the equator is not defined, set the curvature
            // to 0 for an angular appearance at the equator.
            startSlope = Double.NaN;
        }

        // curvature or slope of the meridians at the poles
        final double endSlope;
        if (this.adjustPoleDirection) {
            // use a complete cubic spline if the user provides a slope at the poles

            // in versions of Flex Projector using file format version 1, there
            // was a bug when scaling meridiansPoleDirection:
            // The idea is to scale from 90 degrees to 18 spline segments.
            // However, the tangens value needs to be scaled, not the angle.
            // The wrong computation was:
            // endSlope = Math.tan(Math.toRadians((-90 + this.meridiansPoleDirection) / 5));
            // The correct computation is:
            // endSlope = Math.tan(Math.toRadians((meridiansPoleDirection - 90))) / 5;

            // To convert from old wrong values to new values (all angles in radians):
            // correct = PI / 2 + atan(5 * tan(old / 5 - PI / 10))
            // this is used when importing old files to convert to the correct
            // computation
             
            endSlope = Math.tan(Math.toRadians((meridiansPoleDirection - 90))) / 5;

        } else {
            // compute a spline with a curvature of 0 at the end
            endSlope = Double.NaN;
        }

        this.lengthSpline.setStartSlope(startSlope);
        this.lengthSpline.setEndSlope(endSlope);

    }

    /**
     * Returns the scale factor for the longitude computed with a cubic spline
     * interpolation.
     * @param lat The latitude for which the factor is computed in radians.
     * @return The scale factor.
     */
    public double getLongitudeScaleFactor(double lat) {
        return this.lengthSpline.eval(Math.abs(lat * LAT_INC_INV));
    }

    public double getLongitudeScaleFactor(double t, int i) {
        return this.lengthSpline.eval(i, t);
    }

    public double getLongitudeScaleFactorFirstDerivative(double lat) {
        return this.lengthSpline.firstDerivative(Math.abs(lat * LAT_INC_INV));
    }

    /**
     * Returns the scale factor for the latitude computed with a cubic spline
     * interpolation.
     * @param lat The latitude for which the factor is computed.
     * @return The scale factor.
     */
    public double getLatitudeScaleFactor(double lat) {
        return this.distSpline.eval(Math.abs(lat * LAT_INC_INV));
    }

    public double getLatitudeScaleFactorFirstDerivative(double lat) {
        return this.distSpline.firstDerivative(Math.abs(lat * LAT_INC_INV));
    }

    public double getBendFactor(double lat) {
        return this.bendSpline.eval(Math.abs(lat * LAT_INC_INV));
    }

    /**
     * Returns the bending factor for the parallels at lat computed with a
     * cubic spline interpolation.
     * @param lat The latitude for which the factor is computed in radians.
     * @return The scale factor.
     */
    public double getBendFactorFirstDerivative(double lat) {
        return this.bendSpline.firstDerivative(Math.abs(lat * LAT_INC_INV));
    }

    /**
     * Returns the distance factor for the meridian at lon computed with a 
     * cubic spline interpolation.
     * @param lat The latitude for which the factor is computed in radians.
     * @return The scale factor.
     */
    public double getXDistFactor(double lon) {
        return this.xDistSpline.eval(Math.abs(lon * LON_INC_INV));
    }

    public double getXDistFactorFirstDerivative(double lon) {
        return this.xDistSpline.firstDerivative(Math.abs(lon * LON_INC_INV));
    }

    /**
     * Returns true if the parallels are bended, i.e. the b array contains 
     * non-zero values.
     */
    public boolean isAdjustingBending() {
        return !this.bendSpline.allKnotsEqual(0);
    }

    /**
     * Returns true if the distance between meridians varies, i.e. the xDist
     * array contains non-zero values.
     */
    public boolean isAdjustingMeridians() {
        return !this.xDistSpline.allKnotsEqual(0);
    }

    public double[] getDistSplineCoeffs(int i) {
        return this.distSpline.getCoefficientsClone(i);
    }

    public void setX(int id, double x) {
        if (this.lengthSpline.getKnot(id) == x) {
            return;
        }

        if (x > 1) {
            this.lengthSpline.setKnot(id, 1);
        } else if (x < 0) {
            this.lengthSpline.setKnot(id, 0);
        } else {
            this.lengthSpline.setKnot(id, x);
        }
        this.updateSplineTables();
    }

    public void setY(int id, double y) {

        if (this.distSpline.getKnot(id) == y) {
            return;
        }

        final boolean increasing = y > this.distSpline.getKnot(id);

        // constrain values to [0..1]
        if (y > 1) {
            y = 1;
        } else if (y < 0) {
            y = 0;
        }
        this.distSpline.setKnot(id, y);
        
        // make sure the values in the distance array are increasing.
        if (increasing) {
            for (int i = 0; i < this.distSpline.getKnotsCount() - 1; i++) {
                final double v = this.distSpline.getKnot(i);
                if (v > this.distSpline.getKnot(i + 1)) {
                    this.distSpline.setKnot(i + 1, v);
                }
            }
        } else {
            for (int i = this.distSpline.getKnotsCount() - 1; i > 0; i--) {
                final double v = this.distSpline.getKnot(i);
                if (v < this.distSpline.getKnot(i - 1)) {
                    this.distSpline.setKnot(i - 1, v);
                }
            }
        }

        this.updateSplineTables();
    }

    public void linearYDistribution() {
        this.distSpline.setKnot(0, 0);
        final int nKnots = this.distSpline.getKnotsCount();
        for (int i = 1; i < nKnots; i++) {
            this.distSpline.setKnot(i, i / (nKnots - 1d));
        }
        this.updateSplineTables();
    }

    public void setBending(int id, double b) {
        b = Math.min(Math.max(b, MIN_BENDING), MAX_BENDING);
        this.bendSpline.setKnot(id, b);
    }

    public void resetBending() {
        for (int i = 0; i < this.bendSpline.getKnotsCount(); i++) {
            this.bendSpline.setKnot(i, 0d);
        }
    }

    public void setXDist(int id, double xd) {
        xd = Math.min(Math.max(xd, MIN_MERIDIANS_DIST), MAX_MERIDIANS_DIST);
        this.xDistSpline.setKnot(id, xd);
    }

    public void resetMeridiansDistribution() {
        for (int i = 0; i < xDistSpline.getKnotsCount(); i++) {
            xDistSpline.setKnot(i, 0d);
        }
    }

    public void resetLengthDistribution() {
        for (int i = 0; i < lengthSpline.getKnotsCount(); i++) {
            lengthSpline.setKnot(i, 1d);
        }
    }

    public double getX(int id) {
        return lengthSpline.getKnot(id);
    }

    public double getY(int id) {
        return distSpline.getKnot(id);
    }

    public double getB(int id) {
        return this.bendSpline.getKnot(id);
    }

    public double getXDist(int id) {
        return this.xDistSpline.getKnot(id);
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        if (this.scaleY == scaleY) {
            return;
        }
        this.scaleY = scaleY;
        this.updateSplineTables();
    }

    public double[] getX() {
        return this.lengthSpline.getKnotsClone();
    }

    public double[] getY() {
        return this.distSpline.getKnotsClone();
    }

    public double[] getB() {
        return this.bendSpline.getKnotsClone();
    }

    public double[] getXDist() {
        return this.xDistSpline.getKnotsClone();
    }

    protected String serializeToString() {
        DecimalFormat formatter = new DecimalFormat("##0.#####");
        DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dfs);
        String lineSep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        sb.append(FlexProjection.FORMAT_IDENTIFIER);
        sb.append(lineSep);
        sb.append("Proportions:");
        sb.append(lineSep);
        sb.append(formatter.format(this.scaleY));

        sb.append(lineSep);
        sb.append("Global Scale:");
        sb.append(lineSep);
        sb.append(formatter.format(this.scale));

        sb.append(lineSep);
        sb.append("Length of Parallels:");
        sb.append(lineSep);
        for (int i = 0; i < this.lengthSpline.getKnotsCount(); i++) {
            sb.append(formatter.format(this.lengthSpline.getKnot(i)));
            sb.append(lineSep);
        }

        sb.append("Adjust Meridians Pole Direction:");
        sb.append(lineSep);
        sb.append(this.adjustPoleDirection);
        sb.append(lineSep);
        sb.append("Meridians Pole Direction:");
        sb.append(lineSep);
        sb.append(this.meridiansPoleDirection);

        sb.append(lineSep);
        sb.append("Meridians are Smooth at Equator:");
        sb.append(lineSep);
        sb.append(this.meridiansSmoothAtEquator);

        sb.append(lineSep);
        sb.append("Distance of Parallels from Equator:");
        sb.append(lineSep);
        for (int i = 0; i < this.distSpline.getKnotsCount(); i++) {
            sb.append(formatter.format(this.distSpline.getKnot(i)));
            sb.append(lineSep);
        }

        sb.append("Bending:");
        sb.append(lineSep);
        switch (this.curveShape) {
            case CUBIC_CURVE:
                sb.append("cubic");
                break;
            case QUADRATIC_CURVE:
                sb.append("quadratic");
                break;
            case COSINE_CURVE:
                sb.append("cosine");
                break;
        }
        sb.append(lineSep);
        for (int i = 0; i < this.bendSpline.getKnotsCount(); i++) {
            sb.append(formatter.format(this.bendSpline.getKnot(i)));
            sb.append(lineSep);
        }

        sb.append("Meridians Distribution:");
        sb.append(lineSep);
        for (int i = 0; i < this.xDistSpline.getKnotsCount(); i++) {
            sb.append(formatter.format(this.xDistSpline.getKnot(i)));
            sb.append(lineSep);
        }

        return sb.toString();
    }

    protected void deserializeFromString(String string) {

        // make sure we have Flex Projector Format 1.0
        boolean v1Format = string.startsWith(FlexProjection.LEGACY_FORMAT_IDENTIFIER);
        boolean v2Format = string.startsWith(FlexProjection.FORMAT_IDENTIFIER);

        if (!v1Format && !v2Format) {
            throw new IllegalArgumentException();
        }

        StringTokenizer tokenizer = new StringTokenizer(string, "\n\r");

        // overread Flex Projector Format 1.0
        tokenizer.nextToken();

        // overread "Proportions"
        tokenizer.nextToken();
        this.scaleY = Double.parseDouble(tokenizer.nextToken());

        // overread "Global Scale"
        tokenizer.nextToken();
        this.scale = Double.parseDouble(tokenizer.nextToken());

        // overread "Length of Parallels"
        tokenizer.nextToken();
        for (int i = 0; i < this.lengthSpline.getKnotsCount(); i++) {
            lengthSpline.setKnot(i, Double.parseDouble(tokenizer.nextToken()));
        }

        // overread "Adjust Meridians Pole Direction:"
        tokenizer.nextToken();
        adjustPoleDirection = Boolean.parseBoolean(tokenizer.nextToken());

        // overread "Meridians Pole Direction"
        tokenizer.nextToken();
        double a = Double.parseDouble(tokenizer.nextToken());
        // convert from bug in version 1 to new version
        // for explanations, see updateSplineTables()
        if (v1Format) {
            a = Math.toRadians(a);
            a = Math.PI / 2 + Math.atan(5 * Math.tan(a / 5 - Math.PI / 10));
            a = Math.toDegrees(a);
        }
        meridiansPoleDirection = a;

        // overread "Meridians Smooth at Equator:"
        tokenizer.nextToken();
        meridiansSmoothAtEquator = Boolean.parseBoolean(tokenizer.nextToken());

        // overread "Distance of Parallels from Equator"
        tokenizer.nextToken();
        for (int i = 0; i < distSpline.getKnotsCount(); i++) {
            distSpline.setKnot(i, Double.parseDouble(tokenizer.nextToken()));
        }

        // overread "Bending"
        tokenizer.nextToken();
        String shape = tokenizer.nextToken().trim().toLowerCase();
        if ("cubic".equals(shape)) {
            this.curveShape = CUBIC_CURVE;
        } else if ("quadratic".equals(shape)) {
            this.curveShape = QUADRATIC_CURVE;
        } else if ("cosine".equals(shape)) {
            this.curveShape = COSINE_CURVE;
        }
        for (int i = 0; i < this.bendSpline.getKnotsCount(); i++) {
            this.bendSpline.setKnot(i, Double.parseDouble(tokenizer.nextToken()));
        }

        // overread "Meridians Distribution:\n"
        tokenizer.nextToken();
        for (int i = 0; i < this.xDistSpline.getKnotsCount(); i++) {
            this.xDistSpline.setKnot(i, Double.parseDouble(tokenizer.nextToken()));
        }

        this.updateSplineTables();
    }

    public int getCurveShape() {
        return curveShape;
    }

    public void setCurveShape(int curveShape) {
        if (this.curveShape == curveShape) {
            return;
        }
        this.curveShape = curveShape;
        this.updateSplineTables();
    }

    /**
     * Scale values for length of parallels and distance of parallels to
     * their maximum value 1.
     */
    public void normalize() {

        // force maximum length to 1
        double maxLength = lengthSpline.getKnotMaximum();
        if (maxLength != 1.) {
            for (int i = 0; i <= 18; i++) {
                lengthSpline.setKnot(i, lengthSpline.getKnot(i) / maxLength);
            }
            // adjust vertical scale, such that the length/height proportions don't change.
            scaleY /= maxLength;

            // adjust the global scale
            scale *= maxLength;
        }

        // force pole at distance 1
        final double maxDist = distSpline.getKnot(18);
        if (maxDist != 1) {
            for (int i = 0; i < distSpline.getKnotsCount() - 1; i++) {
                distSpline.setKnot(i, distSpline.getKnot(i) / maxDist);
            }
            distSpline.setKnot(18, 1d);

            // adjust vertical scale, no need to adjust the global scale
            scaleY *= maxDist;
        }

        if (maxLength != 1 || maxDist != 1) {
            updateSplineTables();
        }
    }
    
    /**
     * Returns true if the maximum length of parallels and the maximum distance
     * from the equator are both equal 1. Returns false otherwise. The normalize()
     * method will adjust the length of parallels and their distribution such
     * that isNormalized() returns true.
     * @return 
     */
    public boolean isNormalized() {
        double maxDist = distSpline.getKnot(18);
        double maxLength = lengthSpline.getKnotMaximum();
        return maxLength == 1. && maxDist == 1.;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getMeridiansPoleDirection() {
        return meridiansPoleDirection;
    }

    public void setMeridiansPoleDirection(double meridiansPoleDirection) {
        if (this.meridiansPoleDirection == meridiansPoleDirection) {
            return;
        }

        this.meridiansPoleDirection = meridiansPoleDirection;
        this.updateSplineTables();
    }

    public boolean isAdjustPoleDirection() {
        return adjustPoleDirection;
    }

    public void setAdjustPoleDirection(boolean adjustPoleDirection) {
        if (this.adjustPoleDirection == adjustPoleDirection) {
            return;
        }

        this.adjustPoleDirection = adjustPoleDirection;
        this.updateSplineTables();
    }

    public boolean isMeridiansSmoothAtEquator() {
        return meridiansSmoothAtEquator;
    }

    public void setMeridiansSmoothAtEquator(boolean meridiansSmoothAtEquator) {
        if (this.meridiansSmoothAtEquator == meridiansSmoothAtEquator) {
            return;
        }

        this.meridiansSmoothAtEquator = meridiansSmoothAtEquator;
        this.updateSplineTables();
    }

    public void mixCurves(FlexProjectionModel flexProjectionModel,
            double lengthW, double distanceW, double bendingW, double meridiansW) {

        for (int i = 0; i < this.lengthSpline.getKnotsCount(); i++) {
            final double k1 = this.lengthSpline.getKnot(i);
            final double k2 = flexProjectionModel.lengthSpline.getKnot(i);
            final double knot = (1d - lengthW) * k1 + lengthW * k2;
            this.lengthSpline.setKnot(i, knot);
        }

        for (int i = 0; i < this.distSpline.getKnotsCount(); i++) {
            final double k1 = this.distSpline.getKnot(i);
            final double k2 = flexProjectionModel.distSpline.getKnot(i);
            final double knot = (1d - distanceW) * k1 + distanceW * k2;
            this.distSpline.setKnot(i, knot);
        }

        for (int i = 0; i < this.bendSpline.getKnotsCount(); i++) {
            final double k1 = this.bendSpline.getKnot(i);
            final double k2 = flexProjectionModel.bendSpline.getKnot(i);
            final double knot = (1d - bendingW) * k1 + bendingW * k2;
            this.bendSpline.setKnot(i, knot);
        }

        for (int i = 0; i < this.xDistSpline.getKnotsCount(); i++) {
            final double k1 = this.xDistSpline.getKnot(i);
            final double k2 = flexProjectionModel.xDistSpline.getKnot(i);
            final double knot = (1d - meridiansW) * k1 + meridiansW * k2;
            this.xDistSpline.setKnot(i, knot);
        }
        
        this.updateSplineTables();
    }
}
