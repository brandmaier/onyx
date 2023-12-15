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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BackgroundPane extends JPanel {

    private BufferedImage img;

    @Override
    public Dimension getPreferredSize() {
        BufferedImage img = getBackgroundImage();

        Dimension size = super.getPreferredSize();
        if (img != null) {
            size.width = Math.max(size.width, img.getWidth());
            size.height = Math.max(size.height, img.getHeight());
        }

        return size;
    }

    public BufferedImage getBackgroundImage() {
        return img;
    }

    public void setBackgroundImage(BufferedImage value) {
        if (img != value) {
            BufferedImage old = img;
            img = value;
            firePropertyChange("background", old, img);
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage bg = getBackgroundImage();
        if (bg != null) {
            int x = (getWidth() - bg.getWidth()) / 2;
            int y = (getHeight() - bg.getHeight()) / 2;
            g.drawImage(bg, x, y, this);
        }
    }

}
