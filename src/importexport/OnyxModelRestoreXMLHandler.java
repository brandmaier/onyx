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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.FillStyle;
import gui.graph.Graph.MeanTreatment;
import gui.graph.Node;
import gui.views.ModelView;

public class OnyxModelRestoreXMLHandler implements ContentHandler {
	
	ModelView mv;
	private boolean warningNormalizedDeprecated;
	private boolean warningMeanInconsistency;
	private int img64height;
	private int img64width;
	private String img64data;

	public OnyxModelRestoreXMLHandler(ModelView mv)
	{
		this.mv = mv;
	}
	
	@Override
	public void characters(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endDocument() throws SAXException {
		this.mv.redraw();
		
	/*	if (warningNormalizedDeprecated) {
			JOptionPane.showMessageDialog(this.mv, "Warning!\n"
					+ " The model has observed variables that were tagged as normalized."
					+ " Older versions of Onyx used to normalize data connected to these nodes for you. "
					+ "This feature was removed. Please normalize your data manually! ");
		}*/
		
/*		boolean hasMeanNodes
		for (Node node : mv.getGraph().getNodes()) {
			if (node.isMeanTriangle()) 
		}
*/
		if (warningMeanInconsistency) {
			JOptionPane.showMessageDialog(this.mv, "Warning! This model has a saturated mean structure. The explicit mean structure in the model is currently ignored.");
			mv.getGraph().setMeanTreatment(MeanTreatment.implicit); mv.getModelRequestInterface().setMeanTreatment(mv.getGraph().getMeanTreatment());
		}
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws SAXException {
		//warningNormalizedDeprecated = false;
		warningMeanInconsistency = false;
	}

	@Override
	public void startElement(String namespace, String localname, String qname,
			Attributes attrs) throws SAXException {
		
	//	System.out.println(localname+" "+qname);
		/*
		 * <Node caption="Obs2" height="50" id="4" latent="false" width="50" x="220" y="220"/>
		 * <Edge doubleHeaded="false" fixed="true" parameterName="Intercept-&gt;Obs0" sourceNodeId="0" targetNodeId="2" value="1.0"/>
		 * 
		 */
		try {
			
			if (localname.equalsIgnoreCase( "Model")) {
				if (attrs != null)
		        {
		            int length = attrs.getLength();
		            for (int i = 0; i < length; i++)
		            {
		            	if (attrs.getLocalName(i) == "name") {
		            		this.mv.getModelRequestInterface().requestChangeModelName(attrs.getValue(i));
		            	}
		            	
		            	if (attrs.getLocalName(i).equals("specificationVersion")) {
		            		try {
		            			String[] tokens = attrs.getValue(i).split("-|\\.");
		            			
		            			int major = Integer.parseInt(tokens[0]);
		            			int minor = Integer.parseInt(tokens[1]);
		            			
		            			int svn = 0;
		            			if (tokens.length > 2)
		            				svn = Integer.parseInt(tokens[2]);
		            			
		            			if (MainFrame.SVN_VERSION < svn) {
		            				JOptionPane.showMessageDialog(this.mv, "Warning! The model was saved with a newer version of Onyx than the one you are using. Likely, not all features of the model will be loaded correctly.");
		            			}
		            			
		            		} catch (Exception e) {
		            			//e.printStackTrace();
		            			System.err.println("Could not parse version number in file: "+attrs.getValue(i));
		            		}
		            	}
		            }
		        }
			}
			
		if (localname.equalsIgnoreCase( "Graph")) {
			if (attrs != null)
	        {
	            int length = attrs.getLength();
	            for (int i = 0; i < length; i++)
	            {
	            	if (attrs.getLocalName(i) == "width") {
	            		int w = Integer.parseInt(attrs.getValue(i));
	            		this.mv.setSize(w, mv.getSize().height);
	            	}
	            	
	            	if (attrs.getLocalName(i) == "height") {
	            		int h = Integer.parseInt(attrs.getValue(i));
	            		this.mv.setSize(mv.getSize().width, h);
	            	}
	            	
	            	if (attrs.getLocalName(i) == "backgroundColor") {
	            		Color c = Color.decode(attrs.getValue(i));
	            		this.mv.getGraph().backgroundColor = c;
	            	}
	            	
	            	if (attrs.getLocalName(i) == "meanTreatment") {
	            		try {
	            		this.mv.getGraph().setMeanTreatment(MeanTreatment.valueOf(attrs.getValue(i)));
	                    this.mv.getModelRequestInterface().setMeanTreatment(this.mv.getGraph().getMeanTreatment());
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	            	}
	            }
	        }
			
			//this.mv.setSize(width, height)
		}
		
		if (localname.equalsIgnoreCase("Edge")) {
			
			Edge newEdge = new Edge();
			if (attrs != null)
	        {
	            int length = attrs.getLength();
	            for (int i = 0; i < length; i++)
	            {
	            	if (attrs.getLocalName(i) == "fixed") {
	            		newEdge.setFixed(Boolean.parseBoolean(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i) == "doubleHeaded") {
	            		newEdge.setDoubleHeaded(Boolean.parseBoolean(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i) == "parameterName") {
	            		newEdge.setParameterName(attrs.getValue(i));
	            		newEdge.setAutomaticNaming(false);
	            	} else if (attrs.getLocalName(i) == "automaticNaming") {
	            		newEdge.setAutomaticNaming(Boolean.parseBoolean(attrs.getValue(i)));	       	            
	            	} else if (attrs.getLocalName(i) == "sourceNodeId") {
	            		int sourceId = Integer.parseInt(attrs.getValue(i));
	            		newEdge.setSource( mv.getGraph().getNodeById(sourceId));
	            	} else if (attrs.getLocalName(i) == "targetNodeId") {
	            		int targetId = Integer.parseInt(attrs.getValue(i));
	            		Node node = mv.getGraph().getNodeById(targetId);
	            		if (node==null) {
	            			System.err.println("Error! Undefined node!");
	            		}
	            		newEdge.setTarget( node );	            		
	            	} else if (attrs.getLocalName(i) == "value") {
	            		newEdge.setValue( Double.parseDouble(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i) == "definitionVariable") {
	            		boolean defvar = Boolean.parseBoolean(attrs.getValue(i));

	            		//if (defvar) {
	            			//newEdge.setDefinitionVariable(null, -1, true);
	            			newEdge.getDefinitionVariableContainer().setActive(defvar);
	            	//	} else {	            			
	            			//newEdge.setDefinitionVariable(null, -1, false);
	            		//}
	            		
	            	} else if (attrs.getLocalName(i).equals("lineColor")) {
	            		newEdge.setLineColor( Color.decode(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i) == "relativeLabelPosition") {
	            		newEdge.edgeLabelRelativePosition = (Double.parseDouble(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i) == "curvature") {
	            		newEdge.setCurvature ( Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("arrowHead")) {
	            		newEdge.setArrowStyle(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("strokeWidth")) { 
	            		//newEdge.setStroke(new BasicStroke(Float.parseFloat(attrs.getValue(i)), BasicStroke.CAP_BUTT, 
	            	     //       BasicStroke.JOIN_BEVEL));
	            		newEdge.setLineWidth(Float.parseFloat(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("labelFontSize")) {
	            		newEdge.getLabel().setFontSize( Float.parseFloat(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("labelFontColor")) {
	            		newEdge.getLabel().setColor(Color.decode(attrs.getValue(i)) );	            		
	            	} else if (attrs.getLocalName(i).equals("relativeControlPoints")) {
	            		String[] pairs = attrs.getValue(i).split(";");
	            		int cnt=0;
	            		for (String pair : pairs)
	            		{
	            			String[] coords = pair.split(",");
	            			double x = Double.parseDouble(coords[0]);
	            			double y = Double.parseDouble(coords[1]);
	            			//if (cnt==0) {
	            			//	newEdge.ctrlx1 = x;
	            				//newEdge.ctrly1 = y;
	            				newEdge.setRelativeCtrlPoint(cnt, x, y);
	            			//} else if (cnt==1) {
	//            				newEdge.ctrlx2 = x;
//	            				newEdge.ctrly2 = y;
	            				//newEdge.setCtrlPoint(cnt, x, y);
	            			//} else {
	            				// IGNORE
	            			//}
	            			cnt+=1;
	            		}
	            	
	            	} else if (attrs.getLocalName(i).equals("dash")) {
	            		try {
	            			String[] tokens = attrs.getValue(i).split(";");
	            			float[] f = new float[tokens.length];
	            			for (int j=0; j < f.length; j++)
	            			{
	            				f[j] = Float.parseFloat(tokens[j]);
	            			}
	            			newEdge.setDashStyle(f);
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	            	} else if (attrs.getLocalName(i).equals("automaticControlPoints")) {
	            		newEdge.ctrlAutomatic = Boolean.parseBoolean(attrs.getValue(i));
	            	} else if (attrs.getLocalName(i).equals("arcPosition")) {
	            		newEdge.arcPosition = Double.parseDouble(attrs.getValue(i));
	            	} else if (attrs.getLocalName(i).equals("arcPositionAutoLayout")) {
	            		newEdge.arcPositionAutoLayout = Boolean.parseBoolean(attrs.getValue(i));
	            	}
	            			
	            }
	        }
			
			
			boolean ok = false;
			
			// TvO 01 MAR 14: allows definition variable edges, will automatically call the corresponding request. 
			try {
			ok = mv.getModelRequestInterface().requestAddEdge(newEdge);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!ok) {
				System.err.println("Could not add edge "+newEdge);
			} else {
				//System.out.println(newEdge);
			}
		}
		
		if (localname.equalsIgnoreCase("Node"))
		{
			Node newNode = new Node();
			if (attrs != null)
	        {
	            int length = attrs.getLength();
	            for (int i = 0; i < length; i++)
	            {
//	            	System.out.println(attrs.get)
	            	if (attrs.getLocalName(i) == "x") {
	            		newNode.setX(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="y") {
	            		newNode.setY(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="width") {
	            		newNode.setWidth(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="value") {
	            		newNode.setCaption(attrs.getValue(i));
	            	} else if (attrs.getLocalName(i)=="id") {
	            		newNode.setId(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="latent")
	            	{
	            		newNode.setIsLatent( Boolean.parseBoolean(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="height") {
	            		newNode.setHeight(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i)=="caption") {
	            		newNode.setCaption(attrs.getValue(i));
	            	}else if (attrs.getLocalName(i)=="constant") {
	            		newNode.setTriangle(Boolean.parseBoolean(attrs.getValue(i)));
	            		
	            		if (newNode.isMeanTriangle() && mv.getGraph().getMeanTreatment()==MeanTreatment.implicit ) {
	            			warningMeanInconsistency = true;
	            		}
	            	} else if (attrs.getLocalName(i).equals("roughness")) {
	            		newNode.setRough( Double.parseDouble(attrs.getValue(i))>0.0);
	            	} else if (attrs.getLocalName(i).equals("lineColor")) {
	           
	            		newNode.setLineColor( Color.decode(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("fontColor")) {	     	           
	            		newNode.setFontColor( Color.decode(attrs.getValue(i)));
	            	}else if (attrs.getLocalName(i).equals("fillColor")) {
	            		newNode.setFillColor( Color.decode(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("fillStyle")) {
	            		try {
	            			newNode.setFillStyle(FillStyle.valueOf(attrs.getValue(i)));
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	            	/*} else if (attrs.getLocalName(i).equals("imageBase64Width")) {
	            			
	            		this.img64width = Integer.parseInt(attrs.getValue(i));
	            		
	            		imageReady(newNode);
	            	} else if (attrs.getLocalName(i).equals("imageBase64Height")) {
            			
	            		this.img64height = Integer.parseInt(attrs.getValue(i));
	            			       
	            		imageReady(newNode);
	            	} else if (attrs.getLocalName(i).equals("imageBase64")) {
	            		

	            		this.img64data = attrs.getValue(i);
	            		
	            		imageReady(newNode);
	            		*/
	            	} else if (attrs.getLocalName(i).equals("groupValue")) {
	            		newNode.groupingVariable = true;
	            		newNode.groupValue = Double.parseDouble(attrs.getValue(i));
	            	} else if (attrs.getLocalName(i).equals("groupName")) {
	            		newNode.groupingVariable = true;
	            		newNode.groupName =(attrs.getValue(i));
	            	} else if (attrs.getLocalName(i).equals("labelFontSize")) {
	            		newNode.setFontSize(Integer.parseInt(attrs.getValue(i)));
	            	} else if (attrs.getLocalName(i).equals("strokeWidth")) {
	            		Float f = Float.parseFloat(attrs.getValue(i));
	            		if (f < 0) f = 2.0f;
	            		newNode.setStrokeWidth(f);
	            	} else if (attrs.getLocalName(i).equals("normalized")) {
	            		Boolean b = Boolean.parseBoolean(attrs.getValue(i));
	            		if (b) {
	            			newNode.setNormalized(true);
	            			 //warningNormalizedDeprecated = true;
	            		}
                    } else if (attrs.getLocalName(i).equals("multiplicative")) {
                        Boolean b = Boolean.parseBoolean(attrs.getValue(i));
                        if (b) {
                            newNode.setAsMultiplication(true);
                        }
                    }
	            	
	            }
	        }
			//System.out.println(newNode);
			this.mv.getModelRequestInterface().requestAddNode(newNode);
		}
		
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Cannot handle element in XML");
		}
		
	}

	private void imageReady(Node newNode) {
		Image img=new BufferedImage(img64width, img64height, BufferedImage.TYPE_3BYTE_BGR);
		
		//byte[] srcbuf = Base64.getDecoder().decode();
		//InputStream in = new ByteArrayInputStream(imageInByte);
//		BufferedImage bImageFromConvert = ImageIO.read(in);
		
		byte[] bytearray = Base64.getDecoder().decode(this.img64data);
		
	//	((BufferedImage) img).setData(Raster.createRaster(((BufferedImage) img).getSampleModel(), 
	//			new DataBufferByte(this.img64data, this.img64data.length), new Point() ) );
		newNode.image = img;
		
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
	
}
