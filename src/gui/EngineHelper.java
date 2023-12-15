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
package gui;

import engine.OnyxModel;
import engine.ParameterReader;
import engine.Statik;
import gui.graph.Node;
import gui.views.ModelView;

public class EngineHelper {

	public static double getModelCovariance(ModelView modelView, String vname)
	{
		int idx = -1;
		OnyxModel model = modelView.getModelRequestInterface().getModel();
        for (int i=0; i<model.anzFac; i++) if ( model.variableNames[i].equals(vname)) {idx=i; break;}
		
        if (idx==-1) return Double.NaN;
        
        double[][] cov = getCovarianceMatrix(modelView, false);

        return cov[idx][idx];
	}
	
	public static double[][] getCovarianceMatrix(ModelView modelView, boolean observed)
	{
		OnyxModel model = modelView.getModelRequestInterface().getModel();
		//model.get
		
		int k = model.symVal.length;
		
		if (k==0) return null;
		
		double[][] I = new double[k][k];
		for (int i=0; i < k; i++) I[i][i] = 1;
		
		// Statik.invert
		// Statik.transpose
		// Statik.multiply
		double[][] IminAinv = Statik.invert(Statik.subtract(I, model.asyVal));
		
		double[][] IminAinvT = Statik.transpose(IminAinv);
		
		int obs = 0;
		for (Node node : modelView.getGraph().getNodes()) {
			if (node.isObserved()) obs++;
		}
		
		double[][] F = new double[obs][k];
		for (int i=0; i < model.filter.length; i++) {
			int idx = model.filter[i];
			F[i][idx] = 1;
		}
//		model.getPa
		
		try {
		ParameterReader showingEstimate = modelView.getShowingEstimate();
		if (showingEstimate == null) throw new NoShowingEstimateException();
		model.setParameter(showingEstimate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double[][] cov;
		if (observed) {
		cov = 
				Statik.multiply(
				Statik.multiply(
				F,
						//Statik.multiply( Statik.multiply( F,
				Statik.multiply( Statik.multiply(IminAinv, model.symVal), IminAinvT)
			//	, Statik.transpose(F));
				),
				Statik.transpose(F))
		;
		} else {
			cov = 				Statik.multiply( Statik.multiply(IminAinv, model.symVal), IminAinvT)
					;
		}
		
		return cov;
	}

	
}
