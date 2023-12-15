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

public class BigIntWrapper implements Ring, Signed, Orderd, DoubleCastable, GcdAble
{
	BigInteger value;
	
	public BigIntWrapper(long value)
	{
		this.value = BigInteger.valueOf(value);
	}
	public BigIntWrapper(BigInteger value)
	{
		this.value = new BigInteger(value.toByteArray());
	}
	public Signed abs_abs() {return new BigIntWrapper(value.abs());}
	public Ring abs_add (Ring b) 
		{return new BigIntWrapper(value.add(((BigIntWrapper)b).value));}
	public int abs_compareTo (Orderd b)	
		{return (value.compareTo(((BigIntWrapper)b).value));}
	public GcdAble abs_divide(GcdAble arg2) {return new BigIntWrapper(value.divide(((BigIntWrapper)arg2).value));}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2)
	{
		BigInteger[] zerg = value.divideAndRemainder(((BigIntWrapper)arg2).value);
		BigIntWrapper[] erg = new BigIntWrapper[2];
		erg[0] = new BigIntWrapper(zerg[0]);
		erg[1] = new BigIntWrapper(zerg[1]);
		return erg;		
	}
	public GcdAble abs_gcd(GcdAble arg2) {return new BigIntWrapper(value.gcd(((BigIntWrapper)arg2).value));}
	public boolean abs_isEqual (Ring b) 
		{return (value.equals(((BigIntWrapper)b).value));}
	public Ring abs_multiply (Ring b) 
		{return new BigIntWrapper(value.multiply(((BigIntWrapper)b).value));}
	public Ring abs_negate () {return new BigIntWrapper(value.negate());}
	public Ring abs_pow(long exp) {return new BigIntWrapper(value.pow((int)exp));}
	public GcdAble abs_remainder(GcdAble arg2) {return new BigIntWrapper(value.remainder(((BigIntWrapper)arg2).value));}
	public Signed abs_ringSignum() {return new BigIntWrapper(value.signum());}
	public GcdAble abs_scm(GcdAble arg2)
	{
		BigInteger gcd = value.gcd(((BigIntWrapper)arg2).value);
		return new BigIntWrapper(((BigIntWrapper)arg2).value.multiply(value.divide(gcd)));
	}
	public int abs_signum() {return value.signum();}
	public Ring abs_subtract (Ring b) 
		{return new BigIntWrapper(value.subtract(((BigIntWrapper)b).value));}
	public Ring abs_unit () {return new BigIntWrapper(1);}
	public Ring abs_zero () {return new BigIntWrapper(0);}
	public double doubleValue() {return value.doubleValue();}
	public String toString() {return ""+value;}

/**
 * doubleNorm method comment.
 */
public double doubleNorm() 
{
	return Math.abs(doubleValue());
}
}
