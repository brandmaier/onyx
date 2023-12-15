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

//import gnu.jpdf.PDFJob;

import gui.views.ModelView;

import java.awt.Graphics;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.filechooser.FileFilter;

import de.erichseifert.vectorgraphics2d.EPSGraphics2D;
import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D.FontRendering;

public class EPSExport extends VectorExport {

	public EPSExport(ModelView modelView) {
		super(modelView, null, new String[] {"eps"});
	}
	
	public boolean isValid() {return true;}

	@Override
	public void export(File file) throws Exception {


		EPSGraphics2D pdfg = new EPSGraphics2D(0,0,modelView.getWidth(),modelView.getHeight());

		
		exportToGraphicsContext(pdfg);

	
		FileOutputStream files = new FileOutputStream(file);
        try {
            files.write(pdfg.getBytes());
        } finally {
            files.close();
        }
	}

}
