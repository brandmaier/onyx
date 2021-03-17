package bayes.testcases;

import bayes.BayesianSEM;
import bayes.Chain;
import bayes.gui.ChainPlotWindow;
import bayes.priors.FlatDegeneratePrior;
import bayes.priors.GammaDistribution;
import bayes.priors.GaussianPrior;
import bayes.priors.Uniform;
import bayes.sampler.MetropolisHastings;
import engine.OnyxModel;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class Testcase003SimpleRegression {

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

	        Node node2 = new Node();
	        node2.setX(23);
	        node2.setY(17);
	        node2.setCaption("Y");
	        node2.setIsLatent(false);
	        model.requestAddNode(node2);
	        
	        /*Node tri = new Node();
	        tri.setTriangle(true);
	        model.requestAddNode(tri);
	        */
	        
	        Edge edge = new Edge(node,node2);
	        edge.setFixed(false);
	       
	        edge.setDoubleHeaded(false);
	        model.requestAddEdge(edge);
	        
	        Edge edge1 = new Edge(node,node);
	        edge1.setFixed(true);
	        edge1.setValue(15);
	        edge1.setDoubleHeaded(true);
	        model.requestAddEdge(edge1);
	        
	        
	        Edge edge2 = new Edge(node2,node2);
	        edge2.setFixed(false);
	        edge2.setDoubleHeaded(true);
	        model.requestAddEdge(edge2);
	        
	        
	        double trueA = 5;
	        double trueB = 0;
	        double trueSd = 10;
	        int sampleSize = 31;
	        
	        double[][] data = new double[sampleSize][2];
	        for (int i=0; i < sampleSize; i++){
	        	double x = Math.random();
	        	double y = trueA+trueB*x;
	        }
	        
	        model.setData(data);
	        
	     BayesianSEM bsem = new BayesianSEM(model);
	     
	     bsem.setPrior(0, new GaussianPrior(0, 25));
	     bsem.setPrior(1, new Uniform(1,10));
	   //  bsem.setPrior(2, new GammaDistribution(1,1));
	     

	     
	     MetropolisHastings sampler = new MetropolisHastings(bsem, data);
	     
	     sampler.width=1;
	     
	     Chain chain = sampler.run(1000, 500);
	     
	     System.out.println(chain);
	     
	     ChainPlotWindow cpw = new ChainPlotWindow(chain, model.getParameterNames());
	     cpw.setVisible(true);
	     
	     for (int i=0; i<model.getAnzPar(); i++)
	    	 System.out.println(chain.getMean(i));
	     
	}
	
}
