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
package arithmetik;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class PraMaApplet extends Applet implements ActionListener, WindowListener, Printable
{
	public TextArea ta;
	
	public Button a11, a12;
	public TextField a1n;
	public Choice a1c;

	public Button a21,a22,a23;
	public TextField a2n;
	
	public Button a41, a42;
	public TextField a4n;
	public Choice a4c;
	
	public Button a5;
	public TextField a5n;
	
	public Button a61;
	public Button a62;
	public TextField a6n;
	
	public Frame frame;
	
	public PraMaApplet()
	{}
	public PraMaApplet(Frame frame)
	{
		this.frame = frame;
	}
	public void actionPerformed(ActionEvent e)
	{
		int n = -1;
		try {n = Integer.parseInt(a1n.getText());} catch(Exception ex) {n = -1;}
		
		if ((n>0) && (e.getSource()==a11)) aufgabe1a(n, a1c.getSelectedIndex());
		if (e.getSource()==a12) aufgabe1b();

		try {n = Integer.parseInt(a2n.getText());} catch(Exception ex) {n = -1;}
		
		if (e.getSource()==a21) aufgabe2a();
		if ((n>0) && (e.getSource()==a22)) aufgabe2b(n);
		if (e.getSource()==a23) aufgabe2c();		
		
		try {n = Integer.parseInt(a4n.getText());} catch(Exception ex) {n = -1;}

		if (e.getSource()==a41) aufgabe4a(a4c.getSelectedIndex()==1);
		if (e.getSource()==a42) aufgabe4b(a4c.getSelectedIndex()==1, n);
		
		try {n = Integer.parseInt(a5n.getText());} catch(Exception ex) {n = -1;}

		if (e.getSource()==a5) aufgabe5(n);

		try {n = Integer.parseInt(a6n.getText());} catch(Exception ex) {n = -1;}

		if (e.getSource()==a61) aufgabe6a((int)Math.pow(2,n));
		if (e.getSource()==a62) aufgabe6b((int)Math.pow(2,n));

	}
	public void aufgabe1a(int n, int strategie)
	{
		gap();
		println("Blatt 2, Aufgabe 5: Vorgegebenes Gleichungssystem lösen.");
		println("Matrix - Größe: "+n);
		println("Pivot - Strategie: "+strategie);

		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		for (int i=1; i<=n; i++)
			for (int j=1; j<=n; j++)
				A.setValue(new DoubleWrapper(1.0/((double)i+(double)j-1.0)),i,j);

		RingVector b = new RingVector(new DoubleWrapper(0.0), n);
		for (int i=1; i<=n; i++)
			b.setValue(new DoubleWrapper(1.0/((double)n+(double)i-1.0)),i);

		if (n<=10) 
		{
			println(); 
			println("Matrix A: "+A);
			println(); 
			println("Vector b: "+b);
		}
		
		println ("Beginne Algorithmus (bitte warten)...");
		RingVector x = A.solveWithGauss(b, strategie);
		println ("...fertig.");
		
		println(); 
		println("Ergebnisse: ");
		if (n<=10)
			for (int i=1; i<=n; i++)
				println("x_"+i+": "+x.getValue(i));
			
		
		for (int i=1; i<=n/10; i++)
			println("x_"+(i*10)+": "+x.getValue(i*10));
	}
	public void aufgabe1b()
	{
		gap();
		println("Blatt 2, Aufgabe 5: Invertieren einer vorgegebenen Matrix.");
		println("Matrix - Größe: 10");
		println("Pivot - Strategie: Spalten-Pivoting.");

		int n=10;
		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		for (int i=1; i<=n; i++)
			for (int j=1; j<=n; j++)
				A.setValue(new DoubleWrapper(1.0/((double)i+(double)j-1.0)),i,j);

		println(); 
		println("Matrix A: "+A);
		println(); 
				
		println ("Beginne Algorithmus (bitte warten)...");
		for (int k=1; k<=n; k++)
		{
			RingVector b = new RingVector(new DoubleWrapper(0.0),n);
			for (int i=1; i<=n; i++)
				b.setValue(new DoubleWrapper(0.0),i);
			b.setValue(new DoubleWrapper(1.0),k);
			
			println(k+"ter EV: "+b);
			
			RingVector x = A.solveWithGauss(b,1);
			
			println();
			println("Spalte "+k+": "+x);
			println();
		}
		println ("...fertig.");
	}
	public void aufgabe2a()
	{
		gap();
		println("Blatt 4, Aufgabe 5:  QR - Zerlegung.");
		println("Matrix - Größe: 3 (Matrix aus Aufabe 2)");
		
		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),3,3);
		RingVector b = new RingVector(new DoubleWrapper(0.0), 3);
		
		A.setValue(new DoubleWrapper(-4.0),1,1);
		A.setValue(new DoubleWrapper( 5.0),1,2);
		A.setValue(new DoubleWrapper(15.0),1,3);
		A.setValue(new DoubleWrapper( 3.0),2,1);
		A.setValue(new DoubleWrapper( 0.0),2,2);
		A.setValue(new DoubleWrapper(20.0),2,3);
		A.setValue(new DoubleWrapper( 0.0),3,1);
		A.setValue(new DoubleWrapper( 4.0),3,2);
		A.setValue(new DoubleWrapper(25.0),3,3);
		
		b.setValue(new DoubleWrapper(47.0),1);
		b.setValue(new DoubleWrapper(71.0),2);
		b.setValue(new DoubleWrapper(80.0),3);
		
		println();
		println("Matrix A = "+A);
		println();
		println("Vektor b = "+b);
		
		RingMatrix res[] = A.qRDecomposition();
		RingVector y = res[1].solveWithUFromQRDecomposition(b);
		RingVector x = res[0].solveRightUpperTriangleMatrix(y);
		
		println();
		println("Ergebnis: "+x);		
	}
	public void aufgabe2b(int n)
	{
		gap();
		println("Blatt 4, Aufgabe 5: Vorgegebenes Gleichungssystem lösen.");
		println("Matrix - Größe: "+n);

		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		for (int i=1; i<=n; i++)
			for (int j=1; j<=n; j++)
				A.setValue(new DoubleWrapper(1.0/((double)i+(double)j-1.0)),i,j);

		println(); 
		println("Matrix A: "+A);
				
		RingVector b = new RingVector(new DoubleWrapper(0.0), n);
		for (int i=1; i<=n; i++)
			b.setValue(new DoubleWrapper(1.0/((double)n+(double)i-1.0)),i);

		println(); 
		println("Vector b: "+b);
		
		println ("Beginne QR-Zerlegung (bitte warten)...");
		RingMatrix[] res = A.qRDecomposition();
		println ("...fertig. Beginne Gleichungslösung...");
		RingVector y = res[1].solveWithUFromQRDecomposition(b);
		RingVector x = res[0].solveRightUpperTriangleMatrix(y);
		println ("...fertig.");
		
		println(); 
		println("Ergebnisse: ");
		if (n<=10)
			for (int i=1; i<=n; i++)
				println("x_"+i+": "+x.getValue(i));
			
		
		for (int i=1; i<=n/10; i++)
			println("x_"+(i*10)+": "+x.getValue(i*10));
	}
	public void aufgabe2c()
	{
		gap();
		println("Blatt 4, Aufgabe 5: Invertieren einer vorgegebenen Matrix.");
		println("Matrix - Größe: 10");

		int n=10;
		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		for (int i=1; i<=n; i++)
			for (int j=1; j<=n; j++)
				A.setValue(new DoubleWrapper(1.0/((double)i+(double)j-1.0)),i,j);

		println(); 
		println("Matrix A: "+A);
		println(); 
				
		println ("Beginne QR-Zerlegung (bitte warten)...");
		RingMatrix[] res = A.qRDecomposition();
		println ("...fertig. Beginne Gleichungslösen...");
		for (int k=1; k<=n; k++)
		{
			RingVector b = new RingVector(new DoubleWrapper(0.0),n);
			for (int i=1; i<=n; i++)
				b.setValue(new DoubleWrapper(0.0),i);
			b.setValue(new DoubleWrapper(1.0),k);
			
			println(k+"ter EV: "+b);
			
			RingVector y = res[1].solveWithUFromQRDecomposition(b);
			RingVector x = res[0].solveRightUpperTriangleMatrix(y);
			
			println();
			println("Spalte "+k+": "+x);
			println();
		}
		println ("...fertig.");
	}
	public void aufgabe4a(boolean preconditioning)
	{
		gap();
		println("Blatt 8, Aufgabe 4: Lösung eines Gleichungssystem mit cg-Verfahren.");
		println("Gleichungssystem aus Aufgabe 1");
		if (preconditioning) println("Mit Vorkonditionierung");
		else println("Ohne Vorkonditionierung");

		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),3,3);
		A.setValue(new DoubleWrapper(2.0),1,1);
		A.setValue(new DoubleWrapper(-1.0),2,1);
		A.setValue(new DoubleWrapper(0.0),3,1);
		A.setValue(new DoubleWrapper(-1.0),1,2);
		A.setValue(new DoubleWrapper(2.0),2,2);
		A.setValue(new DoubleWrapper(-1.0),3,2);
		A.setValue(new DoubleWrapper(0.0),1,3);
		A.setValue(new DoubleWrapper(-1.0),2,3);
		A.setValue(new DoubleWrapper(2.0),3,3);
		
		RingVector b = new RingVector(new DoubleWrapper(0.0),3);
		b.setValue(new DoubleWrapper(1.0),1);
		b.setValue(new DoubleWrapper(0.0),2);
		b.setValue(new DoubleWrapper(1.0),3);

		println(); 
		println("Matrix A: "+A);
		println(); 
		
		println("Vector b: "+b);
				
		println ("Beginne cg-Algorithmus (bitte warten)...");
		
		RingVector x = A.solveWithCGMethod(b, 0.00001, preconditioning, this);
		
		println ("...fertig.");
		println ();
		println ("Vector x: "+x);
	}
	public void aufgabe4b(boolean preconditioning, int n)
	{
		gap();
		println("Blatt 8, Aufgabe 4: Lösung eines Gleichungssystem mit cg-Verfahren.");
		println("Gleichungssystem von Blatt 6, n = "+n);
		if (preconditioning) println("Mit Vorkonditionierung");
		else println("Ohne Vorkonditionierung");

		double h = Math.PI / ((double)2*n);
		
		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		RingVector b = new RingVector(new DoubleWrapper(0.0),n);
		
		for (int j=1; j<=n; j++)
		{
			if (j>1) A.setValue(new DoubleWrapper(1.0),j,j-1);
			A.setValue(new DoubleWrapper(-2.0+h*h),j,j);
			if (j<n) A.setValue(new DoubleWrapper(1.0),j,j+1);
			b.setValue(new DoubleWrapper(h*h*Math.pow(Math.E,h*j)),j);
		}

		println(); 
		println("Matrixgröße: "+n);
		println(); 
		
		println("Vector b: "+b);
				
		println ("Beginne cg-Algorithmus (bitte warten)...");
		
		RingVector x = A.solveWithCGMethod(b, 0.00001, preconditioning, this);
		
		println ("...fertig.");
		println ();
		println ("Vector x: "+x);
	}
	public void aufgabe5(int n)
	{
		gap();
		println("Blatt 10, Aufgabe 5: Eigenwerte der Hilbertmatrix.");
		println(); 
		println("Matrixgröße: "+n);
		println(); 

		RingMatrix A = new RingMatrix(new DoubleWrapper(0.0),n,n);
		
		for (int i=1; i<=n; i++)
			for (int j=1; j<=n; j++)
				A.setValue(new DoubleWrapper(1.0/(double)(i+j-1)),i,j);
		
		println ("Beginne Eigenwertberechnung (bitte warten)...");
		
		RingVector x = A.eigenvalues(0.00001);
		
		println ("...fertig.");
		println ();
		println ("Eigenwerte sind: "+x);
	}
	public void aufgabe6a(int n)
	{
		gap();
		println("Blatt 12, Aufgabe 4: Interpolation mit FFT.");
		println(); 
		println("Funktion : f(x) = x^(1/3)");
		println(); 
		
		RingVector r = new RingVector(new DoubleComplex(), n);
		for (int k=0; k<n; k++)
		{
			double xk = Math.cos((2*k+1)*Math.PI / (2*n));		// Tschebycheff-Nullstelle
			r.setValue(new DoubleComplex(Math.pow( (xk+1)*32 , 1.0/3.0)), k+1);
		}
		
		println("Beginne Interpolation...");
		RingVector d = r.getPolynomialRepresentation();
		println("...fertig.");
		println(); 
		
		println ("Fehler an den Tschebychef-Nullstellen:");
		for (int k=0; (k<n) && (k<10); k++)
		{
			double xk = Math.cos((2*k+1)*Math.PI / (2*n));		// Tschebycheff-Nullstelle
			DoubleComplex dist =  new DoubleComplex(Math.pow( (xk+1)*32 , 1.0/3.0));
			dist = dist.subtract((DoubleComplex)d.evalFFT(new DoubleComplex(xk)));
			println ("an x"+k+" : "+dist.absolute());
		}
		
		println();
		println("Fehler an den Stellen -1+j/10 (transformiert):");
		
		for (int j=1; j<=20; j++)
		{
			DoubleComplex dist = (DoubleComplex)d.evalFFT(new DoubleComplex(-1.0 + j/20.0));
			dist = dist.subtract(new DoubleComplex(Math.pow( (j/10.0)*32 , 1.0/3.0)));
			println ("j= "+j+" : "+dist.absolute());
		}		
	}
	public void aufgabe6b(int n)
	{
		gap();
		println("Blatt 12, Aufgabe 4: Interpolation mit FFT.");
		println(); 
		println("Funktion : f(x) = log x");
		println(); 
		
		RingVector r = new RingVector(new DoubleComplex(), n);
		for (int k=0; k<n; k++)
		{
			double xk = Math.cos((2*k+1)*Math.PI / (2*n));		// Tschebycheff-Nullstelle
			r.setValue(new DoubleComplex(Math.log( (xk+1)/2 )), k+1);
		}
		
		println("Beginne Interpolation...");
		RingVector d = r.getPolynomialRepresentation();
		println("...fertig.");
		println(); 
		
		println ("Fehler an den Tschebychef-Nullstellen:");
		for (int k=0; (k<n) && (k<10); k++)
		{
			double xk = Math.cos((2*k+1)*Math.PI / (2*n));		// Tschebycheff-Nullstelle
			DoubleComplex dist =  new DoubleComplex(Math.log( (xk+1)/2 ));
			dist = dist.subtract((DoubleComplex)d.evalFFT(new DoubleComplex(xk)));
			println ("an x"+k+" : "+dist.absolute());
		}
		
		println();
		println("Fehler an den Stellen -1+j/10 (transformiert):");
		
		for (int j=1; j<=20; j++)
		{
			DoubleComplex dist = (DoubleComplex)d.evalFFT(new DoubleComplex(-1.0 + j/20.0));
			dist = dist.subtract(new DoubleComplex(Math.log( (j/20.0) )));
			println ("j= "+j+" : "+dist.absolute());
		}		
	}
	public void gap()
	{
		ta.append("\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
	}
	public void init()
	{
		init(this);
	}
	public void init(Container c)
	{
		c.setLayout(new BorderLayout());
		ta = new TextArea();
		ta.setEditable(false);
		
		c.add(ta, BorderLayout.CENTER);
		
		Panel buttonpanel = new Panel(new GridLayout(5,4));
		c.add(buttonpanel, BorderLayout.SOUTH);

		a11 = new Button("Blatt 2a"); a11.addActionListener(this);
		a12 = new Button("Blatt 2b"); a12.addActionListener(this);
		
		a1n = new TextField("10");
		Panel p = new Panel(new GridLayout(1,2));
		p.add(new Label("n ="));
		p.add(a1n);
		
		a1c = new Choice();
		a1c.add("Ohne Pivotsuche");
		a1c.add("Spalten - Pivotsuche");
		a1c.add("Volle Pivotsuche");
		
		buttonpanel.add(a1c);
		buttonpanel.add(p);
		buttonpanel.add(a11);
		buttonpanel.add(a12);
		
		a21 = new Button("Blatt 4a"); a21.addActionListener(this);
		a22 = new Button("Blatt 4b"); a22.addActionListener(this);
		a23 = new Button("Blatt 4c"); a23.addActionListener(this);

		a2n = new TextField("10");
		p = new Panel(new GridLayout(1,2));
		p.add(new Label("n ="));
		p.add(a2n);

		buttonpanel.add(p);
		buttonpanel.add(a21);
		buttonpanel.add(a22);
		buttonpanel.add(a23);
		
		a41 = new Button("Blatt 8a"); a41.addActionListener(this);
		a42 = new Button("Blatt 8b"); a42.addActionListener(this);
		
		a4n = new TextField("30");
		p = new Panel(new GridLayout(1,2));
		p.add(new Label("n ="));
		p.add(a4n);
		
		a4c = new Choice();
		a4c.add("Ohne Vorkonditionierung");
		a4c.add("Mit Vorkonditionierung");
		
		buttonpanel.add(a4c);
		buttonpanel.add(p);
		buttonpanel.add(a41);
		buttonpanel.add(a42);
		
		a5 = new Button("Blatt 10"); a5.addActionListener(this);
		
		a5n = new TextField("10");
		p = new Panel(new GridLayout(1,2));
		p.add(new Label("n ="));
		p.add(a5n);

		buttonpanel.add(a5);
		buttonpanel.add(a5n);
		buttonpanel.add(new Panel());
		buttonpanel.add(new Panel());

		a61 = new Button("Blatt 12a"); a61.addActionListener(this);
		a62 = new Button("Blatt 12b"); a62.addActionListener(this);
		
		a6n = new TextField("2");
		p = new Panel(new GridLayout(1,2));
		p.add(new Label("n = 2^"));
		p.add(a6n);

		buttonpanel.add(p);
		buttonpanel.add(a61);
		buttonpanel.add(a62);
		buttonpanel.add(new Panel());
	}
	public static void main (String[] arg)
	{
		Frame frame = new Frame();
		frame.setTitle("Praktische Mathematik");
		PraMaApplet app = new PraMaApplet(frame);
		frame.addWindowListener(app);
		app.init(frame);
		frame.setSize(500,350);
		frame.show();
	}
	public void print(String s)
	{
		ta.append(s);
	}
	public void println()
	{
		ta.append("\r\n");
	}
	public void println(String s)
	{
		ta.append(s+"\r\n");
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {frame.dispose(); System.exit(0);}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}
