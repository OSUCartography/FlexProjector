/*
 * GeoClipboard.java
 *
 * Created on March 20, 2007, 10:32 AM
 *
 */

package ika.geo.clipboard;

import ika.geo.GeoSet;
import ika.geoexport.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GeoTransferable implements Transferable, ClipboardOwner {
    
    private static DataFlavor DXFFLAVOR;
    private static DataFlavor PDFFLAVOR;
    private static DataFlavor AIFLAVOR;
    private static DataFlavor WMFFLAVOR;
    private static DataFlavor GEOFLAVOR;
    static {
        try {
            PDFFLAVOR = new DataFlavor("application/pdf");
            DXFFLAVOR = new DataFlavor("image/vnd.dxf");
            AIFLAVOR = new DataFlavor("application/postscript");
            SystemFlavorMap flavorMap = (SystemFlavorMap)SystemFlavorMap.getDefaultFlavorMap();
            flavorMap.addUnencodedNativeForFlavor(AIFLAVOR, "'AICB' (CorePasteboardFlavorType 0x41494342)");
            
            WMFFLAVOR = new DataFlavor("image/x-wmf");
            
            GEOFLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=ika.geo.GeoSet");
            
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
    
    /**
     *  The supported flavors in order of preference.
     *  Only PDF seems to work with Illustrator.
     */
    private DataFlavor[] transferDataFlavors = new DataFlavor[] {
        //WMFFLAVOR,
        PDFFLAVOR,
        //DXFFLAVOR,
        //AIFLAVOR,
        DataFlavor.stringFlavor,
        GEOFLAVOR
    };
    
    private GeoSet geoSet;
    
    public GeoTransferable(GeoSet geoSet) {
        this.geoSet = geoSet;
    }
    
    /**
     * Returns the supported flavors of our implementation
     */
    public DataFlavor[] getTransferDataFlavors() {
        return transferDataFlavors;
    }
    
    /**
     * Returns true if flavor is supported.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < this.transferDataFlavors.length; i++) {
            if (this.transferDataFlavors[i].isMimeTypeEqual(flavor))
                return true;
        }
        return false;
    }
    
    /**
     * Returns an object in the requested data flavor.
     */
    public Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException,IOException {
        System.out.println("DataFlavor requested: " + flavor.getMimeType());
        if (DataFlavor.stringFlavor.equals(flavor)) {
            UngenerateExporter exporter = new UngenerateExporter();
            ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            exporter.export(this.geoSet, outputStream);
            return outputStream.toString();
        } else if (DXFFLAVOR.equals(flavor)) {
            return this.exportToFileInputStream(new DXFExporter());
        } else if (PDFFLAVOR.equals(flavor)) {
            GeoSetExporter exporter = null;
            try {
                Class pathClass = Class.forName("ika.geoexport.PDFExporter");
                Constructor con = pathClass.getConstructor(new Class[]{});
                exporter = (GeoSetExporter) con.newInstance(new Object[] {});
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IOException(ex.getMessage());
            }
            return this.exportToFileInputStream(exporter);
        } else if (AIFLAVOR.equals(flavor)) {
            return this.exportToFileInputStream(new IllustratorExporter());
        } else if (WMFFLAVOR.equals(flavor)) {
            return this.exportToFileInputStream(new WMFExporter());
        } else if (GEOFLAVOR.equals(flavor)) {
            return this.geoSet;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    private FileInputStream exportToFileInputStream(GeoSetExporter exporter)
    throws IOException {
        
        String ext = exporter.getFileExtension();
        java.io.File file = java.io.File.createTempFile("clipboard", "." + ext);
        
        // delete this file when the virtual machine stops
        file.deleteOnExit();
        
        java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
        try {
            exporter.export(this.geoSet, outputStream);
        } finally {
            outputStream.close();
        }
        
        java.io.FileInputStream in = new java.io.FileInputStream(file);
        return in;
    }
    
    /**
     * This object is no longer the owner of the clipboard content. Release the
     * private GeoSet, since it will not be used any longer.
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //this.geoSet = null;
    }
    
    /**
     * Put the passed GeoSet onto the system clipboard.
     * The programmer must make sure that the passed GeoObjects are not altered
     * after being put into the clipboard. Cloning the referenced objects before
     * storing them in the clipboard is the solution.
     * @param geoSet The GeoSet to share with other applications.
     */
    public static void storeInSystemClipboard(GeoSet geoSet) {
        GeoTransferable geoTrans = new GeoTransferable(geoSet);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(geoTrans, geoTrans);
    }
    
    public static GeoSet retreiveSystemClipboardCopy() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable trans = clipboard.getContents(null);
            if (trans != null && 
                    trans.isDataFlavorSupported(GEOFLAVOR)) {
                    GeoSet geoSet = (GeoSet)trans.getTransferData(GEOFLAVOR);
                    if (geoSet != null)
                        return (GeoSet)geoSet.clone();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean isClipboardFull() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return clipboard.isDataFlavorAvailable(GEOFLAVOR);
    }
}