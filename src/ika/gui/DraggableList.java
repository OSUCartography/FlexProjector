/*
 * DraggableList.java
 *
 * Created on March 1, 2006, 1:47 PM
 */

package ika.gui;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * A JList that allows the list items to be re-ordered by drag and drop.
 * Based on https://www.informit.com/guides/content.asp?g=java&seqNum=58
 * With extensions to support not only Strings as list objects, but any class.
 * Only dragging within the same list is supported.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class DraggableList extends JList implements DragSourceListener,
        DragGestureListener,
        DropTargetListener, Serializable {
    
    /*
    public static void main(String [] ars) {
        JFrame frame = new JFrame();
        Object[] v = new Object[]{"pipo", "gaga", "toto", "ginio"};
        DraggableList dndList = new DraggableList(v);
        frame.getContentPane().add(dndList);
        frame.pack();
        frame.show();
    }
    */
    
    private DragSource dragSource;
    private DropTarget dropTarget;
    
    /** set flag while we are dragging
     */
    private boolean dragging = false;
    
    /** List index of the item that we are currently dragging over
     **/
    private int overIndex = -1;
    
    /** Remember the selected list items while dragging.
     */
    private int[] selectedIndicies;
    
    /** Name of the property changed event that is sent after the model
     * has changed due to a dragging operation.*/
    public static final String MODEL_PROPERTY = "modelProperty";
    
    public DraggableList( DnDListModel model ) {
        super( model );
        this.init();
    }
    
    public DraggableList(DefaultListModel defaultListModel) {
        super (new DnDListModel(defaultListModel));
        this.init();
    }
    
    public DraggableList(Object[] listData){
        super (new DnDListModel(listData));
        this.init();
    }
    
    public DraggableList(Vector listData) {
        super (new DnDListModel(listData));
        this.init();
    }
    
    public DraggableList() {
        super(new DnDListModel());
        this.init();
    }
    
    // make sure the DnDListModel is not replaced by another type of ListModel
    @Override
    public void setModel(ListModel model) {
        if (model instanceof DnDListModel == false) {
            model = new DnDListModel(model);
        }
        super.setModel(model);
    }
    
    // make sure the DnDListModel is not replaced by another type of ListModel
    @Override
    public void setListData(Object[] listData) {
        this.setModel(new DnDListModel(listData));
    }
    
    // make sure the DnDListModel is not replaced by another type of ListModel
    @Override
    public void setListData(Vector listData) {
        this.setModel(new DnDListModel(listData));
    }
    
    private void init() {
        // Configure to be a drag source
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer( this,
                DnDConstants.ACTION_MOVE, this);
        
        // Configure to be a drop target
        dropTarget = new DropTarget( this, this );
    }
    
    public void dragGestureRecognized(DragGestureEvent dge) {
        this.selectedIndicies = this.getSelectedIndices();
        Object[] selectedObjects = this.getSelectedValues();
        if( selectedObjects.length > 0 ) {
            
            /* Original code
            StringBuffer sb = new StringBuffer();
            for( int i=0; i<selectedObjects.length; i++ ) {
                sb.append( selectedObjects[ i ].toString() + System.getProperty("line.separator") );
            }
             
            // Build a StringSelection object that the Drag Source
            // can use to transport a string to the Drop Target
            StringSelection text = new StringSelection( sb.toString() );
             
            // Start dragging the object
            this.dragging = true;
            dragSource.startDrag( dge, DragSource.DefaultMoveDrop, text, this );
             */
            
            // Start dragging the object
            this.dragging = true;
            this.dragSource.startDrag( dge, DragSource.DefaultMoveDrop,
                    new ObjTransfer(Arrays.asList(selectedObjects)), this );
        }
    }
    
    public void dragDropEnd(DragSourceDropEvent dsde) {
        this.dragging = false;
    }
    
    public void dragExit(DropTargetEvent dte) {
        this.overIndex = -1;
    }
    public void dragEnter(DropTargetDragEvent dtde) {
        this.overIndex = this.locationToIndex( dtde.getLocation() );
        this.setSelectedIndex( this.overIndex );
    }
    public void dragOver(DropTargetDragEvent dtde) {
        // See who we are over...
        int overIndex = this.locationToIndex( dtde.getLocation() );
        if( overIndex != -1 && overIndex != this.overIndex ) {
            // If the value has changed from what we were previously over
            // then change the selected object to the one we are over; this
            // is a visual representation that this is where the drop will occur
            this.overIndex = overIndex;
            this.setSelectedIndex( this.overIndex );
        }
    }
    
    
    public void drop(DropTargetDropEvent dtde) {
        try {
            
            /* Original code
            Transferable transferable = dtde.getTransferable();
            if( transferable.isDataFlavorSupported( DataFlavor.stringFlavor ) ) {
                dtde.acceptDrop( DnDConstants.ACTION_MOVE );
             
                // Find out where the item was dropped
                int newIndex = this.locationToIndex( dtde.getLocation() );
             
                // Get the items out of the transferable object and build an
                // array out of them...
                String s = ( String )transferable.getTransferData( DataFlavor.stringFlavor );
                StringTokenizer st = new StringTokenizer( s );
                ArrayList items = new ArrayList();
                while( st.hasMoreTokens() ) {
                    items.add( st.nextToken() );
                }
                DnDListModel model = ( DnDListModel )this.getModel();
             
                // If we are dragging from our this to our list them move the items,
                // otherwise just add them...
                if( this.dragging ) {
                    //model.itemsMoved( newIndex, items );
                    model.itemsMoved( newIndex, this.selectedIndicies );
                } else {
                    //                   model.insertItems( newIndex, items );
                }
             
                // Update the selected indicies
                int[] newIndicies = new int[ items.size() ];
                for( int i=0; i<items.size(); i++ ) {
                    newIndicies[ i ] = newIndex + i;
                }
                this.setSelectedIndices( newIndicies );
             
                // Reset the over index
                this.overIndex = -1;
             
                dtde.getDropTargetContext().dropComplete( true );
            } else {
                dtde.rejectDrop();
            }
             **/
            
            // only drag from this DraggableList accepted
            if (!this.dragging) {
                dtde.rejectDrop();
                return;
            }
            
            DataFlavor dataFlavor = getDefaultTransferDataFlavor();
            Transferable transferable = dtde.getTransferable();
            if( transferable.isDataFlavorSupported(dataFlavor) ) {
                dtde.acceptDrop( DnDConstants.ACTION_MOVE );
                
                // Find out where the item was dropped
                int newIndex = this.locationToIndex( dtde.getLocation() );
                
                // move the items in the list
                DnDListModel model = ( DnDListModel )this.getModel();
                model.itemsMoved( newIndex, this.selectedIndicies );
                
                // Update the selected indicies
                List list = (List)transferable.getTransferData(dataFlavor);
                int[] newIndicies = new int[ list.size() ];
                for( int i=0; i<list.size(); i++ ) {
                    newIndicies[ i ] = newIndex + i;
                }
                this.setSelectedIndices( newIndicies );
                
                // Reset the over index
                this.overIndex = -1;
                
                dtde.getDropTargetContext().dropComplete( true );
                
                this.firePropertyChange(MODEL_PROPERTY, null, model);
            } else {
                dtde.rejectDrop();
            }
            
        } catch( IOException exception ) {
            exception.printStackTrace();
            System.err.println( "Exception" + exception.getMessage());
            dtde.rejectDrop();
        } catch( UnsupportedFlavorException ufException ) {
            ufException.printStackTrace();
            System.err.println( "Exception" + ufException.getMessage());
            dtde.rejectDrop();
        }
    }
    
    public void dragEnter(DragSourceDragEvent dsde) {
    }
    
    public void dragOver(DragSourceDragEvent dsde) {
    }
    
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
    
    public void dragExit(DragSourceEvent dse) {
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    
    private static DataFlavor getDefaultTransferDataFlavor() {
        return new DataFlavor(java.lang.Object.class, "Generic object");
    }
    
    private class ObjTransfer implements Transferable {
        Object data;
        
        public ObjTransfer(java.lang.Object data) {
            this.data = data;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{getDefaultTransferDataFlavor()};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return java.lang.Object.class.equals(flavor.getRepresentationClass());
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return data;
        }
    }
}
