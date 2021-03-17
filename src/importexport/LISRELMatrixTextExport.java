package importexport;

import importexport.filters.RFileFilter;
import importexport.filters.TextFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import engine.ParameterReader;

import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;

public class LISRELMatrixTextExport extends StringExport {
	
	public LISRELMatrixTextExport(ModelView modelView) {
		super(modelView, new TextFileFilter(),new String[] {"txt","dat"});
	}
	
	public String getHeader() {return "LISREL Matrices";}	
	
	public boolean isValid() {
	    try {
	        String res = modelView.getModelRequestInterface().getModel().getLISRELMatrixDescription();
	        if (res.startsWith("Error")) return false;
	        return true;
	    } catch (Exception e) {return false;}
	}
	
	public void export(File file)
	{
		String content = modelView.getModelRequestInterface().getModel().getLISRELMatrixDescription();
	
        try {
			createFile(file, content);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @Override
    public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
    	Graph g = modelView.getGraph();
    	
		if (modelView.hasDefinitionEdges()) return "Error! Definition variables are not supported!";
		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";
    	
        return modelView.getModelRequestInterface().getModel().getLISRELMatrixDescription();
    }

	
}
