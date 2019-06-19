package ika.proj;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;

public class ProjectionFactors {

    /**
     * meridional scale
     */
    public double h;

    /**
     * parallel scale
     */
    public double k;

    /**
     * angular distortion
     */
    public double omega;

    /**
     * theta prime
     */
    public double thetap;

    /**
     * convergence
     */
    public double conv;

    /**
     * areal scale factor
     */
    public double s;

    /**
     * max scale error
     */
    public double a;

    /**
     * min scale error
     */
    public double b;

    /**
     * Initialize the values.
     *
     * @param projection The projection to use.
     * @param lam The longitude in radians.
     * @param phi The latitude in radians.
     * @param dh Delta for computing derivatives.
     */
    public void compute(Projection projection, double lam, double phi, double dh) {

        double cosphi, t;
        final double EPS = 1.0e-12;

        // check for latitude or longitude over-range
        if ((t = Math.abs(phi) - MapMath.HALFPI) > EPS || Math.abs(lam) > 10.) {
            throw new ProjectionException("-14");
        }

        // errno = pj_errno = 0;
        if (Math.abs(t) <= EPS) {
            phi = phi < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
        }
        /* else if (P->geoc) FIXME
                    phi = atan(P->rone_es * tan(phi));
         */
        lam = MapMath.normalizeLongitude(lam - projection.getProjectionLongitude()); // compute del lam
        /* FIXME
                if (!P->over)
                    lam = adjlon(lam); // adjust del longitude
         */
 /* FIXME
                if (P->spc)	// get what projection analytic values
                    P->spc(lp, P, fac);
         */
 /* FIXME
                if (((fac->code & (IS_ANAL_XL_YL+IS_ANAL_XP_YP)) !=
                        (IS_ANAL_XL_YL+IS_ANAL_XP_YP)) &&
                        pj_deriv(lp, dh, P, &der))
                    return 1;
                if (!(fac->code & IS_ANAL_XL_YL)) {
                    fac->der.x_l = der.x_l;
                    fac->der.y_l = der.y_l;
                }
                if (!(fac->code & IS_ANAL_XP_YP)) {
                    fac->der.x_p = der.x_p;
                    fac->der.y_p = der.y_p;
                }*/

        ProjectionDerivatives der = new ProjectionDerivatives(projection, lam, phi, dh);
        cosphi = Math.cos(phi);
        /*
            if (!(fac->code & IS_ANAL_HK)) {
                fac->h = hypot(fac->der.x_p, fac->der.y_p);
                fac->k = hypot(fac->der.x_l, fac->der.y_l) / cosphi;
                if (P->es) {
                    t = sin(phi);
                    t = 1. - P->es * t * t;
                    n = sqrt(t);
                    fac->h *= t * n / P->one_es;
                    fac->k *= n;
                    r = t * t / P->one_es;
                } else
                    r = 1.;
            } else if (P->es) {
                r = sin(phi);
                r = 1. - P->es * r * r;
                r = r * r / P->one_es;
            } else
                r = 1.;
         */

        // h = sqrt(E) = sqrt(dx/dphi*dx/dphi + dy/dphi*dy/dphi)
        // Math.hypot is computing the square root of the sum of the squared numbers.
        this.h = Math.hypot(der.x_p, der.y_p);
        // k = sqrt(G)/cosphi = sqrt(dx/dlam*dx/dlam + dy/dlam*dy/dlam)/cosphi
        this.k = Math.hypot(der.x_l, der.y_l) / cosphi;

        /* FIXME
                // convergence
                if (!(fac->code & IS_ANAL_CONV)) {
                    fac->conv = - atan2(fac->der.y_l, fac->der.x_l);
                    if (fac->code & IS_ANAL_XL_YL)
                        fac->code |= IS_ANAL_CONV;
                }
         */
        // areal scale factor
        // Reference: Canters, Small-scale Map Projection Design
        // equation 1.8 (p. 9) and equation 1.20 (page 11)
        // First Gaussian fundamental quantities with Equation 1.8: EE, FF, GG 
        // then compute areal scale factor with equation 1.20
        // s = sqrt(EE*GG-FF*FF)/(R*R*cos(phi)
        // Here, R = 1.
        // This can be simplified to the single line of code below.
        // Thanks to Bojan Savric for his help with this!
        this.s = Math.abs(der.x_p * der.y_l - der.y_p * der.x_l) / cosphi;

        // meridian-parallel angle theta prime
        this.thetap = Math.asin(s / (h * k));

        // Tissot ellipse axis
        t = k * k + h * h;
        this.a = Math.sqrt(t + 2. * s);
        t = (t = t - 2. * s) <= 0. ? 0. : Math.sqrt(t);
        this.b = 0.5 * (a - t);
        this.a = 0.5 * (a + t);

        // omega
        this.omega = 2. * Math.asin((a - b) / (a + b));
    }
}
