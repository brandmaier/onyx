package importexport;

import engine.Preferences;
import gui.Desktop;
import gui.frames.MainFrame;
import gui.views.ModelView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import scc.Tree;

public class OpenMxImport {


	private Desktop desktop;


	
	public static String readFileAsString(String filePath)
		    throws java.io.IOException{
		        StringBuffer fileData = new StringBuffer(1000);
		        BufferedReader reader = new BufferedReader(
		                new FileReader(filePath));
		        char[] buf = new char[1024];
		        int numRead=0;
		        while((numRead=reader.read(buf)) != -1){
		            String readData = String.valueOf(buf, 0, numRead);
		            fileData.append(readData);
		            buf = new char[1024];
		        }
		        reader.close();
		        return fileData.toString();
		    }
	
	private String readFileResourceAsString(String filePath)
		    throws java.io.IOException{
		System.out.println(filePath);
		        StringBuffer fileData = new StringBuffer(1000);
		        BufferedReader reader = new BufferedReader(
		                new InputStreamReader(this.getClass().getResourceAsStream(filePath)));
		        char[] buf = new char[1024];
		        int numRead=0;
		        while((numRead=reader.read(buf)) != -1){
		            String readData = String.valueOf(buf, 0, numRead);
		            fileData.append(readData);
		            buf = new char[1024];
		        }
		        reader.close();
		        return fileData.toString();
		    }


	public OpenMxImport(Desktop desktop)
	{
		this.desktop = desktop;
	}
	
