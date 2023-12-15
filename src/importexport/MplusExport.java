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

import engine.ParameterReader;
import engine.Statik;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;

import importexport.filters.MPlusFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class MplusExport extends StringExport {
	
	boolean defaultStartingValues = false;
	
	public MplusExport(ModelView modelView) {
		super(modelView, new MPlusFileFilter(),new String[] {"inp"});
		
		useStartingValues = true;
	}
	
	public String getHeader() {return "MPlus code";}	
	
    public boolean isValid() {return !modelView.hasDefinitionEdges();}
    
	@Override
	protected String convert(String s)
	{
		if (s.length()>8) {
			s = s.substring(0, 8);
		}
		
		s=s.toUpperCase();
		
		//return s.replaceAll("/[^A-Za-z0-9\\(\\) ]/", "_");
		return s.replaceAll("[^A-Za-z0-9]", "_");

	}
	
	/*public String makeSaveString(String fullname)
	{
		return super.makeSaveString(fullname);
	}*/
	
	@Override
	public void export(File file) {
		Graph g = modelView.getGraph();

		String content = createModelSpec(modelView, modelView.getModelRequestInterface().getName(), false);
		
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

		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";
		if (modelView.hasDefinitionEdges()) return "Error! Definition variables in lavaan are not supported!";

		
		resetNames();
		
		ParameterReader startingValues = null;
		
		if (modelView != null)
			startingValues = modelView.getModelRequestInterface().getStartingValuesUnit();

//		System.out.println()
		
		String inset = "   ";
		
		String model = "";
		/*
		 * model spec is 
		 * 
		 * A BY B (regression )
		 * A BY B@1.2 (fix)
		 * A WITH B (covariance)
		 * A ON B (regression on latent)
		 * 
		 * A BY B*2.5 (starting value)
		 * 
		 * means:
		 * [A] free
		 * [A@0] fixed
		 * 
		 * comment:
		 * ! hello world
		 */
		
		// determine all node names
		String variablenames = "";
		for (Node node : g.getNodes()) {
			if (node.isMeanTriangle()) continue;
			if (node.isLatent()) continue;
			variablenames+=makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+" ";
		}
		
		//variablenames = variablenames.substring(0, variablenames.length()-1);
		//variablenames+="";
		
		// create all regressions between manifest and latent
		model += "! regressions of latents on manifest\n";
		for (Edge edge : g.getEdges()) {
			if (edge.getSource().isLatent() && 
					edge.getTarget().isObserved() && !edge.isDoubleHeaded()
					&& !edge.getSource().isMeanTriangle())
			{
				model+= inset+ createEdgeString(edge, "BY", startingValues);
			}
		}
		
		// also add all unobserved latents here
		Vector<Node> lnodes = new Vector<Node>();
		for (Node node : g.getNodes()) if (node.isLatent() && !node.isMeanTriangle()) lnodes.add(node);
		for (Edge edge : g.getEdges()) {
			if (lnodes.contains(edge.getSource()) && edge.getTarget().isObserved() && !edge.isDoubleHeaded()) {
				lnodes.remove(edge.getSource());
			}
		}
		for (Node node : lnodes) {
			model += inset + makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+" BY ;\n";
		}
		
		
        // create all regressions between manifest and manifest
        model += "! regressions of manifest on manifest\n";
        for (Edge edge : g.getEdges()) {
            if (edge.getSource().isObserved() && 
                    (edge.getTarget().isObserved() || edge.getTarget().isLatent()) && !edge.isDoubleHeaded()
                    && !edge.getSource().isMeanTriangle())
            {
                model+= inset+ createEdgeString(edge, "ON", startingValues);
            }
        }
		
		
		// create all regressions of latent on latent
		model += "! regressions of latents on latents or manifests\n";
		for (Edge edge : g.getEdges()) {
			if (edge.getSource().isLatent() && 
					(edge.getTarget().isLatent() ) && !edge.isDoubleHeaded()
					&& !edge.getSource().isMeanTriangle()
				)
			{
		

				model+= inset+ createEdgeString(edge, "ON", startingValues);
			}
		}
		
		// create all variances and covariances
		model +="! residuals, variances and covariances\n";
		for (Edge edge : g.getEdges()) {
		
			if (!edge.isDoubleHeaded()) continue;

			model+= inset+ createEdgeString(edge, "WITH", startingValues);
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
						model+= inset+ node.getCaption()+"@0;\n";
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
					model += inset + createEdgeString(virtualEdge, "WITH", null);
				}
			}			
		}
		
		latents.clear();
		// create all means
		if (g.getMeanTreatment() == Graph.MeanTreatment.explicit) {
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
					name = "("+makeSaveString(edge.getParameterName())+")";
				}
				
				if (edge.isFixed()) {
					model += inset+"["+
							makeSaveString(edge.getTarget().getCaption())+"@"+
							value+"] "+name+";\n";
				} else {
					model += inset+"["+makeSaveString(edge.getTarget().getCaption())+
							 "*"+value+"]"+name+";\n";
				}
			}
		}	
		
		// fix zero means for remaining nodes
		for (Node node : g.getNodes())
		{
			if (!meanNodes.contains(node) && !node.isMeanTriangle()) {
				model += inset+"["+makeSaveString(node.getCaption())+"@0];\n";
			}
		}
		
		}
		
		String content =
				"!This model specification was automatically created by Onyx\n"+
				"TITLE:\n"+
		inset+modelView.getModelRequestInterface().getName()+
		"\nDATA:\n"+inset+"FILE IS \"DATAFILENAME\";\nVARIABLE:"+
		inset+"NAMES ARE "+variablenames+";\n"+inset+
		"USEVARIABLES ARE "+variablenames+";\n"+
		"MODEL:\n"+model+
		"ANALYSIS:\n"+inset+"TYPE = general;\n"+inset+"ESTIMATOR = ml;\nOUTPUT:\n"+inset+"sampstat;";
		
		// TYPE = meanstructure ?!
		
		return postProcess(content);
	}

	protected String createEdgeString(Edge edge, String keyword, ParameterReader startingValues) {
		
		String value;
		if (useStartingValues && edge.isFree() && startingValues!=null) {
			if (defaultStartingValues) {
				value = "";
			} else {
				value = String.valueOf(
						Statik.round(
						startingValues.getParameterValue(edge.getParameterName()),
						3)
						);
			}
		} else {
			value = String.valueOf( Statik.round(edge.getValue(),3) );
		}
		
		String fix = "";
		if (edge.isFixed()) {
			fix = "@"+value;
		} else {
			fix = "*"+value;
		}
		
		String name = "";
		if (!edge.isAutomaticNaming() && edge.isFree()) {
			name = " ("+makeSaveString(edge.getParameterName())+")";
		}
		
		if (keyword.equals("WITH") && edge.getSource()==edge.getTarget())
		{
//			return makeSaveString(edge.getSource().getCaption())+name+fix+";\n";
			return makeSaveString(edge.getSource().getCaption())+fix+name+";\n";
		}

		if (keyword.equals("WITH") && edge.getSource()!=edge.getTarget())
		{
			return makeSaveString(edge.getSource().getCaption())+" "+keyword+" "+
					makeSaveString(edge.getTarget().getCaption())+fix+name+";\n";
//					makeSaveString(edge.getTarget().getCaption())+name+fix+";\n";
		}
		
		
		if (keyword.equals("BY")) {
		String str = makeSaveString(edge.getSource().getCaption())+" "+keyword+" "+
//				makeSaveString(edge.getTarget().getCaption())+name+fix+";\n";
				makeSaveString(edge.getTarget().getCaption())+fix+name+";\n";
				return str;
		}

		if (keyword.equals("ON")) {
		String str = makeSaveString(edge.getTarget().getCaption())+" "+keyword+" "+
				makeSaveString(edge.getSource().getCaption())+fix+name+";\n";
		//		makeSaveString(edge.getSource().getCaption())+name+fix+";\n";
		return str;
		}
		
		return null;
	}

	private String postProcess(String str) {
		// constrain to 80 characters per line
		
		String result = "";
		
		for (String line : str.split("\n")) {
			
			if (line.length() <= 80) {
				result += line+"\n";
			} else {
				String[] tokens = line.split(" ");
				
				String curline = "";
				for (String token : tokens) {
					
					if (curline.length()+token.length()<=80 || curline.equals("")) {
						if (!curline.equals("")) curline+=" ";
						curline+=token;
						
					} else {
						result+= curline+"\n";
						curline=token;
					}
					
				}
				result+=curline+"\n";
			}
			
		}
		
		return result;
	}

}
