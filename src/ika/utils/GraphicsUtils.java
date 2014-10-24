package ika.utils;

import java.awt.Toolkit;
import java.util.Map;

/**
 *
 * @author jenny
 */
public class GraphicsUtils {

    /**
     * Rendering hints for antialiased text as used by the operating system.
     * Should reflect OS-wide user settings.
     * Apply with g2d.addRenderingHints(GraphicsUtils.antialiasedTextHints);
     */
    public static Map antialiasedTextHints;
    static {
        Toolkit tk = Toolkit.getDefaultToolkit();
        antialiasedTextHints = (Map)(tk.getDesktopProperty("awt.font.desktophints"));
    }
}
