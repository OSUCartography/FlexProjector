/*
 * DBFImporter.java
 *
 * Created on June 9, 2006, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ika.table;

import java.io.*;
import java.net.URLConnection;
import java.util.Vector;
import ika.utils.MathUtils;
import ika.utils.LittleEndianInputStream;

/**
 * An importer for DBF data base files.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class DBFImporter extends TableImporter {
    
    private void printInfo(String str) {
        // uncomment the following line for debugging
        // System.out.println(str);
    }
    
    private Vector fields = new Vector();
    
    private String charsetName;

    /**
     * Default value for numbers that cannot be read.
     */
    private Double DEFAULT_NUMBER = new Double(0);
    
    /** Creates a new instance of DBFImporter */
    public DBFImporter() {
    }
    
    public Table read(java.net.URL url) throws IOException {
        LittleEndianInputStream is = null;
        if (url == null)
            return null;
        
        try {
            // read all data into a data buffer.
            is = new LittleEndianInputStream(new BufferedInputStream(url.openStream()));
            
            URLConnection urlConnection = url.openConnection();
            final long fileLength = urlConnection.getContentLength();
            
            
            int fileCode = MathUtils.unsignedByteToInt(is.readByte());
            int year = MathUtils.unsignedByteToInt(is.readByte());
            int month = MathUtils.unsignedByteToInt(is.readByte());
            int day = MathUtils.unsignedByteToInt(is.readByte());
            long nbrRecords = is.readInt();// & 0xffffffffL;// unsigned int
            int headerSize = is.readShort();
            int recordSize = is.readShort();
            is.readShort();  // overread reserved value
            is.readByte(); // transaction byte
            int encrypted = is.readUnsignedByte(); // encription byte
            is.skipBytes(13);
            int codepage = is.readUnsignedByte();
            is.skipBytes(2);
            
            // map the codepage to a string. Based on:
            // http://www.clicketyclick.dk/databases/xbase/format/dbf.html#DBF_STRUCT
            switch (codepage) {
                case 0x01:  // DOS USA	code page 437
                    this.charsetName = "IBM437";
                    break;
                case 0x02:  // DOS Multilingual	code page 850
                    this.charsetName = "IBM850";
                    break;
                case 0x03:  // Windows ANSI	code page 1252
                    this.charsetName = "windows-1252";
                    break;
                case 0x04:  // Standard Macintosh
                    this.charsetName = "MacRoman";
                    break;
                // ESRI shape files use code 0x57 to indicate that
                // data is written in ANSI (whatever that means).
                // http://www.esricanada.com/english/support/faqs/arcview/avfaq21.asp
                case 0x57:  
                    this.charsetName = "windows-1252";
                    break;
                case 0x64:  // EE MS-DOS	code page 852
                    this.charsetName = "IBM852";
                    break;
                case 0x65:  // Nordic MS-DOS	code page 865
                    this.charsetName = "IBM865";
                    break;
                case 0x66:  // Russian MS-DOS	code page 866
                    this.charsetName = "IBM866";
                    break;
                case 0x67:  // Icelandic MS-DOS
                    this.charsetName = "IBM861";
                    break;
                /*
                case 0x68:  // Kamenicky (Czech) MS-DOS
                    // ?
                    break;
                case 0x69:  // Mazovia (Polish) MS-DOS
                    // ?
                    break;
                */
                case 0x6A:  // Greek MS-DOS (437G) [?]
                    this.charsetName = "x-IBM737";
                    break;
                case 0x6B:  // Turkish MS-DOS
                    this.charsetName = "IBM857";
                    break;
                case 0x96:  // Russian Macintosh
                    this.charsetName = "x-MacCyrillic";
                    break;
                case 0x97:  // Eastern European Macintosh
                    this.charsetName = "x-MacCentralEurope";
                    break;
                case 0x98:  // Greek Macintosh
                    this.charsetName = "x-MacGreek";
                    break;
                case 0xC8:  // Windows EE (=Eastern Europe?) code page 1250
                    this.charsetName = "windows-1250";
                    break;
                case 0xC9:  // Russian Windows
                    this.charsetName = "windows-1251";
                    break;
                case 0xCA:  // Turkish Windows
                    this.charsetName = "windows-1254";
                    break;
                case 0xCB:  // Greek Windows
                    this.charsetName = "windows-1253";
                    break;
                default:
                    this.charsetName = "IBM437";
            }
            
            this.printInfo("File Code: " + fileCode);
            this.printInfo("Year: " + year);
            this.printInfo("Month: " + month);
            this.printInfo("Day: " + day);
            this.printInfo("Nbr Records: " + nbrRecords);
            this.printInfo("Header Size: " + headerSize);
            this.printInfo("Record Size: " + recordSize);
            this.printInfo("Ecrypted: " + encrypted);
            this.printInfo("Codepage: " + codepage);
            
            if (encrypted != 0)
                throw new IOException("Encrypted DBF not supported.");
            
            int nFields = (headerSize - 32) / 32;
            this.readFieldDescriptors(is, nFields);
            
            // create an new table
            String name = ika.utils.FileUtils.getFileNameWithoutExtension(url.getPath());
            Table table = this.initTable(name);
            
            // read the records and fill the table
            for (int i = 0; i < nbrRecords; i++) {
                this.printInfo("Reading Record " + i);
                this.readRecord(is, recordSize, table);
            }
            
            return table;
        } finally {
            if (is != null)
                is.close();
        }
    }
    
    private void readFieldDescriptors(LittleEndianInputStream is, int nFields) 
    throws IOException {
        
        // read description of each field
        byte[] asciiFieldName = new byte[11];
        for (int i = 0; i < nFields; i++) {
            DBFField field = new DBFField();
            is.read(asciiFieldName);
            field.name = ika.utils.StringUtils.bytesToString(
                    asciiFieldName, asciiFieldName.length, this.charsetName);
            field.type = is.readUnsignedByte();
            field.address = is.readInt();
            field.length = is.readUnsignedByte();
            field.decimalCount = is.readUnsignedByte();
            is.readShort();// overread reserved value
            field.workAreaID = is.readUnsignedByte();
            field.multiUserDBase = is.readShort();
            field.setFields = is.readUnsignedByte();
            is.skipBytes(7); // overread 7 reserved bytes
            field.fieldInMDXIndex = is.readUnsignedByte();
            
            this.fields.add(field);
            
            this.printInfo("\n" + field.toString());
        }
        
        // overread the Header Record Terminator, which should be 0x0D
        byte terminator = is.readByte();
        if (terminator != 0x0D) {
            throw new IOException("DBF file is corrupt.");
        }
        
    }
    
    private void readRecord(LittleEndianInputStream is, int recordSize, 
            Table table) throws IOException {
        
        int deletedFlag = is.readUnsignedByte();
        byte[] data = new byte[recordSize];
        
        Vector rowData = new Vector();
        
        java.util.Iterator iterator = this.fields.iterator();
        while (iterator.hasNext()) {
            DBFField field = (DBFField)iterator.next();
            is.read(data, 0, field.length);
            
            switch (field.type) {
                case 'C':   // character string
                    String string = ika.utils.StringUtils.bytesToString(
                            data, field.length, this.charsetName);
                    rowData.add(string.trim());
                    break;
                
                 case 'F':  // floating number
                    try {
                        rowData.add(new Double(new String(data, 0, field.length)));
                    } catch (NumberFormatException exc) {
                        rowData.add(this.DEFAULT_NUMBER);
                    }
                    break;
                
                case 'N':   // number
                    try {
                        rowData.add(new Double(new String(data, 0, field.length)));
                    } catch (NumberFormatException exc) {
                        rowData.add(this.DEFAULT_NUMBER);
                    }
                    break;
                
                case '8':
                case 'O':
                    // little endian 8 byte double. Not tested !!! ???
                    long byte1 = data[0];
                    long byte2 = data[1];
                    long byte3 = data[2];
                    long byte4 = data[3];
                    long byte5 = data[4];
                    long byte6 = data[5];
                    long byte7 = data[6];
                    long byte8 = data[7];
                    long l = (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) +
                            (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
                    rowData.add(new Double(Double.longBitsToDouble(l)));
                    break;
                    
                case '4':
                case 'I':
                    // little endian 4 byte integer. Not tested !!! ???
                    int i = (data[3] << 24) + (data[2] << 16) + (data[1] << 8) + data[0];
                    rowData.add(new Double(i));
                    break;
                
                case '2':
                    // little endian 2 byte integer. Not tested !!! ???
                    rowData.add(new Double((data[1] << 8) + data[0]));
                    break;
                    
                /*    
                case 'D':   // date
                    System.out.println ("Date objects in DBF files not tested.");   // !!! ???
                    
                    // Date in format YYYYMMDD.
                    if (field.length != 8)
                        throw new Exception("Date object has non-standard length in DBF file.");
                    int year = Integer.parseInt(new String(data, 0, 4));
                    int month = Integer.parseInt(new String(data, 4, 2));
                    int day = Integer.parseInt(new String(data, 6, 2));
                    Date date = new Date(year, month, day);
                    rowData.add(date.toString());
                    break;
                    
                case 'L':   // logical
                    //  ?	 Not initialised (default)	 
                    //  Y,y	 Yes
                    //  N,n	 No
                    //  F,f	 False
                    //  T,t	 True
                    final byte b = data[0];
                    if (b == 'Y' || b == 'y' || b == 'T' || b == 't')
                        rowData.add("True");
                    else if (b == 'N' || b == 'n' || b == 'F' || b == 'f')
                        rowData.add("False");
                    else
                        rowData.add("?");
                    break;
                 */
                default:
                    // add the raw bytes as String
                    rowData.add(new String(data, 0, field.length));;
                    
            }
        }
        table.addRow(rowData);
    }
    
    private Table initTable(String name) {
        Table table = new Table(this.charsetName);
        table.setName(name);
        
        java.util.Iterator iterator = this.fields.iterator();
        while (iterator.hasNext()) {
            DBFField field = (DBFField)iterator.next();
            table.addColumn(field.name);
        }
        
        return table;
    }
    
    private class DBFField {
        public String name;
        public int type;
        public int address;
        public int length;
        public int decimalCount;
        public int workAreaID;
        public int multiUserDBase;
        public int setFields;
        public int fieldInMDXIndex;
        
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Name: " + name);
            sb.append(";\tType: " + type);
            switch (type) {
                case 'C': sb.append(" chars"); break;
                case 'D': sb.append(" date"); break;
                case 'F': sb.append(" float"); break;
                case 'N': sb.append(" number"); break;
                case 'L': sb.append(" logical"); break;
                case 'M': sb.append(" memo"); break;
                case 'V': sb.append(" variable"); break;
                case 'P': sb.append(" picture"); break;
                case 'B': sb.append(" binary"); break;
                case 'G': sb.append(" general"); break;
                case '2': sb.append(" 2 byte int"); break;
                case '4': case 'I': sb.append(" 4 byte int"); break;
                case '8': case 'O': sb.append(" double"); break;
                default: sb.append(" unknown field");
            }
            
            sb.append(";\tAddress: " + address);
            sb.append(";\tLength: " + length);
            sb.append(";\tDecimal Count: " + decimalCount);
            sb.append(";\tWork Area ID: " + workAreaID);
            sb.append(";\tMulti User dBase: " + multiUserDBase);
            sb.append(";\tSet Fields: " + setFields);
            sb.append(";\tField .mdx Index: " + fieldInMDXIndex);
            return sb.toString();
        }
    }
}