	public String getModelAsXml(String openmxmodel) throws Exception
	{
		String curDir = System.getProperty("user.dir");
	//	System.out.println("Dir: "+curDir);

		
		boolean ok = RConnection.testRPath(RConnection.getPathToRExecutable(this.desktop) );
		while(!ok) {
			//ok = 
			if (!ok) {
				// invalidate old path to R 
				RConnection.askForRInterpreter(this.desktop);
			}
			ok = RConnection.testRPath(RConnection.pathToRExecutable);
			if (!ok) {
				Object[] options = {"Try again","Cancel"};
				// ask to abort or try once again?
				int n = JOptionPane.showOptionDialog(
						this.desktop,
						
						"The chosen R interpreter is invalid. Do you like to choose the location of the R interpreter again?"
						,"Problem with R"
						,JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null, options, options[0]);
						//paramComponent, paramObject1, paramString, paramInt1, paramInt2, paramIcon, paramArrayOfObject, paramObject2)
				if (n==1) return null;
			}
		
		}
		
		// assemble code

//		String export = readFileAsString(curDir+"/src/xmlexport.R");
		String export = readFileResourceAsString("/xmlexport.R");

		openmxmodel = openmxmodel.replaceAll("\r", "\n");
		
		String tempScriptContent = openmxmodel+"\n"+export;
		RConnection.createTempScript( tempScriptContent );
		
		
		// MAC and Unix it is likey:
		
	
		/*
		 *    --slave
              Make R run as quietly as possible
		
			  -f
			  run file as input
		 */
		String Roptions = " --slave -f "+RConnection.tempFile.getAbsolutePath();
		//String Roptions = " --version";
		
		// Create a new directory in current directory
	//	String folderName = "tempR"+System.currentTimeMillis();
//		boolean success = (new File(curDir+"/"+folderName)).mkdir();
		
		// Run R process
		String output = "";
		try {
			Process p = Runtime.getRuntime().exec(RConnection.getPathToRExecutable(this.desktop)+" "+Roptions);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader br_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			while (true) {
			//br.readLine();
				String line = br.readLine();
				if (line != null) {
					output+=line;//+"\n";
				} else {
					break;
				}
			}
			
			// Filtern
			
			Matcher matcher = Pattern.compile( "<\\?xml.*" ).matcher( output ); 
			if (matcher.find()) {
				output = matcher.group();
			} else {
				System.err.println("Problem in finding an XML representation within console output!");
				
				String err = "";
				while (true) {
					//br.readLine();
						String line = br_err.readLine();
						if (line != null) {
							err+=line+"\n";
						} else {
							break;
						}
					}
				
				//System.out.println(output+"\n"+err);
				JOptionPane.showMessageDialog(this.desktop, "R reported the following error in your script:\n "+err, "Import error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			System.out.println("Ausgabe der Konsole: "+output);
		// Read Input
			
			// send it to the XML parser
			return(output);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// tidy up
		//success = (new File(curDir+"/"+folderName)).delete();
		
		return(null);
	}
	
	public ModelView loadModel() 
	{
		return loadModel(null);
	}
	
	public ModelView loadModelFromString(String openMxScript) {
	    
	    RConnection.getPathToRExecutable(this.desktop);
	    try {
            String xml = getModelAsXml(openMxScript);
            
            if (xml == null) return null;
            
            System.out.println(xml);
            
            // filter out multiple models, if any
           List<XMLModel> models = detectMultipleModels(xml);
           
           if (models.size() > 1) {
               ModelSelectFrame msf = new ModelSelectFrame(null, models);
               msf.toFront();
               models = msf.getSelectedModels();
           }
           
           System.out.println("Start import");
           
           for (XMLModel model : models) {
               
           System.out.println("Importing "+model.name);
               
            ModelView modelView = desktop.loadModel(model.xml);
           
            if (modelView == null) {
                System.err.println("An error occured during import of "+model.name);
                return null;
            }
            
            Tree tree = new Tree(modelView.getGraph(), true);
            tree.layout();
            
//          System.err.println("T")

            //TODO: allow returning a list of models!
            
            return modelView;
           }
           
           
            
       } catch (Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }  
       return null;
	}
	

	public ModelView loadModel(File loadFile)
	{
		
		
		//pathToRExecutable = "/usr/bin/R";
		RConnection.getPathToRExecutable(this.desktop);

		if (loadFile == null) {
			File dir = new File((String)Preferences.getAsString("DefaultWorkingPath"));
			
			final JFileChooser fc = new JFileChooser(dir);
			
			//In response to a button click:
	
			int returnVal = fc.showOpenDialog(null);
			
				if (returnVal == JFileChooser.APPROVE_OPTION) {
	             loadFile = fc.getSelectedFile();
	            
	             Preferences.set("DefaultWorkingPath", loadFile.getParentFile().getAbsolutePath());
		        
			 	}
		}
		
		String modelString;
		try {
			modelString = readFileAsString(loadFile.getAbsolutePath());
			
			return loadModelFromString(modelString); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	



	public static void main(String[] args) throws IOException
	{
		
		String curDir = System.getProperty("user.dir");

		String modelstring = readFileAsString(curDir+"/src/wisc-example-2.R");
		
		try {
			OpenMxImport omxImport = new OpenMxImport(null);
			RConnection.pathToRExecutable = "/usr/bin/R";
			String xml = omxImport.getModelAsXml(modelstring);
			List<XMLModel> models = omxImport.detectMultipleModels(xml);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<XMLModel> detectMultipleModels(String xml) {
		
		List<XMLModel> result = new ArrayList<XMLModel>();
		
		Matcher matcher = Pattern.compile( "<model.*?</model>" ).matcher( xml ); 
		while (matcher.find()) {
			String output = matcher.group();
			//System.out.println(output);
			Matcher nameMatcher = Pattern.compile( "name=\"(.*?)\"" ).matcher( output ); 
			nameMatcher.find();
			String name = nameMatcher.group();
			//System.out.println("Match: "+name+":"+output);
			result.add( new XMLModel(name, output) );
		}
		
		return(result);
	}
	
	public class XMLModel
	{
		public XMLModel(String name, String xml) {
			super();
			this.name = name;
			this.xml = xml;
		}
		String name;
		String xml;
	}
}
