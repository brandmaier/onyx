package engine;

import java.io.File;
import java.util.List;
import java.util.Vector;

public class CovarianceDataset extends Dataset {

	int sampleSize = 0;
	int anzVar;
	//int numColumns;
	
	double[][] cov;
	double[] mean;
	
	public final String CMDSAMPLESIZE = "SAMPLE SIZE",
	                    CMDCOVARIANCE = "COVARIANCE",
	                    CMDMEAN = "MEAN",
	                    CMDLABELS = "OBSERVED VARIABLES";
	
	public CovarianceDataset() {}
	
	public CovarianceDataset(String data) {
		
	    String upData = data.toUpperCase();
        int ixCovariance = upData.indexOf(CMDCOVARIANCE);
	    int ixSamplesize = upData.indexOf(CMDSAMPLESIZE);
        int ixMean = upData.indexOf(CMDMEAN);
        int ixLabels = upData.indexOf(CMDLABELS);
	    
	    // if the COVARIANCE command is found, long format is assumed. 
	    if (ixCovariance != -1) {
	        Vector<double[]> covLines = new Vector<double[]>();
	        int lineIx = data.indexOf('\n', ixCovariance); if (lineIx == -1) lineIx = data.length();
	        double[] line = Statik.getNumericals(data, ixCovariance, lineIx);
	        anzVar = line.length;
	        int start;
	        boolean seemsIncreasing = (anzVar==0 || anzVar==1);
	        do {
	            start = lineIx + 1; 
                lineIx = data.indexOf('\n', start); if (lineIx == -1) lineIx = data.length();
	            if (anzVar > 0) covLines.add(line);
	            line = Statik.getNumericals(data, start, lineIx);
	            if (seemsIncreasing) {
	                if (line.length > 0 && line.length != anzVar+1) seemsIncreasing = false;
	                if (!seemsIncreasing && anzVar == 0) anzVar = line.length;
	                if (line.length == anzVar +1) anzVar++;
	            }
	        } while (line.length == anzVar);
	        if (covLines.size() != anzVar) throw new RuntimeException("Covariance data set has a format error.");
	        cov = new double[anzVar][anzVar]; 
	        for (int i=0; i<anzVar; i++) for (int j=0; j<=i; j++) cov[i][j] = cov[j][i] = covLines.elementAt(i)[j];
	        mean = new double[anzVar]; 
	        sampleSize = 1;
            columnNames = new Vector<String>(anzVar);
            for (int i=0; i<anzVar; i++) columnNames.add("Unnamed "+(i+1)); 
	        
	        if (ixSamplesize != -1) {
	            line = Statik.getNumericals(data, ixSamplesize, '\n', true);
	            if (line.length >= 1) sampleSize = (int)Math.round(line[0]);
	        }
	        
	        if (ixMean != -1) {
	            line = Statik.getNumericals(data, ixMean, '\n', true);
	            if (line.length >= anzVar) for (int i=0; i<anzVar; i++) mean[i] = (int)Math.round(line[i]);
	        }
	        
	        if (ixLabels != -1) {
                lineIx = data.indexOf('\n', ixLabels); if (lineIx == -1) lineIx = data.length();
	            String names = data.substring(ixLabels + CMDLABELS.length(), lineIx).trim();
	            if (names.length()==0)
	                names = data.substring(lineIx+1, data.indexOf('\n', lineIx+1));
                String[] labels = names.trim().split("\t");
	            
//	            lineIx = data.indexOf('\n', ixLabels); if (lineIx == -1) lineIx = data.length();
//	            int tabIx  = data.indexOf('\t', ixLabels); if (tabIx == -1) tabIx = data.length();
//	            // AB: if there is a tab before the first line end, start searching for names on the next line, otherwise start on the very same line
//	            if (lineIx <= tabIx) {start = lineIx+1; lineIx = data.indexOf('\n', lineIx+1);}
//	            else {start = ixLabels + CMDLABELS.length();}
//	            String[] labels = data.substring(start, lineIx).trim().split("\t");
	            if (labels.length >= anzVar)
	                for (int i=0; i<anzVar; i++) columnNames.set(i, labels[i].trim());
	        }
	        
	    } else {
	        throw new RuntimeException("Covariance data set has a format error.");
	    }
	    
	    /*
		String[] lines = data.split("\n");
    	int nums = lines.length;
    	
    	cov = new double[nums][nums];
    	mean = new double[nums];
    	sampleSize = 1;
    	
    	columnNames = new Vector<String>(nums);
    	
    	int voffset = 0;
    	try {
    		String[] tokens = lines[0].split("\t");
    		for (String tok : tokens) Double.parseDouble(tok);	
    	} catch (Exception e) {
    		
    		voffset=1;
    		
    		String[] tokens = lines[0].split("\t");
    		for (int i=0; i < tokens.length; i++) this.setColumnName(i, tokens[i]);
    	}
    	
    	
    	for (int i=0; i < (lines.length-voffset);i++) {
    		String[] tokens = lines[i+voffset].split("\t");
    		for (int j=0; j < tokens.length; j++) {
    			cov[i][j] = Double.parseDouble(tokens[j]);
    			cov[j][i] = cov[i][j];
    			System.out.println("COV "+i+ ","+j+"="+cov[i][j]);
    		}
    	}
    	
    	
    	if (columnNames.size()==0) {
    		columnNames = new Vector<String>(cov.length);
    		for (int i=0; i < cov.length; i++) {
    			columnNames.add( "Unnamed "+(i+1));
    			System.out.println("Colname"+i+"="+getColumnName(i));
    		}
    	}
		*/
	}
	
    public CovarianceDataset(int sampleSize, double[] mean, double[][] cov, List<String>columnNames) {
        this.sampleSize = sampleSize; this.mean = mean; this.cov = cov; this.columnNames = columnNames;
    }
    
	public double[][] getCovarianceMatrix() { return cov; }
	public double[] getMean() { return mean; }

	public int getSampleSize() {
		return sampleSize;
	}
	
	public int getNumColumns() {
		return cov.length;
	}
	
	public double getColumnStandardDeviation(int i)
	{
		return Math.sqrt(cov[i][i]);
	}

	public boolean isPositiveDefinite() {
	    return Statik.isPositiveDefinite(cov);
	}
	
	   @Deprecated // should use CombinedDataset.getDataDistribution.
	 public String getDataDistribution(int[] columnIDs, String[] varNames) {
		
		   return ("NOT IMPLEMENTED YET");
	 }
	 
	 public String getColumnTooltip(int columnId) {
		 String str = "";
		 if (this.mean != null) str+="Mean: " +this.mean[columnId]+"\r\n";
		 if (this.cov != null) str+="Variance: "+this.cov[columnId][columnId];
		 return(str);
	 }
	 
		public boolean hasIdColumn()
		{
			return false;
		}
		
		public int getIdColumn() {
			return -1;
		}

		public void setData(double[][] data, List<String> columnNames) {
			this.cov = data;
			this.columnNames = columnNames;
		//	this.numColumns = columnNames.size();
			
			this.mean = new double[this.getNumColumns()];
			for (int i=0; i < mean.length; i++) this.mean[i]=Double.NaN;
			
			
		}
	
		/** 
		 * saves the dataset. MissingIndicator is ignored.
		 */
		public void save(File f, String missingIndicator) {
		    // TODO not yet implemented
		}
}
