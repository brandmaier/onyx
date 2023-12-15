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
