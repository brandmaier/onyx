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

public abstract class Ring<R extends Ring<R>> {

    public abstract R plus(R second);
    public R minus(R second) {return plus(second.negate());}
    public abstract R negate();
    public abstract boolean isZero();
    public abstract R zero();
    public abstract R one();
    public abstract R times(R second);
}
