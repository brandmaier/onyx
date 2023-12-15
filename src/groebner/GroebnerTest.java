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
 * Created on 01.12.2013
 */
package groebner;

import junit.framework.TestCase;

public class GroebnerTest extends TestCase {

    public void testSimpleExample() {
        // first one is y, second is x.
        Monomial<DoubleField> x2y = new Monomial<DoubleField>(DoubleField.ONE, new int[]{1,2});
        Monomial<DoubleField> one = new Monomial<DoubleField>(DoubleField.ONE, new int[]{0,0});
        Monomial<DoubleField> two = new Monomial<DoubleField>(DoubleField.TWO, new int[]{0,0});
        Monomial<DoubleField> x2 = new Monomial<DoubleField>(DoubleField.ONE, new int[]{0,2});
        Monomial<DoubleField> y2 = new Monomial<DoubleField>(DoubleField.ONE, new int[]{2,0});
        
        Monomial<DoubleField>[][] f = new Monomial[][]{{x2y,two},{y2,x2}};
        Dome<DoubleField> dome = new Dome<DoubleField>(f, DoubleField.ONE);
        dome.extendToGroebner();
        assertEquals(dome.generator.length,3);
        assertEquals(dome.generator[2][0].exp[0], 0);
        assertEquals(dome.generator[2][0].exp[1], 4);
        assertEquals(dome.generator[2][0].coeff.value, -1.0);
        assertEquals(dome.generator[2][1].exp[0], 1);
        assertEquals(dome.generator[2][1].exp[1], 0);
        assertEquals(dome.generator[2][1].coeff.value, 1.0);
    }
}
