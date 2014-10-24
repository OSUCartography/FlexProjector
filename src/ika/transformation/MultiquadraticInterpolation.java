/**
 * MultiquadraticInterpolation.java
 */

package ika.transformation;

import Jama.*;
import Jama.util.*;
import java.awt.geom.*;
import ika.geo.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */

public class MultiquadraticInterpolation {
    
    private double[] aCoeffArray;
    
    private double[] bCoeffArray;
    
    private double[][] destControlPoints;
    
    public MultiquadraticInterpolation() {
    }
    
    /**
     * Compute the coefficients for the multiquadratic interpolation
     * @param srcPoints The control point set of the start coordinate
     * system. This set is transformed to the destination coordinate system
     * @param dstPoints The control points set of the destination coordinate
     * system.
     */
    public void solveCoefficients(double[][] srcPoints, double[][] dstPoints,
            double exaggerationFactor) {
        
        if (srcPoints.length == 0 || srcPoints.length != dstPoints.length)
            throw new IllegalArgumentException();
        
        this.destControlPoints = dstPoints;
        int nbrPts = srcPoints.length;
        
        // differences between the two sets of points
        double[][] u = new double[nbrPts][1];   // differences in x direction
        double[][] w = new double[nbrPts][1];   // differences in y direction
        for (int i=0; i < nbrPts; i++){
            u[i][0] = (dstPoints[i][0] - srcPoints[i][0]) * exaggerationFactor;
            w[i][0] = (dstPoints[i][1] - srcPoints[i][1]) * exaggerationFactor;
        }
        
        // Fill coefficient matrix D (see Beineke p. 30).
        // Java automatically initializes arrays of doubles with 0.
        // So there is no need to initialize the elements on the diagonal with 0.
        double[][] D = new double[nbrPts][nbrPts];
        
        // D is quadratic and symetric
        for (int i = 0; i < nbrPts; i++){
            for (int j = i + 1; j < nbrPts; j++){
                final double dx = dstPoints[i][0]-dstPoints[j][0];
                final double dy = dstPoints[i][1]-dstPoints[j][1];
                D[i][j] = D[j][i] = Math.sqrt(dx*dx+dy*dy);
            }
        }
        
        // solve u = Da and w = Db for a and b
        LUDecomposition luDecomposition = new LUDecomposition(new Matrix(D));
        Matrix mat_a = luDecomposition.solve(new Matrix(u));
        Matrix mat_b = luDecomposition.solve(new Matrix(w));
        
        // store a and b
        this.aCoeffArray = new double[nbrPts];
        this.bCoeffArray = new double[nbrPts];
        final double[][] a = mat_a.getArray();
        final double[][] b = mat_b.getArray();
        for (int i = 0; i < nbrPts; i++){
            this.aCoeffArray[i] = -a[i][0];
            this.bCoeffArray[i] = -b[i][0];
        }
    }
    
    /**
     * Transforms a set of points.
     * @param points The points to be transformed as a an array[n] of xy-arrays[2].
     * points is changed, i.e. the old values are replaced by the new values.
     */
    public void transform(double[][] points) {
        
        final int nbrPts = points.length;
        
        double corrX, corrY;
        
        // loop over all points
        for(int i=0; i < nbrPts; i++){
            corrX = corrY = 0;
            
            // for each point: compute the distance to each control point
            for(int j=0; j < this.aCoeffArray.length; j++){
                final double dx = points[i][0] - this.destControlPoints[j][0];
                final double dy = points[i][1] - this.destControlPoints[j][1];
                final double d = Math.sqrt(dx*dx+dy*dy);
                
                corrX += this.aCoeffArray[j] * d;
                corrY += this.bCoeffArray[j] * d;
            }
            
            points[i][0] += corrX;
            points[i][1] += corrY;
        }
    }
    
