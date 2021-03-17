package importexport.filters;

import importexport.Export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class LaTeXFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		
		String ext = Export.getExtension(f);
		return (ext.equalsIgnoreCase("tex"));
	}

	@Override
	public String getDescription() {
		return "LaTeX File";
	}

}
