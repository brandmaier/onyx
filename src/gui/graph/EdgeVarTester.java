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
