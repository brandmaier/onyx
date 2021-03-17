package importexport;

import engine.ModelRequestInterface;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

/**
 * 
 * @author brandmaier
 *
 */
public class MplusParser {

	public final int TITLE = 0, MODEL = 1, DATA = 2, ANALYSIS=3, OUTPUT=4, CONSTRAINT=5, UNDEF = -1;
	
	public HashMap<String, Node> nodeMap;
	
	ModelView mv;
	
	Vector<String> warnings;
	
	class Tag {
		boolean fixed = false;
		double value = 1.0;
		double startingValue = 1.0;
		String name = null;
		String parameterName = null;
	
		public String toString()
		{
			return ("TAG: "+name+" ("+parameterName+"="+value+"*"+startingValue+  ") fixed"+fixed);
		}
	}
	
	public List<Tag> getTags(String line) {
		
		List<Tag> result = new ArrayList<Tag>();
		
		line = line.replace("@", " @");
		line = line.replace("*", " *");
		line = line.replace("(", " (");
		
		String[] tokens = line.split(" ");
		
		Tag currentTag = null;
		for (int i=0; i < tokens.length; i++) {
			
			tokens[i] = tokens[i].trim();
			
			if (tokens[i].startsWith("@")) {
				currentTag.fixed = true;
				currentTag.value = Double.parseDouble(tokens[i].substring(1));
			} else if (tokens[i].startsWith("*")) {
				currentTag.startingValue = Double.parseDouble(tokens[i].substring(1));
			} else if (tokens[i].startsWith("(")) {
				currentTag.parameterName = tokens[i].substring(1, tokens[i].length()-1);
			} else {
				/*String warning = "Ignoring token "+tokens[i];
				System.err.println(warning);
				warnings.add(warning);*/
				if (currentTag != null) result.add(currentTag);
				currentTag = new Tag();
				currentTag.name = tokens[i];
			}
		}
		
		result.add(currentTag);
		
		return result;
	}
	
	public MplusParser(ModelView mv)
	{
		this.mv = mv;
		nodeMap = new HashMap<String, Node>();
	}

