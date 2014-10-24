/*
 * Serializer.java
 *
 * Created on March 15, 2007, 10:12 AM
 *
 */

package ika.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * A collection of static methods for the serialization and deserialization
 * of arbitrary objects.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class Serializer {
    
    /**
     * Serializes an object and optionally zips the resulting bytes.
     * @param obj The object to serialize.
     * @zip If true, the serialized object is zipped.
     * @return A byte array with the serialized object.
     */
    public static byte[] serialize(Object obj, boolean zip)
    throws java.io.IOException {
        
        if (obj == null)
            return null;
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream os = bos;
        
        if (zip)
            os = new java.util.zip.GZIPOutputStream(os);
        
        os = new BufferedOutputStream(os);
        ObjectOutputStream oos = new ObjectOutputStream(os);
        
        // implementing void writeObject(ObjectOutputSteram out) and
        // calling it throws an exception:
        // java.io.NotActiveException: not in call to writeObject
        oos.writeObject(obj);
        
        // must close, flush is not enough!
        oos.close();
        
        return bos.toByteArray();
    }
    
    /**
     * Serializes an object to a stream. The stream is not closed after
     * serialization.
     * @param obj The object to serialize.
     * @zip If true, the serialized object is zipped.
     * @outputStream The destination stream.
     */
    public static void serialize(Object obj, boolean zip, OutputStream outputStream)
    throws java.io.IOException {
        byte[] b = Serializer.serialize(obj, zip);
        outputStream.write(b);
    }
    
    /**
     * Serializes an object to a file.
     * @param obj The object to serialize.
     * @zip If true, the serialized object is zipped.
     * @filePath The path to the file that will contain the serialized object.
     */
    public static void serialize(Object obj, boolean zip, String filePath)
    throws java.io.IOException {
        OutputStream outputStream = null;
        try {
            byte[] b = Serializer.serialize(obj, zip);
            outputStream = new FileOutputStream(filePath);
            outputStream = new BufferedOutputStream(outputStream);
            outputStream.write(b);
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
    }
    
    /**
     * Deserializes an object and returns it.
     * @param b The byte array to deserialize.
     * @zip If true, the byte array is zipped.
     * @return The deserialized object.
     */
    public static Object deserialize(byte[] b, boolean zip)
    throws java.io.IOException, ClassNotFoundException{
        
        if (b == null || b.length == 0)
            return null;
        
        ObjectInputStream ois = null;
        try {
            // deserialize object
            InputStream is = new ByteArrayInputStream(b);
            if (zip)
                is = new java.util.zip.GZIPInputStream(is);
            is = new BufferedInputStream(is);
            ois = new ObjectInputStream(is);
            return ois.readObject();
        } finally {
            if (ois != null)
                ois.close();
        }
    }
    
    /**
     * Deserializes an object from an InputStream and returns it.
     * @param inputStream The stream that provides the serialized object.
     * @zip If true, the byte array is zipped.
     * @return The deserialized object.
     */
    public static Object deserialize(InputStream inputStream, boolean zip)
    throws java.io.IOException, ClassNotFoundException{
        if (zip)
            inputStream = new java.util.zip.GZIPInputStream(inputStream);
        ObjectInputStream objectStream = new ObjectInputStream(inputStream);
        return objectStream.readObject();
    }
    
    /**
     * Deserializes an object from a file and returns it.
     * @filePath The path to the file that contains the serialized object.
     * @zip If true, the byte array is zipped.
     * @return The deserialized object.
     */
    public static Object deserialize(String filePath, boolean zip)
    throws java.io.IOException, ClassNotFoundException{
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            inputStream = new BufferedInputStream(inputStream);
            return Serializer.deserialize(inputStream, zip);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }
}
