/*
 * FileUtil.java
 *
 * Created on April 1, 2005, 11:31 AM
 */
package ika.utils;

import java.io.*;
import java.awt.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * FileUtils - file related utility methods.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class FileUtils {

    private static final boolean IS_MAC_OSX;

    static {
        String osname = System.getProperty("os.name");
        IS_MAC_OSX = osname.toLowerCase().startsWith("mac os x");
    }

    /**
     * Makes sure that a given name of a file has a certain file extension.<br>
     * Existing file extension that are different from the required one are not
     * removed. The fileName is trimmed in any case (i.e. leading and trailing
     * non-printable characters are remmoved).
     *
     * @param fileName The name of the file.
     * @param ext The extension of the file that will be appended if necessary.
     * @return The trimmed file name with the required extension.
     */
    public static String forceFileNameExtension(String fileName, String ext) {
        if (fileName == null) {
            return null;
        }
        if (ext == null) {
            return fileName;
        }
        fileName = fileName.trim();
        ext = ext.trim();

        String fileNameLower = fileName.toLowerCase();
        String extLower = ext.toLowerCase();

        // test if the fileName has the required extension
        if (!fileNameLower.endsWith("." + extLower)) {

            // fileName has wrong extension: add an extension
            if (!fileNameLower.endsWith(".")) {
                fileName = fileName.concat(".");
            }
            fileName = fileName.concat(ext);   // add extension
        }
        return fileName;
    }

    /**
     * Test whether a file name ends with a specific extension.
     *
     * @param fileName The path to the file.
     * @param ext The required extension.
     * @return True if the file ends with the required extension, false
     * otherwise.
     */
    public static boolean hasExtension(String fileName, String ext) {
        if (fileName == null || ext == null) {
            return false;
        }
        String fileNameLower = fileName.trim().toLowerCase();
        String extLower = ext.trim().toLowerCase();
        return fileNameLower.endsWith("." + extLower);
    }

    /**
     * If the file at filePath exists, a warning message is displayed to the
     * user and true is returned. This method is intended to be used after a
     * dialog for specifying the name and path of a new file. If an extension is
     * required, but the user does not enter an extension, the extension can be
     * added programmatically. If this new concatenated file path conflicts with
     * an existing file, a warning should be displayed and the operation should
     * be aborted.
     *
     * @param filePath The path to the file.
     * @param ext The user is informed to append this extension to the file name
     * when entering a name for a new file.
     * @return True if the file exists, false otherwise.
     */
    public static boolean warningIfFileExists(String filePath, String ext) {

        String newline = System.getProperty("line.separator");

        if (filePath == null) {
            throw new IllegalArgumentException();
        }

        filePath = filePath.trim();
        String fileName = FileUtils.getFileName(filePath);
        String parentDirectoryPath = FileUtils.getParentDirectoryPath(filePath);
        if (ext != null) {
            ext = ext.trim();
        }
        boolean fileExists = new File(filePath).exists();
        if (fileExists) {
            StringBuilder sb = new StringBuilder();
            sb.append("The file \"");
            sb.append(fileName);
            sb.append("\" already exists at").append(newline);
            sb.append(parentDirectoryPath);
            sb.append(".").append(newline);
            sb.append("Please try again");
            if (ext != null) {
                sb.append(" and add the extension \".");
                sb.append(ext);
                sb.append("\" to the file name");
            }
            sb.append(".");

            String title = "File Already Exists";
            ika.utils.ErrorDialog.showErrorDialog(sb.toString(), title);
        }
        return fileExists;
    }

    /**
     * Returns the file extension from a passed file path.
     */
    public static String getFileExtension(String fileName) {
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return new String();
        }
        return fileName.substring(dotIndex + 1);
    }

    /**
     * Change the extension of a file path. The extension is what follows the
     * last dot '.' in the path. If no dot exists in the path, the passed
     * extension is simply appended without replacing anything.
     *
     * @param filePath The path of the file with the extension to replace.
     * @param newExtension The new extension for the file, e.g. "tif".
     * @return A new path to a file. The file may not actually exist on the hard
     * disk.
     */
    public static String replaceExtension(String filePath, String newExtension) {
        final int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex == -1) {
            return filePath + "." + newExtension;
        }
        return filePath.substring(0, dotIndex + 1) + newExtension;
    }

    /**
     * Change the extension of a file path. The extension is what follows the
     * last dot '.' in the path. If no dot exists in the path, the passed
     * extension is simply appended without replacing anything.
     *
     * @param filePath The path of the file with the extension to replace.
     * @param newExtension The new extension for the file, e.g. "tif".
     * @param maxExtensionLength The maximum length of the extension to remove.
     * @return A new path to a file. The file may not actually exist on the hard
     * disk!
     */
    public static String replaceExtension(String filePath, String newExtension,
            int maxExtensionLength) {
        filePath = FileUtils.cutFileExtension(filePath, maxExtensionLength);
        return FileUtils.replaceExtension(filePath, newExtension);
    }

    /**
     * Removes the path to the parent folder and also the extension of a file
     * path.
     *
     * @return The name of the file without the path to its parent folder and
     * without the file extension.
     */
    public static String getFileNameWithoutExtension(String filePath) {
        return getFileName(cutFileExtension(filePath));
    }

    /**
     * Removes the path to the parent folder.
     *
     * @return The name of the file without the path to its parent folder. Null
     * if the passed fileName is null.
     */
    public static String getFileName(String filePath) {

        if (filePath == null) {
            return null;
        }

        // cut the path to the parent folder
        String pathSeparator = System.getProperty("file.separator");
        final int pathSeparatorIndex = filePath.lastIndexOf(pathSeparator);
        if (pathSeparatorIndex != -1) {
            filePath = filePath.substring(pathSeparatorIndex + 1, filePath.length());
        }

        return filePath;

    }

    /**
     * Returns the parent directory for a file.
     *
     * @param filePath Path to a file.
     * @return For "/Volumes/toto/gaga.txt" returns "/Volumes/toto/". Returns
     * filePath if is is a path to a directory.
     */
    public static String getParentDirectoryPath(String filePath) {

        if (filePath == null) {
            return null;
        }

        // cut the path to the parent folder
        String pathSeparator = System.getProperty("file.separator");
        final int pathSeparatorIndex = filePath.lastIndexOf(pathSeparator);
        if (pathSeparatorIndex != -1) {
            filePath = filePath.substring(0, pathSeparatorIndex + 1);
        }

        return filePath;

    }

    /**
     * Removes the extension of a file path. Does not remove extensions longer
     * than 3 characters.
     *
     * @return The name of the file without the file extension.
     */
    public static String cutFileExtension(String fileName) {
        return cutFileExtension(fileName, 3);
    }

    /**
     * Removes the extension of a file path.
     *
     * @param fileName The path to the file.
     * @param maxExtensionLength The maximum length of the extension to remove.
     * If the extension is shorter, it is not removed.
     * @return The name of the file without the file extension.
     */
    public static String cutFileExtension(String fileName, int maxExtensionLength) {
        final int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        final int extensionLength = fileName.length() - dotIndex - 1;
        if (extensionLength <= maxExtensionLength) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    /**
     * Ask the user for a file using the AWT FileDialog.
     */
    private static String askAWTFile(java.awt.Frame frame, String message,
            String defaultFile, boolean load) {
        // use AWT FileDialog on mac
        final int flag = load ? FileDialog.LOAD : FileDialog.SAVE;

        // build dummy Frame if none is passed as parameter.
        if (frame == null) {
            frame = new Frame();
        }

        FileDialog fd = new FileDialog(frame, message, flag);
        fd.setFile(defaultFile);
        fd.setVisible(true);
        String fileName = fd.getFile();
        String directory = fd.getDirectory();
        if (fileName == null || directory == null) {
            return null;
        }
        return directory + fileName;
    }

    /**
     * Returns true if the passed file can be written.
     *
     * @param file
     * @param parent
     * @return
     */
    private static boolean askOverwrite(File file, Component parent) {

        if (file.exists()) {
            StringBuilder sb = new StringBuilder("<html>\"");
            sb.append(file.getName());
            sb.append("\" already exists. Do you want<br>to replace it?</html>");
            String msg = sb.toString();
            String title = "";
            String[] options = new String[]{"Cancel", "Replace"};
            int res = JOptionPane.showOptionDialog(parent,
                    msg,
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            return res == 1;
        }
        return true;
    }

    /**
     * Ask the user for a file using the Swing JFileChooser.
     */
    private static String askSwingFile(java.awt.Frame frame, String message,
            String defaultFile, boolean load) {
        // use Swing FileChooser on other systems
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(message);

        File selFile;
        // set default file
        try {
            File f = new File(new File(defaultFile).getCanonicalPath());
            fc.setSelectedFile(f);
        } catch (Exception e) {
        }

        int result = JFileChooser.ERROR_OPTION;
        do {
            if (load) {
                // Show open dialog
                result = fc.showOpenDialog(frame);
            } else {
                // Show save dialog
                result = fc.showSaveDialog(frame);
            }

            if (result != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            selFile = fc.getSelectedFile();
            if (selFile == null) {
                return null;
            }

        } while (!load && !askOverwrite(selFile, fc));

        return selFile.getPath();
    }

    /**
     * A private utility class that wraps calls to FileUtils.askAWTFile and
     * FileUtils.askSwingFile into a Runnable object.
     */
    private static class GUI implements Runnable {

        public String filePath;
        public Frame frame;
        public String message;
        public String defaultFile;
        public boolean load;
        public String ext;

        public void run() {
            if (FileUtils.IS_MAC_OSX) {
                filePath = FileUtils.askAWTFile(frame, message, defaultFile, load);
            } else {
                filePath = FileUtils.askSwingFile(frame, message, defaultFile, load);
            }

            // append the required file extension if necessary
            if (!load && filePath != null && ext != null) {

                //
                if (new File(filePath).exists() && FileUtils.hasExtension(filePath, ext)) {
                    return;
                }

                // append the extension if necessary
                String extendedFilePath = forceFileNameExtension(filePath, ext);
                if (!extendedFilePath.equals(filePath)) {
                    // make sure there is not a conflict with an existing file
                    if (ika.utils.FileUtils.warningIfFileExists(extendedFilePath, ext)) {
                        filePath = null;
                    } else {
                        filePath = extendedFilePath;
                    }
                }
            }
        }
    };

    /**
     * Ask the user for a file to load or to write to. Uses the awt FileDialog
     * on Mac and the JFileChooser on other platforms. Makes sure the dialog is
     * displayed in the event dispatch thread.
     *
     * @param frame A Frame for which to display the dialog. Cannot be null.
     * @param message A message that will be displayed in the dialog.
     * @param defaultFile The default file name.
     * @param load Pass true if an existing file for reading should be selected.
     * Pass false if a new file for writing should be specified.
     * @param ext A file extension that is required when selecting a file name
     * for saving a file. Can be null.
     * @return A path to the file, including the file name.
     */
    public static String askFile(final java.awt.Frame frame, final String message,
            final String defaultFile, final boolean load, final String ext) {

        GUI gui = new GUI();
        gui.frame = frame;
        gui.message = message;
        gui.defaultFile = defaultFile;
        gui.load = load;
        gui.ext = ext;

        // make sure we run in the event dispatch thread.
        ika.utils.SwingThreadUtils.invokeAndWait(gui);
        return gui.filePath;
    }

    /**
     * Ask the user for a file to load or to write to. Makes sure the dialog is
     * displayed in the event dispatch thread.
     *
     * @param frame A Frame for which to display the dialog. Cannot be null.
     * @param message A message that will be displayed in the dialog.
     * @param load Pass true if an existing file for reading should be selected.
     * Pass false if a new file for writing should be specified.
     * @return A path to the file, including the file name.
     */
    public static String askFile(java.awt.Frame frame, String message, boolean load) {
        return FileUtils.askFile(frame, message, null, load, null);
    }

    public static String askDirectory(java.awt.Frame frame,
            String message,
            boolean load,
            String defaultDirectory) throws IOException {

        if (FileUtils.IS_MAC_OSX) {
            try {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
                FileDialog fd = new FileDialog(frame, message, load ? FileDialog.LOAD : FileDialog.SAVE);
                fd.setFile(defaultDirectory);
                fd.setVisible(true);
                String fileName = fd.getFile();
                String directory = fd.getDirectory();
                return (fileName == null || directory == null) ? null : directory + fileName;
            } finally {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }
        } else {
            JFileChooser fc = new JFileChooser(defaultDirectory);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle(message);
            fc.showOpenDialog(null);

            File selFile = fc.getSelectedFile();
            return selFile.getCanonicalPath();
        }
    }

    /**
     * Converts the contents of a file into a CharSequence suitable for use by
     * the regex package. The matching routines in java.util.regex require that
     * the input be a CharSequence object. This method efficiently returns the
     * contents of a file in a CharSequence object. Based on
     * http://javaalmanac.com/egs/java.util.regex/FromFile.html?l=rel
     *
     * @param filename The file path.
     * @maxNbrBytes The maximum number of bytes that should be read. Pass 0 if
     * all bytes should be read.
     */
    public static CharSequence charSequenceFromFile(String filename, long maxNbrBytes)
            throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        FileChannel fc = fis.getChannel();

        // Create a read-only CharBuffer on the file
        int nbrBytesToRead = (int) Math.max(maxNbrBytes, fc.size());
        ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, nbrBytesToRead);
        CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
        return cbuf;
    }

    /**
     * Converts the contents of a file into a CharSequence suitable for use by
     * the regex package.
     */
    public static CharSequence charSequenceFromFile(String filename)
            throws IOException {
        return FileUtils.charSequenceFromFile(filename, 0);
    }

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new IOException("The file is too large.");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName() + ".");
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public static File createTempDirectory() throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
    }
}