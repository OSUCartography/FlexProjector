/*
 * MixedEndianDataOutputStream.java
 *
 * Created on March 28, 2007, 10:10 PM
 *
 */

package ika.utils;

import java.io.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class MixedEndianDataOutputStream extends DataOutputStream {
    
    /**
     * Creates a new mixed endian output stream and chains it to the
     * output stream specified by the out argument.
     *
     * @param   out   the underlying output stream.
     * @see     java.io.FilterOutputStream#out
     */
    public MixedEndianDataOutputStream(OutputStream out) {
        super(out);
    }
    
    /**
     * Writes a two byte <code>short</code> to the underlying output stream in
     * little endian order, low byte first.
     *
     * @param      s   the <code>short</code> to be written.
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public void writeLittleEndianShort(int s) throws IOException {
        
        out.write(s & 0xFF);
        out.write((s >>> 8) & 0xFF);
        written += 2;
        
    }
    
    /**
     * Writes a two byte <code>char</code> to the underlying output stream
     * in little endian order, low byte first.
     *
     * @param      c   the <code>char</code> value to be written.
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public void writeLittleEndianChar(int c) throws IOException {
        
        out.write(c & 0xFF);
        out.write((c >>> 8) & 0xFF);
        written += 2;
        
    }
    
    /**
     * Writes a four-byte <code>int</code> to the underlying output stream
     * in little endian order, low byte first, high byte last
     *
     * @param      i   the <code>int</code> to be written.
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public void writeLittleEndianInt(int i) throws IOException {
        
        out.write(i & 0xFF);
        out.write((i >>> 8) & 0xFF);
        out.write((i >>> 16) & 0xFF);
        out.write((i >>> 24) & 0xFF);
        written += 4;
        
    }
    
    /**
     * Writes an eight-byte <code>long</code> to the underlying output stream
     * in little endian order, low byte first, high byte last
     *
     * @param      l   the <code>long</code> to be written.
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public void writeLittleEndianLong(long l) throws IOException {
        
        out.write((int) l & 0xFF);
        out.write((int) (l >>> 8) & 0xFF);
        out.write((int) (l >>> 16) & 0xFF);
        out.write((int) (l >>> 24) & 0xFF);
        out.write((int) (l >>> 32) & 0xFF);
        out.write((int) (l >>> 40) & 0xFF);
        out.write((int) (l >>> 48) & 0xFF);
        out.write((int) (l >>> 56) & 0xFF);
        written += 8;
        
    }
    
    /**
     * Writes a 4 byte Java float to the underlying output stream in
     * little endian order.
     *
     * @param      f   the <code>float</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeLittleEndianFloat(float f) throws IOException {
        
        this.writeLittleEndianInt(Float.floatToIntBits(f));
        
    }
    
    /**
     * Writes an 8 byte Java double to the underlying output stream in
     * little endian order.
     *
     * @param      d   the <code>double</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeLittleEndianDouble(double d) throws IOException {
        
        this.writeLittleEndianLong(Double.doubleToLongBits(d));
         
    }
    
    /**
     * Writes a string to the underlying output stream as a sequence of
     * characters. Each character is written to the data output stream as
     * if by the <code>writeChar</code> method.
     *
     * @param      s   a <code>String</code> value to be written.
     * @exception  IOException  if the underlying stream throws an IOException.
     * @see        java.io.LittleEndianOutputStream#writeChar(int)
     * @see        java.io.LittleEndianOutputStream#out
     */
    public void writeLittleEndianChars(String s) throws IOException {
        
        int length = s.length();
        for (int i = 0; i < length; i++) {
            int c = s.charAt(i);
            out.write(c & 0xFF);
            out.write((c >>> 8) & 0xFF);
        }
        written += length * 2;
        
    }
    
    /**
     * Writes a string of no more than 65,535 characters
     * to the underlying output stream using UTF-8
     * encoding. This method first writes a two byte short
     * in <b>big</b> endian order as required by the
     * UTF-8 specification. This gives the number of bytes in the
     * UTF-8 encoded version of the string, not the number of characters
     * in the string. Next each character of the string is written
     * using the UTF-8 encoding for the character.
     *
     * @param      s   the string to be written.
     * @exception  UTFDataFormatException if the string is longer than
     *             65,535 characters.
     * @exception  IOException  if the underlying stream throws an IOException.
     */
    public void writeLittleEndianUTF(String s) throws IOException {
        
        int numchars = s.length();
        int numbytes = 0;
        
        for (int i = 0 ; i < numchars ; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) numbytes++;
            else if (c > 0x07FF) numbytes += 3;
            else numbytes += 2;
        }
        
        if (numbytes > 65535) throw new UTFDataFormatException();
        
        out.write((numbytes >>> 8) & 0xFF);
        out.write(numbytes & 0xFF);
        for (int i = 0 ; i < numchars ; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.write(c);
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >>  6) & 0x3F));
                out.write(0x80 | (c & 0x3F));
                written += 2;
            } else {
                out.write(0xC0 | ((c >>  6) & 0x1F));
                out.write(0x80 | (c & 0x3F));
                written += 1;
            }
        }
        
        written += numchars + 2;
        
    }
    
}
