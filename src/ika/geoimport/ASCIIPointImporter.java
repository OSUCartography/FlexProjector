/*
 * ASCIIPointImporter.java
 *
 * Created on June 29, 2005, 4:28 PM
 *
 */
package ika.geoimport;

import ika.gui.ProgressIndicator;
import java.io.*;
import ika.geo.*;
import java.util.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class ASCIIPointImporter extends GeoImporter {

    private boolean firstColumnIsName = true;

    public ASCIIPointImporter() {
    }
    
    public ASCIIPointImporter(boolean firstColumnIsName) {
        this.firstColumnIsName = firstColumnIsName;
    }
    
    /**
     * Reads an ASCII file with points. First column contains the names, the
     * second column x coordinates, the third column y coordinates. A line may
     * contain additional data or columns, which is ignored.
     */
    protected GeoObject importData(java.net.URL url) throws IOException {

        GeoSet geoSet = this.createGeoSet();
        geoSet.setName(ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath()));

        // count the numbers of read characters and get the size of the file
        // for progress indication
        long charsRead = 0;
        int fileSize = url.openConnection().getContentLength();

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str, name = null;
            while ((str = in.readLine()) != null) {
                charsRead += str.length() + 1;
                StringTokenizer tokenizer = new StringTokenizer(str, " \t,;");
                if (firstColumnIsName) {
                    name = (String) tokenizer.nextToken();
                }
                double x = Double.parseDouble((String) tokenizer.nextToken());
                double y = Double.parseDouble((String) tokenizer.nextToken());

                GeoPoint point = new GeoPoint(x, y);
                if (firstColumnIsName) {
                    point.setName(name);
                }
                geoSet.add(point);

                // update progress indicator
                ProgressIndicator progressIndicator = this.getProgressIndicator();
                if (progressIndicator != null) {
                    final int percentage = (int) (100d * charsRead / fileSize);
                    progressIndicator.progress(percentage);
                    if (progressIndicator.isAborted()) {
                        return null;
                    }
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        return geoSet;
    }

    public String getImporterName() {
        return "ASCII Point Importer";
    }

    /**
     * Test whether the passed file contains valid data. The first row in the
     * file must contain a name, x, and y, separated by tab, comma or
     * semicolumn.
     */
    protected java.net.URL findDataURL(java.net.URL url) {

        if (url == null) {
            return null;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = in.readLine();
            if (str == null) {
                return null;
            }

            StringTokenizer tokenizer = new StringTokenizer(str, " \t,;");
            if (firstColumnIsName) {
                tokenizer.nextToken();
            }
            Double.parseDouble((String) tokenizer.nextToken());
            Double.parseDouble((String) tokenizer.nextToken());
            return url;
        } catch (Exception e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
