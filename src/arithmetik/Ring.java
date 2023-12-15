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

public interface Ring
{
	public Ring abs_add (Ring b);
	public boolean abs_isEqual (Ring b);
	public Ring abs_multiply (Ring b);
	public Ring abs_negate ();
	public Ring abs_pow(long exp);
	public Ring abs_subtract (Ring b);
	public Ring abs_unit ();
	public Ring abs_zero ();
	public String toString();
}
