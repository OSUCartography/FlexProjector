/*
 * MenuUtils.java
 *
 * Created on June 16, 2007, 3:17 PM
 *
 */

package ika.gui;

import javax.swing.*;

/**
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class MenuUtils {
    
    /**
     * Adds a menu shortcut (or accelerator) to the input map. This can be used
     * to add multiple shortcuts to a menu item.
     * @uniqueActionName A unique name.
     * @action The action to add.
     * @keyCode Virtual key code, e.g. KeyEvent.VK_L
     * @frame The frome for which to add the action.
     */
    public static void registerMenuShortcut(String uniqueActionName, 
            Action action, int keyCode, JFrame frame) {
        
        JRootPane rootPane = frame.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        int ctrlKey = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        inputMap.put(KeyStroke.getKeyStroke(keyCode, ctrlKey), uniqueActionName);
        actionMap.put(uniqueActionName, action);
        
    }    
}
