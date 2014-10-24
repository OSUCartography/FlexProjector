package ika.proj;

import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.Projection;
import ika.geo.FlexProjectorModel;

/**
 * Abstract base class for projections that can be designed with Flex Projector.
 * All have an internal scale factor.
 * @author jenny
 */
public abstract class DesignProjection extends Projection implements SerializableProjection {

    public static DesignProjection factory(String str) {
        final DesignProjection proj;
        if (str.startsWith(MeanProjection.FORMAT_IDENTIFIER)) {
            proj = new MeanProjection();
        } else if (str.startsWith(LatitudeMixerProjection.FORMAT_IDENTIFIER)) {
            proj = new LatitudeMixerProjection();
        } else if (str.startsWith(FlexMixProjection.FORMAT_IDENTIFIER)) {
            proj = new FlexMixProjection();
        } else if (str.startsWith(FlexProjection.FORMAT_IDENTIFIER)
                || str.startsWith(FlexProjection.LEGACY_FORMAT_IDENTIFIER)) {
            proj = new FlexProjection();
        } else {
            throw new IllegalArgumentException();
        }

        proj.deserializeFromString(str);
        return proj;
    }

    /** Compute a scale factor that minimizes total areal distortion.
     * This is using a "Bisection method" search to find the
     * minimum of the total areal distortion, which changes with scale.
     * Faster methods could be used here (Newton-Raphson does not work,
     * however, because the first or second derivative of the
     * total areal distortion function behaves badly).
     */
    public void computeAreaDistortionMinimizingScale() {

        double eps = 1e-5; // iterate until the fourth digit after the coma is stable.
        double s1 = 0.5; // lower boundary of possible scale values.
        double s2 = 1.5; // upper boundary of possible scale values.
        double s = 1; // the new scale
        double diff; // difference between the previous and the new scale.

        // work with a clone to make sure nothing is changed
        DesignProjection projClone = (DesignProjection) this.clone();
        projClone.setProjectionLongitude(0);
        projClone.initialize();

        do {
            projClone.setScale(s - 1e-6);
            final double d1 = ProjectionDistortionParameters.getDarIndex(projClone);

            projClone.setScale(s + 1e-6);
            final double d2 = ProjectionDistortionParameters.getDarIndex(projClone);
            if (d1 < d2) {
                s2 = s; // change upper boundary
            } else {
                s1 = s; // adjust lower boundary
            }
            double s_ = (s1 + s2) / 2;
            diff = Math.abs(s - s_);
            s = s_;
        } while (diff > eps);

        // set the scale of this projection
        setScale(s);
    }

    /**
     * Computes a scale factor such that the resulting graticule has the area of
     * a sphere with a radius of 6371008.7714 m.
     */
    public void adjustScaleToEarthArea() {

        // remember the initial scale
        final double initialScale = getScale();

        try {
            // temporarily set the scale to 1
            setScale(1.);

            DesignProjection proj = (DesignProjection) this.clone();
            proj.setEllipsoid(Ellipsoid.SPHERE);
            final double earthRadius = Ellipsoid.SPHERE.getEquatorRadius();
            ika.geo.GeoPath outline = FlexProjectorModel.constructOutline(proj);
            final double flexGraticuleArea = outline.getArea();
            final double sphereArea = 4. * Math.PI * earthRadius * earthRadius;
            setScale(Math.sqrt(sphereArea / flexGraticuleArea));
        } catch (Throwable t) {
            // reset to inital scale.
            setScale(initialScale);
        }
    }

    /**
     * Adjusts the scale factor of this projection such that the areal scale
     * factor equals 1 at the position lam / phi. In other words, the scale factor
     * is adjusted, such that there is no areal distortion at lam / phi. This
     * uses an iterative procedure that may take some time.
     * @param lam The longitude at which areal distortion will be eliminated. In radians.
     * @param phi The latitude at which areal distortion will be eliminated. In radians.
     */
    public void eliminateAreaDistortionForPoint(double lam, double phi) {

        // remember the initial scale
        final double initialScale = getScale();

        try {

            final double EPS = 1e-5; // iterate until the fourth digit after the coma is stable.
            double s1 = 0.1; // lower boundary of possible scale values.
            double s2 = 1.9; // upper boundary of possible scale values.
            double s = (s1 + s2) / 2; // the new scale
            double diff; // difference between the previous and the new scale.
            int iterationCounter = 0;
            do {
                final double d = localArealDist(s, lam, phi);
                if (d > 1) {
                    s2 = s;
                } // change upper boundary
                else {
                    s1 = s;
                } // adjust lower boundary
                double s_ = (s1 + s2) / 2;
                diff = Math.abs(s - s_);
                s = s_;
                iterationCounter++;
                if (iterationCounter > 1000) {
                    break;
                }
            } while (diff > EPS);

            setScale(s);

        } catch (Throwable t) {
            // reset to inital scale.
            setScale(initialScale);
        }
    }

    /**
     * Returns the areal scale factor at point lon/lat
     * @param scale The scale factor of the projection. This scale factor
     * temporarily replaces the current scale factor of this projection for
     * computing the areal scale factor.
     * @param lon Longitude where the areal scale factor is computed in radians.
     * @param lat Latitude where the areal scale factor is computed in radians.
     * @return The areal scale factor at lam/phi
     */
    private double localArealDist(double scale, double lon, double lat) {

        final double d = 1e-5;

        DesignProjection proj = (DesignProjection)this.clone();
        proj.setScale(scale);
        proj.initialize();

        ProjectionFactors f = new ProjectionFactors();
        f.compute(proj, lon, lat, d);
        return f.s;

    }

    /**
     * @return the scale
     */
    public abstract double getScale();

    /**
     * @param scale the scale to set
     */
    public abstract void setScale(double scale);

    public abstract double getVerticalScale();

    public abstract void setVerticalScale(double vScale);

}
