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
package importexport;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import engine.CombinedDataset;
import engine.Preferences;
import engine.backend.Model;
import gui.Desktop;
import gui.frames.MainFrame;
import gui.graph.Graph;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;
import gui.graph.VariableContainer;

public abstract class Export {

	protected ModelView modelView;
	private FileFilter fileFilter;
	String[] defaultExtensions;
	
	public final String DATAFILENAME = "DATAFILENAME";
	
	// maps variable 
	HashMap<String, String> nameMapping = new HashMap<String, String>();
	HashMap<VariableContainer,String> varMapping = new HashMap<VariableContainer, String>();
	public HashMap<VariableContainer,String> getVariableMapping() {return varMapping; }
	
	boolean useStartingValues = false;

    public Export(ModelView modelView, FileFilter fileFilter, String[] defaultExtensions)
    {
        this.modelView = modelView;
        this.fileFilter = fileFilter;
        this.defaultExtensions = defaultExtensions;
    }

    public String getHeader() {return "Export";}	

    public String getMissingDataString() {return ""+Model.MISSING;}
    
    public abstract boolean isValid();
	
	public void resetNames()
	{
		nameMapping.clear();
		varMapping.clear();
	}
	
	protected String convert(String name)
	{
		return name;
	}
	
	public static String colorToHexString(Color color)
	{
		return("#"+Integer.toHexString(color.getRGB() & 0x00ffffff).toString());
	}
	
	public String makeSaveString(String fullname)
	{
		return(makeSaveString(fullname, null));
	}
	
	public String makeSaveString(String fullname, VariableContainer container)
	{
		
		// if variable name was mapped already, return result
		if (nameMapping.containsKey(fullname)) {
			return nameMapping.get(fullname);
		} else {
			
			// map to save name
			String name = convert(fullname);
			
			//int pos = name.length()-1;
			int counter = 0;
			String guess = "";
			boolean found = false;
			while (!found)
			{
				
				String cnt = Integer.toString(counter);
				if (counter==0) {
					guess = name;
				} else {
					guess = name.substring(0, name.length()-cnt.length())+cnt;
				}

				if (!duplicateName(guess)) {
					nameMapping.put(fullname, guess);

					found=true;
				} else {
					counter+=1;
				}
			}
			
			if (container!=null) {
				varMapping.put(container, guess);
			}
			
			return guess;
		}
	}
	
	


	private boolean duplicateName(String guess) {
		for (String v : nameMapping.values())
		{
			
			if (v != null && v.equals(guess)) return true;
		}
		return false;
	}

	public boolean isUseStartingValues() {
		return useStartingValues;
	}

	public void setUseStartingValues(boolean useStartingValues) {
		this.useStartingValues = useStartingValues;
	}


    public abstract void export(File file) throws Exception;
	
	public void exportToGraphicsContext(Graphics2D g)
	{
		//public BufferedImage getImage(Graphics2D g) {
//		System.out.println("Export");

			this.modelView.getGraph().selectAll(false);
			
	       // g.setColor(modelView.getBackground());
			
	        g.setColor(modelView.getGraph().backgroundColor);
			g.fillRect(0, 0, this.modelView.getWidth(), this.modelView.getHeight());
	        boolean oldState = modelView.hideMessageObjectContainer;
	        modelView.hideMessageObjectContainer=true;
	        boolean decor = modelView.hideBorderDecorators;
	        modelView.hideBorderDecorators = true;
	        boolean grid = modelView.isGridShown();
	        boolean markUnconnected = modelView.getGraph().isMarkUnconnectedNodes();
	        modelView.getGraph().setMarkUnconnectedNodes(false);
	        modelView.setGridShown(false);
	        modelView.paintComponent(g);
	        modelView.setGridShown(grid);
	        modelView.getGraph().setMarkUnconnectedNodes(markUnconnected);
	        modelView.hideBorderDecorators = decor;
	        modelView.hideMessageObjectContainer= oldState;

		   
		//}
	}
	
