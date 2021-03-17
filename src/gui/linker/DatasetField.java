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