package ika.gui;

import ika.geo.FlexProjectorModel;
import ika.proj.DistortionComparator;
import com.jhlabs.map.proj.Projection;
import ika.proj.ProjectionDistortionParameters;
import ika.proj.QModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.*;


/**
 * A custom JTable that listens to clicks on the column headers.
 */
public class ProjDistortionTable extends JTable implements QModel.QListener, 
        ProjectionBrewerPanel.DesignProjectionChangeListener{
    
    public static final int NAME_COL = 0;
    public static final int DAB_COL = 1;
    public static final int DABC_COL = 2;
    public static final int DAR_COL = 3;
    public static final int DARC_COL = 4;
    public static final int DAN_COL = 5;
    public static final int DANC_COL = 6;
    public static final int Q_COL = 7;
    
    private static final Color STRIPE_COLOR = new Color(241, 245, 250);
    
    /** text labels displayed in column headers and used when exporting the 
     * table to a text file. The values above are indices into this table.
     */
    private static final String[] COLUMN_NAMES = {
        "Projection", "Scale", "Scale Cont.", "Areal", 
        "Areal Cont.", "Angular", "Angular Cont.", "Acc."};
    
    private final FlexProjectorModel model;
    
    private final ProjectionBrewerPanel pojectionBrewerPanel;
    
    /**
     * A renderer for column headers that can be selected.
     */
    private class SelectableTableHeaderRenderer implements TableCellRenderer {
           
        /**
         * JTable has a selection model for rows or columns, but not both at the
         * same time. We are using the selection model for the rows, so keep
         * track of the selected column here.
         */
        private int selectedColumn = 1;
        
        @Override
        public Component getTableCellRendererComponent(final JTable table,
                final Object value,
                final boolean isSelected,
                final boolean hasFocus,
                final int row,
                final int column) {
            
            JButton b = new JButton((value == null) ? "" : value.toString());
            int modelColumn = getColumnModel().getColumn(column).getModelIndex();
            if (modelColumn == selectedColumn) {
                b.setSelected(true);
            }
            if (ika.utils.Sys.isMacOSX_10_5_orHigherWithJava5()) {
                b.putClientProperty("JComponent.sizeVariant", "small");
                b.putClientProperty("JButton.buttonType", "segmented");
                b.putClientProperty("JButton.segmentPosition", "middle");
                b.setBackground(javax.swing.UIManager.getDefaults().getColor("TabbedPane.background"));
                b.setOpaque(true);
            } else {
                if (modelColumn != selectedColumn) {
                    b.setBackground(UIManager.getColor("control"));
                    b.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("controlHighlight"),UIManager.getColor("controlShadow")));
                } else {
                    b.setBackground(UIManager.getColor("controlShadow"));
                    b.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("controlLtHighlight"),UIManager.getColor("controlDkShadow")));
                }
            }

            // tooltip
            switch (column) {
                case NAME_COL:
                    b.setToolTipText("Click on a column header to order the table.");
                    break;
                case DAB_COL:
                    b.setToolTipText("The weighted mean error for overall scale distortion.");
                    break;
                case DABC_COL:
                    b.setToolTipText("The weighted mean error for overall scale distortion. Continental areas only.");
                    break;
                case DAR_COL:
                    b.setToolTipText("The weighted mean error for areal distortion. 0 indicates equal-area projections.");
                    break;
                case DARC_COL:
                    b.setToolTipText("The weighted mean error for areal distortion. Continental areas only.");
                    break;
                case DAN_COL:
                    b.setToolTipText("The mean angular deformation index. 0Êindicates conformal projections.");
                    break;
                case DANC_COL:
                    b.setToolTipText("The mean angular deformation index. Continental areas only.");
                    break;
                case Q_COL:
                    b.setToolTipText("Acceptance index. Double-click to change.");
                    break;
                    
            }
            return b;
        }

        public int getSelectedColumn() {
            return selectedColumn;
        }

        public void setSelectedColumn(int selectedColumn) {
            this.selectedColumn = selectedColumn;
        }

    }
    
    /**
     * Custom table model.
     */
    private class DistortionTableModel extends javax.swing.table.AbstractTableModel {
        
        @Override
        public Class getColumnClass(int columnIndex) {
            return String.class;
        }
        
        @Override
        public int getRowCount() {
            synchronized (model.getDisplayModel().distParams) {
                return model.getDisplayModel().distParams.size();
            }
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            return getDistParam(row, col);
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
    
    /**
     * Cell renderer for the table that will highlight certain cells.
     */
    private class DistortionTableRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            
            synchronized (model.getDisplayModel().distParams) {
                Projection proj = model.getDisplayModel().distParams.get(row).getProjection();
                isSelected |= proj == model.getDesignProjection();
            }
            
            Component comp = super.getTableCellRendererComponent(table,value,
                    isSelected,hasFocus,row,column);
             
            // set the background color stripes
            if (comp != null && !isSelected) {
                comp.setBackground(row % 2 == 0 ? STRIPE_COLOR : Color.WHITE);
            }
            
            return comp;
        }
        
    }
    
    public ProjDistortionTable(FlexProjectorModel model, 
            ProjectionBrewerPanel pojectionBrewerPane) {
        
        assert model != null;      
        this.model = model;
        this.pojectionBrewerPanel = pojectionBrewerPane;
        
        final JTableHeader header = this.getTableHeader();
        this.setOpaque(false);
        this.setBackground(UIManager.getColor("ScrollPane.background"));
        
        // renderer for table header
        SelectableTableHeaderRenderer headerRenderer = new SelectableTableHeaderRenderer();
        this.getTableHeader().setDefaultRenderer(headerRenderer);
        
        // renderer for table cells
        this.setDefaultRenderer(String.class, new DistortionTableRenderer());
        this.setIntercellSpacing(new java.awt.Dimension(1, 1));
        this.setShowHorizontalLines(false);
        this.setShowVerticalLines(true);
        this.setGridColor(STRIPE_COLOR);
        
        this.setModel(new DistortionTableModel());
        
        // add mouse listener that handles clicks on column headers
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                
                int col = header.columnAtPoint(e.getPoint());
                int colModel = getColumnModel().getColumn(col).getModelIndex();
                
                // show Q dialog after double-click on Q header
                if (e.getClickCount() == 2 && colModel == Q_COL) {
                    pojectionBrewerPanel.showAcceptanceDialog();
                } else {
                    // find pressed colum and inform header renderer
                    getHeaderRenderer().setSelectedColumn(colModel);
                    orderTable();
                }
            }
        });
        
        this.getColumnModel().addColumnModelListener( new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
            
        });
        
        // define names of column headers
        for (int i = NAME_COL; i <= DANC_COL; i++)
            this.getColumnModel().getColumn(i).setHeaderValue(COLUMN_NAMES[i]);
        
        // header of Q column is dynamic
        this.updateQColumnHeader();
        
        // order table by first column
        this.getHeaderRenderer().setSelectedColumn(0);
        this.orderTable();
        
        // adjust width of columns to their content
        ika.utils.TableColumnResizer.adjustColumnPreferredWidths(this, true);
    }
    
    private SelectableTableHeaderRenderer getHeaderRenderer() {
        return (SelectableTableHeaderRenderer)getTableHeader().getDefaultRenderer();
    }
    
    public void orderTable() {
        
        // order table
        int col = this.getHeaderRenderer().getSelectedColumn();
        assert col >= 0 && col < COLUMN_NAMES.length;
        
        synchronized (this.model.getDisplayModel().distParams) {
            Comparator comp = new DistortionComparator(col);
            Collections.sort(this.model.getDisplayModel().distParams, comp);
        }
        
        // force redraw of table
        AbstractTableModel tableModel = (AbstractTableModel)getModel();
        tableModel.fireTableDataChanged();
        
    }
    
    /**
     * Return the value for a table cell.
     */
    private String getDistParam(int projID, int paramID) {
        
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        
        // display NaN and infinity as "-"
        DecimalFormatSymbols dfs = formatter.getDecimalFormatSymbols();
        dfs.setNaN("-");
        dfs.setInfinity("-");
        formatter.setDecimalFormatSymbols(dfs);
        
        ProjectionDistortionParameters params;
        synchronized (model.getDisplayModel().distParams) {
            params = model.getDisplayModel().distParams.get(projID);
        }
        switch (paramID) {
            case NAME_COL: return params.getProjectionName();
            case DAB_COL: return formatter.format(params.getDab());
            case DABC_COL: return formatter.format(params.getDabc());
            case DAR_COL: return formatter.format(params.getDar());
            case DARC_COL: return formatter.format(params.getDarc());
            case DAN_COL: return formatter.format(params.getDan());
            case DANC_COL: return formatter.format(params.getDanc());
            case Q_COL: {
                formatter.setMaximumFractionDigits(1);
                return formatter.format(params.getQ());
            }
        }
        return null;
        
    }
    
    @Override
    public void designProjectionChanged(Projection p) {
        this.orderTable();
    }
    
    @Override
    public String toString() {

        String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        
        // header line
        for (String name : COLUMN_NAMES) {
            sb.append(name);
            sb.append('\t');
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append(newline);
        
        // table content
        synchronized (model.getDisplayModel().distParams) {
            for (int row = 0; row < this.model.getDisplayModel().distParams.size(); row++) {
                for (int col = 0; col < COLUMN_NAMES.length; col++) {
                    sb.append(this.getDistParam(row, col));
                    sb.append('\t');
                }
                sb.deleteCharAt(sb.length()-1);
                sb.append(newline);
            }
        }
        
        return sb.toString();
    }

    /**
     * Changes the column header of the Q column, which displays the current
     * maximum acceptable limits for the computation of Q.
     */
    private void updateQColumnHeader() {
        
        QModel qModel = this.model.getDisplayModel().qModel;
        String headerStr = COLUMN_NAMES[Q_COL] + " " + qModel.getFormattedMaxValues() + " ";
        int viewColumn = this.convertColumnIndexToView(Q_COL);
        this.getColumnModel().getColumn(viewColumn).setHeaderValue(headerStr);
    
    }

    @Override
    public void qChanged(QModel newQModel, QModel oldQModel) {
        
        this.getHeaderRenderer().setSelectedColumn(Q_COL);
        
        synchronized (model.getDisplayModel().distParams) {
            for (ProjectionDistortionParameters param : model.getDisplayModel().distParams) {
                param.qModelChanged(this.model.getDisplayModel().qModel);

            }
            this.orderTable();
        }
        
        // don't call tableModel.fireTableStructureChanged() to repaint the
        // header of the Q column with the new maximum values, because
        // customized widths and other settings would get lost.
        // Change the name of the ColumnModel instead.
        this.updateQColumnHeader();
        this.getTableHeader().repaint();
    }
    
    public void qAreaAcceptanceChanged() {
        
        synchronized (model.getDisplayModel().distParams) {
            for (ProjectionDistortionParameters param : model.getDisplayModel().distParams) {
                param.qModelChanged(this.model.getDisplayModel().qModel);
            }
            this.orderTable();
        }

    }

}