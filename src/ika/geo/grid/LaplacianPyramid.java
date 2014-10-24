package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geoexport.ESRIASCIIGridExporter;
import ika.utils.FileUtils;
import java.io.IOException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class LaplacianPyramid {

    public static GridMask mask; // a hack FIXME
    private GeoGrid[] levels;
    private static final float wa = 0.4f;
    private static final float wb = 0.25f;
    private static final float wc = 0.05f;

    public void createPyramid(GeoGrid[] gaussianPyramid) {

        levels = new GeoGrid[gaussianPyramid.length];

        // store the smallest Gaussian grid in the Laplacian pyramid
        levels[levels.length - 1] = gaussianPyramid[gaussianPyramid.length - 1];

        // compute the levels of this Laplacian pyramid by computing differences
        // between the levels of the Gaussian pyramid.
        for (int i = gaussianPyramid.length - 1; i > 0; i--) {

            GeoGrid nextLargerGrid = gaussianPyramid[i - 1];

            // expand the smaller grid to the size of the larger grid
            GeoGrid expanded = LaplacianPyramid.expand(gaussianPyramid[i],
                    nextLargerGrid.getCols(), nextLargerGrid.getRows());

            // compute the difference
            levels[i - 1] = LaplacianPyramid.difGrids(nextLargerGrid, expanded);
        }

    }

    private static void expandBorderColumns(GeoGrid src, float[][] dst) {

        final int cols = src.getCols();
        final int rows = src.getRows();

        // left column
        for (int r = 0; r < rows; r++) {
            float v0 = src.getValue(0, r);
            float v1 = v0;
            float v2 = src.getValue(1, r);
            float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
            float vOdd = 2.f * wb * (v1 + v2);

            if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                    dst[r][0] = Float.NaN;
                    dst[r][1] = Float.NaN;
                } else {
                    expandWithVoid(src.getGrid(), dst, 0, r, true);
                }
            } else {
                dst[r][0] = vEven;
                dst[r][1] = vOdd;
            }

        }

        // right column
        for (int r = 0; r < rows; r++) {
            float v0 = src.getValue(cols - 2, r);
            float v1 = src.getValue(cols - 1, r);
            float v2 = v1;
            float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
            float vOdd = 2.f * wb * (v1 + v2);
            int c = (cols - 1) * 2;

            if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                    dst[r][c] = Float.NaN;
                    dst[r][c + 1] = Float.NaN;
                } else {
                    expandWithVoid(src.getGrid(), dst, c, r, true);
                }
            } else {
                dst[r][c] = vEven;
                dst[r][c + 1] = vOdd;
            }
        }

    }

    private static void expandBorderRows(float[][] src, GeoGrid dstGeoGrid) {

        final int cols = dstGeoGrid.getCols();
        final int rows = dstGeoGrid.getRows();
        final float[][] dstGrid = dstGeoGrid.getGrid();

        // top row
        for (int c = 0; c < cols; c++) {
            float v0 = src[0][c];
            float v1 = v0;
            float v2 = src[1][c];
            float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
            float vOdd = 2.f * wb * (v1 + v2);
            if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                    dstGrid[0][c] = Float.NaN;
                    dstGrid[1][c] = Float.NaN;
                } else {
                    expandWithVoid(dstGeoGrid.getGrid(), dstGrid, c, 0, false);
                }
            } else {
                dstGrid[0][c] = vEven;
                dstGrid[1][c] = vOdd;
            }

        }

        // bottom row
        for (int c = 0; c < cols; c++) {
            float v0 = src[rows / 2 - 2][c];
            float v1 = src[rows / 2 - 1][c];
            float v2 = v1;
            float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
            float vOdd = 2.f * wb * (v1 + v2);
            if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                    dstGrid[rows - 2][c] = Float.NaN;
                    dstGrid[rows - 1][c] = Float.NaN;
                } else {
                    expandWithVoid(dstGeoGrid.getGrid(), dstGrid, c, rows - 2, false);
                }
            } else {
                dstGrid[rows - 2][c] = vEven;
                dstGrid[rows - 1][c] = vOdd;
            }
        }
    }
    private static float[][] tempGrid = null;

    /**
     * Expand the size of a grid by a factor 2.
     * @param geoGrid The grid to expand.
     * @return
     */
    public static GeoGrid expand(GeoGrid geoGrid, int maxCols, int maxRows) {

        final int cols = geoGrid.getCols();
        final int rows = geoGrid.getRows();

        // the new grid is twice as large
        final int newCols = Math.min(maxCols, cols * 2);
        final int newRows = Math.min(maxRows, rows * 2);

        GeoGrid expandedGrid = new GeoGrid(newCols, newRows, geoGrid.getCellSize() / 2);
        expandedGrid.setWest(geoGrid.getWest());
        expandedGrid.setNorth(geoGrid.getNorth());

        // tempGrid holds an intermediate grid that is expanded horizontally, 
        // but not vertically.
        if (tempGrid == null || tempGrid.length < rows) {
            tempGrid = new float[rows][cols * 2];
        }

        LaplacianPyramid.expandBorderColumns(geoGrid, tempGrid);
        for (int r = 0; r < rows; r++) {
            final float[] tempGridRow = tempGrid[r];
            for (int c = 1; c < cols - 1; c++) {
                final float v0 = geoGrid.getValue(c - 1, r);
                final float v1 = geoGrid.getValue(c, r);
                final float v2 = geoGrid.getValue(c + 1, r);
                final float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
                final float vOdd = 2.f * wb * (v1 + v2);
                if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                    if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                        tempGridRow[c * 2] = Float.NaN;
                        tempGridRow[c * 2 + 1] = Float.NaN;
                    } else {
                        expandWithVoid(geoGrid.getGrid(), tempGrid, c, r, true);
                    }
                } else {
                    tempGridRow[c * 2] = vEven;
                    tempGridRow[c * 2 + 1] = vOdd;
                }
            }
        }

        LaplacianPyramid.expandBorderRows(tempGrid, expandedGrid);
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 0; c < newCols; c++) {

                final float v0 = tempGrid[r - 1][c]; //tempGrid[(r - 1) * cols * 2 + c];
                final float v1 = tempGrid[r][c];
                final float v2 = tempGrid[r + 1][c];
                final float vEven = 2.f * (wc * (v0 + v2) + wa * v1);
                final float vOdd = 2.f * wb * (v1 + v2);
                if (Float.isNaN(vEven) || Float.isNaN(vOdd)) {
                    if (Float.isNaN(vEven) && Float.isNaN(vOdd)) {
                        expandedGrid.setValue(Float.NaN, c, 2 * r);
                        expandedGrid.setValue(Float.NaN, c, 2 * r + 1);
                    } else {
                        expandWithVoid(tempGrid, expandedGrid.getGrid(), c / 2, r, false);
                    }
                } else {
                    expandedGrid.setValue(vEven, c, 2 * r);
                    expandedGrid.setValue(vOdd, c, 2 * r + 1);
                }
            }
        }

        return expandedGrid;
    }

    private static void expandWithVoid(float[][] srcGrid,
            float[][] expandedGrid,
            int c,
            int r,
            boolean horizontal) {

        final float v0, v1, v2;
        if (horizontal) {
            v0 = srcGrid[r][c - 1];
            v1 = srcGrid[r][c];
            v2 = srcGrid[r][c + 1];
        } else {
            v0 = srcGrid[r - 1][c];
            v1 = srcGrid[r][c];
            v2 = srcGrid[r + 1][c];
        }

        float vEven = 0f;
        float vOdd = 0f;
        float totEvenW = 0f;
        float totOddW = 0f;

        if (!Float.isNaN(v0)) {
            vEven = wc * v0;
            totEvenW = wc;
        }
        if (!Float.isNaN(v1)) {
            vEven += wa * v1;
            vOdd += wb * v1;
            totEvenW += wa;
            totOddW += wb;
        }
        if (!Float.isNaN(v2)) {
            vEven += wc * v2;
            vOdd += wb * v2;
            totEvenW += wc;
            totOddW += wb;
        }

        if (totEvenW == 0) {
            vEven = Float.NaN;
        }
        if (totOddW == 0) {
            vOdd = Float.NaN;
        }
        final float scaleEven = (wc * 2 + wa) / totEvenW;
        final float scaleOdd = wb * 2 / totOddW;

        vEven *= 2f * scaleEven;
        vOdd *= 2f * scaleOdd;
        if (horizontal) {
            expandedGrid[r][c * 2] = vEven;
            expandedGrid[r][c * 2 + 1] = vOdd;
        } else {
            expandedGrid[r * 2][c] = vEven;
            expandedGrid[r * 2 + 1][c] = vOdd;
        }
    }

    public static GeoGrid distanceWeightedScaling(GeoGrid geoGrid,
            float wFore,
            float wBack,
            Interpolator interpolator) {

        final int cols = geoGrid.getCols();
        final int rows = geoGrid.getRows();
        GeoGrid resGrid = new GeoGrid(cols, rows, geoGrid.getCellSize());
        resGrid.setWest(geoGrid.getWest());
        resGrid.setNorth(geoGrid.getNorth());

        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = resGrid.getGrid();

        for (int r = 0; r < rows; r++) {
            float[] srcRow = srcGrid[r];
            float[] dstRow = dstGrid[r];
            for (int c = 0; c < cols; c++) {
                float w = interpolator.interpolateWeight(wFore, wBack, c, r, cols, rows);
                dstRow[c] = srcRow[c] * w;
            }
        }

        return resGrid;
    }

    /**
     * Quick approximation of Math.pow.
     * Can generate relatively large errors!
     * FIXME: needs to be tested for the range of values used here.
     * http://martin.ankerl.com/2007/10/04/optimized-pow-approximation-for-java-and-c-c/
     * @param a
     * @param b
     * @return
     */
    public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }

    /**
     *
     * @param lowFreqSum
     * @param highFreq
     * @param highFreqCurvatureGrid Profile curvature of the high frequency band
     * @param wForeground
     * @param wRidgesForeground
     * @param wValleysForeground
     * @param wBackground
     * @param wRidgesBackground
     * @param wValleysBackground
     * @param ridgesWeeding
     * @param valleysWeeding
     * @param interpolator
     * @param pyramidLevel
     */
    public static void sumGrids(GeoGrid lowFreqSum,
            GeoGrid highFreq,
            GeoGrid highFreqCurvatureGrid,
            float wForeground,
            float wRidgesForeground,
            float wValleysForeground,
            float wBackground,
            float wRidgesBackground,
            float wValleysBackground,
            double ridgesWeeding,
            double valleysWeeding,
            Interpolator interpolator,
            int pyramidLevel) {

        if (!lowFreqSum.hasSameExtensionAndResolution(highFreq)) {
            throw new IllegalArgumentException("grids of different size");
        }

        final int cols = lowFreqSum.getCols();
        final int rows = lowFreqSum.getRows();

        if (highFreqCurvatureGrid == null) {
            for (int r = 0; r < rows; r++) {
                final float[] g1row = lowFreqSum.getGrid()[r];
                final float[] g2row = highFreq.getGrid()[r];
                for (int c = 0; c < cols; c++) {
                    lowFreqSum.setValue(g1row[c] + g2row[c], c, r);
                }
            }
        } else {

            // The basic idea is to only add ridge and valley details, where
            // the previous low-frequency terrain is curved. First detect
            // curved areas using plan curvature.

            // compute plan curvature of previous low frequency sum
            GeoGrid lowFreqPlanCurv = new GridPlanCurvatureOperator().operate(lowFreqSum);
            GeoGrid lowFreqRidgesPlanCurv = lowFreqPlanCurv;
            GeoGrid lowFreqValleysPlanCurv = lowFreqPlanCurv;

            // smooth the plan curvature grids of the low frequency sum to
            // avoid spiky structures in the sum.
            GridGaussLowPassOperator gaussOp = new GridGaussLowPassOperator();
            if (valleysWeeding > 0) {
                gaussOp.setStandardDeviation(valleysWeeding);
                lowFreqValleysPlanCurv = gaussOp.operate(lowFreqPlanCurv);
            }
            if (ridgesWeeding > 0) {
                gaussOp.setStandardDeviation(ridgesWeeding);
                lowFreqRidgesPlanCurv = gaussOp.operate(lowFreqPlanCurv);
            }
            
            for (int r = 0; r < rows; r++) {
                final float[] lowFreqSumRow = lowFreqSum.getGrid()[r];
                final float[] highFreqRow = highFreq.getGrid()[r];
                for (int c = 0; c < cols; c++) {

                    // interpolate weights for ridges, vallyes and the global 
                    // terrain between the foreground and the background to adjust
                    // the level of generalization to the distance from the viewer.
                    float wRidges = interpolator.interpolateWeight(wRidgesForeground, wRidgesBackground, c, r, cols, rows);
                    float wValleys = interpolator.interpolateWeight(wValleysForeground, wValleysBackground, c, r, cols, rows);
                    float wFreqBand = interpolator.interpolateWeight(wForeground, wBackground, c, r, cols, rows);

                    // decide whether to use the weight for ridges or for
                    // valleys. Use the ridges weight if the current pixel is
                    // on a ridge, and vice versa.
                    final float wRidgeOrValley;
                    float curv = highFreqCurvatureGrid.getValue(c, r);
                    if (curv < 0 && wRidges > 0) {
                        wRidgeOrValley = wRidges;
                    } else if (curv > 0 && wValleys > 0) {
                        wRidgeOrValley = wValleys;
                    } else {
                        wRidgeOrValley = 0;
                    }

                    // add more of the high frequency band where the terrain, as
                    // accumulated previously, has higher curvature values.
                    float wLowFreqCurv;
                    if (curv < 0) {
                        wLowFreqCurv = lowFreqRidgesPlanCurv.getValue(c, r);
                    } else {
                        wLowFreqCurv = lowFreqValleysPlanCurv.getValue(c, r);
                    }

                    // do some heuristic scaling and transformation.
                    // Scale by -200 to bring curvature values of high-frequency
                    // bands to values around 3.
                    wLowFreqCurv *= -200;
                    //wLowFreqCurv = (float) (Math.sqrt(Math.abs(wLowFreqCurv)));

                    // adjust the influence of the curvature of the low frequency
                    // sum with the pow function.
                    // valleysWeeding is abused here: FIXME
                    // exponent in (0...1]
                    wLowFreqCurv = (float) (pow(Math.abs(wLowFreqCurv), valleysWeeding / 10d));
                    
                    // compute influence of mask
                    final float wMask;
                    if (mask == null) {
                        wMask = 1f;
                    } else {
                        wMask = mask.getWeight(c, r, pyramidLevel);
                    }

                    // compute the weight of the new high-frequency band.
                    // If the mask is 0, the frequency band is added without any
                    // weighting, i.e. no filtering is applied to the frequency
                    // band.
                    // If the mask is between 0 and 1, the resulting weight is
                    // larger or smaller than 1.
                    // If the mask is 1 (i.e. no masking) the resulting weight
                    // is equal to the sum of wFreqBand + wRidgeOrValley * wLowFreqCurv
                    float w = 1 + (wFreqBand + wRidgeOrValley * wLowFreqCurv - 1) * wMask;

                    // compute the accumulated value
                    lowFreqSumRow[c] = lowFreqSumRow[c] + highFreqRow[c] * w;
                }
            }
        }

    }

    private static boolean pointInFlatArea(GeoGrid grid, int col, int row, double minVertDiff) {

        final float c = grid.getValue(col, row);
        if (col > 0) {
            final float w = grid.getValue(col - 1, row);
            if (Math.abs(c - w) < minVertDiff) {
                return true;
            }
        }
        if (row > 0) {
            final float n = grid.getValue(col, row - 1);
            if (Math.abs(c - n) < minVertDiff) {
                return true;
            }
        }
        if (col < grid.getCols() - 1) {
            final float e = grid.getValue(col + 1, row);
            if (Math.abs(c - e) < minVertDiff) {
                return true;
            }
        }
        if (row < grid.getRows() - 1) {
            final float s = grid.getValue(col, row + 1);
            if (Math.abs(c - s) < minVertDiff) {
                return true;
            }
        }
        return false;

    }

    /**
     * Compute the difference between two grids.
     * @param grid1
     * @param grid2
     * @return
     */
    public static GeoGrid difGrids(GeoGrid grid1, GeoGrid grid2) {

        if (!grid1.hasSameExtensionAndResolution(grid2)) {
            throw new IllegalArgumentException("grids of different size");
        }

        final int cols = grid1.getCols();
        final int rows = grid1.getRows();
        GeoGrid difGrid = new GeoGrid(cols, rows, grid1.getCellSize());
        difGrid.setCellSize(grid1.getCellSize());
        difGrid.setWest(grid1.getWest());
        difGrid.setNorth(grid1.getNorth());

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final float v1 = grid1.getValue(c, r);
                final float v2 = grid2.getValue(c, r);
                difGrid.setValue(v1 - v2, c, r);
            }
        }

        return difGrid;
    }

    /**
     * Sums the levels of the pyramid to re-synthesize the original image.
     * @return
     */
    public GeoGrid sumLevels() {

        // copy the smallest grid of the pyramid
        GeoGrid sum = this.levels[this.levels.length - 1].clone();

        // expand the sum and and add the next larger grids
        for (int i = this.levels.length - 2; i >= 0; i--) {
            GeoGrid grid = this.levels[i];
            sum = LaplacianPyramid.expand(sum, grid.getCols(), grid.getRows());
            LaplacianPyramid.sumGrids(sum, grid, null, 1, 0, 0, 1, 0, 0, 0, 0, null, i);
        }
        return sum;

    }

    /**
     *
     * @param curvatureGrids Profile curvature of the grids.
     * @param wForeground
     * @param wRidgesForeground
     * @param wValleysForeground
     * @param wBackground
     * @param wRidgesBackround
     * @param wValleysBackground
     * @param ridgesWeeding
     * @param valleysWeeding
     * @param interpolator
     * @return
     */
    public GeoGrid sumLevels(
            GeoGrid[] curvatureGrids,
            float[] wForeground,
            float[] wRidgesForeground,
            float[] wValleysForeground,
            float[] wBackground,
            float[] wRidgesBackround,
            float[] wValleysBackground,
            double ridgesWeeding,
            double valleysWeeding,
            Interpolator interpolator) {

        int wID = 0;
        /// multiply top level of the pyramid with its weight
        GeoGrid baseGrid = this.levels[this.levels.length - 1];
        float wFore = wForeground[wID];
        float wBack = wBackground[wID++];
        GeoGrid sum = LaplacianPyramid.distanceWeightedScaling(baseGrid, wFore, wBack, interpolator);

        // add the other levels of the pyramid
        for (int i = levels.length - 2; i >= 0; i--, wID++) {
            GeoGrid nextLargerGrid = levels[i];

            // expand current sum to size of next larger level in the pyramid
            int cols = nextLargerGrid.getCols();
            int rows = nextLargerGrid.getRows();
            sum = LaplacianPyramid.expand(sum, cols, rows);

            // sum the expanded grid with the next larger level
            LaplacianPyramid.sumGrids(sum,
                    nextLargerGrid,
                    curvatureGrids[i],
                    wForeground[wID],
                    wRidgesForeground[wID],
                    wValleysForeground[wID],
                    wBackground[wID],
                    wRidgesBackround[wID],
                    wValleysBackground[wID],
                    ridgesWeeding,
                    valleysWeeding,
                    interpolator,
                    i);
        }
        return sum;

    }

    /**
     * Merge this Laplacian pyramid with another one, based on a mask.
     * @param pyramid
     * @param mask
     */
    public void merge(LaplacianPyramid pyramid, GeoGrid mask) {

        for (int i = 0; i < this.levels.length; i++) {

            GeoGrid smoothMask = GridEdgeMarker.markEdges(mask);
            GeoGrid grid1 = this.levels[i];
            GeoGrid grid2 = pyramid.getLevels()[i];
            final int cols = grid1.getCols();
            final int rows = grid1.getRows();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    float w = smoothMask.getValue(c, r);
                    float v = grid1.getValue(c, r) * w + grid2.getValue(c, r) * (1f - w);
                    grid1.setValue(v, c, r);
                }
            }

            /*try {
            System.out.println("Mask: " + i + " " + mask.toString() + "\n");
            GeoImage maskImg = (GeoImage) new GridToImageOperator().operate(smoothMask);
            ImageIO.write(maskImg.getBufferedImage(), "png", new File("/Volumes/Macintosh HD/Users/jenny/Documents/Java/GridMerger/data/mask" + i + ".png"));
            } catch (IOException ex) {
            Logger.getLogger(LaplacianPyramid.class.getName()).log(Level.SEVERE, null, ex);
            }
             */

            mask = HalveGrid.halve(mask);
        }

    }

    public GeoGrid[] getLevels() {
        return levels;
    }

    /**
     * Exports all levels of the pyramid to ASCII grid files.
     * @param filePath The path to one of the files. Levels will be numbered
     * automatically.
     */
    public void export(String filePath) throws IOException {
        filePath = FileUtils.cutFileExtension(filePath);
        for (int i = 0; i < levels.length; i++) {
            ESRIASCIIGridExporter.export(levels[i], filePath + (i + 1) + ".asc");
        }
    }
}
