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
