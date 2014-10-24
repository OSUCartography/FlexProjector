/*
 * GeometryUtils.java
 *
 * Created on April 20, 2005, 5:51 PM
 */
package ika.utils;

import java.awt.geom.*;
import java.util.*;

/**
 * A variety of utilities for computational geometry.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeometryUtils {

    /**
     * Square distance between a point and a line defined by two other points.
     * See http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
     * @param x0 The point not on the line.
     * @param y0 The point not on the line.
     * @param x1 A point on the line.
     * @param y1 A point on the line.
     * @param x2 Another point on the line.
     * @param y2 Another point on the line.
     */
    public static double pointLineDistanceSquare(
            double x0, double y0,
            double x1, double y1,
            double x2, double y2) {

        final double x2_x1 = x2 - x1;
        final double y2_y1 = y2 - y1;

        final double d = (x2_x1) * (y1 - y0) - (x1 - x0) * (y2_y1);
        final double denominator = x2_x1 * x2_x1 + y2_y1 * y2_y1;
        return d * d / denominator;
    }

    /**
     * Square distance between a point and a line defined by two other points.
     * See http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
     * @param p0 The point not on the line.
     * @param p1 A point on the line.
     * @param p2 Another point on the line.
     */
    public static double pointLineDistanceSquare(Point2D p0, Point2D p1, Point2D p2) {
        return pointLineDistanceSquare(p0.getX(), p0.getY(),
                p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * Returns the distance of p3 to the segment defined by p1,p2.
     * http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
     * Wrapper function for distanceToSegment.
     * @param x3
     * @param y3
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distanceToSegment(double x3, double y3, double x1, double y1, double x2, double y2) {
        final Point2D p3 = new Point2D.Double(x3, y3);
        final Point2D p1 = new Point2D.Double(x1, y1);
        final Point2D p2 = new Point2D.Double(x2, y2);
        return distanceToSegment(p1, p2, p3);
    }

    /**
     * Returns the distance of p3 to the segment defined by p1,p2.
     * http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/
     * @param p1 First point of the segment
     * @param p2 Second point of the segment
     * @param p3  Point to which we want to know the distance of the segment
     * defined by p1,p2
     * @return The distance of p3 to the segment defined by p1,p2
     */
    public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {

        final double xDelta = p2.getX() - p1.getX();
        final double yDelta = p2.getY() - p1.getY();

        if ((xDelta == 0) && (yDelta == 0)) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }

        final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        final Point2D closestPoint;
        if (u < 0) {
            closestPoint = p1;
        } else if (u > 1) {
            closestPoint = p2;
        } else {
            closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
        }

        return closestPoint.distance(p3);
    }

    /**
     * Test if four points lay on one line.
     */
    public static boolean isStraightLine(double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double x4, double y4,
            double straightLineTolerance) {

        // length of segment
        double len = Math.sqrt((x4 - x1) * (x4 - x1) + (y4 - y1) * (y4 - y1));
        if (len == 0.) {
            return true; // avoid endless recursion
        }

        double e = straightLineTolerance * len;

        /* common part of area calculation */
        double q = (x1 - x4) * (y1 + y4);

        /* twice the area between p1, p2, and p4 */
        double f = (x2 - x1) * (y1 + y2) + (x4 - x2) * (y2 + y4) + q;

        /* distance from point p2 to line p0_p1 is area f devided by length of line p0_p1 */
        /* instead of distance compare area */
        if (Math.abs(f) < e) {
            /* same procedure with p3 */
            f = (x3 - x1) * (y1 + y3) + (x4 - x3) * (y3 + y4) + q;
            if (Math.abs(f) < e) {
                return true;
            }
        }
        return false;

    }

    /**
     * Test if two rectangles intersect. <br>
     * This test differs from Rectangle2D.intersects()in that it returns also true
     * if one of the rectangles has an height or a width that equals 0.
     * @param r1 Rectangle 1
     * @param r2 Rectangle 2.
     * @return true if the two rectangles intersect, false if the rectangles do
     * not intersect or if they are identical.
     */
    public static boolean rectanglesIntersect(Rectangle2D r1, Rectangle2D r2) {
        if (r1 == null || r2 == null) {
            return false;
        }

        final double xmin1 = r1.getMinX();
        final double xmax1 = r1.getMaxX();
        final double xmin2 = r2.getMinX();
        final double xmax2 = r2.getMaxX();

        final double xmin = xmin1 > xmin2 ? xmin1 : xmin2;
        final double xmax = xmax1 < xmax2 ? xmax1 : xmax2;
        if (xmin > xmax) {
            return false;
        }

        final double ymin1 = r1.getMinY();
        final double ymax1 = r1.getMaxY();
        final double ymin2 = r2.getMinY();
        final double ymax2 = r2.getMaxY();

        // test if rectangles are identical
        if (ymin1 == ymin2 && xmin1 == xmin2) {
            return false;
        }

        final double ymin = ymin1 > ymin2 ? ymin1 : ymin2;
        final double ymax = ymax1 < ymax2 ? ymax1 : ymax2;
        return (ymin <= ymax);
    }

    /**
     * Tests if a line intersects with a rectangle. This method differs from
     * Rectangle2D in that it accepts empty rectangles, i.e. the passed
     * rectangle can have a height or a width of 0. Returns false if the
     * line lies completely within the rectangle.
     * @param x0 Start point of the line. Horizontal coordinate.
     * @param y0 Start point of the line. Vertical coordinate.
     * @param x1 End point of the line. Horizontal coordinate.
     * @param y1 End point of the line. Vertical coordinate.
     * @param r The rectangle to test. Can be empty, i.e. the height or width
     * can be 0.
     * @return True if the line intersects with any of the four border lines
     * of the rectangle, false otherwise.
     */
    public static boolean lineIntersectsRectangle(double x0, double y0,
            double x1, double y1, Rectangle2D r) {

        // use the Rectangle2D.intersectsLine() method.
        boolean intersection = r.intersectsLine(x0, y0, x1, y1);
        if (intersection) {
            return true;
        }

        // Rectangle2D.intersectsLine returns false when the rectangle is empty,
        // i.e. its width or height is zero.
        if (r.isEmpty()) {
            final double minX = r.getMinX();
            final double minY = r.getMinY();
            final double maxX = r.getMaxX();
            final double maxY = r.getMaxY();

            // test for intersection with lower horizontal line
            if (linesIntersect(x0, y0, x1, y1, minX, minY, maxX, minY) > 0
                    // test for intersection with right vertical line
                    || linesIntersect(x0, y0, x1, y1, maxX, minY, maxX, maxY) > 0
                    // test for intersection with upper horizontal line
                    || linesIntersect(x0, y0, x1, y1, minX, maxY, maxX, maxY) > 0
                    // test for intersection with left vertical line
                    || linesIntersect(x0, y0, x1, y1, minX, minY, minX, maxY) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enlarges the passed rectangle by d in all directions.
     * @param rect The rectangle to enlarge.
     * @param d The enlargement to apply.
     */
    public static void enlargeRectangle(Rectangle2D rect, double d) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }

        rect.setRect(rect.getX() - d, rect.getY() - d,
                rect.getWidth() + 2 * d, rect.getHeight() + 2 * d);
    }

    /**
     * Tests whether a passed rectangle has valid coordinates, i.e. x, y, width
     * and height are not NaN and not infinite.
     * @param rect The rectangle to test. Can be null.
     * @return True if the passed rectangle is valid, false otherwise.
     */
    public static boolean isRectangleValid(Rectangle2D rect) {

        if (rect == null) {
            return false;
        }

        final double x = rect.getX();
        final double y = rect.getY();
        final double w = rect.getWidth();
        final double h = rect.getHeight();
        return !Double.isInfinite(x) && !Double.isNaN(x)
                && !Double.isInfinite(y) && !Double.isNaN(y)
                && !Double.isInfinite(w) && !Double.isNaN(w)
                && !Double.isInfinite(h) && !Double.isNaN(h);

    }

    public static double length(double x1, double y1, double x2, double y2) {
        final double dx1 = x1 - x2;
        final double dy1 = y1 - y2;
        return Math.sqrt(dx1 * dx1 + dy1 * dy1);
    }

    /**
     * Computes the angle between two straight lines defined by three points
     * Line 1: p1-p2 and line 2: p2-p3.
     * @return The angle between the two lines in radian between -pi and +pi.
     */
    public static double angle(double x1, double y1,
            double x2, double y2,
            double x3, double y3) {
        final double dx1 = x1 - x2;
        final double dy1 = y1 - y2;
        final double dx2 = x3 - x2;
        final double dy2 = y3 - y2;

        final double ang1 = Math.atan2(dx1, dy1);
        final double ang2 = Math.atan2(dx2, dy2);
        final double angle = ang2 - ang1;

        return GeometryUtils.trimAngle(angle);
    }

    // returns +1 if > 0, -1 if < 0, 0 if == 0
    private static int SIGNTEST(double a) {
        return ((a) > 0.) ? 1 : ((a) < 0.) ? -1 : 0;
    }

    /**
     * Check if two lines intersect.
     * Line definition :
     * Line 1  (x0,y0)-(x1,y1)
     * Line 2  (x2,y2)-(x3,y3)
     * The return values depend on the intersection type:
     * 0: no intersection
     * 1: legal intersection
     * 2: point on line
     * 3: point on point
     * 4: line on line
     * @param x0 Start point of line 1. Horizontal coordinate.
     * @param y0 Start point of line 1. Vertical coordinate.
     * @param x1 End point of line 1. Horizontal coordinate.
     * @param y1 End point of line 1. Vertical coordinate.
     * @param x2 Start point of line 2. Horizontal coordinate.
     * @param y2 Start point of line 2. Vertical coordinate.
     * @param x3 End point of line 2. Horizontal coordinate.
     * @param y4 End point of line 2. Vertical coordinate.
     * @return 0 if no intersection; 1 if intersection; 2 if point on line;
     * 3 if point on point; 4 if line on line.
     */
    public static int linesIntersect(double x0, double y0, double x1, double y1,
            double x2, double y2, double x3, double y3) {

        int k03_01, k01_02, k20_23, k23_21;
        int pos, neg, nul;

        k03_01 = SIGNTEST((x3 - x0) * (y1 - y0) - (y3 - y0) * (x1 - x0));
        k01_02 = SIGNTEST((x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0));
        k20_23 = SIGNTEST((x0 - x2) * (y3 - y2) - (y0 - y2) * (x3 - x2));
        k23_21 = SIGNTEST((x3 - x2) * (y1 - y2) - (y3 - y2) * (x1 - x2));

        pos = neg = nul = 0;

        if (k03_01 < 0) {
            neg++;
        } else if (k03_01 > 0) {
            pos++;
        } else {
            nul++;
        }

        if (k01_02 < 0) {
            neg++;
        } else if (k01_02 > 0) {
            pos++;
        } else {
            nul++;
        }

        if (k20_23 < 0) {
            neg++;
        } else if (k20_23 > 0) {
            pos++;
        } else {
            nul++;
        }

        if (k23_21 < 0) {
            neg++;
        } else if (k23_21 > 0) {
            pos++;
        } else {
            nul++;
        }

        if (nul == 0) {
            if (neg == 4 || pos == 4) {
                return 1;
            } // legal intersection
            else {
                return 0;
            }		// no intersection
        } else {
            if (neg == 3 || pos == 3) {
                return 2;
            } // point on line
            else if (neg == 2 || pos == 2) {
                return 3;
            } // point on point
            else {
                return 4;
            }		// line on line
        }
    }

    // see http://astronomy.swin.edu.au/~pbourke/geometry/lineline2d/
    // If an intersection point exists, it returns the intersection point and
    // the parameter m of p = p1 + m(p2-p1)
    // if the two line segments touch on an end point, an intersection is returned.
    public static double[] intersectLineSegments(double x1, double y1,
            double x2, double y2, double x3, double y3, double x4, double y4) {

        final double denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denominator == 0.d) {
            return null; // lines are parallel
        }

        final double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
        if (ua <= 0.d || ua >= 1.d) {
            return null; // no intersection
        }

        final double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator;
        if (ub <= 0.d || ub >= 1.d) {
            return null; // no intersection
        }

        return new double[]{x1 + ua * (x2 - x1), y1 + ua * (y2 - y1), ua};
    }

    static private class DistComp implements java.util.Comparator {

        public int compare(Object obj1, Object obj2) {
            Double a = new Double(((double[]) obj1)[2]);
            Double b = new Double(((double[]) obj2)[2]);
            return a.compareTo(b);
        }
    }

    // returns a set of intersection points
    public static double[][] intersectLineSegmentWithPolygon(double x1, double y1,
            double x2, double y2, double[][] polygon) {

        java.util.List intersectionList = new java.util.LinkedList();

        // intersect the line segment with each side of the polygon
        for (int i = 1; i < polygon.length; i++) {
            double[] intersection = GeometryUtils.intersectLineSegments(
                    x1, y1, x2, y2,
                    polygon[i - 1][0], polygon[i - 1][1], polygon[i][0], polygon[i][1]);
            if (intersection != null) {
                intersectionList.add(intersection);
            }
        }

        if (intersectionList.isEmpty()) {
            return null;
        }

        // order the intersection points by increasing distance from the start point
        java.util.Collections.sort(intersectionList, new DistComp());

        // copy the coordinates of the intersection points
        double[][] intersections = new double[intersectionList.size()][2];
        for (int i = 0; i < intersections.length; ++i) {
            final double[] inter = (double[]) intersectionList.get(i);
            intersections[i][0] = inter[0];
            intersections[i][1] = inter[1];
        }
        return intersections;
    }

    // see http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/
    /*
    The code below is from Wm. Randolph Franklin <wrf@ecse.rpi.edu>
    (see URL below) with some minor modifications for speed.  It returns
    true for strictly interior points, false for strictly exterior, and false or true
    for points on the boundary.  The boundary behavior is complex but
    determined; in particular, for a partition of a region into polygons,
    each point is "in" exactly one polygon.
    (See p.243 of [O'Rourke (C)] for a discussion of boundary behavior.)
     */
    public static boolean pointInPolygon(double[] point, double[][] polygon) {
        int i, j;
        boolean c = false;
        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((((polygon[i][1] <= point[1]) && (point[1] < polygon[j][1]))
                    || ((polygon[j][1] <= point[1]) && (point[1] < polygon[i][1])))
                    && (point[0] < (polygon[j][0] - polygon[i][0])
                    * (point[1] - polygon[i][1]) / (polygon[j][1] - polygon[i][1]) + polygon[i][0])) {
                c = !c;
            }
        }
        return c;
    }

    /* Same as public static boolean pointInPolygon(double[] point, double[][] polygon)
     * but with x and y as doubles */
    public static boolean pointInPolygon(double x, double y, double[][] polygon) {
        int i, j;
        boolean c = false;
        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((((polygon[i][1] <= y) && (y < polygon[j][1]))
                    || ((polygon[j][1] <= y) && (y < polygon[i][1])))
                    && (x < (polygon[j][0] - polygon[i][0])
                    * (y - polygon[i][1]) / (polygon[j][1] - polygon[i][1]) + polygon[i][0])) {
                c = !c;
            }
        }
        return c;
    }

    /*
    0	: point outside polygon
    1	: point inside polygon
    2	: point on polygon border
     */
    public static int pointInPolygonOrOnBoundary(double[] point, double[][] polygon) {

        int nbrOfPoints = polygon.length;
        if (nbrOfPoints < 3) {
            return 0;
        }

        // find bounding box
        double w = Double.MAX_VALUE;
        double e = Double.MIN_VALUE;
        double s = Double.MAX_VALUE;
        double n = Double.MIN_VALUE;
        for (int i = 0; i < nbrOfPoints; i++) {
            if (polygon[i][0] < w) {
                w = polygon[i][0];
            }
            if (polygon[i][0] > e) {
                e = polygon[i][0];
            }
            if (polygon[i][0] < s) {
                s = polygon[i][1];
            }
            if (polygon[i][0] > n) {
                n = polygon[i][1];
            }
        }

        // test against bounding box
        if (point[0] < w || point[0] > e || point[1] < s || point[1] > n) {
            return 0;
        }

        /* 	Even-odd rule:
        Draw a line from the passed point to a point outside the path. Count the number
        of path segments that the line crosses. If the result is odd, the point is inside the
        path. If the result is even, the point is outside the path.
         */
        double outY = n + 1000.;
        int nbrIntersections = 0;

        int lastPointID = nbrOfPoints - 1;
        for (int i = 0; i < lastPointID; i++) {
            // test intersection with a vertical line starting at the point to test
            int intersection = GeometryUtils.linesIntersect(
                    point[0], point[1], point[0], outY,
                    polygon[i][0], polygon[i][1], polygon[i + 1][0], polygon[i + 1][1]);
            /* GeometryUtils.linesIntersect may return the following values:
            0	: no intersection
            1	: legal intersection
            2	: point on line
            3	: point on point
            4	: line on line */

            if (intersection > 1) {
                return 2; // the point to test is laying on a line: return true
            }
            if (intersection == 1) {
                nbrIntersections++; // found a normal intersection
            }
        }
        return nbrIntersections % 2;
    }

    public static Vector clipPolylineWithPolygon(double[][] polyline,
            double[][] polygon) {

        // make sure the polygon is closed
        if (polygon[0][0] != polygon[polygon.length - 1][0]
                || polygon[0][1] != polygon[polygon.length - 1][1]) {
            throw new IllegalArgumentException("polygon not closed");
        }

        // the polyline with additional intersection points
        Vector points = new Vector();

        // add start point of polyline
        points.add(polyline[0]);

        // get all intersection points
        for (int i = 1; i < polyline.length; ++i) {
            double[][] intersections = GeometryUtils.intersectLineSegmentWithPolygon(
                    polyline[i - 1][0], polyline[i - 1][1], polyline[i][0], polyline[i][1],
                    polygon);

            // add intersection points
            if (intersections != null) {
                for (int j = 0; j < intersections.length; ++j) {
                    points.add(intersections[j]);
                }
            }

            // add end point of line segment
            points.add(polyline[i]);
        }

        // return the lines in this vector
        Vector lines = new Vector();

        // store each line in a new vector
        Vector line = new Vector();

        // counter for all points
        int ptID = 0;

        // remember whether the last point was added to the line
        boolean addedLastPoint = false;

        // loop over all lines
        while (ptID < points.size() - 1) {

            // compute the middle point for each line segment
            final double[] pt1 = (double[]) points.get(ptID);
            final double[] pt2 = (double[]) points.get(ptID + 1);
            final double x = (pt1[0] + pt2[0]) / 2.d;
            final double y = (pt1[1] + pt2[1]) / 2.d;

            // test if the middle point is inside the polygon
            // the middle point is never on the border of the polygon, so no
            // special treatement for this case is needed.
            final boolean in = GeometryUtils.pointInPolygon(new double[]{x, y}, polygon);

            // the middle point is inside the polygon, so add the start point of
            // the current segment to the line
            if (in) {
                line.add(points.get(ptID));
                addedLastPoint = true;
            } // the middle point is outside the polygon. Check if the start point
            // of the current line segment has to be added anyway. This is the
            // case when this is the first line segment outside the polygon.
            else if (addedLastPoint) {
                line.add(points.get(ptID));

                // store the old line and create a new one
                if (line.size() > 1) {
                    lines.add(line);
                }
                line = new Vector();

                addedLastPoint = false;
            }
            ptID++;
        }

        // the last line segment needs special treatment
        if (addedLastPoint) {
            line.add(points.get(points.size() - 1));
        }
        if (line.size() > 1) {
            lines.add(line);
        }

        // convert the Vectors to arrays
        for (int i = 0; i < lines.size(); i++) {
            Vector l = (Vector) lines.get(i);

            // only treat lines with 2 or more points (just to be sure)
            if (l.size() < 2) {
                continue;
            }

            double[][] a = new double[l.size()][2];
            for (int j = 0; j < a.length; ++j) {
                a[j] = (double[]) l.get(j);
            }
            lines.set(i, a);
        }
        return lines;
    }

    /** Compute the difference between two angles.
     * The resulting angle is in the range of -pi..+pi if the input angle are
     * also in this range.
     */
    public static float angleDif(float a1, float a2) {
        double val = a1 - a2;
        if (val > Math.PI) {
            val -= 2. * Math.PI;
        }
        if (val < -Math.PI) {
            val += 2. * Math.PI;
        }
        return (float) val;
    }

    /** Compute the difference between two angles.
     * The resulting angle is in the range of -pi..+pi if the input angle are
     * also in this range.
     */
    public static double angleDif(double a1, double a2) {
        double val = a1 - a2;
        if (val > Math.PI) {
            val -= 2. * Math.PI;
        }
        if (val < -Math.PI) {
            val += 2. * Math.PI;
        }
        return val;
    }

    /** Sum of two angles.
     * The resulting angle is in the range of -pi..+pi if the input angle are
     * also in this range.
     */
    public static float angleSum(float a1, float a2) {
        double val = a1 + a2;
        if (val > Math.PI) {
            val -= 2. * Math.PI;
        }
        if (val < -Math.PI) {
            val += 2. * Math.PI;
        }
        return (float) val;
    }

    public static double trimAngle(double angle) {
        if (angle > Math.PI) {
            return angle - 2. * Math.PI;
        }
        if (angle < -Math.PI) {
            return angle + 2. * Math.PI;
        }
        return angle;
    }
}
