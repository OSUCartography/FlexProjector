/*
 * CombinedTool.java
 *
 * Created on June 11, 2006, 1:54 PM
 *
 */

package ika.map.tools;

import ika.geo.RenderParams;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;

/**
 * A combination of various map tools.
 * @author jenny
 */
public class CombinedTool extends MapTool{
    
    ArrayList tools = new ArrayList();
    
    String name = "";
    
    /** Creates a new instance of CombinedTool */
    public CombinedTool(ika.gui.MapComponent mapComponent, String name) {
        super(mapComponent);
        this.name = name;
    }
    
    public void addMapTool(CombinableTool mapTool) {
        this.tools.add(mapTool);
    }
    
    public void deactivate() {
        try {
            super.deactivate();
        } finally {
            
            Iterator iterator = tools.iterator();
            while (iterator.hasNext()) {
                ((MapTool)iterator.next()).deactivate();
            }
        }
    }
    
    public void pause(){
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).pause();
        }
    }
    
    public void resume(){
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).resume();
        }
    }
    
    
    public void mouseClicked(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).mouseClicked(point, evt);
        }
    }
    
    public void mouseDown(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).mouseDown(point, evt);
        }
    }
    
    public void mouseMoved(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            MapTool tool = ((MapTool)iterator.next());
            tool.mouseMoved(point, evt);
        }
        
        // adjust the cursor
        iterator = tools.iterator();
        boolean cursorAdjusted = false;
        while (iterator.hasNext()) {
            CombinableTool tool = ((CombinableTool)iterator.next());
            if (cursorAdjusted |= tool.adjustCursor(point))
                break;
        }
        if (!cursorAdjusted)
            this.setDefaultCursor();
    }
    
    public void mouseEntered(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).mouseEntered(point, evt);
        }
    }
    
    public void mouseExited(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).mouseExited(point, evt);
        }
    }
    
    public void startDrag(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            MapTool mapTool = (MapTool)iterator.next();
            mapTool.startDrag(point, evt);
            if (mapTool.isDragging())
                break;
        }
    }
    
    public void updateDrag(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            MapTool mapTool = (MapTool)iterator.next();
            if (mapTool.isDragging()) {
                mapTool.updateDrag(point, evt);
                break;
            }
        }
    }
    
    public void endDrag(Point2D.Double point, MouseEvent evt) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            MapTool mapTool = (MapTool)iterator.next();
            if (mapTool.isDragging()) {
                mapTool.endDrag(point, evt);
                break;
            }
        }
    }
    
    /**
     * Treat key events.
     * The event can be consumed (return true) or be delegated to other
     * listeners (return false).
     * @param keyEvent The new key event.
     * @return True if the key event has been consumed, false otherwise.
     */
    public boolean keyEvent(KeyEvent keyEvent) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            final MapTool mapTool = (MapTool)iterator.next();
            final boolean consumed = mapTool.keyEvent(keyEvent);
            if (consumed)
                return true;
        }
        return false;
    }
    
    public void draw(RenderParams rp) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            ((MapTool)iterator.next()).draw(rp);
        }
    }
    
    public boolean drawBackground(java.awt.Graphics2D g2d) {
        Iterator iterator = tools.iterator();
        while (iterator.hasNext()) {
            MapTool mapTool = (MapTool)iterator.next();
            if (mapTool.drawBackground(g2d))
                return true;
        }
        return false;
    }
}
