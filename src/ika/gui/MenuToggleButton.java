package ika.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A toggle button that shows a popup menu while being selected.
 * http://explodingpixels.wordpress.com/2008/11/10/prevent-popup-menu-dismissal/
 */
public class MenuToggleButton extends JToggleButton implements Serializable{

    private JPopupMenu popupMenu = null;
    private boolean shouldHandlePopupWillBecomeInvisible = true;

    public MenuToggleButton() {

        // install a special client property on the button to prevent it from
        // closing of the popup when the down arrow is pressed.
        JComboBox box = new JComboBox();
        Object preventHide = box.getClientProperty("doNotCancelPopup");
        putClientProperty("doNotCancelPopup", preventHide);
    }

    private MouseListener createButtonMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                // if the popup menu is currently showing, then hide it.
                // else if the popup menu is not showing, then show it.
                if (popupMenu.isShowing()) {
                    hidePopupMenu();
                } else if (isEnabled()){
                    showPopupMenu();
                }
            }
        };
    }

    private PopupMenuListener createPopupMenuListener() {
        return new PopupMenuListener() {

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // no implementation.
                }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // handle this event if so indicated. the only time we don't handle
                // this event is when the button itself is pressed, the press action
                // toggles the button selected state for us. this case handles when
                // the button has been toggled, but the user clicks outside the
                // button in order to dismiss the menu.
                if (shouldHandlePopupWillBecomeInvisible) {
                    setSelected(false);
                }
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
                // the popup menu has been canceled externally (either by
                // pressing escape or clicking off of the popup menu). update
                // the button's state to reflect the menu dismissal.
                setSelected(false);
            }
        };
    }

    private void hidePopupMenu() {
        shouldHandlePopupWillBecomeInvisible = false;
        popupMenu.setVisible(false);
        shouldHandlePopupWillBecomeInvisible = true;
    }

    private void showPopupMenu() {
        // show the menu below the button, and slightly to the right.
        popupMenu.show(this, 5, getHeight());
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void setPopupMenu(JPopupMenu popupMenu) {
        
        // bad things happen if the menu is set more than once!
        if (this.popupMenu != null) {
            throw new IllegalStateException("menu already set");
        }
        this.popupMenu = popupMenu;

        // add a popup menu listener to update the button's selection state
        // when the menu is being dismissed.
        this.popupMenu.addPopupMenuListener(createPopupMenuListener());

        // install a mouse listener on the button to hide and show the popup
        // menu as appropriate.
        addMouseListener(createButtonMouseListener());
    }

}
