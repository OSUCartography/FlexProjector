/*
 * Undo.java
 *
 * Created on August 28, 2005, 10:28 AM
 *
 */

package ika.utils;

import java.util.*;
import javax.swing.*;

/**
 * Undo/Redo manager based on a linked list. The list contains objects that allow
 * for the full reconstruction of the state of the software.
 * 
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class Undo {
    
    /**
     * A linked list holding the various states of the software. The first object
     * holds a basic state to which multiple undo commands will lead. The first
     * object cannot be removed, unless reset() is used.
     * The list contains UndoItem that associate a name with a state.
     */
    private LinkedList list = new LinkedList();
    
    /**
     * Points to the data state that is currently in use.
     */
    private int undoID = -1;
    
    /** Undo menu item that is automatically enabled and disabled and the 
     * displayed text updated.
     */
    private JMenuItem undoMenuItem = null;
    
    /** Redo menu item that is automatically enabled and disabled and the 
     * displayed text updated.
     */
    private JMenuItem redoMenuItem = null;
    
    private class UndoItem {
        public String name;
        public Object obj;
        
        public UndoItem(String name, Object obj) {
            this.name = name;
            this.obj = obj;
        }
    }
    
    /** Creates a new instance of Undo */
    public Undo() {
    }

    /**
     * Returns true if getUndo() will return an object.
     * @return
     */
    public boolean canUndo() {
        return list.size() > 0 && undoID > 0;
    }

    /**
     * Add an entry to the list of undoable states.
     * @param The name as it will appear in the Undo and Redo menus.
     * @param undoItem An object holding all data necessary to undo one step.
     */
    public void add(String name, Object undoItem) {
        
        // cut off all undoItems after undoID
        final int nbrItemsToRemove = list.size() - undoID - 1;
        for (int i = 0; i < nbrItemsToRemove; ++i) {
            list.removeLast();
        }
        
        // add undoItem to list
        list.add(new UndoItem(name, undoItem));
        
        // undoID points at the last item in list.
        undoID = list.size() - 1;
        
        updateMenuItems();
    
    }
    
    /**
     * Remove all undoable states from the list and re-initialize the basic undo
     * object to which multiple undo commands will lead.
     * @param basicUndoItem The basic undo state.
     */
    public void reset(Object basicUndoItem) {
        
        // remove all entries in the undoItems list
        this.list.clear();
        this.undoID = -1;
        
        // add the base state.
        this.add(null, basicUndoItem);
        
    }
    
    /**
     * Register the undo and redo menu item to automatically update their
     * enabled state and the displayed text.
     */
    public void registerUndoMenuItems(JMenuItem undoMenuItem, JMenuItem redoMenuItem) {
        this.undoMenuItem = undoMenuItem;
        this.redoMenuItem = redoMenuItem;
    }
    
    /**
     * Update the enabled state and the text displayed by the undo and redo
     * menu item.
     */
    private void updateMenuItems() {
        final int nbrItems = list.size();
        
        // update undo menu
        if (this.undoMenuItem != null) {
            final boolean canUndo= nbrItems > 0  && this.undoID > 0;
            this.undoMenuItem.setEnabled(canUndo);
            String str = "Undo";
            if (canUndo) {
                UndoItem undoItem = (UndoItem)list.get(this.undoID);
                if (undoItem != null && undoItem.name != null) {
                    str += " " + undoItem.name;
                }
            }
            this.undoMenuItem.setText(str);
        }
        
        // update redo menu
        if (this.redoMenuItem != null) {
            final boolean canRedo = nbrItems > 0  && this.undoID < nbrItems - 1;
            this.redoMenuItem.setEnabled(canRedo);
            String str = "Redo";
            if (canRedo) {
               UndoItem redoItem = (UndoItem)list.get(this.undoID + 1);
               if (redoItem != null && redoItem.name != null) {
                    str += " " + redoItem.name;
                }
            }
            this.redoMenuItem.setText(str);
        }
    }
    
    /**
     * Returns the current undo state object, which is one position before the
     * current ID.
     */
    public Object getUndo() {
        if (list.size() > 0 && undoID > 0) {
            UndoItem undoItem = (UndoItem)list.get(--undoID);
            updateMenuItems();
            return undoItem.obj;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the current redo state object, which is one position after the
     * current ID.
     */
    public Object getRedo() {
        if (list.size() > 0
                && undoID >= -1
                && undoID < list.size() - 1) {
            UndoItem undoItem = (UndoItem)list.get(++undoID);
            this.updateMenuItems();
            return undoItem.obj;
        } else {
            return null;
        }
    }
    
    private String toString(UndoItem undoItem) {
        if (undoItem == null) {
            return "";
        }
        
        String str = " Name : " + undoItem.name;
        str += " Value : " + undoItem.obj;
        return str;
    }
    
    public String toString(Object obj) {
        if (obj != null) {
            return "Value : " + obj.toString();
        }
        return "";
    }
    
    @Override
    public String toString() {
        final String newline = System.getProperty("line.separator");
        StringBuilder str = new StringBuilder();
        str.append("Undo ID: ").append(undoID).append(newline);
        for (int i = 0; i < list.size(); ++i) {
            str.append("#").append(i).append(newline);
            str.append(toString((UndoItem)list.get(i)));
            str.append(newline);
        }
        return str.toString();
    }
    
}
