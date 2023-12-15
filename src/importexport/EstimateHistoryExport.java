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

import engine.ModelRun;
import engine.ModelRunUnit;
import engine.ParameterReader;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;

public class EstimateHistoryExport extends StringExport {
	
    private ParameterReader explicitRunner = null;
    
	public EstimateHistoryExport(ModelView modelView) {
		super(modelView, new TextFileFilter(),new String[] {"txt","dat"});
	}
	
	public void setExplicitRunner(ParameterReader reader) {this.explicitRunner = reader;}
	public ParameterReader getExplicParameterReader() {return explicitRunner;}
	
	public String getHeader() {return "Estimate History";}
	
	public boolean isValid() {return true;}
	
    @Override
    public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
    	ParameterReader se = (explicitRunner==null?modelView.getShowingEstimate():explicitRunner);
        if (se != null)
            return se.getHistoryString();
        return "No History to show.";
    }

	
}
