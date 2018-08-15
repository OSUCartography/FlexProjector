/*
 * FlexProjectorWindow.java
 *
 * Created on 23. August 2004, 20:50
 */
package ika.gui;

import com.jhlabs.map.Ellipsoid;
import com.jhlabs.map.proj.EquidistantCylindricalProjection;
import com.jhlabs.map.proj.Projection;
import ika.geo.*;
import ika.geo.FlexProjectorModel.DisplayModel;
import ika.geo.clipboard.GeoTransferable;
import ika.geoexport.*;
import ika.geoimport.*;
import ika.map.tools.*;
import ika.proj.DesignProjection;
import ika.proj.FlexMixProjection;
import ika.proj.FlexProjection;
import ika.proj.ProjectionsManager;
import ika.proj.SerializableProjection;
import ika.utils.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * The main document window.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class FlexProjectorWindow extends MainWindow
        implements RenderParamsProvider {

    ProjDistortionTable distortionTable = null;

    private DistortionProfilesManager distortionProfilesManager = null;

    private CurvesManager curvesManager = null;

    private ProjectionBrewerPanel projectionBrewerPanel;

    /**
     * Creates new form FlexProjectorWindow
     */
    public FlexProjectorWindow() {
        // build the GUI
        this.initComponents();
        projectionBrewerPanel = new ProjectionBrewerPanel();
        getContentPane().add(projectionBrewerPanel, java.awt.BorderLayout.EAST);
        this.initMenus();
    }

    @Override
    protected boolean init() {

        Action showAllAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapComponent.showAll();
            }
        };
        ika.gui.MenuUtils.registerMenuShortcut("showAllAction1", showAllAction,
                KeyEvent.VK_0, this);
        ika.gui.MenuUtils.registerMenuShortcut("showAllAction2", showAllAction,
                KeyEvent.VK_NUMPAD0, this);

        Action zoomInAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapComponent.zoomIn();
            }
        };
        ika.gui.MenuUtils.registerMenuShortcut("zoomInAction", zoomInAction,
                KeyEvent.VK_PLUS | KeyEvent.SHIFT_MASK, this);

        final FlexProjectorModel flexProjectorModel = new FlexProjectorModel();

        // pass a parent GeoSet to the MapComponent
        this.mapComponent.setGeoSet(flexProjectorModel);

        // set background color of MapComponent
        this.mapComponent.setBackground(FlexProjectorPreferencesPanel.getMapBackgroundColor());

        // no interpolation for rendering raster images
        this.mapComponent.setImageRenderingHint(
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        this.projectionBrewerPanel.setModel(flexProjectorModel);

        // add event listener that updates menus when the design projection 
        // changes, and updates the enabled state of the flex curve tab, which
        // is only useful for flex projections.
        projectionBrewerPanel.addFlexListener(new ProjectionBrewerPanel.DesignProjectionChangeListener() {
            @Override
            public void designProjectionChanged(Projection p) {
                updateAllMenus();

                // update flex curve tab
                boolean isFlexProjection = p instanceof FlexProjection;
                int id = infoTabbedPane.indexOfComponent(curvesPanel);
                infoTabbedPane.setEnabledAt(id, isFlexProjection);
                if (!isFlexProjection && infoTabbedPane.getSelectedIndex() == id) {
                    infoTabbedPane.setSelectedIndex(0);
                }
            }
        });

        // add listener to map that displays the coordinates of the current
        // mouse position
        this.mapComponent.addMouseMotionListener(new MapToolMouseMotionListener() {
            @Override
            public void mouseMoved(Point2D.Double xy, MapComponent mapComponent) {
                Projection proj = getProjectionClone();
                if (xy == null || proj == null) {
                    coordinatesInfoPanel.setCoordinates(Double.NaN, Double.NaN,
                            Double.NaN, Double.NaN);
                    coordinatesInfoPanel.setDistortion(Double.NaN, Double.NaN);
                    return;
                }
                try {
                    proj = (Projection) proj.clone();
                    proj.initialize();

                    // normalize the flex projection before projecting in the 
                    // inverse direction.
                    if (proj instanceof FlexProjection) {
                        ((FlexProjection) proj).getModel().normalize();
                    }

                    Point2D.Double lonLat = new Point2D.Double();
                    proj.inverseTransform(xy, lonLat);
                    coordinatesInfoPanel.setCoordinates(lonLat.x, lonLat.y, xy.x, xy.y);

                    // distortion
                    ika.proj.ProjectionFactors pf = new ika.proj.ProjectionFactors();
                    pf.compute(proj, Math.toRadians(lonLat.x), Math.toRadians(lonLat.y), 1e-5);
                    coordinatesInfoPanel.setDistortion(pf.s, Math.toDegrees(pf.omega));
                } catch (Exception e) {
                    coordinatesInfoPanel.setCoordinates(Double.NaN, Double.NaN,
                            Double.NaN, Double.NaN);
                    coordinatesInfoPanel.setDistortion(Double.NaN, Double.NaN);
                }
            }
        });

        // add listener to map that displays the scale of the map
        this.mapComponent.addScaleChangeHandler(new ScaleChangeHandler() {
            @Override
            public void scaleChanged(MapComponent mapComponent,
                    double currentMapScaleFactor, double currentMapScaleNumber) {
                coordinatesInfoPanel.setScale(currentMapScaleNumber);
            }
        });

        // specify the format of displayed coordinates
        this.mapComponent.setCoordinateFormatter(new CoordinateFormatter("###,##0.#", "###,##0.#", 1));

        // register this object so that rendering parameters can be customized.
        this.mapComponent.setRenderParamsProvider(this);

        // add a MapEventListener: When the map changes, the dirty
        // flag is set and the Save menu item updated.
        MapEventListener mel = new MapEventListener() {
            @Override
            public void mapEvent(MapEvent evt) {
                if (mapComponent.canUndo()) {
                    setDocumentDirty();
                } else {
                    setDocumentClean();
                }
                updateAllMenus();
                FlexProjectorModel model = (FlexProjectorModel) mapComponent.getGeoSet();
                projectionBrewerPanel.setModel(model);
            }
        };
        // register the MapEventListener to be informed whenever the map changes.
        GeoSetBroadcaster.addMapEventListener(mel, this.mapComponent.getGeoSet());

        // set the initial tool
        this.mapComponent.setMapTool(new PanTool(this.mapComponent));

        // maximise the size of this window. Fill the primary screen.
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // setup the distortion table
        this.distortionTable = new ProjDistortionTable(flexProjectorModel,
                this.projectionBrewerPanel);
        this.projectionBrewerPanel.addFlexListener(this.distortionTable);
        this.distortionTableScrollPane.setViewportView(this.distortionTable);
        this.mainSplitPane.setDividerLocation(0.8);
        this.distortionTableScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER,
                this.tableInfoButton);

        // setup distortion profiles
        this.distortionProfilesManager = new DistortionProfilesManager(
                vertProfilesMap, horProfilesMap, flexProjectorModel);
        Projection bgrdProj = flexProjectorModel.getDisplayModel().projection;
        this.distortionProfilesManager.setBackgroundProjection(bgrdProj);
        this.vertProfilesMap.getPageFormat().setVisible(false);
        this.horProfilesMap.getPageFormat().setVisible(false);
        distortionProfilesManager.setLat(this.latProfileSlider.getValue());
        distortionProfilesManager.setLon(this.lonProfileSlider.getValue());
        this.projectionBrewerPanel.setDistortionProfilesManager(this.distortionProfilesManager);

        // setup curves manager
        this.curvesManager = new CurvesManager(curvesMap);

        FlexProjection flexProj = projectionBrewerPanel.getModel().getFlexProjection();
        this.curvesManager.setFlexProjection(flexProj);
        this.curvesMap.getPageFormat().setVisible(false);
        this.projectionBrewerPanel.addFlexListener(this.curvesManager);

        // layout this window after the divider of split panes has been set and
        // after the window has been maximized
        this.validate();

        // compute the profiles after the window has been layed out
        distortionProfilesManager.updateDistortionProfiles(false, flexProjectorModel.getDesignProjection());
        distortionProfilesManager.updateDistortionProfiles(true, flexProjectorModel.getDesignProjection());

        // add a window listener that updates the menus when the
        // state of the window changes (minimized, close, focus lost, activated, etc.)
        WindowListener windowListener = new WindowListener() {

            public void windowChanged(WindowEvent e) {
                FlexProjectorWindow mainWindow = (FlexProjectorWindow) e.getWindow();
                mainWindow.updateAllMenus();
                MacWindowsManager.updateFramelessMenuBar();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                this.windowChanged(e);
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
                this.windowChanged(e);
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                this.windowChanged(e);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                this.windowChanged(e);
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                this.windowChanged(e);
            }
        };
        this.addWindowListener(windowListener);

        // intialize the page format, which will be used to export to graphics
        // file formats.
        PageFormat pageFormat = this.mapComponent.getPageFormat();
        pageFormat.setAutomatic(true);
        pageFormat.setUnitPixels(false);
        pageFormat.setPageScale(100000000); // 100 Mio.
        pageFormat.setVisible(false);

        // setup undo/redo
        mapComponent.registerUndoMenuItems(this.undoMenuItem, redoMenuItem);
        FlexUndoManager um = new FlexUndoManager(flexProjectorModel,
                this.projectionBrewerPanel);
        mapComponent.setMapUndoManager(um);

        // pass map to panel for undo/redo support
        this.projectionBrewerPanel.setMap(this.mapComponent);

        // load default coastline data
        Properties props = PropertiesLoader.loadProperties("ika.app.Application.properties");
        String mapData = props.getProperty("MapData");
        java.net.URL url = FlexProjectorWindow.class.getResource(mapData);
        importData(url, true);

        // initialize the undo/redo manager with the current map content.
        this.mapComponent.resetUndo();

        this.getRootPane().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                windowModifiedPropertyChange(evt);
            }
        });

        this.setDocumentClean();

        // add the distortion table as a listener to the QModel: when the user
        // changes the settings for the Q factor, the table is updated.
        flexProjectorModel.getDisplayModel().qModel.addQListener(distortionTable);

        // Asynchronously construct the distortion parameters for all projections.
        // This starts a separate thread to fill the distortion table.
        new ika.proj.TableFiller(distortionTable, flexProjectorModel).execute();

        projectionBrewerPanel.writeDisplayGUI();
        return true;

    }

    private void importData(final java.net.URL url,
            final boolean replacePreviousData) {

        MapDataReceiver receiver = new MapDataReceiver(mapComponent) {
            @Override
            protected GeoSet getDestinationGeoSet() {
                FlexProjectorModel model
                        = (FlexProjectorModel) mapComponent.getGeoSet();
                GeoSet destinationGeoSet = model.getUnprojectedData();
                if (replacePreviousData) {
                    destinationGeoSet.removeAllGeoObjects();
                }
                GeoSet subSet = new GeoMap(); // GeoMap can have attributes attached
                subSet.setName(url.toString());
                destinationGeoSet.add(subSet);
                return subSet;
            }

            private void updateModel(boolean added) {
                if (added) {
                    FlexProjectorModel model
                            = (FlexProjectorModel) mapComponent.getGeoSet();
                    model.designProjectionChanged(model.getDesignProjection());
                    mapComponent.showAll();
                }
            }

            @Override
            public boolean add(GeoSet geoSet) {
                boolean added = super.add(geoSet);
                this.updateModel(added);
                return added;
            }

            @Override
            public boolean add(ika.table.TableLink tableLink) {
                boolean added = super.add(tableLink);
                updateModel(added);
                return added;
            }
        };

        try {
            GeoImporter importer = GeoImporter.findGeoImporter(url);
            // importer.setProgressIndicator(new SwingProgressIndicator(this, "Load Data", null, true));
            importer.read(url, receiver, GeoImporter.SAME_THREAD); // FIXME NEW_THREAD);
        } catch (Exception exc) {
            exc.printStackTrace();
            ika.utils.ErrorDialog.showErrorDialog("Could not load the data. "
                    + "The format may not be supported.",
                    "Data Loading Error");
        }
    }

    /**
     * Mac OS X and Windows specific initialization of the menus
     */
    private void initMenus() {
        if (ika.utils.Sys.isMacOSX()) {

            // remove exit menu item on Mac OS X
            this.fileMenu.remove(this.exitMenuSeparator);
            this.fileMenu.remove(this.exitMenuItem);
            this.fileMenu.validate();

            // remove window info menu item on Mac OS X
            this.menuBar.remove(this.winHelpMenu);

            // remove preferences menu item on Mac OS X
            //this.editMenu.remove(this.preferencesSeparator);
            //this.editMenu.remove(this.preferencesMenuItem);
            //this.editMenu.validate();
        } else if (ika.utils.Sys.isWindows()) {

            // remove fileSeparator and closeMenuItem to make the file menu windows style
            fileMenu.remove(fileSeparator);
            fileMenu.remove(closeMenuItem);

            // remove mac help menu item on Windows
            this.menuBar.remove(this.macHelpMenu);

        }

        this.menuBar.validate();
    }

    /**
     * Customize the passed defaultRenderParams. This implementation does not
     * alter the passed parameters.
     */
    public RenderParams getRenderParams(RenderParams defaultRenderParams) {
        return defaultRenderParams;
    }

    /**
     * Return a GeoMap that can be stored in an external file.
     *
     * @return The document content.
     */
    @Override
    protected byte[] getDocumentData() {
        FlexProjectorModel model = (FlexProjectorModel) mapComponent.getGeoSet();
        SerializableProjection p = model.getDesignProjection();
        return p.serializeToString().getBytes();
    }

    /**
     * Restore the document content from a passed GeoMap.
     *
     * @param data The document content.
     */
    @Override
    protected void setDocumentData(byte[] data) throws Exception {
        DesignProjection p = DesignProjection.factory(new String(data));
        FlexProjectorModel model = (FlexProjectorModel) mapComponent.getGeoSet();
        model.setDesignProjection(p);

        projectionBrewerPanel.updateDistortionIndicesAndInformListeners();
        projectionBrewerPanel.writeMethodGUI();
    }

    /**
     * The preferences changed. The settings are used to generate the graphics,
     * so we need to regenerate the graphics in all windows.
     */
    public static void updateAfterPreferencesChange() {
        final int windowsCount = MainWindow.windows.size();
        for (int i = windowsCount - 1; i >= 0; i--) {
            FlexProjectorWindow w = (FlexProjectorWindow) MainWindow.windows.get(i);
            // pass background color of map to map component
            w.mapComponent.setBackground(FlexProjectorPreferencesPanel.getMapBackgroundColor());
            w.projectionBrewerPanel.updateDistortionIndicesAndInformListeners();
            w.distortionTable.qAreaAcceptanceChanged();
        }
    }

    /**
     * Update all menus of this window.
     */
    private void updateAllMenus() {
        // Only update the menu items if this frame is visible. 
        // This avoids menu items being enabled that will be detached from 
        // this frame and will be attached to a utility frame or will be 
        // displayed when no frame is visible on Mac OS X.
        if (this.isVisible()) {
            this.updateFileMenu();
            this.updateEditMenu();
            this.updateViewMenu();
            MainWindow.updateWindowMenu(this.windowMenu, this);
        }
    }

    /**
     * Update the enabled/disabled state of the items in the file menu.
     */
    private void updateFileMenu() {
        FlexProjectorModel model = this.projectionBrewerPanel.getModel();

        this.closeMenuItem.setEnabled(true);
        this.saveMenuItem.setEnabled(this.isDocumentDirty());
        this.saveAsMenuItem.setEnabled(true);
        this.webImportMenuItem.setEnabled(true);
        this.webDownloadMenuItem.setEnabled(true);

        this.exportDistortionTableMenuItem.setEnabled(true);

        this.projectShapeMenuItem.setEnabled(true);
        this.projectImageMenuItem.setEnabled(true);
        this.projectGridMenuItem.setEnabled(true);

        this.projectImageToGeographicMenuItem.setEnabled(true);
    }

    /**
     * Update the enabled/disabled state of the items in the edit menu.
     */
    private void updateEditMenu() {

        FlexProjectorModel model = this.projectionBrewerPanel.getModel();
        Projection p = model.getDesignProjection();

        boolean mapHasSelectedObj = mapComponent.hasSelectedGeoObjects();
        boolean mapHasVisibleObjects = mapComponent.hasVisibleGeoObjects();
        boolean isFlexProj = p instanceof FlexProjection && !(p instanceof FlexMixProjection);
        // undo and redo menu items are handled by the Undo manager.

        this.deleteMenuItem.setEnabled(mapHasSelectedObj);
        this.copyMenuItem.setEnabled(mapHasSelectedObj);
        this.cutMenuItem.setEnabled(mapHasSelectedObj);
        this.pasteMenuItem.setEnabled(GeoTransferable.isClipboardFull());
        this.selectAllMenuItem.setEnabled(mapHasVisibleObjects);
        this.deselectAllMenuItem.setEnabled(mapHasSelectedObj);

        this.resetProjectionMenuItem.setEnabled(mapHasVisibleObjects && isFlexProj);
        this.scaleMenuItem.setEnabled(mapHasVisibleObjects);

        this.qMenuItem.setEnabled(true);
    }

    /**
     * Update the enabled/disabled state of the items in the view menu.
     */
    private void updateViewMenu() {
        this.zoomInMenuItem.setEnabled(true);
        this.zoomOutMenuItem.setEnabled(true);
        this.showAllMenuItem.setEnabled(true);
        this.showPageCheckBoxMenuItem.setEnabled(true);
        this.showFlexProjectionMenuItem.setEnabled(true);
        this.showSecondProjectionMenuItem.setEnabled(true);
        this.toggleMenuItem.setEnabled(true);
        this.coastlineMenuItem.setEnabled(true);
        this.graticuleMenuItem.setEnabled(true);
        this.tissotMenuItem.setEnabled(true);
        this.switchDisplayMenuItem.setEnabled(true);
    }

    /**
     * Construct a box bounding the gratiule of a projection. A new GeoSet is
     * created that contains the passed GeoSet and a new GeoSet containing the
     * bounding box. The bounding box includes all elements of the passed GeoSet
     * and is projected with the passed projection.
     *
     * @param geoSet A set of features.
     * @param projection The projection to apply.
     * @return A new GeoSet containing the passed GeoSet and the bounding box.
     */
    private GeoSet addBoundingBox(GeoSet geoSet, Projection projection) {
        GeoSet boundingBoxGeoSet = new GeoSet();
        boundingBoxGeoSet.setName("Bounding Box");

        FlexProjectorModel model = this.projectionBrewerPanel.getModel();
        GeoPath boundingBox = model.constructBoundingBox(projection);
        if (boundingBox != null && geoSet != null) {
            boundingBoxGeoSet.add(boundingBox);
        }
        geoSet = geoSet.clone();
        geoSet.add(boundingBoxGeoSet);
        return geoSet;
    }

    private void exportMap() {

        GeoSetExporter exporter = GeoExportGUI.askExporter(this);
        if (exporter == null) {
            return;
        }

        exporter.setDisplayMapScale(mapComponent.getScaleFactor());
        GeoSet geoSet = mapComponent.getImportExportGeoSet();
        PageFormat pageFormat = mapComponent.getPageFormat();

        // add a box surrounding the graticule, not including the ellipses.
        if (exporter instanceof VectorGraphicsExporter) {
            geoSet = addBoundingBox(geoSet, getProjectionClone());
        }

        GeoExportGUI.export(exporter, geoSet, getTitle(), this, pageFormat, true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolBarButtonGroup = new javax.swing.ButtonGroup();
        tableInfoButton = new javax.swing.JButton();
        tableInfoButton.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
        distortionInfoPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        webImportPanel = new javax.swing.JPanel();
        webImportComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        webImportReplaceRadioButton = new javax.swing.JRadioButton();
        webImportAddRadioButton = new javax.swing.JRadioButton();
        webImportButtonGroup = new javax.swing.ButtonGroup();
        curvesButtonGroup = new javax.swing.ButtonGroup();
        centerPanel = new javax.swing.JPanel();
        navigationToolBar = new javax.swing.JToolBar();
        handToggleButton = new javax.swing.JToggleButton();
        zoomInToggleButton = new javax.swing.JToggleButton();
        zoomOutToggleButton = new javax.swing.JToggleButton();
        distanceToggleButton = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        showAllButton = new javax.swing.JButton();
        jSeparator15 = new javax.swing.JToolBar.Separator();
        coordinatesInfoPanel = new ika.gui.CoordinatesInfoPanel();
        mainSplitPane = new javax.swing.JSplitPane();
        mapComponent = new ika.gui.MapComponent();
        infoTabbedPane = new javax.swing.JTabbedPane();
        distortionTableScrollPane = new javax.swing.JScrollPane();
        distortionTableScrollPane.getViewport().setOpaque(false);
        distortionProfilesPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        distortionProfilesPanel.setOpaque(false);
        leftProfilePanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        leftProfilePanel.setOpaque(false);
        verticalProfileLabel = new javax.swing.JLabel();
        vertProfilesMap = new ika.gui.MapComponent();
        jPanel3 = new javax.swing.JPanel();
        lonProfileSlider = new javax.swing.JSlider();
        rightProfilePanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        rightProfilePanel.setOpaque(false);
        horProfilesMap = new ika.gui.MapComponent();
        horizontalProfileLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        latProfileSlider = new javax.swing.JSlider();
        curvesPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        curvesPanel.setOpaque(false);
        curvesMap = new ika.gui.MapComponent();
        curvesTopPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        curvesTopPanel.setOpaque(false);
        curvesToolBar = new javax.swing.JToolBar();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        curvesToolBar.setOpaque(false);
        curveLengthToggleButton = new javax.swing.JToggleButton();
        curveDistanceToggleButton = new javax.swing.JToggleButton();
        curveBendingToggleButton = new javax.swing.JToggleButton();
        curveMeridiansToggleButton = new javax.swing.JToggleButton();
        curvesBottomPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        curvesBottomPanel.setOpaque(false);
        jLabel4 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        fileSeparator = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        webImportMenuItem = new javax.swing.JMenuItem();
        webDownloadMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        exportMapMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        exportDistortionTableMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        projectGeographicToFlexMenu = new javax.swing.JMenu();
        projectShapeMenuItem = new javax.swing.JMenuItem();
        projectImageMenuItem = new javax.swing.JMenuItem();
        projectGridMenuItem = new javax.swing.JMenuItem();
        projectFlexToGeographicMenu = new javax.swing.JMenu();
        projectImageToGeographicMenuItem = new javax.swing.JMenuItem();
        exitMenuSeparator = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        selectAllMenuItem = new javax.swing.JMenuItem();
        deselectAllMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        resetProjectionMenuItem = new javax.swing.JMenuItem();
        scaleMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        qMenuItem = new javax.swing.JMenuItem();
        preferencesSeparator = new javax.swing.JSeparator();
        preferencesMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        showAllMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        showPageCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        showFlexProjectionMenuItem = new javax.swing.JMenuItem();
        showSecondProjectionMenuItem = new javax.swing.JMenuItem();
        toggleMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        coastlineMenuItem = new javax.swing.JMenuItem();
        graticuleMenuItem = new javax.swing.JMenuItem();
        tissotMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        switchDisplayMenuItem = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();
        minimizeMenuItem = new javax.swing.JMenuItem();
        zoomMenuItem = new javax.swing.JMenuItem();
        windowSeparator = new javax.swing.JSeparator();
        winHelpMenu = new javax.swing.JMenu();
        onlineHelpMenuItem = new javax.swing.JMenuItem();
        infoMenuItem = new javax.swing.JMenuItem();
        macHelpMenu = new javax.swing.JMenu();
        macOnlineHelpMenuItem = new javax.swing.JMenuItem();
        infoMenuItem1 = new javax.swing.JMenuItem();

        tableInfoButton.setText("?");
        tableInfoButton.setToolTipText("Click for additional information about the distortion table.");
        tableInfoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tableInfoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableInfoButtonActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Scale\nThe weighted mean error for overall scale distortion.\n\nScale Cont.\nThe weighted mean error for overall scale distortion. Continental areas only.\n\nAreal\nThe weighted mean error for areal distortion. 0 indicates equal-area projections.\n\nAreal Cont.\nThe weighted mean error for areal distortion. Continental areas only.\n\nAngular\nThe mean angular deformation index. 0 indicates conformal projections.\n\nAngular Cont.\nThe mean angular deformation index. Continental areas only.\n\nAcc.\nThe Acceptance index. The relative area with acceptable distortion properties. Double click the column header to change the acceptable distortion. \n\nAll continental values are computed for the standard central meridian at Greenwich.\n\n\nThe computation of distortion parameters is based on Frank Canters and Hugo Decleir (1989), \"The World in Perspective - A Directory of World Map Projections\", Wiley, Chichester, etc., 181 p.\n\nThe acceptance index is based on Capek, R. (2001). Which is the best projection for the world map? Proceedings of the 20th International Cartographic Conference ICC, Beijing.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout distortionInfoPanelLayout = new org.jdesktop.layout.GroupLayout(distortionInfoPanel);
        distortionInfoPanel.setLayout(distortionInfoPanelLayout);
        distortionInfoPanelLayout.setHorizontalGroup(
            distortionInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(distortionInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addContainerGap())
        );
        distortionInfoPanelLayout.setVerticalGroup(
            distortionInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(distortionInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                .addContainerGap())
        );

        webImportPanel.setLayout(new java.awt.GridBagLayout());

        webImportComboBox.setMaximumRowCount(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        webImportPanel.add(webImportComboBox, gridBagConstraints);

        jLabel5.setText("Select a Data Set to Load from www.flexprojector.com:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        webImportPanel.add(jLabel5, gridBagConstraints);

        webImportButtonGroup.add(webImportReplaceRadioButton);
        webImportReplaceRadioButton.setSelected(true);
        webImportReplaceRadioButton.setText("Replace Current Map Content");
        webImportReplaceRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        webImportPanel.add(webImportReplaceRadioButton, gridBagConstraints);

        webImportButtonGroup.add(webImportAddRadioButton);
        webImportAddRadioButton.setText("Add to Current Map Content");
        webImportAddRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        webImportPanel.add(webImportAddRadioButton, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setName(""); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeWindow(evt);
            }
        });

        centerPanel.setMinimumSize(new java.awt.Dimension(500, 494));
        centerPanel.setPreferredSize(new java.awt.Dimension(500, 592));
        centerPanel.setLayout(new java.awt.BorderLayout());

        toolBarButtonGroup.add(handToggleButton);
        handToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/Hand16x16.gif"))); // NOI18N
        handToggleButton.setSelected(true);
        handToggleButton.setToolTipText("Pan");
        handToggleButton.setPreferredSize(new java.awt.Dimension(24, 24));
        handToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handToggleButtonActionPerformed(evt);
            }
        });
        navigationToolBar.add(handToggleButton);

        toolBarButtonGroup.add(zoomInToggleButton);
        zoomInToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/ZoomIn16x16.gif"))); // NOI18N
        zoomInToggleButton.setToolTipText("Zoom In");
        zoomInToggleButton.setPreferredSize(new java.awt.Dimension(24, 24));
        zoomInToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInToggleButtonActionPerformed(evt);
            }
        });
        navigationToolBar.add(zoomInToggleButton);

        toolBarButtonGroup.add(zoomOutToggleButton);
        zoomOutToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/ZoomOut16x16.gif"))); // NOI18N
        zoomOutToggleButton.setToolTipText("Zoom Out");
        zoomOutToggleButton.setPreferredSize(new java.awt.Dimension(24, 24));
        zoomOutToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutToggleButtonActionPerformed(evt);
            }
        });
        navigationToolBar.add(zoomOutToggleButton);

        toolBarButtonGroup.add(distanceToggleButton);
        distanceToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/Ruler16x16.gif"))); // NOI18N
        distanceToggleButton.setToolTipText("Measure Distance and Angle");
        distanceToggleButton.setPreferredSize(new java.awt.Dimension(24, 24));
        distanceToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distanceToggleButtonActionPerformed(evt);
            }
        });
        navigationToolBar.add(distanceToggleButton);
        navigationToolBar.add(jSeparator5);

        showAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ika/icons/ShowAll20x14.png"))); // NOI18N
        showAllButton.setToolTipText("Zoom to Map Extent");
        showAllButton.setBorderPainted(false);
        showAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllButtonActionPerformed(evt);
            }
        });
        navigationToolBar.add(showAllButton);
        navigationToolBar.add(jSeparator15);

        coordinatesInfoPanel.setToolTipText("Coordinates of the current mouse position and scale of the map.");

        org.jdesktop.layout.GroupLayout coordinatesInfoPanelLayout = new org.jdesktop.layout.GroupLayout(coordinatesInfoPanel);
        coordinatesInfoPanel.setLayout(coordinatesInfoPanelLayout);
        coordinatesInfoPanelLayout.setHorizontalGroup(
            coordinatesInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 410, Short.MAX_VALUE)
        );
        coordinatesInfoPanelLayout.setVerticalGroup(
            coordinatesInfoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 26, Short.MAX_VALUE)
        );

        navigationToolBar.add(coordinatesInfoPanel);

        centerPanel.add(navigationToolBar, java.awt.BorderLayout.NORTH);

        mainSplitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setOneTouchExpandable(true);

        mapComponent.setBackground(new java.awt.Color(255, 255, 255));
        mapComponent.setInfoString(" ");
        mapComponent.setMinimumSize(new java.awt.Dimension(100, 200));
        mapComponent.setPreferredSize(new java.awt.Dimension(200, 200));
        mapComponent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mapComponentComponentResized(evt);
            }
        });
        mainSplitPane.setTopComponent(mapComponent);

        distortionTableScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        distortionTableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        distortionTableScrollPane.setOpaque(false);
        distortionTableScrollPane.setPreferredSize(new java.awt.Dimension(300, 300));
        infoTabbedPane.addTab("Distortion Table", distortionTableScrollPane);

        distortionProfilesPanel.setPreferredSize(new java.awt.Dimension(515, 200));
        distortionProfilesPanel.setLayout(new java.awt.GridLayout(1, 2, 3, 0));

        leftProfilePanel.setLayout(new java.awt.BorderLayout());

        verticalProfileLabel.setText("Vertical Profile");
        verticalProfileLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 1));
        verticalProfileLabel.setMinimumSize(new java.awt.Dimension(97, 22));
        verticalProfileLabel.setPreferredSize(new java.awt.Dimension(97, 22));
        leftProfilePanel.add(verticalProfileLabel, java.awt.BorderLayout.NORTH);

        vertProfilesMap.setBackground(new java.awt.Color(255, 255, 255));
        vertProfilesMap.setInfoString(" ");
        vertProfilesMap.setMinimumSize(new java.awt.Dimension(200, 100));
        vertProfilesMap.setPreferredSize(new java.awt.Dimension(400, 200));
        vertProfilesMap.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                profilesMapComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout vertProfilesMapLayout = new org.jdesktop.layout.GroupLayout(vertProfilesMap);
        vertProfilesMap.setLayout(vertProfilesMapLayout);
        vertProfilesMapLayout.setHorizontalGroup(
            vertProfilesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 538, Short.MAX_VALUE)
        );
        vertProfilesMapLayout.setVerticalGroup(
            vertProfilesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 214, Short.MAX_VALUE)
        );

        leftProfilePanel.add(vertProfilesMap, java.awt.BorderLayout.CENTER);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        lonProfileSlider.setBackground(new java.awt.Color(255, 255, 255));
        lonProfileSlider.setMajorTickSpacing(45);
        lonProfileSlider.setMaximum(180);
        lonProfileSlider.setMinorTickSpacing(15);
        lonProfileSlider.setPaintLabels(true);
        lonProfileSlider.setPaintTicks(true);
        lonProfileSlider.setValue(0);
        lonProfileSlider.setMaximumSize(new java.awt.Dimension(300, 54));
        lonProfileSlider.setMinimumSize(new java.awt.Dimension(220, 54));
        lonProfileSlider.setPreferredSize(new java.awt.Dimension(220, 54));
        lonProfileSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lonProfileSliderStateChanged(evt);
            }
        });
        jPanel3.add(lonProfileSlider);
        {
            JSlider slider = lonProfileSlider;
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

        leftProfilePanel.add(jPanel3, java.awt.BorderLayout.SOUTH);

        distortionProfilesPanel.add(leftProfilePanel);

        rightProfilePanel.setLayout(new java.awt.BorderLayout());

        horProfilesMap.setBackground(new java.awt.Color(255, 255, 255));
        horProfilesMap.setInfoString(" ");
        horProfilesMap.setMinimumSize(new java.awt.Dimension(200, 100));
        horProfilesMap.setPreferredSize(new java.awt.Dimension(400, 200));
        horProfilesMap.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                profilesMapComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout horProfilesMapLayout = new org.jdesktop.layout.GroupLayout(horProfilesMap);
        horProfilesMap.setLayout(horProfilesMapLayout);
        horProfilesMapLayout.setHorizontalGroup(
            horProfilesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 484, Short.MAX_VALUE)
        );
        horProfilesMapLayout.setVerticalGroup(
            horProfilesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 278, Short.MAX_VALUE)
        );

        rightProfilePanel.add(horProfilesMap, java.awt.BorderLayout.CENTER);

        horizontalProfileLabel.setText("Horizontal Profile");
        horizontalProfileLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 1));
        horizontalProfileLabel.setMinimumSize(new java.awt.Dimension(116, 22));
        horizontalProfileLabel.setPreferredSize(new java.awt.Dimension(116, 22));
        rightProfilePanel.add(horizontalProfileLabel, java.awt.BorderLayout.NORTH);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        latProfileSlider.setBackground(new java.awt.Color(255, 255, 255));
        latProfileSlider.setMajorTickSpacing(45);
        latProfileSlider.setMaximum(90);
        latProfileSlider.setMinorTickSpacing(15);
        latProfileSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        latProfileSlider.setPaintLabels(true);
        latProfileSlider.setPaintTicks(true);
        latProfileSlider.setValue(0);
        latProfileSlider.setMaximumSize(new java.awt.Dimension(80, 150));
        latProfileSlider.setMinimumSize(new java.awt.Dimension(50, 120));
        latProfileSlider.setPreferredSize(new java.awt.Dimension(54, 150));
        latProfileSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                latProfileSliderStateChanged(evt);
            }
        });
        jPanel4.add(latProfileSlider);
        {
            JSlider slider = latProfileSlider;
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

        rightProfilePanel.add(jPanel4, java.awt.BorderLayout.EAST);

        distortionProfilesPanel.add(rightProfilePanel);

        infoTabbedPane.addTab("Distortion Profiles", distortionProfilesPanel);

        curvesPanel.setLayout(new java.awt.BorderLayout());

        curvesMap.setBackground(new java.awt.Color(255, 255, 255));
        curvesMap.setInfoString(" ");
        curvesMap.setMinimumSize(new java.awt.Dimension(200, 100));
        curvesMap.setPreferredSize(new java.awt.Dimension(400, 200));
        curvesMap.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                curvesMapprofilesMapComponentResized(evt);
            }
        });

        org.jdesktop.layout.GroupLayout curvesMapLayout = new org.jdesktop.layout.GroupLayout(curvesMap);
        curvesMap.setLayout(curvesMapLayout);
        curvesMapLayout.setHorizontalGroup(
            curvesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1079, Short.MAX_VALUE)
        );
        curvesMapLayout.setVerticalGroup(
            curvesMapLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 244, Short.MAX_VALUE)
        );

        curvesPanel.add(curvesMap, java.awt.BorderLayout.CENTER);

        curvesTopPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        curvesToolBar.setBorder(null);
        curvesToolBar.setFloatable(false);

        curvesButtonGroup.add(curveLengthToggleButton);
        curveLengthToggleButton.setSelected(true);
        curveLengthToggleButton.setText("Length");
        curveLengthToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveLengthToggleButtonActionPerformed(evt);
            }
        });
        curvesToolBar.add(curveLengthToggleButton);

        curvesButtonGroup.add(curveDistanceToggleButton);
        curveDistanceToggleButton.setText("Distance");
        curveDistanceToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveDistanceToggleButtonActionPerformed(evt);
            }
        });
        curvesToolBar.add(curveDistanceToggleButton);

        curvesButtonGroup.add(curveBendingToggleButton);
        curveBendingToggleButton.setText("Bending");
        curveBendingToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveBendingToggleButtonActionPerformed(evt);
            }
        });
        curvesToolBar.add(curveBendingToggleButton);

        curvesButtonGroup.add(curveMeridiansToggleButton);
        curveMeridiansToggleButton.setText("Meridians");
        curveMeridiansToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveMeridiansToggleButtonActionPerformed(evt);
            }
        });
        curvesToolBar.add(curveMeridiansToggleButton);

        curvesTopPanel.add(curvesToolBar);

        curvesPanel.add(curvesTopPanel, java.awt.BorderLayout.NORTH);

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        jLabel4.setText("Use the first derivative curve to identify discontinuities in the Flex curves.");
        curvesBottomPanel.add(jLabel4);

        curvesPanel.add(curvesBottomPanel, java.awt.BorderLayout.SOUTH);

        infoTabbedPane.addTab("Flex Curves", curvesPanel);

        mainSplitPane.setBottomComponent(infoTabbedPane);

        centerPanel.add(mainSplitPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N,
            java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    newMenuItem.setText("New");
    newMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            newMenuItemActionPerformed(evt);
        }
    });
    fileMenu.add(newMenuItem);

    openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
