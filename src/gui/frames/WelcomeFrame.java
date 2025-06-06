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
package gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import engine.Preferences;

public class WelcomeFrame extends JFrame implements MouseListener {

	Timer timer;
	
    public static final char OMEGA = '\u03a9';

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4582562843193871011L;

	public class CloseTask extends TimerTask
	{
		JFrame frame;

		public CloseTask(JFrame frame)
		{
			this.frame = frame;
		}
		
		@Override
		public void run() {
			frame.setVisible(false);
			frame.dispose();
			timer.cancel();
			
			// welcome.dispose();
			String prop = null;
			try {prop = Preferences.getAsString("ShowTipOfTheDay"); }
			catch (Exception e) {}
			if (prop == null || prop.equals("true"))			
			{
				//System.out.println("Show tip!");
				TipOfTheDayFrame tip = new TipOfTheDayFrame();
				tip.setVisible(true);
				tip.toFront(); 
			}
		}
		
	}
	
	 public static Image getScaledImage(Image srcImg, int w, int h){
	        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g2 = resizedImg.createGraphics();

	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        g2.drawImage(srcImg, 0, 0, w, h, null);
	        g2.dispose();

	        return resizedImg;
	    }
	
	public WelcomeFrame()
	{
		timer = new Timer();
		
		timer.schedule(new CloseTask(this), 3500);
		
		 URL  url =  this.getClass().getResource("/images/onyx-welcome.png"); 

	
		 
		 
		this.setBackground(Color.white);
		ImageIcon ii = new ImageIcon(url);
		JLabel lab = new JLabel( new ImageIcon(WelcomeFrame.getScaledImage(ii.getImage(), 300, 150)));
		//JLabel lab = new JLabel( ii );
		
		try {
		Graphics2D g2d = (Graphics2D)lab.getGraphics();
		// set anti-aliasing
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	     // Set rendering quality
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Enable fractional metrics for text
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		// Interpolation
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        // Alpha Interp.
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		} catch (Exception e) {}
		
		lab.setHorizontalTextPosition(JLabel.RIGHT);
		lab.setFont( new Font("Arial", Font.PLAIN, 82));
		lab.setBackground(Color.white);
		lab.setOpaque(true);
		
		this.setLayout( new BorderLayout() );
		this.add(lab, BorderLayout.CENTER);

		this.setUndecorated(true);
//		this.setSize(330, 150);
//		this.setSize(600, 300);
		this.pack();
		this.setLocationRelativeTo(null); // center frame on screen

		
	}
	
	public static void main(String[] args)
	{
		JFrame f = new WelcomeFrame();
		
		f.setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
