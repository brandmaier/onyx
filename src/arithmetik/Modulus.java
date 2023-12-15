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

import java.math.*;

public class Modulus
{
	private final BigInteger i1 = BigInteger.valueOf(1);
	private final BigInteger i0 = BigInteger.valueOf(0);
	private final BigInteger i2 = BigInteger.valueOf(2);
	final BigInteger modulo;
	private int certainity = 500;
	final boolean isPrime;
	BigInteger halfmod;
	/**
	 * Erzeuge einen neuen Modulus mir Wert mod
	 */
	public Modulus(long mod)
	{
		modulo = BigInteger.valueOf(mod);
		isPrime=modulo.isProbablePrime(certainity);
		halfmod = (modulo.add(i1)).divide(i2);
	}
/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 14:35:34)
 * @param mod java.math.BigInteger
 */
public Modulus(BigInteger mod) 
{
	modulo = mod.add(BigInteger.valueOf(0));
	isPrime=modulo.isProbablePrime(certainity);
	halfmod = (modulo.add(i1)).divide(i2);
}
	private Modulus(BigInteger mod, boolean ip)
	{
		isPrime = ip;
		modulo = mod.add(i0);
		halfmod = (modulo.add(i1)).divide(i2);
	}
	/**
	 * Testet, ob zwei Moduln gleich sind
	 */
	public boolean isEqual(Modulus mod)
	{
	   return  (mod.modulo.equals(modulo));
	}
	/**
	 *  Erzeugt einen zufälligen modulus <= max, der Primzahl ist, falls prime gesetzt ist.
	 */
	public static Modulus rndModulus(long max, boolean prime)
	{
		Modulus mod = new Modulus(Math.round(Math.random()*max));
		while ( !( mod.isPrime || !prime) )
			mod = new Modulus(Math.round(Math.random()*max));
		return mod;
	}
	/**
	 * Liefert den Modulus zum Quadrat
	 */
	public Modulus square()
	{
		return new Modulus(modulo.pow(2),false);
	}
	/**
	 * liefert den BigInteger-Wert des Moduls
	 */
	public BigInteger toBigInt()
	{
		return modulo.add(i0);
	}
	public String toString()
	{
		return "" + modulo;
	}
}
