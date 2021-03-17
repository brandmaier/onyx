/*
  Copyright 2013 Joshua Nathaniel Pritikin

  This file is free software: you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/>.
*/

package importexport;

import importexport.filters.LaTeXFileFilter;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gui.Constants;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

public class LaTeXExport extends Export{

	public LaTeXExport(ModelView modelView) {
		super(modelView, new LaTeXFileFilter(),new String[] {"tex"});
	}
	
	public String getHeader() {return "LaTeX code";}

	public boolean isValid() {return true;}
    
	private final static String shadow = "preaction={fill=black,opacity=.3, transform canvas={xshift=2pt,yshift=-2pt}}";
	private final static String minSize = ("minimum height="+Constants.DEFAULT_NODE_SIZE+"pt"+
			", minimum width="+Constants.DEFAULT_NODE_SIZE+"pt");
	private final static String preamble =
			("\\documentclass{article}\n" +
			 "% Declare some greek characters commonly used in statistics:\n" +
			 "\\usepackage[utf8]{inputenc}\n" +
			 "\\DeclareUnicodeCharacter{03BC}{\\mu}\n" +
			 "\\DeclareUnicodeCharacter{03C3}{\\sigma}\n" +
			 "% add more here\n" +
			 "\\usepackage{color}\n"+
			 "\\usepackage[usenames,dvipsnames]{xcolor}\n"+
			 "\\usepackage{tikz}\n" +
			 "\\usetikzlibrary{shapes.geometric}\n" +
			 "\\usetikzlibrary{calc}\n" +
			 "\\usetikzlibrary{positioning}\n" +
			 "\\pagestyle{empty}\n" +
			 "\\begin{document}\n" +
			 "% To adjust the figure, I recommend http://gummi.midnightcoding.org/\n" +
			 "\\pgfdeclarelayer{nodes}\n"+
			 "\\pgfdeclarelayer{edges}\n"+
			 "\\pgfsetlayers{main,edges,nodes}\n" +
			 "\\begin{tikzpicture}[scale=1,\n" +
			 "    latent/.style={ellipse, "+ minSize +", "+shadow+", draw, fill=white},\n" +
			 "    manifest/.style={rectangle, "+ minSize +", "+shadow+", draw, fill=white},\n" +
			 "    mean/.style={isosceles triangle, shape border rotate=90,\n" +
			 "    isosceles triangle stretches,shape border uses incircle,\n" +
			 "    isosceles triangle apex angle=60, "+ minSize +", "+shadow+", draw, fill=white},\n" +
			 "    defvar/.style={rectangle, sloped, fill=black, pos=.75},\n" +
			 "    thick, >=latex]\n");
	
	private final static String postamble = "\\end{tikzpicture}\n" + "\\end{document}\n";
	
	private final static String labelFormat = " node[fill=white,%s] {$%s$} ";
	
	private final Map<Node,String> nodeNameMap = new HashMap<Node,String>();
	
