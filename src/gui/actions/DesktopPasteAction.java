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
import gui.FileLoadingException;
import gui.views.ModelView;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;


public class DesktopPasteAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	String name;
	
    public DesktopPasteAction(Desktop desktop) {this(desktop,0,0);}
    public DesktopPasteAction(Desktop desktop, int x, int y) 
	{
		this.desktop = desktop;
	
		putValue(NAME, "Paste");
        putValue(SHORT_DESCRIPTION, "Pastes Clipboard on the desktop");
        this.x = x;
        this.y = y;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
        Clipboard clipboard = desktop.getToolkit().getSystemClipboard();
        Transferable incoming = clipboard.getContents(this);
        desktop.getTransferHandler().importData(desktop, incoming);
	}

}
