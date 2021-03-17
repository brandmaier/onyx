package gui.graph;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;



public class LaTeXParser {


	
	public int stringWidth;

	public LaTeXParser()
	{
		
	}
	
	public static void main(String[] args)
	{
		
		LaTeXParser l = new LaTeXParser();
		
		LaTeXParserTree tree = l.parse("a_{ter}^5 = 1");
	
		System.out.println(tree);
	}

	
	public LaTeXParserTree parse(String string) {
		
		// replace all ^x and _x with ^{x} and _{x}
		string = string.replaceAll("\\^([^{])","\\^{$1}");
		string = string.replaceAll("\\_([^{])","_{$1}");
		
	//	System.out.println("Prepared: "+string);
		
		LaTeXParserTree tree = new LaTeXParserTree();
		
		rec_parse(string, tree);
		
		return tree;
		
	}
	

	private String rec_parse(String string, LaTeXParserTree tree) {
		
		//System.out.println("Parse "+string);
		
		String buffer = "";
		int pos = 0;
		
		boolean updown = false;
		
		while (pos < string.length()) {

		if (string.charAt(pos) == '^') {
			LaTeXParserTree child = new LaTeXParserTree();
			tree.up = child;
			string = rec_parse(string.substring(pos+2), child);
			pos=-1;
			
			updown = true;
			
		} else if (string.charAt(pos) == '_') {
			LaTeXParserTree child = new LaTeXParserTree();
			tree.down = child;
			string = rec_parse(string.substring(pos+2), child);
			pos = -1;
			
			updown = true;

		} else if (string.charAt(pos)=='}') {
			//System.out.println(buffer+ "   -> return on } :"+string.substring(pos+1));
			tree.content = buffer;
			return string.substring(pos+1);
		} else {
			
			if (updown) {
				updown = false;
				LaTeXParserTree old = tree;
				tree = new LaTeXParserTree(); 
				old.next = tree;
				old.content = buffer;
				buffer="";
			}
			
			buffer+=string.charAt(pos);
		}
		pos++;
		
		}
		
		tree.content = buffer;
		//System.out.println("Buffer: "+buffer);
		return "";
		
	}
	
}
