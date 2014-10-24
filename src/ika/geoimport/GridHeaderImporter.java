package ika.geoimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author bernie
 */
public class GridHeaderImporter {

    private int cols = 0;
    private int rows = 0;
    private double west = Double.NaN;
    private double south = Double.NaN;
    private double cellSize = Double.NaN;
    private float noDataValue = Float.NaN;

    /*
     * returns whether valid values have been found
     */
    protected boolean isValid() {
        return cols > 0 && rows > 0 && cellSize > 0 && !Double.isNaN(west) && !Double.isNaN(south);
        // noDataValue is optional
    }

    String readHeader(BufferedReader reader, boolean stopOnFirstUnknownLine) throws IOException {
        cols = rows = 0;
        west = south = cellSize = Double.NaN;
        noDataValue = Float.NaN;
        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, " \t,;");
            String str = tokenizer.nextToken().trim().toLowerCase();
            if (str.equals("ncols")) {
                cols = Integer.parseInt(tokenizer.nextToken());
            } else if (str.equals("nrows")) {
                rows = Integer.parseInt(tokenizer.nextToken());
            } else if (str.equals("xllcenter") || str.equals("xllcorner")) {
                west = Double.parseDouble(tokenizer.nextToken());
            } else if (str.equals("yllcenter") || str.equals("yllcorner")) {
                south = Double.parseDouble(tokenizer.nextToken());
            } else if (str.equals("cellsize")) {
                cellSize = Double.parseDouble(tokenizer.nextToken());
            } else if (str.startsWith("nodata")) {
                noDataValue = Float.parseFloat(tokenizer.nextToken());
            } else {
                // done reading the header
                if (stopOnFirstUnknownLine) {
                    return line;
                }
            }
        }
        return null;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @return the west
     */
    public double getWest() {
        return west;
    }

    /**
     * @return the south
     */
    public double getSouth() {
        return south;
    }

    /**
     * @return the cellSize
     */
    public double getCellSize() {
        return cellSize;
    }

    /**
     * @return the noDataValue
     */
    public float getNoDataValue() {
        return noDataValue;
    }
}
