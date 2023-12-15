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
 * Created on 04.03.2012
 */
package engine;

import java.util.List;

import engine.ModelRunUnit.Objective;

/**
 * Used to reduce functionality of ModelRunUnit to single parameter set reading. 
 * 
 * @author Timo
 */
public interface ParameterReader {
   
    /**
     * Returns the value of the parameter.
     * 
     * @param parameterName
     * @return
     */
    public double getParameterValue(String parameterName);

    /**
     * Returns an array with all parameter values, in the same order that parameter names are given. 
     * @return
     */
    public double[] getParameterValues();
    
    /**
     * returns an alphabetically sorted list of all parameter names
     * 
     * @return
     */
    public List<String> getSortedParameterNames();

    /**
     * returns a list of all parameter names in the same order as they appear in the model.
     * 
     * @return
     */
    public List<String> getParameterNames();
    
    /**
     * returns a string representation of the name of the object
     * @return
     */
	public String getName();

	/**
	 * Returns true if the current parameter set represents starting values
	 * @return
	 */
	public boolean isStartingParameters();
	
	/** 
	 * Returns a description of the estimate corresponding to this parameter reader if it represents an estimate. 
	 * @return
	 */
	public String getDescription();
	
	
	/**
	 *  TODO
	 */
	public double[][] getParameterCovariance();
	
	/**
	 * TODO
	 * @param parameterName
	 * @return
	 */
	 public int getParameterNameIndex(String parameterName);
	 
	 /**
	  * Returns the optimization history of this parameter Reader, if available.
	  * @return
	  */
	 public String getHistoryString();
	 
	 /**
	  * Returns a very short summary of the parameters as represented in the result drawer.
	  * @param asHTML if false, the summary is returned as text, otherwise as HTML. 
	  * @return
	  */
	 public String getShortSummary(boolean asHTML);
	
	 /**
	  * Returns the objective function descriptor of this parameter set. 
	  * @return
	  */
	 public Objective getObjective();
	 
	 
	 /**
	  * Enumeration for available fit indices
	  *
	  */
	 public enum FitIndex {X2, RMSEA, CFI};

	 /**
	  * Get fit index
	  */
	 public double getFitIndex(FitIndex fitIndex);
}
