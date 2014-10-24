package ika.geo;

import java.awt.geom.*;
import java.io.Serializable;

/**
 * GeoObject - an abstract base class for GeoObjects. A GeoObject has a spatial
 * extension and can be selected and drawn in a map.
 *
 * @author Bernhard Jenny, Institute of Cartography, ETH Zurich.
 */
public abstract class GeoObject implements Serializable, Cloneable {

    private static final long serialVersionUID = 3361920857810179938L;
    public static final double UNDEFINED_SCALE = -1;
    private GeoSet parent;
    /**
     * Flag that indicates whether this GeoObject has been selected by some user
     * action.
     */
    private boolean selected;
    /**
     * selectable determines whether this GeoObject can be selected.
     */
    private boolean selectable;
    /**
     * visible determines whether this GeoObject should be drawn in a map.
     */
    private boolean visible;
    /**
     * The name of this GeoObject
     */
    private String name;
    /**
     * An ID that is not guaranteed to be unique. Default value is 0.
     */
    private long id;

    /**
     * Protected constructor.
     */
    protected GeoObject() {
        this.parent = null;
        this.selected = false;
        this.selectable = true;
        this.visible = true;
        this.name = null;
        this.id = 0;
    }

    /**
     * Returns a copy of this GeoObject. The parent of the copy is null. The ID
     * of the copy is the same as the ID of this GeoObject.
     *
     * @return A copy.
     */
    @Override
    public GeoObject clone() {
        try {
            GeoObject copy = (GeoObject) super.clone();
            copy.parent = null;
            return copy;
        } catch (CloneNotSupportedException exc) {
            return null;
        }
    }

    public void cloneIfSelected(GeoSet geoSet) {
        if (this.isSelected()) {
            geoSet.add((GeoObject) this.clone());
        }
    }

    /**
     * Returns whether this GeoObject can be selected. An attempt to select an
     * object that cannot be selected using setSelected (true) will have no
     * effect.
     *
     * @return True if the object can be selected.
     */
    public final boolean isSelectable() {
        return this.selectable;
    }

    /**
     * Set the selectable state of this GeoObject.
     *
     * @param selectable The new selection state.
     */
    public void setSelectable(boolean selectable) {
        final boolean change = this.selectable != selectable;
        this.selectable = selectable;
        if (!selectable && this.selected) {
            this.selected = false;
            MapEventTrigger.inform(MapEvent.selectionChange(), this);
        } else if (change) {
            MapEventTrigger.inform(this);
        }
    }

    /**
     * Return whether this GeoObject is selected.
     *
     * @return The selection state.
     */
    public final boolean isSelected() {
        return this.selected;
    }

    /**
     * Set the selection state of this GeoObject. A call to select an object
     * that cannot be selected (selectable is false) will be ignored.
     *
     * @param selected The new selection state.
     */
    public void setSelected(boolean selected) {
        final boolean newSelection = selected ? this.selectable : false;
        if (this.selected != newSelection) {
            this.selected = newSelection;
            if (this.parent != null) {
                MapEventTrigger.inform(MapEvent.selectionChange(), this);
            }
        }
    }

    /**
     * Returns a bounding box in world coordinates.
     */
    abstract public Rectangle2D getBounds2D(double scale);

    public Rectangle2D getBounds2D(double scale, boolean onlyVisible,
            boolean onlySelected) {
        if (onlySelected && !this.isSelected()) {
            return null;
        }
        if (onlyVisible && !this.isVisible()) {
            return null;
        }
        return this.getBounds2D(scale);
    }

    /**
     * Returns this object if its symbol is under the passed point, null
     * otherwise.
     *
     * @param point The point for hit detection.
     * @param tolDist The tolerance to use for hit detection in world
     * coordinates.
     * @param scale The current scale of the map.
     * @return Returns the GeoObject if its symbol is hit by the passed point,
     * null otherwise.
     */
    public GeoObject getObjectAtPosition(Point2D point,
            double tolDist,
            double scale,
            boolean onlySelectable,
            boolean onlyVisible) {

        // filter invisible GeoObjects
        if (onlyVisible && !this.isVisible()) {
            return null;
        }

        // filter GeoObjects that are not selectable
        if (onlySelectable && !this.isSelectable()) {
            return null;
        }

        // return this GeoObject if the point is on its symbol
        return this.isPointOnSymbol(point, tolDist, scale) ? this : null;
    }

