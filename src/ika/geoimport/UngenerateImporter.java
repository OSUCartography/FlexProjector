/*
 * UngenerateImporter.java
 *
 * Created on April 1, 2005, 12:02 PM
 */
package ika.geoimport;

import ika.geo.*;
import ika.gui.ProgressIndicator;
import java.util.*;
import java.io.*;

/**
 * An importer for the ESRI Ungenerate file format.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class UngenerateImporter extends GeoImporter {

    /**
     * Coordinates can be separated by a coma, a space or a tab. Comas are not
     * standard, but seem to be often used.
     */
    private static final String VALUE_SEPARATOR = ", \t";
    private long charsRead;

    /**
     * Test if the passed file contains valid data. At least one GeoPath must
     * be successfully read to pass the test.
     */
    protected java.net.URL findDataURL(java.net.URL url) {

        if (url == null) {
            return null;
        }

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            GeoPath geoPath = this.readGeoPath(in);
            if (geoPath == null) {
                return null;
            } else {
                return url;
            }
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

    /**
     * Reads an Ungenerate file and returns the found GeoObjects in a GeoSet.
     * @param url The file to import.
     * @return A GeoSet containing all read GeoObjects.
     */
    protected GeoObject importData(java.net.URL url) throws IOException {

        GeoSet geoSet = this.createGeoSet();
        geoSet.setName(ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath()));

        // count the numbers of read characters and get the size of the file
        // for progress indication
        this.charsRead = 0;
        long fileSize = url.openConnection().getContentLength();

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        int counter = 0;
        try {
            String idStr;
            while ((idStr = in.readLine()) != null) {
                this.charsRead += idStr.length() + 1;

                GeoPath geoPath = this.readGeoPath(in);
                if (geoPath != null) {
                    geoPath.setName(idStr.trim());
                    geoSet.add(geoPath);
                }

                // update progress indicator
                if (counter++ % 10 == 0) {
                    final int percentage = (int) (100d * this.charsRead / fileSize);
                    ProgressIndicator pi = getProgressIndicator();
                    if (pi != null) {
                        pi.progress(percentage);
                        if (pi.isAborted()) {
                            return null;
                        }
                    }
                }
            }
        } finally {
            in.close();
        }

        return geoSet;
    }

    public String getImporterName() {
        return "Ungenerate Importer";
    }

    private GeoPath readGeoPath(BufferedReader in) throws java.io.IOException {
        String str;
        boolean firstPoint = true;
        GeoPath geoPath = this.createGeoPath();

        while (true) {
            str = in.readLine();
            if (str == null || str.length() == 0) {
                break;
            }
            this.charsRead += str.length() + 1;

            str = str.trim().toLowerCase();

            if (str.startsWith("end")) {
                break;
            }
            try {
                StringTokenizer tokenizer = new StringTokenizer(str, VALUE_SEPARATOR);
                double x = Double.parseDouble(tokenizer.nextToken());
                double y = Double.parseDouble(tokenizer.nextToken());
                if (firstPoint) {
                    geoPath.moveTo(x, y);
                    firstPoint = false;
                } else {
                    geoPath.lineTo(x, y);
                }
            } catch (NoSuchElementException e) {
                // found a line without any readable data. Just read the next line
            }
        }

        if (geoPath.hasOneOrMorePoints()) {
            VectorSymbol symbol = new VectorSymbol(java.awt.Color.blue, java.awt.Color.black, 1);
            symbol.setScaleInvariant(true);
            geoPath.setVectorSymbol(symbol);
            return geoPath;
        }
        return null;
    }
}
