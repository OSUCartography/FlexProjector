/*
 * TableLinkExporter.java
 *
 * Created on April 13, 2007, 3:49 PM
 *
 */

package ika.table;

import java.io.IOException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface TableLinkExporter {
    
    public void exportTableForGeometry (String geometryFilePath,
            TableLink tableLink) throws IOException;
    
    public int getFeatureCount();
    
}
