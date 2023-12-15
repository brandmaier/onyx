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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class OtherArrow extends Arrow {

	public OtherArrow(int x1, int y1, int x2, int y2) {
		super(x1,y1,x2,y2);
		
	}
	
	@Override
	public void draw(Graphics2D g) {

	  {
	            // where the control point for the intersection of the V needs calculating
	            // by projecting where the ends meet

	            float arrowRatio = 0.5f;
	            float arrowLength = 80.0f;

	            BasicStroke stroke = ( BasicStroke ) g.getStroke();

	            float endX = 350.0f;

	            float veeX;

	            switch ( stroke.getLineJoin() ) {
	                case BasicStroke.JOIN_BEVEL:
	                    // IIRC, bevel varies system to system, this is approximate
	                    veeX = endX - stroke.getLineWidth() * 0.25f;
	                    break;
	                default:
	                case BasicStroke.JOIN_MITER:
	                    veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;
	                    break;
	                case BasicStroke.JOIN_ROUND:
	                    veeX = endX - stroke.getLineWidth() * 0.5f;
	                    break;
	            }

	            // vee
	            Path2D.Float path = new Path2D.Float();

	            path.moveTo ( veeX - arrowLength, -arrowRatio*arrowLength );
	            path.lineTo ( veeX, 0.0f );
	            path.lineTo ( veeX - arrowLength, arrowRatio*arrowLength );

	            g.setColor ( Color.BLUE );
	            g.draw ( path );

	            // stem for exposition only
	            g.setColor ( Color.YELLOW );
	            g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX, 0.0f ) );

	            // in practice, move stem back a bit as rounding errors
	            // can make it poke through the sides of the Vee
	            g.setColor ( Color.RED );
	            g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX - stroke.getLineWidth() * 0.25f, 0.0f ) );
	        }
		 
	}

}
