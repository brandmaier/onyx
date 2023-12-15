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

import gui.views.ModelView;
import importexport.filters.SVGFileFilter;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.filechooser.FileFilter;

import de.erichseifert.vectorgraphics2d.PDFGraphics2D;
import de.erichseifert.vectorgraphics2d.SVGGraphics2D;

public class SVGExport extends VectorExport {



	public SVGExport(ModelView modelView) {
		super(modelView, new SVGFileFilter(),new String[] {"svg","xml"});
	}

    public boolean isValid() {return true;}
	
	@Override
	public void export(File file) throws Exception {
		// TODO Auto-generated method stub
		
		/*FileOutputStream fileOutputStream = new FileOutputStream(file);
		PDFJob job = new PDFJob(fileOutputStream);
		Graphics pdfGraphics = job.getGraphics();
		
		modelView.paintComponent(pdfGraphics);
		
		pdfGraphics.dispose();
		job.end();*/

		SVGGraphics2D pdfg = new SVGGraphics2D(0,0,modelView.getWidth(),modelView.getHeight());

		/*
		pdfg.setFontRendering(FontRendering.VECTORS);
//		modelView.paintComponent(pdfg);

		Style.FillStyle oldStyle = modelView.getGraph().graphStyle.nodeFillGradient;
		if (oldStyle==Style.FillStyle.GRADIENT) {
			 modelView.getGraph().graphStyle.nodeFillGradient = Style.FillStyle.FILL;
		}*/
		
		exportToGraphicsContext(pdfg);

		// modelView.getGraph().graphStyle.nodeFillGradient = oldStyle;
		
		FileOutputStream files = new FileOutputStream(file);
        try {
            files.write(pdfg.getBytes());
        } finally {
            files.close();
        }
	}


}
