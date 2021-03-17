package gui.tutorial;

import java.awt.Component;

import engine.Dataset;
import engine.ModelListener;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.RawDataset;
import engine.backend.Model.Strategy;
import gui.Desktop;
import gui.DesktopListener;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.DataView;
import gui.views.ModelView;
import gui.views.View;

public class TutorialFirstSteps implements DesktopListener, ModelListener {

	int counter = 0;
	
	TutorialView tutorialView;
	Desktop desktop;

	private ModelView modelView;
	DataView dv;
	
	Node node1, node2, node3;
	
	public TutorialFirstSteps(Desktop desktop)
	{
	
		
		//if (ok) {
		tutorialView = new TutorialView(desktop);
		desktop.add(tutorialView);
		this.desktop = desktop;
		desktop.addDesktopListener(this);
		
		step();
		
		//}
	}
	
	
	public void step()
	{
		
		if (counter == 0) {
			tutorialView.setText("Welcome to Onyx! This tutorial will lead you through the basic ideas of modeling in Onyx. Let's start and creaty an empty model. Double-click at the workspace.");

			for (View v : desktop.getViews()) {
				if (v instanceof gui.views.ModelView) {
					modelView = ((ModelView)v);
					modelView.getModelRequestInterface().addModelListener(this);
					counter++;
				}
			}
		}
		
		
		if (counter == 1) {
			tutorialView.setText("Well done! Now, let us open a dataset. Click on the top menu bar and select 'load tutorial'. Then load 'Simple Regression Example'.");
		
			for (View v : desktop.getViews()) {
				if (v instanceof gui.views.DataView) {
					dv = (DataView)v;
					if (dv.getName().equals("Simple Regression Example")) counter++;
//					counter++;
				}
			}
			
		}

		if (counter == 2) {
			tutorialView.setText("Now let us add variables from the dataset to the model. Click on variable X in the dataset and while holding down the mouse button, drag it onto the model panel.");

			for (Node node : modelView.getGraph().getNodes()) {
				if (node.getCaption().equals("X_c")) {
					counter++;
					node1 = node;
					break;
				}
			}
			
		}
		
		if (counter == 3) {
			tutorialView.setText("Great! Now let us add another variable from the dataset to the model. Click on variable Y in the dataset and while holding down the mouse button, drag it onto the model panel.");

			for (Node node : modelView.getGraph().getNodes()) {
				if (node.getCaption().equals("Y_c")) {
					counter++;
					node2 = node;
					break;
				}
			}
			
		}
		
		if (counter == 4) {
			tutorialView.setText("Let's move to latent variables. Double-click onto the model panel to create a latent variable.");

			for (Node node : modelView.getGraph().getNodes()) {
				if (node.isLatent()) {
					counter++;
					node3 = node;break;
				}
			}
			
		}
		
		if (counter == 5) {
			tutorialView.setText("Let's create paths (so called factor loadings) for the latent variable. Right-click on the latent variable and drag a path onto X_c! ");

			for (Edge edge : modelView.getGraph().getEdges()) {
				if (edge.getSource()==node3 && edge.getTarget()==node1) {
					counter++; break;
				}
			}
		}
		
		if (counter == 6) {
			tutorialView.setText("Great! Now connect the latent variable with Y_c in the same manner! ");

			for (Edge edge : modelView.getGraph().getEdges()) {
				if (edge.getSource()==node3 && edge.getTarget()==node2) {
					counter++; break;
				}
			}
		}
		
		if (counter == 7) {
			tutorialView.setText("Let's fix the variance of the latent construct (that is currently freely estimated) to unity. Right-click on the double-headed path on the latent variable and select 'fix parameter'. Right-click again and set the value to '1'. ");

			for (Edge edge : modelView.getGraph().getEdges()) {
				if (edge.getSource()==node3 && edge.getTarget()==node3) {
					if (edge.isFixed() && edge.getValue()==1)
						counter++; break;
				}
			}
		}
		
		if (counter == 8) {
			tutorialView.setText("Let's freely estimate the loading of the latent variable onto Y_c. Right-click on the respective path and choose 'free parameter'. ");

			for (Edge edge : modelView.getGraph().getEdges()) {
				if (edge.getSource()==node3 && edge.getTarget()==node2) {
					if (edge.isFree())
						counter++; break;
				}
			}
		}
		
		
		
		if (counter == 9) {
			tutorialView.setText("Awesome! You have finished this tutorial! Right-click and choose 'close' to close this frame.");

		}
		
	}


	@Override
	public void viewAdded() {
		step();
		
		//desktop.
		desktop.moveToFront(tutorialView);
		//	tutorialView.
	}


	@Override
	public void addNode(Node node) {
		step();
		
	}


	@Override
	public void addEdge(Edge edge) {
		step();
		
	}


	@Override
	public void swapLatentToManifest(Node node) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void changeName(String name) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeEdge(int source, int target, boolean isDoubleHeaded) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeNode(int id) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void deleteModel() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void cycleArrowHeads(Edge edge) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void swapFixed(Edge edge) {
		step();
		
	}


	@Override
	public void changeStatus(Status status) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void notifyOfConvergedUnitsChanged() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setValue(Edge edge) {
		step();
		
	}


	@Override
	public void notifyOfStartValueChange() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void changeParameterOnEdge(Edge edge) {
		step();
		
	}


	@Override
	public void notifyOfWarningOrError(Warning warning) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void newData(int N, boolean isRawData) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void changeNodeCaption(Node node, String name) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setDefinitionVariable(Edge edge) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void notifyOfClearWarningOrError(Warning warning) {}


	public View getView() {
		return this.tutorialView;
	}


	@Override
	public void unsetDefinitionVariable(Edge edge) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setGroupingVariable(Node node) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void unsetGroupingVariable(Node node) {
		// TODO Auto-generated method stub
		
	}


    @Override
    public void notifyOfFailedReset() {
        // TODO Auto-generated method stub
        
    }


	@Override
	public void addDataset(Dataset dataset, int x, int y) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addDataset(double[][] dataset, String name, String[] additionalVariableNames, int x, int y) {
		// TODO Auto-generated method stub
		
	}


    @Override
    public void addAuxiliaryVariable(String name, int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void addControlVariable(String name, int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void removeAuxiliaryVariable(int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void removeControlVariable(int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void notifyOfStrategyChange(Strategy strategy) {
        // TODO Auto-generated method stub
        
    }
	
	
}
