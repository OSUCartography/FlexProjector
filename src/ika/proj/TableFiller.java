/*
 * TableFiller.java
 *
 * Created on January 11, 2008, 9:17 AM
 *
 */

package ika.proj;

import com.jhlabs.map.proj.ProjectionFactory;
import com.jhlabs.map.proj.Projection;
import ika.geo.FlexProjectorModel;
import ika.gui.ProjDistortionTable;
import java.util.List;
import org.jdesktop.swingworker.SwingWorker;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class TableFiller extends SwingWorker {
    
    private final ProjDistortionTable table;
    private final FlexProjectorModel model;
    
    public TableFiller(ProjDistortionTable table, FlexProjectorModel model) {
        this.table = table;
        this.model = model;
    }
    
    @Override
    public Void doInBackground() {
        initializeTable();
        return null;
    }
    
    /**
     * compute the distortion parameters for all supported projections
     */
    private void initializeTable() {
        List<String> projNames = ProjectionsManager.getSelectedProjectionNames();
        for (String name : projNames) {
            try {
                Projection projection = ProjectionFactory.getNamedProjection(name);
                projection.initialize();
                QModel qModel = this.model.getDisplayModel().qModel;
                ProjectionDistortionParameters params;
                params = new ProjectionDistortionParameters(projection, qModel);
                
                synchronized (model.getDisplayModel().distParams) {
                    model.getDisplayModel().distParams.add(params);
                }
                
                // this will call process() in the Event Dispatch Thread to
                // update the table.
                this.publish();
                
            } catch (Exception e) {
                System.err.println("Could not initialize " + name + " projection.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Executed on the Event Dispatch Thread after the doInBackground method
     * is finished. Update the table.
     */
    @Override
    public void done() {
        this.table.orderTable();
    }
    
    /**
     * Update the table with new entries.
     * This is executed in the Event Dispatch Thread.
     */
    @Override
    protected void process(List distParamsList) {
        this.table.orderTable();
    }
    
};