/*
 * DnDListModel.java
 *
 * Created on March 1, 2006, 1:51 PM
 *
 */

package ika.gui;

import java.util.*;

/**
 * A ListModel for DraggableList.
 * Originally based on https://www.informit.com/guides/content.asp?g=java&seqNum=58
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class DnDListModel extends javax.swing.DefaultListModel {
    
    public DnDListModel() {
    }

    public DnDListModel(Object[] listData){
        for (int i = 0; i < listData.length; i++) {
            this.addElement(listData[i]);
        }
    }
    
    public DnDListModel(Vector listData) {
        for (int i = 0; i < listData.size(); i++) {
            this.addElement(listData.get(i));
        }
    }
    
    public DnDListModel(javax.swing.ListModel listModel) {
        for (int i = 0; i < listModel.getSize(); i++) {
            this.addElement(listModel.getElementAt(i));
        }
    }
    
    public void itemsMoved( int newIndex, int[] indicies ) {
        // Copy the objects to a temporary ArrayList
        ArrayList objects = new ArrayList();
        for (int i = 0; i < indicies.length; i++) {
            objects.add(this.get(indicies[i]));
        }
        
        // Delete the objects from the list
        for( int i=indicies.length-1; i>=0; i-- ) {
            this.remove( indicies[ i ] );
        }
        
        // Insert the items at the new location
        for( Iterator i = objects.iterator(); i.hasNext(); ) {
            this.insertElementAt(i.next(), newIndex++);
        }
    }
}
