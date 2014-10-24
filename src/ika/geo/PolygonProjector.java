package ika.geo;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.utils.GeometryUtils;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * Projects closed polygons. Cuts polygons along the boundaries of the graticule.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class PolygonProjector extends FeatureProjector {

    public PolygonProjector(Projection projection) {
        super(projection);
    }
    
    public PolygonProjector(Projection projection, double curveTolerance, boolean addIntermediatePointsAlongCurves) {
        super(projection, curveTolerance, addIntermediatePointsAlongCurves);
    }

    /**
     * Projects a closed path.
     * @param geoPath
     */
    public void projectClosedPath(GeoPath geoPath) {
        
        double lon0 = projection.getProjectionLongitude();
        double lonMin = Math.toDegrees(MapMath.normalizeLongitude(lon0 - Math.PI));
        double latMax = projection.getMaxLatitudeDegrees();
        double latMin = projection.getMinLatitudeDegrees();
        Rectangle2D bounds = new Rectangle2D.Double(lonMin, latMin, 360, -latMin + latMax);

        GeoPathModel projPath = new GeoPathModel();
        Area maskArea = rectToIntersectingArea(geoPath, bounds);
        if (maskArea == null) {
            projectArea(geoPath, projPath, false, latMin, latMax);
        } else {
            projectArea(cutInner(geoPath, maskArea, lon0), projPath, false, latMin, latMax);
            projectArea(cutOutter(geoPath, maskArea, lon0), projPath, true, latMin, latMax);
        }
        geoPath.setPathModel(projPath);
        
    }

    /**
     * Cuts a GeoPath with a masking area. Returns the part of the path that is
     * inside the masking area.
     * @param geoPath The path to intersect.
     * @param maskArea The masking area.
     * @param lon0 The central longitude of the projection.
     * @return A path with clipped with the masking area.
     */
    private GeoPath cutInner(GeoPath geoPath, Area maskArea, double lon0) {

        Area innerArea = geoPath.toArea();
        if (lon0 >= 0d)
            innerArea.intersect(maskArea);
        else
            innerArea.subtract(maskArea);
        GeoPath path = new GeoPath();
        path.append(innerArea, false);
        return path;

    }

    /**
     * Cuts a GeoPath with a masking area. Returns the part of the path that is
     * outside of the masking area.
     * @param geoPath The path to intersect.
     * @param maskArea The masking area.
     * @param lon0 The central longitude of the projection.
     * @return A path with clipped with the masking area.
     */
    private GeoPath cutOutter(GeoPath geoPath, Area maskArea, double lon0) {

        Area outterArea = geoPath.toArea();
        if (lon0 >= 0d)
            outterArea.subtract(maskArea);
        else
            outterArea.intersect(maskArea);
        GeoPath path = new GeoPath();
        path.append(outterArea, false);
        return path;

    }

    /**
     * Returns true if two numbers are close.
     * @param a
     * @param b
     * @return
     */
    private boolean closeNumbers (double a, double b) {
        final double EPS = 1e-6;
        return Math.abs(a - b) < EPS;
    }
    
    /**
     * Projects an area. The area must not intersect the boundaries of the 
     * graticule. Currently the area is stored in a GeoPath. An awt.Area
     * would be more efficient, as the conversion from an Area to a GeoPath would
     * not be necessary.
     * @param srcPath The area to project.
     * @param dstPath The path that will receive the projected coordinates.
     * @param lon0Deg The central longitud of the projection.
     * @param outOfGraticule True if the area is outside the boundaries of the
     * graticule, false otherwise.
     */
    private void projectArea(GeoPath srcPath, GeoPathModel dstPath,
            boolean outOfGraticule, double latMin, double latMax) {

        if (srcPath == null || dstPath == null) {
            return;
        }
        if (srcPath.getDrawingInstructionCount() < 2) {
            return;
        }
        
        double lon0Deg = projection.getProjectionLongitudeDegrees();
        GeoPathIterator iterator = srcPath.getIterator();
        double lon = Double.NaN;
        double lat = Double.NaN;
        double lastMoveToLon = iterator.getX();
        double lastMoveToLat = iterator.getY();
        double prevLon = lastMoveToLon;
        double prevLat = lastMoveToLat;
        
        Rectangle2D bounds = srcPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        boolean onRightEdge = closeNumbers(bounds.getMaxX(), lon0Deg == -180 ? 360 : lon0Deg - 180);
        boolean onLeftEdge = closeNumbers(bounds.getMinX(), lon0Deg == 180 ? 0 : lon0Deg + 180);
        
        do {
            final int inst = iterator.getInstruction();
            if (inst == GeoPathModel.MOVETO || inst == GeoPathModel.LINETO) {
                lon = iterator.getX();
                lat = iterator.getY();
            }
     
            // adjust longitude on left border of graticule
            if (closeNumbers(lon, lon0Deg + 180)) {
                if (outOfGraticule || onLeftEdge) {
                    lon -= 360;
                }
                addIntermediatePointsAlongCurves = closeNumbers(lon, prevLon);
            }
            // adjust latitude on right border of graticule
            else if (closeNumbers(lon, lon0Deg - 180)) {
                if (outOfGraticule || onRightEdge) {
                    lon += 360;
                }
                addIntermediatePointsAlongCurves = closeNumbers(lon, prevLon);
            } else {
                addIntermediatePointsAlongCurves = false;
            }
            
            // this is not sufficient. Should test with projected boundaries of graticule !!! ??? FIXME
            if (closeNumbers(lat, latMin) || closeNumbers(lat, latMax)) {
                addIntermediatePointsAlongCurves = true;
            }

            // do not project polygons that are too far north or south
            if (outOfGraticule && (lat > latMax || lat < latMin)) {
                return;
            }
                        
            //lon = MapMath.normalizeLongitude(lon - lon0Deg);
            /*if (outOfGraticule) {
                if (lon < lon0Deg + 180) {
                    lon = lon0Deg + 180;sdfsdf
                }
            } else {
                if (lon < lon0Deg - 180) {
                    lon = lon0Deg - 180;
                } else if (lon > lon0Deg +180) {
                    lon = lon0Deg +180;
                }
            }
            */
            switch (inst) {
                case GeoPathModel.CLOSE:
                    if (addIntermediatePointsAlongCurves) {
                        lineTo(prevLon, prevLat, lastMoveToLon, lastMoveToLat, dstPath);
                    }
                    dstPath.closePath();
                    prevLon = lon;
                    prevLat = lat;
                    break;

                case GeoPathModel.MOVETO:
                    projectMoveTo(lon, lat, dstPath);
                    prevLon = lastMoveToLon = lon;
                    prevLat = lastMoveToLat = lat;
                    break;

                case GeoPathModel.LINETO:
                    if (!closeNumbers(lon, prevLon) || !closeNumbers(lat, prevLat)) {
                        lineTo(prevLon, prevLat, lon, lat, dstPath);
                        prevLon = lon;
                        prevLat = lat;
                    }
                    break;
            }
            
        } while (iterator.next());

        // remove last command if it was a moveto command.
        if (dstPath.getLastInstruction() == GeoPathModel.MOVETO) {
            dstPath.removeLastInstruction();
        }
    }

    
    /**
     * Converts a rectangle to a java.awt.geom.Area, which can be used to compute
     * intersections.
     * @param geoPath The path to intersect.
     * @param rect Only return an Area when the geoPath intersects this rectangle.
     * @return Null if there is not intersection between geoPath and rect,
     * otherwise the geoPath converted to an Area.
     */
    private Area rectToIntersectingArea(GeoPath geoPath, Rectangle2D rect) {

        final Rectangle2D bounds = geoPath.getBounds2D(GeoObject.UNDEFINED_SCALE);
        if (GeometryUtils.rectanglesIntersect(rect, bounds) == false) {
            return null;
        }
        return new Area(rect);

    }
}
