package importexport;

import importexport.filters.RFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

public class OpenMxMatrixExport extends RExport {

	public static final int LABEL = 3, TO = 2;


	public String getHeader() {return "OpenMx code";}
	
    public boolean isValid() {return !modelView.hasDefinitionEdges();}



	public OpenMxMatrixExport(ModelView modelView) {
		super(modelView, new RFileFilter(),new String[] {"R","r"});
		
		useStartingValues = true;
	}

	
	public String convert(String s)
	{
		//return s.replaceAll("/[^A-Za-z0-9\\(\\) ]/", "_");
		return s.replaceAll("[^A-Za-z0-9]", "_");

	}
	
	@Override
	public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames)
	{
	    Graph g = modelView.getGraph();
		if (g.isMultiGroup()) return "Error! Multigroup models cannot be exported yet!";

		
		resetNames();

		String modelVariable = "model";
		
		
		
		int numLatents = 0;
		int numManifests = 0;
		
		for (Node node : g.getNodes()) {
			
			if (node.isMeanTriangle()) continue;
			
			if (node.isObserved()) {
				numManifests++;
			} else {
				numLatents++;
			}
		}
		
		if (numLatents==0 && numManifests==0) {
			return ("#\n#Empty model\n#");
		}
		
		// create A matrix
		double[][] valsA = new double[numManifests+numLatents][numManifests+numLatents];
		boolean[][] freeA = new boolean[numManifests+numLatents][numManifests+numLatents];
		String[][] labelsA = new String[numManifests+numLatents][numManifests+numLatents];
		
		// create S matrix
		double[][] valsS = new double[numManifests+numLatents][numManifests+numLatents];
		boolean[][] freeS = new boolean[numManifests+numLatents][numManifests+numLatents];
		String[][] labelsS = new String[numManifests+numLatents][numManifests+numLatents];
		
		// create F matrix
		double[][] valsF = new double[numManifests][numManifests+numLatents];
		boolean[][] freeF = new boolean[numManifests][numManifests+numLatents];
		
		//for (int i=0; i < numManifests; i++)
		//	valsF[i][i] = 1;
		int cnt_total = 0; int cnt_manif = 0;
		for (Node node : g.getNodes()) {
			
			if (node.isMeanTriangle()) continue;

			
			if (node.isObserved()) {
				valsF[cnt_manif][cnt_total] = 1;
				cnt_manif+=1;
			}
			cnt_total+=1;
		}
		String[][] labelsF = new String[numManifests+numLatents][numManifests+numLatents];
		
		
		// create M matrix
		double[][] valsM = new double[1][numManifests+numLatents];
		boolean[][] freeM = new boolean[1][numManifests+numLatents];
		String[][] labelsM = new String[1][numManifests+numLatents];
				
		
		// populate matrices
		
		HashMap<Node, Integer> map = new HashMap<Node, Integer>();
		int counter = 0;
		
		for (Node node : g.getNodes()) {
			if (node.isMeanTriangle()) continue;

			map.put(node, counter);
			counter++;
		}
		
		for (Edge edge : g.getEdges()) {
			
			
			int from = -1;
			if (!edge.getSource().isMeanTriangle()) from = map.get(edge.getSource());
			int to = map.get(edge.getTarget());
			
			if (edge.isDoubleHeaded()) {
				// S matrix
				if (edge.isFree()) { 
                    freeS[from][to] = freeS[to][from] = true;
                    labelsS[from][to] = labelsS[to][from] = "\""+makeSaveString(edge.getParameterName())+"\""; 
				}
				if (edge.isDefinitionVariable())
				    labelsS[from][to] = labelsS[to][from] = "\"data."+makeSaveString(edge.getDefinitionVariableContainer().getUniqueName(),edge.getDefinitionVariableContainer())+"\"";
				valsS[from][to] = valsS[to][from] = edge.getValue();
				
			} else {
				if (edge.getSource().isMeanTriangle()) {
				// M matrix
					if (edge.isFree()) {
						freeM[0][to] = true;
						labelsM[0][to] = "\""+makeSaveString(edge.getParameterName())+"\"";
					}
	                if (edge.isDefinitionVariable())
	                    labelsS[from][to] = "\"data."+makeSaveString(edge.getDefinitionVariableContainer().getUniqueName(),edge.getDefinitionVariableContainer())+"\"";
					valsM[0][to] = edge.getValue();
					
				} else {
				// A matrix
					if (edge.isFree()) {
						freeA[to][from] = true;
						labelsA[to][from] = "\""+makeSaveString(edge.getParameterName())+"\"";
					}
	                if (edge.isDefinitionVariable())
	                    labelsS[to][from] = "\"data."+makeSaveString(edge.getDefinitionVariableContainer().getUniqueName(),edge.getDefinitionVariableContainer())+"\"";
					valsA[to][from] = edge.getValue();
				}
			}
			
		}

        String mstr = "";
        String lstr = "";
        String allstr = "";
        
        Iterator<Node> iterNode = g.getNodeIterator();
        while(iterNode.hasNext()) {
            Node node = iterNode.next();
            if (!node.isMeanTriangle()) {
                if (allstr != "") { allstr=allstr+",";}
                allstr=allstr+"\""+makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+"\"";
                if (node.isLatent()) {
                    if (lstr != "") { lstr=lstr+",";}
                    lstr=lstr+"\""+makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+"\"";
                } else {
                    if (mstr != "") { mstr=mstr+",";}
                    mstr=mstr+"\""+makeSaveString(node.getUniqueName(useUniqueNames), node.getObservedVariableContainer())+"\"";
                }
            }
        }
        
        String manifests = "manifests<-c("+mstr+")";
        String latents = "latents<-c("+lstr+")";
        String allVar = "allVariables <- c("+allstr+")";
        boolean isRaw = (modelView.getCombinedDataset()==null || !modelView.getCombinedDataset().hasCovarianceDataset());
		
		// create output
		String output = "#\r\n# This model specification was automatically generated by Onyx\r\n" +
				"# \r\n"+
				"require(\"OpenMx\");\r\n"+
                "modelData <- read.table("+DATAFILENAME+", header = TRUE) \r\n"+
                manifests+"\r\n"+latents+"\r\n"+allVar+"\r\n"+
				modelVariable+" <- mxModel(\""+makeSaveString(modelName)+"\", \r\n"+
			"type=\"RAM\",\r\n"+
			matString(valsA, freeA, labelsA,  "A", null)+",\r\n"+
			matString(valsS, freeS, labelsS,  "S", null)+",\r\n"+
			matString(valsF, freeF, labelsF,  "F", "list(manifests,allVariables)")+",\r\n"+
			matString(valsM, freeM, labelsM,  "M", "list(NULL,allVariables)")+",\r\n"+
			"mxData(modelData, type = "+(isRaw?"\"raw\"":"\"cov\"")+"),\r\n"+
			"mxRAMObjective(\"A\",\"S\",\"F\",\"M\")\r\n"+
			")\r\n"; // close model definition

        output += "\r\nresult <- mxRun("+modelVariable+")\r\n";
        output += "summary(result)\r\n";
		
		return(output);
		
	}
	
	private String matString(double[][] matVals, boolean[][] matFree,String[][] matLabels, String name, String dimNames) {
		
		if (matVals.length == 0) return "mxMatrix(name=\"+name+\")";
		
		String v = "values=c(\n";
		for (int i=0; i < matVals.length; i++)
		{
			for (int j=0; j < matVals[0].length; j++) {
				v+= matVals[i][j];
				if (i != matVals.length-1 || j!= matVals[0].length-1)
					v+=",";
			}
			v+="\n";
		}
		v+=")";
		
		String f = "free=c(\n";
		for (int i=0; i < matVals.length; i++)
		{
			for (int j=0; j < matVals[0].length; j++) {
				if (matFree[i][j])
					f+="T";
				else
					f += "F";
				if (i != matVals.length-1 || j!= matVals[0].length-1)
					f+=",";
			}
			f+="\n";
		}
		f+=")";
		
		String l = "labels=c(\n";
		for (int i=0; i < matVals.length; i++)
		{
			for (int j=0; j < matVals[0].length; j++) {

				if (matLabels[i][j]==null || matLabels[i][j].equals("")) {
					l+="NA";
				} else
					l+= matLabels[i][j];
				
				if (i != matVals.length-1 || j!= matVals[0].length-1)
					l+=",";
			}
			l+="\n";
		}
		l+=")";
		
		
		String ms = "mxMatrix(\"Full\", nrow=" +
				matVals.length+",ncol="+
				matVals[0].length +","+
				(dimNames!=null?"dimnames="+dimNames+", ":"")+ 
				v+", "+f+","+l+
				", byrow=TRUE, name=\""+name+"\")";
		
		return ms;
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
		    		  sb.append( "\"data."+makeSaveString(item.getParameterName(), item.getDefinitionVariableContainer())+"\"");
		    	  } else {
		    		  sb.append( "\""+makeSaveString(item.getParameterName())+"\"");
		    	  }
		    	}
		      
		   }
		   
		   sb.append(")");
		   return sb.toString();
		   
		   
	}

	/*private Object round(double value) {
		return Math.round(value*100.0)/100;
	}*/



	/**@deprecated
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
				lstr=lstr+"\""+node.getCaption()+"\"";
			} else {
				if (mstr != "") { mstr=mstr+",";}
				mstr=mstr+"\""+node.getCaption()+"\"";
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
					from = edge.getSource().getCaption();
				}
				
				to = edge.getTarget().getCaption();
				
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
