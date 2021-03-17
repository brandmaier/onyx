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