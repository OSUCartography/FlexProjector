/*
 * ProjectionBrewerPanel.java
 *
 * Created on May 12, 2007, 9:16 AM
 */
package ika.gui;

import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import ika.geo.FlexProjectorModel;
import ika.geo.FlexProjectorModel.DisplayModel;
import ika.geo.GeoPath;
import ika.geo.GeoSet;
import ika.geo.MapEventTrigger;
import ika.geo.VectorSymbol;
import ika.proj.AbstractMixerProjection;
import ika.proj.DesignProjection;
import ika.proj.FlexMixProjection;
import ika.proj.FlexProjection;
import ika.proj.FlexProjectionModel;
import ika.proj.LatitudeMixerProjection;
import ika.proj.MeanProjection;
import ika.proj.ProjectionDistortionParameters;
import ika.proj.ProjectionsManager;
import ika.proj.QModel;
import ika.utils.Announcer;
import ika.utils.ErrorDialog;
import ika.utils.FileUtils;
import ika.utils.PropertiesLoader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ProjectionBrewerPanel extends javax.swing.JPanel {

    /**
     * KEEP_ASPECT_CONSTANT_HACK
     * A hack to force the aspect ratio to a constant value. Added for designing
     * cylindrical projections with fixed aspect ratios.
     * It will not work for non-cylindrical projections.
     */
    private static final boolean KEEP_ASPECT_CONSTANT_HACK = false;
    private static final double SLIDER_SCALE = 1000d;
    private static final int SLIDER_WIDTH = 250;
    private static final int SLIDER_HEIGHT = 22;
    private static final int FIELD_WIDTH = 65;
    private static final int FIELD_HEIGHT = 20;
    private static final int BORDER_INSET = 0;
    public static final int PEAKCURVE = 0;
    public static final int BELLCURVE = 1;
    public static final int LINEARCURVE = 2;
    private static final int COMBO_BOX_WIDTH = 80;
    /**
     * if true, projections are adjusted while sliders are dragged.
     */
    private final boolean liveUpdate = false;
    private final JSlider xSliders[];
    private final JSlider ySliders[];
    private final JSlider bSliders[];
    private final JSlider xDistSliders[];
    private final JFormattedTextField xNumbers[];
    private final JFormattedTextField yNumbers[];
    private final JFormattedTextField bNumbers[];
    private final JFormattedTextField xDistNumbers[];
    private FlexProjectorModel model = new FlexProjectorModel();
    private transient boolean updatingGUI = false;
    private MapComponent mapComponent;
    private DistortionProfilesManager distortionProfilesManager;
    /**
     * Updates the distortion table entry for the design projection in a
     * separate thread. This uses a single thread, so multiple calls are
     * guaranteed to be executed sequentially.
     */
    private final ExecutorService asynchTableUpdater = Executors.newSingleThreadExecutor();
    /**
     * listeners that are informed whenever the design projection changes.
     */
    private final Announcer<DesignProjectionChangeListener> designProjectionChangeListeners 
            = Announcer.to(DesignProjectionChangeListener.class);

    public interface DesignProjectionChangeListener extends EventListener {

        public void designProjectionChanged(Projection p);
    }

    /**
     * Creates new form ProjectionBrewerPanel
     */    
    public ProjectionBrewerPanel() {
        if (KEEP_ASPECT_CONSTANT_HACK) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "Special version with constant aspect ratio");
                }
            });
        }
        
        GUIUtil.getPreferredSize(new JPanel(), 100);
        initComponents();

        // create the sliders, labels and number fields
        this.xSliders = new JSlider[19];
        this.xNumbers = new JFormattedTextField[19];
        this.ySliders = new JSlider[19];
        this.yNumbers = new JFormattedTextField[19];
        this.bSliders = new JSlider[19];
        this.bNumbers = new JFormattedTextField[19];
        this.xDistSliders = new JSlider[13];
        this.xDistNumbers = new JFormattedTextField[13];

        ChangeListener lengthChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                lengthSliderStateChanged(evt);
            }
        };
        createFlexSliderGUI(lengthSlidersPanel, xSliders, xNumbers, 0, 5, 0, 1, lengthChangeListener);

        ChangeListener distanceChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                distanceSliderStateChanged(evt);
            }
        };
        createFlexSliderGUI(distanceSlidersPanel, ySliders, yNumbers, 0, 5, 0, 1, distanceChangeListener);

        ChangeListener bendingChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                bendingSliderStateChanged(evt);
            }
        };
        createFlexSliderGUI(bendingSlidersPanel, bSliders, bNumbers, 0, 5,
                FlexProjectionModel.MIN_BENDING, FlexProjectionModel.MAX_BENDING,
                bendingChangeListener);

        ChangeListener distributionChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evt) {
                distributionSliderStateChanged(evt);
            }
        };
        createFlexSliderGUI(meridiansSlidersPanel, xDistSliders, xDistNumbers, 0, 15,
                FlexProjectionModel.MIN_MERIDIANS_DIST, FlexProjectionModel.MAX_MERIDIANS_DIST,
                distributionChangeListener);

        // remove the slider for vertical 0 degrees, since it makes no sense
        // to move the equator.
        distanceSlidersPanel.remove(ySliders[0]);
        distanceSlidersPanel.invalidate();
        ySliders[0] = null;
        // disable the corresponding number field
        yNumbers[0].setEnabled(false);
        yNumbers[0] = null;

        /*
         // remove the slider for bending at 0 degrees
         bendingSlidersPanel.remove(bSliders[0]);
         bendingSlidersPanel.invalidate();
         // disable the corresponding number field
         bNumbers[0].setEnabled(false);
         bNumbers[0] = null;
         */
        // disable the slider for distributing the meridian at 0 and 180 degrees
        meridiansSlidersPanel.remove(xDistSliders[0]);
        meridiansSlidersPanel.remove(xDistSliders[xDistSliders.length - 1]);
        // disable the corresponding number fields
        xDistNumbers[0].setEnabled(false);
        xDistSliders[0] = null;
        xDistNumbers[xDistNumbers.length - 1].setEnabled(false);
        xDistSliders[xDistSliders.length - 1] = null;

        writeMethodGUI();
        writeDisplayGUI();

        // init the content of the menu with the background projections
        DefaultComboBoxModel menuModel = new DefaultComboBoxModel();
        menuModel.addElement("Flex Projection (from External File)");
        List<String> selProjs = ProjectionsManager.getSelectedProjectionNames();
        for (String proj : selProjs) {
            menuModel.addElement(proj);
        }
        projectionComboBox.setModel(menuModel);
        projectionComboBox.setSelectedItem("Kavraisky VII");

        // initialize the mixer GUI
        initMixerMenus();
        writeVerticalScaleGUI();
        writeMethodGUI();

        // register a listener for changes to the projection for updating
        // the two small mixer maps and to make sure the designed projection is
        // visible
        DesignProjectionChangeListener fl = new DesignProjectionChangeListener() {
            @Override
            public void designProjectionChanged(Projection p) {

                updateMixerMap(model.getMixerProjection1(), mixerMap1);
                updateMixerMap(model.getMixerProjection2(), mixerMap2);
            }
        };
        addFlexListener(fl);

    }

    protected void showDesignProjection() {
        if (!model.getDisplayModel().showFlexProjection) {
            showFlexCheckBox.doClick();
        }
    }

    private void initMixerMenus() {

        try {
            updatingGUI = true;
            Projection foregroundProj = model.getDesignProjection();
            if (!(foregroundProj instanceof AbstractMixerProjection)) {
                return;
            }

            // find the two mixed projections
            AbstractMixerProjection mixProj = (AbstractMixerProjection) foregroundProj;
            Projection proj1 = mixProj.getProjection1();
            Projection proj2 = mixProj.getProjection2();

            DefaultComboBoxModel model1 = new DefaultComboBoxModel();
            DefaultComboBoxModel model2 = new DefaultComboBoxModel();

            // only projections with straight horizontal parallels can be merged
            // along a latitude.
            boolean onlyCyl = foregroundProj instanceof LatitudeMixerProjection;
            boolean labelApproximated = foregroundProj instanceof FlexProjection;
            List<String> projs;
            projs = ProjectionsManager.getProjectionNames(labelApproximated, onlyCyl);

            // populate the menus
            for (String proj : projs) {
                model1.addElement(proj);
                model2.addElement(proj);
            }
            this.mixerComboBox1.setModel(model1);
            this.mixerComboBox2.setModel(model2);

            // adjust selection in menus
            if (proj1 instanceof DesignProjection) {
                // select entry to load external flex file
                model1.setSelectedItem(ProjectionsManager.SELECT_FLEX_FILE_STRING);
            } else {
                model1.setSelectedItem(proj1.toString());
            }

            // adjust selection in menus
            if (proj2 instanceof DesignProjection) {
                // select entry to load external flex file
                model2.setSelectedItem(ProjectionsManager.SELECT_FLEX_FILE_STRING);
            } else {
                model2.setSelectedItem(proj2.toString());
            }

            // setup the mixer maps
            this.mixerMap1.getPageFormat().setVisible(false);
            this.mixerMap2.getPageFormat().setVisible(false);
            this.updateMixerMap(model.getMixerProjection1(), mixerMap1);
            this.updateMixerMap(model.getMixerProjection2(), mixerMap2);
        } finally {
            updatingGUI = false;
        }
    }

    /**
     * Generates and displays the content for a mixer map.
     *
     * @param projection The projection that is used for the map.
     * @param mixerMapComponent The map component to update.
     */
    private void updateMixerMap(Projection projection,
            MapComponent mixerMapComponent) {

        if (projection == null || mixerMapComponent == null || model == null) {
            return;
        }

        // set central longitude
        double lon0 = model.getDesignProjection().getProjectionLongitude();
        projection.setProjectionLongitude(lon0);
        projection.initialize();

        // get the projected map data
        VectorSymbol symbol = new VectorSymbol(null,
                FlexProjectorPreferencesPanel.getFlexColor(), 1);
        symbol.setScaleInvariant(true);
        GeoSet coastLines = model.constructProjectedCoastlines(projection);
        GeoSet graticule = model.constructGraticule(projection);
        GeoPath outline = FlexProjectorModel.constructOutline(projection);

        mixerMapComponent.removeAllGeoObjects();

        coastLines.setVectorSymbol(symbol);
        graticule.setVectorSymbol(symbol);
        outline.setVectorSymbol(symbol);

        coastLines.setSelectable(false);
        graticule.setSelectable(false);
        outline.setSelectable(false);

        mixerMapComponent.addGeoObject(coastLines, false);
        mixerMapComponent.addGeoObject(graticule, false);
        mixerMapComponent.addGeoObject(outline, false);
        mixerMapComponent.showAll();
    }

    private boolean scalesDiffer(double scale1, double scale2) {
        double scaleDif = scale1 - scale2;
        return Math.abs(scaleDif) >= 0.001;
    }

    /**
     * Adjusts the GUI for selecting the internal scale to the current scale. If
     * the current scale is close to the scale that minimizes the overall area
     * distortion, the according radio button is selected. If the scale is close
     * to the scale that results in a graticule with the size of the generating
     * sphere, the according radio button is selected. Otherwise the "manual"
     * radio button is selected.
     */
    private void writeInternalScaleGUI() {

        DesignProjection proj = model.getDesignProjection();
        double initialScale = proj.getScale();
        try {
            updatingGUI = true;

            // test whether the scale is close to the scale that minimizes
            // the overall area distortion
            proj.computeAreaDistortionMinimizingScale();
            double minAreaDistScale = proj.getScale();
            if (!scalesDiffer(initialScale, minAreaDistScale)) {
                scaleMinimumAreaDistRadioButton.setSelected(true);
                return;
            }

            // test whether the scale is close to the scale that results in
            // a graticule with the size of the generating sphere
            proj.adjustScaleToEarthArea();
            double equalAreaScale = proj.getScale();
            if (!scalesDiffer(initialScale, equalAreaScale)) {
                scaleAreaOfGlobeRadioButton.setSelected(true);
                return;
            }

            // use manual adjustment
            proj.setScale(initialScale);
            scaleManualRadioButton.setSelected(true);
            scaleManualSlider.setValue((int) Math.round(initialScale * SLIDER_SCALE));
            scaleManualField.setValue(initialScale);

        } catch (Exception e) {
            proj.setScale(initialScale);
            scaleManualRadioButton.setSelected(true);
        } finally {
            enableInternalScaleGUI();
            updatingGUI = false;
        }
    }

    /**
     * Read and update GUI for internal scale factor of the projection.
     */
    private void readInternalScaleGUI() {
        DesignProjection proj = model.getDesignProjection();

        // adjust scale and update associated GUI
        boolean minAreaDist = scaleMinimumAreaDistRadioButton.isSelected();
        boolean globeArea = scaleAreaOfGlobeRadioButton.isSelected();
        boolean pointScale = scalePointRadioButton.isSelected();
        boolean manualScale = !minAreaDist && !globeArea && !pointScale;

        if (minAreaDist) {
            proj.computeAreaDistortionMinimizingScale();
        } else if (globeArea) {
            proj.adjustScaleToEarthArea();
        } else if (pointScale) {
            double lon = Math.toRadians(((Number) scaleLonField.getValue()).doubleValue());
            double lat = Math.toRadians(((Number) scaleLatField.getValue()).doubleValue());
            lon = MapMath.normalizeLongitude(lon + proj.getProjectionLongitude());
            lat = MapMath.normalizeLatitude(lat);
            proj.eliminateAreaDistortionForPoint(lon, lat);
        } else if (manualScale) {
            double s = ((Number) scaleManualField.getValue()).doubleValue();
            proj.setScale(s);
        }

        if (!manualScale) {
            try {
                updatingGUI = true;
                scaleManualSlider.setValue((int) (proj.getScale() * SLIDER_SCALE));
                scaleManualField.setValue(proj.getScale());
            } finally {
                updatingGUI = false;
            }
        }

        enableInternalScaleGUI();

        // update label indicating relative size of current flex projection
        writeSizeLabel();
    }

    private void enableInternalScaleGUI() {
        boolean pointScale = scalePointRadioButton.isSelected();
        boolean manualScale = scaleManualRadioButton.isSelected();

        scaleManualSlider.setEnabled(manualScale);
        scaleManualField.setEnabled(manualScale);
        scaleLonSlider.setEnabled(pointScale);
        scaleLatSlider.setEnabled(pointScale);
        scaleLonField.setEnabled(pointScale);
        scaleLatField.setEnabled(pointScale);
    }

    protected void updateDistortionIndicesAndInformListeners() {

        if (updatingGUI) {
            return;
        }

        // Update the distortion parameters of the foreground projection.
        // These parameters are displayed in a table and are used for
        // distortion visualizations.
        // This is done in another thread to keep the GUI responsive.

        // FIXME concurrent access to foreProj, dist and qModel
        final Projection foreProj = model.getDesignProjection();
        asynchTableUpdater.execute(new Runnable() {
            @Override
            public void run() {

                ProjectionDistortionParameters dist = model.getDisplayModel().foreDist;
                QModel qModel = model.getDisplayModel().qModel;
                dist.computeDistortionIndices(qModel, foreProj);

                // inform listeners in event dispatching thread
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        designProjectionChangeListeners.announce().designProjectionChanged(foreProj);
                    }
                });

            }
        });

        // inform listeners
        designProjectionChangeListeners.announce().designProjectionChanged(foreProj);
    }

    public final void addFlexListener(DesignProjectionChangeListener listener) {
        designProjectionChangeListeners.addListener(listener);
    }

    public void removeFlexListener(DesignProjectionChangeListener listener) {
        designProjectionChangeListeners.removeListener(listener);
    }

    /**
     * Fill a panel with sliders and number fields.
     *
     * @param panel
     * @param sliders
     * @param fields
     * @param startLabel
     * @param labelIncrement
     * @param minVal
     * @param maxVal
     * @param changeListener
     */
    private void createFlexSliderGUI(JPanel panel,
            JSlider[] sliders,
            JFormattedTextField[] fields,
            int startLabel,
            int labelIncrement,
            double minVal,
            double maxVal,
            ChangeListener changeListener) {

        try {
            updatingGUI = true;
            for (int i = 0; i < sliders.length; ++i) {

                // create label
                JLabel label = new javax.swing.JLabel();
                label.setText(Integer.toString(startLabel + i * labelIncrement) + '\u00B0');
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = sliders.length - i;
                gridBagConstraints.insets = new java.awt.Insets(0, BORDER_INSET, 0, 0);
                gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
                panel.add(label, gridBagConstraints);

                // create slider
                JSlider slider = new javax.swing.JSlider();
                sliders[i] = slider;
                slider.setMinimum((int) (minVal * SLIDER_SCALE));
                slider.setMaximum((int) (maxVal * SLIDER_SCALE));
                GUIUtil.setPreferredWidth(slider, SLIDER_WIDTH);
//                slider.setPreferredSize(new Dimension(SLIDER_WIDTH, SLIDER_HEIGHT));
                slider.setMinimumSize(new Dimension(SLIDER_WIDTH, SLIDER_HEIGHT));
                slider.addChangeListener(changeListener);

                // add slider to panel
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = sliders.length - i;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                panel.add(slider, gridBagConstraints);

                // create number field
                JFormattedTextField field = fields[i] = new JFormattedTextField();
                NumberFormatter nf = new NumberFormatter(new DecimalFormat("#,###.#####"));
                nf.setMinimum(minVal);
                nf.setMaximum(maxVal);
                field.setFormatterFactory(new DefaultFormatterFactory(nf));
                GUIUtil.setPreferredWidth(field, FIELD_WIDTH);
                field.setMinimumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
                field.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    @Override
                    public void propertyChange(java.beans.PropertyChangeEvent evt) {
                        flexFieldPropertyChange(evt);
                    }
                });

                // add field to panel
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = fields.length - i;
                gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, BORDER_INSET);
                panel.add(field, gridBagConstraints);
            }

            panel.invalidate();

        } finally {
            updatingGUI = false;
        }
    }

    public final void writeFlexSliderGUI() {

        try {
            updatingGUI = true;

            flexSlidersToggleButton.setSelected(true);

            // write the flex projection parameters
            FlexProjectionModel flexModel = model.getFlexProjectionModel();

            // length of parallels
            for (int i = 0; i < this.xSliders.length; i++) {
                xSliders[i].setValue((int) (flexModel.getX(i) * SLIDER_SCALE));
                xNumbers[i].setValue(flexModel.getX(i));
            }

            // pole direction
            poleDirectionCheckBox.setSelected(flexModel.isAdjustPoleDirection());
            poleDirectionSlider.setEnabled(flexModel.isAdjustPoleDirection());
            poleDirectionSlider.setValue((int) flexModel.getMeridiansPoleDirection());

            poleDirectionFormattedTextField.setEnabled(flexModel.isAdjustPoleDirection());
            poleDirectionFormattedTextField.setValue(flexModel.getMeridiansPoleDirection());


            // shape of meridians at the equator
            meridianAngularRadioButton.setSelected(!flexModel.isMeridiansSmoothAtEquator());
            meridianSmoothRadioButton.setSelected(flexModel.isMeridiansSmoothAtEquator());

            // distance of parallels from equator
            for (int i = 1; i < ySliders.length; i++) {
                ySliders[i].setValue((int) (flexModel.getY(i) * SLIDER_SCALE));
                yNumbers[i].setValue(flexModel.getY(i));
            }

            // bending of parallels
            for (int i = 0; i < bSliders.length; i++) {
                bSliders[i].setValue((int) (flexModel.getB(i) * SLIDER_SCALE));
                bNumbers[i].setValue(flexModel.getB(i));
            }
            bendingComboBox.setSelectedIndex(flexModel.getCurveShape());

            // distribution of meridians
            for (int i = 0; i < xDistSliders.length; i++) {
                if (xDistSliders[i] != null) {
                    xDistSliders[i].setValue((int) (flexModel.getXDist(i) * SLIDER_SCALE));
                }
                xDistNumbers[i].setValue(flexModel.getXDist(i));
            }

            // proportion
            int intScale = (int) Math.round(flexModel.getScaleY() * SLIDER_SCALE);
            verticalScaleSlider.setValue(intScale);
            verticalScaleFormattedTextField.setValue(flexModel.getScaleY());

            // scale
            scaleManualSlider.setValue((int) Math.round(flexModel.getScale() * SLIDER_SCALE));
            scaleManualField.setValue(flexModel.getScale());

        } finally {
            updatingGUI = false;
        }
    }

    public final void writeDisplayGUI() {

        try {
            updatingGUI = true;

            FlexProjectorModel.DisplayModel displayModel = model.getDisplayModel();

            // show or hide the flex projection
            showFlexCheckBox.setSelected(displayModel.showFlexProjection);

            // show or hide the second projection
            showSecondProjectionCheckBox.setSelected(displayModel.showSecondProjection);
            projectionComboBox.setSelectedItem(displayModel.projection.getName());
            switch (displayModel.secondProjectionAdjustment) {
                case FlexProjectorModel.DisplayModel.ADJUST_NO:
                    adjustNoRadioButton.setSelected(true);
                    break;
                case FlexProjectorModel.DisplayModel.ADJUST_WIDTH:
                    adjustWidthRadioButton.setSelected(true);
                    break;
                case FlexProjectorModel.DisplayModel.ADJUST_HEIGHT:
                    adjustHeightRadioButton.setSelected(true);
                    break;
            }

            // graticule
            showGraticuleCheckBox.setSelected(displayModel.showGraticule);
            DecimalFormat formatter = new DecimalFormat("##0.#");
            String graticuleDensity = formatter.format(displayModel.graticuleDensity);
            graticuleComboBox.setSelectedItem(graticuleDensity);

            // tissot indicatrices
            showTissotCheckBox.setSelected(displayModel.showTissot);
            String tissotDensity = formatter.format(displayModel.tissotDensity);
            tissotComboBox.setSelectedItem(tissotDensity);

            // acceptance visualization
            showAcceptableAreaCheckBox.setSelected(displayModel.qModel.isShowAcceptableArea());
            String accStr = showAcceptableAreaCheckBox.getName() + " (";
            accStr += displayModel.qModel.getFormattedMaxValues() + ")";
            showAcceptableAreaCheckBox.setText(accStr);

            writeDisplayEnabledState();
        } finally {
            updatingGUI = false;
        }
    }

    /**
     * @param sliders An array holding all neighboring sliders.
     * @param centralID The ID of the slider in the array that changed.
     * @param centralDiff The value of the central slider changed by this
     * amount.
     * @param values An array holding the values of all sliders.
     * @param firstValID ID of first slider in sliders array.
     * @param lastValID ID of last slider in sliders array.
     * @param minVal Minimum new value.
     * @param maxVal Maximum new value.
     */
    private void adjustNeighboringSliders(JSlider[] sliders,
            int centralID, double centralDiff,
            double[] values,
            int firstSliderID, int lastSliderID,
            double minVal, double maxVal) {

        // don't return if centralDiff equals 0!

        // read the number of sliders that should move with the displaced slider
        final int reach = (Integer) (this.linkSpinner.getValue());
        if (reach == 0) {
            return;
        }

        for (int i = 1; i <= sliders.length; i++) {
            double diff = interpolAdjustment(reach, i, centralDiff);
            if (centralID - i >= firstSliderID && centralID - i <= lastSliderID) {
                double oldVal = values[centralID - i];
                double newVal = oldVal + diff;
                newVal = Math.max(minVal, Math.min(maxVal, newVal));
                sliders[centralID - i].setValue((int) (newVal * SLIDER_SCALE));
            }
            if (centralID + i >= firstSliderID && centralID + i <= lastSliderID) {
                double oldVal = values[centralID + i];
                double newVal = oldVal + diff;
                newVal = Math.max(minVal, Math.min(maxVal, newVal));
                sliders[centralID + i].setValue((int) (newVal * SLIDER_SCALE));
            }
        }
    }

    /**
     * @param reach The number of sliders that should change around the central
     * slider. This corresponds to the range of the interpolation function.
     * @param x The id of the slider that has to be changed. This corresponds to
     * the x value for which a value has to be interpolated.
     * @param ymax The value of the central slider changed by this amount. This
     * corresponds to the maximum value.
     */
    private double interpolAdjustment(int reach, double x, double ymax) {
        switch (this.getCurveInterpolation()) {
            case LINEARCURVE:
                return interpolLinearGradient(reach, x, ymax);
            case BELLCURVE:
                return interpolBellCurve(reach, x, ymax);
            case PEAKCURVE:
                return interpolPeakCurve(reach, x, ymax);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * @param reach If reach is for example 5, 2 sliders to the left and 2
     * sliders to the right must be moved.
     * @param x The distance from the center.
     * @param ymax The value at the center.
     */
    private double interpolLinearGradient(int reach, double x, double ymax) {
        final int m = reach + 1;
        if (x > m) {
            return 0;
        }
        return ymax - ymax / m * x;
    }

    private double interpolPeakCurve(int reach, double x, double ymax) {
        reach++;
        x = (x - reach) / reach;
        return (x > 0.) ? 0. : ymax * x * x;
    }

    private double interpolBellCurve(int reach, double x, double ymax) {
        if (x > reach) {
            return 0;
        }

        // y for reach
        final double reachY = 0.01;
        // scale factor s, such that y = exp(-x*s * x*s) = reachY for x = reach
        final double s = Math.sqrt(-Math.log(reachY) / (reach * reach));
        final double xs = x * s;
        final double w = Math.exp(-xs * xs);
        return w < reachY ? 0 : ymax * w;
    }

    /**
     * A slider for adjusting the length of a parallel changed.
     *
     * @param evt
     */
    private void lengthSliderStateChanged(javax.swing.event.ChangeEvent evt) {

        if (updatingGUI) {
            return;
        }

        FlexProjectionModel flexModel = model.getFlexProjectionModel();
        JSlider slider = (JSlider) evt.getSource();

        try {
            updatingGUI = true;

            for (int i = 0; i < xSliders.length; i++) {
                if (xSliders[i] == slider) {
                    final double val = slider.getValue() / SLIDER_SCALE;
                    final double dx = val - flexModel.getX(i);
                    final double xarr[] = (double[]) flexModel.getX().clone();
                    adjustNeighboringSliders(xSliders, i, dx, xarr, 0, xSliders.length - 1, 0d, 1d);

                    // update all number fields
                    for (int j = 0; j < xNumbers.length; j++) {
                        double v = xSliders[j].getValue() / SLIDER_SCALE;
                        xNumbers[j].setValue(v);
                    }
                    break;
                }
            }

        } finally {
            updatingGUI = false;
        }

        // when the user releases the mouse, the values are copied from the
        // GUI to the flex model.
        if (!slider.getValueIsAdjusting()) {
            for (int j = 0; j < xNumbers.length; j++) {
                flexModel.setX(j, ((Number) xNumbers[j].getValue()).doubleValue());
            }

            updateDistortionIndicesAndInformListeners();
            mapComponent.addUndo("Change of Parallels Length");
            showDesignProjection();
        }

    }

    /**
     * A slider for adjusting the distance between the equator and a parallel
     * changed.
     *
     * @param evt
     */
    private void distanceSliderStateChanged(javax.swing.event.ChangeEvent evt) {

        if (updatingGUI) {
            return;
        }

        FlexProjectionModel flexModel = model.getFlexProjectionModel();
        JSlider slider = (JSlider) evt.getSource();

        try {
            updatingGUI = true;

            for (int i = 1; i < ySliders.length; i++) {
                if (ySliders[i] == slider) {
                    final double val = slider.getValue() / SLIDER_SCALE;
                    final double dy = val - flexModel.getY(i);
                    final double yarr[] = (double[]) flexModel.getY().clone();

                    adjustNeighboringSliders(ySliders, i, dy, yarr, 1, ySliders.length - 1, 0d, 1d);

                    // make sure the values in the y array are increasing.
                    if (dy > 0) {
                        for (int j = 1; j < ySliders.length - 1; j++) {
                            final int v = ySliders[j].getValue();
                            if (v > ySliders[j + 1].getValue()) {
                                ySliders[j + 1].setValue(v);
                            }
                        }
                    } else {
                        for (int j = ySliders.length - 1; j > 1; j--) {
                            final int v = ySliders[j].getValue();
                            if (v < ySliders[j - 1].getValue()) {
                                ySliders[j - 1].setValue(v);
                            }
                        }
                    }

                    // update all number fields
                    for (int j = 1; j < yNumbers.length; j++) {
                        double v = ySliders[j].getValue() / SLIDER_SCALE;
                        yNumbers[j].setValue(v);
                    }

                    break;
                }
            }
        } finally {
            updatingGUI = false;
        }


        if (!slider.getValueIsAdjusting()) {

            // when the user releases the mouse, the values are copied from the
            // GUI to the flexModel.
            for (int j = 1; j < yNumbers.length; j++) {
                flexModel.setY(j, ((Number) yNumbers[j].getValue()).doubleValue());

            }

            // a hack to keep the aspect ratio constant
            if (KEEP_ASPECT_CONSTANT_HACK) {
                double scale = flexModel.getScaleY();
                flexModel.normalize();
                flexModel.setScaleY(scale);

                // execute this later to avoid strange problems with sliders snapping back
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        writeFlexSliderGUI();
                    }
                });

            }

            updateDistortionIndicesAndInformListeners();
            mapComponent.addUndo("Change of Parallels Distance");
            showDesignProjection();
        }
    }

    /**
     * A slider for adjusting the bending changed.
     *
     * @param evt
     */
    private void bendingSliderStateChanged(javax.swing.event.ChangeEvent evt) {

        if (updatingGUI) {
            return;
        }

        FlexProjectionModel flexModel = model.getFlexProjectionModel();
        JSlider slider = (JSlider) evt.getSource();

        try {
            updatingGUI = true;

            for (int i = 0; i < bSliders.length; i++) {
                if (bSliders[i] == slider) {
                    final double val = slider.getValue() / SLIDER_SCALE;
                    final double db = val - flexModel.getB(i);
                    final double barr[] = (double[]) flexModel.getB().clone();

                    adjustNeighboringSliders(bSliders, i, db, barr, 0, bSliders.length - 1,
                            FlexProjectionModel.MIN_BENDING, FlexProjectionModel.MAX_BENDING);

                    // update all number fields
                    for (int j = 0; j < bNumbers.length; j++) {
                        double b = bSliders[j].getValue() / SLIDER_SCALE;
                        bNumbers[j].setValue(b);
                    }
                    break;
                }
            }
        } finally {
            updatingGUI = false;
        }

        if (!slider.getValueIsAdjusting()) {
            // when the user releases the mouse, the values are copied from the
            // GUI to the flexModel.
            for (int j = 0; j < bNumbers.length; j++) {
                flexModel.setBending(j, ((Number) bNumbers[j].getValue()).doubleValue());
            }
            updateDistortionIndicesAndInformListeners();
            mapComponent.addUndo("Change of Bending");
            showDesignProjection();
        }
    }

    /**
     * A slider for adjusting the distance between meridians changed.
     *
     * @param evt
     */
    private void distributionSliderStateChanged(javax.swing.event.ChangeEvent evt) {
        if (updatingGUI) {
            return;
        }

        FlexProjectionModel flexModel = model.getFlexProjectionModel();
        JSlider slider = (JSlider) evt.getSource();

        try {
            updatingGUI = true;

            for (int i = 0; i < xDistSliders.length - 1; i++) {
                if (xDistSliders[i] == slider) {
                    final double val = slider.getValue() / SLIDER_SCALE;
                    final double dx = val - flexModel.getXDist(i);
                    final double xdarr[] = (double[]) flexModel.getXDist().clone();

                    adjustNeighboringSliders(xDistSliders, i, dx, xdarr, 1, xDistSliders.length - 2,
                            FlexProjectionModel.MIN_MERIDIANS_DIST,
                            FlexProjectionModel.MAX_MERIDIANS_DIST);

                    // update all number fields
                    for (int j = 1; j < xDistNumbers.length - 1; j++) {
                        double v = xDistSliders[j].getValue() / SLIDER_SCALE;
                        xDistNumbers[j].setValue(v);
                    }
                    break;
                }
            }
        } finally {
            updatingGUI = false;
        }

        if (!slider.getValueIsAdjusting()) {
            // when the user releases the mouse, the values are copied from the
            // GUI to the flexModel.
            for (int j = 0; j < xDistNumbers.length - 1; j++) {
                flexModel.setXDist(j, ((Number) xDistNumbers[j].getValue()).doubleValue());
            }
            updateDistortionIndicesAndInformListeners();
            mapComponent.addUndo("Change of Meridians Distribution");
            showDesignProjection();
        }
    }

    /**
     * A number field for adjusting the Flex projection changed.
     *
     * @param evt
     */
    private void flexFieldPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (updatingGUI || !"value".equals(evt.getPropertyName())) {
            return;
        }

        try {
            updatingGUI = true;

            FlexProjectionModel flexModel = model.getFlexProjectionModel();
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            final double value = ((Number) field.getValue()).doubleValue();

            // update the flexModel and the slider for the changed number field
            for (int i = 0; i < xNumbers.length; i++) {
                if (xNumbers[i] == field) {
                    flexModel.setX(i, value);
                    if (xSliders[i] != null) {
                        xSliders[i].setValue((int) (value * SLIDER_SCALE));
                    }
                    if (mapComponent != null) {
                        mapComponent.addUndo("Change of Parallel Length at " + (i * 5) + '\u00B0');
                    }
                    break;
                }
            }

            for (int i = 1; i < yNumbers.length; i++) {
                if (yNumbers[i] == field) {
                    flexModel.setY(i, value);
                    if (ySliders[i] != null) {
                        ySliders[i].setValue((int) (value * SLIDER_SCALE));
                    }
                    mapComponent.addUndo("Change of Parallel Distance at " + (i * 5) + '\u00B0');
                    break;
                }
            }

            for (int i = 0; i < bNumbers.length; i++) {
                if (bNumbers[i] == field) {
                    flexModel.setBending(i, value);
                    if (bSliders[i] != null) {
                        bSliders[i].setValue((int) (value * SLIDER_SCALE));
                    }
                    mapComponent.addUndo("Change of Bending at " + (i * 5) + '\u00B0');
                    break;
                }
            }

            for (int i = 1; i < xDistNumbers.length; i++) {
                if (xDistNumbers[i] == field) {
                    flexModel.setXDist(i, value);
                    if (xDistSliders[i] != null) {
                        xDistSliders[i].setValue((int) (value * SLIDER_SCALE));
                    }
                    mapComponent.addUndo("Change of Meridian Position at " + (i * 15) + '\u00B0');
                    break;
                }
            }

        } finally {
            updatingGUI = false;
        }
        updateDistortionIndicesAndInformListeners();
        showDesignProjection();
    }

    public FlexProjectorModel getModel() {
        return model;
    }

    public void setModel(FlexProjectorModel model) {
        if (model != this.model) {
            designProjectionChangeListeners.removeListener(this.model);
            this.model = model;
            writeMethodGUI();
            updateDistortionIndicesAndInformListeners();
            designProjectionChangeListeners.addListener(model);
        }
    }

    /**
     * This method is called from within the constructor to initializethe form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        adjustButtonGroup = new javax.swing.ButtonGroup();
        meridiansDirectionPanel = new javax.swing.JPanel();
        poleDirectionSlider = new javax.swing.JSlider();
        poleDirectionCheckBox = new javax.swing.JCheckBox();
        meridianCurvatureLabel = new javax.swing.JLabel();
        meridianSmoothRadioButton = new javax.swing.JRadioButton();
        meridianAngularRadioButton = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        poleDirectionFormattedTextField = new javax.swing.JFormattedTextField();
        curveShapeButtonGroup = new javax.swing.ButtonGroup();
        meridianCurvatureButtonGroup = new javax.swing.ButtonGroup();
        scaleButtonGroup = new javax.swing.ButtonGroup();
        optionsPopupMenu = new javax.swing.JPopupMenu();
        resetMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new JPopupMenu.Separator();
        polesAndEquatorMenuItem = new javax.swing.JMenuItem();
        eliminateShapeDistortionAtOriginMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new JPopupMenu.Separator();
        bendingMenu = new javax.swing.JMenu();
        cubicBendingRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        quadraticBendingRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        cosineBendingRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        makeCylindricalMenuItem = new javax.swing.JMenuItem();
        removeDistanceMenuItem = new javax.swing.JMenuItem();
        removeBendingMenuItem = new javax.swing.JMenuItem();
        removeMeridiansMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new JPopupMenu.Separator();new JPopupMenu.Separator();
        normalizeMenuItem = new javax.swing.JMenuItem();
        methodButtonGroup = new javax.swing.ButtonGroup();
        sizePanel = new javax.swing.JPanel();
        scaleManualSlider = new javax.swing.JSlider();
        jLabel7 = new javax.swing.JLabel();
        currentSizeLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        scaleMinimumAreaDistRadioButton = new javax.swing.JRadioButton();
        scaleAreaOfGlobeRadioButton = new javax.swing.JRadioButton();
        scaleManualRadioButton = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        scalePointRadioButton = new javax.swing.JRadioButton();
        scaleLonSlider = new javax.swing.JSlider();
        scaleLatSlider = new javax.swing.JSlider();
        javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        scaleLatField = new javax.swing.JFormattedTextField();
        scaleLonField = new javax.swing.JFormattedTextField();
        scaleManualField = new javax.swing.JFormattedTextField();
        scaleDialog = new javax.swing.JDialog(GUIUtil.getOwnerFrame(this), true);
        javax.swing.JPanel scaleDialogControlPanel = new javax.swing.JPanel();
        javax.swing.JButton scaleOKButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        bendingButtonGroup = new javax.swing.ButtonGroup();
        mainTabs = new javax.swing.JTabbedPane();
        flexTab = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        flexTab.setOpaque(false);
        javax.swing.JPanel topPanel = new TransparentMacPanel();
        javax.swing.JPanel methodButtonPanel = new TransparentMacPanel();
        flexSlidersToggleButton = new javax.swing.JToggleButton();
        simpleMixerToggleButton = new javax.swing.JToggleButton();
        latitudeMixerToggleButton = new javax.swing.JToggleButton();
        flexMixerToggleButton = new javax.swing.JToggleButton();
        verticalScaleSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        verticalScaleFormattedTextField = new javax.swing.JFormattedTextField();
        sizeButton = new javax.swing.JButton();
        methodPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())     methodPanel.setOpaque(false);
        flexSlidersPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())     flexSlidersPanel.setOpaque(false);
        javax.swing.JPanel jPanel1 = new TransparentMacPanel();
        flexOptionsButton = new ika.gui.MenuToggleButton();
        flexSlidersTabs = new javax.swing.JTabbedPane();
        lengthPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        lengthPanel.setOpaque(false);
        lengthInfoPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        lengthInfoPanel.setOpaque(false);
        javax.swing.JLabel lengthLabel = new javax.swing.JLabel();
        lengthSlidersPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        lengthSlidersPanel.setOpaque(false);
        distancePanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        distancePanel.setOpaque(false);
        distanceInfoPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        distanceInfoPanel.setOpaque(false);
        distanceLabel = new javax.swing.JLabel();
        distanceSlidersPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        distanceSlidersPanel.setOpaque(false);
        bendingPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        bendingPanel.setOpaque(false);
        bendingInfoPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        bendingInfoPanel.setOpaque(false);
        bendingLabel = new javax.swing.JLabel();
        bendingSlidersPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        bendingSlidersPanel.setOpaque(false);
        bendingOptionsPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        bendingOptionsPanel.setOpaque(false);
        bendingShapeLabel = new javax.swing.JLabel();
        bendingComboBox = new javax.swing.JComboBox();
        meridiansPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        meridiansPanel.setOpaque(false);
        meridiansInfoPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        meridiansInfoPanel.setOpaque(false);
        meridiansLabel = new javax.swing.JLabel();
        meridiansSlidersPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        meridiansSlidersPanel.setOpaque(false);
        southPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        southPanel.setOpaque(false);
        linkSlidersLabel = new javax.swing.JLabel();
        linkSpinner = new javax.swing.JSpinner();
        curveShapeLabel = new javax.swing.JLabel();
        curveShapeToolBar = new javax.swing.JToolBar();
        peakCurveToggleButton = new javax.swing.JToggleButton();
        linearCurveToggleButton = new javax.swing.JToggleButton();
        roundCurveToggleButton = new javax.swing.JToggleButton();
        mixerPanel = new TransparentMacPanel();
        mixerMapsPanel = new TransparentMacPanel();
        mixerProjection1Panel = new TransparentMacPanel();
        mixerMap1 = new ika.gui.MapComponent();
        jPanel3 = new TransparentMacPanel();
        mixerComboBox1 = new javax.swing.JComboBox();
        mixerProjection2Panel = new TransparentMacPanel();
        mixerMap2 = new ika.gui.MapComponent();
        jPanel5 = new TransparentMacPanel();
        mixerComboBox2 = new javax.swing.JComboBox();
        mixPanel = new TransparentMacPanel();
        mixerControlsPanel = new TransparentMacPanel();
        javax.swing.JPanel simpleMixerPanel = new TransparentMacPanel();
        javax.swing.JLabel jLabel21 = new javax.swing.JLabel();
        meanSlider = new javax.swing.JSlider();
        meanLabel1 = new javax.swing.JLabel();
        meanLabel2 = new javax.swing.JLabel();
        javax.swing.JPanel meanMixerProjectionNamesPanel = new TransparentMacPanel();
        meanProjection1Label = new javax.swing.JLabel();
        meanProjection2Label = new javax.swing.JLabel();
        javax.swing.JPanel flexMixerPanel = new TransparentMacPanel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        mixerLengthSlider = new javax.swing.JSlider();
        mixerDistanceSlider = new javax.swing.JSlider();
        mixerBendingSlider = new javax.swing.JSlider();
        mixerMeridiansSlider = new javax.swing.JSlider();
        javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
        bendingParallelsLabel = new javax.swing.JLabel();
        meridiansDistributionLabel = new javax.swing.JLabel();
        distanceLabelLeft = new javax.swing.JLabel();
        bendingLabelLeft = new javax.swing.JLabel();
        meridiansLabelLeft = new javax.swing.JLabel();
        lengthLabelLeft = new javax.swing.JLabel();
        lengthLabelRight = new javax.swing.JLabel();
        distanceLabelRight = new javax.swing.JLabel();
        bendingLabelRight = new javax.swing.JLabel();
        meridiansLabelRight = new javax.swing.JLabel();
        flexProjectionsNamesPanel = new TransparentMacPanel();
        flexMixProjection1Label = new javax.swing.JLabel();
        flexMixProjection2Label = new javax.swing.JLabel();
        javax.swing.JPanel latitudeMixerPanel = new TransparentMacPanel();
        blendingLatitudeSlider = new javax.swing.JSlider();
        javax.swing.JLabel jLabel18 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel19 = new javax.swing.JLabel();
        blendingToleranceSlider = new javax.swing.JSlider();
        blendingScaleCheckBox = new javax.swing.JCheckBox();
        latitudeLabel = new javax.swing.JLabel();
        toleranceLabel = new javax.swing.JLabel();
        blendingSizeSlider = new javax.swing.JSlider();
        sizeLabel = new javax.swing.JLabel();
        javax.swing.JPanel latitudeMixerProjectionNamesPanel = new TransparentMacPanel();
        latitudeProjection1Label = new javax.swing.JLabel();
        latitudeProjection2Label = new javax.swing.JLabel();
        javax.swing.JPanel displayTab = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        displayTab.setOpaque(false);
        javax.swing.JPanel displayPanel = new javax.swing.JPanel();
        tissotLabel = new javax.swing.JLabel();
        showFlexCheckBox = new javax.swing.JCheckBox();
        tissotComboBox = new javax.swing.JComboBox();
        showGraticuleCheckBox = new javax.swing.JCheckBox();
        showSecondProjectionCheckBox = new javax.swing.JCheckBox();
        graticuleLabel = new javax.swing.JLabel();
        graticuleComboBox = new javax.swing.JComboBox();
        showTissotCheckBox = new javax.swing.JCheckBox();
        projectionComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel longitudeLabel = new javax.swing.JLabel();
        lon0Slider = new javax.swing.JSlider();
        adjustNoRadioButton = new javax.swing.JRadioButton();
        adjustWidthRadioButton = new javax.swing.JRadioButton();
        adjustHeightRadioButton = new javax.swing.JRadioButton();
        adjustLabel = new javax.swing.JLabel();
        showAngularIsolinesCheckBox = new javax.swing.JCheckBox();
        angularIsolinesEquidistanceLabel = new javax.swing.JLabel();
        angularIsolinesEquidistanceComboBox = new javax.swing.JComboBox();
        showArealIsolinesCheckBox = new javax.swing.JCheckBox();
        arealIsolinesEquidistanceLabel = new javax.swing.JLabel();
        arealIsolinesEquidistanceComboBox = new javax.swing.JComboBox();
        showAcceptableAreaCheckBox = new javax.swing.JCheckBox();
        acceptableAreaOptionsButton = new javax.swing.JButton();
        centralMeridianField = new javax.swing.JFormattedTextField();
        jPanel2 = new TransparentMacPanel();
        tissotScaleLabel = new javax.swing.JLabel();
        tissotScaleSlider = new javax.swing.JSlider();

        meridiansDirectionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 20, 1, 20));
        meridiansDirectionPanel.setLayout(new java.awt.GridBagLayout());

        poleDirectionSlider.setMajorTickSpacing(15);
        poleDirectionSlider.setMaximum(90);
        poleDirectionSlider.setMinimum(15);
        poleDirectionSlider.setMinorTickSpacing(5);
        poleDirectionSlider.setPaintLabels(true);
        poleDirectionSlider.setPaintTicks(true);
        poleDirectionSlider.setValue(20);
        poleDirectionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                poleDirectionSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        meridiansDirectionPanel.add(poleDirectionSlider, gridBagConstraints);

        poleDirectionCheckBox.setText("Adjust Direction of Meridians at Poles");
        poleDirectionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        poleDirectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                poleDirectionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        meridiansDirectionPanel.add(poleDirectionCheckBox, gridBagConstraints);

        meridianCurvatureLabel.setText("Meridian Curvature at Equator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 0, 5, 0);
        meridiansDirectionPanel.add(meridianCurvatureLabel, gridBagConstraints);

        meridianCurvatureButtonGroup.add(meridianSmoothRadioButton);
        meridianSmoothRadioButton.setSelected(true);
        meridianSmoothRadioButton.setText("Smooth");
        meridianSmoothRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        meridianSmoothRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meridianEquatorRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        meridiansDirectionPanel.add(meridianSmoothRadioButton, gridBagConstraints);

        meridianCurvatureButtonGroup.add(meridianAngularRadioButton);
        meridianAngularRadioButton.setText("Angular");
        meridianAngularRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        meridianAngularRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meridianEquatorRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        meridiansDirectionPanel.add(meridianAngularRadioButton, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        jLabel1.setText("<html>Adjusting the direction is mainly recommended <br>when the meridians should start with a flat angle.<br><b>Note:</b> When extreme curvature is added at poles, <br>graticule lines may not draw correctly.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        meridiansDirectionPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        jLabel2.setText("<html>Angular is only recommended when meridians <br>should be broken at the equator.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        meridiansDirectionPanel.add(jLabel2, gridBagConstraints);

        poleDirectionFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        poleDirectionFormattedTextField.setText("0");
        poleDirectionFormattedTextField.setPreferredSize(new java.awt.Dimension(60, 28));
        poleDirectionFormattedTextField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                poleDirectionFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        meridiansDirectionPanel.add(poleDirectionFormattedTextField, gridBagConstraints);

        optionsPopupMenu.setLightWeightPopupEnabled(false);

        resetMenuItem.setText("Reset to Projection…");
        resetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(resetMenuItem);
        optionsPopupMenu.add(jSeparator1);

        polesAndEquatorMenuItem.setText("Poles and Equator…");
        polesAndEquatorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                polesAndEquatorMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(polesAndEquatorMenuItem);

        eliminateShapeDistortionAtOriginMenuItem.setText("Eliminate Shape Distortion at Origin…");
        eliminateShapeDistortionAtOriginMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminateShapeDistortionAtOriginMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(eliminateShapeDistortionAtOriginMenuItem);
        optionsPopupMenu.add(jSeparator2);

        bendingMenu.setText("Parallels Bending");

        bendingButtonGroup.add(cubicBendingRadioButtonMenuItem);
        cubicBendingRadioButtonMenuItem.setSelected(true);
        cubicBendingRadioButtonMenuItem.setText("Cubic");
        cubicBendingRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bendingSelectionChanged(evt);
            }
        });
        bendingMenu.add(cubicBendingRadioButtonMenuItem);

        bendingButtonGroup.add(quadraticBendingRadioButtonMenuItem);
        quadraticBendingRadioButtonMenuItem.setText("Quadratic");
        quadraticBendingRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bendingSelectionChanged(evt);
            }
        });
        bendingMenu.add(quadraticBendingRadioButtonMenuItem);

        bendingButtonGroup.add(cosineBendingRadioButtonMenuItem);
        cosineBendingRadioButtonMenuItem.setText("Cosine");
        cosineBendingRadioButtonMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bendingSelectionChanged(evt);
            }
        });
        bendingMenu.add(cosineBendingRadioButtonMenuItem);

        optionsPopupMenu.add(bendingMenu);
        optionsPopupMenu.add(jSeparator4);

        makeCylindricalMenuItem.setText("Convert to Cylindrical Projection");
        makeCylindricalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeCylindricalMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(makeCylindricalMenuItem);

        removeDistanceMenuItem.setText("Reset to Regular Parallels Distribution");
        removeDistanceMenuItem.setToolTipText("Removes adjustments to the vertical distribution of parallels.");
        removeDistanceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDistanceMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(removeDistanceMenuItem);

        removeBendingMenuItem.setText("Remove Bending Adjustments");
        removeBendingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBendingMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(removeBendingMenuItem);

        removeMeridiansMenuItem.setText("Remove Meridians Adjustments");
        removeMeridiansMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMeridiansMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(removeMeridiansMenuItem);
        optionsPopupMenu.add(jSeparator3);

        normalizeMenuItem.setText("Scale Length and Distance to Full Range");
        normalizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                normalizeMenuItemActionPerformed(evt);
            }
        });
        optionsPopupMenu.add(normalizeMenuItem);

        sizePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sizePanel.setLayout(new java.awt.GridBagLayout());

        scaleManualSlider.setMajorTickSpacing(500);
        scaleManualSlider.setMaximum(1500);
        scaleManualSlider.setMinimum(500);
        scaleManualSlider.setMinorTickSpacing(100);
        scaleManualSlider.setPaintLabels(true);
        scaleManualSlider.setPaintTicks(true);
        scaleManualSlider.setValue(1000);
        scaleManualSlider.setMinimumSize(new java.awt.Dimension(200, 29));
        scaleManualSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                scaleManualSlidersliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 60, 0, 0);
        sizePanel.add(scaleManualSlider, gridBagConstraints);
        {
            Hashtable labelTable = new Hashtable();
            javax.swing.JLabel label;
            label = new JLabel("0.5");
            label.setEnabled(scaleManualSlider.isEnabled());
            labelTable.put( new Integer( 500 ), label );

            label = new JLabel("1.0");
            label.setEnabled(scaleManualSlider.isEnabled());
            labelTable.put( new Integer( 1000 ), label );

            label = new JLabel("1.5");
            label.setEnabled(scaleManualSlider.isEnabled());
            labelTable.put( new Integer( 1500 ), label );
            scaleManualSlider.setLabelTable(labelTable);
        }

        jLabel7.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel7.setText("Current area of map relative to area of sphere:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        sizePanel.add(jLabel7, gridBagConstraints);

        currentSizeLabel.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        currentSizeLabel.setText("-");
        currentSizeLabel.setMinimumSize(new java.awt.Dimension(35, 16));
        currentSizeLabel.setPreferredSize(new java.awt.Dimension(35, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        sizePanel.add(currentSizeLabel, gridBagConstraints);

        jLabel9.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel9.setText("<html>Automatically computes a scale factor that minimizes the total area distortion of the projection. This is the method recommended for most projections.</html>");
        jLabel9.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel9.setPreferredSize(new java.awt.Dimension(400, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        sizePanel.add(jLabel9, gridBagConstraints);

        jLabel10.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel10.setText("<html>Automatically computes a scale factor that results in a graticule with an area that equals the area of the Earth with a radius of 6,371,008 m. This is the method used for the Robinson projection.</html>");
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel10.setPreferredSize(new java.awt.Dimension(400, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        sizePanel.add(jLabel10, gridBagConstraints);

        jLabel11.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel11.setText("<html>Manually enter the internal scale factor.</html> ");
        jLabel11.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel11.setMinimumSize(new java.awt.Dimension(333, 28));
        jLabel11.setPreferredSize(new java.awt.Dimension(333, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        sizePanel.add(jLabel11, gridBagConstraints);

        scaleButtonGroup.add(scaleMinimumAreaDistRadioButton);
        scaleMinimumAreaDistRadioButton.setSelected(true);
        scaleMinimumAreaDistRadioButton.setText("Minimize Area Distortion (Recommended)");
        scaleMinimumAreaDistRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        sizePanel.add(scaleMinimumAreaDistRadioButton, gridBagConstraints);

        scaleButtonGroup.add(scaleAreaOfGlobeRadioButton);
        scaleAreaOfGlobeRadioButton.setText("Adjust to Area of Earth Globe");
        scaleAreaOfGlobeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        sizePanel.add(scaleAreaOfGlobeRadioButton, gridBagConstraints);

        scaleButtonGroup.add(scaleManualRadioButton);
        scaleManualRadioButton.setText("Manual Scale");
        scaleManualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        sizePanel.add(scaleManualRadioButton, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel12.setText("<html>The Flex Projection is scaled by an internal factor, which will determine the size of the graticule. Use the options here to select this scale factor.</html>");
        jLabel12.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel12.setMinimumSize(new java.awt.Dimension(400, 28));
        jLabel12.setPreferredSize(new java.awt.Dimension(400, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add(jLabel12, gridBagConstraints);

        jLabel13.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel13.setText("<html>Automatically computes a scale factor that shows a specific point without area distortion. Select the point below. The coordinates of the point are<br>relative to the current projection center.</html> ");
        jLabel13.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel13.setPreferredSize(new java.awt.Dimension(400, 45));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 0, 0);
        sizePanel.add(jLabel13, gridBagConstraints);

        scaleButtonGroup.add(scalePointRadioButton);
        scalePointRadioButton.setText("Remove Area Distortion at Point");
        scalePointRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(25, 0, 0, 0);
        sizePanel.add(scalePointRadioButton, gridBagConstraints);

        scaleLonSlider.setMajorTickSpacing(90);
        scaleLonSlider.setMaximum(180);
        scaleLonSlider.setMinimum(-180);
        scaleLonSlider.setMinorTickSpacing(15);
        scaleLonSlider.setPaintLabels(true);
        scaleLonSlider.setPaintTicks(true);
        scaleLonSlider.setValue(0);
        scaleLonSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                scalePointSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 60, 0, 0);
        sizePanel.add(scaleLonSlider, gridBagConstraints);
        {
            JSlider slider = scaleLonSlider;
            java.util.Hashtable labels = slider.createStandardLabels(slider.getMajorTickSpacing());
            java.util.Enumeration e = labels.elements();
            while(e.hasMoreElements()) {
                javax.swing.JComponent comp = (javax.swing.JComponent)e.nextElement();
                if (comp instanceof javax.swing.JLabel) {
                    javax.swing.JLabel label = (javax.swing.JLabel)(comp);
                    label.setEnabled(slider.isEnabled());
                    label.setText(label.getText() + "\u00b0");
                }
            }
            slider.setLabelTable(labels);
        }

        scaleLatSlider.setMajorTickSpacing(45);
        scaleLatSlider.setMaximum(90);
        scaleLatSlider.setMinimum(-90);
        scaleLatSlider.setMinorTickSpacing(15);
        scaleLatSlider.setPaintLabels(true);
        scaleLatSlider.setPaintTicks(true);
        scaleLatSlider.setValue(0);
        scaleLatSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                scalePointSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 60, 0, 0);
        sizePanel.add(scaleLatSlider, gridBagConstraints);
        {
            JSlider slider = scaleLatSlider;
            java.util.Hashtable labels = slider.createStandardLabels(slider.getMajorTickSpacing());
            java.util.Enumeration e = labels.elements();
            while(e.hasMoreElements()) {
                javax.swing.JComponent comp = (javax.swing.JComponent)e.nextElement();
                if (comp instanceof javax.swing.JLabel) {
                    javax.swing.JLabel label = (javax.swing.JLabel)(comp);
                    label.setEnabled(slider.isEnabled());
                    label.setText(label.getText() + "\u00b0");
                }
            }
            slider.setLabelTable(labels);
        }

        jLabel6.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel6.setText("Longitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 60, 0, 0);
        sizePanel.add(jLabel6, gridBagConstraints);

        jLabel8.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        jLabel8.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 60, 0, 0);
        sizePanel.add(jLabel8, gridBagConstraints);

        scaleLatField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        scaleLatField.setPreferredSize(new java.awt.Dimension(75, 28));
        scaleLatField.setValue(0.);
        {
            javax.swing.text.NumberFormatter nf = new javax.swing.text.NumberFormatter();
            nf.setMaximum(90.);
            nf.setMinimum(-90.);
            scaleLatField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(nf));
        }
        scaleLatField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                scaleFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add(scaleLatField, gridBagConstraints);

        scaleLonField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        scaleLonField.setPreferredSize(new java.awt.Dimension(75, 28));
        scaleLonField.setValue(0.);
        {
            javax.swing.text.NumberFormatter nf = new javax.swing.text.NumberFormatter();
            nf.setMaximum(180.);
            nf.setMinimum(-180.);
            scaleLonField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(nf));
        }
        scaleLonField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                scaleFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add(scaleLonField, gridBagConstraints);

        scaleManualField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.####"))));
        scaleManualField.setPreferredSize(new java.awt.Dimension(75, 28));
        scaleManualField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                scaleFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        sizePanel.add(scaleManualField, gridBagConstraints);

        scaleDialog.setTitle("Projection Size");
        scaleDialog.setModal(true);
        scaleDialog.setResizable(false);

        scaleDialogControlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 20, 20));
        scaleDialogControlPanel.setLayout(new java.awt.BorderLayout());

        scaleOKButton.setText("OK");
        scaleOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleOKButtonActionPerformed(evt);
            }
        });
        scaleDialogControlPanel.add(scaleOKButton, java.awt.BorderLayout.EAST);

        helpButton.setText("Help");
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5()) {
            helpButton.putClientProperty("JButton.buttonType", "help");
            helpButton.setText("");
        }
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });
        scaleDialogControlPanel.add(helpButton, java.awt.BorderLayout.WEST);

        scaleDialog.getContentPane().add(scaleDialogControlPanel, java.awt.BorderLayout.SOUTH);

        scaleDialog.getContentPane().add(sizePanel, java.awt.BorderLayout.CENTER);

        scaleDialog.getRootPane().setDefaultButton(scaleOKButton);
        scaleDialog.pack();
        scaleDialog.setLocationByPlatform(true);

        setMinimumSize(new java.awt.Dimension(390, 400));
        setPreferredSize(new java.awt.Dimension(390, 400));
        setLayout(new java.awt.BorderLayout());

        mainTabs.setMinimumSize(new java.awt.Dimension(411, 400));
        mainTabs.setOpaque(true);
        mainTabs.setPreferredSize(new java.awt.Dimension(431, 400));

        flexTab.setLayout(new java.awt.BorderLayout());

        topPanel.setLayout(new java.awt.GridBagLayout());

        methodButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 15, 0));
        methodButtonPanel.setLayout(new javax.swing.BoxLayout(methodButtonPanel, javax.swing.BoxLayout.LINE_AXIS));

        methodButtonGroup.add(flexSlidersToggleButton);
        flexSlidersToggleButton.setSelected(true);
        flexSlidersToggleButton.setText("<html><center>Flex<br>Sliders</center></html>");
        flexSlidersToggleButton.setFocusable(false);
        flexSlidersToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        flexSlidersToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flexSlidersToggleButtonActionPerformed(evt);
            }
        });
        methodButtonPanel.add(flexSlidersToggleButton);

        methodButtonGroup.add(simpleMixerToggleButton);
        simpleMixerToggleButton.setText("<html><center>Simple<br>Mixer</center></html>");
        simpleMixerToggleButton.setFocusable(false);
        simpleMixerToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        simpleMixerToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpleMixerToggleButtonActionPerformed(evt);
            }
        });
        methodButtonPanel.add(simpleMixerToggleButton);

        methodButtonGroup.add(latitudeMixerToggleButton);
        latitudeMixerToggleButton.setText("<html><center>Latitude<br>Mixer</center></html>");
        latitudeMixerToggleButton.setFocusable(false);
        latitudeMixerToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        latitudeMixerToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                latitudeMixerToggleButtonActionPerformed(evt);
            }
        });
        methodButtonPanel.add(latitudeMixerToggleButton);

        methodButtonGroup.add(flexMixerToggleButton);
        flexMixerToggleButton.setText("<html><center>Flex<br>Mixer</center></html>");
        flexMixerToggleButton.setFocusable(false);
        flexMixerToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        flexMixerToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flexMixerToggleButtonActionPerformed(evt);
            }
        });
        methodButtonPanel.add(flexMixerToggleButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        topPanel.add(methodButtonPanel, gridBagConstraints);

        {
            SliderUtils.setSliderLabels(verticalScaleSlider,
                new int[] {250, 500, 750, 1000},
                new String[] {"\u00BC", "\u00BD", "\u00BE", "1"});
            SliderUtils.reapplyFontSize(verticalScaleSlider);
        }
        verticalScaleSlider.setMajorTickSpacing(250);
        verticalScaleSlider.setMaximum(1000);
        verticalScaleSlider.setMinimum(250);
        verticalScaleSlider.setPaintLabels(true);
        verticalScaleSlider.setPaintTicks(true);
        verticalScaleSlider.setValue(1000);
        verticalScaleSlider.setPreferredSize(GUIUtil.getPreferredSize(verticalScaleSlider, 150));
        verticalScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                verticalScaleSlidersliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        topPanel.add(verticalScaleSlider, gridBagConstraints);

        jLabel3.setText("Height");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        topPanel.add(jLabel3, gridBagConstraints);

        verticalScaleFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,###.####"))));
        verticalScaleFormattedTextField.setPreferredSize(GUIUtil.getPreferredSize(verticalScaleFormattedTextField, 70));
        verticalScaleFormattedTextField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                verticalScaleFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 6);
        topPanel.add(verticalScaleFormattedTextField, gridBagConstraints);

        sizeButton.setText("Size");
        sizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        topPanel.add(sizeButton, gridBagConstraints);

        flexTab.add(topPanel, java.awt.BorderLayout.PAGE_START);

        methodPanel.setLayout(new java.awt.CardLayout());

        flexSlidersPanel.setLayout(new java.awt.GridLayout(1, 0));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        flexOptionsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/Action.png"))); // NOI18N
        flexOptionsButton.setText("Options");
        flexOptionsButton.setToolTipText("Options");
        flexOptionsButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        flexOptionsButton.setPreferredSize(new java.awt.Dimension(100, 30));
        flexOptionsButton.setPopupMenu(optionsPopupMenu);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 10);
        jPanel1.add(flexOptionsButton, gridBagConstraints);

        flexSlidersTabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        lengthPanel.setLayout(new java.awt.BorderLayout());

        lengthInfoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 2));

        lengthLabel.setText("Length of parallels");
        lengthInfoPanel.add(lengthLabel);

        lengthPanel.add(lengthInfoPanel, java.awt.BorderLayout.NORTH);
        if (Toolkit.getDefaultToolkit().getScreenSize().height <= 768) {
            lengthPanel.remove(lengthInfoPanel);
        }

        lengthSlidersPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        lengthSlidersPanel.setLayout(new java.awt.GridBagLayout());
        lengthPanel.add(lengthSlidersPanel, java.awt.BorderLayout.CENTER);

        flexSlidersTabs.addTab("Length", null, lengthPanel, "Length of parallels.");

        distancePanel.setLayout(new java.awt.BorderLayout());

        distanceInfoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        distanceLabel.setText("Distance of parallels from equator");
        distanceInfoPanel.add(distanceLabel);

        distancePanel.add(distanceInfoPanel, java.awt.BorderLayout.NORTH);
        if (Toolkit.getDefaultToolkit().getScreenSize().height <= 768) {
            distancePanel.remove(distanceInfoPanel);
        }

        distanceSlidersPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 1, 1, 1));
        distanceSlidersPanel.setLayout(new java.awt.GridBagLayout());
        distancePanel.add(distanceSlidersPanel, java.awt.BorderLayout.CENTER);

        flexSlidersTabs.addTab("Distance", null, distancePanel, "Distance of parallels from equator.");

        bendingPanel.setLayout(new java.awt.BorderLayout());

        bendingInfoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        bendingLabel.setText("Concave or convex bending of parallels");
        bendingInfoPanel.add(bendingLabel);

        bendingPanel.add(bendingInfoPanel, java.awt.BorderLayout.NORTH);
        if (Toolkit.getDefaultToolkit().getScreenSize().height <= 768) {
            bendingPanel.remove(bendingInfoPanel);
        }

        bendingSlidersPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 1, 1, 1));
        bendingSlidersPanel.setLayout(new java.awt.GridBagLayout());
        bendingPanel.add(bendingSlidersPanel, java.awt.BorderLayout.CENTER);

        bendingOptionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        bendingOptionsPanel.setLayout(new java.awt.GridBagLayout());

        bendingShapeLabel.setText("Curve:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        bendingOptionsPanel.add(bendingShapeLabel, gridBagConstraints);

        bendingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cubic", "Quadratic", "Cosine" }));
        bendingComboBox.setToolTipText("Select the shape of parallels.");
        bendingComboBox.setPreferredSize(new java.awt.Dimension(150, 27));
        bendingComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bendingComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        bendingOptionsPanel.add(bendingComboBox, gridBagConstraints);

        bendingPanel.add(bendingOptionsPanel, java.awt.BorderLayout.SOUTH);
        {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            if (dim.height <= 768) {
                bendingPanel.remove(bendingOptionsPanel);
            }
        }

        flexSlidersTabs.addTab("Bending", null, bendingPanel, "Concave or convex bending of parallels.");

        meridiansPanel.setLayout(new java.awt.BorderLayout());

        meridiansInfoPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        meridiansLabel.setText("Distribution of meridians");
        meridiansInfoPanel.add(meridiansLabel);

        meridiansPanel.add(meridiansInfoPanel, java.awt.BorderLayout.NORTH);

        if (Toolkit.getDefaultToolkit().getScreenSize().height <= 768) {
            meridiansPanel.remove(meridiansInfoPanel);
        }

        meridiansSlidersPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 1, 1, 1));
        meridiansSlidersPanel.setLayout(new java.awt.GridBagLayout());
        meridiansPanel.add(meridiansSlidersPanel, java.awt.BorderLayout.CENTER);

        flexSlidersTabs.addTab("Meridians", null, meridiansPanel, "Distribution of meridians.");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add(flexSlidersTabs, gridBagConstraints);

        southPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        southPanel.setLayout(new java.awt.GridBagLayout());

        linkSlidersLabel.setText("Linked Sliders:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        southPanel.add(linkSlidersLabel, gridBagConstraints);

        linkSpinner.setModel(new SpinnerNumberModel(3, 0, 17, 1));
        linkSpinner.setToolTipText("The number of sliders that are moved simultaneously. One half of the sliders are above and one half below the central slider.");
        linkSpinner.setValue(new Integer(7));
        {
            JComponent editor = linkSpinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setColumns(2);
            }
        }
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        southPanel.add(linkSpinner, gridBagConstraints);

        curveShapeLabel.setText("Move:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 0, 0);
        southPanel.add(curveShapeLabel, gridBagConstraints);

        curveShapeToolBar.setFloatable(false);
        curveShapeToolBar.setOpaque(false);

        curveShapeButtonGroup.add(peakCurveToggleButton);
        peakCurveToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/curve_peak.png"))); // NOI18N
        peakCurveToggleButton.setSelected(true);
        peakCurveToggleButton.setToolTipText("Move linked sliders along a peak-shaped curve.");
        curveShapeToolBar.add(peakCurveToggleButton);

        curveShapeButtonGroup.add(linearCurveToggleButton);
        linearCurveToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/curve_linear.png"))); // NOI18N
        linearCurveToggleButton.setToolTipText("Move linked sliders along a linear curve.");
        curveShapeToolBar.add(linearCurveToggleButton);

        curveShapeButtonGroup.add(roundCurveToggleButton);
        roundCurveToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/curve_round.png"))); // NOI18N
        roundCurveToggleButton.setToolTipText("Move linked sliders along a bell-shaped curve.");
        curveShapeToolBar.add(roundCurveToggleButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        southPanel.add(curveShapeToolBar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(southPanel, gridBagConstraints);

        flexSlidersPanel.add(jPanel1);

        methodPanel.add(flexSlidersPanel, "flexSlidersCard");

        mixerPanel.setLayout(new java.awt.BorderLayout());

        mixerMapsPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 2));

        mixerProjection1Panel.setLayout(new java.awt.BorderLayout());

        mixerMap1.setBackground(new java.awt.Color(255, 255, 255));
        mixerMap1.setInfoString("");
        mixerMap1.setPreferredSize(new java.awt.Dimension(370, 240));
        mixerMap1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mixerMap1ComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mixerMap1Layout = new org.jdesktop.layout.GroupLayout(mixerMap1);
        mixerMap1.setLayout(mixerMap1Layout);
        mixerMap1Layout.setHorizontalGroup(
            mixerMap1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 377, Short.MAX_VALUE)
        );
        mixerMap1Layout.setVerticalGroup(
            mixerMap1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 223, Short.MAX_VALUE)
        );

        mixerMap1.setZoomWithMouseWheel(false);
        mixerProjection1Panel.add(mixerMap1, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 2, 0));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        mixerComboBox1.setMaximumRowCount(25);
        mixerComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        mixerComboBox1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mixerComboBox1.setName("1"); // NOI18N
        mixerComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mixerComboBoxItemStateChanged(evt);
            }
        });
        jPanel3.add(mixerComboBox1);

        mixerProjection1Panel.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        mixerMapsPanel.add(mixerProjection1Panel);

        mixerProjection2Panel.setLayout(new java.awt.BorderLayout());

        mixerMap2.setBackground(new java.awt.Color(255, 255, 255));
        mixerMap2.setInfoString("");
        mixerMap2.setPreferredSize(new java.awt.Dimension(370, 240));
        mixerMap2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mixerMap2ComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mixerMap2Layout = new org.jdesktop.layout.GroupLayout(mixerMap2);
        mixerMap2.setLayout(mixerMap2Layout);
        mixerMap2Layout.setHorizontalGroup(
            mixerMap2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 377, Short.MAX_VALUE)
        );
        mixerMap2Layout.setVerticalGroup(
            mixerMap2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 223, Short.MAX_VALUE)
        );

        mixerMap2.setZoomWithMouseWheel(false);
        mixerProjection2Panel.add(mixerMap2, java.awt.BorderLayout.CENTER);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 2, 0));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        mixerComboBox2.setMaximumRowCount(25);
        mixerComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        mixerComboBox2.setName("2"); // NOI18N
        mixerComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mixerComboBoxItemStateChanged(evt);
            }
        });
        jPanel5.add(mixerComboBox2);

        mixerProjection2Panel.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        mixerMapsPanel.add(mixerProjection2Panel);

        mixerPanel.add(mixerMapsPanel, java.awt.BorderLayout.CENTER);

        mixPanel.setLayout(new java.awt.BorderLayout());

        mixerControlsPanel.setLayout(new java.awt.CardLayout());

        simpleMixerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        simpleMixerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel21.setText("Weight");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        simpleMixerPanel.add(jLabel21, gridBagConstraints);

        meanSlider.setMajorTickSpacing(25);
        meanSlider.setMinorTickSpacing(5);
        meanSlider.setPaintTicks(true);
        meanSlider.setPreferredSize(GUIUtil.getPreferredSize(meanSlider, 220));
        meanSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                meanSlidermixerSliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        simpleMixerPanel.add(meanSlider, gridBagConstraints);

        meanLabel1.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meanLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        meanLabel1.setText("50%");
        meanLabel1.setMaximumSize(new java.awt.Dimension(28, 16));
        meanLabel1.setMinimumSize(new java.awt.Dimension(28, 16));
        meanLabel1.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        simpleMixerPanel.add(meanLabel1, gridBagConstraints);

        meanLabel2.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meanLabel2.setText("50%");
        meanLabel2.setMaximumSize(new java.awt.Dimension(28, 16));
        meanLabel2.setMinimumSize(new java.awt.Dimension(28, 16));
        meanLabel2.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        simpleMixerPanel.add(meanLabel2, gridBagConstraints);

        meanMixerProjectionNamesPanel.setLayout(new java.awt.GridLayout(1, 0));

        meanProjection1Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meanProjection1Label.setText("Projection 1");
        meanProjection1Label.setPreferredSize(new java.awt.Dimension(150, 16));
        meanMixerProjectionNamesPanel.add(meanProjection1Label);

        meanProjection2Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meanProjection2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        meanProjection2Label.setText("Projection 2");
        meanProjection2Label.setPreferredSize(new java.awt.Dimension(120, 16));
        meanMixerProjectionNamesPanel.add(meanProjection2Label);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        simpleMixerPanel.add(meanMixerProjectionNamesPanel, gridBagConstraints);

        mixerControlsPanel.add(simpleMixerPanel, "simpleMixerCard");

        flexMixerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        flexMixerPanel.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("Parallels Length");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        flexMixerPanel.add(jLabel5, gridBagConstraints);

        mixerLengthSlider.setMinimumSize(new java.awt.Dimension(150, 29));
        mixerLengthSlider.setPreferredSize(new java.awt.Dimension(150, 29));
        mixerLengthSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                flexMixerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        flexMixerPanel.add(mixerLengthSlider, gridBagConstraints);

        mixerDistanceSlider.setMinimumSize(new java.awt.Dimension(150, 29));
        mixerDistanceSlider.setPreferredSize(new java.awt.Dimension(150, 29));
        mixerDistanceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                flexMixerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        flexMixerPanel.add(mixerDistanceSlider, gridBagConstraints);

        mixerBendingSlider.setMinimumSize(new java.awt.Dimension(150, 29));
        mixerBendingSlider.setPreferredSize(new java.awt.Dimension(150, 29));
        mixerBendingSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                flexMixerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        flexMixerPanel.add(mixerBendingSlider, gridBagConstraints);

        mixerMeridiansSlider.setMinimumSize(new java.awt.Dimension(150, 29));
        mixerMeridiansSlider.setPreferredSize(new java.awt.Dimension(150, 29));
        mixerMeridiansSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                flexMixerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        flexMixerPanel.add(mixerMeridiansSlider, gridBagConstraints);

        jLabel14.setText("Parallels Distance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        flexMixerPanel.add(jLabel14, gridBagConstraints);

        bendingParallelsLabel.setText("Parallels Bending");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        flexMixerPanel.add(bendingParallelsLabel, gridBagConstraints);

        meridiansDistributionLabel.setText("Meridians Distribution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        flexMixerPanel.add(meridiansDistributionLabel, gridBagConstraints);

        distanceLabelLeft.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        distanceLabelLeft.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        distanceLabelLeft.setText("50%");
        distanceLabelLeft.setMaximumSize(new java.awt.Dimension(28, 16));
        distanceLabelLeft.setMinimumSize(new java.awt.Dimension(28, 16));
        distanceLabelLeft.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        flexMixerPanel.add(distanceLabelLeft, gridBagConstraints);

        bendingLabelLeft.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        bendingLabelLeft.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bendingLabelLeft.setText("50%");
        bendingLabelLeft.setMaximumSize(new java.awt.Dimension(28, 16));
        bendingLabelLeft.setMinimumSize(new java.awt.Dimension(28, 16));
        bendingLabelLeft.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        flexMixerPanel.add(bendingLabelLeft, gridBagConstraints);

        meridiansLabelLeft.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meridiansLabelLeft.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        meridiansLabelLeft.setText("50%");
        meridiansLabelLeft.setMaximumSize(new java.awt.Dimension(28, 16));
        meridiansLabelLeft.setMinimumSize(new java.awt.Dimension(28, 16));
        meridiansLabelLeft.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        flexMixerPanel.add(meridiansLabelLeft, gridBagConstraints);

        lengthLabelLeft.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lengthLabelLeft.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lengthLabelLeft.setText("50%");
        lengthLabelLeft.setMaximumSize(new java.awt.Dimension(28, 16));
        lengthLabelLeft.setMinimumSize(new java.awt.Dimension(28, 16));
        lengthLabelLeft.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        flexMixerPanel.add(lengthLabelLeft, gridBagConstraints);

        lengthLabelRight.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lengthLabelRight.setText("50%");
        lengthLabelRight.setMaximumSize(new java.awt.Dimension(28, 16));
        lengthLabelRight.setMinimumSize(new java.awt.Dimension(28, 16));
        lengthLabelRight.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        flexMixerPanel.add(lengthLabelRight, gridBagConstraints);

        distanceLabelRight.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        distanceLabelRight.setText("50%");
        distanceLabelRight.setMaximumSize(new java.awt.Dimension(28, 16));
        distanceLabelRight.setMinimumSize(new java.awt.Dimension(28, 16));
        distanceLabelRight.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        flexMixerPanel.add(distanceLabelRight, gridBagConstraints);

        bendingLabelRight.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        bendingLabelRight.setText("50%");
        bendingLabelRight.setMaximumSize(new java.awt.Dimension(28, 16));
        bendingLabelRight.setMinimumSize(new java.awt.Dimension(28, 16));
        bendingLabelRight.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        flexMixerPanel.add(bendingLabelRight, gridBagConstraints);

        meridiansLabelRight.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        meridiansLabelRight.setText("50%");
        meridiansLabelRight.setMaximumSize(new java.awt.Dimension(28, 16));
        meridiansLabelRight.setMinimumSize(new java.awt.Dimension(28, 16));
        meridiansLabelRight.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        flexMixerPanel.add(meridiansLabelRight, gridBagConstraints);

        flexProjectionsNamesPanel.setLayout(new java.awt.GridLayout(1, 0));

        flexMixProjection1Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        flexMixProjection1Label.setText("Eckert IV");
        flexMixProjection1Label.setPreferredSize(new java.awt.Dimension(150, 16));
        flexProjectionsNamesPanel.add(flexMixProjection1Label);

        flexMixProjection2Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        flexMixProjection2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        flexMixProjection2Label.setText("Projection 2");
        flexMixProjection2Label.setPreferredSize(new java.awt.Dimension(150, 16));
        flexProjectionsNamesPanel.add(flexMixProjection2Label);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        flexMixerPanel.add(flexProjectionsNamesPanel, gridBagConstraints);

        mixerControlsPanel.add(flexMixerPanel, "flexMixerCard");

        latitudeMixerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        latitudeMixerPanel.setLayout(new java.awt.GridBagLayout());

        blendingLatitudeSlider.setMajorTickSpacing(45);
        blendingLatitudeSlider.setMaximum(90);
        blendingLatitudeSlider.setMinorTickSpacing(15);
        blendingLatitudeSlider.setPaintLabels(true);
        blendingLatitudeSlider.setPaintTicks(true);
        blendingLatitudeSlider.setValue(45);
        blendingLatitudeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blendingLatitudeSlidercombinerSliderChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        latitudeMixerPanel.add(blendingLatitudeSlider, gridBagConstraints);
        SliderUtils.setSliderLabels(blendingLatitudeSlider, new int[] {0, 45, 90}, new String[] {"0\u00B0", "45\u00B0","90\u00B0"});
        SliderUtils.reapplyFontSize(blendingLatitudeSlider);

        jLabel18.setText("Tolerance");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        latitudeMixerPanel.add(jLabel18, gridBagConstraints);

        jLabel19.setText("Latitude");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        latitudeMixerPanel.add(jLabel19, gridBagConstraints);

        blendingToleranceSlider.setMajorTickSpacing(15);
        blendingToleranceSlider.setMaximum(90);
        blendingToleranceSlider.setMinorTickSpacing(5);
        blendingToleranceSlider.setValue(15);
        blendingToleranceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blendingToleranceSlidercombinerSliderChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        latitudeMixerPanel.add(blendingToleranceSlider, gridBagConstraints);

        blendingScaleCheckBox.setSelected(true);
        blendingScaleCheckBox.setText("Automatic Size of Polar Projection");
        blendingScaleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blendingScaleCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        latitudeMixerPanel.add(blendingScaleCheckBox, gridBagConstraints);

        latitudeLabel.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        latitudeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        latitudeLabel.setText("45\u00B0");
        latitudeLabel.setMaximumSize(new java.awt.Dimension(28, 16));
        latitudeLabel.setMinimumSize(new java.awt.Dimension(28, 16));
        latitudeLabel.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        latitudeMixerPanel.add(latitudeLabel, gridBagConstraints);

        toleranceLabel.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        toleranceLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        toleranceLabel.setText("15\u00B0");
        toleranceLabel.setMaximumSize(new java.awt.Dimension(28, 16));
        toleranceLabel.setMinimumSize(new java.awt.Dimension(28, 16));
        toleranceLabel.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        latitudeMixerPanel.add(toleranceLabel, gridBagConstraints);

        blendingSizeSlider.setMaximum(150);
        blendingSizeSlider.setMinimum(50);
        blendingSizeSlider.setValue(100);
        blendingSizeSlider.setEnabled(false);
        blendingSizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                blendingSizeSlidercombinerSliderChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        latitudeMixerPanel.add(blendingSizeSlider, gridBagConstraints);

        sizeLabel.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        sizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sizeLabel.setText("15\u00B0");
        sizeLabel.setMaximumSize(new java.awt.Dimension(28, 16));
        sizeLabel.setMinimumSize(new java.awt.Dimension(28, 16));
        sizeLabel.setPreferredSize(new java.awt.Dimension(28, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        latitudeMixerPanel.add(sizeLabel, gridBagConstraints);

        latitudeMixerProjectionNamesPanel.setLayout(new java.awt.GridLayout(1, 0));

        latitudeProjection1Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        latitudeProjection1Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        latitudeProjection1Label.setText("Projection 1");
        latitudeProjection1Label.setPreferredSize(new java.awt.Dimension(100, 16));
        latitudeMixerProjectionNamesPanel.add(latitudeProjection1Label);

        latitudeProjection2Label.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        latitudeProjection2Label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        latitudeProjection2Label.setText("Projection 2");
        latitudeProjection2Label.setPreferredSize(new java.awt.Dimension(100, 16));
        latitudeMixerProjectionNamesPanel.add(latitudeProjection2Label);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        latitudeMixerPanel.add(latitudeMixerProjectionNamesPanel, gridBagConstraints);

        mixerControlsPanel.add(latitudeMixerPanel, "latitudeMixerCard");

        mixPanel.add(mixerControlsPanel, java.awt.BorderLayout.PAGE_START);

        mixerPanel.add(mixPanel, java.awt.BorderLayout.PAGE_START);

        methodPanel.add(mixerPanel, "mixerCard");

        flexTab.add(methodPanel, java.awt.BorderLayout.CENTER);

        mainTabs.addTab("Projection", null, flexTab, "Design a projection");

        displayTab.setMinimumSize(new java.awt.Dimension(355, 400));
        displayTab.setPreferredSize(new java.awt.Dimension(410, 400));
        displayTab.setLayout(new java.awt.BorderLayout());

        displayPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 10));
        displayPanel.setOpaque(false);
        displayPanel.setLayout(new java.awt.GridBagLayout());

        tissotLabel.setText("Indicatrix Density:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        displayPanel.add(tissotLabel, gridBagConstraints);

        showFlexCheckBox.setSelected(true);
        showFlexCheckBox.setText("Show Flex Projection");
        showFlexCheckBox.setToolTipText("Select to show the Flex Projection.");
        showFlexCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showFlexCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        displayPanel.add(showFlexCheckBox, gridBagConstraints);

        tissotComboBox.setEditable(true);
        tissotComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "10", "15", "30", "45", "90" }));
        tissotComboBox.setSelectedIndex(2);
        tissotComboBox.setToolTipText("The distance in degrees between two ellipses.");
        tissotComboBox.setMinimumSize(new java.awt.Dimension(50, 27));
        tissotComboBox.setPreferredSize(GUIUtil.getPreferredSize(tissotComboBox, COMBO_BOX_WIDTH));
        tissotComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tissotComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(tissotComboBox, gridBagConstraints);

        showGraticuleCheckBox.setSelected(true);
        showGraticuleCheckBox.setText("Graticule");
        showGraticuleCheckBox.setToolTipText("Select to display a graticule.");
        showGraticuleCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showGraticuleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showGraticuleCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        displayPanel.add(showGraticuleCheckBox, gridBagConstraints);

        showSecondProjectionCheckBox.setText("Show Second Projection");
        showSecondProjectionCheckBox.setToolTipText("Select a second projection for comparison.");
        showSecondProjectionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showSecondProjectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        displayPanel.add(showSecondProjectionCheckBox, gridBagConstraints);

        graticuleLabel.setText("Graticule Density:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        displayPanel.add(graticuleLabel, gridBagConstraints);

        graticuleComboBox.setEditable(true);
        graticuleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "10", "15", "30", "45", "90" }));
        graticuleComboBox.setToolTipText("The distance in degrees between two graticule lines.");
        graticuleComboBox.setMinimumSize(new java.awt.Dimension(50, 27));
        graticuleComboBox.setPreferredSize(GUIUtil.getPreferredSize(graticuleComboBox, COMBO_BOX_WIDTH));
        graticuleComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                graticuleComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(graticuleComboBox, gridBagConstraints);

        showTissotCheckBox.setText("Tissot's Indicatrices");
        showTissotCheckBox.setToolTipText("Select to display distortion ellipses.");
        showTissotCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showTissotCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTissotCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        displayPanel.add(showTissotCheckBox, gridBagConstraints);

        projectionComboBox.setMaximumRowCount(25);
        projectionComboBox.setToolTipText("Select the second projection for comparison.");
        projectionComboBox.setMinimumSize(new java.awt.Dimension(150, 27));
        projectionComboBox.setPreferredSize(GUIUtil.getPreferredSize(projectionComboBox, 250));
        projectionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                projectionComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 5, 0);
        displayPanel.add(projectionComboBox, gridBagConstraints);

        longitudeLabel.setText("Central Meridian");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(longitudeLabel, gridBagConstraints);

        lon0Slider.setMajorTickSpacing(90);
        lon0Slider.setMaximum(180);
        lon0Slider.setMinimum(-180);
        lon0Slider.setMinorTickSpacing(15);
        lon0Slider.setPaintLabels(true);
        lon0Slider.setPaintTicks(true);
        lon0Slider.setToolTipText("Adjust the central meridian of the projection.");
        lon0Slider.setValue(0);
        lon0Slider.setMinimumSize(new java.awt.Dimension(200, 29));
        lon0Slider.setPreferredSize(GUIUtil.getPreferredSize(lon0Slider, 200));
        lon0Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lon0SliderStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        displayPanel.add(lon0Slider, gridBagConstraints);
        {
            JSlider slider = lon0Slider;
            java.util.Hashtable labels = slider.createStandardLabels(slider.getMajorTickSpacing());
            java.util.Enumeration e = labels.elements();
            while(e.hasMoreElements()) {
                javax.swing.JComponent comp = (javax.swing.JComponent)e.nextElement();
                if (comp instanceof javax.swing.JLabel) {
                    javax.swing.JLabel label = (javax.swing.JLabel)(comp);
                    label.setText(label.getText() + "\u00b0");
                }
            }
            slider.setLabelTable(labels);
        }

        adjustButtonGroup.add(adjustNoRadioButton);
        adjustNoRadioButton.setText("Don't Adjust Size");
        adjustNoRadioButton.setToolTipText("If selected, the second projection is displayed at its normal size.");
        adjustNoRadioButton.setEnabled(false);
        adjustNoRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        adjustNoRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adjustRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 60, 3, 0);
        displayPanel.add(adjustNoRadioButton, gridBagConstraints);

        adjustButtonGroup.add(adjustWidthRadioButton);
        adjustWidthRadioButton.setSelected(true);
        adjustWidthRadioButton.setText("Same Width");
        adjustWidthRadioButton.setToolTipText("If selected, the size of the second projection is scaled to match its width with the Flex Projection.");
        adjustWidthRadioButton.setEnabled(false);
        adjustWidthRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        adjustWidthRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adjustRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 60, 3, 0);
        displayPanel.add(adjustWidthRadioButton, gridBagConstraints);

        adjustButtonGroup.add(adjustHeightRadioButton);
        adjustHeightRadioButton.setText("Same Height");
        adjustHeightRadioButton.setToolTipText("If selected, the size of the second projection is scaled to match its height with the Flex Projection.");
        adjustHeightRadioButton.setEnabled(false);
        adjustHeightRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        adjustHeightRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adjustRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 60, 0, 0);
        displayPanel.add(adjustHeightRadioButton, gridBagConstraints);

        adjustLabel.setText("Size Adjustment of Second Projection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 25, 5, 0);
        displayPanel.add(adjustLabel, gridBagConstraints);

        showAngularIsolinesCheckBox.setText("Isolines of Maximum Angular Distortion");
        showAngularIsolinesCheckBox.setToolTipText("Select to display isolines of angular distortion.");
        showAngularIsolinesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showAngularIsolinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAngularIsolinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 3, 0);
        displayPanel.add(showAngularIsolinesCheckBox, gridBagConstraints);

        angularIsolinesEquidistanceLabel.setText("Equidistance:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        displayPanel.add(angularIsolinesEquidistanceLabel, gridBagConstraints);

        angularIsolinesEquidistanceComboBox.setEditable(true);
        angularIsolinesEquidistanceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "10", "20", "30" }));
        angularIsolinesEquidistanceComboBox.setSelectedIndex(2);
        angularIsolinesEquidistanceComboBox.setToolTipText("The equidistance between two isolines.");
        angularIsolinesEquidistanceComboBox.setMinimumSize(new java.awt.Dimension(50, 27));
        angularIsolinesEquidistanceComboBox.setPreferredSize(GUIUtil.getPreferredSize(angularIsolinesEquidistanceComboBox, COMBO_BOX_WIDTH));
        angularIsolinesEquidistanceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                angularIsolinesEquidistanceComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(angularIsolinesEquidistanceComboBox, gridBagConstraints);

        showArealIsolinesCheckBox.setText("Isolines of Areal Distortion");
        showArealIsolinesCheckBox.setToolTipText("Select to display isolines of areal distortion.");
        showArealIsolinesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showArealIsolinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showArealIsolinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 3, 0);
        displayPanel.add(showArealIsolinesCheckBox, gridBagConstraints);

        arealIsolinesEquidistanceLabel.setText("Equidistance:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        displayPanel.add(arealIsolinesEquidistanceLabel, gridBagConstraints);

        arealIsolinesEquidistanceComboBox.setEditable(true);
        arealIsolinesEquidistanceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0.1", "0.25", "0.5", "1", "2" }));
        arealIsolinesEquidistanceComboBox.setSelectedIndex(2);
        arealIsolinesEquidistanceComboBox.setToolTipText("The equidistance between two isolines.");
        arealIsolinesEquidistanceComboBox.setMinimumSize(new java.awt.Dimension(50, 27));
        arealIsolinesEquidistanceComboBox.setPreferredSize(GUIUtil.getPreferredSize(arealIsolinesEquidistanceComboBox, COMBO_BOX_WIDTH));
        arealIsolinesEquidistanceComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                arealIsolinesEquidistanceComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(arealIsolinesEquidistanceComboBox, gridBagConstraints);

        showAcceptableAreaCheckBox.setText("Area of Acceptable Distortion");
        showAcceptableAreaCheckBox.setToolTipText("Select to display the area with acceptable distortion. Use the button below to define what distortion is acceptable.");
        showAcceptableAreaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        showAcceptableAreaCheckBox.setName("Area of Acceptable Distortion"); // NOI18N
        showAcceptableAreaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAcceptableAreaCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 3, 0);
        displayPanel.add(showAcceptableAreaCheckBox, gridBagConstraints);

        acceptableAreaOptionsButton.setText("Acceptable Area Options…");
        acceptableAreaOptionsButton.setToolTipText("Change the acceptable distortion.");
        acceptableAreaOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptableAreaOptionsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        displayPanel.add(acceptableAreaOptionsButton, gridBagConstraints);

        centralMeridianField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        centralMeridianField.setPreferredSize(GUIUtil.getPreferredSize(centralMeridianField, 60));
        centralMeridianField.setValue(0.);
        centralMeridianField.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                centralMeridianFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        displayPanel.add(centralMeridianField, gridBagConstraints);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        tissotScaleLabel.setText("Size:");
        jPanel2.add(tissotScaleLabel);

        tissotScaleSlider.setMaximum(50);
        tissotScaleSlider.setMinimum(-50);
        tissotScaleSlider.setToolTipText("Adjust the size of the ellipses.");
        tissotScaleSlider.setValue(0);
        tissotScaleSlider.setPreferredSize(GUIUtil.getPreferredSize(tissotScaleSlider, 150));
        tissotScaleSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tissotScaleSliderStateChanged(evt);
            }
        });
        jPanel2.add(tissotScaleSlider);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        displayPanel.add(jPanel2, gridBagConstraints);

        displayTab.add(displayPanel, java.awt.BorderLayout.NORTH);

        mainTabs.addTab("Display", null, displayTab, "Configure the content of the map");

        add(mainTabs, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void removeMeridiansMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMeridiansMenuItemActionPerformed
        removeMeridiansAdjustments();
    }//GEN-LAST:event_removeMeridiansMenuItemActionPerformed

    private void removeBendingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBendingMenuItemActionPerformed
        removeBendingAdjustments();
    }//GEN-LAST:event_removeBendingMenuItemActionPerformed

    private void removeDistanceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDistanceMenuItemActionPerformed
        removeDistanceAdjustments();
    }//GEN-LAST:event_removeDistanceMenuItemActionPerformed

    private void normalizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalizeMenuItemActionPerformed
        normalizeFlexCurves();
    }//GEN-LAST:event_normalizeMenuItemActionPerformed

    private void polesAndEquatorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_polesAndEquatorMenuItemActionPerformed
        showPolesAndEquatorDialog();
    }//GEN-LAST:event_polesAndEquatorMenuItemActionPerformed

    private void resetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetMenuItemActionPerformed
        resetFlexProjection();
    }//GEN-LAST:event_resetMenuItemActionPerformed

    public void showAcceptanceDialog() {

        if (!FlexProjectorPreferencesPanel.isAreaAcceptanceRelativeTo1()) {
            String msg = "This will change your preferences settings.\n"
                    + "The acceptable area distortion will be changed to be "
                    + "relative to an equal-area projection.";
            String title = "Change to Preferences";
            int res = JOptionPane.showConfirmDialog(GUIUtil.getOwnerFrame(this), msg, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res != JOptionPane.OK_OPTION) {
                return;
            }
            FlexProjectorPreferencesPanel.setAreaAcceptanceRelativeTo1(true);
        }
        Frame parent = ika.gui.GUIUtil.getOwnerFrame(this);
        new QPanel(model.getDisplayModel().qModel).showQPanel(parent);

        // the visibility of the accptance visualization in the map may have changed
        writeDisplayGUI();
    }

    private void acceptableAreaOptionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptableAreaOptionsButtonActionPerformed
        showAcceptanceDialog();
    }//GEN-LAST:event_acceptableAreaOptionsButtonActionPerformed

    private void showAcceptableAreaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAcceptableAreaCheckBoxActionPerformed
        model.getDisplayModel().qModel.setShowAcceptableArea(
                this.showAcceptableAreaCheckBox.isSelected());
        writeDisplayEnabledState();
    }//GEN-LAST:event_showAcceptableAreaCheckBoxActionPerformed

    private void writeSizeLabel() {
        final double R = Ellipsoid.SPHERE.getEquatorRadius();
        Projection proj = model.getDesignProjection();
        ika.geo.GeoPath outline = FlexProjectorModel.constructOutline(proj);
        double flexArea = outline.getArea();
        double sphereArea = 4. * Math.PI * R * R;
        DecimalFormat format = new DecimalFormat("#,##0.0%");
        this.currentSizeLabel.setText(format.format(flexArea / sphereArea));
    }

    protected void showSizeDialog() {
        showDesignProjection();

        double initialScale = model.getDesignProjection().getScale();
        writeInternalScaleGUI();
        writeSizeLabel();
        scaleDialog.setVisible(true);

        // components in the dialog don't handle undo/redo themselves.
        if (mapComponent != null) {
            double newScale = model.getFlexProjectionModel().getScale();
            if (newScale != initialScale) {
                mapComponent.addUndo("Projection Size");
            }
        }
    }

    private void showPolesAndEquatorDialog() {
        showDesignProjection();
        JOptionPane.showMessageDialog(GUIUtil.getOwnerFrame(this),
                meridiansDirectionPanel,
                "Meridians at Poles and Equator",
                JOptionPane.PLAIN_MESSAGE);
        // components in the dialog don't handle undo/redo themselves.
        if (mapComponent != null) {
            mapComponent.addUndo("Meridians Direction");
        }
    }

    private void meridianEquatorRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meridianEquatorRadioButtonActionPerformed
        if (updatingGUI) {
            return;
        }

        if (!((JRadioButton) evt.getSource()).isSelected()) {
            return;
        }

        boolean smooth = meridianSmoothRadioButton.isSelected();
        model.getFlexProjectionModel().setMeridiansSmoothAtEquator(smooth);
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_meridianEquatorRadioButtonActionPerformed

    private void scaleManualSlidersliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_scaleManualSlidersliderStateChanged
        if (updatingGUI) {
            return;
        }

        // update number field
        try {
            updatingGUI = true;
            double manualScale = scaleManualSlider.getValue() / SLIDER_SCALE;
            scaleManualField.setValue(manualScale);
        } finally {
            updatingGUI = false;
        }

        // update the flexModel
        if (scaleManualSlider.getValueIsAdjusting() == false) {
            readInternalScaleGUI();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_scaleManualSlidersliderStateChanged

    private void arealIsolinesEquidistanceComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_arealIsolinesEquidistanceComboBoxItemStateChanged
        String str = (String) arealIsolinesEquidistanceComboBox.getSelectedItem();
        Number equidistance = null;
        try {
            equidistance = NumberFormat.getNumberInstance().parse(str);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (equidistance != null) {
            model.getDisplayModel().arealIsolinesEquidistance = equidistance.doubleValue();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_arealIsolinesEquidistanceComboBoxItemStateChanged

    private void showArealIsolinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showArealIsolinesCheckBoxActionPerformed
        model.getDisplayModel().showArealIsolines =
                showArealIsolinesCheckBox.isSelected();
        updateDistortionIndicesAndInformListeners();
        writeDisplayEnabledState();
    }//GEN-LAST:event_showArealIsolinesCheckBoxActionPerformed

    private void showAngularIsolinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAngularIsolinesCheckBoxActionPerformed
        model.getDisplayModel().showAngularIsolines =
                showAngularIsolinesCheckBox.isSelected();
        updateDistortionIndicesAndInformListeners();
        writeDisplayEnabledState();
    }//GEN-LAST:event_showAngularIsolinesCheckBoxActionPerformed

    private void angularIsolinesEquidistanceComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_angularIsolinesEquidistanceComboBoxItemStateChanged
        String str = (String) this.angularIsolinesEquidistanceComboBox.getSelectedItem();
        Number equidistance = null;
        try {
            equidistance = NumberFormat.getNumberInstance().parse(str);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (equidistance != null) {
            model.getDisplayModel().angularIsolinesEquidistance = equidistance.doubleValue();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_angularIsolinesEquidistanceComboBoxItemStateChanged

    private void poleDirectionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_poleDirectionCheckBoxActionPerformed
        if (updatingGUI) {
            return;
        }

        boolean adjust = poleDirectionCheckBox.isSelected();
        poleDirectionSlider.setEnabled(adjust);
        poleDirectionFormattedTextField.setEnabled(adjust);
        model.getFlexProjectionModel().setAdjustPoleDirection(adjust);
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_poleDirectionCheckBoxActionPerformed

    private void poleDirectionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_poleDirectionSliderStateChanged

        if (updatingGUI) {
            return;
        }

        double angle = poleDirectionSlider.getValue();
        try {
            updatingGUI = true;

            // update number field
            poleDirectionFormattedTextField.setValue(angle);

        } finally {
            updatingGUI = false;
        }

        // update the flexModel
        if (poleDirectionSlider.getValueIsAdjusting() == false) {
            model.getFlexProjectionModel().setMeridiansPoleDirection(angle);
            updateDistortionIndicesAndInformListeners();
        }

    }//GEN-LAST:event_poleDirectionSliderStateChanged

    /**
     * Resets the length of all parallels to 1.
     */
    private void makeCylindricalProjection() {
        showDesignProjection();
        model.getFlexProjectionModel().resetLengthDistribution();
        mapComponent.addUndo("Make Cylindrical Projection");
        updateDistortionIndicesAndInformListeners();
        writeFlexSliderGUI();
    }

    /**
     * Resets the distances between meridians to a constant spacing.
     */
    private void removeMeridiansAdjustments() {
        showDesignProjection();
        model.getFlexProjectionModel().resetMeridiansDistribution();
        mapComponent.addUndo("Regular Meridians Distribution");
        updateDistortionIndicesAndInformListeners();
        writeFlexSliderGUI();
    }

    /**
     * Resets bent parallels to straight lines.
     */
    private void removeBendingAdjustments() {
        showDesignProjection();
        model.getFlexProjectionModel().resetBending();
        mapComponent.addUndo("Remove Bending");
        updateDistortionIndicesAndInformListeners();
        writeFlexSliderGUI();
    }

    /**
     * Resets the vertical distribution of parallels such that meridians are
     * regularly spaced.
     */
    private void removeDistanceAdjustments() {
        showDesignProjection();
        model.getFlexProjectionModel().linearYDistribution();
        mapComponent.addUndo("Linear Distance Distribution");
        updateDistortionIndicesAndInformListeners();
        writeFlexSliderGUI();
    }

    private void normalizeFlexCurves() {
        showDesignProjection();
        model.getFlexProjectionModel().normalize();
        updateDistortionIndicesAndInformListeners();
        mapComponent.addUndo("Normalize");
        writeFlexSliderGUI();
        writeVerticalScaleGUI();
    }

    private void bendingComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bendingComboBoxItemStateChanged
        if (updatingGUI) {
            return;
        }

        showDesignProjection();
        int curveID = bendingComboBox.getSelectedIndex();

        updatingGUI = true;
        try {
            switch (curveID) {
                case FlexProjectionModel.COSINE_CURVE:
                    cosineBendingRadioButtonMenuItem.setSelected(true);
                    break;
                case FlexProjectionModel.CUBIC_CURVE:
                    cubicBendingRadioButtonMenuItem.setSelected(true);
                    break;
                case FlexProjectionModel.QUADRATIC_CURVE:
                    quadraticBendingRadioButtonMenuItem.setSelected(true);
                    break;
            }
        } finally {
            updatingGUI = false;
        }

        model.getFlexProjectionModel().setCurveShape(curveID);
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_bendingComboBoxItemStateChanged

    private void tissotScaleSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tissotScaleSliderStateChanged
        JSlider slider = (JSlider) evt.getSource();
        if (!slider.getValueIsAdjusting()) {
            double v = slider.getValue();
            if (v > 0) {
                v = 1 + v * 9. / 100.;
            } else {
                v = 1 + v * 0.9 / 100.;
            }
            this.model.getDisplayModel().tissotScale = v;
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_tissotScaleSliderStateChanged

    private void tissotComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tissotComboBoxItemStateChanged
        String gratStr = (String) tissotComboBox.getSelectedItem();
        Number gratNumber = null;
        try {
            gratNumber = NumberFormat.getNumberInstance().parse(gratStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (gratNumber != null) {
            model.getDisplayModel().tissotDensity = gratNumber.doubleValue();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_tissotComboBoxItemStateChanged

    private void showTissotCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTissotCheckBoxActionPerformed
        this.toggleTissot();
    }//GEN-LAST:event_showTissotCheckBoxActionPerformed

    public void toggleTissot() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {
            model.getDisplayModel().showTissot = !model.getDisplayModel().showTissot;
            updateDistortionIndicesAndInformListeners();
            writeDisplayGUI();
        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public boolean isShowingTissot() {
        return model.getDisplayModel().showTissot;
    }

    /**
     * Toggle the visibility of the foreground flex projection and the
     * background second projection.
     */
    public void toggleProjectionVisibility() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {
            boolean showFlex = showFlexCheckBox.isSelected();
            boolean showSecond = showSecondProjectionCheckBox.isSelected();
            if (showFlex && showSecond) {
                showSecond = false;
            } else if (!showFlex && !showSecond) {
                showFlex = true;
            } else {
                showFlex = !showFlex;
                showSecond = !showSecond;
            }

            showFlexCheckBox.setSelected(showFlex);
            showSecondProjectionCheckBox.setSelected(showSecond);

            model.getDisplayModel().showFlexProjection = showFlex;
            model.getDisplayModel().showSecondProjection = showSecond;

            writeDisplayGUI();
            updateDistortionProfilesManager();
            updateDistortionIndicesAndInformListeners();

        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public void toggleFlexProjection() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {

            model.getDisplayModel().showFlexProjection =
                    !model.getDisplayModel().showFlexProjection;
            writeDisplayGUI();
            updateDistortionProfilesManager();
            updateDistortionIndicesAndInformListeners();
        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public boolean isShowingFlexProjection() {
        return model.getDisplayModel().showFlexProjection;
    }

    public void toggleSecondProjection() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {
            model.getDisplayModel().showSecondProjection =
                    !model.getDisplayModel().showSecondProjection;
            writeDisplayGUI();
            updateDistortionProfilesManager();
            updateDistortionIndicesAndInformListeners();
        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public boolean isShowingSecondProjection() {
        return model.getDisplayModel().showSecondProjection;
    }

    private void showGraticuleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showGraticuleCheckBoxActionPerformed
        toggleGraticule();
    }//GEN-LAST:event_showGraticuleCheckBoxActionPerformed

    public void toggleGraticule() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {
            model.getDisplayModel().showGraticule = !model.getDisplayModel().showGraticule;
            updateDistortionIndicesAndInformListeners();
            writeDisplayGUI();
        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public boolean isShowingGraticule() {
        return model.getDisplayModel().showGraticule;
    }

    public void toggleCoastline() {
        // don't trigger map event to avoid setting the dirty flag of the document
        MapEventTrigger trigger = new MapEventTrigger(mapComponent.getGeoSet());
        try {
            model.getDisplayModel().showCoastline = !model.getDisplayModel().showCoastline;
            updateDistortionIndicesAndInformListeners();
            writeDisplayEnabledState();
        } finally {
            trigger.abort();
            mapComponent.repaint();
        }
    }

    public boolean isShowingCoastline() {
        return model.getDisplayModel().showCoastline;
    }

    private void adjustRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adjustRadioButtonActionPerformed
        if (adjustNoRadioButton.isSelected()) {
            model.getDisplayModel().secondProjectionAdjustment = FlexProjectorModel.DisplayModel.ADJUST_NO;
        } else if (adjustWidthRadioButton.isSelected()) {
            model.getDisplayModel().secondProjectionAdjustment = FlexProjectorModel.DisplayModel.ADJUST_WIDTH;
        } else if (adjustHeightRadioButton.isSelected()) {
            model.getDisplayModel().secondProjectionAdjustment = FlexProjectorModel.DisplayModel.ADJUST_HEIGHT;
        }
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_adjustRadioButtonActionPerformed

    private void graticuleComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_graticuleComboBoxItemStateChanged

        String gratStr = (String) this.graticuleComboBox.getSelectedItem();
        Number gratNumber = null;
        try {
            gratNumber = NumberFormat.getNumberInstance().parse(gratStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        if (gratNumber != null) {
            this.model.getDisplayModel().graticuleDensity = gratNumber.doubleValue();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_graticuleComboBoxItemStateChanged

    private void writeDisplayEnabledState() {
        DisplayModel m = model.getDisplayModel();

        this.adjustLabel.setEnabled(m.showSecondProjection);
        this.adjustWidthRadioButton.setEnabled(m.showSecondProjection);
        this.adjustHeightRadioButton.setEnabled(m.showSecondProjection);
        this.adjustNoRadioButton.setEnabled(m.showSecondProjection);

        this.graticuleComboBox.setEnabled(m.showGraticule);
        this.graticuleLabel.setEnabled(m.showGraticule);

        this.tissotLabel.setEnabled(m.showTissot);
        this.tissotComboBox.setEnabled(m.showTissot);
        this.tissotScaleLabel.setEnabled(m.showTissot);
        this.tissotScaleSlider.setEnabled(m.showTissot);

        this.arealIsolinesEquidistanceLabel.setEnabled(m.showArealIsolines);
        this.arealIsolinesEquidistanceComboBox.setEnabled(m.showArealIsolines);

        this.angularIsolinesEquidistanceLabel.setEnabled(m.showAngularIsolines);
        this.angularIsolinesEquidistanceComboBox.setEnabled(m.showAngularIsolines);

        this.acceptableAreaOptionsButton.setEnabled(m.qModel.isShowAcceptableArea());
    }

    private void updateDistortionProfilesManager() {
        if (this.distortionProfilesManager != null) {
            FlexProjectorModel.DisplayModel dm = this.model.getDisplayModel();
            distortionProfilesManager.setShowFlexProfiles(dm.showFlexProjection);
            distortionProfilesManager.setShowBackgroundProfiles(dm.showSecondProjection);
        }
    }

    private void showCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCheckBoxActionPerformed

        FlexProjectorModel.DisplayModel dm = model.getDisplayModel();
        dm.showFlexProjection = showFlexCheckBox.isSelected();
        dm.showSecondProjection = showSecondProjectionCheckBox.isSelected();

        // make sure one projection is always visible
        if (!dm.showFlexProjection && !dm.showSecondProjection) {
            // if the user clicked on check box to hide foreground projection,
            // show the background projection
            if (evt.getSource() == showFlexCheckBox) {
                dm.showSecondProjection = true;
                this.showSecondProjectionCheckBox.setSelected(true);
            }

            // if the user clicked on the check box to hide the background
            // projection, show the foreground projection.
            if (evt.getSource() == showSecondProjectionCheckBox) {
                dm.showFlexProjection = true;
                this.showFlexCheckBox.setSelected(true);
            }
        }

        writeDisplayEnabledState();
        updateDistortionProfilesManager();
        updateDistortionIndicesAndInformListeners();

    }//GEN-LAST:event_showCheckBoxActionPerformed

    private void lon0SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lon0SliderStateChanged
        updatingGUI = true;
        try {
            JSlider slider = (JSlider) evt.getSource();
            centralMeridianField.setValue((double) slider.getValue());
            if (!slider.getValueIsAdjusting()) {
                double lon0 = slider.getValue();
                model.setCentralMeridian(lon0);
                Projection foreProj = model.getDesignProjection();
                designProjectionChangeListeners.announce().designProjectionChanged(foreProj);
            }
        } finally {
            updatingGUI = false;
        }
    }//GEN-LAST:event_lon0SliderStateChanged

    public static DesignProjection loadFlexProjection() {

        String filePath = FileUtils.askFile(null, "Select a Flex Projection Document", true);
        if (filePath == null) {
            return null;  // user cancelled
        }

        try {
            String str = new String(FileUtils.getBytesFromFile(new File(filePath)));
            return DesignProjection.factory(str);
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorDialog.showErrorDialog("Could not read the Flex Projection document.", ex);
            return null;
        }

    }

    public void setBackgroundProjection(String name) {
        final com.jhlabs.map.proj.Projection projection;
        if (name.startsWith("Flex Projection")) {
            projection = ProjectionBrewerPanel.loadFlexProjection();
        } else {
            projection = ProjectionFactory.getNamedProjection(name);
        }

        if (projection == null) {
            // problem finding the projection, revert to the previous one.
            String previousName = model.getDisplayModel().projection.toString();
            projectionComboBox.setSelectedItem(previousName);
            return;
        }

        model.getDisplayModel().projection = projection;
        model.getDisplayModel().showSecondProjection = true;

        if (distortionProfilesManager != null) {
            distortionProfilesManager.setShowBackgroundProfiles(true);
            distortionProfilesManager.setBackgroundProjection(model.getDisplayModel().projection);
        }

        try {
            updatingGUI = true;
            writeDisplayGUI();
        } finally {
            updatingGUI = false;
        }

        // inform listeners
        Projection foreProj = model.getDesignProjection();
        designProjectionChangeListeners.announce().designProjectionChanged(foreProj);
    }

    private void projectionComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_projectionComboBoxItemStateChanged
        if (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
            return;
        }

        String name = (String) projectionComboBox.getSelectedItem();
        setBackgroundProjection(name);
    }//GEN-LAST:event_projectionComboBoxItemStateChanged

    private void resetFlexProjection(String projName) throws IOException {

        if (projName == null) {
            return;
        }

        final Projection proj;
        if (projName.startsWith(ProjectionsManager.SELECT_FLEX_FILE_STRING)) {
            // load flex projection from file
            proj = loadFlexProjection();
            if (proj == null) {
                return; // user canceled
            }
        } else {
            proj = ProjectionsManager.getProjection(projName);
        }

        // if the external file is a flex file (i.e. parameters of a flex
        // projection), do not use getFlexProjectionModel().reset(proj), but 
        // use the model of the imported flex projection.
        if (proj instanceof FlexProjection) {
            FlexProjection flexProj = (FlexProjection) proj;
            model.getFlexProjection().setModel(flexProj.getModel().clone());
        } else {
            model.getFlexProjectionModel().reset(proj);
        }

        updateDistortionIndicesAndInformListeners();
        writeFlexSliderGUI();
    }

    /**
     * Initializes the Flex projection with the shape of another projection.
     */
    public void resetFlexProjection() {

        showDesignProjection();

        FlexProjectionModel initialFlexModel = model.getFlexProjectionModel().clone();
        try {

            // construct list with projection names
            List<String> projNames = ProjectionsManager.getProjectionNames(true, false);

            // ask the user for a projection
            String msg = "Reset to this projection:";
            String title = "Reset Flex Projection";
            ListDialog listDialog = new ListDialog(msg);

            // property change listener that adapts the displayed map to the
            // selected projection
            listDialog.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ListDialog.SELECTION_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                        String projName = (String) ((Object[]) (evt.getNewValue()))[0];
                        try {
                            resetFlexProjection(projName);
                            mapComponent.showAll();
                        } catch (IOException ex) {
                            //
                        }
                    }
                }
            });
            String projName = (String) (listDialog.showDialog(
                    ika.gui.GUIUtil.getOwnerFrame(this),
                    title,
                    projNames.toArray()));

            // reset to previous projection if user cancels
            if (projName == null) {
                model.getFlexProjection().setModel(initialFlexModel);
            }

        } catch (Exception exc) {

            ika.utils.ErrorDialog.showErrorDialog(
                    "An error occured when resetting the projection.",
                    "Flex Projector Error",
                    exc,
                    GUIUtil.getOwnerFrame(this));
            model.getFlexProjection().setModel(initialFlexModel);
        } finally {

            // for cancelling and exceptions
            updateDistortionIndicesAndInformListeners();
            writeFlexSliderGUI();

            mapComponent.addUndo("Reset");
            mapComponent.showAll();
        }
    }

    public void setMap(MapComponent map) {
        mapComponent = map;
    }

    private void eliminateShapeDistortionAtOriginMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminateShapeDistortionAtOriginMenuItemActionPerformed

        if (updatingGUI) {
            return;
        }

        showDesignProjection();

        int res = JOptionPane.showOptionDialog(ika.gui.GUIUtil.getOwnerFrame(this),
                "<html>The height-to-width proportions of the <br>"
                + "projection will be adjusted such that shape is not <br>"
                + "distorted at the origin of the projection.<br>"
                + "<br><small>"
                + "The origin is the central point of the projection graticule.<br>"
                + " The effect of this command can be easily seen when the <br>"
                + " Tissot indictarices are visible. After applying the <br>"
                + "command, the Tissot indicatrix at the center of the <br>"
                + "projection is a perfect circle, and not an ellipse.</small></html>",
                "Eliminate Shape Distortion at Origin",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new String[]{"Continue", "Cancel"},
                "Continue");
        if (res != 0) {
            return;
        }

        try {
            updatingGUI = true;
            model.getFlexProjectionModel().eliminateShapeDistortionAtOrigin();
            mapComponent.addUndo("Eliminate Shape Distortion at Origin");
        } finally {
            updatingGUI = false;
        }
        updateDistortionIndicesAndInformListeners();
        writeMethodGUI();

}//GEN-LAST:event_eliminateShapeDistortionAtOriginMenuItemActionPerformed

    private void scaleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleRadioButtonActionPerformed
        if (updatingGUI) {
            return;
        }
        readInternalScaleGUI();
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_scaleRadioButtonActionPerformed

    private void scalePointSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_scalePointSliderStateChanged
        if (updatingGUI) {
            return;
        }

        // update number fields
        try {
            updatingGUI = true;
            scaleLonField.setValue(scaleLonSlider.getValue());
            scaleLatField.setValue(scaleLatSlider.getValue());
        } finally {
            updatingGUI = false;
        }

        if (((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            readInternalScaleGUI();
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_scalePointSliderStateChanged

    /**
     * updates the GUI depending on the selected method button
     *
     * @param evt
     * @param undoStr String to display in Undo/Redo menus.
     */
    private void switchMethodGUI(String cardName, String undoStr, boolean addUndo) {

        try {
            updatingGUI = true;

            boolean showMixerPanel = cardName.contains("Mixer");
            CardLayout cl = (CardLayout) (methodPanel.getLayout());
            cl.show(methodPanel, showMixerPanel ? "mixerCard" : "flexSlidersCard");

            // not all projections are available, depending on the projection mixer
            // the content of the menus for selecting the mixed projections
            // must be updated.
            initMixerMenus();

            if (showMixerPanel) {
                cl = (CardLayout) (mixerControlsPanel.getLayout());
                cl.show(mixerControlsPanel, cardName);
                writeVerticalScaleGUI();
            }
        } finally {
            updatingGUI = false;
        }

        showDesignProjection();
        updateDistortionIndicesAndInformListeners();
        if (addUndo && mapComponent != null) {
            mapComponent.addUndo(undoStr);
        }

        if (mapComponent != null) {
            mapComponent.showAll();
        }
    }

    private void mixerMap1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mixerMap1ComponentResized
        updateMixerMap(model.getMixerProjection1(), mixerMap1);
}//GEN-LAST:event_mixerMap1ComponentResized

    private void writeVerticalScaleGUI() {
        try {
            updatingGUI = true;
            double vScale = model.getDesignProjection().getVerticalScale();
            verticalScaleSlider.setValue((int) Math.round(vScale * SLIDER_SCALE));
            verticalScaleFormattedTextField.setValue(new Double(vScale));
        } finally {
            updatingGUI = false;
        }
    }

    /**
     * Update the GUI: switch to the appropriate panels and write all settings
     * to the GUI.
     */
    protected final void writeMethodGUI() {
        DesignProjection p = getModel().getDesignProjection();

        writeVerticalScaleGUI();
        try {
            updatingGUI = true;
            if (p instanceof FlexMixProjection) {
                model.flexMixProjectionToForeground();
                switchMethodGUI("flexMixerCard", "Switch to Flex Mixer", false);
                writeFlexMixerGUI();
            } else if (p instanceof MeanProjection) {
                model.meanProjectionToForeground();
                switchMethodGUI("simpleMixerCard", "Switch to Simple Mixer", false);
                writeMeanMixerGUI();
            } else if (p instanceof LatitudeMixerProjection) {
                model.combinedProjectionToForeground();
                switchMethodGUI("latitudeMixerCard", "Switch to Latitude Mixer", false);
                writeLatitudeMixerGUI();
            } else if (p instanceof FlexProjection) {
                model.flexProjectionToForeground();
                switchMethodGUI("flexSlidersCard", "Switch to Flex Sliders", false);
                writeFlexSliderGUI();
            }
        } finally {
            updatingGUI = false;
        }

    }

    private void writeFlexMixerGUI() {

        try {
            updatingGUI = true;

            flexMixerToggleButton.setSelected(true);

            FlexMixProjection p = model.getFlexMixProjection();

            mixerLengthSlider.setValue((int) Math.round(100 * p.getLengthW()));
            mixerDistanceSlider.setValue((int) Math.round(100 * p.getDistanceW()));
            mixerBendingSlider.setValue((int) Math.round(100 * p.getBendingW()));
            mixerMeridiansSlider.setValue((int) Math.round(100 * p.getMeridiansW()));

            // enable or disable the sliders for bending and meridians
            // distribution as they do not apply for all projections.
            boolean bentParallels = p.isAdjustingBending();
            boolean irregularMeridians = p.isAdjustingMeridians();

            mixerBendingSlider.setEnabled(bentParallels);
            bendingLabelLeft.setEnabled(bentParallels);
            bendingLabelRight.setEnabled(bentParallels);
            bendingParallelsLabel.setEnabled(bentParallels);

            mixerMeridiansSlider.setEnabled(irregularMeridians);
            meridiansLabelLeft.setEnabled(irregularMeridians);
            meridiansLabelRight.setEnabled(irregularMeridians);
            meridiansDistributionLabel.setEnabled(irregularMeridians);

            // update labels
            flexMixProjection1Label.setText(p.getProjection1().toString());
            flexMixProjection2Label.setText(p.getProjection2().toString());
        } finally {
            updatingGUI = false;
        }
    }

    private void writeMeanMixerGUI() {
        try {
            updatingGUI = true;
            simpleMixerToggleButton.setSelected(true);
            MeanProjection p = model.getMeanProjection();
            meanSlider.setValue((int) Math.round(100 * p.getWeight()));
            meanProjection1Label.setText(p.getProjection1().toString());
            meanProjection2Label.setText(p.getProjection2().toString());
        } finally {
            updatingGUI = false;
        }
    }

    private void writeLatitudeMixerGUI() {
        try {
            updatingGUI = true;
            latitudeMixerToggleButton.setSelected(true);
            LatitudeMixerProjection p = model.getLatitudeMixerProjection();

            blendingLatitudeSlider.setValue((int) Math.round(p.getLatitude()));
            blendingToleranceSlider.setValue((int) Math.round(p.getTolerance()));
            blendingSizeSlider.setValue((int) Math.round(100 * p.getPoleScale()));
            blendingScaleCheckBox.setSelected(p.isAutomaticScale());

            latitudeProjection1Label.setText(p.getProjection1().toString());
            latitudeProjection2Label.setText(p.getProjection2().toString());
            int scale = (int) Math.round(p.getPoleScale() * 100);
            sizeLabel.setText(scale + "%");
        } finally {
            updatingGUI = false;
        }
    }

    private void readLatitudeMixerGUI() {
        if (updatingGUI) {
            return;
        }

        LatitudeMixerProjection p = model.getLatitudeMixerProjection();
        p.setLatitude(blendingLatitudeSlider.getValue());
        p.setTolerance(blendingToleranceSlider.getValue());
        p.setAutomaticScale(blendingScaleCheckBox.isSelected());
        if (blendingScaleCheckBox.isSelected()) {
            try {
                updatingGUI = true;
                int scale = (int) Math.round(p.getPoleScale() * 100);
                blendingSizeSlider.setValue(scale);
            } finally {
                updatingGUI = false;
            }

        } else {
            p.setPoleScale(blendingSizeSlider.getValue() / 100d);
        }
        updateDistortionIndicesAndInformListeners();
    }

    private void blendingLatitudeSlidercombinerSliderChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blendingLatitudeSlidercombinerSliderChanged
        if (updatingGUI) {
            return;
        }
        latitudeLabel.setText(Integer.toString(blendingLatitudeSlider.getValue()) + "\u00B0");
        if (liveUpdate || ((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            readLatitudeMixerGUI();
            mapComponent.addUndo("Mixer Latitude");
            showDesignProjection();
        }
}//GEN-LAST:event_blendingLatitudeSlidercombinerSliderChanged

    private void blendingToleranceSlidercombinerSliderChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blendingToleranceSlidercombinerSliderChanged
        if (updatingGUI) {
            return;
        }
        toleranceLabel.setText(Integer.toString(blendingToleranceSlider.getValue()) + "\u00B0");
        if (liveUpdate || ((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            readLatitudeMixerGUI();
            mapComponent.addUndo("Latitude Blending Tolerance");
            showDesignProjection();
        }
}//GEN-LAST:event_blendingToleranceSlidercombinerSliderChanged

    private void blendingScaleCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blendingScaleCheckBoxActionPerformed
        if (updatingGUI) {
            return;
        }
        blendingSizeSlider.setEnabled(!blendingScaleCheckBox.isSelected());
        readLatitudeMixerGUI();
        mapComponent.addUndo("Automatic Size of Polar Projection");
        showDesignProjection();
}//GEN-LAST:event_blendingScaleCheckBoxActionPerformed

    private void meanSlidermixerSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_meanSlidermixerSliderStateChanged
        meanLabel1.setText(Integer.toString(100 - meanSlider.getValue()) + "%");
        meanLabel2.setText(Integer.toString(meanSlider.getValue()) + "%");

        if (updatingGUI) {
            return;
        }

        if (liveUpdate || ((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            double w = meanSlider.getValue() / 100D;
            model.getMeanProjection().setWeight(w);
            updateDistortionIndicesAndInformListeners();
            mapComponent.addUndo("Mixer Weight");
            showDesignProjection();
        }
}//GEN-LAST:event_meanSlidermixerSliderStateChanged

    private void mixerMap2ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mixerMap2ComponentResized
        updateMixerMap(model.getMixerProjection2(), mixerMap2);
}//GEN-LAST:event_mixerMap2ComponentResized

    private void simpleMixerToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpleMixerToggleButtonActionPerformed
        model.meanProjectionToForeground();
        switchMethodGUI("simpleMixerCard", "Switch to Simple Mixer", true);
        writeMeanMixerGUI();
    }//GEN-LAST:event_simpleMixerToggleButtonActionPerformed

    private void flexMixerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_flexMixerStateChanged
        if (updatingGUI) {
            return;
        }

        // update the labels
        lengthLabelLeft.setText(Integer.toString(100 - mixerLengthSlider.getValue()) + "%");
        distanceLabelLeft.setText(Integer.toString(100 - mixerDistanceSlider.getValue()) + "%");
        bendingLabelLeft.setText(Integer.toString(100 - mixerBendingSlider.getValue()) + "%");
        meridiansLabelLeft.setText(Integer.toString(100 - mixerMeridiansSlider.getValue()) + "%");

        lengthLabelRight.setText(Integer.toString(mixerLengthSlider.getValue()) + "%");
        distanceLabelRight.setText(Integer.toString(mixerDistanceSlider.getValue()) + "%");
        bendingLabelRight.setText(Integer.toString(mixerBendingSlider.getValue()) + "%");
        meridiansLabelRight.setText(Integer.toString(mixerMeridiansSlider.getValue()) + "%");

        if (liveUpdate || ((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            applyFlexMixerSettings();
            mapComponent.addUndo("Change to Flex Mixer");
        }
    }//GEN-LAST:event_flexMixerStateChanged

    private void flexMixerToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flexMixerToggleButtonActionPerformed
        model.flexMixProjectionToForeground();
        switchMethodGUI("flexMixerCard", "Switch to Flex Mixer", true);
        writeFlexMixerGUI();
    }//GEN-LAST:event_flexMixerToggleButtonActionPerformed

    private void blendingSizeSlidercombinerSliderChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_blendingSizeSlidercombinerSliderChanged
        if (updatingGUI) {
            return;
        }
        LatitudeMixerProjection p = model.getLatitudeMixerProjection();
        sizeLabel.setText(blendingSizeSlider.getValue() + "%");
        if (liveUpdate || ((JSlider) (evt.getSource())).getValueIsAdjusting() == false) {
            readLatitudeMixerGUI();
            mapComponent.addUndo("Size of Polar Projection");
            showDesignProjection();
        }
    }//GEN-LAST:event_blendingSizeSlidercombinerSliderChanged

    private void latitudeMixerToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_latitudeMixerToggleButtonActionPerformed
        model.combinedProjectionToForeground();
        switchMethodGUI("latitudeMixerCard", "Switch to Latitude Mixer", true);
        writeLatitudeMixerGUI();
    }//GEN-LAST:event_latitudeMixerToggleButtonActionPerformed

    private void flexSlidersToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flexSlidersToggleButtonActionPerformed

        // reset flex projection to the current mix projection
        Projection previousProjection = model.getDesignProjection();
        model.getFlexProjectionModel().reset(previousProjection);
        writeFlexSliderGUI();

        model.flexProjectionToForeground();
        switchMethodGUI("flexSlidersCard", "Switch to Flex Sliders", true);
        writeFlexSliderGUI();
    }//GEN-LAST:event_flexSlidersToggleButtonActionPerformed

    private void mixerComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_mixerComboBoxItemStateChanged
        if (updatingGUI || evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) {
            return;
        }

        boolean proj1 = "1".equals(((Component) (evt.getSource())).getName());

        // find the selected projection
        String projName = (String) evt.getItem();
        final Projection proj;
        if (projName.startsWith(ProjectionsManager.SELECT_FLEX_FILE_STRING)) {
            // load flex projection from file
            proj = loadFlexProjection();

            // make sure the loaded projection is supported by the mixer projection
            Projection p = model.getDesignProjection();
            if (p instanceof AbstractMixerProjection) {
                AbstractMixerProjection mixP = (AbstractMixerProjection) p;
                if (!mixP.canMix(proj)) {
                    String msg = "The selected projection cannot be mixed.";
                    String title = "Incompatible Projection";
                    JOptionPane.showMessageDialog(GUIUtil.getOwnerFrame(this),
                            msg, title, JOptionPane.ERROR_MESSAGE);
                    try {
                        updatingGUI = true;
                        mixerComboBox1.setSelectedItem(mixP.getProjection1().toString());
                        mixerComboBox2.setSelectedItem(mixP.getProjection2().toString());
                        return;
                    } finally {
                        updatingGUI = false;
                    }
                }
            }
        } else {
            proj = ProjectionsManager.getProjection(projName);
        }

        // pass the new projection to the model
        if (proj1) {
            model.setMixerProjection1(proj);
        } else {
            model.setMixerProjection2(proj);
        }

        // update the maps and the GUI
        updateMixerMap(proj, proj1 ? mixerMap1 : mixerMap2);
        showDesignProjection();
        writeVerticalScaleGUI();
        writeMethodGUI();
        updateDistortionIndicesAndInformListeners();
        mapComponent.showAll();
    }//GEN-LAST:event_mixerComboBoxItemStateChanged

    private void scaleOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleOKButtonActionPerformed
        scaleDialog.setVisible(false);
    }//GEN-LAST:event_scaleOKButtonActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        Properties props = PropertiesLoader.loadProperties("ika.app.Application.properties");
        String url = props.getProperty("HelpSizeWebPage");
        ika.utils.BrowserLauncherWrapper.openURL(url);
    }//GEN-LAST:event_helpButtonActionPerformed

    private void verticalScaleSlidersliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_verticalScaleSlidersliderStateChanged
        if (updatingGUI) {
            return;
        }

        double vScale = verticalScaleSlider.getValue() / SLIDER_SCALE;
        try {
            updatingGUI = true;
            verticalScaleFormattedTextField.setValue(new Double(vScale));
        } finally {
            updatingGUI = false;
        }

        if (verticalScaleSlider.getValueIsAdjusting() == false) {
            model.getDesignProjection().setVerticalScale(vScale);
            updateDistortionIndicesAndInformListeners();
            showDesignProjection();
            mapComponent.showAll();
            mapComponent.addUndo("Vertical Scale");
        }
    }//GEN-LAST:event_verticalScaleSlidersliderStateChanged

    private double readVerticalScaleField() throws ParseException {
        verticalScaleFormattedTextField.commitEdit();
        return ((Number) verticalScaleFormattedTextField.getValue()).doubleValue();
    }

    private void verticalScaleFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_verticalScaleFormattedTextFieldPropertyChange
        if (!updatingGUI && "value".equals(evt.getPropertyName())) {
            try {
                updatingGUI = true;
                double vScale = readVerticalScaleField();
                verticalScaleSlider.setValue((int) Math.round(vScale * SLIDER_SCALE));
                model.getDesignProjection().setVerticalScale(vScale);
            } catch (ParseException ex) {
            } finally {
                updatingGUI = false;
            }
            updateDistortionIndicesAndInformListeners();
            showDesignProjection();
            mapComponent.showAll();
            mapComponent.addUndo("Vertical Scale");
        }
    }//GEN-LAST:event_verticalScaleFormattedTextFieldPropertyChange

    private void poleDirectionFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_poleDirectionFormattedTextFieldPropertyChange

        if (updatingGUI || !"value".equals(evt.getPropertyName())) {
            return;
        }

        Object v = poleDirectionFormattedTextField.getValue();
        double dir = ((Number) v).doubleValue();
        int min = poleDirectionSlider.getMinimum();
        int max = poleDirectionSlider.getMaximum();
        if (dir < min) {
            poleDirectionFormattedTextField.setValue(new Double(min));
            return;
        }
        if (dir > max) {
            poleDirectionFormattedTextField.setValue(new Double(max));
            return;
        }

        // update the slider
        try {
            updatingGUI = true;
            poleDirectionSlider.setValue((int) Math.round(dir));
        } finally {
            updatingGUI = false;
        }

        // update the flexModel
        if (poleDirectionSlider.getValueIsAdjusting() == false) {
            model.getFlexProjectionModel().setMeridiansPoleDirection(dir);
            updateDistortionIndicesAndInformListeners();
        }
    }//GEN-LAST:event_poleDirectionFormattedTextFieldPropertyChange

    private void centralMeridianFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_centralMeridianFieldPropertyChange
        if (updatingGUI || !"value".equals(evt.getPropertyName())) {
            return;
        }
        try {
            updatingGUI = true;
            if ("value".equals(evt.getPropertyName())) {
                Object v = centralMeridianField.getValue();
                double lon0 = ((Number) v).doubleValue();
                while (lon0 > 180) {
                    lon0 -= 360;
                }
                while (lon0 < -180) {
                    lon0 += 360;
                }

                // update the slider
                lon0Slider.setValue((int) Math.round(lon0));

                // update the model and inform listeners
                model.setCentralMeridian(lon0);
                Projection foreProj = model.getDesignProjection();
                designProjectionChangeListeners.announce().designProjectionChanged(foreProj);
            }
        } finally {
            updatingGUI = false;
        }
    }//GEN-LAST:event_centralMeridianFieldPropertyChange

    private void scaleFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_scaleFieldPropertyChange
        if (updatingGUI || !"value".equals(evt.getPropertyName())) {
            return;
        }

        // set sliders to values entered in text fields
        try {
            updatingGUI = true;
            if ("value".equals(evt.getPropertyName())) {
                double lon = ((Number) scaleLonField.getValue()).doubleValue();
                double lat = ((Number) scaleLatField.getValue()).doubleValue();
                double man = ((Number) scaleManualField.getValue()).doubleValue();

                scaleLonSlider.setValue((int) Math.round(lon));
                scaleLatSlider.setValue((int) Math.round(lat));
                scaleManualSlider.setValue((int) Math.round(man * SLIDER_SCALE));
            }
        } finally {
            updatingGUI = false;
        }

        readInternalScaleGUI();
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_scaleFieldPropertyChange

    private void makeCylindricalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeCylindricalMenuItemActionPerformed
        makeCylindricalProjection();
    }//GEN-LAST:event_makeCylindricalMenuItemActionPerformed

    private void sizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeButtonActionPerformed
        showSizeDialog();
    }//GEN-LAST:event_sizeButtonActionPerformed

    private void bendingSelectionChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bendingSelectionChanged
        if (evt.getStateChange() != ItemEvent.SELECTED || updatingGUI) {
            return;
        }

        showDesignProjection();
        final int curveID;
        if (evt.getItem() == cubicBendingRadioButtonMenuItem) {
            curveID = FlexProjectionModel.CUBIC_CURVE;
        } else if (evt.getItem() == quadraticBendingRadioButtonMenuItem) {
            curveID = FlexProjectionModel.QUADRATIC_CURVE;
        } else {
            curveID = FlexProjectionModel.COSINE_CURVE;
        }

        updatingGUI = true;
        try {
            bendingComboBox.setSelectedIndex(curveID);
        } finally {
            updatingGUI = false;
        }

        model.getFlexProjectionModel().setCurveShape(curveID);
        updateDistortionIndicesAndInformListeners();
    }//GEN-LAST:event_bendingSelectionChanged

    /**
     * Reads the weights for mixing the two projections, mixes the projections,
     * and passes the mix to the main model.
     */
    private void applyFlexMixerSettings() {

        if (updatingGUI) {
            return;
        }

        // read the mixing weights
        double lengthW = mixerLengthSlider.getValue() / 100d;
        double distanceW = mixerDistanceSlider.getValue() / 100d;
        double bendingW = mixerBendingSlider.getValue() / 100d;
        double meridiansW = mixerMeridiansSlider.getValue() / 100d;

        FlexMixProjection p = model.getFlexMixProjection();
        p.setLengthW(lengthW);
        p.setDistanceW(distanceW);
        p.setBendingW(bendingW);
        p.setMeridiansW(meridiansW);
        p.initialize();

        showDesignProjection();
        updateDistortionIndicesAndInformListeners();
    }

    public void switchDisplay() {
        if (mainTabs.getSelectedIndex() == 0) {
            mainTabs.setSelectedIndex(1);
        } else {
            mainTabs.setSelectedIndex(0);
        }
    }

    private int getCurveInterpolation() {
        if (linearCurveToggleButton.isSelected()) {
            return LINEARCURVE;
        } else if (peakCurveToggleButton.isSelected()) {
            return PEAKCURVE;
        } else if (roundCurveToggleButton.isSelected()) {
            return BELLCURVE;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void setDistortionProfilesManager(DistortionProfilesManager distortionProfilesManager) {
        this.distortionProfilesManager = distortionProfilesManager;
        addFlexListener(distortionProfilesManager);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptableAreaOptionsButton;
    private javax.swing.ButtonGroup adjustButtonGroup;
    private javax.swing.JRadioButton adjustHeightRadioButton;
    private javax.swing.JLabel adjustLabel;
    private javax.swing.JRadioButton adjustNoRadioButton;
    private javax.swing.JRadioButton adjustWidthRadioButton;
    private javax.swing.JComboBox angularIsolinesEquidistanceComboBox;
    private javax.swing.JLabel angularIsolinesEquidistanceLabel;
    private javax.swing.JComboBox arealIsolinesEquidistanceComboBox;
    private javax.swing.JLabel arealIsolinesEquidistanceLabel;
    private javax.swing.ButtonGroup bendingButtonGroup;
    private javax.swing.JComboBox bendingComboBox;
    private javax.swing.JPanel bendingInfoPanel;
    private javax.swing.JLabel bendingLabel;
    private javax.swing.JLabel bendingLabelLeft;
    private javax.swing.JLabel bendingLabelRight;
    private javax.swing.JMenu bendingMenu;
    private javax.swing.JPanel bendingOptionsPanel;
    private javax.swing.JPanel bendingPanel;
    private javax.swing.JLabel bendingParallelsLabel;
    private javax.swing.JLabel bendingShapeLabel;
    private javax.swing.JPanel bendingSlidersPanel;
    private javax.swing.JSlider blendingLatitudeSlider;
    private javax.swing.JCheckBox blendingScaleCheckBox;
    private javax.swing.JSlider blendingSizeSlider;
    private javax.swing.JSlider blendingToleranceSlider;
    private javax.swing.JFormattedTextField centralMeridianField;
    private javax.swing.JRadioButtonMenuItem cosineBendingRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem cubicBendingRadioButtonMenuItem;
    private javax.swing.JLabel currentSizeLabel;
    private javax.swing.ButtonGroup curveShapeButtonGroup;
    private javax.swing.JLabel curveShapeLabel;
    private javax.swing.JToolBar curveShapeToolBar;
    private javax.swing.JPanel distanceInfoPanel;
    private javax.swing.JLabel distanceLabel;
    private javax.swing.JLabel distanceLabelLeft;
    private javax.swing.JLabel distanceLabelRight;
    private javax.swing.JPanel distancePanel;
    private javax.swing.JPanel distanceSlidersPanel;
    private javax.swing.JMenuItem eliminateShapeDistortionAtOriginMenuItem;
    private javax.swing.JLabel flexMixProjection1Label;
    private javax.swing.JLabel flexMixProjection2Label;
    private javax.swing.JToggleButton flexMixerToggleButton;
    private ika.gui.MenuToggleButton flexOptionsButton;
    private javax.swing.JPanel flexProjectionsNamesPanel;
    private javax.swing.JPanel flexSlidersPanel;
    private javax.swing.JTabbedPane flexSlidersTabs;
    private javax.swing.JToggleButton flexSlidersToggleButton;
    private javax.swing.JPanel flexTab;
    private javax.swing.JComboBox graticuleComboBox;
    private javax.swing.JLabel graticuleLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JToggleButton latitudeMixerToggleButton;
    private javax.swing.JLabel latitudeProjection1Label;
    private javax.swing.JLabel latitudeProjection2Label;
    private javax.swing.JPanel lengthInfoPanel;
    private javax.swing.JLabel lengthLabelLeft;
    private javax.swing.JLabel lengthLabelRight;
    private javax.swing.JPanel lengthPanel;
    private javax.swing.JPanel lengthSlidersPanel;
    private javax.swing.JToggleButton linearCurveToggleButton;
    private javax.swing.JLabel linkSlidersLabel;
    private javax.swing.JSpinner linkSpinner;
    private javax.swing.JSlider lon0Slider;
    private javax.swing.JTabbedPane mainTabs;
    private javax.swing.JMenuItem makeCylindricalMenuItem;
    private javax.swing.JLabel meanLabel1;
    private javax.swing.JLabel meanLabel2;
    private javax.swing.JLabel meanProjection1Label;
    private javax.swing.JLabel meanProjection2Label;
    private javax.swing.JSlider meanSlider;
    private javax.swing.JRadioButton meridianAngularRadioButton;
    private javax.swing.ButtonGroup meridianCurvatureButtonGroup;
    private javax.swing.JLabel meridianCurvatureLabel;
    private javax.swing.JRadioButton meridianSmoothRadioButton;
    private javax.swing.JPanel meridiansDirectionPanel;
    private javax.swing.JLabel meridiansDistributionLabel;
    private javax.swing.JPanel meridiansInfoPanel;
    private javax.swing.JLabel meridiansLabel;
    private javax.swing.JLabel meridiansLabelLeft;
    private javax.swing.JLabel meridiansLabelRight;
    private javax.swing.JPanel meridiansPanel;
    private javax.swing.JPanel meridiansSlidersPanel;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JPanel methodPanel;
    private javax.swing.JPanel mixPanel;
    private javax.swing.JSlider mixerBendingSlider;
    private javax.swing.JComboBox mixerComboBox1;
    private javax.swing.JComboBox mixerComboBox2;
    private javax.swing.JPanel mixerControlsPanel;
    private javax.swing.JSlider mixerDistanceSlider;
    private javax.swing.JSlider mixerLengthSlider;
    private ika.gui.MapComponent mixerMap1;
    private ika.gui.MapComponent mixerMap2;
    private javax.swing.JPanel mixerMapsPanel;
    private javax.swing.JSlider mixerMeridiansSlider;
    private javax.swing.JPanel mixerPanel;
    private javax.swing.JPanel mixerProjection1Panel;
    private javax.swing.JPanel mixerProjection2Panel;
    private javax.swing.JMenuItem normalizeMenuItem;
    private javax.swing.JPopupMenu optionsPopupMenu;
    private javax.swing.JToggleButton peakCurveToggleButton;
    private javax.swing.JCheckBox poleDirectionCheckBox;
    private javax.swing.JFormattedTextField poleDirectionFormattedTextField;
    private javax.swing.JSlider poleDirectionSlider;
    private javax.swing.JMenuItem polesAndEquatorMenuItem;
    private javax.swing.JComboBox projectionComboBox;
    private javax.swing.JRadioButtonMenuItem quadraticBendingRadioButtonMenuItem;
    private javax.swing.JMenuItem removeBendingMenuItem;
    private javax.swing.JMenuItem removeDistanceMenuItem;
    private javax.swing.JMenuItem removeMeridiansMenuItem;
    private javax.swing.JMenuItem resetMenuItem;
    private javax.swing.JToggleButton roundCurveToggleButton;
    private javax.swing.JRadioButton scaleAreaOfGlobeRadioButton;
    private javax.swing.ButtonGroup scaleButtonGroup;
    private javax.swing.JDialog scaleDialog;
    private javax.swing.JFormattedTextField scaleLatField;
    private javax.swing.JSlider scaleLatSlider;
    private javax.swing.JFormattedTextField scaleLonField;
    private javax.swing.JSlider scaleLonSlider;
    private javax.swing.JFormattedTextField scaleManualField;
    private javax.swing.JRadioButton scaleManualRadioButton;
    private javax.swing.JSlider scaleManualSlider;
    private javax.swing.JRadioButton scaleMinimumAreaDistRadioButton;
    private javax.swing.JRadioButton scalePointRadioButton;
    private javax.swing.JCheckBox showAcceptableAreaCheckBox;
    private javax.swing.JCheckBox showAngularIsolinesCheckBox;
    private javax.swing.JCheckBox showArealIsolinesCheckBox;
    private javax.swing.JCheckBox showFlexCheckBox;
    private javax.swing.JCheckBox showGraticuleCheckBox;
    private javax.swing.JCheckBox showSecondProjectionCheckBox;
    private javax.swing.JCheckBox showTissotCheckBox;
    private javax.swing.JToggleButton simpleMixerToggleButton;
    private javax.swing.JButton sizeButton;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JPanel sizePanel;
    private javax.swing.JPanel southPanel;
    private javax.swing.JComboBox tissotComboBox;
    private javax.swing.JLabel tissotLabel;
    private javax.swing.JLabel tissotScaleLabel;
    private javax.swing.JSlider tissotScaleSlider;
    private javax.swing.JLabel toleranceLabel;
    private javax.swing.JFormattedTextField verticalScaleFormattedTextField;
    private javax.swing.JSlider verticalScaleSlider;
    // End of variables declaration//GEN-END:variables
}
