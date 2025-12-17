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
import java.util.HashSet;
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

public abstract class GenericSyntaxExport extends StringExport {


	protected List<String> parameterSlotNames;
	protected List<Node> observeds;
	protected List<Node> latents;
	
	public static final int LABEL = 3, TO = 2;

	public String getHeader() {return "R export";}

	public GenericSyntaxExport(ModelView modelView) {
		this(modelView, new RFileFilter(),new String[] {"R","r"});
		useStartingValues = true;
	}

    public GenericSyntaxExport(ModelView modelView, FileFilter fileFilter, String[] defaultExtensions)
    {
        super(modelView, fileFilter, defaultExtensions);

		parameterSlotNames = new ArrayList<String>();

    }
    
    // TODO make this abstract and re-sort the OMX classes
	protected  String createEdgeString(Edge edge, String keyword, ParameterReader startingValues, boolean useUniqueNames)
	{
		return("");
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
	

	@Override
	public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
		Graph g = modelView.getGraph();
		parameterSlotNames.clear();
	    
		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";
		if (modelView.hasDefinitionEdges()) return "Error! Definition variables are not supported!";
		
		resetNames();
		
		ParameterReader startingValues = modelView.getModelRequestInterface().getStartingValuesUnit();

		String model="";
		String inset = "   ";
	
		observeds = new ArrayList<Node>();
		latents = new ArrayList<Node>();


		for (Node node : g.getNodes()) if (node.isLatent() && !node.isMeanTriangle()) latents.add(node);
		for (Node node : g.getNodes()) if (!node.isLatent() && !node.isMeanTriangle()) observeds.add(node);
		HashSet<Node> latentsWithOutgoing = new HashSet<>();

		// create all regressions between manifest and latent
		model += inset + comment_symbol + " regressions \n";
		for (Edge edge : g.getEdges()) {
			if (edge.getSource().isLatent() && 
					edge.getTarget().isObserved() && !edge.isDoubleHeaded()
					&& !edge.getSource().isMeanTriangle())
			{
				model+= inset+ createEdgeString(edge, "BY", startingValues, useUniqueNames);
		        latentsWithOutgoing.add(edge.getSource());
			}
		}
		
		for (Node latent : latents) {
		    if (!latentsWithOutgoing.contains(latent)) {
		    	
		    	Edge tempEdge = new Edge(latent, latent, false, 0);
		    	tempEdge.setFixed(true);
		        String pl_st= //makeSaveString(latent.getCaption()) + " =~ 0\n";
		        		createEdgeString(tempEdge, "PseudoLatent", startingValues, useUniqueNames);
		        if (pl_st.length()>0) {
		        	model+=inset+pl_st;
		        }
		    }
		}
        // create all regressions between manifest and manifest
       // model += "! regressions of manifest on manifest\n";
        for (Edge edge : g.getEdges()) {
            if (edge.getSource().isObserved() && 
                    (edge.getTarget().isObserved() || edge.getTarget().isLatent() ) && !edge.isDoubleHeaded()
                    && !edge.getSource().isMeanTriangle())
            {
                model+= inset+ createEdgeString(edge, "ON", startingValues, useUniqueNames);
            }
        }
		
		
		// create all regressions of latent on latent
		//model += "! regressions of latents on latents\n";
		for (Edge edge : g.getEdges()) {
			if (edge.getSource().isLatent() && 
					edge.getTarget().isLatent() && !edge.isDoubleHeaded()
					&& !edge.getSource().isMeanTriangle()
				)
			{
		

				model+= inset+ createEdgeString(edge, "ON", startingValues, useUniqueNames);
			}
		}
		
		// create all variances and covariances
		model += inset + comment_symbol + " residuals, variances and covariances\n";
		for (Edge edge : g.getEdges()) {
		
			if (!edge.isDoubleHeaded()) continue;

			model+= inset+ createEdgeString(edge, "WITH", startingValues, useUniqueNames);
		}
		
		// create zero-fixed latent variances
		for (Node node : g.getNodes()) {
			if (!node.isLatent() || node.isMeanTriangle()) continue;
			boolean hasResidual = false;
			for (Edge edge : g.getEdges()) {
				if (edge.isVarianceEdge() && edge.getSource()==node && edge.getTarget()==node) {
					hasResidual=true; //break;
					//System.out.println("Node"+node);
					//System.out.println(edge);
				}
			}
			
			if (!hasResidual) {
				Edge tempEdge = new Edge(node,node, true, 0);
				tempEdge.setFixed(true);
				model+= inset+ //makeSaveString(node.getCaption())+"~~0*"+makeSaveString(node.getCaption())+"\n";
						createEdgeString(tempEdge, "WITH",startingValues, useUniqueNames);
				
			}
		}
		
		// create zero-fixed latent covariances
	
		boolean[][] spec = new boolean[latents.size()][latents.size()];
		
		for (Edge edge : g.getEdges()) {
			

			
			int sindex = latents.indexOf(edge.getSource());
			int tindex = latents.indexOf(edge.getTarget());
			
			if (sindex != -1 && tindex != -1) {
				spec[sindex][tindex] = true;
				spec[tindex][sindex] = true;
				
			}
			
		}
		
		
		for (int i = 0; i < latents.size(); i++) {
			for (int j = i+1; j < latents.size(); j++) {
				if (!spec[i][j]) {
					Edge virtualEdge = new Edge(latents.get(i),latents.get(j),true);
					virtualEdge.setValue(0);
					virtualEdge.setFixed(true);
					model += inset + createEdgeString(virtualEdge, "WITH", null, useUniqueNames);
				}
			}			
		}
		
		//latents.clear();
		// create all means
		//if (graph.)
		if (g.getMeanTreatment()==Graph.MeanTreatment.explicit) {
    		List<Node> meanNodes = new ArrayList<Node>();
    		
    		model += inset+comment_symbol+" means\n";
    		for (Edge edge : g.getEdges()) {
    			if (edge.getSource().isMeanTriangle()) {
    				meanNodes.add(edge.getTarget());
    				
    				double value;
    				if (useStartingValues && edge.isFree()) {
    					value = startingValues.getParameterValue(edge.getParameterName());
    				} else {
    					value = edge.getValue();
    				}
    				
    				String name = "";
    				if (!edge.isAutomaticNaming() && edge.isFree()) {
    					name = ""+makeSaveString(edge.getParameterName())+"*";
    				}
    				
    				if (edge.isFixed()) {
/*    					model += inset+""+
    							makeSaveString(edge.getTarget().getUniqueName(useUniqueNames), edge.getTarget().getObservedVariableContainer())+name+"~"+
    							value+"*1;\n";*/
    					model += inset + createEdgeString(edge, "MEAN", null, useUniqueNames);
    					parameterSlotNames.add("fixed");
    				} else {
/*    					model += inset+""+makeSaveString(edge.getTarget().getCaption(), edge.getTarget().getObservedVariableContainer())+
    							 "~"+name+"1\n";*/
    					model += inset + createEdgeString(edge, "MEAN", null, useUniqueNames);
    					parameterSlotNames.add(edge.getParameterName());
    				}
    			}
    		}	
    		
    		// fix zero means for remaining nodes
    		for (Node node : g.getNodes())
    		{
    			if (!meanNodes.contains(node) && !node.isMeanTriangle()) {
    				//model += inset+""+makeSaveString(node.getCaption(), node.getObservedVariableContainer())+"~0*1;\n";
    				Edge vedge = new Edge(node, node);
    				vedge.setFixed(true); vedge.setValue(0);
    				model += inset+createEdgeString(vedge, "MEAN", null, useUniqueNames);
    				parameterSlotNames.add("fixed");
    			}
    		}
		
		} else {
			model += inset+comment_symbol+" observed means\n";
			// add explicit free estimation of observed
			for (Node node : g.getNodes())
    		{
				if (!node.isMeanTriangle() && node.isObserved()) {
    				Edge vedge = new Edge(node, node);
    				vedge.setFixed(false); vedge.setValue(0); vedge.setParameterName(null);
    				model += inset+createEdgeString(vedge, "MEAN", null, useUniqueNames);
    				//model += inset+""+makeSaveString(node.getCaption(), node.getObservedVariableContainer())+"~1;\n";
				}
    		}
            // intercept at the end need parameterSlots "fixed"
            for (Node node : g.getNodes()) parameterSlotNames.add("fixed");
		}
		
		return(model);
	}
}
