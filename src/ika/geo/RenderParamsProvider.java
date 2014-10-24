/*
 * RenderParamsProvider.java
 *
 * Created on December 17, 2006, 6:41 PM
 *
 */

package ika.geo;

/**
 * A RenderParamsProvider can customize the rendering parameters used by a 
 * MapComponent.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public interface RenderParamsProvider {
    
    /**
     * Customize the passed defaultRenderParams. These parameters are used
     * to render a map. The customized RenderParams are returned from this 
     * method. A new object of a class derived from RenderParams can be returned.
     */
    public RenderParams getRenderParams(RenderParams defaultRenderParams);
}
