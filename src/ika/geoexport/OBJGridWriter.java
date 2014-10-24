package ika.geoexport;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author jenny
 */
public final class OBJGridWriter {

    /**
     * if a vertex is not valid, this value is written instead
     */
    private static final String NO_DATA_VALUE = "0";
    /**
     * A system dependent separator string, typically '\n' or '\r' or a
     * combination of the two.
     */
    private static final String NL = System.getProperty("line.separator");
    private final PrintWriter writer;
    private final int cols;
    private final int rows;
    private final double cellSize;
    private final DecimalFormat vertexFormat;
    private final boolean writeTexture;
    /**
     * Counts the number of vertices written to the file.
     */
    private int vertexCounter = 0;

    public OBJGridWriter(PrintWriter writer, int cols, int rows,
            double cellSize, boolean writeTexture) {

        if (cols <= 1 || rows <= 1 || cellSize <= 0) {
            throw new IllegalArgumentException();
        }

        vertexFormat = new DecimalFormat("##0.######");
        DecimalFormatSymbols dfs = vertexFormat.getDecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        vertexFormat.setDecimalFormatSymbols(dfs);

        this.writer = writer;
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        this.writeTexture = writeTexture;
    }

    /**
     * Writes a value to the grid file. Throws an exception if all possible
     * values have already been written.
     * @param v The value to write to the file. Can be NaN or infinite.
     */
    public void write(float v) {
        assertGridNotFull();

        double x = vertexCounter % cols * cellSize;
        double y = (int) (vertexCounter / cols) * cellSize;

        writer.write("v ");
        writer.write(vertexFormat.format(x));
        writer.write(" ");
        writer.write(vertexFormat.format(y));
        writer.write(" ");
        if (Float.isNaN(v) || Float.isInfinite(v)) {
            writer.write(NO_DATA_VALUE);
        } else {
            writer.write(vertexFormat.format(v));
        }

        writer.write(NL);
        ++vertexCounter;

        if (vertexCounter == cols * rows) {
            writeTrailer();
        }
    }

    private void writeTrailer() {

        // write texture coordinates
        if (writeTexture) {
            for (int r = 0; r < rows; r++) {
                double y = (double) r / (rows - 1);
                for (int c = 0; c < cols; c++) {
                    double x = (double) c / (cols - 1);
                    writer.write("vt ");
                    writer.write(vertexFormat.format(x));
                    writer.write(" ");
                    writer.write(vertexFormat.format(y));
                    writer.write(NL);
                }
            }
        }

        // write faces
        for (int r = 1; r < rows; r++) {
            for (int c = 1; c < cols; c++) {
                // uper left triangle
                int id = c + (r - 1) * cols;	// id of top left vertex
                writeTriangle(id, id + cols, id + 1);
                // lower right triangle
                writeTriangle(id + cols, id + cols + 1, id + 1);
            }
        }
    }

    private void writeTriangle(int ID1, int ID2, int ID3) {
        writer.write("f ");
        writer.write(vertexFormat.format(ID1));
        if (writeTexture) {
            writer.write("/");
            writer.write(vertexFormat.format(ID1));
        }
        writer.write(" ");
        writer.write(vertexFormat.format(ID2));
        if (writeTexture) {
            writer.write("/");
            writer.write(vertexFormat.format(ID2));
        }
        writer.write(" ");
        writer.write(vertexFormat.format(ID3));
        if (writeTexture) {
            writer.write("/");
            writer.write(vertexFormat.format(ID3));
        }
        writer.write(NL);
    }

    /**
     * Make sure not all values have already been written to the grid file.
     */
    private void assertGridNotFull() {
        if (vertexCounter >= cols * rows) {
            throw new IllegalStateException("grid is written completely");
        }
    }
}