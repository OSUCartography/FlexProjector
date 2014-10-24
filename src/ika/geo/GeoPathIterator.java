package ika.geo;

/**
 * An iterator that provides access to the points and the drawing 
 * instructions of a GeoPathModel.
 */
public class GeoPathIterator {

    /**
     * The path to iterate over. Initialized by the constructor.
     */
    private final GeoPathModel path;
    /**
     * The current drawing instruction. A call to next() moves this index
     * to the next instruction.
     */
    private int instructionID = 0;
    /**
     * The current point. A call to next() moves this index to the next
     * point for the current drawing instruction.
     */
    private int pointID = 0;

    /**
     * Construct a new iterator.
     * @param path The GeoPathModel which will be iterated.
     */
    protected GeoPathIterator(GeoPathModel path) {
        this.path = path;
    }

    /**
     * Move to the next drawing instruction.
     * @return True if there exists a next drawing instruction, false 
     * otherwise.
     */
    public boolean next() {
        if (instructionID + 1 >= path.instructions.length) {
            return false;
        }

        switch (path.instructions[instructionID]) {
            case GeoPathModel.CLOSE:
                break;

            case GeoPathModel.MOVETO:
            case GeoPathModel.LINETO:
                pointID += 2;
                break;

            case GeoPathModel.QUADCURVETO:
                pointID += 4;
                break;

            case GeoPathModel.CURVETO:
                pointID += 6;
                break;
        }

        ++instructionID;
        return true;
    }

    /**
     * Returns the current drawing instruction.
     * @return The current drawing instruction.
     */
    public byte getInstruction() {
        if (path.instructions.length == 0) {
            return GeoPathModel.NONE;
        }
        return path.instructions[instructionID];
    }

    /**
     * Returns whether the current instruction is the first instruction.
     * @return True if a call to getInstruction() will return the first instruction.
     */
    public boolean atFirstInstruction() {
        return instructionID == 0;
    }

    /**
     * Returns the horizontal x coordinate for the current drawing 
     * instruction. An exception will be thrown if the current instruction 
     * is a close instruction or if there are no drawing instructions.
     * @return The x coordinate for the current drawing instruction. 
     */
    public double getX() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CLOSE) {
            throw new IllegalStateException();
        }
        return path.points[pointID];
    }

    /**
     * Returns the vertical y coordinate for the current drawing 
     * instruction. An exception will be thrown if the current instruction 
     * is a close instruction or if there are no drawing instructions.
     * @return The y coordinate for the current drawing instruction. 
     */
    public double getY() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CLOSE) {
            throw new IllegalStateException();
        }
        return path.points[pointID + 1];
    }

    /**
     * Returns the horizontal x coordinate of the second control point of 
     * a cubic bezier curve or the final point of a quadratic bezier curve.
     * An exception will be thrown if the current instruction is not a cubic 
     * or quadratic bezier curve.
     * @return The x coordinate of the first control point. 
     */
    public double getX2() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CURVETO || instruction == GeoPathModel.QUADCURVETO) {
            return path.points[pointID + 2];
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the vertical y coordinate of the second control point of 
     * a cubic bezier curve or the final point of a quadratic bezier curve.
     * An exception will be thrown if the current instruction is not a cubic 
     * or quadratic bezier curve.
     * @return The y coordinate of the first control point. 
     */
    public double getY2() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CURVETO || instruction == GeoPathModel.QUADCURVETO) {
            return path.points[pointID + 3];
        } else {
            throw new IllegalStateException();
        }

    }

    /**
     * Returns the horizontal x coordinate of the final point of 
     * a cubic bezier curve. An exception will be thrown if the current 
     * instruction is not a cubic bezier curve.
     * @return The x coordinate of the second control point. 
     */
    public double getX3() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CURVETO) {
            return path.points[pointID + 4];
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the vertical y coordinate of the final control point of 
     * a cubic bezier curve. An exception will be thrown if the current 
     * instruction is not a cubic bezier curve.
     * @return The y coordinate of the second control point. 
     */
    public double getY3() {
        final int instruction = path.instructions[instructionID];
        if (instruction == GeoPathModel.CURVETO) {
            return path.points[pointID + 5];
        } else {
            throw new IllegalStateException();
        }

    }
}