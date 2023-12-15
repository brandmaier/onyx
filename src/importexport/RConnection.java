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

import engine.Preferences;
import gui.frames.MainFrame;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RConnection {

	public static String pathToRExecutable;
	public static File tempFile = null;
	public static void createTempScript(String content) throws Exception
	{
	        BufferedWriter writer = null;

	        try {
	            tempFile = File.createTempFile("rscript",".R");
	        } catch (Exception e) {
	            throw new Exception("Cannot open tempopary file: " + e.toString());
	        }

	        try {
	            writer = new BufferedWriter(new FileWriter(tempFile));
	            writer.write(content);
	            writer.flush();
	        } catch (Exception e) {
	            throw new Exception("Cannot write to temporary file: " + e.toString());
	        } finally {
	            try {
	                writer.close();
	            } catch (Exception einner) {
	            }
	        }
		
	}
	
	private static boolean rPathIsValid() {
	    if (pathToRExecutable==null || pathToRExecutable.length() == 0) return false;
        try {
            ProcessBuilder builder = new ProcessBuilder(RConnection.pathToRExecutable,"--version");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String err = brerr.readLine();
            String output = "";
            while (true) {
                String line = br.readLine();
                if (line != null) {
                    output+=line+"\r\n";
                } else {
                    break;
                }
            }
            System.out.println("Ausgabe der Konsole: "+output);
            if ((!output.startsWith("R version")) 
            	&& (!output.startsWith("R Under development")))
            	return false;
            return true;
            
        } catch (Exception e) {
        	e.printStackTrace();
        	return false;}
	}
	
	public static String getPathToRExecutable(Component parent) {
//	    if (pathToRExecutable == null) pathToRExecutable = "\""+Preferences.getAsString("RPath")+"\"";
	    if (pathToRExecutable == null) pathToRExecutable = Preferences.getAsString("RPath");
	    if (pathToRExecutable == null || pathToRExecutable.length() == 0 || !rPathIsValid()) {
            askForRInterpreter(parent);
	    }
        return pathToRExecutable;
	}

    public static void askForRInterpreter(Component parent) {
        //pathToRExecutable = "\""+Preferences.getAsString("RPath")+"\"";	//AB: This definitely breaks MAC and UNIX-systems 
        pathToRExecutable = Preferences.getAsString("RPath");
        System.out.println("RPath "+pathToRExecutable);
        if (rPathIsValid()) return;
//        JOptionPane.showMessageDialog(parent, "Onyx does not know where your local R installation is located. Please locate the exectuable R program (likely it is called R.exe, R, R32, or R64) on your harddrive in the following dialog." );
        JOptionPane.showMessageDialog(parent, "Please locate the exectuable R program (usually called R.exe, R, R32, or R64) on your harddrive in the following dialog." );
        
        final JFileChooser fc = new JFileChooser();
        fc.setVisible(true);
        //In response to a button click:
        int returnVal = fc.showOpenDialog(parent);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
          
          /*  if (!testRPath(file.getAbsolutePath()))
            {
                JOptionPane.showMessageDialog(null, "Invalid R interpreter! Please see the manual!");
            }*/
            
            Preferences.set("RPath", file.getAbsolutePath());
            pathToRExecutable = file.getAbsolutePath();
        } 
    	
    }

	static boolean testRPath(String pathToRExecutable) {
		try {
		//    pathToRExecutable = "\""+Preferences.getAsString("RPath")+"\"";//AB: This definitely breaks MAC and UNIX-systems 
		    pathToRExecutable = Preferences.getAsString("RPath");
			System.out.println(pathToRExecutable+" --version");
			Process p = Runtime.getRuntime().exec(new String[]{pathToRExecutable,"--version"});	
			//AB to TvO: calling exec() with a String-array should work around the spaces problem
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String output = "";
			while (true) {
			//br.readLine();
				String line = br.readLine();
				if (line != null) {
					output+=line+"\n";
				} else {
					break;
				}
			}
			
			System.out.println("Ausgabe der Konsole: "+output);
		// Read Input
			
			// send it to the XML parser
			//return(output);
		
			return true;
			
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, "Error! Invalid R interpreter! Please see the manual!");
			e.printStackTrace();
			return false;
		}
		
		
	}

	
}
