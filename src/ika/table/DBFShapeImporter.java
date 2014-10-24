/*
 * DBFShapeImporter.java
 *
 * Created on July 6, 2006, 2:28 PM
 *
 */

package ika.table;


/**
 * Extends DBFImporter to add support for finding a dbf file that is part of 
 * a ESRI shape file set.
 * @author jenny
 */
public class DBFShapeImporter extends DBFImporter {
    
    /** Creates a new instance of DBFShapeImporter */
    public DBFShapeImporter() {
    }
    
    private java.net.URL findDbfFileSibling(java.net.URL url) {
        
        if (url == null || url.getPath().length() < 5)
            return null;
        
        String lowerCaseFilePath = url.getPath().toLowerCase();
        if (lowerCaseFilePath.endsWith(".dbf"))
            return url;
        
        final boolean is_dbf_sibling =
                lowerCaseFilePath.endsWith(".shp") ||
                lowerCaseFilePath.endsWith(".prj") ||
                lowerCaseFilePath.endsWith(".sbn") ||
                lowerCaseFilePath.endsWith(".sbx") ||
                lowerCaseFilePath.endsWith(".shx");
        
        if (!is_dbf_sibling)
            return null;
        
        url = ika.utils.URLUtils.replaceFileExtension(url, "dbf");
        if (!ika.utils.URLUtils.resourceExists(url))
            url = ika.utils.URLUtils.replaceFileExtension(url, "DBF");
        return ika.utils.URLUtils.resourceExists(url) ? url : null;
    }
    
    public Table read(java.net.URL url) throws java.io.IOException {
        return super.read(this.findDbfFileSibling(url));
    }
}
