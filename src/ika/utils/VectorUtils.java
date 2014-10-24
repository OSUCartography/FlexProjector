/*
 * VectorUtils.java
 *
 * Created on January 12, 2006, 11:11 AM
 *
 */

package ika.utils;

import java.awt.geom.*;
/**
 *
 * @author jenny
 */
public class VectorUtils {
    
    /**
     * Mirror a point on a line. The line is defined by two points.
     * See http://mathworld.wolfram.com/Reflection.html
     * @param p The point to mirror. Will be changed.
     * @param p1 A point on the line.
     * @param p2 Another point on the line.
     */
    static public void mirrorPointOnLine (Point2D p, Point2D p1, Point2D p2) {
        
        final double px = p.getX();
        final double py = p.getY();
        
        final double p1x = p1.getX();
        final double p1y = p1.getY();
        
        final double p2x = p2.getX();
        final double p2y = p2.getY();
        
        double[] rp = VectorUtils.mirrorPointOnLine (px, py, p1x, p1y, p2x, p2y);
        p.setLocation (rp[0], rp[1]);
    }
    
    /**
     * Mirror a point on a line. The line is defined by two points.
     * See http://mathworld.wolfram.com/Reflection.html
     * @param px The point to mirror.
     * @param py The point to mirror.
     * @param p1x A point on the line.
     * @param p1y A point on the line.
     * @param p2x Another point on the line.
     * @param p2y Another point on the line.
     * @return An array with two double values (x / y).
     */
    static public double[] mirrorPointOnLine (double px, double py,
            double p1x, double p1y, double p2x, double p2y) {

        // unary normal vector on line
        double nx = p1x - p2x;
        double ny = p1y - p2y;
        final double nl = Math.sqrt(nx*nx + ny*ny);
        nx /= nl;
        ny /= nl;
                
        // vector from p1 to p
        final double dx = px - p1x;
        final double dy = py - p1y;
        
        // compute the length of the vector from p1 to p with the dot product
        final double l = dx * nx + dy * ny;
        
        // give the vector a direction and add p1
        final double vx = nx * l + p1x;
        final double vy = ny * l + p1y;
        
        // compute the reflected point
        final double rx = -px + 2 * vx;
        final double ry = -py + 2 * vy;
        
        return new double[]{rx, ry};
    }
}
