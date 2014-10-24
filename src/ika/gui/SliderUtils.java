package ika.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class SliderUtils {

    private SliderUtils() {
    }

    /**
     * Apply the font size to the labels of a slider. This must be called after
     * the labels of a slider are changed.
     *
     * First set up the label hash table and add it to the slider. Then, after
     * the slider has been added to a parent window and had its UI assigned,
     * call this method to change the label sizes.
     *
     * http://nadeausoftware.com/node/93#Settingsliderlabelfontsizes
     * 
     * @param slider
     */
    public static void reapplyFontSize(JSlider slider) {

        Font font = slider.getFont();
        Dictionary dict = slider.getLabelTable();
        Enumeration keys = dict.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Component label = (Component) dict.get(key);
            label.setFont(font);
            label.setSize(label.getPreferredSize());
        }

    }

    /**
     * Set the labels of a slider.
     * @param slider
     * @param values
     * @param labels
     */
    public static void setSliderLabels(JSlider slider, int[] values, String[] labels) {

        assert (slider != null && values != null && labels != null);
        assert (values.length == labels.length);

        Hashtable labelTable = new Hashtable();
        for (int i = 0; i < values.length; i++) {
            assert (values[i] >= slider.getMinimum());
            assert (values[i] <= slider.getMaximum());
            JLabel label = new JLabel(labels[i]);
            labelTable.put(new Integer(values[i]), label);
        }
        slider.setLabelTable(labelTable);
    }

    /**
     * Set the label for the minimum and the maximum value of a slider. The slider
     * will not show values for intermediate labels.
     * @param slider
     * @param labels
     */
    public static void setMinMaxSliderLabels(JSlider slider, String[] labels) {
        setSliderLabels(slider, new int[] {slider.getMinimum(), slider.getMaximum()}, labels);
    }

}
