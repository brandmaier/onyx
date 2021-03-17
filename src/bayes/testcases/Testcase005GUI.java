package bayes.testcases;

import engine.ModelRequestInterface;
import engine.ModelRun;
import engine.OnyxModel;
import engine.RawDataset;
import engine.backend.Model;
import engine.backend.Model.Strategy;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.linker.LinkException;
import gui.views.DataView;
import gui.views.ModelView;

public class Testcase005GUI {

	  public void testGUIEstimation() throws LinkException
	    {
	    	Desktop desktop = new Desktop();
	    	ModelView mv = new ModelView(desktop);
	    
	    	RawDataset dataset = RawDataset.createRandomDataset(100, 5);
	    	DataView dv = new DataView(desktop,dataset);
	    
	    	Graph graph = mv.getGraph();
	    
	    	ModelRequestInterface model = mv.getModelRequestInterface();
	        ((Model)model).setRandomSeed(887127872106L);
	    
	        /*
	    	Node intercept = new Node("Intercept"), slope = new Node("Slope");
	    	model.requestAddNode(intercept);
	    	model.requestAddNode(slope);
	    	Node[] obs = new Node[] { new Node("Obs0", false),
	    			new Node("Obs1", false), new Node("Obs2", false) };
	    	for (int i = 0; i < obs.length; i++)
	    		model.requestAddNode(obs[i]);
	    	Edge[] edges = new Edge[] { 
	    			new Edge(intercept, obs[0], false, 1.0),
	    			new Edge(intercept, obs[1], false, 1.0),
	    			new Edge(intercept, obs[2], false, 1.0),
	    			new Edge(slope, obs[1], false, 1.0),
	    			new Edge(slope, obs[2], false, 2.0),
	    			new Edge(intercept, intercept, true, 1.0),
	    			new Edge(slope, slope, true, 1.0),
	    			new Edge(intercept, slope, true, "covIS", 0.3) };
	    	for (int i = 0; i < edges.length; i++)
	    		model.requestAddEdge(edges[i]);
	    	for (int i = 0; i < obs.length; i++)
	    		model.requestAddEdge(new Edge(obs[i], obs[i], true, 1.0));
	    */
	    
	        
	        
	    	/*desktop.getLinkHandler().link(dataset, 0, graph, 3);
	    	desktop.getLinkHandler().link(dataset, 1, graph, 4);
	    	desktop.getLinkHandler().link(dataset, 2, graph, 2);
	    */
	        
	        Node x = new Node("x",false);
	        model.requestAddNode(x);
	        
	        Node y = new Node("y",false);
	        model.requestAddNode(y);
	        
	    

	        
	        Edge xx = new Edge(x,x,true);
	        xx.setFixed(false);
	        model.requestAddEdge(xx);

	        Edge yy = new Edge(y,y,true);
	        model.requestAddEdge(yy);
	        
	        model.setStrategy(Strategy.MCMC);
	        
	       	
	        desktop.getLinkHandler().link(dataset, 0, graph, model, x);
	        desktop.getLinkHandler().link(dataset, 1, graph, model, y);

	        mv.modelChangedEvent();
	        
	    	try {
	            Thread.sleep(500);
	            while (((OnyxModel)model).getStatus() == ModelRun.Status.RUNNING) Thread.sleep(200);
	            System.out.print(".");
	        } catch (InterruptedException e) {
	            //assertTrue(false);
	        }
	        System.out.println("DONE!"+ ((OnyxModel)model).getStatus());
	        
	        System.out.println("Anz converged"+model.getAnzConverged());
	        
	        System.out.println("Wert:"+model.getAllConvergedUnits().get(0).getParameterValues()[0]);
	    	
	    }
	  
	  public static void main(String[] args) {
		  try {
			(new Testcase005GUI()).testGUIEstimation();
		} catch (LinkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
}
