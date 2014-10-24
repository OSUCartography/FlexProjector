/*
 * RotateScaleMoveSelectionTool.java
 *
 * Created on May 31, 2006, 11:15 AM
 *
 */

package ika.map.tools;

import ika.gui.MapComponent;

/**
 * A tool to select objects (by clicking or by rectangular dragging), to move,
 * scale and rotate objects.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class RotateScaleMoveSelectionTool extends CombinedTool {
    
    /**
     * Creates a new instance of RotateScaleMoveSelectionTool
     */
    public RotateScaleMoveSelectionTool(MapComponent mapComponent) {
        super(mapComponent, "Select - Move - Scale");
        
        SelectionTool selectionTool = new SelectionTool(this.mapComponent);
        MoverTool moveTool = new MoverTool(this.mapComponent);
        moveTool.setSelectOnDragStart(true);
        ScaleTool scaleTool = new ScaleTool(this.mapComponent);     
        RotateTool rotateTool = new RotateTool(this.mapComponent);
        
        this.addMapTool(scaleTool);
        this.addMapTool(rotateTool);
        this.addMapTool(moveTool);
        // selection tool must be last added tool
        this.addMapTool(selectionTool);
    }
}
