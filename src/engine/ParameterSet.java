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
