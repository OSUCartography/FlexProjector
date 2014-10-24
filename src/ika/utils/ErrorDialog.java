/*
 * ErrorDialog.java
 *
 * Created on January 9, 2006, 10:37 AM
 */
package ika.utils;

import javax.swing.*;
import java.awt.*;

/**
 * A utility class with static methods to display simple error dialogs.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ErrorDialog {

    /**
     * Displays a simple error dialog to let the user know about some bad
     * situation.
     * Makes sure the dialog is displayed in the event dispatch thread. Does not
     * throw any exception.
     * @param msg The message that is displayed by the dialog.
     * @param title The title of the dialog.
     * @param e If e is not null, its message is appended on a new line to msg.
     * @param parentComponent The frame for which the dialog is displayed.
     */
    public static void showErrorDialog(final String msg, 
            final String title,
            final Throwable e,
            Component parentComponent) {

        try {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println(title);
                System.err.println(msg);
                System.err.println(e);
                return;
            }

            // try finding a parent component if none is specified
            if (parentComponent == null) {
                parentComponent = ika.gui.GUIUtil.getFrontMostFrame();
            }
            final Component component = parentComponent;

            Runnable r = new Runnable() {
                public void run() {
                    String message = msg;
                    if (e != null && e.getMessage() != null) {
                        String newline = System.getProperty("line.separator");
                        message += newline + e.getClass().getName() + ": " + e.getMessage().trim();
                    }
                    JOptionPane.showMessageDialog(component, message,
                            title, JOptionPane.ERROR_MESSAGE);
                }
            };
            
            // make sure we run in the event dispatch thread.
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }

        } catch (Exception exc) {
        }
        
    }

    /**
     * Displays a simple error dialog to let the user know about some bad
     * situation.
     * Makes sure the dialog is displayed in the event dispatch thread.
     * @param msg The message that is displayed by the dialog.
     * @param title The title of the dialog.
     */
    public static void showErrorDialog(String msg, String title) {
        ErrorDialog.showErrorDialog(msg, title, null, null);
    }

    /**
     * Displays a simple error dialog to let the user know about some bad
     * situation. Uses a default title "Error".
     * Makes sure the dialog is displayed in the event dispatch thread.
     * @param msg The message that is displayed by the dialog.
     */
    public static void showErrorDialog(String msg) {
        ErrorDialog.showErrorDialog(msg, "Error", null, null);
    }

    /**
     * Displays a simple error dialog to let the user know about some bad
     * situation. Uses a default title "Error".
     * Makes sure the dialog is displayed in the event dispatch thread.
     * @param msg The message that is displayed by the dialog.
     * @param parentComponent Determines the frame for which the dialog is displayed.
     */
    public static void showErrorDialog(String msg, Component parentComponent) {
        ErrorDialog.showErrorDialog(msg, "Error", null, parentComponent);
    }

    /**
     * Displays a simple error dialog to let the user know about some bad
     * situation. Uses a default title "Error".
     * Makes sure the dialog is displayed in the event dispatch thread.
     * @param msg The message that is displayed by the dialog.
     * @param e The Exception that occured.
     */
    public static void showErrorDialog(String msg, Throwable e) {
        ErrorDialog.showErrorDialog(msg, "Error", e, null);
    }
}