/*
 * DBFExporter.java
 *
 * Created on April 13, 2007, 2:18 PM
 *
 */

package ika.table;

import ika.utils.LittleEndianOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.table.TableColumn;

/**
 * Exporter for the DBF file format.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class DBFExporter implements TableExporter {
    
    private static final int STRING_LENGTH = 64;
    private static final int NUMBER_LENGTH = 20;    // F n=1..20
    private static final int NUMBER_DECIMALS = 8;
    
    /** Creates a new instance of DBFExporter */
    public DBFExporter() {
    }
    
    public void exportTable(OutputStream outputStream, Table table)
    throws IOException {
        
        BufferedOutputStream bos = null;
        try {           
            bos = new BufferedOutputStream(outputStream);
            LittleEndianOutputStream dos = new LittleEndianOutputStream(bos);
            
            // dbf flag
            dos.write(0x03);
            
            // current date
            Calendar cal = GregorianCalendar.getInstance();
            int year = cal.get(Calendar.YEAR);             // 2002
            int month = cal.get(Calendar.MONTH);           // 0=Jan, 1=Feb, ...
            int day = cal.get(Calendar.DAY_OF_MONTH);      // 1...
            dos.write(year - 1900);
            dos.write(month);
            dos.write(day);
            
            // number of records
            dos.writeInt(table.getRowCount());
            
            // header size
            final int columnsCount = table.getColumnCount();
            dos.writeShort(32 + columnsCount * 32 + 1);
            
            // record size
            dos.writeShort(this.computeRecordSize(table));
            
            // reserved value, fill with 0
            dos.writeShort(0);
            
            // transaction byte
            dos.write(0);
            
            // encription byte
            dos.write(0);
            
            // multi user environment use
            for (int i = 0; i < 13; i++)
                dos.write(0);
            
            // codepage / language driver
            // ESRI shape files use code 0x57 to indicate that
            // data is written in ANSI (whatever that means).
            // http://www.esricanada.com/english/support/faqs/arcview/avfaq21.asp
            dos.write(0x57);
            
            // two reserved bytes
            dos.writeShort(0);
            
            this.writeFieldDescriptors(dos, table);
            
            // header record terminator
            dos.write(0x0D);
            
            this.writeRecords(dos, table);
        } finally {
            if (bos != null)
                bos.close();
        }
    }
    
    private void writeString(LittleEndianOutputStream dos, String str, int length)
    throws IOException {
        byte[] b = str.getBytes("ISO-8859-1");
        dos.write(b, 0, Math.min(length, b.length));
        for (int c = b.length; c < length; c++)
            dos.write(0);
    }
    
    private void writeFieldDescriptors(LittleEndianOutputStream dos, Table table)
            throws IOException {
        
        int columnsCount = table.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            TableColumn tc = table.getColumn(i);
            
            // write column title, 10 chars, plus terminating 0.
            String title = tc.getHeaderValue().toString().trim();
            this.writeString(dos, title, 10);
            dos.write(0x0);
            
            // write field type
            if (table.isStringColumn(i)) {
                dos.write('C');
                dos.writeInt(0);            // field address (ignored)
                dos.write(STRING_LENGTH);   // field length
                dos.write(0);               // decimal count not used
            } else if (table.isDoubleColumn(i)) {
                dos.write('F');
                dos.writeInt(0);            // field address (ignored)
                dos.write(NUMBER_LENGTH);   // field length
                dos.write(NUMBER_DECIMALS); // decimal count
            } else {
                throw new IOException("DBF export: unsupported type");
            }
            
            // 14 reserved or unusued bytes
            for (int c = 0; c < 14; c++)
                dos.write(0);
            
        }
        
    }
    
    private int computeRecordSize(Table table) throws IOException {
        
        int recordSize = 1; // 1 for deletion flag
        int columnsCount = table.getColumnCount();
        for (int i = 0; i < columnsCount; i++) {
            TableColumn tc = table.getColumn(i);
            
            if (table.isStringColumn(i)) {
                recordSize += STRING_LENGTH;
            } else if (table.isDoubleColumn(i)) {
                recordSize += NUMBER_LENGTH;
            } else {
                throw new IOException("DBF export: unsupported type");
            }
        }
        
        return recordSize;
    }
    
    private void writeRecords(LittleEndianOutputStream dos, Table table)
    throws IOException {
        
        int columnsCount = table.getColumnCount();
        int rowsCount = table.getRowCount();
        
        for (int row = 0; row < rowsCount; row++) {
            // write deleted flag
            dos.write(' ');

            for (int col = 0; col < columnsCount; col++) {
                
                TableColumn tc = table.getColumn(col);
                if (table.isDoubleColumn(col)) {
                    final Double d = (Double)table.getValueAt(row, col);
                    String nbrStr = ika.utils.NumberFormatter.format(
                            d.doubleValue(), NUMBER_LENGTH, NUMBER_DECIMALS);
                    this.writeString(dos, nbrStr, NUMBER_LENGTH);
                } else {
                    String str = (String)table.getValueAt(row, col);
                    this.writeString(dos, str, STRING_LENGTH);  
                }
                
            }
        }
    }
}
