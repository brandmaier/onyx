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

/**
 * Insert the type's description here.
 * Creation date: (06.02.2003 14:31:09)
 * @author: 
 */
public class QuotientField implements Field
{
	Ring zaehler, nenner;
/**
 * QuotientField constructor comment.
 */
public QuotientField(Ring zaehler) 
{
	this.zaehler = zaehler;
	nenner = zaehler.abs_unit();
}
/**
 * QuotientField constructor comment.
 */
public QuotientField(Ring zaehler, Ring nenner) 
{
	this.zaehler = zaehler;
	this.nenner = nenner;
}
/**
 * abs_add method comment.
 */
public Ring abs_add(Ring b) 
{
	QuotientField q = (QuotientField)b;
	if (zaehler instanceof GcdAble)
	{
		GcdAble scm = ((GcdAble)nenner).abs_scm((GcdAble)q.nenner);
		Ring z1 = ((Ring)scm.abs_divide((GcdAble)nenner)).abs_multiply(zaehler);
		Ring z2 = ((Ring)scm.abs_divide((GcdAble)q.nenner)).abs_multiply(q.zaehler);
		return new QuotientField(z1.abs_add(z2),(Ring)scm);
	}
	return new QuotientField(zaehler.abs_multiply(q.nenner).abs_add(q.zaehler.abs_multiply(nenner)),nenner.abs_multiply(q.nenner));
}
/**
 * abs_divide method comment.
 */
public Field abs_divide(Field b) 
{
	QuotientField q = (QuotientField)b;
	Ring nz = zaehler.abs_multiply(q.nenner);
	Ring nn = nenner.abs_multiply(q.zaehler);
	if (zaehler instanceof GcdAble)
	{
		GcdAble gcd = ((GcdAble)nz).abs_gcd((GcdAble)nn);
		nz = (Ring)((GcdAble)nz).abs_divide(gcd);
		nn = (Ring)((GcdAble)nn).abs_divide(gcd);
	}		
	return new QuotientField(nz,nn);
}
/**
 * abs_divide method comment.
 */
public GcdAble abs_divide(GcdAble arg2) 
{
	return (GcdAble)abs_divide(arg2);
}
/**
 * abs_divideAndRemainder method comment.
 */
public arithmetik.GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
{
	return new GcdAble[]{(GcdAble)abs_divide(arg2),(GcdAble)abs_unit()};
}
/**
 * abs_gcd method comment.
 */
public GcdAble abs_gcd(GcdAble arg2) 
{
	return (GcdAble)abs_unit();
}
/**
 * abs_isEqual method comment.
 */
public boolean abs_isEqual(Ring b) 
{
	QuotientField a = (QuotientField)abs_subtract(b);
	return a.abs_isEqual(a.abs_zero());
}
/**
 * abs_multiply method comment.
 */
public Ring abs_multiply(Ring b)
{
	QuotientField q = (QuotientField)b;
	Ring nz = zaehler.abs_multiply(q.zaehler);
	Ring nn = nenner.abs_multiply(q.nenner);
	if (zaehler instanceof GcdAble)
	{
		GcdAble gcd = ((GcdAble)nz).abs_gcd((GcdAble)nn);
		nz = (Ring)((GcdAble)nz).abs_divide(gcd);
		nn = (Ring)((GcdAble)nn).abs_divide(gcd);
	}		
	return new QuotientField(nz,nn);
}
/**
 * abs_negate method comment.
 */
public Ring abs_negate() 
{
	return new QuotientField(zaehler.abs_negate(),nenner);
}
/**
 * abs_pow method comment.
 */
public Ring abs_pow(long exp) 
{
	return new QuotientField(zaehler.abs_pow(exp),nenner.abs_pow(exp));
}
/**
 * abs_reciprocal method comment.
 */
public Field abs_reciprocal() 
{
	return new QuotientField(nenner,zaehler);
}
/**
 * abs_remainder method comment.
 */
public GcdAble abs_remainder(GcdAble arg2) 
{
	return (GcdAble)abs_zero();
}
/**
 * abs_scm method comment.
 */
public GcdAble abs_scm(GcdAble arg2) 
{
	return (GcdAble)abs_unit();
}
/**
 * abs_subtract method comment.
 */
public Ring abs_subtract(Ring b) 
{
	return abs_add(b.abs_negate());
}
/**
 * abs_unit method comment.
 */
public Ring abs_unit() 
{
	return new QuotientField(zaehler.abs_unit(),nenner.abs_unit());
}
/**
 * abs_zero method comment.
 */
public Ring abs_zero() 
{
	return new QuotientField(zaehler.abs_zero(),nenner.abs_unit());
}
}
