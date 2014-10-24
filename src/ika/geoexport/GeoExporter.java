package ika.geoexport;

import ika.gui.ProgressIndicator;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class GeoExporter {

    /**
     * A ProgressIndicator that is displayed during a long export.
     * The ProgressIndicator must be set using setProgressIndicator().
     * The default is not to have any ProgressIndicator.
     */
    protected ProgressIndicator progressIndicator = null;

    protected GeoExporter() {
    }
    
    /**
     * Returns the file extension of the main file created by this exporter.
     * @return The file extension.
     */
    public abstract String getFileExtension();

    /**
     * Returns a short string that can be used to construct a string for 
     * a file selection dialog of the form "Save xyz file".
     * @return The name of the format.
     */
    public abstract String getFileFormatName();

    /**
     * @return the progressIndicator
     */
    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    /**
     * @param progressIndicator the progressIndicator to set
     */
    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }
    
}
