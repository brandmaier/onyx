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
package gui.tutorial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import gui.Desktop;
import gui.Utilities;
import gui.views.View;

public class TutorialView extends View implements MouseListener, ActionListener {

	private static final long serialVersionUID = 1L;
	GeneralPath framePath;
	int stroke;
	JLabel label;
	private JMenuItem menuClose;

	public TutorialView(Desktop desktop) {
		this.desktop = desktop;
		this.setSize(300, 200);
		this.setOpaque(false);
		this.setResizable(false);
		this.setSelectable(false);
		this.setMinimizable(false);

		int w = this.getWidth();
		int h = this.getHeight();
		int offset_top = 15;
		int offset_curve = 15;
		int offset_xarrow = 50;
		int width_arrow = 30;
		stroke = 2;

		framePath = new GeneralPath();
		framePath.moveTo(offset_curve, offset_top); // top left, after curve
		framePath.lineTo(w - offset_curve - offset_xarrow - 2 * width_arrow, offset_top); // top right before arrow
		framePath.lineTo(w - offset_curve - offset_xarrow - width_arrow, 0); // peak arrow
		framePath.lineTo(w - offset_curve - offset_xarrow, offset_top); // right end arrow
		framePath.lineTo(w - offset_curve, offset_top); // top right corner before curve
		// framePath.quadTo(w,0, w, offset_top+offset_curve); //top right curve
		framePath.lineTo(w - stroke, offset_top + offset_curve);
		framePath.lineTo(w - stroke, h - stroke);
		framePath.lineTo(offset_curve, h - stroke); // bottom left before curve
		// framePath.quadTo(0, h, 0, h-offset_curve);
		framePath.lineTo(0, h - offset_curve);
		framePath.lineTo(0, h - offset_curve - offset_top);
		framePath.lineTo(0, offset_top + offset_curve);
		framePath.lineTo(offset_curve, offset_top);

		label = new JLabel("No text available");
		this.add(label);

		int inset = 10;
		label.setSize(w - 2 * inset, h - 2 * inset);
		label.setLocation(inset, inset);

		this.setX(300);
		this.setY(300);

		label.addMouseListener(this);
		this.addMouseListener(this);
	}

	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setStroke(new BasicStroke(stroke));

		g.setColor(Color.WHITE);
		g2d.fill(framePath);
		g.setColor(Color.DARK_GRAY);
		g2d.draw(framePath);
		super.paint(g);

	}

	public void setText(String string) {
		this.label.setText("<html>" + string + "</html>");

	}

	public void mouseClicked(MouseEvent arg0) {

		System.out.println("Mouse clicked!");
		if (Utilities.isRightMouseButton(arg0)) {

			JPopupMenu menu = new JPopupMenu();
			menuClose = new JMenuItem("Close");
			menu.add(menuClose);
			menuClose.addActionListener(this);
			try {
				menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			} catch (java.awt.IllegalComponentStateException e) {
				e.printStackTrace();
			}
		}

		super.mouseClicked(arg0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == menuClose) {
			desktop.remove(this);
			desktop.removeView(this);

		}

	}
}
