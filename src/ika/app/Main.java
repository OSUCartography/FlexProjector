/*
 * Main.java
 *
 * Created on November 1, 2005, 10:19 AM
 *
 */

package ika.app;

import ika.gui.MacWindowsManager;
import ika.gui.MainWindow;
import ika.utils.IconUtils;
import javax.swing.*;

/**
 * Main entry point.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class Main {
    
    /**
     * main routine for the application.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        // on Mac OS X: take the menu bar out of the window and put it on top
        // of the main screen.
        if (ika.utils.Sys.isMacOSX()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        
        // use the standard look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // set icon for JOptionPane dialogs. This is done automatically on Mac 10.5 and later
        if (!ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5()) {
            java.util.Properties props
                    = ika.utils.PropertiesLoader.loadProperties("ika.app.Application");
            IconUtils.setOptionPaneIcons(props.getProperty("ApplicationIcon"));
        }
        
        // Replace title of progress monitor dialog with empty string.
        UIManager.put("ProgressMonitor.progressText", "");
        
        // RepaintManager.setCurrentManager(
        // new com.clientjava.examples.badswingthread.ThreadCheckingRepaintManager(false));
        
        SwingUtilities.invokeLater( new Runnable() {
            
            @Override
            public void run() {
                // create a temporary invisible BaseMainWindow, extract its
                // menu bar and pass it to the MacWindowsManager.
                if (ika.utils.Sys.isMacOSX()) {
                    MacWindowsManager.init(MainWindow.getMenuBarClone());
                }
                
                // create a new empty window
                MainWindow.newDocumentWindow();
                
                /*
                // initialize output and error stream for display in a window
                String appName = ika.app.ApplicationInfo.getApplicationName();
                String outTitle = appName + " - Standard Output";
                String errTitle = appName + " - Error Messages";
                new ika.utils.StdErrOutWindows(null, outTitle, errTitle);
                 */
            }
        });
        
    }
}
