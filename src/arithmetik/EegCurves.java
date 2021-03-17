package arithmetik;

/**
 * Insert the type's description here.
 * Creation date: (09.05.2003 12:59:50)
 * @author: 
 */

import java.io.*;
import java.util.*;

import engine.Statik;
 
public class EegCurves 
{
	private double[][] data;
/**
 * EegCurves constructor comment.
 */
public EegCurves() {
	super();
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 12:13:51)
 * @param toCopy arithmetik.EegCurves
 */
public EegCurves(EegCurves toCopy) 
{
	data = new double[toCopy.data.length][toCopy.data[0].length];
	for (int i=0; i<data.length; i++)
		for (int j=0; j<data[i].length; j++)
			data[i][j] = toCopy.data[i][j];	
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 12:16:03)
 * @param toCopy arithmetik.EegCurves
 * @param spur int
 */
public EegCurves(EegCurves toCopy, int spur) 
{
	data = new double[1][toCopy.data[spur].length];
	for (int i=0; i<data[0].length; i++)
		data[0][i] = toCopy.data[spur][i];
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:37:14)
 * @param input java.io.File
 */
public EegCurves(File input) 
{
	try {
		BufferedReader b = new BufferedReader(new FileReader(input));
		Vector erg = new Vector();
		while (b.ready()) 
		{
			String s = b.readLine();
			try {
				double d = Double.parseDouble(Statik.ersetzeString(s,",","."));
				erg.addElement(new Double(d));
			} catch (Exception e) {}
		}
		b.close();
		
		data = new double[1][erg.size()];
		for (int i=0; i<erg.size(); i++) data[0][i] = ((Double)erg.elementAt(i)).doubleValue();
	} catch (IOException e) {System.out.println("Error Reading eeg data: "+e);}
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 13:14:22)
 * @param dim int
 */
public double[] autoCorrelationFunction(int spur, int dim) 
{
	EegCurves kopie = new EegCurves(this, spur);
	kopie.normalize();
	double[] korrelationen = new double[dim];
	for (int i=0; i<dim; i++) korrelationen[i] = 0.0;
	for (int i=0; i<kopie.data[0].length; i++)
		for (int j=0; (j<dim) && (i+j<kopie.data[0].length); j++) korrelationen[j] += kopie.data[0][i]*kopie.data[0][i+j];

	for (int i=0; i<dim; i++) korrelationen[i] /= data[spur].length;

	return korrelationen;
}
/**
 * Insert the method's description here.
 * Creation date: (08.09.2003 12:17:40)
 * @return double

	diese Methode nimmt die Spuren als Dimensionen und die Zeitpunkte als Punkte in diesem Raum. Von
	n zufällig gewählten Punkten aus wird auf einer doppelt logarithmischen Skala der Radius einer Kugel
	gegen die Anzahl der Punkte in dieser Kugel abgetragen. Die ersten m Punkte werden linear approximiert;
	so lange der höchste Punkt am weitesten von der Kurve entfernt liegt, wird die Anzahl der Punkte verringert.
	Die durchschnittliche Steigung der so erhaltenen Werte wird zurückgegeben.
 
 */
public double dimensionality() 
{
	final int ANZSTARTS	= 8;
	final int ANZMAXPUNKTE = 10;
	int anzdim = data.length;
	int anzpunkte = data[0].length;

	double erg = 0.0;
	for (int i=0; i<ANZSTARTS; i++)
	{
		
	}
	return erg / (double)ANZSTARTS;
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 13:49:07)
 * @param spur int
 * @param wavelet double[]

	Faltet die Spur entlang dem wavelet. Ränder werden halb hinzugenommen; Die Passung ist jeweils
	in der Mitte des angepassten Bereiches.
 
 */
public void fold(int spur, double[] wavelet) 
{
	double med = 0.0;
	for (int i=0; i<wavelet.length; i++) med += wavelet[i];
	med /= wavelet.length;
	for (int i=0; i<wavelet.length; i++) wavelet[i] -= med;
	double stddev = 0.0;
	for (int i=0; i<wavelet.length; i++) stddev += wavelet[i]*wavelet[i];
	stddev = Math.sqrt(stddev/wavelet.length);
	for (int i=0; i<wavelet.length; i++) wavelet[i] /= stddev;

	int anzpunkte = wavelet.length;
	int anzphalb = anzpunkte / 2;
	double[] schnitt = new double[anzpunkte];
	double[] neueSpur = new double[data[spur].length];
	for (int i=0; i<data[spur].length; i++)
	{
		int anzreal=0;
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) {anzreal++; schnitt[j+anzphalb] = data[spur][i+j];}
		
		med = 0.0;
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) med += schnitt[j+anzphalb];
		med /= anzreal;
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) schnitt[j+anzphalb] -= med;
		
		stddev = 0.0;
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) stddev += schnitt[j+anzphalb]*schnitt[j+anzphalb];
		stddev = Math.sqrt(stddev/anzreal);
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) schnitt[j+anzphalb] /= stddev;

		double kor = 0.0;
		for (int j=-anzphalb; j<anzphalb; j++)
			if ((j+i>0) && (j+i<data[spur].length)) kor += schnitt[j+anzphalb]*wavelet[j+anzphalb];
		neueSpur[i] = kor / anzreal;
	}

	data[spur] = neueSpur;
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:37:39)
 * @param args java.lang.String[]
 */
