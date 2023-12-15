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
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

import importexport.filters.XMLFileFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains the main code to serialize an Onyx model
 * to a file. Code for loading is contained in XMLContentHandler.class
 * 
 * 
 */
public class XMLExport extends Export
{
	
	List<String> errors = new ArrayList<String>();

    public boolean isValid() {return true;}

	
    public String getHeader() {return "XML code";}    
    
	private static int getStrokeWidth(Node node)
	{
		if (node.getStroke() == Graph.strokeThin) {
			return 1;
		} else if (node.getStroke() == Graph.strokeMedium) {
			return 2;
		} else if (node.getStroke() == Graph.strokeThick) {
			return 3;
		} else if (node.getStroke() == Graph.strokeVeryThick) {
			return 4;
		} 
		
		return -1;
	}
	
	public XMLExport(ModelView modelView) {
		super(modelView, new XMLFileFilter(),new String[] {"xml"});
	}

	public Element export(Document doc) {
		
		
		errors.clear();
		
		ModelRequestInterface model = modelView.getModelRequestInterface();
		Graph graph = modelView.getGraph();
		
		Iterator<Node> nodeIter = graph.getNodeIterator();
		Iterator<Edge> edgeIter = graph.getEdgeIterator();

		Element rootElement =  doc.createElement("model");
		rootElement.setAttribute("name", escapeXML(model.getName()) );
		rootElement.setAttribute("specificationType", "ONYX");
		rootElement.setAttribute("specificationVersion", MainFrame.MAJOR_VERSION+"-"+MainFrame.SVN_VERSION);


		Element graphElement = doc.createElement("graph");
		graphElement.setAttribute("width", Integer.toString(modelView.getWidth()));
		graphElement.setAttribute("height", Integer.toString(modelView.getHeight()));
		graphElement.setAttribute("backgroundColor",  "#"+Integer.toHexString(graph.backgroundColor.getRGB() & 0x00ffffff));
		
		/*graphElement.setAttribute("style",graph.graphStyle.toString());
		graphElement.setAttribute("defaultNodeStroke",graph.defaultNodeStroke.toString());
		graphElement.setAttribute("defaultEdgeStroke",graph.defaultEdgeStroke.toString());
			*/		
		
		rootElement.appendChild(graphElement);
		
		//Element estimationElement = doc.createElement("estimation");
		graphElement.setAttribute("meanTreatment", graph.getMeanTreatment().toString());
		

		while (nodeIter.hasNext()) {
			Node node = nodeIter.next();
			Element nodeElement = doc.createElement("node");
			graphElement.appendChild(nodeElement);
			
			try {
			nodeElement.setAttribute("id", Integer.toString(node.getId()));
			nodeElement.setAttribute("x", Integer.toString(node.getX()));
			nodeElement.setAttribute("y", Integer.toString(node.getY()));
			nodeElement.setAttribute("width", Integer.toString(node.getWidth()));
			nodeElement.setAttribute("height", Integer.toString(node.getHeight()));
			nodeElement.setAttribute("latent", Boolean.toString(node.isLatent()));
			nodeElement.setAttribute("caption", escapeXML(node.getCaption()));
			nodeElement.setAttribute("constant", Boolean.toString(node.isMeanTriangle()));
			nodeElement.setAttribute("fillColor", "#"+Integer.toHexString(node.getFillColor().getRGB() & 0x00ffffff)  );
			nodeElement.setAttribute("fillStyle", node.getFillStyle().toString());
			nodeElement.setAttribute("roughness", Integer.toString((node.isRough()?1:0)));
			nodeElement.setAttribute("lineColor", "#"+Integer.toHexString(node.getLineColor().getRGB() & 0x00ffffff)  );
			nodeElement.setAttribute("strokeWidth", Integer.toString(getStrokeWidth(node)) );
			nodeElement.setAttribute("strokeWidth", Float.toString(
					((BasicStroke)node.getStroke()).getLineWidth() ));
			nodeElement.setAttribute("labelFontSize", Integer.toString(node.getFontSize()));
            nodeElement.setAttribute("multiplicative", Boolean.toString(node.isMultiplicationNode()));
			nodeElement.setAttribute("fontColor", "#"+ Integer.toHexString(node.getFontColor().getRGB() & 0x00ffffff) );
			

			/*if (node.image != null) {
				//
				 WritableRaster raster = ((BufferedImage)node.image).getRaster();
				 DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
				 
				 ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
			        //BufferedImage img=ImageIO.read(new File(dirName,"rose.jpg"));
			        ImageIO.write((BufferedImage)node.image, "jpg", baos);
			        baos.flush();

			       // String base64String=Base64.getEncoder().encode(baos.toByteArray());
			        baos.close();
			        
				// byte[] encoded = Base64.getEncoder().encode(data.getData());
				nodeElement.setAttribute("imageBase64", 
						Base64.getEncoder().encodeToString(data.getData()));
				nodeElement.setAttribute("imageBase64Width",String.valueOf(node.image.getWidth(null)));
				nodeElement.setAttribute("imageBase64Height",String.valueOf(node.image.getHeight(null)));
								
			}*/
			if (node.image != null) {
				nodeElement.setAttribute("imageFilename", "image_model1_node"+node.getId()+".png");
			}
			
			if (node.groupingVariable) {
				nodeElement.setAttribute("groupValue", Double.toString(node.groupValue));		
				nodeElement.setAttribute("groupName", node.groupName);
			}
			} catch (Exception e) {
				errors.add( "Node "+node.getCaption()+" could not be saved correctly.");
				e.printStackTrace();
			}
			
			nodeElement.setAttribute("normalized", Boolean.toString(node.isNormalized()));
			
		}
		
		while (edgeIter.hasNext()) {
			
			Edge edge = edgeIter.next();
			Element nodeElement = doc.createElement("edge");
			graphElement.appendChild(nodeElement);
			
			try {
			nodeElement.setAttribute("parameterName", escapeXML(edge.getParameterName()));
			nodeElement.setAttribute("sourceNodeId", Integer.toString(edge.getSource().getId()));				
			nodeElement.setAttribute("targetNodeId", Integer.toString(edge.getTarget().getId()));
			nodeElement.setAttribute("doubleHeaded", Boolean.toString(edge.isDoubleHeaded()));
			nodeElement.setAttribute("fixed", Boolean.toString(edge.isFixed()));
			nodeElement.setAttribute("value", Double.toString(edge.getValue()));
			nodeElement.setAttribute("automaticNaming", Boolean.toString(edge.isAutomaticNaming()));
			nodeElement.setAttribute("definitionVariable", Boolean.toString(edge.isDefinitionVariable()));				
			nodeElement.setAttribute("lineColor", "#"+Integer.toHexString(edge.getLineColor().getRGB() & 0x00ffffff)  ); 
			nodeElement.setAttribute("relativeLabelPosition", Double.toString(edge.edgeLabelRelativePosition ));
			nodeElement.setAttribute("curvature", Integer.toString(edge.getCurvature()));
			nodeElement.setAttribute("arrowHead", Integer.toString(edge.getArrowStyle()));
			if (edge.getDashStyle() != null) {
				float[] dash = edge.getDashStyle();
				//String dashstr = "";
				StringBuilder builder = new StringBuilder();
				
				for (float f : dash) {
					 if (builder.length() != 0) {
					        builder.append(";");
					    }
					 
					builder.append("" + f);
				}
				nodeElement.setAttribute("dash", builder.toString());
			}
			nodeElement.setAttribute("strokeWidth", Float.toString(
					((BasicStroke)edge.getStroke()).getLineWidth() ));
			nodeElement.setAttribute("labelFontSize", Float.toString(edge.getLabel().getFontSize()));
			nodeElement.setAttribute("labelFontColor", "#"+Integer.toHexString(edge.getLabel().getColor().getRGB() & 0x00ffffff ) );
			
			} catch (Exception e) {
				errors.add("Edge "+edge.getParameterName()+" could not be saved correctly");
				e.printStackTrace();
			}
			nodeElement.setAttribute("automaticControlPoints", Boolean.toString(edge.ctrlAutomatic));
			if (!edge.ctrlAutomatic) {
				nodeElement.setAttribute("relativeControlPoints", edge.relctrlx1
						+","+edge.relctrly1+";"+edge.relctrlx2+","+edge.relctrly2
						);
				nodeElement.setAttribute("controlPoints", edge.ctrlx1
						+","+edge.ctrly1+";"+edge.ctrlx2+","+edge.ctrly2
						);
			}
			nodeElement.setAttribute("arcPosition", Double.toString(edge.arcPosition));
			nodeElement.setAttribute("arcPositionAutoLayout", Boolean.toString(edge.arcPositionAutoLayout));

		}	
	
		return(rootElement);
	}
	