	public void export(File file) throws Exception
	{
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			export(fw);
		} finally {
			fw.close();
		}
	}
	
	private String getNodeType(final Node node) {
		String nodeType;
		if (node.isMeanTriangle()) {
			nodeType = "mean";
		} else if (node.isLatent()) {
			nodeType = "latent";
		} else {
			nodeType = "manifest";
		}
		return nodeType;
	}
	
	private Color roundColor(Color c) {
		final int SLOP = 16;
		return new Color(Math.round(c.getRed()/(int)SLOP) * SLOP,
				Math.round(c.getGreen()/(int)SLOP) * SLOP,
				Math.round(c.getBlue()/(int)SLOP) * SLOP);
	}
	
	private Map<Color,Integer> makeBgColorMap(FileWriter fw) throws IOException {
		int bgx=0;
		Map<Color,Integer> bgMap = new HashMap<Color,Integer>();
		for (Node node : modelView.getGraph().getNodes()) {
			if (!node.getFillColor().equals(Color.white)) {
				Color c = roundColor(node.getFillColor());
				if (bgMap.get(c) != null) continue;
				fw.append("\\definecolor{bg-"+bgx+"}{rgb}{"+
						(c.getRed()/255.0)+","+(c.getGreen()/255.0)+","+(c.getBlue()/255.0)+"}\n");
				bgMap.put(c, bgx++);
			}
		}
		return bgMap;
	}
	
	private void export(FileWriter fw) throws Exception
	{
		fw.append(preamble);
		
		Graph graph = modelView.getGraph();
		
		fw.append("\\begin{pgfonlayer}{nodes}\n");
		Map<Color,Integer> bgMap = makeBgColorMap(fw);
		for (Node node : graph.getNodes()) {
			StringBuilder nameBuilder = new StringBuilder();
			String caption = node.getCaption();
			for (int cx=0; cx < Math.min(8, caption.length()); cx++) {
				if (Character.isLetterOrDigit(caption.codePointAt(cx))) {
					nameBuilder.appendCodePoint(caption.codePointAt(cx));
				}
			}
			String name = nameBuilder.toString();
			String nodeType = getNodeType(node);
			if (name.length() > 0 && nodeNameMap.containsValue(name)) {
				name += "-" + node.getId();
			}
			if (name.length() == 0 || nodeNameMap.containsValue(name)) { // one last try
				name = nodeType + "-" + node.getId();
			}
			if (nodeNameMap.containsValue(name)) {
				throw new Exception("Can't devise unique name"+name);
			}
			nodeNameMap.put(node, name);
			
			if (node.isMeanTriangle()) caption = "1";
			
			caption = "$"+caption+"$";  // latex math mode
			
			int x = node.getXCenter();
			int y = -node.getYCenter(); // Y axis is mirrored between screen and paper
			
			String nodeStyle = nodeType;
			if (node.getHeight() != Constants.DEFAULT_NODE_SIZE) nodeStyle += ",minimum height="+node.getHeight()+"pt";
			if (node.getWidth() != Constants.DEFAULT_NODE_SIZE)  nodeStyle += ",minimum width="+node.getWidth()+"pt";
			
			if (!node.getFillColor().equals(Color.white)) {
				Integer bgx = bgMap.get(roundColor(node.getFillColor()));
				if (bgx == null) throw new RuntimeException("No mapping for "+node.getFillColor());
				nodeStyle += ",fill=bg-"+bgx;
			}
			fw.append("  \\node["+nodeStyle+"] ("+name+") at ("+x+"pt, "+y+"pt) {"+caption+"};\n");
			if (node.isGrouping()) {
				String groupStyle = "draw, fill=white";
				if (node.isGroupingVariableConnected()) {
					groupStyle = "text=white, fill=black";
				}
				fw.append("  \\node[diamond, "+groupStyle+"] ("+name+
						":g) [above=-12pt of "+name+".south east] {\\textbf{"+node.groupValue+"}};\n");
			}
		}
		fw.append("\\end{pgfonlayer}\n");

		fw.append("\\begin{pgfonlayer}{edges}\n");
		for (Edge edge : graph.getEdges()) {
			String label = edge.getLabelInLaTeXStyle();
			boolean needScope = edge.getStrokeWidth() != Edge.DEFAULT_STROKEWIDTH;
			if (needScope) {
				fw.append("\\begin{pgfscope}\n"+
						  "  \\pgfsetlinewidth{"+ edge.getStrokeWidth()/3.75 +"pt}\n");
			}
			if (!edge.isDoubleHeaded()) {
				String src = nodeNameMap.get(edge.getSource());
				String dest = nodeNameMap.get(edge.getTarget());
				String labelNode = "";
				if (label.length() > 0) {
					labelNode = String.format(labelFormat, "midway", label);
				}
				String defvar = "";
				if (edge.isDefinitionVariable()) {
					defvar = " node[defvar] {}";
				}
				fw.append("  \\draw [->] ("+src+") to "+defvar+" "+labelNode+" ("+dest+");\n");
			} else {
				if (edge.isLoop()) {
					int yawn = 20;
					int angle = 270;
					int offset = -15;
					String labelPos = "below";
					if (edge.getSource().getRenderingHintArcPosition()==Node.NORTH) {
						angle = 90;
						offset = 15;
						labelPos = "above";
					}
					int tangent = angle+90;
					String name = nodeNameMap.get(edge.getSource());
					int angle_out = angle+yawn;
					int angle_in = angle-yawn;
					fw.append("  \\draw [<->] ("+name+"."+angle_out+") .. controls +("+angle_out+":3mm) and +("+tangent+":5mm) .. ");
					fw.append("($("+name+"."+angle+") + (0,"+offset+"pt)$)");
					String labelNode="";
					if (label.length() > 0) {
						labelNode = String.format(labelFormat, labelPos, label);
					}
					fw.append(labelNode+" .. controls +("+tangent+":-5mm) and +("+angle_in+":3mm) .. ("+
							name+"."+angle_in+");\n");
				} else {
					Node src = edge.getSource();
					Node dest = edge.getTarget();
					String sname = nodeNameMap.get(src);
					String dname = nodeNameMap.get(dest);
					String labelNode="";
					if (label.length() > 0) {
						labelNode = String.format(labelFormat, "midway", label);
					}
					fw.append("  \\draw [<->, bend left=20, looseness=1] ("+sname+") to "+
							labelNode+" ("+dname+");\n");
				}
			}
			if (needScope) {
				fw.append("\\end{pgfscope}\n");
			}
		}
		fw.append("\\end{pgfonlayer}\n");
		
		fw.append(postamble);
	}
	
}
