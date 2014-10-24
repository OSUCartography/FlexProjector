/*
 * ShapeExporter.java
 *
 * Created on April 13, 2007, 3:41 PM
 *
 */

package ika.geoexport;

import ika.geo.GeoSet;
import ika.table.DBFExporter;
import ika.table.TableLink;
import ika.table.TableLinkExporter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ShapeExporter extends GeoSetExporter implements TableLinkExporter {
    
    private ShapeGeometryExporter shapeGeometryExporter = new ShapeGeometryExporter();
    
    private boolean shapeTypeSet = false;
    
    /** Creates a new instance of ShapeExporter */
    public ShapeExporter() {
    }
    
    public String getFileFormatName() {
        return "Shape";
    }
    
    public String getFileExtension() {
        return "shp";
    }
    
    protected void write(GeoSet geoSet, OutputStream outputStream)
    throws IOException {
        
        if (!this.shapeTypeSet)
            shapeGeometryExporter.setShapeTypeFromFirstGeoObject(geoSet);
        shapeGeometryExporter.write(geoSet, outputStream);
        
    }
    
    public int getFeatureCount() {
        return this.shapeGeometryExporter.getWrittenRecordCount();
    }
    
    public void exportTableForGeometry(String geometryFilePath,
            TableLink tableLink) throws IOException {
        
        FileOutputStream dbfOutputStream = null;
        FileOutputStream shxOutputStream = null;
        
        try {
            ika.table.Table table = tableLink.getTable();
            String dbfPath = ika.utils.FileUtils.replaceExtension(geometryFilePath, "dbf");
            dbfOutputStream = new FileOutputStream(dbfPath);
            new DBFExporter().exportTable(dbfOutputStream, table);
            
            String shxPath = ika.utils.FileUtils.replaceExtension(geometryFilePath, "shx");
            shxOutputStream = new FileOutputStream(shxPath);
            shapeGeometryExporter.writeSHXFile(shxOutputStream, 
                    tableLink.getGeoSet());
            
        } finally {
            if (dbfOutputStream != null)
                dbfOutputStream.close();
            if (shxOutputStream != null)
                shxOutputStream.close();
        }
    }
    
    /**
     * Overwrite setBezierConversionTolerance to propagate bezierConversionTolerance
     * to private ShapeGeometryExporter.
     */
    @Override
    public void setBezierConversionTolerance(double bezierConversionTolerance) {
        super.setBezierConversionTolerance(bezierConversionTolerance);
        this.shapeGeometryExporter.setBezierConversionTolerance(bezierConversionTolerance);
    }

    /**
     * Set the type of shape file that will be generated. Valid values are 
     * POINT_SHAPE_TYPE, POLYLINE_SHAPE_TYPE, and POLYGON_SHAPE_TYPE.
     * The default value is POLYLINE_SHAPE_TYPE.
     */
    public void setShapeType(int shapeType) {
        
        this.shapeTypeSet = true;
        this.shapeGeometryExporter.setShapeType(shapeType);
        
    }
   
}
