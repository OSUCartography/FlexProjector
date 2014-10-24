/*
 * BasePreferencesPanel.java
 *
 * Created on April 20, 2007, 10:38 AM
 */
package ika.gui;

import ika.proj.ProjectionsManager;
import java.awt.Color;
import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The preferences panel. It is displayed by the ika.gui.PreferencesDialog.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class FlexProjectorPreferencesPanel extends JPanel
        implements PreferencesPanel {

    /**
     * key to store interpolation settings for grids and images..
     */
    public static final String INTERPOLATION_PREFS = "interpolation";
    /**
     * nearest neighbor interpolation for grids and images
     */
    public static final int INTERPOLATION_NEAREST = 0;
    /**
     * bicubic spline interpolation for grids and images.
     */
    public static final int INTERPOLATION_BICUBIC = 1;
    private static final String FLEX_R = "flexr";
    private static final String FLEX_G = "flexg";
    private static final String FLEX_B = "flexb";
    private static final String SEC_R = "secr";
    private static final String SEC_G = "secg";
    private static final String SEC_B = "secb";
    private static final String ANGULAR_R = "angularr";
    private static final String ANGULAR_G = "angularg";
    private static final String ANGULAR_B = "angularb";
    private static final String AREAL_R = "arealr";
    private static final String AREAL_G = "arealg";
    private static final String AREAL_B = "arealb";
    private static final String MAP_R = "mapbackgroundr";
    private static final String MAP_G = "mapbackgroundg";
    private static final String MAP_B = "mapbackgroundb";
    private static final String ACCEPTANCE_RELATIVE_TO_1 = "acceptance_rel_to_1";

    private static Preferences getPreferences() {
        return Preferences.userNodeForPackage(FlexProjectorPreferencesPanel.class);
    }

    public static Color getFlexColor() {
        Preferences prefs = getPreferences();
        int flexR = prefs.getInt(FLEX_R, 0);
        int flexG = prefs.getInt(FLEX_G, 0);
        int flexB = prefs.getInt(FLEX_B, 0);
        return new Color(flexR, flexG, flexB);
    }

    public static Color getSecondColor() {
        Preferences prefs = getPreferences();
        int r = prefs.getInt(SEC_R, 127);
        int g = prefs.getInt(SEC_G, 127);
        int b = prefs.getInt(SEC_B, 127);
        return new Color(r, g, b);
    }

    public static Color getAngularIsolinesColor() {
        Preferences prefs = getPreferences();
        int r = prefs.getInt(ANGULAR_R, 204);
        int g = prefs.getInt(ANGULAR_G, 0);
        int b = prefs.getInt(ANGULAR_B, 204);
        return new Color(r, g, b);
    }

    public static Color getArealIsolinesColor() {
        Preferences prefs = getPreferences();
        int r = prefs.getInt(AREAL_R, 0);
        int g = prefs.getInt(AREAL_G, 0);
        int b = prefs.getInt(AREAL_B, 255);
        return new Color(r, g, b);
    }

    public static Color getMapBackgroundColor() {
        Preferences prefs = getPreferences();
        int r = prefs.getInt(MAP_R, 255);
        int g = prefs.getInt(MAP_G, 255);
        int b = prefs.getInt(MAP_B, 255);
        return new Color(r, g, b);
    }

    public static boolean isNearestNeighbor() {
        Preferences prefs = getPreferences();
        int interpol = prefs.getInt(INTERPOLATION_PREFS, INTERPOLATION_BICUBIC);
        return interpol == INTERPOLATION_NEAREST;
    }

    public static boolean isAreaAcceptanceRelativeTo1() {
        Preferences prefs = getPreferences();
        return prefs.getBoolean(ACCEPTANCE_RELATIVE_TO_1, true);
    }

    public static void setAreaAcceptanceRelativeTo1(boolean b) {
        Preferences prefs = getPreferences();
        prefs.putBoolean(ACCEPTANCE_RELATIVE_TO_1, b);
    }
    
    /**
     * Creates new form BasePreferencesPanel
     */
    public FlexProjectorPreferencesPanel() {
        initComponents();

        Preferences prefs = getPreferences();

        // init interpol radio buttons
        int interpol = prefs.getInt(INTERPOLATION_PREFS, INTERPOLATION_BICUBIC);
        switch (interpol) {
            case INTERPOLATION_BICUBIC:
                this.bicubicRadioButton.setSelected(true);
                break;
            default:
                this.nearestNeighborRadioButton.setSelected(true);
        }

        // init color buttons
        this.flexColorButton.setColor(getFlexColor());
        this.secondColorButton.setColor(getSecondColor());
        this.arealIsolinesColorButton.setColor(getArealIsolinesColor());
        this.angularIsolinesColorButton.setColor(getAngularIsolinesColor());
        this.mapBackgroundColorButton.setColor(getMapBackgroundColor());

        // init projections
        DefaultListModel model = new DefaultListModel();
        int projCount = ProjectionsManager.getAvailableProjectionsCount();
        for (int i = 0; i < projCount; i++) {
            String projName = ProjectionsManager.getProjectionName(i);
            model.addElement(projName);
        }
        this.projectionsList.setModel(model);
        this.updateProjectionSelection();

        // init area acceptance radio buttons
        boolean relTo1 = isAreaAcceptanceRelativeTo1();
        this.qAreaEqualRadioButton.setSelected(relTo1);
        this.qAreaMinRadioButton.setSelected(!relTo1);

    }

    private void updateProjectionSelection() {
        this.projectionsList.clearSelection();
        int projCount = ProjectionsManager.getAvailableProjectionsCount();
        for (int i = 0; i < projCount; i++) {
            boolean selected = ProjectionsManager.isProjectionSelected(i);
            if (selected) {
                this.projectionsList.addSelectionInterval(i, i);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        interpolationButtonGroup = new javax.swing.ButtonGroup();
        acceptanceButtonGroup = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        projectionsPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        projectionsPanel.setOpaque(false);
        projectionsScrollPane = new javax.swing.JScrollPane();
        projectionsList = new javax.swing.JList();
        selectAllProjectionsButton = new javax.swing.JButton();
        selectNoProjectionButton = new javax.swing.JButton();
        selectDefaultProjectionsButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        colorPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        colorPanel.setOpaque(false);
        javax.swing.JPanel innerColorPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        innerColorPanel.setOpaque(false);
        flexColorButton = new ika.gui.ColorButton();
        secondColorButton = new ika.gui.ColorButton();
        arealIsolinesColorButton = new ika.gui.ColorButton();
        angularIsolinesColorButton = new ika.gui.ColorButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        mapBackgroundColorButton = new ika.gui.ColorButton();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        extrasPanel = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        extrasPanel.setOpaque(false);
        extrasPanelContent = new javax.swing.JPanel();
        if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5())
        extrasPanelContent.setOpaque(false);
        javax.swing.JLabel interpolationLabel = new javax.swing.JLabel();
        nearestNeighborRadioButton = new javax.swing.JRadioButton();
        bicubicRadioButton = new javax.swing.JRadioButton();
        javax.swing.JLabel areaDistortionLabel = new javax.swing.JLabel();
        qAreaEqualRadioButton = new javax.swing.JRadioButton();
        qAreaMinRadioButton = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(540, 660));
        setLayout(new java.awt.BorderLayout());

        tabbedPane.setMinimumSize(new java.awt.Dimension(351, 500));
        tabbedPane.setPreferredSize(new java.awt.Dimension(355, 600));

        projectionsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        projectionsList.setCellRenderer(new CheckBoxCellRenderer());
        projectionsList.setSelectionModel(new ToggleSelectionModel());
        projectionsScrollPane.setViewportView(projectionsList);

        selectAllProjectionsButton.setText("Select All");
        selectAllProjectionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllProjectionsButtonActionPerformed(evt);
            }
        });

        selectNoProjectionButton.setText("Select None");
        selectNoProjectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoProjectionButtonActionPerformed(evt);
            }
        });

        selectDefaultProjectionsButton.setText("Default");
        selectDefaultProjectionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectDefaultProjectionsButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("<html> Changes will take effect the next time <br>you start Flex Projector.</html>");

        jLabel6.setText("<html>Select the projections that will appear in <br>the distortion table, and will be available <br>for display in the map background and for <br>resetting the current projection.</html>");

        org.jdesktop.layout.GroupLayout projectionsPanelLayout = new org.jdesktop.layout.GroupLayout(projectionsPanel);
        projectionsPanel.setLayout(projectionsPanelLayout);
        projectionsPanelLayout.setHorizontalGroup(
            projectionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(projectionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(projectionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectionsPanelLayout.createSequentialGroup()
                        .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                        .add(32, 32, 32))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, projectionsPanelLayout.createSequentialGroup()
                        .add(projectionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, projectionsPanelLayout.createSequentialGroup()
                                .add(selectAllProjectionsButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(selectNoProjectionButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 104, Short.MAX_VALUE)
                                .add(selectDefaultProjectionsButton))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .add(projectionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        projectionsPanelLayout.setVerticalGroup(
            projectionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, projectionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(projectionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(projectionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAllProjectionsButton)
                    .add(selectNoProjectionButton)
                    .add(selectDefaultProjectionsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addContainerGap())
        );

        tabbedPane.addTab("Projections", projectionsPanel);

        colorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        colorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 20));

        innerColorPanel.setLayout(new java.awt.GridBagLayout());

        flexColorButton.setMaximumSize(new java.awt.Dimension(32, 32));
        flexColorButton.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerColorPanel.add(flexColorButton, gridBagConstraints);

        secondColorButton.setForeground(new java.awt.Color(102, 102, 102));
        secondColorButton.setMaximumSize(new java.awt.Dimension(32, 32));
        secondColorButton.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerColorPanel.add(secondColorButton, gridBagConstraints);

        arealIsolinesColorButton.setForeground(new java.awt.Color(102, 102, 102));
        arealIsolinesColorButton.setMaximumSize(new java.awt.Dimension(32, 32));
        arealIsolinesColorButton.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerColorPanel.add(arealIsolinesColorButton, gridBagConstraints);

        angularIsolinesColorButton.setForeground(new java.awt.Color(102, 102, 102));
        angularIsolinesColorButton.setMaximumSize(new java.awt.Dimension(32, 32));
        angularIsolinesColorButton.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerColorPanel.add(angularIsolinesColorButton, gridBagConstraints);

        jLabel1.setText("Flex Projection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        innerColorPanel.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Background Projection");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        innerColorPanel.add(jLabel2, gridBagConstraints);

        jLabel4.setText("Isolines of Areal Distortion");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        innerColorPanel.add(jLabel4, gridBagConstraints);

        jLabel3.setText("Isolines of Maximum Angular Distortion");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        innerColorPanel.add(jLabel3, gridBagConstraints);

        mapBackgroundColorButton.setForeground(new java.awt.Color(102, 102, 102));
        mapBackgroundColorButton.setMaximumSize(new java.awt.Dimension(32, 32));
        mapBackgroundColorButton.setPreferredSize(new java.awt.Dimension(32, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        innerColorPanel.add(mapBackgroundColorButton, gridBagConstraints);

        jLabel7.setText("Map Background");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        innerColorPanel.add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        innerColorPanel.add(jSeparator1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        innerColorPanel.add(jSeparator2, gridBagConstraints);

        colorPanel.add(innerColorPanel);

        tabbedPane.addTab("Color", colorPanel);

        extrasPanelContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        extrasPanelContent.setLayout(new java.awt.GridBagLayout());

        interpolationLabel.setText("Image and Grid Interpolation:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        extrasPanelContent.add(interpolationLabel, gridBagConstraints);

        interpolationButtonGroup.add(nearestNeighborRadioButton);
        nearestNeighborRadioButton.setText("Nearest Neighbor");
        nearestNeighborRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 40, 6, 0);
        extrasPanelContent.add(nearestNeighborRadioButton, gridBagConstraints);

        interpolationButtonGroup.add(bicubicRadioButton);
        bicubicRadioButton.setSelected(true);
        bicubicRadioButton.setText("Bicubic");
        bicubicRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        extrasPanelContent.add(bicubicRadioButton, gridBagConstraints);

        areaDistortionLabel.setText("Acceptance Index");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(40, 0, 5, 0);
        extrasPanelContent.add(areaDistortionLabel, gridBagConstraints);

        acceptanceButtonGroup.add(qAreaEqualRadioButton);
        qAreaEqualRadioButton.setSelected(true);
        qAreaEqualRadioButton.setText("Equal-area projection (Recommended)");
        qAreaEqualRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        qAreaEqualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qAreaEqualRadioButtonqRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 40, 6, 0);
        extrasPanelContent.add(qAreaEqualRadioButton, gridBagConstraints);

        acceptanceButtonGroup.add(qAreaMinRadioButton);
        qAreaMinRadioButton.setText("Minimum distortion of projection (as by Capek)");
        qAreaMinRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        qAreaMinRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qAreaMinRadioButtonqRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        extrasPanelContent.add(qAreaMinRadioButton, gridBagConstraints);

        jLabel8.setText("Area distortion is relative to…");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        extrasPanelContent.add(jLabel8, gridBagConstraints);

        extrasPanel.add(extrasPanelContent);

        tabbedPane.addTab("Extras", extrasPanel);

        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void selectNoProjectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoProjectionButtonActionPerformed
        ProjectionsManager.selectNoProjections();
        this.updateProjectionSelection();
    }//GEN-LAST:event_selectNoProjectionButtonActionPerformed

    private void selectAllProjectionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllProjectionsButtonActionPerformed
        ProjectionsManager.selectAllProjections();
        this.updateProjectionSelection();
    }//GEN-LAST:event_selectAllProjectionsButtonActionPerformed

    private void selectDefaultProjectionsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectDefaultProjectionsButtonActionPerformed
        ProjectionsManager.selectDefaultProjections();
        this.updateProjectionSelection();
    }//GEN-LAST:event_selectDefaultProjectionsButtonActionPerformed

    private void qAreaEqualRadioButtonqRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qAreaEqualRadioButtonqRadioButtonActionPerformed
    }//GEN-LAST:event_qAreaEqualRadioButtonqRadioButtonActionPerformed

    private void qAreaMinRadioButtonqRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qAreaMinRadioButtonqRadioButtonActionPerformed
    }//GEN-LAST:event_qAreaMinRadioButtonqRadioButtonActionPerformed

    @Override
    public void okPressed() {

        Preferences prefs = getPreferences();

        // read interpolation
        final int interpolation;
        if (this.nearestNeighborRadioButton.isSelected()) {
            interpolation = INTERPOLATION_NEAREST;
        } else {
            interpolation = INTERPOLATION_BICUBIC;
        }
        prefs.putInt(INTERPOLATION_PREFS, interpolation);

        // read area acceptance
        final boolean relTo1 = this.qAreaEqualRadioButton.isSelected();
        prefs.putBoolean(ACCEPTANCE_RELATIVE_TO_1, relTo1);

        // read colors
        Color flexColor = this.flexColorButton.getColor();
        prefs.putInt(FLEX_R, flexColor.getRed());
        prefs.putInt(FLEX_G, flexColor.getGreen());
        prefs.putInt(FLEX_B, flexColor.getBlue());

        Color secColor = this.secondColorButton.getColor();
        prefs.putInt(SEC_R, secColor.getRed());
        prefs.putInt(SEC_G, secColor.getGreen());
        prefs.putInt(SEC_B, secColor.getBlue());

        Color angularColor = this.angularIsolinesColorButton.getColor();
        prefs.putInt(ANGULAR_R, angularColor.getRed());
        prefs.putInt(ANGULAR_G, angularColor.getGreen());
        prefs.putInt(ANGULAR_B, angularColor.getBlue());

        Color arealColor = this.arealIsolinesColorButton.getColor();
        prefs.putInt(AREAL_R, arealColor.getRed());
        prefs.putInt(AREAL_G, arealColor.getGreen());
        prefs.putInt(AREAL_B, arealColor.getBlue());

        Color mapColor = this.mapBackgroundColorButton.getColor();
        prefs.putInt(MAP_R, mapColor.getRed());
        prefs.putInt(MAP_G, mapColor.getGreen());
        prefs.putInt(MAP_B, mapColor.getBlue());

        // read selected projections
        final int projCount = ProjectionsManager.getAvailableProjectionsCount(); // this.projectionsList.getModel().getSize();
        boolean projSelectionChanged = false;
        for (int i = 0; i < projCount; i++) {
            boolean selected = this.projectionsList.isSelectedIndex(i);
            projSelectionChanged |= (ProjectionsManager.isProjectionSelected(i) != selected);
            ProjectionsManager.setProjectionSelected(i, selected);
        }
        if (ProjectionsManager.getSelectedProjectionsCount() == 0) {
            ika.utils.ErrorDialog.showErrorDialog("No projection was selected. "
                    + "The default set of projections will be selected instead.\n"
                    + "The changes will take effect the next time you start "
                    + "Flex Projector.",
                    (Component) null // prefs dialog has been hidden already
                    );
            ProjectionsManager.selectDefaultProjections();
        } else if (projSelectionChanged) {
            JOptionPane.showMessageDialog(this.getParent(), // must pass parent because of bug in Mac OS X
                    "The changes to the selection of projections will take "
                    + "effect the next time you start Flex Projector or when you open a new window.",
                    "Selection of Projections", JOptionPane.INFORMATION_MESSAGE);
        }
        ProjectionsManager.writeProjectionsToPreferences();

        FlexProjectorWindow.updateAfterPreferencesChange();
    }

    public void cancelPressed() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup acceptanceButtonGroup;
    private ika.gui.ColorButton angularIsolinesColorButton;
    private ika.gui.ColorButton arealIsolinesColorButton;
    private javax.swing.JRadioButton bicubicRadioButton;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JPanel extrasPanel;
    private javax.swing.JPanel extrasPanelContent;
    private ika.gui.ColorButton flexColorButton;
    private javax.swing.ButtonGroup interpolationButtonGroup;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private ika.gui.ColorButton mapBackgroundColorButton;
    private javax.swing.JRadioButton nearestNeighborRadioButton;
    private javax.swing.JList projectionsList;
    private javax.swing.JPanel projectionsPanel;
    private javax.swing.JScrollPane projectionsScrollPane;
    private javax.swing.JRadioButton qAreaEqualRadioButton;
    private javax.swing.JRadioButton qAreaMinRadioButton;
    private ika.gui.ColorButton secondColorButton;
    private javax.swing.JButton selectAllProjectionsButton;
    private javax.swing.JButton selectDefaultProjectionsButton;
    private javax.swing.JButton selectNoProjectionButton;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
