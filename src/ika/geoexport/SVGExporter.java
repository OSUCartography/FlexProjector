package ika.geoexport;

import ika.geo.*;
import ika.utils.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * Exporter for the SVG file format.
 */
public class SVGExporter extends VectorGraphicsExporter {

    /**
     * If useCSSStyles is true vector styles are written using CSS styles.
     * Otherwise attributes are used. Rendering of interactively changed CSS
     * styles seems to be slower. CSS should therefore not be used for
     * interactive maps that change symbolization attributes. CSS styles are not
     * recommended, see http://jwatt.org/svg/authoring/ and are not part of SVG
     * Tiny.
     */
    protected boolean useCSSStyles = false;
    protected boolean writeCompactPath = false;
    protected static String SVGNAMESPACE = "http://www.w3.org/2000/svg";
    protected static String XLINKNAMESPACE = "http://www.w3.org/1999/xlink";
    protected static String XMLEVENTSNAMESPACE = "http://www.w3.org/2001/xml-events";

//    private static String svgIdentifier = "-//W3C//DTD SVG 1.0//EN";
//    private static String svgDTD = "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd";
    public SVGExporter() {
    }

    public String getFileFormatName() {
        return "SVG";
    }

    public String getFileExtension() {
        return "svg";
    }

    /**
     * Give derived classes an option to provide own Document.
     *
     * @return
     */
    protected Document createDocument() throws ParserConfigurationException {
        // create a document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();

    }

    /**
     * Exports a GeoSet to a new SVG file.
     *
     * @param geoSet The GeoSet to export.
     * @param outputStream The OutputStream that will be used to export to.
     * @throws java.io.IOException Throws an exception if export is not
     * possible.
     */
    protected void write(GeoSet geoSet, OutputStream outputStream)
            throws IOException {
        try {
            Document document = createDocument();

            // construct the SVG root element
            Element svgRootElement = createSVGRootElement(geoSet, document);
            document.appendChild(svgRootElement);

            // add content to SVG root element
            addSVGContent(geoSet, document, svgRootElement);

            // Prepare the output file
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(outputStream, "utf-8");
            StreamResult result = new StreamResult(outputStreamWriter);

            // Write the DOM document to the file
            /*
             There is a bug in Java 1.5: XML output is not indented.
             To work around this bug:
             http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
             (1)set the indent-number in the transformerfactory
             TransformerFactory tf = new TransformerFactory.newInstance();
             tf.setAttribute("indent-number", new Integer(2));
             (2)enable the indent in the transformer
             Transformer t = tf.newTransformer();
             t.setOutputProperty(OutputKeys.INDENT, "yes");
             (3)wrap the otuputstream with a writer (or bufferedwriter)
             t.transform(new DOMSource(doc),
             new StreamResult(new OutputStreamWriter(out, "utf-8"));
             You must do (3) to workaround a "buggy" behavior of the
             xml handling code.
             */

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));
            Transformer xformer = tf.newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Source source = new DOMSource(document);
            xformer.transform(source, result);

            // don't add doctype to SVG files. see http://jwatt.org/svg/authoring/
            /*
             xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, svgIdentifier);
             xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, svgDTD);
             */

