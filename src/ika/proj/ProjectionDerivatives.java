package ika.proj;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import java.awt.geom.Point2D;

/**
 * First derivatives for a projection. Based on proj4.
 */
public class ProjectionDerivatives {

    // derivatives of x for lambda-phi
    public double x_l = Double.NaN;
    public double x_p = Double.NaN;
    // derivatives of y for lambda-phi
    public double y_l = Double.NaN;
    public double y_p = Double.NaN;

    private final java.awt.geom.Point2D.Double t = new Point2D.Double();

    /**
     * compute derivatives for a passed location
     * FIXME: does not return correct values along the border (e.g. lam= -90 / phi= 0)
     * @param projection
     * @param lam
     * @param phi
     * @param h
     */
    public final void compute(Projection projection, double lam, double phi, double h) {

        if (lam + h > Math.PI) {
            lam = Math.PI - h;
        } else if (lam - h < -Math.PI) {
            lam = -Math.PI + h;
        }

        if (phi + h > MapMath.HALFPI) {
            phi = MapMath.HALFPI - h;
        } else if (phi - h < -MapMath.HALFPI) {
            phi = -MapMath.HALFPI + h;
        }

        if (lam + h > Math.PI || lam - h < -Math.PI || phi + h > Math.PI / 2 || phi - h < -Math.PI / 2) {
            // FIXME
            System.err.println("Derivative out of bounds: " + projection.toString() + " " + Math.toDegrees(lam) + " " + Math.toDegrees(phi));
        }

        lam += h;
        phi += h;
        if (Math.abs(phi) > MapMath.HALFPI) {
            throw new ProjectionException();
        }
        h += h;
        projection.project(lam, phi, t);
        if (Double.isNaN(t.x)) {
            throw new ProjectionException();
        }
        x_l = t.x;
        y_p = t.y;
        x_p = -t.x;
        y_l = -t.y;
        phi -= h;
        if (Math.abs(phi) > MapMath.HALFPI) {
            throw new ProjectionException();
        }
        projection.project(lam, phi, t);
        if (Double.isNaN(t.x)) {
            throw new ProjectionException();
        }
        x_l += t.x;
        y_p -= t.y;
        x_p += t.x;
        y_l -= t.y;
        lam -= h;
        projection.project(lam, phi, t);
        if (Double.isNaN(t.x)) {
            throw new ProjectionException();
        }
        x_l -= t.x;
        y_p -= t.y;
        x_p += t.x;
        y_l += t.y;
        phi += h;
        projection.project(lam, phi, t);
        if (Double.isNaN(t.x)) {
            throw new ProjectionException();
        }
        x_l -= t.x;
        y_p += t.y;
        x_p -= t.x;
        y_l += t.y;

        x_l /= (h += h);
        y_p /= h;
        x_p /= h;
        y_l /= h;
    }
}