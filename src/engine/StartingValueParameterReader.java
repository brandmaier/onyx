/*
 * Created on 12.06.2014
 */
package engine;

import java.util.ArrayList;
import java.util.List;

import engine.ModelRunUnit.Objective;

public class StartingValueParameterReader implements ParameterReader {
    
    public OnyxModel model;
    
    public StartingValueParameterReader(OnyxModel model) {this.model = model;}
    
    public double getParameterValue(String parameterName) {return model.getStartingValue(parameterName);}
    public double[] getParameterValues() {return model.getParameter();}
    public double getStandardizedParameterValue(String parameterName) {return Double.NaN; }

    public List<String> getSortedParameterNames() {return model.getSortedParameterNames();}
    public List<String> getParameterNames() {
        String[] names = model.getParameterNames();
        ArrayList<String> erg = new ArrayList<String>();
        for (int i=0; i<names.length; i++) erg.add(names[i]);
        return erg;
    }
    @Override
    public String getName() {
        return "Starting Values";
    }
    @Override
    public String toString() { return getName();}
    public boolean isStartingParameters() {return true;}
    public String getDescription() {return "Starting value set";}
    @Override
    public double[][] getParameterCovariance() {
        // AB: really ?!  TvO: I think so... fixed starting value parameter covariances are unknown.
        return null;
    }
    @Override
    public int getParameterNameIndex(String parameterName) {
        return model.getParameterIndex(parameterName);
    }
    public String getHistoryString() {return "Starting value set";}
    @Override
    public String getShortSummary(boolean asHTML) {

        if (asHTML) {
            String params;
                
            params = "<html><h3>Starting Values</h3><br><hr><br>";
            for (String parameterName : getSortedParameterNames()) {
                
                int idx = getParameterNameIndex(parameterName); 
                params += parameterName + ":"
                        + (Math.round(getParameterValue(parameterName)*1000.0)/1000.0)
                        + "<br>";
            }
            
            params += "<br><hr></html>";

            return params;
            
        } else {
            String erg = "Starting Value set\r\n";
            erg += "Name\tEstimate\tStd. error\r\n";
            for (String parameterName:getSortedParameterNames())
                erg += parameterName+"\t"+getParameterValue(parameterName)+"\t\r\n";
            
            return erg;
        }
    }
    @Override
    public Objective getObjective() {return Objective.STARTINGVALUES;}

	@Override
	public double getFitIndex(FitIndex fitIndex) {
		return(Double.NaN);
	}
}