	public void parseNew(String text)
	{
		
		ModelRequestInterface mri = mv.getModelRequestInterface();
		
		text = text.toUpperCase();
		
		Node meanNode = new Node();
		meanNode.setTriangle(true);
	
		boolean meanUsed = false;
		
		int type = UNDEF;
		for (String line : text.split(";|\\n"))
		{
			line = line.trim();
			
			if (line.length()==0) continue;
			if (line.startsWith("!")) continue;
			
			System.out.println("Reading: "+line+"\n");
			// determine Section switch
			if (line.contains(":")) {
				
				if (line.startsWith("MODEL CONSTRAINT")) {
					type = CONSTRAINT;
 				} else if
				(line.startsWith("MODEL")) {
					type= MODEL;
				} else {
					type = UNDEF;
				}
					
				continue;
			}
			
			// parse line (conditioned on currently active section)
			if (type==MODEL) {
				
				
				
				String connecttype = "";
				String[] tokens;
				if (line.contains(" BY ")) {
					tokens = line.split(" BY ");
					connecttype ="BY";
				} else if (line.contains(" ON ")) {
					tokens = line.split(" ON ");
					connecttype="ON";
				} else if (line.contains(" WITH ")) {
					tokens = line.split(" WITH ");
					connecttype="WITH";
				} else if (line.contains(" PWITH ")) {
					tokens = line.split(" PWITH ");
					connecttype="PWITH";
				} else {
					
					if (line.startsWith("[")) {
						
						line = line.substring(1, line.length()-1);
						
						List<Tag> tags = getTags(line);
						for (Tag tag : tags) {
						
							System.out.println("Mean "+tag);
							
							Node node = getNode(tag.name);
							Edge edge = new Edge(meanNode, node);
							edge.setFixed(tag.fixed);
							if (tag.parameterName != null) {
								edge.setParameterName(tag.parameterName);
								edge.setAutomaticNaming(false);
							}
							edge.setValue(tag.value);
							if (tag.value != 0)
								
								
								
								{ 
								if (!meanUsed) 
									mri.requestAddNode(meanNode);
								
								mri.requestAddEdge(edge);
									meanUsed = true;
								}
						}
						
						
					} else {
					
					List<Tag> tags = getTags(line);
					for (Tag tag : tags) {
						System.out.println(tag);
						Node node = getNode(tag.name);
						
						
						System.out.println("Edge "+tag.name+" ->" + tag.name+" Node:"+node);
						Edge edge = new Edge(node, node);
						edge.setDoubleHeaded(true);
						edge.setFixed(tag.fixed);
						edge.setValue(tag.value);
						
						if (tag.parameterName != null) {
							edge.setParameterName(tag.parameterName);
							edge.setAutomaticNaming(false);
						}
						mri.requestAddEdge(edge);
					}
					
					}
					
					continue;
				}
				
				
				// BY / WITH / ON
				//List<Tag> fromTags =

				
				
			/*	String[] leftTokens = tokens[0].split(" ");
				String[] rightTokens = tokens[1].split(" ");
				*/
				
				
				List<Tag> leftTags = getTags(tokens[0]);
				List<Tag> rightTags = getTags(tokens[1]);
				
				
				for (Tag fromTag : leftTags) {
					for (Tag toTag : rightTags) {
						
						Node fromNode = getNode(fromTag.name);
						Node toNode = getNode(toTag.name);
						
						if (connecttype.equals("PWITH")) {
							if (leftTags.indexOf(fromTag) != rightTags.indexOf(toTag)) continue;
						}
						
						if (connecttype.equals("BY")) {
							if (toNode.isLatent())
								mri.requestSwapLatentToManifest(toNode);
						}
						
						System.out.println("Connecting "+fromTag+" -> "+toTag);
						
						Edge edge = new Edge(fromNode, toNode);
						
						edge.setFixed(fromTag.fixed || toTag.fixed);
						
						if (edge.isFixed()) {
						if (toTag.value != fromTag.value) {
							if (toTag.value==1) {
								edge.setValue(fromTag.value);
							} else {
								edge.setValue(toTag.value);
							}
						} else {
							edge.setValue(toTag.value);
						}
						} else {
							if (toTag.startingValue != fromTag.startingValue) {
								if (toTag.startingValue==1) {
									edge.setValue(fromTag.startingValue);
								} else {
									edge.setValue(toTag.startingValue);
								}
							} else {
								edge.setValue(toTag.startingValue);
							}
						}
					    
						
					    
					    if (fromTag.parameterName != null) {
							edge.setParameterName(fromTag.parameterName);
							edge.setAutomaticNaming(false);
						}
					    
						if (toTag.parameterName != null) {
							edge.setParameterName(toTag.parameterName);
							edge.setAutomaticNaming(false);
						}
						
						mri.requestAddEdge(edge);
						
					}
				}
				
				
			}
			
		
		}
		

		
		mv.getGraph().autoLayout();
	}
	
	
	