    public double getCenterX(double scale) {
        final Rectangle2D bbox = this.getBounds2D(scale);
        return bbox.getCenterX();
    }

    public double getCenterY(double scale) {
        final Rectangle2D bbox = this.getBounds2D(scale);
        return bbox.getCenterY();
    }

    /**
     * Draw this GeoObject into a Graphics2D object. This method is only called
     * if the object is visible.
     *
     * @param rp Parameters for rendering.
     */
    abstract public void drawNormalState(RenderParams rp);

    /**
     * Draw this GeoObject as it appears when it is selected. This method is
     * only called if the object is visible. Overwriting methods must make sure
     * the object is selected before drawing the selected state. The foreground
     * color in rp.g2d is set to a highlighting color; the stroke width is set
     * to a scale-independent thin width; the affine transformation is set to
     * transform selected objects if required; rendering hints are set for fast
     * not anti-aliased rendering. Important: THESE SETTINGS MUST NOT BE
     * CHANGED!
     *
     * @param rp Parameters for rendering.
     */
    abstract public void drawSelectedState(RenderParams rp);

    /**
     * Tests whether a point hits the graphical representation of this
     * GeoObject.
     *
     * @param point The point to test.
     * @param tolDist The tolerance distance for hit detection.
     * @param scale The current scale of the map.
     * @return Returns true if the passed point hits this GeoObject.
     */
    abstract public boolean isPointOnSymbol(Point2D point, double tolDist,
            double scale);

    abstract public boolean isIntersectedByRectangle(Rectangle2D rect, double scale);

    /**
     * Select this GeoObject if it intersects with a rectangle.
     *
     * @param rect The rectangle to test.
     * @param scale The current scale of the map.
     * @param extendSelection If true, this GeoObject is not deselected if the
     * rectangle does not interesect with it. Otherwise, the GeoObject is
     * deselected.
     */
    public boolean selectByRectangle(Rectangle2D rect, double scale,
            boolean extendSelection) {
        // don't do anything if this object cannot be selected.
        if (!this.selectable) {
            return false;
        }

        // remember the inital selection state
        final boolean initialSelection = this.isSelected();

        // default is that the rectangle does not intersect with the object.
        boolean newSelection = extendSelection && initialSelection;

        // test if this object is hit by the passed point.
        if (this.isIntersectedByRectangle(rect, scale) == true) {
            // The object is hit and already selected.
            // Toggle selection state if needed.
            if (this.isSelected()) {
                newSelection = !extendSelection;
            } else // object is hit but not selected yet.
            {
                newSelection = true;
            }
        }

        this.setSelected(newSelection);
        return initialSelection != newSelection;

        // trigger MapEventTrigger.inform(this); ? !!! ???
    }

    /**
     * Select this GeoObject if it is hit by a passed point.
     *
     * @param point The point to test.
     * @param scale The current scale of the map.
     * @param extendSelection If true, this GeoObject is not deselected if the
     * point does not hit this object. Otherwise, it is deselected.
     * @return Returns true if the selection state has been changed
     */
    public boolean selectByPoint(Point2D point, double scale,
            boolean extendSelection, double tolDist) {

        // don't do anything if this object cannot be selected.
        if (!this.selectable || !this.visible) {
            return false;
        }

        // remember the inital selection state
        final boolean initialSelection = this.isSelected();

        // default is that the point does not hit the object.
        boolean newSelection = extendSelection && initialSelection;

        // test if this object is hit by the passed point.
        if (this.isPointOnSymbol(point, tolDist, scale) == true) {
            // Toggle selection state if needed.
            if (this.isSelected()) {
                // The object is hit and already selected.
                newSelection = !extendSelection;
            } else {
                // object is hit but not selected yet.
                newSelection = true;
            }
        }
        this.setSelected(newSelection);
        return initialSelection != newSelection;

        // trigger MapEventTrigger.inform(this); ? !!! ???
    }

