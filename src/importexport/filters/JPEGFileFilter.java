package importexport.filters;

import importexport.Export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class JPEGFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		
		String ext = Export.getExtension(f);
		
		// TvO: null point when no extension was available. I added a check for null, which rejects.
		if (ext==null) return false;
		return (ext.equalsIgnoreCase("jpg")) || (ext.equalsIgnoreCase("jpeg"));
	}

	@Override
	public String getDescription() {
		return("JPEG");
	}

}
