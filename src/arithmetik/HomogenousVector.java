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

import java.io.*;

/*
	Darstellung von Vektoren der Ebene.
	Die HomogenousVectoren werden in homogenen Koordinaten angegeben,
	jedes als RExpression. Sie sind auf diesen Typ spezialisiert.
*/

public class HomogenousVector
{
	public RExpression x,y,z;
	private static PrintWriter docfile;
	public static boolean DEBUG;

	HomogenousVector () 
	{
		x = new RExpression();
		y = new RExpression();
		z = new RExpression(Qelement.ONE);
	}
	// Bezeichnernummern
	HomogenousVector (int identiferNr1, int identiferNr2, int identiferNr3)
	{
		x = new RExpression(identiferNr1);
		y = new RExpression(identiferNr2);
		z = new RExpression(identiferNr3);
	}
	HomogenousVector (HomogenousVector copy)
	{
		x = new RExpression(copy.x);
		y = new RExpression(copy.y);
		z = new RExpression(copy.z);
	}
/**
 * Insert the method's description here.
 * Creation date: (14.03.01 12:12:28)
 * @param c arithmetik.RExpression
 * @param y arithmetik.RExpression
 */
public HomogenousVector(RExpression x, RExpression y) 
{
	this.x = new RExpression(x);
	this.y = new RExpression(y);
	this.z = new RExpression(Qelement.ONE);
}
	public HomogenousVector (RExpression v_x, RExpression v_y, RExpression v_z)
	{
		x = new RExpression(v_x);
		y = new RExpression(v_y);
		z = new RExpression(v_z);
	}
	public HomogenousVector add (HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Addition von 2 HomogenousVectoren *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 1: "+this.infos()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.infos()+"\r\n");
		}
		HomogenousVector[] ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg2;
		HomogenousVector[] ret = erweitert(ove);
		
