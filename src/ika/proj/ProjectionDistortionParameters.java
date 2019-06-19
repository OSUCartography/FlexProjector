/*
 * ProjectionDistortionParameters.java
 *
 * Created on September 3, 2007, 1:55 PM
 *
 */

package ika.proj;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import ika.geo.FlexProjectorModel;
import ika.geo.GeoImage;
import ika.geo.GeoObject;
import ika.geo.GeoPath;
import ika.gui.FlexProjectorPreferencesPanel;
import ika.utils.PropertiesLoader;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Properties;

/**
 * ProjectionDistortionParameters provides indices for a projection that
 * describe areal, angular and distance distortion.
 * Three indices are computed for the continental areas. A raster image is used to
 * decide whether a point is on land or on sea.
 *
 * Computation of distortion parameters according to Frank Canters and Hugo
 * Decleir (1989), The World in Perspective - A Directory of World Map
 * Projections", Wiley, Chichester, etc., 181 p.
 * For a comparison, see this book, page 44 and 45.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ProjectionDistortionParameters {
    
    /**
     * The distance between two sample points in degrees for computing the
     * indices defined by Canters and Decleir.
     * The default value is 2.5 degree, which is the value used by Canters and
     * Decleir.
     */
    private static final double INDEX_SAMPLING_DIST_DEG = 2.5;
    
    /**
     * Increment for computing local first derivative. In radians.
     */
    private static final double DERIVATIVE_INC_RAD = 1e-5;
    
    /**
     * Continental areas are depicted in the mask image with this color.
     */
    private static final java.awt.Color continentalColor = java.awt.Color.WHITE;
    
    /**
     * Mask for terrestrial areas stored in a black-and-white image.
     */
    private static final java.awt.image.BufferedImage CONTINENTAL_MASK;

    // load the continental image mask
    static {
        java.awt.image.BufferedImage img;
        try {
            // load continental mask
            Properties props =
                    PropertiesLoader.loadProperties("ika.app.Application.properties");
            String mapData = props.getProperty("EarthMask");
            java.net.URL url = ProjectionDistortionParameters.class.getResource(mapData);
            img = javax.imageio.ImageIO.read(url);
        } catch (Exception exc) {
            exc.printStackTrace();
            img = null;
        }
        CONTINENTAL_MASK = img;
    }
    
    /**
     * The mean angular deformation index.
     */
    private double Dan;
    
    /**
     * The mean angular deformation index for continental areas.
     */
    private double Danc;
    
    /**
     * The weighted mean error in areal distortion.
     */
    private double Dar;
    
    /**
     * The weighted mean error in areal distortion for continental areas.
     */
    private double Darc;
    
    /**
     *The weighted mean error in the overall scale distortion.
     */
    private double Dab;
    
    /**
     *The weighted mean error in the overall scale distortion for continental areas.
     */
    private double Dabc;
    
    /**
     * Acceptance index Q after R. Capek.
     */
    private double Q;
    
    /**
     * The distance between two sample points in degrees for computing the
     * Q index.
     */
    private static final int Q_CELLSIZE = 1;
    
    /**
     * Q_CELLSIZE converted to radians.
     */
    private static final double Q_CELLSIZE_RAD = Math.toRadians(Q_CELLSIZE);
    
    /**
     * The number of columns in qAreaGridQuadrant and qAngleGridQuadrant.
     */
    private static final int Q_GRID_COLUMNS = 180;
    
    /**
     * The number of rows in qAreaGridQuadrant and qAngleGridQuadrant.
     */
    private static final int Q_GRID_ROWS = 90;
   
    /**
     * A grid storing area distortion values with a cell size of Q_CELLSIZE degree.
     * This is used to quickly compute a new Q index when the parameters for the
     * computation of the Q index change.
     * The grid only contains the upper right quarter of the coordinate space, as the
     * full grid is symmetric relative to the central meridian and the equator.
     * The values in this grid need to be updated when the projection changes, 
     * which is only the case for flex projections that the user edits.
     */
    private double[][] qAreaGridQuadrant = new double[Q_GRID_ROWS][Q_GRID_COLUMNS];
    
    /**
     * A grid storing angle distortion values with a cell size of Q_CELLSIZE degree.
     * This is used to quickly compute a new Q index when the parameters for the
     * computation of the Q index change.
     * The grid only contains the upper right quarter of the coordinate space, as the
     * full grid is symmetric relative to the central meridian and the equator.
     * The values in this grid need to be updated when the projection changes, 
     * which is only the case for flex projections that the user edits.
     */
    private double[][] qAngleGridQuadrant = new double[Q_GRID_ROWS][Q_GRID_COLUMNS];
    
    /**
     * The smallest value in the qAreaGridQuadrant grid. This is used to compute
     * the Q index according to the original specification of Capek.
     */
    private double qMinArea; 
    
    /**
     * A grid in the projected coordinate space (not the geographic space, as 
     * qAreaGridQuadrant or qAngleGridQuadrant).
     * This is used to visualize the acceptance Q.
     * acceptanceIndexGrid contains pointers to cells in qAreaGridQuadrant 
     * and qAngleGridQuadrant.
     * The values in this grid need to be updated when the projection changes,
     * which is only the case for flex projections that the user edits.
     * The grid only covers the top-right quarter of the graticule.
     * West is not at the central meridian, but east of the central meridian by
     * a half cell size.
     * The southern-most row is not along the equator, but a half cell size above.
     */
    private ika.geo.GeoGrid acceptanceIndexGrid;
    
    /**
     * The distortion parameters are computed for this projection.
     */
    private Projection projection;
    
    /** Creates a new instance of ProjectionDistortionParameters */
    public ProjectionDistortionParameters(Projection projection, QModel qModel) {
    
        this.projection = projection;
        computeDistortionIndices(qModel);
        
    }
    
    /**
     * The shape or other characteristics of the projection changed. Update the
     * distortion indices and the grids used to compute them.
     * @param qModel The parameters for the computation of the Q index.
     */
    public final void computeDistortionIndices(QModel qModel) {
        
        // update Q grids in spherical coordinates
        this.initAcceptanceDegreeGrids();
        
        // update the Q index based on the Q grids in spherical coordinates
        this.computeAcceptanceIndex(qModel);
        
        // update Q grid in projected coordinates
        this.initQProjectedGrid();
        
        // update indices by Canters & Decleir
        this.computeCantersDecleirIndices();
        
    }

    public void computeDistortionIndices(QModel qModel, Projection projection) {
        this.projection = projection;
        computeDistortionIndices(qModel);
    }
    
    /**
     * Parameters for the computation of Q changed. Recompute the Q index.
     */
    public void qModelChanged(QModel qModel) {
        
        this.computeAcceptanceIndex(qModel);
        
    }
    
    /**
     * Computes the 6 distortion indices defined by Canters and Decleir, 
     * i.e. Dan, Dar, Dab, Danc, Darc, Dabc
     */
    private void computeCantersDecleirIndices() {
        
        try {
            Projection normalAspectProj = (Projection)this.projection.clone();
            normalAspectProj.setProjectionLongitude(0);
            normalAspectProj.initialize();
                    
            final int continentARGB = continentalColor.getRGB();
            
            final double d_rad = Math.toRadians(INDEX_SAMPLING_DIST_DEG);
            int nh = (int)Math.round(180. / INDEX_SAMPLING_DIST_DEG);
            int nv = (int)Math.round(90. / INDEX_SAMPLING_DIST_DEG);
            
            Dan = 0;
            Dar = 0;
            Dab = 0;
            Danc = 0;
            Darc = 0;
            Dabc = 0;
            double continentalArea = 0;
            ProjectionFactors f = new ProjectionFactors();
            
            for (int v = -nv; v < nv; v++) {
                final double phi = (v + 0.5) * d_rad;
                
                // area of infinitesimal patch on sphere
                final double patchArea = Math.cos(phi) * d_rad * d_rad;
                
                for (int h = -nh; h < nh; h++) {
                    
                    final double lam = (h + 0.5) * d_rad;
                    f.compute(normalAspectProj, lam, phi, DERIVATIVE_INC_RAD);
                    
                    final double an = f.omega * patchArea;
                    Dan += an;
                    
                    final double axb = f.a * f.b;
                    final double ar = ((axb < 1. ? 1./axb : axb) - 1.) * patchArea;
                    Dar += ar;
                    
                    final double a_b = (f.a < 1. ? 1./f.a : f.a) + (f.b < 1. ? 1./f.b : f.b);
                    final double ab = (a_b * 0.5 - 1.) * patchArea;
                    Dab += ab;
                    
                    if (CONTINENTAL_MASK.getRGB(h+nh, v+nv) == continentARGB) {
                        Danc += an;
                        Darc += ar;
                        Dabc += ab;
                        continentalArea += patchArea;
                    }
                }
            }
            final double sphereArea = 4. * Math.PI;
            Dan = Math.toDegrees(Dan / sphereArea);
            Dar = Dar / sphereArea;
            Dab = Dab / sphereArea;
            Danc = Math.toDegrees(Danc / continentalArea);
            Darc = Darc / continentalArea;
            Dabc = Dabc / continentalArea;
            
            if (normalAspectProj.isEqualArea()) {
                Dar = Darc = 0;
            }
            if (normalAspectProj.isConformal()) {
                Dan = Danc = 0;
            }
            
        } catch (Exception e) {
            Dan = Double.NaN;
            Dar = Double.NaN;
            Dab = Double.NaN;
            Danc = Double.NaN;
            Darc = Double.NaN;
            Dabc = Double.NaN;
        }
    }

    /**
     * Computes Dar distortion indices as defined by Canters and Decleir
     */
    public static double getDarIndex(Projection normalProjection) {

        final double d_rad = Math.toRadians(INDEX_SAMPLING_DIST_DEG);
        final int nh = (int) Math.round(180. / INDEX_SAMPLING_DIST_DEG);
        final int nv = (int) Math.round(90. / INDEX_SAMPLING_DIST_DEG);


        
        double Dar = 0;
        for (int v = -nv; v < nv; v++) {
            final double phi = (v + 0.5) * d_rad;
            final double cosphi_inv = 1. / Math.cos(phi);

            // area of infinitesimal patch on sphere
            final double patchArea = Math.cos(phi) * d_rad * d_rad;

            for (int h = -nh; h < nh; h++) {
                final double lam = (h + 0.5) * d_rad;
                ProjectionDerivatives der;
                der = new ProjectionDerivatives(normalProjection, lam, phi, DERIVATIVE_INC_RAD);
                final double axb = (der.y_p * der.x_l - der.x_p * der.y_l) * cosphi_inv;
                final double ar = ((axb < 1. ? 1. / axb : axb) - 1.) * patchArea;
                Dar += ar;
            }
        }
        final double sphereArea = 4. * Math.PI;
        return Dar / sphereArea;

    }

    /**
     * Q acceptance index according to R. Capek or A acceptance index according
     * to Jenny.
     * This uses precomputed grids with areal and angular distortion values. The
     * grids are initialized by initAcceptanceDegreeGrids().
     */
    private void computeAcceptanceIndex(QModel qModel) {
        
        if (qModel == null) {
            this.Q = Double.NaN;
            return;
        }
        
        final boolean qRelativeTo1 
                = FlexProjectorPreferencesPanel.isAreaAcceptanceRelativeTo1();
            
        double acceptableArea = 0;

        // loop over rows of grids for topleft quadrant
        for (int row = 0; row < Q_GRID_ROWS; row++) {
            
            // area of infinitesimal patch on sphere
            final double phi = Math.PI / 2. - (row + 0.5) * Q_CELLSIZE_RAD;
            final double patchArea = Math.cos(phi) * Q_CELLSIZE_RAD * Q_CELLSIZE_RAD;

            // loop over colums of grid for topleft quadrant
            for (int col = 0; col < Q_GRID_COLUMNS; col++) {
                if (isDistortionAcceptable(row, col, qModel, qRelativeTo1)) {
                    acceptableArea += patchArea * 4.; // * 4 for 4 quadrants
                }
            }
        }
        
        // divide by area of sphere with radius 1 and convert to percentage
        final double sphereArea = 4. * Math.PI;
        
        // theoretically, the area of the patches should be summed
        // for each row: totPatchArea += patchArea;
        // then: totPatchArea *= 4 * Q_GRID_COLUMNS;
        // However, the difference between the sphere area and the total patch
        // area is is very small and the resulting acceptance indices are
        // practically identical.

        this.Q = acceptableArea / sphereArea * 100.;
        
    }
    
    /**
     * Initializes qAngleGridQuadrant and qAreaGridQuadrant and stores the 
     * smallest areal distortion value in qMinArea.
     * The grids are in unprojected spherical coordinates. The top left cell is
     * centered on 0.5/89.5.
     */
    private void initAcceptanceDegreeGrids() {
        
        Projection normalAspectProj = (Projection)projection.clone();
        normalAspectProj.setProjectionLongitude(0);
        normalAspectProj.initialize();
            
        ProjectionFactors f = new ProjectionFactors();
        this.qMinArea = Double.MAX_VALUE;
        final boolean equalArea = normalAspectProj.isEqualArea();
        final boolean conformal = normalAspectProj.isConformal();
        
        for (int row = 0; row < Q_GRID_ROWS; row++) {
            final double phi = Math.PI / 2. - (row + 0.5) * Q_CELLSIZE_RAD;
            for (int col = 0; col < Q_GRID_COLUMNS; col++) {
                try {
                    final double lam = (col + 0.5) * Q_CELLSIZE_RAD;
                    f.compute(normalAspectProj, lam, phi, DERIVATIVE_INC_RAD);
                    
                    // area distortion
                    qAreaGridQuadrant[row][col] = equalArea ? 1. : f.s;
                    if (f.s < qMinArea) {
                        this.qMinArea = f.s;
                    }
                    
                    // angular distortion
                    qAngleGridQuadrant[row][col] = conformal ? 0. : f.omega;
                } catch (Exception exc) {
                    qAreaGridQuadrant[row][col] = Double.NaN;
                    qAngleGridQuadrant[row][col] = Double.NaN;
                }
            }
        }
        
        if (equalArea) {
            qMinArea = 1.;
        }
    }

    /**
     * Initializes the acceptanceIndexGrid, which holds pointers to cells in 
     * qAreaGridQuadrant and qAngleGridQuadrant.
     * acceptanceIndexGrid covers the top-right quarter of the projected graticule.
     */
    private void initQProjectedGrid() {

        Projection normalAspectProj = (Projection)this.projection.clone();
        normalAspectProj.setProjectionLongitude(0);
        // normalize the flex projection before projecting in the inverse direction.
        if (normalAspectProj instanceof FlexProjection) {
            ((FlexProjection)normalAspectProj).getModel().normalize();
        }
        normalAspectProj.initialize();
        
        // find the bounding box of the projected grid
        GeoPath outline = FlexProjectorModel.constructOutline(normalAspectProj);
        Rectangle2D projBB = outline.getBounds2D(GeoObject.UNDEFINED_SCALE);
        final double projNorth = projBB.getMaxY();
        final int maxLat = (int)Math.min(normalAspectProj.getMaxLatitudeDegrees(), 
                -normalAspectProj.getMinLatitudeDegrees());
        
        // compute the cell size and the dimension of the new grid
        final double projCellSize = Math.min(projBB.getWidth() / 360,
                projBB.getHeight() / 180);
        final int projCols = (int)Math.ceil(projBB.getWidth() / projCellSize / 2.);
        final int projRows = (int)Math.ceil(projBB.getHeight() / projCellSize / 2.);
        
        // create the projected grid
        this.acceptanceIndexGrid = new ika.geo.GeoGrid(projCols, projRows, projCellSize, -1f);
        // position it a half cell size from the origin
        this.acceptanceIndexGrid.setWest(projCellSize / 2);
        this.acceptanceIndexGrid.setNorth(projNorth - projCellSize / 2);
        if (!normalAspectProj.hasInverse()) {
            return;
        }
        
        // store indices into qAreaGridQuadrant and qAngleGridQuadrant in the grid
        // only a quarter of the sphere is computed
        Point2D.Double dstPt = new Point2D.Double();
        final double sphereRadius = normalAspectProj.getEquatorRadius();
        for (int r = 0; r < projRows; r++) {
            double y = (projNorth - (r + 0.5) * projCellSize) / sphereRadius;
            for (int c = 0; c < projCols; c++) {
                double x = (c + 0.5) * projCellSize / sphereRadius;
                normalAspectProj.projectInverse(x, y, dstPt);
                if (Double.isNaN(dstPt.x) || Double.isNaN(dstPt.y)) {
                    continue;
                }
                if (dstPt.x > Math.PI || dstPt.x < -Math.PI 
                        || dstPt.y > MapMath.HALFPI || dstPt.y < -MapMath.HALFPI) {
                    continue;
                }
                int lon = (int)Math.abs(Math.toDegrees(dstPt.x));
                int lat = (int)Math.abs(Math.toDegrees(dstPt.y));
                if (lon >= 180) {
                    lon = 179;
                }
                if (lat > maxLat) {
                    lat = maxLat;
                }
                int cellID = lon + (maxLat - lat - 1) * 180;
                this.acceptanceIndexGrid.setValue(cellID, c, r);
            }
        }

    }
    
    /**
     * Generate a GeoImage that visualizes the acceptance according to the 
     * passed acceptance parameters.
     * @param qModel The parameters for computing the acceptance.
     */
    public GeoImage computeAcceptanceImage(QModel qModel) {
        
//        ika.utils.NanoTimer timer = new ika.utils.NanoTimer();
//        long start = timer.nanoTime();
        
        int cols = acceptanceIndexGrid.getCols();
        int rows = acceptanceIndexGrid.getRows();
        
        // clear blue for accepted areas
        int acceptColor = new Color (147/255f, 196/255f, 251/255f, 0.5f).getRGB();
        
        // transparent white for rejected areas.
        int rejectColor = new Color(1f, 1f, 1f, 0f).getRGB();
        
        BufferedImage img = new BufferedImage(cols * 2, rows * 2, 
                BufferedImage.TYPE_INT_ARGB);
                           
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int cellID = (int)acceptanceIndexGrid.getValue(c, r);
                
                // outside of graticule
                if (cellID < 0) {
                    img.setRGB(cols + c, r, rejectColor);
                    // top-left quadrant
                    img.setRGB(cols - c - 1, r, rejectColor);
                    // bottom-left quadrant
                    img.setRGB(cols - c - 1, 2 * rows - r - 1, rejectColor);
                    // bottom-right quadrant
                    img.setRGB(cols + c, 2 * rows - r - 1, rejectColor);

                    continue;
                }
                
                final int accCol = cellID % 180;
                final int accRow = cellID / 180;
                
                // compute acceptance of areal distortion for visualization 
                // always relative to 1 to compare the visualization with an
                // equal-area projection
                boolean accept = isDistortionAcceptable(accRow, accCol, qModel, true);
                final int color = accept ? acceptColor : rejectColor;
                
                // top-righ quadrant
                img.setRGB(cols + c, r, color);
                // top-left quadrant
                img.setRGB(cols - c - 1, r, color);
                // bottom-left quadrant
                img.setRGB(cols - c - 1, 2 * rows - r - 1, color);
                // bottom-right quadrant
                img.setRGB(cols + c, 2 * rows - r - 1, color);
            }
        }
        
        double cellSize = this.acceptanceIndexGrid.getCellSize();
        double west = -this.acceptanceIndexGrid.getEast() - cellSize / 2;
        double north = this.acceptanceIndexGrid.getNorth() + cellSize / 2;
        GeoImage geoImage = new GeoImage(img, west, north, cellSize);
        
