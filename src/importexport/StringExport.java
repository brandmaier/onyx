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
package importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import javax.swing.filechooser.FileFilter;

import gui.graph.Graph;
import gui.views.ModelView;

public abstract class StringExport extends Export {
    
    private int textWidth;
    
    private String syntax_prefix = "";
    private String syntax_postfix = "";
    
    protected String comment_symbol = "!";
    
	protected void setPostfix(String string) {
		syntax_postfix = string;
		
	}

	protected void setPrefix(String string) {
		syntax_prefix = string;
	}
    
    public String getPrefix() {
		return syntax_prefix;
	}

	public String getPostfix() {
		return syntax_postfix;
	}

	public StringExport(ModelView modelView, FileFilter fileFilter, String[] defaultExtensions)
    {
        super(modelView, fileFilter, defaultExtensions);
    }
    
    public int getTextWidth() {
        String content = createModelSpec(modelView, modelView.getName(), false);
        BufferedReader buf = new BufferedReader( new StringReader(content));
        String line = "";
        try {
        while ((line = buf.readLine()) != null) 
            textWidth = Math.max(textWidth, line.length());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return textWidth;
    }
    
	protected abstract String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames);
	
	public String getModelSpec(ModelView modelView, String modelName, boolean useUniqueNames)
	{
		return( this.getPrefix()+createModelSpec(modelView, modelName, useUniqueNames)+getPostfix());
	}
	
    public void export(File file)
    {
        String content = getModelSpec(modelView, modelView.getName(), false); 
        
        try {
            createFile(file, content);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
