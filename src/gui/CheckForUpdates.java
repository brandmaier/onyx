package gui;

import gui.frames.MainFrame;
import gui.frames.WelcomeFrame;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class CheckForUpdates implements Runnable
{

	//private MainFrame mainFrame;

	public CheckForUpdates(MainFrame mainFrame) {
	//	this.mainFrame  = mainFrame;
	}

	@Override
	public void run() {
		try {
			URL url = new URL("http://onyx.brandmaier.de/latest");
			
			InputStream is = url.openStream();
			
			String r = new Scanner(is).useDelimiter("\\Z").next();

			Integer latestVersion = Integer.parseInt(r);
			
			if (latestVersion > MainFrame.SVN_VERSION)
			{
				MainFrame.getContextHelpPanel().setText("Your version of "+WelcomeFrame.OMEGA + "nyx is out of date! Please download a new version on our website!");
				MainFrame.getContextHelpPanel().setVisible(true);
			}
			
		} catch (Exception e) {
			System.err.println("Cannot access latest version number!");
		}
		
		
		
	}

}
