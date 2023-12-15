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
 * Created on 15.03.2010
 */
package engine.backend;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PreprocessingForPPML 
{
    public static void main(String[] args)
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String structureInput = "";
        if (args.length == 0) {
            System.out.println("Please insert filename for structure matrix (tab-separated, one line per observation): ");
            try {structureInput = r.readLine();} catch (Exception e) {System.out.println("Sorry, I didn't get this."); System.exit(1);}
        } else structureInput = args[0];
        String dataInput = "";
        if (args.length <= 1) {
            System.out.println("Please insert filename for data matrix (tab-separated, one line per participant): ");
            try {dataInput = r.readLine();} catch (Exception e) {System.out.println("Sorry, I didn't get this."); System.exit(1);}
        } else dataInput = args[1];
        boolean fullTransformedDataMatrix = false; String s = "";
        if (args.length <= 2) {
            System.out.println("Do you want the complete transformed data matrix (yes/no): ");
            try {
                s = r.readLine();
            } catch (Exception e) {System.out.println("Sorry, I didn't get this."); System.exit(1);}
        } else dataInput = args[1];
        fullTransformedDataMatrix = s.toLowerCase().equals("yes");
        
        // TODO continue.
    }
    
    
}
