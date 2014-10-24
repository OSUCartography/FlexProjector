/*
 * Created on July 2, 2005, 10:24 AM
 */

package ika.geoexport;

import ika.geo.*;
import java.io.*;
import java.awt.geom.*;
import java.util.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class WMFExporter extends VectorGraphicsExporter{
    
    /*
     * Stream to write to.
     */
    private java.io.ByteArrayOutputStream stream = null;
    
    /*
     * Keep track of the number of objects written to the WMF file.
     * This includes pen and brush records.
     */ 
    private int nbrObjects = 0;
    
    /*
     * Keep track of the largest record.
     */
    private int sizeOfLargestRecord = 0;
    
    /*
     * Vector holding all VectorSymbols used to draw the GeoObjects.
     */
    private Vector vectorSymbols = new Vector();
    
    /*
     * Index into vectorSymbols on the VectorSymbol that is currently used for 
     * drawing.
     */
    private int currentVectorSymbolID = 0;
    
    /* 
     * The largest possible coordinate in a WMF file.
     */
    private static final int MAX_COORD = 16384;
    
    public WMFExporter(){
    }
    
    public String getFileFormatName(){
        return "WMF";
    }
    
    public String getFileExtension() {
        return "wmf";
    }
    
    protected void write(GeoSet geoSet, OutputStream outputStream)
    throws IOException {
        
        stream = new java.io.ByteArrayOutputStream();
        
        writeHeader();
        writeMapModeRecord();
        writeWindowOriginRecord((short)0, (short)0);
        short w = (short)dimToPagePx(pageFormat.getPageWidth());
        short h = (short)dimToPagePx(pageFormat.getPageHeight());
        writeWindowExtensionRecord(w, h);
        
        // write a default VectorSymbol used to draw bounding boxes for GeoImages
        VectorSymbol defaultVectorSymbol = new VectorSymbol();     
        vectorSymbols.add(defaultVectorSymbol);
        
        // extract all VectorSymbols and write them to the file
        extractVectorSymbols(geoSet);
       
        // IMPORTANT: write the symbols before any other record that increments
        // nbrObjects
        writeVectorSymbols();
        
        writeGeoSet(geoSet);

        writeFooterRecord();
        byte[] bytes = updateHeader();
        
        // write all to file
        outputStream.write(bytes);
        outputStream.flush();
    }
    
    @Override
    public double getMaximumExportSize() {
        return MAX_COORD;
    }
    
    private void writeGeoSet(GeoSet geoSet) {
        if (geoSet.isVisible() == false) {
            return;
        }
        
        final int numberOfChildren = geoSet.getNumberOfChildren();
        for (int i = 0; i < numberOfChildren; i++) {
            GeoObject geoObject = geoSet.getGeoObject(i);
            
            // only write visible objects
            if (geoObject.isVisible() == false) {
                continue;
            }
            
            if (geoObject instanceof GeoPath) {
                writeGeoPathAsPolyLine((GeoPath)geoObject);
            } else if (geoObject instanceof GeoPoint) {
                writeGeoPoint((GeoPoint)geoObject);
            } else if (geoObject instanceof GeoImage) {
                writeGeoImage((GeoImage)geoObject);
            } else if (geoObject instanceof GeoSet) {
                writeGeoSet((GeoSet)geoObject);
            }
        }
    }
    
    
    /* Polygon - private class that holds pairs of points and a closed flag. */
    private class Polygon extends java.util.Vector{
        public boolean closed = false;
    }
    
    private void writePathIterator(GeoPathIterator pi, VectorSymbol symbol){
        
        // vector to hold Polygon objects
        java.util.Vector polygons = new java.util.Vector();
        Polygon polygon = new Polygon();
        
        // convert the GeoPath to a series of Polygon objects
       do {
            switch (pi.getInstruction()) {
                
                case GeoPathModel.MOVETO:{
                    if (polygon.size() > 1) {
                    polygons.add(polygon);
                }
                    polygon = new Polygon();
                    final int x = (short)xToPagePx(pi.getX()); // FIXME round?
                    final int y = (short)yToPagePx(pi.getY());
                    polygon.add(new java.awt.Point(x, y));
                    break;
                }
                
                case GeoPathModel.CLOSE: {
                    polygon.closed = true;
                    if (polygon.size() > 1) {
                    polygons.add(polygon);
                }
                    polygon = new Polygon();
                    break;
                }
                case GeoPathModel.LINETO:{
                    final int x = (short)xToPagePx(pi.getX()); // FIXME round?
                    final int y = (short)yToPagePx(pi.getY());
                    polygon.add(new java.awt.Point(x, y));
                    break;
                }
            }
        } while (pi.next());
        
        // add polygon to polygons if this has not yet been done
        if (polygons.contains(polygon) == false && polygon.size() > 1) {
            polygons.add(polygon);
        }
        
        // write each Polygon in the polygons vector
        for (int i = 0; i < polygons.size(); i++){
            
            selectVectorSymbol(symbol);
            
            polygon = (Polygon)polygons.get(i);
            int nbrPoints = polygon.size();
            if (nbrPoints > Short.MAX_VALUE) {
                nbrPoints = Short.MAX_VALUE;
            }
            
            // allocate an array to hold the points
            short points[] = new short[nbrPoints * 2 + 1];
            
            // write the number of points to the array
            points[0] = (short)nbrPoints;
            
            // copy the points
            for (int j = 0; j < nbrPoints; j++){
                java.awt.Point pt = (java.awt.Point)polygon.get(j);
                points[j*2+1] = (short)pt.x;
                points[j*2+2] = (short)pt.y;
            }
            
            // write a polyline or a polygon
            if (polygon.closed){
                /*
                The polygon is closed automatically by drawing a line from the
                last vertex to the first.
                The current position is neither used nor updated by the Polygon
                function.
                 */
                writeRecord(0x0324, points);
            } else {
                /*
                The lines are drawn from the first point through subsequent
                points by using the current pen. Unlike the LineTo or PolylineTo
                functions, the Polyline function neither uses nor updates the
                current position.
                 */
                writeRecord(0x0325, points);
            }
            ++nbrObjects;
        }
    }
    
    private void writeGeoPoint(GeoPoint geoPoint) {
        PointSymbol pointSymbol = geoPoint.getPointSymbol();
        GeoPath geoPath = pointSymbol.getPointSymbol(getDisplayMapScale(), 
                geoPoint.getX(), geoPoint.getY());
        GeoPathIterator pi = geoPath.getIterator(); // !!! ??? bezierConversionTolerance);
        writePathIterator(pi, pointSymbol);
    }
    
    private void writeGeoImage(GeoImage geoImage) {
        Rectangle2D bounds = geoImage.getBounds2D(GeoObject.UNDEFINED_SCALE);
        selectVectorSymbol(0); // draw with default VectorSymbol
        writeRectangle(bounds);
    }
    
    private void writeGeoPathAsPolyLine(GeoPath geoPath) {
        GeoPathIterator pi = geoPath.getIterator();  // !!! ??? bezierConversionTolerance);
        writePathIterator(pi, geoPath.getVectorSymbol());
    }
    
    private void writeLine(int x1, int y1, int x2, int y2) {
        writeMoveToRecord(x1, y1);
        writeLineToRecord(x2, y2);
        ++nbrObjects;
    }
    
    private void writeRectangle(Rectangle2D rect) {
        final short x = (short)xToPagePx(rect.getMinX());
        final short y = (short)yToPagePx(rect.getMaxY());
        final short w = (short)dimToPagePx(rect.getWidth());
        final short h = (short)dimToPagePx(rect.getHeight());
        writeRectangleRecord(x, y, w, h);
    }
    
    private void writeCircle(double cx, double cy, double radius) {
        final short x = (short)xToPagePx(cx);
        final short y = (short)yToPagePx(cy);
        final short r = (short)dimToPagePx(radius);
        writeCircleRecord(x, y, r);
    }
    
    private void writeHeader() {
        
        final double wWC = pageFormat.getPageWidthWorldCoordinates();
        final double hWC = pageFormat.getPageHeightWorldCoordinates();
        final short w = (short)dimToPagePx(wWC);
        final short h = (short)dimToPagePx(hWC);
        
        // Placeable Meta File Header
        // DWORD   key;
        writeInt(0x9AC6CDD7);
        // HANDLE  hmf;
        writeShort((short)0);
        // RECT    bbox;
        // left
        writeShort((short)0);
        // top
        writeShort((short)0);
        // right
        writeShort(w);
        // bottom
        writeShort(h);
        // WORD    inch;
        writeShort((short)1440);
        // DWORD   reserved;
        writeInt(0);
        // WORD    checksum;
        short checksum = 0;
        byte[] b = stream.toByteArray();
        for (int i = 0; i < b.length; ++i) {
            checksum ^= b[i];
        }
        writeShort(checksum);
        
        // Standard Meta File Header
        // WORD  FileType;       // Type of metafile (0=memory, 1=disk)
        writeShort((short)1);
        // WORD  HeaderSize;     // Size of header in WORDS (always 9)
        writeShort((short)9);
        // WORD  Version;        // Version of Microsoft Windows used
        writeShort((short)0x0300);
        // DWORD FileSize;       // Total size of the metafile in WORDs
        writeInt(0);
        // WORD  NumOfObjects;   // Number of objects in the file
        writeShort((short)1);
        // DWORD MaxRecordSize;  // The size of largest record in WORDs
        writeInt(0);
        // WORD  NumOfParams;    // Not Used (always 0)
        writeShort((short)0);
    }
    
    private byte[] updateHeader() throws java.io.IOException {
        
        final int headerOffset = 22; // length of placeable meta file header
        
        byte[] bytes = stream.toByteArray();
        // update total size of the metafile in WORDs
        byte[] b = intToBytes(bytes.length / 2-11);
        for (int i = 0; i < 4; ++i) {
            bytes[i+6+headerOffset] = b[3-i];
        }
        
        // update number of objects in the file
        if (nbrObjects > Short.MAX_VALUE) {
            throw new java.io.IOException("Too many objects in WMF file");
        }
        bytes[10+headerOffset] = right((short)nbrObjects);
        bytes[11+headerOffset] = left((short)nbrObjects);
        
        // update size of largest record in WORDs
        b = intToBytes(sizeOfLargestRecord);
        for (int i = 0; i < 4; ++i) {
            bytes[i+12+headerOffset] = b[3-i];
        }
        
        return bytes;
    }
    
    private void writeRecord(int fctNumber, short [] params) {
        // write size of this record in 32-bit words
        final int recordSize = params != null ? params.length + 3 : 3;
        writeInt(recordSize);
        writeShort((short)fctNumber);
        if (params!= null) {
            for (int i = 0; i < params.length; ++i) {
                writeShort(params[i]);
            }
        }
        updateSizeOfLargestRecord(recordSize);
    }
    
    private void writeFooterRecord() {
        writeRecord(0x0000, null);
    }
    
    private void writeMapModeRecord() {
        writeRecord(0x0103, new short[]{8});
    }
    
    private void writeWindowOriginRecord(short x, short y) {
        writeRecord(0x020B, new short[]{y, x});
    }
    
    private void writeWindowExtensionRecord(short widht, short height) {
        writeRecord(0x020C, new short[]{height, widht});
    }
    
    private void writeMoveToRecord(short x, short y) {
        writeRecord(0x0214, new short[]{y, x});
    }
    
    private void writeMoveToRecord(float x, float y) {
        writeMoveToRecord(Math.round(x), Math.round(y));
    }
    
    private void writeLineToRecord(short x, short y) {
        writeRecord(0x0213, new short[]{y, x});
    }
    
    private void writeLineToRecord(float x, float y) {
        writeLineToRecord(Math.round(x), Math.round(y));
    }
    
    private void writeSelectObjectRecord(short objID) {
        writeRecord(0x012D, new short[]{objID});
    }
    
    private void writeRectangleRecord(short x, short y, short width, short height) {
        // bottom|right|top|left|
        writeRecord(0x041B, new short[]{(short)(y + height), (short)(x + width), y, x});
        ++nbrObjects;
    }
    
    private void writeCircleRecord(short x, short y, short r) {
        // bottom|right|top|left|
        writeRecord(0x0418, new short[]{(short)(y + r), (short)(x + r),
                (short)(y - r), (short)(x - r)});
                ++nbrObjects;
    }
    
    /*
     * Returns the position of a VectorSymbol in vectorSymbols.
     * If vectorSymbols does not contain the VectorSymbol, -1 is returned.
     */
    private int findVectorSymbol(VectorSymbol symbol){
        final int nbrSymbols = vectorSymbols.size();
        for (int i = 0; i < nbrSymbols; i++){
            VectorSymbol s = (VectorSymbol)(vectorSymbols.get(i));
            if (symbol.equals(s)) {
                return i;
            }
        }
        return -1;
    }
    
    /*
     * Selects a VectorSymbol, i.e. following drawing occurs with the passed
     * VectorSymbol.
     */
    private void selectVectorSymbol(VectorSymbol symbol){
        final int symbolID = findVectorSymbol(symbol);
        selectVectorSymbol(symbolID);
    }
    
    /*
     * Selects a VectorSymbol, i.e. following drawing occurs with the passed
     * VectorSymbol.
     */
    private void selectVectorSymbol(int symbolID){
        if (symbolID < 0) {
            throw new IllegalArgumentException("problem writing symbols for WMF file.");
        }
        
        if (currentVectorSymbolID != symbolID){
            // select pen for stroking
            writeSelectObjectRecord((short)symbolID);
            
            // select brush for filling
            writeSelectObjectRecord((short)(symbolID + vectorSymbols.size()));
            currentVectorSymbolID = symbolID;
        }
    }
    
    /*
     * Find all VectorSymbols used in a GeoSet and add the found VectorSymbols
     * to this.vectorSymbols.
     */ 
    private void extractVectorSymbols(GeoSet geoSet){
        if (geoSet.isVisible() == false) {
            return;
        }
        
        final int numberOfChildren = geoSet.getNumberOfChildren();
        for (int i = 0; i < numberOfChildren; i++) {
            GeoObject geoObject = geoSet.getGeoObject(i);
            
            // only extract symbols from visible objects
            if (geoObject.isVisible() == false) {
                continue;
            }
            
            if (geoObject instanceof GeoPath) {
                GeoPath geoPath = (GeoPath)geoObject;
                VectorSymbol symbol = geoPath.getVectorSymbol();
                if (findVectorSymbol(symbol) < 0) {
                    vectorSymbols.add(symbol);
                }
            } else if (geoObject instanceof GeoPoint) {
                GeoPoint geoPoint = (GeoPoint)geoObject;
                PointSymbol pointSymbol = geoPoint.getPointSymbol();
                if (findVectorSymbol(pointSymbol) < 0) {
                    vectorSymbols.add(pointSymbol);
                }
                
            } else if (geoObject instanceof GeoSet) {
                extractVectorSymbols((GeoSet)geoObject);
            }
        }
    }
    
    /*
     * Write the VectorSymbols in this.vectorSymbols to the WMF file.
     */ 
    private void writeVectorSymbols(){
        // first write the pens for stroking
        Iterator iterator = vectorSymbols.iterator();
        while (iterator.hasNext()){
            VectorSymbol symbol = (VectorSymbol)iterator.next();
            writePenRecord(symbol);
        }
        
        // then write the brushes for filling
        iterator = vectorSymbols.iterator();
        while (iterator.hasNext()){
            VectorSymbol symbol = (VectorSymbol)iterator.next();
            writeBrushRecord(symbol);
        }
    }
    
    /*
     * Writes the stroking information of a VectorSymbol to a WMF file.
     */
    private void writePenRecord(VectorSymbol symbol) {
        
        /* When specifying an explicit RGB color, the COLORREF value has the
        following hexadecimal form: 0x00bbggrr */
        
        // stroke style: solid = 0; dashed = 1; null = 5. There are some more...
        short style = (short)(symbol.isDashed() ? 1 : 0);
        if (symbol.isStroked() == false) {
            style = 5;
        }
        
        // stroke width
        final float w = symbol.getScaledStrokeWidth(getDisplayMapScale());
        short strokeWidth = (short)dimToPagePx(w);
        if (strokeWidth <= 0) {
            strokeWidth = 1;
        }
        
        // stroke color
        final int strokeColor = symbol.getStrokeColor().getRGB();
        final short r = (short)((strokeColor & 0x00ff0000) >> 16);
        final short g = (short)((strokeColor & 0x0000ff00) >> 8);
        final short b = (short) (strokeColor & 0x000000ff);
        final short gr = (short)(g << 8 | r);
        
        // 02FA CreatePenIndirect
        writeRecord(0x02FA, new short[]{style, strokeWidth, strokeWidth, gr, b});
        
        // CreatePenIndirect adds an entry for itself in the object list.
        ++nbrObjects;
    }
    
    /*
     * Writes the filling information of a VectorSymbol to a WMF file.
     */
    private void writeBrushRecord(VectorSymbol symbol) {
        // brush style: solid = 0; hollow = 1; hatched = 2;
        final short brushStyle = (short)(symbol.isFilled() ? 0 : 1);
        
        // fill color
        final int fillColor = symbol.getFillColor().getRGB();
        final short r = (short)((fillColor & 0x00ff0000) >> 16);
        final short g = (short)((fillColor & 0x0000ff00) >> 8);
        final short b = (short) (fillColor & 0x000000ff);
        final short gr = (short)(g << 8 | r);
        
        // hatch only used if brushStyle == 2
        final int brushHatch = 0;
        
        // CreateBrushIndirect
        writeRecord(0x02FC, new short[]{brushStyle, gr, b, brushHatch});
        
        // CreateBrushIndirect adds an entry for itself in the object list.
        ++nbrObjects;
    }
    
    private void updateSizeOfLargestRecord(int recordSizeInWords) {
        if (recordSizeInWords > sizeOfLargestRecord) {
            sizeOfLargestRecord = recordSizeInWords;
        }
    }
    
    /**
     * Shift a short left 8
     * @param s the short to shift left 8
     * @return the byte result of the shift
     */
    byte left(short s) {
        return ((byte) ((s & (short) 0xFF00) >> 8));
    }
    
    /**
     * Lower Byte of a short
     * @param s the short
     * @return byte with lower byte of short
     */
    byte right(short s) {
        return ((byte) ((s & (short) 0x00FF)));
    }
    
    byte[] intToBytes(int i) {
        byte[] b = new byte[4];
        b[0] = ((byte) ((i & 0xFF000000) >> 24));
        b[1] = ((byte) ((i & 0x00FF0000) >> 16));
        b[2] = ((byte) ((i & 0x0000FF00) >> 8));
        b[3] = ((byte) ((i & 0x000000FF)));
        return b;
    }
    
    /**
     * Write a short to the byte output stream
     * @param s the short
     */
    void writeShort(short s) {
        stream.write(right(s));
        stream.write(left(s));
    }
    
    /**
     * Write a short to the byte output stream
     * @param s the short
     */
    void writeInt(int i) {
        byte[] b = intToBytes(i);
        for (int k = 3; k >= 0; --k) {
            stream.write(b[k]);
        }
    }
    
    void writeByte(byte b) {
        stream.write(b);
    }
}
