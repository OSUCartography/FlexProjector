package ika.geo;

import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.utils.GeometryUtils;
import java.awt.geom.Point2D;

/**
 *
 * @author jenny
 */
public class FeatureProjector {

    public static void main(String[] args) {
        com.jhlabs.map.proj.Projection proj = new com.jhlabs.map.proj.MercatorProjection();//FlexProjection();
        proj.setProjectionLongitudeDegrees(103);
        proj.initialize();
        Point2D.Double dst = new Point2D.Double();
        proj.transform(-76, 13, dst);
        System.out.println(dst);
        proj.transform(-77, 13, dst);
        System.out.println(dst);
        proj.transform(-78, 13, dst);
        System.out.println(dst);
        System.out.println();

        proj.setProjectionLongitudeDegrees(90);
        proj.initialize();
        proj.transform(-89, 13, dst);
        System.out.println(dst);
        proj.transform(-91, 13, dst);
        System.out.println(dst);
        proj.transform(-90, 13, dst);
        System.out.println(dst);
    }
    protected Projection projection;
    protected double curveTolerance = 5000;
    protected boolean addIntermediatePointsAlongCurves;

    public FeatureProjector(Projection projection, double curveTolerance,
            boolean addIntermediatePointsAlongCurves) {
        this.projection = projection;
        this.addIntermediatePointsAlongCurves = addIntermediatePointsAlongCurves;
        this.curveTolerance = curveTolerance;
    }

    public FeatureProjector(Projection projection) {
        this.projection = projection;
        this.addIntermediatePointsAlongCurves = false;
    }

    protected void curvedLineTo(double lonStart, double latStart, double lonEnd, double latEnd, GeoPathModel projPath) {
        Point2D xyEnd = projectPoint(lonEnd, latEnd);
        double lonStartNorm = normalizeLongitude(lonStart);
        double lonEndNorm = normalizeLongitude(lonEnd);
        final double lon0Deg = projection.getProjectionLongitudeDegrees();

        // project the intermediate point between the start and the end point
        double lonMean = (lonStartNorm + lonEndNorm) * 0.5 + lon0Deg;
        double latMean = (latStart + latEnd) * 0.5;
        Point2D xyMean = this.projectPoint(lonMean, latMean);

        if (xyEnd == null || xyMean == null) {
            return;
        }
        final Point2D xyStart = projPath.getEndPoint();

        // compute the orthogonal distance of the mean point to the line
        // between the start and the end point
        double dsq = GeometryUtils.pointLineDistanceSquare(xyMean, xyStart, xyEnd);
        if (dsq > curveTolerance * curveTolerance) {
            curvedLineTo(lonStart, latStart, lonMean, latMean, projPath);
            projPath.lineTo(xyMean.getX(), xyMean.getY());
            curvedLineTo(lonMean, latMean, lonEnd, latEnd, projPath);
        }
        projPath.lineTo(xyEnd.getX(), xyEnd.getY());
    }

    /**
     * Normalizes a longitude in degrees.
     */
    private double normalizeLongitude(double lon) {
        lon *= MapMath.DTR;
        final double lon0Rad = this.projection.getProjectionLongitude();
        return MapMath.normalizeLongitude(lon - lon0Rad) * MapMath.RTD;
    }

    protected void lineTo(double lonStart, double latStart, double lonEnd, double latEnd, GeoPathModel projPath) {

        if (lonStart == lonEnd && latStart == latEnd) {
            return;
        }
        if (addIntermediatePointsAlongCurves) {
            curvedLineTo(lonStart, latStart, lonEnd, latEnd, projPath);
        } else {
            straightLineTo(lonEnd, latEnd, projPath);
        }
    }

    protected void projectMoveTo(double x, double y, GeoPathModel projPath) {

        // project the point
        Point2D.Double dst = new Point2D.Double();
        try {
            projection.transform(x, y, dst);
        } catch (ProjectionException exc) {
            return;
        }
        if (Double.isNaN(dst.x) || Double.isNaN(dst.y)) {
            return;
        }
        projPath.moveTo(dst.x, dst.y);
    }

    protected Point2D projectPoint(double lon, double lat) {
        // project the point
        Point2D.Double dst = new Point2D.Double();
        try {
            projection.transform(lon, lat, dst);
        } catch (ProjectionException exc) {
            return null;
        }
        if (Double.isNaN(dst.x) || Double.isNaN(dst.y)) {
            return null;
        }
        return dst;
    }

    protected void straightLineTo(double lonEnd, double latEnd, GeoPathModel projPath) {
        Point2D xy = this.projectPoint(lonEnd, latEnd);
        if (xy == null) {
            return;
        }
        if (projPath.getEndPoint().equals(xy)) {
            return;
        }
        projPath.lineTo(xy.getX(), xy.getY());
    }

    public void setCurveTolerance(double curveTolerance) {
        this.curveTolerance = curveTolerance;
    }

    public void setAddIntermediatePointsAlongCurve(boolean addIntermediatePointsAlongCurves) {
        this.addIntermediatePointsAlongCurves = addIntermediatePointsAlongCurves;
    }
}
