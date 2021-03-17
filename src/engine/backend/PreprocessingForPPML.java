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
