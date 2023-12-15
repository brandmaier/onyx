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
