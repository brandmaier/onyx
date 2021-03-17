package arithmetik;
import java.math.*;
import java.util.*;

public class FactorTree
{
	final RemainderRingPolynomial value;
	final FactorTree leftSubTree;
	final RemainderRingPolynomial leftMultiplier;
	final FactorTree rightSubTree;
	final RemainderRingPolynomial rightMultiplier;
	final RemainderRing inverse;
	final Modulus modulo;
	final UnivariatePolynomial root;
	
	private FactorTree(RemainderRingPolynomial v, FactorTree lt, RemainderRingPolynomial lm, FactorTree rt, RemainderRingPolynomial rm, Modulus m)
	{
		value = v;
		leftSubTree = lt;
		leftMultiplier =lm ;
		rightSubTree = rt;
		rightMultiplier = rm;
		inverse=null;
		modulo = m;
		root = null;
	}

	private FactorTree(UnivariatePolynomial urt, RemainderRing i, RemainderRingPolynomial v, FactorTree lt, RemainderRingPolynomial lm, FactorTree rt, RemainderRingPolynomial rm, Modulus m)
	{
		value = v;
		leftSubTree = lt;
		leftMultiplier =lm ;
		rightSubTree = rt;
		rightMultiplier = rm;
		inverse = i;
		modulo = m;
		root = urt;
	}
	
	/**
	 * Erzeugt einen Faktorbaum mit Faktoren von f
	 */
	public FactorTree(UnivariatePolynomial fu, Modulus m)
	{
		RemainderRingPolynomial f = new RemainderRingPolynomial(fu,m);
		modulo = m;
		root = new UnivariatePolynomial(fu);
		inverse = (f.leadingCoefficient()).reciprocal();
//		System.out.println("Inverse is : lc*i=1 : " + f.leadingCoefficient() + " * " + inverse + " = " +f.leadingCoefficient().multiply(inverse));
//		System.out.println("Factorizing : " + f.monomialMultiply(inverse,0) );
		Stack erg = (f.monomialMultiply(inverse,0)).factorize();
		int number = erg.size();
		FactorTree[] nodes = new FactorTree[number];
		// die Bl„tter des Baumes sind die irreduziblen Faktoren
		for (int i = 0; i < number ; i++ )
		{
			nodes[i] = new FactorTree((RemainderRingPolynomial)erg.pop(),null,null,null,null,modulo);
//			System.out.println("true factor " + nodes[i].value);
		}
		
		// Nun mssen wir die Knoten induktiv erzeugen, bis wir die Wurzel erzeugt haben.
		
		for (int j = 1 ; j<number ; j *= 2 )
		{
			for (int i=0 ; i+j < number ; i+=2*j)
			{
				RemainderRingPolynomial[] xggt = nodes[i].value.extGcd(nodes[i+j].value);
				nodes[i] = new FactorTree(nodes[i].value.multiply(nodes[i+j].value),nodes[i],xggt[0],nodes[i+j],xggt[1],modulo);
			}
		}
		// Nun steht in nodes[0] die Wurzel...
		value = nodes[0].value;
		leftSubTree = nodes[0].leftSubTree;
		rightSubTree = nodes[0].rightSubTree;
		leftMultiplier = nodes[0].leftMultiplier;
		rightMultiplier = nodes[0].rightMultiplier;
	}

	/**
	 * Gibt den Faktorbaum aus
	 */
	public String toString()
	{
		/* geändert TvO
		String erg = "Root ";
		erg = erg + root;
		erg = erg + " Modulo: ";
		erg = erg + value +"\n";
		erg = erg + leftMultiplier + " | " + rightMultiplier + "\n";
		if (leftSubTree != null) erg = erg + "Left: "+ leftSubTree.toString();
		if (rightSubTree != null) erg = erg + "Right: "+ rightSubTree.toString();
		return erg;
		*/
		return toString(0);
	}
	
