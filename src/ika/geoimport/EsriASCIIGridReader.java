package ika.geoimport;

import ika.geo.GeoGrid;
import ika.gui.ProgressIndicator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EsriASCIIGridReader {

    public static void main(String[] args) {
        try {
            String filePath = "/Users/jennyb/Documents/3D Cartography/Plan Oblique Relief/Local Exaggeration/data/out.asc";

            long startTime = System.nanoTime();
            GeoGrid grid = EsriASCIIGridReader.read(filePath);
            long endTime = System.nanoTime();
            ika.geo.GeoImage geoImage = new ika.geo.grid.GridToImageOperator().operate(grid);
            ika.utils.ImageUtils.displayImageInWindow(geoImage.getBufferedImage());
            
            System.out.println((endTime - startTime) / 1000 / 1000);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    volatile private Exception producerConsumerException = null;
    
    private EsriASCIIGridReader() {
    }

    /**
     * Returns whether a reader references valid data that can be read.
     * @param br
     * @return
     */
    public static boolean canRead(BufferedReader br) {
        try {
            GridHeaderImporter header = new GridHeaderImporter();
            header.readHeader(br, true);
            return header.isValid();
        } catch (IOException exc) {
            return false;
        }
    }

    /**
     * Returns whether a file references valid data that can be rea
     * @param filePath
     */
    public static boolean canRead(String filePath) {
        BufferedReader br = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            InputStreamReader in = new InputStreamReader(fis);
            br = new BufferedReader(in);
            return EsriASCIIGridReader.canRead(br);
        } catch (FileNotFoundException exc) {
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable exc) {
                }
            }
        }
    }

    /** Read a Grid from a file in ESRI ASCII format.
     * @param filePath The path to the file to be read.
     * @return The read grid.
     * @throws java.io.IOException
     */
    public static GeoGrid read(String filePath) throws java.io.IOException {
        return EsriASCIIGridReader.read(filePath, null);
    }

    /** Read a Grid from a file in ESRI ASCII format.
     * @param filePath The path to the file to be read.
     * @param progressIndicator A WorkerProgress to inform about the progress.
     * @return The read grid.
     * @throws java.io.IOException
     */
    public static GeoGrid read(String filePath, ProgressIndicator progressIndicator)
            throws java.io.IOException {

        File file = new File(filePath);
        InputStream fis = new FileInputStream(file.getAbsolutePath());
        EsriASCIIGridReader esriReader = new EsriASCIIGridReader();
        GeoGrid grid = esriReader.read(fis, progressIndicator);
        if (progressIndicator != null && progressIndicator.isAborted()) {
            return null;
        }
        String name = file.getName();
        if (!"".equals(name)) {
            grid.setName(name);
        }
        return grid;
    }

    /** Read a grid from an InputStream.
     * @param input The stream to read from.
     * @param progressIndicator A WorkerProgress to inform about the progress.
     * @return The read grid.
     * @throws java.io.IOException
     */
    public static GeoGrid readStream(InputStream input, ProgressIndicator progressIndicator)
            throws IOException {
        
        EsriASCIIGridReader esriReader = new EsriASCIIGridReader();
        GeoGrid grid = esriReader.read(input, progressIndicator);
        if (progressIndicator != null && progressIndicator.isAborted()) {
            return null;
        }
        return grid;

    }

    /** Read a grid from a stream in ESRI ASCII format.
     * @param input The stream to read from. The stream is closed at the end.
     * @param progressIndicator A WorkerProgress to inform about the progress.
     * @return The read grid.
     * @throws java.io.IOException
     */
    public GeoGrid read(InputStream input, ProgressIndicator progressIndicator)
            throws IOException {

        // initialize the progress monitor at the beginning
        if (progressIndicator != null) {
            progressIndicator.start();
        }

        BufferedReader br = null;

        try {
            InputStreamReader in = new InputStreamReader(input);
            br = new BufferedReader(in);
            GridHeaderImporter header = new GridHeaderImporter();
            String firstGridLine = header.readHeader(br, true);
            GeoGrid grid = new GeoGrid(header.getCols(), header.getRows(), header.getCellSize());
            grid.setWest(header.getWest());
            grid.setNorth(header.getSouth() + (header.getRows() - 1) * header.getCellSize());

            // http://www.java2s.com/Code/Java/Threads/ProducerconsumerforJ2SE15usingconcurrent.htm
            BlockingQueue<String> q = new LinkedBlockingQueue<String>(64);
            q.put(firstGridLine);
            
            Producer producer = new Producer(q, br);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            
            Consumer consumer = new Consumer(q, grid, header.getNoDataValue(), progressIndicator);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            
            try {
                producerThread.join();
            } catch (InterruptedException ex) {
                consumerThread.interrupt();
            }
            consumerThread.join();
            
            return grid;
        } catch (InterruptedException ex) {
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException exc) {
            }
            
            if (producerConsumerException != null) {
                throw new IOException(producerConsumerException.getMessage());
            }
        }

    }

    /**
     * Indicates end of file.
     */
    private static final String EOF = "END_OF_FILE";
    
    /**
     * Reads the grid body line by line.
     */
    private class Producer implements Runnable {

        private final BlockingQueue<String> queue;
        private final BufferedReader reader;

        Producer(BlockingQueue<String> queue, BufferedReader reader) {
            this.queue = queue;
            this.reader = reader;
        }

        public void run() {
            try {
                // read file line by line and store read lines in blocking queue
                String line;
                while ((line = reader.readLine()) != null
                        // check whether this thread has been interrupted
                        && !Thread.currentThread().isInterrupted()
                        // check whether consumer thread has encountered an exception
                        && producerConsumerException == null) {
                    queue.put(line);
                }
                
                // add end-of-file object
                queue.put(EOF);
            } catch (IOException ex) {
                // store the exception for the main thread
                producerConsumerException = ex;
            } catch (InterruptedException ex) {
                // store the exception for the main thread
                producerConsumerException = ex;
            }
        }
    }

    /**
     * Parses grid lines.
     */
    private class Consumer implements Runnable {

        private final BlockingQueue<String> queue;
        private final float noDataValue;
        private final GeoGrid grid;
        private final ProgressIndicator progressIndicator;
        private int counter = 0;

        Consumer(BlockingQueue<String> queue, GeoGrid grid, float noDataValue, ProgressIndicator progressIndicator) {
            this.queue = queue;
            this.grid = grid;
            this.noDataValue = noDataValue;
            this.progressIndicator = progressIndicator;
        }

        public void run() {
            try {
                final int nCols = grid.getCols();
                final int nRows = grid.getRows();
                final int nbrValues = nRows * nCols;
                do {
                    String str = queue.take();
                    
                    // test for end of file
                    if (EOF.equals(str)) {
                        // make sure the correct number of values has been read
                        if (counter != nbrValues) {
                            throw new IOException("invalid Esri Ascii grid file");
                        }
                        break;
                    }
                    
                    // split each line in tokens and parse the tokens for a float.
                    // One row in the grid might not correspond to a grid row.
                    StringTokenizer tokenizer = new StringTokenizer(str, " \t");
                    while (tokenizer.hasMoreTokens()
                            // check whether this thread has been interrupted
                            && !Thread.currentThread().isInterrupted()
                            // check whether producer thread has encountered an exception
                            && producerConsumerException == null) {
                        int col = counter % nCols;
                        int row = counter / nCols;
                        ++counter;
                        
                        // make sure we do not read too many cell values
                        if (counter > nbrValues) {
                            throw new IOException("corrupt Esri Ascii grid file");
                        }
                        float v = Float.parseFloat(tokenizer.nextToken());
                        grid.setValue(v == noDataValue ? Float.NaN : v, col, row);
                    }
                    
                    // update progress info
                    if (progressIndicator != null) {
                        int row = counter / nCols;
                        int perc = (int) ((double) (row + 1) / nRows * 100);
                        if (!progressIndicator.progress(perc)) {
                            counter = nbrValues;
                            break;
                        }
                    }
                } while (counter < nbrValues
                        // check whether this thread has been interrupted
                        && !Thread.currentThread().isInterrupted()
                        // check whether producer thread has encountered an exception
                        && producerConsumerException == null);
            } catch (IOException ex) {
                // store the exception for the main thread
                producerConsumerException = ex;
            } catch (InterruptedException ex) {
                // store the exception for the main thread
                producerConsumerException = ex;
            } catch (NumberFormatException ex) {
                // store the exception for the main thread
                producerConsumerException = ex;
            }
        }
    }
}
