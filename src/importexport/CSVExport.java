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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import engine.CovarianceDataset;
import engine.Dataset;
import engine.RawDataset;
import engine.Statik;

public class CSVExport {

	private Dataset dataset;
	
	char separator;

	public CSVExport(Dataset dataset)
	{
		this(dataset,'\t');
	}
	
	public CSVExport(Dataset dataset, char delimiter)
	{
		this.dataset = dataset;
		this.separator = delimiter;
	}
	
	public void export(File file) {

		if (dataset instanceof RawDataset) {
			String[] colNames = dataset.getColumnNames();
			String header = "";
			for (int i = 0; i < colNames.length; i++)
				header += colNames[i]
						+ (i == colNames.length - 1 ? "" : separator);
			Statik.writeMatrix(((RawDataset) dataset).getData(),
					file, separator, header);
		} else if (dataset instanceof CovarianceDataset) {
		
			try {
			
			String[] colNames = dataset.getColumnNames();
			String header = "";
			for (int i = 0; i < colNames.length; i++)
				header += colNames[i]
						+ (i == colNames.length - 1 ? "" : separator);
			
			BufferedWriter w = new BufferedWriter(new FileWriter(file));
			
			w.write(header+"\r\n");
			
			
			
			CovarianceDataset cd = (CovarianceDataset)dataset;
			
			double[][] matrix = cd.getCovarianceMatrix();
			w.write("Covariance Matrix\n");
			for (int i=0; i<matrix.length; i++)
            {
                for (int j=0; j<=i; j++)
                    w.write(""+matrix[i][j]+(j==matrix[i].length-1?"":separator));
                w.write("\r\n");
            }
			
			//if (cd.hasMean()) {
				double[] matrix2 = cd.getMean();
				w.write("Means\n");
				for (int j=0; j<matrix2.length; j++)
                    w.write(""+matrix2[j]+(j==matrix2.length-1?"":separator));
                w.write("\r\n");
			//}
			
			w.flush();
            w.close();
            
            

			} catch (Exception e) {
				e.printStackTrace();	
			}
            
		} else {
			JOptionPane
			.showMessageDialog(null,
					"Writing this type of dataset is not implemented yet!");
		}
		
		
	}
}
