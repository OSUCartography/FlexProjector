/*
 * ArrayUtils.java
 *
 * Created on June 22, 2006, 2:54 PM
 *
 */

package ika.utils;

/**
 *
 * @author jenny
 */
public class ArrayUtils {
    /**
     * Inverts the order of the values in an array of doubles.
     * The passed array is altered.
     */
    public static void invertOrder(double[] values) {
        // invert order of values
        final int length = values.length;
        final int halfLength = length / 2;
        for (int i = 0; i < halfLength; i++) {
            final double temp = values[i];
            values[i] = values[length - i - 1];
            values[length - i - 1] = temp;
        }
    }
    
    /**
     * Removes duplicate values from an array of doubles in increasing order. 
     * If the values are not ordered, the result is undefined and an 
     * IllegalArgumentException is thrown.
     * @param values The array of doubles in increasing order.
     * @return A new array in increasing order with duplicate values removed, or
     * null if the passed array is null.
     */
    public static double[] removeDuplicatesFromSortedArray(double[] values) {
        if (values == null)
            return null;
        if (values.length < 2)
            return (double[])values.clone();
        
        int currentValueID = 0;
        double currentValue = values[0];
        for (int i = 1; i < values.length; i++) {
            final double d = values[i];
            if (d > currentValue) {
                values[++currentValueID] = d;
                currentValue = d;
            } else if (d < currentValue) {
                throw new IllegalArgumentException
                        ("array values not in increasing order");
            }
        }
        return ArrayUtils.trimLength(values, currentValueID+1);
    }
    
    public static double[] removeDuplicateValues(double[] values) {
        System.err.println ("removeDuplicateValues() not tested");
        
        if (values == null)
            return null;
        
        java.util.ArrayList res = new java.util.ArrayList(values.length);
        for (int i = 0; i < values.length; i++) {
            Double d = new Double(values[i]);
            if (!res.contains(d))
                res.add(d);
        }
        
        double[] resValues = new double[res.size()];
        for (int i = 0; i < resValues.length; i++) {
            resValues[i] = ((Double)res.get(i)).doubleValue();
        }
        return resValues;
    }
    
    public static boolean containsRef(java.lang.Object[] anArray,
                                           java.lang.Object anElement) {
        for (int i = 0; i < anArray.length; i++) {
            if (anArray[i] == anElement)
                return true;
        }
        return false;
    }
    
    public static double[] trimLength(double[] values, int nbrNewValues) {
        if (nbrNewValues > values.length)
            throw new IllegalArgumentException();
        
        double[] trimmed = new double[nbrNewValues];
        System.arraycopy(values, 0, trimmed, 0, nbrNewValues);
        return trimmed;
    }

    /**
     * Clones a two-dimensional matrix.
     * @param a The matrix to copy.
     * @return A clone of the matrix or null if parameter a is null.
     */
    public static double[][] clone2DArray(double[][] a) {
        
        if (a == null)
            return null;
        
        double[][] copy = new double[a.length][a.length == 0 ? 0 : a[0].length];
        for (int i = 0; i < a.length; i++) {
            System.arraycopy(a[i], 0, copy[i], 0, a[i].length);
        }
        
        return copy;
        
    }
}
