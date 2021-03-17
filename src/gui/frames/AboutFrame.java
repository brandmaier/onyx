package gui.frames;


import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class AboutFrame extends JFrame {

	public AboutFrame()
	{
		setSize(400,600);
		setBackground(Color.white);
		
		/*String version = this.getClass().getPackage().getImplementationVersion();
		
		if (version == null) {
			version = "";
		} else {
			version = "Version: "+version;
		}
		*/
		
		String version = MainFrame.MAJOR_VERSION+"-"+Integer.toString(MainFrame.SVN_VERSION);
		
		Annotation[] annot = this.getClass().getPackage().getAnnotations();
		
		
		String license = "<br><br><br>";
		try {
		InputStream is = getClass().getResourceAsStream("/Onyx License");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;
		
			while (br.ready())
			{
				license+= br.readLine()+"<br>\n";
			}
		} catch (Exception e) {
		
			e.printStackTrace();
		}
		license+="";
		
		String text = "<html><center><h1>"+WelcomeFrame.OMEGA+"NYX</h1>" +
				"<p>&nbsp;</p>" +
			//	"a collaboration of University of Virginia and the Center for Lifespan Psychology at the Max-Planck-Institute for Human Development"
				"<br><br>Build number:"+version+"<br>"+license+"<br><br>"
				+"</center></html>";
		
		//System.out.println(text);
		
		JLabel label = new JLabel(text);
		
		this.getContentPane().add(label, BorderLayout.CENTER);
		
		this.setLocationRelativeTo(null); // center frame on screen

		this.pack();
		
		setVisible(true);
	}
}
