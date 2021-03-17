package gui.graph;

import java.awt.Graphics;

import javax.swing.JFrame;

public class EdgeVarTester {
	public static void main(String[] args) {
		
		JFrame frame = new JFrame() {
			public void paintComponent(Graphics g) {
				int per_row = 4; int size=50;
				for (int i=0; i < 10; i++) {
					int x = (i % per_row) * 50;
					int y = (int) Math.floor(i/4) * 50;
					int width = size;
					int height = size;
					int startAngle = i*10;
					int arcAngle = startAngle*20;
					
					g.drawArc(x, y, width, height, startAngle, arcAngle);
				}
			}
		};
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}