    /**
     * Transforms a set of points.
     * @param coords The points to be transformed as a an array of x-y pairs. This
     * array is changed, i.e. the old values are replaced by the new values.
     * @param nbrPts The number of xy pairs that will be transformed.
     */
    public void transform(double[] coords, int nbrPts) {
        double corrX, corrY;
        for (int i = 0; i < nbrPts; ++i) {
            corrX = corrY = 0;
            
            for(int j=0; j < this.aCoeffArray.length; j++){
                
                // compute the distance to each control point
                final double dx = coords[i*2] - this.destControlPoints[j][0];
                final double dy = coords[i*2+1] - this.destControlPoints[j][1];
                final double d = Math.sqrt(dx*dx+dy*dy);
                
                corrX += this.aCoeffArray[j] * d;
                corrY += this.bCoeffArray[j] * d;
            }
            
            coords[i*2] += corrX;
            coords[i*2+1] += corrY;
        }
    }
    
    /**
     * Transforms a GeneralPath.
     */
    public GeneralPath transform(GeneralPath generalPath) {
        java.awt.geom.PathIterator pi = generalPath.getPathIterator(null);
        double [] coords = new double [6];
        int segmentType;
        
        GeneralPath newGeneralPath = new GeneralPath();
        while (pi.isDone() == false) {
            segmentType = pi.currentSegment(coords);
            switch (segmentType) {
                case java.awt.geom.PathIterator.SEG_CLOSE:
                    newGeneralPath.closePath();
                    break;
                case java.awt.geom.PathIterator.SEG_LINETO:
                    transform(coords, 1);
                    newGeneralPath.lineTo((float)coords[0], (float)coords[1]);
                    break;
                case java.awt.geom.PathIterator.SEG_MOVETO:
                    transform(coords, 1);
                    newGeneralPath.moveTo((float)coords[0], (float)coords[1]);
                    break;
                case java.awt.geom.PathIterator.SEG_QUADTO:
                    transform(coords, 2);
                    newGeneralPath.quadTo((float)coords[0], (float)coords[1],
                            (float)coords[2], (float)coords[3]);
                    break;
                case java.awt.geom.PathIterator.SEG_CUBICTO:
                    transform(coords, 3);
                    newGeneralPath.curveTo((float)coords[0], (float)coords[1],
                            (float)coords[2], (float)coords[3],
                            (float)coords[4], (float)coords[5]);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            // move to next segment
            pi.next();
        }
        
        return newGeneralPath;
    }
    
    public GeoPath transform(GeoPath geoPath) {
        /*
         if (geoPath == null)
            throw new IllegalArgumentException();
        
        GeoPath newGeoPath = new GeoPath();
        newGeoPath.setPath(this.transform(geoPath.getPath()));
        newGeoPath.setVectorSymbol((VectorSymbol)geoPath.getVectorSymbol().clone());
        return newGeoPath;
         */
        throw new UnsupportedOperationException("MultiquadraticInterpolation.transform not implemented yet.");
    }
    
    public GeoPoint transform(GeoPoint geoPoint) {
        if (geoPoint == null)
            throw new IllegalArgumentException();
        double point [] = {geoPoint.getX(), geoPoint.getY()};
        this.transform(point, 1);
        GeoPoint newGeoPoint= new GeoPoint(point[0], point[1]);
        return newGeoPoint;
    }
    
    public GeoSet transform(GeoSet geoSet) {
        if (geoSet == null)
            throw new IllegalArgumentException();
        
        GeoSet newGeoSet = new GeoSet();
        final int nbrGeoObjects = geoSet.getNumberOfChildren();
        for (int i = 0; i < nbrGeoObjects; ++i) {
            GeoObject geoObject = geoSet.getGeoObject(i);
            GeoObject transformedGeoObj = null;
            if (geoObject instanceof GeoSet)
                transformedGeoObj = this.transform((GeoSet)geoObject);
            else if (geoObject instanceof GeoPath)
                transformedGeoObj = this.transform((GeoPath)geoObject);
            else if (geoObject instanceof GeoPoint)
                transformedGeoObj = this.transform((GeoPoint)geoObject);
            newGeoSet.add(transformedGeoObj);
        }
        return newGeoSet;
    }
    
    /**
     * output the coefficients of the mutltiquadradtc interpolation
     */
    public void printCoefficients(){
        System.out.println("Coefficient a:");
        for(int i=0; i < this.aCoeffArray.length; i++){
            System.out.println(this.aCoeffArray[i]);
        }
        System.out.println("Coefficient b:");
        for(int i=0; i < this.bCoeffArray.length; i++){
            System.out.println(this.bCoeffArray[i]);
        }
    }

}