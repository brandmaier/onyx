package engine;


import engine.backend.Model;
import gui.Desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class RawDataset extends Dataset {


	protected double[][] data;
	private int numRows;
	private int numColumns;
	
	// centralizationMeans, if not null, contains values that were subtracted from the raw data at a potential centralization process.
	private double[] centralizationMeans;
	
	private int idColumn = -1;
	
	public void setData(double[][] data, List<String> colNames) {
		this.data = data;
		this.numRows = data.length;
		this.numColumns = colNames.size();
		this.columnNames = colNames;
		this.centralizationMeans = new double[numColumns];
	}
	
	public void setData(double[][] data, List<String> colNames, double[] centralizationMeans) {
	    this.setData(data, colNames);
	    Statik.copy(centralizationMeans, this.centralizationMeans);
	}

	public void setIdColumn(int id)
	{
		assert(id > 0);
		assert(id < numColumns);
		this.idColumn = id;

		Desktop.getLinkHandler().notifyLinkedModels(this);
	}
	
	public boolean hasIdColumn()
	{
		return idColumn != -1;
	}
	
	public void removeIdColumn()
	{
		idColumn = -1;
		Desktop.getLinkHandler().notifyLinkedModels(this);
	}
	
	
	
	public RawDataset()
	{
		this.numRows = 0;
		this.numColumns = 0;
		this.columnNames = new ArrayList<String>();
		this.centralizationMeans = new double[0];
		
	}
	
	public RawDataset(int numRows, int numColumns)
	{
		this();
		this.data = new double[numRows][numColumns];
		this.numRows = numRows;
		this.numColumns = numColumns;
		for (int i=0; i < numColumns; i++) this.columnNames.add("Unnamed"+Integer.toString(i+1));
		centralizationMeans = new double[numColumns];
	}

	public RawDataset(double[][] data) {this(data, null, null);}
	public RawDataset(double[][] data, double[] centralizationMeans) {this(data, null, centralizationMeans);}
	public RawDataset(double[][] data, List<String> names) {this(data, names, null);}
	public RawDataset(double[][] data, List<String> names, double[] centralizationMeans)
	{
		this();
		this.data = data;
		this.numRows = data.length;
		this.numColumns =data[0].length;
		if (names != null) this.columnNames = names; else for (int i=0; i < numColumns; i++) this.columnNames.add("Unnamed"+Integer.toString(i+1));
        if (centralizationMeans == null) this.centralizationMeans = new double[numColumns]; else this.centralizationMeans = centralizationMeans;
	}

	@Deprecated // use Dataset.createDatasetFromString
	public RawDataset(String table, String name) {
	    this();
	    setName(name);
	    this.setData(table);
	}
	
    @Deprecated // use Dataset.createDatasetFromReader
	public RawDataset(BufferedReader table, String name) {
	    this();
	    setName(name);
	    this.setData(table);
	}
	

	
	public int getNumRows() {
		return numRows;
	}
	
	public int getNumColumns() {
		return numColumns;
	}
	
	public double get(int x, int y)
	{
		assert(x>=0);
		assert(y>=0);
		assert(x < numColumns);
		assert(y < numRows);
		
		return this.data[x][y];
	}
	
	protected void set(int i, int j, double d) {
		
		this.data[i][j] = d;
		
	}
	
	public double[][] getData()
	{
		return this.data;
	}
	
	public double[][] getData(String[] names) {
	    int[] ix = new int[names.length];
	    for (int i=0; i<names.length; i++) {
	        int j=0; while (j<columnNames.size() && !columnNames.get(j).equals(names[i])) j++;
	        if (j==columnNames.size()) throw new RuntimeException("Variable name "+names[i]+" does not exist in data set.");
	        ix[i] = j;
	    }
	    
	    double[][] erg = new double[data.length][ix.length];
	    for (int i=0; i<data.length; i++) {
	        for (int j=0; j<ix.length; j++) erg[i][j] = data[i][ix[j]];
	    }
	    return erg;
	}
	
	
	public static RawDataset createRandomDataset(int numRows, int numColumns)
	{
		Random r = new Random();
		
		double[][] data = new double[numRows][numColumns];
				
		for (int i=0; i < numRows; i++)
		{
			for (int j=0; j < numColumns; j++)
			{
				data[i][j] = r.nextDouble();
			}
		}
		
		RawDataset dataset = new RawDataset(data);
		
	
		dataset.setName("Random Data");
		return(dataset);
	}


	public double getColumnMean(int i)
	{
		double mu=0;
		for (int j=0; j < numRows;j++) {
			mu+= this.data[j][i];
		}
		mu /= numRows;
		
		return(mu);
	}

	public double getColumnStandardDeviation(int i)
	{
		double mu= getColumnMean(i);
		double sd=0;
		for (int j=0; j < numRows;j++) {
			sd+= (this.data[j][i]-mu)*(this.data[j][i]-mu);
		}
		sd/= (numRows-1);
		
		return(Math.sqrt(sd));
	}
	
	public double[] getColumn(int j)
	{
		double[] col = new double[getNumRows()];
		for (int i=0; i < getNumRows();i++) col[i] = data[i][j];
		
		return(col);
	}
	
	public double getColumnMedian(int i)
	{
//	    Collections.sort();
		double[] values = this.getColumn(i);
		java.util.Arrays.sort(values);
		
	    if (getNumRows() % 2 == 1)
		return values[(getNumRows()+1)/2-1];
	    else
	    {
		double lower = values[(getNumRows()/2-1)];
		double upper = values[getNumRows()/2];
	 
		return (lower + upper) / 2.0;
	    }			
	}
	
    public int getColumnNumber(String headerName) {
        for (int i=0; i<columnNames.size(); i++) if (headerName.equals(columnNames.get(i))) return i;
        return -1;
    }
    

	


	
	/**
	 * 
	 * @param column   the number of the column in the data set to look at. 
	 * @return a double array containing min, first quartile, median, mean, third quartile, max, stdv, number of columns, and number of missing values
	 */
    public double[] getVariableDescriptivesArray(int column) {return getVariableDescriptivesArray(data, centralizationMeans, column);}
    public static double[] getVariableDescriptivesArray(double[][] data, int column){return getVariableDescriptivesArray(data, null, column);}
    public static double[] getVariableDescriptivesArray(double[][] data, double[] centralizationMean, int column){
        if (data == null || data.length == 0) return new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, 0};
        int numRows = data.length;
        int anzMiss = 0;
        for (int i=0; i<numRows; i++) if (Model.isMissing(data[i][column])) anzMiss++;
        int anz = numRows - anzMiss;
        double[] col = new double[anz]; int j = 0; for (int i=0; i<numRows; i++) if (!Model.isMissing(data[i][column])) col[j++] = data[i][column] + (centralizationMean!=null?centralizationMean[column]:0);
        double min = Double.NaN, max = Double.NaN, fqu = Double.NaN, median = Double.NaN, mean = Double.NaN, tqu = Double.NaN, stdv = Double.NaN;
        if (col.length > 0) {
            Arrays.sort(col);
            min = col[0]; max = col[anz-1];
            int ix1st = (anz-1)/4; if (((anz-1)/4) % 4 == 0) {fqu = col[ix1st]; tqu = col[anz-1-ix1st]; } else {fqu = (col[ix1st]+col[ix1st+1])/2.0; tqu = (col[anz-1-ix1st]+col[anz-2-ix1st])/2.0;}
            if (anz % 2 == 0) median = (col[anz/2]+col[anz/2-1])/2.0; else median = col[anz/2];
            mean = 0; double var = 0; for (int i=0; i<anz; i++) {mean += col[i]; var += col[i]*col[i];}
            mean /= anz; var = var / (anz-1) - mean*mean * anz / (anz-1); stdv = Math.sqrt(var);
            /*mean = 0; double var = 0; for (int i=0; i<anz; i++) {mean += col[i]; }; mean /= anz;
            for (int i=0; i < anz; i++) {var += (col[i]-mean)*(col[i]-mean);}
            var = var / (anz-1); stdv = Math.sqrt(var);*/
        }
        return new double[]{min, fqu, median, mean, tqu, max, stdv, numRows, anzMiss};
    }
    
    public String getVariableDescriptives(int column) {return getVariableDescriptives(column, 5);}
    public String getVariableDescriptives(int column, int digits) {return getVariableDescriptives(data, centralizationMeans, getColumnName(column), column, digits);}
    public static String getVariableDescriptives(double[][] data, double[] centralizationMeans, String columnName, int column) {return getVariableDescriptives(data, centralizationMeans, columnName, column, 5);}
    public static String getVariableDescriptives(double[][] data, double[] centralizationMeans, String columnName, int column, int digits) {
        double[] desc = getVariableDescriptivesArray(data, centralizationMeans, column);
        String erg  = 
            "<h4>"+columnName+"</h4>"+
               "Min.   :"+Statik.doubleNStellen(desc[0], digits)+"<br>";
        erg += "1st Qu.:"+Statik.doubleNStellen(desc[1], digits)+"<br>";
        erg += "Median :"+Statik.doubleNStellen(desc[2], digits)+"<br>";
        erg += "Mean   :"+Statik.doubleNStellen(desc[3], digits)+"<br>";
        erg += "3rd Qu.:"+Statik.doubleNStellen(desc[4], digits)+"<br>";
        erg += "Max.   :"+Statik.doubleNStellen(desc[5], digits)+"<br>";
        erg += "Stdv   :"+Statik.doubleNStellen(desc[6], digits)+"<br>";
        erg += "Total  :"+Statik.doubleNStellen(desc[7], 0)+"<br>";
        erg += "Missing:"+Statik.doubleNStellen(desc[8], 0)+"<br>";
        erg += "";
        return erg;        
    }
    
    public static String[] getVariableDescriptivesArray(double[][] data, double[] centralizationMean, String columnName, int column, int digits) {
        double[] desc = getVariableDescriptivesArray(data, centralizationMean, column);
        String[] erg = new String[]{
                columnName,
                "Min.   :"+Statik.doubleNStellen(desc[0], digits),
                "1st Qu.:"+Statik.doubleNStellen(desc[1], digits),
                "Median :"+Statik.doubleNStellen(desc[2], digits),
                "Mean   :"+Statik.doubleNStellen(desc[3], digits),
                "3rd Qu.:"+Statik.doubleNStellen(desc[4], digits),
                "Max.   :"+Statik.doubleNStellen(desc[5], digits),
                "Stdv   :"+Statik.doubleNStellen(desc[6], digits),
                "Total  :"+Statik.doubleNStellen(desc[7], 0),
                "Missing:"+Statik.doubleNStellen(desc[8], 0)
        };
        return erg;
    }
    
    public static String getVariableDescription(double[][] data, double[] centralizationMean, String[] variableNames) {return getVariableDescription(data,centralizationMean, variableNames, 4);}
    public static String getVariableDescription(double[][] data, double[] centralizationMean, String[] variableNames, int varPerBlock) {
        String erg = "";
        int anzVar = variableNames.length;
        for (int i=0; i<anzVar; i+=varPerBlock) {
            String[] lines = new String[10]; for (int j=0; j<lines.length; j++) lines[j] = "";
            for (int j=i; j<i+varPerBlock && j<anzVar; j++) {
                String[] vals = getVariableDescriptivesArray(data, centralizationMean, variableNames[j], j, 5);
                int max = 0; 
                for (int k=0; k<vals.length; k++) max = Math.max(max, vals[k].length());
                for (int k=0; k<vals.length; k++) lines[k] += vals[k] + Statik.repeatString(" ", 3+max-vals[k].length());
            }
            for (int k=0; k<lines.length; k++) erg += lines[k] += "\r\n";
            erg += "\r\n";
        }
        return erg;
    }
    

	public boolean hasColumnMissing(int columnId) {
		
		double[] d = getColumn(columnId);
		for (int i=0; i < d.length; i++) {
			if (Model.isMissing(d[i])) return true;
		}
		return false;
		
	}
	
	public boolean hasColumnAllMissing(int columnId) {
		double[] d = getColumn(columnId);
		for (int i=0; i < d.length; i++) {
			if (!Model.isMissing(d[i])) return false;
		}
		return true;
	}

	public String getColumnTooltip(int columnId) {
	    return getVariableDescriptives(columnId, 5);
	}

	public void setColumn(int row, int column, double d) {
		
		this.data[row][column] = d;
		
	}

    /**
     * Returns a string that describes the data distribution of the selected nodes in the format
     * 
     *  Data Mean  Data Covariance
     *  54          20  1   3
     *  43          1   15  7
     *  0           3   7   20
     *  
     *  Rows with missingness are deleted. Returns an empty string if less than 2 data rows have no missingness.
     *  
     * @param selected
     * @return
     */
    @Deprecated // should use CombinedDataset.getDataDistribution.
    public String getDataDistribution(int[] columnIDs, String[] varNames) {
        int anzVar = columnIDs.length;
        double[] mean = new double[anzVar];
        double[][] cov = new double[anzVar][anzVar];
        int anzValid = 0;
        for (int i=0; i<data.length; i++) {
            boolean hasMissing = false; for (int j=0; j<anzVar; j++) if (Model.isMissing(data[i][columnIDs[j]])) hasMissing = true;
            if (!hasMissing) {
                anzValid++;
                for (int j=0; j<anzVar; j++) {
                    mean[j] += data[i][columnIDs[j]];
                    for (int k=0; k<anzVar; k++) cov[j][k] += data[i][columnIDs[j]]*data[i][columnIDs[k]]; 
                }
            }
        }
        if (anzValid < 2) return "";
        for (int i=0; i<anzVar; i++) mean[i] /= anzValid;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) cov[i][j] = cov[i][j] / (anzValid-1) - mean[i]*mean[j]*anzValid/(anzValid-1);
        
        String erg = "<table>\r\n<tr><td>Variable</td><td>Data Mean</td><td>Data Covariance</td></tr>";
        for (int i=0; i<anzVar; i++) {
            erg += "<tr><td>"+varNames[i]+"</td>";
            erg += "<td>"+Statik.doubleNStellen(mean[i],2)+"</td>";
            erg += "<td>"; for (int j=0; j<anzVar; j++) erg += Statik.doubleNStellen(cov[i][j],2)+"&nbsp;&nbsp;&nbsp;"; erg += "</td>";
            erg += "</tr>\r\n";
        }
        erg += "</table>\r\n";
        return erg;
    }
    
   /* public void setData(double[][] data) {
        this.data = data;
        this.numRows = data.length;
    }*/

    // Following three methods have been removed by AB for unknown reasons; TvO re-added them since I couldn't find the code elsewhere, and it was
    // used in the Test classes. Check whether they are deprecated, and replaced by what code.
    
    private boolean checkEqual(int[] numbers) {
        if (numbers.length <= 1) return true;
        int nr = numbers[1]; for (int i=2; i<numbers.length; i++) if (numbers[i] != nr) return false;
        return (numbers[0] == nr || numbers[0]+1 == nr);
    }
    
    @Deprecated // use Dataset.createDatasetFromString to create a new object
    public boolean setData(String table) {return setData(new BufferedReader(new StringReader(table)));}
    @Deprecated // use Dataset.createDatasetFromBuffer to create a new object
    public boolean setData(BufferedReader reader) {
        int INITLINES = 10;
        Vector<String> initLine = new Vector<String>(INITLINES);
        for (int i=0; i<INITLINES; i++) {
            try {
                String s = reader.readLine(); if (s!=null) initLine.add(s); 
                else INITLINES = i;
            } catch (IOException e) {
                INITLINES = i;
            }
        }
        
        // The following lines search for the separator that appears equally often in the first INITLINES lines (allowing for one less in the first line)
        // and chooses the one that appears most often among those. 
        int[][] firstAnz = new int[separatorCandidate.length][INITLINES]; 
        for (int j=0; j<INITLINES; j++) 
            for (int i=0; i<separatorCandidate.length; i++) 
            {
                firstAnz[i][j] = Statik.countSubstring(initLine.elementAt(j), ""+separatorCandidate[i]);
            }
       
        int nr = 0; while (nr < separatorCandidate.length && !checkEqual(firstAnz[nr])) nr++;

        // return failure as no separator appears equally often in the first INITLINES lines. 
        if (nr >= separatorCandidate.length) return false;      
        
        for (int i=nr+1; i<separatorCandidate.length; i++) if (checkEqual(firstAnz[i]) && firstAnz[i][1]>firstAnz[nr][1]) nr = i;
        String separator = separatorCandidate[nr];
        numColumns = firstAnz[nr][0]+1; 
        
        // determines whether the first line is a header line
        String[] first = initLine.elementAt(0).split(""+separator);
        boolean noHeader = true;
        for (int i=0; i<first.length; i++) noHeader = noHeader && Model.isMissingOrNumber(first[i]);
        columnNames = new ArrayList<String>(numColumns);
        if (noHeader) {
            for (int i=0; i<numColumns; i++) columnNames.add("X"+(i+1));
        } else {
            for (int i=0; i<numColumns; i++) {
                first[i] = first[i].trim();
                if (first[i].startsWith("\"") && first[i].endsWith("\"")) first[i] = first[i].substring(1, first[i].length()-1);
                if (first[i].length()==0) first[i] = (i==0?"ID":"Unnamed Variable");
                columnNames.add(first[i]);
            }
        }
        
        
        Vector<double[]> dataLines = new Vector<double[]>();
        int counter = (noHeader?0:1);
        try {
            String s = (reader.ready()?reader.readLine():null);
            while (counter<INITLINES || s != null) {
                String line = null; if (counter<INITLINES) line = initLine.elementAt(counter); else {
                    line = s;
                    s = (reader.ready()?reader.readLine():null);
                }
                String[] split = line.split(""+separator);
                double[] row = new double[numColumns];
                for (int j=0; j<numColumns; j++) {
                    String content = (j<split.length?split[j].trim():Model.MISSING+"");
                    if (content.startsWith("\"") && content.endsWith("\"")) content = content.substring(1, content.length()-1);
                    if (Model.isMissing(content)) row[j] = Model.MISSING;
                    else {
                        try {
                            row[j] = Double.parseDouble(content);
                        } catch (Exception e) {
                            row[j] = Model.MISSING;
                        }
                    }
                }
                dataLines.add(row);
                counter++;
            }
        } catch (IOException e) {}
        
        numRows = dataLines.size();
        this.data = new double[numRows][];
        for (int i=0; i<numRows; i++) data[i] = dataLines.elementAt(i);
        
        return true;
    }

    
	public int getIdColumn() {
		return idColumn;
	}
	
	public void save(File file, String missingIndicator) {
        String[] colNames = getColumnNames(); 
        String header = ""; for (int i=0; i<colNames.length; i++) header += colNames[i]+(i==colNames.length-1?"":"\t");
        Statik.writeMatrix(data, file, '\t', header, missingIndicator);
	}

    public void centerMeans() {
        for (int var=0; var<numColumns; var++) {
            double sum = 0.0; int anz = 0;
            for (int i=0; i<data.length; i++) if (!Model.isMissing(data[i][var])) {anz++; sum += data[i][var];}
            if (anz > 0) {
                double mean = sum / (double)anz;
                for (int i=0; i<data.length; i++) if (!Model.isMissing(data[i][var])) data[i][var] -= mean;
                centralizationMeans[var] += mean;
            }
        }
    }
    
    public boolean isValid() {return data != null;}
    
    public double[] getCentraliziationMeans() {return centralizationMeans;}

	@Override
	public int getSampleSize() {
		return(getNumRows());
	}

    public static RawDataset loadRawDataset(String filename) {
        Dataset dataset = Dataset.loadDataset(filename);
        if (dataset instanceof RawDataset) return ((RawDataset)dataset); else return null;
    }

}

