/*
 * GridThresholdOperator.java
 *
 * Created on February 6, 2006, 9:45 AM
 *
 */
package ika.geo.grid;

import ika.geo.GeoGrid;

/**
 * Changes all values greater or smaller than a limit value to another value.
 * @author jenny
 */
public class GridThresholdOperator extends ThreadedGridOperator {

    private float thresholdValue = 0.f;
    private float replaceValue = 0.f;
    private boolean smallerThan = true;

    /** Creates a new instance of GridThresholdOperator */
    public GridThresholdOperator() {
    }

    public String getName() {
        return "Threshold";
    }
    
    public void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {

        float[][] srcGrid = src.getGrid();
        float[][] dstGrid = dst.getGrid();
        final int ncols = src.getCols();

        // Special treatment if the source and the destination grid is the same
        // object. There is no need to write to the destination grid if the
        // value is accepted.
        if (src == dst) {
            if (smallerThan) {
                for (int row = startRow; row < endRow; ++row) {
                    float[] srcRow = srcGrid[row];
                    float[] dstRow = dstGrid[row];
                    for (int col = 0; col < ncols; ++col) {
                        if (srcRow[col] < thresholdValue) {
                            dstRow[col] = replaceValue;
                        }
                    }
                }
            } else {
                for (int row = startRow; row < endRow; ++row) {
                    float[] srcRow = srcGrid[row];
                    float[] dstRow = dstGrid[row];
                    for (int col = 0; col < ncols; ++col) {
                        if (srcRow[col] > thresholdValue) {
                            dstRow[col] = replaceValue;
                        }
                    }
                }
            }
        } else {
            if (smallerThan) {
                for (int row = startRow; row < endRow; ++row) {
                    float[] srcRow = srcGrid[row];
                    float[] dstRow = dstGrid[row];
                    for (int col = 0; col < ncols; ++col) {
                        final float v = srcRow[col];
                        dstRow[col] = v < thresholdValue ? replaceValue : v;
                    }
                }
            } else {
                for (int row = startRow; row < endRow; ++row) {
                    float[] srcRow = srcGrid[row];
                    float[] dstRow = dstGrid[row];
                    for (int col = 0; col < ncols; ++col) {
                        final float v = srcRow[col];
                        dstRow[col] = v > thresholdValue ? replaceValue : v;
                    }
                }
            }
        }
    }

    public float getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(float minValue) {
        this.thresholdValue = minValue;
    }

    public float getReplaceValue() {
        return replaceValue;
    }

    public void setReplaceValue(float replaceValue) {
        this.replaceValue = replaceValue;
    }

    public void clipSmallValues(float val) {
        this.thresholdValue = val;
        this.replaceValue = val;
        this.smallerThan = true;
    }

    public void clipLargeValues(float val) {
        this.thresholdValue = val;
        this.replaceValue = val;
        this.smallerThan = false;
    }
}
