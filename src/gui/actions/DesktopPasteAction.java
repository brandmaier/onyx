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
