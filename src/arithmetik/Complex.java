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

public interface Complex extends Field
{
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:10:12)
 * @return double
 */
double abs_doubleNorm();
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:55:55)
 * @return arithmetik.Complex
 */
public Complex abs_fromDouble(double reel, double imag);
	public Complex conjugate();
	public double imagValue();
	public double reelValue();
}
