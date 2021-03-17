package gui.graph.presets.yaml;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * 
 * 
 * The YAML format looks like this
 * 
 * 
 * node:
 * 	font-color:
 * 
 * 
 * @author brandmaier
 *
 */

public class YAMLParser {

	
	public YAMLParser() {}
	
	public YAMLPreset parse(String s) {
		YAMLPreset yp = new YAMLPreset();
		
		String[] lines = s.split("\\n");
		
		Vector<String> context_list = new Vector<String>();
		
		for (int i=0; i < lines.length; i++) {
			
			// get line
			String line = lines[i];
			
			int level = 0;
			while (line.startsWith("\t")) { level=level+1; line = line.substring(1);}
			//while (context_list.size()> level) {context_list.removeElementAt(context_list.size());}
			
			// strip whitespaces
			line = line.strip();
			
			
			// multi-line?
			boolean multiline = line.endsWith(":");
			
			if (multiline) {
				// multi line
				String ctxt = line.replace(":", "");
				context_list.add(ctxt);
			} else {
				// single line
				boolean has_key = line.contains(":");
				
				String key, val;
				if (has_key) {
					String[] tokens = line.split(":");
					key = tokens[0].strip();
					val = tokens[1].strip();
					
					
				} else {
					// no key
					key = "";
					for (int i1=0; i1 < context_list.size(); i1++) {if (i1>0) key=key+"-"; key=key+context_list.get(i1);}
					val = line.strip();
				}
				
				System.out.println("Set "+key+ " to "+ val+"  at level"+level+"\n");
				// set key-value 
				if (key=="node-fill-color") yp.nodeFillColor = Color.decode(val);
				if (key=="node-font-color") yp.nodeFontColor = Color.decode(val);
				if (key=="edge-line-color") yp.edgeLineColor = Color.decode(val);
				if (key=="edge-arrow-type") yp.edgeArrowType = Integer.parseInt(val);
			}
			
		}
		
		return(yp);
	}
	
	public static void main(String[] args) {
		YAMLParser yp = new YAMLParser();
		String s = "";
		try {
			s = Files.readString(Paths.get(	"/Users/brandmaier/Desktop/onyx-preset-demo.yaml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		yp.parse(s);
	}
}