openMenuItem.setText("Open…");
openMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        openMenuItemActionPerformed(evt);
    }
    });
    fileMenu.add(openMenuItem);
    fileMenu.add(fileSeparator);

    closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
closeMenuItem.setText("Close");
closeMenuItem.setEnabled(false);
closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeMenuItemActionPerformed(evt);
    }
    });
    fileMenu.add(closeMenuItem);

    saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
saveMenuItem.setText("Save");
saveMenuItem.setEnabled(false);
saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveMenuItemActionPerformed(evt);
    }
    });
    fileMenu.add(saveMenuItem);

    saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
        | java.awt.event.InputEvent.SHIFT_MASK));
saveAsMenuItem.setText("Save As...");
saveAsMenuItem.setEnabled(false);
saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        saveAsMenuItemActionPerformed(evt);
    }
    });
    fileMenu.add(saveAsMenuItem);
    fileMenu.add(jSeparator2);

    webImportMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
        | java.awt.event.InputEvent.SHIFT_MASK));
webImportMenuItem.setText("Import Vector Data from www.flexprojector.com…");
webImportMenuItem.setEnabled(false);
webImportMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        webImportMenuItemActionPerformed(evt);
    }
    });
    fileMenu.add(webImportMenuItem);

    webDownloadMenuItem.setText("Download Data from www.flexprojector.com…");
    webDownloadMenuItem.setEnabled(false);
    webDownloadMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            webDownloadMenuItemActionPerformed(evt);
        }
    });
    fileMenu.add(webDownloadMenuItem);
    fileMenu.add(jSeparator11);

    exportMapMenuItem.setText("Export Map…");
    exportMapMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            exportMapMenuItemActionPerformed(evt);
        }
    });
    fileMenu.add(exportMapMenuItem);
    fileMenu.add(jSeparator7);

    exportDistortionTableMenuItem.setText("Export Distortion Table…");
    exportDistortionTableMenuItem.setEnabled(false);
    exportDistortionTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            exportDistortionTableMenuItemActionPerformed(evt);
        }
    });
    fileMenu.add(exportDistortionTableMenuItem);
    fileMenu.add(jSeparator6);

    projectGeographicToFlexMenu.setText("Project from Geographic");

    projectShapeMenuItem.setText("Project Shapefile…");
    projectShapeMenuItem.setEnabled(false);
    projectShapeMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            projectShapeMenuItemActionPerformed(evt);
        }
    });
    projectGeographicToFlexMenu.add(projectShapeMenuItem);

    projectImageMenuItem.setText("Project Image…");
    projectImageMenuItem.setEnabled(false);
    projectImageMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            projectImageMenuItemActionPerformed(evt);
        }
    });
    projectGeographicToFlexMenu.add(projectImageMenuItem);

    projectGridMenuItem.setText("Project Grid…");
    projectGridMenuItem.setEnabled(false);
    projectGridMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            projectGridMenuItemActionPerformed(evt);
        }
    });
    projectGeographicToFlexMenu.add(projectGridMenuItem);

    fileMenu.add(projectGeographicToFlexMenu);

    projectFlexToGeographicMenu.setText("Project from Flex to Geographic");

    projectImageToGeographicMenuItem.setText("Project Image…");
    projectImageToGeographicMenuItem.setEnabled(false);
    projectImageToGeographicMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            projectImageToGeographicMenuItemActionPerformed(evt);
        }
    });
    projectFlexToGeographicMenu.add(projectImageToGeographicMenuItem);

    fileMenu.add(projectFlexToGeographicMenu);
    fileMenu.add(exitMenuSeparator);

    exitMenuItem.setText("Exit");
    exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            exitMenuItemActionPerformed(evt);
        }
    });
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    editMenu.setText("Edit");

    undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
