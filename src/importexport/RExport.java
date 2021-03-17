package importexport;

import importexport.filters.RFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import engine.ParameterReader;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;
import gui.views.ModelView;

public abstract class RExport extends StringExport {

	public static final int LABEL = 3, TO = 2;

	public String getHeader() {return "R export";}

	public RExport(ModelView modelView) {
		this(modelView, new RFileFilter(),new String[] {"R","r"});
		useStartingValues = true;
	}

    public RExport(ModelView modelView, FileFilter fileFilter, String[] defaultExtensions)
    {
        super(modelView, fileFilter, defaultExtensions);
    }
	
    @Override
    public String getMissingDataString() {return "";}
	
	public String convert(String s)
	{
		if (s == null) return null;
		return s.replaceAll("[^A-Za-z0-9]", "_");
	}

	protected Object round(double value) {
		return Math.round(value*100.0)/100.0;
	}
	
}
