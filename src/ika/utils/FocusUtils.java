/*
 * FocusUtils.java
 *
 * Created on October 15, 2006, 10:16 PM
 *
 */

package ika.utils;

import java.awt.*;
import javax.swing.*;

/**
 * Utility methods for handling the focus of Swing.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class FocusUtils {
    
    /**
     * Returns the window that is currently the focus owner. May be null if no
     * window owns the focus.
     * @return The window that currently owns the focus, or null.
     */
    static public Window focusedWindow() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
    }
    
    /**
     * Returns whether the parent window of a component has the current focus (or
     * any other component in the window).
     * @param component The component for which the parent window is searched.
     * @return True if the parent window currently owns the focus, false otherwise.
     **/
    static public boolean parentWindowHasFocus(JComponent component) {
        Window windowFocusOwner = FocusUtils.focusedWindow();
        if (windowFocusOwner == null)
            return false;
        return component.getTopLevelAncestor() == windowFocusOwner;
    }
    
    /**
     * Returns whether a component listens for a key code, i.e. it has an 
     * action associated with the key code.
     * @param component The component to examine.
     * @param keyCode The key code to test.
     * @return True if the component listens for the key code, false otherwise.
     */
    static public boolean componentListensForKey(JComponent component, int keyCode) {
        KeyStroke[] keyStrokes = component.getInputMap().allKeys();
        if (keyStrokes == null)
            return false;
        for (int i = keyStrokes.length - 1; i >= 0; i--) {
            if (keyCode == keyStrokes[i].getKeyCode()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns whether the component that currently has the focus listens for a
     * specified key code, i.e. it has an action associated with the key code.
     * @param keyCode The key code to test.
     * @return True if the component with the focus listens for the key code,
     * false otherwise.
     */
    static public boolean currentFocusOwnerListensForKey (int keyCode) {
        Component focusOwner = KeyboardFocusManager.
                getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner == null)
            return false;
        
        if (focusOwner instanceof JComponent == false)
            return false;
        
        return FocusUtils.componentListensForKey ((JComponent)focusOwner, keyCode);
    }
    
}