undoMenuItem.setText("Undo");
undoMenuItem.setEnabled(false);
undoMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        undoMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(undoMenuItem);

    redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
redoMenuItem.setText("Redo");
redoMenuItem.setEnabled(false);
redoMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        redoMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(redoMenuItem);
    editMenu.add(jSeparator3);

    cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
cutMenuItem.setText("Cut");
cutMenuItem.setEnabled(false);
cutMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        cutMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(cutMenuItem);

    copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
copyMenuItem.setText("Copy");
copyMenuItem.setEnabled(false);
copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        copyMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(copyMenuItem);

    pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
pasteMenuItem.setText("Paste");
pasteMenuItem.setEnabled(false);
pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        pasteMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(pasteMenuItem);

    deleteMenuItem.setText("Delete");
    deleteMenuItem.setEnabled(false);
    deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            deleteMenuItemActionPerformed(evt);
        }
    });
    editMenu.add(deleteMenuItem);
    editMenu.add(jSeparator4);

    selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
selectAllMenuItem.setText("Select All");
selectAllMenuItem.setEnabled(false);
selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectAllMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(selectAllMenuItem);

    deselectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
        | java.awt.event.InputEvent.SHIFT_MASK));
deselectAllMenuItem.setText("Deselect All");
deselectAllMenuItem.setEnabled(false);
deselectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        deselectAllMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(deselectAllMenuItem);
    editMenu.add(jSeparator10);

    resetProjectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
