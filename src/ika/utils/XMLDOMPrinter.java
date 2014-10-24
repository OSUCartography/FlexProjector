/*
 * XMLDOMPrinter.java
 *
 * Created on May 31, 2005, 2:32 PM
 *
 */

package ika.utils;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * Outputs a XML DOM to a file or to a PrtinStream.
 */
public class XMLDOMPrinter {
    
    /**
     * Writes a DOM to a file.
     * @param filename The path of the file that will be generated.
     * @param document The DOM to write.
     */
    public static void writeXML(String filename, Document document)
    throws TransformerConfigurationException, TransformerException{
        // Prepare the output file
        File file = new File(filename);
        Result result = new StreamResult(file);
        XMLDOMPrinter.outputXML(result, document);
    }
    
    /**
     * Prints a DOM to a PrintStream.
     * Use with standard output: XMLDOMPrinter.printXML(document, System.out);
     * @param document The DOM to print.
     * @param printer The printer to print to.
     */
    public static void printXML(Document document, PrintStream printer)
    throws TransformerConfigurationException, TransformerException{
        // First write to a StringWriter, then print to the printer.
        StringWriter w = new StringWriter();
        Result result = new StreamResult(w);
        XMLDOMPrinter.outputXML(result, document);
        printer.println(w.getBuffer().toString());
    }
    
    /**
     * Outputs a DOM to a Result object.
     * @param result The destination to write to.
     * @param document The DOM to output.
     */
    public static void outputXML(Result result, Document document)
    throws TransformerConfigurationException, TransformerException {
        // Prepare the DOM document for writing
        Source source = new DOMSource(document);
        // Get Transformer
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        // Write
        xformer.transform(source, result);
    }
    
}
