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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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

public abstract class RExport extends StringExport {

	public static final int LABEL = 3, TO = 2;

	public String getHeader() {return "R export";}

	public RExport(ModelView modelView) {
		this(modelView, new RFileFilter(),new String[] {"R","r"});
		useStartingValues = true;
	}

    public RExport(ModelView modelView, FileFilter fileFilter, String[] defaultExtensions)
    {
        super(modelView, fileFilter, defaultExtensions);
    }
	
    @Override
    public String getMissingDataString() {return "";}
	
	public String convert(String s)
	{
		if (s == null) return null;
		return s.replaceAll("[^A-Za-z0-9]", "_");
	}

	protected Object round(double value) {
		return Math.round(value*100.0)/100.0;
	}
	
}
