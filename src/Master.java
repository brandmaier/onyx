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
/**
 * 
 *  _____                  
 * |  _  |                 
 * | | | |  __  _   ___  __
 * | | | | '  \| | | \ \/ /
 * \ \ / / | | | |_| |>  < 
 * /_| |_\ |_| |_|\__/_/\_\
 *               __/ |     
 *              |___/      
 * 
 * written by Timo von Oertzen and Andreas M. Brandmaier
 * 
 * 
 * Dependent source code:
 *  - vectorgraphics2d (by Erich Seifert; LGPL-3)
 *  - diff match patch (by Neil Fraser; Apache Licence 2.0)
 */
import gui.Desktop;
import gui.ImageLoaderWorker;
import gui.frames.MainFrame;
import gui.frames.WelcomeFrame;

import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;

import parallelProcesses.ParallelProcessHandler;



public class Master {

	 static  boolean fDevMode; // defines developer mode, if TRUE, undocumented and experimental functions are activated
	 static String filename = null;
	 
	
	 
	/**
	 * Main entry point for Onyx.
	 * Parse command line arguments if any and pass to Batch class. Otherwise start
	 * GUI in Master class.
	 *  
	 * @param args
	 */
	public static void main(String[] args) {

		// reduce mouse drag sensitivity
		//System.setProperty("awt.dnd.drag.threshold", "25");
		
		Arguments arguments = Arguments.parse(args);
		if (arguments.containsKey("--input-file")) filename = arguments.get("--input-file");
		 
		if (arguments.isBatch()) {
			System.out.println("Running Onyx in batch mode.");
			Batch.convert(arguments);
			
			System.exit(0);
		}
		
		
	   final boolean developerMode = arguments.isDeveloper();

      
	    // using invokeLater for GUI threads 
		// as suggested by http://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	
		    		Master master = new Master();
		    		master.startUp(true, developerMode, filename);


		    }
		});
	}

	private void startUp(boolean showSplashScreen, boolean devloperMode, String filename) {
		
		// start a background thread that loads all images
		(new ImageLoaderWorker(this.getClass())).execute();
		   
        // show splash screen
        WelcomeFrame welcome = null;
        if (showSplashScreen) {
	        welcome = new WelcomeFrame();
	        welcome.setVisible(true);
	        welcome.toFront();
        }
		
        
        // start GUI mainframe
		MainFrame mFrame = new MainFrame();
        mFrame.setDeveloperMode(devloperMode);
        


        // set an application image (currently broken)
		try {
			
			 //from JDK 9 on 
	        final Taskbar taskbar = Taskbar.getTaskbar();

	        try {
	        	final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
	            final URL imageResource = Master.class.getClassLoader().getResource("images/onyx-taskbar.png");
	            final Image image = defaultToolkit.getImage(imageResource);
	            //set icon for mac OSX
	            taskbar.setIconImage(image);
	        } catch (final UnsupportedOperationException e) {
	            System.out.println("The os does not support: 'taskbar.setIconImage'");
	        } catch (final SecurityException e) {
	            System.out.println("There was a security exception for: 'taskbar.setIconImage'");
	        }


			
		} catch (Exception e) {
			System.err.println("Cannot set icon for taskbar.");
		}
		
		if (filename != null && filename.length() > 0) {
			Desktop desktop = mFrame.getDesktop();
			File file = new File(filename);
            try {
				desktop.importFromFile(file, file.getName(), 10, 10);
			} catch (IOException e) {
				
				// TODO throw a GUI error?!
				e.printStackTrace();
			}
			
		}

		new ParallelProcessHandler();
		
		mFrame.setVisible(true);
		
		if (showSplashScreen)
			welcome.toFront();

	}

    
	
}
