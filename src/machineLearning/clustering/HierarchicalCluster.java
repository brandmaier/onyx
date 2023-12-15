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
 * Created on 04.05.2011
 */
package machineLearning.clustering;

import engine.Statik;

public class HierarchicalCluster  {

    public HierarchicalCluster[] children;
    public int[] member;
    public double distance;
    
    public HierarchicalCluster(int[] member, HierarchicalCluster[] children) {
        this.member = member; this.children = children;
    }
    
    public String toString() {
        return toString(0);
    }
    public String toString(int depth) {
        String erg = ""; for (int i=0; i<depth; i++) erg += "  ";
        erg += "Cluster ("+Statik.matrixToString(member)+")\r\n";
        if (children!=null) for (int i=0; i<children.length; i++) erg += children[i].toString(depth+1);
        return erg;
    }

    public String toShortString() {
        return "("+Statik.matrixToString(member)+")";
    }
}
