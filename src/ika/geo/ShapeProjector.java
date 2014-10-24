/*
 * ShapeProjector.java
 *
 * Created on May 27, 2007, 5:43 PM
 *
 */
package ika.geo;

import ika.geoimport.ShapeImporter;
import ika.gui.GeoExportGUI;
import ika.gui.PageFormat;
import ika.gui.SwingWorkerWithProgressIndicator;
import com.jhlabs.map.proj.Projection;
import ika.table.TableLink;
import ika.utils.FileUtils;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;

/**
 * Projects an ESRI Shape file from geographic coordinates. Reads the shape file
 * and stores the result in a new shape file.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ShapeProjector {

    /** The grid will be projected using this Projection. */
    private Projection projection;
    /** The file to read the input grid. */
    private String importFilePath;
    /** The file that will receive the projected grid. */
    private String exportFilePath;

    /**
     * Creates a new instance of ShapeProjector
     */
    public ShapeProjector(JFrame ownerFrame, Projection projection,
            String importFilePath, String exportFilePath) {

        if (projection == null || importFilePath == null) {
            throw new IllegalArgumentException();
        }

        String fileName = FileUtils.getFileName(importFilePath);
        String progressTitle = "Projecting " + fileName;

        ShapeProjectorTask shapeProjectorTask = new ShapeProjectorTask(ownerFrame,
                progressTitle, "<html><small></small></html>", false);
        shapeProjectorTask.setIndeterminate(true);

        this.projection = projection;
        this.importFilePath = importFilePath;
        this.exportFilePath = exportFilePath;

        shapeProjectorTask.execute();
    }

    class ShapeProjectorTask extends SwingWorkerWithProgressIndicator<GeoSet> {

        private String errMessage = "An error occured while projecting a Shape file";

        public ShapeProjectorTask(Frame owner,
                String dialogTitle,
                String message,
                boolean blockOwner) {
            super(owner, dialogTitle, message, blockOwner);
        }

        protected GeoSet doInBackground() throws Exception {
            try {

                start();

                // read the input file
                setMessage("<html><small>Reading Shape File</small></html>");
                setTotalTasksCount(2);
                ShapeImporter importer = new ShapeImporter();
                importer.setProgressIndicator(this);

                GeoSet geoSet = (GeoSet) importer.read(importFilePath);
                TableLink tableLink = importer.getTableLink();
                if (geoSet == null || tableLink == null) {
                    return null;
                }

                setMessage("<html><small>Projecting</small></html>");
                nextTask();

                // project
                GeoProjector projector = new GeoProjector(projection, this);
                projector.project(geoSet);
                if (this.isAborted()) {
                    return null;
                } else {
                    return geoSet;
                }

            } catch (Exception e) {
                e.printStackTrace();

                // this will be executed in the event dispatching thread.
                ika.utils.ErrorDialog.showErrorDialog("The file could not be projected.", e);
                throw e;
            } finally {
                completeProgress();
            }
        }

        @Override
        public void done() {

            try {
                GeoSet geoSet = get();
                if (geoSet == null) {
                    return;
                }
                completeProgress();
                
                String name = geoSet.getName();
                if (name == null) {
                    name = "projected";
                } else {
                    name += " " + projection.getName();
                }

                PageFormat pageFormat = new PageFormat();
                pageFormat.setAutomatic(true);
                pageFormat.setUnitPixels(false);
                pageFormat.setPageScale(100000000); // 100 Mio.
                pageFormat.setVisible(false);
                Rectangle2D box = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
                pageFormat.setPageWorldCoordinates(box);
                GeoExportGUI.export(null, geoSet, name, null, pageFormat, true,
                        null, null, null, null, null, this);

            } catch (InterruptedException e) {
                //
            } catch (Exception e) {
                ika.utils.ErrorDialog.showErrorDialog("The file could not be projected.", e);
            } finally {
                completeProgress();
            }
        }
    }
}