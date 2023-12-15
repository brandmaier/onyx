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
 * Created on 30.11.2013
 */
package groebner;

import engine.Statik;

public class DoubleField extends Field<DoubleField> {

    public final static DoubleField ZERO = new DoubleField(0.0);
    public final static DoubleField ONE = new DoubleField(1.0);
    public final static DoubleField TWO = new DoubleField(2.0);
    
    public double value;
    public DoubleField(double value) {this.value = value;}

    @Override
    public DoubleField one() {return ONE;}
    
    @Override
    public DoubleField zero() {return ZERO;}
    
    @Override
    public DoubleField times(DoubleField second) {return new DoubleField(value * second.value);}

    @Override
    public DoubleField over(DoubleField second) {return new DoubleField(value / second.value);}

    @Override
    public DoubleField plus(DoubleField second) {return new DoubleField(value + second.value);}

    @Override
    public DoubleField minus(DoubleField second) {return new DoubleField(value - second.value);}

    @Override
    public DoubleField inverse() {return new DoubleField(1.0 / value);}

    @Override
    public DoubleField negate() {return new DoubleField(-value);}
    
    @Override
    public boolean isZero() {return value == 0.0;}
    
    public String toString() {
        return Statik.doubleNStellen(value, 3);
    }
    
}