		HomogenousVector ergvec = new HomogenousVector ( (ret[0].x).add(ret[1].x),
						             (ret[0].y).add(ret[1].y),
							          ret[0].z);
		ergvec.clean();

		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+ergvec.debugEvaluation()+"\r\n\r\n");
			docfile.print("Ergebnis  : "+ergvec.infos()+"\r\n\r\n");
		}
		return ergvec;
	}
	// Kürzt den HomogenousVector so weit wie möglich, bereinigt unsaubere 0-Darstellungen,
	public void clean()
	{
		RExpression[] arg = new RExpression[3]; arg[0]=x; arg[1]=y; arg[2]=z;
		RExpression[] erg = RExpression.lazyGcdFaks(arg);
		x = erg[0]; 
		y = erg[1];
		z = erg[2];
//		x.faktor.faktor = x.faktor.faktor.divide(z.faktor.faktor);
//		y.faktor.faktor = y.faktor.faktor.divide(z.faktor.faktor);
//		z.faktor.faktor = new Qelement(Qelement.ONE);
		if (isZero()) z = new RExpression(Qelement.ONE);
	}
	// x1*y2 - y1*x2
	RQuotientExp crossProduct (HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** crossProductprodukt *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
		}

		RQuotientExp erg = new RQuotientExp( (x.multiply(arg2.y)).add(y.multiply(arg2.x.negate())), 
			                        z.multiply(arg2.z) );
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
		}
		return erg;
	}
	String debugEvaluation()
	{
		return "("+(x.debugEvaluation() / z.debugEvaluation())+","+(y.debugEvaluation() / z.debugEvaluation())+")";
	}
	// Erweitert alle HomogenousVectoren der Liste so, dass sie gleich z-Werte haben.
	// VORSICHT: Nur der 0. HomogenousVector hat einen gültigen z-Wert!!!
	// die der anderen sind identisch (ausrechnen dauert zu lange)
	public static HomogenousVector[] erweitert (HomogenousVector[] arg)
	{
		RExpression[] ove = new RExpression[arg.length];
		for (int i=0; i<arg.length; i++) ove[i]=arg[i].z;
		RExpression[] ret = RExpression.lazyScmFaks(ove);
		HomogenousVector[] erg = new HomogenousVector[arg.length];
		for (int i=0; i<arg.length; i++)
			erg[i]= new HomogenousVector (arg[i].x.multiply(ret[i]),
							    arg[i].y.multiply(ret[i]),
								new RExpression());
		erg[0].z = arg[0].z.multiply(ret[0]);
		return erg;
	}
	String infos()
	{
		return "["+x.infos()+" , "+y.infos()+" , "+z.infos()+"]";
	}
	/*
	Wie bei on_cut_cc beschrieben, kann man (ohne sqrtn) den Punkt
	auf der Verbindungsgerade zweier Kreismittelpunkte A und B berechnen,
	an dem AB von der Verbindungsgeraden der beiden Kreisschnittpunkte
	senkrecht geschnitten wird; über das "senkrecht" läßt sich dann die
	Verbindungsgerade der Kreisschnittpunkte bestimmen. Schneiden sich
	alle 3 Kreise in einem Punkt, so laufen die drei dadurch bestimmten
	Geraden durch den gemeinsamen Schnittpunkt. In der Routine sind
	einige Beschleunigungen eingebaut, aber es ist alles diese Idee.
	
	boolean is_circle_meet(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4,
						   HomogenousVector arg5, HomogenousVector arg6)
	{
		if (DEBUG) docfile.print(" *** Test auf gemeinsamen Schnitt von Kreisen *** \r\n");

		HomogenousVector[] ove = new HomogenousVector[4]; ove[0]=this; ove[1]=arg2; ove[2]=arg3; ove[3]=arg4;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector A = ret[0];
		HomogenousVector B = ret[2]; B.z=new RExpression (ret[0].z);
		HomogenousVector Q = ret[1]; Q.z=new RExpression (ret[0].z);
		HomogenousVector R = ret[3]; R.z=new RExpression (ret[0].z);

		RQuotientExp fak1 = new RQuotientExp( (A.my_strecke(Q).zaehler).add
									(B.my_strecke(R).zaehler.negate()).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );
		RQuotientExp fak2 = new RQuotientExp( (A.my_strecke(Q).zaehler.negate()).add
									(B.my_strecke(R).zaehler).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );

		ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg3;
		ret = erweitert(ove);

		HomogenousVector AB_P = new HomogenousVector((ret[0].x.multiply(fak2.zaehler)).add(ret[1].x.multiply(fak1.zaehler)),
								 (ret[0].y.multiply(fak2.zaehler)).add(ret[1].y.multiply(fak1.zaehler)),
								  ret[0].z.multiply(fak1.nenner) );
/*		HomogenousVector AB_Q = new HomogenousVector((ret[0].x.multiply(fak2.zaehler)).add(ret[1].x.multiply(fak1.zaehler)).subtract
								 (ret[1].y.multiply(fak1.nenner )).add(ret[0].y.multiply(fak1.nenner )),
								 (ret[0].y.multiply(fak2.zaehler)).add(ret[1].y.multiply(fak1.zaehler)).subtract
								 (ret[0].x.multiply(fak1.nenner )).add(ret[1].x.multiply(fak1.nenner )),
								  ret[0].z.multiply(fak1.nenner) );

		HomogenousVector AB_Q = new HomogenousVector (AB_P.x.add( fak1.nenner.multiply(ret[1].y.subtract(ret[0].y)) ),
								  AB_P.y.add( fak1.nenner.multiply(ret[0].x.subtract(ret[1].x)) ),
								  AB_P.z );

		ove = new HomogenousVector[4]; ove[0]=arg3; ove[1]=arg4; ove[2]=arg5; ove[3]=arg6;
		ret = erweitert(ove);

		HomogenousVector Bs = ret[0];
		HomogenousVector C  = ret[2];  C.z=new RExpression (ret[0].z);
		HomogenousVector Rs = ret[1]; Rs.z=new RExpression (ret[0].z);
		HomogenousVector S  = ret[3];  S.z=new RExpression (ret[0].z);

		RQuotientExp fak1s = new RQuotientExp( (Bs.my_strecke(Rs).zaehler).add
									 (C .my_strecke(S).zaehler.negate()).add
									 (Bs.my_strecke(C).zaehler)  , 
									 (Bs.my_strecke(C).zaehler).multiply(new RExpression(Qelement.TWO)) );
		RQuotientExp fak2s = new RQuotientExp( (Bs.my_strecke(Rs).zaehler.negate()).add
									 (C .my_strecke(S).zaehler).add
									 (Bs.my_strecke(C).zaehler)  , 
									 (Bs.my_strecke(C).zaehler).multiply(new RExpression(Qelement.TWO)) );

		ove = new HomogenousVector[2]; ove[0]=arg3; ove[1]=arg5;
		ret = erweitert(ove);

		HomogenousVector BC_P = new HomogenousVector((ret[0].x.multiply(fak2s.zaehler)).add(ret[1].x.multiply(fak1s.zaehler)),
								 (ret[0].y.multiply(fak2s.zaehler)).add(ret[1].y.multiply(fak1s.zaehler)),
								  ret[0].z.multiply(fak1s.nenner) );
/*		HomogenousVector BC_Q = new HomogenousVector((ret[0].x.multiply(fak2s.zaehler)).add(ret[1].x.multiply(fak1s.zaehler)).subtract
								 (ret[1].y.multiply(fak1s.nenner )).add(ret[0].y.multiply(fak1s.nenner )),
								 (ret[0].y.multiply(fak2s.zaehler)).add(ret[1].y.multiply(fak1s.zaehler)).subtract
								 (ret[0].x.multiply(fak1s.nenner )).add(ret[1].x.multiply(fak1s.nenner )),
								  ret[0].z.multiply(fak1s.nenner) );

		HomogenousVector BC_Q = new HomogenousVector(BC_P.x.add( fak1s.nenner.multiply(ret[1].y.subtract(ret[0].y)) ),
								 BC_P.y.add( fak1s.nenner.multiply(ret[0].x.subtract(ret[1].x)) ),
								 BC_P.z );

		AB_P.clean(); BC_P.clean(); AB_Q.clean(); BC_Q.clean();
		HomogenousVector CUT = AB_P.on_cut(AB_Q,BC_P,BC_Q);
		
		String out;
		if ((Qelement.warning) && (DEBUG))
		{
			docfile.print("\r\n\r\n ********************************* \r\n");
			docfile.print(        " *      LONG-GRENZE ERREICHT     * \r\n");
			docfile.print(        " ********************************* \r\n\r\n");
		}
		if (DEBUG)
		{
			       out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Argument 5   : "+arg5.debugEvaluation()+"\r\n";
				   out += "Argument 6   : "+arg6.debugEvaluation()+"\r\n";
				   out += "A            : "+A   .debugEvaluation()+"\r\n";
				   out += "Q            : "+Q   .debugEvaluation()+"\r\n";
				   out += "B            : "+B   .debugEvaluation()+"\r\n";
				   out += "R            : "+R   .debugEvaluation()+"\r\n";
				   out += "fak1			: "+fak1.debugEvaluation()+"\r\n";
				   out += "fak2			: "+fak2.debugEvaluation()+"\r\n";
				   out += "AB_P         : "+AB_P.debugEvaluation()+"\r\n";
				   out += "AB_Q         : "+AB_Q.debugEvaluation()+"\r\n";
				   out += "B (2. Gang)  : "+Bs  .debugEvaluation()+"\r\n";
				   out += "R (2. Gang)  : "+Rs  .debugEvaluation()+"\r\n";
				   out += "C            : "+C   .debugEvaluation()+"\r\n";
				   out += "S            : "+S   .debugEvaluation()+"\r\n";
				   out += "fak1 (2.Gang): "+fak1s.debugEvaluation()+"\r\n";
				   out += "fak2 (2.Gang): "+fak2s.debugEvaluation()+"\r\n";
				   out += "BC_P         : "+BC_P.debugEvaluation()+"\r\n";
				   out += "BC_Q         : "+BC_Q.debugEvaluation()+"\r\n";
				   out += "CUT          : "+CUT .debugEvaluation()+"\r\n";
			docfile.print(out);
		}

		RQuotientExp way1 = CUT .strecke(arg3);
		RQuotientExp way2 = arg4.strecke(arg3);
		boolean erg = way1.equals()(way2);
		
		if (DEBUG)
		{
				   out  = "Strecke 1    : "+way1.debugEvaluation()+"\r\n";
				   out += "Strecke 2    : "+way2.debugEvaluation()+"\r\n";
		  if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	*/
	public boolean is_circle_meet(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4,
						   HomogenousVector arg5, HomogenousVector arg6)
	{
		if (DEBUG) docfile.print(" *** Schnittpunkt zweier Kreise *** \r\n");

		HomogenousVector[] ove = new HomogenousVector[4]; ove[0]=this; ove[1]=arg2; ove[2]=arg3; ove[3]=arg4;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector A = ret[0];
		HomogenousVector B = ret[2]; B.z=new RExpression (ret[0].z);
		HomogenousVector Q = ret[1]; Q.z=new RExpression (ret[0].z);
		HomogenousVector R = ret[3]; R.z=new RExpression (ret[0].z);

		RQuotientExp lambda = new RQuotientExp( (A.my_strecke(Q).zaehler).add
									(B.my_strecke(R).zaehler.negate()).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );
		// onelambda = 1-lambda.
		RQuotientExp onelambda = new RQuotientExp( (A.my_strecke(Q).zaehler.negate()).add
									(B.my_strecke(R).zaehler).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );

		ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg3;
		ret = erweitert(ove);

		HomogenousVector P = new HomogenousVector((ret[0].x.multiply(onelambda.zaehler)).add(ret[1].x.multiply(lambda.zaehler)),
							  (ret[0].y.multiply(onelambda.zaehler)).add(ret[1].y.multiply(lambda.zaehler)),
							   ret[0].z.multiply(lambda.nenner) );

		RQuotientExp mue_sqr = ((A.my_strecke(Q)).divide(A.my_strecke(B))).subtract(lambda.sqr());
		RQuotientExp mue = mue_sqr.sqrt();

		HomogenousVector move = new HomogenousVector ((ret[1].y.multiply(mue.zaehler)).subtract(ret[0].y.multiply(mue.zaehler)),
								  (ret[0].x.multiply(mue.zaehler)).subtract(ret[1].x.multiply(mue.zaehler)),
								   ret[0].z.multiply(mue.nenner) );

		HomogenousVector C = P.add(move);
		HomogenousVector D = P.subtract(move);
		RQuotientExp way1 = arg6.strecke(arg5);
		RQuotientExp way2 = C.strecke(arg5);
		RQuotientExp way3 = D.strecke(arg5);
		
		boolean erg = ((way2.equals(way1)) || (way3.equals(way1)));
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Argument 5   : "+arg5.debugEvaluation()+"\r\n";
				   out += "Argument 6   : "+arg6.debugEvaluation()+"\r\n";
				   out += "P            : "+P   .debugEvaluation()+"\r\n";
				   out += "move         : "+move.debugEvaluation()+"\r\n";
				   out += "lambda	    : "+lambda.debugEvaluation()+"\r\n";
				   out += "mue_sqr      : "+mue_sqr.debugEvaluation()+"\r\n";
				   out += "mue          : "+mue .debugEvaluation()+"\r\n";
				   out += "C            : "+C   .debugEvaluation()+"\r\n";
				   out += "D            : "+D   .debugEvaluation()+"\r\n";
				   out += "Strecke 1    : "+way1.debugEvaluation()+"\r\n";
				   out += "Strecke 2    : "+way2.debugEvaluation()+"\r\n";
				   out += "Strecke 3    : "+way3.debugEvaluation()+"\r\n";
		  if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
/*	Der Wert der Der QPolynomialinaten ist genau
	dann 0, wenn die 3 HomogenousVectoren kollinear sind.
	Anmerkung: Ich bin mir noch gar nicht ganz sicher, warum das
	eigentlich so ist. Muss ich noch multiply durchrechnen.
	TvO, 21.12.98
	Inzwischen erledigt. TvO, 31.1.99
	Seien die 3 Punkte also gegeben durch A = (a1,a2,a3), B = (b1,b2,b3)
	und C = (c1,c2,c3), dann gilt es zu berechnen:

	I a1  b1  c1 I
	I a2  b2  c2 I = a1*(b2*c3-c2*b3) - b1*(a2*c3-c2*a3) + c1*(a2*b3-b2*a3)
	I a3  b3  c3 I 

	Seien L,M,N die jeweiligen 2x2-DeFastPolynomialinanten.
*/
	public boolean is_collinear(HomogenousVector arg2, HomogenousVector arg3)
	{
		if (DEBUG) docfile.print(" *** Test auf Kollinearität *** \r\n");

		RExpression L = (arg2.y.multiply(arg3.z)).add(arg3.y.multiply(arg2.z.negate()));
		RExpression M = (y.multiply(arg3.z.negate())).add(arg3.y.multiply(z));
		RExpression N = (y.multiply(arg2.z)).add(arg2.y.multiply(z.negate()));

		RExpression totest = (x.multiply(L)).add((arg2.x.multiply(M)).add(arg3.x.multiply(N))); 
		boolean erg = totest.isZero();

		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "1. Teilmatrix: "+L   .debugEvaluation()+"\r\n";
				   out += "2. Teilmatrix: "+M   .debugEvaluation()+"\r\n";
				   out += "3. Teilmatrix: "+N   .debugEvaluation()+"\r\n";
				   out += "Zu testen    : "+totest.debugEvaluation()+"\r\n";
			
				   out += "ex. zu testen: "+totest.toString()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	public boolean is_concur(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4, HomogenousVector arg5, HomogenousVector arg6)
	{
		if (DEBUG) docfile.print(" *** Test auf gemeinsamen Schnitt *** \r\n");

		HomogenousVector X = arg3.on_cut(arg4,arg5,arg6);
		boolean erg = is_collinear(arg2, X);
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Argument 5   : "+arg5.debugEvaluation()+"\r\n";
				   out += "Argument 6   : "+arg6.debugEvaluation()+"\r\n";
				   out += "Schnitt(4,5,6):"+X   .debugEvaluation()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;

	}
	public boolean is_one_circle(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4)
	{
		if (DEBUG) docfile.print(" *** Test auf gemeinsamen Umkreis *** \r\n");

		HomogenousVector A =       this.on_line(arg2,new RQuotientExp(Qelement.HALF));
		HomogenousVector B =       this.on_thales_and_rect(arg2);
		HomogenousVector C =		 arg2.on_line(arg3,new RQuotientExp(Qelement.HALF));
		HomogenousVector D =		 arg2.on_thales_and_rect(arg3);
		HomogenousVector E =		 arg3.on_line(arg4,new RQuotientExp(Qelement.HALF));
		HomogenousVector F =		 arg3.on_thales_and_rect(arg4);

		boolean erg = A.is_concur(B,C,D,E,F);
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Mitte (1,2)  :"+A   .debugEvaluation()+"\r\n";
				   out += "Senke (1,2)  :"+B   .debugEvaluation()+"\r\n";
				   out += "Mitte (3,4)  :"+C   .debugEvaluation()+"\r\n";
				   out += "Senke (3,4)  :"+D   .debugEvaluation()+"\r\n";
				   out += "Mitte (5,6)  :"+E   .debugEvaluation()+"\r\n";
				   out += "Senke (5,6)  :"+F   .debugEvaluation()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	public boolean is_parallel(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4)
	{
		if (DEBUG) docfile.print(" *** Test auf Parallelität *** \r\n");

		boolean erg = ((arg2.add(this.negate())).crossProduct(arg4.add(arg3.negate()))).isZero();
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	public boolean is_perpendicular(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4)
	{
		if (DEBUG) docfile.print(" *** Test auf rechten Winkel *** \r\n");

		boolean erg = ((arg2.add(this.negate())).scalarProduct(arg4.add(arg3.negate()))).isZero();
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	public boolean isEqual(HomogenousVector arg2)
	{
		return subtract(arg2).isZero();
	}
	public boolean isEqualAnkle(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4, HomogenousVector arg5, HomogenousVector arg6)
	{
		if (DEBUG) {
			docfile.print(" *** Test auf gleichen Winkel *** \r\n");
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Argument 5   : "+arg5.debugEvaluation()+"\r\n";
				   out += "Argument 6   : "+arg6.debugEvaluation()+"\r\n";
			docfile.print (out);
		}

		HomogenousVector diffvec1 = this.subtract(arg2);
		HomogenousVector diffvec2 = arg3.subtract(arg2);
		HomogenousVector diffvec3 = arg4.subtract(arg5);
		HomogenousVector diffvec4 = arg6.subtract(arg5);

		RQuotientExp denum1 = (diffvec1.length()).multiply(diffvec2.length());
		RQuotientExp cos1 = (diffvec1.scalarProduct(diffvec2)).divide(denum1);
//		RQuotientExp sin1 = (diffvec1.crossProduct(diffvec2)).divide(denum1);
		RQuotientExp denum2 = (diffvec3.length()).multiply(diffvec4.length());
		RQuotientExp cos2 = (diffvec3.scalarProduct(diffvec4)).divide(denum2);
//		RQuotientExp sin2 = (diffvec3.crossProduct(diffvec4)).divide(denum1);

		boolean erg = (cos1.equals(cos2));
		
		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "Argument 5   : "+arg5.debugEvaluation()+"\r\n";
				   out += "Argument 6   : "+arg6.debugEvaluation()+"\r\n";
				   out += "Cosinus 1    : "+cos1.debugEvaluation()+"\r\n";
				   out += "Nenner 1     : "+denum1.debugEvaluation()+"\r\n";
				   out += "Cosinus 2    : "+cos2.debugEvaluation()+"\r\n";
				   out += "Nenner 2     : "+denum2.debugEvaluation()+"\r\n";
	      if (erg) out += "Ergebnis     : wahr\r\n\r\n";
		     else  out += "Ergebnis     : falsch\r\n\r\n"; 
			docfile.print(out);
		}
		return erg;
	}
	public boolean isZero()
	{
		boolean erg = (x.isZero() && y.isZero());
		if ((erg) && (z.isZero())) throw RQuotientExp.ZeroByZeroException;
		return erg;
	}
	public RQuotientExp length()
	{
		RQuotientExp erg = (scalarProduct(this)).sqrt();
		if (DEBUG)
			docfile.print ("Ergebnis der Längenberechnung: "+erg.debugEvaluation());
		return erg;
	}
	// Berechnet die Strecke zwischen 2 Punkten ohne Berücksichtigung
	// der z-Koordinaten (respektive des Nenners). Wird nur von 
	// is_circle_meet sozusagen als "Auslagerung" benutzt, außerdem
	// noch von on_cut_cc und on_cut_lc zu gleichem Zwecke.
	private RQuotientExp my_strecke(HomogenousVector arg2)
	{
		return new RQuotientExp( (x.subtract(arg2.x).sqr()).add
			                (y.subtract(arg2.y).sqr()),
							new RExpression(Qelement.ONE) );
	}
	HomogenousVector negate()
	{
		return new HomogenousVector (x.negate(),y.negate(),z);
	}
	/*
		(X-A)^2=(X-B)^2=(X-C)^2 = r^2

	Ausgehend von dieser Gleichung und mit x0 := r^2-X^2 erhält man:
	x0+2a1x1+2a2x2 = A^2
	x0+2b1x1+2b2x2 = B^2
	x0+2c1x1+2c2x2 = C^2

	und daraus nach einigem Umformen:

	x1 = A^2*(b2-c2) - B^2*(a2-c2) + C^2*(a2-b2)
	     ---------------------------------------
		 2n * [a2(c1-b1)+a1(b2-c2)+b1*c2-c1*b2 ]

  	x2 = A^2*(b1-c1) - B^2*(a1-c1) + C^2*(a1-b1)
	     ---------------------------------------
		 2n * [a1(c2-b2)+a2(b1-c1)+b2*c1-c2*b1 ]

	mit entsprechenden Ersetzungen:

	x1 = ( A*D - B*E + C*F) / G * ( a2*H + a1*D + I - J)
	x2 = (-A*H - B*K + C*L) / G * (-a1*D - a2*H + J - I)
	   = ( A*H + B*K - C*L) / G * ( a2*H + a1*D + I - J)
	*/

	public HomogenousVector on_center_of_circle(HomogenousVector arg2, HomogenousVector arg3)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Kreismittelpunkt *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Argument 3: "+arg3.debugEvaluation()+"\r\n");
		}

		HomogenousVector[] ove = new HomogenousVector[3]; ove[0]=this; ove[1]=arg2; ove[2]=arg3;
		HomogenousVector[] ret = erweitert(ove);

		RExpression A = (ret[0].x.sqr()).add(ret[0].y.sqr());
		RExpression B = (ret[1].x.sqr()).add(ret[1].y.sqr());
		RExpression C = (ret[2].x.sqr()).add(ret[2].y.sqr());
		RExpression D =  ret[1].y.subtract(ret[2].y);
		RExpression E =  ret[0].y.subtract(ret[2].y);
		RExpression F =  ret[0].y.subtract(ret[1].y);
		RExpression G =  ret[0].z.multiply(new RExpression(Qelement.TWO));
		RExpression H =  ret[2].x.subtract(ret[1].x);
		RExpression I =  ret[1].x.multiply(ret[2].y);
		RExpression J =  ret[2].x.multiply(ret[1].y);
		RExpression K =  ret[0].x.subtract(ret[2].x);
		RExpression L =  ret[0].x.subtract(ret[1].x);

		RExpression e1 = A.multiply(D).subtract
				  (B.multiply(E)).add
				  (C.multiply(F));
		RExpression e2 =(A.multiply(H)).add
				  (B.multiply(K)).subtract
				  (C.multiply(L));
		RExpression e3  = G.multiply((ret[0].y.multiply(H)).add
						  (ret[0].x.multiply(D)).add(I).subtract(J));

		HomogenousVector erg = new HomogenousVector (e1,e2,e3);
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Kreismittelpunkt  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;

	}
	/*
	Betrachte: sin Phi =    (2t) / (1+t^2),
	 		   cos Phi = (1-t^2) / (1+t^2)
	
	Sei Kreis gegeben durch M (Mittelpunkt) und Q 
	mit M+Q auf dem Kreis. 
	Sei Q' senkrecht zu Q mit M+Q' auf dem Kreis.
	Dann ist ein beliebigier Punkt P auf dem Kreis:
	
	P = M + cos(Phi) * Q + sin(Phi) * Q'
	  = ( m1 + [(1-t^2)] q1 - [(2t)] q2 )
	    ( m2 + [(1-t^2)] q2 + [(2t)] q1 )
		( (1+t^2) )
	
	mit entsprechender Ersetzung:
	 
	  = ( m1 + SIN*q1 - COS*q2 )
	    ( m2 + SIN*q2 + COS*q1 )
		( n * (1+t^2)		   )
	*/
	// this ist Mittelpunkt, arg2 ist Punkt auf Umkreis.
	public HomogenousVector on_circle (HomogenousVector arg2, RExpression winkel)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Punkt auf Kreisumfang *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Winkel    : "+winkel.debugEvaluation()+"\r\n");
		}

		HomogenousVector[] ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg2;
		HomogenousVector[] ret = erweitert(ove);

		RExpression q1 = ret[1].x.subtract(ret[0].x);
		RExpression q2 = ret[1].y.subtract(ret[0].y);

		RExpression SIN = (winkel.multiply(winkel).negate()).add(new RExpression(Qelement.ONE));
		RExpression COS = winkel.multiply(new RExpression(Qelement.TWO));

		RExpression n = (winkel.sqr()).add(new RExpression(Qelement.ONE));
		
		RExpression e1 = (ret[0].x.multiply(n)).add(SIN.multiply(q1)).add(COS.multiply(q2.negate()));
		RExpression e2 = (ret[0].y.multiply(n)).add(SIN.multiply(q2)).add(COS.multiply(q1));
		RExpression e3 = ret[0].z.multiply(winkel.multiply(winkel).add(new RExpression(Qelement.ONE)));

		HomogenousVector erg = new HomogenousVector (e1,e2,e3);
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;
	}
	/*
	HomogenousVector auf dem Schnitt der beiden durch die 4 HomogenousVectoren gegebenen
	Geraden. Berechnet streng in homogenen Koordinaten.
	
	Seien so die 4 Argumente gegeben durch A=(a1,a2,a3), B=(b1,b2,b3), 
	C = (c1,c2,c3) und D = (d1,d2,d3).	  
	Für die Schnittgeraden gilt dann:

	g = AB = (a2*b3-a3*b2, a3*b1-a1*b3, a1*b2-a2*b1)
	h = CD = (c2*d3-c3*d2, c3*d1-c1*d3, c1*d2-c2*d1)

	E = gh = (g2*h3-g3*h2, g3*h1-g1*h3, g1*h2-g2*h1)
	  = [ ((a3*b1-a1*b3)(c1*d2-c2*d1)-(a1*b2-a2*b1)(c3*d1-c1*d3)),
		  ((a1*b2-a2*b1)(c2*d3-c3*d2)-(a2*b3-a3*b2)(c1*d2-c2*d1)),
		  ((a2*b3-a3*b2)(c3*d1-c1*d3)-(a3*b1-a1*b3)(c2*d3-c3*d2)) ]

	  = (L*M-N*O, N*P-Q*M, Q*O-L*P) mit enstprechender Belegung.
	*/
	public HomogenousVector on_cut(HomogenousVector b, HomogenousVector c, HomogenousVector d)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Schnittpunkt *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+b   .debugEvaluation()+"\r\n");
			docfile.print("Argument 3: "+c   .debugEvaluation()+"\r\n");
			docfile.print("Argument 4: "+d   .debugEvaluation()+"\r\n");
		}

		RExpression L = (z.multiply(b.x)).add(x.multiply(b.z.negate()));
		RExpression M = (c.x.multiply(d.y)).add(c.y.multiply(d.x.negate()));
		RExpression N = (x.multiply(b.y)).add(y.multiply(b.x.negate()));
		RExpression O = (c.z.multiply(d.x)).add(c.x.multiply(d.z.negate()));
		RExpression P = (c.y.multiply(d.z)).add(c.z.multiply(d.y.negate()));
		RExpression Q = (y.multiply(b.z)).add(z.multiply(b.y.negate()));

		RExpression e1 = (L.multiply(M)).add(N.multiply(O.negate()));
		RExpression e2 = (N.multiply(P)).add(Q.multiply(M.negate()));
		RExpression e3 = (Q.multiply(O)).add(L.multiply(P.negate()));

		HomogenousVector erg = new HomogenousVector (e1,e2,e3); 
		HomogenousVector mem = new HomogenousVector (erg);
		erg.clean();
		if (DEBUG)
		{
			docfile.print("L              : "+L.debugEvaluation()+"\r\n");
			docfile.print("M              : "+M.debugEvaluation()+"\r\n");
			docfile.print("N              : "+N.debugEvaluation()+"\r\n");
			docfile.print("O              : "+O.debugEvaluation()+"\r\n");
			docfile.print("P              : "+P.debugEvaluation()+"\r\n");
			docfile.print("Q              : "+Q.debugEvaluation()+"\r\n");
			docfile.print("e1             : "+e1.debugEvaluation()+"\r\n");
			docfile.print("e2             : "+e2.debugEvaluation()+"\r\n");
			docfile.print("e3             : "+e3.debugEvaluation()+"\r\n");
			docfile.print("Vor bereinigen : "+mem.debugEvaluation()+"\r\n");
			docfile.print("Schnittpunkt   : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Schnitt    : "+erg);
			docfile.println();
		}
		return erg;
	}
	/*
	Seien A und B die beiden Kreismittelpunkte, C und D die beiden 
	Kreisschnittpunkte. AB und CD stehen senkrecht aufeinander und 
	treffen sich in einem Punkt P. Es ist AP^2+PC^2 = r1^2 und 
	PB^2+PC^2 = r2^2, woraus sich (AP)/(AB) = r1^2-r2^2+(AB)^2 / 2(AB)^2
	ergibt. Heiße dieser Faktor lambda, so berechnet sich 
	mue = (PC)/(AB) = sqrt ((r1^2/AB^2) - lambda^2).
	*/
	public HomogenousVector on_cut_cc(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4, boolean left)
	{
		if (DEBUG) docfile.print(" *** Schnittpunkt zweier Kreise *** \r\n");

		HomogenousVector[] ove = new HomogenousVector[4]; ove[0]=this; ove[1]=arg2; ove[2]=arg3; ove[3]=arg4;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector A = ret[0];
		HomogenousVector B = ret[2]; B.z=new RExpression (ret[0].z);
		HomogenousVector Q = ret[1]; Q.z=new RExpression (ret[0].z);
		HomogenousVector R = ret[3]; R.z=new RExpression (ret[0].z);

		RQuotientExp lambda = new RQuotientExp( (A.my_strecke(Q).zaehler).add
									(B.my_strecke(R).zaehler.negate()).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );
		// onelambda = 1-lambda.
		RQuotientExp onelambda = new RQuotientExp( (A.my_strecke(Q).zaehler.negate()).add
									(B.my_strecke(R).zaehler).add
									(A.my_strecke(B).zaehler)  , 
									(A.my_strecke(B).zaehler).multiply(new RExpression(Qelement.TWO)) );

		ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg3;
		ret = erweitert(ove);

		HomogenousVector P = new HomogenousVector((ret[0].x.multiply(onelambda.zaehler)).add(ret[1].x.multiply(lambda.zaehler)),
							  (ret[0].y.multiply(onelambda.zaehler)).add(ret[1].y.multiply(lambda.zaehler)),
							   ret[0].z.multiply(lambda.nenner) );

/*
		RQuotientExp mue = ((A.my_strecke(Q)).divide(A.my_strecke(B))).subtract(lambda);
			    mue = mue.sqrt();

		HomogenousVector move = new HomogenousVector ((ret[1].y.multiply(mue.zaehler)).subtract(ret[0].y.multiply(mue.zaehler)),
								  (ret[0].x.multiply(mue.zaehler)).subtract(ret[1].x.multiply(mue.zaehler)),
								   ret[0].z.multiply(mue.nenner) );

		HomogenousVector C = new HomogenousVector (P.x.add(move.x), P.y.add(move.y), P.z);
		HomogenousVector D = new HomogenousVector (P.x.subtract(move.x), P.y.subtract(move.y), P.z);
*/

		RQuotientExp mue_sqr = ((A.my_strecke(Q)).divide(A.my_strecke(B))).subtract(lambda.sqr());
		RQuotientExp mue = mue_sqr.sqrt();

		HomogenousVector move = new HomogenousVector ((ret[1].y.multiply(mue.zaehler)).subtract(ret[0].y.multiply(mue.zaehler)),
								  (ret[0].x.multiply(mue.zaehler)).subtract(ret[1].x.multiply(mue.zaehler)),
								   ret[0].z.multiply(mue.nenner) );

		HomogenousVector C = P.add(move);
		HomogenousVector D = P.subtract(move);

		HomogenousVector erg;
		if (left) erg = C; else erg = D;
		if (DEBUG)
		{
			docfile.print("Ergebnis      : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;
	}
	/* Sei der Kreis gegeben durch Mittelpunkt M und Radius^2 = (R-M)^2.
	   Dann liegt ein Punkt P auf dem Kreis, wenn (P-M)^2 = r^2 ist, und
	   auf der Geraden, wenn P = A + lambda (B-A) gilt. Eingesetzt ergibt
	   das für lambda den Ausdruck:
	   lambda = - (B-A)(A-M) / (B-A)^2 +- 
					sqrt ( ((B-A)(A-M))^2-(A-M)^2(B-A)^2+r^2(B-A)^2) / (B-A)^2.
	*/
	public HomogenousVector on_cut_lc(HomogenousVector arg2, HomogenousVector arg3, HomogenousVector arg4, boolean left)
	{
		if (DEBUG) docfile.print(" *** Schnittpunkt von Gerade und Kreis *** \r\n");

		HomogenousVector[] ove = new HomogenousVector[4]; ove[0]=this; ove[1]=arg2; ove[2]=arg3; ove[3]=arg4;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector A = ret[0];
		HomogenousVector B = ret[1]; B.z=new RExpression (ret[0].z);
		HomogenousVector M = ret[2]; M.z=new RExpression (ret[0].z);
		HomogenousVector R = ret[3]; R.z=new RExpression (ret[0].z);

		RExpression linzaehler = new RExpression ( ((B.x.subtract(A.x)).multiply(A.x.subtract(M.x))).add
									   ((B.y.subtract(A.y)).multiply(A.y.subtract(M.y))) );
			  linzaehler = linzaehler.negate();

//		Fehlerhaft:			  
//		RExpression wurzaehler = new RExpression ( (A.my_strecke(B).zaehler).multiply(M.my_strecke(R).zaehler));

		RExpression r  = M.my_strecke(R).zaehler;		
		RExpression BA = A.my_strecke(B).zaehler;
		RExpression AM = A.my_strecke(M).zaehler;
		RExpression wurzaehler = new RExpression ( (linzaehler.sqr()).subtract
									   ((AM).multiply(BA)).add
									   ((r).multiply(BA)) );
		
		RExpression mem = wurzaehler;
		if (left) wurzaehler = wurzaehler.sqrt();
		else	  wurzaehler = (wurzaehler.sqrt()).negate();

		RQuotientExp lambda = new RQuotientExp (linzaehler.add(wurzaehler) , (B.my_strecke(A).zaehler));

		HomogenousVector erg = A.add( (B.subtract(A)).scalarMultiply(lambda) );

		if (DEBUG)
		{
			String out  = "Argument 1   : "+this.debugEvaluation()+"\r\n";
				   out += "Argument 2   : "+arg2.debugEvaluation()+"\r\n";
				   out += "Argument 3   : "+arg3.debugEvaluation()+"\r\n";
				   out += "Argument 4   : "+arg4.debugEvaluation()+"\r\n";
				   out += "A            : "+A   .debugEvaluation()+"\r\n";
				   out += "B            : "+B   .debugEvaluation()+"\r\n";
				   out += "M            : "+M   .debugEvaluation()+"\r\n";
				   out += "R            : "+R   .debugEvaluation()+"\r\n";
				   out += "linzaehler   : "+linzaehler.debugEvaluation()+"\r\n";
//				   out += "Radius       : "+r.   debugEvaluation()+"\r\n";
//				   out += "BA           : "+BA.  debugEvaluation()+"\r\n";
//				   out += "AM           : "+AM.  debugEvaluation()+"\r\n";
				   out += "vor sqrt   : "+mem. debugEvaluation()+"\r\n";
				   out += "wurzaehler   : "+wurzaehler.debugEvaluation()+"\r\n";
				   out += "lambda       : "+lambda.debugEvaluation()+"\r\n";

			docfile.print(out);
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;
	}
	public HomogenousVector on_foot(HomogenousVector arg2, HomogenousVector arg3)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Fusspunkt *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Argument 3: "+arg3.debugEvaluation()+"\r\n");
		}

		HomogenousVector erg = on_cut(arg2,arg3,arg3.add((arg2).subtract(this).senkrecht()));
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;
	}
	// HomogenousVector auf der Verbindungslinie von this und arg2
	public HomogenousVector on_line(HomogenousVector arg2, RQuotientExp faktor)
	{
		if (DEBUG) 
		{
			docfile.print(" *** HomogenousVector auf Verbindungsgeraden *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Faktor    : "+faktor.debugEvaluation()+"\r\n");
		}

		RQuotientExp negfaktor = new RQuotientExp (Qelement.ONE);
		        negfaktor = negfaktor.add(faktor.negate());
		HomogenousVector erg = scalarMultiply(negfaktor).add(arg2.scalarMultiply(faktor));
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;
	}
	// HomogenousVector auf dem 4. Punkt eines Parallelogramms
	public HomogenousVector on_parallelogram(HomogenousVector arg2, HomogenousVector arg3)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Punkt auf Parallelogrammecke *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
			docfile.print("Argument 3: "+arg3.debugEvaluation()+"\r\n");
		}
		HomogenousVector[] ove = new HomogenousVector[3]; ove[0]=this; ove[1]=arg2; ove[2]=arg3;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector erg = new HomogenousVector ((ret[2].x).add(ret[1].x).subtract(ret[0].x),
							     (ret[2].y).add(ret[1].y).subtract(ret[0].y),
							      ret[0].z );
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;

	}
	// HomogenousVector senkrecht auf (this,arg2), über arg2.
	public HomogenousVector on_rect(HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** HomogenousVector auf Senkrechten *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
		}

		HomogenousVector[] ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg2;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector erg = new HomogenousVector ( (ret[1].x).add(ret[0].y).subtract(ret[1].y),
							(ret[1].y).subtract(ret[0].x).add(ret[1].x),
							ret[0].z);
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;

	}
	// HomogenousVector auf Schnitt von Thaleskreis und Mittelsenkrechten;
	// Es ist x1 = a1 + (b1-a1)*0.5 - (b2-a2)*0.5 = 0.5 * (+a1+a2+b1-b2)
	//        x2 = a2 + (b2-a2)*0.5 + (b1-a1)*0.5 = 0.5 * (-a1+a2+b1+b2)
	public HomogenousVector on_thales_and_rect(HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Punkt auf Thaleskreis und Mittelsenkrechten *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
		}
		
		HomogenousVector[] ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg2;
		HomogenousVector[] ret = erweitert(ove);

		HomogenousVector erg = new HomogenousVector ( (ret[0].x).add(ret[0].y).add(ret[1].x).subtract(ret[1].y),
							(ret[0].x.negate()).add(ret[0].y).add(ret[1].x).add (ret[1].y),
							 ret[0].z.multiply(new RExpression(Qelement.TWO)) );
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
			
			docfile.println("ex. Ergebnis  : "+erg.toString());
			docfile.println();
		}
		return erg;

	}
	public RQuotientExp real_strecke (HomogenousVector arg2)
	{
		return (strecke(arg2)).sqrt();
	}
	// Skalarmultiplikation mit einer einzelnen Zahl
	public HomogenousVector scalarMultiply (Qelement faktor)
	{
		RQuotientExp fakfunc = new RQuotientExp(faktor);
		return scalarMultiply(fakfunc);
	}
	// Skalarmultiplikation mit einer rationalen Funktion
	public HomogenousVector scalarMultiply (RQuotientExp faktor)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Skalarmultiplikation *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 1: "+this.toString()+"\r\n");
			docfile.print("Skalar    : "+faktor.debugEvaluation()+"\r\n");
			docfile.print("Skalar    : "+faktor.toString()+"\r\n");
		}
		HomogenousVector erg = new HomogenousVector(x.multiply(faktor.zaehler), y.multiply(faktor.zaehler), z.multiply(faktor.nenner));
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
		}
		return erg;
	}
	// x1*x2 + y1*y2
	public RQuotientExp scalarProduct (HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** Skalarprodukt *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
		}

		RQuotientExp erg = new RQuotientExp( (x.multiply(arg2.x)).add(y.multiply(arg2.y)), 
			                  z.multiply(arg2.z) );
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
		}
		return erg;
	}
	// Ein senkrechter HomogenousVector zu diesem HomogenousVector
	HomogenousVector senkrecht ()
	{
		return new HomogenousVector (y,x.negate(),z);
	}
	RQuotientExp strecke (HomogenousVector arg2)
	{
		if (DEBUG) 
		{
			docfile.print(" *** sqr über Strecke *** \r\n");
			docfile.print("Argument 1: "+this.debugEvaluation()+"\r\n");
			docfile.print("Argument 2: "+arg2.debugEvaluation()+"\r\n");
		}

		HomogenousVector[] ove = new HomogenousVector[2]; ove[0]=this; ove[1]=arg2;
		HomogenousVector[] ret = erweitert(ove);
		
		RExpression xz = ((ret[0].x.subtract(ret[1].x)).sqr());
		RExpression yz = ((ret[0].y.subtract(ret[1].y)).sqr());
		RExpression su = xz.add(yz);
		RExpression ne = ret[0].z.sqr();
		RQuotientExp erg = new RQuotientExp ( su, ne);
		RQuotientExp ergcop = new RQuotientExp (erg);
		erg.clean();
		if (DEBUG)
		{
			docfile.print("Arg 1.konv: "+ret[0].debugEvaluation()+"\r\n");
			docfile.print("Arg 2.konv: "+ret[1].debugEvaluation()+"\r\n");
			docfile.print("x-Zähler  : "+xz.debugEvaluation()+"\r\n\r\n");
			docfile.print("y-Zähler  : "+yz.debugEvaluation()+"\r\n\r\n");
			docfile.print("Summe     : "+su.debugEvaluation()+"\r\n");
			docfile.print("Nenner    : "+ne.debugEvaluation()+"\r\n");
			docfile.print("Ergebnis  : "+ergcop.debugEvaluation()+"\r\n\r\n");
			docfile.print("Ergebnis  : "+erg.debugEvaluation()+"\r\n\r\n");
		}
		return erg;
	}
	HomogenousVector subtract (HomogenousVector arg2)
	{
		return add(arg2.negate());
	}
	public String toString()
	{
		return "( " + x.toString() + " , " + y.toString() + " , " + z.toString() + " )";
	}
}
