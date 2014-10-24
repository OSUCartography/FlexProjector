/*
 * GeoProjector.java
 *
 * Created on June 5, 2007, 11:08 PM
 *
 */
package ika.geo;

import com.jhlabs.map.proj.ProjectionException;
import ika.gui.ProgressIndicator;
import com.jhlabs.map.proj.Projection;
import java.awt.geom.Point2D;

/**
 * Projects GeoObjects.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GeoProjector {

    /**
     * The projection to use.
     */
    private final Projection projection;
    /**
     * A projector for closed polygons.
     */
    private PolygonProjector polygonProjector;
    /**
     * A projector for open polylines.
     */
    private LineProjector lineProjector;

    private ProgressIndicator progressIndicator;

    /** Creates a new instance of Projector */
    public GeoProjector(Projection projection, ProgressIndicator progressIndicator) {

        this.projection = projection;
        this.progressIndicator = progressIndicator;
        this.polygonProjector = new PolygonProjector(projection);
        this.lineProjector = new LineProjector(projection);

    }

    public GeoProjector(Projection projection) {

        this.projection = projection;
        this.polygonProjector = new PolygonProjector(projection);
        this.lineProjector = new LineProjector(projection);

    }

    /**
     * Projects a GeoSet. Suspends triggering of events while the features in
     * the set are projected.
     * @param geoSet
     */
    public void project(GeoSet geoSet) {

        MapEventTrigger trigger = new MapEventTrigger(geoSet);
        try {
            final int featureCount = geoSet.getNumberOfChildren();
            for (int i = 0; i < featureCount; i++) {

                if (this.progressIndicator != null) {
                    final int perc = (100 * i + 1) / featureCount;
                    if (!this.progressIndicator.progress(perc)) {
                        System.out.println ("abort projection");
                        return;
                    }
                }
                
                final GeoObject geoObject = geoSet.getGeoObject(i);
                if (geoObject instanceof GeoPath) {
                    this.project((GeoPath) geoObject);
                } else if (geoObject instanceof GeoPoint) {
                    this.project((GeoPoint) geoObject);
                } else if (geoObject instanceof GeoSet) {
                    this.project((GeoSet) geoObject);
                }
            }
        } finally {
            trigger.inform();
        }

    }

    /**
     * Projects a GeoPoint.
     * @param geoPoint
     */
    final public void project(GeoPoint geoPoint) {

        try {
            Point2D.Double dst = new Point2D.Double();
            projection.transform(geoPoint.getX(), geoPoint.getY(), dst);
            geoPoint.setXY(dst.x, dst.y);
        } catch (ProjectionException exc) {
            geoPoint.setXY(Double.NaN, Double.NaN);
            return;
        }

    }

    /**
     * Projects a GeoPath.
     * @param geoPath
     */
    public void project(GeoPath geoPath) {

        if (geoPath.isClosed()) {
            this.polygonProjector.projectClosedPath(geoPath);
        } else {
            this.lineProjector.projectOpenPath(geoPath);
        }

    }

    public void setAddIntermediatePointsAlongCurves(boolean addIntermediatePointsAlongCurves) {
        this.polygonProjector.setAddIntermediatePointsAlongCurve(addIntermediatePointsAlongCurves);
        this.lineProjector.setAddIntermediatePointsAlongCurve(addIntermediatePointsAlongCurves);
    }

    public void setCurveTolerance(double curveTolerance) {
        this.polygonProjector.setCurveTolerance(curveTolerance);
        this.lineProjector.setCurveTolerance(curveTolerance);
    }
}
