package ika.gui;

import java.beans.*;

/**
 *
 * @author bernie
 */
public class MapComponentBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( ika.gui.MapComponent.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_accessibleContext = 0;
    private static final int PROPERTY_actionMap = 1;
    private static final int PROPERTY_alignmentX = 2;
    private static final int PROPERTY_alignmentY = 3;
    private static final int PROPERTY_allVisible = 4;
    private static final int PROPERTY_ancestorListeners = 5;
    private static final int PROPERTY_autoscrolls = 6;
    private static final int PROPERTY_background = 7;
    private static final int PROPERTY_backgroundSet = 8;
    private static final int PROPERTY_baselineResizeBehavior = 9;
    private static final int PROPERTY_border = 10;
    private static final int PROPERTY_boundingBoxOfSelectedGeoObjects = 11;
    private static final int PROPERTY_bounds = 12;
    private static final int PROPERTY_colorModel = 13;
    private static final int PROPERTY_component = 14;
    private static final int PROPERTY_componentCount = 15;
    private static final int PROPERTY_componentListeners = 16;
    private static final int PROPERTY_componentOrientation = 17;
    private static final int PROPERTY_componentPopupMenu = 18;
    private static final int PROPERTY_components = 19;
    private static final int PROPERTY_containerListeners = 20;
    private static final int PROPERTY_coordinateFormatter = 21;
    private static final int PROPERTY_cursor = 22;
    private static final int PROPERTY_cursorSet = 23;
    private static final int PROPERTY_debugGraphicsOptions = 24;
    private static final int PROPERTY_displayable = 25;
    private static final int PROPERTY_doubleBuffer = 26;
    private static final int PROPERTY_doubleBuffered = 27;
    private static final int PROPERTY_dropTarget = 28;
    private static final int PROPERTY_enabled = 29;
    private static final int PROPERTY_focusable = 30;
    private static final int PROPERTY_focusCycleRoot = 31;
    private static final int PROPERTY_focusCycleRootAncestor = 32;
    private static final int PROPERTY_focusListeners = 33;
    private static final int PROPERTY_focusOwner = 34;
    private static final int PROPERTY_focusTraversable = 35;
    private static final int PROPERTY_focusTraversalKeys = 36;
    private static final int PROPERTY_focusTraversalKeysEnabled = 37;
    private static final int PROPERTY_focusTraversalPolicy = 38;
    private static final int PROPERTY_focusTraversalPolicyProvider = 39;
    private static final int PROPERTY_focusTraversalPolicySet = 40;
    private static final int PROPERTY_font = 41;
    private static final int PROPERTY_fontSet = 42;
    private static final int PROPERTY_foreground = 43;
    private static final int PROPERTY_foregroundGeoSet = 44;
    private static final int PROPERTY_foregroundSet = 45;
    private static final int PROPERTY_geoSet = 46;
    private static final int PROPERTY_graphics = 47;
    private static final int PROPERTY_graphicsConfiguration = 48;
    private static final int PROPERTY_height = 49;
    private static final int PROPERTY_hierarchyBoundsListeners = 50;
    private static final int PROPERTY_hierarchyListeners = 51;
    private static final int PROPERTY_ignoreRepaint = 52;
    private static final int PROPERTY_imageRenderingHint = 53;
    private static final int PROPERTY_importExportGeoSet = 54;
    private static final int PROPERTY_infoString = 55;
    private static final int PROPERTY_inheritsPopupMenu = 56;
    private static final int PROPERTY_inputContext = 57;
    private static final int PROPERTY_inputMap = 58;
    private static final int PROPERTY_inputMethodListeners = 59;
    private static final int PROPERTY_inputMethodRequests = 60;
    private static final int PROPERTY_inputVerifier = 61;
    private static final int PROPERTY_insets = 62;
    private static final int PROPERTY_keyListeners = 63;
    private static final int PROPERTY_layout = 64;
    private static final int PROPERTY_lightweight = 65;
    private static final int PROPERTY_locale = 66;
    private static final int PROPERTY_location = 67;
    private static final int PROPERTY_locationOnScreen = 68;
    private static final int PROPERTY_managingFocus = 69;
    private static final int PROPERTY_mapDropTarget = 70;
    private static final int PROPERTY_mapTool = 71;
    private static final int PROPERTY_mapUndoManager = 72;
    private static final int PROPERTY_maximumSize = 73;
    private static final int PROPERTY_maximumSizeSet = 74;
    private static final int PROPERTY_minimumSize = 75;
    private static final int PROPERTY_minimumSizeSet = 76;
    private static final int PROPERTY_mouseListeners = 77;
    private static final int PROPERTY_mouseMotionListeners = 78;
    private static final int PROPERTY_mousePosition = 79;
    private static final int PROPERTY_mouseWheelListeners = 80;
    private static final int PROPERTY_name = 81;
    private static final int PROPERTY_nextFocusableComponent = 82;
    private static final int PROPERTY_opaque = 83;
    private static final int PROPERTY_optimizedDrawingEnabled = 84;
    private static final int PROPERTY_pageFormat = 85;
    private static final int PROPERTY_paintingForPrint = 86;
    private static final int PROPERTY_paintingTile = 87;
    private static final int PROPERTY_parent = 88;
    private static final int PROPERTY_peer = 89;
    private static final int PROPERTY_preferredSize = 90;
    private static final int PROPERTY_preferredSizeSet = 91;
    private static final int PROPERTY_propertyChangeListeners = 92;
    private static final int PROPERTY_registeredKeyStrokes = 93;
    private static final int PROPERTY_renderParamsProvider = 94;
    private static final int PROPERTY_requestFocusEnabled = 95;
    private static final int PROPERTY_rootPane = 96;
    private static final int PROPERTY_scaleFactor = 97;
    private static final int PROPERTY_scaleNumber = 98;
    private static final int PROPERTY_showing = 99;
    private static final int PROPERTY_size = 100;
    private static final int PROPERTY_toolkit = 101;
    private static final int PROPERTY_toolTipText = 102;
    private static final int PROPERTY_topLevelAncestor = 103;
    private static final int PROPERTY_transferHandler = 104;
    private static final int PROPERTY_transformForSelectedObjects = 105;
    private static final int PROPERTY_treeLock = 106;
    private static final int PROPERTY_UIClassID = 107;
    private static final int PROPERTY_undo = 108;
    private static final int PROPERTY_undoRedoState = 109;
    private static final int PROPERTY_valid = 110;
    private static final int PROPERTY_validateRoot = 111;
    private static final int PROPERTY_verifyInputWhenFocusTarget = 112;
    private static final int PROPERTY_vetoableChangeListeners = 113;
    private static final int PROPERTY_visible = 114;
    private static final int PROPERTY_visibleArea = 115;
    private static final int PROPERTY_visibleHeight = 116;
    private static final int PROPERTY_visibleRect = 117;
    private static final int PROPERTY_visibleWidth = 118;
    private static final int PROPERTY_width = 119;
    private static final int PROPERTY_x = 120;
    private static final int PROPERTY_y = 121;
    private static final int PROPERTY_zoomWithMouseWheel = 122;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[123];
    
        try {
            properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", ika.gui.MapComponent.class, "getAccessibleContext", null ); // NOI18N
            properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", ika.gui.MapComponent.class, "getActionMap", "setActionMap" ); // NOI18N
            properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", ika.gui.MapComponent.class, "getAlignmentX", "setAlignmentX" ); // NOI18N
            properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", ika.gui.MapComponent.class, "getAlignmentY", "setAlignmentY" ); // NOI18N
            properties[PROPERTY_allVisible] = new PropertyDescriptor ( "allVisible", ika.gui.MapComponent.class, "isAllVisible", null ); // NOI18N
            properties[PROPERTY_ancestorListeners] = new PropertyDescriptor ( "ancestorListeners", ika.gui.MapComponent.class, "getAncestorListeners", null ); // NOI18N
            properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", ika.gui.MapComponent.class, "getAutoscrolls", "setAutoscrolls" ); // NOI18N
            properties[PROPERTY_background] = new PropertyDescriptor ( "background", ika.gui.MapComponent.class, "getBackground", "setBackground" ); // NOI18N
            properties[PROPERTY_backgroundSet] = new PropertyDescriptor ( "backgroundSet", ika.gui.MapComponent.class, "isBackgroundSet", null ); // NOI18N
            properties[PROPERTY_baselineResizeBehavior] = new PropertyDescriptor ( "baselineResizeBehavior", ika.gui.MapComponent.class, "getBaselineResizeBehavior", null ); // NOI18N
            properties[PROPERTY_border] = new PropertyDescriptor ( "border", ika.gui.MapComponent.class, "getBorder", "setBorder" ); // NOI18N
            properties[PROPERTY_boundingBoxOfSelectedGeoObjects] = new PropertyDescriptor ( "boundingBoxOfSelectedGeoObjects", ika.gui.MapComponent.class, "getBoundingBoxOfSelectedGeoObjects", null ); // NOI18N
            properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", ika.gui.MapComponent.class, "getBounds", "setBounds" ); // NOI18N
            properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", ika.gui.MapComponent.class, "getColorModel", null ); // NOI18N
            properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", ika.gui.MapComponent.class, null, null, "getComponent", null ); // NOI18N
            properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", ika.gui.MapComponent.class, "getComponentCount", null ); // NOI18N
            properties[PROPERTY_componentListeners] = new PropertyDescriptor ( "componentListeners", ika.gui.MapComponent.class, "getComponentListeners", null ); // NOI18N
            properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", ika.gui.MapComponent.class, "getComponentOrientation", "setComponentOrientation" ); // NOI18N
            properties[PROPERTY_componentPopupMenu] = new PropertyDescriptor ( "componentPopupMenu", ika.gui.MapComponent.class, "getComponentPopupMenu", "setComponentPopupMenu" ); // NOI18N
            properties[PROPERTY_components] = new PropertyDescriptor ( "components", ika.gui.MapComponent.class, "getComponents", null ); // NOI18N
            properties[PROPERTY_containerListeners] = new PropertyDescriptor ( "containerListeners", ika.gui.MapComponent.class, "getContainerListeners", null ); // NOI18N
            properties[PROPERTY_coordinateFormatter] = new PropertyDescriptor ( "coordinateFormatter", ika.gui.MapComponent.class, "getCoordinateFormatter", "setCoordinateFormatter" ); // NOI18N
            properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", ika.gui.MapComponent.class, "getCursor", "setCursor" ); // NOI18N
            properties[PROPERTY_cursorSet] = new PropertyDescriptor ( "cursorSet", ika.gui.MapComponent.class, "isCursorSet", null ); // NOI18N
            properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", ika.gui.MapComponent.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" ); // NOI18N
            properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", ika.gui.MapComponent.class, "isDisplayable", null ); // NOI18N
            properties[PROPERTY_doubleBuffer] = new PropertyDescriptor ( "doubleBuffer", ika.gui.MapComponent.class, "getDoubleBuffer", null ); // NOI18N
            properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", ika.gui.MapComponent.class, "isDoubleBuffered", "setDoubleBuffered" ); // NOI18N
            properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", ika.gui.MapComponent.class, "getDropTarget", "setDropTarget" ); // NOI18N
            properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", ika.gui.MapComponent.class, "isEnabled", "setEnabled" ); // NOI18N
            properties[PROPERTY_focusable] = new PropertyDescriptor ( "focusable", ika.gui.MapComponent.class, "isFocusable", "setFocusable" ); // NOI18N
            properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", ika.gui.MapComponent.class, "isFocusCycleRoot", "setFocusCycleRoot" ); // NOI18N
            properties[PROPERTY_focusCycleRootAncestor] = new PropertyDescriptor ( "focusCycleRootAncestor", ika.gui.MapComponent.class, "getFocusCycleRootAncestor", null ); // NOI18N
            properties[PROPERTY_focusListeners] = new PropertyDescriptor ( "focusListeners", ika.gui.MapComponent.class, "getFocusListeners", null ); // NOI18N
            properties[PROPERTY_focusOwner] = new PropertyDescriptor ( "focusOwner", ika.gui.MapComponent.class, "isFocusOwner", null ); // NOI18N
            properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", ika.gui.MapComponent.class, "isFocusTraversable", null ); // NOI18N
            properties[PROPERTY_focusTraversalKeys] = new IndexedPropertyDescriptor ( "focusTraversalKeys", ika.gui.MapComponent.class, null, null, null, "setFocusTraversalKeys" ); // NOI18N
            properties[PROPERTY_focusTraversalKeysEnabled] = new PropertyDescriptor ( "focusTraversalKeysEnabled", ika.gui.MapComponent.class, "getFocusTraversalKeysEnabled", "setFocusTraversalKeysEnabled" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicy] = new PropertyDescriptor ( "focusTraversalPolicy", ika.gui.MapComponent.class, "getFocusTraversalPolicy", "setFocusTraversalPolicy" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicyProvider] = new PropertyDescriptor ( "focusTraversalPolicyProvider", ika.gui.MapComponent.class, "isFocusTraversalPolicyProvider", "setFocusTraversalPolicyProvider" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicySet] = new PropertyDescriptor ( "focusTraversalPolicySet", ika.gui.MapComponent.class, "isFocusTraversalPolicySet", null ); // NOI18N
            properties[PROPERTY_font] = new PropertyDescriptor ( "font", ika.gui.MapComponent.class, "getFont", "setFont" ); // NOI18N
            properties[PROPERTY_fontSet] = new PropertyDescriptor ( "fontSet", ika.gui.MapComponent.class, "isFontSet", null ); // NOI18N
            properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", ika.gui.MapComponent.class, "getForeground", "setForeground" ); // NOI18N
            properties[PROPERTY_foregroundGeoSet] = new PropertyDescriptor ( "foregroundGeoSet", ika.gui.MapComponent.class, "getForegroundGeoSet", null ); // NOI18N
            properties[PROPERTY_foregroundSet] = new PropertyDescriptor ( "foregroundSet", ika.gui.MapComponent.class, "isForegroundSet", null ); // NOI18N
            properties[PROPERTY_geoSet] = new PropertyDescriptor ( "geoSet", ika.gui.MapComponent.class, "getGeoSet", "setGeoSet" ); // NOI18N
            properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", ika.gui.MapComponent.class, "getGraphics", null ); // NOI18N
            properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", ika.gui.MapComponent.class, "getGraphicsConfiguration", null ); // NOI18N
            properties[PROPERTY_height] = new PropertyDescriptor ( "height", ika.gui.MapComponent.class, "getHeight", null ); // NOI18N
            properties[PROPERTY_hierarchyBoundsListeners] = new PropertyDescriptor ( "hierarchyBoundsListeners", ika.gui.MapComponent.class, "getHierarchyBoundsListeners", null ); // NOI18N
            properties[PROPERTY_hierarchyListeners] = new PropertyDescriptor ( "hierarchyListeners", ika.gui.MapComponent.class, "getHierarchyListeners", null ); // NOI18N
            properties[PROPERTY_ignoreRepaint] = new PropertyDescriptor ( "ignoreRepaint", ika.gui.MapComponent.class, "getIgnoreRepaint", "setIgnoreRepaint" ); // NOI18N
            properties[PROPERTY_imageRenderingHint] = new PropertyDescriptor ( "imageRenderingHint", ika.gui.MapComponent.class, "getImageRenderingHint", "setImageRenderingHint" ); // NOI18N
            properties[PROPERTY_importExportGeoSet] = new PropertyDescriptor ( "importExportGeoSet", ika.gui.MapComponent.class, "getImportExportGeoSet", null ); // NOI18N
            properties[PROPERTY_infoString] = new PropertyDescriptor ( "infoString", ika.gui.MapComponent.class, "getInfoString", "setInfoString" ); // NOI18N
            properties[PROPERTY_inheritsPopupMenu] = new PropertyDescriptor ( "inheritsPopupMenu", ika.gui.MapComponent.class, "getInheritsPopupMenu", "setInheritsPopupMenu" ); // NOI18N
            properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", ika.gui.MapComponent.class, "getInputContext", null ); // NOI18N
            properties[PROPERTY_inputMap] = new PropertyDescriptor ( "inputMap", ika.gui.MapComponent.class, "getInputMap", null ); // NOI18N
            properties[PROPERTY_inputMethodListeners] = new PropertyDescriptor ( "inputMethodListeners", ika.gui.MapComponent.class, "getInputMethodListeners", null ); // NOI18N
            properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", ika.gui.MapComponent.class, "getInputMethodRequests", null ); // NOI18N
            properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", ika.gui.MapComponent.class, "getInputVerifier", "setInputVerifier" ); // NOI18N
            properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", ika.gui.MapComponent.class, "getInsets", null ); // NOI18N
            properties[PROPERTY_keyListeners] = new PropertyDescriptor ( "keyListeners", ika.gui.MapComponent.class, "getKeyListeners", null ); // NOI18N
            properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", ika.gui.MapComponent.class, "getLayout", "setLayout" ); // NOI18N
            properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", ika.gui.MapComponent.class, "isLightweight", null ); // NOI18N
            properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", ika.gui.MapComponent.class, "getLocale", "setLocale" ); // NOI18N
            properties[PROPERTY_location] = new PropertyDescriptor ( "location", ika.gui.MapComponent.class, "getLocation", "setLocation" ); // NOI18N
            properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", ika.gui.MapComponent.class, "getLocationOnScreen", null ); // NOI18N
            properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", ika.gui.MapComponent.class, "isManagingFocus", null ); // NOI18N
            properties[PROPERTY_mapDropTarget] = new PropertyDescriptor ( "mapDropTarget", ika.gui.MapComponent.class, "getMapDropTarget", "setMapDropTarget" ); // NOI18N
            properties[PROPERTY_mapTool] = new PropertyDescriptor ( "mapTool", ika.gui.MapComponent.class, "getMapTool", "setMapTool" ); // NOI18N
            properties[PROPERTY_mapUndoManager] = new PropertyDescriptor ( "mapUndoManager", ika.gui.MapComponent.class, "getMapUndoManager", "setMapUndoManager" ); // NOI18N
            properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", ika.gui.MapComponent.class, "getMaximumSize", "setMaximumSize" ); // NOI18N
            properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", ika.gui.MapComponent.class, "isMaximumSizeSet", null ); // NOI18N
            properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", ika.gui.MapComponent.class, "getMinimumSize", "setMinimumSize" ); // NOI18N
            properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", ika.gui.MapComponent.class, "isMinimumSizeSet", null ); // NOI18N
            properties[PROPERTY_mouseListeners] = new PropertyDescriptor ( "mouseListeners", ika.gui.MapComponent.class, "getMouseListeners", null ); // NOI18N
            properties[PROPERTY_mouseMotionListeners] = new PropertyDescriptor ( "mouseMotionListeners", ika.gui.MapComponent.class, "getMouseMotionListeners", null ); // NOI18N
            properties[PROPERTY_mousePosition] = new PropertyDescriptor ( "mousePosition", ika.gui.MapComponent.class, "getMousePosition", null ); // NOI18N
            properties[PROPERTY_mouseWheelListeners] = new PropertyDescriptor ( "mouseWheelListeners", ika.gui.MapComponent.class, "getMouseWheelListeners", null ); // NOI18N
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", ika.gui.MapComponent.class, "getName", "setName" ); // NOI18N
            properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", ika.gui.MapComponent.class, "getNextFocusableComponent", "setNextFocusableComponent" ); // NOI18N
            properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", ika.gui.MapComponent.class, "isOpaque", "setOpaque" ); // NOI18N
            properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", ika.gui.MapComponent.class, "isOptimizedDrawingEnabled", null ); // NOI18N
            properties[PROPERTY_pageFormat] = new PropertyDescriptor ( "pageFormat", ika.gui.MapComponent.class, "getPageFormat", null ); // NOI18N
            properties[PROPERTY_paintingForPrint] = new PropertyDescriptor ( "paintingForPrint", ika.gui.MapComponent.class, "isPaintingForPrint", null ); // NOI18N
            properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", ika.gui.MapComponent.class, "isPaintingTile", null ); // NOI18N
            properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", ika.gui.MapComponent.class, "getParent", null ); // NOI18N
            properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", ika.gui.MapComponent.class, "getPeer", null ); // NOI18N
            properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", ika.gui.MapComponent.class, "getPreferredSize", "setPreferredSize" ); // NOI18N
            properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", ika.gui.MapComponent.class, "isPreferredSizeSet", null ); // NOI18N
            properties[PROPERTY_propertyChangeListeners] = new PropertyDescriptor ( "propertyChangeListeners", ika.gui.MapComponent.class, "getPropertyChangeListeners", null ); // NOI18N
            properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", ika.gui.MapComponent.class, "getRegisteredKeyStrokes", null ); // NOI18N
            properties[PROPERTY_renderParamsProvider] = new PropertyDescriptor ( "renderParamsProvider", ika.gui.MapComponent.class, "getRenderParamsProvider", "setRenderParamsProvider" ); // NOI18N
            properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", ika.gui.MapComponent.class, "isRequestFocusEnabled", "setRequestFocusEnabled" ); // NOI18N
            properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", ika.gui.MapComponent.class, "getRootPane", null ); // NOI18N
            properties[PROPERTY_scaleFactor] = new PropertyDescriptor ( "scaleFactor", ika.gui.MapComponent.class, "getScaleFactor", "setScaleFactor" ); // NOI18N
            properties[PROPERTY_scaleNumber] = new PropertyDescriptor ( "scaleNumber", ika.gui.MapComponent.class, "getScaleNumber", null ); // NOI18N
            properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", ika.gui.MapComponent.class, "isShowing", null ); // NOI18N
            properties[PROPERTY_size] = new PropertyDescriptor ( "size", ika.gui.MapComponent.class, "getSize", "setSize" ); // NOI18N
            properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", ika.gui.MapComponent.class, "getToolkit", null ); // NOI18N
            properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", ika.gui.MapComponent.class, "getToolTipText", "setToolTipText" ); // NOI18N
            properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", ika.gui.MapComponent.class, "getTopLevelAncestor", null ); // NOI18N
            properties[PROPERTY_transferHandler] = new PropertyDescriptor ( "transferHandler", ika.gui.MapComponent.class, "getTransferHandler", "setTransferHandler" ); // NOI18N
            properties[PROPERTY_transformForSelectedObjects] = new PropertyDescriptor ( "transformForSelectedObjects", ika.gui.MapComponent.class, "getTransformForSelectedObjects", "setTransformForSelectedObjects" ); // NOI18N
            properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", ika.gui.MapComponent.class, "getTreeLock", null ); // NOI18N
            properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", ika.gui.MapComponent.class, "getUIClassID", null ); // NOI18N
            properties[PROPERTY_undo] = new PropertyDescriptor ( "undo", ika.gui.MapComponent.class, "getUndo", null ); // NOI18N
            properties[PROPERTY_undoRedoState] = new PropertyDescriptor ( "undoRedoState", ika.gui.MapComponent.class, "getUndoRedoState", null ); // NOI18N
            properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", ika.gui.MapComponent.class, "isValid", null ); // NOI18N
            properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", ika.gui.MapComponent.class, "isValidateRoot", null ); // NOI18N
            properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", ika.gui.MapComponent.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" ); // NOI18N
            properties[PROPERTY_vetoableChangeListeners] = new PropertyDescriptor ( "vetoableChangeListeners", ika.gui.MapComponent.class, "getVetoableChangeListeners", null ); // NOI18N
            properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", ika.gui.MapComponent.class, "isVisible", "setVisible" ); // NOI18N
            properties[PROPERTY_visibleArea] = new PropertyDescriptor ( "visibleArea", ika.gui.MapComponent.class, "getVisibleArea", null ); // NOI18N
            properties[PROPERTY_visibleHeight] = new PropertyDescriptor ( "visibleHeight", ika.gui.MapComponent.class, "getVisibleHeight", null ); // NOI18N
            properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", ika.gui.MapComponent.class, "getVisibleRect", null ); // NOI18N
            properties[PROPERTY_visibleWidth] = new PropertyDescriptor ( "visibleWidth", ika.gui.MapComponent.class, "getVisibleWidth", null ); // NOI18N
            properties[PROPERTY_width] = new PropertyDescriptor ( "width", ika.gui.MapComponent.class, "getWidth", null ); // NOI18N
            properties[PROPERTY_x] = new PropertyDescriptor ( "x", ika.gui.MapComponent.class, "getX", null ); // NOI18N
            properties[PROPERTY_y] = new PropertyDescriptor ( "y", ika.gui.MapComponent.class, "getY", null ); // NOI18N
            properties[PROPERTY_zoomWithMouseWheel] = new PropertyDescriptor ( "zoomWithMouseWheel", ika.gui.MapComponent.class, "isZoomWithMouseWheel", "setZoomWithMouseWheel" ); // NOI18N
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        return properties;     }//GEN-LAST:Properties
    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_ancestorListener = 0;
    private static final int EVENT_componentListener = 1;
    private static final int EVENT_containerListener = 2;
    private static final int EVENT_focusListener = 3;
    private static final int EVENT_hierarchyBoundsListener = 4;
    private static final int EVENT_hierarchyListener = 5;
    private static final int EVENT_inputMethodListener = 6;
    private static final int EVENT_keyListener = 7;
    private static final int EVENT_mouseListener = 8;
    private static final int EVENT_mouseMotionListener = 9;
    private static final int EVENT_mouseWheelListener = 10;
    private static final int EVENT_propertyChangeListener = 11;
    private static final int EVENT_vetoableChangeListener = 12;

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[13];
    
        try {
            eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[] {"ancestorAdded", "ancestorRemoved", "ancestorMoved"}, "addAncestorListener", "removeAncestorListener" ); // NOI18N
            eventSets[EVENT_componentListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "componentListener", java.awt.event.ComponentListener.class, new String[] {"componentResized", "componentMoved", "componentShown", "componentHidden"}, "addComponentListener", "removeComponentListener" ); // NOI18N
            eventSets[EVENT_containerListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "containerListener", java.awt.event.ContainerListener.class, new String[] {"componentAdded", "componentRemoved"}, "addContainerListener", "removeContainerListener" ); // NOI18N
            eventSets[EVENT_focusListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "focusListener", java.awt.event.FocusListener.class, new String[] {"focusGained", "focusLost"}, "addFocusListener", "removeFocusListener" ); // NOI18N
            eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[] {"ancestorMoved", "ancestorResized"}, "addHierarchyBoundsListener", "removeHierarchyBoundsListener" ); // NOI18N
            eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[] {"hierarchyChanged"}, "addHierarchyListener", "removeHierarchyListener" ); // NOI18N
            eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[] {"inputMethodTextChanged", "caretPositionChanged"}, "addInputMethodListener", "removeInputMethodListener" ); // NOI18N
            eventSets[EVENT_keyListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "keyListener", java.awt.event.KeyListener.class, new String[] {"keyTyped", "keyPressed", "keyReleased"}, "addKeyListener", "removeKeyListener" ); // NOI18N
            eventSets[EVENT_mouseListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "mouseListener", java.awt.event.MouseListener.class, new String[] {"mouseClicked", "mousePressed", "mouseReleased", "mouseEntered", "mouseExited"}, "addMouseListener", "removeMouseListener" ); // NOI18N
            eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[] {"mouseDragged", "mouseMoved"}, "addMouseMotionListener", "removeMouseMotionListener" ); // NOI18N
            eventSets[EVENT_mouseWheelListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "mouseWheelListener", java.awt.event.MouseWheelListener.class, new String[] {"mouseWheelMoved"}, "addMouseWheelListener", "removeMouseWheelListener" ); // NOI18N
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
            eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( ika.gui.MapComponent.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" ); // NOI18N
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;     }//GEN-LAST:Events
    // Method identifiers//GEN-FIRST:Methods
    private static final int METHOD_action0 = 0;
    private static final int METHOD_add1 = 1;
    private static final int METHOD_add2 = 2;
    private static final int METHOD_add3 = 3;
    private static final int METHOD_add4 = 4;
    private static final int METHOD_add5 = 5;
    private static final int METHOD_add6 = 6;
    private static final int METHOD_addGeoObject7 = 7;
    private static final int METHOD_addMouseMotionListener8 = 8;
    private static final int METHOD_addNotify9 = 9;
    private static final int METHOD_addPropertyChangeListener10 = 10;
    private static final int METHOD_addScaleChangeHandler11 = 11;
    private static final int METHOD_addUndo12 = 12;
    private static final int METHOD_applyComponentOrientation13 = 13;
    private static final int METHOD_applyUndoRedoState14 = 14;
    private static final int METHOD_areFocusTraversalKeysSet15 = 15;
    private static final int METHOD_bounds16 = 16;
    private static final int METHOD_canUndo17 = 17;
    private static final int METHOD_centerOnPoint18 = 18;
    private static final int METHOD_centerOnPoint19 = 19;
    private static final int METHOD_checkImage20 = 20;
    private static final int METHOD_checkImage21 = 21;
    private static final int METHOD_cloneAndMoveSelectedGeoObjects22 = 22;
    private static final int METHOD_computeVisibleRect23 = 23;
    private static final int METHOD_contains24 = 24;
    private static final int METHOD_contains25 = 25;
    private static final int METHOD_countComponents26 = 26;
    private static final int METHOD_createImage27 = 27;
    private static final int METHOD_createImage28 = 28;
    private static final int METHOD_createToolTip29 = 29;
    private static final int METHOD_createVolatileImage30 = 30;
    private static final int METHOD_createVolatileImage31 = 31;
    private static final int METHOD_deformSelectedGeoObjects32 = 32;
    private static final int METHOD_deliverEvent33 = 33;
    private static final int METHOD_deselectAllAndAddChildren34 = 34;
    private static final int METHOD_deselectAllGeoObjects35 = 35;
    private static final int METHOD_disable36 = 36;
    private static final int METHOD_dispatchEvent37 = 37;
    private static final int METHOD_doLayout38 = 38;
    private static final int METHOD_enable39 = 39;
    private static final int METHOD_enable40 = 40;
    private static final int METHOD_enableInputMethods41 = 41;
    private static final int METHOD_findComponentAt42 = 42;
    private static final int METHOD_findComponentAt43 = 43;
    private static final int METHOD_firePropertyChange44 = 44;
    private static final int METHOD_firePropertyChange45 = 45;
    private static final int METHOD_firePropertyChange46 = 46;
    private static final int METHOD_firePropertyChange47 = 47;
    private static final int METHOD_firePropertyChange48 = 48;
    private static final int METHOD_firePropertyChange49 = 49;
    private static final int METHOD_firePropertyChange50 = 50;
    private static final int METHOD_firePropertyChange51 = 51;
    private static final int METHOD_getActionForKeyStroke52 = 52;
    private static final int METHOD_getBaseline53 = 53;
    private static final int METHOD_getBounds54 = 54;
    private static final int METHOD_getClientProperty55 = 55;
    private static final int METHOD_getComponentAt56 = 56;
    private static final int METHOD_getComponentAt57 = 57;
    private static final int METHOD_getComponentZOrder58 = 58;
    private static final int METHOD_getConditionForKeyStroke59 = 59;
    private static final int METHOD_getDefaultLocale60 = 60;
    private static final int METHOD_getFocusTraversalKeys61 = 61;
    private static final int METHOD_getFontMetrics62 = 62;
    private static final int METHOD_getInsets63 = 63;
    private static final int METHOD_getListeners64 = 64;
    private static final int METHOD_getLocation65 = 65;
    private static final int METHOD_getMousePosition66 = 66;
    private static final int METHOD_getObjectAtPosition67 = 67;
    private static final int METHOD_getPopupLocation68 = 68;
    private static final int METHOD_getPropertyChangeListeners69 = 69;
    private static final int METHOD_getSize70 = 70;
    private static final int METHOD_getToolTipLocation71 = 71;
    private static final int METHOD_getToolTipText72 = 72;
    private static final int METHOD_gotFocus73 = 73;
    private static final int METHOD_grabFocus74 = 74;
    private static final int METHOD_handleEvent75 = 75;
    private static final int METHOD_hasFocus76 = 76;
    private static final int METHOD_hasSelectedGeoObjects77 = 77;
    private static final int METHOD_hasVisibleGeoObjects78 = 78;
    private static final int METHOD_hide79 = 79;
    private static final int METHOD_imageUpdate80 = 80;
    private static final int METHOD_insets81 = 81;
    private static final int METHOD_inside82 = 82;
    private static final int METHOD_invalidate83 = 83;
    private static final int METHOD_isAncestorOf84 = 84;
    private static final int METHOD_isFocusCycleRoot85 = 85;
    private static final int METHOD_isLightweightComponent86 = 86;
    private static final int METHOD_isObjectVisibleOnMap87 = 87;
    private static final int METHOD_isObjectVisibleOnMap88 = 88;
    private static final int METHOD_keyDown89 = 89;
    private static final int METHOD_keyUp90 = 90;
    private static final int METHOD_layout91 = 91;
    private static final int METHOD_list92 = 92;
    private static final int METHOD_list93 = 93;
    private static final int METHOD_list94 = 94;
    private static final int METHOD_list95 = 95;
    private static final int METHOD_list96 = 96;
    private static final int METHOD_locate97 = 97;
    private static final int METHOD_location98 = 98;
    private static final int METHOD_lostFocus99 = 99;
    private static final int METHOD_mapEvent100 = 100;
    private static final int METHOD_minimumSize101 = 101;
    private static final int METHOD_mouseDown102 = 102;
    private static final int METHOD_mouseDrag103 = 103;
    private static final int METHOD_mouseEnter104 = 104;
    private static final int METHOD_mouseExit105 = 105;
    private static final int METHOD_mouseMove106 = 106;
    private static final int METHOD_mouseUp107 = 107;
    private static final int METHOD_move108 = 108;
    private static final int METHOD_moveSelectedGeoObjects109 = 109;
    private static final int METHOD_nextFocus110 = 110;
    private static final int METHOD_offsetVisibleArea111 = 111;
    private static final int METHOD_paint112 = 112;
    private static final int METHOD_paintAll113 = 113;
    private static final int METHOD_paintComponents114 = 114;
    private static final int METHOD_paintImmediately115 = 115;
    private static final int METHOD_paintImmediately116 = 116;
    private static final int METHOD_paintMap117 = 117;
    private static final int METHOD_postEvent118 = 118;
    private static final int METHOD_preferredSize119 = 119;
    private static final int METHOD_prepareImage120 = 120;
    private static final int METHOD_prepareImage121 = 121;
    private static final int METHOD_print122 = 122;
    private static final int METHOD_printAll123 = 123;
    private static final int METHOD_printComponents124 = 124;
    private static final int METHOD_putClientProperty125 = 125;
    private static final int METHOD_redo126 = 126;
    private static final int METHOD_registerKeyboardAction127 = 127;
    private static final int METHOD_registerKeyboardAction128 = 128;
    private static final int METHOD_registerUndoMenuItems129 = 129;
    private static final int METHOD_remove130 = 130;
    private static final int METHOD_remove131 = 131;
    private static final int METHOD_remove132 = 132;
    private static final int METHOD_removeAll133 = 133;
    private static final int METHOD_removeAllGeoObjects134 = 134;
    private static final int METHOD_removeMouseMotionListener135 = 135;
    private static final int METHOD_removeNotify136 = 136;
    private static final int METHOD_removePropertyChangeListener137 = 137;
    private static final int METHOD_removeScaleChangeHandler138 = 138;
    private static final int METHOD_removeSelectedGeoObjects139 = 139;
    private static final int METHOD_repaint140 = 140;
    private static final int METHOD_repaint141 = 141;
    private static final int METHOD_repaint142 = 142;
    private static final int METHOD_repaint143 = 143;
    private static final int METHOD_repaint144 = 144;
    private static final int METHOD_requestDefaultFocus145 = 145;
    private static final int METHOD_requestFocus146 = 146;
    private static final int METHOD_requestFocus147 = 147;
    private static final int METHOD_requestFocusInWindow148 = 148;
    private static final int METHOD_resetKeyboardActions149 = 149;
    private static final int METHOD_resetUndo150 = 150;
    private static final int METHOD_reshape151 = 151;
    private static final int METHOD_resize152 = 152;
    private static final int METHOD_resize153 = 153;
    private static final int METHOD_revalidate154 = 154;
    private static final int METHOD_scaleSelectedGeoObjects155 = 155;
    private static final int METHOD_scrollRectToVisible156 = 156;
    private static final int METHOD_selectAllGeoObjects157 = 157;
    private static final int METHOD_selectByPoint158 = 158;
    private static final int METHOD_selectByRectangle159 = 159;
    private static final int METHOD_setBounds160 = 160;
    private static final int METHOD_setComponentZOrder161 = 161;
    private static final int METHOD_setDefaultLocale162 = 162;
    private static final int METHOD_shiftGraphics2DByBorderWidth163 = 163;
    private static final int METHOD_show164 = 164;
    private static final int METHOD_show165 = 165;
    private static final int METHOD_showAll166 = 166;
    private static final int METHOD_size167 = 167;
    private static final int METHOD_toString168 = 168;
    private static final int METHOD_transferFocus169 = 169;
    private static final int METHOD_transferFocusBackward170 = 170;
    private static final int METHOD_transferFocusDownCycle171 = 171;
    private static final int METHOD_transferFocusUpCycle172 = 172;
    private static final int METHOD_transformSelectedGeoObjects173 = 173;
    private static final int METHOD_undo174 = 174;
    private static final int METHOD_unregisterKeyboardAction175 = 175;
    private static final int METHOD_update176 = 176;
    private static final int METHOD_updateUI177 = 177;
    private static final int METHOD_validate178 = 178;
    private static final int METHOD_zoom179 = 179;
    private static final int METHOD_zoomIn180 = 180;
    private static final int METHOD_zoomIn181 = 181;
    private static final int METHOD_zoomOnPoint182 = 182;
    private static final int METHOD_zoomOnRectangle183 = 183;
    private static final int METHOD_zoomOut184 = 184;
    private static final int METHOD_zoomOut185 = 185;

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[186];
    
        try {
            methods[METHOD_action0] = new MethodDescriptor(java.awt.Component.class.getMethod("action", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_action0].setDisplayName ( "" );
            methods[METHOD_add1] = new MethodDescriptor(java.awt.Component.class.getMethod("add", new Class[] {java.awt.PopupMenu.class})); // NOI18N
            methods[METHOD_add1].setDisplayName ( "" );
            methods[METHOD_add2] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_add2].setDisplayName ( "" );
            methods[METHOD_add3] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.lang.String.class, java.awt.Component.class})); // NOI18N
            methods[METHOD_add3].setDisplayName ( "" );
            methods[METHOD_add4] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_add4].setDisplayName ( "" );
            methods[METHOD_add5] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_add5].setDisplayName ( "" );
            methods[METHOD_add6] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, java.lang.Object.class, int.class})); // NOI18N
            methods[METHOD_add6].setDisplayName ( "" );
            methods[METHOD_addGeoObject7] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("addGeoObject", new Class[] {ika.geo.GeoObject.class, boolean.class})); // NOI18N
            methods[METHOD_addGeoObject7].setDisplayName ( "" );
            methods[METHOD_addMouseMotionListener8] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("addMouseMotionListener", new Class[] {ika.map.tools.MapToolMouseMotionListener.class})); // NOI18N
            methods[METHOD_addMouseMotionListener8].setDisplayName ( "" );
            methods[METHOD_addNotify9] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("addNotify", new Class[] {})); // NOI18N
            methods[METHOD_addNotify9].setDisplayName ( "" );
            methods[METHOD_addPropertyChangeListener10] = new MethodDescriptor(java.awt.Container.class.getMethod("addPropertyChangeListener", new Class[] {java.lang.String.class, java.beans.PropertyChangeListener.class})); // NOI18N
            methods[METHOD_addPropertyChangeListener10].setDisplayName ( "" );
            methods[METHOD_addScaleChangeHandler11] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("addScaleChangeHandler", new Class[] {ika.gui.ScaleChangeHandler.class})); // NOI18N
            methods[METHOD_addScaleChangeHandler11].setDisplayName ( "" );
            methods[METHOD_addUndo12] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("addUndo", new Class[] {java.lang.String.class})); // NOI18N
            methods[METHOD_addUndo12].setDisplayName ( "" );
            methods[METHOD_applyComponentOrientation13] = new MethodDescriptor(java.awt.Container.class.getMethod("applyComponentOrientation", new Class[] {java.awt.ComponentOrientation.class})); // NOI18N
            methods[METHOD_applyComponentOrientation13].setDisplayName ( "" );
            methods[METHOD_applyUndoRedoState14] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("applyUndoRedoState", new Class[] {java.lang.Object.class})); // NOI18N
            methods[METHOD_applyUndoRedoState14].setDisplayName ( "" );
            methods[METHOD_areFocusTraversalKeysSet15] = new MethodDescriptor(java.awt.Container.class.getMethod("areFocusTraversalKeysSet", new Class[] {int.class})); // NOI18N
            methods[METHOD_areFocusTraversalKeysSet15].setDisplayName ( "" );
            methods[METHOD_bounds16] = new MethodDescriptor(java.awt.Component.class.getMethod("bounds", new Class[] {})); // NOI18N
            methods[METHOD_bounds16].setDisplayName ( "" );
            methods[METHOD_canUndo17] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("canUndo", new Class[] {})); // NOI18N
            methods[METHOD_canUndo17].setDisplayName ( "" );
            methods[METHOD_centerOnPoint18] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("centerOnPoint", new Class[] {java.awt.geom.Point2D.Double.class})); // NOI18N
            methods[METHOD_centerOnPoint18].setDisplayName ( "" );
            methods[METHOD_centerOnPoint19] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("centerOnPoint", new Class[] {double.class, double.class})); // NOI18N
            methods[METHOD_centerOnPoint19].setDisplayName ( "" );
            methods[METHOD_checkImage20] = new MethodDescriptor(java.awt.Component.class.getMethod("checkImage", new Class[] {java.awt.Image.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_checkImage20].setDisplayName ( "" );
            methods[METHOD_checkImage21] = new MethodDescriptor(java.awt.Component.class.getMethod("checkImage", new Class[] {java.awt.Image.class, int.class, int.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_checkImage21].setDisplayName ( "" );
            methods[METHOD_cloneAndMoveSelectedGeoObjects22] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("cloneAndMoveSelectedGeoObjects", new Class[] {double.class, double.class})); // NOI18N
            methods[METHOD_cloneAndMoveSelectedGeoObjects22].setDisplayName ( "" );
            methods[METHOD_computeVisibleRect23] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("computeVisibleRect", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_computeVisibleRect23].setDisplayName ( "" );
            methods[METHOD_contains24] = new MethodDescriptor(java.awt.Component.class.getMethod("contains", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_contains24].setDisplayName ( "" );
            methods[METHOD_contains25] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("contains", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_contains25].setDisplayName ( "" );
            methods[METHOD_countComponents26] = new MethodDescriptor(java.awt.Container.class.getMethod("countComponents", new Class[] {})); // NOI18N
            methods[METHOD_countComponents26].setDisplayName ( "" );
            methods[METHOD_createImage27] = new MethodDescriptor(java.awt.Component.class.getMethod("createImage", new Class[] {java.awt.image.ImageProducer.class})); // NOI18N
            methods[METHOD_createImage27].setDisplayName ( "" );
            methods[METHOD_createImage28] = new MethodDescriptor(java.awt.Component.class.getMethod("createImage", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_createImage28].setDisplayName ( "" );
            methods[METHOD_createToolTip29] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("createToolTip", new Class[] {})); // NOI18N
            methods[METHOD_createToolTip29].setDisplayName ( "" );
            methods[METHOD_createVolatileImage30] = new MethodDescriptor(java.awt.Component.class.getMethod("createVolatileImage", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_createVolatileImage30].setDisplayName ( "" );
            methods[METHOD_createVolatileImage31] = new MethodDescriptor(java.awt.Component.class.getMethod("createVolatileImage", new Class[] {int.class, int.class, java.awt.ImageCapabilities.class})); // NOI18N
            methods[METHOD_createVolatileImage31].setDisplayName ( "" );
            methods[METHOD_deformSelectedGeoObjects32] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("deformSelectedGeoObjects", new Class[] {java.awt.geom.Rectangle2D.class})); // NOI18N
            methods[METHOD_deformSelectedGeoObjects32].setDisplayName ( "" );
            methods[METHOD_deliverEvent33] = new MethodDescriptor(java.awt.Container.class.getMethod("deliverEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_deliverEvent33].setDisplayName ( "" );
            methods[METHOD_deselectAllAndAddChildren34] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("deselectAllAndAddChildren", new Class[] {ika.geo.GeoSet.class})); // NOI18N
            methods[METHOD_deselectAllAndAddChildren34].setDisplayName ( "" );
            methods[METHOD_deselectAllGeoObjects35] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("deselectAllGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_deselectAllGeoObjects35].setDisplayName ( "" );
            methods[METHOD_disable36] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("disable", new Class[] {})); // NOI18N
            methods[METHOD_disable36].setDisplayName ( "" );
            methods[METHOD_dispatchEvent37] = new MethodDescriptor(java.awt.Component.class.getMethod("dispatchEvent", new Class[] {java.awt.AWTEvent.class})); // NOI18N
            methods[METHOD_dispatchEvent37].setDisplayName ( "" );
            methods[METHOD_doLayout38] = new MethodDescriptor(java.awt.Container.class.getMethod("doLayout", new Class[] {})); // NOI18N
            methods[METHOD_doLayout38].setDisplayName ( "" );
            methods[METHOD_enable39] = new MethodDescriptor(java.awt.Component.class.getMethod("enable", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_enable39].setDisplayName ( "" );
            methods[METHOD_enable40] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("enable", new Class[] {})); // NOI18N
            methods[METHOD_enable40].setDisplayName ( "" );
            methods[METHOD_enableInputMethods41] = new MethodDescriptor(java.awt.Component.class.getMethod("enableInputMethods", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_enableInputMethods41].setDisplayName ( "" );
            methods[METHOD_findComponentAt42] = new MethodDescriptor(java.awt.Container.class.getMethod("findComponentAt", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_findComponentAt42].setDisplayName ( "" );
            methods[METHOD_findComponentAt43] = new MethodDescriptor(java.awt.Container.class.getMethod("findComponentAt", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_findComponentAt43].setDisplayName ( "" );
            methods[METHOD_firePropertyChange44] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, byte.class, byte.class})); // NOI18N
            methods[METHOD_firePropertyChange44].setDisplayName ( "" );
            methods[METHOD_firePropertyChange45] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, short.class, short.class})); // NOI18N
            methods[METHOD_firePropertyChange45].setDisplayName ( "" );
            methods[METHOD_firePropertyChange46] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, long.class, long.class})); // NOI18N
            methods[METHOD_firePropertyChange46].setDisplayName ( "" );
            methods[METHOD_firePropertyChange47] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, float.class, float.class})); // NOI18N
            methods[METHOD_firePropertyChange47].setDisplayName ( "" );
            methods[METHOD_firePropertyChange48] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, double.class, double.class})); // NOI18N
            methods[METHOD_firePropertyChange48].setDisplayName ( "" );
            methods[METHOD_firePropertyChange49] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, boolean.class, boolean.class})); // NOI18N
            methods[METHOD_firePropertyChange49].setDisplayName ( "" );
            methods[METHOD_firePropertyChange50] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, int.class, int.class})); // NOI18N
            methods[METHOD_firePropertyChange50].setDisplayName ( "" );
            methods[METHOD_firePropertyChange51] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, char.class, char.class})); // NOI18N
            methods[METHOD_firePropertyChange51].setDisplayName ( "" );
            methods[METHOD_getActionForKeyStroke52] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getActionForKeyStroke", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_getActionForKeyStroke52].setDisplayName ( "" );
            methods[METHOD_getBaseline53] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getBaseline", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_getBaseline53].setDisplayName ( "" );
            methods[METHOD_getBounds54] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getBounds", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_getBounds54].setDisplayName ( "" );
            methods[METHOD_getClientProperty55] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getClientProperty", new Class[] {java.lang.Object.class})); // NOI18N
            methods[METHOD_getClientProperty55].setDisplayName ( "" );
            methods[METHOD_getComponentAt56] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentAt", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_getComponentAt56].setDisplayName ( "" );
            methods[METHOD_getComponentAt57] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentAt", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_getComponentAt57].setDisplayName ( "" );
            methods[METHOD_getComponentZOrder58] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentZOrder", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getComponentZOrder58].setDisplayName ( "" );
            methods[METHOD_getConditionForKeyStroke59] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getConditionForKeyStroke", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_getConditionForKeyStroke59].setDisplayName ( "" );
            methods[METHOD_getDefaultLocale60] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getDefaultLocale", new Class[] {})); // NOI18N
            methods[METHOD_getDefaultLocale60].setDisplayName ( "" );
            methods[METHOD_getFocusTraversalKeys61] = new MethodDescriptor(java.awt.Container.class.getMethod("getFocusTraversalKeys", new Class[] {int.class})); // NOI18N
            methods[METHOD_getFocusTraversalKeys61].setDisplayName ( "" );
            methods[METHOD_getFontMetrics62] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getFontMetrics", new Class[] {java.awt.Font.class})); // NOI18N
            methods[METHOD_getFontMetrics62].setDisplayName ( "" );
            methods[METHOD_getInsets63] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getInsets", new Class[] {java.awt.Insets.class})); // NOI18N
            methods[METHOD_getInsets63].setDisplayName ( "" );
            methods[METHOD_getListeners64] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getListeners", new Class[] {java.lang.Class.class})); // NOI18N
            methods[METHOD_getListeners64].setDisplayName ( "" );
            methods[METHOD_getLocation65] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getLocation", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_getLocation65].setDisplayName ( "" );
            methods[METHOD_getMousePosition66] = new MethodDescriptor(java.awt.Container.class.getMethod("getMousePosition", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_getMousePosition66].setDisplayName ( "" );
            methods[METHOD_getObjectAtPosition67] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("getObjectAtPosition", new Class[] {java.awt.geom.Point2D.class, double.class, boolean.class, boolean.class})); // NOI18N
            methods[METHOD_getObjectAtPosition67].setDisplayName ( "" );
            methods[METHOD_getPopupLocation68] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getPopupLocation", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getPopupLocation68].setDisplayName ( "" );
            methods[METHOD_getPropertyChangeListeners69] = new MethodDescriptor(java.awt.Component.class.getMethod("getPropertyChangeListeners", new Class[] {java.lang.String.class})); // NOI18N
            methods[METHOD_getPropertyChangeListeners69].setDisplayName ( "" );
            methods[METHOD_getSize70] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getSize", new Class[] {java.awt.Dimension.class})); // NOI18N
            methods[METHOD_getSize70].setDisplayName ( "" );
            methods[METHOD_getToolTipLocation71] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getToolTipLocation", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getToolTipLocation71].setDisplayName ( "" );
            methods[METHOD_getToolTipText72] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getToolTipText", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getToolTipText72].setDisplayName ( "" );
            methods[METHOD_gotFocus73] = new MethodDescriptor(java.awt.Component.class.getMethod("gotFocus", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_gotFocus73].setDisplayName ( "" );
            methods[METHOD_grabFocus74] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("grabFocus", new Class[] {})); // NOI18N
            methods[METHOD_grabFocus74].setDisplayName ( "" );
            methods[METHOD_handleEvent75] = new MethodDescriptor(java.awt.Component.class.getMethod("handleEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_handleEvent75].setDisplayName ( "" );
            methods[METHOD_hasFocus76] = new MethodDescriptor(java.awt.Component.class.getMethod("hasFocus", new Class[] {})); // NOI18N
            methods[METHOD_hasFocus76].setDisplayName ( "" );
            methods[METHOD_hasSelectedGeoObjects77] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("hasSelectedGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_hasSelectedGeoObjects77].setDisplayName ( "" );
            methods[METHOD_hasVisibleGeoObjects78] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("hasVisibleGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_hasVisibleGeoObjects78].setDisplayName ( "" );
            methods[METHOD_hide79] = new MethodDescriptor(java.awt.Component.class.getMethod("hide", new Class[] {})); // NOI18N
            methods[METHOD_hide79].setDisplayName ( "" );
            methods[METHOD_imageUpdate80] = new MethodDescriptor(java.awt.Component.class.getMethod("imageUpdate", new Class[] {java.awt.Image.class, int.class, int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_imageUpdate80].setDisplayName ( "" );
            methods[METHOD_insets81] = new MethodDescriptor(java.awt.Container.class.getMethod("insets", new Class[] {})); // NOI18N
            methods[METHOD_insets81].setDisplayName ( "" );
            methods[METHOD_inside82] = new MethodDescriptor(java.awt.Component.class.getMethod("inside", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_inside82].setDisplayName ( "" );
            methods[METHOD_invalidate83] = new MethodDescriptor(java.awt.Container.class.getMethod("invalidate", new Class[] {})); // NOI18N
            methods[METHOD_invalidate83].setDisplayName ( "" );
            methods[METHOD_isAncestorOf84] = new MethodDescriptor(java.awt.Container.class.getMethod("isAncestorOf", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_isAncestorOf84].setDisplayName ( "" );
            methods[METHOD_isFocusCycleRoot85] = new MethodDescriptor(java.awt.Container.class.getMethod("isFocusCycleRoot", new Class[] {java.awt.Container.class})); // NOI18N
            methods[METHOD_isFocusCycleRoot85].setDisplayName ( "" );
            methods[METHOD_isLightweightComponent86] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("isLightweightComponent", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_isLightweightComponent86].setDisplayName ( "" );
            methods[METHOD_isObjectVisibleOnMap87] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("isObjectVisibleOnMap", new Class[] {ika.geo.GeoObject.class})); // NOI18N
            methods[METHOD_isObjectVisibleOnMap87].setDisplayName ( "" );
            methods[METHOD_isObjectVisibleOnMap88] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("isObjectVisibleOnMap", new Class[] {ika.geo.GeoObject.class, boolean.class})); // NOI18N
            methods[METHOD_isObjectVisibleOnMap88].setDisplayName ( "" );
            methods[METHOD_keyDown89] = new MethodDescriptor(java.awt.Component.class.getMethod("keyDown", new Class[] {java.awt.Event.class, int.class})); // NOI18N
            methods[METHOD_keyDown89].setDisplayName ( "" );
            methods[METHOD_keyUp90] = new MethodDescriptor(java.awt.Component.class.getMethod("keyUp", new Class[] {java.awt.Event.class, int.class})); // NOI18N
            methods[METHOD_keyUp90].setDisplayName ( "" );
            methods[METHOD_layout91] = new MethodDescriptor(java.awt.Container.class.getMethod("layout", new Class[] {})); // NOI18N
            methods[METHOD_layout91].setDisplayName ( "" );
            methods[METHOD_list92] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {})); // NOI18N
            methods[METHOD_list92].setDisplayName ( "" );
            methods[METHOD_list93] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {java.io.PrintStream.class})); // NOI18N
            methods[METHOD_list93].setDisplayName ( "" );
            methods[METHOD_list94] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {java.io.PrintWriter.class})); // NOI18N
            methods[METHOD_list94].setDisplayName ( "" );
            methods[METHOD_list95] = new MethodDescriptor(java.awt.Container.class.getMethod("list", new Class[] {java.io.PrintStream.class, int.class})); // NOI18N
            methods[METHOD_list95].setDisplayName ( "" );
            methods[METHOD_list96] = new MethodDescriptor(java.awt.Container.class.getMethod("list", new Class[] {java.io.PrintWriter.class, int.class})); // NOI18N
            methods[METHOD_list96].setDisplayName ( "" );
            methods[METHOD_locate97] = new MethodDescriptor(java.awt.Container.class.getMethod("locate", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_locate97].setDisplayName ( "" );
            methods[METHOD_location98] = new MethodDescriptor(java.awt.Component.class.getMethod("location", new Class[] {})); // NOI18N
            methods[METHOD_location98].setDisplayName ( "" );
            methods[METHOD_lostFocus99] = new MethodDescriptor(java.awt.Component.class.getMethod("lostFocus", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_lostFocus99].setDisplayName ( "" );
            methods[METHOD_mapEvent100] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("mapEvent", new Class[] {ika.geo.MapEvent.class})); // NOI18N
            methods[METHOD_mapEvent100].setDisplayName ( "" );
            methods[METHOD_minimumSize101] = new MethodDescriptor(java.awt.Container.class.getMethod("minimumSize", new Class[] {})); // NOI18N
            methods[METHOD_minimumSize101].setDisplayName ( "" );
            methods[METHOD_mouseDown102] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseDown", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseDown102].setDisplayName ( "" );
            methods[METHOD_mouseDrag103] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseDrag", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseDrag103].setDisplayName ( "" );
            methods[METHOD_mouseEnter104] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseEnter", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseEnter104].setDisplayName ( "" );
            methods[METHOD_mouseExit105] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseExit", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseExit105].setDisplayName ( "" );
            methods[METHOD_mouseMove106] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseMove", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseMove106].setDisplayName ( "" );
            methods[METHOD_mouseUp107] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseUp", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseUp107].setDisplayName ( "" );
            methods[METHOD_move108] = new MethodDescriptor(java.awt.Component.class.getMethod("move", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_move108].setDisplayName ( "" );
            methods[METHOD_moveSelectedGeoObjects109] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("moveSelectedGeoObjects", new Class[] {double.class, double.class})); // NOI18N
            methods[METHOD_moveSelectedGeoObjects109].setDisplayName ( "" );
            methods[METHOD_nextFocus110] = new MethodDescriptor(java.awt.Component.class.getMethod("nextFocus", new Class[] {})); // NOI18N
            methods[METHOD_nextFocus110].setDisplayName ( "" );
            methods[METHOD_offsetVisibleArea111] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("offsetVisibleArea", new Class[] {double.class, double.class})); // NOI18N
            methods[METHOD_offsetVisibleArea111].setDisplayName ( "" );
            methods[METHOD_paint112] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("paint", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paint112].setDisplayName ( "" );
            methods[METHOD_paintAll113] = new MethodDescriptor(java.awt.Component.class.getMethod("paintAll", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paintAll113].setDisplayName ( "" );
            methods[METHOD_paintComponents114] = new MethodDescriptor(java.awt.Container.class.getMethod("paintComponents", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paintComponents114].setDisplayName ( "" );
            methods[METHOD_paintImmediately115] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("paintImmediately", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_paintImmediately115].setDisplayName ( "" );
            methods[METHOD_paintImmediately116] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("paintImmediately", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_paintImmediately116].setDisplayName ( "" );
            methods[METHOD_paintMap117] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("paintMap", new Class[] {java.awt.Graphics2D.class, boolean.class})); // NOI18N
            methods[METHOD_paintMap117].setDisplayName ( "" );
            methods[METHOD_postEvent118] = new MethodDescriptor(java.awt.Component.class.getMethod("postEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_postEvent118].setDisplayName ( "" );
            methods[METHOD_preferredSize119] = new MethodDescriptor(java.awt.Container.class.getMethod("preferredSize", new Class[] {})); // NOI18N
            methods[METHOD_preferredSize119].setDisplayName ( "" );
            methods[METHOD_prepareImage120] = new MethodDescriptor(java.awt.Component.class.getMethod("prepareImage", new Class[] {java.awt.Image.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_prepareImage120].setDisplayName ( "" );
            methods[METHOD_prepareImage121] = new MethodDescriptor(java.awt.Component.class.getMethod("prepareImage", new Class[] {java.awt.Image.class, int.class, int.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_prepareImage121].setDisplayName ( "" );
            methods[METHOD_print122] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("print", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_print122].setDisplayName ( "" );
            methods[METHOD_printAll123] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("printAll", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_printAll123].setDisplayName ( "" );
            methods[METHOD_printComponents124] = new MethodDescriptor(java.awt.Container.class.getMethod("printComponents", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_printComponents124].setDisplayName ( "" );
            methods[METHOD_putClientProperty125] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("putClientProperty", new Class[] {java.lang.Object.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_putClientProperty125].setDisplayName ( "" );
            methods[METHOD_redo126] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("redo", new Class[] {})); // NOI18N
            methods[METHOD_redo126].setDisplayName ( "" );
            methods[METHOD_registerKeyboardAction127] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("registerKeyboardAction", new Class[] {java.awt.event.ActionListener.class, java.lang.String.class, javax.swing.KeyStroke.class, int.class})); // NOI18N
            methods[METHOD_registerKeyboardAction127].setDisplayName ( "" );
            methods[METHOD_registerKeyboardAction128] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("registerKeyboardAction", new Class[] {java.awt.event.ActionListener.class, javax.swing.KeyStroke.class, int.class})); // NOI18N
            methods[METHOD_registerKeyboardAction128].setDisplayName ( "" );
            methods[METHOD_registerUndoMenuItems129] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("registerUndoMenuItems", new Class[] {javax.swing.JMenuItem.class, javax.swing.JMenuItem.class})); // NOI18N
            methods[METHOD_registerUndoMenuItems129].setDisplayName ( "" );
            methods[METHOD_remove130] = new MethodDescriptor(java.awt.Component.class.getMethod("remove", new Class[] {java.awt.MenuComponent.class})); // NOI18N
            methods[METHOD_remove130].setDisplayName ( "" );
            methods[METHOD_remove131] = new MethodDescriptor(java.awt.Container.class.getMethod("remove", new Class[] {int.class})); // NOI18N
            methods[METHOD_remove131].setDisplayName ( "" );
            methods[METHOD_remove132] = new MethodDescriptor(java.awt.Container.class.getMethod("remove", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_remove132].setDisplayName ( "" );
            methods[METHOD_removeAll133] = new MethodDescriptor(java.awt.Container.class.getMethod("removeAll", new Class[] {})); // NOI18N
            methods[METHOD_removeAll133].setDisplayName ( "" );
            methods[METHOD_removeAllGeoObjects134] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("removeAllGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_removeAllGeoObjects134].setDisplayName ( "" );
            methods[METHOD_removeMouseMotionListener135] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("removeMouseMotionListener", new Class[] {ika.map.tools.MapToolMouseMotionListener.class})); // NOI18N
            methods[METHOD_removeMouseMotionListener135].setDisplayName ( "" );
            methods[METHOD_removeNotify136] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("removeNotify", new Class[] {})); // NOI18N
            methods[METHOD_removeNotify136].setDisplayName ( "" );
            methods[METHOD_removePropertyChangeListener137] = new MethodDescriptor(java.awt.Component.class.getMethod("removePropertyChangeListener", new Class[] {java.lang.String.class, java.beans.PropertyChangeListener.class})); // NOI18N
            methods[METHOD_removePropertyChangeListener137].setDisplayName ( "" );
            methods[METHOD_removeScaleChangeHandler138] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("removeScaleChangeHandler", new Class[] {ika.gui.ScaleChangeHandler.class})); // NOI18N
            methods[METHOD_removeScaleChangeHandler138].setDisplayName ( "" );
            methods[METHOD_removeSelectedGeoObjects139] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("removeSelectedGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_removeSelectedGeoObjects139].setDisplayName ( "" );
            methods[METHOD_repaint140] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {})); // NOI18N
            methods[METHOD_repaint140].setDisplayName ( "" );
            methods[METHOD_repaint141] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {long.class})); // NOI18N
            methods[METHOD_repaint141].setDisplayName ( "" );
            methods[METHOD_repaint142] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_repaint142].setDisplayName ( "" );
            methods[METHOD_repaint143] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("repaint", new Class[] {long.class, int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_repaint143].setDisplayName ( "" );
            methods[METHOD_repaint144] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("repaint", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_repaint144].setDisplayName ( "" );
            methods[METHOD_requestDefaultFocus145] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestDefaultFocus", new Class[] {})); // NOI18N
            methods[METHOD_requestDefaultFocus145].setDisplayName ( "" );
            methods[METHOD_requestFocus146] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocus", new Class[] {})); // NOI18N
            methods[METHOD_requestFocus146].setDisplayName ( "" );
            methods[METHOD_requestFocus147] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocus", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_requestFocus147].setDisplayName ( "" );
            methods[METHOD_requestFocusInWindow148] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocusInWindow", new Class[] {})); // NOI18N
            methods[METHOD_requestFocusInWindow148].setDisplayName ( "" );
            methods[METHOD_resetKeyboardActions149] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("resetKeyboardActions", new Class[] {})); // NOI18N
            methods[METHOD_resetKeyboardActions149].setDisplayName ( "" );
            methods[METHOD_resetUndo150] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("resetUndo", new Class[] {})); // NOI18N
            methods[METHOD_resetUndo150].setDisplayName ( "" );
            methods[METHOD_reshape151] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("reshape", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_reshape151].setDisplayName ( "" );
            methods[METHOD_resize152] = new MethodDescriptor(java.awt.Component.class.getMethod("resize", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_resize152].setDisplayName ( "" );
            methods[METHOD_resize153] = new MethodDescriptor(java.awt.Component.class.getMethod("resize", new Class[] {java.awt.Dimension.class})); // NOI18N
            methods[METHOD_resize153].setDisplayName ( "" );
            methods[METHOD_revalidate154] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("revalidate", new Class[] {})); // NOI18N
            methods[METHOD_revalidate154].setDisplayName ( "" );
            methods[METHOD_scaleSelectedGeoObjects155] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("scaleSelectedGeoObjects", new Class[] {double.class, double.class})); // NOI18N
            methods[METHOD_scaleSelectedGeoObjects155].setDisplayName ( "" );
            methods[METHOD_scrollRectToVisible156] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("scrollRectToVisible", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_scrollRectToVisible156].setDisplayName ( "" );
            methods[METHOD_selectAllGeoObjects157] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("selectAllGeoObjects", new Class[] {})); // NOI18N
            methods[METHOD_selectAllGeoObjects157].setDisplayName ( "" );
            methods[METHOD_selectByPoint158] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("selectByPoint", new Class[] {java.awt.geom.Point2D.class, boolean.class, double.class})); // NOI18N
            methods[METHOD_selectByPoint158].setDisplayName ( "" );
            methods[METHOD_selectByRectangle159] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("selectByRectangle", new Class[] {java.awt.geom.Rectangle2D.class, boolean.class})); // NOI18N
            methods[METHOD_selectByRectangle159].setDisplayName ( "" );
            methods[METHOD_setBounds160] = new MethodDescriptor(java.awt.Component.class.getMethod("setBounds", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_setBounds160].setDisplayName ( "" );
            methods[METHOD_setComponentZOrder161] = new MethodDescriptor(java.awt.Container.class.getMethod("setComponentZOrder", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_setComponentZOrder161].setDisplayName ( "" );
            methods[METHOD_setDefaultLocale162] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("setDefaultLocale", new Class[] {java.util.Locale.class})); // NOI18N
            methods[METHOD_setDefaultLocale162].setDisplayName ( "" );
            methods[METHOD_shiftGraphics2DByBorderWidth163] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("shiftGraphics2DByBorderWidth", new Class[] {java.awt.Graphics2D.class})); // NOI18N
            methods[METHOD_shiftGraphics2DByBorderWidth163].setDisplayName ( "" );
            methods[METHOD_show164] = new MethodDescriptor(java.awt.Component.class.getMethod("show", new Class[] {})); // NOI18N
            methods[METHOD_show164].setDisplayName ( "" );
            methods[METHOD_show165] = new MethodDescriptor(java.awt.Component.class.getMethod("show", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_show165].setDisplayName ( "" );
            methods[METHOD_showAll166] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("showAll", new Class[] {})); // NOI18N
            methods[METHOD_showAll166].setDisplayName ( "" );
            methods[METHOD_size167] = new MethodDescriptor(java.awt.Component.class.getMethod("size", new Class[] {})); // NOI18N
            methods[METHOD_size167].setDisplayName ( "" );
            methods[METHOD_toString168] = new MethodDescriptor(java.awt.Component.class.getMethod("toString", new Class[] {})); // NOI18N
            methods[METHOD_toString168].setDisplayName ( "" );
            methods[METHOD_transferFocus169] = new MethodDescriptor(java.awt.Component.class.getMethod("transferFocus", new Class[] {})); // NOI18N
            methods[METHOD_transferFocus169].setDisplayName ( "" );
            methods[METHOD_transferFocusBackward170] = new MethodDescriptor(java.awt.Container.class.getMethod("transferFocusBackward", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusBackward170].setDisplayName ( "" );
            methods[METHOD_transferFocusDownCycle171] = new MethodDescriptor(java.awt.Container.class.getMethod("transferFocusDownCycle", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusDownCycle171].setDisplayName ( "" );
            methods[METHOD_transferFocusUpCycle172] = new MethodDescriptor(java.awt.Component.class.getMethod("transferFocusUpCycle", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusUpCycle172].setDisplayName ( "" );
            methods[METHOD_transformSelectedGeoObjects173] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("transformSelectedGeoObjects", new Class[] {java.awt.geom.AffineTransform.class})); // NOI18N
            methods[METHOD_transformSelectedGeoObjects173].setDisplayName ( "" );
            methods[METHOD_undo174] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("undo", new Class[] {})); // NOI18N
            methods[METHOD_undo174].setDisplayName ( "" );
            methods[METHOD_unregisterKeyboardAction175] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("unregisterKeyboardAction", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_unregisterKeyboardAction175].setDisplayName ( "" );
            methods[METHOD_update176] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("update", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_update176].setDisplayName ( "" );
            methods[METHOD_updateUI177] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("updateUI", new Class[] {})); // NOI18N
            methods[METHOD_updateUI177].setDisplayName ( "" );
            methods[METHOD_validate178] = new MethodDescriptor(java.awt.Container.class.getMethod("validate", new Class[] {})); // NOI18N
            methods[METHOD_validate178].setDisplayName ( "" );
            methods[METHOD_zoom179] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoom", new Class[] {double.class})); // NOI18N
            methods[METHOD_zoom179].setDisplayName ( "" );
            methods[METHOD_zoomIn180] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomIn", new Class[] {})); // NOI18N
            methods[METHOD_zoomIn180].setDisplayName ( "" );
            methods[METHOD_zoomIn181] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomIn", new Class[] {java.awt.geom.Point2D.Double.class})); // NOI18N
            methods[METHOD_zoomIn181].setDisplayName ( "" );
            methods[METHOD_zoomOnPoint182] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomOnPoint", new Class[] {double.class, java.awt.geom.Point2D.Double.class})); // NOI18N
            methods[METHOD_zoomOnPoint182].setDisplayName ( "" );
            methods[METHOD_zoomOnRectangle183] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomOnRectangle", new Class[] {java.awt.geom.Rectangle2D.class})); // NOI18N
            methods[METHOD_zoomOnRectangle183].setDisplayName ( "" );
            methods[METHOD_zoomOut184] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomOut", new Class[] {})); // NOI18N
            methods[METHOD_zoomOut184].setDisplayName ( "" );
            methods[METHOD_zoomOut185] = new MethodDescriptor(ika.gui.MapComponent.class.getMethod("zoomOut", new Class[] {java.awt.geom.Point2D.Double.class})); // NOI18N
            methods[METHOD_zoomOut185].setDisplayName ( "" );
        }
        catch( Exception e) {}//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.
        
        return methods;     }//GEN-LAST:Methods
    private static java.awt.Image iconColor16 = null;//GEN-BEGIN:IconsDef
    private static java.awt.Image iconColor32 = null;
    private static java.awt.Image iconMono16 = null;
    private static java.awt.Image iconMono32 = null;//GEN-END:IconsDef
    private static String iconNameC16 = null;//GEN-BEGIN:Icons
    private static String iconNameC32 = null;
    private static String iconNameM16 = null;
    private static String iconNameM32 = null;//GEN-END:Icons
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx

