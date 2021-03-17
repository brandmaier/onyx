package arithmetik;

import java.util.*;

public class Automaton
{
	int anzStates;
	String[] letter;
	int start;
	int[] end;
	boolean[][][] edge;				// True, wenn das Alphabetszeichen im letzten Index auf der 
									// Kante vom ersten zum zweiten vorhanden ist.
	
	public Automaton(int anzStates, String[] letter, int start, int[] end)
	{
		this.anzStates = anzStates;
		this.letter = letter;
		this.start = start;
		this.end = end;
		this.edge = new boolean[anzStates][anzStates][letter.length];
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					edge[i][j][k] = false;
	}
	public Automaton(int  anzStates, int anzLetter, int start, int[] end)
	{
		this(anzStates, new String[anzLetter], start, end);
		for (int i=0; i<letter.length; i++)
			letter[i] = (new Character((char)(97+i))).toString();
	}
	public Automaton(Automaton copy)
	{
		this.anzStates = copy.anzStates;
		this.letter = new String[copy.letter.length];
		for (int i=0; i<letter.length; i++) letter[i] = ""+copy.letter[i];
		this.end = new int[copy.end.length];
		for (int i=0; i<end.length; i++) end[i] = copy.end[i];
		this.start = copy.start;
		this.edge = new boolean[anzStates][anzStates][letter.length];
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					edge[i][j][k] = copy.edge[i][j][k];
	}
/**
 * Insert the method's description here.
 * Creation date: (24.02.2001 19:28:42)
 * @return Automaton
 */
public Automaton complement() {
	Automaton a = this.efficientMakeDeterministic();
	a = a.makeComplete();

	boolean[] endStates = new boolean[a.anzStates];
	for(int i=0;i<a.end.length;i++)
		endStates[a.end[i]]=true;

	int[] newEnd = new int[a.anzStates-a.end.length];
	int k=0;
	for(int i=0;i<a.anzStates;i++)
	{
		if(!endStates[i])
		{
			newEnd[k]=i;
			k++;
		}
	}
	a.end=newEnd;
	return a;
}
	public Automaton concat(Automaton snd)
	{
		Vector newlet = new Vector();
		for (int i=0; i<letter.length; i++)
			newlet.addElement(letter[i]);
		int[] verweis = new int[snd.letter.length];
		for (int i=0; i<snd.letter.length; i++)
		{
			int in = -1;
			for (int j=0; (in==-1) && (j<letter.length); j++)
				if (snd.letter[i].equals(letter[j])) in = j;
			if (in!=-1) verweis[i] = in;
			else {verweis[i] = newlet.size(); newlet.addElement(snd.letter[i]);}
		}
		String[] newletarr = new String[newlet.size()];
		for (int i=0; i<newletarr.length; i++)
			newletarr[i] = ((String)newlet.elementAt(i));
		Automaton erg = new Automaton(anzStates+snd.anzStates, newletarr, start, snd.end);
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					erg.edge[i][j][k] = edge[i][j][k];
		for (int i=0; i<end.length; i++)
		{
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					erg.edge[j][snd.start+anzStates][k] = edge[j][end[i]][k];
			for (int j=0; j<snd.anzStates; j++)
				for (int k=0; k<snd.letter.length; k++)
					erg.edge[end[i]][j+anzStates][verweis[k]] = snd.edge[snd.start][j][k];
		}
		for (int i=0; i<snd.anzStates; i++)
			for (int j=0; j<snd.anzStates; j++)
				for (int k=0; k<snd.letter.length; k++)
					erg.edge[i+anzStates][j+anzStates][verweis[k]] = snd.edge[i][j][k];
		int[] newend = new int[snd.end.length];

		if ((this.containsEpsilon()) && (snd.containsEpsilon())) 
		{
			newend = new int[snd.end.length+1];
			newend[newend.length-1] = start;
		}
		for (int i=0; i<snd.end.length; i++)
			newend[i] = snd.end[i]+anzStates;
		erg.end = newend;
		
		return erg;
	}
	public boolean containsEpsilon()
	{
		for (int i=0; i<end.length; i++)
			if (end[i]==start) return true;
		return false;
	}
/**
 * Insert the method's description here.
 * Creation date: (23.02.01 13:37:35)
 * @return int[][]
 */
public int[][] createAdjacencyList() {
	int[][] adjacency = new int[anzStates][letter.length];
	
	for(int i=0;i<anzStates;i++)
		for(int j=0;j<anzStates;j++)
			for(int k=0;k<letter.length;k++)
				if(edge[i][j][k])
					adjacency[i][k]=j;

	return adjacency;
}
/**
 * Insert the method's description here.
 * Creation date: (23.02.01 13:37:35)
 * @return int[][]
 */
public int[][][] createInverseAdjacencyList() {
	int[][][] adjacency = new int[anzStates][letter.length][];
	int[][] numbers = new int[anzStates][letter.length];
	
	for(int i=0;i<anzStates;i++)
		for(int j=0;j<anzStates;j++)
			for(int k=0;k<letter.length;k++)
				if(edge[j][i][k])
				{
					numbers[i][k]++;
				}

	for(int i=0;i<anzStates;i++)
		for(int k=0;k<letter.length;k++)
			adjacency[i][k] = new int[numbers[i][k]];

	for(int i=0;i<anzStates;i++)
		for(int k=0;k<letter.length;k++)
		{
			int cnt = 0;
			for(int j=0;j<anzStates;j++)
			{
				if(edge[j][i][k])
				{
					adjacency[i][k][cnt]=j;
					cnt++;
				}
			}
		}
		
	return adjacency;
}
	public Automaton deleteDeathEnds()
	{
		boolean[] toBeDeleted = new boolean[anzStates];
		boolean[] isFinal = new boolean[anzStates];
		for (int i=0; i<anzStates; i++) {toBeDeleted[i] = false; isFinal[i] = false;}
		for (int i=0; i<end.length; i++) isFinal[end[i]] = true;
		boolean change = true;
		while (change)
		{
			change = false;
			for (int i=0; i<anzStates; i++)
			{
				if ((!toBeDeleted[i]) && (!isFinal[i]))
				{
					toBeDeleted[i] = true;
					for (int j=0; j<anzStates; j++)
						if (!toBeDeleted[j])
							for (int k=0; k<letter.length; k++)
								if (edge[i][j][k]) toBeDeleted[i] = false;
					if (toBeDeleted[i]) change = true;
				}
			}
		}
		
		int neuLen = 0;
		int[] verweisNeuAlt = new int[anzStates];
		int[] verweisAltNeu = new int[anzStates];
		for (int i=0; i<anzStates; i++)
			if (!toBeDeleted[i]) {verweisNeuAlt[neuLen] = i; verweisAltNeu[i] = neuLen++;}
		
		Automaton erg = new Automaton(neuLen, letter, start, end);
		for (int i=0; i<erg.anzStates; i++)
			for (int j=0; j<erg.anzStates; j++)
				for (int k=0; k<letter.length; k++)
					erg.edge[i][j][k] = edge[verweisNeuAlt[i]][verweisNeuAlt[j]][k];
		
		for (int i=0; i<erg.end.length; i++)
			erg.end[i] = verweisAltNeu[end[i]];
		
		return erg;
	}
	// make the automaton deterministic
	// while doing this, make a reachability test ON THE FLY.
	
