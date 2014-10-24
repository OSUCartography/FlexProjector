package ika.geo.grid;

import ika.geo.GeoGrid;
import ika.geo.GeoObject;
import ika.geoexport.ESRIASCIIGridExporter;
import ika.geoimport.EsriASCIIGridReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multiply the cell values of two grids.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GridMultiplyOperator implements GridOperator{

    public static void main(String[] args) {
        try {
            String f1 = ika.utils.FileUtils.askFile(null, "Grid 1 (ESRI ASCII format)", true);
            if (f1 == null) {
                System.exit(0);
            }
            String f2 = ika.utils.FileUtils.askFile(null, "Grid 2 (ESRI ASCII format)", true);
            if (f2 == null) {
                System.exit(0);
            }
            GeoGrid grid1 = EsriASCIIGridReader.read(f1);
            GeoGrid grid2 = EsriASCIIGridReader.read(f2);

            GeoGrid res = new GridMultiplyOperator().operate(grid1, grid2);

            String resFilePath = ika.utils.FileUtils.askFile(null, "Result Grid (ESRI ASCII format)", false);
            if (resFilePath != null) {
                ESRIASCIIGridExporter.export(res, resFilePath);
            }
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(GridMultiplyOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getName() {
        return "Multiply";
    }

    public GeoObject operate(GeoGrid geoGrid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GeoGrid operate(GeoGrid grid1, GeoGrid grid2) {
        
        if (!grid1.hasSameExtensionAndResolution(grid2)) {
            throw new IllegalArgumentException("grids of different size");
        }
        
        final int nrows = grid1.getRows();
        final int ncols = grid1.getCols();
        GeoGrid newGrid = new GeoGrid(ncols, nrows, grid1.getCellSize());
        newGrid.setWest(grid1.getWest());
        newGrid.setNorth(grid1.getNorth());
        
        float[][] src1 = grid1.getGrid();
        float[][] src2 = grid2.getGrid();
        float[][] dstGrid = newGrid.getGrid();
        
        for (int row = 0; row < nrows; ++row) {
            float[] srcRow1 = src1[row];
            float[] srcRow2 = src2[row];
            float[] dstRow = dstGrid[row];
            for (int col = 0; col < ncols; ++col) {
                dstRow[col] = srcRow1[col] * srcRow2[col];
            }
        }
        return newGrid;
        
    }
}
