package ika.proj;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.StringTokenizer;

/**
 * Combines to projections along a latitude of fusion.
 * Important: this class is not thread safe!
 * @author Bernhard Jenny, Institute of Cartography ETH Zurich
 */
public class LatitudeMixerProjection extends AbstractMixerProjection {

    public static final String FORMAT_IDENTIFIER = "Flex Projector Format 2.0 - Latitude Mixer";

    /**
     * latitude of fusion in degrees
     */
    private double latitude = 45;

    /**
     * transition zone in degrees
     */
    private double tolerance = 20;

    /**
     * scale factor for projection 2 at poles
     */
    private double poleScale = 1D;

    /**
     * whether or not to automatically adjust the scale of the pole projection
     */
    private boolean automaticPoleScale = true;

    /**
     * Precomputed value: vertical shift for projection 2 at poles
     */
    private double poleOffset = 0;

    /**
     * Scale factor applied to the graticule
     */
    private double scale = 1.0;

    /**
     * Vertical scale factor
     */
    private double vScale = 1.0;
    
    public LatitudeMixerProjection() {
        updateAutomaticPoleScale();
        updatePoleOffset();
    }

    @Override
    public Object clone() {
        LatitudeMixerProjection clone = (LatitudeMixerProjection) super.clone();
        clone.p1 = (Projection) this.p1.clone();
        clone.p2 = (Projection) this.p2.clone();
        return clone;
    }

    @Override
    public void initialize() {
        super.initialize();
        p1.initialize();
        p2.initialize();
    }

    @Override
    public boolean canMix(Projection p) {
        // only projections with straight parallel parallels are acceptable
        return p.parallelsAreParallel();
    }

    @Override
    public boolean parallelsAreParallel() {
        return true;
    }

    private void updateAutomaticPoleScale() {
        if (!automaticPoleScale || p1 == null || p2 == null) {
            return;
        }
        Point2D.Double dst1 = new Point2D.Double();
        Point2D.Double dst2 = new Point2D.Double();
        final double blendLatRad = Math.toRadians(latitude);
        p1.project(Math.PI, blendLatRad, dst1);
        p2.project(Math.PI, blendLatRad, dst2);
        poleScale = dst1.x / dst2.x;
    }

    private void updatePoleOffset() {
        if (p1 == null || p2 == null) {
            return;
        }
        Point2D.Double dst1 = new Point2D.Double();
        Point2D.Double dst2 = new Point2D.Double();
        final double blendLatRad = Math.toRadians(latitude);
        p1.project(Math.PI, blendLatRad, dst1);
        p2.project(Math.PI, blendLatRad, dst2);
        final double y2 = dst2.y * poleScale;
        poleOffset = dst1.y - y2;
    }

    @Override
    public void setProjection1(Projection p) {
        super.setProjection1(p);
        updateAutomaticPoleScale();
        updatePoleOffset();
    }

    @Override
    public void setProjection2(Projection p) {
        super.setProjection2(p);
        updateAutomaticPoleScale();
        updatePoleOffset();
    }
    
    @Override
    public Point2D.Double project(double lon, double lat, Point2D.Double dst) {

        final double absLat = Math.abs(lat);
        final double tolRad = Math.toRadians(tolerance);
        final double blendLatRad = Math.toRadians(latitude);

        final double offset = lat > 0 ? poleOffset : -poleOffset;
        // test for >= and <= below to avoid division by zero if tolRad == 0
        if (absLat >= blendLatRad + tolRad) {
            p2.project(lon, lat, dst);
            dst.x *= poleScale;
            dst.y = dst.y * poleScale + offset;
        } else if (absLat <= blendLatRad - tolRad) {
            p1.project(lon, lat, dst);
        } else {
            final double w2 = (absLat - blendLatRad + tolRad) / (2 * tolRad);
            final double w1 = 1D - w2;
            p1.project(lon, lat, dst);

            // copy to temporary variable to avoid the allocation of a Double2D object
            final double x = dst.x;
            final double y = dst.y;

            p2.project(lon, lat, dst);
            dst.x = dst.x * poleScale * w2 + x * w1;
            dst.y = (dst.y * poleScale + offset) * w2 + y * w1;
        }

        dst.x *= scale;
        dst.y *= scale * vScale;
        return dst;
    }

