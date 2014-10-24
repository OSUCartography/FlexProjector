/*
 * GeoSet.java
 *
 * Created on 5. Februar 2005, 16:48
 */
package ika.geo;

import java.awt.geom.*;
import java.io.*;
import java.util.*;

/**
 * GeoSet - an ordered group of GeoObjects.<br>
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public class GeoSet extends GeoObject implements Serializable, Cloneable {

    private static final long serialVersionUID = -8029643397815392824L;
    /**
     * A vector that contains the GeoObjects pertaining to this GeoSet.
     */
    private java.util.Vector vector = new java.util.Vector();
    private boolean grouped = false;

    /**
     * Creates a new instance of GeoSet
     */
    public GeoSet() {
    }

    /**
     * Creates a new instance of GeoSet and adds the passed GeoObject as a child
     * to the new GeoSet.
     *
     * @param child A GeoObject that will be added to the new GeoSet.
     */
    public GeoSet(GeoObject child) {
        this.add(child);
    }

    /**
     * Returns a copy of this GeoObject. Clones the tree of this GeoSet.
     *
     * @return A copy.
     */
    @Override
    public GeoSet clone() {
        try {
            GeoSet copy = (GeoSet) super.clone();

            // clone all children in this GeoSet and add them to the copy
            copy.vector = new Vector(this.vector.size());
            final int nbrChildren = this.getNumberOfChildren();
            for (int i = 0; i < nbrChildren; i++) {
                GeoObject geoObject = this.getGeoObject(i);
                copy.add(geoObject.clone());
            }
            return copy;
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * Copy all selected children to the passed GeoSet.
     *
     * @param geoSet The GeoSet that will receive the copied GeoObjects.
     */
    @Override
    public void cloneIfSelected(GeoSet geoSet) {
        // no trigger here, since we are only reading from this GeoSet.
        if (this.hasSelectedGeoObjects() == false) {
            return;
        }

        GeoSet newGeoSet = new GeoSet();
        geoSet.add(newGeoSet);

        Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            final GeoObject geoObject = (GeoObject) iterator.next();
            geoObject.cloneIfSelected(newGeoSet);
        }
    }

    /**
     * Add a GeoObject to this GeoSet. The object will be inserted after all
     * currently contained GeoObjects.
     *
     * @param geoObject The GeoObject to add.
     */
    public synchronized void add(GeoObject geoObject) {
        this.add(this.vector.size(), geoObject);
    }

    /**
     * Deselect all GeoObjects contained in this GeoSet and insert the passed
     * GeoObject. The object will be inserted after all currently contained
     * GeoObjects.
     */
    public synchronized void deselectAndAdd(GeoObject geoObject) {

        final MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.setSelected(false);
            this.add(this.vector.size(), geoObject);
        } finally {
            trigger.inform(new MapEvent(true, true, true));
        }
    }

    /**
     * Deselect all GeoObjects contained in this GeoSet and insert the children
     * of the passed GeoSet. The passed GeoSet itself is not added. The children
     * will be inserted after all currently contained GeoObjects.
     */
    public synchronized void deselectAndAddChildren(GeoSet geoSet) {

        final MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.setSelected(false);
            final int nbrChildren = geoSet.getNumberOfChildren();
            for (int i = 0; i < nbrChildren; i++) {
                this.add(geoSet.getGeoObject(i));
            }
        } finally {
            trigger.inform(new MapEvent(true, true, true));
        }
    }

    /**
     * Add a GeoObject at a specified index.
     *
     * @param index The position in the internal store, starting with 0.
     * @param geoObject The GeoObject to add.
     */
    public synchronized void add(int index, GeoObject geoObject) {
        if (geoObject == null) {
            return;
        }

        // apply selectable state on the new child
        if (!this.isSelectable()) {
            geoObject.setSelectable(false);
        }

        vector.add(index, geoObject);
        geoObject.setParent(this);

        MapEventTrigger.inform(MapEvent.structureChange(), this);
    }

    /**
     * Replaces a GeoObject by another GeoObject. If geoObjectToReplace cannot
     * be found, the newGeoObject is added to this GeoSet after all other
     * GeoObjects. The newGeoObject has the same values for visible and
     * selected. Selectable, name and id are not changed.
     *
     * @param newGeoObject The GeoObject that will replace another one.
     * @param geoObjectToReplace The GeoObject that will be removed.
     */
    public synchronized void replaceGeoObject(GeoObject newGeoObject,
            GeoObject geoObjectToReplace) {
        final int id = this.getIndexOfGeoObject(geoObjectToReplace);
        this.replaceGeoObject(newGeoObject, id);
    }

    /**
     * Replaces a GeoObject by another GeoObject. If the object to replace
     * cannot be found, the newGeoObject is added to this GeoSet after all other
     * GeoObjects. The newGeoObject has the same values for visible and
     * selected. Selectable, name and id are not changed.
     *
     * @param newGeoObject The GeoObject that will replace another one.
     * @param index The index of the GeoObject that will be removed.
     */
    public synchronized void replaceGeoObject(GeoObject newGeoObject, int index) {
        final MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (index >= 0 && index < this.getNumberOfChildren()) {
                GeoObject removedObj = this.remove(index);
                if (removedObj != null) {
                    newGeoObject.setSelected(removedObj.isSelected());
                    newGeoObject.setVisible(removedObj.isVisible());
                }
                this.add(index, newGeoObject);
            } else {
                this.add(newGeoObject);
            }
        } finally {
            trigger.inform(new MapEvent(true, true, true));
        }
    }

    /**
     * Replaces a GeoObject by another GeoObject. If an object with the
     * specified name cannot be found, the newGeoObject is added to this GeoSet
     * after all other GeoObjects. The newGeoObject has the same values for
     * visible and selected. Selectable, name and id are not changed.
     *
     * @param newGeoObject The GeoObject that will replace another one.
     * @param name The name of the GeoObject that will be removed.
     */
    public synchronized void replaceGeoObject(GeoObject newGeoObject, String name) {
        final int id = this.getIndexForName(name);
        this.replaceGeoObject(newGeoObject, id);
    }

    /**
     * Removes all currently contained GeoObjects and copies references of the
     * GeoObjects contained by the passed GeoSet.
     */
    public synchronized void replaceGeoObjects(GeoSet geoSet) {

        final boolean hasSelected = this.hasSelectedGeoObjects();

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.removeAllGeoObjects();

            if (geoSet == null) {
                return;
            }
            final int nbrObjects = geoSet.getNumberOfChildren();
            for (int i = 0; i < nbrObjects; i++) {
                GeoObject obj = geoSet.getGeoObject(i);
                this.add(obj);
            }
        } finally {
            trigger.inform(new MapEvent(true, hasSelected, true));
        }
    }

    /**
     * Remove all currently contained GeoObjects from this GeoSet.
     */
    public synchronized void removeAllGeoObjects() {

        final boolean hasSelected = this.hasSelectedGeoObjects();

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.setParent(null);
                if (geoObject instanceof GeoSet) {
                    final GeoSet geoSet = (GeoSet) geoObject;
                    geoSet.removeAllGeoObjects();
                }
            }
            vector.clear();
        } finally {
            trigger.inform(new MapEvent(true, hasSelected, true));
        }
    }

    public synchronized void remove(GeoObject geoObject) {
        if (geoObject == null) {
            return;
        }
        int index = vector.indexOf(geoObject);
        if (index == -1) {
            return;
        }
        vector.remove(index);
        geoObject.setParent(null);
        MapEventTrigger.inform(new MapEvent(true, geoObject.isSelected(), false), this);
    }

    /**
     * Removes a GeoObject at the specified index.
     *
     * @param index The position of the object to remove.
     * @return The removed object.
     */
    public synchronized GeoObject remove(int index) {
        GeoObject geoObject = (GeoObject) vector.get(index);
        if (geoObject == null) {
            return null;
        }
        vector.remove(index);
        geoObject.setParent(null);
        MapEventTrigger.inform(new MapEvent(true, geoObject.isSelected(), false), this);
        return geoObject;
    }

    /**
     * Remove all currently selected GeoObjects from this GeoSet.
     */
    public synchronized boolean removeSelectedGeoObjects() {
        boolean foundSelected = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            for (int i = vector.size() - 1; i >= 0; i--) {
                GeoObject geoObject = (GeoObject) (vector.get(i));
                if (geoObject instanceof GeoSet) {
                    foundSelected |= ((GeoSet) geoObject).removeSelectedGeoObjects();
                }

                if (geoObject.isSelected()) {
                    vector.remove(i);
                    geoObject.setParent(null);
                    if (geoObject instanceof GeoSet) {
                        final GeoSet geoSet = (GeoSet) geoObject;
                    }
                    foundSelected = true;
                }
            }
            return foundSelected;
        } finally {
            if (foundSelected) {
                trigger.inform(MapEvent.structureChange());
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Remove all objects that have a specified name. A map event is triggered
     * if an object is removed. THIS HAS NOT BEEN STRESS-TESTED!!! ???
     *
     * @param name The name identifying objects to be removed. If null, nothing
     * is removed.
     * @return True if an object has been removed.
     */
    public synchronized boolean removeByName(String name) {
        if (name == null) {
            return false;
        }

        boolean removedObject = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            for (int i = vector.size() - 1; i >= 0; i--) {
                GeoObject geoObject = (GeoObject) (vector.get(i));

                if (name.equals(geoObject.getName())) {
                    vector.remove(i);
                    geoObject.setParent(null);
                    removedObject = true;
                } else {
                    if (geoObject instanceof GeoSet) {
                        removedObject |= ((GeoSet) geoObject).removeByName(name);
                    }
                }
            }
            return removedObject;
        } finally {
            if (removedObject) {
                trigger.inform(MapEvent.structureChange());
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Returns a bounding box in world coordinates.
     */
    public java.awt.geom.Rectangle2D getBounds2D(double scale) {
        return this.getBounds2D(scale, false, false);
    }

    /**
     * Returns the bounding box of all GeoObjects contained by this GeoSet.
     *
     * @param onlyVisible If true, only the bounding box of the currently
     * visible GeoObjects is returned.
     */
    public synchronized java.awt.geom.Rectangle2D getBounds2D(
            double scale,
            boolean onlyVisible,
            boolean onlySelected) {

        if (vector.size() == 0) {
            return null;
        }

        // search through children for first object with valid bounding box
        Rectangle2D rect = null;
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext() && rect == null) {
            final GeoObject geoObject = (GeoObject) iterator.next();
            rect = geoObject.getBounds2D(scale, onlyVisible, onlySelected);
            if (!ika.utils.GeometryUtils.isRectangleValid(rect)) {
                rect = null;
            }
        }

        if (rect == null) {
            return null;
        }
        rect = (Rectangle2D) rect.clone();

        // compute union with bounding boxes of all following objects
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            final Rectangle2D objBounds =
                    geoObject.getBounds2D(scale, onlyVisible, onlySelected);
            if (objBounds != null && ika.utils.GeometryUtils.isRectangleValid(objBounds)) {
                Rectangle2D.union(rect, objBounds, rect);
            }
        }
        return rect;
    }

    /**
     * Required by abstract super class GeoObject. This implementation returns
     * alway false, since a GeoSet does not have its own geometry.
     */
    public boolean isPointOnSymbol(Point2D point, double tolDist, double scale) {
        return false;
    }

    /**
     * Required by abstract super class GeoObject. This implementation returns
     * alway false, since a GeoSet does not have its own geometry.
     */
    public boolean isIntersectedByRectangle(Rectangle2D rect, double scale) {
        return false;
    }

    /**
     * Returns the visually top-most object contained in this GeoSet that is
     * under a passed point.
     *
     * @param point The point for hit detection.
     * @param tolDist The tolerance to use for hit detection in world
     * coordinates.
     * @param scale The current scale of the map.
     * @return Returns the GeoObject if any, null otherwise.
     */
    public synchronized GeoObject getObjectAtPosition(Point2D point, double tolDist,
            double scale,
            boolean onlySelectable,
            boolean onlyVisible) {

        // search in inverse order
        for (int i = vector.size() - 1; i >= 0; i--) {
            final GeoObject geoObject = (GeoObject) vector.get(i);
            // test if point is on symbolized GeoObject
            final GeoObject geoObjectAtPosition =
                    geoObject.getObjectAtPosition(point, tolDist, scale,
                    onlySelectable, onlyVisible);
            if (geoObjectAtPosition != null) {
                return geoObjectAtPosition;
            }
        }
        return null;
    }

    private boolean selectByPointForUngrouped(Point2D point, double scale,
            boolean extendSelection, double tolDist) {

        // find object at position
        final GeoObject geoObjectAtPosition =
                this.getObjectAtPosition(point, tolDist, scale, true, true);

        boolean selectionChanged = false;
        if (geoObjectAtPosition != null) {
            selectionChanged = (geoObjectAtPosition.isSelected() == false);
            // deselect all currently selected objects
            this.setSelected(false);
            // select the found object (it has been deselected in the line above)
            geoObjectAtPosition.setSelected(true);
        } else {
            selectionChanged = this.hasSelectedGeoObjects();
            this.setSelected(false);
        }
        return selectionChanged;
    }

    private boolean selectByPointForGrouped(Point2D point, double scale,
            boolean extendSelection, double tolDist) {
        // remember if the selection state of any child changed
        boolean selectionChanged = false;

        boolean objectHit = false;
        for (int i = this.vector.size() - 1; i >= 0; i--) {
            final GeoObject geoObject = (GeoObject) this.vector.get(i);
            if (!geoObject.isVisible()) {
                continue;
            }
            objectHit = geoObject.isPointOnSymbol(point, tolDist, scale);
            if (objectHit) {
                break;
            }
        }

        final boolean select;
        final GeoObject firstGeoObject = (GeoObject) this.vector.get(0);
        if (objectHit) {
            if (extendSelection) {
                select = !firstGeoObject.isSelected();
                selectionChanged = true;
            } else {
                select = true;
                selectionChanged = !firstGeoObject.isSelected();
            }
        } else {
            select = false;
            selectionChanged = firstGeoObject.isSelected();
        }

        this.setSelected(select);
        return selectionChanged;
    }

    public synchronized boolean selectByPoint(Point2D point, double scale,
            boolean extendSelection, double tolDist) {

        if (this.vector.size() == 0 || !this.isVisible()) {
            return false;
        }
        boolean selectionChanged = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (this.grouped) {
                selectionChanged =
                        this.selectByPointForGrouped(point, scale, extendSelection, tolDist);
            } else {
                selectionChanged =
                        this.selectByPointForUngrouped(point, scale, extendSelection, tolDist);
            }
            return selectionChanged;
        } finally {
            if (selectionChanged) {
                trigger.inform(MapEvent.selectionChange());
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Selects all GeoObjects contained by this GeoSet that intersect with the
     * passed rectangle.
     */
    public synchronized boolean selectByRectangle(Rectangle2D rect, double scale,
            boolean extendSelection) {

        if (this.vector.size() == 0) {
            return false;
        }

        boolean selectionChanged = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (this.grouped) {
                // this is a group, test if rectangle hits any child.
                boolean objectHit = false;
                for (int i = this.vector.size() - 1; i >= 0; i--) {
                    final GeoObject geoObject = (GeoObject) this.vector.get(i);
                    objectHit = geoObject.isIntersectedByRectangle(rect, scale);
                    if (objectHit) {
                        break;
                    }
                }

                final boolean select;
                final GeoObject firstGeoObject = (GeoObject) this.vector.get(0);
                if (objectHit) {
                    if (extendSelection) {
                        select = !firstGeoObject.isSelected();
                        selectionChanged = true;
                    } else {
                        select = true;
                        selectionChanged = !firstGeoObject.isSelected();
                    }
                } else {
                    select = false;
                    selectionChanged = firstGeoObject.isSelected();
                }

                this.setSelected(select);

            } else {
                java.util.Iterator iterator = this.vector.iterator();
                while (iterator.hasNext()) {
                    final GeoObject geoObject = (GeoObject) iterator.next();
                    selectionChanged |= geoObject.selectByRectangle(rect, scale, extendSelection);
                }
            }
            return selectionChanged;
        } finally {
            if (selectionChanged) {
                trigger.inform(MapEvent.selectionChange());
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Overwrite GeoObject's setSelected method. The new selection state is
     * applied to each GeoObject contained by this GeoSet.
     *
     * @param selected The new selection state of all GeoObjects contained by
     * this GeoSet.
     */
    public synchronized void setSelected(boolean selected) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            // call the overwritten method to select this GeoSet
            super.setSelected(selected);

            // pass the selection state to all children
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.setSelected(selected);
            }
        } finally {
            trigger.inform(MapEvent.selectionChange());
        }
    }

    /**
     * Overwrite GeoObject's setSelectable method. The new selectable state is
     * applied to each GeoObject contained by this GeoSet.
     *
     * @param selectable The new selectable state of all GeoObjects contained by
     * this GeoSet.
     */
    public synchronized void setSelectable(boolean selectable) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            // call the overwritten method to select this GeoSet
            super.setSelectable(selectable);

            // pass the selection state to all children
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.setSelectable(selectable);
            }
        } finally {
            trigger.inform(MapEvent.selectionChange());
        }
    }

    /**
     * Assign a shared VectorSymbol to all GeoPath in this GeoSet and all
     * sub-GeoSets.
     *
     * @param vectorSymbol The shared instance of a VectorSymbol that will be
     * assigned to all children GeoPaths.
     */
    public synchronized void setVectorSymbol(VectorSymbol vectorSymbol) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof GeoPath) {
                    ((GeoPath) obj).setVectorSymbol(vectorSymbol);
                } else if (obj instanceof GeoSet) {
                    ((GeoSet) obj).setVectorSymbol(vectorSymbol);
                }
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Returns the number of GeoObjects contained by this GeoSet.
     *
     * @return The number of GeoObjects contained by this GeoSet.
     */
    public synchronized int getNumberOfChildren() {
        return this.vector.size();
    }

    /**
     * Returns the number of GeoSets that are direct children of this GeoSet.
     * GeoSets further down the tree are not counted.
     *
     * @return The number of GeoSets contained by this GeoSet.
     */
    public synchronized int getNumberOfSubSets() {
        int numberOfSubSets = 0;
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            if (geoObject instanceof GeoSet) {
                numberOfSubSets++;
            }
        }
        return numberOfSubSets;
    }

    /**
     * Returns the number of GeoSets contained in the tree below this GeoSet.
     *
     * @return The number of GeoSets contained by this GeoSet.
     */
    public synchronized int getNumberOfSubSetsInTree() {
        int numberOfSubSets = 0;
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            Object geoObject = iterator.next();
            if (geoObject instanceof GeoSet) {
                numberOfSubSets += ((GeoSet) geoObject).getNumberOfSubSetsInTree() + 1;
            }
        }
        return numberOfSubSets;
    }

    /**
     * Returns the GeoObject at a certain index.
     */
    public synchronized GeoObject getGeoObject(int id) {
        return (GeoObject) this.vector.get(id);
    }

    public synchronized Object[] getGeoObjectsAsArray() {
        return this.vector.toArray();
    }

    /**
     * Returns the first GeoObject with an ID. Note that IDs need not to be
     * unique.
     */
    public synchronized GeoObject getGeoObjectByID(long id) {
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            if (geoObject.getID() == id) {
                return geoObject;
            }
        }
        return null;
    }

    /**
     * Returns this GeoSet if the passed name is the name of this GeoSet.
     * Otherwise searches through all children for a GeoObject with this name.
     * Uses a depth-first search.
     *
     * @param name The name of the GeoObject that is being searched.
     * @return This GeoSet or the first child with the passed name.
     */
    public synchronized GeoObject getGeoObject(String name) {

        if (name.equals(this.getName())) {
            return this;
        }

        Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            GeoObject foundGeoObject = geoObject.getGeoObject(name);
            if (foundGeoObject != null) {
                return foundGeoObject;
            }
        }

        return null;
    }

    /**
     *
     * @return The position of the passed object in the array that stores the
     * GeoObjects of this GeoSet. Returns -1 if the object is not found.
     */
    public synchronized int getIndexOfGeoObject(Object obj) {
        if (obj == null) {
            return -1;
        }
        return this.vector.indexOf(obj);
    }

    /**
     *
     * @return The position of the object with the passed name in the array that
     * stores the GeoObjects of this GeoSet. Returns -1 if the object is not
     * found.
     */
    public synchronized int getIndexForName(String name) {

        int id = 0;
        Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            GeoObject foundGeoObject = geoObject.getGeoObject(name);
            if (foundGeoObject != null) {
                return id;
            }
            ++id;
        }
        return -1;

    }

    public synchronized void drawNormalState(RenderParams rp) {
        if (this.isVisible()) {
            final java.util.Iterator iterator = vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                if (geoObject.isVisible()) {
                    geoObject.drawNormalState(rp);
                }
            }
        }
    }

    public synchronized void drawSelectedState(RenderParams rp) {
        if (this.isVisible()) {
            final java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                if (geoObject.isVisible()) {
                    geoObject.drawSelectedState(rp);
                }
            }
        }
    }

    /**
     * Returns true if this GeoSet contains any GeoObject that is currently
     * selected.
     */
    public synchronized boolean hasSelectedGeoObjects() {
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            if (geoObject instanceof GeoSet) {
                GeoSet geoSet = (GeoSet) geoObject;
                if (geoSet.hasSelectedGeoObjects()) {
                    return true;
                }
            } else {
                if (geoObject.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this GeoSet contains any GeoObject that is currently
     * visible. Returns false if the GeoSet itself is not visible. Returns false
     * if the GeoSet does not contain any children. This traverses all children
     * and children of children until a visible GeoObject is found.
     */
    public synchronized boolean hasVisibleGeoObjects() {
        if (!this.isVisible()) {
            return false;
        }

        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            if (geoObject instanceof GeoSet) {
                if (((GeoSet) geoObject).hasVisibleGeoObjects()) {
                    return true;
                }
            } else {
                if (geoObject.isVisible()) {
                    return true;
                }
            }
        }
        return false;
    }

    private class SingleSelectionSearcher {

        private GeoObject foundSelectedObj = null;
        private boolean moreThanOneSelected = false;

        public SingleSelectionSearcher(GeoSet geoSet) {
            this.search(geoSet);
        }

        private void search(GeoSet geoSet) {

            java.util.Iterator iterator = geoSet.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                if (geoObject instanceof GeoSet && !((GeoSet) geoObject).isGrouped()) {
                    this.search((GeoSet) geoObject);
                } else if (geoObject.isSelected()) {
                    if (this.foundSelectedObj != null) {
                        this.moreThanOneSelected = true;
                    } else {
                        this.foundSelectedObj = geoObject;
                    }
                }
                if (this.moreThanOneSelected) {
                    break;
                }
            }
        }

        public GeoObject getSingleSelectedGeoObject() {
            if (foundSelectedObj != null && moreThanOneSelected == false) {
                return foundSelectedObj;
            } else {
                return null;
            }
        }
    }

    /**
     * Private helper method. Returns the selected GeoObject, if there is
     * exactly one selected, returns null otherwise. Abuses excpetions. Should
     * be done in a nicer and better way. !!! ???
     */
    private synchronized GeoObject searchSingleSelectedGeoObject(
            GeoObject selectedObj, boolean searchChildren) throws Exception {
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            GeoObject geoObject = (GeoObject) iterator.next();
            if (searchChildren
                    && geoObject instanceof GeoSet
                    && !((GeoSet) geoObject).isGrouped()) {
                GeoSet geoSet = (GeoSet) geoObject;

                GeoObject selectedObjOfSubSet =
                        geoSet.searchSingleSelectedGeoObject(selectedObj, searchChildren);
                if (selectedObjOfSubSet != null) {
                    if (selectedObj != null) {
                        throw new Exception();
                    } else {
                        selectedObj = selectedObjOfSubSet;
                    }
                }
            } else {
                if (geoObject.isSelected()) {
                    if (selectedObj != null) {
                        throw new Exception();
                    }
                    selectedObj = geoObject;
                }
            }
        }
        return selectedObj;
    }

    /**
     * Returns the selected GeoObject, if there is exactly one selected one,
     * returns null otherwise. A grouped GeoSet is considered to be a single
     * object.
     */
    public synchronized GeoObject getSingleSelectedGeoObject(boolean searchChildren) {
        return new SingleSelectionSearcher(this).getSingleSelectedGeoObject();
    }

    public GeoObject getSingleSelectedGeoObject() {
        return this.getSingleSelectedGeoObject(true);
    }

    /**
     * Returns the selected GeoObject, if there is exactly one selected object
     * of the required class or of a sublcass, returns null otherwise. A grouped
     * GeoSet is considered to be a single object.
     *
     */
    public synchronized GeoObject getSingleSelectedGeoObject(Class requiredClass,
            boolean searchChildren) {

        GeoObject geoObject =
                new SingleSelectionSearcher(this).getSingleSelectedGeoObject();
        if (geoObject != null && requiredClass.isInstance(geoObject)) {
            return geoObject;
        }
        return null;
    }

    public GeoObject getSingleSelectedGeoObject(Class requiredClass) {
        return this.getSingleSelectedGeoObject(requiredClass, true);
    }

    /**
     * Returns all GeoObject of a certain class or its subclasses.
     */
    public synchronized void getAllGeoObjects(Class cl,
            Collection foundGeoObjects,
            boolean onlySelected) {

        try {
            // do a scan among all children of this GeoSet
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (cl.isInstance(obj)) {
                    if (onlySelected) {
                        if (((GeoObject) obj).isSelected()) {
                            foundGeoObjects.add(obj);
                        }
                    } else {
                        foundGeoObjects.add(obj);
                    }
                }
            }

            // ask children for selected objects
            iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (GeoSet.class.isInstance(obj)) {
                    final GeoSet geoSet = (GeoSet) obj;
                    geoSet.getAllGeoObjects(cl, foundGeoObjects, onlySelected);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the first GeoObject of a specified class, or the first GeoObject
     * that is not of the specified class. The search can optionally be limited
     * to selected objects.
     *
     * @param requiredClass The GeoObject must be of this class, unless
     * exclusive is true.
     * @param exclusive If true, the first GeoObject that is not of the
     * requiredClass is returned, otherwise the first GeoObject that is of the
     * specified class is returned.
     * @param requireSelected The GeoObject must be selected.
     * @return The first GeoObject in this GeoSet or in one of its sub-GeoSets
     * that is of the specified class.
     */
    public synchronized GeoObject getFirstGeoObject(Class requiredClass,
            boolean exclusive, boolean requireSelected) {

        try {
            // do a scan among all children of this GeoSet
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final Object obj = iterator.next();
                final boolean sameClass = requiredClass.isInstance(obj);
                if (sameClass ^ exclusive) {
                    if (!requireSelected || (requireSelected && ((GeoObject) obj).isSelected())) {
                        return (GeoObject) obj;
                    }
                }
            }

            // ask child GeoSets for the object
            iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof GeoSet) {
                    GeoObject childGeoObject = ((GeoSet) obj).getFirstGeoObject(
                            requiredClass, exclusive, requireSelected);
                    if (childGeoObject != null) {
                        return childGeoObject;
                    }
                }
            }

        } catch (Exception e) {
        }
        return null;
    }

    public synchronized void move(double dx, double dy) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.move(dx, dy);
            }
        } finally {
            trigger.inform();
        }
    }

    public synchronized void rotate(double rotRad) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.rotate(rotRad);
            }
        } finally {
            trigger.inform();
        }
    }

    public synchronized void transform(AffineTransform affineTransform) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.transform(affineTransform);
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Transform all selected GeoObjects contained by this GeoSet.
     */
    public synchronized boolean transformSelected(
            AffineTransform affineTransform) {
        boolean transformedChild = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                transformedChild |= geoObject.transformSelected(affineTransform);
            }
            return transformedChild;
        } finally {
            if (transformedChild) {
                trigger.inform();
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Moves all selected GeoObjects contained by this GeoSet by a certain
     * distance.
     *
     * @return Returns true if there was any object moved.
     */
    public synchronized boolean moveSelected(double dx, double dy) {
        boolean movedChild = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                movedChild |= geoObject.moveSelected(dx, dy);
            }
            return movedChild;
        } finally {
            if (movedChild) {
                trigger.inform();
            } else {
                trigger.abort();
            }
        }
    }

    public synchronized boolean cloneAndMoveSelected(double dx, double dy) {
        boolean foundSelected = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            // can't use an iterator, since the cloned objects will be appended to
            // the vector of this GeoSet
            final int nbrInitialChildren = this.getNumberOfChildren();
            for (int i = 0; i < nbrInitialChildren; i++) {
                final GeoObject geoObject = (GeoObject) this.vector.get(i);
                foundSelected |= geoObject.cloneAndMoveSelected(dx, dy);
            }
            return foundSelected;
        } finally {
            trigger.inform();
        }
    }

    public void scale(double scale) {
        this.scale(scale, scale);
    }

    /**
     * Scale this GeoObject horizontally and vertically. Attention: not all
     * derived classes support scaling, or uneven scaling.
     *
     * @param hScale The horizontal scale factor.
     * @param vScale The vertical scale factor.
     */
    public synchronized void scale(double hScale, double vScale) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                GeoObject geoObject = (GeoObject) iterator.next();
                geoObject.scale(hScale, vScale);
            }
        } finally {
            trigger.inform();
        }
    }

    /**
     * Scale all selected GeoObjects contained by this GeoSet.
     */
    public void scaleSelected(double scale) {
        this.scaleSelected(scale, scale);
    }

    /**
     * Scales all selected GeoObjects contained by this GeoSet.
     */
    public synchronized boolean scaleSelected(double hScale, double vScale) {
        boolean scaledChild = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            java.util.Iterator iterator = this.vector.iterator();
            while (iterator.hasNext()) {
                final GeoObject geoObject = (GeoObject) iterator.next();
                scaledChild |= geoObject.scaleSelected(hScale, vScale);
            }
            return scaledChild;
        } finally {
            if (scaledChild) {
                trigger.inform();
            } else {
                trigger.abort();
            }
        }
    }

    /**
     * Relatively deforms all selected and visible GeoObjects contained by this
     * GeoSet to fit into a new bounding box.
     */
    public synchronized boolean deformSelected(Rectangle2D newBounds) {

        boolean changedChild = false;
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {

            Rectangle2D bounds = getBounds2D(GeoObject.UNDEFINED_SCALE, true, true);
            changedChild = this.moveSelected(-bounds.getMinX(), -bounds.getMinY());
            double hScale = newBounds.getWidth() / bounds.getWidth();
            double vScale = newBounds.getHeight() / bounds.getHeight();
            changedChild |= this.scaleSelected(hScale, vScale);
            changedChild |= this.moveSelected(newBounds.getMinX(), newBounds.getMinY());
            return changedChild;
        } finally {
            if (changedChild) {
                trigger.inform();
            } else {
                trigger.abort();
            }
        }

    }

    public synchronized boolean isGrouped() {
        return grouped;
    }

    public synchronized void setGrouped(boolean grouped) {
        this.grouped = grouped;

        // propagate to children GeoSets
        java.util.Iterator iterator = this.vector.iterator();
        while (iterator.hasNext()) {
            final GeoObject geoObject = (GeoObject) iterator.next();
            if (geoObject instanceof GeoSet) {
                GeoSet geoSet = (GeoSet) geoObject;
                geoSet.setGrouped(grouped);
            }
        }
    }

    public synchronized ArrayList toArrayList() {
        return new ArrayList(this.vector);
    }
}
