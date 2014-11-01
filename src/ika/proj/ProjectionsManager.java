/*
 * ProjectionsManager.java
 *
 * Created on September 30, 2007, 7:12 PM
 *
 */

package ika.proj;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.CylindricalProjection;
import com.jhlabs.map.proj.PseudoCylindricalProjection;
import ika.app.ApplicationInfo;
import ika.gui.FlexProjectorWindow;
import ika.utils.PropertiesLoader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * A central store for projections that are used by the distortion table, 
 * the reset functionality and the background projection. Projections can be 
 * selected in the preferences panel. Projections that are not selected will 
 * not appear as an option in the GUI. 
 * Projections with their selection state are read from a file or from the 
 * system-wide preferences. They can also be written to the system preferences.
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich
 */
public class ProjectionsManager {

    /**
     * String to display in GUI for selecting files storing the definition of a
     * flex projection
     */
    public static final String SELECT_FLEX_FILE_STRING = "Flex Projection (from external file)";
    
    /**
     * properties file that contains the path to the file with all projections.
     */
    private static final String PROPERTIES_PATH = "ika.app.Application.properties";
    
    /**
     * Key for the file path.
     */
    private static final String PROPERTY_KEY = "Projections";
    
    /**
     * Key for storing the projections and their selection state in the
     * preferences.
     */
    private static final String PREFS_KEY = "Selected_Projections";
    
    /**
     * This string is appended to a projection name if a flex projection only
     * approximates the projection.
     */
    public static final String APPROXIMATED_INFO = " (approximated)";
    
    /**
     * An array containing all projections and their selection state.
     */
    private static ArrayList<ProjectionSelection> list;

    /**
     * ProjectionSelection associates a projection name and a selection state.
     */
    private static class ProjectionSelection {
        
        public ProjectionSelection(String projName, boolean selected, boolean approximated) {
            this.projName = projName;
            this.selected = selected;
            this.approximated = approximated;
        }

        public ProjectionSelection(String[] desc) {
            this.projName = desc[0].trim();
            this.selected = Boolean.parseBoolean(desc[1].trim());
            this.approximated = desc.length > 2 && "approx".equals(desc[2].trim());
        }
        
        public String projName;
        public boolean selected;
        public boolean approximated;
        
    }
    
    private ProjectionsManager() {
    }
    
    private static void loadProjections(){
        if (ProjectionsManager.list != null)
            return;
        ProjectionsManager.loadProjectionsFromFile();
        ProjectionsManager.loadProjectionsSelectionFromPreferences();
    }
    
    private static void loadProjectionsSelectionFromPreferences(){
        try {
            Preferences prefs = Preferences.userNodeForPackage(ProjectionsManager.class);
            String versionStr = prefs.get("VERSION", null);
            if (versionStr == null) {
                prefs.put("VERSION", ApplicationInfo.getApplicationVersion());
                return;
            }
            
            String[] projs = prefs.get(PREFS_KEY, null).split(",");
            for (int i = 0; i < projs.length / 2; i++) {
                String name = projs[i * 2];
                boolean selected = Boolean.parseBoolean(projs[i * 2 + 1]);
                ProjectionsManager.setProjectionSelected(name, selected);
            }
        } catch (Exception e) {
        }
    }
    