	@Deprecated
	public void parse(String text)
	{
		warnings = new Vector<String>();
		boolean constraintsIgnored = false;
		
		text = text.toUpperCase();
		
		String title = "";
		
		int type = UNDEF;
		for (String line : text.split(";|\\n"))
		{
			line = line.trim();
			System.out.println("Reading: "+line+"\n");
			// determine Section switch
			if (line.contains(":")) {
				
				if (line.startsWith("MODEL CONSTRAINT")) {
					type = CONSTRAINT;
 				} else if
				(line.startsWith("MODEL")) {
					type= MODEL;
				} else {
					type = UNDEF;
				}
					
				continue;
			}
			
			// parse line (conditioned on currently active section)
			if (type==MODEL) {
				
				String[] tokens = null;
				String connecttype = "";
				
				if (line.contains(" BY ")) {
					tokens = line.split(" BY ");
					connecttype ="BY";
				} else if (line.contains(" ON ")) {
					tokens = line.split(" ON ");
					connecttype="ON";
				} else if (line.contains(" WITH ")) {
					tokens = line.split(" WITH ");
					connecttype="WITH";
				} else {
					connecttype="SINGLE";
					
					tokens = line.split(" ");
					for (int i=0; i < tokens.length; i++) {
						
						boolean skip = false;
						
						if (tokens[i].contains("[")) {
							String name = tokens[i].substring(1, tokens[i].length()-1);
							
							Node nd = new Node();
							nd.setTriangle(true);
							mv.getModelRequestInterface().requestAddNode(nd);
							
							Node newnode = getNode(name);
							
							Edge edge = new Edge(nd, newnode);
							mv.getModelRequestInterface().requestAddEdge(edge);
							
							
							continue;
						}
						
						// variance or mean
						String nodes = tokens[i];
						double value = 1.0;
						boolean fixed = false;
						if (tokens[i].contains("@")) {
							String[] attok = tokens[i].split("@");
							nodes = attok[0];
							value = Double.parseDouble(attok[1]);
							fixed = true;
						}
						
						
						Node node = getNode(nodes);
						Edge edge = new Edge(node,node);
						edge.setValue(value);
						edge.setDoubleHeaded(true);
						
						if (i+1 < tokens.length)
						if (tokens[i+1].startsWith("(")) {
							String name = tokens[i+1].substring(1, tokens[i+1].length()-1);
							edge.setParameterName(name);
							edge.setAutomaticNaming(false);
							skip = true;
						}
						
						//if (fixed)
							edge.setFixed(fixed);
						//}
						
						mv.getModelRequestInterface().requestAddEdge(edge);
						
						if (skip) i++;
						
					}
					
					continue;
				}
				
				String[] leftTokens = tokens[0].split(" ");
				String[] rightTokens = tokens[1].split(" ");
				
				for (int left = 0; left < leftTokens.length; left++)
				{
					for (int right = 0; right < rightTokens.length; right++)
					{
						String from = leftTokens[left];
						String to = rightTokens[right];
						
						boolean fixed;
						
						Double value = 1.0;
						if (to.contains("@")) {
							String[] attok = to.split("@");
							to = attok[0];
							value = Double.parseDouble(attok[1]);
							fixed = true;
						} else {
							fixed = false;
						}
						
						Node fromNode = getNode(from);
						Node toNode = getNode(to);
						
//						addNode(from)
						
						Edge edge = new Edge(fromNode, toNode);
						edge.setValue(value);
						edge.setFixed(fixed);
						if (connecttype.equals("WITH")) edge.setDoubleHeaded(true);
						
						
						mv.getModelRequestInterface().requestAddEdge(edge);
					}
				}
				
				
				
				
			} else if (type==TITLE) {
				
				if (!title.trim().startsWith("NOTE"))
					title+=line.trim();
			} else if (type==CONSTRAINT) {
				constraintsIgnored = true;
			}
			
			
			
			
		}
		
		if (constraintsIgnored) {
			warnings.add("Constraints were ignored!");
		}
		
		System.out.println("Title: "+title);
		
		// Layout
		
		mv.getGraph().autoLayout();
	}
	
	private Node getNode(String to) {
		if (nodeMap.containsKey(to)) return nodeMap.get(to);
		Node node = new Node();
		node.setCaption(to);
		
		nodeMap.put(to, node);
		mv.getModelRequestInterface().requestAddNode(node);
		return(node);
	}

	public static void main(String[] args) throws IOException {
		
		
		
		ModelView mv = new ModelView(null);
	//	File f = new File("test/mplus.txt");
		 StringBuffer fileData = new StringBuffer(1000); 
		 BufferedReader reader = new BufferedReader( new FileReader("src/test/mplus2.txt")); 
		 char[] buf = new char[1024]; int numRead=0; while((numRead=reader.read(buf)) != -1){ fileData.append(buf, 0, numRead); } reader.close(); 
		 String inp = fileData.toString();
		 MplusParser parser = new MplusParser(mv);
		 parser.parseNew(inp);
		 
		 
		 JFrame frame = new JFrame();
		 frame.add(mv);
		 frame.pack();
		 frame.setSize(500,500);
		 frame.setVisible(true);
	}
}
