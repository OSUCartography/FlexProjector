/*
 * ShapeImporter.java
 *
 * Created on August 16, 2006, 7:56 PM
 *
 */
package ika.geoimport;

import ika.geo.*;
import ika.table.*;
import java.io.IOException;

/**
 * An importer for ESRI shape file sets. Reads geometry from a shp file, and 
 * attributes from a dbf file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ShapeImporter extends ShapeGeometryImporter
        implements TableLinkImporter {

    private TableLink tableLink;

    /** Creates a new instance of ShapeImporter */
    public ShapeImporter() {
    }

    @Override
    protected GeoObject importData(java.net.URL url) throws IOException {
        GeoSet geoSet = (GeoSet) super.importData(url);
        if (this.progressIndicator != null && this.progressIndicator.isAborted()) {
            return null;
        }

        // import the Table
        DBFImporter dbfImporter = new DBFShapeImporter();
        Table table = dbfImporter.read(url);

        if (table.getRowCount() < geoSet.getNumberOfChildren()) {
            throw new java.io.IOException("DBF Shape attributes corrupt.");
        }

        // create a link between the table and the geometry
        if (geoSet != null && table != null) {
            this.tableLink = new TableLink(table, geoSet);
        } else {
            this.tableLink = null;
        }

        return geoSet;
    }

    public TableLink getTableLink() {
        return this.tableLink;
    }
}
