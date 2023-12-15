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
package importexport;

import gui.views.ModelView;
import importexport.filters.JPEGFileFilter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import javax.imageio.ImageIO;
/*import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
*/

public class JPEGExport extends Export {

	boolean nogui = false;
	
	public JPEGExport(ModelView modelView, boolean nogui) {
		super(modelView, new JPEGFileFilter(), new String[] {"jpg","jpeg"});
		this.nogui = nogui;
	}

    public boolean isValid() {return true;}
	
	public BufferedImage getImage() {return getImage((int)Math.round(300*8.5));}
	public BufferedImage getImage(int widthPixel) {
        double scale = widthPixel/(double)modelView.getWidth();
        
        // int n = 400;
        int w = (int)Math.round(modelView.getWidth()*scale);
        int h = (int)Math.round(modelView.getHeight()*scale);
        BufferedImage img = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);

        Graphics g = img.getGraphics();

        // make it look nicer!
        Graphics2D g2 = (Graphics2D)  g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.scale(scale, scale);
        
      /*  g.fillRect(0, 0, w, h);
        boolean oldState = modelView.hideMessageObjectContainer;
        modelView.hideMessageObjectContainer=true;
        boolean grid = modelView.isGridShown();
        modelView.setGridShown(false);
        modelView.paintComponent(g);
        modelView.setGridShown(grid);
        modelView.hideMessageObjectContainer= oldState;
*/
        exportToGraphicsContext(g2);
        
	    return img;
	}
	
	public void export(File file) {
    	try {
    		//final JFileChooser fc = new JFileChooser();
    
    
    
    		ByteArrayOutputStream out = new ByteArrayOutputStream(0xfff);
    
    		int width = 500;
    		if (!nogui) {
    			DPIDialog dpiDialog = new DPIDialog();
    			width = dpiDialog.widthPixel;
    		}
    		BufferedImage img = getImage(width);
    		float quality = 0.98f;
            
			/*JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(img);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
            param.setQuality(quality, true);
            encoder.encode(img, param);
    */

			
            FileOutputStream fos = new FileOutputStream(file);
    		//fos.write(out.toByteArray());
			ImageIO.write(img, "jpg", fos);
    		fos.close();
    		out.close();
    	} catch (Exception exc) {
    		// TODO catch exception!
    		exc.printStackTrace();
    		JOptionPane.showMessageDialog(null, exc);
    	}
	}
}
