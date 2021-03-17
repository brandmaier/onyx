package importexport.filters;

import importexport.Export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SVGFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		
		String ext = Export.getExtension(f);
		return (ext=="svg");
	}

	@Override
	public String getDescription() {
		return "SVG - Vector File Format";
	}

}
