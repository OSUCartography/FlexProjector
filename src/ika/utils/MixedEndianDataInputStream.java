/*
 * MixedEndianDataInputStream.java
 *
 * Created on January 31, 2007, 9:54 PM
 *
 */

package ika.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * MixedEndianDataInputStream can be used to read little endian and big endian data
 * from one single stream.
 * Based on O'Reilly's "Java I/O" second edition, chapter 8, page 137,
 * LittleEndianInputStream class.
 * 
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class MixedEndianDataInputStream extends DataInputStream {
    
    /**
     * Creates a new instance of MixedEndianDataInputStream
     */
    public MixedEndianDataInputStream(InputStream i) {
        
        super(i);
        
    }
    
    /**
     * Reads a two byte signed <code>short</code> from the underlying
     * input stream in little endian order, low byte first.
     *
     * @return     the <code>short</code> read.
     * @exception  EOFException  if the end of the underlying input stream
     *              has been reached
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public short readLittleEndianShort() throws IOException {
        
        int byte1 = in.read();
        int byte2 = in.read();
        // only need to test last byte read
        // if byte1 is -1 so is byte2
        if (byte2 == -1) throw new EOFException();
        return (short) ((byte2 << 8) + byte1);
        
    }
    
    /**
     * Reads a two byte unsigned <code>short</code> from the underlying
     * input stream in little endian order, low byte first.
     *
     * @return     the int value of the unsigned short read.
     * @exception  EOFException  if the end of the underlying input stream
     *              has been reached
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public int readLittleEndianUnsignedShort() throws IOException {
        
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) throw new EOFException();
        return (byte2 << 8) + byte1;
        
    }
    
    /**
     * Reads a two byte Unicode <code>char</code> from the underlying
     * input stream in little endian order, low byte first.
     *
     * @return     the int value of the unsigned short read.
     * @exception  EOFException  if the end of the underlying input stream
     *              has been reached
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public char readLittleEndianChar() throws IOException {
        
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) throw new EOFException();
        return (char) ((byte2 << 8) + byte1);
        
    }
    
    
    /**
     * Reads a four byte signed <code>int</code> from the underlying
     * input stream in little endian order, low byte first.
     *
     * @return     the <code>int</code> read.
     * @exception  EOFException  if the end of the underlying input stream
     *              has been reached
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public int readLittleEndianInt() throws IOException {
        
        int byte1, byte2, byte3, byte4;
        
        byte1 = in.read();
        byte2 = in.read();
        byte3 = in.read();
        byte4 = in.read();
        if (byte4 == -1) {
            throw new EOFException();
        }
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
        
    }
    
    /**
     * Reads an eight byte signed <code>int</code> from the underlying
     * input stream in little endian order, low byte first.
     *
     * @return     the <code>int</code> read.
     * @exception  EOFException  if the end of the underlying input stream
     *              has been reached
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public long readLittleEndianLong() throws IOException {
        
        long byte1 = in.read();
        long byte2 = in.read();
        long byte3 = in.read();
        long byte4 = in.read();
        long byte5 = in.read();
        long byte6 = in.read();
        long byte7 = in.read();
        long byte8 = in.read();
        if (byte8 == -1) {
            throw new EOFException();
        }
        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) +
                (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
        
    }
    
    /**
     * Reads a string of no more than 65,535 characters
     * from the underlying input stream using UTF-8
     * encoding. This method first reads a two byte short
     * in <b>big</b> endian order as required by the
     * UTF-8 specification. This gives the number of bytes in
     * the UTF-8 encoded version of the string.
     * Next this many bytes are read and decoded as UTF-8
     * encoded characters.
     *
     * @return     the decoded string
     * @exception  UTFDataFormatException if the string cannot be decoded
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public String readLittleEndianUTF() throws IOException {
        
        
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) throw new EOFException();
        int numbytes = (byte1 << 8) + byte2;
        
        char result[] = new char[numbytes];
        int numread = 0;
        int numchars = 0;
        
        while (numread < numbytes) {
            
            int c1 = readUnsignedByte();
            
            int c2, c3;
            
            // look at the first four bits of c1 to determine how many
            // bytes in this char
            int test = c1 >> 4;
            if (test < 8) {  // one byte
                numread++;
                result[numchars++] = (char) c1;
            } else if (test == 12 || test == 13) { // two bytes
                numread += 2;
                if (numread > numbytes) throw new UTFDataFormatException();
                c2 = readUnsignedByte();
                if ((c2 & 0xC0) != 0x80) throw new UTFDataFormatException();
                result[numchars++] = (char) (((c1 & 0x1F) << 6) | (c2 & 0x3F));
            } else if (test == 14) { // three bytes
                numread += 3;
                if (numread > numbytes) throw new UTFDataFormatException();
                c2 = readUnsignedByte();
                c3 = readUnsignedByte();
                if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                    throw new UTFDataFormatException();
                }
                result[numchars++] = (char)
                (((c1 & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
            } else { // malformed
                throw new UTFDataFormatException();
            }
            
        }  // end while
        
        return new String(result, 0, numchars);
        
    }
    
    /**
     *
     * @return     the next eight bytes of this input stream, interpreted as a
     *             little endian <code>double</code>.
     * @exception  EOFException if end of stream occurs before eight bytes
     *             have been read.
     * @exception  IOException   if an I/O error occurs.
     */
    public final double readLittleEndianDouble() throws IOException {
        
        return Double.longBitsToDouble(this.readLittleEndianLong());
        
    }
    
    /**
     *
     * @return     the next four bytes of this input stream, interpreted as a
     *             little endian <code>int</code>.
     * @exception  EOFException if end of stream occurs before four bytes
     *             have been read.
     * @exception  IOException  if an I/O error occurs.
     */
    public final float readLittleEndianFloat() throws IOException {
        
        return Float.intBitsToFloat(this.readLittleEndianInt());
        
    }
    
}
