/*
 * Created on 13.03.2014
 */
package engine;

import engine.backend.Model;
import gui.graph.Edge;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;

import java.util.*;

/**
 * Container for a dataset combined from multiple datasets, potentially overlapping, p
 * potentially raw and Covariance mixed, potentially with groups. 
 * 
 * @author timo
 */
public class CombinedDataset {

    public final static double EPS = 0.001;
    
    public List<Node> nodes;
    public List<Edge> edges;
    public double[][] rawData;
    public double[][] definitionData; 
    public double[] mean;
    public double[][] cov;
    public boolean[] isRaw;             // true if the corresponding column is linked to a rawDataset
    public boolean[] isZTransform;      // true if the corresponding column is marked as normalized

    public int anzVar, anzObs, anzRawObs, anzPer, anzDef, anzDatasets;
    
    public boolean allObservedConnected, allDefinitionConnected, allGroupingConnected, 
        noMissingInDefinitions, noMissingInGroupings, allDatasetsHaveId, hasGroups, hasMissingness,
        allRawHaveMeanZero, hasCovarianceDatasets, hasNonPositiveDefiniteCovarianceDataset;

    // centralizationMeans, if not null, contains values that were subtracted from the raw data at a potential centralization process.
    private double[] centralizationMeans;
    
    public CombinedDataset(LinkHandler link, List<Node> inNodes) {this(link, inNodes, null, true, false);}
    public CombinedDataset(LinkHandler link, List<Node> inNodes, List<Edge> edges, boolean centralizeMeans) {this(link, inNodes, edges, true, centralizeMeans);}
    /**
     * Computes raw data, mean and covariance matrix of a set of nodes. Means and covariances of latents will be zero. If different datasets are used, they
     * are assumed to be independent unless they are raw and have id columns; in this case, they are matched by the id columns. Covariances of 
     * raw columns are computed by pairwise listwise deletion. If no participant has both variables, value will be NA. 
     */
    public CombinedDataset(LinkHandler link, List<Node> inNodes, List<Edge> edges, boolean observedOnly, boolean centralizeMeans) {
        if (observedOnly) {
            this.nodes = new ArrayList<Node>();
            for (Node node:inNodes) if (node.isObserved()) this.nodes.add(node);
        } else this.nodes = inNodes;
        this.edges = edges;
        
        allObservedConnected = true;
        allGroupingConnected = true;
        allDefinitionConnected = true;
        noMissingInDefinitions = true;
        noMissingInGroupings = true;
        hasGroups = false;
        allDatasetsHaveId = true;
        allRawHaveMeanZero = true;
        hasCovarianceDatasets = false;
        hasMissingness = false;
        hasNonPositiveDefiniteCovarianceDataset = false;
        
        anzVar = nodes.size();
        anzObs = 0;
        anzRawObs = 0;
        anzPer = 0;
        for (Node n:nodes) {
        	DatasetField df = link.getDatasetField(n.getObservedVariableContainer());
            if (n.isObserved() && n.isConnected() &&  (df != null && df.dataset instanceof RawDataset)) anzRawObs++;
        }
        isRaw = new boolean[anzVar];
        isZTransform = new boolean[anzVar];
        mean = new double[anzVar];
        cov = new double[anzVar][anzVar];
        Hashtable<Double, double[]> rawDataTable = new Hashtable<Double, double[]>();
        Hashtable<RawDataset, double[]> idTable = new Hashtable<RawDataset, double[]>();
        Hashtable<String, Integer> uniqueNameTable = new Hashtable<String, Integer>();
        HashSet<CovarianceDataset> covarianceDatasets = new HashSet<CovarianceDataset>();
        
        centralizationMeans = new double[anzRawObs];
        
        // just a number to start unique IDs with. Will not be used outside this method. 
        double uniqueID = -730000;
        
        // Loops through all nodes. latent, non-connected and covarianceDataset - nodes will be completed here, rawDataset-Fields will be pushed in 
        // the rawData table with unique ids as identifier unless the variable has a grouping indicator and the groups don't fit.
        // IDs are taken from the dataset if available or created arbitrarily. Since all separate datasets are supposed to be independent, 
        // the total number of participants is added up unless participants are identified via the id columns. 
        for (int i=0; i<nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n.isObserved() && !n.isConnected()) allObservedConnected = false;
            if (n.isObserved()) {
                anzObs++;
                n.getObservedVariableContainer().setUniqueName(getUniqueName(n.getCaption(),uniqueNameTable));
                if (n.isGrouping() && n.getGroupingVariableContainer().isConnected()) 
                    n.getGroupingVariableContainer().setUniqueName(getUniqueName(n.groupName,uniqueNameTable));
            }
            if (n.isLatent() || !n.isConnected()) {mean[i] = Double.NaN; for (int j=0; j<anzVar; j++) cov[i][j] = cov[j][i] = Double.NaN;}
            else {
                isZTransform[i] = n.isNormalized();
                DatasetField field = link.getDatasetField(n.getObservedVariableContainer());
                isRaw[i] = field.dataset instanceof RawDataset;
                if (isRaw[i]) {
                    RawDataset dataset = (RawDataset)field.dataset;
                    double[] ids = idTable.get(dataset);
                    if (ids == null) {
                        ids = new double[dataset.getNumRows()];
                        if (dataset.hasIdColumn()) {
                            for (int j=0; j<dataset.getNumRows(); j++) ids[j] = dataset.getData()[j][dataset.getIdColumn()];
                        } else {
                            for (int j=0; j<dataset.getNumRows(); j++) ids[j] = uniqueID++;
                            allDatasetsHaveId = false;
                        }
                        idTable.put(dataset, ids);
                    }
                    for (int j=0; j<dataset.getNumRows(); j++) {
                        double[] datarow = rawDataTable.get(ids[j]);
                        if (datarow == null) {datarow = new double[anzVar]; Statik.setTo(datarow, Model.MISSING); rawDataTable.put(ids[j], datarow);}
                        datarow[i] = dataset.get(j, field.columnId);
                        if (n.groupingVariable) {
                            hasGroups = true;
                            if (!n.isGroupingVariableConnected()) {allGroupingConnected = false; datarow[i] = Model.MISSING;}
                            else try{
                                DatasetField groupField = link.getDatasetField(n.getGroupingVariableContainer());
                                RawDataset groupDataset = (RawDataset)groupField.dataset;
                                double[] groupIds = idTable.get(groupDataset);
                                if (groupIds == null) {
                                    groupIds = new double[groupDataset.getNumRows()];
                                    if (groupDataset.hasIdColumn()) {
                                        for (int k=0; k<groupDataset.getNumRows(); k++) groupIds[k] = groupDataset.getData()[k][groupDataset.getIdColumn()];
                                    } else {
                                        for (int k=0; k<groupDataset.getNumRows(); k++) groupIds[k] = uniqueID++;
                                        allDatasetsHaveId = false; 
                                    }
                                    idTable.put(groupDataset, groupIds);
                                }
                                double group = Double.NaN;
                                for (int k=0; k<groupIds.length; k++) if (groupIds[k] == ids[j]) group = groupDataset.get(k, groupField.columnId);
                                if (Model.isMissing(group)) noMissingInGroupings = false;
                                if (group != n.groupValue) datarow[i] = Model.MISSING;
                            } catch (Exception e) {
                                datarow[i] = Model.MISSING;
                            }
                        }
                    }
                } else {
                    hasCovarianceDatasets = true;
                    CovarianceDataset dataset = (CovarianceDataset)field.dataset;
                    if (!dataset.isPositiveDefinite()) hasNonPositiveDefiniteCovarianceDataset = true;
                    if (!covarianceDatasets.contains(dataset)) {
                        anzPer += dataset.getSampleSize(); 
                        covarianceDatasets.add(dataset);
                    }
                    mean[i] = dataset.mean[field.columnId];
                    for (int j=0; j<anzVar; j++) {
                        DatasetField field2 = link.getDatasetField(nodes.get(j).getObservedVariableContainer());
                        if (field2 != null && field2.dataset == dataset)
                            cov[i][j] = dataset.cov[field.columnId][field2.columnId];
                        else cov[i][j] = 0.0;
                    }
                }
            }
        }
        anzDatasets = idTable.size();
        int anzRawPer = rawDataTable.size();
        anzPer += anzRawPer;

