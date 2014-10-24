/*
 * 
 * 
 */

package ika.geo.grid;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface GridMask {

    public float getWeight(int col, int row, int pyramidLevel);
    
}