            outputStreamWriter.flush();

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().toString();
            throw new IOException("Export to SVG not possible. " + msg);
        }
    }

    /**
     * Creates the top level SVG element.
     */
    protected Element createSVGRootElement(GeoSet geoSet, Document document) {
        // create the main svg element
        Element svg = (Element) document.createElementNS(SVGNAMESPACE, "svg");

        // specify a namespace prefix on the 'svg' element, which means that
        // SVG is the default namespace for all elements within the scope of
        // the svg element with the xmlns attribute:
        // See http://www.w3.org/TR/SVG11/struct.html#SVGElement
        // and http://jwatt.org/svg/authoring/
        svg.setAttribute("xmlns", SVGNAMESPACE);
        svg.setAttribute("xmlns:xlink", XLINKNAMESPACE);
        svg.setAttribute("xmlns:ev", XMLEVENTSNAMESPACE);
        svg.setAttribute("version", "1.0");
        svg.setAttribute("preserveAspectRatio", "xMinYMin");

        final double wWC = pageFormat.getPageWidthWorldCoordinates();
        final double hWC = pageFormat.getPageHeightWorldCoordinates();
        final double w = dimToPageRoundedPx((float) wWC);
        final double h = dimToPageRoundedPx((float) hWC);
        svg.setAttribute("width", Double.toString(w));
        svg.setAttribute("height", Double.toString(h));

        // Define the viewBox.
        String viewBoxStr = "0 0 " + w + " " + h;
        svg.setAttribute("viewBox", viewBoxStr);

        return svg;
    }

    /**
     * The default implementation simply creates a g element. Derived classes
     * may overwrite this.
     */
    protected Element createSVGGroupElement(GeoSet geoSet, Document document) {
        // don't write invisible or empty GeoSets
        if (!geoSet.hasVisibleGeoObjects()) {
            return null;
        }

        return document.createElementNS(SVGNAMESPACE, "g");
    }

    protected void finish(GeoSet geoSet, Document doc,
            Element element) {
    }

    protected void finish(GeoPath geoPath, Document doc,
            Element element) {
    }

    protected void finish(GeoPoint geoPoint, Document doc,
            Element element) {
    }

    protected void finish(GeoText geoText, Document doc,
            Element element) {
    }

    protected void addSVGContent(GeoSet geoSet, Document document, Element svgRootElement) throws IOException {

        // add a description element
        appendDescription(svgRootElement, document);

        // convert GeoSet to SVG DOM
        writeGeoSet(geoSet, svgRootElement, document);
    }

    protected void writeGeoObject(GeoObject obj, Element parent, Document doc) throws IOException {
        if (obj instanceof GeoPath) {
            writeGeoPath((GeoPath) obj, parent, doc);
        } else if (obj instanceof GeoImage) {
            writeGeoImage((GeoImage) obj, parent, doc);
        } else if (obj instanceof GeoPoint) {
            writeGeoPoint((GeoPoint) obj, parent, doc);
        } else if (obj instanceof GeoText) {
            writeGeoText((GeoText) obj, parent, doc);
        }
    }

    /**
     * Converts a GeoSet to a SVG DOM.
     *
     * @param geoSet The GeoSet to convert.
     * @param parent The parent element that will contain the passed GeoSet.
     * @param document The DOM.
     */
    protected void writeGeoSet(GeoSet geoSet, Element parent, Document document) throws IOException {

        Element g = createSVGGroupElement(geoSet, document);
        if (g == null) {
            return;
        }
        parent.appendChild(g);

        final int nbrObj = geoSet.getNumberOfChildren();
        for (int i = 0; i < nbrObj; i++) {
            GeoObject obj = geoSet.getGeoObject(i);

            // only write visible elements
            if (obj.isVisible() == false) {
                continue;
            }

            if (obj instanceof GeoSet) {
                writeGeoSet((GeoSet) obj, g, document);
            } else {
                writeGeoObject(obj, g, document);
            }
        }

        finish(geoSet, document, g);
    }

    protected Element geoTextToSVG(GeoText geoText, Document document) {
        FontSymbol symbol = geoText.getFontSymbol();

        final double x = xToPageRoundedPx((float) geoText.getVisualX(1. / getDisplayMapScale()));
        final double y = yToPageRoundedPx((float) geoText.getVisualY(1. / getDisplayMapScale()));

        Element text = document.createElementNS(SVGNAMESPACE, "text");
        text.setAttribute("x", Double.toString(x));
        text.setAttribute("y", Double.toString(y));
        if (useCSSStyles) {
            text.setAttribute("style", symbolToCSS(symbol));
        } else {
            Font font = symbol.getFont();
            text.setAttribute("font-size", Integer.toString(symbol.getSize()));
            text.setAttribute("font-family", font.getFamily());
            text.setAttribute("fill", "black");

            switch (font.getStyle()) {
                case Font.PLAIN:
                    text.setAttribute("font-style", "normal");
                    break;
                case Font.BOLD:
                    text.setAttribute("font-weight", "bold");
                    break;
                case Font.ITALIC:
                    text.setAttribute("font-style", "italic");
                    break;
            }

            if (symbol.isCenterHor()) {
                text.setAttribute("text-anchor", "middle");
            } else {
                text.setAttribute("text-anchor", "start");
            }

            if (symbol.isCenterVer()) {
                text.setAttribute("baseline-shift", "50%");
            }
        }

        text.setAttribute("id", Long.toString(geoText.getID()));
        Node textNode = document.createTextNode(geoText.getText());
        text.appendChild(textNode);
        return text;
    }

    protected void writeGeoText(GeoText geoText, Element parent, Document document) throws IOException {
        Element el = geoTextToSVG(geoText, document);
        parent.appendChild(el);
        finish(geoText, document, el);
    }

    protected void writeGeoImage(GeoImage geoImage, Element parent, Document document) throws IOException {
        Rectangle2D bounds = geoImage.getBounds2D(GeoObject.UNDEFINED_SCALE);
        String xStr = Double.toString(xToPageRoundedPx((float) bounds.getMinX()));
        String yStr = Double.toString(yToPageRoundedPx((float) bounds.getMaxY()));
        String wStr = Double.toString(dimToPageRoundedPx((float) bounds.getWidth()));
        String hStr = Double.toString(dimToPageRoundedPx((float) bounds.getHeight()));

        Element image = (Element) document.createElementNS(SVGNAMESPACE, "image");
        image.setAttribute("x", xStr);
        image.setAttribute("y", yStr);
        image.setAttribute("width", wStr);
        image.setAttribute("height", hStr);
        image.setAttribute("xlink:href", geoImage.getURL().toExternalForm());
        parent.appendChild(image);

        // add rectangle of the size of the image
        Element rect = (Element) document.createElementNS(SVGNAMESPACE, "rect");
        rect.setAttribute("x", xStr);
        rect.setAttribute("y", yStr);
        rect.setAttribute("width", wStr);
        rect.setAttribute("height", hStr);
        rect.setAttribute("fill", "none");
        rect.setAttribute("stroke", "blue");
        rect.setAttribute("stroke-width", "1");
        parent.appendChild(rect);
    }

    protected void writeGeoPoint(GeoPoint geoPoint, Element parent, Document document) throws IOException {

        // Unfortunately Illustrator CS does not support SVG symbols correctly.
        // Therefore don't write SVG symbols, but convert GeoPoints to
        // normal graphics.
        PointSymbol pointSymbol = geoPoint.getPointSymbol();
        GeoPath geoPath = pointSymbol.getPointSymbol(getDisplayMapScale(),
                geoPoint.getX(), geoPoint.getY());
        GeoPathIterator pi = geoPath.getIterator();
        Element pathElement = writePathIterator(pi, pointSymbol, document);
        parent.appendChild(pathElement);
        finish(geoPoint, document, pathElement);
    }

    /**
     * Converts a GeoPath to a SVG path.
     *
     * @param geoPath The GeoPath to convert.
     * @param parent The parent element that will contain the passed GeoSet.
     * @param document The DOM.
     */
    protected void writeGeoPath(GeoPath geoPath, Element parent, Document document) throws IOException {
        GeoPathIterator pi = geoPath.getIterator();
        Element pathElement = writePathIterator(pi, geoPath.getVectorSymbol(), document);
        parent.appendChild(pathElement);
        finish(geoPath, document, pathElement);
    }

    protected void writeVectorSymbol(Element svgElement, VectorSymbol vectorSymbol) {
        if (vectorSymbol != null && svgElement != null) {
            if (useCSSStyles) {
                svgElement.setAttribute("style", symbolToCSS(vectorSymbol));
            } else {
                String strokeColor = vectorSymbol.isStroked()
                        ? ColorUtils.colorToCSSString(vectorSymbol.getStrokeColor())
                        : "none";
                svgElement.setAttribute("stroke", strokeColor);

                String fillColor = vectorSymbol.isFilled()
                        ? ColorUtils.colorToCSSString(vectorSymbol.getFillColor())
                        : "none";
                svgElement.setAttribute("fill", fillColor);

                if (vectorSymbol.isFillTransparent()) {
                    float alpha = Math.round(vectorSymbol.getFillTransparency() / 255f * 100) / 100f;
                    svgElement.setAttribute("fill-opacity", Float.toString(alpha));
                }
                double strokeWidth = vectorSymbol.getScaledStrokeWidth(getDisplayMapScale());
                if (strokeWidth <= 0) {
                    strokeWidth = 1;
                }
                svgElement.setAttribute("stroke-width", Double.toString(strokeWidth));
            }
        }
    }

    protected Element writePathIterator(GeoPathIterator pi, VectorSymbol vectorSymbol,
            Document document) {
        String svgPath = convertPathIteratorToSVG(pi);
        Element pathElement = (Element) document.createElementNS(SVGNAMESPACE, "path");
        writeVectorSymbol(pathElement, vectorSymbol);
        pathElement.setAttribute("d", svgPath);
        return pathElement;
    }

    private String convertPathIteratorToSVG(GeoPathIterator iterator) {
       
        StringBuilder str = new StringBuilder();
        do {
            final int instruction = iterator.getInstruction();
            switch (instruction) {
                case GeoPathModel.CLOSE:
                    if (!writeCompactPath) {
                        str.append(" ");
                    }
                    str.append("z");
                    break;

                case GeoPathModel.MOVETO:
                    if (!writeCompactPath && str.length() > 0) {
                        str.append(" ");
                    }
                    str.append("M");
                    str.append(xToPageRoundedPx(iterator.getX()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY()));
                    break;

                case GeoPathModel.LINETO:
                    if (!writeCompactPath) {
                        str.append(" ");
                    }
                    str.append("L");
                    str.append(xToPageRoundedPx(iterator.getX()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY()));
                    break;

                case GeoPathModel.QUADCURVETO:
                    if (!writeCompactPath) {
                        str.append(" ");
                    }
                    str.append("Q");
                    str.append(xToPageRoundedPx(iterator.getX()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY()));
                    str.append(" ");
                    str.append(xToPageRoundedPx(iterator.getX2()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY2()));
                    break;

                case GeoPathModel.CURVETO:
                    if (!writeCompactPath) {
                        str.append(" ");
                    }
                    str.append("C");
                    str.append(xToPageRoundedPx(iterator.getX()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY()));
                    str.append(" ");
                    str.append(xToPageRoundedPx(iterator.getX2()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY2()));
                    str.append(" ");
                    str.append(xToPageRoundedPx(iterator.getX3()));
                    str.append(" ");
                    str.append(yToPageRoundedPx(iterator.getY3()));
                    break;
            }
        } while (iterator.next());

        return str.toString();
    }

    /**
     * Converts a VectorSymbol to a CSS style.
     *
     * @param symbol The VectorSymbol to convert.
     * @return A CSS formated string.
     */
    public String symbolToCSS(VectorSymbol symbol) {
        StringBuffer str = new StringBuffer();

        // fill
        if (symbol.isFilled()) {
            str.append("fill:");
            str.append(ColorUtils.colorToCSSString(symbol.getFillColor()));
            str.append(";");
            if (symbol.isFillTransparent()) {
                String fillOpacity =
                        Float.toString(symbol.getFillTransparency() / 255.f);
                str.append("fill-opacity:");
                str.append(fillOpacity);
                str.append(";");
            }
        } else {
            str.append("fill:none;");
        }

        // stroke
        if (symbol.isStroked()) {
            str.append("stroke:");
            str.append(ColorUtils.colorToCSSString(symbol.getStrokeColor()));
            str.append(";");
            str.append("stroke-width:");
            double strokeWidth = dimToPageRoundedPx(
                    symbol.getScaledStrokeWidth(getDisplayMapScale()));
            if (strokeWidth <= 0) {
                strokeWidth = 1;
            }
            str.append(strokeWidth);
            str.append(";");
            if (symbol.isDashed()) {
                str.append("stroke-dasharray:");
                str.append(dimToPageRoundedPx(symbol.getScaledDashLength(getDisplayMapScale())));
                str.append(",");
                str.append(dimToPageRoundedPx(symbol.getScaledDashLength(getDisplayMapScale())));
                str.append(";");
            }
        } // stroke:none is default and therefore not needed.

        return str.toString();
    }

    protected String symbolToCSS(FontSymbol symbol) {
        StringBuilder str = new StringBuilder();
        str.append("font-size:");
        str.append(/*this.transformDimRound*/(symbol.getSize()));

        Font font = symbol.getFont();
        str.append(";font-family:");

        // font names that consist of multiple words must be enclosed by ''
        String fontFamily = font.getFamily();
        if (fontFamily.indexOf(" ") != -1) {
            fontFamily = "'" + fontFamily + "'";
        }
        str.append(fontFamily);
        str.append(";fill:black;");

        switch (font.getStyle()) {
            case Font.PLAIN:
                str.append("font-style:normal;");
                break;
            case Font.BOLD:
                str.append("font-weight:bold;");
                break;
            case Font.ITALIC:
                str.append("font-style:italic;");
                break;
        }

        if (symbol.isCenterHor()) {
            str.append("text-anchor:middle;");
        } else {
            str.append("text-anchor:start;");
        }

        if (symbol.isCenterVer()) {
            str.append("baseline-shift:-50%;");
        }

        return str.toString();
    }

    protected void appendDescription(Element svgRootElement, Document document) {

        StringBuilder str = new StringBuilder();
        String userName = getDocumentAuthor();
        if (userName != null && userName.length() > 0) {
            str.append("Author:");
            str.append(userName);
            str.append(" - ");
        }

        String appName = getApplicationName();
        if (appName != null && appName.length() > 0) {
            str.append("Generator:");
            str.append(appName);
            str.append(" - ");
        }
        str.append("Date:");
        str.append(java.util.Calendar.getInstance().getTime());

        // create a description element
        Element desc = (Element) document.createElementNS(SVGNAMESPACE, "desc");
        desc.appendChild(document.createTextNode(str.toString()));

        // append description element
        svgRootElement.appendChild(desc);
    }
    
    protected void appendTitle(String title, Element svgRootElement) {
        Document doc = svgRootElement.getOwnerDocument();
        Element el = (Element) (doc.createElementNS(SVGNAMESPACE, "title"));
        el.appendChild(doc.createTextNode(title));
        svgRootElement.appendChild(el);
    }

    public boolean isUseCSSStyles() {
        return useCSSStyles;
    }

    public void setUseCSSStyles(boolean useCSSStyles) {
        this.useCSSStyles = useCSSStyles;
    }
}