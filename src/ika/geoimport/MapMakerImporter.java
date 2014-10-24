/*
 * MapMakerImporter.java
 *
 * Created on February 15, 2006, 1:34 PM
 *
 */

package ika.geoimport;

import ika.geo.*;
import ika.geoimport.*;
import ika.gui.ProgressIndicator;
import java.util.*;
import java.io.*;

/**
 * An importer for the MapMaker file format.
 * @author jenny
 */
public class MapMakerImporter extends GeoImporter {
    
    private String SEPARATOR = "\t";
    private boolean readPoints = false;
    private long charsRead;
    
    public MapMakerImporter(){
    }
    
    private String readDataLine(BufferedReader in) {
        try {
            while (true) {
                String str = in.readLine();
                if (str == null)
                    return null;
                
                this.charsRead += str.length() + 1;
                
                // '!' seem to mark comments
                if (str.startsWith("!"))
                    continue;
                
                // if the first column is empty, the line is a comment
                if (str.startsWith(SEPARATOR))
                    continue;
                
                // every line must start with a number, otherwise it's a comment
                str = str.trim();
                if (!str.matches("^\\d.*"))
                    continue;
                return str;
            }
        } catch (java.io.IOException exc) {
            return null;
        }
    }
    
    private void readXY (float[] xy, BufferedReader in) {
        String str = readDataLine(in);
        StringTokenizer tokenizer = new StringTokenizer(str, SEPARATOR);
        xy[0] = Float.parseFloat((String)tokenizer.nextToken());
        xy[1] = Float.parseFloat((String)tokenizer.nextToken());
    }
    
    protected GeoObject importData(java.net.URL url) throws IOException {
        GeoSet geoSet = this.createGeoSet();
        geoSet.setName(ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath()));
        
        // count the numbers of read characters and get the size of the file
        // for progress indication
        this.charsRead = 0;
        long fileSize = url.openConnection().getContentLength();
        
        BufferedReader in = new BufferedReader(new InputStreamReader (url.openStream()));
        try {
            String str;
            float[] xy = new float[2];
            while ((str = this.readDataLine(in)) != null) {
                
                // don't use empty space ' ' to tokenize, since names can consist
                // of multiple words.
                StringTokenizer tokenizer = new StringTokenizer(str, SEPARATOR);
                int id = Integer.parseInt((String)tokenizer.nextToken());
                String name = (String)tokenizer.nextToken();
                int npts = Integer.parseInt((String)tokenizer.nextToken());
                
                if (npts == 1 && this.readPoints) {          // point
                    this.readXY(xy, in);
                    GeoPoint geoPoint = new GeoPoint(xy[0], xy[1]);
                    geoPoint.setName(name);
                    geoPoint.setID(id);
                    geoSet.add(geoPoint);
                } else if (!this.readPoints) {    // surface or line
                    GeoPath geoPath = this.createGeoPath();
                    geoPath.setName(name);
                    geoPath.setID(id);
                    final boolean close = npts > 1;
                    npts = Math.abs(npts);
                    float startX = 0;
                    float startY = 0;
                    for (int i = 0; i < npts; i++) {
                        this.readXY(xy, in);
                        if (i == 0) {
                            geoPath.moveTo(xy[0], xy[1]);
                            startX = xy[0];
                            startY = xy[1];
                        } else {
                            if (xy[0] == startX && xy[1] == startY) {
                                geoPath.closePath();
                                if (i == npts - 1)
                                    break;
                                this.readXY(xy, in);
                                geoPath.moveTo(xy[0], xy[1]);
                                startX = xy[0];
                                startY = xy[1];
                                i++;
                            } else {
                                geoPath.lineTo(xy[0], xy[1]);
                            }
                        }
                    }
                    
                    if (geoPath.hasOneOrMorePoints()) {
                        if (close)
                            geoPath.closePath();
                        geoSet.add(geoPath);
                    }
                    
                }
                
                // update progress indicator
                ProgressIndicator progressIndicator = this.getProgressIndicator();
                if (progressIndicator != null) {
                    final int percentage = (int)(100d * this.charsRead / fileSize);
                    progressIndicator.progress(percentage);
                    if (progressIndicator.isAborted())
                        return null;
                }
            }
        } catch (NoSuchElementException e) {
            
        } finally {
            in.close();
        }
        
        // assign VectorSymbol to GeoSet
        VectorSymbol symbol = new VectorSymbol(java.awt.Color.black, java.awt.Color.black, 1);
        symbol.setScaleInvariant(true);
        geoSet.setVectorSymbol(symbol);
        
        return geoSet;
    }
    
    public String getImporterName() {
        return "MapMaker Importer";
    }
    
    public void setReadPoints(boolean readPoints) {
        this.readPoints = readPoints;
    }
    
    /**
     * Test if the passed file contains valid data.
     * At least one valid number is required.
     */
    protected java.net.URL findDataURL(java.net.URL url) {
        
        if (url == null)
            return null;
   
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader (url.openStream()));
            String str = this.readDataLine(in);
            StringTokenizer tokenizer = new StringTokenizer(str, SEPARATOR);
            Float.parseFloat((String)tokenizer.nextToken());
            return url;
        } catch (Exception e) {
            return null;
        } finally {
            if (in != null)
                try { in.close(); } catch (IOException e) {}
        }

    }

}
