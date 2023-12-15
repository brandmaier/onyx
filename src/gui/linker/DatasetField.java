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
package gui.linker;

import engine.Dataset;
import engine.RawDataset;

public class DatasetField
{
	public Dataset dataset;
	public int columnId;
	public DatasetField(Dataset dataset, int columnId) {
		super();
		this.dataset = dataset;
		this.columnId = columnId;
	}
	
	public int hashCode()
	{
		return dataset.hashCode()+this.columnId;	//TODO: is this a good idea?
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof DatasetField) {
		DatasetField b = (DatasetField)obj;
		return (this.dataset == b.dataset && this.columnId == b.columnId);
		} else {
			return false;
		}
	}
	
	public String toString()
	{
		return this.dataset.getName()+":"+this.columnId;
	}
	
}
