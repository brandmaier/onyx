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
package gui.arrows;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class LineArrow extends Arrow {

	Stroke stroke;
	
	public LineArrow(int x1, int y1, int x2, int y2) {
		super(x1,y1,x2,y2);
	
		// set drawing style
		stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_BEVEL);
	}
	
	
	@Override
	public void draw(Graphics2D g) {
		
		g.setStroke(stroke);
		g.drawLine(x1,y1,x2,y2);
		
		

	}

}
