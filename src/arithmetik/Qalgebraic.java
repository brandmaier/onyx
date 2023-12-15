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
 * Creation date: (23.05.2002 10:45:05)
 * @author: 
 */
public class Qalgebraic implements Field, DoubleNormable
{
	QPolynomial mod;
	QPolynomial rep;
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:50:12)
 */
public Qalgebraic(Qalgebraic q)
{
	this.mod = new QPolynomial(q.mod);
	this.rep = new QPolynomial(q.rep);
}
/**
 * Qadjunct constructor comment.
 */
public Qalgebraic(QPolynomial mod) 
{
	this.mod = new QPolynomial(mod);
	this.rep = new QPolynomial();
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:55:58)
 */
public Qalgebraic(QPolynomial mod, Qelement q)
{
  this.mod = mod;
  this.rep = new QPolynomial(q);
}

/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:48:36)
 */
public Qalgebraic(QPolynomial mod, QPolynomial rep)
{
	this.mod = new QPolynomial(mod);
	this.rep = new QPolynomial(rep);
}
/**
 * abs_add method comment.
 */
public Ring abs_add(Ring b) {
	return add((Qalgebraic) b);
}
/**
 * abs_divide method comment.
 */
public Field abs_divide(Field b) {
	return divide((Qalgebraic) b);
}
/**
 * abs_divide method comment.
 */
public GcdAble abs_divide(GcdAble b) {
	return divide((Qalgebraic) b);
}
/**
 * abs_divideAndRemainder method comment.
 */
public arithmetik.GcdAble[] abs_divideAndRemainder(GcdAble b) {
	return new Qalgebraic[]{divide((Qalgebraic) b),new Qalgebraic(mod)};
}
/**
 * abs_gcd method comment.
 */
public GcdAble abs_gcd(GcdAble arg2) {
	return new Qalgebraic(mod, Qelement.ONE);
}
/**
 * abs_isEqual method comment.
 */
public boolean abs_isEqual(Ring b) {
	return subtract((Qalgebraic) b).isZero();
}
/**
 * abs_multiply method comment.
 */
public Ring abs_multiply(Ring b) {
	return multiply((Qalgebraic) b);
}
/**
 * abs_negate method comment.
 */
public Ring abs_negate() {
	return negate();
}
/**
 * abs_pow method comment.
 */
public Ring abs_pow(long exp) {
	return pow(exp);
}
/**
 * abs_reciprocal method comment.
 */
public Field abs_reciprocal() {
	return reciprocal();
}
/**
 * abs_remainder method comment.
 */
public GcdAble abs_remainder(GcdAble arg2) {
	return new Qalgebraic(mod);
}
/**
 * abs_scm method comment.
 */
public GcdAble abs_scm(GcdAble arg2) 
{
	return new Qalgebraic(mod, Qelement.ONE);
}
/**
 * abs_subtract method comment.
 */
public Ring abs_subtract(Ring b) 
{
	return subtract((Qalgebraic) b);
}
/**
 * abs_unit method comment.
 */
public Ring abs_unit() 
{
	return new Qalgebraic(mod, Qelement.ONE);
}
/**
 * abs_zero method comment.
 */
public Ring abs_zero() {
	return new Qalgebraic(mod);
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:51:48)
 */
public Qalgebraic add (Qalgebraic arg2)
{
  Qalgebraic erg = new Qalgebraic(this);
  erg.rep = (erg.rep.add(arg2.rep)).remainder(mod);
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:58:13)
 */
public Qalgebraic alpha()
{
  Qalgebraic erg = new Qalgebraic(mod);
  erg.rep = new QPolynomial(0);
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:53:49)
 */
public Qalgebraic divide (Qalgebraic arg2)
{
  return multiply(arg2.reciprocal());
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:58:39)
 */
public double doubleNorm()
{
  return toCelement().doubleNorm();
}
/**
 * Insert the method's description here.
 * Creation date: (28.05.2002 09:36:51)
 * @return arithmetik.Qalgebraic
 */
public Qalgebraic getX() 
{
	return new Qalgebraic(mod, new QPolynomial(0));
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:57:13)
 */
public boolean isEqual(Qalgebraic arg2)
{
  return subtract(arg2).isZero();
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:56:52)
 */
public boolean isRational()
{
  return rep.isConstant();
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:56:18)
 */
public boolean isZero()
{
  return rep.isZero();
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:53:19)
 */
public Qalgebraic multiply (Qalgebraic arg2)
{
  Qalgebraic erg = new Qalgebraic(this);
  erg.rep = (erg.rep.multiply(arg2.rep)).remainder(mod);
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:54:41)
 */
public Qalgebraic negate()
{
  Qalgebraic erg = new Qalgebraic(this);
  erg.rep = erg.rep.negate();
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:55:23)
 */
public Qalgebraic pow(long exp)
{
  if (exp < 0) return pow(-exp).reciprocal();
  if (exp==0) return new Qalgebraic(mod, Qelement.ONE);
  long e = exp/2;
  return pow(e).multiply(pow(exp-e));
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:54:15)
 */
public Qalgebraic reciprocal()
{
  QPolynomial[] bez = rep.getBezout(mod);
  Qalgebraic erg = new Qalgebraic(mod);
  erg.mod = mod;
  erg.rep = bez[0];
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:55:03)
 */
public Qalgebraic sqr()
{
  return multiply(this);
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:52:45)
 */
public Qalgebraic subtract (Qalgebraic arg2)
{
  Qalgebraic erg = new Qalgebraic(this);
  erg.rep = (erg.rep.subtract(arg2.rep)).remainder(mod);
  return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:57:41)
 */
public Celement toCelement()
{
  if (mod.isConstant()) return new Celement();
  if (rep.isConstant()) return new Celement(rep.leadingFactor());
  Celement[] wr = mod.qralgorithm(0, new Qelement(1,100));

  Celement alpha = wr[0];
  for (int i=1; i<wr.length; i++)
   if (wr[i].doubleNorm() > alpha.doubleNorm()) alpha = wr[i];

  return rep.evaluate(0,alpha);
}
/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 14:59:05)
 */
public String toString()
{
  return "{ "+rep.toString()+" where 0 == "+mod.toString()+" }";
}
}
