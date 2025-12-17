/*
* Copyright 2025 by Timo von Oertzen and Andreas M. Brandmaier
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

import java.io.File;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import engine.ParameterReader;
import gui.graph.Edge;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.views.ModelView;


public class JuliaExport extends LavaanExport{
	
	String inset = "   ";
	
	public JuliaExport(ModelView modelView) {
		super(modelView);
		
		setPrefix("graph = @StenoGraph begin\n");
		setPostfix("\nend");
		
		this.comment_symbol = "#";
		
		parameterSlotNames = new ArrayList<String>();
	}
	
	/*public String makeSaveString(String fullname, VariableContainer container)
	{
		return(fullname);
	}*/

	public String getHeader() {return "StructuralEquationModels.jl syntax";}
	
    public boolean isValid() {return !modelView.hasDefinitionEdges();}

	private List<String> parameterSlotNames;
    
	public String getPostfix()
	{
		String latnam = latents.stream().map(node -> ":" + node.getUniqueName(true))
		        .collect(Collectors.joining(","));
		String obsnam = observeds.stream().map(node -> ":" + node.getUniqueName(true))
		        .collect(Collectors.joining(","));		
		String vars = "lat = ["+latnam+"]\r\nobs = ["+obsnam+"]\r\n\r\n";
		
		String partab = "partable = ParameterTable(\r\n"
				+ "graph,\r\n"
				+ "latent_vars = lat,\r\n"
				+ "observed_vars = obs\r\n"
				+ ")"
				+ ""
				+ "";
		
		String model = "model = Sem(\r\n"
				+ "specification = partable,\r\n"
				+ "data = data,\r\n"
				+ "meanstructure = true\r\n"
				+ ")";
		
		return super.getPostfix()+"\n"+vars+partab+"\r\n"+model;
		
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
			fix = "fixed("+value+")*";
			parameterSlotNames.add("fixed");
		} else {
			if (edge.getParameterName() == null) {
				fix = "";
			} else {
				parameterSlotNames.add(edge.getParameterName());
				
				fix = "label(:"+makeSaveString(edge.getParameterName())+")*";
			}
		}
		
		String sname = makeSaveString(edge.getSource().getUniqueName(useUniqueNames), edge.getSource().getObservedVariableContainer());
		String tname = makeSaveString(edge.getTarget().getUniqueName(useUniqueNames), edge.getTarget().getObservedVariableContainer());
		
		if (edge.getSource().isMeanTriangle()) {
			sname = "Symbol(1)";
		}
		
		if (keyword.equals( "WITH")) {
				return (sname + " ↔ " + fix+tname)+"\n";
		} else if (keyword.equals( "BY")) {
				return (sname + " → " + fix+tname)+"\n";
		} else if (keyword.equals( "ON")) {
			return (tname + " →  " + fix+sname)+"\n";			
		} else if (keyword.equals("PseudoLatent")) {
			return ("");
		} else if (keyword.equals("MEAN")) {
			return ("Symbol(1) → "+fix+tname+"\n");
		}
		
		return "";

	}

}