resetProjectionMenuItem.setText("Reset Projection");
resetProjectionMenuItem.setEnabled(false);
resetProjectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetProjectionMenuItemActionPerformed(evt);
    }
    });
    editMenu.add(resetProjectionMenuItem);

    scaleMenuItem.setText("Projection Size…");
    scaleMenuItem.setEnabled(false);
    scaleMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            scaleMenuItemActionPerformed(evt);
        }
    });
    editMenu.add(scaleMenuItem);
    editMenu.add(jSeparator14);

    qMenuItem.setText("Adjust Acceptable Q Index…");
    qMenuItem.setEnabled(false);
    qMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            qMenuItemActionPerformed(evt);
        }
    });
    editMenu.add(qMenuItem);
    editMenu.add(preferencesSeparator);

    preferencesMenuItem.setText("Preferences…");
    preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            preferencesMenuItemActionPerformed(evt);
        }
    });
    editMenu.add(preferencesMenuItem);

    menuBar.add(editMenu);

    viewMenu.setText("View");

    zoomInMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
zoomInMenuItem.setText("Zoom In");
zoomInMenuItem.setEnabled(false);
zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        zoomInMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(zoomInMenuItem);

    zoomOutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
zoomOutMenuItem.setText("Zoom Out");
zoomOutMenuItem.setEnabled(false);
zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        zoomOutMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(zoomOutMenuItem);
    viewMenu.add(jSeparator12);

    showAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
