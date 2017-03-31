/*
 * MacWindowsManager.java
 *
 * Created on October 25, 2005, 12:09 PM
 *
 */

package ika.gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 * Mac OS X specific code. Integrates a Java application to the standard Mac
 * look and feel.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class MacWindowsManager {
    
    /** This JMenuBar is displayed when no window is open.
     */
    private static JMenuBar macFramelessMenuBar = null;
    
    private MacWindowsManager(){
    }
    
    /**
     * Setup the menu bar for Mac OS X.
     */
    public static void init(JMenuBar menuBar) {
        if (!ika.utils.Sys.isMacOSX() || menuBar == null)
            return;
        
        MacWindowsManager.macFramelessMenuBar = menuBar;
        /*
        // attach the menu bar to an invisible JFrame. The menu
        // will be visible if no window is open.
        MRJAdapter.setFramelessJMenuBar(macFramelessMenuBar);
        
        // setup about command in apple menu
        MRJAdapter.addAboutListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ika.gui.ProgramInfoPanel.showApplicationInfo();
            }
        }
        );
        
        // setup quit command in apple menu
        MRJAdapter.addQuitApplicationListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (MainWindow.closeAllDocumentWindows())
                    System.exit(0);
            }
        }
        );
        
        // setup preferences command in apple menu
        if (PreferencesDialog.canCreatePreferencesDialog()) {
            MRJAdapter.addPreferencesListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // search the foreground window
                    MainWindow frontWindow = MainWindow.getFocusedFrontWindow();

                    // show the preferences dialog
                    new PreferencesDialog(frontWindow, true).setVisible(true);
                }
            });
        }
        */
    }
    
    /**
     * Update the menu bar that is displayed when no document window is open.
     */
    public static void updateFramelessMenuBar() {
        // Search for the Window menu in the menu bar that is displayed
        // when no window is open or when all windows are minimized.
        // Update its Window menu to list all windows. This is important when 
        // all windows are minimized. The other menus don't need to be updated,
        // they are disabled by default.
        if (macFramelessMenuBar == null)
            return;
        final int menuCount = macFramelessMenuBar.getMenuCount();
        for (int i = menuCount - 1; i >= 0; i--) {
            JMenu menu = macFramelessMenuBar.getMenu(i);
            if ("WindowsMenu".equals(menu.getName())) {
                MainWindow.updateWindowMenu(menu, null);
                break;
            }
        }
    }
    
}
