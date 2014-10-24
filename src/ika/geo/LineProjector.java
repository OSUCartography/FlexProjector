package ika.geo;

import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.Projection;
import java.awt.geom.Point2D;

/**
 * Projects polylines that are not closed. Cuts lines at the graticule boundaries.
 * 
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class LineProjector extends FeatureProjector {
    
    private boolean prevPointOutOfRange = false;
    
    private boolean firstMoveTo = true;

    public LineProjector(Projection projection, 
            double curveTolerance,
            boolean addIntermediatePointsAlongCurves) {
        super(projection, curveTolerance, addIntermediatePointsAlongCurves);
    }
    
    public LineProjector(Projection projection) {
        super(projection);
    }
    
    public void projectOpenPath(GeoPath geoPath) {
        if (geoPath.getDrawingInstructionCount() < 1) {
            return;
        }
        
        GeoPathModel projPath = new GeoPathModel();
        GeoPathIterator iterator = geoPath.getIterator();
        
        prevPointOutOfRange = false;
        firstMoveTo = true;
        
        double prevLon = iterator.getX();
        double prevLat = iterator.getY();
        
        do {
            final int type = iterator.getInstruction();
            switch (type) {
                case GeoPathModel.CLOSE:
                   // close(projPath);
                    break;
                    
                case GeoPathModel.MOVETO:
                    projectMoveTo(iterator.getX(), iterator.getY(), projPath);
                    break;
                    
                case GeoPathModel.LINETO:
                    final double lon = iterator.getX();
                    final double lat = iterator.getY();
                    projectLineTo(lon, lat, prevLon, prevLat, projPath);
                    prevLon = lon;
                    prevLat = lat;
                    break;
                    
                case GeoPathModel.QUADCURVETO:
                    break;
                    
                case GeoPathModel.CURVETO:
                    break;
            }
        } while (iterator.next());
        
        // remove last command if it was a moveto command.
        if (projPath.getLastInstruction() == GeoPathModel.MOVETO) {
            projPath.removeLastInstruction();
        }
        
        geoPath.setPathModel(projPath);

    }
    
     /**
     * Project the end point of a straight line segment.
     * 
     * @param lonEnd The x coordinate of the end point of the straight line segment.
     * @param latEnd The y coordinate of the end point of the straight line segment.
     * @param lonStart The x coordinate of the start point of the straight line 
     * segment. This is only used when the line segment intersects the bounding
     * meridian of graticule to compute the intersection point.
     * @param latStart The y coordinate of the start point of the straight line 
     * segment. This is only used when the line segment intersects the bounding
     * meridian of graticule to compute the intersection point.
     * @param projPath The path that will receive the projected point(s).
     */
    private void projectLineTo(double lonEnd, double latEnd,
            double lonStart, double latStart,
            GeoPathModel projPath) {

        // test if the point is outside of lon0 +/- 180deg
        final double lon0 = projection.getProjectionLongitudeDegrees();
        final double xlon0 = lonEnd - lon0;
        final boolean pointOutOfRange = xlon0 < -180d || xlon0 > 180d;

        // move or line to
        if (firstMoveTo) {
            Point2D xy = projectPoint(lonEnd, latEnd);
            if (xy != null) {
                projPath.moveTo(xy.getX(), xy.getY());
            }
            firstMoveTo = false;
            prevPointOutOfRange = pointOutOfRange;
        } else {
            if (prevPointOutOfRange != pointOutOfRange) {
                prevPointOutOfRange = pointOutOfRange;
                projectIntersectingLineTo(lonEnd, latEnd, lonStart, latStart, projPath);
            } else {
                lineTo(lonStart, latStart, lonEnd, latEnd, projPath);
            }
        }

    }

    @Override
     protected void projectMoveTo(double x, double y, GeoPathModel projPath) {

        // test if the point is outside of lon0 +/- 180deg
        final double lon0 = projection.getProjectionLongitudeDegrees();
        final double xlon0 = x - lon0;
        final boolean pointOutOfRange = xlon0 < -180 || xlon0 > 180;

        // project the point
        Point2D.Double dst = new Point2D.Double();
        try {
            projection.transform(x, y, dst);
        } catch (ProjectionException exc) {
            return;
        }
        if (Double.isNaN(dst.x) || Double.isNaN(dst.y)) {
            return;        // move to
        }
        projPath.moveTo(dst.x, dst.y);
        prevPointOutOfRange = pointOutOfRange;
        firstMoveTo = false;

    }

     
     /**
     * Computes two intersection points for a straight line segment that crosses
     * the bounding meridian. Projects and adds the two intersection points and 
     * the next end point to a path.
     * @param lonEnd The longitude of the end point of the line segment.
     * @param latEnd The latitude of the end point of the line segment.
     * @param lonStart The longitude of the start point of the line segment.
     * @param latStart The latitude of the start point of the line segment.
     * @param projPath This path will receive three new projected points.
     */
    private void projectIntersectingLineTo(double lonEnd, double latEnd,
            double lonStart, double latStart,
            GeoPathModel projPath) {

        final double dLon = lonEnd - lonStart;
        final double dLat = latEnd - latStart;

        // compute intersection point in geographic coordinates
        final double lon0 = projection.getProjectionLongitudeDegrees();
        final double lonMax = 180 + lon0;
        final double lonMin = -180 + lon0;

        final double lon1; // the longitude of the intermediate end point
        final double lon2; // the longitude of the intermediate start point
        final double lat; // the latitude of both intermediate points
        if (lonEnd > lonMax) {   // leaving graticule towards east
            lon1 = lonMax;
            lat = latStart + dLat * (lonMax - lonStart) / dLon;
            lon2 = lonMin;
        } else if (lonStart > lonMax) { // entering graticule from east
            lon1 = lonMin;
            lat = latStart + dLat * (lonMax - lonStart) / dLon;
            lon2 = lonMax;
        } else if (lonEnd < lonMin) { // leaving graticule towards west
            lon1 = lonMin;
            lat = latStart + dLat * (lonMin - lonStart) / dLon;
            lon2 = lonMax;
        } else if (lonStart < lonMin) { // entering graticule from west
            lon1 = lonMax;
            lat = latStart + dLat * (lonMin - lonStart) / dLon;
            lon2 = lonMin;
        } else {
            return;        // project the intermediate end point
        }
        lineTo(lonStart, latStart, lon1, lat, projPath);

        // remove the last instruction if it is a moveto instruction.
        if (projPath.getLastInstruction() == GeoPathModel.MOVETO) {
            // project the intermediate start point
            projPath.removeLastInstruction();        
        }
        Point2D xy = projectPoint(lon2, lat);
        if (xy != null) {
            // project the new end point 
            projPath.moveTo(xy.getX(), xy.getY());        
        }
        lineTo(lon2, lat, lonEnd, latEnd, projPath);
    }

}
