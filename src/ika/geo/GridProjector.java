/*
 * GridProjector.java
 *
 * Created on May 27, 2007, 5:43 PM
 *
 */

package ika.geo;

import com.jhlabs.map.MapMath;
import ika.geoexport.ESRIASCIIGridWriter;
import ika.geoimport.EsriASCIIGridReader;
import ika.gui.FlexProjectorPreferencesPanel;
import ika.gui.SwingWorkerWithProgressIndicator;
import com.jhlabs.map.proj.Projection;
import ika.utils.FileUtils;
import java.awt.Frame;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.prefs.Preferences;
import javax.swing.JFrame;

/**
 * Projects a grid from geographic coordinates. Reads the grid from a file and
 * stores the result in a file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class GridProjector extends RasterProjector {
    
    /** The grid will be projected using this Projection. */
    private Projection projection;
    
    /** The file to read the input grid. */
    private String importFilePath;
    
    /** The file that will receive the projected grid. */
    private String exportFilePath;
    
    /**
     * Creates a new instance of GridProjector
     */
    public GridProjector(JFrame ownerFrame, Projection projection,
            String importFilePath, String exportFilePath) {
        
        if (projection == null
                || importFilePath == null
                || exportFilePath == null)
            throw new IllegalArgumentException();
        
        this.projection = projection;
        this.importFilePath = importFilePath;
        this.exportFilePath = exportFilePath;
        
        String fileName = FileUtils.getFileName(importFilePath);
        String title = "Projecting " + fileName;
        String msg = "<html><small>Reading Grid<br></small></html>";
        GridProjectorTask gridProjectorTask = new GridProjectorTask(ownerFrame, title, msg, false);
        gridProjectorTask.setIndeterminate(true);
        gridProjectorTask.execute();
    }

    class GridProjectorTask extends SwingWorkerWithProgressIndicator <Object> {

        public GridProjectorTask(Frame owner,
                String dialogTitle,
                String message,
                boolean blockOwner) {
            super(owner, dialogTitle, message, blockOwner);
        }

        
        protected Object doInBackground() throws Exception {
            PrintWriter printWriter = null;
            
            try {
                
                // preferences store the interpolation mode: bilinear or 
                // neareast neighbor
                final int interpolationMethod = Preferences.userRoot().getInt(
                        FlexProjectorPreferencesPanel.INTERPOLATION_PREFS,
                        FlexProjectorPreferencesPanel.INTERPOLATION_BICUBIC);
                
                this.start();
                this.setTotalTasksCount(2);
                
                // Create the file already now to show the user where the 
                // projected grid will be stored.
                printWriter = new PrintWriter(new BufferedWriter(
                        new FileWriter(exportFilePath)));
                
                // read the input grid file
                GeoGrid grid = EsriASCIIGridReader.read(importFilePath, this);
                if (grid == null) {
                    new File(exportFilePath).delete();
                    if (this.isAborted()) {
                        return null;
                    } else {
                        throw new IOException("Could not read grid file at " + importFilePath);
                    }
                }

                // update the progress dialog
                this.nextTask();
                String msg = "<html><small>Projecting with "
                        + (interpolationMethod ==
                        FlexProjectorPreferencesPanel.INTERPOLATION_BICUBIC 
                        ? "bicubic" : "nearest neighbor") 
                        + " interpolation."
                        + "<br>Saving to " 
                        + FileUtils.getFileName(exportFilePath)
                        + "</small></html>";
                this.setMessage(msg);
                
                // find the extension of the projected grid by projecting the border
                // of the unprojected grid. This assumes that the projection does not
                // fold or otherwise distort space in an unusual way.
                Rectangle2D.Double projBounds = findProjectedExtension(projection, grid);
                final double projWidth = projBounds.getWidth();
                final double projHeight = projBounds.getHeight();
                final double projWest = projBounds.getMinX();
                final double projNorth = projBounds.getMaxY();
                
                // compute the cell size and size of the new grid
                final int gridCols = grid.getCols();
                final int gridRows = grid.getRows();
                final double projCellSize = Math.min(projWidth / gridCols,
                        projHeight / gridRows);
                final int projCols 
                        = (int)Math.ceil(projWidth / projCellSize);
                final int projRows 
                        = (int)Math.ceil(projHeight / projCellSize);
                
                // Use an ESRIASCIIGridWriter instead of writing to a second
                // GeoGrid. ESRIASCIIGridWriter directly writes to a stream and does
                // not cache the grid. This divides the amunt of required memory by 2.
                float minMax[] = grid.getMinMax();
                final float noDataValue = (float)Math.floor(minMax[0] * 2);
                ESRIASCIIGridWriter gridWriter = new ESRIASCIIGridWriter(
                        printWriter, projCols, projRows,
                        projWest, projNorth - projHeight, projCellSize, noDataValue);
                
                final double earthRadius = projection.getEquatorRadius();
                final double lon0 = projection.getProjectionLongitude();
                Point2D.Double pt = new Point2D.Double();
                for (int r = 0; r < projRows; r++) {
                    
                    if (!this.progress((int)((double)r / projRows * 100))) {
                        // delete the new file
                        new File(exportFilePath).delete();
                        break;
                    }
                    
                    final double y = (projNorth - r * projCellSize) / earthRadius;
                    for (int c = 0; c < projCols; c++) {
                        final double x = (projWest + c * projCellSize) / earthRadius;
                        
                        // don't use inverseTransformRadians here. The lon/lat values
                        // have to be checked after the inverse projection to make
                        // sure they fall in [-PI..+PI] for the longitude, and
                        // [-PI/2..+PI/2] for the latitude.
                        projection.projectInverse(x, y, pt);
                        if (Double.isNaN(pt.x) || Double.isNaN(pt.y)
                                || pt.x < -Math.PI || pt.x > Math.PI
                                || pt.y < -Math.PI / 2 || pt.y > Math.PI / 2) {
                            gridWriter.writeNoData();
                            continue;
                        }
                        
                        if (lon0 != 0)
                            pt.x = MapMath.normalizeLongitude(pt.x+lon0);
                        
                        pt.x = Math.toDegrees(pt.x);
                        pt.y = Math.toDegrees(pt.y);
                        
                        final float v;
                        if (interpolationMethod ==
                                FlexProjectorPreferencesPanel.INTERPOLATION_BICUBIC)
                            v = grid.getBicubicInterpol(pt.x, pt.y);
                        else
                            v = grid.getNearestNeighbor(pt.x, pt.y);
                        gridWriter.write(v);
                    }
                    gridWriter.newLine();
                }
                
            } catch (Exception e) {
                // delete the new file
                new File(exportFilePath).delete();
                
                e.printStackTrace();
                
                // this will be executed in the event dispatching thread.
                ika.utils.ErrorDialog.showErrorDialog("The grid could not be projected.", e);
                throw e;
            } finally {
                if (printWriter != null)
                    try {printWriter.close(); } catch (Exception exc) {}
            }
            return null;
        }
        
        @Override
        public void done() {
            this.completeProgress();
        }
    }
}