    @Override
    public Point2D.Double projectInverse(double x, double y, Point2D.Double out) {

        try {
            if (p1.hasInverse()) {
                p1.projectInverse(x, y, out);
            } else if (p2.hasInverse()) {
                p2.projectInverse(x, y, out);
            } else {
                out.x = 0;
                out.y = 0;
            }
        } catch (ProjectionException exc) {
            out.x = 0;
            out.y = 0;
        }
        binarySearchInverse(x, y, out.x, out.y / vScale, out);
        return out;
    }

    @Override
    public boolean hasInverse() {
        return true;
    }
    
    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        updateAutomaticPoleScale();
        updatePoleOffset();
    }

    /**
     * @return the tolerance
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Latitude Mixer");
        if (p1 != null && p2 != null) {
            sb.append(": ");
            sb.append(p1.toString());
            sb.append("\u2005\u002B\u2005");
            sb.append(p2.toString());
        }
        return sb.toString();
    }

    /**
     * @return the poleScale
     */
    public double getPoleScale() {
        return poleScale;
    }

    /**
     * @param poleScale the poleScale to set
     */
    public void setPoleScale(double poleScale) {
        this.poleScale = poleScale;
        updatePoleOffset();
    }

    /**
     * @return the scale
     */
    @Override
    public double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    @Override
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @return the automaticScale
     */
    public boolean isAutomaticScale() {
        return automaticPoleScale;
    }

    /**
     * @param automaticScale the automaticScale to set
     */
    public void setAutomaticScale(boolean automaticScale) {
        this.automaticPoleScale = automaticScale;
        updateAutomaticPoleScale();
    }

    @Override
    public double getVerticalScale() {
        return vScale;
    }

    @Override
    public void setVerticalScale(double vScale) {
        this.vScale = vScale;
    }

    @Override
    public String serializeToString() {

        DecimalFormat formatter = new DecimalFormat("##0.#####");
        DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(dfs);
        String lineSep = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        sb.append(FORMAT_IDENTIFIER).append(lineSep);

        sb.append("Vertical Scale:").append(lineSep);
        sb.append(formatter.format(vScale));
        sb.append(lineSep);

        sb.append("Global Scale:").append(lineSep);
        sb.append(formatter.format(scale)).append(lineSep);

        sb.append("Pole Scale:").append(lineSep);
        sb.append(formatter.format(automaticPoleScale ? -1 : poleScale));
        sb.append(lineSep);

        sb.append("Latitude:").append(lineSep);
        sb.append(formatter.format(latitude)).append(lineSep);

        sb.append("Tolerance:").append(lineSep);
        sb.append(formatter.format(tolerance)).append(lineSep);

        sb.append("Projection 1:").append(lineSep);
        sb.append(p1.getName()).append(lineSep);

        sb.append("Projection 2:").append(lineSep);
        sb.append(p2.getName()).append(lineSep);

        return sb.toString();
    }

    @Override
    public void deserializeFromString(String string) {

        // make sure we have a file in Flex Projector format
        if (!string.startsWith(FORMAT_IDENTIFIER)) {
            throw new IllegalArgumentException();
        }

        StringTokenizer tokenizer = new StringTokenizer(string, "\n\r");

        // overread format identifier
        tokenizer.nextToken();

        // overread "Vertical Scale"
        tokenizer.nextToken();
        setVerticalScale(Double.parseDouble(tokenizer.nextToken()));

        // overread "Global Scale"
        tokenizer.nextToken();
        setScale(Double.parseDouble(tokenizer.nextToken()));

        // overread "Pole Scale"
        tokenizer.nextToken();
        double ps = Double.parseDouble(tokenizer.nextToken());
        setPoleScale(ps);
        setAutomaticScale(ps == -1.);

        // overread "Latitude"
        tokenizer.nextToken();
        setLatitude(Double.parseDouble(tokenizer.nextToken()));

        // overread "Tolerance"
        tokenizer.nextToken();
        setTolerance(Double.parseDouble(tokenizer.nextToken()));

        // overread "Projection 1"
        tokenizer.nextToken();
        String proj1Name = tokenizer.nextToken();
        setProjection1(ProjectionFactory.getNamedProjection(proj1Name));
        
        // overread "Projection 2"
        tokenizer.nextToken();
        String proj2Name = tokenizer.nextToken();
        setProjection2(ProjectionFactory.getNamedProjection(proj2Name));
    }
}
