package arithmetik;

/**
 * Insert the type's description here.
 * Creation date: (16.10.2002 18:59:50)
 * @author: 
 */
public class HashableArrays 
{
	int[] arr;
/**
 * HashableArrays constructor comment.
 */
public HashableArrays() {
	super();
}
/**
 * HashableArrays constructor comment.
 */
public HashableArrays(int[] arr) 
{
	this.arr = arr;
}
/**
 * Insert the method's description here.
 * Creation date: (16.10.2002 19:01:10)
 * @return boolean
 * @param obj java.lang.Object
 */
public boolean equals(Object obj) 
{
	if (obj instanceof HashableArrays)
	{
		int[] arg = ((HashableArrays)obj).arr;
		if (arg.length==arr.length)
		{
			for (int i=0; i<arg.length; i++)
				if (arg[i]!=arr[i]) return false;
			return true;
		}
	}
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (16.10.2002 19:02:59)
 * @return int
 */
public int hashCode() 
{
	int erg = 0;
	for (int j=0; j<arr.length; j++)
	{
		erg += arr[arr.length-1-j]*Math.pow(100,j);
	}
	return erg;
}
}
