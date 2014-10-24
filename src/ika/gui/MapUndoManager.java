/*
 * MapUndoManager.java
 *
 * Created on September 28, 2007, 10:55 PM
 *
 */

package ika.gui;

import java.io.IOException;

/**
 * This is a plug-in for the MapComponent that allows the undo/redo
 * process to be customized.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public interface MapUndoManager {
    
    /**
     * Restore the data to a snapshot that was previously taken.
     */
    public void applyUndoRedoState(Object data) throws IOException, ClassNotFoundException;
    
    /**
     * Provide a data snapshot of the current state.
     */
    public Object getUndoRedoState() throws IOException;
    
}
