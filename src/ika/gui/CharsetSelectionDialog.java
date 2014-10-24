/*
 * CharsetSelectionDialog.java
 *
 * Created on February 26, 2007, 2:17 PM
 *
 */

package ika.gui;

import ika.utils.ApplicationIcon;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A modal dialog to pick a character set encoding.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class CharsetSelectionDialog {
    
     private CharsetSelectionDialog(){
     }
     
    /**
     * Ask the user to pick a character set encoding from a list containing
     * all availabe character sets installed on this VM. Displays a modal dialog.
     * @initialCharsetName The name of the current character set. The character
     * set with this name is selected in the user interface. Passing null is
     * possible.
     * @tableName The name of the table. Can be null.
     * @frame The parent frame for the dialog.
     * @return The name of the new character set selected by the user, or null if
     * the user cancels the selection process.
     */
    static public String askCharset (String initialCharsetName, 
            String tableName, JFrame frame) {
        // get all available character sets
        java.util.SortedMap charsets = Charset.availableCharsets();
        
        // search the current character set
        Charset initialCharset = null;
        if (initialCharsetName != null && Charset.isSupported(initialCharsetName))
            initialCharset = Charset.forName(initialCharsetName);
        
        String msg;
        if (tableName != null)
            msg = "Select a new encoding for the table\" " + tableName + "\":";
        else
            msg = "Select a new encoding:";
        
        // display the dialog
        Charset newCharset = (Charset)JOptionPane.showInputDialog(
                frame, 
                msg, 
                "Character Encoding",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                charsets.values().toArray(),
                initialCharset);
        return newCharset == null ? null : newCharset.name();
        
    }
     
}
