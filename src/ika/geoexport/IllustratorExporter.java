/*
 * IllustratorExporter.java
 *
 * Created on February 16, 2006, 3:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ika.geoexport;

import ika.geo.*;
import ika.gui.PageFormat;
import java.io.*;
import java.awt.geom.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * An exporter for the Adobe Illustrator version 7 file format.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class IllustratorExporter extends VectorGraphicsExporter {

    /* to keep track of current drawing settings. */
    private Color currentFillColor;
    private Color currentStrokeColor;
    private float currentStrokeWidth;
    private DecimalFormat coordinateFormat;
    /**
     * Count the number of layers created.
     */
    private int layerCounter = 0;

    /**
     * Creates a new instance of IllustratorExporter.
     */
    public IllustratorExporter() {
        coordinateFormat = new DecimalFormat();
        coordinateFormat.setGroupingUsed(false);
        coordinateFormat.getDecimalFormatSymbols().setDecimalSeparator('.');
        coordinateFormat.setDecimalSeparatorAlwaysShown(false);
    }

    public String getFileFormatName() {
        return "Illustrator";
    }

    public String getFileExtension() {
        return "ai";
    }

    protected void write(GeoSet geoSet, OutputStream outputStream) throws IOException {

        PrintWriter writer = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(outputStream)));

        this.currentFillColor = null;
        this.currentStrokeColor = null;
        this.currentStrokeWidth = 0;

// HEADER
        // start the header
        writer.println("%!PS-Adobe-3.0");
        // header comments
        // creator / version
        writer.println("%%Creator: Adobe Illustrator(TM) 3.2");
        // title
        if (geoSet.getName() != null) {
            writer.println("%%Title: " + stringToPostScript(geoSet.getName().trim()));
        }

        // creation date and time %%CreationDate: (5/9/96) (3:57 PM)
        writer.print("%%CreationDate: ");
        Date date = new Date();
        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat("'('M/d/yy') ('HH:mm a')'");
        writer.println(format.format(date));

        // bounding box
        // If a high-resolution bounding box is specified, the limits
        // of the bounding box are derived from the high-resolution bounding 
        // box. To generate the lower left and upper right limits of the low 
        // resolution bounding box, the high-resolution bounding box llx and 
        // lly values are rounded down, and urx and ury values are rounded up.
        final double wWC = this.pageFormat.getPageWidthWorldCoordinates();
        final double hWC = this.pageFormat.getPageHeightWorldCoordinates();
        final double w = dimToPageRoundedPx(wWC);
        final double h = dimToPageRoundedPx(hWC);

        // %%BoundingBox: llx lly urx ury
        writer.print("%%BoundingBox: ");
        writer.println(0 + " " + 0 + " " + (long) Math.ceil(w) + " " + (long) Math.ceil(h));

        // high resolution bounding box %%HiResBoundingBox: llx lly urx ury
        writer.print("%%HiResBoundingBox: ");
        writer.println(0 + " " + 0 + " " + w + " " + h);

        // document preview
        writer.println("%AI3_DocumentPreview: None");
        // end header
        writer.println("%%EndComments");

// PROLOG
        writer.println("%%BeginProlog");
        writer.println("%%EndProlog");

// SCRIPT SETUP
        writer.println("%%BeginSetup");
        writer.println("%%EndSetup");

// SCRIPT BODY
        // write geometry
        this.writeTopLevelGeoSet(geoSet, writer);

// PAGE Trailer
        writer.println("%%PageTrailer");
        writer.println("gsave annotatepage grestore showpage");

// SCRIPT TRAILER
        writer.println("%%Trailer");

