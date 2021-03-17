package importexport;

import gui.views.ModelView;

import importexport.filters.JPEGFileFilter;
import importexport.filters.PNGFileFilter;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class PNGExport extends Export {

	public PNGExport(ModelView modelView) {
		super(modelView, new PNGFileFilter(), new String[] {"png"});
	}

    public boolean isValid() {return !modelView.hasDefinitionEdges();}

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
        
        g.fillRect(0, 0, w, h);
       /* boolean oldState = modelView.hideMessageObjectContainer;
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
    
    
    
    	//	ByteArrayOutputStream out = new ByteArrayOutputStream(0xfff);
    
    		//DPIDialog dpiDialog = new DPIDialog();
    
    		/*BufferedImage img = getImage(dpiDialog.widthPixel);
    		float quality = 0.98f;
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
            encoder.encode(img);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
            param.setQuality(quality, true);
            encoder.encode(img, param);
    
            FileOutputStream fos = new FileOutputStream(file);
    		fos.write(out.toByteArray());
    		fos.close();
    		out.close();*/
    		BufferedImage bi = getImage();
    	  
    	   // System.out.println(outputfile);
    	    ImageIO.write(bi, "png", file);
    		
    	} catch (Exception exc) {
    		// TODO catch exception!
    		exc.printStackTrace();
    	}
	}
}
