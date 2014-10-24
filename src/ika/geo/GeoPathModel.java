/*
 * PathModel.java
 *
 * Created on March 30, 2007, 11:32 AM
 *
 */
package ika.geo;

import ika.utils.GeometryUtils;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * GeoPathModel holds the geometry model of a GeoPath. It does not handle 
 * symbolization.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public final class GeoPathModel implements Serializable, Cloneable {

    private static final long serialVersionUID = 74565327855645505L;
    public static final byte NONE = 0; // no instruction defined.
    // NONE is not stored in this.instructions[]
    public static final byte MOVETO = 1;
    public static final byte LINETO = 2;
    public static final byte CURVETO = 3;       // cubic bezier curve
    public static final byte QUADCURVETO = 4;   // quadratic bezier curve
    public static final byte CLOSE = 5;
    /**
     * An array containing all points: x1, y1, x2, y2, etc.
     */
    protected double[] points = new double[0];
    /**
     * An array containing MOVETO, LINETO, CURVETO, QUADCURVETO or CLOSE.
     */
    protected byte[] instructions = new byte[0];
    /**
     * The bounding box of this path.
     * Rectangle2D is not serializable!
     */
    private transient Rectangle2D bounds = null;

    /** Creates a new instance of PathModel */
    public GeoPathModel() {
    }

    /**
     * Deserialize this object.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        // read the serializable part of this GeoPath.
        stream.defaultReadObject();

        // compute bounds of path. this.bounds is not serialized.
        updateBounds();
    }

    /**
     * Create and return a copy of this path.
     */
    @Override
    public Object clone() {
        GeoPathModel copy = new GeoPathModel();

        final int instructionsCount = this.instructions.length;
        final byte[] instructionsCopy = new byte[instructionsCount];
        System.arraycopy(this.instructions, 0, instructionsCopy, 0, instructionsCount);
        copy.instructions = instructionsCopy;

        final int coordinatesCount = this.points.length;
        final double[] pointsCopy = new double[coordinatesCount];
        System.arraycopy(this.points, 0, pointsCopy, 0, coordinatesCount);
        copy.points = pointsCopy;

        if (this.bounds != null) {
            copy.bounds = (Rectangle2D) this.bounds.clone();
        }

        return copy;
    }

    /**
     * Convert a cubic bezier spline to an array of straight lines.
     * @param path Destination for straight line segments.
     * @param tol The maximum tolerable deviation from the bezier spline.
     */
    public static void bezierToStraightLines(GeoPathModel path,
            double x1, double y1,
            double x2, double y2,
            double x3, double y3,
            double x4, double y4,
            double tol) {

        final double t = 0.5;

        if (GeometryUtils.isStraightLine(x1, y1, x2, y2, x3, y3, x4, y4, tol)) {
            // don't add new point if start and end point coincide.
            if (x1 != x4 || y1 != y4) {
                path.lineTo(x4, y4);
            }
        } else {   // divide curve by factor of two

            final double hx = t * (x2 + x3);
            final double hy = t * (y2 + y3);
            final double l2x = t * (x1 + x2);
            final double l2y = t * (y1 + y2);
            final double l3x = t * (l2x + hx);
            final double l3y = t * (l2y + hy);
            final double r3x = t * (x3 + x4);
            final double r3y = t * (y3 + y4);
            final double r2x = t * (hx + r3x);
            final double r2y = t * (hy + r3y);
            final double lrx = t * (l3x + r2x);
            final double lry = t * (l3y + r2y);

            // left part of curve
            bezierToStraightLines(path, x1, y1, l2x, l2y, l3x, l3y, lrx, lry, tol);

            // right part of curve
            bezierToStraightLines(path, lrx, lry, r2x, r2y, r3x, r3y, x4, y4, tol);
        }

    }

    /**
     * Adds the passed drawing instruction to this.instructions. This method does
     * not alter any other variable of this object.
     * IMPORTANT: It is not guaranteed that the passed drawing instruction is 
     * added to this.instructions. 
     * @param instruction The drawing instruction to add.
     */
    private void pushDrawingInstruction(byte instruction) {
        final int instructionsCount = instructions.length;
        final byte[] newInstructions = new byte[instructionsCount + 1];
        System.arraycopy(instructions, 0, newInstructions, 0, instructionsCount);
        newInstructions[instructionsCount] = instruction;
        instructions = newInstructions;
    }

    /**
     * Removes the last drawing instruction from instructions. This method 
     * does not alter any other variable of this object.
     */
    private void popDrawingInstruction() {
        final int instructionsCount = instructions.length;
        if (instructionsCount == 0) {
            return;
        }
        final byte[] newInstructions = new byte[instructionsCount - 1];
        System.arraycopy(instructions, 0, newInstructions, 0, instructionsCount - 1);
        instructions = newInstructions;
    }

    /**
     * Add a drawing instruction that uses a single point as argument (lineto or
     * moveto).
     * @param x The horizontal coordinate of the point.
     * @param y The horizontal coordinate of the point.
     * @param drawingInstruction The instruction to add.
     */
    private void add(double x, double y, byte drawingInstruction) {

        // add point
        final int coordinatesCount = points.length;
        final double[] newPoints = new double[coordinatesCount + 2];
        System.arraycopy(points, 0, newPoints, 0, coordinatesCount);
        newPoints[coordinatesCount] = x;
        newPoints[coordinatesCount + 1] = y;
        points = newPoints;

        // update bounding box
        includeLastPointInBoundingBox();

        // add drawing instruction
        pushDrawingInstruction(drawingInstruction);
    }

    /**
     * Removes count x-y point pairs from the points array.
     */
    private void removePoints(int count) {
        final int coordinatesCount = points.length;
        final double[] newPoints = new double[coordinatesCount - count * 2];
        System.arraycopy(points, 0, newPoints, 0, newPoints.length);
        points = newPoints;

        updateBounds();
    }

    /**
     * Makes sure the last point added to this path is included in the bounding
     * box. If it is not, the bounding box is altered accordingly.
     */
    private void includeLastPointInBoundingBox() {
        final int coordinatesCount = points.length;
        final double x = points[coordinatesCount - 2];
        final double y = points[coordinatesCount - 1];
        if (bounds == null) {
            bounds = new Rectangle2D.Double(x, y, 0, 0);
            return;
        }

        // enlarge the bounds rectangle if the new point lays ouside its 
        // current extension.
        bounds.add(x, y);
    }

    /**
     *  Compute the bounding box of this path and store it in this.bounds.
     */
    protected void updateBounds() {

        final int coordinatesCount = points.length;
        if (coordinatesCount == 0) {
            bounds = null;
            return;
        }

        double minX = points[0];
        double maxX = points[0];
        double minY = points[1];
        double maxY = points[1];

        for (int i = 2; i < coordinatesCount; i += 2) {
            final double x = points[i];
            final double y = points[i + 1];
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
        }

        bounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Removes the last drawing instruction of the path that was added with 
     * moveto, lineto, etc.
     */
    public void removeLastInstruction() {
        final int instructionsCount = instructions.length;
        if (instructionsCount == 0) {
            return;
        }

        switch (instructions[instructionsCount - 1]) {
            case CLOSE:
                break;
            case MOVETO:
            case LINETO:
                removePoints(1);
                break;
            case QUADCURVETO:
                removePoints(2);
                break;
            case CURVETO:
                removePoints(3);
                break;
        }
        popDrawingInstruction();
    }

    /**
     * Removes all currently stored drawing instructions and all points.
     */
    public void reset() {
        points = new double[0];
        instructions = new byte[0];
        bounds = null;
    }

    /**
     * Removes all currently stored drawing instructions and points, and 
     * replaces them with the drawing stored in the passed PathIterator.
     * @param pathIterator Contains the new geometry of the path.
     */
    public void reset(PathIterator pathIterator) {
        reset();
        append(pathIterator);
    }

    /**
     * Append a move-to command to the current path. Places the virtual pen at
     * the specified location without drawing any line.
     * @param x The location to move to.
     * @param y The location to move to.
     */
    public void moveTo(double x, double y) {
        final int instructionsCount = instructions.length;
        if (instructionsCount > 0 
                && instructions[instructionsCount - 1] == MOVETO) {
            removeLastInstruction();
        }

        add(x, y, MOVETO);
    }

    /**
     * Append a move-to command to the current path. Places the virtual pen at the
     * specified location without drawing any line.
     * @param xy An array containing the x and the y coordinate.
     */
    public void moveTo(double[] xy) {
        moveTo(xy[0], xy[1]);
    }

    /**
     * Draws a line from the current location of the pen to the specified location. Before
     * calling lineTo, moveTo must be called. Alternatively, use moveOrLineTo that makes
     * sure moveTo is called before lineTo (or quadTo, resp. curveTo).
     * @param x The end point of the new line.
     * @param y The end point of the new line.
     */
    public void lineTo(double x, double y) {
        add(x, y, LINETO);
    }

    /**
     * Draws a line from the current location of the pen to the specified location. Before
     * calling lineTo, moveTo must be called. Alternatively, use moveOrLineTo that makes
     * sure moveTo is called before lineTo (or quadTo, resp. curveTo).
     * @param xy An array containing the x and the y coordinate.
     */
    public void lineTo(double[] xy) {
        add(xy[0], xy[1], LINETO);
    }

    /**
     * Moves the virtual pen to the specified location if this is the first call that
     * changes the geometry. If this is not the first geometry changing call, a straight
     * line is drawn to the specified location.
     * @param x The end point of the new line, or the location to move to.
     * @param y The end point of the new line, or the location to move to.
     */
    public void moveOrLineTo(double x, double y) {
        if (points.length == 0) {
            moveTo(x, y);
        } else {
            add(x, y, LINETO);
        }
    }

    /**
     * Appends a quadratic bezier curve.
     * @param x1 The location of the control point that is not on the curve.
     * @param y1 The location of the control point that is not on the curve.
     * @param x2 The location of the end point of the new curve.
     * @param y2 The location of the control point that is not on the curve.
     */
    public void quadTo(double x1, double y1, double x2, double y2) {
        // add two points
        int coordinatesCount = points.length;
        final double[] expandedPoints = new double[coordinatesCount + 4];
        System.arraycopy(points, 0, expandedPoints, 0, coordinatesCount);
        expandedPoints[coordinatesCount] = x1;
        expandedPoints[++coordinatesCount] = y1;
        expandedPoints[++coordinatesCount] = x2;
        expandedPoints[++coordinatesCount] = y2;
        points = expandedPoints;

        // update bounding box
        includeLastPointInBoundingBox();
        includeLastPointInBoundingBox();

        // add drawing instruction
        pushDrawingInstruction(QUADCURVETO);
    }

    /**
     * Appends a cubic bezier curve.
     * @param x1 The location of the first control point that is not on the curve.
     * @param y1 The location of the first control point that is not on the curve.
     * @param x2 The location of the second control point that is not on the curve.
     * @param y2 The location of the second control point that is not on the curve.
     * @param x3 The location of the end point of the new curve.
     * @param y3 The location of the end point of the new curve.
     */
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {

        // add three points
        int coordinatesCount = points.length;
        final double[] expandedPoints = new double[coordinatesCount + 6];
        System.arraycopy(points, 0, expandedPoints, 0, coordinatesCount);
        expandedPoints[coordinatesCount] = x1;
        expandedPoints[++coordinatesCount] = y1;
        expandedPoints[++coordinatesCount] = x2;
        expandedPoints[++coordinatesCount] = y2;
        expandedPoints[++coordinatesCount] = x3;
        expandedPoints[++coordinatesCount] = y3;
        points = expandedPoints;

        // update bounding box
        includeLastPointInBoundingBox();
        includeLastPointInBoundingBox();
        includeLastPointInBoundingBox();

        // add drawing instruction
        pushDrawingInstruction(CURVETO);
    }

    /**
     * Closes the path by connecting the last point with the first point using a
     * straight line. 
     */
    public void closePath() {
        if (instructions.length > 0) {
            pushDrawingInstruction(CLOSE);
        }
    }

    /**
     * Returns true if any of the sub-paths is closed.
     * @return True if the path is closed.
     */
    public boolean isClosed() {
        final int instructionsCount = instructions.length;
        for (int i = 0; i < instructionsCount; i++) {
            if (instructions[i] == CLOSE) {
                return true;
            }
        }
        return false;
    }

    public void append(GeoPathModel pathModel, boolean connect) {
        if (pathModel.getDrawingInstructionCount() == 0) {
            return;
        }

        final int instructionsCount = pathModel.instructions.length;
        int pt = 0;
        for (int i = 0; i < instructionsCount; i++) {
            switch (pathModel.instructions[i]) {
                case MOVETO:
                    if (i == 0 && connect) {
                        lineTo(pathModel.points[pt++], pathModel.points[pt++]);
                    } else {
                        moveTo(pathModel.points[pt++], pathModel.points[pt++]);
                    }
                    break;
                case LINETO:
                    lineTo(pathModel.points[pt++], pathModel.points[pt++]);
                    break;
                case CLOSE:
                    closePath();
                    break;
                case QUADCURVETO:
                    quadTo(pathModel.points[pt++], pathModel.points[pt++],
                            pathModel.points[pt++], pathModel.points[pt++]);
                    break;
                case CURVETO:
                    curveTo(pathModel.points[pt++], pathModel.points[pt++],
                            pathModel.points[pt++], pathModel.points[pt++],
                            pathModel.points[pt++], pathModel.points[pt++]);
                    break;
            }
        }
    }

    public void append(PathIterator pathIterator) {
        double coords[] = new double[6];
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.SEG_CLOSE:
                    closePath();
                    break;
                case PathIterator.SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_MOVETO:
                    moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    quadTo(coords[0], coords[1],
                            coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;

            }
            pathIterator.next();
        }
        updateBounds();
    }

    /**
     * Returns true if this GeoPath consists of more than one line or polygon.
     * @return True if this is a compound path.
     */
    public boolean isCompound() {
        final int instructionsCount = instructions.length;
        if (instructionsCount == 0) {
            return false;
        }
        for (int i = 1; i < instructionsCount; i++) {
            if (instructions[i] == MOVETO) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * Returns the number of compound sub-paths.
     * @return The number of sub-paths. Returns 0 if this path does not contain
     * any instruction.
     */
    public int getCompoundCount() {
        final int instructionsCount = instructions.length;
        if (instructionsCount == 0) {
            return 0;
        }
        int compoundCount = 0;
        for (int i = 0; i < instructionsCount; i++) {
            if (instructions[i] == MOVETO) {
                ++compoundCount;
            }
        }
        return compoundCount;
    }

    /**
     * Returns true if the passed point is inside this path.
     * @param x
     * @param y
     * @return
     */
    public boolean contains(double x, double y) {
        if (bounds.contains(x, y) == false) {
            return false;
        }
        return false; // FIXME toGeneralPath().contains(x, y);
    }

    /**
     * Returns the bounding box of this path.
     * @return The bounding box around all points of this path. May be null if 
     * the path does not contain any drawing instructions. The width and / or
     * height may be 0.
     */
    public Rectangle2D getBounds2D() {
        return bounds;
    }

    /**
     * Returns the number of drawing instructions.
     * @return The number of instructions.
     */
    public int getDrawingInstructionCount() {
        return instructions.length;
    }

    /**
     * Returns the last drawing instruction.
     * @return The drawing command.
     */
    public byte getLastInstruction() {
        if (instructions.length == 0) {
            return GeoPathModel.NONE;
        }
        return instructions[instructions.length - 1];
    }

    public Point2D getLastMoveTo() {

        if (getPointsCount() < 1) {
            return null;
        }

        GeoPathIterator iterator = getIterator();
        Point2D pt = new Point2D.Double();
        while (iterator.next()) {
            if (iterator.getInstruction() == GeoPathModel.MOVETO) {
                pt.setLocation(iterator.getX(), iterator.getY());
            }
        }

        return pt;
    }

    public Point2D getStartPoint() {
        if (points.length == 0) {
            return null;
        }
        final double x = points[0];
        final double y = points[1];
        return new Point2D.Double(x, y);
    }

    public Point2D getEndPoint() {
        if (points.length == 0) {
            return null;
        }
        final double x = points[points.length - 2];
        final double y = points[points.length - 1];
        return new Point2D.Double(x, y);
    }

    public int getPointsCount() {
        return points.length / 2;
    }

    /**
     * Scale this path by a factor relative to a passed origin.
     * @param scale Scale factor.
     * @param cx The x coordinate of the point relativ to which the object is scaled.
     * @param cy The y coordinate of the point relativ to which the object is scaled.
     */
    public void scale(double scale, double cx, double cy) {
        final int pointsCount = points.length / 2;
        if (pointsCount == 0) {
            bounds = null;
            return;
        }

        for (int i = 0; i < pointsCount; i++) {
            final double x = points[2 * i];
            final double y = points[2 * i + 1];
            points[2 * i] = (x - cx) * scale + cx;
            points[2 * i + 1] = (y - cy) * scale + cy;
        }

        updateBounds();
    }

    /**
     * Apply an affine transformation on this path.
     * @param affineTransform The transformation to apply.
     */
    public void transform(AffineTransform affineTransform) {
        affineTransform.transform(points, 0, points, 0, points.length / 2);
        updateBounds();
    }

    /**
     * Returns the signed area enclosed by a GeoPath. The last point and the first 
     * point are connected.
     * Does not work with bezier curves and polygons with islands and holes !!! ???
     */
    public double getSignedArea() {
        double area = 0.;

        // convert this Path if it's (partially) built of bezier curves
        // GeoPath path = onlyStraightLines() ? this : convertToStraightLines();
        // this.bezierToStraightLines()

        final int nbrPoints = getPointsCount();
        if (nbrPoints < 3) {
            return 0;
        }

        GeoPathIterator pIter = getIterator();
        double x0 = pIter.getX();
        double y0 = pIter.getY();
        double x1 = x0;
        double y1 = y0;

        while (pIter.next() && pIter.getInstruction() == LINETO) {
            final double x2 = pIter.getX();
            final double y2 = pIter.getY();
            area += x1 * y2 - x2 * y1;
            x1 = x2;
            y1 = y2;
        }
        area += x1 * y0 - x0 * y1;
        return area * 0.5;
    }

    /**
     * Returns the area enclosed by a GeoPath. The last point and the first 
     * point are connected.
     * Does not work with bezier curves !!! ???
     */
    public double getArea() {
        return Math.abs(getSignedArea());
    }

    /**
     * Converts all bezier lines to straight lines and returns the result in a
     * new PathModel. Does not change this PathModel.
     * @param flatness The maximum distance between the smooth bezier curve and
     * the new straight lines approximating the bezier curve.
     */
    public GeoPathModel toFlattenedPath(RenderParams rp, double flatness) {
        PathIterator pi = toGeneralPath(rp).getPathIterator(null, flatness);
        GeoPathModel pm = new GeoPathModel();
        pm.reset(pi);
        return pm;
    }

    /**
     * Returns true if any segments in the path is a bezier curve.
     * @return 
     */
    public boolean hasBezierSegment() {
        final int instructionsCount = instructions.length;
        if (instructionsCount == 0) {
            return false;
        }
        for (int i = 1; i < instructionsCount; i++) {
            final byte b = instructions[i];
            if (b == QUADCURVETO || b == CURVETO) {
                return true;
            }
        }
        return false;
    
    }
    
    public GeneralPath toGeneralPath(RenderParams rp) {
        if (rp == null) {
            return toGeneralPath();
        }
        
        GeneralPath path = new GeneralPath();
        final int instructionsCount = instructions.length;
        int ptID = 0;
        for (int i = 0; i < instructionsCount; i++) {
            switch (instructions[i]) {
                case MOVETO:
                    path.moveTo(rp.tX(points[ptID++]), rp.tY(points[ptID++]));
                    break;
                case LINETO:
                    path.lineTo(rp.tX(points[ptID++]), rp.tY(points[ptID++]));
                    break;
                case CLOSE:
                    path.closePath();
                    break;
                case QUADCURVETO:
                    path.quadTo(rp.tX(points[ptID++]), rp.tY(points[ptID++]),
                            rp.tX(points[ptID++]), rp.tY(points[ptID++]));
                    break;
                case CURVETO:
                    path.curveTo(rp.tX(points[ptID++]), rp.tY(points[ptID++]),
                            rp.tX(points[ptID++]), rp.tY(points[ptID++]),
                            rp.tX(points[ptID++]), rp.tY(points[ptID++]));
                    break;
            }
        }
        return path;
    }

    public GeneralPath toGeneralPath() { // FIXME remove > loss of precision when converting to float
        GeneralPath path = new GeneralPath();
        final int instructionsCount = instructions.length;
        int ptID = 0;
        for (int i = 0; i < instructionsCount; i++) {
            switch (instructions[i]) {
                case MOVETO:
                    path.moveTo(points[ptID++],points[ptID++]);
                    break;
                case LINETO:
                    path.lineTo(points[ptID++],points[ptID++]);
                    break;
                case CLOSE:
                    path.closePath();
                    break;
                case QUADCURVETO:
                    path.quadTo(points[ptID++],points[ptID++],
                            points[ptID++],points[ptID++]);
                    break;
                case CURVETO:
                    path.curveTo(points[ptID++],points[ptID++],
                            points[ptID++],points[ptID++],
                            points[ptID++],points[ptID++]);
                    break;
            }
        }
        return path;
    }

    
    PathIterator toPathIterator(RenderParams rp) {
        return toGeneralPath(rp).getPathIterator(null);
    }

    PathIterator toPathIterator(RenderParams rp, double flatness) {
        return toGeneralPath(rp).getPathIterator(null, flatness);
    }
    
    PathIterator toPathIterator(RenderParams rp, AffineTransform at) {
        return toGeneralPath(rp).getPathIterator(at);
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder();
        final int instructionsCount = instructions.length;
        int ptID = 0;
        for (int i = 0; i < instructionsCount; i++) {
            switch (instructions[i]) {
                case MOVETO:
                    str.append("moveto ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append("\n");
                    break;
                case LINETO:
                    str.append("lineto ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append("\n");
                    break;
                case CLOSE:
                    str.append("close\n");
                    break;
                case QUADCURVETO:
                    str.append("quad ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append("\n");
                    break;
                case CURVETO:
                    str.append("cubic ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append(" ");
                    str.append(points[ptID++]);
                    str.append("\n");
                    break;
            }
        }
        return str.toString();
    }

    /**
     * Returns an iterator for this path. It is the caller's responsibility
     * to make sure that the GeoPathModel is not changed while a GeoPathIterator
     * is used.
     */
    public GeoPathIterator getIterator() {
        return new GeoPathIterator(this);
    }
}
