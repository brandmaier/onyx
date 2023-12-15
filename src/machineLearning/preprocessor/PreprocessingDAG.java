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
 * Created on 09.10.2014
 */
package machineLearning.preprocessor;

import java.util.Stack;
import java.util.Vector;

/**
 * Combines multiple preprocessor into a DAG. All roots use the original data set and target, all other nodes use the transformed data set from all their inputs combined. 
 * 
 * @author timo
 */
public class PreprocessingDAG extends Preprocessor {

    private class PreprocessingDAGNode {
        protected Preprocessor preprocessor;
        protected Vector<PreprocessingDAGNode> next;
        protected Vector<PreprocessingDAGNode> prev;
        protected double[] transformResult;
        protected double[][] matrixTransformResult;
        PreprocessingDAGNode(Preprocessor preprocessor) {this.preprocessor = preprocessor; next = new Vector<PreprocessingDAGNode>(); prev = new Vector<PreprocessingDAGNode>();}
        protected boolean finished;
    }
    
    private boolean training;
    private Vector<PreprocessingDAGNode> node;

    public PreprocessingDAG(Preprocessor[] processor) {
        node = new Vector<PreprocessingDAGNode>(processor.length); for (int i=0; i<processor.length; i++) node.add(new PreprocessingDAGNode(processor[i]));
    }
    
    public void link(Preprocessor a, Preprocessor b) {
        int ia=0; while (ia<node.size() && node.elementAt(ia).preprocessor != a) ia++;
        int ib=0; while (ib<node.size() && node.elementAt(ib).preprocessor != b) ib++;
        if (ia < node.size() && ib < node.size() && !node.elementAt(ia).next.contains(node.elementAt(ib)))
            node.elementAt(ia).next.add(node.elementAt(ib)); node.elementAt(ib).prev.add(node.elementAt(ia));
    }
    public void link(Preprocessor[] a, Preprocessor[] b) {for (int i=0; i<a.length; i++) for (int j=0; j<b.length; j++) link(a[i], b[j]);}

    private void combineNode(PreprocessingDAGNode node, int start, int end, double[] input) {
        if (node.prev.size() == 0) if (training) node.preprocessor.setDataAndTarget(data, target); else node.transformResult = node.preprocessor.transform(input); 
        if (node.prev.size() == 1) if (training) node.preprocessor.setDataAndTarget(node.prev.elementAt(0).matrixTransformResult, target); 
                                   else node.transformResult = node.preprocessor.transform(node.prev.elementAt(0).transformResult);
        if (node.prev.size() >= 2) {
            int k=0; for (PreprocessingDAGNode n:node.prev) k += (training?n.matrixTransformResult[0].length:n.transformResult.length);
            double[][] nData=null; double[] nRow=null; if (training) nData = new double[data.length][k]; else nRow = new double[k];
            int j=0; for (PreprocessingDAGNode n:node.prev) 
                for (int l=0; l<n.matrixTransformResult[0].length; l++) {
                    if (training) for (int i=0; i<nData.length; i++) nData[i][j] = n.matrixTransformResult[i][l];
                    else nRow[j] = n.transformResult[l];
                    j++;
                }
            if (training) node.preprocessor.setDataAndTarget(nData, target);
            else node.transformResult = node.preprocessor.transform(nRow);
        }
        if (training) {
            node.preprocessor.train(start, end);
            node.matrixTransformResult = node.preprocessor.transform(node.preprocessor.data);
        } 
    }
    
    private void depthSearch(int start, int end, double[] input) {
        for (int i=0; i<node.size(); i++) node.elementAt(i).finished = false;
        Stack<PreprocessingDAGNode> stack = new Stack<PreprocessingDAGNode>();
        for (int i=0; i<node.size(); i++) {
            if (!node.elementAt(i).finished) stack.push(node.elementAt(i));
            while (stack.size() > 0) {
                PreprocessingDAGNode k = stack.peek();
                boolean ready = true; for (PreprocessingDAGNode n:k.prev) if (!n.finished) {ready = false; stack.push(n);}
                if (ready) {
                    combineNode(k, start, end, input);
                    k.finished = true;
                    stack.pop();
                }
            }
        }
    }
    
    @Override
    public void train(int start, int end) {
        training = true;
        depthSearch(start, end, null);
    }

    private double[] collectLeafResults() {
        int k = 0; for (PreprocessingDAGNode n:node) if (n.next.size()==0) k += n.transformResult.length;
        double[] erg = new double[k]; 
        int l = 0; for (PreprocessingDAGNode n:node) if (n.next.size()==0) for (int i=0; i<n.transformResult.length; i++) erg[l++] = n.transformResult[i]; 
        return erg;
    }
    
    @Override
    public double[] transform(double[] in) {
        training = false;
        depthSearch(-1, -1, in);
        return collectLeafResults();
    }

}