    public static void writeProjectionsToPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(ProjectionsManager.class);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).projName);
            sb.append(',');
            sb.append(list.get(i).selected);
            sb.append(',');
        }
        prefs.put(PREFS_KEY, sb.toString());
    }
    
    private static void loadProjectionsFromFile(){
        BufferedReader reader = null;
        try {
            ProjectionsManager.list = new ArrayList<ProjectionSelection>();
            
            // load projection description
            Properties props = PropertiesLoader.loadProperties(PROPERTIES_PATH);
            String mapData = props.getProperty(PROPERTY_KEY);
            java.net.URL url = FlexProjectorWindow.class.getResource(mapData);
            
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            InputStreamReader isr = new InputStreamReader(bis, "UTF-8");
            reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] projDesc = line.split(",");
                ProjectionsManager.list.add(new ProjectionSelection(projDesc));
            }
        } catch (IOException e) {
            e.printStackTrace();
            ProjectionsManager.list = null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public static int getAvailableProjectionsCount() {
        ProjectionsManager.loadProjections();
        return ProjectionsManager.list.size();
    }
    
    public static int getSelectedProjectionsCount() {
        ProjectionsManager.loadProjections();
        int counter = 0;
        final int projCount = ProjectionsManager.list.size();
        for (int i = 0; i < projCount; i++) {
            if (ProjectionsManager.list.get(i).selected)
                ++counter;
        }
        return counter;
    }
    
    public static String getProjectionName(int id) {
        ProjectionsManager.loadProjections();
        return ProjectionsManager.list.get(id).projName;
    }
    
    public static List<String> getSelectedProjectionNames() {
        ProjectionsManager.loadProjections();
        if (ProjectionsManager.list == null)
            return null;
        ArrayList<String> selectedProjs = new ArrayList<String>();
        for (ProjectionSelection projSel : ProjectionsManager.list) {
            if (projSel.selected)
                selectedProjs.add(projSel.projName);
        }
        return selectedProjs;
        
    }
    
    /**
     * Returns a list of selected projection names with an optional string appended
     * that indicates whether a flex projection will only approximate the 
     * projections. Does not include the Mercator projection. The first entry
     * is for loading an external flex file.
     * @param labelApproximated If true approximated projections are labeled.
     * @param onlyCylindrical If true only cylindrical projections are included.
     * @param includeExternalFlexProjectorFile If true an option is added for 
     * loading external Flex Projector files.
     * @return
     */
    public static List<String> getProjectionNames(boolean labelApproximated,
            boolean onlyCylindrical, boolean includeExternalFlexProjectorFile) {

        ProjectionsManager.loadProjections();
        
        ArrayList<String> selectedProjs = new ArrayList<String>();
        for (ProjectionSelection projSel : list) {
            if (projSel.selected) {
                String name = projSel.projName;
                Projection proj = ProjectionsManager.getProjection(name);
                if (proj == null) {
                    continue;
                }
                if (onlyCylindrical) {
                    boolean isCylindrical = proj instanceof CylindricalProjection;
                    boolean isPseudoCylindrical = proj instanceof PseudoCylindricalProjection;
                    if (!isCylindrical && !isPseudoCylindrical) {
                        continue;
                    }
                }
                if (labelApproximated && projSel.approximated)
                    name += APPROXIMATED_INFO;
                selectedProjs.add(name);
            }
        }

        Collections.sort(selectedProjs);

        // remove Mercator projection which has the poles at infinity and
        // hence cannot be approximated.
        selectedProjs.remove("Mercator");

        // first entry is for external Flex Projection files.
        if (includeExternalFlexProjectorFile) {
            selectedProjs.add(0, SELECT_FLEX_FILE_STRING);
        }

        return selectedProjs;

    }

    public static Projection getProjection(String projName) {

        // remove " (approximated)" from name
        int approxIndex = projName.indexOf(ProjectionsManager.APPROXIMATED_INFO);
        if (approxIndex != -1) {
            projName = projName.substring(0, approxIndex);
        }
        return com.jhlabs.map.proj.ProjectionFactory.getNamedProjection(projName);

    }

    public static boolean isProjectionSelected(int id) {
        ProjectionsManager.loadProjections();
        return ProjectionsManager.list.get(id).selected;
    }
    
    public static boolean isProjectionSelected(String name) {
        ProjectionsManager.loadProjections();
        final int id = ProjectionsManager.findProjectionID(name);
        return ProjectionsManager.list.get(id).selected;
    }
    
    public static void setProjectionSelected(int id, boolean selected) {
        ProjectionsManager.loadProjections();
        ProjectionsManager.list.get(id).selected = selected;
    }
    
    public static void setProjectionSelected(String name, boolean selected) {
        ProjectionsManager.loadProjections();
        final int id = ProjectionsManager.findProjectionID(name);
        if (id != -1)
            ProjectionsManager.list.get(id).selected = selected;
    }
    
    private static int findProjectionID(String name) {
        final int projCount = ProjectionsManager.getAvailableProjectionsCount();
        for (int i = 0; i < projCount; i++) {
            if (ProjectionsManager.list.get(i).projName.equals(name))
                return i;
        }
        return -1;
    }

    public static void selectDefaultProjections() {
        ProjectionsManager.loadProjectionsFromFile();
    }

    public static void selectAllProjections() {
        ProjectionsManager.loadProjections();
        for (ProjectionSelection sel : ProjectionsManager.list) {
            sel.selected = true;
        }
    }

    public static void selectNoProjections() {
        ProjectionsManager.loadProjections();
        for (ProjectionSelection sel : ProjectionsManager.list) {
            sel.selected = false;
        }
    }
    
}