// EOF
        writer.println("%%EOF");

        writer.flush();
    }

    /**
     * Writes a GeoSet to a PrintWriter. Wraps all features in layers. 
     * Layers are supported since the file format version 5. Only top-level 
     * layers are supported.
     * geoSet The GeoSet to write.
     * @writer The destination to write to.
     */
    private void writeTopLevelGeoSet(GeoSet geoSet, PrintWriter writer) {

        // travel down the tree until we find a GeoSet that contains more
        // than just another GeoSet.
        while (geoSet.getNumberOfChildren() == 1
                && geoSet.getNumberOfSubSets() == 1) {
            geoSet = (GeoSet) geoSet.getGeoObject(0);
        }

        // flag that remembers whether we are currently writing to a layer 
        // that was created for top-level objects that are not GeoSets.
        boolean usingNonGeoSetLayer = false;

        final int objCount = geoSet.getNumberOfChildren();
        for (int i = 0; i < objCount; i++) {
            GeoObject obj = geoSet.getGeoObject(i);

            // only write visible elements
            if (obj.isVisible() == false) {
                continue;
            }

            if (obj instanceof GeoSet) {

                // close the current non-GeoSet layer
                if (usingNonGeoSetLayer) {
                    usingNonGeoSetLayer = false;
                    writeLayerEnd(writer);
                }

                // wrap the GeoSet into a layer
                writeLayerStart(obj.getName(), writer);
                writeGeoSet((GeoSet) obj, writer);
                writeLayerEnd(writer);

            } else {

                // create a layer if this is a non-GeoSet and there is currently
                // no layer.
                if (!usingNonGeoSetLayer) {
                    writeLayerStart(null, writer);
                    usingNonGeoSetLayer = true;
                }

                if (obj instanceof GeoPath) {
                    writeGeoPath((GeoPath) obj, writer);
                } else if (obj instanceof GeoPoint) {
                    writeGeoPoint((GeoPoint) obj, writer);
                } else if (obj instanceof GeoText) {
                    writeGeoText((GeoText) obj, writer);
                } else if (obj instanceof GeoImage) {
                    writeGeoImage((GeoImage) obj, writer);
                }

            }

        }

    }

    /**
     * Write the start of a layer.
     * @param layerName The name for the layer. If null or empty, an automatically
     * created name will be used.
     * @writer The destination to write to.
     */
    private void writeLayerStart(String layerName, PrintWriter writer) {

        ++layerCounter;

        writer.println("%AI5_BeginLayer");
        writer.println("1 1 1 1 0 0 0 0 0 0 Lb");
        if (layerName != null && layerName.trim().length() > 0) {
            writer.print(this.stringToPostScript(layerName.trim()));
            writer.println(" Ln");
        } else {
            writer.println("(Layer " + layerCounter + ") Ln");
        }
    }

    /**
     * Write the end of a layer.
     * @writer The destination to write to.
     */
    private void writeLayerEnd(PrintWriter writer) {
        writer.println("LB");
        writer.println("%AI5_EndLayer--");
    }

    /** Write a GeoSet and all its children.
     */
    private void writeGeoSet(GeoSet geoSet, PrintWriter writer) {

        final int nbrObj = geoSet.getNumberOfChildren();
        for (int i = 0; i < nbrObj; i++) {
            GeoObject obj = geoSet.getGeoObject(i);

            // only write visible elements
            if (obj.isVisible() == false) {
                continue;
            }

            if (obj instanceof GeoSet) {
                writeGeoSet((GeoSet) obj, writer);
            } else if (obj instanceof GeoPath) {
                writeGeoPath((GeoPath) obj, writer);
            } else if (obj instanceof GeoPoint) {
                writeGeoPoint((GeoPoint) obj, writer);
            } else if (obj instanceof GeoText) {
                writeGeoText((GeoText) obj, writer);
            } else if (obj instanceof GeoImage) {
                writeGeoImage((GeoImage) obj, writer);
            }
        }

    }

    /**
     * Convert and write the pixels of an image to hexadecimal format.
     */
    private void streamImage(GeoImage geoImage, PrintWriter writer) {
        // write lines of 60 characters, as Illustrator does.
        char[] line = new char[60];

        // count the characters in a line
        int counter = 0;

        java.awt.image.BufferedImage image = geoImage.getBufferedImage();

        // loop over all rows and columns in the image
        int cols = geoImage.getCols();
        int rows = geoImage.getRows();

        // ASCII code of 0 and A minus 10
        final int i0 = '0';
        final int iA = 'A' - 10;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // getRGB is slow !!! ???
                final int rgb = image.getRGB(col, row);

                // red
                final int r = (rgb >> 16) & 255;
                final int r1 = r / 16;
                line[counter++] = (char) (r1 + (r1 < 10 ? i0 : iA));
                final int r2 = r % 16;
                line[counter++] = (char) (r2 + (r2 < 10 ? i0 : iA));

                // green
                final int g = (rgb >> 8) & 255;
                final int g1 = g / 16;
                line[counter++] = (char) (g1 + (g1 < 10 ? i0 : iA));
                final int g2 = g % 16;
                line[counter++] = (char) (g2 + (g2 < 10 ? i0 : iA));

                // blue
                final int b = rgb & 255;
                final int b1 = b / 16;
                line[counter++] = (char) (b1 + (b1 < 10 ? i0 : iA));
                final int b2 = b % 16;
                line[counter++] = (char) (b2 + (b2 < 10 ? i0 : iA));

                // write the line if it's full
                if (counter == line.length) {
                    writer.print("%");
                    writer.println(line);
                    counter = 0;
                }
            }
        }

        // write the rest of the last line
        if (counter > 0) {
            writer.print("%");
            for (int i = 0; i < counter; i++) {
                writer.print(line[i]);
            }
            writer.println();
        }
    }

    /**
     * Write a GeoImage:
     *  This does not work !!! ???
     */
    private void writeGeoImage(GeoImage geoImage, PrintWriter writer) {

        // write bounding box
        Rectangle2D rect = geoImage.getBounds2D(GeoObject.UNDEFINED_SCALE);
        this.writeGeoPath(GeoPath.newRect(rect), writer);

        // decide whether to write internal (embedded) or external (linked) image
        java.net.URL url = geoImage.getURL();
        final boolean writeExternalFile = url != null
                && url.getProtocol().equals("file")
                && url.getPath() != null;

        // get the dimension of the image
        Rectangle2D bounds = geoImage.getBounds2D(GeoObject.UNDEFINED_SCALE);
        double w = dimToPageRoundedPx(bounds.getWidth());
        double h = dimToPageRoundedPx(bounds.getHeight());

        // reset graphics state. Raster images will be filled with the
        // current foreground color otherwise. Copied from Illustrator output.
        writer.println("0 O"); // no overprinting
        writer.println("0 g"); // gray fill color: 0 (black), 1 (white)
        writer.println("1 w"); // linewidth 1

        // start writing a raster image
        writer.println("%AI5_File:");
        writer.println("%AI5_BeginRaster");

        // write file path for external image file.
        if (writeExternalFile) {
            // write the file path to the image that is locally stored.
            String path = url.getPath();
            // replace '/' by ':' on Mac
            if (ika.utils.Sys.isMacOSX()) {
                String pathSeparator = System.getProperty("file.separator");
                path = ika.utils.StringUtils.replace(path, pathSeparator, ":");
            }
            writer.print(this.stringToPostScript(path));
            writer.println(" 0 XG");
        }

        // rotation and scale
        writer.print("[1 0 0 1 ");

        // position
        writer.print(xToPageRoundedPx(bounds.getMinX()));
        writer.print(" ");
        writer.print(yToPageRoundedPx(bounds.getMaxY()));
        writer.print("]");

        // Bounds (lower left and upper right x,y coordinates).
        writer.print(" 0 0 ");
        writer.print(w);
        writer.print(" ");
        writer.print(h);

        // Size in pixel: width and height (The documentation inverts height
        // and width mistakenly).
        writer.print(" ");
        writer.print(geoImage.getCols());
        writer.print(" ");
        writer.print(geoImage.getRows());

        // number of bits
        writer.print(" 8");

        // image type
        writer.print(" 3"); // Grayscale:1 RGB:3 CMYK:4     // !!! ???

        // alpha channel count
        writer.print(" 0");

        // reserved
        writer.print(" 0");

        // binary or ascii
        writer.print(" 0"); // binary

        // image mask
        writer.print(" 0"); // opaque

        // image operator
        if (writeExternalFile) {
            writer.println(" XF");
        } else {
            writer.println(" XI");

            // write the image data for images on remote servers or images
            // that are not stored on the hard disk
            this.streamImage(geoImage, writer);
        }

        // close writing of the raster image
        writer.println("%AI5_EndRaster");
        writer.println("F");

        this.writeName(geoImage, writer);
    }

    /**
     * Write a GeoText
     */
    private void writeGeoText(GeoText geoText, PrintWriter writer) {

        FontSymbol symbol = geoText.getFontSymbol();

        writePaintingAttributes(Color.black, Color.black,
                this.currentStrokeWidth, writer);

        // 0 To   text object of type point text start
        writer.println("0 To");

        // text at a point start Tp
        writer.print("1 0 0 1 ");   // rotation and scale
        final double x = geoText.getVisualX(1. / getDisplayMapScale());
        final double y = geoText.getVisualY(1. / getDisplayMapScale());
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
        writer.print(this.fontToPSFontName(symbol.getFont()));
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
        // 0Ñleft aligned, 1Ñcenter aligned, 2Ñright aligned,
        // 3Ñjustified (right and left)
        if (symbol.isCenterHor()) {
            writer.println("1 Ta");
        } else {
            writer.println("0 Ta");
        }

        // leading = space between lines and paragraphs
        writer.println("0 0 Tl");

        // actual text
        String str = geoText.getText();
        str = this.stringToPostScript(str);
        writer.print(str);
        writer.println(" TX");

        // TO text object of type point text end
        writer.println("TO");

        this.writeName(geoText, writer);
    }

    /**
     * Write a GeoPoint.
     */
    private void writeGeoPoint(GeoPoint geoPoint, PrintWriter writer) {
        PointSymbol pointSymbol = geoPoint.getPointSymbol();
        GeoPath geoPath = pointSymbol.getPointSymbol(this.getDisplayMapScale(),
                geoPoint.getX(), geoPoint.getY());

        // find out whether the path is compound, i.e. it consists of more than
        // one path. A new PathIterator has to be created for this.
        final boolean compound = geoPath.isCompound();

        GeoPathIterator pi = geoPath.getIterator();
        this.writePathIterator(pi, compound, pointSymbol, writer);
        this.writeName(geoPoint, writer);
    }

    /** Write a GeoPath.
     */
    private void writeGeoPath(GeoPath geoPath, PrintWriter writer) {

        if (geoPath.getDrawingInstructionCount() == 0) {
            return;
        }

        // find out whether the path is compound, i.e. it consists of more than
        // one path.
        final boolean compound = geoPath.isCompound();

        GeoPathIterator pi = geoPath.getIterator();
        this.writePathIterator(pi, compound, geoPath.getVectorSymbol(), writer);
        this.writeName(geoPath, writer);
    }

    /** Write a path iterator describing a graphic objects of straight lines,
     * Bezier curves, potentially with holes and islands.
     * @param pi The GeoPathIterator to write.
     * @param compound True if the path consists of multiple compound paths.
     * Note: This property cannot be derived from the pi, since there is no way
     * to reset a PathIterator after iterating through it.
     * @param vectorSymbol The VectorSymbol specifying the appearance of pi.
     */
    private void writePathIterator(GeoPathIterator pi,
            boolean compound,
            VectorSymbol vectorSymbol,
            PrintWriter writer) {

        final int UNDEF_SEG_TYPE = -999;

        // remember the last written segment type and the point position.
        int lastSegmentType = UNDEF_SEG_TYPE;
        double lastMoveToX = 0;
        double lastMoveToY = 0;
        double lastEndX = 0;
        double lastEndY = 0;
        // write colors and stroke width if necessary.
        this.writePaintingAttributes(vectorSymbol, writer);

        if (compound) {
            writer.print("*u\n");
        }

        // write geometry
        do {
            final int segmentType = pi.getInstruction();
            switch (segmentType) {
                case GeoPathModel.CLOSE:
                    if (lastSegmentType == GeoPathModel.CLOSE
                            || lastSegmentType == GeoPathModel.MOVETO
                            || lastSegmentType == UNDEF_SEG_TYPE) {
                        break;
                    }

                    // draw straight line to starting point if the last
                    // segment did not already close the path
                    if (lastEndX != lastMoveToX || lastEndY != lastMoveToY) {
                        writeCoordinate(lastMoveToX, lastMoveToY, "L", writer);
                    }

                    // paint partial path
                    this.writeFillStroke(vectorSymbol, true, writer);
                    break;

                case GeoPathModel.LINETO:
                    lastEndX = pi.getX();
                    lastEndY = pi.getY();
                    writeCoordinate(pi.getX(), pi.getY(), "L", writer);
                    break;

                case GeoPathModel.MOVETO:
                    // paint previous partial path
                    if (lastSegmentType != GeoPathModel.CLOSE
                            && lastSegmentType != GeoPathModel.MOVETO
                            && lastSegmentType != UNDEF_SEG_TYPE) {
                        this.writeFillStroke(vectorSymbol, false, writer);
                    }

                    // start defintion of new path
                    lastMoveToX = pi.getX();
                    lastMoveToY = pi.getY();
                    lastEndX = pi.getX();
                    lastEndY = pi.getY();
                    this.writeCoordinate(lastMoveToX, lastMoveToY, "m", writer);

                    break;

                case GeoPathModel.QUADCURVETO:
                    // not implemented: transform to cubic bezier
                    throw new UnsupportedOperationException("Not yet implemented");

                case GeoPathModel.CURVETO:
                    this.writeCoordinate(pi.getX(), pi.getY(), writer);
                    this.writeCoordinate(pi.getX2(), pi.getY2(), writer);
                    this.writeCoordinate(pi.getX3(), pi.getY3(), "C", writer);
                    lastEndX = pi.getX3();
                    lastEndY = pi.getY3();
                    break;
            }
            lastSegmentType = segmentType;
        } while (pi.next());

        if (lastSegmentType != GeoPathModel.CLOSE
                && lastSegmentType != GeoPathModel.MOVETO) {
            this.writeFillStroke(vectorSymbol, false, writer);
        }
        // close the compound path
        if (compound) {
            writer.print("*U\n");
        }
    }

    /**
     * Write the name of an object (if it has one). This feature is not officially documented.
     * Before version 6, no names have been found in any file. Names are written using "object tags" (XT).
     * This XT operator uses the "AIArtName" identifier. Identifiers must be preceded by a slash.
     * Names are also written for layers although layers have their own operator to indicate the name.
     * This is conformal to what has been found in files written by Illustrator 9.
     */
    private void writeName(GeoObject geoObject, PrintWriter writer) {
        String name = geoObject.getName();
        if (name == null || name.length() == 0) {
            return;
        }

        name = this.stringToPostScript(name);
        if (name != null && name.trim().length() > 0) {
            writer.print("/AIArtName ");
            writer.print(name.trim());
            writer.println(" XT");
        }
    }

    /**
     * Convert a java string to a PostScript string.
     */
    private String stringToPostScript(String str) {
        if (str == null) {
            return "()";
        }

        // replace bachslash by double backslash
        str = ika.utils.StringUtils.replace(str, "\\", "\\\\");

        // replace formatting characters
        str = ika.utils.StringUtils.replace(str, "\n", "\\n");
        str = ika.utils.StringUtils.replace(str, "\r", "\\r");
        str = ika.utils.StringUtils.replace(str, "\t", "\\t");
        str = ika.utils.StringUtils.replace(str, "\b", "\\b");
        str = ika.utils.StringUtils.replace(str, "\f", "\\f");

        // replace parentheses
        str = ika.utils.StringUtils.replace(str, "(", "\\(");
        str = ika.utils.StringUtils.replace(str, ")", "\\)");

        // missing: convert ASCII chars > 127 to base 85 encoding !!! ???

        return "(" + str + ")";
    }

    /**
     * Transforms a pair of coordinate to sheet coordinates, and writes them
     * using the specified operator.
     */
    private void writeCoordinate(double x, double y, String operator, PrintWriter writer) {
        writeCoordinate(x, y, writer);
        writer.println(operator);
    }

    /**
     * Transforms a pair of coordinate to sheet coordinates, and writes them.
     */
    private void writeCoordinate(double x, double y, PrintWriter writer) {
        // apply offset and scale
        x = xToPageRoundedPx(x);
        y = yToPageRoundedPx(y);

        writer.print(coordinateFormat.format(x));
        writer.print(" ");
        writer.print(coordinateFormat.format(y));
        writer.print(" ");
    }

    /** Writes commands to stroke and / or fill the last geometry.
     */
    private void writeFillStroke(VectorSymbol symbol, boolean close, PrintWriter writer) {
        if (symbol == null) {
            return;
        }

        String paintingOperator;
        boolean fill = symbol.isFilled();
        boolean stroke = symbol.isStroked();

        if (fill && stroke) {
            paintingOperator = close ? "b" : "B";
        } else if (fill) {
            paintingOperator = close ? "f" : "F";
        } else if (stroke) {
            paintingOperator = close ? "s" : "S";
        } else // nothing: invisible element
        {
            paintingOperator = close ? "n" : "N";
        }

        writer.println(paintingOperator);
    }

    /**
     * Writes a new graphic state (fill and stroke colors, stroke width). Does
     * not write information that has not changed since the last call of this
     * method.
     */
    private void writePaintingAttributes(VectorSymbol symbol, PrintWriter writer) {
        if (symbol == null) {
            return;
        }
        Color newFillColor = symbol.getFillColor();
        Color newStrokeColor = symbol.getStrokeColor();
        float newStrokeWidth = symbol.getStrokeWidth();
        writePaintingAttributes(newFillColor, newStrokeColor, newStrokeWidth, writer);
    }

    /**
     * Writes a new graphic state (fill and stroke colors, stroke width). Does
     * not write information that has not changed since the last call of this
     * method.
     */
    private void writePaintingAttributes(Color newFillColor,
            Color newStrokeColor, float newStrokeWidth, PrintWriter writer) {

        if (newStrokeWidth <= 0) {
            newStrokeWidth = 1;
        }

        // make sure currentColors have been initialized. This is not the case
        // for the first object.
        if (this.currentFillColor == null) {
            writeFillColor(newFillColor, writer);
            this.currentFillColor = newFillColor;
            writeStrokeColor(newStrokeColor, writer);
            this.currentStrokeColor = newStrokeColor;
            writer.println(Float.toString(newStrokeWidth) + " w");
            this.currentStrokeWidth = newStrokeWidth;
            return;
        }

        if (newFillColor != null && !newFillColor.equals(this.currentFillColor)) {
            writeFillColor(newFillColor, writer);
            this.currentFillColor = newFillColor;
        }
        if (newStrokeColor != null && !newStrokeColor.equals(this.currentStrokeColor)) {
            writeStrokeColor(newStrokeColor, writer);
            this.currentStrokeColor = newStrokeColor;
        }
        if (newStrokeWidth >= 0.f && newStrokeWidth != this.currentStrokeWidth) {
            writer.println(Float.toString(newStrokeWidth) + " w");
            this.currentStrokeWidth = newStrokeWidth;
        }
    }

    /** Write RGB color.
     */
    private void writeColor(Color color, PrintWriter writer) {
        writer.print(color.getRed() / 255.f);
        writer.print(" ");
        writer.print(color.getGreen() / 255.f);
        writer.print(" ");
        writer.print(color.getBlue() / 255.f);
    }

    /** Write fill color in RGB.
     */
    private void writeFillColor(Color color, PrintWriter writer) {
        this.writeColor(color, writer);
        writer.println(" Xa");
    }

    /** Write stroke color in RGB.
     */
    private void writeStrokeColor(Color color, PrintWriter writer) {
        this.writeColor(color, writer);
        writer.println(" XA");
    }

    private String fontToPSFontName(Font font) {
        String fontName = font.getPSName();
        if ("ArialMS".equals(fontName)) // Illustrator cannot read Microsoft's Arial
        {
            fontName = "ArialMT";
        }
        return fontName;
    }

    /**
     * Transforms y coordinate to pixel coordinates.
     * Overwrites method because y axis is upwards oriented in Illustrator.
     */
    @Override
    protected double yToPagePx(double y) {
        final double mapScale = this.pageFormat.getPageScale();
        final double south = this.pageFormat.getPageBottom();
        return (y - south) * 1000. * PageFormat.MM2PX / mapScale;
    }
}
