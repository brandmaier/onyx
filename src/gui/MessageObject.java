package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class MessageObject {

	private String textMessage;
	private String imageRef;
	private Image image;
	private ImageObserver imgObserver;
	
	//public static Image bulbImage = new ImageIcon("./src/icons/570133364965954419-bulb-32.png").getImage();
	public final static int SIZE = 24;
	

	
//	static BufferedImage img;
//	public static Image imageWarning, imageError, imageInformation, imageGears, imageDefault;
//	public static Image imageSparkling;
	public static HashMap<String, Image> imageTable = new HashMap<String, Image>();
	
	
	/*static {
		 try {
			img = ImageIO.read(new File("./src/icons/icon_info_SIZE.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 

	}*/
	
/*	public static void init(Class cl)
	{
		if (imageWarning == null) {
			 URL  url =  cl.getResource("/icons/mono/warning32.png"); 
			 if (url != null) {
			 	imageWarning = resizeImage(new ImageIcon(url).getImage(),SIZE,SIZE );
			 }	
			 
			 url =  cl.getResource("/icons/mono/comment32.png"); 
			 if (url != null) {
			 	imageInformation = resizeImage(new ImageIcon(url).getImage(),SIZE,SIZE);
			 }	
			 
			 url =  cl.getResource("/icons/mono/wrench32.png"); 
			 if (url != null) {
			 	imageGears = resizeImage(new ImageIcon(url).getImage(),SIZE,SIZE);
			 	if (imageGears == null) {
			 		 System.err.println("Image not valid: wrench32.png");
			 	}
			 }	else {
				 System.err.println("URL not valid: wrench32.png");
			 }
			 
			 url =  cl.getResource("/icons/mono/block32.png"); 
			 if (url != null) {
			 	imageError = resizeImage(new ImageIcon(url).getImage(),SIZE,SIZE);
			 }	

			 url = cl.getResource("/icons/mono/glitter32.png"); 
			 if (url != null) {
			 	imageSparkling = resizeImage(new ImageIcon(url).getImage(),SIZE,SIZE);
			 }	
			 
			 imageDefault = imageInformation;
		}
	}
	*/
	
	/**
	 * 
	 * using Mono icon set
	 * http://www.tutorial9.net/downloads/108-mono-icons-huge-set-of-minimal-icons/
	 * 
	 */
	public MessageObject()
	{
		
	}
	
	public MessageObject(String textMessage) {
		super();
		this.setTextMessage(textMessage);
		this.setImageRef(ImageLoaderWorker.DEFAULT);
	}

	public MessageObject(String textMessage, String imageRef) {
		this();
		this.setTextMessage(textMessage);
		this.setImageRef(imageRef);
	}
	
	public MessageObject(String textMessage, String imageRef, ImageObserver imgObserver) {
		this();
		this.setTextMessage(textMessage);
		this.setImageRef(imageRef);
		this.setImgObserver(imgObserver);
	}
	
	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String getImageRef() {
		return imageRef;
	}

	public void setImageRef(String imageRef) {
		this.imageRef = imageRef;
	}
	
	public String toString()
	{
		return "MessageObject: "+this.textMessage;
	}

	public void paint(Graphics g, int x, int y, int width, int height)
	{
		/*g.setColor(Color.white);
		g.fillRect(0,0,width, height);
		*/
		if (image == null) {
			image = imageTable.get(getImageRef());
		}
		
		if (image != null) {
			g.setColor(Color.black);
		//	g.drawRect(x, y, width, height);
		//	((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		//			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, x, y, width, height, imgObserver);
		} else {
//			System.err.println("Error!");
			g.setColor(Color.black);
			g.fillOval(x, y, width, height);
			//g.setColor(Color.black);
			
		}
	}

	public void setImgObserver(ImageObserver imgObserver) {
		this.imgObserver = imgObserver;
	}
	

	
}