	public Automaton efficientMakeDeterministic()
	{
		if (isDeterministic()) return this.makeComplete();
	
		Vector reachableMetaStates = new Vector();
		
		Vector queue = new Vector();

		Vector edges = new Vector();
		
		int[] metaState = new int[1];
		metaState[0]=start;
		
		queue.addElement(metaState);
		reachableMetaStates.addElement(metaState);
		
		while(queue.size()!=0)
		{
			metaState = (int[]) queue.elementAt(queue.size()-1);
			queue.removeElementAt(queue.size()-1);
			for(int l=0;l<letter.length;l++)
			{
				boolean[] newMetaField = new boolean[anzStates];
				for(int i=0;i<metaState.length;i++)
				{
					for(int k=0;k<anzStates;k++)
						if(edge[metaState[i]][k][l])
						{
							newMetaField[k]=true;
						}
					
				}
				int newstatesize=0;
				for(int z=0;z<anzStates;z++)
					if(newMetaField[z])
						newstatesize++;
				
				int[] newMeta = new int[newstatesize];
				int k=0;
				for(int i=0;i<anzStates;i++)
				{
					if(newMetaField[i])
					{
						newMeta[k]=i;
						k++;
					}
				}
			
				if(newMeta.length!=0)
				{
				int schondrin = getMeta(newMeta,reachableMetaStates);
				if(schondrin==-1)
				{
					queue.addElement(newMeta);
					reachableMetaStates.addElement(newMeta);
					int[] ij = new int[3];
					ij[0] = getMeta(metaState,reachableMetaStates);
					ij[1] = reachableMetaStates.size()-1;
					ij[2] = l;
					edges.addElement(ij);

				}
				else
				{
					int[] ij = new int[3];
					ij[0] = getMeta(metaState,reachableMetaStates);
					ij[1] = schondrin;
					ij[2] = l;
					edges.addElement(ij);
				}
				}
			}
		}
		
		int finals=0;
		boolean[] endState = new boolean[anzStates];
	
		//array of booleans endState[i]==1 <==> i is finalState
		for(int i=0;i<end.length;i++)
		endState[end[i]]=true;
		
		for(int i=0;i<reachableMetaStates.size();i++)
		{
			metaState = (int[]) reachableMetaStates.elementAt(i);
			boolean fin=false;
			for(int j=0;j<metaState.length;j++)
				if(endState[metaState[j]])
					fin=true;
			if(fin) finals++;			
		}
		int[] metaEnd = new int[finals];
		int k=0;
		for(int i=0;i<reachableMetaStates.size();i++)
		{
			metaState = (int[]) reachableMetaStates.elementAt(i);
			boolean fin=false;
			for(int j=0;j<metaState.length;j++)
				if(endState[metaState[j]])
				{
					metaEnd[k]=i;
					k++;
					break;
				}
		}

		boolean[][][] newEdges = new boolean[reachableMetaStates.size()][reachableMetaStates.size()][letter.length];
		
		Automaton erg = new Automaton(reachableMetaStates.size(),letter,0,metaEnd);

		for(int i=0;i<edges.size();i++)
		{
			int[] ij = (int[])edges.elementAt(i);
			newEdges[ij[0]][ij[1]][ij[2]]=true;
		}
		
		erg.edge = newEdges;
		return erg;
	}
 int getMeta(int[] metaState, Vector metaList)
 {
	 for(int i=0;i<metaList.size();i++)
	 {
		 int[] metaCompare = (int[])metaList.elementAt(i);
		 boolean equal = true;
		 if(metaCompare.length!=metaState.length) equal = false;
		 else
		 {
			for(int k=0;k<metaState.length;k++)
			{
				boolean drin = false;
				for(int j=0;j<metaCompare.length;j++)
					if(metaCompare[j]==metaState[k])
						drin = true;						
					
				if(!drin)
					equal = false;
			}
		 }
		 if(equal)
			 return i;
	 }
	 return -1;
 }  
	public int[] getStartVector()
	{
		int[] erg = new int[anzStates];
		for (int i=0; i<anzStates; i++)
			erg[i] = 0;
		erg[start] = 1;
		return erg;
	}
	public int[] getTerminateVector()
	{
		int[] erg = new int[anzStates];
		for (int i=0; i<anzStates; i++)
			erg[i] = 0;
		for (int i=0; i<end.length; i++)
			erg[end[i]] = 1;
		return erg;
	}
	public Automaton intersect(Automaton snd)
	{
		int[] letterRef = new int[letter.length];
		for(int i=0;i<letter.length;i++)
		{
			for(int k=0;k<snd.letter.length;k++)
			{
				letterRef[i]=-1;
				if(snd.letter[k].equals(letter[i]))
				{
					letterRef[i]=k;
					break;
				}
			}
		}

		int newAnz = anzStates*snd.anzStates;
		
		int[] newEnd = new int[end.length*snd.end.length];
		int k=0;
		for(int i=0;i<end.length;i++)
			for(int j=0;j<snd.end.length;j++)
			{
				newEnd[k]=snd.anzStates*end[i]+snd.end[j];
				k++;				
			}

		boolean[][][] newEdge = new boolean[newAnz][newAnz][letter.length];
		for(int i=0;i<newAnz;i++)
			for(int j=0;j<newAnz;j++)
				for(k=0;k<letter.length;k++)
				if(letterRef[k]!=-1)
				{
					int i1=i%snd.anzStates,j1=j%snd.anzStates;
//					int i2=(i-(i%snd.anzStates))/snd.anzStates,j2=(j-(j%snd.anzStates))/snd.anzStates;
					int i2=i/snd.anzStates,j2=j/snd.anzStates;
					newEdge[i][j][k] = (snd.edge[i1][j1][letterRef[k]]) & (edge[i2][j2][k]);
				}

		Automaton a = new Automaton(newAnz,letter,snd.anzStates*start+snd.start,newEnd);
		a.edge = newEdge;
		return a;
	}
	public boolean isComplete()
	{
		for (int i=0; i<anzStates; i++)
			for (int k=0; k<letter.length; k++)
			{
				boolean hasEdge = false;
				for (int j=0; j<anzStates; j++)
					hasEdge |= edge[i][j][k];
				if (!hasEdge) return false;
			}
		return true;					
	}
	public boolean isDeterministic()
	{
		for (int i=0; i<anzStates; i++)
			for (int k=0; k<letter.length; k++)
			{
				int anzEdge = 0;
				for (int j=0; j<anzStates; j++)
					if (edge[i][j][k]) anzEdge++;
				if (anzEdge > 1) return false;
			}
		return true;					
	}
	public Automaton makeComplete()
	{
		if (isComplete()) return new Automaton(this);
		Automaton erg = new Automaton(anzStates+1, letter, start, end);
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					erg.edge[i][j][k] = edge[i][j][k];
		for (int i=0; i<anzStates; i++)
			for (int k=0; k<letter.length; k++)
			{
				boolean hasEdge = false;
				for (int j=0; j<anzStates; j++)
					hasEdge |= edge[i][j][k];
				if (!hasEdge) erg.edge[i][erg.anzStates-1][k] = true;
			}		
		for (int k=0; k<letter.length; k++)
			erg.edge[erg.anzStates-1][erg.anzStates-1][k] = true;
		
		return erg;
	}
	public Automaton makeDeterministic()
	{
		if (isDeterministic()) return this.makeComplete();
		Automaton erg = new Automaton((int)Math.pow(2,anzStates), letter, (int)Math.pow(2,start), new int[0]);
		Vector nend = new Vector();
		for (int i=0; i<erg.anzStates; i++)
		{
			boolean isTerminate = false;								// Endzustand ?
			for (int j=0; j<end.length; j++)
				if ((i & (int)Math.pow(2,end[j]))!=0) isTerminate = true;
			if (isTerminate) nend.addElement(new Integer(i));
			
			for (int k=0; k<letter.length; k++)
			{
				int val = 0;
				for (int j=0; j<anzStates; j++)
				{
					boolean isTarget = false;
					for (int l=0; (!isTarget) && (l<anzStates); l++)
						if (((i & (int)Math.pow(2,l)) != 0) && (edge[l][j][k])) isTarget = true;
					if (isTarget) val += Math.pow(2, j);
				}
				erg.edge[i][val][k] = true;
			}
		}
		int[] nendarr = new int[nend.size()];
		for (int i=0; i<nendarr.length; i++)
			nendarr[i] = ((Integer)nend.elementAt(i)).intValue();
		erg.end = nendarr;
		
		return erg;
	}
public Automaton makeMinimal() {	

	int[][][] adjacency = createInverseAdjacencyList();
	boolean[] endState = new boolean[anzStates];
	
	boolean[][] different = new boolean[anzStates][anzStates]; //distinguishable states will be marked
	Vector marked = new Vector(); //Queue for marked pairs, could be improved not using Vector 

	//array of booleans endState[i]==1 <==> i is finalState
	for(int i=0;i<end.length;i++)
		endState[end[i]]=true;
		
	//first, final states are distinguished from non-final states
	for(int i=0;i<end.length;i++)
		for(int j=0;j<anzStates;j++)
		{
			if(!endState[j])
			{
				if(end[i]<j)
				{
					int[] ij = new int[2];
					ij[0]=end[i];
					ij[1]=j;
					marked.addElement(ij);
					different[end[i]][j]=true;
				}
				else
				{
					int[] ij = new int[2];
					ij[1]=end[i];
					ij[0]=j;
					marked.addElement(ij);
					different[j][end[i]]=true;

				}
			}
		}

	// now do a bfs in the product automaton

	while(marked.size()!=0)
	{
		int[] ij = (int[]) marked.elementAt(marked.size()-1);
		marked.removeElementAt(marked.size()-1);
//		System.out.println("Examining ("+ij[0]+","+ij[1]+")");
		for(int k=0;k<letter.length;k++)
		{
			int[] rs = new int[2];  // (r,s) will be marked
			for(int l=0;l<adjacency[ij[0]][k].length;l++)
			for(int m=0;m<adjacency[ij[1]][k].length;m++)
			{
				rs[0]=adjacency[ij[0]][k][l];
				rs[1]=adjacency[ij[1]][k][m];
				if(rs[0]>rs[1])
				{
					int dummy = rs[0];
					rs[0] = rs[1];
					rs[1] = dummy;
				}
				if(!different[rs[0]][rs[1]])
				{
//					System.out.println("From ("+ij[0]+","+ij[1]+") to ("+rs[0]+","+rs[1]+") with letter "+letter[k]);
					different[rs[0]][rs[1]]=true;
					marked.addElement((int[])rs.clone());					
				}

			}
		}
	}

	// now combine equivalent states

	int stateCounter=0;
	int[] belongsTo = new int[anzStates];
	for(int i=0;i<anzStates;i++)
		belongsTo[i]=i;
		
	for(int i=0;i<anzStates;i++)
	{
		if(belongsTo[i]==i)
		{
			belongsTo[i]=stateCounter;
			for(int j=i+1;j<anzStates;j++)
			{
				if(!different[i][j])
				{
					belongsTo[j]=stateCounter;
				}
			}
			stateCounter++;
		}
	}

	int minStart = belongsTo[start];
	int minStates = stateCounter;
	boolean[][][] minEdge = new boolean[minStates][minStates][letter.length];
	boolean[] minEndStates = new boolean[minStates];

	int[][] adj = createAdjacencyList();
	
	for(int i=0;i<anzStates;i++)
	{
		for(int k=0;k<letter.length;k++)
		{
			minEdge[belongsTo[i]][belongsTo[adj[i][k]]][k]=true;
		}
	}

	for(int i=0;i<end.length;i++)
	{
		minEndStates[belongsTo[end[i]]]=true;
	}

	int minEnds=0;
	for(int i=0;i<minStates;i++)
		if(minEndStates[i]) minEnds++;

	int[] minEnd = new int[minEnds];

	int k=0;
	for(int i=0;i<minStates;i++)
		if(minEndStates[i])
		{
			minEnd[k]=i;
			k++;
		}

	Automaton minimal = new Automaton(minStates,letter,minStart,minEnd);
	minimal.edge = minEdge;
	
	return minimal;
}
	public static Automaton parseString(String s)
	{
		if (s.length() == 0) return new Automaton(1, new String[0], 0, new int[]{0});
		if (s.length() == 1) 
		{
			Automaton erg = new Automaton(2, new String[]{""+s.charAt(0)}, 0, new int[]{1});
			erg.edge[0][1][0] = true;
			return erg;
		}
		
		int pos = 1;
		Automaton first;
		if (s.charAt(0)=='(') 
		{
			int k=1; 
			while ((pos < s.length()) && (k>0)) 
			{
				 if (s.charAt(pos)=='(') k++; if (s.charAt(pos++)==')') k--;
			}
			if (k>0) 
				throw new RuntimeException("Brackets in String do not fit");
			first = parseString(s.substring(1,pos-1));
		} else {
			first = parseString(s.substring(0,1));
		}
		while ((pos < s.length()) && ((s.charAt(pos)=='*')||(s.charAt(pos)=='~'))) 
			{
				if(s.charAt(pos)=='*')
					first = first.star();
				if(s.charAt(pos)=='~')
					first = first.complement();
				pos++;
			}
		if (pos >= s.length()) return first;
		Automaton erg;
		if (s.charAt(pos)=='+') erg = first.unite(parseString(s.substring(pos+1)));
		else if (s.charAt(pos)=='&') {
			Automaton aa = parseString(s.substring(pos+1));
			first = first.efficientMakeDeterministic();
			first = first.makeComplete();
			first = first.makeMinimal();
			aa = aa.efficientMakeDeterministic();
			aa = aa.makeComplete();
			aa = aa.makeMinimal();
			System.out.println(first);
			System.out.println(aa);
			erg = first.intersect(aa);
			System.out.println(erg);
		}
		else erg = first.concat(parseString(s.substring(pos)));
		
		return erg.removeNonreachableStates();
	}
/**
 * Insert the method's description here.
 * Creation date: (23.02.01 13:39:27)
 * @return Automaton
 */
public Automaton removeNonreachableStates() {
	int[][] adjacency = createAdjacencyList();
	boolean[] reachable = new boolean[anzStates];

	Vector queue = new Vector();
	queue.addElement(new Integer(start));
	reachable[start]=true;	

	while(queue.size()!=0)
	{
		int state = ((Integer) queue.elementAt(queue.size()-1)).intValue();
		queue.removeElementAt(queue.size()-1);
		for(int k=0;k<letter.length;k++)
		{
			if(!reachable[adjacency[state][k]])
			{
				reachable[adjacency[state][k]] = true;
				queue.addElement(new Integer(adjacency[state][k]));
			}
		}
	}

	int[] newNumber = new int[anzStates];
	int stateCounter=0;
	
	for(int i=0;i<anzStates;i++)
	{
		if(reachable[i])
		{
			newNumber[i]=stateCounter;
			stateCounter++;
		}
		else newNumber[i]=-1;
	}

	int newStates = stateCounter;
	int newStart = newNumber[start];
	boolean[][][] newEdge = new boolean[newStates][newStates][letter.length];
	for(int i=0;i<anzStates;i++)
	for(int j=0;j<anzStates;j++)
	{
		if((reachable[i])&&(reachable[j]))
		{
			for(int k=0;k<letter.length;k++)
				newEdge[newNumber[i]][newNumber[j]][k] = edge[i][j][k];
		}
	}

	boolean[] endStates = new boolean[newStates];
	for(int i=0;i<end.length;i++)
	if(reachable[end[i]])
		endStates[newNumber[end[i]]]=true;

	int newends=0;
	for(int i=0;i<newStates;i++)
		if(endStates[i])
			newends++;

	int[] newEnd = new int[newends];
	int k=0;
	for(int i=0;i<newStates;i++)
		if(endStates[i])
		{
			newEnd[k]=i;
			k++;
		}
			
	Automaton newAutomaton = new Automaton(newStates,letter, newStart, newEnd);
	newAutomaton.edge = newEdge;
	return newAutomaton;
}
	public Automaton star()
	{
		Automaton erg = new Automaton(this);
		boolean in = false;
		for (int i=0; (!in) && (i<erg.end.length); i++)
			if (erg.end[i] == erg.start) in = true;
		if (!in)
		{
			int[] newend = new int[erg.end.length+1];
			for (int i=0; i<erg.end.length; i++)
				newend[i] = erg.end[i];
			newend[newend.length-1] = erg.start;
			erg.end = newend;
		}
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<erg.end.length; j++)
				if (erg.end[j]!=erg.start) 
					for (int k=0; k<letter.length; k++)
						erg.edge[i][start][k] |= erg.edge[i][erg.end[j]][k];
		
