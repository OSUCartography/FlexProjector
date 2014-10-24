package ika.geo;

import java.awt.geom.*;
import java.awt.*;
import java.io.*;
import ika.utils.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * GeoPath - a class that models vector data. It can treat straight lines and
 * bezier curves.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoPath extends GeoObject implements Serializable, Cloneable {

    static public GeoPath newRect(Rectangle2D bounds) {
        GeoPath geoPath = new GeoPath();
        geoPath.rectangle(bounds);
        return geoPath;
    }

    static public GeoPath newRect(double west, double south, double w, double h) {
        GeoPath geoPath = new GeoPath();
        geoPath.moveTo(west, south);
        geoPath.lineTo(west + w, south);
        geoPath.lineTo(west + w, south + h);
        geoPath.lineTo(west, south + h);
        geoPath.closePath();
        return geoPath;
    }

    static public GeoPath newCircle(double cx, double cy, double r) {
        GeoPath geoPath = new GeoPath();
        geoPath.circle(cx, cy, r);
        return geoPath;
    }

    static public GeoPath newSquare(double cx, double cy, double d) {
        GeoPath geoPath = new GeoPath();
        geoPath.square(cx, cy, d);
        return geoPath;
    }

    /**
     * Build a bézier GeoPath that approximates an arc of an ellipsoid.
     * @param fromAngle counted in clock wise direction from north.
     * @param arcAngle counted in clock wise direction from north.
     */
    public static GeoPath newArc(double cx, double cy, double rx, double ry,
            double fromAngle, double arcAngle) {

        // arcAngle must be larger than kMinAngle.
        final double kMinAngle = 0.0001;

        // a segment of a path may not be larger than pi/4
        final double kMaxSegmentAngle = Math.PI / 4;

        double phi;		// half angle of current segment
        double remainingArc;	// rest of arc to convert into segments
        double arcSign;	// -1. or +1. Indicates direction of arcAngle.
        GeoPath path = null;	// new path.


        // arcAngle must have certain size.
        if (Math.abs(arcAngle) < kMinAngle) {
            return null;
        }

        // arcAngle may not be larger than 2*PI
        if (Math.abs(arcAngle) > Math.PI * 2) {
            arcAngle = Math.PI * 2;
        }

        arcSign = (arcAngle > 0.) ? 1. : -1.;
        remainingArc = arcAngle;
        path = new GeoPath();

        // split the arc and construct each segment
        final int nbrOfSegments = (int) Math.ceil(Math.abs(arcAngle / kMaxSegmentAngle));
        for (int i = 0; i < nbrOfSegments; i++) {
            if (Math.abs(remainingArc) > kMaxSegmentAngle) {
                phi = arcSign * kMaxSegmentAngle * 0.5;
                remainingArc -= arcSign * kMaxSegmentAngle;
            } else {
                phi = 0.5 * remainingArc;
            }
            final double cosPhi = Math.cos(phi);
            final double sinPhi = Math.sin(phi);
            final double c1x = (4. - cosPhi) / 3.;
            final double c1y = (1. - cosPhi) * (cosPhi - 3.) / (3. * sinPhi);

            // arc around x axis with radius = 1 at center x = 0 / y = 0.
            if (i == 0) {
                path.moveTo(cosPhi, sinPhi);
            }
            path.curveTo(c1x, -c1y, c1x, c1y, cosPhi, -sinPhi);

            // rotate arc against the direction of arcAngle to add next segment at end of the path.
            if (i < nbrOfSegments - 2) {
                path.rotate(arcSign * kMaxSegmentAngle);
            } else if (i == nbrOfSegments - 2) {
                path.rotate(arcSign * (0.5 * kMaxSegmentAngle + Math.abs(0.5 * remainingArc)));
            }
        }

        // rotate finished arc
        double finalRot = -arcSign * (nbrOfSegments - 1) * kMaxSegmentAngle - 0.5 * arcSign * remainingArc - fromAngle + Math.PI * 0.5;
        path.rotate(finalRot);

        // scale from 1 to desired radius in x and y direction.
        path.scale(rx, ry);

        // center on cx, cy.
        path.move(cx, cy);

        return path;
    }
    private static final long serialVersionUID = 7350986432785586245L;
    /**
     * The geometry of this GeoPath.
     */
    private GeoPathModel path;
    /**
     * A VectorSymbol that stores the graphic attributes of this GeoPath.
     */
    private VectorSymbol symbol;

    /** Creates a new instance of GeoPath */
    public GeoPath() {
        this.path = new GeoPathModel();
        this.symbol = new VectorSymbol();
    }

    protected GeoPath(GeoPath geoPath) {
        this.path = geoPath.path;
        this.symbol = geoPath.symbol;
    }

    @Override
    public GeoPath clone() {
        try {
            GeoPath geoPath = (GeoPath) super.clone();

            // make deep clone of the VectorSymbol and the path
            geoPath.symbol = (VectorSymbol) this.symbol.clone();
            geoPath.path = (GeoPathModel) this.path.clone();

            return geoPath;
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Append a move-to command to the current path. Places the virtual pen at the
     * specified location without drawing any line.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param x The location to move to.
     * @param y The location to move to.
     */
    public void moveTo(double x, double y) {
        path.moveTo(x, y);
    }

    /**
     * Append a move-to command to the current path. Places the virtual pen at the
     * specified location without drawing any line.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param xy An array containing the x and the y coordinate.
     */
    public void moveTo(double[] xy) {
        path.moveTo(xy[0], xy[1]);
    }

    /**
     * Append a move-to command to the current path. Places the virtual pen at the
     * specified location without drawing any line.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param point A point containing the x and the y coordinate.
     */
    public void moveTo(Point2D point) {
        path.moveTo(point.getX(), point.getY());
    }

    /**
     * Draws a line from the current location of the pen to the specified location. Before
     * calling lineTo, moveTo must be called. Alternatively, use moveOrLineTo that makes
     * sure moveTo is called before lineTo (or quadTo, resp. curveTo).
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param x The end point of the new line segment.
     * @param y The end point of the new line segment.
     */
    public void lineTo(double x, double y) {
        path.lineTo(x, y);
    }

    /**
     * Draws a line from the current location of the pen to the specified location. Before
     * calling lineTo, moveTo must be called. Alternatively, use moveOrLineTo that makes
     * sure moveTo is called before lineTo (or quadTo, resp. curveTo).
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param xy An array containing the x and the y coordinate.
     */
    public void lineTo(double[] xy) {
        path.lineTo(xy[0], xy[1]);
    }

    /**
     * Draws a line from the current location of the pen to the specified location. Before
     * calling lineTo, moveTo must be called. Alternatively, use moveOrLineTo that makes
     * sure moveTo is called before lineTo (or quadTo, resp. curveTo).
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param point A point containing the x and the y coordinate.
     */
    public void lineTo(Point2D point) {
        path.lineTo(point.getX(), point.getY());
    }

    /**
     * Moves the virtual pen to the specified location if this is the first call that
     * changes the geometry. If this is not the first geometry changing call, a straight
     * line is drawn to the specified location.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param x The end point of the new line segment, or the location to move to.
     * @param y The end point of the new line segment, or the location to move to.
     */
    public void moveOrLineTo(double x, double y) {
        if (hasOneOrMorePoints()) {
            path.lineTo(x, y);
        } else {
            path.moveTo(x, y);
        }
    }

    /**
     * Appends a quadratic bezier curve to this GeoPath.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param x1 The location of the control point that is not on the curve.
     * @param y1 The location of the control point that is not on the curve.
     * @param x2 The location of the end point of the new curve segment.
     * @param y2 The location of the control point that is not on the curve.
     */
    public void quadTo(double x1, double y1, double x2, double y2) {
        path.quadTo(x1, y1, x2, y2);
    }

    /**
     * Appends a cubic bezier curve to this GeoPath.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param x1 The location of the first control point that is not on the curve.
     * @param y1 The location of the first control point that is not on the curve.
     * @param x2 The location of the second control point that is not on the curve.
     * @param y2 The location of the second control point that is not on the curve.
     * @param x3 The location of the end point of the new curve segment.
     * @param y3 The location of the end point of the new curve segment.
     */
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        path.curveTo(x1, y1, x2, y2, x3, y3);
    }

    /**
     * Appends a cubic bezier curve to this GeoPath.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     * @param ctrl1 The location of the first control point that is not on the curve.
     * @param ctrl2 The location of the second control point that is not on the curve.
     * @param end The location of the end point of the new curve segment.
     */
    public void curveTo(Point2D ctrl1, Point2D ctrl2, Point2D end) {
        final double ctrl1x = ctrl1.getX();
        final double ctrl1y = ctrl1.getY();
        final double ctrl2x = ctrl2.getX();
        final double ctrl2y = ctrl2.getY();
        final double endx = end.getX();
        final double endy = end.getY();

        path.curveTo(ctrl1x, ctrl1y, ctrl2x, ctrl2y, endx, endy);
    }

    /**
     * Closes the path by connecting the last point with the first point using a
     * straight line.
     * <B>Important: A call to this method does not generate a MapEvent!</B>
     */
    public void closePath() {
        path.closePath();
    }

    /**
     * Returns true if any of the possible sub-paths is closed.
     * @return True if the path is closed.
     */
    public boolean isClosed() {
        return path.isClosed();
    }

    /**
     * Returns true if this GeoPath consists of more than one line or polygon.
     * @return True if this is a compound path.
     */
    public boolean isCompound() {
        return this.path.isCompound();
    }

    /**
     * Returns the number of compound sub-paths.
     * @return The number of sub-paths. Returns 0 if this path does not contain
     * any instruction.
     */
    public int getCompoundCount() {
        return this.path.getCompoundCount();
    }

    /**
     * Constructs a path from a series of points that will be connected by straight
     * lines.
     * @param points The points to connect.
     */
    public void straightLines(Point2D[] points) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            path.reset();
            if (points.length >= 1) {
                path.moveTo(points[0].getX(), points[0].getY());
                for (int i = 1; i < points.length; i++) {
                    path.lineTo(points[i].getX(), points[i].getY());
                }
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Constructs a path from a series of points that will be connected by straight
     * lines.
     * @param points The points to connect.
     * @param firstPoint The id of the first point in the array.
     * @nbrPoints The number of point to use.
     */
    public void straightLines(double[][] points, int firstPoint, int nbrPoints) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            path.reset();
            if (points.length >= 1) {
                final int lastPoint = firstPoint + nbrPoints;
                path.moveTo(points[firstPoint][0], points[firstPoint][1]);
                for (int i = firstPoint + 1; i < lastPoint; i++) {
                    path.lineTo(points[i][0], points[i][1]);
                }
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Constructs a path from a series of points that will be connected by straight
     * lines.
     * @param param points The points to connect.
     */
    public void straightLines(double[] points) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            path.reset();
            if (points.length >= 1) {
                path.moveTo(points[0], points[1]);
                for (int i = 1; i < points.length / 2; i++) {
                    path.lineTo(points[i * 2], points[i * 2 + 1]);
                }
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Constructs a bezier control point for two straight lines that meet in a point.
     * The control point lies in backward direction from point 1  towards point 0.
     */
    private void bezierPoint(
            double p0x, double p0y,
            double p1x, double p1y,
            double p2x, double p2y,
            double[] controlPoint,
            double smoothness) {

        final double F = 0.39;

        // length of the line connecting the previous point P0 with the current
        // point P1.
        final double length = GeometryUtils.length(p1x, p1y, p2x, p2y);

        // unary vector from P1 to P0.
        double dx1 = p0x - p1x;
        double dy1 = p0y - p1y;
        final double l1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
        dx1 /= l1;
        dy1 /= l1;
        if (Double.isNaN(dx1) || Double.isNaN(dy1)
                || Double.isInfinite(dx1) || Double.isInfinite(dy1)) {
            controlPoint[0] = p0x;
            controlPoint[1] = p1y;
            return;
        }

        // unary vector from P2 to P1.
        double dx2 = p1x - p2x;
        double dy2 = p1y - p2y;
        final double l2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
        dx2 /= l2;
        dy2 /= l2;

        // direction of tangent where bezier control point lies on.
        double tx = dx1 + dx2;
        double ty = dy1 + dy2;
        final double l = Math.sqrt(tx * tx + ty * ty);
        tx /= l;
        ty /= l;

        // first control point
        controlPoint[0] = (p1x - length * F * smoothness * tx);
        controlPoint[1] = (p1y - length * F * smoothness * ty);
    }

    /**
     *
     */
    public void smooth(double smoothness, double[][] points,
            int firstPoint, int nbrPoints) {

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (smoothness <= 0. || MathUtils.numbersAreClose(0., smoothness)) {
                straightLines(points, firstPoint, nbrPoints);
                return;
            }

            final double F = 0.39;
            final int lastPoint = firstPoint + nbrPoints;

            if (points[0].length < 2) {
                throw new IllegalArgumentException();
            }

            path.reset();

            final boolean closePath = MathUtils.numbersAreClose(
                    points[firstPoint][0], points[lastPoint - 1][0])
                    && MathUtils.numbersAreClose(
                    points[firstPoint][1], points[lastPoint - 1][1]);

            double prevX = points[firstPoint][0];
            double prevY = points[firstPoint][1];

            double[] ctrlP1 = new double[2];
            double[] ctrlP2 = new double[2];

            // move to first point
            path.moveTo(points[firstPoint][0], points[firstPoint][1]);

            for (int i = firstPoint + 1; i < lastPoint - 1; i++) {

                // previous point P0
                final double x0 = points[i - 1][0];
                final double y0 = points[i - 1][1];

                // current point P1
                final double x1 = points[i][0];
                final double y1 = points[i][1];

                // next point P2
                final double x2 = points[i + 1][0];
                final double y2 = points[i + 1][1];

                bezierPoint(prevX, prevY, x0, y0, x1, y1, ctrlP1, smoothness);
                bezierPoint(x2, y2, x1, y1, x0, y0, ctrlP2, smoothness);

                // add a bezier line segment to the path
                path.curveTo(ctrlP1[0], ctrlP1[1], ctrlP2[0], ctrlP2[1], x1, y1);
                prevX = x0;
                prevY = y0;
            }

            final double x0 = points[lastPoint - 1][0];
            final double y0 = points[lastPoint - 1][1];
            bezierPoint(x0, y0, x0, y0, prevX, prevY, ctrlP1, smoothness);
            path.curveTo(ctrlP1[0], ctrlP1[1], x0, y0, x0, y0);
        } finally {
            trigger.inform();
        }
    }

    /**
     * Creates a circle. Replaces the current geometry.
     * @param cx The horizontal coordinate of the center.
     * @param cy The vertical coordinate of the center.
     * @param r The radius of the circle.
     */
    public void circle(double cx, double cy, double r) {
        // Build a Bezier path that approximates a full circle.
        // Based on an web-article by G. Adam Stanislav:
        // "Drawing a circle with Bézier Curves"
        if (r <= 0.f) {
            return;
        } // throw new IllegalArgumentException();
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.reset();

            final double kappa = (Math.sqrt(2.) - 1.) * 4. / 3.;
            final double l = r * kappa;

            // move to top center
            this.moveTo(cx, cy + r);
            // I. quadrant
            this.curveTo(cx + l, cy + r, cx + r, cy + l, cx + r, cy);
            // II. quadrant
            this.curveTo(cx + r, cy - l, cx + l, cy - r, cx, cy - r);
            // III. quadrant
            this.curveTo(cx - l, cy - r, cx - r, cy - l, cx - r, cy);
            // IV. quadrant
            this.curveTo(cx - r, cy + l, cx - l, cy + r, cx, cy + r);

            this.closePath();
        } finally {
            trigger.inform();
        }
    }

    /**
     * Creates a square. Replaces the current geometry.
     * @param cx The horizontal coordinate of the center.
     * @param cy The vertical coordinate of the center.
     * @param r The length of one side of the square.
     */
    public void square(double cx, double cy, double d) {
        if (d <= 0.f) {
            throw new IllegalArgumentException();
        }

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.reset();

            double d_2 = d / 2f;
            this.moveTo(cx - d_2, cy - d_2);
            this.lineTo(cx + d_2, cy - d_2);
            this.lineTo(cx + d_2, cy + d_2);
            this.lineTo(cx - d_2, cy + d_2);
            this.closePath();
        } finally {
            trigger.inform();
        }
    }

    /**
     * Creates a rectangle. Replaces the current geometry.
     * @param rect The geometry describing the rectangle.
     */
    public void rectangle(Rectangle2D rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.reset();

            final double xMin = rect.getMinX();
            final double xMax = rect.getMaxX();
            final double yMin = rect.getMinY();
            final double yMax = rect.getMaxY();
            this.moveTo(xMin, yMin);
            this.lineTo(xMax, yMin);
            this.lineTo(xMax, yMax);
            this.lineTo(xMin, yMax);
            this.closePath();
        } finally {
            trigger.inform();
        }
    }

    public void reset() {
        path.reset();
        MapEventTrigger.inform(this);
    }

    public void setPathModel(GeoPathModel path) {
        this.path = path;
        MapEventTrigger.inform(this);
    }

    /**
     * Removes the last point of the path that was added with moveto, lineto, etc.
     */
    public void removeLastPoint() {
        path.removeLastInstruction();
        MapEventTrigger.inform(this);
    }

    /**
     * Appends the geometry contained in a GeoPath to this GeoPath.
     * @param geoPath The GeoPath to append.
     * @param connect If true, the currently existing geometry is connected with the new geometry.
     */
    public void append(GeoPath geoPath, boolean connect) {
        if (geoPath != null) {
            path.append(geoPath.path, connect);
            MapEventTrigger.inform(this);
        }
    }

    /**
     * Appends the geometry contained by a Shape object to this GeoPath.
     * @param s The Shape to append.
     * @param connect If true, the currently existing geometry is connected with the new geometry.
     */
    public void append(java.awt.Shape s, boolean connect) {
        GeoPathModel pm = new GeoPathModel();
        pm.reset(s.getPathIterator(null));
        path.append(pm, connect);
        MapEventTrigger.inform(this);
    }

    private class PathSegment {
        public double[] coords;
        int id;
    }

    /**
     * Inverts the order of points in a line.
     * <B>Only for straight open lines!</B>
     */
    public void invertDirection() {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            PathIterator pathIterator = toPathIterator(null); // FIXME without PathIterator
            if (pathIterator == null) {
                return;
            }

            java.util.Vector segments = new java.util.Vector(); // FIXME

            while (!pathIterator.isDone()) {
                PathSegment ps = new PathSegment();
                ps.coords = new double[6];
                ps.id = pathIterator.currentSegment(ps.coords);
                segments.add(ps);
                pathIterator.next();
            }

            if (segments.isEmpty()) {
                return;
            }

            this.path.reset();

            PathSegment ps = (PathSegment) (segments.get(segments.size() - 1));
            this.path.moveTo(ps.coords[0], ps.coords[1]);

            for (int i = segments.size() - 2; i > 0; --i) {
                ps = (PathSegment) (segments.get(i));
                switch (ps.id) {
                    case PathIterator.SEG_MOVETO:
                        this.path.moveTo(ps.coords[0], ps.coords[1]);
                        break;

                    case PathIterator.SEG_LINETO:
                        this.path.lineTo(ps.coords[0], ps.coords[1]);
                        break;

                    /*
                    case PathIterator.SEG_QUADTO:
                    this.path.quadTo(ps.coords[0], ps.coords[1], ps.coords[2], ps.coords[3]);
                    break;
                    
                    case PathIterator.SEG_CUBICTO:
                    this.path.curveTo(ps.coords[0], ps.coords[1], ps.coords[2], ps.coords[3],
                    ps.coords[4], ps.coords[5]);
                    break;
                    
                    
                    case PathIterator.SEG_CLOSE:
                    this.path.closePath();
                    break;
                     */
                }
            }
            // treat initial moveto
            ps = (PathSegment) (segments.get(0));
            this.path.lineTo(ps.coords[0], ps.coords[1]);
        } finally {
            trigger.inform();
        }
    }

    /**
     * Converts all bezier lines of a GeneralPath to straight lines, and stores
     * the resulting path in a other GeneralPath.
     * @param flatness The maximum distance between the smooth bezier curve and
     * the new straight lines approximating the bezier curve.
     * @param generalPath The GeneralPath that will receive the flattened path. If null
     * a new GeneralPath will be created.
     * @return Returns the passed generalPath if not null, or a new GeneralPath otherwise.
     */
    private GeneralPath flatten(double flatness, GeneralPath generalPath) {
        PathIterator pathIterator = this.toPathIterator(null, flatness);
        if (generalPath == null) {
            generalPath = new GeneralPath();
        }
        double coords[] = new double[6];
        while (!pathIterator.isDone()) {
            int id = pathIterator.currentSegment(coords);
            switch (id) {
                case PathIterator.SEG_CLOSE:
                    generalPath.closePath();
                    break;
                case PathIterator.SEG_LINETO:
                    generalPath.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_MOVETO:
                    generalPath.moveTo(coords[0], coords[1]);
                    break;
                /*case PathIterator.SEG_QUADTO:
                generalPath.quadTo(coords[0], coords[1],
                coords[2], coords[3]);
                break;
                case PathIterator.SEG_CUBICTO:
                generalPath.curveTo(coords[0], coords[1],
                coords[2], coords[3],
                coords[4], coords[5]);
                break;
                 */
            }
            pathIterator.next();
        }
        return generalPath;
    }

    /**
     * Converts all bezier lines to straight lines. This changes this GeoPath.
     * @param flatness The maximum distance between the smooth bezier curve and
     * the new straight lines approximating the bezier curve.
     */
    public void flatten(RenderParams rp, double flatness) {
        this.path = path.toFlattenedPath(rp, flatness);
        MapEventTrigger.inform(this);
    }

    /**
     * Converts all bezier lines to straight lines. This does not change this 
     * GeoPath. A new GeoPath is returned instead.
     * @param flatness The maximum distance between the smooth bezier curve and
     * the new straight lines approximating the bezier curve.
     */
    public GeoPath toFlattenedPath(RenderParams rp, double flatness) {
        GeoPath geoPath = (GeoPath) this.clone();
        geoPath.setPathModel(path.toFlattenedPath(rp, flatness));
        return geoPath;
    }

    public double[][] getFirstFlattenedPolygon(double flatness) {
        GeneralPath generalPath = this.flatten(flatness, (GeneralPath) null);
        if (generalPath == null) {
            return null;
        }

        // count number of points in flattened path
        int nbrPts = 0;
        PathIterator pathIterator = generalPath.getPathIterator(null, flatness);
        while (!pathIterator.isDone()) {
            pathIterator.next();
            nbrPts++;
        }

        // allocate memory for coordinates
        double[][] pts = new double[nbrPts][2];

        // clone points
        int ptID = 0;
        pathIterator = generalPath.getPathIterator(null, flatness);
        double coords[] = new double[6];
        while (!pathIterator.isDone()) {
            int id = pathIterator.currentSegment(coords);
            switch (id) {
                case PathIterator.SEG_CLOSE:
                    pts[ptID][0] = pts[0][0];
                    pts[ptID][1] = pts[0][1];
                    return pts;
                case PathIterator.SEG_LINETO:
                    pts[ptID][0] = coords[0];
                    pts[ptID][1] = coords[1];
                    break;
                case PathIterator.SEG_MOVETO:
                    if (ptID > 0) {
                        return pts;
                    }
                    pts[ptID][0] = coords[0];
                    pts[ptID][1] = coords[1];
                    break;
            }
            pathIterator.next();
            ptID++;
        }
        return pts;
    }

    /**
     * Returns true if any segments in the path is a bezier curve.
     * @return 
     */
    public final boolean hasBezierSegment() {
        return path.hasBezierSegment();
    }
    
    /**
     * Returns true if this GeoPath contains at least one point.
     * @return True if number of points > 0, false otherwise.
     */
    public boolean hasOneOrMorePoints() {
        return path.getDrawingInstructionCount() > 0;
    }

    public int getPointsCount() {
        return path.getPointsCount();
    }

    /**
     * Returns the number of drawing instructions that build this GeoPath.
     * @return The number of instructions.
     */
    public int getDrawingInstructionCount() {
        return path.getDrawingInstructionCount();
    }

    public byte getLastDrawingInstruction() {
        return path.getLastInstruction();
    }

    public Point2D getStartPoint() {
        return path.getStartPoint();
    }

    public Point2D getEndPoint() {
        return path.getEndPoint();
    }

    /**
     * Returns a reference on the vector symbol that stores the graphic attributes
     * used to draw this GeoPath.
     * @return The VectorSymbol used to draw this GeoPath.
     */
    public VectorSymbol getVectorSymbol() {
        return symbol;
    }

    /**
     * Set the VectorSymbol that stores the graphic attributes used to draw this
     * GeoPath. The VectorSymbol is not copied, but simply a reference to it is retained.
     * @param symbol The new VectorSymbol.
     */
    public void setVectorSymbol(VectorSymbol symbol) {
        this.symbol = symbol;
        MapEventTrigger.inform(this);
    }

    /**
     * Returns a PathIterator that can be used to draw this GeoPath or iterate over its
     * geometry.
     * @param affineTransform An AffineTransform to apply before the PathIterator is returned.
     * @return The PathIterator.
     */
    public PathIterator toPathIterator(RenderParams rp) {
        return path.toPathIterator(rp);
    }
    
    public PathIterator toPathIterator(RenderParams rp, AffineTransform at) {
        return path.toPathIterator(rp, at);
    }

    /**
     * Returns a flattened PathIterator that can be used to draw this GeoPath or iterate over its
     * geometry. A flattened PathIterator does not contain any quatratic or cubic bezier
     * curve segments, but only straight lines.
     * @param affineTransform An AffineTransform to apply before the PathIterator is returned.
     * @param flatness The maximum deviation of the flatted geometry from the original bezier geometry.
     * @return The PathIterator.
     */
    public PathIterator toPathIterator(RenderParams rp, double flatness) {
        return path.toPathIterator(rp, flatness);
    }

    public void drawNormalState(RenderParams rp) {

        final Graphics2D g2d = rp.g2d;
        final double scale = rp.scale;
        final GeneralPath flattenedPath = path.toGeneralPath(rp);

        // fill
        if (symbol != null && symbol.isFilled()) {
            g2d.setColor(symbol.getFillColor());
            g2d.fill(flattenedPath);
        }

        // stroke
        if (symbol != null) {
            // apply the symbol attached to this path
            if (symbol.isStroked()) {
                g2d.setStroke(symbol.getStroke(scale));
                g2d.setColor(symbol.getStrokeColor());
                g2d.draw(flattenedPath); // stroke it
            }
        } else {
            // there is no VectorSymbol present, use a default stroke.
            g2d.setStroke(new BasicStroke(0));
            g2d.setColor(Color.BLACK);
            g2d.draw(flattenedPath); // stroke it
        }

    }

    public void drawSelectedState(RenderParams rp) {

        if (!this.isSelected()) {
            return;
        }

        // only stroke, no fill
        final GeneralPath flattenedPath = path.toGeneralPath(rp);
        rp.g2d.draw(flattenedPath);

    }

    public boolean isPointOnSymbol(Point2D point, double tolDist, double scale) {

        if (point == null) {
            return false;
        }

        /* First test if point is inside the bounding box.
        The rectangle has to be enlarged by tolDist, otherwise contains()
        returns false for a straight horizontal or vertical line. */
        Rectangle2D bounds = this.getBounds2D(scale);
        if (bounds == null) {
            return false;
        }
        bounds = (Rectangle2D) bounds.clone();

        GeometryUtils.enlargeRectangle(bounds, tolDist);
        if (bounds.contains(point) == false) {
            return false;
        }

        // if path is filled, test if point is inside path
        if (this.symbol.isFilled() && path.contains(point.getX(), point.getY())) {
            return true;
        }

        // test if distance to line is smaller than tolDist
        // create new path with straight lines only
        PathIterator pi = this.path.toPathIterator(null, tolDist / 2.);
        double x1 = 0;
        double y1 = 0;
        double lastMoveToX = 0;
        double lastMoveToY = 0;
        double[] coords = new double[6];
        int segmentType;
        while (pi.isDone() == false) {
            segmentType = pi.currentSegment(coords);
            switch (segmentType) {
                case PathIterator.SEG_CLOSE:
                    // SEG_CLOSE does not return any point.
                    coords[0] = lastMoveToX;
                    coords[1] = lastMoveToY;
                // fall thru, no break here

                case PathIterator.SEG_LINETO:
                    double d = Line2D.ptSegDistSq(x1, y1, coords[0], coords[1],
                            point.getX(), point.getY());
                    if (d < tolDist * tolDist) {
                        return true;
                    }
                    x1 = coords[0];
                    y1 = coords[1];
                    break;

                case PathIterator.SEG_MOVETO:
                    lastMoveToX = x1 = coords[0];
                    lastMoveToY = y1 = coords[1];
                    break;
            }
            pi.next();
        }
        return false;
    }

    public boolean contains(double x, double y) {
        return path.contains(x, y);
    }

    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {

        // Test if the passed rectangle and the bounding box of this object
        // intersect.
        // Don't use Rectangle2D.intersects, but use 
        // GeometryUtils.rectanglesIntersect, which can handle rectangles with
        // an heigt or a width of 0.
        final Rectangle2D bounds = this.getBounds2D(scale);
        if (GeometryUtils.rectanglesIntersect(rect, bounds) == false) {
            return false;
        }

        // transform curved bezier segments to straight line segments.
        // tolerance for conversion is 0.5 pixel converted to world coordinates.
        final double tolDist = 0.5 / scale;

        // loop over all straight line segments of this path
        PathIterator pi = path.toPathIterator(null, tolDist);
        double lx1 = 0;
        double ly1 = 0;
        double lx2, ly2;
        double lastMoveToX = 0;
        double lastMoveToY = 0;
        double[] coords = new double[6];
        int segmentType;
        while (pi.isDone() == false) {
            segmentType = pi.currentSegment(coords);
            lx2 = coords[0];
            ly2 = coords[1];
            switch (segmentType) {
                case PathIterator.SEG_CLOSE:
                    lx2 = lastMoveToX;
                    ly2 = lastMoveToY;
                // fall through, no break here
                case PathIterator.SEG_LINETO:
                    // test if rect and the line segment intersect.
                    if (GeometryUtils.lineIntersectsRectangle(lx1, ly1, lx2, ly2, rect)) {
                        return true;
                    }
                    lx1 = lx2;
                    ly1 = ly2;
                    break;

                case PathIterator.SEG_MOVETO:
                    lastMoveToX = lx1 = lx2;
                    lastMoveToY = ly1 = ly2;
                    break;
            }
            pi.next();
        }
        return false;
    }

    public Rectangle2D getBounds2D(double scale) {
        return (path != null) ? path.getBounds2D() : null;
    }

    /**
     * Scale this path by a factor relative to a passed origin.
     * @param scale Scale factor.
     * @param cx The x coordinate of the point relativ to which the object is scaled.
     * @param cy The y coordinate of the point relativ to which the object is scaled.
     */
    @Override
    public void scale(double scale, double cx, double cy) {
        this.path.scale(scale, cx, cy);
    }

    public void transform(AffineTransform affineTransform) {
        this.path.transform(affineTransform);
        MapEventTrigger.inform(this);
    }

    /**
     * Returns an iterator for this path. It is the caller's responsibility
     * to make sure that this path is not changed while a GeoPathIterator
     * is used.
     */
    public GeoPathIterator getIterator() {
        return path.getIterator();
    }

    public Area toArea() {
        return new Area(path.toGeneralPath());
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder();
        PathIterator pi = path.toPathIterator(null);
        double[] coord = new double[6];
        while (pi.isDone() == false) {
            switch (pi.currentSegment(coord)) {
                case PathIterator.SEG_MOVETO:
                    str.append("moveto ");
                    str.append(coord[0]);
                    str.append(" ");
                    str.append(coord[1]);
                    str.append("\n");
                    break;

                case PathIterator.SEG_LINETO:
                    str.append("lineto ");
                    str.append(coord[0]);
                    str.append(" ");
                    str.append(coord[1]);
                    str.append("\n");
                    break;

                case PathIterator.SEG_QUADTO:
                    str.append("quad ");
                    str.append(coord[0]);
                    str.append(" ");
                    str.append(coord[1]);
                    str.append("\n\t");
                    str.append(coord[2]);
                    str.append(" ");
                    str.append(coord[3]);
                    str.append("\n");
                    break;

                case PathIterator.SEG_CUBICTO:
                    str.append("cubic ");
                    str.append(coord[0]);
                    str.append(" ");
                    str.append(coord[1]);
                    str.append("\n\t");
                    str.append(coord[2]);
                    str.append(" ");
                    str.append(coord[3]);
                    str.append("\n\t");
                    str.append(coord[4]);
                    str.append(" ");
                    str.append(coord[5]);
                    str.append("\n");
                    break;

                case PathIterator.SEG_CLOSE:
                    str.append("close\n");
                    break;

            }
            pi.next();
        }

        return super.toString() + " \n" + str.toString() + this.symbol.toString();
    }

    public double getArea() {
        return this.path.getArea();
    }

    public ArrayList<Point2D> toBeads(double d,
            double jitterAlongLine, double jitterVertical) {

        Random random = new Random(0);

        if (d <= 0) {
            throw new IllegalArgumentException();
        }

        ArrayList<Point2D> xy = new ArrayList<Point2D>();

        GeoPathIterator iterator = path.getIterator();
        double startX = iterator.getX();
        double startY = iterator.getY();

        // add start point
        xy.add(new Point2D.Double(startX, startY));

        double lastMoveToX = startX;
        double lastMoveToY = startY;

        double length = 0;
        while (iterator.next()) {
            double endX = 0;
            double endY = 0;
            final int inst = iterator.getInstruction();
            switch (inst) {

                case GeoPathModel.CLOSE:
                    endX = lastMoveToX;
                    endY = lastMoveToY;
                    break;

                case GeoPathModel.MOVETO:
                    startX = lastMoveToX = iterator.getX();
                    startY = lastMoveToY = iterator.getY();
                    continue;

                default:
                    endX = iterator.getX();
                    endY = iterator.getY();
                    break;

            }

            // normalized direction dx and dy
            double dx = endX - startX;
            double dy = endY - startY;
            final double l = Math.hypot(dx, dy);
            dx /= l;
            dy /= l;

            double rest = length;
            length += l;
            while (length >= d) {
                // compute new point
                length -= d;
                startX += dx * (d - rest);
                startY += dy * (d - rest);
                rest = 0;
                Point2D.Double pt = new Point2D.Double(startX, startY);
                this.jitter(pt, dx, dy, jitterAlongLine, jitterVertical, random);
                xy.add(pt);
            }
            startX = endX;
            startY = endY;
        }

        return xy;
    }

    private void jitter(Point2D.Double pt, double ndx, double ndy,
            double maxJitterAlongLine, double maxJitterVertical, Random random) {

        final double jitterAlongLine = maxJitterAlongLine * (random.nextDouble() - 0.5);
        final double jitterVertical = maxJitterVertical * (random.nextDouble() - 0.5);
        final double dx = ndx * jitterAlongLine - ndy * jitterVertical;
        final double dy = ndy * jitterAlongLine + ndx * jitterVertical;
        pt.x += dx;
        pt.y += dy;

    }
}
