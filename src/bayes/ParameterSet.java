package bayes;

public class ParameterSet implements Cloneable {

	double[] params;
	
	public ParameterSet(int size)
	{
		params = new double[size];
		for (int i=0; i < size; i++) params[i] = 1;
	}
	
	public Object clone() {
		try {
			ParameterSet ps = (ParameterSet)super.clone();
			ps.params = new double[params.length];
			for (int i=0; i < params.length; i++) {
				ps.params[i] = params[i];
			}
			return(ps);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return(null);
		}
	}

	public double getValue(int i) {
		return params[i];
	}
	
	public void setValue(int i, double j) 
	{
		params[i] = j;
	}

	public int size() {
		return(params.length);
	}
	
	public String toString() {
		String str=("<PSet #"+params.length+" ");
		for (int i=0; i < params.length; i++) {
			if (i!=0) str+=",";
			str+=params[i];
			
		}
		str+=">";
		return(str);
	}
	
}
