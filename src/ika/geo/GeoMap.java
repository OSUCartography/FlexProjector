/*
 * GeoMap.java
 *
 * Created on February 15, 2006, 1:52 PM
 *
 */

package ika.geo;

import ika.table.Table;
import ika.table.TableLink;
import java.io.*;
import java.util.*;

/**
 * GeoMap extends a GeoSet by adding support for attribute Tables. This is a 
 * central store for all tables accessible to a tree of GeoObjects.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoMap extends GeoSet implements Serializable {
    
    private static final long serialVersionUID = -896934826985392824L;
    
    private ArrayList tables = new ArrayList();
    private ArrayList tableLinks = new ArrayList();
   
    /**
     * Creates a new instance of GeoMap
     */
    public GeoMap(){
    }
    
    public void tableAdd(Table table) {
        if (table == null)
            return;
        this.tables.add(0, table);
    }
    
    public ArrayList tableGetAll() {
        return this.tables;
    }
    
    public void tableRemove(Table table, boolean removeDependentItems) {
        
        // remove charts that depend on this table
        /* remains to be done !!! ???
        if (removeDependentItems){
            for (int i = this.charts.size() - 1; i >= 0; i--) {
                Chart chart = (Chart)this.charts.get(i);
                if (chart.get() == geoSet)
                    this.chartRemove(chart, removeDependentItems);
            }
        }
         */
        
        // remove the table
        this.tables.remove(table);
    }
    
    public void tableLinkAdd(TableLink tableLink) {
        if (tableLink == null)
            return;
        
        this.tableLinks.add(0, tableLink);
    }
    
    public ArrayList tableLinkGetAll() {
        return this.tableLinks;
    }
    
    public int tableLinkGetNumber() {
        return this.tableLinks.size();
    }
    
    public TableLink tableLinkGet (int index) {
        return (TableLink)this.tableLinks.get(index);
    }
    
    public void tableRemove(TableLink tableLink, boolean removeDependentItems) {
        
        // remove charts that depend on this table
        /* remains to be done !!! ???
        if (removeDependentItems){
            for (int i = this.charts.size() - 1; i >= 0; i--) {
                Chart chart = (Chart)this.charts.get(i);
                if (chart.get() == geoSet)
                    this.chartRemove(chart, removeDependentItems);
            }
        }
         */
        
        // remove the table
        this.tableLinks.remove(tableLink);
    }
}