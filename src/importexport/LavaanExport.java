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

import java.util.ArrayList;
import java.util.List;

import engine.ParameterReader;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

public class LavaanExport extends RExport {

	boolean fullcode = true;
	private List<String> parameterSlotNames;
	
	public LavaanExport(ModelView modelView) {
		super(modelView);
		parameterSlotNames = new ArrayList<String>();
	}
	
	public String getHeader() {return "Lavaan code";}
	
    public boolean isValid() {return !modelView.hasDefinitionEdges();}
    
	public String convert(String s)
	{
		/*if (s.length()>8) {
			s = s.substring(0, 8);
		}
		
		s=s.toUpperCase();
		*/
		
		//return s.replaceAll("/[^A-Za-z0-9\\(\\) ]/", "_");
		s = s.replaceAll("[^A-Za-z0-9]", "_");

		while( s.startsWith("_") ) {
			s = s.substring(1);
		}
		
		if (s.length()==0) { s="parameter";}
		
		return(s);
	}
	
	@Override
	public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
		Graph g = modelView.getGraph();
		parameterSlotNames.clear();
	    
		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";
		if (modelView.hasDefinitionEdges()) return "Error! Definition variables in lavaan are not supported!";
		
		resetNames();
		
		ParameterReader startingValues = modelView.getModelRequestInterface().getStartingValuesUnit();

		String model="";
		String inset = "   ";
		
		// determine all node names
		/*String variablenames = "";
		for (Node node : g.getNodes()) {
			if (node.isMeanTriangle()) continue;
			if (node.isLatent()) continue;
			variablenames+=node.getCaption()+" ";
		}*/
		
		//variablenames = variablenames.substring(0, variablenames.length()-1);
		//variablenames+="";
		
		// create all regressions between manifest and latent
		model += "! regressions \n";
		for (Edge edge : g.getEdges()) {
			if (edge.getSource().isLatent() && 
					edge.getTarget().isObserved() && !edge.isDoubleHeaded()
					&& !edge.getSource().isMeanTriangle())
			{
				model+= inset+ createEdgeString(edge, "BY", startingValues, useUniqueNames);
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
		model +="! residuals, variances and covariances\n";
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
				model+= inset+ node.getCaption()+"~~0*"+node.getCaption()+"\n";
			}
		}
		
		// create zero-fixed latent covariances
		List<Node> latents = new ArrayList<Node>();

		for (Node node : g.getNodes()) if (node.isLatent() && !node.isMeanTriangle()) latents.add(node);
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
		
		latents.clear();
		// create all means
		//if (graph.)
		if (g.getMeanTreatment()==Graph.MeanTreatment.explicit) {
    		List<Node> meanNodes = new ArrayList<Node>();
    		
    		model += "! means\n";
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
    					model += inset+""+
    							makeSaveString(edge.getTarget().getUniqueName(useUniqueNames), edge.getTarget().getObservedVariableContainer())+name+"~"+
    							value+"*1;\n";
    					parameterSlotNames.add("fixed");
    				} else {
    					model += inset+""+makeSaveString(edge.getTarget().getCaption(), edge.getTarget().getObservedVariableContainer())+
    							 "~"+name+"1\n";
    					parameterSlotNames.add(edge.getParameterName());
    				}
    			}
    		}	
    		
    		// fix zero means for remaining nodes
    		for (Node node : g.getNodes())
    		{
    			if (!meanNodes.contains(node) && !node.isMeanTriangle()) {
    				model += inset+""+makeSaveString(node.getCaption(), node.getObservedVariableContainer())+"~0*1;\n";
    				parameterSlotNames.add("fixed");
    			}
    		}
		
		} else {
			model += "! observed means\n";
			// add explicit free estimation of observed
			for (Node node : g.getNodes())
    		{
				if (!node.isMeanTriangle() && node.isObserved()) {

    				model += inset+""+makeSaveString(node.getCaption(), node.getObservedVariableContainer())+"~1;\n";
				}
    		}
            // intercept at the end need parameterSlots "fixed"
            for (Node node : g.getNodes()) parameterSlotNames.add("fixed");
		}
		
		if (fullcode) {
			
			String prefix = "#\r\n# This model specification was automatically generated by Onyx\r\n#\r\n"; 
			        
			model = prefix+"library(lavaan);\r\nmodelData <- read.table("+DATAFILENAME+", header = TRUE) ;"
			+"\r\n model<-\"\r\n"+model+"\"\r\nresult<-lavaan(model, data=modelData, fixed.x=FALSE, missing=\"FIML\")\r\nsummary(result, fit.measures=TRUE)";
			
			return model;
			
		} else {
		
			return model;
	
		}
	}
	
	protected String createEdgeString(Edge edge, String keyword, ParameterReader startingValues, boolean useUniqueNames) {
		
		double value;
		if (useStartingValues && edge.isFree() && startingValues!=null) {
			value = startingValues.getParameterValue(edge.getParameterName());
		} else {
			value = edge.getValue();
		}

		String fix="";
		if (edge.isFixed()) {
			fix = value+"*";
			parameterSlotNames.add("fixed");
		} else {
			parameterSlotNames.add(edge.getParameterName());
			
			fix = makeSaveString(edge.getParameterName())+"*";
		}
		
		String sname = makeSaveString(edge.getSource().getUniqueName(useUniqueNames), edge.getSource().getObservedVariableContainer());
		String tname = makeSaveString(edge.getTarget().getUniqueName(useUniqueNames), edge.getTarget().getObservedVariableContainer());
		
		if (edge.getSource().isMeanTriangle()) {
			sname = "1";
		}
		
		if (keyword.equals( "WITH")) {
				return (sname + " ~~ " + fix+tname)+"\n";
		} else if (keyword.equals( "BY")) {
				return (sname + "=~" + fix+tname)+"\n";
		} else if (keyword.equals( "ON")) {
			return (tname + " ~ " + fix+sname)+"\n";			
		}
		
		return "";
/*		
		
		String fix = "";
		if (edge.isFixed()) {
			fix = "@"+value;
		} else {
			fix = "*"+value;
		}
		
		String name = "";
		if (!edge.isAutomaticNaming() && edge.isFree()) {
			name = "("+makeSaveString(edge.getParameterName())+")";
		}
		
		if (keyword.equals("WITH") && edge.getSource()==edge.getTarget())
		{
			return makeSaveString(edge.getSource().getCaption())+name+fix+";\n";
		}
		
		String str = makeSaveString(edge.getSource().getCaption())+" "+keyword+" "+
				makeSaveString(edge.getTarget().getCaption())+name+fix+";\n";
		return str;
		*/
	}

	public List<String> getParameterSlotNames() {return parameterSlotNames;}
}
