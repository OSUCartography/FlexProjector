/*
 * Geothis.java
 *
 * Created on May 13, 2007, 9:09 PM
 *
 */

package ika.gui;

import ika.geo.*;
import java.awt.Color;
import javax.swing.*;

/**
 * A tool bar that offers buttons to create rectangles, lines, text, etc.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class CreationToolBar extends JToolBar {
    
    // icons for the buttons
    private final String setIcon = "/ika/icons/folder.png";
    private final String textIcon = "/ika/icons/Text16x16.gif";
    private final String rectIcon = "/ika/icons/Box16x16.gif";
    private final String circleIcon = "/ika/icons/Circle16x16.png";
    private final String lineIcon = "/ika/icons/Line16x16.gif";

    /** counts the text labels created so that each new label gets increasingly
     * numbered.
     */
    private int geoTextCounter = 1;
    
    /** counts the rectangles created so that each new rectangle gets increasingly
     * numbered.
     */
    private int rectangleCounter = 1;
    
    /** counts the lines created so that each new line gets increasingly
     * numbered.
     */
    private int lineCounter = 1;
    
    /** counts the circles created so that each new circle gets increasingly
     * numbered.
     */
    private int circleCounter = 1;
    
    /**
     * The map that will receive the newly created geometry.
     */
    private MapComponent mapComponent;
    
    /** Creates a new instance of Geothis */
    public CreationToolBar() {
        
        JButton setButton = new javax.swing.JButton();
        JButton textButton = new javax.swing.JButton();
        JButton rectangleButton = new javax.swing.JButton();
        JButton circleButton = new javax.swing.JButton();
        JButton lineButton = new javax.swing.JButton();
        
        setButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(setIcon)));
        setButton.setToolTipText("Create a Set");
        setButton.setMargin(new java.awt.Insets(4, 3, 3, 4));
        setButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GeoSet geoSet = new GeoSet();
                geoSet.setName("Set");
                addGraphics(geoSet, true, "Group");
            }
        });
        
        this.add(setButton);
        
        textButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(textIcon)));
        textButton.setToolTipText("Create a Text Label");
        textButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addText();
            }
        });
        
        this.add(textButton);
        
        rectangleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(rectIcon)));
        rectangleButton.setToolTipText("Create a Rectangle");
        rectangleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRectangle();
            }
        });
        
        this.add(rectangleButton);
        
        circleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(circleIcon)));
        circleButton.setToolTipText("Create a Circle");
        circleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCircle();
            }
        });
        
        this.add(circleButton);
        
        lineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(lineIcon)));
        lineButton.setToolTipText("Create a Line");
        lineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLine();
            }
        });
        
        this.add(lineButton);
    }
    
    private void addText() {
        GeoText geoText = new GeoText();
        geoText.setText("Label " + geoTextCounter);
        geoText.setID(geoTextCounter++);
        
        geoText.setName(geoText.getText());
        geoText.setScaleInvariant(true);
        geoText.setCenterHor(false);
        geoText.setCenterVer(false);
        geoText.setSize(40);
        
        // position geoText
        java.awt.geom.Rectangle2D bounds
                = this.mapComponent.getVisibleArea();
        if (bounds != null) {
            geoText.setX((bounds.getMinX() + bounds.getMaxX())/2);
            geoText.setY((bounds.getMinY() + bounds.getMaxY())/2);
        }
        
        this.addGraphics(geoText, true, "Text");
    }
    
    private VectorSymbol standardFillSymbol() {
        VectorSymbol vs = new VectorSymbol();
        vs.setScaleInvariant(false);
        vs.setStrokeWidth(1);
        vs.setStrokeColor(Color.BLACK);
        vs.setFilled(true);
        vs.setFillColor(Color.LIGHT_GRAY);
        return vs;
    }
    
    private void addRectangle() {
        java.awt.geom.Rectangle2D visibleArea = this.mapComponent.getVisibleArea();
        double cx = visibleArea.getCenterX();
        double cy = visibleArea.getCenterY();
        double d = Math.min(visibleArea.getWidth(), visibleArea.getHeight()) / 2;
        GeoPath geoPath = new GeoPath();
        geoPath.append(GeoPath.newSquare(cx, cy, d), false);
        geoPath.setName("Rectangle " + this.rectangleCounter++);
        geoPath.setVectorSymbol(standardFillSymbol());
        this.addGraphics(geoPath, true, "Rectangle");
    }
    
    private void addLine() {
        java.awt.geom.Rectangle2D visibleArea = this.mapComponent.getVisibleArea();
        final double w = visibleArea.getWidth();
        final double h = visibleArea.getHeight();
        final double x = visibleArea.getX();
        final double y = visibleArea.getY();
        final double x1 = w / 4 + x;
        final double y1 = h / 4 + y;
        final double x2 = w / 4 * 3 + x;
        final double y2 = h / 4 * 3 + y;
        
        GeoPath geoPath = new GeoPath();
        geoPath.setName("Line " + this.lineCounter++);
        
        geoPath.moveTo(x1, y1);
        geoPath.lineTo(x2, y2);
        
        VectorSymbol vs = new VectorSymbol();
        vs.setScaleInvariant(true);
        vs.setStrokeWidth(2);
        geoPath.setVectorSymbol(vs);
        
        this.addGraphics(geoPath, true, "Line");
    }
    
    private void addCircle() {
        java.awt.geom.Rectangle2D visibleArea = this.mapComponent.getVisibleArea();
        final double w = visibleArea.getWidth();
        final double h = visibleArea.getHeight();
        final double r = Math.min(w, h) / 4.f;
        final double cx = visibleArea.getX() + w / 2;
        final double cy = visibleArea.getY() + h / 2;
        
        GeoPath geoPath = GeoPath.newCircle(cx, cy, r);
        geoPath.setName("Circle " + circleCounter++);
        geoPath.setVectorSymbol(standardFillSymbol());
        addGraphics(geoPath, true, "Circle");
    }
    
    private void addGraphics(GeoObject geoObject, boolean select, String undoText) {
        
        // find the parent GeoSet to add the passed GeoObject to.
        GeoSet parentGeoSet = this.mapComponent.getGeoSet();

        final MapEventTrigger trigger = new MapEventTrigger(parentGeoSet);
        try {
            // first add the object...
            parentGeoSet.add(geoObject);
            
            // ...then select it.
            if (select) {
                this.mapComponent.deselectAllGeoObjects();
                geoObject.setSelected(true);
            }
            
            this.mapComponent.addUndo(undoText);
        } finally {
            trigger.inform(new MapEvent(true, true, true));
        }
        
    }
    
    public MapComponent getMapComponent() {
        return mapComponent;
    }
    
    public void setMapComponent(MapComponent mapComponent) {
        this.mapComponent = mapComponent;
    }
    
}
