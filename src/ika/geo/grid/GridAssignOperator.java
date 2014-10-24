package ika.geo.grid;

import ika.geo.GeoGrid;
import java.util.Arrays;

/**
 * Assign a constant value to a grid. The source grid is ignored.
 * @author jenny
 */
public class GridAssignOperator extends ThreadedGridOperator {

    private float value = 0f;
    
    public GridAssignOperator() {
    }
    
    public GridAssignOperator(float value) {
        this.value = value;
    }

    @Override
    protected void operate(GeoGrid src, GeoGrid dst, int startRow, int endRow) {
        for (int row = startRow; row < endRow; ++row) {
            Arrays.fill(src.getGrid()[row], value);
        }
    }

    public String getName() {
        return "Assign Constant";
    }

    /**
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }
    
}