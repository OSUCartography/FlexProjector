/*
 * TableLink.java
 *
 * Created on June 26, 2006, 11:33 AM
 *
 */

package ika.table;

import ika.geo.*;
import javax.swing.table.TableColumn;

/**
 * Links table rows with GeoObjects in a GeoSet.
 * @author jenny
 */
public class TableLink extends java.util.HashMap {
    
    private Table table;
    private GeoSet geoSet;
    
    /** Creates a new instance of TableLink. 
     * Links the rows in the table with the GeoObjects in the GeoSet in
     * sequential order, i.e. the first row is linked with the first GeoObject, 
     * and so on.
     */
    public TableLink(Table table, GeoSet geoSet) {
        super (Math.min(table.getRowCount(), 
                geoSet.getNumberOfChildren()));
        
        this.table = table;
        this.geoSet = geoSet;
        
        this.initSequential();
    }
    
    /** Creates a new instance of TableLink. 
     */
    public TableLink(Table table,
            TableLink oldTableLink,
            TableColumn oldTableColumn,
            TableColumn newTableColumn) {
        
        if (table == null || oldTableLink == null 
                || oldTableColumn == null || newTableColumn == null)
            throw new IllegalArgumentException(
                    "Parameters for TableLink must not be null");
        
        this.table = table;
        this.geoSet = oldTableLink.geoSet;
        this.initByTableColumns(oldTableLink, oldTableColumn, newTableColumn);
    }
    
    /**
     * Links the rows in the table with the GeoObjects in the GeoSet in
     * sequential order, i.e. the first row is linked with the first GeoObject, 
     * and so on. If the number of GeoObjects is different from the number of
     * records in the table, not everything will be linked.
     */
    private void initSequential() {
        final int nbrEntries = Math.min(this.table.getRowCount(), 
                this.geoSet.getNumberOfChildren());
        this.clear();
        for (int i = 0; i < nbrEntries; i++) {
            final Integer key = new Integer(i);
            final long geoObjectID = this.geoSet.getGeoObject(i).getID();
            final Long value = new Long(geoObjectID);
            this.put(key, value);
        }
    }
    
    private void initByTableColumns(TableLink oldTableLink, 
            TableColumn oldTableColumn, 
            TableColumn newTableColumn) {
        
        final int oldTableColumnID = oldTableColumn.getModelIndex();
        final int newTableColumnID = newTableColumn.getModelIndex();
        
        final Table oldTable = oldTableLink.table;
        final int nbrEntries = Math.min(this.table.getRowCount(), 
                this.geoSet.getNumberOfChildren());
        for (int i = 0; i < nbrEntries; i++) {
            final Integer key = new Integer(i);
            
            Object newValue = this.table.getValueAt(i, newTableColumnID);
            int oldRowID = oldTable.findRowWithValue(newValue, oldTableColumnID);
            if (oldRowID < 0)
                continue;
            final Object value = oldTableLink.get(new Integer(oldRowID));

            this.put(key, value);
        }
    }
    
    /**
     * Returns a GeoObject for the passed row ID. Returns null if there is no
     * GeoObject associated with the passed row ID.
     */
    public final GeoObject getGeoObject (int rowID) {
        final Integer key = new Integer(rowID);
        final Object value = this.get(key);
        if (value == null)
            return null;
        final long geoObjectID = ((Long)value).longValue();
        return this.geoSet.getGeoObjectByID(geoObjectID);
    }
    
    public String toString() {
        String tableName = this.table.getName();
        String geoSetName = this.geoSet.getName();
        if (tableName.equals(geoSetName))
            return tableName;
        else
            return tableName + " - " + geoSetName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public GeoSet getGeoSet() {
        return geoSet;
    }

    public void setGeoSet(GeoSet geoSet) {
        this.geoSet = geoSet;
    }

    public boolean isValid() {
        return this.geoSet != null && this.table != null;
    }
}