	/**
	 * Liftet den Faktorbaum mittels Hensel-Lifting
	 */
	public FactorTree lift()
	{
		Modulus nm = modulo.square();
		RemainderRing i = inverse.lift(nm);
		RemainderRing newi = i.multiply(new RemainderRing(2,nm));
		RemainderRing lcmod = new RemainderRing(root.leadingCoefficient(),nm);
		newi = newi.subtract(lcmod.multiply(i).multiply(i));
		return lift(this,(new RemainderRingPolynomial(root,nm)).monomialMultiply(newi,0),newi,root,nm);
	}
	
	private FactorTree lift(FactorTree node, RemainderRingPolynomial lifted, Modulus m)
	{
		if (node.leftSubTree == null)
			return new FactorTree(lifted,null,null,null,null,m);
		RemainderRingPolynomial[] io = new RemainderRingPolynomial[4];
		io[0] = node.leftSubTree.value;
		io[1] = node.rightSubTree.value;
		io[2] = node.leftMultiplier;
		io[3] = node.rightMultiplier;
		io = lifted.henselStep(io,m);
		return new FactorTree(lifted,lift(node.leftSubTree,io[1],m),io[3],lift(node.rightSubTree,io[2],m),io[4],m);
	}

	private FactorTree lift(FactorTree node, RemainderRingPolynomial lifted, RemainderRing i,UnivariatePolynomial rt, Modulus m)
	{
		if (node.leftSubTree == null) return new FactorTree(rt,i,lifted,null,null,null,null,m);
		RemainderRingPolynomial[] io = new RemainderRingPolynomial[4];
		io[0] = node.leftSubTree.value;
		io[1] = node.rightSubTree.value;
		io[2] = node.leftMultiplier;
		io[3] = node.rightMultiplier;
		io = lifted.henselStep(io,m);
		return new FactorTree(rt,i,lifted,lift(node.leftSubTree,io[1],m),io[3],lift(node.rightSubTree,io[2],m),io[4],m);
	}
	
	/**
	 * Liefert die Blätter des Faktorbaumes als RemainderRingPolynomial-Array
	 */
	public RemainderRingPolynomial[] getLeaves()
	{
		Stack est = new Stack();
		getLeaves(est);
		RemainderRingPolynomial[] erg = new RemainderRingPolynomial[est.size()];
		int i = 0;
		while ( !(est.isEmpty()) )
		{
			erg[i] = (RemainderRingPolynomial)est.pop();
			i++;
		}
		return erg;
	}
	
	private void getLeaves(Stack erg)
	{
		if ( leftSubTree != null) leftSubTree.getLeaves(erg);
		if ( rightSubTree != null) rightSubTree.getLeaves(erg);
		if ( ( leftSubTree == null) && (rightSubTree == null) ) erg.push(value);
	}
	
	public boolean check()
	{
		if ( leftSubTree != null)
		{
			if (rightSubTree == null) return false;
			else return (leftSubTree.check() && rightSubTree.check());
		} else {
			return (rightSubTree == null);
		}
	}
	
	public String leavesToString()
	{
		if ( leftSubTree != null) return (leftSubTree.leavesToString() + rightSubTree.leavesToString());
		else return "~~  " + value.toString() + "\n";
	}

/**
 * Insert the method's description here.
 * Creation date: (16.01.2003 17:45:07)
 * @return boolean
 * @param c java.math.BigInteger
 */
public boolean modulusSmallerThan(BigInteger c) 
{
	return (modulo.modulo.compareTo(c) == -1);
}

	/**
	 * Gibt den Faktorbaum aus
	 */
	public String toString(int einrueck)
	{
		String val = ""+value.deg;
		String erg = "";
		if (leftSubTree!=null) erg += leftSubTree.toString(einrueck+val.length());
		erg += "\r\n";
		for (int i=0; i<einrueck; i++) erg += " ";
		erg += val+"\r\n";
		if (rightSubTree!=null) erg += rightSubTree.toString(einrueck+val.length());
		return erg;
	}
}
