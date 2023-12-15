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
package engine.externalRunner;

import importexport.OpenMxExport;
import importexport.RConnection;
import importexport.RExport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import engine.backend.Model;
import gui.graph.Graph;

public class OpenMxRunUnit extends ExternalRunUnit {
    
    // starts with -1
    public final static String[] openMxStatusMessage = new String[]{
        "The optimizer got stuck in  a location where the objective function could not be calculated.",
        "Optimization successful.",
        "An optimal solution was found, but the sequence of iterates did not converge (Mx status GREEN).",
        "Optimization parameter bounds could not be satisfied.",
        "Optimization parameter constraints could not be satisfied.",
        "The iteration limit was reached (Mx status BLUE).",
        "",
        "Optimallity conditions could not be reached (Mx status RED)",
        "",
        "",
        ""};
    
    public int status;
    
    public String getAgentLabel() {return "OpenMx";}

    protected RExport getExporter() {return new OpenMxExport(modelView);}

    protected String getOutputCommands() {
        return "c(\"Onyx input\","
                +"paste(\"iterations=\",result@output$iterations,sep=\"\"),"
                +"paste(\"status=\",result@output$status[1],sep=\"\"),"
                +"paste(names(result@output$estimate),result@output$estimate,sep=\"=\"),"
                +"\"Onyx input end\")";
    }
    
    /**
     * Parses the textual output returned by R. If needed, should be overwritten for other R runners.
     * @param output
     */
    protected void parseResult(BufferedReader rOutput) throws IOException {
        steps = 0; status = 999;
        RExport exporter = getExporter();
        boolean readingOnyxInput = false;
        String debug = "";
        while (true) {
            String line = rOutput.readLine();
            if (line != null) {
                debug += line + "\r\n";
                if (line.contains("Onyx input")) readingOnyxInput = true;
                if (readingOnyxInput) {
                    String[] args = line.split("\"");
                    for (int i=0; i<args.length; i++) if (args[i].contains("=")) {
                        String[] nameAndValue = args[i].split("=");
                        if (nameAndValue[0].equals("iterations")) try {steps = Integer.parseInt(nameAndValue[1]);} catch (Exception e) {}
                        if (nameAndValue[0].equals("status")) try {status = Integer.parseInt(nameAndValue[1]);} catch (Exception e) {}
                        for (int j=0; j<parameterNames.length; j++)  {
                        	
                        	if (nameAndValue[0].equals(exporter.convert(parameterNames[j]))) 
                        		try {
                        			position[j] = Double.parseDouble(nameAndValue[1]);
                        		} catch (Exception e) {}
                        }
                    }
                    if (line.contains("Onyx input end")) readingOnyxInput = false;
                }
            } else {
                break;
            }
        }
        if (status == 999) agentStatus = AgentStatus.FAIL;
    }
    
    @Override
    protected void makeOutsideCall() {
    	
    	try {
            RExport exporter = getExporter();
            String tempScriptContent = exporter.createModelSpec(modelView, "OpenMx runner model", true);

            String dataFile = createTemporaryDataFile(exporter.getVariableMapping());
            
            tempScriptContent = tempScriptContent.replace(exporter.DATAFILENAME, "\""+dataFile+"\"");
            tempScriptContent += getOutputCommands();
    		
        	RConnection.createTempScript( tempScriptContent );
    		
        	System.out.println("TEMP script contents");
    		System.out.println(tempScriptContent);
        	System.out.println("--");
        	
    		// run script
//    		String Roptions = " --slave -f \""+RConnection.tempFile.getAbsolutePath()+"\"";
//            ProcessBuilder builder = new ProcessBuilder(RConnection.pathToRExecutable+Roptions);
        	String quote = "\"";
        	quote = "";
            ProcessBuilder builder = new ProcessBuilder(RConnection.getPathToRExecutable(null),"--slave","-f",quote+
            		RConnection.tempFile.getAbsolutePath()+quote);
  
    		builder.redirectErrorStream(true);
    		Process p = builder.start();
    		
    		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            parseResult(br);
//    		String output = "";
//    		while (true) {
//    			String line = br.readLine();
//    			if (line != null) {
//    				output += line+"\r\n";
//    			} else {
//    				break;
//    			}
//    		}
    		boolean success = (status == 0 || status == 1);
    		for (int i=0; i<anzPar; i++) if (Double.isNaN(position[i])) success = false;
    		agentStatus = (success?AgentStatus.SUCCESS:AgentStatus.FAIL);
    		if (status == 999) {agentMessage = "R code execution failed.";}
    		else agentMessage = openMxStatusMessage[status+1];
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public String getMissingIndicator() {return "NA";}

}