showAllMenuItem.setText("Zoom to Map Extent");
showAllMenuItem.setEnabled(false);
showAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        showAllMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(showAllMenuItem);
    viewMenu.add(jSeparator8);

    showPageCheckBoxMenuItem.setText("Show Map Outline");
    showPageCheckBoxMenuItem.setEnabled(false);
    showPageCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            showPageCheckBoxMenuItemActionPerformed(evt);
        }
    });
    viewMenu.add(showPageCheckBoxMenuItem);
    viewMenu.add(jSeparator9);

    showFlexProjectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
showFlexProjectionMenuItem.setText("Flex Projection");
showFlexProjectionMenuItem.setEnabled(false);
showFlexProjectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        showFlexProjectionMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(showFlexProjectionMenuItem);

    showSecondProjectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
showSecondProjectionMenuItem.setText("Second Projection");
showSecondProjectionMenuItem.setEnabled(false);
showSecondProjectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        showSecondProjectionMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(showSecondProjectionMenuItem);

    toggleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
toggleMenuItem.setText("Toggle Projections");
toggleMenuItem.setEnabled(false);
toggleMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(toggleMenuItem);
    viewMenu.add(jSeparator13);

    coastlineMenuItem.setText("Coastlines");
    coastlineMenuItem.setEnabled(false);
    coastlineMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            coastlineMenuItemActionPerformed(evt);
        }
    });
    viewMenu.add(coastlineMenuItem);

    graticuleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
graticuleMenuItem.setText("Graticule");
graticuleMenuItem.setEnabled(false);
graticuleMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        graticuleMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(graticuleMenuItem);

    tissotMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
tissotMenuItem.setText("Tissot's Indicatrices");
tissotMenuItem.setEnabled(false);
tissotMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        tissotMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(tissotMenuItem);
    viewMenu.add(jSeparator1);

    switchDisplayMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
switchDisplayMenuItem.setText("Switch Display");
switchDisplayMenuItem.setEnabled(false);
switchDisplayMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        switchDisplayMenuItemActionPerformed(evt);
    }
    });
    viewMenu.add(switchDisplayMenuItem);

    menuBar.add(viewMenu);

    windowMenu.setText("Window");
    windowMenu.setName("WindowsMenu"); // NOI18N

    minimizeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M,
        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
