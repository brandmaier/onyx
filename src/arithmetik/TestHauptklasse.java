package arithmetik;

import java.util.*;
import java.io.*;
import java.math.*;

public class TestHauptklasse implements Printable
{
	public static void main (String[] args)
	{
/*		
		int ky=0;
		while (ky != 115) {
			System.out.println();
			System.out.println("---------------------------...starte...--------------------------");
//			factorTreeTest();
//			henselBeispiel();
			long btime = System.currentTimeMillis();

			testHenselFactorization();
			
			long etime = System.currentTimeMillis();
			System.out.println(etime-btime +" Millisekunden benötigt");

/*			UnivariatePolynomial f = new UnivariatePolynomial(2);
			UnivariatePolynomial g = new UnivariatePolynomial(1);
			f.set(2,2);
			f.set(-2,0);
			g.set(16,1);
			g.set(20,0);
			UnivariatePolynomial q = f.trueDivide(g);
			System.out.println(f + " / " + g + " = " + q);
			q = f.pseudoDivision(g)[0];
			System.out.println(f + " /p " + g + " = " + q);
			q = f.gcd(g);
			System.out.println("("+f+","+g+") = " +q);
*/
/*		
			System.out.println("s um anzuhalten, jede andere Taste um weiterzukommen...");
			try {
				ky = System.in.read();
				System.in.read();
			} catch (Exception e) {}
		}

		QPolynomial x = new QPolynomial(0);
		QPolynomial y = new QPolynomial(1);
		
		QPolynomial p = (new QPolynomial(new Qelement(9))).add(
						(new QPolynomial(new Qelement(23)).multiply(y.sqr())).add(
						(new QPolynomial(new Qelement(13)).multiply(y)).multiply(x.sqr()).add(
						(new QPolynomial(new Qelement(6)).multiply(y))).add(
						(new QPolynomial(new Qelement(7)).multiply(y.pow(3)))).add(
						(new QPolynomial(new Qelement(13)).multiply(y.pow(2)).multiply(x.pow(2)))).add(
						(new QPolynomial(new Qelement(1)).multiply(x.pow(4)))).add(
						(new QPolynomial(new Qelement(6)).multiply(x.pow(4)).multiply(y.pow(1)))).add(
						(new QPolynomial(new Qelement(1)).multiply(x.pow(6))))));
*/
//		QPolynomial p = (new QPolynomial(new Qelement(1)).multiply(x.sqr())).add(
//						(new QPolynomial(new Qelement(2)).multiply(x).multiply(y)).add(
//						(new QPolynomial(new Qelement(1)).multiply(y.sqr()))));
		

//		long[][] l = {{9,0,0},{23,0,2},{13,2,1},{6,0,1},{7,0,3},{13,2,2},{1,4,0},{6,4,1},{1,6,0}};
//		long[][] l = {{135,0,0},{5,3,4},{27,0,1},{1,3,5},{27,2,0},{1,5,4}};
		// Folgender läuft nicht, wenn mans nicht quadratfrei macht.
		//		long[][] l = {{4,6,4},{1,6,3},{16,3,3},{64,3,2},{12,4,2},{3,4,1},{12,1,1},{3,1,0},{4,5,5},{21,5,4},{4,2,4},{21,2,3},{12,0,2},{63,0,1},{5,5,3},{5,2,2},{15,3,1},{15,0,0}};
//		long[][] l = {{1,1,1},{1,0,2},{1,1,0},{1,0,1}};
//		long[][] l = {{1,3,1},{2,2,2},{1,2,1},{3,1,2},{1,1,1},{1,0,2},{1,0,1}};
//		long[][] l = {{1,2,0},{2,1,1},{1,0,2},{-2,0,0}};
//		long[][] l = {{1,6,6},{1,5,5},{1,4,4},{2,3,3},{-3,2,2},{-2,0,0}};
//		long[][] l = {{1,6,0},{-1,5,0},{3,4,0},{-2,3,0},{2,3,2},{-2,2,2},{4,1,2},{-4,0,2},{1,2,0},{-2,0,0}};
//		long[][] l = {{1,5,5},{1,3,3},{-1,2,2},{-1,0,0}};
//		long[][] l = {{1,3,3},{-1,2,2},{1,1,1},{-1,0,0}};
//		QPolynomial p = QPolynomial.fromArray(l);
/*
		QPolynomial p = QPolynomial.getGeneralQuadraticMinimalPolynomial(3);

		for (int i=0; i<=6; i++)
		{
			System.out.println("Faktor "+i+": "+p.getCoefficient(0,i));
		}
		FastPolynomial res[] = new FastPolynomial[5];
		int var = 7;
		QPolynomial q = p.getCoefficient(0,6).subtract(new QPolynomial(var++));
		for (int i=5; i>=1; i--)
		{
			res[i-1] = q.resultant(p.getCoefficient(0,i).subtract(new QPolynomial(var++)),1);
			System.out.println ("Resultante(c6, c"+i+", X1): "+res[i-1]);
		}
		for (int i=4; i>=1; i--)
		{
			res[i-1] = res[4].resultant(res[i-1],2);
			System.out.println ("Resultante(r5, r"+i+", X2): "+res[i-1]);
		}
			
*/		
/*
		System.out.println(p);
		
		QPolynomial[] facs = p.factorBivariate(0,1);
		for (int i=0; i<facs.length; i++)
			System.out.println("Faktor nr. "+i+": "+facs[i]);

		QPolynomial r1 = p;
		QPolynomial r2 = (new QPolynomial(1)).subtract((new QPolynomial(2)).multiply(p.derive(0)));
		System.out.println("r1 = "+r1);
		System.out.println("r2 = "+r2);
		System.out.println("Resultante = "+r1.resultant(r2,0));
			

		Qelement[][] mat = {{new Qelement(3), new Qelement(2), new Qelement(1)},
							{new Qelement(3), new Qelement(2), new Qelement(8)},
							{new Qelement(6), new Qelement(4), new Qelement(9)}};
		
		RingMatrix m = new RingMatrix(mat);
		RingMatrix r = m.findRMatrix();
		System.out.println(r);
		
		RingVector[] kern = m.coreBasis();
		for (int i=0; i<kern.length; i++)
			System.out.println(i+". Kernvektor : "+kern[i]);
		
		Qelement[] vec = {new Qelement(6), new Qelement(13), new Qelement(19)};
		RingVector b = new RingVector(vec);
		RingVector a = m.solveWithGauss(b);
		System.out.println("Ergebnis :"+a);
*/		
								
/*
		QPolynomial p = QPolynomial.fromArray(new long[][]{{1,4},{2,3},{1,1},{1,0}});
		QPolynomial q = QPolynomial.fromArray(new long[][]{{1,3},{4,2},{5,1},{1,0}});

		System.out.println("p = "+p);
		System.out.println("q = "+q);
		System.out.println("gcd = "+p.gcd(q));
		QPolynomial[] bezout = p.getBezout(q);
		System.out.println("("+bezout[0]+")   *   ("+p+")   +   ("+bezout[1]+")   *   ("+q+")   =   ("+bezout[2]+")");


		Polynomial p1 = new Polynomial(new Polynomial[]{new Polynomial(new Qelement[]{Qelement.ZERO,Qelement.ZERO,Qelement.ONE}),
									    				new Polynomial(new Qelement[]{Qelement.ZERO,Qelement.TWO,Qelement.TWO}),
									    				new Polynomial(new Qelement[]{Qelement.ZERO,new Qelement(5)}),
									    				new Polynomial(new Qelement[]{Qelement.TWO})});

		Polynomial p2 = new Polynomial(new Polynomial[]{new Polynomial(new Qelement[]{Qelement.ZERO,Qelement.ONE,Qelement.TWO}),
									    				new Polynomial(new Qelement[]{Qelement.ZERO,Qelement.TWO,new Qelement(4)}),
									    				new Polynomial(new Qelement[]{Qelement.ONE,Qelement.TWO})});

		System.out.println("p1 = "+p1);
		System.out.println("p2 = "+p2);
		Polynomial g = p1.gcd(p2);
		System.out.println("gcd= "+g);
	
		QPolynomial f = QPolynomial.fromArray(new long[][]{{1,7,0},{2,1,1},{-1,1,0},{1,0,0}});
		QPolynomial g = QPolynomial.fromArray(new long[][]{{1,10,0},{1,0,1},{1,1,0}});

		System.out.println("Start");
		FastPolynomial res = f.resultant(g,0);
		System.out.println("res1 = "+res);
		res = f.resultant2(g,0);
		System.out.println("res2 = "+res);

		Vector in = new Vector(); in.addElement(new Integer(0)); 
		
		in.addElement(new Qelement[]{new Qelement(0),new Qelement(1), new Qelement(2), new Qelement(3)});

		Vector i1 = new Vector(); i1.addElement(new Integer(1)); 
		i1.addElement(new Qelement[]{new Qelement(0),new Qelement(1), new Qelement(2)});
		i1.addElement(new Qelement[]{new Qelement(1),new Qelement(3), new Qelement(9)});
		
		Vector i2 = new Vector(); i2.addElement(new Integer(1)); 
		i2.addElement(new Qelement[]{new Qelement(0),new Qelement(1), new Qelement(2)});
		i2.addElement(new Qelement[]{new Qelement(2),new Qelement(5), new Qelement(12)});

		Vector i3 = new Vector(); i3.addElement(new Integer(1)); 
		i3.addElement(new Qelement[]{new Qelement(0),new Qelement(1), new Qelement(2)});
		i3.addElement(new Qelement[]{new Qelement(3),new Qelement(13), new Qelement(27)});
			
		Vector i4 = new Vector(); i4.addElement(new Integer(1)); 
		i4.addElement(new Qelement[]{new Qelement(0),new Qelement(1), new Qelement(2)});
		i4.addElement(new Qelement[]{new Qelement(4),new Qelement(33), new Qelement(66)});
		
		in.addElement(new Vector[]{i1,i2,i3,i4});

		QPolynomial p = QPolynomial.interpolate(in);
		System.out.println("p = "+p);


		QPolynomial p = QPolynomial.fromArray(new long[][]{{16,3},{207,1},{-52,0}});
		Vector v = p.findRationalZeros3();
		for (int i=0; i<v.size(); i++)
		{
			System.out.println("Nullstelle: "+v.elementAt(i));
		}

		int bis = 6;

		int[] v = new int[bis];
		for (int i=0; i<bis; i++) v[i]=1;

		QPolynomial g = QPolynomial.gaertnerPolynomial(v);

		System.out.println(g);

		int deg = 8;

		QPolynomial x = new QPolynomial(0);
		QPolynomial p = x.pow(deg);
		for (int i=2; i<=deg; i++)
			p = p.add((new QPolynomial(i)).multiply(x.pow(deg-i)));

//		p = new QPolynomial("X^4+2*X^2+3*X+5");
			
		System.out.println(p);
		
		QPolynomial[] ps = p.findCombinationPolynomials(0, new TestHauptklasse());

		System.out.println("Sub : "+ps[0]);
		System.out.println("Add : "+ps[1]);



//		QPolynomial p = QPolynomial.fromArray(new long[][]{{-4,0,1},{-4,1,0},{-3,1,1},{-3,0,2}});

		(-40288943149/74152721280)*X1 + (9997847493/11884260080)*X1^2 +
		(-40288943149/74152721280)*X0 + (21882107573/11884260080)*X0X1 + X0^2

		QPolynomial x = new QPolynomial(0), y = new QPolynomial(1),
					p = (y.multiply(new QPolynomial(new Qelement(new BigInteger("-40288943149"),new BigInteger("74152721280"))))).add
						(y.pow(2).multiply(new QPolynomial(new Qelement(new BigInteger("9997847493"),new BigInteger("11884260080"))))).add
						(x.multiply(new QPolynomial(new Qelement(new BigInteger("-40288943149"),new BigInteger("74152721280"))))).add
						(x.multiply(y).multiply(new QPolynomial(new Qelement(new BigInteger("21882107573"),new BigInteger("11884260080"))))).add
						(x.pow(2));

		System.out.println("p = "+p);

		System.out.println();

		QPolynomial[] fak = p.factorBivariate(0,1);

		System.out.println();
		for (int i=0; i<fak.length; i++)
			System.out.println("Faktor "+i+": "+fak[i]);


		String s = "X^16 - 16* X^15 + 64* X^14 + 224* X^13 - 1924* X^12 - 208* X^11 +";
		s +=       "21696* X^10 - 1888* X^9 - 178186* X^8 - 30128* X^7";
		s +=       "+ 1036224* X^6 + 411424* X^5 - 3427684* X^4 - 2163056* X^3 +";
		s +=       "4848448* X^2 + 3686752* X - 545999";

//		String s = "183835030548042527863480472628150713038768340377534464  -8650255762478775447398356356863989112627016813772800*X0 + 615040368308180269969436504199832073580768028262400*X0^2  -72012344004685958622628296718196174126283510251520*X0^3 + 9133188275793880743177367854227209189268127744*X0^4  -50817201617882227632688458403295653140551958528*X0^5  -2673548066944038425441783481539719478472867840*X0^6 + 71412983216103867784257947165799364524244992*X0^7 + 1742586376943188875200055742655987974144000*X0^8  -8266963646933122708670091366119675789312*X0^9  -361290025944840941732232029827206479872*X0^10 + 22990243711060416780988259276257492992*X0^11 + 403708513103560469675938966579183616*X0^12 -4059236891883523771768583994998784*X0^13 -111150352334487892804127002984448*X0^14 -239075199255782447106559574016*X0^15 + 9376674783115290162533236736*X0^16 + 71907987590574854664880128*X0^17  -127667135762429238050816*X0^18  -3827772239137638187008*X0^19  -11438337786652524544*X0^20 + 135193376496549888*X0^21 + 597516493422592*X0^22  -2763361615872*X0^23  -9746818048*X0^24 + 54927360*X0^25  -78208*X0^26  -320*X0^27 + X0^28"	;
		QPolynomial p = new QPolynomial(s);
//		QPolynomial p = new QPolynomial("30592 -9856*X0 + 1136*X0^2  -56*X0^3 + X0^4");
			
//		QPolynomial p = new QPolynomial("((1/3)*X0+4*X1^2*X0+X1^2)*X0^2*(2*X0*X1+2)*X1*(X0+X1)^2");
//		QPolynomial p = new QPolynomial("(1+X1+X2)*(X0+1+X2)*(X1+1)");
//		QPolynomial p = new QPolynomial("(2/24)*(3*X0+3-X1)*(4*X0+3-2*X1)");
//		QPolynomial p = new QPolynomial("-1260 + -12*X0 + X0^2");

//		QPolynomial p = new QPolynomial("5*(3*X^2+2*X+1)^4");
//		QPolynomial p = new QPolynomial("48369664 + -1204224*X0 + -22400*X0^2 + X0^4");
//		QPolynomial p = new QPolynomial("X^4-10*X^2+12*X-2");
		System.out.println(p);

//		RemainderRingPolynomial f = new RemainderRingPolynomial(p.toUnivariatePolynomial(),new Modulus(11));
//		System.out.println(f);
//		System.out.println("Inverse is : lc*i=1 : " + f.leadingCoefficient() + " * " + inverse + " = " +f.leadingCoefficient().multiply(inverse));
//		System.out.println("Factorizing : " + f.monomialMultiply(inverse,0) );
//		Stack erg = f.factorize();

//		QPolynomial q = p.sqrt();
//		System.out.println("Wurzel = "+q);

		RQuotientExp[] ea = p.findSquarerootZeros(0);
//		QPolynomial[] ea = p.factorize();
*/

//		QPolynomial p1 = new QPolynomial("6*X^9-27*X^7+10*X^6+2*X^5-15*X^4+9*X^3-9*X^2-2*X+7");
//		QPolynomial p2 = new QPolynomial("6*X^8-12*X^7-3*X^6+10*X^5-6*X^4-X^2-X+2");
//		QPolynomial p1 = new QPolynomial("-5925*X^14-1240*X^12+1885*X^8-4685*X^7+9805*X^4+7058*X^3-5081*X+50");
//		QPolynomial p2 = new QPolynomial("-4694*X^14-5762*X^13+4862*X^12-1347*X^11-4687*X^10+6520*X^9+9937*X^7-6439*X^4-3317*X^3");
//		QPolynomial p1 = new QPolynomial("(X0^17+2*X1^2*X0)*(X0^12+X1^8*X2^9+3*X1+5)");
//		QPolynomial p2 = new QPolynomial("(X0^17+2*X1^2*X0)*(X1^22+X0^7*X2^3+3*X1+1)");
//		QPolynomial p1 = new QPolynomial("2*X0*X1^10 + X0^17*X1^8");
//		QPolynomial p2 = new QPolynomial("10*X0*X1^2 + 6*X0*X1^3 + 2*X0^13*X1^2 + 5*X0^17 + 3*X0^17*X1 + X0^29");
//		QPolynomial p1 = new QPolynomial("20*X1^4 + -40*X1^5 + (-1330/9)*X1^6 + (2662/9)*X1^7 + (63317/324)*X1^8 + (-4832/9)*X1^9 + 256*X1^10 + 80*X0*X1^3 + -160*X0*X1^4 + (-5800/9)*X0*X1^5 + (11704/9)*X0*X1^6 + (78557/81)*X0*X1^7 + (-7760/3)*X0*X1^8 + 1280*X0*X1^9 + 120*X0^2*X1^2 + -240*X0^2*X1^3 + (-3440/3)*X0^2*X1^4 + (6896/3)*X0^2*X1^5 + (113903/54)*X0^2*X1^6 + -5246*X0^2*X1^7 + 2656*X0^2*X1^8 + 80*X0^3*X1 + -160*X0^3*X1^2 + (-9460/9)*X0^3*X1^3 + (18532/9)*X0^3*X1^4 + (207506/81)*X0^3*X1^5 + (-52120/9)*X0^3*X1^6 + 2960*X0^3*X1^7 + 20*X0^4 + -40*X0^4*X1 + (-4600/9)*X0^4*X1^2 + (8668/9)*X0^4*X1^3 + (601583/324)*X0^4*X1^4 + (-11288/3)*X0^4*X1^5 + 1921*X0^4*X1^6 + -120*X0^5*X1 + 216*X0^5*X1^2 + 802*X0^5*X1^3 + -1460*X0^5*X1^4 + 740*X0^5*X1^5 + -10*X0^6 + 18*X0^6*X1 + (1183/6)*X0^6*X1^2 + (-2972/9)*X0^6*X1^3 + 166*X0^6*X1^4 + 25*X0^7*X1 + -40*X0^7*X1^2 + 20*X0^7*X1^3 + (5/4)*X0^8 + -2*X0^8*X1 + X0^8*X1^2");
//		QPolynomial p2 = new QPolynomial("80*X1^3 + -160*X1^4 + (-5800/9)*X1^5 + (11704/9)*X1^6 + (78557/81)*X1^7 + (-7760/3)*X1^8 + 1280*X1^9 + 240*X0*X1^2 + -480*X0*X1^3 + (-6880/3)*X0*X1^4 + (13792/3)*X0*X1^5 + (113903/27)*X0*X1^6 + -10492*X0*X1^7 + 5312*X0*X1^8 + 240*X0^2*X1 + -480*X0^2*X1^2 + (-9460/3)*X0^2*X1^3 + (18532/3)*X0^2*X1^4 + (207506/27)*X0^2*X1^5 + (-52120/3)*X0^2*X1^6 + 8880*X0^2*X1^7 + 80*X0^3 + -160*X0^3*X1 + (-18400/9)*X0^3*X1^2 + (34672/9)*X0^3*X1^3 + (601583/81)*X0^3*X1^4 + (-45152/3)*X0^3*X1^5 + 7684*X0^3*X1^6 + -600*X0^4*X1 + 1080*X0^4*X1^2 + 4010*X0^4*X1^3 + -7300*X0^4*X1^4 + 3700*X0^4*X1^5 + -60*X0^5 + 108*X0^5*X1 + 1183*X0^5*X1^2 + (-5944/3)*X0^5*X1^3 + 996*X0^5*X1^4 + 175*X0^6*X1 + -280*X0^6*X1^2 + 140*X0^6*X1^3 + 10*X0^7 + -16*X0^7*X1 + 8*X0^7*X1^2");
//		QPolynomial p1 = new QPolynomial("(2*X0^2*X1+5*X0*X1^3+X0*X1+2*X1)*(X0^3+X1^3)");
//		QPolynomial p1 = new QPolynomial("(2*X0*X1+5)*(X0+X1)");
/*
		Qelement[][] mat = new Qelement[4][6];
		String out = "[[";
		for (int i=0; i<mat.length; i++)
			for (int j=0; j<mat[0].length; j++)
			{
				mat[i][j] = Qelement.random(100);
				out += mat[i][j];
				if (j <mat[0].length-1) out += ","; else out += "],[";
			}
		System.out.println(out+"]]");
*/

		long start = System.currentTimeMillis();

//		QPolynomial p1 = new QPolynomial("-1*X0^2*X4^2*X5^8 + 4*X0^2*X4^4*X5^6 -6*X0^2*X4^6*X5^4 + 4*X0^2*X4^8*X5^2 -1*X0^2*X4^10 + X0^4*X5^8 -4*X0^4*X4^2*X5^6 + 6*X0^4*X4^4*X5^4 -4*X0^4*X4^6*X5^2 + X0^4*X4^8");
//		QPolynomial p2 = new QPolynomial("-928*X0^2*X4^2 + 3888*X0^2*X4^2*X5^2 + -3186*X0^2*X4^2*X5^4 + 4*X0^2*X4^2*X5^6 + 2124*X0^2*X4^4*X5^2 + -8*X0^2*X4^4*X5^4 + 1062*X0^2*X4^6 + 4*X0^2*X4^6*X5^2 + 928*X0^4 + -3888*X0^4*X5^2 + 3186*X0^4*X5^4 + -4*X0^4*X5^6 + -2124*X0^4*X4^2*X5^2 + 8*X0^4*X4^2*X5^4 + -1062*X0^4*X4^4 + -4*X0^4*X4^4*X5^2");

//		QPolynomial erg = p1.gcd(p2);
//		System.out.println(erg);

		System.out.println("Starte Berechnung");
/*		
		QPolynomial p = new QPolynomial("X0^128+X0^127-127*X0^126-126*X0^125+7875*X0^124+7750*X0^123-317750*X0^122-310124*X0^121+9381251*X0^120+9078630*X0^119-216071394*X0^118-207288004*X0^117+4042116078*X0^116+3843323484*X0^115-63140314380*X0^114-59487568920*X0^113+840261910995*X0^112+784244450262*X0^111-9672348219898*X0^110-8940826085620*X0^109+97455004333258*X0^108+89196105660948*X0^107-867634845974676*X0^106-786062339088168*X0^105+6878045467021470*X0^104+6166523522157180*X0^103-48857840214014580*X0^102-43334780015908584*X0^101+312629484400483356*X0^100+274236389824985400*X0^99-1809960172844903640*X0^98-1569699972909739440*X0^97+9516306085765295355*X0^96+8156833787798824590*X0^95-45582306461228725650*X0^94-38601232498698200100*X0^93+199439701243274033850*X0^92+166804113767101919220*X0^91-798903913305593402580*X0^90-659645433004618405800*X0^89+2935422176870551905810*X0^88+2391825477450079330660*X0^87-9908991263721757227020*X0^86-7964235968972627303960*X0^85+30770911698303332765300*X0^84+24384496062806414644200*X0^83-87996224922301409368200*X0^82-68720861367892529220880*X0^81+231932907116637286120470*X0^80+178409928551259450861900*X0^79-563775374221979864723604*X0^78-426936691158392518916904*X0^77+1264389431507547075253908*X0^76+942094086221309585483304*X0^75-2616928017281415515231400*X0^74-1917353200780443050763600*X0^73+4998813702034726525205100*X0^72+3599145865465003098147672*X0^71-8811701946483283447189128*X0^70-6230496325796261023265040*X0^69+14330141549331400353509592*X0^68+9943363524025869633047472*X0^67-21490495358378492432715504*X0^66-14622398903638974232569312*X0^65+29701747773016666409906415*X0^64+19801165182011110939937610*X0^63-37802224438384848158062710*X0^62-24670925422945900903156716*X0^61+44262542670579410443898814*X0^60+28252686811008134325892860*X0^59-47625957767127997863647964*X0^58-29702210220359396517113784*X0^57+47028499515569044485430158*X0^56+28626043183389853165044444*X0^55-42552226353687619569660660*X0^54-25250771682408037986392040*X0^53+35218181557042789823125740*X0^52+20348282677402500786694872*X0^51-26609292731987885644139448*X0^50-14949040860667351485471600*X0^49+18312575054317505569702710*X0^48+9988677302355003038019660*X0^47-11450434956358174214315220*X0^46-6054252965430758779982760*X0^45+6486699605818670121410100*X0^44+3318776542511877736535400*X0^43-3318776542511877736535400*X0^42-1639866056299986646288080*X0^41+1528057007006805738586620*X0^40+727646193812764637422200*X0^39-630626701304396019099240*X0^38-288720658428518659346640*X0^37+232231833953373704257080*X0^36+101955439296603089673840*X0^35-75924263305981024225200*X0^34-31869443856831541032800*X0^33+21910242651571684460050*X0^32+8764097060628673784020*X0^31-5544632834275283414380*X0^30-2105556772509601296600*X0^29+1221222928055568752028*X0^28+438387717763537500728*X0^27-232087615286578676856*X0^26-78367246720143449328*X0^25+37676560923145889100*X0^24+11897861344151333400*X0^23-5163222847461899400*X0^22-1514545368588823824*X0^21+588989865562320376*X0^20+159186450151978480*X0^19-54991682779774384*X0^18-13559593014190944*X0^17+4116305022165108*X0^16+914734449370024*X0^15-240719591939480*X0^14-47465835030320*X0^13+10638894058520*X0^12+1823810410032*X0^11-340032449328*X0^10-49280065120*X0^9+7392009768*X0^8+869648208*X0^7-99795696*X0^6-8936928*X0^5+720720*X0^4+43680*X0^3-2080*X0^2-64*X0+1");
		QPolynomial[] erg = p.findCombinationPolynomials(0,new Printable(){
			public void print(String s) {System.out.print(s);}
			public void println() {System.out.print("\r\n");}
			public void println(String s) {System.out.print(s+"\r\n");}
		});
		*/
		/*
	[_x^2*z_-1*z^3],
	[_x*z^2_-z^3],
	[_x*y_+z^2],
	[_y^2_-x*z],
	[_y*z_+z^2]

		Vector p1 = (new QPolynomial("X0^2*X2-X2^3")).resort(QPolynomial.grevlexorder).toModulMomialList();
		Vector p2 = (new QPolynomial("X0*X2^2-X2^3")).resort(QPolynomial.grevlexorder).toModulMomialList();
		Vector p3 = (new QPolynomial("X0*X1+X2^2")).resort(QPolynomial.grevlexorder).toModulMomialList();
		Vector p4 = (new QPolynomial("X1^2-X0*X2")).resort(QPolynomial.grevlexorder).toModulMomialList();
		Vector p5 = (new QPolynomial("X1*X2+X2^2")).resort(QPolynomial.grevlexorder).toModulMomialList();
*/
/*
	[X4^2], 
	[X3*X4], 
	[X1*X4-23*X2*X4], 
	[X1*X3-21*X2*X3+48*X3^2+21*X0*X4+49*X2*X4], 
	[X0*X3], 
	[X2^2], 
	[X1*X2-32*X2*X3+34*X3^2], 
	[X1^2+36*X2*X3-33*X0*X4+42*X2*X4], 
	[X0*X1-23*X0*X2+28*X2*X3+6*X2*X4], 
	[X0^2+36*X0*X2+43*X2*X3-31*X0*X4-10*X2*X4], 
	[X2*X3^2], 
	[X3^3+38*X0*X2*X4]	
*/
/*
		// Beispiel 1
		Vector p10 = (new QPolynomial("X0^2+36*X0*X2+43*X2*X3-31*X0*X4-10*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p9 = (new QPolynomial("X0*X1-23*X0*X2+28*X2*X3+6*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p8 = (new QPolynomial("X1^2+36*X2*X3-33*X0*X4+42*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p7 = (new QPolynomial("X1*X2-32*X2*X3+34*X3^2")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p6 = (new QPolynomial("X2^2")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p5 = (new QPolynomial("X0*X3")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p4 = (new QPolynomial("X1*X3-21*X2*X3+48*X3^2+21*X0*X4+49*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p11 = (new QPolynomial("X2*X3^2")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p12 = (new QPolynomial("X3^3+38*X0*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p3 = (new QPolynomial("X1*X4-23*X2*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p2 = (new QPolynomial("X3*X4")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
		Vector p1 = (new QPolynomial("X4^2")).resort(QPolynomial.grevlexorder).toModulMomialList(5);
			
		Vector in = new Vector();
		in.addElement(p1); in.addElement(p2); in.addElement(p3); in.addElement(p4); in.addElement(p5);
		in.addElement(p6); in.addElement(p7); in.addElement(p8); in.addElement(p9); in.addElement(p10);
		in.addElement(p11); in.addElement(p12);
*/
/*
		// Beispiel 2 (Goren4)
		Vector in = new Vector();

		 in.addElement((new QPolynomial("X3^3-7*X0^2*X4-49*X0*X1*X4-38*X1^2*X4-31*X0*X2*X4-37*X1*X2*X4+15*X2^2*X4-41*X0*X3*X4-42*X1*X3*X4-50*X2*X3*X4-47*X3^2*X4+7*X0*X4^2-48*X1*X4^2+5*X2*X4^2+38*X3*X4^2+50*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2*X3^2-18*X0^2*X4+19*X0*X1*X4+35*X1^2*X4+32*X0*X2*X4+21*X1*X2*X4-17*X2^2*X4-31*X0*X3*X4+35*X1*X3*X4-43*X2*X3*X4-32*X3^2*X4+39*X0*X4^2-47*X1*X4^2+16*X2*X4^2+26*X3*X4^2-20*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X3^2+8*X0^2*X4+26*X0*X1*X4-14*X1^2*X4-4*X0*X2*X4+24*X1*X2*X4-39*X2^2*X4+26*X0*X3*X4+36*X1*X3*X4+11*X2*X3*X4-35*X3^2*X4-35*X0*X4^2-18*X1*X4^2+11*X2*X4^2+10*X3*X4^2+25*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X3^2+13*X0^2*X4-15*X0*X1*X4+13*X1^2*X4-23*X0*X2*X4+26*X1*X2*X4-30*X2^2*X4+46*X0*X3*X4+17*X1*X3*X4+10*X2*X3*X4-42*X3^2*X4-39*X0*X4^2-8*X1*X4^2+34*X2*X4^2-19*X3*X4^2")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2^2*X3+33*X0^2*X4+38*X0*X1*X4-14*X1^2*X4+35*X0*X2*X4-X1*X2*X4+31*X2^2*X4+14*X0*X3*X4+34*X1*X3*X4-43*X2*X3*X4+34*X3^2*X4+7*X0*X4^2-49*X1*X4^2+36*X2*X4^2-30*X3*X4^2-48*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X2*X3+13*X0^2*X4+2*X0*X1*X4+3*X1^2*X4-21*X0*X2*X4-35*X1*X2*X4-42*X2^2*X4+X0*X3*X4+48*X1*X3*X4-41*X2*X3*X4-38*X3^2*X4+31*X0*X4^2+37*X1*X4^2-42*X2*X4^2+24*X3*X4^2-34*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X2*X3-16*X0*X1*X4+20*X1^2*X4+2*X0*X2*X4+43*X1*X2*X4-26*X2^2*X4-3*X0*X3*X4+24*X1*X3*X4+50*X2*X3*X4-32*X3^2*X4-35*X0*X4^2+30*X1*X4^2-23*X2*X4^2-33*X3*X4^2+46*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1^2*X3+15*X0^2*X4-15*X0*X1*X4+31*X1^2*X4+38*X0*X2*X4-18*X1*X2*X4+15*X2^2*X4-50*X0*X3*X4+45*X1*X3*X4+15*X2*X3*X4-X3^2*X4-16*X0*X4^2+19*X1*X4^2-22*X2*X4^2+44*X3*X4^2-8*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X1*X3+47*X0^2*X4-X0*X1*X4+6*X1^2*X4-46*X0*X2*X4-11*X1*X2*X4-8*X2^2*X4+33*X0*X3*X4-46*X1*X3*X4-35*X2*X3*X4-40*X3^2*X4+38*X0*X4^2-42*X1*X4^2-20*X2*X4^2+39*X3*X4^2-5*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0^2*X3-9*X0^2*X4-45*X0*X1*X4-5*X1^2*X4-29*X0*X2*X4+42*X1*X2*X4-21*X2^2*X4-8*X0*X3*X4+23*X1*X3*X4+27*X2*X3*X4-32*X3^2*X4+8*X0*X4^2+37*X1*X4^2-8*X2*X4^2-2*X3*X4^2+7*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2^3+29*X0^2*X4-39*X0*X1*X4-20*X1^2*X4-30*X0*X2*X4-12*X1*X2*X4+16*X2^2*X4+32*X0*X3*X4-15*X1*X3*X4+36*X2*X3*X4-20*X3^2*X4-44*X0*X4^2+44*X1*X4^2+5*X2*X4^2+49*X3*X4^2-X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X2^2+35*X0^2*X4-48*X0*X1*X4-34*X1^2*X4-8*X0*X2*X4-18*X1*X2*X4+7*X2^2*X4+29*X0*X3*X4-14*X1*X3*X4-20*X2*X3*X4+46*X3^2*X4-19*X0*X4^2-24*X1*X4^2-50*X2*X4^2-14*X3*X4^2-39*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X2^2-16*X0^2*X4+29*X0*X1*X4+19*X1^2*X4-49*X0*X2*X4+5*X1*X2*X4-24*X2^2*X4-9*X0*X3*X4+27*X1*X3*X4+40*X2*X3*X4-36*X3^2*X4+17*X0*X4^2-16*X1*X4^2+24*X2*X4^2-12*X3*X4^2+28*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1^2*X2-39*X0^2*X4-15*X0*X1*X4+18*X1^2*X4-37*X0*X2*X4+35*X1*X2*X4-50*X2^2*X4+7*X0*X3*X4+7*X1*X3*X4+4*X2*X3*X4+X3^2*X4-49*X0*X4^2+28*X1*X4^2-4*X2*X4^2+15*X3*X4^2-22*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X1*X2-8*X0^2*X4+20*X0*X1*X4+X1^2*X4-47*X0*X2*X4+16*X1*X2*X4+19*X2^2*X4+29*X0*X3*X4-18*X1*X3*X4-15*X2*X3*X4-30*X3^2*X4+21*X0*X4^2-24*X1*X4^2-37*X2*X4^2-12*X3*X4^2+41*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0^2*X2-39*X0^2*X4-34*X0*X1*X4-3*X1^2*X4-17*X0*X2*X4-46*X1*X2*X4-49*X2^2*X4+16*X0*X3*X4+32*X1*X3*X4-18*X2*X3*X4+41*X3^2*X4-44*X0*X4^2+20*X1*X4^2+47*X2*X4^2+37*X3*X4^2-15*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1^3+6*X0^2*X4+35*X0*X1*X4-35*X1^2*X4-24*X0*X2*X4-X1*X2*X4-23*X2^2*X4-8*X0*X3*X4+5*X1*X3*X4+25*X2*X3*X4+2*X3^2*X4-49*X0*X4^2+42*X1*X4^2-28*X2*X4^2+36*X3*X4^2+24*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X1^2-6*X0^2*X4-24*X0*X1*X4-18*X1^2*X4-45*X0*X2*X4+13*X1*X2*X4+20*X2^2*X4-48*X0*X3*X4-23*X1*X3*X4-38*X2*X3*X4-29*X3^2*X4-3*X0*X4^2+40*X1*X4^2-50*X2*X4^2-37*X3*X4^2+48*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0^2*X1-42*X0^2*X4+9*X0*X1*X4-X1^2*X4+29*X0*X2*X4+35*X1*X2*X4+50*X2^2*X4+46*X0*X3*X4+29*X1*X3*X4+41*X2*X3*X4-45*X3^2*X4-45*X0*X4^2-42*X1*X4^2-23*X2*X4^2+24*X3*X4^2+27*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0^3-14*X0^2*X4-25*X0*X1*X4+18*X1^2*X4-28*X0*X2*X4+34*X1*X2*X4-27*X2^2*X4+46*X0*X3*X4+37*X1*X3*X4+11*X2*X3*X4+40*X3^2*X4+5*X0*X4^2+5*X1*X4^2+41*X2*X4^2+46*X3*X4^2-40*X4^3")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0^2*X4^2+20*X0*X4^3+20*X1*X4^3-48*X2*X4^3-10*X3*X4^3-41*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X1*X4^2+4*X0*X4^3-24*X1*X4^3+36*X2*X4^3+8*X3*X4^3-14*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1^2*X4^2-34*X0*X4^3+8*X1*X4^3+X2*X4^3-48*X3*X4^3+16*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X2*X4^2+24*X0*X4^3+38*X1*X4^3+9*X2*X4^3+49*X3*X4^3+41*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X2*X4^2-8*X0*X4^3+40*X1*X4^3-42*X2*X4^3+18*X3*X4^3-7*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2^2*X4^2+31*X0*X4^3-21*X1*X4^3+2*X2*X4^3-39*X3*X4^3-X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X3*X4^2-35*X0*X4^3+9*X1*X4^3+6*X2*X4^3+9*X3*X4^3+14*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X3*X4^2-12*X0*X4^3+22*X1*X4^3+43*X2*X4^3-5*X3*X4^3+8*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2*X3*X4^2-42*X0*X4^3-9*X1*X4^3-14*X2*X4^3+10*X3*X4^3-19*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X3^2*X4^2-48*X0*X4^3+50*X1*X4^3+49*X2*X4^3-15*X3*X4^3+38*X4^4")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X0*X4^4-11*X4^5")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X1*X4^4+50*X4^5")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X2*X4^4+26*X4^5")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X3*X4^4+43*X4^5")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
		 in.addElement((new QPolynomial("X4^6")).resort(QPolynomial.grevlexorder).toModulMomialList(5)); 
	

		int anzVar = 5;
		ModulMonomial.invertAnz = 0;
		long time = System.currentTimeMillis();
		final int RUNS = 5;
		for (int i=0; i<RUNS; i++)
		{
//			System.out.println(i);
//			ModulMonomial.computeHilbertResolveOfLeadingMonomials(new Vector[]{in},anzVar);
		}
		time = System.currentTimeMillis()-time;
		System.out.println("Invertierungen Leitmonome = "+(ModulMonomial.invertAnz/RUNS));
		System.out.println("Dauer Leitmonome: "+(time/RUNS));
		ModulMonomial.invertAnz = 0;
		ModulMonomial.cutoutAnz1 = 0;
		ModulMonomial.cutoutAnz2 = 0;
		ModulMonomial.cutoutAnz3 = 0;
		ModulMonomial.findDividerAufrufe = 0;
		ModulMonomial.cutoutSimpel = 0;
		ModulMonomial.dividesAufrufe = 0;
		time = System.currentTimeMillis();
		for (int i=0; i<RUNS; i++)
		{
//			System.out.println(i);
			ModulMonomial.computeHilbertResolve(new Vector[]{in},anzVar,false);
			System.gc();
		}
		time = System.currentTimeMillis()-time;
		System.out.println("Invertierungen Hashtable = "+(ModulMonomial.invertAnz/RUNS));
		System.out.println("Aufrufe divides = "+(ModulMonomial.dividesAufrufe/RUNS));
		System.out.println("Aufrufe findDivider = "+(ModulMonomial.findDividerAufrufe/RUNS));
		System.out.println("Lower order mit Kriterium = "+(ModulMonomial.cutoutAnz1/RUNS));
		System.out.println("Davon auch durch einfaches = "+(ModulMonomial.cutoutSimpel/RUNS));
		System.out.println("Lower order normal = "+(ModulMonomial.cutoutAnz2/RUNS));
		System.out.println("Lower order wegen leer = "+(ModulMonomial.cutoutAnz3/RUNS));
		System.out.println("Dauer Hashtable: "+(time/RUNS));
		ModulMonomial.invertAnz = 0;
		ModulMonomial.invertAnz = 0;
		ModulMonomial.cutoutAnz1 = 0;
		ModulMonomial.cutoutAnz2 = 0;
		ModulMonomial.cutoutAnz3 = 0;
		ModulMonomial.findDividerAufrufe = 0;
		ModulMonomial.cutoutSimpel = 0;
		ModulMonomial.dividesAufrufe = 0;
		time = System.currentTimeMillis();
		for (int i=0; i<RUNS; i++)
		{
//			System.out.println(i);
//			ModulMonomial.computeHilbertResolve2(new Vector[]{in},anzVar);
		}
		time = System.currentTimeMillis()-time;
		System.out.println("Invertierungen Liste = "+(ModulMonomial.invertAnz/RUNS));
		System.out.println("Aufrufe divides = "+(ModulMonomial.dividesAufrufe/RUNS));
		System.out.println("Aufrufe findDivider = "+(ModulMonomial.findDividerAufrufe/RUNS));
		System.out.println("Lower order mit Kriterium = "+(ModulMonomial.cutoutAnz1/RUNS));
		System.out.println("Davon auch durch einfaches = "+(ModulMonomial.cutoutSimpel/RUNS));
		System.out.println("Lower order normal = "+(ModulMonomial.cutoutAnz2/RUNS));
		System.out.println("Lower order wegen leer = "+(ModulMonomial.cutoutAnz3/RUNS));
		System.out.println("Dauer Liste: "+(time/RUNS));
		ModulMonomial.invertAnz = 0;
		ModulMonomial.invertAnz = 0;
		ModulMonomial.cutoutAnz1 = 0;
		ModulMonomial.cutoutAnz2 = 0;
		ModulMonomial.cutoutAnz3 = 0;
		ModulMonomial.findDividerAufrufe = 0;
		ModulMonomial.cutoutSimpel = 0;
		ModulMonomial.dividesAufrufe = 0;
		time = System.currentTimeMillis();
		for (int i=0; i<RUNS; i++)
		{
//			System.out.println(i);
			ModulMonomial.computeHilbertResolve3(new Vector[]{in},anzVar);
			System.gc();
		}
		time = System.currentTimeMillis()-time;
		System.out.println("Invertierungen RotSchwarz = "+(ModulMonomial.invertAnz/RUNS));
		System.out.println("Aufrufe divides = "+(ModulMonomial.dividesAufrufe/RUNS));
		System.out.println("Aufrufe findDivider = "+(ModulMonomial.findDividerAufrufe/RUNS));
		System.out.println("Lower order mit Kriterium = "+(ModulMonomial.cutoutAnz1/RUNS));
		System.out.println("Davon auch durch einfaches = "+(ModulMonomial.cutoutSimpel/RUNS));
		System.out.println("Lower order normal = "+(ModulMonomial.cutoutAnz2/RUNS));
		System.out.println("Lower order wegen leer = "+(ModulMonomial.cutoutAnz3/RUNS));
		System.out.println("Dauer RotSchwarz: "+(time/RUNS));
		

//		QPolynomial[] test = new QPolynomial[]{new QPolynomial("X0^2+X0*X1+1"),new QPolynomial("X0*X1^2-1")};
//		QPolynomial[] test = new QPolynomial[2];
//		test[0] = new QPolynomial("(X1^2+(X0-1)^2-1)*(X0-(1/2)*X1)*(X0+(1/2)*X1)*(X1^2+(X0-(1/2))^2-(1/4))*(X0-X1)*(X0+X1)");
//		test[1] = test[0].derive(1);
		
//		Qelement[][] kern = Qelement.findCoreBasis(mat);
//		QPolynomial[] fac = p1.factorize();
	/*
		QMonomial[] id = QPolynomial.getLeadingTermIdeal(new QPolynomial[]{new QPolynomial("X2^2"),
														  new QPolynomial("X1*X2^3"),
														  new QPolynomial("X1*X2^4"),
														  new QPolynomial("X1^2"),
														  new QPolynomial("X0*X2"),
														  new QPolynomial("X0*X2^3"),
														  new QPolynomial("X0^2*X1*X2^3"),
														  new QPolynomial("X0^4*X2^3"),
														  new QPolynomial("X0^4*X2^4"),
														  new QPolynomial("X0^4*X2^5"),
														  new QPolynomial("X0^6")});
		QMonomial.getRemainderOfIdeal(id);
   */

//   		int anz = QPolynomial.countReelZeros(test);
//   		System.out.println("Anzahl reeler Nullstellen = "+anz);

//		testSingulaerwertzerlegung();
//		testePathtracking();
		testeGuessSystem();
		long dauer = (System.currentTimeMillis()-start);
/*
		for (int i=0;  i<kern.length; i++) 
		{
			out = "[";
			for (int j=0; j<kern[i].length; j++) {out += kern[i][j]; if (j<kern[i].length-1) out += ","; else out+="]";}
			System.out.println(out);
		}
		*/
//		for (int i=0; i<fac.length; i++) System.out.println("Faktor "+i+": "+fac[i]);
//		System.out.println("Dauer = "+dauer);
//		System.out.println("gcd = "+gcd);
/*
		for (int i=0; i<lsg.length; i++)
			System.out.println("Lösung "+i+": "+lsg[i]);


		QPolynomial p1 = new QPolynomial("X0^2*X1+2*X2-3");
		QPolynomial p2 = new QPolynomial("X0^3*X2^2-X1");
		QPolynomial p3 = new QPolynomial("X0+X1+X2-3");

		QPolynomial[] gres = QPolynomial.generalizedResultant(new QPolynomial[]{p1,p2,p3},0);

		for (int i=0; i<gres.length; i++)
			System.out.println("Generalisierte Resultante "+i+": "+gres[i]);
*/
		System.out.println("Fertig."+dauer);
	}
	public void print(String s) 
	{
		System.out.print(s);

		try{
			OutputStreamWriter b = new OutputStreamWriter(new FileOutputStream("TestAusgabe.txt",true));
			b.write(s, 0, s.length());
			b.close();
		} catch (Exception e) {}
		
	}
	public void println() {println("");}
	public void println(String s) {print(s+"\r\n");}
/**
 * Insert the method's description here.
 * Creation date: (13.08.2004 12:24:32)
 */
public static void testeGuessSystem() 
{
	// Lineares System 3x + 2y + z;
//	double[][] punkte = new double[][]{{0,0,0},{1,-1,-1},{1,1,-5},{1,-2,1},{0,-1,2}};
	// Quadric: x^2 + x + y + y^2;
	double[][] punkte = new double[][]{{0,0},{-1,0},{0,-1},{-0.5,0.207},{-0.5,-1.207},{0.207,-0.5},{-1.207,-0.5}};


	DoubleComplex[][] punkteC = new DoubleComplex[punkte.length][punkte[0].length];
	for (int i=0; i<punkte.length; i++)
		for (int j=0; j<punkte[i].length; j++)
			punkteC[i][j] = new DoubleComplex(punkte[i][j]);

	QPolynomial[] erg = QPolynomial.guessSystem(punkteC, 2);
	for (int i=0; i<erg.length; i++)
		System.out.println(erg[i]);
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:42:13)
 */
public static void testePathtracking() 
{
	QPolynomial[] system = new QPolynomial[]
		{new QPolynomial("X0^2+2*X0*X1+5")};

	Complex[] erg = QPolynomial.approximateZeroOfSystem(system,17,0.0001,100);
	System.out.print("Ergebnis: ");
	for (int i=0; i<erg.length; i++) System.out.print(erg[i]+", ");
	System.out.println();

	double d = (5123.0/4321.0) + 0.0000013214243;
	Qelement q = Qelement.guessFromDouble(d);
	System.out.println("Double = "+d);
	System.out.println("Qelement = "+q);
	System.out.println("Zieldouble = "+(5123.0/4321.0));
	System.out.println("Qelement Wert = "+q.toDouble());
}
/**
 * Insert the method's description here.
 * Creation date: (29.07.2004 11:23:08)
 */
public static void testSingulaerwertzerlegung() 
{
	RingMatrix m = new RingMatrix(new double[][]{{1,2,3},{2,1,3},{4,3,7},{1,1,2}});
	RingMatrix[] svd = m.singularValueDecomposition(0.0001);
	RingMatrix[] svd2 = m.singularValueDecomposition2(0.0001);
	
	System.out.println(m);
	System.out.println(svd[0]);
	System.out.println(svd[1]);
	System.out.println(svd[2]);
	System.out.println("Zweite Variante: ");
	System.out.println(svd2[0]);
	System.out.println(svd2[1]);
	System.out.println(svd2[2]);
/*	
	RingMatrix a = new RingMatrix(new double[][]{{0,0,0,1,3},{0,0,0,2,4},{0,0,0,7,1},{1,2,7,0,0},{3,4,1,0,0}});
//	RingMatrix a = new RingMatrix(new double[][]{{2,1,3},{1,2,4},{3,4,2}});
	RingMatrix u = a.unit();
	RingVector v = a.eigenvalues(0.0001,u);
	System.out.println(a);
	System.out.println(v);
	System.out.println(u);
//	for (int i=0; i<ev.length; i++) System.out.println("EV "+i+": "+ev[i]);

	System.out.println();
	Ring[] ev = RingMatrix.eigenvalues22(m.getValue(1,1),m.getValue(1,2),m.getValue(2,1),m.getValue(2,2),m);
	System.out.println(ev[0]);
	System.out.println(ev[1]);
	System.out.println(m);
	*/
}
}
