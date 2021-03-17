package importexport;

import engine.ModelRequestInterface;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

import java.io.File;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class VisioExport extends XMLExport {

	public VisioExport(ModelView modelView) {
		super(modelView);
		// TODO Auto-generated constructor stub
	}

	public Element export(Document doc) {
		
		
		errors.clear();
		
		ModelRequestInterface model = modelView.getModelRequestInterface();
		Graph graph = modelView.getGraph();
		
		Iterator<Node> nodeIter = graph.getNodeIterator();
		Iterator<Edge> edgeIter = graph.getEdgeIterator();
	
		
		Element rootElement =  doc.createElement("VisioDocument");
		rootElement.setAttribute("xmlns","urn:schemas-microsoft-com:office:visio" );
		
		Element docElement = doc.createElement("DocumentProperties");
		rootElement.appendChild(docElement);
		
		Element titleElement = doc.createElement("Title");
		docElement.appendChild(titleElement);
		titleElement.setTextContent(modelView.getName());
		
		Element creatorElement = doc.createElement("Creator");
		creatorElement.setTextContent("Onyx");
		docElement.appendChild(creatorElement);
		
		Element pagesElement = doc.createElement("Pages");
		rootElement.appendChild(pagesElement);
		
		Element pageElement = doc.createElement("Page");
		pagesElement.appendChild(pageElement);
		pageElement.setAttribute("ID", "0");
		pageElement.setAttribute("Name", modelView.getName());
		
		Element shapesElement = doc.createElement("Shapes");
		pageElement.appendChild(shapesElement);
		
		int id = 0;
		for (Node node : modelView.getGraph().getNodes()) {
			
			Element shapeElement = doc.createElement("Shape");
			id++;
		}
		
		return rootElement;
	}


}
