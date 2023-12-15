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
package gui.views;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import javax.swing.RepaintManager;

import engine.ModelComparison;
import engine.ModelRunUnit;
import engine.OnyxModel;
import engine.ParameterReader;
import engine.backend.Model;
import geometry.GeometricObject;
import geometry.Line;
import geometry.Rectangle;
import gui.Desktop;

public class ViewConnection {

	View from, to;
	ModelComparison modelComparison;

	public View getFrom() {
		return from;
	}

	public View getTo() {
		return to;
	}

	final static float dash1[] = {10.0f};
	final static BasicStroke dashed =
	        new BasicStroke(1.0f,
	                        BasicStroke.CAP_BUTT,
	                        BasicStroke.JOIN_MITER,
	                        10.0f, dash1, 0.0f);
	
	final static int size = 10;

	private int mx;

	private int my;

	private Point fromConnector;

	private Point toConnector;
	
	public ViewConnection(View from, View to) {
		super();
		this.from = from;
		this.to = to;
		// TvO 11.04.2019 added these lines to prepare nesting test in own Thread:
        Model m1 = ((ModelView)from).getModelRequestInterface().getModel();
        Model m2 = ((ModelView)to).getModelRequestInterface().getModel();
		modelComparison = new ModelComparison((OnyxModel)m1, (OnyxModel)m2);
	}
	
	public void paint(Graphics g, RepaintManager rm, Desktop desktop)
	{
		
		// show only, if both models are iconified
		//if (!this.from.isIconified() || !this.to.isIconified()) return;
		
		Graphics2D g2d = ((Graphics2D)g);
		
		Rectangle r1 = from.getRectangle();
		Rectangle r2 = to.getRectangle();
		
		Point centerFrom = r1.getCenter();
		Point centerTo = r2.getCenter();
		
		Line line = new Line(r1.getCenterX(), r1.getCenterY(), r2.getCenterX(), r2.getCenterY());
		
		
		
		fromConnector = GeometricObject.closestPoint( line.intersect(r1), centerTo );
		toConnector = GeometricObject.closestPoint( line.intersect(r2), centerFrom );
		
		
		Stroke oldStroke = g2d.getStroke();

		/*if (rm != null) {
			rm.addDirtyRegion(desktop, centerFrom.x, centerFrom.y, centerTo.x-centerFrom.x,
						centerTo.y-centerFrom.y);
		}*/

		
		
		g2d.setStroke(dashed);
		
		if (fromConnector != null && toConnector != null)
			g2d.drawLine(fromConnector.x, fromConnector.y, toConnector.x, toConnector.y);

		

		
		g2d.setStroke(oldStroke);
	
	
		/*mx = (centerTo.x+centerFrom.x)/2;
		my = (centerTo.y+centerFrom.y)/2;
		*/
		
		mx = (fromConnector.x+toConnector.x)/2;
		my = (fromConnector.y+toConnector.y)/2;
		
		
		g2d.fillOval(mx-size, my-size, 2*size, 2*size);
		

		
	}

	public Point getCenter() {
		return new Point(mx, my);
	}

	public String getModelComparisonString() {

	    // TvO 11.04.2019: Changed source for m1 and m2 to the modelComparison object
//	    OnyxModel m1 = modelComparison.first;
//	    OnyxModel m2 = modelComparison.second;

	    Model m1 = ((ModelView)from).getModelRequestInterface().getModel();
	    Model m2 = ((ModelView)to).getModelRequestInterface().getModel();
        if (m1.anzVar == 0 || m2.anzVar == 0) return "At least one model has no variables."; 
	    
//	    if (m1.anzPar < m2.anzPar) {OnyxModel t = m1; m1 = m2; m2 = t;}
//	    if (m1.anzPar == m2.anzPar) return "Models have the same number of parameter.";
//	    boolean isNested = m1.isNestedSubmodel(m2, 0.00001); 

		ParameterReader pr1 = ((ModelView)from).getShowingEstimate();
		ParameterReader pr2 = ((ModelView)to).getShowingEstimate();
		
		if ((pr1 == null) || (pr2 == null) || !(pr1 instanceof ModelRunUnit) || !(pr2 instanceof ModelRunUnit)) {
			return "Model estimates for at least one of the models are not available.";
		} else {
			ModelRunUnit mr1 = (ModelRunUnit)pr1;
			ModelRunUnit mr2 = (ModelRunUnit)pr2;
			return modelComparison.getLikelihoodRatioComparison(mr1, mr2);
		}
	}
	
	public boolean equals(Object o)
	{
		if (o instanceof ViewConnection)
		{
			ViewConnection vc = (ViewConnection)o;
			return (vc.from==this.from && vc.to==this.to);
		} else {
			return false;
		}
	}

	public boolean isOnLabel(int x, int y) {
		
		int xc = (fromConnector.x+toConnector.x)/2;
		int yc = (fromConnector.y+toConnector.y)/2;
		
		double d = Math.sqrt((xc-x)*(xc-x)+(yc-y)*(yc-y));
		
		return (d<size);
	}

    public ModelComparison getModelComparison() {return modelComparison;}

    public void initiateModelComparison() {
        modelComparison.first.addModelListener(modelComparison);
        modelComparison.second.addModelListener(modelComparison);
        modelComparison.startTestNesting();
    }
	
}
