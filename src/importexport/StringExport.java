package importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;

import javax.swing.filechooser.FileFilter;

import gui.graph.Graph;
import gui.views.ModelView;

public abstract class StringExport extends Export {
    
    private int textWidth;
    
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
    
//    public String createModelSpec(ModelView modelView, String modelName) {return createModelSpec(modelView, modelName, false);}
	public abstract String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames);
    public void export(File file)
    {
        String content = createModelSpec(modelView, modelView.getName(), false); // exportNaive(g);
        
        try {
            createFile(file, content);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
