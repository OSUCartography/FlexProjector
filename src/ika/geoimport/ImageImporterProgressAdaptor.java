/*
 * ImageImporterProgressAdaptor.java
 *
 * Created on August 15, 2006, 8:51 PM
 *
 */

package ika.geoimport;

import ika.gui.ProgressIndicator;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ImageImporterProgressAdaptor
        implements javax.imageio.event.IIOReadProgressListener{
    
    private ProgressIndicator progressIndicator;
    
    /** Creates a new instance of ImageImporterProgressAdaptor */
    public ImageImporterProgressAdaptor(ProgressIndicator progressIndicator) {
        if (progressIndicator == null)
            throw new IllegalArgumentException();
        this.progressIndicator = progressIndicator;
    }
    
    public void showDialog() {
        this.progressIndicator.start();
    }
    
    public void imageStarted(javax.imageio.ImageReader imageReader, int param) {
    }
    
    public void imageComplete(javax.imageio.ImageReader source) { 
    }
    
    public void imageProgress(javax.imageio.ImageReader imageReader,
            final float percentage) {
        if (this.progressIndicator.isAborted()) {
            imageReader.abort();
        } else
            this.progressIndicator.progress((int)percentage);
    }
    
    public void readAborted(javax.imageio.ImageReader source) {
        this.progressIndicator.abort();
    }
    
    public void sequenceComplete(javax.imageio.ImageReader source) {
    }
    
    public void sequenceStarted(javax.imageio.ImageReader imageReader, int param) {
    }
    
    public void thumbnailComplete(javax.imageio.ImageReader source) {
    }
    
    public void thumbnailProgress(javax.imageio.ImageReader imageReader, float param) {
    }
    
    public void thumbnailStarted(javax.imageio.ImageReader imageReader, int param, int param2) {
    }
}
