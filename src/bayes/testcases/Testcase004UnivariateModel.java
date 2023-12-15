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
package bayes.testcases;

import bayes.BayesianSEM;
import bayes.Chain;
import bayes.gui.ChainPlotWindow;
import bayes.priors.FlatDegeneratePrior;
import bayes.priors.GaussianPrior;
import bayes.priors.Uniform;
import bayes.sampler.MetropolisHastings;
import engine.OnyxModel;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class Testcase004UnivariateModel {

	public static void main(String[] args) {
		
		 Desktop desktop = new Desktop();
	     ModelView mv = new ModelView(desktop);
	     
	     OnyxModel model = (OnyxModel)mv.getModelRequestInterface();
	     
	     Node node = new Node();
	        node.setX(123);
	        node.setY(77);
	        node.setCaption("X");
	        node.setIsLatent(false);
	        model.requestAddNode(node);

	        
	        Node tri = new Node();
	        tri.setTriangle(true);
	        model.requestAddNode(tri);
	        
	        
	        Edge edge = new Edge(tri,node);
	        edge.setFixed(false);
	        edge.setDoubleHeaded(false);
	        model.requestAddEdge(edge);
	        
	        Edge edge1 = new Edge(node,node);
	        edge1.setFixed(false);
	        
	        edge1.setDoubleHeaded(true);
	        model.requestAddEdge(edge1);
	        
	      
	        int sampleSize = 20;
	        double[][] data = new double[sampleSize][1];
	        for (int i=0; i < sampleSize; i++){
	        	data[i][0] = 3+(i % 2==0?2:0);
	        }
	        
	        model.setData(data);
	        
	     BayesianSEM bsem = new BayesianSEM(model);
	     
	     bsem.setPrior(0, new GaussianPrior(1, 10.1));
	    // bsem.setPrior(1, new GaussianPrior(5, 100));
	      bsem.setPrior(1, new Uniform(0,10));
	   
	  //   bsem.setPrior(0, new FlatDegeneratePrior());
	   //  bsem.setPrior(1, new FlatDegeneratePrior());

	     
	     MetropolisHastings sampler = new MetropolisHastings(bsem, data);
	     sampler.setSeed(123);
	     
	     sampler.width=.5;
	     
	     Chain chain = sampler.run(1000, 500);
	     
	     System.out.println(chain);
	     
	     ChainPlotWindow cpw = new ChainPlotWindow(chain, model.getParameterNames());
	     cpw.setVisible(true);
	     
	     for (int i=0; i<model.getAnzPar(); i++)
	    	 System.out.println(chain.getMean(i));
	     
	}
	
}