//GEN-FIRST:Superclass
    // Here you can add code for customizing the Superclass BeanInfo.
//GEN-LAST:Superclass
    /**
     * Gets the bean's
     * <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable properties of this bean.
     * May return null if the information should be obtained by automatic
     * analysis.
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }

    /**
     * Gets the bean's
     * <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean. May return null if the information
     * should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will belong
     * to the IndexedPropertyDescriptor subclass of PropertyDescriptor. A client
     * of getPropertyDescriptors can use "instanceof" to check if a given
     * PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }

    /**
     * Gets the bean's
     * <code>EventSetDescriptor</code>s.
     *
     * @return An array of EventSetDescriptors describing the kinds of events
     * fired by this bean. May return null if the information should be obtained
     * by automatic analysis.
     */
    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }

    /**
     * Gets the bean's
     * <code>MethodDescriptor</code>s.
     *
     * @return An array of MethodDescriptors describing the methods implemented
     * by this bean. May return null if the information should be obtained by
     * automatic analysis.
     */
    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     *
     * @return Index of default property in the PropertyDescriptor array
     * returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    @Override
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will mostly
     * commonly be used by human's when using the bean.
     *
     * @return Index of default event in the EventSetDescriptor array returned
     * by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    @Override
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }

    /**
     * This method returns an image object that can be used to represent the
     * bean in toolboxes, toolbars, etc. Icon images will typically be GIFs, but
     * may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from this
     * method.
     * <p>
     * There are four possible flavors of icons (16x16 color, 32x32 color, 16x16
     * mono, 32x32 mono). If a bean choses to only support a single icon we
     * recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background so they can be
     * rendered onto an existing background.
     *
     * @param iconKind The kind of icon requested. This should be one of the
     * constant values ICON_COLOR_16x16, ICON_COLOR_32x32, ICON_MONO_16x16, or
     * ICON_MONO_32x32.
     * @return An image object representing the requested icon. May return null
     * if no suitable icon is available.
     */
    @Override
    public java.awt.Image getIcon(int iconKind) {
        switch (iconKind) {
            case ICON_COLOR_16x16:
                if (iconNameC16 == null) {
                    return null;
                } else {
                    if (iconColor16 == null) {
                        iconColor16 = loadImage(iconNameC16);
                    }
                    return iconColor16;
                }
            case ICON_COLOR_32x32:
                if (iconNameC32 == null) {
                    return null;
                } else {
                    if (iconColor32 == null) {
                        iconColor32 = loadImage(iconNameC32);
                    }
                    return iconColor32;
                }
            case ICON_MONO_16x16:
                if (iconNameM16 == null) {
                    return null;
                } else {
                    if (iconMono16 == null) {
                        iconMono16 = loadImage(iconNameM16);
                    }
                    return iconMono16;
                }
            case ICON_MONO_32x32:
                if (iconNameM32 == null) {
                    return null;
                } else {
                    if (iconMono32 == null) {
                        iconMono32 = loadImage(iconNameM32);
                    }
                    return iconMono32;
                }
            default:
                return null;
        }
    }
}
