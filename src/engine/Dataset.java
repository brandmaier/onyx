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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import engine.backend.Model;

public abstract class Dataset {
    
	public static final String[] separatorCandidate = new String[]{"\t", ",", ";", " ", "\\|"}; // these are Strings because they represent REXEXPs

    
    protected List<String> columnNames;
	private String name = "unnamed";
	protected List<DatasetChangedListener> changeListeners;

	public Dataset() {
		changeListeners = new ArrayList<DatasetChangedListener>();
	}
	
	// should be overwritten by subtypes that may contain invalid data.
	public boolean isValid() {return true;}
	
	public String[] getColumnNames() {
		return this.columnNames.toArray( new String[] {});
	}
	
	public List<String> getColumnNamesAsList()
	{
		return this.columnNames;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getColumnName(int i) {
		if (i > this.columnNames.size()) { return "ERROR"; }
		return columnNames.get(i);
	}
	
	public void setColumnName(int i, String s)
	{
		this.columnNames.set(i, s);
	}
	

	
	public List<String> getNames() {
		return columnNames;
	}
	
	public abstract int getNumColumns();
	
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
    public abstract String getDataDistribution(int[] columnIDs, String[] varNames);
    
    public abstract double getColumnStandardDeviation(int i);
    
    public abstract String getColumnTooltip(int columnId);
	
	public abstract boolean hasIdColumn();
	public abstract int getIdColumn();
	
    public static Dataset createDatasetFromString(String table, String name) throws Exception {
    	return createDatasetFromReader(new BufferedReader(new StringReader(table)), name);
    	}
    
    /**
     * Creates a data set from a BufferedReader that provides a string as described in <code>getDataDistribution()</code> or in CovarianceDataSet. 
     * Multiple missing codes are accepted. Unreadable strings are also considered missing. The first line may contain a header (otherwise, 
     * variable names are created). The separator is automatically determined if possible. The optional parameter wordToNumber can contain
     * String codes which are then replaced by numbers if they occur in the data set; the same string code in multiple of the arrays
     * will always be numbered according to the last array in row. If a numeric value contains only digits and a single comma, then European
     * numbers are assumed, where the comma is replaced by a point (e.g., 17,5 is interpreted as 17.5). 
     * 
     * @param reader
     * @param name
     * @return
     * @throws Exception
     */
    public static Dataset createDatasetFromReader(BufferedReader reader, String name) throws Exception {return createDatasetFromReader(reader,name,null,false);}
    public static Dataset createDatasetFromReader(BufferedReader reader, String name, String[][] wordToNumber, boolean forceToRawDataset) throws Exception {
    	
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
            	// counting only those separators that are not within quotes (using lookahead)
            	String[] split = initLine.elementAt(j).split(separatorCandidate[i]+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            	firstAnz[i][j] = split.length-1;
            }
       
        int nr = 0; while (nr < separatorCandidate.length && !checkEqual(firstAnz[nr])) nr++;

        // return failure as no separator appears equally often in the first INITLINES lines. 
        if (nr >= separatorCandidate.length) throw new Exception("Could not determine separator!");      
        
        for (int i=nr+1; i<separatorCandidate.length; i++) if (checkEqual(firstAnz[i]) && firstAnz[i][1]>firstAnz[nr][1]) nr = i;
        String separator = separatorCandidate[nr];
        int numColumns = firstAnz[nr][0]+1; 
        
        // determines whether the first line is a header line
        String[] first = initLine.elementAt(0).split(""+separator);
        boolean noHeader = true;
        for (int i=0; i<first.length; i++) noHeader = noHeader && Model.isMissingOrNumber(first[i]);
        List<String> columnNames = new ArrayList<String>(numColumns);
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
                // instead of a naive split use a lookahead that
                // only matches a separator if the number of quotes ahead is even
                //String[] split = line.split(""+separator);
                //String[] split = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String[] split = line.split(separator+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                double[] row = new double[numColumns];
                for (int j=0; j<numColumns; j++) {
                    String content = (j<split.length?split[j].trim():Model.MISSING+"");
                    if (content.startsWith("\"") && content.endsWith("\"")) content = content.substring(1, content.length()-1);
                    if (Model.isMissing(content)) row[j] = Model.MISSING;
                    else {
                        int code = -1;
                        if (wordToNumber != null) {
                            for (int i=0; i<wordToNumber.length; i++) for (int k=0; k<wordToNumber[i].length; k++) 
                                if (wordToNumber[i][k].equals(content)) code = k;
                        }
                        if (content.indexOf('.')==-1 && content.indexOf(',') != -1 && content.indexOf(',')==content.lastIndexOf(','))
                            content = content.replace(',', '.');
                        try {
                            row[j] = Double.parseDouble(content);
                        } catch (Exception e) {
                            row[j] = Model.MISSING;
                            if (code != -1) row[j] = code;
                        }
                    }
                }
                dataLines.add(row);
                counter++;
            }
        } catch (IOException e) {}
        
        int numRows = dataLines.size();
        double[][] data = new double[numRows][];
        for (int i=0; i<numRows; i++) data[i] = dataLines.elementAt(i);
        
        
        // decide whether this is a covariance or a raw data set
        boolean isRaw = true;
        if (numRows==numColumns && !forceToRawDataset) {
        	// TODO check symmetry
        	isRaw = false;
        }
        // -- -- --
        
        if (isRaw) {
        RawDataset dataset = new RawDataset();
        dataset.setName(name);
        dataset.setData(data, columnNames);

        return dataset;
        } else {
//        	System.out.println("Covariance dataset!");
        	CovarianceDataset dataset = new CovarianceDataset();
        	dataset.setName(name);
        	dataset.setData(data, columnNames);
        	return dataset;
        }
    }
    
	
    /**
     * AB: returns true if all entries of an int[] array are equal
     * with the exception of the first entry that also can be one more than the rest
     * @param numbers
     * @return
     */
    private static boolean checkEqual(int[] numbers) {
        if (numbers.length <= 1) return true;
        int nr = numbers[1]; for (int i=2; i<numbers.length; i++) if (numbers[i] != nr) return false;
        return (numbers[0] == nr || numbers[0]+1 == nr);
    }
    
    public void save(File file) {save(file, null);}
    public abstract void save(File file, String missingIndicator);
    
    public abstract int getSampleSize();

    public static Dataset loadDataset(String filename) {
        try {
            Dataset erg = createDatasetFromReader(new BufferedReader(new FileReader(new File(filename))), "dataSet");
            return erg;
        } catch (Exception e) {return null;}
    }
}