minimizeMenuItem.setText("Minimize");
minimizeMenuItem.setEnabled(false);
minimizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        minimizeMenuItemActionPerformed(evt);
    }
    });
    windowMenu.add(minimizeMenuItem);

    zoomMenuItem.setText("Zoom");
    zoomMenuItem.setEnabled(false);
    zoomMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            zoomMenuItemActionPerformed(evt);
        }
    });
    windowMenu.add(zoomMenuItem);
    windowMenu.add(windowSeparator);

    menuBar.add(windowMenu);

    winHelpMenu.setText("?");

    onlineHelpMenuItem.setText("Flex Projector Online Manual");
    onlineHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            onlineHelpMenuItemActionPerformed(evt);
        }
    });
    winHelpMenu.add(onlineHelpMenuItem);

    infoMenuItem.setText("Info");
    infoMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            infoMenuItemActionPerformed(evt);
        }
    });
    winHelpMenu.add(infoMenuItem);

    menuBar.add(winHelpMenu);

    macHelpMenu.setText("Help");

    macOnlineHelpMenuItem.setText("Flex Projector Online Manual");
    macOnlineHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            onlineHelpMenuItemActionPerformed(evt);
        }
    });
    macHelpMenu.add(macOnlineHelpMenuItem);

    infoMenuItem1.setText("Info");
    infoMenuItem1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            infoMenuItem1ActionPerformed(evt);
        }
    });
    macHelpMenu.add(infoMenuItem1);

    menuBar.add(macHelpMenu);

    setJMenuBar(menuBar);

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onlineHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlineHelpMenuItemActionPerformed

        Properties props = PropertiesLoader.loadProperties("ika.app.Application.properties");
        String url = props.getProperty("HelpWebPage", "http://www.flexprojector.com/man/index.html");
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                Logger.getLogger(FlexProjectorWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_onlineHelpMenuItemActionPerformed

    private void curveDistanceToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveDistanceToggleButtonActionPerformed
        this.curvesManager.setCurveType(CurvesManager.CurveType.Y);
        this.curvesManager.updateCurves();
    }//GEN-LAST:event_curveDistanceToggleButtonActionPerformed

    private void curveLengthToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveLengthToggleButtonActionPerformed
        this.curvesManager.setCurveType(CurvesManager.CurveType.X);
        this.curvesManager.updateCurves();
    }//GEN-LAST:event_curveLengthToggleButtonActionPerformed

    private void curveMeridiansToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveMeridiansToggleButtonActionPerformed
        this.curvesManager.setCurveType(CurvesManager.CurveType.MERIDIANS);
        this.curvesManager.updateCurves();
    }//GEN-LAST:event_curveMeridiansToggleButtonActionPerformed

    private void curveBendingToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveBendingToggleButtonActionPerformed
        this.curvesManager.setCurveType(CurvesManager.CurveType.BENDING);
        this.curvesManager.updateCurves();
    }//GEN-LAST:event_curveBendingToggleButtonActionPerformed

    private void coastlineMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coastlineMenuItemActionPerformed
        projectionBrewerPanel.toggleCoastline();
    }//GEN-LAST:event_coastlineMenuItemActionPerformed

    private void qMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qMenuItemActionPerformed
        this.projectionBrewerPanel.showAcceptanceDialog();
    }//GEN-LAST:event_qMenuItemActionPerformed

    private void curvesMapprofilesMapComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_curvesMapprofilesMapComponentResized
        curvesManager.updateCurves();
    }//GEN-LAST:event_curvesMapprofilesMapComponentResized

    private void mapComponentComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mapComponentComponentResized
        this.mapComponent.showAll();
    }//GEN-LAST:event_mapComponentComponentResized

    private void profilesMapComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_profilesMapComponentResized
        FlexProjectorModel model = this.projectionBrewerPanel.getModel();
        Projection p = model.getDesignProjection();
        distortionProfilesManager.updateDistortionProfiles(false, p);
        distortionProfilesManager.updateDistortionProfiles(true, p);
    }//GEN-LAST:event_profilesMapComponentResized

    private void latProfileSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latProfileSliderStateChanged
        if (distortionProfilesManager == null) {
            return;
        }
        FlexProjectorModel model = projectionBrewerPanel.getModel();
        Projection p = model.getDesignProjection();

        int lat = latProfileSlider.getValue();
        horizontalProfileLabel.setText("Horizontal Profile at \u00B1" + lat + '\u00B0');
        distortionProfilesManager.setLat(lat);
        distortionProfilesManager.updateDistortionProfiles(false, p);

        GeoSet foregroundGeoSet = mapComponent.getForegroundGeoSet();
        foregroundGeoSet.removeByName(distortionProfilesManager.getMapProfileName());

        // display profile location in map while slider is being dragged
        if (latProfileSlider.getValueIsAdjusting()) {
            GeoSet profiles = distortionProfilesManager.constructProfilesInMap(false, p);
            foregroundGeoSet.add(profiles);
        }

    }//GEN-LAST:event_latProfileSliderStateChanged

    private void lonProfileSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonProfileSliderStateChanged
        if (distortionProfilesManager == null) {
            return;
        }

        FlexProjectorModel model = projectionBrewerPanel.getModel();
        Projection p = model.getDesignProjection();

        int lon = lonProfileSlider.getValue();
        verticalProfileLabel.setText("Vertical Profile at \u00B1" + lon + '\u00B0');
        distortionProfilesManager.setLon(lon);
        distortionProfilesManager.updateDistortionProfiles(true, p);

        GeoSet foregroundGeoSet = mapComponent.getForegroundGeoSet();
        foregroundGeoSet.removeByName(distortionProfilesManager.getMapProfileName());

        // display profile location in map while slider is being dragged
        if (lonProfileSlider.getValueIsAdjusting()) {
            GeoSet profiles = distortionProfilesManager.constructProfilesInMap(true, p);
            foregroundGeoSet.add(profiles);

            // make sure the profiles are visible on the map
            if (!mapComponent.isObjectVisibleOnMap(profiles)) {
                mapComponent.showAll();
            }
        }

    }//GEN-LAST:event_lonProfileSliderStateChanged

    private void exportDistortionTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDistortionTableMenuItemActionPerformed

        String filePath = ika.utils.FileUtils.askFile(null, "Save Text File",
                "Distortion Parameters.txt", false, "txt");
        if (filePath == null) {
            return;
        }

        java.io.PrintStream out = null;
        try {
            out = new java.io.PrintStream(new java.io.FileOutputStream(filePath));
            out.print(this.distortionTable.toString());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }//GEN-LAST:event_exportDistortionTableMenuItemActionPerformed

    private void tableInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableInfoButtonActionPerformed
        JOptionPane.showMessageDialog(this, this.distortionInfoPanel,
                "Projection Distortion Table", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_tableInfoButtonActionPerformed

    private void switchDisplayMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchDisplayMenuItemActionPerformed
        this.projectionBrewerPanel.switchDisplay();
    }//GEN-LAST:event_switchDisplayMenuItemActionPerformed

    private void webDownloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webDownloadMenuItemActionPerformed
        Properties props = PropertiesLoader.loadProperties("ika.app.Application.properties");
        String url = props.getProperty("DataWebPage", "http://www.flexprojector.com/datadownload.html");
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                Logger.getLogger(FlexProjectorWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_webDownloadMenuItemActionPerformed

    private void exportMapMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMapMenuItemActionPerformed
        exportMap();
    }//GEN-LAST:event_exportMapMenuItemActionPerformed

    private void webImportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webImportMenuItemActionPerformed

        Vector names = new Vector(5);
        Vector urls = new Vector(5);

        // read the index of available data sets.
        // This could be done in a separate thread while the event dispatching
        // thread could display an indeterminate progress bar.
        // This would be useful for instable internet connections, but is quite
        // complex to implement.
        BufferedReader in = null;
        try {
            java.net.URL url = new java.net.URL("http://www.flexprojector.com/data/index.txt");
            in = new BufferedReader(new InputStreamReader(url.openStream()));

            String str;
            while ((str = in.readLine()) != null) {
                String[] strParts = str.split("=");
                names.add(strParts[0]);
                urls.add(strParts[1]);
            }
        } catch (Exception e) {
            ika.utils.ErrorDialog.showErrorDialog("<html>Connecting to www.flexprojector.com is not possible. "
                    + "<br>Please make sure you have a working Internet connection.<html>",
                    "Data Loading Error");
            return;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        // ask the user which data set to load
        this.webImportComboBox.removeAllItems();
        for (int i = 0; i < names.size(); i++) {
            this.webImportComboBox.addItem(names.get(i));
        }
        int result = JOptionPane.showConfirmDialog(this, this.webImportPanel,
                "Load Web Data", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        String name = (String) this.webImportComboBox.getSelectedItem();
        boolean replaceCurrentData = this.webImportReplaceRadioButton.isSelected();

        // load the data
        try {
            String url = (String) urls.get(names.indexOf(name));
            this.importData(new java.net.URL(url), replaceCurrentData);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ika.utils.ErrorDialog.showErrorDialog("Could not find the data on www.flexprojector.com.", e);
        }
    }//GEN-LAST:event_webImportMenuItemActionPerformed

    private void showSecondProjectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSecondProjectionMenuItemActionPerformed
        projectionBrewerPanel.toggleSecondProjection();
    }//GEN-LAST:event_showSecondProjectionMenuItemActionPerformed

    private void showFlexProjectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFlexProjectionMenuItemActionPerformed
        projectionBrewerPanel.toggleFlexProjection();
    }//GEN-LAST:event_showFlexProjectionMenuItemActionPerformed

    private void resetProjectionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetProjectionMenuItemActionPerformed
        projectionBrewerPanel.resetFlexProjection();
    }//GEN-LAST:event_resetProjectionMenuItemActionPerformed

    private void graticuleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graticuleMenuItemActionPerformed
        projectionBrewerPanel.toggleGraticule();
    }//GEN-LAST:event_graticuleMenuItemActionPerformed

    private Projection getProjectionClone() {

        DisplayModel dm = projectionBrewerPanel.getModel().getDisplayModel();
        Projection p;
        if (dm.showFlexProjection) {
            // designed projection
            p = projectionBrewerPanel.getModel().getDesignProjection();
        } else {
            // background projection
            p = (Projection) dm.projection;
        }
        p = (Projection) p.clone();
        p.setEllipsoid(Ellipsoid.SPHERE);
        p.initialize();
        return p;
    }

    private Projection askUserForProjection() {
        Projection proj = null;

        // construct list with projection names
        java.util.List projNames = ProjectionsManager.getProjectionNames(false, false, false);
        DisplayModel dm = projectionBrewerPanel.getModel().getDisplayModel();
        if (dm.showFlexProjection) {
            Projection flexProj = projectionBrewerPanel.getModel().getDesignProjection();
            projNames.add(new JSeparator());
            projNames.add(flexProj.getName());
        }

        // display dialog
        JComboBox jcb = new JComboBox(projNames.toArray());
        // custom renderer for separator in menu
        jcb.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof JSeparator) {
                    return (JSeparator) value;
                }
                return this;
            }
        });

        int res = JOptionPane.showOptionDialog(this, jcb, "Select a Projection",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, null, null);
        if (res == JOptionPane.OK_OPTION) {
            if (jcb.getSelectedIndex() == projNames.size() - 1) {
                proj = projectionBrewerPanel.getModel().getDesignProjection();
                proj = cloneAndNormalizeIfFlexProjection(proj);
            } else {
                proj = ProjectionsManager.getProjection((String) (jcb.getSelectedItem()));
            }
            proj.initialize();
        }

        return proj;
    }

    private void projectShapeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectShapeMenuItemActionPerformed
        // ask the user for a file to read
        String msg = "Project ESRI Shapefile";
        String importFilePath = FileUtils.askFile(this, msg, true);
        if (importFilePath == null) {
            return;
        }

        // ask user for a projection
        Projection proj = askUserForProjection();
        if (proj != null) {
            new ShapeProjector(this, proj, importFilePath, null);
        }
    }//GEN-LAST:event_projectShapeMenuItemActionPerformed

    /**
     * If the passed projection is a FlexProjection and if this projection is
     * not normalized (i.e. the maximum value of the length or distribution is
     * not 1), then the passed projection is cloned and normalized, and the
     * clone is returned. Otherwise the passed projection is returned unaltered.
     *
     * @param p
     * @return
     */
    private Projection cloneAndNormalizeIfFlexProjection(Projection p) {
        if (p instanceof FlexProjection) {
            FlexProjection fp = (FlexProjection) p;
            if (fp.getModel().isNormalized() == false) {
                fp = (FlexProjection) p.clone();
                fp.getModel().normalize();
                return fp;
            }
        }
        return p;

    }

    private void projectImage(Projection srcProj, Projection dstProj) {
        final String openMsg = "Project " + srcProj.getName() + " Image";
        final String saveMsg = "Save Image (TIFF Format)";

        // ask the user for a file to read
        String importPath = FileUtils.askFile(this, openMsg, true);
        if (importPath == null) {
            return;
        }

        // ask the user for a file to store the projected image
        String fileName = FileUtils.getFileNameWithoutExtension(importPath);
        fileName += ".tif";
        String exportPath = FileUtils.askFile(this, saveMsg, fileName, false, "tif");
        if (exportPath == null) {
            return; // user canceled
        }

        boolean nearestNeighbor = FlexProjectorPreferencesPanel.isNearestNeighbor();
        new ImageProjector(this, srcProj, dstProj, importPath, exportPath, nearestNeighbor);
    }
    private void projectImageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectImageMenuItemActionPerformed
        // ask user for a projection
        Projection dstProj = askUserForProjection();
        if (dstProj == null) {
            return;
        }
        Projection srcProj = new EquidistantCylindricalProjection();
        srcProj.initialize();
        projectImage(srcProj, dstProj);
    }//GEN-LAST:event_projectImageMenuItemActionPerformed

    private void tissotMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tissotMenuItemActionPerformed
        projectionBrewerPanel.toggleTissot();
    }//GEN-LAST:event_tissotMenuItemActionPerformed

    private void toggleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleMenuItemActionPerformed
        projectionBrewerPanel.toggleProjectionVisibility();
    }//GEN-LAST:event_toggleMenuItemActionPerformed

    private void projectGridMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectGridMenuItemActionPerformed
        // ask the user for a grid file to read
        String msg = "Project ESRI ASCII Grid with Current Projection";
        String importFilePath = FileUtils.askFile(this, msg, true);
        if (importFilePath == null) {
            return;
        }

        // ask user for a projection
        Projection proj = askUserForProjection();
        if (proj == null) {
            return;
        }

        // ask the user for a file to store the projected grid
        String fileName = FileUtils.forceFileNameExtension(importFilePath, "asc");
        String exportFilePath = FileUtils.askFile(this, "Save Projected Grid",
                fileName, false, "asc");
        if (exportFilePath == null) {
            return; // user canceled
        }

        new GridProjector(this, proj, importFilePath, exportFilePath);
    }//GEN-LAST:event_projectGridMenuItemActionPerformed

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        // this handler is not used on Macintosh.
        // show the preferences dialog
        new ika.gui.PreferencesDialog(this, true).setVisible(true);
    }//GEN-LAST:event_preferencesMenuItemActionPerformed

    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        FlexProjectorWindow.newDocumentWindow();
    }//GEN-LAST:event_newMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        FlexProjectorWindow w = (FlexProjectorWindow) openDocumentWindow();
        if (w != null) {
            w.mapComponent.showAll();

            // the first undo state holds the default Robinson projection. Replace
            // this first state with the state that was just loaded.
            w.mapComponent.resetUndo();
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        this.closeDocumentWindow();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        this.saveDocumentWindow();
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        String filePath = this.askFileToSave("Save As");
        this.saveDocumentWindow(filePath);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        // this handler is not used on Macintosh. On Windows and other platforms
        // only this window is closed.
        this.closeDocumentWindow();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        try {
            projectionBrewerPanel.showDesignProjection();
            mapComponent.undo();
        } catch (Exception e) {
            e.printStackTrace();
            ika.utils.ErrorDialog.showErrorDialog("Could not undo.", this);
        }
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        try {
            projectionBrewerPanel.showDesignProjection();
            this.mapComponent.redo();
        } catch (Exception e) {
            e.printStackTrace();
            ika.utils.ErrorDialog.showErrorDialog("Could not successfully redo.", this);
        }
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemActionPerformed
        // create a GeoSet with copies of the currently selected GeoObjects
        GeoSet copyGeoSet = new GeoSet();
        this.mapComponent.getGeoSet().cloneIfSelected(copyGeoSet);
        if (copyGeoSet.getNumberOfChildren() == 0) {
            return;
        }

        // put the selected GeoObjects onto the clipboard
        GeoTransferable.storeInSystemClipboard(copyGeoSet);

        // delete the selected GeoObjects
        this.mapComponent.removeSelectedGeoObjects();

        this.mapComponent.addUndo("Cut");
    }//GEN-LAST:event_cutMenuItemActionPerformed

    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed

        // create a GeoSet with copies of the currently selected GeoObjects
        GeoSet copyGeoSet = new GeoSet();
        this.mapComponent.getGeoSet().cloneIfSelected(copyGeoSet);
        if (copyGeoSet.getNumberOfChildren() == 0) {
            return;
        }
        copyGeoSet = (GeoSet) copyGeoSet.getGeoObject(0);

        // put the selected objects onto the clipboard
        GeoTransferable.storeInSystemClipboard(copyGeoSet);

        // update the "Paste" command in the edit menu
        this.updateEditMenu();
    }//GEN-LAST:event_copyMenuItemActionPerformed

    private void pasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemActionPerformed
        GeoSet geoSet = GeoTransferable.retreiveSystemClipboardCopy();
        if (geoSet == null) {
            return;
        }

        // make all pasted objects visible to show the result of the paste action.
        geoSet.setVisible(true);

        this.mapComponent.deselectAllAndAddChildren(geoSet);
        this.mapComponent.addUndo("Paste");

        // make sure the pasted objects are visible in the map
        if (this.mapComponent.isObjectVisibleOnMap(geoSet) == false) {
            this.mapComponent.showAll();
        }
    }//GEN-LAST:event_pasteMenuItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        this.mapComponent.removeSelectedGeoObjects();
        this.mapComponent.addUndo("Delete");
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
        this.mapComponent.selectAllGeoObjects();
    }//GEN-LAST:event_selectAllMenuItemActionPerformed

    private void deselectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllMenuItemActionPerformed
        this.mapComponent.deselectAllGeoObjects();
    }//GEN-LAST:event_deselectAllMenuItemActionPerformed

    private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuItemActionPerformed
        this.mapComponent.zoomIn();
    }//GEN-LAST:event_zoomInMenuItemActionPerformed

    private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuItemActionPerformed
        this.mapComponent.zoomOut();
    }//GEN-LAST:event_zoomOutMenuItemActionPerformed

    private void showAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllMenuItemActionPerformed
        mapComponent.showAll();
    }//GEN-LAST:event_showAllMenuItemActionPerformed

    private void showPageCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPageCheckBoxMenuItemActionPerformed
        boolean show = this.showPageCheckBoxMenuItem.isSelected();
        this.mapComponent.getPageFormat().setVisible(show);
    }//GEN-LAST:event_showPageCheckBoxMenuItemActionPerformed

    private void minimizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeMenuItemActionPerformed
        this.setState(Frame.ICONIFIED);
    }//GEN-LAST:event_minimizeMenuItemActionPerformed

    private void zoomMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomMenuItemActionPerformed
        if ((this.getExtendedState() & Frame.MAXIMIZED_BOTH) != MAXIMIZED_BOTH) {
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            this.setExtendedState(JFrame.NORMAL);
        }
        this.validate();
    }//GEN-LAST:event_zoomMenuItemActionPerformed

    private void infoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoMenuItemActionPerformed
        ika.gui.ProgramInfoPanel.showApplicationInfo();
    }//GEN-LAST:event_infoMenuItemActionPerformed

    private void zoomInToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInToggleButtonActionPerformed
        this.mapComponent.setMapTool(new ZoomInTool(this.mapComponent));
    }//GEN-LAST:event_zoomInToggleButtonActionPerformed

    private void zoomOutToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutToggleButtonActionPerformed
        this.mapComponent.setMapTool(new ZoomOutTool(this.mapComponent));
    }//GEN-LAST:event_zoomOutToggleButtonActionPerformed

    private void handToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_handToggleButtonActionPerformed
        this.mapComponent.setMapTool(new PanTool(this.mapComponent));
    }//GEN-LAST:event_handToggleButtonActionPerformed

    private void distanceToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distanceToggleButtonActionPerformed
        MeasureTool tool = new MeasureTool(this.mapComponent);
        final JFrame frame = this;
        MeasureToolListener listener = new MeasureToolListener() {

            @Override
            public void distanceChanged(double distance, double angle,
                    MapComponent mapComponent) {
            }

            @Override
            public void newDistance(double distance, double angle, ika.gui.MapComponent mapComponent) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>Distance: ");
                sb.append(new DecimalFormat("#,###.##m").format(distance));
                sb.append("<br>Angle: ");
                sb.append(new DecimalFormat("#.#").format(Math.toDegrees(angle)));
                sb.append("\u00B0<br><br><small>Angle is counterclockwise from the horizontal axis.</small></html>");
                JOptionPane.showMessageDialog(frame, sb.toString(),
                        "Distance and Angle", JOptionPane.PLAIN_MESSAGE);
            }

            @Override
            public void clearDistance() {
            }

        };
        tool.addMeasureToolListener(listener);
        this.mapComponent.setMapTool(tool);
    }//GEN-LAST:event_distanceToggleButtonActionPerformed

    private void showAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllButtonActionPerformed
        this.mapComponent.showAll();
    }//GEN-LAST:event_showAllButtonActionPerformed

    private void closeWindow(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeWindow
        this.closeDocumentWindow();
    }//GEN-LAST:event_closeWindow

    private void scaleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleMenuItemActionPerformed
        projectionBrewerPanel.showSizeDialog();
    }//GEN-LAST:event_scaleMenuItemActionPerformed

    private void projectImageToGeographicMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectImageToGeographicMenuItemActionPerformed
        Projection dstProj = new EquidistantCylindricalProjection();
        dstProj.initialize();
        projectImage(getProjectionClone(), dstProj);
    }//GEN-LAST:event_projectImageToGeographicMenuItemActionPerformed

    private void infoMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoMenuItem1ActionPerformed
        ika.gui.ProgramInfoPanel.showApplicationInfo();
    }//GEN-LAST:event_infoMenuItem1ActionPerformed

    @Override
    protected boolean closeDocumentWindow() {
        Window[] ownedWindows = this.getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; i++) {
            if (!ownedWindows[i].isVisible()) {
                continue;
            }
            String name = ownedWindows[i].getName();
            if (name != null && name.equals(ProgressPanel.DIALOG_NAME)) {
                String msg = "Please first stop the projection of the external data.";
                String title = "Unable to Close Window";
                ErrorDialog.showErrorDialog(msg, title, null, this);
                return false;
            }
        }
        return super.closeDocumentWindow();
    }

    /**
     * A property change listener for the root pane that adjusts the enabled
     * state of the save menu depending on the windowModified property attached
     * to the root pane.
     */
    private void windowModifiedPropertyChange(java.beans.PropertyChangeEvent evt) {

        // only treat changes to the windowModified property
        if (!"windowModified".equals(evt.getPropertyName())) {
            return;
        }

        // retrieve the value of the windowModified property
        Boolean windowModified = null;
        if (saveMenuItem != null && this.getRootPane() != null) {
            windowModified = (Boolean) this.getRootPane().getClientProperty("windowModified");
        }

        // enable or disable the saveMenu accordingly
        if (windowModified != null) {
            this.saveMenuItem.setEnabled(windowModified.booleanValue());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem coastlineMenuItem;
    private ika.gui.CoordinatesInfoPanel coordinatesInfoPanel;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JToggleButton curveBendingToggleButton;
    private javax.swing.JToggleButton curveDistanceToggleButton;
    private javax.swing.JToggleButton curveLengthToggleButton;
    private javax.swing.JToggleButton curveMeridiansToggleButton;
    private javax.swing.JPanel curvesBottomPanel;
    private javax.swing.ButtonGroup curvesButtonGroup;
    private ika.gui.MapComponent curvesMap;
    private javax.swing.JPanel curvesPanel;
    private javax.swing.JToolBar curvesToolBar;
    private javax.swing.JPanel curvesTopPanel;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem deselectAllMenuItem;
    private javax.swing.JToggleButton distanceToggleButton;
    private javax.swing.JPanel distortionInfoPanel;
    private javax.swing.JPanel distortionProfilesPanel;
    private javax.swing.JScrollPane distortionTableScrollPane;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JSeparator exitMenuSeparator;
    private javax.swing.JMenuItem exportDistortionTableMenuItem;
    private javax.swing.JMenuItem exportMapMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JSeparator fileSeparator;
    private javax.swing.JMenuItem graticuleMenuItem;
    private javax.swing.JToggleButton handToggleButton;
    private ika.gui.MapComponent horProfilesMap;
    private javax.swing.JLabel horizontalProfileLabel;
    private javax.swing.JMenuItem infoMenuItem;
    private javax.swing.JMenuItem infoMenuItem1;
    private javax.swing.JTabbedPane infoTabbedPane;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JToolBar.Separator jSeparator15;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSlider latProfileSlider;
    private javax.swing.JPanel leftProfilePanel;
    private javax.swing.JSlider lonProfileSlider;
    private javax.swing.JMenu macHelpMenu;
    private javax.swing.JMenuItem macOnlineHelpMenuItem;
    private javax.swing.JSplitPane mainSplitPane;
    private ika.gui.MapComponent mapComponent;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem minimizeMenuItem;
    private javax.swing.JToolBar navigationToolBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem onlineHelpMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JSeparator preferencesSeparator;
    private javax.swing.JMenu projectFlexToGeographicMenu;
    private javax.swing.JMenu projectGeographicToFlexMenu;
    private javax.swing.JMenuItem projectGridMenuItem;
    private javax.swing.JMenuItem projectImageMenuItem;
    private javax.swing.JMenuItem projectImageToGeographicMenuItem;
    private javax.swing.JMenuItem projectShapeMenuItem;
    private javax.swing.JMenuItem qMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem resetProjectionMenuItem;
    private javax.swing.JPanel rightProfilePanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem scaleMenuItem;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JButton showAllButton;
    private javax.swing.JMenuItem showAllMenuItem;
    private javax.swing.JMenuItem showFlexProjectionMenuItem;
    private javax.swing.JCheckBoxMenuItem showPageCheckBoxMenuItem;
    private javax.swing.JMenuItem showSecondProjectionMenuItem;
    private javax.swing.JMenuItem switchDisplayMenuItem;
    private javax.swing.JButton tableInfoButton;
    private javax.swing.JMenuItem tissotMenuItem;
    private javax.swing.JMenuItem toggleMenuItem;
    private javax.swing.ButtonGroup toolBarButtonGroup;
    private javax.swing.JMenuItem undoMenuItem;
    private ika.gui.MapComponent vertProfilesMap;
    private javax.swing.JLabel verticalProfileLabel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem webDownloadMenuItem;
    private javax.swing.JRadioButton webImportAddRadioButton;
    private javax.swing.ButtonGroup webImportButtonGroup;
    private javax.swing.JComboBox webImportComboBox;
    private javax.swing.JMenuItem webImportMenuItem;
    private javax.swing.JPanel webImportPanel;
    private javax.swing.JRadioButton webImportReplaceRadioButton;
    private javax.swing.JMenu winHelpMenu;
    private javax.swing.JMenu windowMenu;
    private javax.swing.JSeparator windowSeparator;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JToggleButton zoomInToggleButton;
    private javax.swing.JMenuItem zoomMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    private javax.swing.JToggleButton zoomOutToggleButton;
    // End of variables declaration//GEN-END:variables

}
