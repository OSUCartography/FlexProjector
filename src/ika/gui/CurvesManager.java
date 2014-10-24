/*
 * CurvesManager.java
 *
 * Created on October 23, 2007, 9:42 PM
 *
 */

package ika.gui;

import com.jhlabs.map.proj.Projection;
import ika.geo.*;
import ika.proj.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

/**
 * Generates and displays the curves defining a flex projection.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class CurvesManager implements ProjectionBrewerPanel.DesignProjectionChangeListener {
    
    private MapComponent curvesMap;
    
    private FlexProjection flexProjection;
    
    private static final int GRID_DIST = 15;
    
    private static final float VAL_SCALE = 50f;
    
    public enum CurveType {X, Y, BENDING, MERIDIANS};
    
    private CurveType curveType = CurveType.X;

    /** Creates a new instance of CurvesManager */
    public CurvesManager(MapComponent curvesMap) {
        this.curvesMap = curvesMap;
    }
    
    @Override
    public void designProjectionChanged(Projection p) {
        if (p instanceof FlexProjection) {
            this.flexProjection = (FlexProjection)p;
        }
        this.updateCurves();
    }
    
    public void updateCurves() {
        if (curvesMap == null) {
            return;
        }
        
        final int maxX = getCurveType() == CurveType.MERIDIANS ? 180 : 90;
        
        curvesMap.removeAllGeoObjects();
        if (this.flexProjection == null) {
            return;
        }
        
        // curve diagram
        String title;
        switch (getCurveType()) {
            case X:
                title = "Length of Parallels";
                curvesMap.addGeoObject(this.constructGrid(title, 0, 1.f, .2f, VAL_SCALE), false);
                break;
            case Y:
                title = "Distance of Parallels from Equator";
                curvesMap.addGeoObject(this.constructGrid(title, 0, 1.f, .2f, VAL_SCALE), false);
                break;
            case BENDING:
                title = "Bending of Parallels";
                curvesMap.addGeoObject(this.constructGrid(title, -1f, 1.f, .2f, VAL_SCALE / 2f), false);
                break;
            case MERIDIANS:
                title = "Distribution of Meridians";
                curvesMap.addGeoObject(this.constructGrid(title, -2f, 2f, .25f, VAL_SCALE / 2f), false);
                break;
            default:
                title = "";
        }
        
        curvesMap.addGeoObject(constructCurves(false), false);
        
        // first derivative diagram
        GeoSet curve = constructCurves(true);
        
        Rectangle2D curveBounds = curve.getBounds2D(GeoObject.UNDEFINED_SCALE);
        float yMin = (float)curveBounds.getMinY();
        float yMax = (float)curveBounds.getMaxY();
        float yScale = VAL_SCALE / (float)curveBounds.getHeight();
        if (curveBounds.getHeight() <= 0) {
            yMin = -1;
            yMax = 1;
            yScale = VAL_SCALE / 2;
            curve.move(maxX + 30, VAL_SCALE / 2);
        } else {
            curve.scale(1, yScale);
            curve.move(maxX + 30, -yMin * yScale);
        }
        
        float horLinesDist = .25f;
        while ((yMax - yMin) / 5 > horLinesDist) {
            horLinesDist *= 2;
        }
        GeoSet grid = this.constructGrid("First Derivative", yMin, yMax, horLinesDist, yScale);
        grid.move(maxX + 30, 0);
        curvesMap.addGeoObject(grid, false);
        curvesMap.addGeoObject(curve, false);
        
        curvesMap.showAll();
    }
    
    private GeoSet constructGrid(String title, double minY, double maxY, double dy, 
            double val_scale) {
        
        GeoSet geoSet = new GeoSet();
        final int maxX = getCurveType() == CurveType.MERIDIANS ? 180 : 90;
        
        // an ugly hack to make sure scale-independent labels are visible
        // add a white box first and draw the rest over it
        GeoPath frame = GeoPath.newRect(-15, minY * val_scale - 15, maxX + 25,
                (maxY - minY) * val_scale + 30);
        geoSet.add(frame);
        
        GeoText titleGeoText = new GeoText(title, 0, maxY * val_scale + 5);
        titleGeoText.setAlignLeft();
        geoSet.add(titleGeoText);
        
        // vertical grid lines
        for (int i = 0; i <= maxX; i += GRID_DIST) {
            GeoPath path = new GeoPath();
            path.moveTo(i, minY * val_scale);
            path.lineTo(i, maxY * val_scale);
            geoSet.add(path);
            
            // labels
            GeoText t = new GeoText(i + "\u00B0", i, minY * val_scale);
            t.setDy(-t.getSize() * 1.2);
            t.setCenterHor(true);
            t.setCenterVer(false);
            geoSet.add(t);
        }
        
        // horizontal grid lines and labels
        DecimalFormat format = new DecimalFormat("0.0");
        for (double y = minY; y <= maxY; y += dy) {
            GeoPath path = new GeoPath();
            path.moveTo(0, y * val_scale);
            path.lineTo(maxX, y * val_scale);
            geoSet.add(path);
            
            // labels
            GeoText t = new GeoText(format.format(y), 0, y * val_scale);
            t.setDx(-2);
            t.setAlignRight();
            t.setCenterVer(true);
            geoSet.add(t);
        }
        
        // topmost horizontal line
        GeoPath path = new GeoPath();
        path.moveTo(0, maxY * val_scale);
        path.lineTo(maxX, maxY * val_scale);
        geoSet.add(path);
        
        VectorSymbol symbol = new VectorSymbol(null, Color.BLACK, 1);
        symbol.setScaleInvariant(true);
        geoSet.setVectorSymbol(symbol);
        
        // hack
        frame.setVectorSymbol(new VectorSymbol(null, Color.WHITE, 0));
        
        // move vertically to 0
        geoSet.move(0, -minY * val_scale);
        
        return geoSet;
    }
    
    private GeoSet constructCurves(boolean derivative) {
        
        final int maxX = getCurveType() == CurveType.MERIDIANS ? 180 : 90;
        
        GeoSet geoSet = new GeoSet();
        GeoPath curve = new GeoPath();
        
        FlexProjectionModel model = this.flexProjection.getModel();
        for (int i = 0; i <= maxX; i++) {
            final double lat = Math.toRadians(i);
            double y = 0;
            switch (getCurveType()) {
                case X:
                    y = derivative 
                            ? model.getLongitudeScaleFactorFirstDerivative(lat)
                            : model.getLongitudeScaleFactor(lat);
                    break;
                case Y:
                    y = derivative 
                            ? model.getLatitudeScaleFactorFirstDerivative(lat)
                            : model.getLatitudeScaleFactor(lat);
                    break; 
                case BENDING:
                    y = derivative
                            ? model.getBendFactorFirstDerivative(lat)
                            : model.getBendFactor(lat) * 0.5f + 0.5f;
                    break;
                case MERIDIANS:
                    y = derivative
                            ? model.getXDistFactorFirstDerivative(lat)
                            : model.getXDistFactor(lat) * 0.5f + 1f;
                    break;
            }
            curve.moveOrLineTo(i, y * VAL_SCALE);
        }
        
        VectorSymbol symbol = new VectorSymbol(null, Color.BLUE, 1);
        symbol.setScaleInvariant(true);
        curve.setVectorSymbol(symbol);
        geoSet.add(curve);
        
        return geoSet;
    }
    
    public void setFlexProjection(FlexProjection flexProjection) {
        this.flexProjection = flexProjection;
    }

    public CurveType getCurveType() {
        return curveType;
    }

    public void setCurveType(CurveType curveType) {
        this.curveType = curveType;
    }
    
}
