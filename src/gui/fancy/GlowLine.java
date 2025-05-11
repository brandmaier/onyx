package gui.fancy;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;


public class GlowLine extends JFrame {

    public GlowLine() {
        setTitle("Glow Line Example");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Create a simple line
        int x1 = 50, y1 = 200;
        int x2 = 350, y2 = 200;

        // Draw the original line
     /*   g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
*/
        
        // Create a buffered image to hold the blurred line
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bufferedGraphics = bufferedImage.createGraphics();

        // Draw the blurred line on the buffered image
        bufferedGraphics.setColor(new Color(0, 255, 0, 50)); // Adjust alpha for transparency
        bufferedGraphics.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        bufferedGraphics.draw(new Line2D.Double(x1, y1, x2, y2));

        // Apply Gaussian blur filter to the buffered image
        float[] blurMatrix = {
                1 / 16f, 1 / 8f, 1 / 16f,
                1 / 8f, 1 / 4f, 1 / 8f,
                1 / 16f, 1 / 8f, 1 / 16f
        };
        BufferedImageOp blurFilter = new ConvolveOp(new Kernel(3, 3, blurMatrix), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage blurredImage = blurFilter.filter(bufferedImage, null);

        // Draw the blurred image on the original graphics
        g2d.drawImage(blurredImage, 0, 0, null);
    }


    public static void main(String[] args) {
       new GlowLine();
    }
}