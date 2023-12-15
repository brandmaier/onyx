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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import engine.RawDataset;
import engine.backend.Model;
import gui.views.ModelView;

/**
 * 
 * A lot of the following functionality was ported from
 *  Alan James Salmoni's wonderful Python module for importing SPSS files.
 *  
 *  A couple of bugfixes and additions were made throughout.
 * 
 * @author Andreas Brandmaier
 *
 */
public class SPSSImport extends Import {

	InputStream fis;
	private int recordType;
	private String eyeCatcher;
	private String metaStr;
	private String compressionBias;
	private int numCases;
	private int compressionSwitch;
	private int numOBSElements;
	private int fileLayoutCode; 
	
	Vector<RecordType2> recordsType2 = new Vector<RecordType2>();
	private String documentString;
	private int caseWeightvar;
	private int releaseNum;
	private int releaseSubNum;
	private int releaseIdNum;
	private int machineCode;
	private String fPrep;
	private int compressionScheme;
	private String endianCode;
	private String charRepCode;
	private int charRep;
	private Vector<Integer> cluster;
	
	enum variableType {NUMERIC, STRING};
	
	public RawDataset importSPSS(InputStream fis) throws Exception
	{
		
		try {
			this.fis = fis;
			
			getRecordType1();
			
			boolean EOF = false;
			while (!EOF) {
				
				int type = readNextInteger();
				if (debug) System.out.println("Type:"+type);
				switch(type)
				{
				case 2:
					getRecordType2();
				break;
				case 3:
					getRecordType3();
				break;
				case 6:
					getRecordType6();
				break;
				case 7:
					getRecordType7();
				break;
				case 999:
					readNextBytes(4);
					getData();
					EOF = true;
				break;
				case 0:
					EOF = true;
				break;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return(getDataset());
	}
	
	double[][] data;
	List<String> names;
	private boolean debug = false;
	private int compressionBiasInt;
	
	private void prepareDataset()
	{
		names = new ArrayList<String>();
		for (RecordType2 rec : recordsType2) names.add(rec.name);
		data = new double[this.numCases][this.recordsType2.size()];
	}
	
	public RawDataset getDataset()
	{
		return new RawDataset(data, names);
	}

	/**
	 * 
	 * returns data case by case
	 * 
	 * @throws Exception
	 */
	private void getData() throws Exception {
		
		prepareDataset();
		
		this.cluster = new Vector<Integer>();
		
		int numVars = recordsType2.size();
	
		for (int ncase=0; ncase < numCases; ncase++)
		{
			//if (debug)
				System.out.println("Getting case #"+ncase+" -------------------------");
			for (int i = 0; i < numVars; i++)
			{
				RecordType2 rec = recordsType2.get(i);
				
				if (rec.type == variableType.NUMERIC) {
					
					double val = getNumber();
					
					if (rec.formatType == 5) {//float
					
					
						if (!Double.isNaN(val)) { data[ncase][i] = val;} else { data[ncase][i] = Model.MISSING; }
						
						if (rec.missingR != null) {
							if (rec.missingR[0] <= val && val <= rec.missingR[1]) data[ncase][i] = Model.MISSING; 
						}
						if (rec.missingD != null) {
							for (double v : rec.missingD) {
								if (v == val)  data[ncase][i] = Model.MISSING; 
							}
						}
						
						if (debug)
						//	if (i < 220)
							System.out.println(" |- Get value #"+i+" ("+recordsType2.get(i).name+") "+val);
					
					} else {
						 data[ncase][i] = Model.MISSING; 
					}
				} else {
					String str = getString(rec);
					if (debug)
					System.out.println(" |- Get string #"+i+": "+str+ " type="+rec.typeCode);
					data[ncase][i] = Model.MISSING; 
				}
			}
		}
	}
	

	/*
	 * Strings can be up to 8 bytes or longer, if so indicated by bytecode 253
	 * 
	 * /* Data in system files is compressed in the following manner:
   data values are grouped into sets of eight; each of the eight has
   one instruction byte, which are output together in an octet; each
   byte gives a value for that byte or indicates that the value can be
   found following the instructions. 
   
   see about line 1488
   
   see sfm.read.c from PSPP (which is also found in  foreign-library in R)
   
   
   see also PSPP documentation: http://www.gnu.org/software/pspp/pspp-dev/html_node/Data-Record.html#Data-Record
	 */
	private String getString(RecordType2 rec) throws Exception {
		
		if (this.compressionSwitch == 0) {	// read an uncompressed number
			String val = readNextString(8);	// 8 bytes of string
			return val;
		} else {	// read a compressed number
			
		
			String ln = "";
			while(true) {
				
				// read blocks of 8 bytes
				if (this.cluster.size()==0) {	
					for (int i=0; i < 8; i++) {
						int in = readNextInteger8();
						if (in< 0) in+=256;	// read as unsigned char TODO: is this correct?
						this.cluster.add(in);
					}
				}
				
				int byteData = this.cluster.remove(0);		
				System.out.println("Byte"+byteData);
				
				switch(byteData)
				{
					case 0:
					// case 0 is ignored
					continue;
					case 252:	//EOF
						throw new Exception("Compressed data is corrupted. Data ends partway through a case.");
					case 253:	//values are stored explicitly following the instruction byte
						String app=readNextString(8); 
						System.out.println("Read additional String elements "+app);
						ln=ln+app;
						if (ln.length() >= rec.typeCode) return ln;
					break;
					case 254:	// all blanks
						//if (!ln.equals(""))
						//return ln;
						ln = ln + "        ";
						if (ln.length() >= rec.typeCode) return ln;
					break;
					case 255:	// system missing value
						return null;
					default:	// 1 through 251 are taked to indicate a value of (BYTE - BIAS), BIAS is typically 100 ?!
						return Integer.toString( byteData-100);
				}
			}
			}
				/*System.out.println("Reading "+ln);
				
				
				System.out.println("byte" + byteData);
				//if (byteData > 0 && byteData < 252) byteData-=100;
//				if (byteData > 1 && byteData < 252) return null; //byteData-100;
				
				
				if (byteData==252) return null; 	//TODO: unsure about this
				else if (byteData==253) {
					
					
					String app=readNextString(8); 
					System.out.println("Read additional String elements "+app);
					ln=ln+app; 
					return ln;
					}
				else if (byteData==254) return(ln);
				else if (byteData==255) return null;
				else return null;	
				
				
			}
			

		}
*/		
	}

	
	public double getNumber() throws IOException {
		if (this.compressionSwitch == 0) {	// read an uncompressed number
			double val = readNextDouble();
			return val;
		} else {
			
			while(true) {
			
			if (this.cluster.size()==0) {	// read a compressed number
				// determine 
				for (int i=0; i < 8; i++) {
					int in = readNextInteger8();
					if (in < 0) in+=256;
					this.cluster.add(in);
				//	System.out.print(in+",");
					
//					if (endianCode)
				}
				//System.out.println();
			}
			
				int byteData = this.cluster.remove(0);
				
				//if (debug) System.out.println("Bytedata: "+byteData);
				if (byteData > 1 && byteData < 252) return byteData-100;	//byte - bias
				else if (byteData==0) continue;	// zero padding
 				else if (byteData==252) return Model.MISSING; 	//EOF, should rather be an error
				else if (byteData==253) return readNextDouble(); //explicit value
				else if (byteData==254) return 0.0f; // all blank string
				else if (byteData==255) return Model.MISSING;	// System missing value
				else return Float.NaN;
			}
			}
	}


	private void getRecordType1() throws IOException {

		
		this.recordType = readNextInteger(); 
		this.eyeCatcher = readNextString(60);
		this.fileLayoutCode = readNextInteger();
		this.numOBSElements = readNextInteger();
		this.compressionSwitch = readNextInteger();
		this.caseWeightvar = readNextInteger();
		this.numCases = readNextInteger();
		this.compressionBias = readNextString(8);
		this.metaStr = readNextString(84);
		
		//System.out.println(this.compressionBiasInt);
		if (debug) {
			System.out.println("Record Type 1 (file tag= "+recordType+")");
			System.out.println("Eye catcher: "+eyeCatcher);
			System.out.println("Meta:"+this.metaStr);
			System.out.println("Compression:"+compressionSwitch);
			System.out.println("NumCases"+this.numCases);
		}
	}
	
	private void getRecordType6() throws IOException  {
	
		
		int numBytes = readNextInteger(); 
		this.documentString = readNextString(80*numBytes);
		if (debug) {
		System.out.println("Document record");
		System.out.println(documentString);
		}
	}
	
	public class RecordType2 {
		int labelMarker;
		int missingMarker;
		int decPlaces;
		int colWidth;
		variableType type;
		//int rawFormatType;
		public int formatType;
		public String name;
		public int labelLength;
		public String label;
		public Double[] missingD;
		public Double[] missingR;
		public int typeCode;
	}
	
	/**
	 * This method reads in a type-2-record describing meta-data of a variable
	 * @throws Exception 
	 */
	private void getRecordType2() throws Exception {
		
		if (debug)
		System.out.println("Record Type 2");
		
		RecordType2 record = new RecordType2();
		record.typeCode = readNextInteger();
		
		
		if (record.typeCode==0) { record.type = variableType.NUMERIC; } else { record.type = variableType.STRING; }
		if (debug) System.out.println(" |- type: "+record.type);
		//if (debug) 
			System.out.println(" |- typeCode: "+record.typeCode);
		if (record.typeCode != -1) {	// if type code != -1, then record is for a numeric variable or first-and-only instance of a string
			record.labelMarker = readNextInteger(); 
			record.missingMarker = readNextInteger();
			record.decPlaces = readNextInteger8();
			record.colWidth = readNextInteger8();
			record.formatType = readNextInteger8();
			if (debug) System.out.println(" |- formatType:"+record.formatType);
			readNextInteger8();
			/*record.decPlaces =*/ readNextInteger8();
			/*record.colWidth =*/ readNextInteger8();
			/*record.formatType =*/ readNextInteger8();
			readNextInteger8();
		//	boolean nameBlankFlag = true;
			record.name = readNextString(8).trim();
			if (debug) System.out.println(" |- name:"+record.name+"|");
			if (record.labelMarker == 1) {	// has a label ?
				record.labelLength = readNextInteger();
				int numNextBytes = record.labelLength;
				if (numNextBytes % 4 != 0)	// make sure we read blocks of 4 bytes
					numNextBytes = numNextBytes + 4 - (numNextBytes % 4);
				if (debug) System.out.println(" | labelLength"+record.labelLength+" read bytes:"+numNextBytes);
				record.label = readNextString(numNextBytes);
				if (debug) System.out.println(" |- label:"+record.label);
			} else {
				record.label = "";
			}
			// for some reason skip next blocks of 8-bytes each times the missing marker index
			for (int i=0; i < Math.abs(record.missingMarker); i++) {
				readNextBytes(8);
			}
			// determine missing value definition
			record.missingD = null;
			record.missingR = null;
			if (record.missingMarker == 0) { // no missing values
				if (debug) System.out.println("No missing values!");
				
			} else if (record.missingMarker == -2 || record.missingMarker == -3) {	// range of missing values
				
				double val1 = (float)readNextDouble();
				double val2 = (float)readNextDouble();
				if (debug) System.out.println("Range of missing values: "+val1+" to "+val2);
				record.missingD = null;
				record.missingR = new Double[] {val1, val2};
				
				if (record.missingMarker == -3) {
					double val3 = (float)readNextDouble(); //readNextBytes(3); TODO: this in?
					record.missingD = new Double[] {val3};
				}
				
				
				
			} else if (record.missingMarker > 0 && record.missingMarker < 4) {
				record.missingD = new Double[record.missingMarker];
				for (int i=0; i < record.missingMarker; i++)
				{
					Double val3 = readNextDouble();// readNextBytes(3);TODO: this in?
					record.missingD[i] = val3;
				}
			}
			
			//if (record.missingMarker != 0) {
				if (record.missingR != null)
				System.out.println("Missing values are in range "+record.missingR[0]+" to "+record.missingR[1]);
				if (record.missingD != null)  {
					System.out.println("Missing values are ");
					for (int i=0; i < record.missingD.length;i++) System.out.print(record.missingD[i]);
					System.out.println();
					}
			//}
			
			this.recordsType2.add(record);
			
		} else { 	// typecode == -1 => record is continuation of a string variable
			
			//throw new Exception("Cannot import SPSS file!");
			
			// read and ignore the next 24 bytes , can we do better?
			//byte[] temp = readNextBytes(24);
			String content = readNextString(24);	// remember that previous typecode tells us the length of the string
			//System.out.println("Content bytes : "+content);
		}

	}
	private void getRecordType7() throws Exception
	{

		int subtype = readNextInteger();
		if (debug) System.out.println("Type 7:"+subtype+" Record");
		switch(subtype)
		{
		case 3:
			getSubType73();
		break;

		case 11:
			getSubType711();
		break;
		default:
		getUnknownType7();
		//break;
		}
	}
	private void getUnknownType7() throws IOException {
		
		int dataType = readNextInteger();
		int numElements = readNextInteger();
		readNextBytes( dataType * numElements);
		System.out.println("Unknown type-7 record read");
	}
	
	
	private void getSubType711() throws IOException {
		
		 String[] measures = new String[]{"Nominal", "Ordinal", "Continuous","Undefined"}; // Undefined is our own coding (AB)
	     String[] aligns = new String[]{"Left", "Right", "Center"};
			        		
		int dataType = readNextInteger();
		int numElements = readNextInteger() / 3;
		for (int i=0; i < numElements; i++)
		{
			int nextint = readNextInteger();
			if (nextint<=0 || nextint>2) {
				System.err.println("Unexpected index encountered in SubType711");
				nextint = 4;
			}
			String measure = measures[nextint-1];
			int width = readNextInteger();
			int align_index = readNextInteger();
			String align = aligns[align_index];
			if (debug)System.out.println(" Varinfo "+i+" "+measure+","+width+","+align);
		}
	}
	

	private void getSubType73() throws Exception {
		
		if (debug) System.out.println("Subtype 73");
	
       String[] FPrep = new String[] {"IEEE","IBM 370", "DEC VAX E"};
        String[] endian = new String[]{"Big-endian","Little-endian"};
        String[] charrep = new String[]{"EBCDIC","7-bit ASCII","8-bit ASCII","DEC Kanji"};
        		
		int dataType = readNextInteger();
		int numElements = readNextInteger();
		if (debug) System.out.println(" |- Following elements "+numElements);
		if (numElements == 8) 
		{
			releaseNum = readNextInteger();
			releaseSubNum = readNextInteger();
			releaseIdNum = readNextInteger();
			machineCode = readNextInteger();
			fPrep = FPrep[readNextInteger()-1];
			
			//System.out.println(fPrep);
			if (!fPrep.equals("IEEE")) throw new Exception("Cannot read non-IEEE numeric format in SPSS file");
			
			compressionScheme = readNextInteger();
			endianCode = endian[readNextInteger()-1];
			//charRepCode = charrep[readNextInteger()-1];
			charRep = readNextInteger();	// this needs to be fixed TODO
			
			
			System.out.println("Endianness " + endianCode);
			System.out.println("Char rep "+charRep);	// 1252 is Windows codepage, 28605 = ISO-8859-15
			// 65001 is UTF-8

		} else {
			throw new Exception("Cannot read machine-specific description of SPSS file.");
		}
	}
	
/*
 * value / label record
 * 
 * SPSS has value labels (e.g. {1 => YES, 0 => NO} ).
 * type3 records list value=>label mappings
 * the following type4 records lists to which variable the previous list belongs
 * (can also be multiple variables)
 * 
 */
	private void getRecordType3() throws Exception {
		
		if (debug)
		System.out.println("Record Type 3/4");
		
		// type 3 record
		
		int num = readNextInteger();	// read counter for number of pairs following
		Vector<Double> values = new Vector<Double>();
		if (debug)
		System.out.println("Num entries: "+num);
		for (int i=0; i < num; i++)
		{
			double val = readNextDouble();	// read value
			values.add(val);
			if (debug)
			System.out.println("Read "+i+"th value: "+val);
			
			// read next byte to obtain label length
			int llen = readNextInteger8();
			//if (l>0) {
			int l = llen;
			if (debug) System.out.print("label length l="+l+", thus reading ");
			if ((l % 8 ) != 0) { l = l + 8 - (l%8); }	// read up to next multiple of 8 bytes
			else l = l + 8;
			//int tmpl = (l+1)%8;
			//if (tmpl != 0) l += 8 - tmpl;
			if (debug) System.out.println(l+"bytes for field entry");
			String field = readNextString(l-1);
			if (debug) System.out.println("Field: "+field);
			//}
			
			
		
		}
		
		// type 4 record
		int t = readNextInteger(); 
		if (t == 4) {	// a type==4 must follow a type==3
			int numVars = readNextInteger(); 
			Vector<Integer> labelIndices = new Vector<Integer>();
			for (int i=0; i < numVars; i++) {
				int labelIndex = readNextInteger(); 
				labelIndices.add(labelIndex);
				if (debug) System.out.println("Concerning variable with index"+labelIndex+"\n");
			}
		} else {
			throw new Exception("Invalud subtype!");
		}

	}
	
	private byte[] readNextBytes(int bytes) throws IOException
	{
		byte[] b = new byte[bytes];
		fis.read(b);
		return b;
	}
	
	private int readNextInteger() throws IOException {
		
		byte[] b = new byte[4];
		fis.read(b);
		ByteBuffer buf = ByteBuffer.wrap(b); //new ByteBuffer(b);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		return buf.getInt();
		
		
		
	}
	
	private double readNextDouble() throws IOException {
		
		byte[] b = new byte[8];
		fis.read(b);
		ByteBuffer buf = ByteBuffer.wrap(b); //new ByteBuffer(b);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		return buf.getDouble();
	}
	
	private String readNextString(int len) throws IOException {
		
		byte[] b = new byte[len];
		fis.read(b);
		return new String(b);
	}
	
	private int readNextInteger8() throws IOException {
		
		byte[] b = new byte[1];
		fis.read(b);
		//ByteBuffer buf = ByteBuffer.wrap(b); //new ByteBuffer(b);
	//	return buf.get();
		return (int)b[0];
	}
	
	public static void main(String [] args)
	{
		SPSSImport imp = new SPSSImport();
		try {
//			imp.importSPSS( new FileInputStream("SPSS7test2.sav"));
//			imp.importSPSS( new FileInputStream("/Users/andreas/Downloads/aldaspss/doctors.sav"));
//			imp.importSPSS( new FileInputStream("/Users/andreas/Desktop/multigroup2-excerpt.sav"));
//			imp.importSPSS( new FileInputStream("/Users/andreas/Desktop/multigroup2-1line.sav"));
			//imp.importSPSS( new FileInputStream("/Users/andreas/Desktop/multigroup2.sav"));
			//imp.importSPSS( new FileInputStream("/Users/andreas/Desktop/strings.sav"));
		//	imp.importSPSS( new FileInputStream("/Users/andreas/Desktop/tiny.sav"));
			imp.importSPSS( new FileInputStream("/Users/brandmaier/Seafile/Nina_Andy_Ylva/modelsD2full_210308.sav"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
