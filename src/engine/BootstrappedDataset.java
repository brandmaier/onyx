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
package engine;

import java.util.Random;

public class BootstrappedDataset extends RawDataset {

	public RawDataset source;
	
	public BootstrappedDataset(RawDataset dataset)
	{
		
		
		
		super(dataset.getNumRows(), dataset.getNumColumns());
		
		for (int i=0; i < dataset.getNames().size(); i++)
		{
			this.setColumnName(i, dataset.getNames().get(i) );
		}
		
		this.source = dataset;
		
		this.setName("Bootstrapped "+dataset.getName());
		bootstrap();
		
	}

	public void bootstrap() {
		// do the bootstrap now
		Random random = new Random();
		
		for (int i=0; i < source.getNumRows(); i++)
		{
			int idx = random.nextInt(source.getNumRows());
			for (int j=0; j < source.getNumColumns(); j++)
				this.set(i,j, source.get(idx, j));
			
		}
	}


	
}