    /**
     * Move this object by a specified amount horizontally and vertically. This
     * default implementation uses transform(AffineTransform). Derived classes
     * that can provide a better solution can overwrite this method.
     *
     * @param dx The distance by which this object should be moved in horizontal
     * direction.
     * @param dy The distance by which this object should be moved in vertical
     * direction.
     */
    public void move(double dx, double dy) {
        this.transform(AffineTransform.getTranslateInstance(dx, dy));
    }

    /**
     * Move this object if it is selected by a specified amount horizontally and
     * vertically.
     *
     * @param dx The distance by which this object should be moved in horizontal
     * direction.
     * @param dy The distance by which this object should be moved in vertical
     * direction.
     * @return True if the object has been moved.
     */
    public boolean moveSelected(double dx, double dy) {
        if (this.isSelected()) {
            this.move(dx, dy);
            return dx != 0 || dy != 0;
        }
        return false;
    }

    /**
     * Clone this object if it is selected and move it by a specified amount
     * horizontally and vertically. This object is deselected, the new clone is
     * selected.
     *
     * @param dx The distance by which this object should be moved in horizontal
     * direction.
     * @param dy The distance by which this object should be moved in vertical
     * direction.
     * @return True if the object has been moved.
     */
    public boolean cloneAndMoveSelected(double dx, double dy) {
        if (this.parent == null) {
            return false;
        }

        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            if (this.isSelected() && (dx != 0 || dy != 0)) {
                GeoObject clone = (GeoObject) this.clone();
                clone.move(dx, dy);
                this.setSelected(false);
                this.parent.add(clone);

                return true;
            }
            return false;
        } finally {
            trigger.inform();
        }
    }

    /**
     * Scale this object by a factor relative to the origin of the coordinate
     * system. This default implementation uses transform(AffineTransform).
     * Derived classes that can provide a better solution can overwrite this
     * method.
     *
     * @param scale Scale factor.
     */
    public void scale(double scale) {
        this.transform(AffineTransform.getScaleInstance(scale, scale));
    }

    /**
     * Scale this GeoObject horizontally and vertically. This default
     * implementation uses transform(AffineTransform). Derived classes that can
     * provide a better solution can overwrite this method. Attention: not all
     * derived classes support scaling, or uneven scaling.
     *
     * @param hScale The horizontal scale factor.
     * @param vScale The vertical scale factor.
     */
    public void scale(double hScale, double vScale) {
        this.transform(AffineTransform.getScaleInstance(hScale, vScale));
    }

    /**
     * Scale this object by a factor relative to a passed origin. Derived
     * classes that can provide a more efficient solution can overwrite this
     * method.
     *
     * @param scale Scale factor.
     * @param cx The x coordinate of the point relativ to which the object is
     * scaled.
     * @param cy The y coordinate of the point relativ to which the object is
     * scaled.
     */
    public void scale(double scale, double cx, double cy) {
        this.move(-cx, -cy);
        this.scale(scale);
        this.move(cx, cy);
    }

    /**
     * Scale this object if it is selected.
     *
     * @param hScale The horizontal scale factor.
     * @param vScale The vertical scale factor.
     * @return True if the object has been scaled.
     */
    public boolean scaleSelected(double hScale, double vScale) {
        if (this.isSelected()) {
            this.scale(hScale, vScale);
            return hScale != 1 || vScale != 1;
        }
        return false;
    }

    /**
     * Rotate this object around the origin of the coordinate system in
     * counter-clockwise direction. This default implementation uses
     * transform(AffineTransform). Derived classes that can provide a better
     * solution can overwrite this method. Attention: not all derived classes
     * support rotating.
     */
    public void rotate(double rotRad) {
        this.transform(AffineTransform.getRotateInstance(rotRad));
    }

    abstract public void transform(AffineTransform affineTransform);

    /**
     * Transform this object if it is selected.
     *
     * @param affineTransform The affine transformation to apply.
     * @return True if the object has been transformed.
     */
    public boolean transformSelected(AffineTransform affineTransform) {
        if (this.isSelected() && affineTransform.isIdentity() == false) {
            this.transform(affineTransform);
            return true;
        }
        return false;
    }

    /**
     * Rotate this object around a point in counter-clockwise direction.
     *
     * @param rotRad The rotation angle.
     */
    public void rotateAroundPoint(double rotRad, double x, double y) {
        MapEventTrigger trigger = new MapEventTrigger(this);
        try {
            this.move(-x, -y);
            this.rotate(rotRad);
            this.move(x, y);
        } finally {
            trigger.inform();
        }
    }

    /**
     * Returns whether this GeoObject is currently visible.
     *
     * @return True if visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Show or hide an object.
     *
     * @param visible True: the object should be made visible.
     */
    public void setVisible(boolean visible) {
        final boolean change = this.visible != visible;
        this.visible = visible;
        if (change) {
            MapEventTrigger.inform(MapEvent.visibilityChange(), this);
        }
    }

    /**
     * Returns the name of this GeoObject.
     *
     * @return The name of this GeoObject.
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the name of this GeoObject.
     *
     * @param name The new name of this object. Can be null.
     */
    public void setName(String name) {

        final boolean change;
        if (name == null) {
            change = this.name != null;
        } else {
            change = !name.equals(this.name);
        }

        this.name = name;
        if (change) {
            MapEventTrigger.inform(this);
        }

    }

    /**
     * Returns this GeoObject if its name is equal to the passed name. Returns
     * null otherwise.
     *
     * @param name The name of the object that is searched.
     * @return A reference to this GeoObject if the passed name equals its name.
     */
    public synchronized GeoObject getGeoObject(String name) {
        return name.equals(this.getName()) ? this : null;
    }

    /**
     * Returns a string representation of this GeoObject, which is simply the
     * name.
     *
     * @return The name of this GeoObject.
     */
    @Override
    public String toString() {
        String n = this.getName();
        return n != null ? n : "<unnamed>";
    }

    /**
     * Returns the ID of this GeoObject.
     *
     * @param The ID.
     */
    public long getID() {
        return id;
    }

    /**
     * Change the ID of this GeoObject. Important: It is not checked whether the
     * new ID is unique!
     *
     * @param id The new ID.
     */
    public void setID(long id) {
        final boolean change = this.id != id;
        this.id = id;
        if (change) {
            MapEventTrigger.inform(this);
        }
    }

    /**
     * Returns the parent GeoSet, i.e. the GeoSet that contains this GeoObject.
     *
     * @return The parent GeoSet or null if this object is not contained by any
     * GeoSet.
     */
    public GeoSet getParent() {
        return parent;
    }

    /**
     * Set the parent of this GeoObject. The parent is the GeoSet that contains
     * this GeoObject. This method does not trigger an event to inform
     * MapEventListeners.
     *
     * @param parent The parent GeoSet.
     */
    protected void setParent(GeoSet parent) {
        this.parent = parent;
        // don't inform MapEventListeners. setParent is only called from
        // methods that change the structure of the tree of GeoObjects and
        // inform MapEventListeners themselves.
    }

    /**
     * Returns the GeoTreeRoot of the tree of GeoObjects. Returns null if the
     * tree has not a GeoTreeRoot as root. Returns null if this object has no
     * parent.
     *
     * @return The GeoSetBroadcaster of the tree, which is the topmost GeoSet of
     * the tree.
     */
    protected GeoTreeRoot getRoot() {
        return this.parent == null ? null : this.parent.getRoot();
    }

    /**
     * Returns an array with all parent GeoSets of this GeoObject and the object
     * itself.
     *
     * @return An array of all parent GeoSets and this object. The first object
     * in the array is this object, the parents follow. The last element is the
     * root.
     */
    public Object[] getTreePath() {
        if (this.parent == null) {
            return new GeoObject[]{this};
        }

        java.util.ArrayList path = new java.util.ArrayList(4);
        this.constructTreePath(path);
        return path.toArray();
    }

    /**
     * Constructs a chain of all parent GeoSets and this object itself.
     * Recursively calls the parent GeoSets until it reaches the root of the
     * tree.
     *
     * @param path The ArrayList that will contain the complete path.
     */
    protected void constructTreePath(java.util.ArrayList path) {
        path.add(this);
        if (this.parent != null) {
            this.parent.constructTreePath(path);
        }
    }
}