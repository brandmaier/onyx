package gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import engine.ModelRequestInterface;
import gui.Constants;
import gui.graph.Edge;
import gui.graph.Node;

public class ModelFactory {


		
	public static void createFactorModel(ModelRequestInterface model,
			int N, String label_factor, List<String> label_observed,
			List<String> label_residuals, int group, int xOffset, int yOffset, boolean item_scaling,
			boolean force_latent_variance_free, String latent_tag, String group_variable_name)
	{

		//int N = numObs;
		
		int x_spacing = 4*Constants.DEFAULT_GRID_SIZE;

		int xFactor = (xOffset + (xOffset+x_spacing*(N-1))) / 2;
		
		Node factor = new Node(label_factor);
		factor.addTag(latent_tag);
		factor.setPosition(xFactor, yOffset+70);
		
		model.requestAddNode(factor);
		
		//int maxx = 200;
		

		
		Node[] obs = new Node[N];
		for (int i=0; i < N; i++) {
			obs[i] = new Node(label_observed.get(i));
			obs[i].setIsLatent(false);
			obs[i].setPosition(xOffset+i*x_spacing, yOffset+200);
			model.requestAddNode(obs[i]);

			if (group != -1) {
				obs[i].setGrouping(true);
				obs[i].groupValue = group;
				obs[i].groupName = group_variable_name;
			}
			
			
			if (i>0 || !item_scaling) {
				Node m = new Node();
				m.setTriangle(true);
				m.setPosition(xOffset+i*x_spacing, yOffset+200+130);
				model.requestAddNode(m);
				
				Edge edge = new Edge(m, obs[i]);
				edge.setDoubleHeaded(false);
				edge.setValue(0);
				edge.setFixed(false);
				model.requestAddEdge(edge);
				
				//maxx = Math.max(maxx, m.getX()+x_spacing);
			}
		}
		

		// Loadings
		for (int i=0; i < N; i++) {

				
			

				Edge edge2 = new Edge(factor, obs[i],false);
				
				edge2.edgeLabelRelativePosition = ((i+1)/(float)N);
				
				if (i>0 || !item_scaling){
					edge2.setFixed(false);
					edge2.setValue(.7);
				}
				
				edge2.setAutomaticNaming(false);
				edge2.setParameterName("\\lambda"+(i+1));
				
				model.requestAddEdge(edge2);
			//}
		}
		
		// residual variances
		for (int i=0; i < N; i++) {
			Edge edge = new Edge(obs[i], obs[i],true);
			edge.setValue(1.0);
			//if (uniqueResiduals.isSelected())
			//	edge.setParameterName(nameErrInput.getText()+(i+1));
			//else
				edge.setParameterName(label_residuals.get(i));
			edge.setAutomaticNaming(false);
			edge.setFixed(false);
			model.requestAddEdge(edge);
		}
		
		// latent variances
		
		
		Edge edgeS = new Edge(factor, factor, true);
		edgeS.setValue(1.0);
		edgeS.setParameterName("\\sigma");
		edgeS.setAutomaticNaming(false);
		edgeS.setFixed(false);
		
		if (!item_scaling && !force_latent_variance_free) {
			edgeS.setFixed(true);
			edgeS.setValue(1);
		}
		
		model.requestAddEdge(edgeS);
		
		// add mean
		if (item_scaling) {
			Node mean = new Node();
			mean.setPosition(xOffset+100, yOffset-60);
			mean.setTriangle(true);
	
			
			model.requestAddNode(mean);
			
	
			
			Edge edgeMs = new Edge(mean, factor);
			edgeMs.setFixed(false);
			edgeMs.setAutomaticNaming(false);
			edgeMs.setParameterName("\\mu");
	
			model.requestAddEdge(edgeMs);
		}
	
	
		
	}




	
}
