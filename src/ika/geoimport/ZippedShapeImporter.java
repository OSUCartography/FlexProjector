/*
 * ZippedShapeImporter.java
 *
 * Created on June 26, 2007, 2:03 PM
 *
 */

package ika.geoimport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Importer for zipped ESRI Shape files. The shp file is gzipped and its ".shp"
 * file extension is replaced by ".gz".
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ZippedShapeImporter extends ShapeImporter {
    
    /** Creates a new instance of ZippedShapeImporter */
    public ZippedShapeImporter() {
    }
    
    protected String getLowerCaseDataFileExtension() {
        return "gz";
    }
    
    protected BufferedInputStream findInputStream(java.net.URL url) throws IOException {
        BufferedInputStream bis = new BufferedInputStream( new GZIPInputStream(url.openStream()));
        return bis;
    }
}