public static void main(String[] args) 
{
	EegCurves eeg = new EegCurves(new File("Probe_1.dat"));
	System.out.println(eeg);
	double[] autokor = eeg.autoCorrelationFunction(0,50);
	for (int i=0; i<autokor.length; i++) System.out.println("Autokorrelation "+i+": "+autokor[i]);
	double[][] pvecs = eeg.principalVectors(0,3,50);
	for (int i=0; i<pvecs.length; i++)
	{
		System.out.print("Hauptvektor "+i+": ");
		for (int j=0; j<pvecs[i].length; j++) System.out.print(pvecs[i][j]+", ");
		System.out.println();
	}
	
	System.exit(0);
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 12:06:38)
 * @param spur int

	Setzt Mittel der Spur auf 0.
 
 */
public void medialize(int spur) 
{
	double mittel = 0.0;
	for (int i=0; i<data[spur].length; i++) mittel += data[spur][i];
	mittel /= data[spur].length;
	for (int i=0; i<data[spur].length; i++) data[spur][i] -= mittel;
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:14:56)
 * @param spur int
 * @param anzpunkte int

	Führt für jeden Punkt eine Mittelung von anzpunkte vielen Stellen um den Punkt herum aus und zieht diese
	Mittelung vom Wert ab.
 
 */
public void medializeLocaly(int spur, int anzpunkte) 
{
	int anzphalb = anzpunkte / 2;
	double[] mittelbereich = new double[anzpunkte];
	int mittelpos = 0; 
	double mittelsumme = 0.0;
	for (mittelpos = 0; mittelpos < anzphalb; mittelpos++) {mittelbereich[mittelpos] = data[spur][mittelpos]; mittelsumme += mittelbereich[mittelpos];}
	int mittelanz = mittelpos;
	for (int i=0; i<data[spur].length; i++)
	{
		if (i<anzphalb) mittelanz++;
		if (i>data[spur].length-anzphalb) {mittelanz--; mittelsumme -= mittelbereich[mittelpos]; mittelbereich[mittelpos] = 0.0;}
		else {mittelsumme += data[spur][i+anzphalb] - mittelbereich[mittelpos]; mittelbereich[mittelpos] = data[spur][i+anzphalb];}
		data[spur][i] -= mittelsumme / mittelanz;
	}			
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 12:20:19)
 */
public void normalize() 
{
	for (int i=0; i<data.length; i++) normalize(i);
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:14:56)
 * @param spur int
 * @param anzpunkte int

	Führt für jeden Punkt eine Mittelung von anzpunkte vielen Stellen um den Punkt herum aus und zieht diese
	Mittelung vom Wert ab.
 
 */
public void normalize(int spur) 
{
	medialize(spur);
	if (data[spur].length <= 1) return;
	double varianz = 0.0;
	for (int i=0; i<data[spur].length; i++) varianz += data[spur][i]*data[spur][i];
	varianz /= (data[spur].length-1);
	double standartabweichung = Math.sqrt(varianz);
	for (int i=0; i<data[spur].length; i++) data[spur][i] /= standartabweichung;
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:14:56)
 * @param spur int
 * @param anzpunkte int

	Führt für jeden Punkt eine Mittelung von anzpunkte vielen Stellen um den Punkt herum aus und zieht diese
	Mittelung vom Wert ab. Danach wird durch die Varianz in diesem Bereich geteilt.

	(Varianz richtig berechnet? Quadratsumme / (n-1) ?
 
 */
public void normalizeLocaly(int spur, int anzpunkte) 
{
	medializeLocaly(spur,anzpunkte);

	int anzphalb = anzpunkte / 2;
	double[] mittelbereich = new double[anzpunkte];
	int mittelpos = 0; 
	double quadratsumme = 0.0;
	for (mittelpos = 0; mittelpos < anzphalb; mittelpos++) {mittelbereich[mittelpos] = data[spur][mittelpos]; quadratsumme += mittelbereich[mittelpos]*mittelbereich[mittelpos];}
	int mittelanz = mittelpos;
	for (int i=0; i<data[spur].length; i++)
	{
		if (i<anzphalb) mittelanz++;
		if (i>data[spur].length-anzphalb) {mittelanz--; quadratsumme -= mittelbereich[mittelpos]*mittelbereich[mittelpos]; mittelbereich[mittelpos] = 0.0;}
		else {quadratsumme += data[spur][i+anzphalb]*data[spur][i+anzphalb] - mittelbereich[mittelpos]*mittelbereich[mittelpos]; mittelbereich[mittelpos] = data[spur][i+anzphalb];}
		double varianz = quadratsumme / (mittelanz-1);
		double standartabweichung = Math.sqrt(varianz);
		data[spur][i] /= standartabweichung;
	}			
}
/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 13:23:45)
 * @return double[][]
 * @param anz int
 * @param length int
 * @param spur int
 */
public double[][] principalVectors(int spur, int anz, int length) 
{
	double[] autokor = autoCorrelationFunction(spur, length);
	double[][] mat = new double[length][length];

	for (int i=0; i<length; i++)
		for (int j=0; j<=i; j++)
			mat[i][j] = mat[j][i] = autokor[i-j];

	RingMatrix m = new RingMatrix(mat);
	RingVector[] e = m.eigenvectors(0.01, anz);

	double[][] erg = new double[anz][length];
	for (int i=0; (i<anz) && (i<e.length); i++)
		erg[i] = e[i].toDoubleArray();

	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (09.05.2003 18:58:23)
 * @return java.lang.String
 */
public String toString() 
{
	String erg = "EEG - Curve ("+data.length+" lanes with "+data[0].length+" data points)";
	return erg;
}
}
