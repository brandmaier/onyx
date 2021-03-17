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
