package ika.utils;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author jenny
 */
public class TextWindow {
    
    public static void showSimpleTextDialog(String text, String title, 
            java.awt.Frame ownerFrame, int tabSize) {
        if (text == null || text.length() == 0)
            return;
        
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setFont(new Font ("Monospaced", Font.PLAIN, 13));
        textArea.setTabSize(tabSize);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JDialog dialog = new JDialog(ownerFrame);
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setContentPane(scrollPane);
        dialog.setLocation(50, 50);
        dialog.setSize(800, 600);
        dialog.setVisible(true);
    }
    
    public static void showSimpleTextWindow(String text) {
        if (text == null || text.length() == 0)
            return;
        
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(scrollPane);
        frame.setLocation(50, 50);
        frame.setSize(500, 600);
        frame.setVisible(true);
    }
    
    private JDialog dialog;
    private JTextArea textArea;
    
    public TextWindow (java.awt.Frame ownerFrame, boolean modal, 
            boolean initiallyVisible,
            String text, String title) {
        this.textArea = new JTextArea();
        this.textArea.setText(text);
        this.textArea.setFont(new Font ("Monospaced", Font.PLAIN, 13));
        this.textArea.setTabSize(16);
        this.textArea.setEditable(false);
        this.textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(this.textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.dialog = new JDialog(ownerFrame);
        dialog.setTitle(title);
        dialog.setModal(modal);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setContentPane(scrollPane);
        dialog.setLocation(50, 50);
        dialog.setSize(800, 600);
        if (initiallyVisible)
            dialog.setVisible(true);
    }
    
    public void show() {
        this.dialog.setVisible(true);
    }
    
    public void hide() {
        this.dialog.setVisible(false);
    }
        
    public String getText() {
        return this.textArea.getText();
    }

    public void setText(String text) {
        this.textArea.setText(text);
        this.textArea.setCaretPosition(0);
    }

    public String getTitle() {
        return dialog.getTitle();
    }

    public void setTitle(String title) {
        dialog.setTitle(title);
    }
    
    public void setTabSize (int tabSize) {
        this.textArea.setTabSize(tabSize);
    }
}
