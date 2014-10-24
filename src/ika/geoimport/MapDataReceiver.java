/*
 * MapDataReceiver.java
 *
 * Created on November 5, 2006, 3:43 PM
 *
 */

package ika.geoimport;

import ika.geo.*;
import ika.gui.*;
import ika.table.Table;

/**
 * MapDataReceiver receives imported GeoObjects from a GeoImporter and is 
 * responsible for storing the data.
 * It has a reference on a MapComponent which is used to adjust the currently 
 * visible area when GeoObjects are added.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class MapDataReceiver extends DataReceiver {
    
    private MapComponent mapComponent;
    
    /** Creates a new instance of MapDataReceiver */
    public MapDataReceiver(MapComponent mapComponent) {
        this.mapComponent = mapComponent;
    }
    
    public boolean add(GeoSet geoSet) {
        boolean added = super.add(geoSet);
        if (added)
            this.informMapComponent();
        return added;
    }
    
    public boolean add(GeoImage geoImage) {
        boolean added = super.add(geoImage);
        if (added)
            this.informMapComponent();
        return added;
    }
    
    public boolean add(ika.table.TableLink tableLink) {
        boolean added = super.add(tableLink);
        if (added)
            this.informMapComponent();
        return added;
    }

    protected GeoSet getDestinationGeoSet() {
        return this.mapComponent.getImportExportGeoSet();
    }
    
    private void informMapComponent() {
        this.mapComponent.showAll();
        this.mapComponent.addUndo("Import Data");
    }
}