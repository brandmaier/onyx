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

import java.io.File;

import javax.swing.SwingUtilities;

import parallelProcesses.ParallelProcessHandler;



public class Master {

	 static  boolean fDevMode;
	 static String filename = null;
	 
	
	 
	/**
	 * Main entry point for Onyx.
	 * Parse command line arguments if any and pass to Batch class. Otherwise start
	 *  Master class.
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
		//	URL url = this.getClass().getResource("./icons/icon_256.png");
		//	Image image = (new ImageIcon(url)).getImage();
		    //mFrame.setIconImage(image);
		
			// in MAC OSX we get this from Frame.setIconImage(image); :
			//Oct 23 13:47:10 lip-osx-157-82 java[78938] <Error>: CGContextGetCTM: invalid context 0x0
		//	Oct 23 13:47:10 lip-osx-157-82 java[78938] <Error>: CGContextSetBaseCTM: invalid context 0x0
	//		Oct 23 13:47:10 lip-osx-157-82 java[78938] <Error>: CGContextGetCTM: invalid context 0x0
//			Oct 23 13:47:10 lip-osx-157-82 java[78938] <Error>: CGContextSetBaseCTM: invalid context 0x0

			if (filename != null && filename.length() > 0) {
				Desktop desktop = mFrame.getDesktop();
				File file = new File(filename);
	            desktop.importFromFile(file, file.getName(), 10, 10);
				
			}
			
		// setting dock image on OSX - commented out
		//    Application application = Application.getApplication();
		//	application.setDockIconImage(image);
	
			new ParallelProcessHandler();
			
		} catch (Exception e) {
			System.err.println("Cannot set icon for frame!");
		}
		
		mFrame.setVisible(true);
		
		if (showSplashScreen)
			welcome.toFront();

	}

    
	
}
