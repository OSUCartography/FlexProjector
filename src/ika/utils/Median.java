/*
 * Median.java
 *
 * Created on March 27, 2005, 2:05 PM
 */

package ika.utils;

/**
 * Algorithm from N. Wirth's book,<br>
 * implementation by N. Devillard in C,<br>
 * port to Java by B. Jenny.<br>
 * This code is in public domain.<br>
 *
 * Reference:<br>
 * Author: Wirth, Niklaus<br>
 * Title: Algorithms + data structures = programs<br>
 * Publisher: Englewood Cliffs: Prentice-Hall, 1976<br>
 * Physical description: 366 p.<br>
 * Series: Prentice-Hall Series in Automatic Computation<br>
 */
public class Median {
    
    /**
     * Find the kth smallest element in the array<br>
     * Notice: use median() to get the median.<br>
     * Important: The passed array will be altered. The order of its
     * elements will change!<br>
     * @param a Array of elements. The order of the elements will be changed!
     * @param k rank k, starting with 0, up to a.length-1
     * @return kth element in array a
     */
    public static double kth_smallest(double[] a, int k) {
        final int n = a.length;
        int i,j,l,m;
        double x;
        l=0;
        m = n-1;
        while (l<m) {
            x=a[k];
            i=l;
            j=m;
            do {
                while (a[i]<x)
                    i++;
                while (x<a[j])
                    j--;
                if (i<=j) {
                    // ELEM_SWAP(a[i],a[j]);
                    final double temp = a[i];
                    a[i]=a[j];
                    a[j]=temp;
                    
                    i++;
                    j--;
                }
            } while (i<=j);
            if (j<k) l=i;
            if (k<i) m=j;
        }
        return a[k];
    }
    
    /** The same for floats.
     */
    public static float kth_smallest(float[] a, int k) {
        final int n = a.length;
        int i,j,l,m;
        float x;
        l=0;
        m = n-1;
        while (l<m) {
            x=a[k];
            i=l;
            j=m;
            do {
                while (a[i]<x)
                    i++;
                while (x<a[j])
                    j--;
                if (i<=j) {
                    // ELEM_SWAP(a[i],a[j]);
                    final float temp = a[i];
                    a[i]=a[j];
                    a[j]=temp;
                    
                    i++;
                    j--;
                }
            } while (i<=j);
            if (j<k) l=i;
            if (k<i) m=j;
        }
        return a[k];
    }
    
    /**
     * Find the median of an array.<br>
     * Important: The passed array will be altered if preserveOrder is false.
     * The order of its elements will change!<br>
     * @param a The array of values
     * @param preserveOrder If true, the order of the values in a is guaranteed
     * to remain the same. Passing true for preserveOrder forces median() to
     * allocate a copy of a, and thus slows it down.
     * @return The median value of a.
     */
    public static double median(double[] a, boolean preserveOrder) {
        final int  n = a.length;
        if (preserveOrder) {
            a = (double[])(a.clone());
        }
        return Median.kth_smallest(a, (n%2==0) ? n/2-1 : n/2);
    }
    
    public static float median(float[] a, boolean preserveOrder) {
        final int  n = a.length;
        if (preserveOrder) {
            a = (float[])(a.clone());
        }
        return Median.kth_smallest(a, (n%2==0) ? n/2-1 : n/2);
    }
    
    public static float upperQuartile(float[] a) {
        return Median.kth_smallest(a, a.length * 3 / 4);
    }
    
    public static float lowerQuartile(float[] a) {
        return Median.kth_smallest(a, a.length / 4);
    }
    
    /**
     * A few tests.
     */
    /*
    public static void main(String[]args) {
        double[] a = {1, 2, 3};
        System.out.println(Median.median(a, false));
        double[] b = {4, 1, 3, 2};
        System.out.println(Median.median(b, false));
        double[] c = {2, 4, 1, 3};
        System.out.println(Median.kth_smallest(c, 0));
        System.out.println(Median.kth_smallest(c, 1));
        System.out.println(Median.kth_smallest(c, 2));
        System.out.println(Median.kth_smallest(c, 3));
        System.out.println();
     
        double[] d = {0, -9, 2, -3, 5, -999, 0.1, -6, 11332, -6.366, 456};
        System.out.println("Median: " + Median.median(d, true));
        MatrixUtils.printVector (d);
        System.out.println("Median: " + Median.median(d, false));
        MatrixUtils.printVector (d);
    }*/
}