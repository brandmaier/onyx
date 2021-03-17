package gui.graph;

import java.util.ArrayList;
import java.util.List;

public class LaTeXParserTree {
	
	
	
		LaTeXParserTree up, down, next;
	
		public String content;
		public int level = 0;
		public int type;
	
		public String toString()
		{
			return toStringRec(0);
		}

		private String toStringRec(int i) {
			
			String tabs = "";
			for (int j=0; j<i;j++) tabs+="\t";
			
			String s =  tabs+"Content:"+content+"\n";
			if (next != null)
				s+=tabs+"Next:\n"+next.toStringRec(i+1)+"\n";
		
			if (up != null)
				s+=tabs+"Up:"+up.content+"\n";
			if (down != null)
				s+=tabs+"Down:"+down.content+"\n";
			
			return s;
		}
		
}
