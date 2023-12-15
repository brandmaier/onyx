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
