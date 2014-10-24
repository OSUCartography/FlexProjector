/*
 * ApplicationIcon.java
 *
 * Created on March 29, 2005, 11:39 PM
 */

package ika.utils;

/**
 * Returns an icon for this application.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ApplicationIcon {
    
    /**
     * Returns an icon for this application.
     * @return The icon of this application.
     */
    public static final javax.swing.Icon getApplicationIcon() {
        try {
            Class applicationInfo = Class.forName("ika.app.ApplicationInfo");
            java.lang.reflect.Method met = applicationInfo.getMethod(
                    "getApplicationIcon", (java.lang.Class[])null);
            return (javax.swing.Icon)met.invoke(null, (java.lang.Object[])null);
        } catch (Exception exc) {
            return null;
        }
    }
    
}
