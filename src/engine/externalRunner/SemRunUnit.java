package engine.externalRunner;

import java.util.HashMap;

import gui.graph.VariableContainer;
import importexport.RExport;
import importexport.SemExport;

public class SemRunUnit extends OpenMxRunUnit {

    SemExport exporter = null;;
    
    public String getAgentLabel() {return "sem package";}

    protected RExport getExporter() {
        if (exporter==null) exporter = new SemExport(modelView);
        return exporter;
    }
    
    @Override
    public String createTemporaryDataFile(HashMap<VariableContainer, String> nameMapping) {
        String erg = super.createTemporaryDataFile(nameMapping);
        if (modelView.getCombinedDataset().hasMissingness) exporter.useFIML = true;
        return erg;
    }

    protected String getOutputCommands() {
        return "c(\"Onyx input\","
                +"paste(\"iterations=\",result$iterations,sep=\"\"),"
                +"paste(names(result$coeff),result$coeff,sep=\"=\"),"
                +"\"Onyx input end\")";
    }
    
}
