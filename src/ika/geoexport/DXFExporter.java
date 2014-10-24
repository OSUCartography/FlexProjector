/*
 * DXFExporter.java
 *
 * Created on March 29, 2005, 11:32 PM
 */

package ika.geoexport;

import java.io.*;
import java.awt.geom.*;
import java.util.*;
import ika.geo.*;

/**
 * Exports a GeoSet to a Autocad DXF file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class DXFExporter extends GeoSetExporter {
    
    private GeoSet geoSet;
    private PrintWriter writer;
    private double south;
    private double west;
    
    public DXFExporter(){
    }
    
    public String getFileFormatName(){
        return "DXF";
    }
    
    public String getFileExtension() {
        return "dxf";
    }
    
    protected void write(GeoSet geoSet, OutputStream outputStream) throws IOException {
        this.writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(outputStream)));
        
        this.geoSet = geoSet;
        Rectangle2D bounds = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
        this.west = bounds.getMinX();
        this.south = bounds.getMinY();
        
        this.writeLines();
    }
    
    private void writeLines() {
        
        if (geoSet.getNumberOfChildren() == 0)
            throw new IllegalArgumentException();
        
        final int numberOfLayers = geoSet.getNumberOfSubSetsInTree();
        
        writeDXFHeader();
        writeDXFTables(numberOfLayers);
        writeDXFBlocks();
        writeDXFEntities();
        writeDXFEnd();
        writer.flush();
    }
    
    private void writeDXFHeader() {
        Rectangle2D bounds = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
        
        code(0);
        value("SECTION");
        code(2);
        value("HEADER");
        
        // Version 10 of the dxf specs
        code(9);
        value("$ACADVER");
        code(1);
        value("AC1006");
        
        // Insertion base set by BASE command (in WCS)
        code(9);
        value("$INSBASE");
        code(10);
        value(0.0);
        code(20);
        value(0.0);
        code(30);
        value(0.0);
        
        // south west minimum: X, Y, and Z drawing extents lower-left corner (in WCS)
        code(9);
        value("$EXTMIN");
        code(10);
        value(bounds.getMinX());
        code(20);
        value(bounds.getMinY());
        code(30);
        value(0.0);
        
        // maximum north west
        code(9);
        value("$EXTMAX");
        code(10);
        value(bounds.getMaxX());
        code(20);
        value(bounds.getMaxY());
        code(30);
        value(0.0);
        
        // XY drawing limits lover-left corner (in WCS)
        code(9);
        value("$LINMIN");
        code(10);
        value(bounds.getMinX());
        code(20);
        value(bounds.getMinY());
        code(30);
        value(0.0);
        
        // XY drawing limits upper-right corner (in WCS)
        code(9);
        value("$LINMAX");
        code(10);
        value(bounds.getMaxX());
        code(20);
        value(bounds.getMaxY());
        code(30);
        value(0.0);
        
        code(0);
        value("ENDSEC");
    }
    
    private void writeDXFTables(int numberOfLayers) {
        code(0);
        value("SECTION");
        code(2);
        value("TABLES");
        
        // line type table
/*      code(0);          // start of table
        value("TABLE");
        code(2);          // table name
        value("LTYPE");
        code(70);         // number of entries in table
        value(1);
        code(0);          // table entry (same name as table)
        value("LTYPE");
        code(2);          // name of table entry
        value("CONTINUOUS");
        code(70);         // flags relevant to the table entry
        value(64);
        code(3);          // descriptive text for linetype
        value("Solid line");
        code(72);         // alignment code
        value(65);
        code(73);         // number of dash length items
        value(0);
        code(40);         // total pattern length
        value("0.000000");
        code(0);          // end of table
        value("ENDTAB");
 */
        // layer table
        code(0);          // start of table
        value("TABLE");
        code(2);          // table name
        value("LAYER");
        code(70);         // number of entries in table
        value(numberOfLayers);
        for (int i = 1; i <= numberOfLayers; i++) {
            code(0);          // table entry (same name as table)
            value("LAYER");
            code(2);          // name of table entry
            valueAsString(i);
            code(70);         // flags relevant to the table entry.
            // The 1 bit is set in the 70 group flags if the layer is frozen
            value(0);
            code(62);         // color number
            value(i);
            code(6);          // linetype name
            value("CONTINUOUS");
        }
        code(0);          // end of table
        value("ENDTAB");
        
 /*
  // style table
        code(0);          // start of table
        value("TABLE");
        code(2);          // table name
        value("STYLE");
        code(70);         // number of entries in table
        value(0);
        code(0);          // end of table
        value("ENDTAB");
  */
        code(0);
        value("ENDSEC");
    }
    
    private void writeDXFBlocks() {
        code(0);
        value("SECTION");
        code(2);
        value("BLOCKS");
        code(0);
        value("ENDSEC");
    }
    
    private void writeDXFEntities() {
        code(0);
        value("SECTION");
        code(2);
        value("ENTITIES");
        writeLines(this.geoSet);
        code(0);
        value("ENDSEC");
    }
    
    private void writeDXFEnd() {
        code(0);
        value("EOF");
    }
    
    private void writePolyline(Vector vector, int layerID, int lineWidth) {
        if (vector.size() == 0)
            return;
        
        code(0);                  // entity type
        value("POLYLINE");
        code(8);                  // name of the layer
        valueAsString(layerID);
        code(39);                  // Thickness (if nonzero)
        value(lineWidth);
        code(70);                 // polyline flags
        value(0);                 // set to 1 if polygon is closed.
        code(40);                 // default starting width
        value(lineWidth);
        code(41);                 // default ending width
        value(lineWidth);
        code(66);                 // start vertices
        value(1);
        
        for (int i = 0; i < vector.size(); i++) {
            double[] point = (double[])vector.get(i);
            code(0);
            value("VERTEX");
            code(70);               // vertex flags
            value(0);
            code(42);              // bulge
            value(0);              // 0 = straight line
            // coordinates
            code(10);
            value(this.transformX(point[0]));
            code(20);
            value(this.transformY(point[1]));
            code(30);
            value(0.0);
        }
        
        // end of vertices
        code(0);
        value("SEQEND");
    }
    
    private static int layerID = 0;
    private static int layerIDForNewGroup() {
        return layerID++;
    }
    
    private void writeLines(GeoSet geoSet) {
        
        if (geoSet.isVisible() == false)
            return;
        
        final int layerID = layerIDForNewGroup();
        final int lineWidth = 5;
        
        final int numberOfChildren = geoSet.getNumberOfChildren();
        for (int i = 0; i < numberOfChildren; i++) {
            GeoObject geoObject = geoSet.getGeoObject(i);
            
            // only write visible objects
            if (geoObject.isVisible() == false)
                continue;
            
            if (geoObject instanceof GeoPath) {
                GeoPath geoPath = (GeoPath)geoObject;
                PathIterator iterator = geoPath.toPathIterator(null, 
                        this.bezierConversionTolerance);
                double [] coords = new double [6];
                java.util.Vector vector = new java.util.Vector();
                while (!iterator.isDone()) {
                    final int type = iterator.currentSegment(coords);
                    switch (type) {
                        case PathIterator.SEG_CLOSE:
                            double[] firstPoint = (double[])vector.firstElement();
                            vector.add(new double[] {firstPoint[0], firstPoint[1]});
                            writePolyline(vector, layerID, lineWidth);
                            vector.clear();
                            break;
                        case PathIterator.SEG_MOVETO:
                            writePolyline(vector, layerID, lineWidth);
                            vector.clear();
                            // fall through
                        case PathIterator.SEG_LINETO:
                            vector.add(new double[] {coords[0], coords[1]});
                            break;
                    }
                    iterator.next();
                }
                writePolyline(vector, layerID, lineWidth);
            } else if (geoObject instanceof GeoSet) {
                GeoSet childGeoSet = (GeoSet)geoObject;
                writeLines(childGeoSet);
            }
        }
    }
    
    private void code(int code) {
        this.writer.println(code);
    }
    private void valueAsString(int value) {
        this.writer.println(value);
    }
    private void value(int value) {
        this.writer.print(" ");
        this.writer.println(value);
    }
    private void value(double value) {
        this.writer.print(" ");
        this.writer.println(value);
    }
    private void value(String value) {
        this.writer.println(value);
        
    }
    
    private double transformX(double x) {
        return x;
//        return (x - this.west) * exportMapScale; !!! ???
    }
    
    private double transformY(double y) {
        return y;
//        return (y - this.south) * exportMapScale; !!! ???
    }
    
}
