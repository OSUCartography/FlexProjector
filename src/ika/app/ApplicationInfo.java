/*
 * ApplicationInfo.java
 *
 * Created on March 29, 2005, 11:39 PM
 */

package ika.app;

import java.util.Properties;

/**
 * Information about this application.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ApplicationInfo {
    
    private static String loadProperty(String propertyName) {
        Properties props 
                = ika.utils.PropertiesLoader.loadProperties("ika.app.Application");
        return props.getProperty(propertyName);
    }
    
    /**
     * Returns the name of this application.
     * @return The name of this application.
     */
    public static final String getApplicationName() {
        return loadProperty("ApplicationName");
    }
    
    /**
     * Returns a string containing version information about this application.
     * @return The version of this application.
     */
    public static final String getApplicationVersion() {
        return loadProperty("ApplicationVersion");
    }
    
    /**
     * Returns an icon for this application.
     * @return The icon of this application.
     */
    public static final javax.swing.Icon getApplicationIcon() {
        String iconName = loadProperty("ApplicationIcon");
        return ika.utils.IconUtils.loadImageIcon(iconName, null);
    }
    
    
    public static final javax.swing.Icon getLargeApplicationIcon() {
        String iconName = loadProperty("LargeApplicationIcon");
        return ika.utils.IconUtils.loadImageIcon(iconName, null);
    }
            
    /**
     * Returns a copyright string for this application.
     * @return The copyright description of this application.
     */
    public static final String getCopyright() {
        return loadProperty("CopyrightHTML");
    }
    
    /**
     * Returns information about this application.
     * @return The information about this application.
     */
    public static final String getInformation() {
        return loadProperty("InformationHTML");
    }
    
    /**
     * Returns the homepage (a html web address) for this application.
     * @return The homepage of this application.
     */
    public static final String getHomepage() {
        return loadProperty("Homepage");
    }
    
    /**
     * Returns the file extension for documents created by this application.
     * @return The file extension.
     */
    public static final String getDocumentExtension() {
        return loadProperty("FileExtension");
    }
   
}
