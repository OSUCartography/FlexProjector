package ika.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * BorderedDropTarget adds a border to a JComponent when the user drags some
 * data over it. This serves as visual feedback for dragging operations. The
 * border should be drawn when the data can be accepted by the JComponent.
 * Derived classes should overwrite isDataFlavorSupported to customize the
 * appearance of the border. 
 */
public class BorderedDropTarget implements DropTargetListener {
    
    /** The component for which a border will be created. */
    protected JComponent targetComponent;
    
    /** The width of the border that is temporarily displayed. */
    private int borderWidth = 3;
    
    public BorderedDropTarget(JComponent targetComponent)  {
        this.targetComponent = targetComponent;
        
        // set up drop target: attach this drop target to the passed component.
        new DropTarget(this.targetComponent, this);
    }
   
    /**
     * Shows a border. A standard higlighting color is used to draw a LineBorder.
     */
    private void showBorder() {
        Color color = UIManager.getColor("Table.selectionBackground");
        Border newBorder = BorderFactory.createLineBorder(color, this.borderWidth);
        Border oldBorder = this.targetComponent.getBorder();
        Border compoundBorder = BorderFactory.createCompoundBorder(newBorder, oldBorder); 
        this.targetComponent.setBorder(compoundBorder);
    }
    
    /**
     * Hides the border. The currently visible border is replaced by the 
     * previous one.
     */
    private void hideBorder() {
        Border border = this.targetComponent.getBorder();
        if (border instanceof CompoundBorder) {
            CompoundBorder compoundBorder = (CompoundBorder)border;
            this.targetComponent.setBorder(compoundBorder.getInsideBorder());
        } else {
            this.targetComponent.setBorder(null);
        }
    }
    
    /**
     * The mouse entered the component. Show the border if the dragged data
     * can be accepted.
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        
        // there is a bug / limitation in Java DnD:
        // Transferable does not return the transfer data on dragEnter events.
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4378091
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4248542
        // It is therefore impossible to test in dragEnter whether the data can be
        // accepted.
        showBorder();
    }
    
    /**
     * The mouse left the component. Hide the border again.
     */
    public void dragExit(DropTargetEvent dte) {
        hideBorder();
    }
    
    public void dragOver(DropTargetDragEvent dtde) {
    }
    
    /**
     * The dragged data is dropped over the component. Hide the border. 
     * Derived classes should overwrite drop and hide the border by 
     * calling super.drop(dtde);
     */
    public void drop(DropTargetDropEvent dtde) {
        hideBorder();
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    
    /** Getter for the width of the border. */
    public int getBorderWidth() {
        return borderWidth;
    }
    
    /* Setter for the width of the border. */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }
    
}
