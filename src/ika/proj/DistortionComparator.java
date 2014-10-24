/*
 * DistortionComparator.java
 *
 * Created on January 11, 2008, 9:25 AM
 *
 */

package ika.proj;

import ika.gui.ProjDistortionTable;

/**
 * Comparator for sorting ProjectionDistortionParameters.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class DistortionComparator
        implements java.util.Comparator<ProjectionDistortionParameters> {
    
    private int col;
    
    public DistortionComparator(int col) {
        this.col = col;
    }
    
    private int compareNames(ProjectionDistortionParameters p1,
            ProjectionDistortionParameters p2) {
        return p1.getProjectionName().compareTo(p2.getProjectionName());
    }
    
    private int compare(double d1, double d2) {
        if (Double.isNaN(d1))
            return 1;
        if (Double.isNaN(d2))
            return -1;
        if (Double.isInfinite(d1))
            return 1;
        if (Double.isInfinite(d2))
            return -1;
        return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
    }
    
    private int inverseCompare(double d1, double d2) {
        if (Double.isNaN(d1))
            return -1;
        if (Double.isNaN(d2))
            return 1;
        if (Double.isInfinite(d1))
            return -1;
        if (Double.isInfinite(d2))
            return 1;
        return d1 > d2 ? -1 : (d1 < d2 ? 1 : 0);
    }
    
    public int compare(ProjectionDistortionParameters p1,
            ProjectionDistortionParameters p2) {
        
        int res = 0;
        switch (col) {
            case ProjDistortionTable.NAME_COL:
                return compareNames(p1, p2);
            case ProjDistortionTable.DAB_COL:
                res = compare(p1.getDab(), p2.getDab());
                break;
            case ProjDistortionTable.DABC_COL:
                res = compare(p1.getDabc(), p2.getDabc());
                break;
            case ProjDistortionTable.DAR_COL:
                res = compare(p1.getDar(), p2.getDar());
                break;
            case ProjDistortionTable.DARC_COL:
                res = compare(p1.getDarc(), p2.getDarc());
                break;
            case ProjDistortionTable.DAN_COL:
                res = compare(p1.getDan(), p2.getDan());
                break;
            case ProjDistortionTable.DANC_COL:
                res = compare(p1.getDanc(), p2.getDanc());
                break;
            case ProjDistortionTable.Q_COL:
                res = inverseCompare(p1.getQ(), p2.getQ());
                break;
        }
        return res == 0 ? compareNames(p1, p2) : res;
    }
}