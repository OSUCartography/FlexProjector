/*
 * FlexUndoManager.java
 *
 * Created on September 28, 2007, 11:17 PM
 *
 */

package ika.gui;

import ika.geo.FlexProjectorModel;
import ika.proj.DesignProjection;
import java.io.IOException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class FlexUndoManager implements MapUndoManager {
    
    FlexProjectorModel flexProjectorModel;
    ProjectionBrewerPanel flexPanel;
    
    /** Creates a new instance of FlexUndoManager */
    public FlexUndoManager(FlexProjectorModel flexProjectorModel,
            ProjectionBrewerPanel flexPanel) {
        
        this.flexProjectorModel = flexProjectorModel;
        this.flexPanel = flexPanel;
        
    }
    
    @Override
    public void applyUndoRedoState(Object data) throws IOException, ClassNotFoundException {

        DesignProjection p = DesignProjection.factory((String)data);
        flexProjectorModel.setDesignProjection(p);
        flexPanel.updateDistortionIndicesAndInformListeners();
        
        // update the gui
        flexPanel.writeMethodGUI();
    }
    
    @Override
    public Object getUndoRedoState() throws IOException {
        return flexProjectorModel.getDesignProjection().serializeToString();
    }
    
}
