/*
 * QModel.java
 *
 * Created on November 16, 2007, 12:03 PM
 *
 */

package ika.proj;

import ika.utils.Announcer;
import java.util.EventListener;

/**
 * Model for the acceptability value.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class QModel {
    
    public interface QListener extends EventListener {
        public void qChanged(QModel newQModel, QModel oldQModel);
    }
    
    public static final int DEFAULT_MAX_ANGLE_DEG = 40;
    
    public static final int DEFAULT_MAX_AREA_PERC = 150;
    
    /**
     * Maximum acceptable angle for computation of Capek's Q index in radians.
     * Default is 40 degrees.
     */
    private double qMaxAngle = Math.toRadians(DEFAULT_MAX_ANGLE_DEG);
    
    /**
     * Maximum acceptable area deviation from the minimum area for computation
     * of Capek's Q index.
     * Default is 1.5.
     */
    private double qMaxAreaScale = DEFAULT_MAX_AREA_PERC / 100d;
    
    /**
     * If true, accepted areas are highlighted in the map.
     */
    private boolean showAcceptableArea = false;
    
    /**
     * Update the distortion values and the visualization while the sliders
     * are being dragged.
     */
    private boolean liveUpdate = true;
    
    /**
     * listeners that are informed whenever an attribute changes.
     */
    private Announcer<QListener> qListeners = Announcer.to(QListener.class);
        
    /** Creates a new instance of QModel */
    public QModel() {
    }
    
    /**
     * copy constructor. Does not copy listeners.
     */
    public QModel (QModel qModel) {
        this.qMaxAngle = qModel.qMaxAngle;
        this.qMaxAreaScale = qModel.qMaxAreaScale;
        this.showAcceptableArea = qModel.showAcceptableArea;
        this.liveUpdate = qModel.liveUpdate;
    }
    
    public void addQListener(QListener  listener) {
        qListeners.addListener(listener);
    }
    
    public void removeQListener(QListener listener) {
        qListeners.removeListener(listener);
    }
    
    protected void announceQModelChanged(QModel oldQModel) {
        qListeners.announce().qChanged(this, oldQModel);
    }
    
    public double getQMaxAngle() {
        return qMaxAngle;
    }
    
    public double getQMaxAreaScale() {
        return qMaxAreaScale;
    }
    
    public String getFormattedMaxValues() {
        StringBuilder sb = new StringBuilder();
        sb.append( Math.round(Math.toDegrees(this.getQMaxAngle())));
        sb.append("\u00B0 ");
        sb.append(Math.round(this.getQMaxAreaScale() * 100));
        sb.append("%");
        return sb.toString();
    }
    
    public boolean isShowAcceptableArea() {
        return showAcceptableArea;
    }
    
    public boolean isLiveUpdate() {
        return liveUpdate;
    }

    /**
     * 
     * @param qMaxAngle maximum acceptable angle in radian, larger than 0.
     * @param qMaxAreaScale Maximum acceptable area scale, larger than 1.
     */
    public void setQ(double qMaxAngle, double qMaxAreaScale) {
        if (this.qMaxAngle == qMaxAngle 
                && this.qMaxAreaScale == qMaxAreaScale) {
            return;
        }
        if (qMaxAreaScale < 1) {
            throw new IllegalArgumentException("Acceptable area distortion smaller than 100%");
        }
        if (qMaxAngle < 0) {
            throw new IllegalArgumentException("Acceptable angle distortion smaller than 0");
        }
        QModel oldModel = new QModel(this);
        this.qMaxAngle = qMaxAngle;
        this.qMaxAreaScale = qMaxAreaScale;
        this.announceQModelChanged(oldModel);
    }

    public void setShowAcceptableArea(boolean showAcceptableArea) {
        if (this.showAcceptableArea != showAcceptableArea) {
            QModel oldModel = new QModel(this);
            this.showAcceptableArea = showAcceptableArea;
            this.announceQModelChanged(oldModel);
        }
    }
    
    public void setLiveUpdate(boolean liveUpdate) {
        this.liveUpdate = liveUpdate;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("QModel max angle:");
        sb.append(Math.toDegrees(this.qMaxAngle));
        sb.append(" max area scale:");
        sb.append((int)(100d * this.qMaxAreaScale));
        sb.append(" relative to minimum:");
        return sb.toString();
    }
}
