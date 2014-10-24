/*
 * PDFExporter.java
 *
 * Created on February 22, 2006, 9:38 PM
 *
 */
package ika.geoexport;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import ika.geo.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jenny
 */
public class PDFExporter extends VectorGraphicsExporter {

    /* to keep track of current drawing settings. */
    private Color currentFillColor;
    private Color currentStrokeColor;
    private float currentStrokeWidth;

    /** Creates a new instance of PDFExporter */
    public PDFExporter() {
    }

    @Override
    public String getFileFormatName() {
        return "PDF";
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }

    @Override
    protected void write(GeoSet geoSet, OutputStream outputStream) throws IOException {

        Document document = null;

        try {

            currentFillColor = null;
            currentStrokeColor = null;
            currentStrokeWidth = 0;

            // creation of a document-object
            document = new Document();

            // create a writer that listens to the document
            // and directs a PDF-stream to a file
            // this must be done before properties of the document are set!
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.setMargins(0, 0, 0, 0);

            if (applicationName != null) {
                document.addCreator(applicationName);
            }
            if (documentName != null) {
                document.addTitle(documentName);
            }
            if (documentAuthor != null) {
                document.addAuthor(documentAuthor);
            }
            if (documentSubject != null) {
                document.addSubject(documentSubject);
            }
            if (documentKeyWords != null) {
                document.addKeywords(documentKeyWords);
            }

            final double wWC = pageFormat.getPageWidthWorldCoordinates();
            final double hWC = pageFormat.getPageHeightWorldCoordinates();
            final float w = (float)dimToPagePx(wWC);
            final float h = (float)dimToPagePx(hWC);
            com.lowagie.text.Rectangle pdfBounds = new com.lowagie.text.Rectangle(w, h);
            document.setPageSize(pdfBounds);

            // open the document
            document.open();

            // grab the ContentByte
            PdfContentByte cb = writer.getDirectContent();

            // write the GeoObjects
            writeGeoSet(geoSet, cb);

        } catch (DocumentException de) {
            System.err.println(de.getMessage());
            throw new IOException(de.getMessage());
        } finally {
            // close the document
            if (document != null) {
                document.close();
            }
        }
    }

    /** Write a GeoSet and all its children.
     */
    private void writeGeoSet(GeoSet geoSet, PdfContentByte cb) {

        final int nbrObj = geoSet.getNumberOfChildren();
        for (int i = 0; i < nbrObj; i++) {
            GeoObject obj = geoSet.getGeoObject(i);

            // only write visible elements
            if (obj.isVisible() == false) {
                continue;
            }

            if (obj instanceof GeoSet) {
                writeGeoSet((GeoSet) obj, cb);
            } else if (obj instanceof GeoPath) {
                writeGeoPath((GeoPath) obj, cb);
            } else if (obj instanceof GeoPoint) {
                writeGeoPoint((GeoPoint) obj, cb);
            } else if (obj instanceof GeoText) {
                writeGeoText((GeoText) obj, cb);
            } else if (obj instanceof GeoImage) {
                writeGeoImage((GeoImage) obj, cb);
            }
        }
    }

    /**
     * Write a GeoImage
     */
    private void writeGeoImage(GeoImage geoImage, PdfContentByte cb) {
        /*
        // first draw rectangle with the size of the image
        Rectangle2D bounds = geoImage.getBounds2D();
        GeoPath rect = new GeoPath();
        rect.rectangle(bounds);
        writeGeoPath(rect, writer);
        
        // make sure the GeoImage has a path to a file
        String path = geoImage.getFilePath();
        if (path == null)
        return;
        
        // replace '/' by ':' on Mac
        if (com.muchsoft.util.Sys.isMacOSX()) {
        String pathSeparator = System.getProperty("file.separator");
        path = ika.utils.StringUtils.replace(path, pathSeparator, ":");
        }
        
        double w = transformDim_round(bounds.getWidth());
        double h = transformDim_round(bounds.getHeight());
        
        writer.println("%AI5_File:");
        writer.println("%AI5_BeginRaster");
        
        writer.print(stringToPostScript(path));
        writer.println(" 0 XG");
        
        // rotation and scale
        writer.print("[1 0 0 1 ");
        
        // position
        writer.print(transformX_round(bounds.getMinX()));
        writer.print(" ");
        writer.print(transformY_round(bounds.getMaxY()));
        writer.print("]");
        
        // Bounds (lower left and upper right x,y coordinates).
        writer.print(" 0 0 ");
        writer.print(w);
        writer.print(" ");
        writer.print(h);
        
        // Size (height and width).
        writer.print(" ");
        writer.print(w);
        writer.print(" ");
        writer.print(h);
        
        // number of bits
        writer.print(" 8");
        
        // image type
        writer.print(" 3"); // RGB
        
        // alpha channel count
        writer.print(" 0");
        
        // reserved
        writer.print(" 0");
        
        // binary or ascii
        writer.print(" 0"); // binary
        
        // image mask
        writer.println(" 0 XF"); // opaque
        
        writer.println("%AI5_EndRaster");
        writer.println("F");
         */
    }

