import java.util.HashMap;


public class Arguments extends HashMap<String, String>{

	static final String parameterLongPrefix = "--";
	static final String parameterShortPrefix = "-";
	
	/** possible arguments are
	 * 
	 * Long                short   Means
	 * --developer         -d      Developer mode
	 * --batch             -b      Batch mode
	 * --input-file        -f      Input File
	 * --output-file       -o      Output File
	 * --output-format 
	 * 
	 * @param args
	 * @return 
	 * @throws Exception
	 */
	
	public static Arguments parse(String[] args) 
	{
		Arguments arguments = new Arguments();
		
		for (int i=0; i < args.length; i++)
		{
			String cur = args[i];
            if (cur.equals("--developer") || cur.equals("-d")) {
                arguments.put("--developer", "");
            }
            else if (cur.equals("--batch") || cur.equals("-b")) {
                arguments.put("--batch", "");
            }
            else if (cur.equals("--input-file") || cur.equals("-i")) {
				arguments.put("--input-file",  args[i+1]);
				i=i+1;
			}
            else if (cur.equals("--output-filetype") || cur.equals("-t")) {
				arguments.put("--output-filetype", args[i+1]);
				i=i+1;
			}
            else if (cur.equals("--output-file") || cur.equals("-o")) {
				arguments.put("--output-file", args[i+1]);
				i=i+1;
			} else {
				System.err.println("I don't understand '"+args[i]+"'.");
			}
		}
		
		return arguments;
	
	}
	
    public boolean isBatch() {return(this.containsKey("--batch"));}
    public boolean isDeveloper() {return(this.containsKey("--developer"));}
}