		return erg;
	}
	public Graph toGraph()
	{
		Automaton work = this;
		if (!isDeterministic()) work = this.makeDeterministic();
		
		int[][] erg = new int[work.anzStates][work.anzStates];
		for (int i=0; i<work.anzStates; i++)
			for (int j=0; j<work.anzStates; j++)
			{
				erg[i][j] = 0;
				for (int k=0; k<work.letter.length; k++)
					if (work.edge[i][j][k]) erg[i][j]++;
			}
		
		return new Graph(erg);
	}
	public String toString()
	{
		String erg = anzStates+" States\r\nAlphabet = {";
		for (int i=0; i<letter.length; i++) {erg += letter[i]; if (i<letter.length-1) erg += ","; }
		erg += "}\r\n";
		erg += "Startstate = S"+start+"\r\n";
		erg += "Terminating states = {";
		for (int i=0; i<end.length; i++) {erg += "S"+end[i]; if (i<end.length-1) erg += ","; }
		erg += "}\r\n";
		
		for (int i=0; i<anzStates; i++)
		{
			erg += "From S"+i+" to ";
			for (int j=0; j<anzStates; j++)
			{
				String with = "";
				for (int k=0; k<letter.length; k++) if (edge[i][j][k]) with += " "+letter[k];
				if (with.length()!=0) erg += "S"+j+" ("+with+") | ";
			}
			erg += "\r\n";
		}
		return erg;
	}
	public Automaton unite(Automaton snd)
	{
		Vector newlet = new Vector();
		for (int i=0; i<letter.length; i++)
			newlet.addElement(letter[i]);
		int[] verweis = new int[snd.letter.length];
		for (int i=0; i<snd.letter.length; i++)
		{
			int in = -1;
			for (int j=0; (in==-1) && (j<letter.length); j++)
				if (snd.letter[i].equals(letter[j])) in = j;
			if (in!=-1) verweis[i] = in;
			else {verweis[i] = newlet.size(); newlet.addElement(snd.letter[i]);}
		}
		String[] newletarr = new String[newlet.size()];
		for (int i=0; i<newletarr.length; i++)
			newletarr[i] = ((String)newlet.elementAt(i));
		int[] newend = new int[end.length + snd.end.length];
		if ((this.containsEpsilon()) || (snd.containsEpsilon()))
		{
			newend = new int[end.length + snd.end.length +1];
			newend[newend.length-1] = 0;
		}
		for (int i=0; i<end.length; i++) newend[i] = end[i]+1;
		for (int j=0; j<snd.end.length; j++) newend[end.length+j] = snd.end[j]+anzStates+1;
		
		Automaton erg = new Automaton(1+anzStates+snd.anzStates, newletarr, 0,  newend);
		for (int i=0; i<anzStates; i++)
			for (int j=0; j<anzStates; j++)
				for (int k=0; k<letter.length; k++)
					erg.edge[i+1][j+1][k] = edge[i][j][k];
		for (int i=0; i<snd.anzStates; i++)
			for (int j=0; j<snd.anzStates; j++)
				for (int k=0; k<snd.letter.length; k++)
					erg.edge[i+anzStates+1][j+anzStates+1][verweis[k]] = snd.edge[i][j][k];
		for (int i=0; i<anzStates; i++)
			for (int k=0; k<letter.length; k++)
				erg.edge[0][i+1][k] = edge[start][i][k];
		for (int i=0; i<snd.anzStates; i++)
			for (int k=0; k<snd.letter.length; k++)
				erg.edge[0][i+anzStates+1][verweis[k]] = snd.edge[snd.start][i][k];
		
		return erg;		
	}
}