	public File export() {
		try {
			
			
			File dir = new File(Preferences.getAsString("DefaultWorkingPath"));
			
			
			final JFileChooser fc = new JFileChooser(dir);

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			//fc.setFileHidingEnabled(false);
//			fc.setFileFilter(fileFilter);
		
			fc.addChoosableFileFilter(fileFilter);
			fc.setAcceptAllFileFilterUsed(true);
			
			
			fc.setDialogTitle("Export model");
			//In response to a button click:
			int returnVal = fc.showSaveDialog(modelView );

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				File file = fc.getSelectedFile();

				// System.out.println(file.getAbsolutePath());
				Preferences.set("DefaultWorkingPath", file.getParentFile().getAbsolutePath());

				if (file.getName().equals("")) return null;

				//if (!file.getName().endsWith("."+defaultExtension)
				//		&& !file.getName().endsWith(".jepg")) {
				boolean hasExtension = false;

				for (String ext : defaultExtensions) {
					if (file.getName().endsWith("."+ext)) hasExtension=true;
				}

				if (!hasExtension)
					file = new File(file.getAbsolutePath() + "."+ defaultExtensions[0]);

				boolean ok = true;
				if (file.exists()) {
					int result = JOptionPane.showConfirmDialog(this.modelView, "Warning: File exists!",
							"A file with the selected name already exists. Do you want to overwrite the existing file?",
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null);
					if (result == JOptionPane.CANCEL_OPTION || (result == JOptionPane.NO_OPTION)) {
						ok = false;
					}
					//System.out.println("OK"+ok);
				} 

				if (ok) {
					if (modelView != null)
				    if (modelView.getName().equals("Unnamed Model")) {
				        String name = file.getName(); 
				        if (name.indexOf('.') != -1) name = name.substring(0, name.indexOf('.'));
				        modelView.setName(name);
				    }
				    export(file);
				}

				return file;
				
			} else {
				//JOptionPane.showMessageDialog(null, "An error occured during saving the model!", "Save model", JOptionPane.OK_OPTION);

				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.modelView, "An error has occured!");
		}
		
		return null;
	}
	
	public void export(String filename) throws Exception
	{
		File file = new File(filename);
		export(file);
	}
	
	 /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
 
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
	public static void createFile(File file, String content) throws Exception
	{
	
	        
	        BufferedWriter writer = null;


	        try {
	            writer = new BufferedWriter(new FileWriter(file));
	            writer.write(content);
	            writer.flush();
	        } catch (Exception e) {
	            throw new Exception("Cannot write to temporary file: " + e.toString());
	        } finally {
	            try {
	                writer.close();
	            } catch (Exception einner) {
	            	einner.printStackTrace();
	            }
	        }
	}
	
/*  // Data treatment is now done via a temporary file. 

	boolean withData = false;
	double[][] data;
	
	public boolean isWithData() {
		return withData;
	}

	public void setWithData(boolean withData) {
		this.withData = withData;
	}

	public String exportData()
	{
		return exportData(3);
	}
	
	public String exportData(int rounding)
	{
//		data = modelView.assembleData();
		data = (new CombinedDataset(Desktop.getLinkHandler(), modelView.getGraph().getNodes())).rawData;
		
		String names = "c(";
		int[] filter = modelView.getModelRequestInterface().getObservedIds();
		LinkHandler link = Desktop.getLinkHandler();
		Graph graph = modelView.getGraph();
		for (int i = 0; i < filter.length; i++) {
			DatasetField datasetField = link.getDatasetField(
					graph.getNodeById(filter[i]).getObservedVariableContainer());
			
			names+="\""+datasetField.dataset.getColumnName(datasetField.columnId)+
					"\",";
			
		}
		
		names = names.substring(0, names.length()-1);
		names+=")";
		
		String dataString = "data <- data.frame(matrix(c(";
		
		double fac = Math.pow(10,rounding);
		
		for (int i=0; i < data.length; i++)
		{
			for (int j=0; j < data[i].length;j++)
			{
				String ds = "";
				if (Model.isMissing(data[i][j])) {
					ds = "NA";
				} else {
					ds = Double.toString( Math.round(data[i][j]*fac)/fac	);
				}
				
				dataString+=ds+",";
			}
		}
		dataString = dataString.substring(0, dataString.length()-1);
		
		dataString+="),byrow=T,nrow="+data.length+"));";
		dataString+="\nnames(data)<-"+names+";";
		return(dataString);
	}
	*/
}
