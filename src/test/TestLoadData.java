package test;

import java.io.BufferedReader;
import java.io.StringReader;

import engine.Dataset;
import engine.RawDataset;
import junit.framework.TestCase;

public class TestLoadData extends TestCase {

	public void testLoadCSV() {
		
		String stringrep = 
				"col1,col2,col3\n"+
				"0,1,2\n"+
				"0,2,4\n"+
				"0,3,2\n"+
				"0,4,4\n"+
				"0,5,2\n"+
				"0,6,4\n";
		
		BufferedReader br = new BufferedReader(new StringReader(stringrep));
		
		try {
			RawDataset ds = (RawDataset) Dataset.createDatasetFromReader(br, "My Name", null, true);
			
			
			assertEquals(0.0,ds.getColumnMean(0));
			assertEquals(3.5,ds.getColumnMean(1));
			assertEquals(3.0,ds.getColumnMean(2));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
public void testLoadCSVwithQuotes() {
		
		String stringrep = 
				"col1,col2,col3\n"+
				"0,6,4\n"+
				"0,4,4\n"+
				"\"hi, there!\",2,4\n"+
				"0,0,4\n"+
				"0,0,4\n"+
				"\"uh\",0,4\n";
				
				
		
		BufferedReader br = new BufferedReader(new StringReader(stringrep));
		
		try {
			RawDataset ds = (RawDataset) Dataset.createDatasetFromReader(br, "My Name", null, true);
			
			assertEquals(ds.getNumColumns(),3);
		
			assertEquals(2.0,ds.getColumnMean(1));
			assertEquals(4.0,ds.getColumnMean(2));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testLoadTSV() {
		
		String stringrep = 
				"col1\tcol2\tcol3\n"+
				"0\t1\t2\n"+
				"0\t2\t4\n"+
				"0\t3\t2\n"+
				"0\t4\t4\n"+
				"0\t5\t2\n"+
				"0\t6\t4\n";
		
		BufferedReader br = new BufferedReader(new StringReader(stringrep));
		
		try {
			RawDataset ds = (RawDataset) Dataset.createDatasetFromReader(br, "My Name", null, true);
			
			
			assertEquals(0.0,ds.getColumnMean(0));
			assertEquals(3.5,ds.getColumnMean(1));
			assertEquals(3.0,ds.getColumnMean(2));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
public void testLoadTSVwithQuotes() {
		
		String stringrep = 
				"col1\tcol2\tcol3\n"+
				"0\t1\t2\n"+
				"\"hi\tthere!\tYeah, you!\"\t2\t4\n"+
				"0\t3\t2\n"+
				"0\t4\t4\n"+
				"0\t5\t2\n"+
				"0\t6\t4\n";
		
		BufferedReader br = new BufferedReader(new StringReader(stringrep));
		
		try {
			RawDataset ds = (RawDataset) Dataset.createDatasetFromReader(br, "My Name", null, true);
			
			

			assertEquals(3.5,ds.getColumnMean(1));
			assertEquals(3.0,ds.getColumnMean(2));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
