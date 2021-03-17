package importexport.filters;

import importexport.Export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

// import cannot be resolved at TvO's site
//import com.sun.tools.example.debug.bdi.Utils;

public class NYXFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		
		if (f.isDirectory()) return true;
		
		String ext = Export.getExtension(f);
		if (ext == null) return false;
		return (ext.equalsIgnoreCase("nyx") || ext.equalsIgnoreCase("onyx"));
	}

	@Override
	public String getDescription() {
		return("Onyx container (*.nyx)");
	}

}
