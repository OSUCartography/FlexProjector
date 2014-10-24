/*
 * DistortionProfilesManager.java
 *
 * Created on October 21, 2007, 12:52 PM
 *
 */
package ika.gui;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.geo.*;
import ika.geo.FlexProjectorModel.DisplayModel;
import ika.proj.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

/**
 * Generates and displays distortion profiles.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public final class DistortionProfilesManager
        implements ProjectionBrewerPanel.DesignProjectionChangeListener {

    /** Vertical profiles are displayed in this map. */
    private final MapComponent verticalProfilesMap;
    /** Horizontal profiles are displayed in this map. */
    private final MapComponent horizontalProfilesMap;
    /** The longitude for which to generate vertical profiles in degrees. */
    private int profileLon;
    /** The latitude for which to generate horizontal profiles in degrees. */
    private int profileLat;
    /** The projection in the background for which profiles are generated. */
    private Projection backgroundProjection;
    /** If true, profiles for the Flex Projection are generated. */
    private boolean showFlexProfiles = true;
    /** If true, profiles for the background projection are generated. */
    private boolean showBackgroundProfiles = false;
    private FlexProjectorModel flexProjectorModel;
    private static final int LAT_INCREMENT = 1;
    private static final int LON_INCREMENT = 1;
    private static final double DH = 1e-5;
    private static final float AREA_SCALE = 45;
    private static final float MAX_AREA = 4;
    private static final float MAX_ANGLE = 180;
    private static final float ANGLE_GRID_DIST = 45;
    /** Length of dashes to draw curves for background projection. */
    private static final float BACKGROUND_PROJECTION_DASH_LENGTH = 5;
    private static final int PROFILE_LINE_WIDTH = 2;

    /** Creates a new instance of DistortionProfilesManager.
     * @param verticalProfilesMap The MapComponent that will be used to display
     * the vertical profiles.
     * @param horizontalProfilesMap The MapComponent that will be used to display
     * the horizontal profiles.
     */
    public DistortionProfilesManager(
            MapComponent verticalProfilesMap,
            MapComponent horizontalProfilesMap,
            FlexProjectorModel flexProjectorModel) {

        this.verticalProfilesMap = verticalProfilesMap;
        this.horizontalProfilesMap = horizontalProfilesMap;
        this.flexProjectorModel = flexProjectorModel;
    }

    @Override
    public void designProjectionChanged(Projection p) {
        updateDistortionProfiles(false, p);
        updateDistortionProfiles(true, p);
    }

    /**
     * Update the horizontal or vertical profiles.
     * @param vertical If true, the vertical profiles are updated, if false
     * the horizontal profiles.
     */
    public void updateDistortionProfiles(boolean vertical, Projection foregroundProj) {

        final MapComponent map = vertical ? verticalProfilesMap : horizontalProfilesMap;

        if (map == null) {
            return;
        }

        map.removeAllGeoObjects();

        if (vertical) {
            map.addGeoObject(contructVerticalGrid(), false);
        } else {
            map.addGeoObject(contructHorizontalGrid(), false);
        }

        if (showFlexProfiles && foregroundProj != null) {
            GeoSet flexProfiles = constructDistortionProfiles(foregroundProj, vertical, false);
            map.addGeoObject(flexProfiles, false);
        }

        if (showBackgroundProfiles && backgroundProjection != null) {
            GeoSet bgrdProfiles = constructDistortionProfiles(backgroundProjection, vertical, true);
            map.addGeoObject(bgrdProfiles, false);
        }

        map.showAll();

    }

    /**
     * Create the grid and labels for the vertical profiles.
     * @return A GeoSet containing the symbolized grid and the labels.
     */
    private GeoSet contructVerticalGrid() {

        GeoSet geoSet = new GeoSet();

        // an ugly hack to make sure scale-independent labels are visible
        // add a white box first and draw the rest over it
        GeoPath frame = GeoPath.newRect(-5, -135, 240, 270);
        geoSet.add(frame);

        Color angleColor = FlexProjectorPreferencesPanel.getAngularIsolinesColor();
        Color areaColor = FlexProjectorPreferencesPanel.getArealIsolinesColor();

        // vertical grid lines
        for (int i = 0; i <= MAX_ANGLE; i += ANGLE_GRID_DIST) {
            GeoPath path = new GeoPath();
            path.moveTo(i, -90);
            path.lineTo(i, 90);
            geoSet.add(path);

            // area labels
            String str = Integer.toString((int) (i / ANGLE_GRID_DIST));
            GeoText t = new GeoText(str, i, 90);
            t.getFontSymbol().setColor(areaColor);
            t.setDy(2);
            t.setCenterHor(true);
            t.setCenterVer(false);
            geoSet.add(t);
        }

        // only draw labes for 0, 90 and 180 degrees
        GeoText t = new GeoText("0\u00B0", 0, -90);
        t.getFontSymbol().setColor(angleColor);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        t = new GeoText("90\u00B0", 90, -90);
        t.getFontSymbol().setColor(angleColor);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        t = new GeoText("180\u00B0", 180, -90);
        t.getFontSymbol().setColor(angleColor);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);


        // horizontal grid lines
        for (int i = -90; i <= 90; i += ANGLE_GRID_DIST) {
            GeoPath path = new GeoPath();
            path.moveTo(0, i);
            path.lineTo(MAX_ANGLE, i);
            geoSet.add(path);
        }

        // labels on vertical axis
        GeoText equatorText = new GeoText("Equator", MAX_ANGLE, 0);
        equatorText.setDx(5);
        equatorText.setCenterHor(false);
        equatorText.setCenterVer(true);
        geoSet.add(equatorText);

        GeoText northText = new GeoText("North Pole", MAX_ANGLE, MAX_ANGLE / 2);
        northText.setDx(5);
        northText.setDy(-5);
        northText.setCenterHor(false);
        northText.setCenterVer(true);
        geoSet.add(northText);

        GeoText southText = new GeoText("South Pole", MAX_ANGLE, -MAX_ANGLE / 2);
        southText.setDx(5);
        southText.setDy(5);
        southText.setCenterHor(false);
        southText.setCenterVer(true);
        geoSet.add(southText);

        // label angle distortion
        t = new GeoText("Maximum Angular Distortion", MAX_ANGLE / 2, -90);
        t.setDy(-t.getFontSymbol().getSize() * 2.2);
        t.getFontSymbol().setColor(angleColor);
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        // label area distortion
        t = new GeoText("Areal Distortion", MAX_ANGLE / 2, 90);
        t.setDy(t.getFontSymbol().getSize() + 4);
        t.getFontSymbol().setColor(areaColor);
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        // draw all lines in black with line width 1
        VectorSymbol symbol = new VectorSymbol(null, Color.BLACK, 1);
        symbol.setScaleInvariant(true);
        geoSet.setVectorSymbol(symbol);

        // hack
        frame.setVectorSymbol(new VectorSymbol(null, Color.WHITE, 0));

        return geoSet;
    }

    /**
     * Create the grid and labels for the horizontal profiles.
     * @return A GeoSet containing the symbolized grid and the labels.
     */
    private GeoSet contructHorizontalGrid() {

        GeoSet geoSet = new GeoSet();

        // an ugly hack to make sure scale-independent labels are visible
        // add a white box first and draw the rest over it
        GeoPath frame = GeoPath.newRect(-230, -15, 445, 200);
        geoSet.add(frame);


        Color angleColor = FlexProjectorPreferencesPanel.getAngularIsolinesColor();
        Color areaColor = FlexProjectorPreferencesPanel.getArealIsolinesColor();

        // vertical grid lines
        for (int i = -180; i <= 180; i += ANGLE_GRID_DIST) {
            GeoPath path = new GeoPath();
            path.moveTo(i, 0);
            path.lineTo(i, MAX_ANGLE);
            geoSet.add(path);
        }

        // horizontal grid lines and labels
        for (int i = 0; i <= MAX_ANGLE; i += ANGLE_GRID_DIST) {
            GeoPath path = new GeoPath();
            path.moveTo(-180, i);
            path.lineTo(180, i);
            geoSet.add(path);

            // angle labels
            GeoText t = new GeoText(Integer.toString(i) + "\u00B0", -180, i);
            t.getFontSymbol().setColor(angleColor);
            t.setDx(-2);
            t.setAlignRight();
            t.setCenterVer(true);
            geoSet.add(t);

            // area labels
            String str = Integer.toString((int) (i / ANGLE_GRID_DIST));
            t = new GeoText(str, 180, i);
            t.getFontSymbol().setColor(areaColor);
            t.setDx(5);
            t.setCenterHor(false);
            t.setCenterVer(true);
            geoSet.add(t);
        }

        // labels for horizontal axis
        GeoText t = new GeoText("-180\u00B0", -180, 0);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        t = new GeoText("0\u00B0", 0, 0);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        t = new GeoText("180\u00B0", 180, 0);
        t.setDy(-t.getFontSymbol().getSize());
        t.setCenterHor(true);
        t.setCenterVer(false);
        geoSet.add(t);

        // label angle distortion
        t = new GeoText("Max. Angular Distortion", -180, 90);
        t.setDy(35);
        t.getFontSymbol().setColor(angleColor);
        t.setCenterHor(true);
        t.setCenterVer(false);
        t.setRotation(-90);
        geoSet.add(t);

        // label area distortion
        t = new GeoText("Areal Distortion", 180, 90);
        t.setDy(-25);
        t.getFontSymbol().setColor(areaColor);
        t.setCenterHor(true);
        t.setCenterVer(false);
        t.setRotation(-90);
        geoSet.add(t);

        // draw all lines in black with line width 1
        VectorSymbol symbol = new VectorSymbol(null, Color.BLACK, 1);
        symbol.setScaleInvariant(true);
        geoSet.setVectorSymbol(symbol);

        // hack
        frame.setVectorSymbol(new VectorSymbol(null, Color.WHITE, 0));

        return geoSet;
    }

    /**
     * Generate the horizontal or vertical profiles for a passed projection.
     * @param projection The projection for which to generate the profiles.
     * @param vertical If true, vertical profiles are generated, if false, horizontal.
     * @param dashed If true, profiles are dashed.
     **/
    private GeoSet constructDistortionProfiles(Projection projection,
            boolean vertical, boolean dashed) {

        if (projection == null) {
            return null;
        }

        // make copy and set central meridian to Greenwhich
        projection = (Projection) projection.clone();
        projection.setProjectionLongitude(0);
        projection.initialize();

        GeoSet geoSet = new GeoSet();
        GeoPath areaProfile = new GeoPath();
        GeoPath angleProfile = new GeoPath();
        ProjectionFactors projFactors = new ProjectionFactors();

        if (vertical) {
            // vertical profiles
            for (int lat = -90; lat < 90; lat += LAT_INCREMENT) {
                try {
                    projFactors.compute(projection, profileLon * MapMath.DTR, lat * MapMath.DTR, DH);
                    if (projFactors.s <= MAX_AREA && projFactors.s > 0) {
                        areaProfile.moveOrLineTo(projFactors.s * AREA_SCALE, lat);
                    }
                    if (projFactors.omega <= MAX_ANGLE && projFactors.omega >= 0) {
                        angleProfile.moveOrLineTo(projFactors.omega * MapMath.RTD, lat);
                    }
                } catch (Exception exc) {
                    //
                }
            }
        } else {
            // horizontal profiles
            for (int lon = -180; lon <= 180; lon += LON_INCREMENT) {
                try {
                    projFactors.compute(projection, lon * MapMath.DTR, profileLat * MapMath.DTR, DH);
                    if (projFactors.s <= MAX_AREA && projFactors.s > 0) {
                        areaProfile.moveOrLineTo(lon, projFactors.s * AREA_SCALE);
                    }
                    if (projFactors.omega <= MAX_ANGLE && projFactors.omega >= 0) {
                        angleProfile.moveOrLineTo(lon, projFactors.omega * MapMath.RTD);
                    }
                } catch (Exception exc) {
                    //
                }
            }
        }

        // symbol for area profiles
        VectorSymbol areaSymbol = new VectorSymbol(null,
                FlexProjectorPreferencesPanel.getArealIsolinesColor(), PROFILE_LINE_WIDTH);
        areaSymbol.setScaleInvariant(true);
        if (dashed) {
            areaSymbol.setDashLength(BACKGROUND_PROJECTION_DASH_LENGTH);
        }
        areaProfile.setVectorSymbol(areaSymbol);

        // symbol for angle profiles
        VectorSymbol angleSymbol = new VectorSymbol(null,
                FlexProjectorPreferencesPanel.getAngularIsolinesColor(), PROFILE_LINE_WIDTH);
        angleSymbol.setScaleInvariant(true);
        if (dashed) {
            angleSymbol.setDashLength(BACKGROUND_PROJECTION_DASH_LENGTH);
        }
        angleProfile.setVectorSymbol(angleSymbol);

        // add profiles to GeoSet
        if (areaProfile.getPointsCount() > 10) {
            geoSet.add(areaProfile);
        }
        if (angleProfile.getPointsCount() > 10) {
            geoSet.add(angleProfile);
        }
        
        return geoSet;

    }

    /**
     * Constructs the projected location of vertical and horizontal profiles for
     * display in the map.
     */
    public GeoSet constructProfilesInMap(boolean vertical, Projection foregroundProj) {

        GeoSet geoSet = new GeoSet();

        if (showFlexProfiles && foregroundProj != null) {
            geoSet.add(constructProfileInMap(foregroundProj, vertical, false));
        }

        if (showBackgroundProfiles && backgroundProjection != null) {
            GeoSet backgroundGeoSet = new GeoSet();
            backgroundGeoSet.add(constructProfileInMap(backgroundProjection, vertical, true));

            // the following lengthy code is to optionally scale the profile for
            // the background projection. Scaling is necessary if the background
            // projection is scaled to width with the width or height of the
            // flex projection.
            DisplayModel dm = flexProjectorModel.getDisplayModel();
            GeoPath backOutline = FlexProjectorModel.constructOutline(dm.projection);
            Rectangle2D backBounds = backOutline.getBounds2D(GeoObject.UNDEFINED_SCALE);
            Projection foreProj = flexProjectorModel.getDesignProjection();
            GeoPath foreOutline = FlexProjectorModel.constructOutline(foreProj);
            Rectangle2D foreBounds = foreOutline.getBounds2D(GeoObject.UNDEFINED_SCALE);
            flexProjectorModel.scaleBackgroundProjection(foreBounds, backBounds, backgroundGeoSet);
            geoSet.add(backgroundGeoSet);
        }

        return geoSet;

    }

    /**
     * Constructs the projected location of vertical or horizontal profiles for
     * display in the map.
     * @param projection The projection for which the profile locations are constructed.
     * @param vertical If true, vertical profiles, otherwise horizontal profiles
     * are constructed.
     * @param dashed If true, the profile locations area dashed.
     */
    private GeoPath constructProfileInMap(Projection projection,
            boolean vertical, boolean dashed) {

        if (projection == null) {
            return null;
        }

        final double initialLon0 = projection.getProjectionLongitude();
        try {
            projection.setProjectionLongitudeDegrees(0);
            //projection.initialize();

            final double minLon = projection.getMinLongitude();
            final double maxLon = projection.getMaxLongitude();
            final double minLat = projection.getMinLatitude();
            final double maxLat = projection.getMaxLatitude();

            final double lonRad = Math.min(maxLon, Math.toRadians(profileLon));
            final double latRad = Math.min(maxLat, Math.toRadians(profileLat));

            GeoPath profile = new GeoPath();
            profile.setName(this.getMapProfileName());

            final int nbrPts = 180;
            final double lonInc = (maxLon - minLon) / nbrPts;
            final double latInc = (maxLat - minLat) / nbrPts;

            if (vertical) {
                // right line
                profile.moveTo(lonRad, minLat);
                for (int i = 1; i < nbrPts; i++) {
                    double lat = latInc * i + minLat;
                    profile.lineTo(lonRad, lat);
                }

                // left line
                profile.moveTo(-lonRad, minLat);
                for (int i = 1; i < nbrPts; i++) {
                    double lat = latInc * i + minLat;
                    profile.lineTo(-lonRad, lat);
                }
            } else {
                // top line
                profile.moveTo(minLon, latRad);
                for (int i = 1; i <= nbrPts; i++) {
                    double lon = lonInc * i + minLon;
                    profile.lineTo(lon, latRad);
                }

                // bottom line
                profile.moveTo(minLon, -latRad);
                for (int i = 1; i <= nbrPts; i++) {
                    double lon = lonInc * i + minLon;
                    profile.lineTo(lon, -latRad);
                }
            }

            // scale from radians to degrees
            profile.scale(180 / Math.PI);

            // project the outline
            new LineProjector(projection, 5000, false).projectOpenPath(profile);

            // symbol for profiles
            VectorSymbol symbol = new VectorSymbol(null, Color.RED, 2);
            symbol.setScaleInvariant(true);
            if (dashed) {
                symbol.setStrokeWidth(1);
            }
            profile.setVectorSymbol(symbol);

            return profile;

        } finally {
            projection.setProjectionLongitude(initialLon0);
            //projection.initialize();
        }

    }

    /**
     * Returns the longitude for which vertical profiles are generated.
     * @return The longitude in degrees.
     */
    public int getLon() {
        return profileLon;
    }

    /**
     * Sets the longitude for which vertical profiles are generated.
     * @param The longitude in degrees in [-180,180].
     */
    public void setLon(int lon) {
        if (lon > 180 || lon < -180) {
            throw new IllegalArgumentException("longitude out of bounds");
        }
        this.profileLon = lon;
    }

    /**
     * Returns the latitude for which horizontal profiles are generated.
     * @return The latitude in degrees.
     */
    public int getLat() {
        return profileLat;
    }

    /**
     * Sets the latitude for which horizontal profiles are generated.
     * @param The latitude in degrees in [-90,900].
     */
    public void setLat(int lat) {
        if (lat > 90 || lat < -90) {
            throw new IllegalArgumentException("latitude out of bounds");
        }
        this.profileLat = lat;
    }

    public void setBackgroundProjection(Projection backgroundProjection) {
        this.backgroundProjection = backgroundProjection;
    }

    public boolean isShowFlexProfiles() {
        return showFlexProfiles;
    }

    public void setShowFlexProfiles(boolean showFlexProfiles) {
        this.showFlexProfiles = showFlexProfiles;
    }

    public boolean isShowBackgroundProfiles() {
        return showBackgroundProfiles;
    }

    public void setShowBackgroundProfiles(boolean showBackgroundProfiles) {
        this.showBackgroundProfiles = showBackgroundProfiles;
    }

    /** Returns a name to identify the profile locations displayed in the main map. */
    public String getMapProfileName() {
        return "Vertical Profile";
    }
}
