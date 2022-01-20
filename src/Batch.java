import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import importexport.EPSExport;
import importexport.Export;
import importexport.JPEGExport;
import importexport.LavaanExport;
import importexport.MplusExport;
import importexport.OpenMxExport;
import importexport.PDFExport;
import importexport.PNGExport;
import gui.Desktop;
import gui.Desktop.ImportType;
import gui.graph.Edge;
import gui.views.ModelView;


public class Batch {

	public static void convert(Arguments arguments) {
		
		if (!arguments.containsKey("--input-file")) {
			System.out.println("Please specify an input file (Onyx XML) using '--input-file' argument!");
			System.exit(0);
		}
		
		if (!arguments.containsKey("--output-file")) {
			System.out.println("Please specify an output file using '--output-file' argument!");
			System.exit(0);
		}
		
		if (!arguments.containsKey("--output-filetype")) {
			System.out.println("Please specify an output filetype (pdf/eps/png/jpg) using '--output-filetype' argument!");
			System.exit(0);
		}
		
		File file = new File(arguments.get("--input-file"));
		
		//BufferedReader in;
		try {
			//in = new BufferedReader(new FileReader(file));
			// determine input type
			/*  String firstLines = ""; 
	          in.mark(100000);
	          for (int i=0; i<10; i++) firstLines += in.readLine()+"\r\n";
	          in.reset();
	          ImportType type = gui.Desktop.determineType(firstLines);
			*/

	          // load model
	          Desktop desktop = new Desktop();  
	//          desktop.importFromBuffer(in, file);  
	          desktop.importFromFile(file, file.getName(),0,0);
	          

	          
	          // grab modelview
	          ModelView view = (ModelView) desktop.getViews().get(1);	//0=ParameterDrawer
	          
			/*	for (Edge edge : view.getGraph().getEdges()) {
					view.getGraph().cleverEdgeLabelLayout(edge);
				}
	          */
	          
	          // and convert
	           
	          Export exporter;
	          if (arguments.get("--output-filetype").equals("mplus")) {
	        	  exporter = new MplusExport(view) ; 
	          } else if (arguments.get("--output-filetype").equals("lavaan")) {
	        	  exporter = new LavaanExport(view);
	          }  else if (arguments.get("--output-filetype").equals("pdf")) {
	        	  exporter = new PDFExport(view); 
	          }  else if (arguments.get("--output-filetype").equals("jpg")) {
	        	  exporter = new JPEGExport(view, true); 
	          }  else if (arguments.get("--output-filetype").equals("eps")) {
	        	  exporter = new EPSExport(view); 
	          } else if (arguments.get("--output-filetype").equals("png")){
	        	  exporter = new PNGExport(view);
	          } else {
	        	  
	        	  exporter = new OpenMxExport(view);
	          }
	          exporter.setUseStartingValues(false);
	          exporter.export(arguments.get("--output-file"));
	          // output
	          
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

          
	}
	
}
