package importexport;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import engine.Dataset;
import gui.Desktop;
import gui.frames.MainFrame;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.DataView;
import gui.views.ModelView;
import gui.views.ScriptView;
import gui.views.View;
import gui.views.ViewConnection;
import importexport.filters.NYXFileFilter;

/**
 * SaveZIP encapsulates the logic for saving in the Onyx compact ZIP format that
 * allows storing one or multiple models along with data and other types of
 * files, such as images
 * 
 * @author brandmaier
 *
 */
public class SaveZIP extends Export {

	boolean withData = false;
	private List<ModelView> modelViews = new ArrayList<ModelView>();
	private List<Dataset> datasets = new ArrayList<Dataset>();
	private List<DataView> dataViews;
	//private boolean workspace = false;
	private Desktop desktop;
	
	public SaveZIP(ModelView modelView)
	{
		this(modelView, false);
	}
	
	public SaveZIP(ModelView modelView, boolean includeData) {
		super(modelView, new NYXFileFilter(),new String[] {"nyx"});
		this.datasets = LinkHandler.getGlobalLinkhandler().getAllConnectedDatasets(modelView);
		this.modelViews.add( modelView );
	}
	
	public SaveZIP(List<ModelView> modelViews, List<DataView> dataViews, Desktop desktop) {

		//this(modelViews, null);
		super(null, new NYXFileFilter(),new String[] {"nyx"});
		// collect datasets
		this.desktop = desktop;
		this.modelViews = modelViews;
		this.dataViews = dataViews;
		datasets = new ArrayList<Dataset>();
		for (DataView dv : dataViews) {
			datasets.add(dv.getDataset());
		}
		
		//this.workspace  = true;
		
	}
	
	public SaveZIP(List<ModelView> modelViews, List<Dataset> datasets) {
		super(null, new NYXFileFilter(),new String[] {"nyx"});
		
		this.modelViews = modelViews;
		this.datasets = datasets;
		//this.withData = b;
	}

	/*
	 * save in new *.nyx file format, which is a ZIP container format
	 */
	public void export(File f) {
		
		try {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
		
		// create model files
		int model_counter = 1;
		for (ModelView modelView : modelViews) {
						
	
			ZipEntry e = new ZipEntry("model"+model_counter+".xml");
			out.putNextEntry(e);
			XMLExport exp = new XMLExport(modelView);
			String sb = exp.exportString();
			
			byte[] data = sb.getBytes();
			out.write(data, 0, data.length);
			out.closeEntry();
			
			// create ZIP entries for all images in nodes
			List<Node> nodes = modelView.getGraph().getNodes();
			for (Node node : nodes){
				if (node.image != null) {
					e = new ZipEntry("model"+model_counter+"_image"+node.getId()+".png");
					out.putNextEntry(e);				
					 ImageIO.write((BufferedImage)node.image, "png", out);
					out.closeEntry();

				}
			}
			
			model_counter = model_counter + 1;
		}
		

		
		// create ZIP entries for all dataset
		//List<Dataset> datasets = 
		int dcount=1;
		for (Dataset ds : datasets) {
			ZipEntry e = new ZipEntry("dataset"+dcount+"_"+ds.getName()+".csv");
			out.putNextEntry(e);
			
			CSVExport dexp = new CSVExport(ds);
			
			// silly temporary solution
			File tempf = File.createTempFile("onyx","tmp");
			dexp.export(tempf);
			// read from file and pipe into out
			FileInputStream fis = new FileInputStream(tempf);
			byte[] buffer = new byte[1024*16];
			int len;
			while ((len = fis.read(buffer)) != -1) {
			    out.write(buffer, 0, len);
			}
			fis.close();
			tempf.delete();
			
			
			
			
			out.closeEntry();
			dcount=dcount+1;
		}
		
		// workspace file stores information about how models and dataset
		// are arranged
		if (desktop != null) {
			ZipEntry e = new ZipEntry("workspace.xml");
			out.putNextEntry(e);
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			
			Element rootElement =  doc.createElement("workspace");
			
			doc.appendChild(rootElement);
			
			HashMap<View, Integer> viewIds = new HashMap<View,Integer>();
			int vid_count = 0;
			
			// write all View placements in here
			for (View view : desktop.getViews()) {
				vid_count=vid_count+1;
				Element nodeElement = doc.createElement("view");
				rootElement.appendChild(nodeElement);
				nodeElement.setAttribute("x", Integer.toString(view.getX()));
				nodeElement.setAttribute("y", Integer.toString(view.getY()));
				nodeElement.setAttribute("width", Integer.toString(view.getWidth()));
				nodeElement.setAttribute("height", Integer.toString(view.getHeight()));
				nodeElement.setAttribute("name", view.getName());
				nodeElement.setAttribute("class", view.getClass().toString());
				nodeElement.setAttribute("id", Integer.toString(vid_count));
				
				if (view instanceof ScriptView) {
					nodeElement.setAttribute("modelView", ((ScriptView)view).getName() );
				}
				
			}
			// write LR edges in here
			for (ViewConnection vc : desktop.getViewConnections()) {
				Element nodeElement = doc.createElement("connection");
				nodeElement.setAttribute("from", Integer.toString(viewIds.get(vc.getFrom()))); 
				nodeElement.setAttribute("to", Integer.toString(viewIds.get(vc.getTo()))); 
			
				rootElement.appendChild(nodeElement);
			}
			
			// write all connections between datasets and models from LinkHandler
			LinkHandler lh = Desktop.getLinkHandler();
			Set<DatasetField> keyset = lh.dToG.keySet();
			for (DatasetField key : keyset) {
				List<VariableContainer> field = lh.dToG.get(key);
				for (VariableContainer vc : field) {
					// TODO write out connection
					//key.dataset
					vc.getUniqueName();
				}
				
			}
			
			// write contents to file
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, MainFrame.CHAR_ENCODING);
		
			DOMSource source = new DOMSource(doc);
			
			Result result = new StreamResult(new OutputStreamWriter(out,
					"utf-8"));
			
			transformer.transform(source, result);
			
			out.closeEntry();
		}
		
		out.close();
		
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: add useful message
		}
		
		
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
}