        // collecting all definition variables
        Hashtable<Double, double[]> definitionDataTable = null;  
        if (edges != null && edges.size() > 0) {
            anzDef = edges.size();
            definitionDataTable = new Hashtable<Double, double[]>(anzRawPer); 
            for (Double key:rawDataTable.keySet()) {
                double[] definitionRow = new double[anzDef]; Statik.setTo(definitionRow, Model.MISSING);
                definitionDataTable.put(key, definitionRow);
            }
            for (int i=0; i<edges.size(); i++) {
                Edge edge = edges.get(i);
                if (edge.isDefinitionVariable()) {
                	
                	if (edge.definitionVariableName != null)
                		edge.getDefinitionVariableContainer().setUniqueName(getUniqueName(edge.definitionVariableName, uniqueNameTable));
                    
                    if (!edge.getDefinitionVariableContainer().isConnected()) allDefinitionConnected = false; else {
                        DatasetField field = link.getDatasetField(edge.getDefinitionVariableContainer());
                        if (field != null && field.dataset != null && field.dataset instanceof RawDataset) {
                            RawDataset dataset = (RawDataset)field.dataset;
                            double[] ids = idTable.get(dataset);
                            if (ids != null) {
                                for (int j=0; j<ids.length; j++) {
                                    double[] definitionRow = definitionDataTable.get(ids[j]);
                                    if (definitionRow != null) definitionRow[i] = dataset.get(j, field.columnId);
                                }
                            }
                        }
                    }
                }
            }
        }

