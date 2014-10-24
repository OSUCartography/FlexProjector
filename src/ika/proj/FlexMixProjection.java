package ika.proj;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.StringTokenizer;

/**
 * Mixes two projections by first converting them to Flex projections and then
 * blending them to a single Flex projection.
 * @author Bernhard Jenny, Institute of Cartography ETH Zurich
 */
public class FlexMixProjection extends AbstractMixerProjection {

    public static final String FORMAT_IDENTIFIER = "Flex Projector Format 2.0 - Flex Mixer";
    
    /**
     * The mixed projection is cashed for accelerating computations.
     */
    private FlexProjection flexP;
    
    /**
     * Weight factor for blending length of parallels between proj1 and proj2.
     */
    private double lengthW = 0.5;
    
    /**
     * Weight factor for blending vertical distribution of parallels between proj1 and proj2.
     */
    private double distanceW = 0.5;
    
    /**
     * Weight factor for bending of parallels between proj1 and proj2.
     */
    private double bendingW = 0.5;
    
    /**
     * Weight factor for the horizontal distribution of parallels between proj1 and proj2.
     */
    private double meridiansW = 0.5;

    public FlexMixProjection() {
        flexP = new FlexProjection();
        flexP.getModel().setScaleY(0.6);
        flexP.getModel().setScale(1);
        initialize();
    }

    @Override
    public FlexMixProjection clone() {
        FlexMixProjection clone = (FlexMixProjection) super.clone();
        clone.p1 = (Projection) this.p1.clone();
        clone.p2 = (Projection) this.p2.clone();
        clone.initialize();
        return clone;
    }

    @Override
    public void initialize() {
        super.initialize();
        
        // setup the two projections that will be mixed
        FlexProjectionModel model1 = toFlexProjection(p1);
        FlexProjectionModel model2 = toFlexProjection(p2);
        if (model1 == null || model2 == null) {
            throw new ProjectionException("Failure when building flex projection");
        }
        double scale = flexP.getModel().getScale();
        double scaleY = flexP.getModel().getScaleY();

        // mix the two projections, model1 contains the new mix
        model1.mixCurves(model2, lengthW, distanceW, bendingW, meridiansW);

        // replace the model
        model1.setScale(scale);
        model1.setScaleY(scaleY);
        flexP.setModel(model1);
        flexP.initialize();
    }

    @Override
    public boolean parallelsAreParallel() {
        initialize();
        return flexP.parallelsAreParallel();
    }

    /**
     * Returns true if this projection has an inverse
     */
    @Override
    public boolean hasInverse() {
        return true;
    }

    @Override
    public Point2D.Double project(double x, double y, Point2D.Double dst) {
        return flexP.project(x, y, dst);
    }

    @Override
    public Point2D.Double projectInverse(double x, double y, Point2D.Double lp) {
        return flexP.projectInverse(x, y, lp);
    }

    /**
     * Returns true if the parallels are bended, i.e. the b array contains
     * non-zero values.
     */
    public boolean isAdjustingBending() {
        initialize();
        return flexP.getModel().isAdjustingBending();
    }

    /**
     * Returns true if the distance between meridians varies, i.e. the xDist
     * array contains non-zero values.
     */
    public boolean isAdjustingMeridians() {
        initialize();
        return flexP.getModel().isAdjustingMeridians();
    }

    /**
     * Returns the flex curves and other flex parameters for any projection.
     * @param projection
     * @return 
     */
    private static FlexProjectionModel toFlexProjection(Projection projection) {
        if (projection == null) {
            return null;
        }
        projection.initialize();
        FlexProjection flex = new FlexProjection();
        flex.getModel().reset(projection);
        flex.initialize();
        return flex.getModel();
    }

    @Override
    public String toString() {
        return "Flex Mixer: " + p1.toString() + "\u2005\u002B\u2005" + p2.toString();
    }

    /**
     * @return the lengthW
     */
    public double getLengthW() {
        return lengthW;
    }

    /**
     * @param lengthW the lengthW to set
     */
    public void setLengthW(double lengthW) {
        this.lengthW = lengthW;
    }

    /**
     * @return the distanceW
     */
    public double getDistanceW() {
        return distanceW;
    }

    /**
     * @param distanceW the distanceW to set
     */
    public void setDistanceW(double distanceW) {
        this.distanceW = distanceW;
    }

    /**
     * @return the bendingW
     */
    public double getBendingW() {
        return bendingW;
    }

    /**
     * @param bendingW the bendingW to set
     */
    public void setBendingW(double bendingW) {
        this.bendingW = bendingW;
    }

    /**
     * @return the meridiansW
     */
    public double getMeridiansW() {
        return meridiansW;
    }

    /**
     * @param meridiansW the meridiansW to set
     */
    public void setMeridiansW(double meridiansW) {
        this.meridiansW = meridiansW;
    }

    /**
     * @return the p1
     */
    public Projection getP1() {
        return p1;
    }

    /**
     * @return the p2
     */
    public Projection getP2() {
        return p2;
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

        sb.append("Global Scale:").append(lineSep);
        sb.append(formatter.format(getScale())).append(lineSep);

        sb.append("Vertical Scale:").append(lineSep);
        sb.append(formatter.format(getVerticalScale())).append(lineSep);

        sb.append("Length Weight:").append(lineSep);
        sb.append(formatter.format(lengthW)).append(lineSep);

        sb.append("Distance Weight:").append(lineSep);
        sb.append(formatter.format(distanceW)).append(lineSep);

        sb.append("Bending Weight:").append(lineSep);
        sb.append(formatter.format(bendingW));
        sb.append(lineSep);

        sb.append("Meridians Weight:").append(lineSep);
        sb.append(formatter.format(meridiansW)).append(lineSep);

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

        // overread "Global Scale"
        tokenizer.nextToken();
        setScale(Double.parseDouble(tokenizer.nextToken()));

        // overread "Vertical Scale"
        tokenizer.nextToken();
        setVerticalScale(Double.parseDouble(tokenizer.nextToken()));

        // overread "Length Weight"
        tokenizer.nextToken();
        setLengthW(Double.parseDouble(tokenizer.nextToken()));

        // overread "Distance Weight"
        tokenizer.nextToken();
        setDistanceW(Double.parseDouble(tokenizer.nextToken()));

        // overread "Bending Weight"
        tokenizer.nextToken();
        setBendingW(Double.parseDouble(tokenizer.nextToken()));

        // overread "Meridians Weight"
        tokenizer.nextToken();
        setMeridiansW(Double.parseDouble(tokenizer.nextToken()));

        // overread "Projection 1"
        tokenizer.nextToken();
        String proj1Name = tokenizer.nextToken();
        p1 = ProjectionFactory.getNamedPROJ4Projection(proj1Name);

        // overread "Projection 2"
        tokenizer.nextToken();
        String proj2Name = tokenizer.nextToken();
        p2 = ProjectionFactory.getNamedPROJ4Projection(proj2Name);
    }

    @Override
    public double getScale() {
        return flexP.getScale();
    }

    @Override
    public void setScale(double scale) {
        flexP.setScale(scale);
    }

    @Override
    public double getVerticalScale() {
        return flexP.getVerticalScale();
    }

    @Override
    public void setVerticalScale(double vScale) {
        flexP.setVerticalScale(vScale);
    }
}
