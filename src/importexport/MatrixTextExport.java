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

public class MatrixTextExport extends StringExport {
	
	public MatrixTextExport(ModelView modelView) {
		super(modelView, new TextFileFilter(),new String[] {"txt","dat"});
	}
	
	public String getHeader() {return "RAM Matrices";}	
	
	public boolean isValid() {return !modelView.hasDefinitionEdges();}
	
    @Override
    public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
        return modelView.getModelRequestInterface().getModel().getMatrixDescription();
    }

	
}
