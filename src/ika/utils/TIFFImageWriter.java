package ika.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes TIFF rgb images. Can handle large images that do not fit into 
 * available memory.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class TIFFImageWriter extends ImageWriter {

    private static final short kTiffTypeUShort = 3;
    private static final short kTiffTypeULong = 4;
    
    // tag IDs
    private static final short tagImageWidth = 256;
    private static final short tagImageLength = 257;
    private static final short tagBitsPerSample = 258;
    private static final short tagCompression = 259;
    private static final short tagPhotometricInterpretation = 262;
    private static final short tagStripOffsets = 273;
    private static final short tagSamplesPerPixel = 277;
    private static final short tagRowsPerStrip = 278;
    private static final short tagStripByteCounts = 279;
    private static final short tagXResolution = 282;
    private static final short tagYResolution = 283;
    private static final short tagResolutionUnit = 296;
    private static final short tagExtraSamples = 338;

    /** Creates a new instance of TIFFWriter and writes the header
    of the file.
     */
    public TIFFImageWriter(OutputStream out, int cols, int rows)
            throws java.io.IOException {
        
        super(out, cols, rows);
        
    }

    @Override
    public void writeRGB(int r, int g, int b) throws IOException {
        
        this.out.write(r);
        this.out.write(g);
        this.out.write(b);
        this.out.write(255);
        
    }

    /**
     * Write an rgba value to the file. The r, g, and b values must be
     * premultiplied by the a value.
     * @param r Red in the range [0..255]
     * @param g Green in the range [0..255]
     * @param b Blue in the range [0..255]
     * @param a Alpha in the range [0..255]
     * @throws java.io.IOException
     */
    @Override
    protected void writeRGB(int r, int g, int b, int a) throws java.io.IOException {
        
        // premultiplied rgb values must be smaller than a
        assert r <= a && g <= a && b <= a;
        
        this.out.write(r);
        this.out.write(g);
        this.out.write(b);
        this.out.write(a);
        
    }
    
    /**
     * Write an argb value to the file. The r, g, and b values must be
     * premultiplied by the a value.
     * @param color An rgba value packed in an integer
     * @throws java.io.IOException
     */
    @Override
    public void write(int argb) throws java.io.IOException {

        // write single bytes, which is not slower than writing ints to a DataOutputStream.
        out.write((argb >> 16) & 0xff);
        out.write((argb >> 8) & 0xff);
        out.write(argb & 0xff);
        out.write((argb >> 24) & 0xff);

    }
    
    @Override
    protected void writeHeader() throws IOException {
    
        DataOutputStream dout = new DataOutputStream(out);
        
        // the number of directory entries
        final short TAG_COUNT = 13;
        
        // write 4 bytes per pixel: rgba
        final int CHANNEL_COUNT = 4;
        
        final int kFileHeaderLength = 8;
        final int kDirectoryHeaderLength = 2;
        final int kDirEntryLength = 12;
        final int kDirectoryFooterLength = 4;
        int dataSectionPos = kFileHeaderLength +
                kDirectoryHeaderLength +
                kDirEntryLength * TAG_COUNT +
                kDirectoryFooterLength;
        
        // write file header
        dout.write('M');
        dout.write('M');
        dout.write((byte)0);
        dout.write('*');
        dout.writeInt(8); // start of first (and only) IFD (image file directory)

        // write directory header
        dout.writeShort(TAG_COUNT);

        // 1. image width: cols
        this.write4ByteTag(this.cols, tagImageWidth, dout);

        // 2. image length: rows
        write4ByteTag(this.rows, tagImageLength, dout);

        // 3. bits per sample
        writeOffsetTag(dataSectionPos, CHANNEL_COUNT, tagBitsPerSample, false, dout);
        dataSectionPos += CHANNEL_COUNT * 2;

        // 4. compression
        write2ByteTag((short)1, tagCompression, dout);	// no compression

        // 5. photometric interpretation: the color space of the image data
        write2ByteTag((short)2, tagPhotometricInterpretation, dout);

        // 6. samples per pixel
        write2ByteTag((short)CHANNEL_COUNT, tagSamplesPerPixel, dout);

        // 7. rows per strip
        write4ByteTag(this.rows, tagRowsPerStrip, dout);

        // 8. strip byte counts
        write4ByteTag(this.cols * this.rows * CHANNEL_COUNT, tagStripByteCounts, dout);

        // 9. resolution in x direction
        writeOffsetTag(dataSectionPos, 2, tagXResolution, true, dout);
        dataSectionPos += 2 * 4;
		
	// 10 resolution in y direction
	writeOffsetTag (dataSectionPos, 2, tagYResolution, true, dout);
        dataSectionPos += 2 * 4;
		
	// 11 resolution unit
	write2ByteTag ((short)2, tagResolutionUnit, dout);	// inch
        
        // 12 extra samples are transparency
        write2ByteTag ((short)1, tagExtraSamples, dout);
        
        // 13 strip offsets: write the offset to the pixels after dataSectionPos
        // is updated above
        write4ByteTag(dataSectionPos, tagStripOffsets, dout);
        
        // Directory footer: end of last (and only) IDF
        dout.writeInt(0);

        // write data for tagBitsPerSample
        for (int i = 0; i < CHANNEL_COUNT; i++)
            dout.writeShort((short)8);
        
        // resolution in x and y direction
        dout.writeInt(144);
        dout.writeInt(1);
        dout.writeInt(144);
        dout.writeInt(1);
        
        // here follow the pixel values

    }

    /**
     * writes a tiff tag consisting of four bytes
     */
    void write4ByteTag(int i, short tagID, DataOutputStream dout) 
            throws java.io.IOException {
        
        dout.writeShort(tagID);
        dout.writeShort(kTiffTypeULong);
        dout.writeInt(1);
        dout.writeInt(i);
        
    }

    /**
     * writes a tiff tag consisting of two bytes
     */ 
    void write2ByteTag(short s, short tagID, DataOutputStream dout) 
            throws java.io.IOException {
        
        dout.writeShort(tagID);
        dout.writeShort(kTiffTypeUShort);
        dout.writeInt(1);
        dout.writeShort(s);
        dout.writeShort(0);	// filler
        
    }

    /**
     * writes a tiff tag consisting of an offset to its data
     */
    void writeOffsetTag(int dataOffset, int nbrValues, short tagID,
            boolean writeLong, DataOutputStream dout) throws java.io.IOException {
        
        dout.writeShort(tagID);
        if (writeLong) {
            dout.writeShort(kTiffTypeULong);
        } else {
            dout.writeShort(kTiffTypeUShort);
        }
        dout.writeInt(nbrValues);
        
        // data offset must be even number
        assert dataOffset % 2 == 0;
        
        dout.writeInt(dataOffset);
        
    }

}
