/*
 * TableExporter.java
 *
 * Created on April 13, 2007, 3:46 PM
 *
 */

package ika.table;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface TableExporter {
    
    public void exportTable(OutputStream outputStream, Table table) 
    throws IOException;
    
}
