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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class CurvedArrow 
    {
        // to draw a nice curved arrow, fill a V shape rather than stroking it with lines
        public void draw ( Graphics2D g )
        {
            // as we're filling rather than stroking, control point is at the apex,

            float arrowRatio = 0.5f;
            float arrowLength = 80.0f;

            BasicStroke stroke = ( BasicStroke ) g.getStroke();

            float endX = 350.0f;

            float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;

            // vee
            Path2D.Float path = new Path2D.Float();

            float waisting = 0.5f;

            float waistX = endX - arrowLength * 0.5f;
            float waistY = arrowRatio * arrowLength * 0.5f * waisting;
            float arrowWidth = arrowRatio * arrowLength;

            path.moveTo ( veeX - arrowLength, -arrowWidth );
            path.quadTo ( waistX, -waistY, endX, 0.0f );
            path.quadTo ( waistX, waistY, veeX - arrowLength, arrowWidth );

            // end of arrow is pinched in
            path.lineTo ( veeX - arrowLength * 0.75f, 0.0f );
            path.lineTo ( veeX - arrowLength, -arrowWidth );

           

            
            
        }
    }
