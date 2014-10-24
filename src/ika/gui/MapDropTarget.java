/*
 * MapDropTarget.java
 *
 * Created on August 13, 2006, 2:21 PM
 *
 */

package ika.gui;

import ika.geo.*;
import ika.geoimport.*;
import ika.geoimport.DataReceiver;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * MapDropTarget handles drag and drop for a MapComponent.
 * MapDropTarget contains a reference to a DataReceiver which handles
 * drop events of GeoObjects on a map and stores the passed GeoObjects.
 * Dropped GeoObjects are stored by MapDropTarget at the top level of the map,
 * unless a different DataReceiver is set by setDataReceiver().
 * 
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class MapDropTarget extends BorderedDropTarget {
    
    // a special data flavour for exchanging images on Macs.
    static DataFlavor macPictStreamFlavor;
    static {
        try {
            MapDropTarget.macPictStreamFlavor =
                    new DataFlavor("image/x-pict; class=java.io.InputStream");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
    
    /**
     * The DataReceiver handles drop events of data on a MapComponent.
     * It is responsible for storing the data at the right place. This object 
     * is used in an importer thread to store the imported objects.
     */
    private DataReceiver dataReceiver;
    
    /** Creates a new instance of MapDropTarget */
    public MapDropTarget(MapComponent mapComponent) {
        super(mapComponent);
        
        this.dataReceiver = new MapDataReceiver(mapComponent);
    }
    
    /**
     * Returns a hash map of urls (key) and importers (value) for a
     * list of Files that is stored in a Transferable.
     */
    private Hashtable fileImporters(Transferable trans) {
        try {
            java.util.List fileList = (java.util.List)
            trans.getTransferData(DataFlavor.javaFileListFlavor);
            return ika.geoimport.GeoImporter.findGeoImporters(fileList);
        } catch (Exception e) {
            return new Hashtable(0);
        }
    }
    
    /**
     * Handle a drop event: extract the data and call the MapDataReceiver to
     * store the data.
     */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        // call parent drop() to hide the border.
        super.drop(dtde);
        
        /*
         *From this method, the DropTargetListener shall accept or reject the
         drop via the acceptDrop(int dropAction) or rejectDrop() methods of the
         DropTargetDropEvent parameter. Subsequent to acceptDrop(), but not
         before, DropTargetDropEvent's getTransferable() method may be invoked,
         and data transfer may be performed via the returned Transferable's
         getTransferData() method.
         */
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        Transferable trans = dtde.getTransferable();
        
        boolean gotData = false;
        try {
            // treat a list of File objects
            if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                this.importFiles(dtde);
                gotData = true;
            } // try to get an image
            else if (trans.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                this.importImage(dtde);
                gotData = true;
            } // try to get a Mac PICT using QuickTime. Only needed on Mac.
            else if (trans.isDataFlavorSupported(macPictStreamFlavor)) {
                this.importPICT(dtde);
                gotData = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*
             *At the completion of a drop, an implementation of this method is
             required to signal the success/failure of the drop by passing an
             appropriate boolean to the DropTargetDropEvent's
             dropComplete(boolean success) method.
             Note: The data transfer should be completed before the call to the
             DropTargetDropEvent's dropComplete(boolean success) method. After
             that, a call to the getTransferData() method of the Transferable
             returned by DropTargetDropEvent.getTransferable() is guaranteed
             to succeed only if the data transfer is local; that is, only if
             DropTargetDropEvent.isLocalTransfer() returns true. Otherwise, the
             behavior of the call is implementation-dependent.
             */
            dtde.dropComplete(gotData);
        }
    }
    
    /**
     * URLImporter reads a series of files using corresponding importers.
     */
    private class URLImporter extends Thread {
        
        /** The progress indicator */
        public ProgressPanel prog;
        
        /** A hash map with pairs of files and importers. */
        public Hashtable url_importer_pairs;
        
        private String constructProgressMessage(java.net.URL url) {
            String fileName = ika.utils.FileUtils.getFileName(url.getPath());
            return "Reading " + fileName + ". Please wait...";
        }
        
        @Override
        public void run() {
            // import each data source sequentially
            Iterator iterator = url_importer_pairs.keySet().iterator();
            while (iterator.hasNext()) {
                // get the URL
                final java.net.URL url = (java.net.URL)iterator.next();
                
                // get the importer
                final GeoImporter importer = (GeoImporter)url_importer_pairs.get(url);
                
                // configure the progress dialog
                ika.utils.SwingThreadUtils.invokeAndWait(new Runnable() {
                    public void run() {
                        prog.setMessage(constructProgressMessage(url));
                        //importer.setProgressIndicator(prog); FIXME
                    }
                });
                
                // import the data in this same thread.
                importer.read(url, dataReceiver, GeoImporter.SAME_THREAD);
            }
        }
    }
    
    /** Reads data from a list of File objects. */
    private void importFiles(DropTargetDropEvent dtde) throws Exception {

        // URLImporter is a local class defined above.
        URLImporter fileImporter = new URLImporter();
        // Find importers for the files before the new thread is started!
        // The passed event is not valid anymore in the new thread.
        fileImporter.url_importer_pairs = fileImporters(dtde.getTransferable());
        
        // setup progress dialog in Swing thread
        if (this.targetComponent != null) {
            JFrame owner = (JFrame)this.targetComponent.getTopLevelAncestor();
            // fileImporter.prog = new ika.gui.ProgressPanel(owner, "Import", null, true); FIXME
            
            // start new thread that will sequentially read all files
            fileImporter.start();
        }
    }
    
    /** Reads a Java Image object */
    private void importImage(DropTargetDropEvent dtde)
    throws UnsupportedFlavorException, IOException {
        Transferable trans = dtde.getTransferable();
        Image img = (Image) trans.getTransferData(DataFlavor.imageFlavor);
        GeoImage geoImage = this.imageToGeoImage(img, dtde.getLocation());
        // store the image
        this.dataReceiver.add(geoImage);
    }
    
    /** Reads a Mac PICT using QuickTime. Only needed on Mac. */
    private void importPICT(DropTargetDropEvent dtde)
    throws Exception {
        Transferable trans = dtde.getTransferable();
        InputStream in =
                (InputStream) trans.getTransferData(macPictStreamFlavor);
        // for the benefit of the non-mac crowd, this is
        // done with reflection.  directly, it would be:
        // Image img =  QTJPictHelper.pictStreamToJavaImage (in);
        Class qtjphClass = Class.forName("ika.gui.QTJPictHelper");
        Class[] methodParamTypes = { java.io.InputStream.class };
        Method method = qtjphClass.getDeclaredMethod(
                "pictStreamToJavaImage", methodParamTypes);
        InputStream[] methodParams = { in };
        
        // create a GeoImage
        Image img = (Image) method.invoke(null, (Object[])methodParams);
        GeoImage geoImage = this.imageToGeoImage(img, dtde.getLocation());
        // store the image
        this.dataReceiver.add(geoImage);
    }
    
    /** Creates a GeoImage from an Image. Takes care of the position and size
     * of the new GeoImage.
     */
    private GeoImage imageToGeoImage(Image img, Point location) {
        if (img == null || location == null)
            return null;
        
        // convert image to GeoImage
        BufferedImage bImg = ika.utils.ImageUtils.makeBufferedImage(img);
        MapComponent mapComponent = (MapComponent)this.targetComponent;
        final java.awt.geom.Point2D.Double pt;
        pt = mapComponent.userToWorldSpace(location);
        
        // scale and place image
        final double pixelSize = 1. / mapComponent.getScaleFactor();
        return new GeoImage(bImg, pt.getX(), pt.getY(), pixelSize);
    }
    
    /** Utility method for debugging. */
    private void dumpDataFlavors(Transferable trans) {
        System.out.println("Flavors:");
        DataFlavor[] flavors = trans.getTransferDataFlavors();
        for (int i=0; i<flavors.length; i++) {
            System.out.println("*** " + i + ": " + flavors[i]);
        }
    }
    
    /**
     * Getter for the DataReceiver, which stores dropped GeoObjects.
     */
    public DataReceiver getDataReceiver() {
        return dataReceiver;
    }
    
    /**
     * Setter for the DataReceiver, which stores dropped GeoObjects.
     */
    public void setDataReceiver(DataReceiver mapDataReceiver) {
        this.dataReceiver = mapDataReceiver;
    }

}
