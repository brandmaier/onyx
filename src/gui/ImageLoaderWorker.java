package gui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class ImageLoaderWorker extends SwingWorker<HashMap<String, Image>, Void> {


	public static String DEFAULT = "default";
	public static String SPARKLING = "sparkling";
	public static String WARNING = "warning";
	public static String ERROR = "error";
	public static String INFORMATION = "information";
	
//	private String[] filenames;
	private Class cl;

	public ImageLoaderWorker(Class cl)
	{
		this.cl = cl;
		//this.filenames = filenames;
	}
	
	// executed in the event-dispatch thread
	@Override
	protected void done() {

		  try {
			MessageObject.imageTable = get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	@Override
	protected HashMap<String, Image> doInBackground() throws Exception {
		
		HashMap<String, Image> images = new HashMap<String, Image>();
		

		URL  url =  cl.getResource("/icons/mono/warning32.png"); 
		if (url != null) {
			 Image imageWarning = Utilities.resizeImage(new ImageIcon(url).getImage(),MessageObject.SIZE,MessageObject.SIZE );
			 images.put("warning",imageWarning);
		}	
			 
			 url =  cl.getResource("/icons/mono/comment32.png"); 
			 if (url != null) {
			 	Image imageInformation = Utilities.resizeImage(new ImageIcon(url).getImage(),MessageObject.SIZE,MessageObject.SIZE);
			 	Image imageDefault = imageInformation;
			 	images.put("information",imageInformation);
			 	images.put("default",imageInformation);
			 }	
			 
			 url =  cl.getResource("/icons/mono/wrench32.png"); 
			 if (url != null) {
			 	Image imageGears = Utilities.resizeImage(new ImageIcon(url).getImage(),MessageObject.SIZE,MessageObject.SIZE);
			 	if (imageGears == null) {
			 		 System.err.println("Image not valid: wrench32.png");
			 	}
			 	 images.put("gears",imageGears);
			 }	else {
				 System.err.println("URL not valid: wrench32.png");
			 }
			 
			 url =  cl.getResource("/icons/mono/block32.png"); 
			 if (url != null) {
			 	Image imageError = Utilities.resizeImage(new ImageIcon(url).getImage(),MessageObject.SIZE,MessageObject.SIZE);
			 	 images.put("error",imageError);
			 }	

			 url = cl.getResource("/icons/mono/glitter32.png"); 
			 if (url != null) {
			 	Image imageSparkling = Utilities.resizeImage(new ImageIcon(url).getImage(),MessageObject.SIZE,MessageObject.SIZE);
			 	 images.put("sparkling",imageSparkling);
			 }	
			 
			
		
		return images;
	}

}
