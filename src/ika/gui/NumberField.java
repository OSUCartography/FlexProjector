/*
 * NumberField.java
 *
 * Created on September 15, 2005, 10:44 AM
 *
 */

package ika.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.text.*;
import java.awt.event.*;
import java.lang.reflect.*;

/**
 * A field to enter a floating point number. The user is forced to enter only
 * characters that can be interpreded as floating point number.
 * Use propertyChange events for listening to changes of the value. Test for
 * the name of the event, which is "value" when the value changes (a lot of other
 * types of property change events are sent too):
 * <code>if ("value".equals(evt.getPropertyName())) {do something}</code>
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class NumberField extends JFormattedTextField {
    
    /**
     * The default formatting pattern.
     */
    private static final String DEFAULT_PATTERN = "#,##0.###";
    
    private String pattern = DEFAULT_PATTERN;
    
    /**
     * The default value displayed in the NumberField is 0.
     */
    private static final double DEFAULT_VALUE = 0;
   
    /**
     * Select the text completely when focus is gained.
     */
    private boolean selectTextOnFocusGain = true;
    
    /** Creates a new instance of NumberField */
    public NumberField() {
        
        // create a formatter for displaying and editing
        InternationalFormatter formatter = new MinMaxFormatter();
        
        // specifiy the number format
        DecimalFormat decimalFormat = new DecimalFormat(DEFAULT_PATTERN);
        // require US format. This avoids a lot of problems.
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(java.util.Locale.US));
        formatter.setFormat(decimalFormat);
        
        // allow all possible double values as default range of allowed values.
        formatter.setMinimum(new Double(-Double.MAX_VALUE));
        formatter.setMaximum(new Double(Double.MAX_VALUE));
        
        // allow the user to temporarily enter invalid values. This allows for
        // completely deleting all text
        formatter.setAllowsInvalid(true);
        
        // typing should insert new characters and not overwrite old ones
        formatter.setOverwriteMode(false);
        
        // commit on edit TRUE: after every change that results in a new valid value,
        // a property change event is sent. This can generate property change events
        // when the text changes, but not the value represented by the text.
        // commit on edit FALSE: property change event is only sent when the user
        // confirms the number by pressin enter, return or tab, or when the
        // component loses the focus, e.g. by a mouse click.
        formatter.setCommitsOnValidEdit(false);
        
        // getValue must return a Double object
        formatter.setValueClass(java.lang.Double.class);
        
        // the kind of formatter getFormatter should return
        this.setFormatterFactory(new DefaultFormatterFactory(formatter));
        
        // default value is 0
        this.setValue(new Double(DEFAULT_VALUE));
        
        // Add a key listener that updates the text after return key strokes.
        // The JFormattedTextField leaves the value unchanged after a return
        // key stroke. In <code>NumberField</code> the value can change after
        // a return key stroke if the value is not within the allowed boundaries
        // of min/max. Without this key listener, the value is adjusted,
        // a property change event with the correct new value is sent, but
        // the displayed text is not updated to the new value.
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        // get the value adjusted to min/max
                        Object value = getFormatter().stringToValue(getText());
                        // set the new value
                        setValue(value);
                    } catch (ParseException evt) {}
                    
                    // select the text completely if required.
                    if (isSelectTextOnFocusGain()) {
                        setCaretPosition(0);
                        moveCaretPosition(getText().length());
                    }
                }
            }
        });
    }
    
    /**
     * Returns the current formatting pattern.
     * For a description see:
     * http://java.sun.com/j2se/1.4.2/docs/api/java/text/DecimalFormat.html
     * @return The formatting pattern.
     */
    public String getPattern() {
        return this.pattern;
        /*
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        DecimalFormat decF = (DecimalFormat)f.getFormat();
        return decF.toPattern();
        */
    }
    
    /**
     * Set the formatting pattern.
     * For a description see:
     * http://java.sun.com/j2se/1.4.2/docs/api/java/text/DecimalFormat.html
     * @param pattern The new formatting pattern.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        DecimalFormat decF = (DecimalFormat)f.getFormat();
        decF.applyPattern(pattern);
    }
    
    /**
     * Returns the minimum value required. Values smaller than the minimum
     * will not be accpeted.
     * @return The miminum value.
     */
    public double getMin() {
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        return ((Double)f.getMinimum()).doubleValue();
    }
    
    /**
     * Set the minimum value required. Values smaller than the minimum
     * will not be accpeted.
     * @param min The minimum value.
     */
    public void setMin(double min) {
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        f.setMinimum(new Double(min));
    }
    
    /**
     * Returns the maximum value required. Values larger than the maximum
     * will not be accpeted.
     * @return The maximum value.
     */
    public double getMax() {
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        return ((Double)f.getMaximum()).doubleValue();
    }
    
    /**
     * Set the maximum value required. Values larger than the maximum
     * will not be accpeted.
     * @param max The maximum value.
     */
    public void setMax(double max) {
        InternationalFormatter f = (InternationalFormatter)this.getFormatter();
        f.setMaximum(new Double(max));
    }
    
    /**
     * Returns the current value.
     * @return The current value entered by the user.
     */
    public double getDoubleValue() {
        return ((Double)this.getValue()).doubleValue();
    }
    
    /**
     * Sets the value.
     * @param value The new value.
     */
    public void setDoubleValue(double value) {
        this.setValue(new Double(value));
    }
    
    public void setText(String text) {

        Double value = null;
        try {
            // get the value adjusted to min/max
            value = (Double)this.getFormatter().stringToValue(text);
        } catch (Exception e) {
            value = new Double(this.DEFAULT_VALUE);
        } finally {
            super.setText(value.toString());
        }

    }
    
    /**
     * Overwrite processFocusEvent of JFormattedTextField to select all text on
     * focus gain.
     */
    protected void processFocusEvent(java.awt.event.FocusEvent e) {
        super.processFocusEvent(e);
        
        // ignore temporary focus event
        if (e.isTemporary()) {
            return;
        }
        
        // select the text completely if required.
        if (this.isSelectTextOnFocusGain() &&
                e.getID() == FocusEvent.FOCUS_GAINED) {
            this.setCaretPosition(0);
            this.moveCaretPosition(this.getText().length());
        }
    }
    
    /**
     * Returns whether the text is completely selected when focus is gained.
     * @return True when all text is selected on focus gain.
     */
    public boolean isSelectTextOnFocusGain() {
        return selectTextOnFocusGain;
    }
    
    /**
     * Set whether the text is completely selected when focus is gained.
     * @param selectTextOnFocusGain When true all text is selected on focus gain.
     */
    public void setSelectTextOnFocusGain(boolean selectTextOnFocusGain) {
        this.selectTextOnFocusGain = selectTextOnFocusGain;
    }
    
    /**
     * MinMaxFormatter is a formatter for numbers that limits the allowed
     * values to a range between a minimum and a maximum. It extends
     * InternationalFormatter which offers this functionality.
     * InternationalFormatter does not adjust the value to the min or max if
     * it is smaller or greater as MinMaxFormatter does. InternationalFormatter
     * throws an exception instead.
     */
    private class MinMaxFormatter extends InternationalFormatter {
        
        /**
         * Returns the <code>Object</code> representation of the
         * <code>String</code> <code>text</code>.
         *
         * @param string <code>String</code> to convert
         * @return <code>Object</code> representation of text
         * @throws ParseException if there is an error in the conversion
         */
        public Object stringToValue(String string) throws ParseException {
            
            Comparable minimum = this.getMinimum();
            Comparable maximum = this.getMaximum();
            
            // use overwritten stringToValue of super class if min/max
            // are not specified.
            if (minimum == null || maximum == null) {
                return super.stringToValue(string);
            }
            
            Format format = this.getFormat();
            Class valueClass = this.getValueClass();
            Object value = format.parseObject(string);
            
            // Convert to the value class if the value returned from the
            // Format does not match.
            // This will convert from Long to Double, for example.
            if (value != null && getValueClass() != null &&
                    !getValueClass().isInstance(value)) {
                value = this.stringToValueClass(value.toString());
            }
            
            // Check for minimum and maximum.
            // If value is out of bounds, return minimum or maximum.
            if (minimum != null && minimum.compareTo(value) > 0) {
                // return a copy of minimum and not a reference.
                value = this.stringToValueClass(minimum.toString());
            }
            if (maximum != null && maximum.compareTo(value) < 0) {
                // return a copy of maximum and not a reference.
                value = this.stringToValueClass(maximum.toString());
            }
            
            return value;
        }
        
        /**
         * This is a copy of Object stringToValue(String string), the method
         * of super.super (=DefaultFormatter) that this class overwrites.
         * This method must be called to convert a string to an instance of
         * <code>getValueClass</code>.
         * It has been renamed to stringToValueClass.
         * --------------------------------------------------------
         * Converts the passed in String into an instance of
         * <code>getValueClass</code> by way of the constructor that
         * takes a String argument. If <code>getValueClass</code>
         * returns null, the Class of the current value in the
         * <code>JFormattedTextField</code> will be used. If this is null, a
         * String will be returned. If the constructor thows an exception, a
         * <code>ParseException</code> will be thrown. If there is no single
         * argument String constructor, <code>string</code> will be returned.
         *
         * @throws ParseException if there is an error in the conversion
         * @param string String to convert
         * @return Object representation of text
         */
        public Object stringToValueClass(String string) throws ParseException {
            Class vc = getValueClass();
            JFormattedTextField ftf = getFormattedTextField();
            
            if (vc == null && ftf != null) {
                Object value = ftf.getValue();
                
                if (value != null) {
                    vc = value.getClass();
                }
            }
            if (vc != null) {
                Constructor cons;
                
                try {
                    cons = vc.getConstructor(new Class[] { String.class });
                    
                } catch (NoSuchMethodException nsme) {
                    cons = null;
                }
                
                if (cons != null) {
                    try {
                        return cons.newInstance(new Object[] { string });
                    } catch (Throwable ex) {
                        throw new ParseException("Error creating instance", 0);
                    }
                }
            }
            return string;
        }
    }
}