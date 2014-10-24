/*
 * GridSlopeLineOperator.java
 *
 * Created on May 25, 2006, 2:23 PM
 *
 */

package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;
import ika.geo.GeoPath;
import ika.geo.GeoRefGrid;
import ika.geo.VectorSymbol;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridFalllineOperator implements GridOperator {

    public class WeightedGeoPath extends GeoPath{
        public double w;
    }
    
    
    public enum SearchMethod {UP, DOWN, UP_THEN_DOWN, THROUGH_POINT};
    
    private static final double HALFPI = Math.PI / 2.;
    
    private SearchMethod searchMethod = SearchMethod.THROUGH_POINT;
    
    /**
     * Follow lines until their slope is flatter than minSlopeDegree.
     */
    private float minSlopeDegree;
    
    private double startX;
    
    private double startY;
    
    /** Creates a new instance of GridSlopeLineOperator */
    public GridFalllineOperator() {
    }
    
    public String getName() {
        return "Slope Line";
    }
    
    public WeightedGeoPath operate(GeoGrid elevationGrid) {
        return operate(elevationGrid, null, null, null, 0, null);
    }
    
    /**
     * Searches a fall line following maximum slope.
     * @param elevationGrid
     * @param refGrid Searching the fall line stops when a cell is reached in 
     * the refGrid that already contains a reference to another object. refGrid can be 
     * null. The found fall line does not have a width, i.e. line ends behave 
     * like butt caps.
     * @param clipArea
     * @param curvatureGrid
     * @param minCurvature 
     * @return
     */
    public WeightedGeoPath operate(GeoGrid elevationGrid,
            GeoRefGrid refGrid,
            GeoPath clipArea,
            GeoGrid curvatureGrid,
            float minCurvature,
            GeoGrid mask255) {

        if (elevationGrid == null)
            throw new IllegalArgumentException();
        
        // make sure the start point is on the grid
        Rectangle2D bounds = elevationGrid.getBounds2D(GeoObject.UNDEFINED_SCALE);
        if (bounds.contains(getStartX(), getStartY()) == false) {
            return null;
        }

        WeightedGeoPath path;
        switch (this.searchMethod) {
            case THROUGH_POINT:
                 {
                    // search line passing through the point startX/startY
                    path = extractSlopeLine(elevationGrid, startX, startY, true, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                    if (path == null) {
                        path = extractSlopeLine(elevationGrid, startX, startY, false, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                    } else {
                        path.invertDirection();
                        path.append(extractSlopeLine(elevationGrid, startX, startY, false, refGrid, clipArea, curvatureGrid, minCurvature, mask255), true);
                    }

                }
                break;

            case UP_THEN_DOWN:
                 {
                    // follow steepest line upwards to highest point, and then downslope again.
                    // the resulting line does not necesseraly pass through startX/startY
                    // go upwards
                    path = extractSlopeLine(elevationGrid, startX, startY, false, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                    if (path == null) {
                        return null;
                    }

                    // start from highest point and go downwards
                    path.removeLastPoint();
                    Point2D endPoint = path.getEndPoint();
                    path = extractSlopeLine(elevationGrid, endPoint.getX(), endPoint.getY(), true, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                }
                break;
                
            case DOWN:
                // only search downslope line
                path = extractSlopeLine(elevationGrid, startX, startY, true, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                break;
                
            case UP:
                // only search upslope line
                path = extractSlopeLine(elevationGrid, startX, startY, false, refGrid, clipArea, curvatureGrid, minCurvature, mask255);
                break;
                
            default:
                path = null;
        }

        if (path == null) {
            return null;
        }

        VectorSymbol vs = new VectorSymbol();
        vs.setScaleInvariant(true);
        path.setVectorSymbol(vs);
        return path;
    }

    /**
     * Finds the next point of a slope line.
     * @param elevationGrid The DEM.
     * @param pt On entry, contains the start point. On exit, contains the new point.
     * @param downwards If true, search in downward direction, upwards otherwise.
     * @return The direction of the found line in radians in CCW direction 
     * starting from the positive horizontal x axis.
     */
    private double findNextPoint(GeoGrid elevationGrid, Point2D pt, boolean downwards) {
        
        double x = pt.getX();
        double y = pt.getY();
        
        double dir = elevationGrid.getAspect(x, y);
        if (downwards) {
            dir += Math.PI;
            if (dir > Math.PI)
                dir -= 2. * Math.PI;
        }
       
        double tanDir = Math.tan(dir);
        
        final double cellSize = elevationGrid.getCellSize();
        final double west = elevationGrid.getWest();
        final double north = elevationGrid.getNorth();
        
        // *** intersect with horizontal line
        // compute y value of upper or lower border of the cell that contains x/y
        // intersection with upper or lower border of cell?
        final int lowerIntersection = dir < 0. ? 1 : 0;
        double y1 = north - cellSize * (Math.floor((north - y) / cellSize) + lowerIntersection);
        if (y1 == y)	// special case if x/y is on grid node
            y1 += cellSize;
        final double dy1 = y1 - y;        // ver. distance from upper border of cell to point
        final double dx1 = dy1 / tanDir;  // hor. distance from left border of cell to point
        final double dist1_square = dx1*dx1 + dy1*dy1;
        
        // *** intersect with vertical line
        // intersection with right or left border of cell?
        final int rightIntersection = (dir > -HALFPI && dir < HALFPI) ? 1 : 0;
        // compute x value of right or left border of the cell that contains x/y
        double x2 = west + cellSize * (Math.floor((x - west) / cellSize) + rightIntersection);
        if (x2 == x)	// special case if x/y is on grid node
            x2 -= cellSize;
        final double dx2 = x2 - x;
        final double dy2 = tanDir * dx2;
        final double dist2_square = dx2*dx2 + dy2*dy2;
        
        // test which intersection point is closer to x/y
        if (dist1_square < dist2_square) {
            x += dx1;	// x of intersection with upper cell border
            y = y1;
        } else {
            x = x2;
            y += dy2;
        }
        
        pt.setLocation(x, y);
        return dir;
    }
    
    /**
     * Extracts a slope line in upward or downward direction.
     * @param elevationGrid The DEM.
     * @param seedX The position to start searching.
     * @param seedY The position to start searching.
     * @param downwards Search in upward or downward direction.
     * @param refGrid A grid with references to obstacles that the slope line must
     * not touch. refGrid can be null.
     * @param clipPolygon The slope line must be inside this polygon. 
     * clipPolygon can be null.
     * @param weightGrid A grid containing an attribute that is accumulated along
     * the slope line. Important: The accumulated attribute is only a rough guess, as
     * the cell values of the weightGrid are not weighted with the length of the 
     * corresponding line segments. weightGrid can be null.
     * @param minWeight Stop searching for the slope line if the value in weightGrid
     * is smaller than minWeight. Ignored if weightGrid is null.
     * @return A WeightedGeoPath with the accumulated attribute inside the 
     * clipPolygon. null is returned if no slope line can be found.
     */
    private WeightedGeoPath extractSlopeLine(GeoGrid elevationGrid, 
            double seedX,
            double seedY,
            boolean downwards, 
            GeoRefGrid refGrid,
            GeoPath clipPolygon,
            GeoGrid weightGrid,
            float minWeight,
            GeoGrid mask255) {
    
        final float minSlopeRadian = (float)Math.toRadians(minSlopeDegree);
        
        WeightedGeoPath polyLine = new WeightedGeoPath();
        double x = seedX;
        double y = seedY;
        
        Rectangle2D gridBounds = elevationGrid.getBounds2D(GeoObject.UNDEFINED_SCALE);
        Point2D pt = new Point2D.Double();
        
        while (gridBounds.contains(x, y)) {

            // stop when the found line leaves the clipping area
            if (clipPolygon != null) {
                if (!clipPolygon.contains(x, y)) {
                    break;
                }
            }

            // stop when slope line reaches area that is already occupied by another feature
            if (refGrid != null) {
                if (refGrid.getObjectsAtPosition(x, y) != null)
                    break;
            }
            
            // stop when curvature is too small
            if (weightGrid != null) {
                final float w = weightGrid.getNearestNeighbor(x, y) ;
                if (w < minWeight)
                    break;
            }

            if (mask255 != null) {
                final float w = mask255.getNearestNeighbor(x, y) ;
                if (w >= 255)
                    break;
            }

            // accumulate weight along the found slope line
            if (weightGrid != null) {
                polyLine.w += weightGrid.getNearestNeighbor(x, y) ;
            }
            
            // add point to line
            polyLine.moveOrLineTo(x, y);
            if (Math.abs(elevationGrid.getSlope(x, y)) < minSlopeRadian)
                break;

            // elevation of current point
            float z1 = elevationGrid.getBilinearInterpol(x, y);
            
            // find next point in slope line
            pt.setLocation(x, y);
            double dir = findNextPoint(elevationGrid, pt, downwards);
            if (Double.isNaN(dir))
                break;

            // elevation of new point
            x = pt.getX();
            y = pt.getY();
            float z2 = elevationGrid.getBilinearInterpol(x, y);

            // stop search if line has wrong slope direction
            if (downwards) {
                if (z1 <= z2) {
                    break;
                }
            } else {
                if (z1 >= z2) {
                    break;
                }
            }
            
        }

        if (polyLine.getPointsCount() < 2) {
            return null;
        }

        return polyLine;
    }

    public float getMinSlopeDegree() {
        return minSlopeDegree;
    }
    
    public void setMinSlopeDegree(float minSlopeDegree) {
        this.minSlopeDegree = minSlopeDegree;
    }
    
    public double getStartX() {
        return startX;
    }
    
    public void setStartX(double startX) {
        this.startX = startX;
    }
    
    public double getStartY() {
        return startY;
    }
    
    public void setStartY(double startY) {
        this.startY = startY;
    }
    
    public void setStart(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
    }
    
    public SearchMethod getSearchMethod() {
        return searchMethod;
    }

    public void setSearchMethod(SearchMethod searchMethod) {
        this.searchMethod = searchMethod;
    }
    
}
