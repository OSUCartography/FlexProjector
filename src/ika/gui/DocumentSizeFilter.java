/**
 * A DocumentFilter to limit the number of characters in a Document used
 * by swing text fields.
 * A 1.4 class used by TextComponentDemo.java.
 * Adapted and changed from:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/example-1dot4/DocumentSizeFilter.java
 */

package ika.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Toolkit;

public class DocumentSizeFilter extends DocumentFilter {
    int maxCharacters;

    public DocumentSizeFilter(int maxChars) {
        maxCharacters = maxChars;
    }

    public void insertString(FilterBypass fb, int offs,
                             String str, AttributeSet a)
        throws BadLocationException {

        //This rejects the entire insertion if it would make
        //the contents too long. Another option would be
        //to truncate the inserted string so the contents
        //would be exactly maxCharacters in length.
        if ((fb.getDocument().getLength() + str.length()) <= maxCharacters)
            super.insertString(fb, offs, str, a);
        else
            Toolkit.getDefaultToolkit().beep();
    }
    
    public void replace(FilterBypass fb, int offs,
                        int length, 
                        String str, AttributeSet a)
        throws BadLocationException {
        
        if (fb == null || str == null)
            return;
        
        //This rejects the entire replacement if it would make
        //the contents too long. Another option would be
        //to truncate the replacement string so the contents
        //would be exactly maxCharacters in length.
        if ((fb.getDocument().getLength() + str.length()
             - length) <= maxCharacters)
            super.replace(fb, offs, length, str, a);
        else
            Toolkit.getDefaultToolkit().beep();
    }

}
