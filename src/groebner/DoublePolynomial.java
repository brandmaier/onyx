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
/*
 * Created on 08.10.2018
 */
package groebner;


public class DoublePolynomial extends ListPolynomial<DoubleField> {

    public static final DoublePolynomial ZERO = new DoublePolynomial(0.0);
    public static final DoublePolynomial ONE = new DoublePolynomial(1.0);
    public static final DoublePolynomial TWO = new DoublePolynomial(2.0);
    
    public DoublePolynomial() {super(new DoubleField(0.0));}
    
    public DoublePolynomial(double scalar) {
        this();
        if (scalar != 0.0) this.addMonomial(new Monomial<DoubleField>(new DoubleField(scalar), new int[0]));
    }
    
    public DoublePolynomial(int variable) {super(DoubleField.ONE, variable);}

    public DoublePolynomial(DoublePolynomial toCopy) {super(toCopy);}
    
    public void addMonomial(double coeff, int variable) {
        int[] exp = new int[variable+1]; exp[variable] = 1;
        super.addMonomial(new Monomial<DoubleField>(new DoubleField(coeff), exp));
    }
    
    public DoublePolynomial times(DoublePolynomial arg) {
        DoublePolynomial erg = new DoublePolynomial(this);
        erg.multiply(arg);
        return erg;
    }
    
//    public void multiply(DoublePolynomial arg) {
//        this.multiply(arg);
//    }
}