    /**
     * Write a GeoText
     */
    private void writeGeoText(GeoText geoText, PdfContentByte cb) {
        /*
        FontSymbol symbol = geoText.getFontSymbol();
        
        writePaintingAttributes(Color.black, Color.black,
        currentStrokeWidth, writer);
        
        // 0 To   text object of type point text start
        writer.println("0 To");
        
        // text at a point start Tp
        writer.print("1 0 0 1 ");   // rotation and scale
        final double x = geoText.getVisualX(1./getDisplayMapScale());
        final double y = geoText.getVisualY(1./getDisplayMapScale());
        writeCoordinate(x, y, writer);
        writer.println("0 Tp");
        writer.println("TP");
        
        // text style
        // 0 Tr   filled text render mode
        writer.println("0 Tr");
        
        // use non-zero winding rule
        writer.println("0 XR");
        
        // /_fontname size ascent descent Tf  font and size
        writer.print("/_");
        writer.print(symbol.getFont().getPSName());
        writer.print(" ");
        writer.print(symbol.getSize());
        writer.println(" Tf");
        
        // no raise or fall: write on baseline
        writer.println("0 Ts");
        
        // no horizontal text scaling
        writer.println("100 Tz");
        
        // no tracking between characters
        writer.println("0 Tt");
        
        // use auto kerning
        writer.println("1 TA");
        
        // hyphenation language: US English
        writer.println("%_ 0 XL");
        
        // setup tabs
        writer.println("36 0 Xb");
        // end of tab definition
        writer.println("XB");
        
        // intents for paragraphs
        writer.println("0 0 0 Ti");
        
        // text alignment
        // 1 left aligned, 1 center aligned, 2 right aligned,
        // 3 justified (right and left)
        if (symbol.isCenterHor())
        writer.println("1 Ta");
        else
        writer.println("0 Ta");
        
        // leading = space between lines and paragraphs
        writer.println("0 0 Tl");
        
        // actual text
        String str = geoText.getText();
        str = stringToPostScript(str);
        writer.print(str);
        writer.println(" TX");
        
        // TO text object of type point text end
        writer.println("TO");
        
         **/
    }

    /**
     * Write a GeoPoint.
     */
    private void writeGeoPoint(GeoPoint geoPoint, PdfContentByte cb) {
        PointSymbol pointSymbol = geoPoint.getPointSymbol();
        GeoPath geoPath = pointSymbol.getPointSymbol(getDisplayMapScale(),
                geoPoint.getX(), geoPoint.getY());
        GeoPathIterator pi = geoPath.getIterator();
        writePathIterator(pi, pointSymbol, cb);
    }

    /** Write a GeoPath.
     */
    private void writeGeoPath(GeoPath geoPath, PdfContentByte cb) {
        GeoPathIterator pi = geoPath.getIterator();
        writePathIterator(pi, geoPath.getVectorSymbol(), cb);
    }

