/*
 * GridOperator.java
 *
 * Created on January 28, 2006, 2:10 PM
 *
 */

package ika.geo.grid;

import ika.geo.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface GridOperator {
    
    /** Returns a descriptive name of this GridOperator
     * @return The name of this GridOperator.
     */
    public String getName();
    
    /**
     * Start operating on the passed GeoGrid.
     * @param geoGrid The GeoGrid to operate on.
     * @return A new GeoGrid containing the result. The resulting GeoGrid may
     * be of a different size than the passed GeoGrid.
     */
    public GeoObject operate (GeoGrid geoGrid);
    
}