//        long end = timer.nanoTime();
//        System.out.println("Computing Q image: " + (end - start) / 1000 / 1000 + "ms");
        
        return geoImage;
        
    }
    
    /**
     * Returns whether the distortion for a point is acceptable or not.
     * @param row The point is located in this row of qAngleGridQuadrant and qAreaGridQuadrant
     * @param col The point is located in this column of qAngleGridQuadrant and qAreaGridQuadrant
     * @param qModel The parameters for computing the acceptance.
     * @param qRelativeTo1 True if the acceptable area distortion is relative to an equal area projection.
     * @return True if the point has an acceptable distortion, false otherwise.
     */
    private boolean isDistortionAcceptable(int row, int col, 
            QModel qModel,
            boolean qRelativeTo1) {

        double maxQAngleDist = qModel.getQMaxAngle();
        double maxQAreaDist = qModel.getQMaxAreaScale();
        
        // test for valid maximum values
        if (maxQAngleDist == 0 && maxQAreaDist == 1) {
            return false; // conformal and equal-area are not possible
        }

        // test whether the area distortion is acceptable
        final boolean acceptArea;
        final double areaDist = qAreaGridQuadrant[row][col];
        if (qRelativeTo1) {

            // maxQAreaDist must be larger than 1
            if (maxQAreaDist < 1d) {
                maxQAreaDist = 1d / maxQAreaDist;
            }

            // with maxQAreaDist equal to 200%, accept values in [100/200%..200%] = [50%..200%]
            acceptArea = areaDist <= maxQAreaDist && areaDist >= 1d / maxQAreaDist;
        } else {
            acceptArea = areaDist <= maxQAreaDist * this.qMinArea;
        }

        // test for conformal projection
        if (maxQAngleDist < 0.00001 && this.projection.isConformal()) {
            return acceptArea;
        }

        // thest whether the angular distortion is acceptable
        final double angleDist = this.qAngleGridQuadrant[row][col];
        final boolean acceptAngle = angleDist <= maxQAngleDist;

        // test for equal area projection
        if (this.projection.isEqualArea()) {
            return acceptAngle;
        }

        return acceptArea && acceptAngle;
        
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dar: \t").append(Dar);
        sb.append("\nDarc: \t").append(Darc);
        sb.append("\nDan: \t").append(Dan);
        sb.append("\nDanc: \t").append(Danc);
        sb.append("\nDab: \t").append(Dab);
        sb.append("\nDabc: \t").append(Dabc);
        sb.append("\nQ: \t").append(Q);
        return sb.toString();
    }
    
    public String getProjectionName() {
        return this.projection.toString();
    }

    public Projection getProjection() {
        return projection;
    }

    public double getDan() {
        return Dan;
    }

    public double getDanc() {
        return Danc;
    }

    public double getDar() {
        return Dar;
    }

    public double getDarc() {
        return Darc;
    }

    public double getDab() {
        return Dab;
    }

    public double getDabc() {
        return Dabc;
    }

    public double getQ() {
        return Q;
    }
}
