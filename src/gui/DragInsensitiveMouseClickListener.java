/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gui;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

public class DragInsensitiveMouseClickListener implements MouseInputListener {

    protected static final int MAX_CLICK_DISTANCE = 15;

    private final MouseInputListener target;

    public MouseEvent pressed;

    public DragInsensitiveMouseClickListener(MouseInputListener target) {
        this.target = target;
    }

    @Override
    public final void mousePressed(MouseEvent e) {
        pressed = e;
        target.mousePressed(e);
    }

    private int getDragDistance(MouseEvent e) {
        int distance = 0;
        distance += Math.abs(pressed.getXOnScreen() - e.getXOnScreen());
        distance += Math.abs(pressed.getYOnScreen() - e.getYOnScreen());
        return distance;
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        target.mouseReleased(e);

        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE) {
                MouseEvent clickEvent = new java.awt.event.MouseEvent(             
                		(Component) pressed.getSource(),
                        MouseEvent.MOUSE_CLICKED, e.getWhen(), pressed.getModifiers(),
                        pressed.getX(), pressed.getY(), pressed.getXOnScreen(), pressed.getYOnScreen(),
                        pressed.getClickCount(), pressed.isPopupTrigger(), pressed.getButton());
                
                target.mouseClicked(clickEvent);
            }
            pressed = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //do nothing, handled by pressed/released handlers
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        target.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        target.mouseExited(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressed != null) {
            if (getDragDistance(e) < MAX_CLICK_DISTANCE) return; //do not trigger drag yet (distance is in "click" perimeter
            pressed = null;
        }
        target.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        target.mouseMoved(e);
    }




}
