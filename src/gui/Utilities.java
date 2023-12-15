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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.SwingUtilities;

//import sun.swing.SwingUtilities2;

public class Utilities {
	
	public static Image resizeImage(Image originalImage, int IMG_WIDTH, int IMG_HEIGHT)
	{

		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		
		
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		g.setComposite(AlphaComposite.Src);

		return(resizedImage);
	
	}

	public static long profileTimeStamp = System.currentTimeMillis();
	
	public static void profile(String name) {
		
		System.out.println(name + (System.currentTimeMillis()-profileTimeStamp) );
		
		profileTimeStamp = System.currentTimeMillis();
	}

	public static boolean isRightMouseButton(MouseEvent event)
	{
/*		return (!event.isConsumed()) && ((event.getButton()==MouseEvent.BUTTON3)
				|| (event.getButton()==MouseEvent.BUTTON1 && event.isControlDown()));
	*/
//		return (!event.isConsumed() && event.isControlDown());
		return (((event.getModifiers() & 0x4) == 4) ||
				((event.getModifiers() & 0x10) != 0) && event.isControlDown());
	}
	
	public static boolean isLeftMouseButton(MouseEvent event) {
//		return (!event.isConsumed()) && ((event.getButton()==MouseEvent.BUTTON1));
		//return ((event.getButton()==MouseEvent.BUTTON1));
		return ((event.getModifiers() & 0x10) != 0) && (!event.isControlDown());
	}
	
	public static void paintGlow(Graphics2D g2d, Shape edgePath, int size, float shapesize)
	{
		Composite comp = g2d.getComposite();
		size = 8;
		for (int j = 0; j<size; j++) {
			Stroke shiny = new BasicStroke(shapesize+j);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			/*g2d.setPaint(new Color(0.6f,0.8f,0.8f,j*(1/(size+1.0f))));
			g2d.setColor(new Color(0.6f,0.8f,0.8f,j*(1/(2*size+1.0f))));
*/
			g2d.setColor(new Color(80,180,180,8*size-j*8));
			
			//g2d.setColor(new Color(0,100,100,8*size-j*8));
			//			g2d.setColor(new Color(0.6f,0.8f,0.8f,j*(1/(size+1.0f))));
			g2d.setStroke(shiny);
			g2d.draw(edgePath);
		}
		
		g2d.setComposite(comp);
	}
	
	public static String readFileContentsUTF8(File file) throws IOException
	{
		String UTF8result = "";
		
	   	   BufferedReader in
	   	   = new BufferedReader(new FileReader(file));
	   	      
	   	   String strLine;
	   	   while ((strLine = in.readLine()) != null)   {
	   	  // Print the content on the console
	   		UTF8result += new String(strLine.getBytes(),"UTF-8");
	   	  
	   	   }
	   	
	   	return UTF8result;
	}
	
}
