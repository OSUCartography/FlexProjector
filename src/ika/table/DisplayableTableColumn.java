/*
 * DisplayableTableColumn.java
 *
 * Created on June 15, 2006, 5:34 PM
 *
 */

package ika.table;

import javax.swing.table.TableColumn;
/**
 * A TableColumn that overwrites toString to return the header value.
 * @author jenny
 */
public class DisplayableTableColumn extends TableColumn {
    
    /**
     * Creates a new instance of DisplayableTableColumn
     */
    public DisplayableTableColumn(int modelIndex) {
        super(modelIndex);
    }
       
    public String toString() {
        return this.getHeaderValue().toString();
    }
}
