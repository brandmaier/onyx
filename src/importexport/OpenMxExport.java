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

public class OpenMxExport extends RExport {

	public static final int LABEL = 3, TO = 2;


	public String getHeader() {return "OpenMx code";}
	
	public OpenMxExport(ModelView modelView) {
		super(modelView);
	}
    public boolean isValid() {return !modelView.hasDefinitionEdges();}

	@Override
	public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames)
	{
	    Graph g = modelView.getGraph();
		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";
		
		resetNames();
		
		String modelVariable = "model";
	//	String modelName = "ONYX model";
		

		
		String mstr = "";
		String lstr = "";
		
//		for (int i=0; i < g.ge)
		Iterator<Node> iterNode = g.getNodeIterator();
		while(iterNode.hasNext()) {
			Node node = iterNode.next();
			if (!node.isMeanTriangle())
			if (node.isLatent()) {
				if (lstr != "") { lstr=lstr+",";}
				lstr=lstr+"\""+makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+"\"";
			} else {
				if (mstr != "") { mstr=mstr+",";}
				mstr=mstr+"\""+makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+"\"";
			}
		}
		
		String manifests = "manifests<-c("+mstr+")";
		String latents = "latents<-c("+lstr+")";
		
		String output = "#\r\n# This model specification was automatically generated by Onyx\n" +
				"# \r\n"+
				"require(\"OpenMx\");\r\n"+
				"modelData <- read.table("+DATAFILENAME+", header = TRUE) \r\n"+
				manifests+"\r\n"+latents+"\r\n"+
				modelVariable+" <- mxModel(\""+makeSaveString(modelName)+"\", \r\n"+
			"type=\"RAM\",\r\n"+
			"manifestVars = manifests,\r\n"+
			"latentVars = latents";
		
			boolean withMeans = true;
		
			List<Node> meanNodes = new ArrayList<Node>();
			
			
			// create edges

		
			// create all regression edges
			for (Node source : g.getNodes())
			{
				// collect all outgoing edges (regressions)
				List<Edge> e = new ArrayList<Edge>();
				
				for (Edge edge : g.getEdges()) {
					if (edge.isDoubleHeaded()) continue;
					if (edge.getSource()==source) {
						
						e.add(edge);
					
						if (source.isMeanTriangle() && edge.getTarget().isObserved())
							meanNodes.add(edge.getTarget());
					}
				}
				
				// create Omx path
				String from = "";
				String to = "";
				String free = "";
				String value = "";
				String arrows = "";
				String label = "";
				if (source.isMeanTriangle()) {
					from = "one";
					withMeans= true;
					
				} else {
					from = source.getUniqueName(useUniqueNames);
				}
				
				if (e.isEmpty()) continue;
				
				free = combine(e, 0, useUniqueNames);
				value = combine(e, 1, useUniqueNames);
				arrows = "1";
				to = combine(e, TO, useUniqueNames);
				label = combine(e, LABEL, useUniqueNames);
				

				
				String edgeString = ",\nmxPath(from=\""+ makeSaveString(from, source.getObservedVariableContainer())+"\",to="+to+
						", free="+free+", value="+value+" "+
						", arrows="+arrows+ ", label="+label+" )";
				output+=edgeString;
				
			}
			
			// create all covariance / variance edges
			for (Node source : g.getNodes())
			{
				// collect all outgoing edges (regressions)
				List<Edge> e = new ArrayList<Edge>();
				
				for (Edge edge : g.getEdges()) {
					if (!edge.isDoubleHeaded()) continue;
					if (edge.getSource()==source) e.add(edge);
				}
				
				// create Omx path
				String from = "";
				String to = "";
				String free = "";
				String value = "";
				String arrows = "";
				String label = "";
				if (source.isMeanTriangle()) {
					from = "one";
					withMeans= true;
				} else {
					from = source.getUniqueName(useUniqueNames);
				}
				
				if (e.isEmpty()) continue;
				
				free = combine(e, 0, useUniqueNames);
				value = combine(e, 1, useUniqueNames);
				arrows = "2";
				to = combine(e, 2, useUniqueNames);
				label = combine(e, 3, useUniqueNames);
				
				String edgeString = ",\nmxPath(from=\""+makeSaveString(from)+"\",to="+to+
						", free="+free+", value="+value+" "+
						", arrows="+arrows+ ", label="+label+" )";
				output+=edgeString;
				
			}	
			
/*
			// TvO, 15 MAY 2014: To use implicit means, the data set needs to be centered. This cannot be done in the export code.
			 
			if (g.getMeanTreatment() == Graph.MeanTreatment.implicit) {
				String to = "c(", labels = "c(";
				for (Node node : g.getNodes()) {
					if (node.isObserved()) {
						to+="\""+makeSaveString(node.getCaption())+"\",";
						labels += "\"mean_"+makeSaveString(node.getCaption())+"\",";
					}
				}
                to = to.substring(0, to.length()-1)+")";
                labels = labels.substring(0, labels.length()-1)+")";
				String edgeString = ",\nmxPath(from=\"one\",to="+to+
						", free=T, value=0 "+
						", arrows=1, label="+labels+")";
				output += edgeString;
			}
*/			
			// zero means
			List<Node> zeroMeanNodes = new ArrayList<Node>();
//			if (g.getMeanTreatment() != Graph.MeanTreatment.implicit) {
			for (Node n : g.getNodes()) {
				if (!meanNodes.contains(n) && !n.isMeanTriangle() && !n.isLatent()) {
					zeroMeanNodes.add(n);
				}
			}
//			}
			
			if (zeroMeanNodes.size()>0) {
				
				String to = "\""+makeSaveString(zeroMeanNodes.get(0).getUniqueName(useUniqueNames),
						zeroMeanNodes.get(0).getObservedVariableContainer())+"\"";
				for (int i = 1; i < zeroMeanNodes.size();i++)
					to+=",\""+makeSaveString(zeroMeanNodes.get(i).getUniqueName(useUniqueNames),
							zeroMeanNodes.get(i).getObservedVariableContainer())+"\"";
				String edgeString = ",\nmxPath(from=\"one\",to=c("+to+
						"), free=F, value=0"+
						", arrows=1)";
				
				output+=edgeString;

			}
	/*		
			if (withData) {
				
				if (withMeans) {
					output+=",\r\nmxData(data,type=\"raw\")\r\n"; 
				} else {
					output+=",\r\nmxData(cov(data),type=\"cov\",numObs="+data.length+" )\n";
				}
			}
		*/	
			boolean isRaw = (modelView.getCombinedDataset()==null || !modelView.getCombinedDataset().hasCovarianceDataset());
			output += ",\r\nmxData(modelData, type = "+(isRaw?"\"raw\"":"\"cov\"")+")\r\n";
			output+=");\r\n";	//close model definition
			
			output += "\r\nresult <- mxRun("+modelVariable+")\r\n";
			output += "summary(result)\r\n";
			
			return(output);
		
	}
	
	private String combine(List<Edge> list, int type, boolean useUniqueNames) {
		
		ParameterReader startingValues = modelView.getModelRequestInterface().getStartingValuesUnit();

		 StringBuilder sb = new StringBuilder();
		 sb.append("c(");
		   boolean first = true;
		   for (Edge item : list)
		   {
		      if (first)
		         first = false;
		      else
		         sb.append(",");
		      if (type==0) {
		    	  if (item.isFree()) {
		    	  sb.append("TRUE");
		    	  } else {
		    		  sb.append("FALSE");
		    	  }
		      } else if (type==1) {
		    	  if (useStartingValues && item.isFree()) {
		    		  sb.append( round(startingValues.getParameterValue(item.getParameterName())));
		    	  } else {
		    		  sb.append( round(item.getValue()));
		    	  }
		      } else if (type == TO)
		      {
		    
		    		  sb.append("\""+makeSaveString(item.getTarget().getUniqueName(useUniqueNames), item.getTarget().getObservedVariableContainer())+"\"");
		    	  //}
		      } else if (type == LABEL) {
		    	  if (item.isDefinitionVariable()) {
		    		  sb.append( "\"data."+makeSaveString(item.getDefinitionVariableContainer().getUniqueName(),
		    				  item.getDefinitionVariableContainer())+"\"");
		    	  } else {
		    		  sb.append( "\""+makeSaveString(item.getParameterName())+"\"");
		    	  }
		    	}
		      
		   }
		   
		   sb.append(")");
		   return sb.toString();
		   
		   
	}

	/**
	 * @Deprecated
	*/
	public String exportNaive(Graph g)
	{
		String modelVariable = "model";
		String modelName = "ONYX model";
		
		String mstr = "";
		String lstr = "";
		
//		for (int i=0; i < g.ge)
		Iterator<Node> iterNode = g.getNodeIterator();
		while(iterNode.hasNext()) {
			Node node = iterNode.next();
			if (!node.isMeanTriangle())
			if (node.isLatent()) {
				if (lstr != "") { lstr=lstr+",";}
				lstr=lstr+"\""+node.getUniqueName(false)+"\"";
			} else {
				if (mstr != "") { mstr=mstr+",";}
				mstr=mstr+"\""+node.getUniqueName(false)+"\"";
			}
		}
		
		String manifests = "manifests<-c("+mstr+")";
		String latents = "latents<-c("+lstr+")";
		
		String output = "#\n# This RAM specification was automatically generated by Onyx" +
				"# \n"+
				"require(\"OpenMx\");\n"+
				manifests+"\n"+latents+"\n"+
				modelVariable+" <- mxModel(\""+modelName+"\", \n"+
			"type=\"RAM\",\n"+
			"manifestVars = manifests,\n"+
			"latentVars = latents,\n";
		
			boolean withMeans = false;
		
			Iterator<Edge> iter = g.getEdgeIterator();
			while(iter.hasNext())  
			{
				Edge edge = iter.next();
				String from = "";
				String to = "";
				String free = "";
				String value = "";
				String arrows = "";
				
				if (edge.getSource().isMeanTriangle()) {
					from = "one";
					withMeans= true;
				} else {
					from = edge.getSource().getUniqueName(false);
				}
				
				to = edge.getTarget().getUniqueName(false);
				
				value = Double.toString(edge.getValue());
				
				if (edge.isDoubleHeaded()) {
					arrows = "2";
				} else {
					arrows = "1";
				}
				
				if (edge.isFixed()) {
					free = "F";
				} else {
					free = "T";
				}
				
				String label = edge.getParameterName();
				
				String edgeString = "mxPath(from=\""+from+"\",to=\""+to+
						"\", free="+free+", value="+value+" "+
						", arrows="+arrows+ ", label=\""+label+"\" )";
				
				if (iter.hasNext()) edgeString+=",\n";
				
				output+=edgeString;
			}	
	/*		
			if (withData) {
				
				if (withMeans) {
					output+=",mxData(data,type=\"raw\")\n"; 
				} else {
					output+=",mxData(cov(data),type=\"cov\",numObs="+data.length+" )\n";
				}
			}
	*/		
			output+=");";	//close model definition
			
			return(output);
		
	}
	
}
