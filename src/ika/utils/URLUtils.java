/*
 * URLUtils.java
 *
 * Created on November 9, 2006, 11:37 AM
 *
 */

package ika.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class URLUtils {
    
    public static boolean resourceExists(URL url) {
        if (url.getProtocol().equals("file"))
            return new File (url.getFile()).exists();
        
        try {
            return (url.openStream().read() != -1); // there must be a better way for doing this !!! ???
            // is if (url.openStream() != null) enough?
        } catch (Exception exc) {
        }
        return false;
    }
    
    public static URL replaceFileExtension(URL url, String newFileExtension) {
        try {
            String filePath = url.getPath();
            filePath = FileUtils.replaceExtension(filePath, newFileExtension);
            return new java.net.URL(url.getProtocol(), url.getHost(),
                    url.getPort(), filePath);
        } catch (Exception exc) {
        }
        return null;
    }
    
    public static URL replaceFile (URL url, String newFileName) {
        try {
            return new java.net.URL(url.getProtocol(), url.getHost(),
                    url.getPort(), newFileName);
        } catch (Exception exc) {
        }
        return null;
    }
    
    public static byte[] loadFromURL(URL url) throws IOException {
        InputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            is = url.openStream();
            int r;
            byte[] buffer = new byte[8000];
            while ((r = is.read(buffer)) >= 0) {
                if (r == 0) continue;
                baos.write(buffer, 0, r);
            }
            return baos.toByteArray();
        } finally {
            if (is != null)
                is.close();
        }
    }
    
    public static URL filePathToURL (String filePath) {
        try {
        if (filePath.startsWith("/"))
            return new java.net.URL("file://" + filePath);
        else 
            return new java.net.URL("file:///" + filePath);
        } catch (MalformedURLException exc) {
            exc.printStackTrace();
            return null;
        }
    }
}
