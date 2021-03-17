package bayes.testcases;

import bayes.BayesianSEM;
import bayes.Chain;
import bayes.gui.ChainPlotWindow;
import bayes.priors.ChiSquare;
import bayes.priors.FlatDegeneratePrior;
import bayes.priors.GammaDistribution;
import bayes.priors.GaussianPrior;
import bayes.sampler.MetropolisHastings;
import engine.ModelRequestInterface;
import engine.OnyxModel;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;
/*
 * 
 * 
 * R CODE: 
df <- data.frame(x=c(10,3,1,3),y=c(2,4,5,4))
cov(df)
summary(lm(y~x,data=df))


Varianz x: ca 11.19
Residual Varianz Y: ca 0.05
Regression x->y: ca. -0.32
 * 
 */
public class Testcase001BayesianSEM {

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
	        
	        Edge edge = new Edge(node,node2);
	        edge.setFixed(false);
	        edge.setDoubleHeaded(false);
	        model.requestAddEdge(edge);
	        
	        Edge edge1 = new Edge(node,node);
	        edge1.setFixed(false);
	        edge1.setDoubleHeaded(true);
	        model.requestAddEdge(edge1);
	        
	        
	        Edge edge2 = new Edge(node2,node2);
	        edge2.setFixed(false);
	        edge2.setDoubleHeaded(true);
	        model.requestAddEdge(edge2);
	        
	        
	        
	        
	        double[][] data = new double[][] {
	        	{10,2},
	        	{3,4},
	        	{1,5},
	        	{5,4}
	        	
	        	
	        };
	        
	       model.setData(data);
	        
	     BayesianSEM bsem = new BayesianSEM(model);
	     
	     bsem.setPrior(0, new GaussianPrior(0, 100));
	     bsem.setPrior(1, new GammaDistribution(1,1));
	     bsem.setPrior(2, new GammaDistribution(1,1));
	     
	   // for (int i=0; i < model.getAnzPar(); i++)
	   //  bsem.setPrior(i, new FlatDegeneratePrior());
	     
	     MetropolisHastings sampler = new MetropolisHastings(bsem, data);
	     
	     sampler.width=.09;
	     
	     Chain chain = sampler.run(10000, 5000);
	     
	     System.out.println(chain);
	     
	     ChainPlotWindow cpw = new ChainPlotWindow(chain, model.getParameterNames());
	     cpw.setVisible(true);
	     
	     for (int i=0; i<model.getAnzPar(); i++)
	    	 System.out.println(chain.getMean(i));
	     
	}
	
}
