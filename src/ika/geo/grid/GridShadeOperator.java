/*
 * GridShadeOperator.java
 *
 * Created on January 28, 2006, 6:28 PM
 *
 */

package ika.geo.grid;

import ika.geo.*;
import ika.utils.ImageUtils;
import java.awt.image.BufferedImage;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridShadeOperator implements GridOperator {
    
    private double elevation = 25;
    private double azimuth = 315;
    private double LIGHT_AMBIENT = 0.4;
    private double LIGHT_DIFFUSE = 0.6;
    
    /** Creates a new instance of GridShadeOperator */
    public GridShadeOperator() {
    }
    
    public String getName() {
        return "Grid Shade";
    }
    
    public GeoGrid operate(GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int newCols = Math.max(2, geoGrid.getCols() - 1);
        final int newRows = Math.max(2, geoGrid.getRows() - 1);
        
        final double meshSize = geoGrid.getCellSize();
        GeoGrid newGrid = new GeoGrid(newCols, newRows, meshSize);
        newGrid.setWest(geoGrid.getWest() + meshSize);
        newGrid.setNorth(geoGrid.getNorth() + meshSize);
        
        if (newCols <= 2 || newRows <= 2) {
            return newGrid;
        }
        
        float[][] srcGrid = geoGrid.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();

        // compute normalized light vector for azimuth and zenith
        final double alpha = Math.toRadians(90 - this.azimuth);
        final double zenith = Math.toRadians(90 - this.elevation);
        final double sinz = Math.sin(zenith);
        final double luxX = (float) (Math.cos(alpha) * sinz);
        final double luxY = (float) (Math.sin(alpha) * sinz);
        final double luxZ = (float) Math.cos(zenith);

        for (int row = 0; row < srcRows - 1; row++) {
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < srcCols - 1; col++) {
                
                // two diagonal vectors
                final float d1 = srcGrid[row + 1][col + 1] - srcGrid[row][col];
                final float d2 = srcGrid[row][col + 1] - srcGrid[row + 1][col];
                double nx = d2 - d1;
                double ny = -d1 - d2;
                double nz = 2. * meshSize;
                
                // normalize vector
                final double lengthInv = Math.sqrt(nx*nx + ny*ny + nz*nz);
                nx /= lengthInv;
                ny /= lengthInv;
                nz /= lengthInv;
                
                // scalar product of light and normal vector
                final double cosa = luxX * nx + luxY * ny + luxZ * nz;
                if (cosa > 0.)
                    dstRow[col] = (float)(cosa) * 255.f;
            }
        }
        return newGrid;
    }
    
    public ika.geo.GeoImage operateToImage(ika.geo.GeoGrid geoGrid) {
        return phongShading(geoGrid);
        
        /*
        if (geoGrid == null)
            throw new IllegalArgumentException();
        
        final int imgCols = Math.max(2, geoGrid.getCols() - 1);
        final int imgRows = Math.max(2, geoGrid.getRows() - 1);
        final double meshSize = geoGrid.getCellSize();
        if (imgCols <= 2 || imgRows <= 2)
            return null;
        
        float[][] srcGrid = geoGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();

        // compute normalized light vector for azimuth and zenith
        final double alpha = Math.toRadians(90 - this.azimuth);
        final double zenith = Math.toRadians(90 - this.elevation);
        final double sinz = Math.sin(zenith);
        final double luxX = (float) (Math.cos(alpha) * sinz);
        final double luxY = (float) (-Math.sin(alpha) * sinz);
        final double luxZ = (float) Math.cos(zenith);

        short[] gray = new short[imgCols * imgRows];
        int grayID = 0;
        for (int row = 0; row < srcRows - 1; row++) {
            for (int col = 0; col < srcCols - 1; col++) {
                
                // two diagonal vectors
                final float d1 = srcGrid[row + 1][col + 1] - srcGrid[row][col];
                final float d2 = srcGrid[row][col + 1] - srcGrid[row + 1][col];
                double nx = d2 - d1;
                double ny = -d1 - d2;
                double nz = 2. * meshSize;
                
                // normalize vector
                final double lengthInv = Math.sqrt(nx*nx + ny*ny + nz*nz);
                nx /= lengthInv;
                ny /= lengthInv;
                nz /= lengthInv;
                
                // scalar product of light and normal vector
                final double cosa = luxX * nx + luxY * ny + luxZ * nz;
                
                if (cosa < 0.)
                    gray[grayID] = 0;
                else if (cosa > 1.)
                    gray[grayID] = 255;
                else
                    gray[grayID] = (short)(cosa * 255);
                ++grayID;                    
            }
        }
        
        BufferedImage image = ImageUtils.createGrayscaleImage(imgCols, imgRows, 8, gray);
        GeoImage newImage = new GeoImage(image, imgCols, imgRows, meshSize);
        newImage.setWest(geoGrid.getWest());
        newImage.setNorth(geoGrid.getNorth());
        
        return newImage;*/
    }
    
    public ika.geo.GeoImage phongShading(ika.geo.GeoGrid geoGrid) {
        if (geoGrid == null) {
            throw new IllegalArgumentException();
        }
        
        final int imgCols = Math.max(2, geoGrid.getCols() - 3);
        final int imgRows = Math.max(2, geoGrid.getRows() - 3);
        final double d = geoGrid.getCellSize();
        final double d2 = 2 * d;
        if (imgCols <= 2 || imgRows <= 2) {
            return null;
        }
        
        float[][] srcGrid = geoGrid.getGrid();
        final int srcRows = geoGrid.getRows();
        final int srcCols = geoGrid.getCols();

        // compute normalized light vector for azimuth and zenith
        final double alpha = Math.toRadians(90 - this.azimuth);
        final double zenith = Math.toRadians(90 - this.elevation);
        final double sinz = Math.sin(zenith);
        final double luxX = Math.cos(alpha) * sinz;
        final double luxY = Math.sin(alpha) * sinz;
        final double luxZ = Math.cos(zenith);

        byte[] gray = new byte[imgCols * imgRows];
        int grayID = 0;
        for (int row = 1; row < srcRows - 2; row++) {
            for (int col = 1; col < srcCols - 2; col++) {
                
                final float v01 = srcGrid[row - 1][col];
                final float v02 = srcGrid[row - 1][col + 1];
                final float v10 = srcGrid[row][col - 1];
                final float v11 = srcGrid[row][col];
                final float v12 = srcGrid[row][col + 1];
                final float v13 = srcGrid[row][col + 2];
                final float v20 = srcGrid[row + 1][col - 1];
                final float v21 = srcGrid[row + 1][col];
                final float v22 = srcGrid[row + 1][col + 1];
                final float v23 = srcGrid[row + 1][col + 2];
                final float v31 = srcGrid[row + 2][col];
                final float v32 = srcGrid[row + 2][col + 1];
                
                // top left vector
                double xtl = v10 - v12;
                double ytl = v21 - v01;
                double ztl = d2;
                double l = Math.sqrt(xtl*xtl + ytl*ytl + ztl*ztl);
                xtl /= l;
                ytl /= l;
                ztl /= l;
                
                // top right vector
                double xtr = v11 - v13;
                double ytr = v22 - v02;
                double ztr = d2;
                l = Math.sqrt(xtr*xtr + ytr*ytr + ztr*ztr);
                xtr /= l;
                ytr /= l;
                ztr /= l;
                
                // bottom left vector
                double xbl = v20 - v22;
                double ybl = v31 - v11;
                double zbl = d2;
                l = Math.sqrt(xbl*xbl + ybl*ybl + zbl*zbl);
                xbl /= l;
                ybl /= l;
                zbl /= l;
                
                // bottom right vector
                double xbr = v21 - v23;
                double ybr = v32 - v12;
                double zbr = d2;
                l = Math.sqrt(xbr*xbr + ybr*ybr + zbr*zbr);
                xbr /= l;
                ybr /= l;
                zbr /= l;
                
                // sum of four vectors
                final double nx = (xtl + xtr + xbl + xbr) * 0.25;
                final double ny = (ytl + ytr + ybl + ybr) * 0.25;
                final double nz = (ztl + ztr + zbl + zbr) * 0.25;
                
                // scalar product of light and normal vector
                final double cosa = luxX * nx + luxY * ny + luxZ * nz;
                final double b = cosa > 0. ? cosa * LIGHT_DIFFUSE + LIGHT_AMBIENT : LIGHT_AMBIENT;
                /*if (b < 0.)
                    gray[grayID] = 0;
                else if (b > 1.)
                    gray[grayID] = 255;
                else*/
                gray[grayID] = (byte) (((short) (b * 255)) & 0xFF);
                ++grayID;                    
            }
        }
        
        BufferedImage image = ImageUtils.createGrayscaleImage(imgCols, imgRows, gray);
        GeoImage newImage = new GeoImage(image, imgCols, imgRows, d);
        newImage.setWest(geoGrid.getWest());
        newImage.setNorth(geoGrid.getNorth());
        
        return newImage;
    }
}