        // writing rawData and DefinitionData
        rawData = new double[anzRawPer][];
        definitionData = (anzDef > 0?new double[anzRawPer][anzDef]:null);
        int per = 0;
        Double[] keys = new Double[anzRawPer]; rawDataTable.keySet().toArray(keys);
        Arrays.sort(keys);
        for (Double key:keys) {
            rawData[per] = rawDataTable.get(key);
            if (!hasMissingness) for (int i=0; i<rawData[per].length; i++) if (Model.isMissing(rawData[per][i])) hasMissingness = true;
            if (definitionDataTable != null) {
                definitionData[per] = definitionDataTable.get(key);
                for (int j=0; j<anzDef; j++) if (Model.isMissing(definitionData[per][j])) noMissingInDefinitions = false;
            }
            per++;
        }
        
        
        // computing means and covariances for the raw data
        for (int i=0; i<anzVar; i++) if (isRaw[i]) {
            double sum = 0; int anz = 0; 
            for (int p=0; p<anzRawPer; p++) if (!Model.isMissing(rawData[p][i])) {anz++; sum += rawData[p][i];} 
            mean[i] = (anz==0?Double.NaN:sum / (double)anz);
            if (!Double.isNaN(mean[i]) && Math.abs(mean[i]) > EPS) allRawHaveMeanZero = false;
        }
        for (int i=0; i<anzVar; i++) if (isRaw[i]) {
            for (int j=i; j<anzVar; j++) if (isRaw[j]) {
                double sum = 0; int anz = 0; 
                for (int p=0; p<anzRawPer; p++) if (!Model.isMissing(rawData[p][i]) && !Model.isMissing(rawData[p][j])) {anz++; sum += rawData[p][i]*rawData[p][j];} 
                cov[i][j] = cov[j][i] = (anz<=0?Double.NaN:(sum - mean[i]*mean[j]*anz) / (double)(anz));        // using population covariance matrix, appropriate for centralized means.  
            }
        }
        if (centralizeMeans) {
            for (int i=0; i<anzRawPer; i++) 
                for (int j=0; j<anzVar; j++) if (isRaw[j]) {
                    if (!Double.isNaN(mean[j]) && !Model.isMissing(rawData[i][j])) rawData[i][j] -= mean[j];
                }
            int k=0; for (int j=0; j<anzVar; j++) if(isRaw[j]) centralizationMeans[k++] = mean[j]; 
            Statik.setTo(mean, 0.0);
        }
        // do z-transform
        for (int i=0; i<anzVar; i++) if (isZTransform[i] && !Double.isNaN(mean[i]) && !Double.isNaN(cov[i][i])) {
            double stdv = Math.sqrt(cov[i][i]);
            if (isRaw[i]) {
                for (int j=0; j<anzRawPer; j++) 
                    if (!Model.isMissing(rawData[j][i])) rawData[j][i] = (rawData[j][i]-mean[i])/stdv;
            }
            for (int j=0; j<anzVar; j++) cov[i][j] = cov[j][i] = cov[i][j] / stdv; 
            mean[i] = 0; cov[i][i] = 1.0; 
        }
    }
    
    private static String getUniqueName(String name, Hashtable<String,Integer> uniqueNameTable) {
        if (!uniqueNameTable.containsKey(name)) {
            uniqueNameTable.put(name, 1); 
            return name;
        }
        int nr = uniqueNameTable.get(name)+1; 
        uniqueNameTable.put(name, nr);
        return name += "_"+nr;
    }
    
    public int getNumCompletelyMissingCases() {
    	int count = 0;
    	for (int i=0; i < rawData.length; i++) {
    		boolean allmiss = true;
    		for (int j=0; j< rawData[i].length; j++) {
    			if (!Model.isMissing(rawData[i][j])) {
    				allmiss=false; j = rawData[j].length;
    				}
    			
    		}
    		if (allmiss) count=count+1;
    	}
    	return(count);
    }
    
    public boolean isAllColumnsNonNA() {
        for (int i=0; i< rawData[0].length; i++) {
        	boolean ok = false;
        	for (int j=0; j < rawData.length; j++) {
        		if (!Model.isMissing(rawData[j][i])) {ok=true; break;}
        	}
        	if (!ok) return false;
        }
        return true;
    }
    
    public boolean isAllConnected() {return allObservedConnected && allDefinitionConnected && allGroupingConnected;}
    public boolean isRaw() {return anzRawObs == anzVar;}
    public boolean hasCovarianceDataset() {return hasCovarianceDatasets;}
    public boolean hasDefinitionVariables() {return anzDef > 0;}
    public boolean hasGroups() {return hasGroups;}
    public boolean hasLatents() {return anzObs != anzVar;}
    public boolean hasMultipleDatasetsPartlyWithoutId() {return anzDatasets > 1 && !allDatasetsHaveId;}
    public boolean isValidForMomentFit() {
        if (hasDefinitionVariables() || hasGroups() || hasLatents() || !allObservedConnected || anzPer==0) return false;
        for (int i=0; i<anzVar; i++) if (Double.isNaN(mean[i])) return false;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) if (Double.isNaN(cov[i][j])) return false;
        return true;
    }
    public boolean isValidForRawFit() {
        if (hasLatents() || !noMissingInDefinitions || !isAllConnected() || anzPer == 0
        		|| (hasGroups && !allGroupingConnected)
        		|| (hasDefinitionVariables() && !allDefinitionConnected)
        		|| !allObservedConnected 
                || hasCovarianceDataset()
           ) return false;
        return true;
    }

    
    public int getIx(Node node) {
        int ix = -1; for (int i=0; i<nodes.size(); i++) if (node == nodes.get(i)) ix = i;
        return ix;
    }
    public int getRawIx(Node node) {
        int ix = -1; int k=0; for (int i=0; i<nodes.size(); i++) {if (node == nodes.get(i)) ix = k; if (isRaw[i]) k++; }
        return ix;
    }

    /**
     * Returns an HTML-string that describes the data distribution of the selected nodes in the format
     * 
     *  Variable    Data Mean  Data Covariance
     *  Name 1      54          20  1   3
     *  Name 2      12          1   15  7
     *  Name 3      22          3   7   20
     *  
     *  The order of variables is by the order in the selected list. 
     *  
     * @param selected  Nodes that will be shown
     * @return          HTML-string to represent mean and covariance matrix.
     */
    public String getDataDistributionString(List<Node> selected) {
        String erg = "<table>\r\n<tr><td>Variable</td><td>Data Mean</td><td>Data Covariance</td></tr>";
        for (Node node:selected) {
            int ix = getIx(node); 
            if (ix != -1) {
                erg += "<tr><td>"+node.getCaption()+"</td>";
                erg += "<td>"+Statik.doubleNStellen(mean[ix] + (isRaw[ix] && centralizationMeans!=null?centralizationMeans[getRawIx(node)]:0) ,2)+"</td>";
                erg += "<td>"; for (Node node2:selected) {
                    int ix2 = getIx(node2);
                    if (ix2 != -1) erg += Statik.doubleNStellen(cov[ix][ix2],2)+"&nbsp;&nbsp;&nbsp;"; 
                }
                erg += "</td></tr>\r\n";
            }
        }
        erg += "</table>\r\n";
        return erg;
    }
    
    public List<String> getVariableNames(HashMap<VariableContainer, String> nameMapping) {
        List<String> varnames = new ArrayList<String>();
        for (Node node:nodes) {
            if (nameMapping != null && nameMapping.containsKey(node.getObservedVariableContainer())) varnames.add(nameMapping.get(node.getObservedVariableContainer()));
            else varnames.add(node.getUniqueName(true));
        }
        for (Edge edge:edges) if (edge.isDefinitionVariable()) {
            if (nameMapping != null && nameMapping.containsKey(edge.getDefinitionVariableContainer())) varnames.add(nameMapping.get(edge.getDefinitionVariableContainer()));
            else varnames.add(edge.getDefinitionVariableContainer().getUniqueName());
        }
        return varnames;
    }
    
    public Dataset createDataset() {return createDataset(false, null);}
    public Dataset createDataset(boolean centerMeans, HashMap<VariableContainer, String> nameMapping) {
        if (hasCovarianceDatasets) {
            return new CovarianceDataset(anzPer, mean, cov, getVariableNames(nameMapping));
        } else {
            double[][] rawAndDefinition = new double[anzPer][anzVar+anzDef];
            for (int i=0; i<anzPer; i++) {
                for (int j=0; j<anzVar; j++) rawAndDefinition[i][j] = (!centerMeans || Model.isMissing(rawData[i][j])?rawData[i][j]:rawData[i][j] - mean[j]);
                for (int j=0; j<anzDef; j++) rawAndDefinition[i][j+anzVar] = definitionData[i][j];
            }
            
            if (!centerMeans) return new RawDataset(rawAndDefinition, getVariableNames(nameMapping));
            else return new RawDataset(rawAndDefinition, getVariableNames(nameMapping), Statik.add(mean,centralizationMeans) );
        }
    }
    
    public double[] getCentralizationMeans() {return centralizationMeans;}
}
