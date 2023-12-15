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

import java.util.List;

import importexport.LavaanExport;
import importexport.RExport;
import importexport.SemExport;

public class LavaanRunUnit extends OpenMxRunUnit {

    public LavaanExport exporter;
    
    public String getAgentLabel() {return "Lavaan";}

    protected RExport getExporter() {return (exporter = new LavaanExport(modelView));}

    protected String getOutputCommands() {
        String parameterNameList = "c(";
        List<String> parNames = exporter.getParameterSlotNames();
        for (String name: parNames) parameterNameList += "\""+exporter.convert(name)+"\",";
        parameterNameList = parameterNameList.substring(0, parameterNameList.length()-1) + ")";
        
        return "c(\"Onyx input\","
                +"paste(\"iterations=\",result@Fit@iterations,sep=\"\"),"
                +"paste("+parameterNameList+",result@Fit@est,sep=\"=\"),"
                +"\"Onyx input end\")";
    }
    
}
