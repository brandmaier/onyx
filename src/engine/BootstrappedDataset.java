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
