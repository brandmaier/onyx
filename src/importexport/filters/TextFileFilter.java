package importexport.filters;

import importexport.Export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TextFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		
		String ext = Export.getExtension(f);
		return (ext.equalsIgnoreCase("txt"));
	}

	@Override
	public String getDescription() {
		return "Textfile";
	}

}
