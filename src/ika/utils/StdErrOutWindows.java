package ika.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 * Displays standard output and error output in windows.
 * Based on O'Reilly Swing Hacks #95 p. 478
 */
public class StdErrOutWindows extends Object {
    
    JTextArea outArea, errArea;
    
    public StdErrOutWindows(JFrame ownerFrame, String outName, String errName) {
        
        final int OUTDIALOGOFFSET = 15;
        final int ERRDIALOGOFFSET = 30;
        
        // out
        this.outArea = new JTextArea(20, 50);
        JScrollPane pain = new JScrollPane(outArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JDialog outDialog = new JDialog(ownerFrame, outName);
        outDialog.getContentPane().add(pain);
        outDialog.setFocusableWindowState(false);
        outDialog.pack();
        outDialog.setAlwaysOnTop(true);
        outDialog.setLocation(outDialog.getLocation().x + OUTDIALOGOFFSET,
                outDialog.getLocation().y + OUTDIALOGOFFSET);
        
        // err
        this.errArea = new JTextArea(20, 50);
        pain = new JScrollPane(errArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        JDialog errDialog = new JDialog(ownerFrame, errName);
        errDialog.getContentPane().add(pain);
        errDialog.setFocusableWindowState(false);
        errDialog.pack();
        errDialog.setAlwaysOnTop(true);
        errDialog.setLocation(errDialog.getLocation().x + ERRDIALOGOFFSET,
                errDialog.getLocation().y + ERRDIALOGOFFSET);
        
        // set up streams
        System.setOut(new PrintStream(new JTextAreaOutputStream(outArea, System.out)));
        System.setErr(new PrintStream(new JTextAreaOutputStream(errArea, System.err)));
    }
    
    public class JTextAreaOutputStream extends OutputStream {
        JTextArea textArea;
        PrintStream defaultStream;
        
        public JTextAreaOutputStream(JTextArea textArea, PrintStream defaultStream) {
            super();
            this.textArea = textArea;
            this.defaultStream = defaultStream;
        }
        
        public void write(int i) {
            // show the dialog and bring it to the front
            this.textArea.getTopLevelAncestor().setVisible(true);
            
            char[] chars = new char[1];
            chars[0] = (char) i;
            String s = new String(chars);
            
            // write to the JTextArea
            this.textArea.append(s);
            
            // scroll down to the end of the added text
            this.textArea.setCaretPosition(textArea.getDocument().getLength());
            
            // write to the default stream
            this.defaultStream.print(s);
        }
        
        public void write(char[] buf, int off, int len) {
            // show the dialog and bring it to the front
            this.textArea.getTopLevelAncestor().setVisible(true);
            
            String s = new String(buf, off, len);
            
            // write to the JTextArea
            this.textArea.append(s);
            
            // scroll down to the end of the added text
            this.textArea.setCaretPosition(textArea.getDocument().getLength());
            
            // write to the default stream
            this.defaultStream.print(s);
        }
    }
    
}
