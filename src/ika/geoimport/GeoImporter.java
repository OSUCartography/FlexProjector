/*
 * GeoImporter.java
 *
 * Created on May 28, 2006, 12:44 PM
 *
 */

package ika.geoimport;

import ika.geo.*;
import ika.gui.ProgressIndicator;
import ika.table.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Abstract class for importing GeoSets.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class GeoImporter extends Thread {
    
    final static private String GEOIMPORTER_PROPERTIES = 
            "ika.geoimport.geoimport";
    
    final static private String GEOIMPORTERS_PROPERTY = "geoimporters";
    
    final static private String GEOPATH_PROPERTY = "geopath";
    
    final static private String GEOSET_PROPERTY = "geoset";
    
    final static public boolean NEW_THREAD = true;
    
    final static public boolean SAME_THREAD = false;

     /**
     * A ProgressIndicator that is displayed during a long import.
     * The ProgressIndicator must be set using setProgressIndicator().
     * The default is not to have any ProgressIndicator.
     */
    protected ProgressIndicator progressIndicator = null;

    /**
     * call the complete() method of the progressIndicator when read() finishes.
     */
    private boolean callProgressComplete = false;


    /**
     * A copy of prototypeGeoPath is returned by createGeoPath().
     */
    protected static GeoPath prototypeGeoPath;
    static {
        try {
            Properties properties =
                ika.utils.PropertiesLoader.loadProperties(GEOIMPORTER_PROPERTIES);
            String pathClassName = properties.getProperty(GEOPATH_PROPERTY);
            Class pathClass = Class.forName(pathClassName);
            Constructor con = pathClass.getConstructor(new Class[]{});
            prototypeGeoPath = (GeoPath)con.newInstance(new Object[]{});
        } catch (Exception e) {
            prototypeGeoPath = new GeoPath();
        }
    }

    
    /**
     * Derived importer classes should not create default GeoPaths, but 
     * ask createGeoPath for a new GeoPath. This allows for customizing the type
     * of new GeoPaths.
     */
    protected GeoPath createGeoPath() {
        return (GeoPath)prototypeGeoPath.clone();
    }
    
    /**
     * A copy of prototypeGeoSet is returned by createGeoSet().
     */
    protected static GeoSet prototypeGeoSet;
    static {
        try {
            Properties properties =
                    ika.utils.PropertiesLoader.loadProperties(GEOIMPORTER_PROPERTIES);
            String pathClassName = properties.getProperty(GEOSET_PROPERTY);
            Class pathClass = Class.forName(pathClassName);
            Constructor con = pathClass.getConstructor(new Class[]{});
            prototypeGeoSet = (GeoSet)con.newInstance(new Object[]{});
        } catch (Exception e) {
            prototypeGeoSet = new GeoSet();
        }
    }
    
    /**
     * Derived importer classes should not create default GeoSets, but 
     * ask createGeoSet for a new GeoSet. This allows for customizing the type
     * of new GeoSets.
     */
    protected GeoSet createGeoSet() {
        return (GeoSet)prototypeGeoSet.clone();
    }
    
   
    /**
     * GeoImporters that are asked to import data sources.
     * The names of the GeoImporters are loaded from a properties file located
     * at GEOIMPORTER_PROPERTIES.
     * The property GEOIMPORTERS_PROPERTY contains all class names.
     * GeoImporter.findGeoSetImporter(s) searches through this list until it 
     * finds an importer for a particular data source. The order of the importers 
     * therefore matters.
     */
    private static final ArrayList importers = new ArrayList();
    static {
        String classesString = null;
        try {
            // load the class names of the GeoImporters from a properties file.
            Properties properties = ika.utils.PropertiesLoader.loadProperties(
                    GEOIMPORTER_PROPERTIES);
            classesString = properties.getProperty(GEOIMPORTERS_PROPERTY);
        } catch (Exception exc) {
            classesString = "ika.geoimport.ImageImporter," +
                    "ika.geoimport.ShapeImporter," +
                    "ika.geoimport.UngenerateImporter," +
                    "ika.geoimport.ASCIIPointImporter";
        }
        
        String[] classes = classesString.split(",");
        for (int i = 0; i < classes.length; i++) {
            GeoImporter.importers.add(classes[i]);
        }
    }
    
    /**
     * Searches a GeoImporter for a passed URL.
     * 
     * @return The GeoImporter that claims to be able to import the data specified
     * by the passed URL or null if no importer can be found.
     */
    public static GeoImporter findGeoImporter(java.net.URL url) {
        try {
            Iterator iterator = GeoImporter.importers.iterator();
            while (iterator.hasNext()) {
                // construct an instance of an importer
                String importerClassName = (String)iterator.next();
                Class importerClass = Class.forName(importerClassName);
                Class partypes[] = new Class[0];
                Constructor cstr = importerClass.getConstructor(partypes);
                GeoImporter importer = (GeoImporter)cstr.newInstance((Object[])null);
                
                if (importer.findDataURL(url) != null)
                    return importer;
            }
            return null;
        } catch (Throwable e) {
            return null;
        }
    }
    
    /**
     * Searches a GeoImporter for a passed file path.
     * @return The GeoImporter that should be able to import the data or null 
     * if no importer could be found.
     */
    public static GeoImporter findGeoImporter(String filePath) {
        java.net.URL url = ika.utils.URLUtils.filePathToURL(filePath);
        if (url == null)
            return null;
        return GeoImporter.findGeoImporter(url);
    }
    
    /**
     * Searches GeoSetImporters for a set of passed files. Makes sure that 
     * only one importer is returned per importable data set. A data set can
     * consist of multiple files.
     * 
     * @param files A collection of File objects.
     * @return An Hashtable containing a series of file paths as String(keys) 
     * and the associated GeoImporter (value).
     */
    
    public static Hashtable findGeoImporters(Collection files) {
        Hashtable url_importer_pairs = new Hashtable();
        if (files == null)
            return url_importer_pairs;
        
        try {
            // loop over all files
            Iterator fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                File file = (File) fileIterator.next();
                java.net.URL url = file.toURL();
                
                // ask each importer whether it can import the file
                Iterator importerIterator = GeoImporter.importers.iterator();
                while (importerIterator.hasNext()) {
                    
                    // build an instance of the importer
                    GeoImporter importer = null;
                    String importerClassName = "";
                    try {
                        // construct an instance of an importer
                        importerClassName = (String)importerIterator.next();
                        Class importerClass = Class.forName(importerClassName);
                        Constructor cstr = importerClass.getConstructor(new Class[0]);
                        importer = (GeoImporter)cstr.newInstance(new Object[0]);
                    } catch (Throwable e) {
                        System.err.println ("Could not instantiate " + importerClassName);
                        e.printStackTrace();
                        importer = null;
                    }
                    
                    // ask the importer whether it can import the file
                    try {
                        if (importer != null) {
                            // ask the importer for the file containing the data
                            java.net.URL dataURL = importer.findDataURL(url);
                            if (dataURL != null) {
                                url_importer_pairs.put(dataURL, importer);
                                break;  // no need to ask other importers, go to next file.
                            }
                        }
                    } catch (Throwable e) {}
                }
            }
        } catch (Throwable e) {}
        
        return url_importer_pairs;
    }
    
    /**
     * Creates a new instance of GeoImporter
     */
    protected GeoImporter() {  
    }
    
    /**
     * Read data from the passed URL and return as GeoObject.
     * The concrete implementation of importData of derived classes must use 
     * this.createGeoPath() to create new GeoPaths, and 
     * this.createGeoSet() to create new GeoSets.
     * importData() is protected. Use read() to import data, wich takes care 
     * of displaying a progress indicator and calling the DataReceiver.
     */
    protected abstract GeoObject importData(java.net.URL url) throws IOException;
    
    /**
     * Returns a human readable string with the name of this GeoImporter.
     */
    public abstract String getImporterName();
    
    /**
     * Read some data. The data is stored by the DataReceiver. If an error 
     * occurs, DataReceiver.error() is called.
     * @param url The data source to read.
     * @param mapDataReceiver DataReceiver that is responsible for storing imported data.
     * @param newOrSameThread If newOrSameThread equals GeoImporter.NEW_THREAD an 
     * additional thread is started to read the data. If newOrSameThread equals 
     * GeoImporter.SAME_THREAD no additional thread is started.
     */
    public void read(java.net.URL url, DataReceiver mapDataReceiver, 
            boolean newOrSameThread) {
        
        // initialize the progress indicator.
        // the run() method is responsible for closing the progress indicator.
        if (this.progressIndicator != null) {
            this.progressIndicator.start();
        }
        
        this.threadParams.url = url;
        this.threadParams.dataReceiver = mapDataReceiver;
        if (newOrSameThread == GeoImporter.NEW_THREAD) {
            this.start();
        } else {
            this.run();
        }
    }
    
    /**
     * Read data from the passed file and return as GeoObject.
     */
    public GeoObject read (String filePath ) throws IOException {
        java.net.URL url = ika.utils.URLUtils.filePathToURL(filePath);
        SynchroneDataReceiver dataReceiver = new SynchroneDataReceiver();
        this.read(url, dataReceiver, GeoImporter.SAME_THREAD);
        return dataReceiver.getImportedData();
    }

    /**
     * Read data from the passed file and return as GeoObject.
     */
    public GeoObject read (String filePath, GeoSet destinationGeoSet) throws IOException {
        java.net.URL url = ika.utils.URLUtils.filePathToURL(filePath);
        SynchroneDataReceiver dataReceiver = new SynchroneDataReceiver(destinationGeoSet);
        this.read(url, dataReceiver, GeoImporter.SAME_THREAD);
        return dataReceiver.getImportedData();
    }
    
    /**
     * A private utility class to pass parameters to the thread that reads the
     * data.
     */
    private class ThreadParams {
        public java.net.URL url;
        public DataReceiver dataReceiver;
    };
    ThreadParams threadParams = new ThreadParams();
    
    /**
     * Read data.
     */
    @Override
    public void run() {
        try {
            if (threadParams.dataReceiver == null || threadParams.url == null) {
                return;
            }

            GeoObject importedGeoObj = importData(threadParams.url);

            // pass the imported GeoObject to the DataReceiver, which is 
            // responsible for storing it.
            if (this instanceof ika.table.TableLinkImporter) {
                TableLink tableLink = ((TableLinkImporter) this).getTableLink();
                if (tableLink == null) {
                    threadParams.dataReceiver.add(importedGeoObj);
                } else {
                    threadParams.dataReceiver.add(tableLink);
                }
            } else {
                threadParams.dataReceiver.add(importedGeoObj);
            }
        } catch (Exception e) {
            // hide the progress dialog
            if (progressIndicator != null && callProgressComplete) {
                progressIndicator.completeProgress();
            }

            // pass the exception to the DataReceiver, which is responsible for
            // informing the user.
            threadParams.dataReceiver.error(e, threadParams.url);
        } finally {
            if (this.progressIndicator != null && this.callProgressComplete) {
                this.progressIndicator.completeProgress();
            }
        }
    }

    /**
     * An importer may have to access multiple files to importData data, or it 
     * may not be clear to the user which file in a file set contains the data
     * (e.g. a Shape file data set also contains a .shx file). findDataURL will
     * search for the file that contains the actual data. It always returns the
     * same type of file if the data is spread on multiple files. E.g. for a 
     * Shape file data set: input data.shx, output data.shp
     * @param url The URL pointing to a potential data source.
     * @return The URL pointing to the data or null if no valid data is found.
     */
    protected abstract java.net.URL findDataURL(java.net.URL url);

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator, boolean callProgressComplete) {
        this.progressIndicator = progressIndicator;
        this.callProgressComplete = callProgressComplete;
    }

    @Override
    public String toString() {
        return this.getImporterName();
    }
}
