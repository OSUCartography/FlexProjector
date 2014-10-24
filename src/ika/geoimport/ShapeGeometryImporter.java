/*
 * ShapeGeometryImporter.java
 *
 * Created on June 9, 2006, 1:51 PM
 *
 */
package ika.geoimport;

import ika.geo.*;
import ika.utils.MixedEndianDataInputStream;
import java.io.*;

/**
 * An importer for ESRI shape files. This importer only reads geometry from
 * .shp files.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ShapeGeometryImporter extends GeoImporter {

    /** Identifiers for different shape types. */
    private static final int NULLSHAPE = 0;
    private static final int POINT = 1;
    private static final int POLYLINE = 3;
    private static final int POLYGON = 5;
    private static final int MULTIPOINT = 8;
    private static final int POINTZ = 11;
    private static final int POLYLINEZ = 13;
    private static final int POLYGONZ = 15;
    private static final int MULTIPOINTZ = 18;
    private static final int POINTM = 21;
    private static final int POLYLINEM = 23;
    private static final int POLYGONM = 25;
    private static final int MULTIPOINTM = 28;
    private static final int MULTIPATCH = 31;   // not supported yet
    /**
     * ESRI shapefile magic code at the beginning of the .shp file.
     */
    private static final int FILE_CODE = 9994;

    /**
     * Creates a new instance of ShapeGeometryImporter
     */
    public ShapeGeometryImporter() {
    }

    protected java.net.URL findDataURL(java.net.URL url) {

        if (url == null || url.getPath().length() < 5) {
            return null;
        }

        String dataFileExtension = this.getLowerCaseDataFileExtension();
        String lowerCaseFilePath = url.getPath().toLowerCase();
        if (lowerCaseFilePath.endsWith("." + dataFileExtension)) {
            return url;
        }

        final boolean is_shp_sibling =
                lowerCaseFilePath.endsWith(".dbf")
                || lowerCaseFilePath.endsWith(".prj")
                || lowerCaseFilePath.endsWith(".sbn")
                || lowerCaseFilePath.endsWith(".sbx")
                || lowerCaseFilePath.endsWith(".shx");

        if (!is_shp_sibling) {
            return null;
        }

        url = ika.utils.URLUtils.replaceFileExtension(url, dataFileExtension);
        if (ika.utils.URLUtils.resourceExists(url)) {
            return url;
        }
        url = ika.utils.URLUtils.replaceFileExtension(url, dataFileExtension.toUpperCase());
        return ika.utils.URLUtils.resourceExists(url) ? url : null;
    }

    protected String getLowerCaseDataFileExtension() {
        return "shp";
    }

    private java.net.URL findSHXURL(java.net.URL url) {
        if (url == null || url.getPath().length() < 5) {
            return null;
        }
        url = ika.utils.URLUtils.replaceFileExtension(url, "shx");
        if (!ika.utils.URLUtils.resourceExists(url)) {
            url = ika.utils.URLUtils.replaceFileExtension(url, "SHX");
        }
        return ika.utils.URLUtils.resourceExists(url) ? url : null;
    }

    protected BufferedInputStream findInputStream(java.net.URL url) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        return bis;
    }

    protected GeoObject importData(java.net.URL url) throws IOException {
        MixedEndianDataInputStream is = null;
        try {
            url = this.findDataURL(url);
            if (url == null) {
                return null;
            }

            GeoSet geoSet = this.createGeoSet();
            geoSet.setName(ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath()));

            BufferedInputStream bis = this.findInputStream(url);
            is = new MixedEndianDataInputStream(bis);

            // magic code is 9994
            int fileCode = is.readInt();
            if (fileCode != FILE_CODE) {
                throw new IOException("File is not an ESRI Shape file. "
                        + "Found file code: " + fileCode);
            }

            is.skipBytes(5 * 4);
            int fileLength = is.readInt() * 2;

            // read version and shape type
            int version = is.readLittleEndianInt();
            int shapeType = is.readLittleEndianInt();

            // skip bounding box and four double values
            is.skipBytes(8 * 8);

            // Read all features stored in records. The shp file does not contain
            // the number of records present in the file. The shx file can be 
            // used to extract this information.
            // If the shx file cannot be found, -1 is returned.
            final int recordCount = this.readRecordCountFromSHXFile(url);
            final int[] recOffsets = readSHXFile(url);
            
            // Read until as many records as specified in the shx file are 
            // imported or until the end of file is reached and an EOFException
            // is thrown.
            int currentRecord = 0;
            int currentPos = 100;
            try {
                while (true) {
                    
                    // move to beginning of record
                    is.skipBytes(recOffsets[currentRecord] - currentPos);
                    currentPos = recOffsets[currentRecord];
                    currentPos += readRecord(is, geoSet);
                    
                    if (progressIndicator != null) {
                        final int percentage = (currentRecord + 1) * 100 / recordCount;
                        if (!progressIndicator.progress(percentage)) {
                            return null;
                        }
                    }
                    if (++currentRecord == recordCount) {
                        break;
                    }
                }
            } catch (EOFException e) {
                // EOFException indicates that all records have been read.
            }

            // setup the symbol
            VectorSymbol symbol = new VectorSymbol();
            symbol.setScaleInvariant(true);
            symbol.setStrokeWidth(1);
            if (shapeType == POLYGON || shapeType == POLYGONZ || shapeType == POLYGONM) {
                symbol.setFilled(true);
                symbol.setFillColor(java.awt.Color.WHITE);
            }
            geoSet.setVectorSymbol(symbol);

            return geoSet;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String getImporterName() {
        return "Shape Importer";
    }

    private int readRecord(MixedEndianDataInputStream is,
            GeoSet geoSet)
            throws IOException {

        final int recordNumber = is.readInt();
        final int contentLength = is.readInt() * 2;
        // content is at least one int (i.e. the ShapeType)
        if (contentLength < 4) {
            throw new EOFException("Negative record length");
        }

        final int shapeType = is.readLittleEndianInt();
        int recordBytesRead = 8 + 4; // header is 8 bytes, shapeType is 4 bytes

        switch (shapeType) {
            case NULLSHAPE:
                break;
            case POINT:
            case POINTZ:
            case POINTM:
                recordBytesRead += readPoint(is, geoSet, recordNumber);
                break;
            case MULTIPOINT:
            case MULTIPOINTZ:
            case MULTIPOINTM:
                recordBytesRead += readMultipoint(is, geoSet, recordNumber);
                break;
            case POLYLINE:
            case POLYLINEZ:
            case POLYLINEM:
                recordBytesRead += readPolyline(is, geoSet, recordNumber);
                break;
            case POLYGON:
            case POLYGONZ:
            case POLYGONM:
                recordBytesRead += readPolygon(is, geoSet, recordNumber);
                break;
            case MULTIPATCH:
                throw new IOException("Multipatch Shape files are not supported.");
            default:
                throw new IOException("Shapefile contains unsupported "
                        + "geometry type: " + shapeType);
        }

        return recordBytesRead;
    }

    private int readPoint(MixedEndianDataInputStream is, GeoSet geoSet, int recordID)
            throws IOException {

        final double x = is.readLittleEndianDouble();
        final double y = is.readLittleEndianDouble();
        GeoPoint geoPoint = new GeoPoint(x, y);
        geoSet.add(geoPoint);
        geoSet.setID(recordID);
        return 2 * 8;

    }

    private int readMultipoint(MixedEndianDataInputStream is, GeoSet geoSet, int recordID)
            throws IOException {

        is.skipBytes(4 * 8); // skip bounding box
        final int numPoints = is.readLittleEndianInt();
        for (int ptID = 0; ptID < numPoints; ptID++) {
            readPoint(is, geoSet, recordID);
        }
        return 4 * 8 + 4 + numPoints * 2 * 8;

    }

    private int readPolyline(MixedEndianDataInputStream is, GeoSet geoSet,
            int recordID) throws IOException {

        is.skipBytes(4 * 8); // skip bounding box
        final int numParts = is.readLittleEndianInt();
        final int numPoints = is.readLittleEndianInt();

        // read indices into point array
        int[] pointIds = new int[numParts];
        for (int partID = 0; partID < numParts; partID++) {
            pointIds[partID] = is.readLittleEndianInt();
        }

        // read point array
        double[] x = new double[numPoints];
        double[] y = new double[numPoints];
        for (int ptID = 0; ptID < numPoints; ptID++) {
            x[ptID] = is.readLittleEndianDouble();
            y[ptID] = is.readLittleEndianDouble();
        }

        // construct one GeoPath
        GeoPath geoPath = this.createGeoPath();
        geoPath.setID(recordID);

        for (int partID = 0; partID < numParts; partID++) {

            int firstPtID = pointIds[partID];
            int lastPtID = partID + 1 < numParts ? pointIds[partID + 1] : numPoints;

            // part must have at least two points
            if ((lastPtID - firstPtID) < 2) {
                continue;
            }

            geoPath.moveTo(x[firstPtID], y[firstPtID]);
            for (int ptID = firstPtID + 1; ptID < lastPtID; ptID++) {
                geoPath.lineTo(x[ptID], y[ptID]);
            }
        }

        geoSet.add(geoPath);

        return 4 * 8 + 4 + 4 + numParts * 4 + numPoints * 2 * 8;
    }

    private int readPolygon(MixedEndianDataInputStream is, GeoSet geoSet,
            int recordID) throws IOException {

        is.skipBytes(4 * 8); // skip bounding box

        final int numParts = is.readLittleEndianInt();
        final int numPoints = is.readLittleEndianInt();

        // read indices into point array
        int[] pointIds = new int[numParts];
        for (int partID = 0; partID < numParts; partID++) {
            pointIds[partID] = is.readLittleEndianInt();
        }

        // read point array
        double[] x = new double[numPoints];
        double[] y = new double[numPoints];
        for (int ptID = 0; ptID < numPoints; ptID++) {
            x[ptID] = is.readLittleEndianDouble();
            y[ptID] = is.readLittleEndianDouble();
        }

        // construct one GeoPath
        GeoPath geoPath = this.createGeoPath();
        geoPath.setID(recordID);

        // add sections
        for (int partID = 0; partID < numParts; partID++) {
            int firstPtID = pointIds[partID];
            int lastPtID = partID + 1 < numParts ? pointIds[partID + 1] : numPoints - 1;

            // part must have at least two points
            if ((lastPtID - firstPtID) < 2) {
                continue;
            }

            geoPath.moveTo(x[firstPtID], y[firstPtID]);
            for (int ptID = firstPtID + 1; ptID < lastPtID; ptID++) {
                geoPath.lineTo(x[ptID], y[ptID]);
            }

            // close polygon when there are more than 2 points
            if (geoPath.getDrawingInstructionCount() > 2) {
                geoPath.closePath();
            }
        }

        geoSet.add(geoPath);
        return 4 * 8 + 4 + 4 + numParts * 4 + numPoints * 2 * 8;
    }

    /**
     * Reads the number of records from the shx file.
     * @param shapeURL The URL of the data shape file.
     * @return The number of records or -1 if the shx file cannot be found.
     */
    private int readRecordCountFromSHXFile(java.net.URL shapeURL) {

        MixedEndianDataInputStream is = null;
        try {
            java.net.URL shxURL = findSHXURL(shapeURL);
            if (shxURL == null) {
                return -1;
            }
            BufferedInputStream bis = new BufferedInputStream(shxURL.openStream());
            is = new MixedEndianDataInputStream(bis);
            is.skipBytes(24);
            final int fileLength = is.readInt() * 2;
            final int recordsCount = (fileLength - 100) / 8;
            return recordsCount;
        } catch (java.io.IOException e) {
            return -1;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

    }

    private int[] readSHXFile(java.net.URL shapeURL) {

        MixedEndianDataInputStream is = null;
        try {
            java.net.URL shxURL = findSHXURL(shapeURL);
            if (shxURL == null) {
                return null;
            }
            BufferedInputStream bis = new BufferedInputStream(shxURL.openStream());
            is = new MixedEndianDataInputStream(bis);
            is.skipBytes(24);
            final int fileLength = is.readInt() * 2;
            final int recordsCount = (fileLength - 100) / 8;
            int[] offsets = new int[recordsCount];
            is.skipBytes(72);
            int[] recOffsets = new int[recordsCount];
            
            for (int i = 0; i < recordsCount; i++) {
                recOffsets[i] = is.readInt() * 2;
                // skip length
                is.readInt();
                
                /*
                System.out.println("Shape " + i);
                System.out.println("Offset: " + is.readInt() * 2);
                System.out.println("Content Length: " + is.readInt() * 2);
                */
            }

            return recOffsets;
        } catch (java.io.IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException e) {
                }
            }
        }

    }
}