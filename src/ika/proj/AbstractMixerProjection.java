package ika.proj;

import com.jhlabs.map.proj.MillerCylindrical1Projection;
import com.jhlabs.map.proj.MollweideProjection;
import com.jhlabs.map.proj.Projection;

/**
 *
 * @author jenny
 */
public abstract class AbstractMixerProjection extends DesignProjection {

    protected Projection p1 = new MollweideProjection();
    protected Projection p2 = new MillerCylindrical1Projection();

    public Projection getProjection1() {
        return p1;
    }

    public void setProjection1(Projection p) {
        if (!canMix(p)) {
            throw new IllegalArgumentException("cannot mix projection " + p);
        }
        this.p1 = p;
    }

    public Projection getProjection2() {
        return p2;
    }

    public void setProjection2(Projection p) {
        if (!canMix(p)) {
            throw new IllegalArgumentException("cannot mix projection " + p);
        }
        this.p2 = p;
    }

    public boolean canMix(Projection p) {
        return true;
    }
    
}
