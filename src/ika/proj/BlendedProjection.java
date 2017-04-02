package ika.proj;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.utils.CatmullRomSpline;
import java.awt.geom.Point2D;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class BlendedProjection extends Projection {

    private Projection p1;
    private Projection p2;
    private CatmullRomSpline curve;
    private double xScale = 1d;

    public BlendedProjection() {
    }

    @Override
    public Object clone() {
        BlendedProjection clone = (BlendedProjection) super.clone();
        clone.p1 = (Projection) this.p1.clone();
        clone.p2 = (Projection) this.p2.clone();
        clone.curve = (CatmullRomSpline)curve.clone();
        return clone;
    }
    
    public void setProjections (Projection p1, Projection p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
    
    @Override
    public Point2D.Double project(double lon, double lat, Point2D.Double dst) {
        
        p1.project(lon, lat, dst);
        // copy to temporary variable to avoid the allocation of a Double2D object
        final double x = dst.x;
        final double y = dst.y;

        p2.project(lon, lat, dst);
        final double t = Math.abs(lat) / MapMath.HALFPI;
        final double w1 = curve.evaluate(t);
        if (w1 < 0 || w1 > 1) {
            System.err.println(t + " " + w1);
        }

        final double w2 = 1d - w1;
        dst.x = (x * w1 + dst.x * w2) * xScale;
        dst.y = y * w1 + dst.y * w2;

        return dst;
    }

    /**
     * @return the curve
     */
    public CatmullRomSpline getCurve() {
        return curve;
    }

    /**
     * @param curve the curve to set
     */
    public void setCurve(CatmullRomSpline curve) {
        this.curve = curve;
    }

    /**
     * @return the xScale
     */
    public double getxScale() {
        return xScale;
    }

    /**
     * @param xScale the xScale to set
     */
    public void setxScale(double xScale) {
        this.xScale = xScale;
    }

    @Override
    public String toString() {
        return "Blend of " + p1.toString() + " and " + p2.toString();
    }
}
