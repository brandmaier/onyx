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
package gui.actions;

import gui.Desktop;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


public class LoadAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	
	Desktop desktop;
	int x,y;
	String name;
	File file = null;
	
	public LoadAction(Desktop desktop)
	{
		this(desktop, null, null, 0,0);
	}
	
    public LoadAction(Desktop desktop, File file) {this(desktop,file,0,0);}
    public LoadAction(Desktop desktop, File file, int x, int y) {this(desktop,file,file.getName(),x,y);}
	public LoadAction(Desktop desktop, File file, String name, int x, int y)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Load Model or Data");
        putValue(SHORT_DESCRIPTION, "Loads file (model or data set) on the desktop");
		this.name = name;
		this.file = file;
        this.x = x;
        this.y = y;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {


    	try {
    		
    		   if (file == null) 
    		   {
    //			 desktop.impo  
    			   desktop.load();
    		   } else {
    			   	desktop.importFromFile(file, file.getName(), x, y);
    		   }
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this.desktop, "Unable to load file!");
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.desktop, "Unable to load file!");
			e.printStackTrace();
		} 
		
	}

}
