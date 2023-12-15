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

public class DoubleWrapper implements Field, Squarerootable, Signed, Orderd, DoubleCastable
{
	double value;
	
	public DoubleWrapper(double value)
	{
		this.value = value;
	}
	public Signed abs_abs() {return new DoubleWrapper(Math.abs(value));}
	public Ring abs_add (Ring b) 
		{return new DoubleWrapper(value+((DoubleWrapper)b).value);}
	public int abs_compareTo (Orderd b)	
	{
		if (value < ((DoubleWrapper)b).value) return -1;
		if (value > ((DoubleWrapper)b).value) return 1;
		return 0;
	}
	public Field abs_divide (Field b)
		{return new DoubleWrapper(value/((DoubleWrapper)b).value);}
	public GcdAble abs_divide(GcdAble arg2) {return abs_divide((DoubleWrapper)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		GcdAble[] erg = new GcdAble[2]; erg[0] = abs_divide((DoubleWrapper)arg2); erg[1] = new DoubleWrapper(0.0);
		return erg;
	}
	public GcdAble abs_gcd(GcdAble arg2) {return new DoubleWrapper(1.0);}
	public boolean abs_isEqual (Ring b) 
		{return (value == ((DoubleWrapper)b).value);}
	public Ring abs_multiply (Ring b) 
		{return new DoubleWrapper(value*((DoubleWrapper)b).value);}
	public Ring abs_negate () {return new DoubleWrapper(-value);}
	public Ring abs_pow(long exp) {return new DoubleWrapper(Math.pow(value, (double)exp));}
	public Field abs_reciprocal () {return new DoubleWrapper(1/value); }
	public GcdAble abs_remainder(GcdAble arg2) {return new DoubleWrapper(0.0);}
	public Signed abs_ringSignum() {return new DoubleWrapper((double)abs_signum());}
	public GcdAble abs_scm(GcdAble arg2) {return new DoubleWrapper(1.0);}
	public int abs_signum() {return abs_compareTo(new DoubleWrapper(0.0));}
	public Squarerootable abs_sqrt() {return new DoubleWrapper(Math.sqrt(value));}
	public Ring abs_subtract (Ring b) 
		{return new DoubleWrapper(value-((DoubleWrapper)b).value);}
	public Ring abs_unit () {return new DoubleWrapper(1.0);}
	public Ring abs_zero () {return new DoubleWrapper(0.0);}
	public double doubleValue() {return value;}
	public String toString()
	{
		return toString(4);
	}
	public String toString(int digits) 
	{
		/*
		int dig = Math.min(digits, 10);
		if (dig < 0) dig *= -1;
		String erg = ""+value;
		int ix = erg.indexOf('E');
		int ex=0;
		if (ix != -1)
			ex = Integer.parseInt(erg.substring(ix+1));
		else ix = erg.length();
		int ix2 = erg.indexOf('.');
		if (ix2 != -1) ex -= (ix-ix2-1);
		if (ix2!=-1) erg = erg.substring(0,ix2)+erg.substring(ix2+1);
		while (dig > erg.length()) {erg += "0"; ex--;}
		return erg.substring(0,dig) + "E"+(ex+erg.length()-dig);		
		*/
		return value+"";
	}

/**
 * doubleNorm method comment.
 */
public double doubleNorm() 
{
	return Math.abs(value);
}
}
