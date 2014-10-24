package ika.geoexport;

import ika.geo.GeoSet;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract exporter base class to write GeoSets to files and streams.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
abstract public class GeoSetExporter extends GeoExporter {
    
    /** 
     * displayMapScale is the current scale of the map displayed on screen.
     * This is needed for distances and coordinates that must be scaled by
     * the current map display scale. This should guarantee that the exported
     * graphics has the same appearance as the one currently displayed.
     */
    private double displayMapScale = 1;
    
    /**
     * The maximum tolerable deviation when converting Bezier splines to 
     * straight line segments.
     */
    protected double bezierConversionTolerance = 1;
    
    /**
     * The name of the user of this software.
     */
    protected String documentAuthor;

    /**
     * The subject of the exported data.
     */
    protected String documentSubject;
    /**
     * Key words describing the exported data.
     */
    protected String documentKeyWords;

    /**
     * The name of the software application generating the file.
     */
    protected String applicationName;

    /**
     * The name of the document to store inside the document, not the file name.
     */
    protected String documentName;

    protected GeoSetExporter() {
    }
    
    /**
     * Exports a GeoSet to an output stream. Derived classes can overwrite this
     * method to initialize themselves. However, the exporting should be done in
     * write().
     * @param geoSet The GeoSet to export.
     * @param outputStream The destination stream that will receive the result. This
     * stream is not closed by export() - it is the responsibility of the caller
     * to close it.
     */
    public void export (GeoSet geoSet, OutputStream outputStream)
    throws IOException {
        
        if (geoSet == null || outputStream == null)
            throw new IllegalArgumentException();
        this.write(geoSet, outputStream);
        
    }
    
    /**
     * Export a GeoSet to a file.
     * @param geoSet The GeoSet to export.
     * @param filePath A path to a file that will receive the result. If the file
     * already exists, its content is completely overwritten. If the file does 
     * not exist, a new file is created.
     */
    public final void export (GeoSet geoSet, String filePath) throws IOException {
        
        if (geoSet == null || filePath == null)
            throw new IllegalArgumentException();
        
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            this.export(geoSet, outputStream);
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
        
    }
    /**
     * Writes a GeoSet to an output stream. Derived classes must overwrite it
     * and do the actual export here.
     * @param geoSet The GeoSet to export.
     * @param outputStream The destination stream that will receive the result.
     */
    abstract protected void write (GeoSet geoSet, OutputStream outputStream)
    throws IOException;
    
    public double getDisplayMapScale() {
        return displayMapScale;
    }

    public void setDisplayMapScale(double displayMapScale) {
        if (displayMapScale <= 0)
            throw new IllegalArgumentException();
        this.displayMapScale = displayMapScale;
    }

    public double getBezierConversionTolerance() {
        return bezierConversionTolerance;
    }

    public void setBezierConversionTolerance(double bezierConversionTolerance) {
        if (bezierConversionTolerance <= 0.)
            throw new IllegalArgumentException();
        this.bezierConversionTolerance = bezierConversionTolerance;
    }

    /**
     * @return the documentAuthor
     */
    public String getDocumentAuthor() {
        return documentAuthor;
    }

    /**
     * @param documentAuthor the documentAuthor to set
     */
    public void setDocumentAuthor(String documentAuthor) {
        this.documentAuthor = documentAuthor;
    }

    /**
     * @return the documentSubject
     */
    public String getDocumentSubject() {
        return documentSubject;
    }

    /**
     * @param documentSubject the documentSubject to set
     */
    public void setDocumentSubject(String documentSubject) {
        this.documentSubject = documentSubject;
    }

    /**
     * @return the documentKeyWords
     */
    public String getDocumentKeyWords() {
        return documentKeyWords;
    }

    /**
     * @param documentKeyWords the documentKeyWords to set
     */
    public void setDocumentKeyWords(String documentKeyWords) {
        this.documentKeyWords = documentKeyWords;
    }

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName the applicationName to set
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * @return the documentName
     */
    public String getDocumentName() {
        return documentName;
    }

    /**
     * @param documentName the documentName to set
     */
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