	/*private String escapeXML(String in) {
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < in.length(); i++) {
		    char c = in.charAt(i);
		    if(c < 31 || c > 126 || "<>\"'\\&".indexOf(c) >= 0) {
		        out.append("&#" + (int) c + ";");
		    } else {
		        out.append(c);
		    }
		}
		return out.toString();
	}*/
	
	private String escapeXML(String in )
	{
	//	System.out.println(StringEscapeUtils.escapeXml(in));
		
	//	return StringEscapeUtils.escapeXml(in);
		
		return in;
		
	
	}
	
	public String exportString() throws ParserConfigurationException, TransformerException 
	{

		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
//		rootElement.setAttribute("onyxVersion", "")
		
		Element rootElement = export(doc);
		
		doc.appendChild(rootElement);
		
		return(getStringFromDocument(doc));
	}
	
	public static String getStringFromDocument(Document doc) throws TransformerException {
	    DOMSource domSource = new DOMSource(doc);
	    StringWriter writer = new StringWriter();
	    StreamResult result = new StreamResult(writer);
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.transform(domSource, result);
	    return writer.toString();
	}

	@Override
	public void export(File file) {




		
		try {
			
			
			ModelRequestInterface model = modelView.getModelRequestInterface();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
//			rootElement.setAttribute("onyxVersion", "")
			
			Element rootElement = export(doc);
			
			doc.appendChild(rootElement);

			// write contents to file
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, MainFrame.CHAR_ENCODING);
		
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			
			transformer.transform(source, result);

		} catch (Exception e) {
			System.err.println("Error!");
			JOptionPane.showMessageDialog(this.modelView, "We are sorry! An error occured during writing the file: "+e.toString()+". Please report this error to onyx@brandmaier.de"); 
			e.printStackTrace();

		}
		
		if (errors.size() > 0) {
			String errstr="";
			for (String error : errors) errstr+="\n"+error;
			JOptionPane.showMessageDialog(this.modelView,"We are sorry! Your model could not be saved completely. The following errors occured during saving: "+errstr);
		}

	}





}
