/*
 * CheckBoxCellRenderer.java
 *
 * Created on March 6, 2006, 11:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ika.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author jenny
 */
public class CheckBoxCellRenderer implements ListCellRenderer {
    
    /** Creates a new instance of CheckBoxCellRenderer */
    public CheckBoxCellRenderer() {
    }
    
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        JCheckBox checkbox = new JCheckBox(value.toString());
        checkbox.setBackground(Color.WHITE);
        checkbox.setSelected(isSelected);
        checkbox.setEnabled(true);
        checkbox.setFocusPainted(false);
        return checkbox;
    }
}