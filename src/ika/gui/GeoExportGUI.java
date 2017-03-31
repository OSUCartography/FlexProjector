/*
 * GeoExportGUI.java
 *
 * Created on March 22, 2007, 12:10 PM
 *
 */
package ika.gui;

import ika.geo.GeoMap;
import ika.geo.GeoObject;
import ika.geo.GeoSet;
import ika.geoexport.*;
import ika.table.TableLink;
import ika.table.TableLinkExporter;
import ika.utils.ErrorDialog;
import ika.utils.FileUtils;
import java.awt.Component;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Utility class that handles the GUI for a GeoSetExporter.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoExportGUI {

    private GeoExportGUI() {
    }

    /**
     * Exports map features to a file. The user is asked to select a file path to a
     * new file. This is designed for vector data.
     * 
     * @param exporter The GeoSetExporter to export the exporterMap. If null, the
     * user is asked to select a GeoSetExporter from a list.
     * @param geoSet The GeoSet to export.
     * @param fileName A default file name without extension. If null, the name 
     * of the GeoSet is used.
     * @param frame The Frame for which the dialog for selecting a file
     * is displayed.
     * @param pageFormat The page format for exporting to vector graphics formats
     * (not georeferenced formats). If null and the exporter is a 
     * VectorGraphicsExporter, the page format of the exporter is used. If this
     * is also null, a default page format is used that includes the whole GeoSet
     * to export.
     * @param askScale If true and if exporting to a vector graphics format (not
     * a georeferenced GIS format), the user is asked for a scale that is 
     * applied to the data prior to export.
     */
    public static void export(GeoSetExporter exporter,
            GeoSet geoSet,
            String fileName,
            Frame frame,
            PageFormat pageFormat,
            boolean askScale) {

        GeoExportGUI.export(exporter, geoSet, fileName, frame, pageFormat,
                askScale, null, null, null, null, null, null);

    }

    /**
     * Exports map features to a file. The user is asked to select a file path to a
     * new file. This is designed for vector data.
     * 
     * @param exporter The GeoSetExporter to export the exporterMap. If null, the
     * user is asked to select a GeoSetExporter from a list.
     * @param geoSet The GeoSet to export.
     * @param fileName A default file name without extension. If null, the name 
     * of the GeoSet is used.
     * @param frame The Frame for which the dialog for selecting a file
     * is displayed.
     * @param pageFormat The page format for exporting to vector graphics formats
     * (not georeferenced formats). If null and the exporter is a 
     * VectorGraphicsExporter, the page format of the exporter is used. If this
     * is also null, a default page format is used that includes the whole GeoSet
     * to export.
     * @param askScale If true and if exporting to a vector graphics format (not
     * a georeferenced GIS format), the user is asked for a scale that is 
     * applied to the data prior to export.
     */
    public static void export(GeoSetExporter exporter,
            GeoSet geoSet,
            String fileName,
            Frame frame,
            PageFormat pageFormat,
            boolean askScale,
            String applicationName,
            String documentName,
            String documentAuthor,
            String documentSubject,
            String documentKeyWords,
            ProgressIndicator progressIndicator) {

        try {
            if (exporter == null) {
                exporter = GeoExportGUI.askExporter(frame);
            }
            if (exporter == null) {
                return; // user cancelled
            }

            // construct a message for the file selection dialog
            exporter.setApplicationName(applicationName);
            exporter.setDocumentName(documentName);
            exporter.setDocumentAuthor(documentAuthor);
            exporter.setDocumentSubject(documentSubject);
            exporter.setDocumentKeyWords(documentKeyWords);

            String msg = "Save " + exporter.getFileFormatName() + " File";

            // construct a file name
            if (fileName == null) {
                fileName = geoSet.getName();
            }
            String ext = exporter.getFileExtension();
            fileName = FileUtils.forceFileNameExtension(fileName, ext);

            // ask the user for a file.
            String filePath = FileUtils.askFile(frame, msg, fileName, false, ext);
            if (filePath == null) {
                return; // user canceled
            }

            // ask the user for a scale for graphics file formats if the exporter
            // does not have a valid page format. Don't do this for georeferenced
            // GIS export formats.
            if (exporter instanceof VectorGraphicsExporter) {
                VectorGraphicsExporter gExporter = (VectorGraphicsExporter) exporter;
                if (pageFormat == null) {
                    pageFormat = gExporter.getPageFormat();
                }
                if (pageFormat == null) {
                    pageFormat = new PageFormat();
                    pageFormat.setAutomatic(true);
                    Rectangle2D box = geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE);
                    pageFormat.setPageWorldCoordinates(box);
                }
                if (askScale) {
                    if (!GeoExportGUI.askScale(exporter, pageFormat, geoSet, frame)) {
                        return;
                    }
                } else {
                    gExporter.setPageFormat(pageFormat);
                }
            } else if (exporter instanceof RasterImageExporter) {
                String rasterSizeMsg = "Please enter the width of the image in pixels:";
                String rasterTitle = "Image Width";
                String widthStr = (String) JOptionPane.showInputDialog(frame, rasterSizeMsg, rasterTitle, JOptionPane.QUESTION_MESSAGE, null, null, new Integer(1000));
                if (widthStr == null) {
                    return; // user canceled
                }
                try {
                    int width = (int) Double.parseDouble(widthStr);
                    ((RasterImageExporter) exporter).setImageWidth(width);
                } catch (NumberFormatException exc) {
                    ErrorDialog.showErrorDialog("Please enter a valid number for the image width.");
                    return;
                }
            }

            if (progressIndicator == null) {
                GeoExportGUI.export(exporter, geoSet, filePath, null);
            } else {
                new GeoExportTask(exporter, geoSet, filePath, progressIndicator).execute();
            }

        } catch (Exception e) {
            // show an error message.
            String msg = "The data could not be exported.";
            ika.utils.ErrorDialog.showErrorDialog(msg, "Export Error", e, frame);
            e.printStackTrace();
        }

    }

    private static void export(GeoSetExporter exporter,
            GeoSet geoSet,
            String filePath,
            ProgressIndicator progressIndicator) throws IOException {

        // export the GeoSet to the file
        if (progressIndicator != null) {
            exporter.setProgressIndicator(progressIndicator);
            progressIndicator.start();
        }
        exporter.export(geoSet, filePath);

        // FIXME this is an ugly hack !!! ???
        if (exporter instanceof TableLinkExporter) {

            TableLinkExporter tableLinkExporter = (TableLinkExporter) exporter;
            TableLink tableLink = null;

            if (geoSet instanceof GeoMap && ((GeoMap) geoSet).tableLinkGetNumber() > 0) {
                tableLink = ((GeoMap) geoSet).tableLinkGet(0); // !!! ???
            } else {
                ika.table.Table table = new ika.table.Table("US-ASCII");
                table.setName("table");
                table.addColumn("ID");
                final int rowCount = tableLinkExporter.getFeatureCount();
                for (int i = 0; i < rowCount; i++) {
                    table.addRow(new Object[]{new Double(i)});
                }
                tableLink = new TableLink(table, geoSet);
            }
            tableLinkExporter.exportTableForGeometry(filePath, tableLink);
        }

    }

    /**
     * Ask the user to select a scale for graphics file formats (not 
     * georeferenced GIS formats).
     * @param exporter The export that will receive the selected scale.
     * @param pageFormat A page format with the size, position and scale of the map.
     * @param geoSet The data to export.
     * @param frame The parent frame.
     * @return False if the user cancels or if the passed exporter is not an 
     * instance of a VectorGraphicsExporter; true otherwise.
     */
    public static boolean askScale(GeoSetExporter exporter, PageFormat pageFormat,
            GeoSet geoSet, Frame frame) {

        // only for graphics file formats
        if (exporter instanceof VectorGraphicsExporter == false) {
            return false;
        }
        SimplePageFormatDialog dlg = new SimplePageFormatDialog(frame, true, pageFormat);
        //dlg.setMapObjectsBoundingBox(geoSet.getBounds2D(GeoObject.UNDEFINED_SCALE));
        pageFormat = dlg.showDialog();
        if (pageFormat == null) {
            return false; // user cancelled
        }
        VectorGraphicsExporter vectorExporter = (VectorGraphicsExporter) exporter;
        vectorExporter.setPageFormat(pageFormat);
        return true;

    }
    private static final HashMap<String, Class> exporterMap;
    private static final String[] exporterNames = {"SVG", "Illustrator", "PDF", "Shape", "DXF", "Ungenerate", "PNG", "JPEG"};

    static {
        exporterMap = new HashMap<String, Class>();
        for (String exporterClassName : exporterNames) {
            try {
                Class exporterClass = Class.forName("ika.geoexport." + exporterClassName + "Exporter");
                GeoExporter exporter = (GeoExporter) exporterClass.newInstance();
                String exporterName = exporter.getFileFormatName();
                exporterMap.put(exporterName, exporterClass);
            } catch (Exception ex) {
                Logger.getLogger(GeoExportGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }

    /**
     * Ask the user to select an export file format.
     * @return A GeoSetExporter selected by the user, or null if the user cancels.
     */
    public static GeoSetExporter askExporter(Component parent) {
        
        String[] orderedExporterNames = exporterMap.keySet().toArray(new String[]{});
        Arrays.sort(orderedExporterNames);
        String exporterName = (String) JOptionPane.showInputDialog(parent,
                "Export File Format",
                "",
                JOptionPane.QUESTION_MESSAGE, null,
                orderedExporterNames, null);
        if (exporterName == null) {
            return null; // user canceled
        }
        Class cls = (Class) exporterMap.get(exporterName);
        if (cls != null) {
            try {
                return (GeoSetExporter) cls.newInstance();
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            }
        }
        return null;
    }


    private static class GeoExportTask extends SwingWorker<GeoSet, Void>
            implements PropertyChangeListener {

        private ProgressIndicator progressIndicator;
        private GeoSetExporter exporter;
        private GeoSet geoSet;
        private String filePath;

        protected GeoExportTask(GeoSetExporter exporter,
                GeoSet geoSet,
                String filePath,
                ProgressIndicator progressIndicator) {
            this.progressIndicator = progressIndicator;
            this.exporter = exporter;
            this.geoSet = geoSet;
            this.filePath = filePath;
            this.addPropertyChangeListener(this);
        }

        /**
         * Invoked when task's progress property changes. Update the value displayed
         * by the progress monitor dialog and inform the task if the user presses
         * the cancel button. This is called in the event dispatching thread.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = ((Integer) evt.getNewValue()).intValue();
                System.out.println(progress);
                this.progressIndicator.progress(progress);
                if (this.progressIndicator.isAborted()) {
                    this.cancel(true);
                }
            }
        }

        protected GeoSet doInBackground() throws Exception {
            try {

                progressIndicator.start();
                this.setProgress(0);
                GeoExportGUI.export(exporter, geoSet, filePath, progressIndicator);

                return null;

            } catch (Exception e) {

                e.printStackTrace();

                // this will be executed in the event dispatching thread.
                ika.utils.ErrorDialog.showErrorDialog("The file could not be exported.", e);
                throw e;
            } finally {
                progressIndicator.completeProgress();
            }
        }
    }
}
