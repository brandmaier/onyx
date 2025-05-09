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
			
			if (latestVersion > MainFrame.MINOR_VERSION)
			{
				MainFrame.getContextHelpPanel().setText("Your version of "+WelcomeFrame.OMEGA + "nyx is out of date! Please download a new version on our website!");
				MainFrame.getContextHelpPanel().setVisible(true);
			}
			
		} catch (Exception e) {
			System.err.println("Cannot access latest version number!");
		}
		
		
		
	}

}
