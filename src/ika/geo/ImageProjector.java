/*
 * GridProjector.java
 *
 * Created on May 27, 2007, 5:43 PM
 *
 */

package ika.geo;

import com.jhlabs.map.MapMath;
import com.jhlabs.map.proj.EquidistantCylindricalProjection;
import ika.utils.ImageWriter;
import ika.utils.TIFFImageWriter;
import ika.geoexport.WorldFileExporter;
import ika.geoimport.GeoImporter;
import ika.geoimport.ImageImporter;
import ika.geoimport.SynchroneDataReceiver;
import ika.gui.SwingWorkerWithProgressIndicator;
import com.jhlabs.map.proj.Projection;
import ika.utils.FileUtils;
import java.awt.Frame;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import javax.swing.JFrame;

/**
 * Changes the projection of an image. Reads the image from a file and
 * stores the result in a new file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ImageProjector extends RasterProjector {
    
    /** The image will be projected to this Projection. */
    private Projection destProj;

    /** The source image uses this projection. */
    private Projection srcProj;
    
    /** The file to read the input grid. */
    private String importFilePath;
    
    /** The file that will receive the projected grid. */
    private String exportFilePath;
    
    /** True if neareast neighbor resampling. */
    private boolean nearestNeighbor = false;
    
    /**
     *
     * @param ownerFrame Parent frame of progress dialog
     * @param srcProj Projection of source image, must be initialized. If null,
     * a longitude/latitude graticule is used.
     * @param destProj Projection of final image, must be initialized. If null,
     * a longitude/latitude graticule is used.
     * @param importFilePath
     * @param exportFilePath
     * @param nearestNeighbor
     */
    public ImageProjector(JFrame ownerFrame, 
            Projection srcProj,
            Projection destProj,
            String importFilePath,
            String exportFilePath,
            boolean nearestNeighbor) {
        
        if (importFilePath == null || exportFilePath == null) {
            throw new IllegalArgumentException();
        }
        if (destProj == null) {
            destProj = new EquidistantCylindricalProjection();
            destProj.initialize();
        }
        if (srcProj == null) {
            srcProj = new EquidistantCylindricalProjection();
            srcProj.initialize();
        }

        this.destProj = destProj;
        this.srcProj = srcProj;
        this.importFilePath = importFilePath;
        this.exportFilePath = exportFilePath;
        this.nearestNeighbor = nearestNeighbor;
        
        String fileName = FileUtils.getFileName(importFilePath);
        String progressTitle = "Projecting " + fileName;
        ImageProjectorTask imageProjectorTask = new ImageProjectorTask(
                ownerFrame, progressTitle, null, false);
        imageProjectorTask.progress(0);
        imageProjectorTask.setIndeterminate(true);
        String msg = "<html><small>Reading Image<br></small></html>";
        imageProjectorTask.setMessage(msg);
        imageProjectorTask.setTotalTasksCount(2);
        imageProjectorTask.execute();
    }
    
    class ImageProjectorTask extends SwingWorkerWithProgressIndicator {
        
        private final double HALFPI = Math.PI / 2.;

        public ImageProjectorTask(Frame owner,
                String dialogTitle,
                String message,
                boolean blockOwner) {
            super(owner, dialogTitle, message, blockOwner);
        }

        
        @Override
        protected Object doInBackground() throws Exception {
            OutputStream out = null;
            String worldFilePath = WorldFileExporter.constructPath(exportFilePath);
            
            try {
                this.setProgress(0);
                
                // Create the file already now to show the user where the 
                // projected image will be stored.
                // Increasing the buffer size does not accelerate writing.
                out = new BufferedOutputStream(new FileOutputStream(exportFilePath));
                
                // read the input  file
                ImageImporter importer = new ImageImporter();
                importer.setProgressIndicator(this);
                importer.setOptimizeForDisplay(false);
                java.net.URL url = ika.utils.URLUtils.filePathToURL(importFilePath);
                SynchroneDataReceiver dataReceiver = new SynchroneDataReceiver();
                dataReceiver.setShowMessageOnError(false);
                importer.read(url, dataReceiver, GeoImporter.SAME_THREAD);
                
                // test whether the image has been successfully read
                if (dataReceiver.hasReceivedError()) {
                     (new File(exportFilePath)).delete();
                     throw new IOException("Could not read image file at "
                             + importFilePath);
                }
                
                // retrieve the image
                GeoImage image = (GeoImage)dataReceiver.getImportedData();
                if (image == null) {
                    return null; // user canceled
                }
                
                // make sure the image is georeferenced
                // assume geographic coordinates if the width is twice as large
                // as the height of the image. This is a hack. FIXME
                // 
                if (importer.isGeoreferenced() == false || image.getCols() == 2 * image.getRows()) {
                    
                    if (srcProj instanceof EquidistantCylindricalProjection) {

                        // if the image is twice as wide as high, assume it covers
                        // the whole globe if the source projection is plate carree

                        if (image.getCols() == 2 * image.getRows()) {
                            Point2D.Double pt = new Point2D.Double();
                            srcProj.transform(180, 90, pt);
                            image.setWest(-pt.getX());
                            image.setNorth(pt.getY());
                            image.setCellSize(pt.getX() * 2 / image.getCols());
                        } else {
                            throw new IOException("The image is neither "
                                    + "georeferenced, nore is the width twice the height.");
                        }
                    } else {

                        // for source projections other than plate carree, scale
                        // the image to cover the complete projected graticule
                        Rectangle2D projBounds = findProjectedExtension(srcProj, null);
                        image.setWest(projBounds.getMinX() );
                        image.setNorth(projBounds.getMaxY());
                        double hCellSize = projBounds.getWidth() / image.getCols();
                        double vCellSize = projBounds.getHeight() / image.getRows();
                        image.setCellSize((hCellSize + vCellSize) / 2.);
                    }

                }
                
                // update the progress monitor dialog
                this.nextTask();
                this.setMessage("<html><small>Projecting with "
                        + (nearestNeighbor ? "nearest neighbor" : "bicubic") 
                        + " interpolation."
                        + "<br>Saving to " 
                        + FileUtils.getFileName(exportFilePath)
                        + "</small></html>");
                
                Rectangle2D projBounds = findProjectedExtension(destProj, null);
                final double projWidth = projBounds.getWidth();
                final double projHeight = projBounds.getHeight();
                final double projWest = projBounds.getMinX();
                final double projNorth = projBounds.getMaxY();
                
                 // compute the cell size and size of the new image
                final double projCellSize = Math.min(projWidth / image.getCols(),
                        projHeight / image.getRows());
                final int projCols
                        = (int)Math.ceil(projWidth / projCellSize);
                final int projRows
                        = (int)Math.ceil(projHeight / projCellSize);
        
                ImageWriter writer = new TIFFImageWriter(out, projCols, projRows);
                
                final double earthRadius = destProj.getEquatorRadius();
                final double lon0 = destProj.getProjectionLongitude();
                Point2D.Double lonlat = new Point2D.Double();
                Point2D.Double srcXY = new Point2D.Double();

                for (int row = 0; row < projRows; row++) {
                    
                    if (this.isCancelled()) {
                        (new File(exportFilePath)).delete();
                        return null;
                    }
                    this.progress((int)((double)row / projRows * 100));
                    
                    final double dstY = (projNorth - row * projCellSize) / earthRadius;
                    for (int col = 0; col < projCols; col++) {
                        final double dstX = (projWest + col * projCellSize) / earthRadius;

                        // inverse projection from projected destination grid
                        // to intermediat longitude/latitude graticule

                        // don't use inverseTransformRadians here. The lon/lat values
                        // have to be checked after the inverse projection to make
                        // sure they fall in [-PI..+PI] for the longitude, and
                        // [-PI/2..+PI/2] for the latitude.
                        destProj.projectInverse(dstX, dstY, lonlat);
                        if (Double.isNaN(lonlat.x) || Double.isNaN(lonlat.y)
                                || lonlat.x < -Math.PI || lonlat.x > Math.PI
                                || lonlat.y < -HALFPI || lonlat.y > HALFPI) {
                            writer.write(0, 0, 0, 0);
                            continue;
                        }
                        if (lon0 != 0) {
                            lonlat.x = MapMath.normalizeLongitude(lonlat.x+lon0);
                        }
                        
                        // forward projection from longitude/latitude graticule
                        // to projected source image
                        srcProj.project(lonlat.x, lonlat.y, srcXY);
                        srcXY.x *= earthRadius;
                        srcXY.y *= earthRadius;
                       
                        final int color;
                        if (nearestNeighbor) {
                            color = image.getNearestNeighbor(srcXY.x, srcXY.y);
                        } else {
                            color = image.getBicubicInterpol(srcXY.x, srcXY.y);
                        }

                        writer.write(color);
                    }
                }
                
                // write a world file
                WorldFileExporter.writeWorldFile(worldFilePath, projCellSize, 
                        projWest, projNorth);
        
            } catch (Exception e) {
                
                // delete the new files
                new File(exportFilePath).delete();
                new File(worldFilePath).delete();
                
                e.printStackTrace();
                
                // this will be executed in the event dispatching thread.
                ika.utils.ErrorDialog.showErrorDialog("The image could not be projected.", e);
                throw e;
            } finally {
                if (out != null)
                    try { out.close(); } catch (Exception exc) {}
            }
            return null;
        }
        
        @Override
        public void done() {
            this.completeProgress();
        }
    }
}
