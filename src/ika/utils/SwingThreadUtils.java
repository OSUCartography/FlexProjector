/*
 * SwingThreadUtils.java
 *
 * Created on December 3, 2006, 6:01 PM
 *
 */

package ika.utils;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class SwingThreadUtils {
    
    /**
     * A wrapper around SwingUtilities.invokeAndWait() that makes sure that 
     * SwingUtilities.invokeAndWait() is only called when the current thread is 
     * not the AWT event dispatching thread, as required by the documentation
     * of SwingUtilities.invokeAndWait(); plus catches exceptions thrown by 
     * SwingUtilities.invokeAndWait().
     * @param runnable The Runnable to call in the event dispatch thread.
     */
    public static void invokeAndWait(Runnable runnable) {
        try {
            if (SwingUtilities.isEventDispatchThread())
                runnable.run();
            else
                SwingUtilities.invokeAndWait(runnable);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