    /** Write a path describing a graphic objects of straight lines,
     * Bezier curves, potentially with holes and islands.
     * @param iterator The path iterator to write.
     * @param vectorSymbol The VectorSymbol specifying the appearance of pi.
     */
    private void writePathIterator(GeoPathIterator iterator,
            VectorSymbol vectorSymbol, PdfContentByte cb) {

        // write colors and stroke width if necessary.
        writePaintingAttributes(vectorSymbol, cb);

        // write the path
        // remember the last written segment type and the point position.
        int lastSegmentType = GeoPathModel.NONE;
        do {
            final int type = iterator.getInstruction();
            switch (type) {
                case GeoPathModel.CLOSE:
                    cb.closePath();
                    writeFillStroke(vectorSymbol, true, cb); // !!! ??? needed?
                    break;

                case GeoPathModel.MOVETO:
                    writeFillStroke(vectorSymbol, false, cb);// !!! ??? needed?
                    // start defintion of new path
                    cb.moveTo((float) xToPagePx(iterator.getX()),
                            (float) yToPagePx(iterator.getY()));
                    break;

                case GeoPathModel.LINETO:
                    cb.lineTo((float) xToPagePx(iterator.getX()),
                            (float) yToPagePx(iterator.getY()));
                    break;

                case GeoPathModel.QUADCURVETO:
                    cb.curveTo((float) xToPagePx(iterator.getX()),
                            (float) yToPagePx(iterator.getY()),
                            (float) xToPagePx(iterator.getX2()),
                            (float) yToPagePx(iterator.getY2()));
                    break;

                case GeoPathModel.CURVETO:
                    cb.curveTo((float) xToPagePx(iterator.getX()),
                            (float) yToPagePx(iterator.getY()),
                            (float) xToPagePx(iterator.getX2()),
                            (float) yToPagePx(iterator.getY2()),
                            (float) xToPagePx(iterator.getX3()),
                            (float) yToPagePx(iterator.getY3()));
                    break;
            }
            lastSegmentType = type;
        } while (iterator.next());

        if (lastSegmentType != PathIterator.SEG_CLOSE
                && lastSegmentType != PathIterator.SEG_MOVETO) {
            writeFillStroke(vectorSymbol, false, cb);
        }
    }

    /** Writes commands to stroke and / or fill the last geometry.
     */
    private void writeFillStroke(VectorSymbol symbol, boolean close, PdfContentByte cb) {
        if (symbol == null) {
            return;
        }

        final boolean fill = symbol.isFilled();
        final boolean stroke = symbol.isStroked();

        if (fill && stroke) {
            cb.fillStroke();
        } else if (fill) {
            cb.fill();
        } else if (stroke) {
            cb.stroke();
        } else // nothing: invisible element
            ;
    }

    /**
     * Writes a new graphic state (fill and stroke colors, stroke width). Does
     * not write information that has not changed since the last call of this
     * method.
     */
    private void writePaintingAttributes(VectorSymbol symbol, PdfContentByte cb) {
        if (symbol == null) {
            return;
        }
        Color newFillColor = symbol.getFillColor();
        Color newStrokeColor = symbol.getStrokeColor();
        float newStrokeWidth = symbol.getStrokeWidth();
        writePaintingAttributes(newFillColor, newStrokeColor, newStrokeWidth, cb);
    }

    /**
     * Writes a new graphic state (fill and stroke colors, stroke width). Does
     * not write information that has not changed since the last call of this
     * method.
     */
    private void writePaintingAttributes(Color newFillColor,
            Color newStrokeColor, float newStrokeWidth, PdfContentByte cb) {
        // make sure currentColors have been initialized. This is not the case
        // for the first object.
        if (currentFillColor == null) {
            writeFillColor(newFillColor, cb);
            currentFillColor = newFillColor;
            writeStrokeColor(newStrokeColor, cb);
            currentStrokeColor = newStrokeColor;
            cb.setLineWidth(newStrokeWidth);
            currentStrokeWidth = newStrokeWidth;
            return;
        }

        if (newFillColor != null && !newFillColor.equals(currentFillColor)) {
            writeFillColor(newFillColor, cb);
            currentFillColor = newFillColor;
        }
        if (newStrokeColor != null && !newStrokeColor.equals(currentStrokeColor)) {
            writeStrokeColor(newStrokeColor, cb);
            currentStrokeColor = newStrokeColor;
        }
        if (newStrokeWidth >= 0.f && newStrokeWidth != currentStrokeWidth) {
            cb.setLineWidth(newStrokeWidth);
            currentStrokeWidth = newStrokeWidth;
        }
    }

    /** Write fill color in RGB.
     */
    private void writeFillColor(Color color, PdfContentByte cb) {
        cb.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Write stroke color in RGB.
     */
    private void writeStrokeColor(Color color, PdfContentByte cb) {
        cb.setRGBColorStroke(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Transforms y coordinate to sheet coordinates.
     * Overwrites method because y axis is upwards oriented in PDF.
     */
    @Override
    protected double yToPage(double y) {
        final double mapScale = pageFormat.getPageScale();
        final double south = pageFormat.getPageBottom();
        return (y - south) / mapScale;
    }
}
