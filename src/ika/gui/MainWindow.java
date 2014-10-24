/*
 * MainWindow.java
 *
 * Created on May 14, 2007, 4:25 PM
 *
 */

package ika.gui;

import ika.utils.FileUtils;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.Vector;
import javax.swing.*;

/**
 * An abstract document window that handles (1) saving, opening and closing 
 * documents, (2) resizing the window, (3) dirty state flag, (4) a menu listing
 * all currently open windows. 
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class MainWindow extends javax.swing.JFrame 
        implements ComponentListener {
    
    /** static vector holding all windows currently open.
     */
    protected static Vector windows = new Vector();
    
    /** name of a new window is WINDOWNAME + windowCounter
     */
    private static final String WINDOWNAME = "Untitled ";
    
    /** counts the windows created so that each new window gets increasingly
     * numbered.
     */
    private static int windowCounter = 1;

    /** file path to location on the hard disk where this document is stored. 
     * It is null if the document has never been saved.
     */
    private String filePath = null;
    
    /** dimension of the window when minimized.
     */
    private java.awt.Dimension packedSize = null;
    
    /** dirty flag: true means document must be saved before closing.
     */
    private boolean dirty = false;
    
    /** add these WindowFocusListeners to all document windows.
     */
    private static Vector windowFocusListeners = new Vector();
    
    /**
     * Creates a new document window. Automatically assigns a title, and adds the 
     * new window to the menu listing all currently open windows. The new 
     * window can be removed from the menu with removeFromWindowMenu().
     */
    public MainWindow() {
        
        // assign an automatically generated name
        this.setTitle(this.getWindowTitle(MainWindow.windowCounter++));
        
        // add the window to the menu that lists all windows.
        windows.add(this);
                
        // register this as a ComponentListener to get resize events.
        // the resize-event-handler makes sure the window is not gettting too small.
        this.addComponentListener(this);
    }

    protected String getWindowTitle(int windowNumber) {
        String title = WINDOWNAME + windowNumber;
        if (ika.utils.Sys.isWindows()) {
            title += " - " + ika.app.ApplicationInfo.getApplicationName();
        }
        return title;
    }

    /**
     * This method is called after the constructor. Data models and the GUI should
     * be initialized here.
     * @return Returns true if the initialization is successful and the window
     * should be shown, and false otherwise.
     */
    protected boolean init() {
        return true;
    }
    
    /**
     * Return a byte array that can be stored in an external file.
     * @return The document content.
     */
    abstract protected byte[] getDocumentData();
    
    /**
     * Restore the document content from a passed byte array.
     * @param data The document content.
     * @throws An exception is thrown if the passed data cannot be used to set
     * the data of the document.
     */
    abstract protected void setDocumentData(byte[] data) throws Exception;
    
    /**
     * Creates a new sub-class of MainWindow and returns it. The name of the 
     * class that is instantiated is extracted from the MainWindow.properties 
     * file, where the property is called "MainWindow".
     * This method only calls window.init() when told to do so.
     * @param callIInit If true, window.init() is called. The window is usually 
     * displayed to the user if init is true.     
     * @return The new document window.
     * @throws java.lang.ClassNotFoundException 
     * @throws java.lang.InstantiationException 
     * @throws java.lang.IllegalAccessException 
     */
    private static MainWindow createNewMainWindow(boolean callIInit) 
    throws java.lang.ClassNotFoundException, 
            java.lang.InstantiationException,
            IllegalAccessException {
        
        java.util.Properties props 
                = ika.utils.PropertiesLoader.loadProperties("ika.app.Application");
        String className = props.getProperty("MainWindow");
        MainWindow window = (MainWindow)Class.forName(className).newInstance();
        if (window == null) {
            return null;
        }
        
        if (callIInit) {
            if (!window.init()) {
                return null;
            }
            
            // remember the size
            Dimension size = window.getSize();
            
            // pack the window (= bring it to its smallest possible size) 
            // and store this minimum size
            window.pack();
            window.packedSize = window.getSize();
            
            // restore the size
            window.setSize(size);
            
        }
        
        return window;
    }
    
    /**
     * Returns a clone of the menu bar used by this document window.
     */
    public static JMenuBar getMenuBarClone() {
        
        MainWindow tempWindow = null;
        try {
            tempWindow = MainWindow.createNewMainWindow(false);
            
            JMenuBar menuBar = tempWindow.getJMenuBar();
            
            // the window was added to the menu that lists all windows. Remove it.
            MainWindow.windows.remove(tempWindow);
            
            // creating the window increased the window counter. Decrease it again.
            --MainWindow.windowCounter;
            
            tempWindow.dispose();
            
            return menuBar;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * Returns the frontmost document window that currently owns the focus.
     * @return The window with the focus, or null if there is no focused window.
     */
    public static MainWindow getFocusedFrontWindow() {
        // search the foreground window that owns the focus
        Window focusedWindow = ika.utils.FocusUtils.focusedWindow();
        if (focusedWindow instanceof javax.swing.JFrame == false)
            return null;
        javax.swing.JFrame foregroundFrame = (javax.swing.JFrame)focusedWindow;
        
        // search the focused window in the array of document windows.
        final int windowsCount = MainWindow.windows.size();
        for (int i = 0; i < windowsCount; i++) {
            MainWindow w = (MainWindow)MainWindow.windows.elementAt(i);
            if (w == foregroundFrame)
                return w;
        }
        return null;
    }
    
    /**
     * Update the menu bar of this window. Each window has its own clone of the
     * menu bar.
     * @param foregroundWindow The window currently in the foreground.
     * @param windowMenu The menu where each window will be appended to.
     */
    public static void updateWindowMenu(JMenu windowMenu, JFrame foregroundWindow) {
        
        final int windowsCount = MainWindow.windows.size();
        
        // enable or disable the "Minimize" and "Zoom" menu items
        boolean hasVisibleWindow = false;
        for (int i = 0; i < windowsCount; i++) {
            MainWindow w = (MainWindow)MainWindow.windows.elementAt(i);
            if (w.isVisible() &&
                    (w.getState() & Frame.ICONIFIED) != Frame.ICONIFIED){
                hasVisibleWindow = true;
                break;
            }
        }
        JMenuItem minimizeMenuItem = (JMenuItem)windowMenu.getMenuComponent(0);
        JMenuItem zoomMenuItem = (JMenuItem)windowMenu.getMenuComponent(1);
        minimizeMenuItem.setEnabled(hasVisibleWindow);
        zoomMenuItem.setEnabled(hasVisibleWindow);
        
        // remove all menu items, except the first "Minimize" item, the second
        // "Zoom" item and the following separator.
        final int nbrMenuItems = windowMenu.getMenuComponentCount();
        for (int i = nbrMenuItems - 1; i >= 3; i--) {
            windowMenu.remove(i);
        }
        
        // add each window again.
        for (int i = 0; i < windowsCount; i++) {
            MainWindow w = (MainWindow)MainWindow.windows.elementAt(i);
            
            JMenuItem menuItem;
            if (foregroundWindow == w && foregroundWindow != null) {
                menuItem = new JCheckBoxMenuItem(foregroundWindow.getTitle(), true);
            } else {
                menuItem = new JMenuItem(w.getTitle());
            }
            windowMenu.add(menuItem);
            menuItem.setName(Integer.toString(i));
            menuItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    JMenuItem menuItem = (JMenuItem)evt.getSource();
                    try {
                        ((JCheckBoxMenuItem)menuItem).setState(true);
                    } catch (ClassCastException ex){}
                    
                    int id = Integer.parseInt(menuItem.getName());
                    MainWindow w = (MainWindow)MainWindow.windows.elementAt(id);
                    if (w.getExtendedState() == Frame.ICONIFIED) {
                        w.setExtendedState(Frame.NORMAL);
                    }
                    
                    // bring the selected window to the front
                    w.toFront();
                }
            });
        }
    }
    
    /**
     * Creates a new empty document. The new document window is added to the
     * menu listing all currently open windows.
     * @return A new empty document, which is a sub-class of MainWindow.
     */
    static public MainWindow newDocumentWindow() {
        MainWindow w = null;
        try {
            w = MainWindow.createNewMainWindow(true);
            if (w != null) {
                w.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return w;
        }
    }
    
    /**
     * Asks the user to select a file and opens a new window and initializes 
     * its contents with the selected file.
     * @return The new document window, or null if no document was opened.
     */
    public MainWindow openDocumentWindow() {
        try {
            MainWindow w = null;
            String newFilePath = FileUtils.askFile(this, "Open Document", true);
            if (newFilePath == null) {
                return null; // user canceled
            }
            
            // import data from the file
            File file = new File(newFilePath);
            byte[] data = FileUtils.getBytesFromFile(file);
            
            // successfully imported new data. Create new window.
            w = MainWindow.createNewMainWindow(true);
            w.setDocumentData(data);
            w.setTitle(FileUtils.getFileNameWithoutExtension(newFilePath));
            w.filePath = newFilePath;
            w.setVisible(true);
            w.setDocumentClean();
            return w;
        } catch(Exception e) {
            ika.utils.ErrorDialog.showErrorDialog("The file could not be opened.",
                    "File Error", e, this);
            return null;
        }
    }

    /**
     * Ask the user whether changes to the document should be saved.
     * @return True if the document window can be closed, false otherwise.
     */
    protected boolean canDocumentBeClosed() {
        switch (SaveFilePanel.showSaveDialogForNamedDocument(this, getTitle())) {
            case DONTSAVE:
                // document has possibly been edited but user does not want to save it
                return true;

            case CANCEL:
                // document has been edited and user canceled
                return false;

            case SAVE:
                // document has been edited and user wants to save it
                if (this.filePath == null || !new File(this.filePath).exists()) {
                    this.filePath = this.askFileToSave("Save");
                    if (this.filePath == null) {
                        return false; // user canceled
                    }
                }
                return this.saveDocumentWindow(this.filePath);
        }
        return false;
    }

    /**
     * Closes this window. Asks the user whether to save or discard changes
     * if necessary, and saves the document to a file if necessary.
     * @return True if the document has been closed, false otherwise.
     */
    protected boolean closeDocumentWindow() {

        // Ask user whether to save the document if it has been edited.
        if (this.dirty && this.isVisible()) {
            
            // first make it visible if it has been miminized
            if ((this.getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED) {
                this.setExtendedState(Frame.MAXIMIZED_BOTH);
                this.validate();
            }
            
            // ask the user whether to save the document
            if (!canDocumentBeClosed())
                return false;
        }
        
        // remove this window from the list of open windows
        MainWindow.windows.remove(this);
        
        // dispose and close it
        this.setVisible(false);
        this.dispose();
        
        // close the about dialog if this was the last open document on Windows or Linux
        if (MainWindow.windows.size() == 0 && !ika.utils.Sys.isMacOSX())
            ika.gui.ProgramInfoPanel.hideApplicationInfo();
        
        // if this is not running on Mac, the application should exit if
        // the last window is closed.
        if (MainWindow.windows.size() == 0 && !ika.utils.Sys.isMacOSX()) {
            System.exit(0);
        }
        
        return true;
    }
    
    /**
     * Closes all currently open document windows that are stored in  
     * this.windows vector. 
     * Asks the user for a location to store the document if it has not been 
     * saved yet. Stops closing windows when the user cancels.
     * @return True if all windows have been closed, false otherwise.
     */
    public static boolean closeAllDocumentWindows() {
        final int windowsCount = MainWindow.windows.size();
        boolean windowsClosed = windowsCount < 1;
        for (int i = windowsCount - 1; i >= 0; i--) {
            MainWindow w = (MainWindow)MainWindow.windows.get(i);
            windowsClosed = w.closeDocumentWindow();
            if (!windowsClosed) // stop if user cancels closing of windows
                return false;
        }
        return true;
    }
    
    /**
     * Saves this document window to a file. Asks the user for a location to
     * store the document if it has not been saved yet.
     * @return True if the document has been succesfully saved, false otherwise.
     */
    protected boolean saveDocumentWindow() {
        // ask for file path if the document has never been saved or if its
        // path is invalid.
        if (filePath == null || !new java.io.File(filePath).exists()) {
            String newFilePath = this.askFileToSave("Save Document");
            if (newFilePath == null) { // user canceled
                return false;
            }
            filePath = newFilePath;
        }
        
        return saveDocumentWindow(filePath);
    }
    
    /**
     * Saves this document to a file and displays an error message if an error
     * occurs.
     * @param filePath The path to the file.
     * @return True if the document has been succesfully saved, false otherwise.
     */
    protected boolean saveDocumentWindow(String filePath) {
        if (filePath == null) {
            return false;
        }
        java.io.OutputStream outputStream = null;
        try {
            byte[] data = this.getDocumentData();
            outputStream = new java.io.FileOutputStream(filePath);
            outputStream = new java.io.BufferedOutputStream(outputStream);
            outputStream.write(data);
        
            // ika.utils.Serializer.serialize(obj, false, filePath);
            this.setDocumentClean();
            this.setTitle(ika.utils.FileUtils.getFileNameWithoutExtension(filePath));
            
            // store the path to the file
            this.filePath = filePath;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            final Icon icon = ika.app.ApplicationInfo.getApplicationIcon();
            String newline = System.getProperty("line.separator");
            JOptionPane.showMessageDialog(this, "An error occured. " +
                    "The file could not be saved." + newline +
                    "Please try to save the file to another location.",
                    "File Error", JOptionPane.ERROR_MESSAGE, icon);
            return false;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (java.io.IOException e) {
                }
            }
        }
    }
    
    /**
     * Sets the dirty flag of this window. This indicates that the documents must
     * be saved before it can be closed.
     */
    protected void setDocumentDirty() {
        if (!this.dirty) {
            this.dirty = true;
            this.getRootPane().putClientProperty("windowModified", Boolean.TRUE);
        }
    }
    
    /**
     * Clears the dirty flag of this window. This indicates that the documents 
     * has been saved to disk and that it is save to close it.
     */
    protected void setDocumentClean() {
        if (this.dirty) {
            this.dirty = false;
            this.getRootPane().putClientProperty("windowModified", Boolean.FALSE);
        }
    }
    
    /**
     * Returns whether the document must be saved before being closed.
     * @return True if the document has unsaved edits, false otherwise.
     */
    public boolean isDocumentDirty() {
        return dirty;
    }

    /**
     * Asks the user for a new file to save this document.
     * @param message Message displayed in the file dialog (e.g. "Save As").
     * @return The path to the new file, or null if the user cancels.
     */
    protected String askFileToSave(String message) {
        // ask for file path
        String ext = ika.app.ApplicationInfo.getDocumentExtension();
        String name = FileUtils.forceFileNameExtension(this.getTitle(), ext);
        return FileUtils.askFile(this, message, name, false, ext);
    }
    
    /**
     * Part of the ComponentListener interface.
     */
    public void componentShown(ComponentEvent e) {
    }
    
    /**
     * Part of the ComponentListener interface.
     */
    public void componentHidden(ComponentEvent e) {
    }
    
    /**
     * Part of the ComponentListener interface.
     */
    public void componentMoved(ComponentEvent e) {
    }
    
    /**
     * Part of the ComponentListener interface. Make sure this window is not 
     * getting too small.
     */
    public void componentResized(ComponentEvent e) {
        
        if (this.packedSize == null)
            return;
        
        // Check if either the width or the height are below minimum
        // and reset size if necessary.
        // Note: this is not elegant, but SUN recommends doing it that way.
        int width = getWidth();
        int height = getHeight();
        boolean resize = false;
        
        if (width < this.packedSize.width) {
            resize = true;
            width = this.packedSize.width;
        }
        if (height < this.packedSize.height) {
            resize = true;
            height = this.packedSize.height;
        }
        if (resize) {
            setSize(width, height);
        }
    }
   
}
