/*
 * GeoClipboard.java
 *
 * Created on March 20, 2007, 10:40 AM
 *
 */

package ika.geo.clipboard;

import ika.geo.GeoSet;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GeoClipboard {
    
    /** Creates a new instance of GeoClipboard */
    public GeoClipboard() {
    }
    
    
    public static void setClipboard(GeoSet geoSet) {
        GeoTransferable geoSelection = new GeoTransferable(geoSet);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(geoSelection, null);
    }
    
    public static void printClipboardFlavors() {
        // get the system clipboard
        Clipboard systemClipboard =
                Toolkit
                .getDefaultToolkit()
                .getSystemClipboard();
        // get the contents on the clipboard in a
        // transferable object
        Transferable clipboardContents =
                systemClipboard
                .getContents(
                null);
        // check if clipboard is empty
        if (clipboardContents == null) {
            System.out.println("Clipboard is empty!!!");
        } else {
            DataFlavor[] flavors = clipboardContents.getTransferDataFlavors();
            for(int i = 0; i < flavors.length; i++) {
                System.out.println(flavors[i].toString());
            }
            /*
            try {
                // see if DataFlavor of
                // DataFlavor.stringFlavor is supported
                DataFlavor df = new DataFlavor(String.class, null);
                
                if (clipboardContents.isDataFlavorSupported(df)) {
                    // return text content
                    String returnText = (String) clipboardContents.getTransferData(df);
                    System.out.print(returnText);
                }
            } catch (java.awt.datatransfer.UnsupportedFlavorException ufe) {
                ufe.printStackTrace();
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }
            */
            
//            FlavorMap flavorMap = FlavorMap.getDefaultFlavorMap();
//            flavorMap.getNativesForFlavors(flavors);
        }
    }
    
    
    public static void main(String[] args) {
        
        GeoSet geoSet = new GeoSet();
        ika.geo.GeoPath geoPath = new ika.geo.GeoPath();
        geoPath.moveTo(0, 0);
        geoPath.lineTo(100, 60);
        geoSet.add(geoPath);
        setClipboard(geoSet);
        
        
        printClipboardFlavors();
    }
    
    
}
