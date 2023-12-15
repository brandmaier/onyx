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
