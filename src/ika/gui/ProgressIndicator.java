/*
 * ProgressIndicator.java
 *
 * Created on August 15, 2006, 12:30 PM
 *
 */
package ika.gui;

/**
 * ProgressIndicator defines the methods that an object must implemented to 
 * serve as progress indicator.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public interface ProgressIndicator {

    /**
     * start() should be called when the operation starts.
     */
    public void start();

    /**
     * abort() is called when the operation encounters a problem and
     * cannot continue. The GUI should be cleaned up.
     */
    public void abort();

    /**
     * complete() is called to inform that the operation terminated. The GUI should
     * be cleaned up.
     */
    public void completeProgress();

    /**
     * progress() informs of the progress of the current task.
     * @param percentage A value between 0 and 100
     * @return True if the operation should continue, false if the user canceled
     * the operation.
     */
    public boolean progress(int percentage);

    /**
     * Return whether the user canceled the operation, e.g. by pressing a 
     * Cancel button.
     */
    public boolean isAborted();

    /**
     * don't allow the user to cancel the operation.
     */
    public void disableCancel();

    /**
     * Allow the user to cancel the operation. This is the default setting.
     */
    public void enableCancel();

    /**
     * Display a message to the user. The message can change regularly. HTML is legal.
     * @param msg
     */
    public void setMessage(final String msg);

    /**
     * Sets the number of tasks. Each task has a progress between 0 and 100.
     * If the number of tasks is larger than 1, progress of task 1 will be
     * rescaled to 0..50.
     * @param tasksCount The total number of tasks.
     */
    public void setTotalTasksCount(int tasksCount);

    /**
     * Returns the total numbers of tasks for this progress indicator.
     * @return The total numbers of tasks.
     */
    public int getTotalTasksCount();

    /**
     * Switch to the next task.
     */
    public void nextTask();

    /**
     * Switch to the next task and change the message text.
     */
    public void nextTask (String message);

    /**
     * Returns the ID of the current task. The first task has ID 1 (and not 0).
     * @return The ID of the current task.
     */
    public int currentTask();
}
