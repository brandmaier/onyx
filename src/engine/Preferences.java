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
package engine;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Preferences {
	private static Properties userPreferences;
	
	public static Object get(String key)
	{
		return userPreferences.get(key);
	}
	
	public static String getAsString(String key)
	{
		Object result = get(key);
		if (result != null) return (result.toString());
		else return(""); 
	}
	
	public static boolean getAsBoolean(String key)
	{
		return Boolean.parseBoolean(get(key).toString());
	}
	
	// Changed 2nd argument style to String because preferences (stupid, I know)
	// only allows Strings to be put into it.
	public static void set(String key, String value) {

	    if (userPreferences == null) return;
		if (value != null) {
			userPreferences.setProperty(key, value);
		} else {
			try {
				userPreferences.remove(key);
			} catch (Exception e) {
				System.err.println("Could not delete key " + key
						+ " from properties");
			}
		}

		// save properties
		FileOutputStream out;
		try {
			out = new FileOutputStream(getSettingsFilename());
			userPreferences.store(out, "---No Comment---");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static {
		// load properties
				userPreferences = new Properties();
				userPreferences.put("RPath", "");
				userPreferences.put("DefaultWorkingPath", "");
				userPreferences.put("CheckForUpdates","true");
				userPreferences.put("ShowTipOfTheDay","true");
				userPreferences.put("BackgroundImage","");
				userPreferences.put("HoldWhenInBackground", "false");
				// now load properties
				// from last invocation
				FileInputStream in;
				try {
					in = new FileInputStream(getSettingsFilename());
					userPreferences.load(in);
					in.close();
				} catch (FileNotFoundException e) {
					// no problem. we created a new properties object
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	public static String getSettingsFilename() {
		File settingsDirectory = null;
		boolean ok = true;
	    String userHome = System.getProperty("user.home");
	    if(userHome == null) {
	        ok = false;
	    } else {
	    File home = new File(userHome);
	    settingsDirectory = new File(home, ".onyx");
	    if(!settingsDirectory.exists()) {
	        if(!settingsDirectory.mkdir()) {
	            ok = false;
	        }
	    }
	    }
	    String fullname = settingsDirectory.getAbsoluteFile().toString()+File.separator+"onyxProperties";
	    
	    try {
	    	FileInputStream fin = new FileInputStream(fullname);
	    	fin.close();
	    } catch (Exception e) {
	    	//e.printStackTrace();
	    	//TODO: tell the user that it would be great if their home directory was writable...
	    	ok = false;
	    }
//System.out.println(	 settingsDirectory.getAbsoluteFile().toString()+System.getProperty("path.separator")+"onyxProperties");
	    ok = false;
	    if (ok)
	    	return fullname;
	    else
	    	return "./onyxProperties";
	}

	public static boolean contains(String key) {
		return userPreferences.containsKey(key);
	}
}
