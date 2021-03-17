/*
 * Created on 05.09.2012
 */
package gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class DesktopTransferHandler extends TransferHandler {
    
    Desktop desktop;
    
    public DesktopTransferHandler(Desktop desktop) {
        this.desktop = desktop;
    }

    public boolean canImport(JComponent arg0, DataFlavor[] arg1) {
        for (int i = 0; i < arg1.length; i++) {
          DataFlavor flavor = arg1[i];
          if (flavor.equals(DataFlavor.javaFileListFlavor)) {
            return true;
          }
          if (flavor.equals(DataFlavor.stringFlavor)) {
            return true;
          }
        }
        // Didn't find any that match, so:
        return false;
      }
    
    
    /**
     * Do the actual import.
     * 
     */
    public boolean importData(JComponent comp, Transferable t) {
      if (t==null) return false;
      DataFlavor[] flavors = t.getTransferDataFlavors();
      for (int i = 0; i < flavors.length; i++) {
        DataFlavor flavor = flavors[i];
        try {
          if (flavor.equals(DataFlavor.javaFileListFlavor)) {

            List<File> list = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
            for (File file:list) {
             // BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
             // desktop.importFromBuffer(in, file);
            	desktop.importFromFile(file, file.getName());	
//              String s = "";
//              while (in.ready()) s += in.readLine()+"\r\n";
//              desktop.importString(s, file);
              
              desktop.mainFrame.addToRecentFiles(file);
            }
            return true;
          } else if (flavor.equals(DataFlavor.stringFlavor)) {
            desktop.importString((String)t.getTransferData(flavor));
            return true;
          } else {
            // Don't return; try next flavor.
          }
        } catch (IOException ex) {
          System.err.println("IOError getting data: " + ex);
        } catch (UnsupportedFlavorException e) {
        }
      }
      // If you get here, I didn't like the flavor.
      return false;
    }
  

}
