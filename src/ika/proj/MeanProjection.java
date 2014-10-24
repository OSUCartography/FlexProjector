package ika.proj;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.StringTokenizer;

/**
 * Arithmetic weighted mean of two projections.
 * @author Bernhard Jenny, Institute of Cartography ETH Zurich
 */
public class MeanProjection extends AbstractMixerProjection {

    public static final String FORMAT_IDENTIFIER = "Flex Projector Format 2.0 - Mean Mixer";

    /**
     * weight for mixing the two projections
     */
    private double weight = 0.5;

    private double scale = 1.0;

    private double vScale = 1.0;

    public MeanProjection() {
    }

    @Override
    public Object clone() {
        MeanProjection clone = (MeanProjection) super.clone();
        clone.p1 = (Projection) this.p1.clone();
        clone.p2 = (Projection) this.p2.clone();
        return clone;
    }

    @Override
    public boolean parallelsAreParallel() {
        return p1.parallelsAreParallel() && p2.parallelsAreParallel();
    }
    
    @Override
    public Point2D.Double project(double lon, double lat, Point2D.Double dst) {

        final double w2 = weight;
        final double w1 = 1D - w2;
        p1.project(lon, lat, dst);
        // copy to temporary variable to avoid the allocation of a Point2D object
        double x = dst.x;
        double y = dst.y;

        p2.project(lon, lat, dst);
        dst.x = (dst.x * w2 + x * w1) * scale;
        dst.y = (dst.y * w2 + y * w1) * scale * vScale;

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
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Simple Mixer: " + p1.toString() + "\u2005\u002B\u2005" + p2.toString();
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

        sb.append("Weight:").append(lineSep);
        sb.append(formatter.format(weight)).append(lineSep);
        
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
        vScale = Double.parseDouble(tokenizer.nextToken());

        // overread "Global Scale"
        tokenizer.nextToken();
        scale = Double.parseDouble(tokenizer.nextToken());

        // overread "Weight"
        tokenizer.nextToken();
        weight = Double.parseDouble(tokenizer.nextToken());

        // overread "Projection 1"
        tokenizer.nextToken();
        String proj1Name = tokenizer.nextToken();
        p1 = ProjectionFactory.getNamedPROJ4Projection(proj1Name);

        // overread "Projection 2"
        tokenizer.nextToken();
        String proj2Name = tokenizer.nextToken();
        p2 = ProjectionFactory.getNamedPROJ4Projection(proj2Name);
    }
}
