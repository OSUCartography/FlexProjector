package ika.proj;

/**
 * Methods for serializing and de-serializing designed projections to a string.
 * @author jenny
 */
public interface SerializableProjection {

    public String serializeToString();
    public void deserializeFromString(String str);
    
}
