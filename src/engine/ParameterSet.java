/*
 * Created on 02.03.2012
 */
package engine;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * DEPRECATED
 * @deprecated
 * @author Timo
 */
public class ParameterSet {

    Hashtable<String,Double> values;
    
    public ParameterSet() {
        values = new Hashtable<String,Double>();
    }

    public ParameterSet(String[] parameterNames, double[] values) {
        this.values = new Hashtable<String,Double>();
        setParameter(parameterNames, values);
    }
    
    public double getParameter(String parameterName) {
        Double d = values.get(parameterName);
        return d.doubleValue();
    }
    
    public void setParameter(String parameterName, double value) {
        values.put(parameterName, new Double(value));
    }
    
    public double[] getParameter(String[] parameterNames) {
        double[] erg = new double[parameterNames.length];
        for (int i=0; i<erg.length; i++) erg[i] = getParameter(parameterNames[i]);
        return erg;
    }
    
    public void setParameter(String[] parameterNames, double[] values) {
        for (int i=0; i<parameterNames.length; i++) setParameter(parameterNames[i], values[i]);
    }
    
    public int getLength() {return values.size();}
    
    public List<String> getSortedParameterNames()
    {
    	List<String> list = Collections.list(values.keys());
    	Collections.sort(list);
    	return(list);
    }
}
