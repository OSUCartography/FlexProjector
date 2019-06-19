/*
 * FlexProjectorModel.java
 *
 * Created on May 16, 2007, 12:04 PM
 *
 */
package ika.geo;

import com.jhlabs.map.MapMath;
import ika.gui.ProjectionBrewerPanel;
import com.jhlabs.map.proj.*;
import ika.geo.grid.Contourer;
import ika.gui.FlexProjectorPreferencesPanel;
import ika.proj.*;
import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.Projection;
import ika.utils.GeometryUtils;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * The model object for the Flex Projector application. Holds all model data.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class FlexProjectorModel extends GeoMap implements Serializable,
        QModel.QListener, ProjectionBrewerPanel.DesignProjectionChangeListener {

    private static final long serialVersionUID = -56297773451900288L;
    /**
     * Number of points in a Tissot ellipse.
     */
    private static final int TISSOT_CIRCLE_POINT_COUNT = 40;
    /**
     * The radius of a Tissot circle on the sphere.
     */
    private static final double TISSOT_CIRCLE_RADIUS = 0.00000025;
    /**
     * Scale factor applied to Tissot indicatrices to approach an infinitesimal
     * size.
     */
    private static final double TISSOT_SCALE = 500000;
    /**
     * Stroke width used for drawing most elements of the map.
     */
    private static final float STROKE_WIDTH = 1f;
    private static final int GRID_COLS = 360;
    private static final int GRID_ROWS = 180;
    private static final double GRID_CELL_SIZE = 1;
    /**
     * tolerance for interpolating projected curved lines.
     */
    private static final double CURVE_TOLERANCE = 500;
    private final GeoGrid flexAngleGrid;
    private final GeoGrid flexAreaGrid;
    private final GeoGrid secondAngleGrid;
    private final GeoGrid secondAreaGrid;

    /**
     * Class modeling the display settings
     */
    public class DisplayModel implements Serializable {

        public static final int ADJUST_NO = 0;
        public static final int ADJUST_WIDTH = 1;
        public static final int ADJUST_HEIGHT = 2;
        public boolean showFlexProjection = true;
        public boolean showSecondProjection = false;
        public int secondProjectionAdjustment = ADJUST_NO;
        /**
         * second projection in the background
         */
        public Projection projection = ProjectionFactory.getNamedProjection("Kavrayskiy VII");
        public boolean showCoastline = true;
        public boolean showGraticule = true;
        public double graticuleDensity = 30;
        public boolean showTissot = false;
        public double tissotDensity = 30;
        public double tissotScale = 1;
        public boolean showAngularIsolines = false;
        public double angularIsolinesEquidistance = 20;
        public boolean showArealIsolines = false;
        public double arealIsolinesEquidistance = 0.5;
        public QModel qModel = new QModel();
        /**
         * An array containing the information displayed by the distortion
         * table. Access to distParams must be synchronized as it is used by two
         * threads. One thread is the Event Dispatching Thread, the other one is
         * a SwingWorker thread that fills the table with distortion indices.
         * Synchronization: synchronized (distParams) {...} Using a Vector that
         * is internally synchronized instead of an ArrayList is not a solution,
         * since the array is ordered by some external classes that do not
         * synchronize Vectors.
         */
        public final ArrayList<ProjectionDistortionParameters> distParams = new ArrayList();
        public final ProjectionDistortionParameters foreDist;

        public DisplayModel() {

            assert flexProjection != null;
            assert qModel != null;

            /*
            // initialize entries for foreground projections
            for (Projection p : flexProjections) {
            ProjectionDistortionParameters fdist;
            fdist = new ProjectionDistortionParameters(p, qModel);
            distParams.add(fdist);
            }
             */
            foreDist = new ProjectionDistortionParameters(designProjection, qModel);
            distParams.add(foreDist);

        }

        public ProjectionDistortionParameters getDistortionParameters(Projection proj) {

            for (ProjectionDistortionParameters p : distParams) {
                Class c1 = p.getProjection().getClass();
                Class c2 = proj.getClass();
                if (c1 == c2) {
                    return p;
                }
            }
            return null;

        }
    }
    /**
     * the flex projection that is used in this document.
     */
    private FlexProjection flexProjection = new FlexProjection();
    /**
     * linear mean of two projections
     */
    private MeanProjection meanProjection = new MeanProjection();
    /**
     * combines two projections along a latitude
     */
    private LatitudeMixerProjection combinedProjection = new LatitudeMixerProjection();
    /**
     * mixes two Flex projections
     */
    private FlexMixProjection flexMixProjection = new FlexMixProjection();
    /**
     * keeps track of the projection that is currently modified
     */
    private DesignProjection designProjection = flexProjection;
    /**
     * display settings.
     */
    private DisplayModel displayModel = new DisplayModel();
    /**
     * A GeoSet with unprojected GeoPath coast lines (or whatever data are
     * displayed).
     */
    private final GeoSet unprojectedData = new GeoSet();
    /**
     * projected data (coast lines, graticule, etc) is stored in this GeoSet
     */
    private final GeoSet projectedDataDestination = new GeoSet();

    /**
     * Creates a new instance of FlexProjectorModel
     */
    public FlexProjectorModel() {

        projectedDataDestination.setSelectable(false);
        this.add(this.projectedDataDestination);

        DesignProjection[] designProjections = getDesignProjections();
        for (Projection p : designProjections) {
            p.setProjectionLongitude(0);
            p.setEllipsoid(Ellipsoid.SPHERE);
            p.initialize();
        }

        final double west = -GRID_COLS / 2d;
        final double north = GRID_ROWS / 2d;

        this.flexAreaGrid = new GeoGrid(GRID_COLS, GRID_ROWS, GRID_CELL_SIZE);
        this.flexAreaGrid.setNorth(north);
        this.flexAreaGrid.setWest(west);

        this.flexAngleGrid = new GeoGrid(GRID_COLS, GRID_ROWS, GRID_CELL_SIZE);
        this.flexAngleGrid.setNorth(north);
        this.flexAngleGrid.setWest(west);

        this.secondAreaGrid = new GeoGrid(GRID_COLS, GRID_ROWS, GRID_CELL_SIZE);
        this.secondAreaGrid.setNorth(north);
        this.secondAreaGrid.setWest(west);

        this.secondAngleGrid = new GeoGrid(GRID_COLS, GRID_ROWS, GRID_CELL_SIZE);
        this.secondAngleGrid.setNorth(north);
        this.secondAngleGrid.setWest(west);

        this.displayModel.qModel.addQListener(this); // FIXME

        AbstractMixerProjection[] mixerProjections = getMixerProjections();
        for (AbstractMixerProjection p : mixerProjections) {
            Projection mixerProjection1 = searchProjectionForMixer(null, p);
            Projection mixerProjection2 = searchProjectionForMixer(mixerProjection1, p);
            p.setProjection1(mixerProjection1);
            p.setProjection2(mixerProjection2);
        }

    }

    private DesignProjection[] getDesignProjections() {
        return new DesignProjection[]{
            flexProjection,
            meanProjection,
            combinedProjection,
            flexMixProjection};
    }

    private AbstractMixerProjection[] getMixerProjections() {
        return new AbstractMixerProjection[]{
            meanProjection,
            combinedProjection,
            flexMixProjection};
    }

    private boolean isMixableProjection(String name,
            Projection projToExclude,
            AbstractMixerProjection mixer) {

        if (projToExclude != null && name.equals(projToExclude.toString())) {
            return false;
        }
        if (!ProjectionsManager.isProjectionSelected(name)) {
            return false;
        }
        Projection p = ProjectionFactory.getNamedProjection(name);
        return mixer.canMix(p);
    }

    /**
     * Searches a projection to be used when the mixer is first started.
     *
     * @param projToExclude Don't take this projection.
     */
    private Projection searchProjectionForMixer(Projection projToExclude,
            AbstractMixerProjection mixer) {

        String[] projNames = {"Eckert IV", "Winkel Tripel", "Robinson"};
        String name = projNames[0];
        for (String projName : projNames) {
            name = projName;
            if (isMixableProjection(name, projToExclude, mixer)) {
                break;
            }
        }

        // take the first available in the list of projections
        if (!isMixableProjection(name, projToExclude, mixer)) {
            java.util.List<String> selProjs = ProjectionsManager.getSelectedProjectionNames();
            for (String projName : selProjs) {
                name = projName;
                if (isMixableProjection(name, projToExclude, mixer)) {
                    break;
                }
            }
        }

        return ProjectionFactory.getNamedProjection(name);

    }

    /**
     * Use FlexProjectionModel.toString() instead.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    /**
     * Use FlexProjectionModel.fromString() instead.
     */
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }

    /**
     * Computes the visualizations for the map and redraws the map.
     */
    @Override
    public void designProjectionChanged(Projection p) {
        mapChanged();
    }

    /**
     * Returns a FlexProjection. A shared instance of this.flexProjectionModel,
     * not a copy.
     */
    public FlexProjection getFlexProjection() {
        return flexProjection;
    }

    public DesignProjection getDesignProjection() {
        return designProjection;
    }

    public void setDesignProjection(DesignProjection p) {
        if (p instanceof FlexMixProjection) {
            flexMixProjection = (FlexMixProjection) p;
        } else if (p instanceof FlexProjection) {
            flexProjection = (FlexProjection) p;
        } else if (p instanceof LatitudeMixerProjection) {
            combinedProjection = (LatitudeMixerProjection) p;
        } else if (p instanceof MeanProjection) {
            meanProjection = (MeanProjection) p;
        } else {
            throw new IllegalArgumentException();
        }
        designProjection = p;
    }

    public FlexMixProjection getFlexMixProjection() {
        return this.flexMixProjection;
    }

    public MeanProjection getMeanProjection() {
        return this.meanProjection;
    }

    public LatitudeMixerProjection getLatitudeMixerProjection() {
        return this.combinedProjection;
    }

    public Projection getMixerProjection1() {
        // return the mixed projection in the foreground
        if (designProjection instanceof AbstractMixerProjection) {
            return ((AbstractMixerProjection) designProjection).getProjection1();
        }
        return meanProjection.getProjection1();
    }

    public void setMixerProjection1(Projection p1) {
        if (designProjection instanceof AbstractMixerProjection) {
            AbstractMixerProjection mixP = (AbstractMixerProjection) designProjection;
            mixP.setProjection1(p1);
        }
    }

    public Projection getMixerProjection2() {
        // return the mixed projection in the foreground
        if (designProjection instanceof AbstractMixerProjection) {
            return ((AbstractMixerProjection) designProjection).getProjection2();
        }
        return meanProjection.getProjection2();
    }

    public void setMixerProjection2(Projection p2) {
        if (designProjection instanceof AbstractMixerProjection) {
            AbstractMixerProjection mixP = (AbstractMixerProjection) designProjection;
            mixP.setProjection2(p2);
        }
    }

    public void flexProjectionToForeground() {
        this.designProjection = flexProjection;
    }

    public void flexMixProjectionToForeground() {
        this.designProjection = flexMixProjection;
    }

    public void meanProjectionToForeground() {
        this.designProjection = meanProjection;
    }

    public void combinedProjectionToForeground() {
        this.designProjection = combinedProjection;
    }

    public VectorSymbol getForegroundVectorSymbol() {
        VectorSymbol symbol = new VectorSymbol(null,
                FlexProjectorPreferencesPanel.getFlexColor(), STROKE_WIDTH);
        symbol.setScaleInvariant(true);
        return symbol;
    }

    public VectorSymbol getBackgroundVectorSymbol() {
        VectorSymbol symbol = new VectorSymbol(null,
                FlexProjectorPreferencesPanel.getSecondColor(), STROKE_WIDTH);
        symbol.setScaleInvariant(true);
        return symbol;
    }

    /**
     * Computes the visualizations for the map and redraws the map.
     */
    public void mapChanged() {

        designProjection.initialize();

        if (unprojectedData.getNumberOfChildren() == 0) {
            return;
        }

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            projectedDataDestination.removeAllGeoObjects();

            GeoPath flexBoundingBox = constructBoundingBox(designProjection);
            java.awt.geom.Rectangle2D flexBounds = null;
            if (flexBoundingBox != null) {
                flexBounds = flexBoundingBox.getBounds2D(GeoObject.UNDEFINED_SCALE);
            }

            if (displayModel.showFlexProjection) {

                // destination GeoSet
                GeoSet flexGeoSet = new GeoSet();
                flexGeoSet.setName("Flex");
                projectedDataDestination.add(flexGeoSet);

                VectorSymbol symbol = this.getForegroundVectorSymbol();

                // Q acceptance
                if (displayModel.qModel.isShowAcceptableArea()) {
                    ProjectionDistortionParameters p;
                    p = displayModel.getDistortionParameters(designProjection);
                    GeoImage img = p.computeAcceptanceImage(displayModel.qModel);
                    flexGeoSet.add(img);
                }

                // copy and project unprojected coastlines
                if (displayModel.showCoastline) {
                    GeoSet geoSet = constructProjectedCoastlines(designProjection);
                    geoSet.setVectorSymbol(symbol);
                    flexGeoSet.add(geoSet);
                }

                // graticule
                if (displayModel.showGraticule) {
                    GeoSet graticule = constructGraticule(designProjection);
                    graticule.setVectorSymbol(symbol);
                    flexGeoSet.add(graticule);
                }

                // Tissot indicatrices
                if (displayModel.showTissot) {
                    GeoSet tiss = constructTissotIndicatrices(designProjection);
                    tiss.setVectorSymbol(symbol);
                    flexGeoSet.add(tiss);
                }

                // isolines
                if (displayModel.showAngularIsolines
                        || displayModel.showArealIsolines) {
                    flexGeoSet.add(constructIsolines(designProjection,
                            flexAreaGrid, flexAngleGrid));
                }

                // outline
                if (needsOutline(designProjection)) {
                    GeoPath outline = FlexProjectorModel.constructOutline(designProjection);
                    outline.setVectorSymbol(symbol);
                    GeoSet outlineGeoSet = constructOutlineGeoSet(outline);
                    flexGeoSet.add(outlineGeoSet);
                }

            }

            if (displayModel.showSecondProjection) {
                if (displayModel.projection == null) {
                    return;
                }

                GeoSet projGeoSet = new GeoSet();
                projGeoSet.setName(displayModel.projection.getName()); // toString instead of getName ? FIXME
                projectedDataDestination.add(0, projGeoSet);

                double lon0 = designProjection.getProjectionLongitude();
                displayModel.projection.setProjectionLongitude(lon0);
                displayModel.projection.setEllipsoid(Ellipsoid.SPHERE);
                displayModel.projection.initialize();

                VectorSymbol symbol = this.getBackgroundVectorSymbol();

                // Q acceptance
                if (this.displayModel.qModel.isShowAcceptableArea()) {
                    ProjectionDistortionParameters p;
                    p = displayModel.getDistortionParameters(displayModel.projection);
                    GeoImage img = p.computeAcceptanceImage(displayModel.qModel);
                    projGeoSet.add(img);
                }

                // copy and project unprojected data
                if (this.displayModel.showCoastline) {
                    GeoSet geoSet = constructProjectedCoastlines(displayModel.projection);
                    geoSet.setVectorSymbol(symbol);
                    projGeoSet.add(geoSet);
                }

                // graticule
                if (this.displayModel.showGraticule) {
                    GeoSet graticule = constructGraticule(displayModel.projection);
                    graticule.setVectorSymbol(symbol);
                    projGeoSet.add(graticule);
                }

                // Tissot indicatrices
                if (this.displayModel.showTissot) {
                    GeoSet tiss = constructTissotIndicatrices(displayModel.projection);
                    tiss.setVectorSymbol(symbol);
                    projGeoSet.add(tiss);
                }

                // outline
                GeoPath outline = FlexProjectorModel.constructOutline(displayModel.projection);
                if (this.needsOutline(displayModel.projection)) {
                    outline.setVectorSymbol(symbol);
                    GeoSet outlineGeoSet = this.constructOutlineGeoSet(outline);
                    projGeoSet.add(outlineGeoSet);
                }

                // isolines
                if (this.displayModel.showAngularIsolines
                        || this.displayModel.showArealIsolines) {
                    projGeoSet.add(constructIsolines(displayModel.projection,
                            secondAreaGrid, secondAngleGrid));
                }

                // scale the second projection to the size of the flexed projection
                Rectangle2D backBounds = outline.getBounds2D(GeoObject.UNDEFINED_SCALE);
                scaleBackgroundProjection(flexBounds, backBounds, projGeoSet);
            }
        } finally {
            trigger.inform();
        }

    }

    public void scaleBackgroundProjection(Rectangle2D foreBounds,
            Rectangle2D backBounds,
            GeoSet geoSet) {

        if (foreBounds == null || backBounds == null || geoSet == null) {
            return;
        }

        switch (displayModel.secondProjectionAdjustment) {
            case DisplayModel.ADJUST_WIDTH: {
                double flexW = foreBounds.getWidth();
                double projW = backBounds.getWidth();
                double scale = flexW / projW;
                double cx = backBounds.getCenterX();
                double cy = backBounds.getCenterY();
                geoSet.move(-cx, -cy);
                geoSet.scale(scale);
                geoSet.move(cx, cy);
                break;
            }
            case DisplayModel.ADJUST_HEIGHT: {
                double flexH = foreBounds.getHeight();
                double projH = backBounds.getHeight();
                double scale = flexH / projH;
                double cx = backBounds.getCenterX();
                double cy = backBounds.getCenterY();
                geoSet.move(-cx, -cy);
                geoSet.scale(scale);
                geoSet.move(cx, cy);
                break;
            }
        }
    }

    public GeoSet constructProjectedCoastlines(Projection projection) {
        GeoSet geoSet = (GeoSet) this.unprojectedData.clone();
        new GeoProjector(projection).project(geoSet);
        return geoSet;
    }

    /**
     * Returns true if the graticule does not coincide with the outline of the
     * projection, i.e. an outline must be generated.
     */
    private boolean needsOutline(Projection projection) {
        // always return true to fix bug in the proejction of the outline
        // constructed by constructLine(). One vertical line is not visible if
        // it falls on +180 or -180
        return true;
        /*
        final double minLon = projection.getProjectionLongitude() + projection.getMinLongitude();
        final double maxLon = projection.getProjectionLongitude() + projection.getMaxLongitude();
        final double minLat = projection.getMinLatitude();
        final double maxLat = projection.getMaxLatitude();

        final double gratDens = Math.toRadians(this.displayModel.graticuleDensity);
        final double TOL = 0.000001;

        return Math.abs(minLon % gratDens) > TOL
        || Math.abs(maxLon % gratDens) > TOL
        || Math.abs(minLat % gratDens) > TOL
        || Math.abs(maxLat % gratDens) > TOL;
         */
    }

    public GeoSet constructOutlineGeoSet(GeoPath outline) {

        GeoSet outlineGeoSet = new GeoSet();
        outlineGeoSet.add(outline);
        outlineGeoSet.setName("Outline");
        return outlineGeoSet;

    }

    /**
     * Returns the outline of the valid area of a projection in the projected
     * coordinate system.
     */
    public static GeoPath constructOutline(Projection projection) {

        projection = (Projection) projection.clone();
        projection.setProjectionLongitudeDegrees(0);
        projection.initialize();

        final double minLon = projection.getMinLongitude();
        final double maxLon = projection.getMaxLongitude();
        final double minLat = projection.getMinLatitude();
        final double maxLat = projection.getMaxLatitude();

        GeoPath outline = new GeoPath();
        outline.setName("Outline");

        // bottom line
        outline.moveTo(minLon, minLat);

        // right line
        outline.lineTo(maxLon, minLat);

        // top line
        outline.lineTo(maxLon, maxLat);

        // left line
        outline.lineTo(minLon, maxLat);

        // close the line
        outline.lineTo(minLon, minLat);

        // scale from
        outline.scale(180d / Math.PI);

        // project the outline
        LineProjector projector = new LineProjector(projection, CURVE_TOLERANCE, true);
        projector.projectOpenPath(outline);
        outline.closePath();

        return outline;
    }

    /**
     * Construct a rectangular bounding box around the outlines of the
     * projection. The bounding box includes the graticule and projected data,
     * but does not necesseraly include all Tissot indictrices.
     *
     * @return A rectangular path including the graticule.
     */
    public GeoPath constructBoundingBox(Projection projection) {

        GeoPath outline = FlexProjectorModel.constructOutline(projection);
        if (outline == null) {
            return null;
        }

        Rectangle2D bounds = outline.getBounds2D(GeoObject.UNDEFINED_SCALE);
        if (bounds == null) {
            return null;
        }

        GeoPath boundingBoxGeoPath = GeoPath.newRect(bounds);
        boundingBoxGeoPath.setName("Bounding Box");
        return boundingBoxGeoPath;

    }

    private void graticuleNextPoint(Projection projection,
            double lon1, double lat1,
            double lon2, double lat2,
            double x1, double y1,
            double x2, double y2,
            GeoPathModel projPath) {

        double tolerance = 1;

        final double lon3 = (lon1 + lon2) * 0.5;
        final double lat3 = (lat1 + lat2) * 0.5;
        Point2D.Double p3 = new Point2D.Double();
        try {
            projection.transform(lon3, lat3, p3);
        } catch (ProjectionException exc) {
            return;
        }
        if (Double.isNaN(p3.x) || Double.isNaN(p3.y)) {
            return;
        }

        final double d
                = GeometryUtils.pointLineDistanceSquare(p3.x, p3.y, x1, y1, x2, y2);
        if (d > tolerance) {
            graticuleNextPoint(projection, lon1, lat1, lon3, lat3, x1, y1, p3.x, p3.y, projPath);
        } else {
            projPath.lineTo(p3.x, p3.y);
        }
    }

    /**
     * Construct a graticule (a projected regularly spaced grid). The graticule
     * is projected.
     *
     * @return The projected graticule.
     */
    public GeoSet constructGraticule(Projection projection) {

        projection = (Projection) projection.clone();
        LineProjector projector = new LineProjector(projection, CURVE_TOLERANCE, true);
        GeoSet geoSet = new GeoSet();
        geoSet.setName("Graticule");

        final int linesPerHemisphere = (int) (180 / displayModel.graticuleDensity);

        final double maxLat = projection.getMaxLatitudeDegrees();
        final double minLat = projection.getMinLatitudeDegrees();

        // vertical meridian lines
        for (int i = -linesPerHemisphere; i <= linesPerHemisphere; i++) {
            final double x = i * displayModel.graticuleDensity;
            GeoPath geoPath = new GeoPath();
            geoPath.moveTo(x, maxLat);
            // Add an intermediat point at the equator. Othewrwise the projected 
            // graticule will be a wrong straight line for pseudocylindrical
            // projections that have a pole line with the same length as the 
            // equator. This is caused by the way intermediate points are added
            // by the LineProjector. It tests the middle point for each line
            // segment. If its distance from the line connecting the start and 
            // the end line is large enough, an intermediate point is recursevly
            // added. Problems aris when this intermediate point is on this line,
            // as in the case above.
            if (!projection.isRectilinear()) {
                geoPath.lineTo(x, 0);
            }
            geoPath.lineTo(x, minLat);

            projector.projectOpenPath(geoPath);
            geoSet.add(geoPath);
        }

        // horizontal parallels
        projection.setProjectionLongitudeDegrees(0);
        for (int j = -linesPerHemisphere / 2; j <= linesPerHemisphere / 2; j++) {
            GeoPath geoPath = new GeoPath();
            final double y = j * displayModel.graticuleDensity;
            if (y > maxLat || y < minLat) {
                continue;
            }
            geoPath.moveTo(-180, y);
            geoPath.lineTo(180, y);
            projector.projectOpenPath(geoPath);
            geoSet.add(geoPath);
        }

        return geoSet;
    }

    /**
     * Constructs an array of Tissot indicatrices for the passed projection.
     * 
     * @param projection The projection for which indicatrices are constructed.
     * @return A GeoSet containing the indicatrices as GeoPath objects.
     */
    public GeoSet constructTissotIndicatrices(Projection projection) {

        // remember the central meridian and set it to 0.
        final double lon0 = projection.getProjectionLongitude();
        projection.setProjectionLongitude(0);

        try {
            // the GeoSet that will contain all ellipses
            GeoSet geoSet = new GeoSet();
            geoSet.setName("Tissot's Indicatrices");

            // the distance between the centers of two neighboring ellipses
            final double ellDist = Math.toRadians(this.displayModel.tissotDensity);

            // the number of ellipses in vertical direction
            final int nVertical = (int) (Math.PI / ellDist);

            // container for the coordinates of a projected point
            java.awt.geom.Point2D.Double projPt = new java.awt.geom.Point2D.Double();

            // scale factor to convert from the unary sphere to earth coordinates
            final double scale = projection.getEquatorRadius();

            // scale factor to enlarge the small ellipses
            final double indicatrixScale
                    = TISSOT_SCALE * this.displayModel.tissotScale;

            // the number of ellipses per hemisphere.
            final int l = (int) Math.floor(Math.PI / ellDist) + 1;
            final int r = (int) Math.ceil(Math.PI / ellDist) + 1;

            ProjectionFactors projFactors = new ProjectionFactors();

            // construct the ellipses per columns, from left to right
            for (int col = -l; col <= r; col++) {

                // the longitude of the current column of ellipses
                final double lon = -lon0 % ellDist + col * ellDist;

                // make sure the longitude is in the range -pi..+pi
                if (lon < -Math.PI - 0.0000001 || lon > Math.PI + 0.0000001) {
                    continue;
                }

                // construct a column of indicatrices from bottom to top
                // Tissot indicatrices cannot be computed for poles
                for (int row = 1; row < nVertical; row++) {

                    try {
                        // the latitude of the current ellipse
                        double lat = -Math.PI / 2 + row * ellDist;

                        projFactors.compute(projection, lon, lat, 1e-5);

                        ProjectionDerivatives der = new ProjectionDerivatives(projection, lon, lat, 1e-5);

                        // compute Gaussian fundamental quantities E, F, G
                        // Canters 1.8
                        double E = der.E();
                        double F = der.F();
                        double G = der.G();

                        // angle between meridian and parallel. Canters 1.17
                        double sinthetap = Math.sqrt((E * G - F * F) / (E * G));

                        // scales along meridian and parallel. Canters 1.11 and 1.12
                        double h = der.h();
                        double k = der.k(lat);

                        // Snyder 4-12 and 4-13, p. 24. Scale factors.
                        double a_ = Math.sqrt(h * h + k * k + 2 * h * k * sinthetap);
                        double b_ = Math.sqrt(h * h + k * k - 2 * h * k * sinthetap);
                        // Snyder 4-12a and 4-13a, p. 24
                        double a = (a_ + b_) * 0.5;
                        double b = (a_ - b_) * 0.5;

                        // angle between meridians and parallels. Canters 1.16
                        // sine of sinthetap would not be correct
                        double thetap = Math.atan2(Math.sqrt(E * G - F * F), F);

                        // angle between major axis and parallel. Canters 1.34
                        double m = (1 - a * a / (k * k)) / (1 - a * a / (b * b));
                        m = Math.min(1, Math.max(0, m));
                        double alphap = Math.asin(Math.sqrt(m));
                        
                        // adjust sign. Canters p. 14 bottom and Fig. 1.5
                        if (thetap < MapMath.HALFPI) {
                            alphap = -alphap;
                        }

                        // angle between X-axis of map and parallel. Canters 1.36
                        double thetapp = Math.atan2(der.y_l, der.x_l);

                        // angle between X-axis and major axis. Canters 1.35
                        double orient = thetapp - alphap;

                        // compute the center of the indicatrix in Catesian coordinates
                        projection.project(lon, lat, projPt);
                        final double cx = scale * projPt.x;
                        final double cy = scale * projPt.y;
                        if (!Double.isFinite(cx) || !Double.isFinite(cy)) {
                            continue;
                        }

                        // construct the ellipse
                        GeoPath ellipse = GeoPath.newCircle(0, 0, (float) indicatrixScale);
                        ellipse.scale(a, b);
                        ellipse.rotate(orient);

                        ellipse.move(cx, cy);
                        geoSet.add(ellipse);

                    } catch (ProjectionException exc) {
                        System.err.println(exc);
                    }

                }
            }

            return geoSet;
        } finally {
            // reset to the initial central meridian
            projection.setProjectionLongitude(lon0);
        }
    }

    private void fillDistortionGrids(Projection projection,
            GeoGrid areaGrid, GeoGrid projGrid) {

        final double dh = 1e-5; // DEFAULT_H;

        // fill the grids
        ProjectionFactors projFactors = new ProjectionFactors();
        for (int r = 0; r < GRID_ROWS; r++) {
            final double lat = FlexProjectorModel.GRID_ROWS / 2 - r;
            for (int c = 0; c < GRID_COLS; c++) {
                final double lon = -180 + c;

                try {
                    projFactors.compute(projection, lon * MapMath.DTR, lat * MapMath.DTR, dh);
                    areaGrid.setValue((float) projFactors.s, c, r);
                    flexAngleGrid.setValue((float) (projFactors.omega * MapMath.RTD), c, r);
                } catch (Exception exc) {
                    areaGrid.setValue(Float.NaN, c, r);
                    flexAngleGrid.setValue(Float.NaN, c, r);
                }
            }
        }
    }

    /**
     * Constructs isolines of maximum angular distoration and isolines of areal
     * distortion.
     */
    public GeoSet constructIsolines(Projection projection,
            GeoGrid areaGrid, GeoGrid projGrid) {

        // isolines are symmetrical relative to the central longitude.
        // simplify computations by recentering the projection
        projection = (Projection) projection.clone();
        projection.setProjectionLongitude(0);
        projection.initialize();

        GeoSet contoursGeoSet = new GeoSet();
        contoursGeoSet.setName("Distortion Isolines");

        // fill the grids with distortion values
        this.fillDistortionGrids(projection, areaGrid, projGrid);

        // compute area contours
        if (this.displayModel.showArealIsolines && !projection.isEqualArea()) {
            Contourer contourer = new Contourer();
            contourer.setInterval(displayModel.arealIsolinesEquidistance);
            GeoSet areaContours = (GeoSet) contourer.operate(areaGrid, 0, 5);
            areaContours.setName("Isolines of Areal Distortion");

            // project the area contours
            new GeoProjector(projection).project(areaContours);

            // set isolines symbols. use thick line for isoline at value 1.
            final int count = areaContours.getNumberOfChildren();
            VectorSymbol stdSymbol = new VectorSymbol(null,
                    FlexProjectorPreferencesPanel.getArealIsolinesColor(), 1);
            stdSymbol.setScaleInvariant(true);
            VectorSymbol thickLineSymbol = new VectorSymbol(null,
                    FlexProjectorPreferencesPanel.getArealIsolinesColor(), 2);
            thickLineSymbol.setScaleInvariant(true);
            for (int i = 0; i < count; i++) {
                GeoSet isolines = (GeoSet) areaContours.getGeoObject(i);
                if (isolines.getName().equals("1.0")) {
                    isolines.setVectorSymbol(thickLineSymbol);
                } else {
                    isolines.setVectorSymbol(stdSymbol);
                }
            }

            contoursGeoSet.add(areaContours);
            contoursGeoSet.add(this.labelLines(areaContours));
        }

        // compute angular distortion contours
        if (this.displayModel.showAngularIsolines && !projection.isConformal()) {
            Contourer contourer = new Contourer();
            contourer.setInterval(displayModel.angularIsolinesEquidistance);
            contourer.setTreatDegreeJump(true);
            GeoSet angleContours = (GeoSet) contourer.operate(flexAngleGrid, 0, 120);
            angleContours.setName("Isolines of Maximum Angular Distortion");

            // project the angle contours
            new GeoProjector(projection).project(angleContours);
            VectorSymbol symbol = new VectorSymbol(null,
                    FlexProjectorPreferencesPanel.getAngularIsolinesColor(), 1);
            symbol.setScaleInvariant(true);
            angleContours.setVectorSymbol(symbol);

            contoursGeoSet.add(angleContours);
            contoursGeoSet.add(this.labelLines(angleContours));
        }

        return contoursGeoSet;
    }

    private GeoSet labelLines(GeoSet lines) {
        GeoSet labels = new GeoSet();
        final int childrenCount = lines.getNumberOfChildren();
        DecimalFormat format = new DecimalFormat("#.##");
        for (int i = 0; i < childrenCount; i++) {
            GeoObject geoObject = lines.getGeoObject(i);
            if (geoObject instanceof GeoSet) {
                labels.add(this.labelLines((GeoSet) geoObject));
            } else {
                GeoPath line = (GeoPath) geoObject;
                if (line != null) {
                    Point2D pt = line.getEndPoint();
                    String name = lines.getName();

                    // try formatting the label if it is a number
                    try {
                        GeoText text = new GeoText(name, pt.getX(), pt.getY(), 10, 0);
                        text.setText(format.format(Double.parseDouble(name)));
                        text.setScaleInvariant(true);
                        text.setCenterVer(true);
                        text.setCenterHor(false);
                        labels.add(text);
                    } catch (Exception exc) {
                        // do nothing.
                    }
                }
            }
        }
        return labels;
    }

    public FlexProjectionModel getFlexProjectionModel() {
        return flexProjection.getModel();
    }

    public DisplayModel getDisplayModel() {
        return displayModel;
    }

    public void setDisplayModel(DisplayModel displayModel) {
        this.displayModel = displayModel;
        this.mapChanged();
    }

    public GeoSet getUnprojectedData() {
        return unprojectedData;
    }

    public GeoSet getProjectedDataDestination() {
        return projectedDataDestination;
    }

    /**
     * Central meridian in degrees.
     */
    public void setCentralMeridian(double lon0) {
        DesignProjection[] projections = getDesignProjections();
        for (Projection p : projections) {
            p.setProjectionLongitudeDegrees(lon0);
        }
    }

    public GeoGrid getFlexAngleGrid() {
        return flexAngleGrid;
    }

    public GeoGrid getFlexAreaGrid() {
        return flexAreaGrid;
    }

    public GeoGrid getSecondAngleGrid() {
        return secondAngleGrid;
    }

    public GeoGrid getSecondAreaGrid() {
        return secondAreaGrid;
    }

    @Override
    public void qChanged(QModel newQModel, QModel oldQModel) {
        // don't redraw the map if the acceptance visualization is not visible
        // in the map, except when the acceptance visualization has been switched off.
        if (newQModel.isShowAcceptableArea()
                || newQModel.isShowAcceptableArea() != oldQModel.isShowAcceptableArea()) {
            this.designProjectionChanged(designProjection);
        }
    }
}
