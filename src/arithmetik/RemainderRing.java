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

public class RemainderRing implements Field
{
	private final BigInteger i1 = BigInteger.valueOf(1);
	private final BigInteger i0 = BigInteger.valueOf(0);
	private final BigInteger i2 = BigInteger.valueOf(2);

	final Modulus modulo;
	final BigInteger value;
	
	public RemainderRing(Modulus p)
	{
		modulo = p;
		value = i0.add(i0);
	}

	public RemainderRing(long v, Modulus p)
	{
		modulo = p;
		BigInteger val = BigInteger.valueOf(v).mod(modulo.modulo);
		if (val.compareTo(modulo.halfmod) >= 0) value = val.subtract(modulo.modulo);
		else value = val;
	}

	public RemainderRing(BigInteger v, Modulus p)
	{
		modulo = p;
		BigInteger val = v.mod(modulo.modulo);
		if (val.compareTo(modulo.halfmod) >= 0) value = val.subtract(modulo.modulo);
		else value = val;
	}

	public RemainderRing(RemainderRing a)
	{
		modulo = a.modulo;
		value = a.value.add(i0);
	}

// Ende der Konstruktoren... Nun die Methoden
	
	public boolean isZero()
	{
		return (value.signum() == 0);
	}
	
	public RemainderRing add (RemainderRing b)
	{
		if ( !(modulo.isEqual(b.modulo)) ) throw new WrongFieldException("addieren");
		RemainderRing erg = new RemainderRing(value.add(b.value), modulo);
		return erg;
	}
	
	public Ring abs_add (Ring b)
	{
		return add((RemainderRing)b);
	}

	public boolean isEqual (RemainderRing b)
	{
		if ( !(modulo.isEqual(b.modulo)) ) throw new WrongFieldException("vergleichen");
		return value.equals(b.value);
	}

	public boolean abs_isEqual (Ring b)
	{
		return isEqual((RemainderRing)b);
	}
	
	public RemainderRing multiply (RemainderRing b)
	{
		if ( !(modulo.isEqual(b.modulo)) ) throw new WrongFieldException("multiplizieren");
		return new RemainderRing(value.multiply(b.value),modulo);
	}

	public Ring abs_multiply (Ring b)
	{
		return multiply((RemainderRing)b);
	}
	
	public RemainderRing negate()
	{
		return new RemainderRing(modulo.modulo.subtract(value),modulo);
	}
	
	public Ring abs_negate ()
	{
		return negate();
	}
	
	public RemainderRing pow(long exp)
	{
		BigInteger power = value;
		BigInteger erg = i1.add(i0);
		while (exp != 0)
		{
			if ( (exp % 2) == 1)
				erg = erg.multiply(power).mod(modulo.modulo);
			power = (power.pow(2)).mod(modulo.modulo);
			exp = exp/2;
		}
		return new RemainderRing(erg,modulo);
	}

	public Ring abs_pow(long exp)
	{
		return pow(exp);
	}
	
	public RemainderRing subtract(RemainderRing b)
	{
		if ( !(modulo.isEqual(b.modulo)) ) throw new WrongFieldException("subtrahieren");
		return new RemainderRing(value.subtract(b.value),modulo);
	}
	
	public Ring abs_subtract (Ring b)
	{
		return subtract((RemainderRing)b);
	}
	
	public RemainderRing unit()
	{
		return new RemainderRing(i1,modulo);
	}
	
	public Ring abs_unit ()
	{
		return unit();
	}
	
	public RemainderRing zero()
	{
		return new RemainderRing(i0,modulo);
	}
	
	public Ring abs_zero ()
	{
		return zero();
	}
	
	public String toString()
	{
		return value + " mod " + modulo.modulo;
	}

	public RemainderRing divide (RemainderRing b)
	{
		if ( !(modulo.isEqual(b.modulo)) ) throw new WrongFieldException("dividieren");
		BigInteger tmp = b.value.modInverse(modulo.modulo);
		return new RemainderRing(value.multiply(tmp),modulo);
	}

	public Field abs_divide (Field b)
	{
		return divide((RemainderRing)b);
	}
	
	public RemainderRing reciprocal ()
	{
		BigInteger tmp = value.modInverse(modulo.modulo);
		return new RemainderRing(tmp,modulo);
	}
	
	public Field abs_reciprocal ()
	{
		return reciprocal();
	}
	
	public GcdAble abs_divide(GcdAble arg2)
	{
		return divide((RemainderRing) arg2);
	}
	
	public RemainderRing[] divideAndRemainder(RemainderRing b)
	{
		RemainderRing[] r = new RemainderRing[2];
		r[0] = divide(b);
		r[1] = zero();
		return r;
	}
	
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2)
	{
		return divideAndRemainder((RemainderRing)arg2);
	}
	
	public RemainderRing gcd(RemainderRing b)
	{
		return unit();
	}
	
	public GcdAble abs_gcd(GcdAble arg2)
	{
		return gcd((RemainderRing)arg2);
	}
		
	public RemainderRing remainder(RemainderRing b)
	{
		return zero();
	}
	
	public GcdAble abs_remainder(GcdAble arg2)
	{
		return remainder((RemainderRing) arg2);
	}
	
	public RemainderRing scm(RemainderRing b)
	{
		return unit();
	}
	
	public GcdAble abs_scm(GcdAble arg2)
	{
		return scm((RemainderRing)arg2);
	}
		
	/**
	 * Liftet ein Element modulo n zu einem modulo m
	 */
	public RemainderRing lift(Modulus m)
	{
		return new RemainderRing(value,m);
	}

}


