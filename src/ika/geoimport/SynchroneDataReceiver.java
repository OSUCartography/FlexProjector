package ika.geoimport;

import ika.geo.GeoObject;
import ika.geo.GeoSet;

/**
 * A data receiver that collects imported data in a GeoSet. It is executed in
 * the thread of the calling method.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class SynchroneDataReceiver extends DataReceiver {

    private GeoSet geoSet;

    public SynchroneDataReceiver() {
        this.geoSet = new GeoSet();
    }

    public SynchroneDataReceiver(GeoSet destinationGeoSet) {
        this.geoSet = destinationGeoSet != null ? destinationGeoSet : new GeoSet();
    }

    public GeoSet getDestinationGeoSet() {
        return this.geoSet;
    }

    public GeoObject getImportedData() {
        
        if (this.geoSet.getNumberOfChildren() == 0)
            return null;

        GeoSet res = this.geoSet;
        while (res.getNumberOfChildren() == 1 && (res.getGeoObject(0)) instanceof GeoSet) {
            res = (GeoSet) res.getGeoObject(0);
        }
        return res.getNumberOfChildren() == 1 ? res.getGeoObject(0) : res;
    }
}
